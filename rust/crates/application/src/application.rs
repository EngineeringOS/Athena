//! Narrow application facade shared by desktop and WASM adapters.

use athena_domain::{FolioId, Project};
use athena_editor::DocumentRevision;
use serde::{Deserialize, Serialize};

use crate::{AthenaDispatcher, AthenaFrontendMessage, AthenaMessage};

/// Immutable application state returned for tests, rendering, and diagnostics.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct EditorSnapshot {
    /// Immutable electrical project aggregate.
    pub project: Project,
    /// Folio currently presented by the document viewport and properties panel.
    pub active_folio_id: FolioId,
    /// Stable semantic state identity used by history and save results.
    pub revision: DocumentRevision,
    /// Whether the current revision differs from the saved checkpoint.
    pub dirty: bool,
    /// Whether the application awaits a platform save result.
    pub save_pending: bool,
}

/// Platform-neutral entrypoint for all Athena application behavior.
#[derive(Default)]
pub struct AthenaEditor {
    dispatcher: AthenaDispatcher,
}

impl AthenaEditor {
    /// Handles one typed root message and returns ordered semantic effects.
    pub fn handle_message(
        &mut self,
        message: impl Into<AthenaMessage>,
    ) -> Vec<AthenaFrontendMessage> {
        self.dispatcher.dispatch(message.into())
    }

    /// Returns an immutable state clone without exposing handler internals.
    #[must_use]
    pub fn state_snapshot(&self) -> Option<EditorSnapshot> {
        self.dispatcher.state_snapshot()
    }
}
