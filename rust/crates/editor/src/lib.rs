//! Deterministic editing commands, validation, and history contracts.

mod command;
mod history;
mod interaction;
mod persistence;
mod selection;
mod session;
mod validation;

pub use command::{
    AppliedCommand, ApplyError, CommandEnvelope, EditorCommand, EditorState, FieldTarget, ItemId,
    StoredItem,
};
pub use history::{History, HistoryError, snapshot_bytes, snapshots_equal};
pub use interaction::{
    DragSelectionState, InteractionState, MarqueeSelectionMode, MarqueeState, PointerModifiers,
    PresentationPointer, PropertyEditingState, ToolPlacementTransientState,
    WireEndpointReconnectState, WireVertexDragState,
};
pub use persistence::{
    InMemoryPersistence, OutboxStore, PersistenceError, SnapshotSink, SnapshotSource,
};
pub use selection::SelectionState;
pub use session::{EditorSession, SessionError};

/// Compile-time marker for the editor crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct EditorContract;

#[cfg(test)]
mod tests {
    use super::EditorContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = EditorContract;
        assert_eq!(marker, EditorContract);
    }
}
