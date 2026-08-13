//! Shell-facing controller combining document state, history, presentation,
//! selection, and transient pointer interaction into one shared API.

use athena_domain::{Project, SheetId, SymbolInstanceId};
use athena_geometry::WorldPoint;
use athena_render::{
    EditorPresentation, HitRegion, PresentationItemId, Scene, hit_test, project_sheet,
};
use thiserror::Error;

use crate::{
    DebugMarqueeMode, DragSelectionState, EditorCommand, EditorState, History, HistoryError,
    InteractionState, MarqueeState, PointerModifiers, SelectionState,
};

#[derive(Debug, Error)]
pub enum SessionError {
    #[error("active sheet does not exist")]
    UnknownSheet,
    #[error(transparent)]
    History(#[from] HistoryError),
}

pub struct EditorSession {
    state: EditorState,
    history: History,
    active_sheet_id: SheetId,
    presentation: EditorPresentation,
    selection: SelectionState,
    interaction: InteractionState,
}

impl EditorSession {
    /// Creates a session over a project and active sheet.
    #[must_use]
    pub fn new(project: Project, active_sheet_id: SheetId) -> Self {
        Self {
            state: EditorState::new(project),
            history: History::new(64),
            active_sheet_id,
            presentation: EditorPresentation::default(),
            selection: SelectionState::default(),
            interaction: InteractionState::Idle,
        }
    }

    /// Returns the persistent editor state.
    #[must_use]
    pub fn state(&self) -> &EditorState {
        &self.state
    }
    /// Returns the active sheet identity.
    #[must_use]
    pub const fn active_sheet_id(&self) -> SheetId {
        self.active_sheet_id
    }
    /// Returns presentation-only scene inputs.
    #[must_use]
    pub fn presentation(&self) -> &EditorPresentation {
        &self.presentation
    }
    /// Returns the current transient interaction mode.
    #[must_use]
    pub fn interaction(&self) -> InteractionState {
        self.interaction
    }
    /// Reports whether a symbol is selected.
    #[must_use]
    pub fn is_symbol_selected(&self, id: SymbolInstanceId) -> bool {
        self.selection
            .items()
            .contains(&PresentationItemId::Symbol(id))
    }

    /// Synchronizes presentation selection with the current transient state.
    pub fn refresh_scene(&mut self) -> Result<(), SessionError> {
        if project_sheet(
            &self.state.project(),
            self.active_sheet_id,
            &self.presentation,
        )
        .is_none()
        {
            return Err(SessionError::UnknownSheet);
        }
        self.presentation.selected = self.selection.items().clone();
        Ok(())
    }

    /// Test-facing alias for refreshing the projected scene.
    pub fn refresh_scene_for_test(&mut self) -> Result<(), SessionError> {
        self.refresh_scene()
    }

    /// Projects the active sheet into a deterministic render scene.
    pub fn scene(&self) -> Result<Scene, SessionError> {
        project_sheet(
            &self.state.project(),
            self.active_sheet_id,
            &self.presentation,
        )
        .ok_or(SessionError::UnknownSheet)
    }

    /// Applies one persistent command through the shared history boundary.
    pub fn apply_command(&mut self, command: EditorCommand) -> Result<(), SessionError> {
        self.history.apply(&mut self.state, command)?;
        self.refresh_scene()
    }

    /// Selects exactly one symbol.
    pub fn select_only_symbol(&mut self, id: SymbolInstanceId) -> Result<(), SessionError> {
        self.selection.replace(PresentationItemId::Symbol(id));
        self.refresh_scene()
    }

    /// Starts a pointer gesture and resolves the topmost hit target.
    pub fn pointer_down(
        &mut self,
        point: WorldPoint,
        modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        let scene = self.scene()?;
        if let Some(item) = hit_test(&scene, point, 6.0).and_then(item_from_hit) {
            if modifiers.shift {
                self.selection.toggle(item);
            } else {
                self.selection.replace(item);
            }
            self.interaction = InteractionState::DraggingSelection(DragSelectionState {
                start: point,
                current: point,
            });
        } else {
            if !modifiers.shift {
                self.selection.clear();
            }
            self.interaction = InteractionState::MarqueeSelecting(MarqueeState {
                start: point,
                current: point,
                mode: DebugMarqueeMode::Enclosed,
            });
        }
        self.refresh_scene()
    }

