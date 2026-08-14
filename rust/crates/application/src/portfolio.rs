//! Project lifetime, open/save coordination, and active document ownership.

use athena_domain::{Project, ProjectId};
use athena_editor::DocumentRevision;
use athena_format::SnapshotStore;
use serde::{Deserialize, Serialize};

use crate::{
    AthenaFrontendMessage, AthenaMessage, DocumentHandler, DocumentMessage, EditorSnapshot,
    LayoutMessage, PortfolioMessage, SaveOutcome, outline_effect,
};

/// Deterministic save request identity allocated by the application layer.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub struct SaveRequestId(pub u64);

struct ActiveSave {
    request_id: SaveRequestId,
    project_id: ProjectId,
    revision: DocumentRevision,
}

pub(crate) struct PortfolioOutput {
    pub effects: Vec<AthenaFrontendMessage>,
    pub messages: Vec<AthenaMessage>,
}

/// Owns project lifetime and delegates active-project edits to `DocumentHandler`.
#[derive(Default)]
pub struct PortfolioHandler {
    document: Option<DocumentHandler>,
    active_save: Option<ActiveSave>,
    next_save_request: u64,
}

impl PortfolioHandler {
    pub(crate) fn snapshot(&self) -> Option<EditorSnapshot> {
        self.document.as_ref().map(DocumentHandler::snapshot)
    }

    pub(crate) fn handle_document(&mut self, message: DocumentMessage) -> PortfolioOutput {
        match self.document.as_mut() {
            Some(document) => {
                let active_before = document.snapshot().active_folio_id;
                let output = document.handle(message);
                let active_after = document.snapshot().active_folio_id;
                let mut effects = output.effects;
                if active_after != active_before {
                    effects.insert(
                        0,
                        AthenaFrontendMessage::ActiveFolioChanged {
                            folio_id: active_after,
                        },
                    );
                }
                PortfolioOutput {
                    effects,
                    messages: output.messages,
                }
            }
            None => PortfolioOutput {
                effects: vec![AthenaFrontendMessage::Error {
                    message: "No project is open".into(),
                }],
                messages: Vec::new(),
            },
        }
    }

    pub(crate) fn handle(&mut self, message: PortfolioMessage) -> PortfolioOutput {
        match message {
            PortfolioMessage::CreateProject { project_id, name } => {
                let mut project = Project::new(name);
                project.id = project_id;
                let active_folio_id = project.folio_order()[0];
                self.document = Some(DocumentHandler::new(project, active_folio_id));
                self.active_save = None;
                let snapshot = self.snapshot().expect("created document");
                PortfolioOutput {
                    effects: vec![
                        AthenaFrontendMessage::ProjectOpened {
                            project_id,
                            name: snapshot.project.name.clone(),
                        },
                        AthenaFrontendMessage::ActiveFolioChanged {
                            folio_id: active_folio_id,
                        },
                        outline_effect(&snapshot),
                        AthenaFrontendMessage::DirtyStateChanged { dirty: false },
                    ],
                    messages: vec![
                        LayoutMessage::RequestWorkspace.into(),
                        LayoutMessage::RequestFolioPlate {
                            folio_id: active_folio_id,
                        }
                        .into(),
                    ],
                }
            }
            PortfolioMessage::RequestOpen => PortfolioOutput {
                effects: vec![AthenaFrontendMessage::OpenRequested],
                messages: Vec::new(),
            },
            PortfolioMessage::OpenBytes { bytes } => match SnapshotStore::decode_snapshot(&bytes) {
                Ok(persisted) => {
                    let project_id = persisted.project.id;
                    let name = persisted.project.name.clone();
                    let active_folio_id = persisted.active_folio_id;
                    self.document = Some(DocumentHandler::new(persisted.project, active_folio_id));
                    self.active_save = None;
                    let snapshot = self.snapshot().expect("opened document");
                    PortfolioOutput {
                        effects: vec![
                            AthenaFrontendMessage::ProjectOpened { project_id, name },
                            AthenaFrontendMessage::ActiveFolioChanged {
                                folio_id: active_folio_id,
                            },
                            outline_effect(&snapshot),
                            AthenaFrontendMessage::DirtyStateChanged { dirty: false },
                        ],
                        messages: vec![
                            LayoutMessage::RequestWorkspace.into(),
                            LayoutMessage::RequestFolioPlate {
                                folio_id: active_folio_id,
                            }
                            .into(),
                        ],
                    }
                }
                Err(error) => PortfolioOutput {
                    effects: vec![AthenaFrontendMessage::Error {
                        message: error.to_string(),
                    }],
                    messages: Vec::new(),
                },
            },
            PortfolioMessage::RequestSave => self.request_save(),
            PortfolioMessage::SaveResult {
                request_id,
                project_id,
                revision,
                outcome,
            } => self.save_result(request_id, project_id, revision, outcome),
            PortfolioMessage::CloseProject => {
                self.document = None;
                self.active_save = None;
                PortfolioOutput {
                    effects: vec![AthenaFrontendMessage::ProjectClosed],
                    messages: Vec::new(),
                }
            }
        }
    }

