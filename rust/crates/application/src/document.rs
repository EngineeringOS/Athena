//! Active document handler translating messages into editor transactions.

use athena_domain::{Folio, FolioId, Project, TemplateText, TitleBlockPlacement};
use athena_editor::{
    DocumentRevision, EditorCommand, EditorSession, SaveSnapshot, SessionError, TitleBlockField,
    TitleBlockValue,
};

use crate::{AthenaFrontendMessage, AthenaMessage, DocumentMessage, EditorSnapshot, LayoutMessage};

pub(crate) struct DocumentOutput {
    pub effects: Vec<AthenaFrontendMessage>,
    pub messages: Vec<AthenaMessage>,
}

/// Owns one active project's editor session and document history.
pub struct DocumentHandler {
    session: EditorSession,
}

impl DocumentHandler {
    pub(crate) fn new(project: Project, active_folio_id: FolioId) -> Self {
        Self {
            session: EditorSession::new(project, active_folio_id),
        }
    }

    pub(crate) fn snapshot(&self) -> EditorSnapshot {
        EditorSnapshot {
            project: self.session.state().project().clone(),
            active_folio_id: self.session.active_folio_id(),
            revision: self.session.document_revision(),
            dirty: self.session.is_dirty(),
            save_pending: self.session.save_is_pending(),
        }
    }

    pub(crate) fn begin_save(&mut self) -> Result<SaveSnapshot, SessionError> {
        self.session.begin_save()
    }
    pub(crate) fn complete_save(&mut self, revision: DocumentRevision) -> bool {
        self.session.complete_save(revision)
    }
    pub(crate) fn cancel_save(&mut self, revision: DocumentRevision) -> bool {
        self.session.cancel_save(revision)
    }
    pub(crate) fn fail_save(&mut self, revision: DocumentRevision) -> bool {
        self.session.fail_save(revision)
    }

    pub(crate) fn handle(&mut self, message: DocumentMessage) -> DocumentOutput {
        let active_before = self.session.active_folio_id();
        let result = self.apply_message(message);
        match result {
            Ok(changed) => {
                let snapshot = self.snapshot();
                let mut effects = Vec::new();
                if changed {
                    effects.push(outline_effect(&snapshot));
                    effects.push(AthenaFrontendMessage::DirtyStateChanged {
                        dirty: snapshot.dirty,
                    });
                }
                let mut messages = Vec::new();
                if changed {
                    messages.push(LayoutMessage::RequestProjectPlate.into());
                    messages.push(
                        LayoutMessage::RequestFolioPlate {
                            folio_id: snapshot.active_folio_id,
                        }
                        .into(),
                    );
                } else if self.session.active_folio_id() != active_before {
                    messages.push(
                        LayoutMessage::RequestFolioPlate {
                            folio_id: self.session.active_folio_id(),
                        }
                        .into(),
                    );
                }
                DocumentOutput { effects, messages }
            }
            Err(error) => DocumentOutput {
                effects: vec![AthenaFrontendMessage::Diagnostic {
                    code: "document-command".into(),
                    message: error.to_string(),
                }],
                messages: Vec::new(),
            },
        }
    }

