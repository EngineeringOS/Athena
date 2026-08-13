//! Shell-facing controller combining document state, history, presentation,
//! selection, and transient pointer interaction into one shared API.

use athena_domain::{Project, SheetId, SymbolInstanceId};
use athena_geometry::{Rect, WorldPoint};
use athena_render::{
    EditorPresentation, HitRegion, PresentationItemId, Scene, hit_test, project_sheet,
};
use thiserror::Error;

use crate::{
    DragSelectionState, EditorCommand, EditorState, History, HistoryError, InteractionState,
    MarqueeSelectionMode, MarqueeState, PointerModifiers, PresentationPointer, SelectionState,
};

/// Errors produced by the shell-facing editor-session contract.
#[derive(Debug, Error)]
pub enum SessionError {
    #[error("active sheet does not exist")]
    UnknownSheet,
    #[error(transparent)]
    History(#[from] HistoryError),
}

/// The shared controller through which desktop and browser shells edit a sheet.
///
/// It owns persistent state and history alongside non-persistent selection,
/// presentation, and interaction state so shells do not duplicate editor logic.
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
    /// Replaces the non-persistent canvas viewport used for scene projection.
    pub fn set_viewport(&mut self, viewport: athena_render::Viewport) {
        self.presentation.viewport = viewport;
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
            self.state.project(),
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

    /// Projects the active sheet into a deterministic render scene.
    pub fn scene(&self) -> Result<Scene, SessionError> {
        project_sheet(
            self.state.project(),
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
        canvas_pointer: PresentationPointer,
        modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        let scene = self.scene()?;
        let canvas_point = canvas_pointer.position();
        if let Some(item) = hit_test(&scene, canvas_point, 6.0).and_then(item_from_hit) {
            if modifiers.shift {
                self.selection.toggle(item);
            } else {
                self.selection.replace(item);
            }
            self.interaction = InteractionState::DraggingSelection(DragSelectionState {
                start: canvas_point,
                current: canvas_point,
            });
        } else {
            if !modifiers.shift {
                self.selection.clear();
            }
            self.interaction = InteractionState::MarqueeSelecting(MarqueeState {
                start: canvas_point,
                current: canvas_point,
                mode: MarqueeSelectionMode::Enclosed,
                shift: modifiers.shift,
            });
        }
        self.refresh_scene()
    }

    /// Updates the active pointer gesture.
    pub fn pointer_move(
        &mut self,
        canvas_pointer: PresentationPointer,
        _modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        let canvas_point = canvas_pointer.position();
        self.interaction = match self.interaction {
            InteractionState::MarqueeSelecting(state) => {
                InteractionState::MarqueeSelecting(MarqueeState {
                    mode: if canvas_point.x >= state.start.x {
                        MarqueeSelectionMode::Enclosed
                    } else {
                        MarqueeSelectionMode::Touched
                    },
                    current: canvas_point,
                    ..state
                })
            }
            InteractionState::DraggingSelection(state) => {
                InteractionState::DraggingSelection(DragSelectionState {
                    current: canvas_point,
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
        canvas_pointer: PresentationPointer,
        _modifiers: PointerModifiers,
    ) -> Result<(), SessionError> {
        self.pointer_move(canvas_pointer, PointerModifiers::default())?;
        if let InteractionState::MarqueeSelecting(marquee) = self.interaction {
            let scene = self.scene()?;
            let items = marquee_selection(&scene, marquee);
            if marquee.shift {
                self.selection.toggle_all(items);
            } else {
                self.selection.replace_all(items);
            }
        }
        self.interaction = InteractionState::Idle;
        self.refresh_scene()
    }

    #[must_use]
    /// Exposes the current directional marquee rule for focused contracts.
    pub fn marquee_selection_mode(&self) -> Option<MarqueeSelectionMode> {
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

fn marquee_selection(scene: &Scene, marquee: MarqueeState) -> Vec<PresentationItemId> {
    let bounds = Rect::from_corners(
        scene.viewport.viewport_to_world(marquee.start),
        scene.viewport.viewport_to_world(marquee.current),
    );

    // Enclosed selection requires every region of an item to be inside the box.
    // Touched selection accepts any region intersection, matching directional tools.
    let mut item_regions = std::collections::BTreeMap::<PresentationItemId, Vec<Rect>>::new();
    for region in &scene.hit_regions {
        if let Some((item, region_bounds)) = selection_region(region) {
            item_regions.entry(item).or_default().push(region_bounds);
        }
    }

    item_regions
        .into_iter()
        .filter_map(|(item, regions)| {
            let selected = match marquee.mode {
                MarqueeSelectionMode::Enclosed => {
                    regions.iter().all(|region| rect_contains(bounds, *region))
                }
                MarqueeSelectionMode::Touched => regions
                    .iter()
                    .any(|region| rect_intersects(bounds, *region)),
            };
            selected.then_some(item)
        })
        .collect()
}

fn selection_region(region: &HitRegion) -> Option<(PresentationItemId, Rect)> {
    match region {
        HitRegion::SymbolBody { symbol_id, bounds } => {
            Some((PresentationItemId::Symbol(*symbol_id), *bounds))
        }
        HitRegion::WireVertex {
            wire_id, position, ..
        } => Some((
            PresentationItemId::Wire(*wire_id),
            Rect::from_corners(*position, *position),
        )),
        HitRegion::WireSegment {
            wire_id,
            start,
            end,
            ..
        } => Some((
            PresentationItemId::Wire(*wire_id),
            Rect::from_corners(*start, *end),
        )),
        HitRegion::Junction {
            junction_id,
            position,
            radius,
        } => Some((
            PresentationItemId::Junction(*junction_id),
            circle_bounds(*position, *radius),
        )),
        HitRegion::Annotation {
            annotation_id,
            bounds,
        } => Some((PresentationItemId::Annotation(*annotation_id), *bounds)),
        HitRegion::Terminal { .. } => None,
    }
}

fn circle_bounds(center: WorldPoint, radius: f64) -> Rect {
    Rect::from_corners(
        WorldPoint::new(center.x - radius, center.y - radius),
        WorldPoint::new(center.x + radius, center.y + radius),
    )
}

fn rect_contains(outer: Rect, inner: Rect) -> bool {
    outer.contains(inner.min) && outer.contains(inner.max)
}

fn rect_intersects(first: Rect, second: Rect) -> bool {
    first.min.x <= second.max.x
        && first.max.x >= second.min.x
        && first.min.y <= second.max.y
        && first.max.y >= second.min.y
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
