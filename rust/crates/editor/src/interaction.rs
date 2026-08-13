//! Pointer interaction state shared by desktop and browser shells.

use athena_geometry::WorldPoint;

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
pub enum DebugMarqueeMode {
    Enclosed,
    Touched,
}

/// Current marquee drag geometry and directional rule.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct MarqueeState {
    pub start: WorldPoint,
    pub current: WorldPoint,
    pub mode: DebugMarqueeMode,
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

/// Explicit transient interaction modes shared by all shells.
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum InteractionState {
    Idle,
    MarqueeSelecting(MarqueeState),
    DraggingSelection(DragSelectionState),
    EditingWireVertex(WireVertexDragState),
    ReconnectingWireEndpoint(WireEndpointReconnectState),
}

impl Default for InteractionState {
    fn default() -> Self {
        Self::Idle
    }
}
