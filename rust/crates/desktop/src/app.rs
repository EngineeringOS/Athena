//! Native adapter for Athena's typed application protocol.
//!
//! The desktop owns GPUI-facing caches and native persistence requests only.
//! Electrical project state, history, validation, and property-plate callbacks
//! remain private to [`AthenaEditor`].

use std::{collections::BTreeMap, path::PathBuf};

use athena_application::{
    AthenaEditor, AthenaFrontendMessage, AthenaMessage, EditorSnapshot, LayoutTarget, OpenOutcome,
    PortfolioMessage, ResolvedTitleBlockDisplay, SaveOutcome, Widget, WorkspaceLayout,
};
use athena_domain::{FolioId, ProjectId};

use crate::storage::FileSnapshotStore;

/// GPUI-facing state derived exclusively from application frontend effects.
#[derive(Clone, Debug, Default)]
pub struct DesktopViewState {
    /// Open project's stable identity, when a project is active.
    pub project_id: Option<ProjectId>,
    /// Project display name shown in the compact title controls.
    pub project_name: Option<String>,
    /// Folio currently presented by the viewport and properties plate.
    pub active_folio_id: Option<FolioId>,
    /// Ordered project/folio outline supplied by the application.
    pub outline: Vec<(FolioId, String)>,
    /// Backend-owned panel hierarchy and visibility state.
    pub workspace: WorkspaceLayout,
    /// Whether the open document differs from its save checkpoint.
    pub dirty: bool,
    /// Last platform or application status suitable for the status bar.
    pub status: Option<String>,
    plates: BTreeMap<LayoutTarget, Vec<Widget>>,
    resolved_title_blocks: BTreeMap<FolioId, ResolvedTitleBlockDisplay>,
    pending_save: Option<AthenaFrontendMessage>,
    open_dialog_pending: bool,
}

impl DesktopViewState {
    /// Returns the Rust-owned widgets for a project or folio plate.
    #[must_use]
    pub fn widgets(&self, target: LayoutTarget) -> &[Widget] {
        self.plates.get(&target).map(Vec::as_slice).unwrap_or(&[])
    }

    /// Returns application-resolved title-block display data for one folio.
    #[must_use]
    pub fn resolved_title_block(&self, folio_id: FolioId) -> Option<&ResolvedTitleBlockDisplay> {
        self.resolved_title_blocks.get(&folio_id)
    }

    /// Reports whether GPUI must resolve an emitted native save request.
    #[must_use]
    pub fn save_dialog_pending(&self) -> bool {
        self.pending_save.is_some()
    }

    /// Reports whether GPUI must resolve an emitted native open request.
    #[must_use]
    pub const fn open_dialog_pending(&self) -> bool {
        self.open_dialog_pending
    }
}

/// Thin desktop facade containing the shared application and adapter-only view state.
#[derive(Default)]
pub struct DesktopEditor {
    application: AthenaEditor,
    view: DesktopViewState,
    recent_project_path: Option<PathBuf>,
}

impl DesktopEditor {
    /// Dispatches one typed user action and reduces every ordered frontend effect.
    pub fn dispatch(&mut self, message: impl Into<AthenaMessage>) -> Vec<AthenaFrontendMessage> {
        let effects = self.application.handle_message(message);
        for effect in &effects {
            self.reduce_effect(effect);
        }
        effects
    }

    /// Returns the current adapter-owned presentation cache.
    #[must_use]
    pub const fn view_state(&self) -> &DesktopViewState {
        &self.view
    }

    /// Returns an immutable application snapshot for rendering diagnostics and tests.
    #[must_use]
    pub fn state_snapshot(&self) -> Option<EditorSnapshot> {
        self.application.state_snapshot()
    }

    /// Completes the active native save dialog and reports its result through a typed message.
    pub fn complete_save_dialog(&mut self, path: Option<PathBuf>) -> Result<(), String> {
        let Some(AthenaFrontendMessage::SaveRequested {
            request_id,
            project_id,
            revision,
            bytes,
        }) = self.view.pending_save.take()
        else {
            return Err("No desktop save request is pending".into());
        };

        let (outcome, result, saved_name, saved_path) = match path {
            None => (SaveOutcome::Cancelled, Ok(()), None, None),
            Some(path) => {
                let saved_name = path
                    .file_name()
                    .map(|name| name.to_string_lossy().into_owned());
                match FileSnapshotStore::new(&path).write(&bytes) {
                    Ok(()) => (SaveOutcome::Success, Ok(()), saved_name, Some(path)),
                    Err(error) => {
                        let message = error.to_string();
                        (
                            SaveOutcome::Failed {
                                message: message.clone(),
                            },
                            Err(message),
                            saved_name,
                            None,
                        )
                    }
                }
            }
        };

        self.dispatch(PortfolioMessage::SaveResult {
            request_id,
            project_id,
            revision,
            outcome,
        });
        if result.is_ok()
            && let Some(name) = saved_name
        {
            self.view.status = Some(format!("Saved {name}"));
        }
        if let Some(path) = saved_path {
            self.recent_project_path = Some(path);
        }
        result
    }