    fn request_save(&mut self) -> PortfolioOutput {
        if self.active_save.is_some() {
            return diagnostic("save-pending", "A save request is already active");
        }
        let Some(document) = self.document.as_mut() else {
            return error("No project is open");
        };
        match document.begin_save() {
            Ok(snapshot) => {
                self.next_save_request += 1;
                let request_id = SaveRequestId(self.next_save_request);
                let project_id = document.snapshot().project.id;
                self.active_save = Some(ActiveSave {
                    request_id,
                    project_id,
                    revision: snapshot.revision,
                });
                PortfolioOutput {
                    effects: vec![AthenaFrontendMessage::SaveRequested {
                        request_id,
                        project_id,
                        revision: snapshot.revision,
                        bytes: snapshot.bytes,
                    }],
                    messages: Vec::new(),
                }
            }
            Err(error) => diagnostic("save-request", &error.to_string()),
        }
    }

    fn save_result(
        &mut self,
        request_id: SaveRequestId,
        project_id: ProjectId,
        revision: DocumentRevision,
        outcome: SaveOutcome,
    ) -> PortfolioOutput {
        let Some(active) = self.active_save.as_ref() else {
            return diagnostic("stale-save", "No save request is active");
        };
        if active.request_id != request_id
            || active.project_id != project_id
            || active.revision != revision
        {
            return diagnostic(
                "stale-save",
                "Save result does not match the active request",
            );
        }
        let document = self.document.as_mut().expect("active save has document");
        let effects = match outcome {
            SaveOutcome::Success => {
                document.complete_save(revision);
                vec![AthenaFrontendMessage::DirtyStateChanged {
                    dirty: document.snapshot().dirty,
                }]
            }
            SaveOutcome::Cancelled => {
                document.cancel_save(revision);
                vec![AthenaFrontendMessage::Diagnostic {
                    code: "save-cancelled".into(),
                    message: "Save was cancelled".into(),
                }]
            }
            SaveOutcome::Failed { message } => {
                document.fail_save(revision);
                vec![AthenaFrontendMessage::Diagnostic {
                    code: "save-failed".into(),
                    message,
                }]
            }
        };
        self.active_save = None;
        PortfolioOutput {
            effects,
            messages: Vec::new(),
        }
    }
}

fn diagnostic(code: &str, message: &str) -> PortfolioOutput {
    PortfolioOutput {
        effects: vec![AthenaFrontendMessage::Diagnostic {
            code: code.into(),
            message: message.into(),
        }],
        messages: Vec::new(),
    }
}
fn error(message: &str) -> PortfolioOutput {
    PortfolioOutput {
        effects: vec![AthenaFrontendMessage::Error {
            message: message.into(),
        }],
        messages: Vec::new(),
    }
}
