//! Pointer interaction state shared by desktop and browser shells.

use athena_geometry::WorldPoint;
use athena_render::PresentationItemId;

/// Modifier keys needed to disambiguate selection gestures.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct PointerModifiers {
    /// Whether Shift is held for additive/toggle selection.
    pub shift: bool,
    /// Whether the platform command modifier is held.
    pub command: bool,
}

/// Directional marquee semantics, matching the editor reference behavior.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum MarqueeSelectionMode {
    Enclosed,
    Touched,
}

/// A pointer position in viewport/canvas coordinates supplied by a shell.
///
/// World coordinates remain reserved for persisted schematic geometry and scene
/// projection. The session converts this type only at render interaction edges.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct PresentationPointer {
    position: WorldPoint,
}

impl PresentationPointer {
    /// Creates a canvas-space pointer position.
    #[must_use]
    pub const fn new(x: f64, y: f64) -> Self {
        Self {
            position: WorldPoint::new(x, y),
        }
    }

    pub(crate) const fn position(self) -> WorldPoint {
        self.position
    }
}

/// Current marquee drag geometry and directional rule.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct MarqueeState {
    /// Canvas-space location at which the shell began the marquee gesture.
    pub start: PresentationPointer,
    /// Current canvas-space pointer location for marquee rendering and selection.
    pub current: PresentationPointer,
    pub mode: MarqueeSelectionMode,
    /// Preserves additive/toggle intent across a multi-event marquee gesture.
    pub shift: bool,
}

/// Current selection drag geometry.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct DragSelectionState {
    pub start: WorldPoint,
    pub current: WorldPoint,
}

/// Minimal wire-vertex edit state reserved for the wire-editing task.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct WireVertexDragState {
    pub wire_id: athena_domain::WireId,
    pub vertex_index: usize,
}

/// Minimal endpoint reconnect state reserved for the wire-editing task.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct WireEndpointReconnectState {
    pub wire_id: athena_domain::WireId,
    pub start: bool,
}

/// Tracks the item whose typed properties are being drafted before commit.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct PropertyEditingState {
    pub target: PresentationItemId,
}

/// Holds placement-preview geometry without creating a persistent item.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct ToolPlacementTransientState {
    pub preview_position: WorldPoint,
}

/// Explicit transient interaction modes shared by all shells.
#[derive(Clone, Copy, Debug, Default, PartialEq)]
pub enum InteractionState {
    #[default]
    Idle,
    MarqueeSelecting(MarqueeState),
    DraggingSelection(DragSelectionState),
    EditingWireVertex(WireVertexDragState),
    ReconnectingWireEndpoint(WireEndpointReconnectState),
    EditingProperties(PropertyEditingState),
    ToolPlacementTransient(ToolPlacementTransientState),
}