    /// Completes the active native open dialog and supplies bytes through a typed message.
    pub fn complete_open_dialog(&mut self, path: Option<PathBuf>) -> Result<(), String> {
        if !self.view.open_dialog_pending {
            return Err("No desktop open request is pending".into());
        }
        let Some(path) = path else {
            self.dispatch(PortfolioMessage::OpenResult {
                outcome: OpenOutcome::Cancelled,
            });
            return Ok(());
        };
        let name = path
            .file_name()
            .map(|name| name.to_string_lossy().into_owned());
        let bytes = match FileSnapshotStore::new(&path).read() {
            Ok(bytes) => bytes,
            Err(error) => {
                let message = error.to_string();
                self.dispatch(PortfolioMessage::OpenResult {
                    outcome: OpenOutcome::Failed {
                        message: message.clone(),
                    },
                });
                return Err(message);
            }
        };
        let effects = self.dispatch(PortfolioMessage::OpenResult {
            outcome: OpenOutcome::Success { bytes },
        });
        if effects
            .iter()
            .any(|effect| matches!(effect, AthenaFrontendMessage::Error { .. }))
        {
            return Err(self
                .view
                .status
                .clone()
                .unwrap_or_else(|| "Open failed".into()));
        }
        if let Some(name) = name {
            self.view.status = Some(format!("Opened {name}"));
        }
        self.recent_project_path = Some(path);
        Ok(())
    }

    /// Reopens the last successfully saved or opened project through typed open messages.
    pub fn reopen_recent_project(&mut self) -> Result<(), String> {
        let Some(path) = self.recent_project_path.clone() else {
            self.view.status = Some("No recent project is available".into());
            return Err("No recent project is available".into());
        };
        self.dispatch(PortfolioMessage::RequestOpen);
        self.complete_open_dialog(Some(path))
    }

    fn reduce_effect(&mut self, effect: &AthenaFrontendMessage) {
        match effect {
            AthenaFrontendMessage::ProjectOpened { project_id, name } => {
                self.view.open_dialog_pending = false;
                self.view.project_id = Some(*project_id);
                self.view.project_name = Some(name.clone());
                self.view.status = Some(format!("Project open: {name}"));
            }
            AthenaFrontendMessage::ProjectClosed => {
                let workspace = self.view.workspace.clone();
                self.view = DesktopViewState {
                    workspace,
                    status: Some("Project closed".into()),
                    ..DesktopViewState::default()
                };
            }
            AthenaFrontendMessage::ActiveFolioChanged { folio_id } => {
                self.view.active_folio_id = Some(*folio_id);
            }
            AthenaFrontendMessage::DirtyStateChanged { dirty } => self.view.dirty = *dirty,
            AthenaFrontendMessage::OutlineChanged {
                project_name,
                folios,
            } => {
                self.view.project_name = Some(project_name.clone());
                self.view.outline.clone_from(folios);
            }
            AthenaFrontendMessage::WorkspaceLayoutUpdated(workspace) => {
                self.view.workspace = workspace.clone();
            }
            AthenaFrontendMessage::PanelLayoutUpdated { target, widgets } => {
                self.view.plates.insert(*target, widgets.clone());
            }
            AthenaFrontendMessage::WidgetValuesUpdated { target, values } => {
                if let Some(widgets) = self.view.plates.get_mut(target) {
                    for (id, value) in values {
                        if let Some(widget) = widgets.iter_mut().find(|widget| widget.id == *id) {
                            widget.value = value.clone();
                        }
                    }
                }
            }
            AthenaFrontendMessage::ResolvedTitleBlockUpdated { folio_id, display } => {
                self.view
                    .resolved_title_blocks
                    .insert(*folio_id, display.as_ref().clone());
            }
            effect @ AthenaFrontendMessage::SaveRequested { .. } => {
                self.view.pending_save = Some(effect.clone());
                self.view.status = Some("Choose a project save location".into());
            }
            AthenaFrontendMessage::OpenRequested => {
                self.view.open_dialog_pending = true;
                self.view.status = Some("Choose an Athena project to open".into());
            }
            AthenaFrontendMessage::Diagnostic { code, message } => {
                self.view.status = Some(match code.as_str() {
                    "save-failed" => format!("Save failed: {message}"),
                    "save-cancelled" => "Save cancelled".into(),
                    "open-failed" => {
                        self.view.open_dialog_pending = false;
                        format!("Open failed: {message}")
                    }
                    "open-cancelled" => {
                        self.view.open_dialog_pending = false;
                        "Open cancelled".into()
                    }
                    _ => message.clone(),
                });
            }
            AthenaFrontendMessage::Error { message } => {
                self.view.open_dialog_pending = false;
                self.view.status = Some(format!("Error: {message}"));
            }
        }
    }
}

/// Launches the native GPUI workbench.
pub fn run() {
    crate::panels::run_native_shell();
}

#[cfg(test)]
mod tests {
    use athena_application::{LayoutTarget, PortfolioMessage, WidgetId, WidgetValue};
    use athena_domain::ProjectId;

    use super::DesktopEditor;

    #[test]
    fn value_only_effects_update_cached_widgets_in_place() {
        let mut desktop = DesktopEditor::default();
        desktop.dispatch(PortfolioMessage::CreateProject {
            project_id: ProjectId::new(),
            name: "Initial".into(),
        });
        desktop.dispatch(athena_application::LayoutMessage::RequestProjectPlate);
        desktop.dispatch(athena_application::LayoutMessage::CommitWidget {
            target: LayoutTarget::Project,
            widget_id: WidgetId::new("project.name"),
            value: WidgetValue::Text("Updated".into()),
        });

        assert!(
            desktop
                .view_state()
                .widgets(LayoutTarget::Project)
                .iter()
                .any(|widget| widget.id == WidgetId::new("project.name")
                    && widget.value == WidgetValue::Text("Updated".into()))
        );
    }
}