    fn apply_message(&mut self, message: DocumentMessage) -> Result<bool, SessionError> {
        match message {
            DocumentMessage::AddFolio { folio_id, label } => {
                let project = self.session.state().project();
                let mut folio = Folio::new(
                    label,
                    project.folio_defaults.title_block.clone(),
                    project.folio_defaults.variables.clone(),
                );
                folio.id = folio_id;
                self.session.apply_command(EditorCommand::AddFolio {
                    folio,
                    position: project.folio_order().len(),
                })?;
            }
            DocumentMessage::ActivateFolio { folio_id } => {
                self.session.activate_folio(folio_id)?;
                return Ok(false);
            }
            DocumentMessage::MoveFolio { folio_id, to } => {
                let from = self
                    .session
                    .state()
                    .project()
                    .folio_order()
                    .iter()
                    .position(|id| *id == folio_id)
                    .unwrap_or(usize::MAX);
                self.session
                    .apply_command(EditorCommand::MoveFolio { folio_id, from, to })?;
            }
            DocumentMessage::RenameProject { name } => {
                let old_name = self.session.state().project().name.clone();
                self.session.apply_command(EditorCommand::RenameProject {
                    old_name,
                    new_name: name,
                })?;
            }
            DocumentMessage::RenameFolio { folio_id, label } => {
                let old_label = self
                    .session
                    .state()
                    .project()
                    .folio(folio_id)
                    .ok_or(SessionError::UnknownFolio)?
                    .label
                    .clone();
                self.session.apply_command(EditorCommand::RenameFolio {
                    folio_id,
                    old_label,
                    new_label: label,
                })?;
            }
            DocumentMessage::SetProjectVariable { key, value } => {
                let old_value = self.session.state().project().variables.get(&key).cloned();
                self.session
                    .apply_command(EditorCommand::SetProjectVariable {
                        key,
                        old_value,
                        new_value: value,
                    })?;
            }
            DocumentMessage::SetFolioVariable {
                folio_id,
                key,
                value,
            } => {
                let old_value = self
                    .session
                    .state()
                    .project()
                    .folio(folio_id)
                    .ok_or(SessionError::UnknownFolio)?
                    .variables
                    .get(&key)
                    .cloned();
                self.session
                    .apply_command(EditorCommand::SetFolioVariable {
                        folio_id,
                        key,
                        old_value,
                        new_value: value,
                    })?;
            }
            DocumentMessage::SetTitleBlockValue {
                folio_id,
                field,
                value,
            } => {
                let old_value =
                    current_title_block_value(self.session.state().project(), folio_id, field)?;
                self.session
                    .apply_command(EditorCommand::SetTitleBlockValue {
                        folio_id,
                        field,
                        old_value,
                        new_value: value,
                    })?;
            }
            DocumentMessage::SetDefaultPlacement { placement } => {
                self.update_defaults(None, Some(placement), None)?
            }
            DocumentMessage::SetDefaultTemplate { template_id } => {
                self.update_defaults(Some(template_id), None, None)?
            }
            DocumentMessage::SetDefaultPageNumber { value } => {
                self.update_defaults(None, None, Some(value))?
            }
            DocumentMessage::Undo => return self.session.undo(),
            DocumentMessage::Redo => return self.session.redo(),
        }
        Ok(true)
    }

    fn update_defaults(
        &mut self,
        template_id: Option<String>,
        placement: Option<TitleBlockPlacement>,
        page_number: Option<TemplateText>,
    ) -> Result<(), SessionError> {
        let old_defaults = self.session.state().project().folio_defaults.clone();
        let mut new_defaults = old_defaults.clone();
        if let Some(template_id) = template_id {
            new_defaults.title_block.template_id = template_id;
        }
        if let Some(placement) = placement {
            new_defaults.title_block.placement = placement;
        }
        if let Some(page_number) = page_number {
            new_defaults.title_block.page_number = page_number;
        }
        self.session
            .apply_command(EditorCommand::UpdateFolioDefaults {
                old_defaults,
                new_defaults,
            })
    }
}

pub(crate) fn outline_effect(snapshot: &EditorSnapshot) -> AthenaFrontendMessage {
    AthenaFrontendMessage::OutlineChanged {
        project_name: snapshot.project.name.clone(),
        folios: snapshot
            .project
            .folio_order()
            .iter()
            .filter_map(|id| {
                snapshot
                    .project
                    .folio(*id)
                    .map(|folio| (*id, folio.label.clone()))
            })
            .collect(),
    }
}

fn current_title_block_value(
    project: &Project,
    folio_id: FolioId,
    field: TitleBlockField,
) -> Result<TitleBlockValue, SessionError> {
    let values = &project
        .folio(folio_id)
        .ok_or(SessionError::UnknownFolio)?
        .title_block;
    Ok(match field {
        TitleBlockField::TemplateId => TitleBlockValue::Text(values.template_id.clone()),
        TitleBlockField::Placement => TitleBlockValue::Placement(values.placement),
        TitleBlockField::Title => TitleBlockValue::Template(values.title.clone()),
        TitleBlockField::Author => TitleBlockValue::Template(values.author.clone()),
        TitleBlockField::DateText => TitleBlockValue::Template(values.date_text.clone()),
        TitleBlockField::FileLabel => TitleBlockValue::Template(values.file_label.clone()),
        TitleBlockField::FolioLabel => TitleBlockValue::Template(values.folio_label.clone()),
        TitleBlockField::Plant => TitleBlockValue::Template(values.plant.clone()),
        TitleBlockField::Location => TitleBlockValue::Template(values.location.clone()),
        TitleBlockField::Revision => TitleBlockValue::Template(values.revision.clone()),
        TitleBlockField::PageNumber => TitleBlockValue::Template(values.page_number.clone()),
    })
}