    /// Updates the active pointer gesture.
    pub fn pointer_move(
        &mut self,
        point: WorldPoint,
        _modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        self.interaction = match self.interaction {
            InteractionState::MarqueeSelecting(state) => {
                InteractionState::MarqueeSelecting(MarqueeState {
                    mode: if point.x >= state.start.x {
                        DebugMarqueeMode::Enclosed
                    } else {
                        DebugMarqueeMode::Touched
                    },
                    current: point,
                    ..state
                })
            }
            InteractionState::DraggingSelection(state) => {
                InteractionState::DraggingSelection(DragSelectionState {
                    current: point,
                    ..state
                })
            }
            other => other,
        };
        Ok(())
    }

    /// Completes the active pointer gesture.
    pub fn pointer_up(
        &mut self,
        point: WorldPoint,
        _modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        self.pointer_move(point, PointerModifiers::default())?;
        self.interaction = InteractionState::Idle;
        self.refresh_scene()
    }

    #[must_use]
    /// Exposes the current directional marquee rule for focused contracts.
    pub fn debug_marquee_mode(&self) -> Option<DebugMarqueeMode> {
        match self.interaction {
            InteractionState::MarqueeSelecting(state) => Some(state.mode),
            _ => None,
        }
    }

    /// Deletes all currently selected persistent items.
    pub fn delete_selection(&mut self) -> Result<(), SessionError> {
        let items = self
            .selection
            .items()
            .iter()
            .copied()
            .map(item_to_command)
            .collect::<Vec<_>>();
        if !items.is_empty() {
            self.apply_command(EditorCommand::DeleteItems {
                sheet_id: self.active_sheet_id,
                items,
            })?;
            self.selection.clear();
            self.refresh_scene()?;
        }
        Ok(())
    }

    /// Rotates the selected items by one quarter turn.
    pub fn rotate_selection_90(&mut self) -> Result<(), SessionError> {
        self.transform_selection(EditorCommand::RotateItems {
            sheet_id: self.active_sheet_id,
            items: self.command_selection(),
            quarter_turns: 1,
        })
    }
    /// Mirrors the selected items.
    pub fn mirror_selection(&mut self) -> Result<(), SessionError> {
        self.transform_selection(EditorCommand::MirrorItems {
            sheet_id: self.active_sheet_id,
            items: self.command_selection(),
        })
    }
    fn transform_selection(&mut self, command: EditorCommand) -> Result<(), SessionError> {
        if !self.selection.items().is_empty() {
            self.apply_command(command)?;
        }
        Ok(())
    }
    fn command_selection(&self) -> Vec<crate::ItemId> {
        self.selection
            .items()
            .iter()
            .copied()
            .map(item_to_command)
            .collect()
    }
    /// Undoes the latest persistent command.
    pub fn undo(&mut self) -> Result<bool, SessionError> {
        let changed = self.history.undo(&mut self.state)?;
        self.refresh_scene()?;
        Ok(changed)
    }
    /// Redoes the latest undone persistent command.
    pub fn redo(&mut self) -> Result<bool, SessionError> {
        let changed = self.history.redo(&mut self.state)?;
        self.refresh_scene()?;
        Ok(changed)
    }
}

fn item_from_hit(hit: HitRegion) -> Option<PresentationItemId> {
    match hit {
        HitRegion::SymbolBody { symbol_id, .. } => Some(PresentationItemId::Symbol(symbol_id)),
        HitRegion::WireSegment { wire_id, .. } | HitRegion::WireVertex { wire_id, .. } => {
            Some(PresentationItemId::Wire(wire_id))
        }
        HitRegion::Junction { junction_id, .. } => Some(PresentationItemId::Junction(junction_id)),
        HitRegion::Annotation { annotation_id, .. } => {
            Some(PresentationItemId::Annotation(annotation_id))
        }
        HitRegion::Terminal { symbol_id, .. } => Some(PresentationItemId::Symbol(symbol_id)),
    }
}
fn item_to_command(item: PresentationItemId) -> crate::ItemId {
    match item {
        PresentationItemId::Symbol(id) => crate::ItemId::Symbol(id),
        PresentationItemId::Wire(id) => crate::ItemId::Wire(id),
        PresentationItemId::Junction(id) => crate::ItemId::Junction(id),
        PresentationItemId::Annotation(id) => crate::ItemId::Annotation(id),
    }
}
