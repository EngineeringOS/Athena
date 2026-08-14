//! Thin WASM protocol adapter around one persistent [`AthenaEditor`].
//!
//! Browser DOM, focus, and file handles stay in JavaScript. All project,
//! history, validation, panel, and widget state remains in shared Rust.

use athena_application::{
    AthenaEditor, AthenaFrontendMessage, AthenaMessage, EditorSnapshot, PortfolioMessage,
    SaveOutcome, SaveRequestId, WorkspaceShell,
};
use athena_domain::ProjectId;
use athena_editor::DocumentRevision;
use js_sys::Function;
use uuid::Uuid;
use wasm_bindgen::prelude::*;

/// Native-testable protocol core used by the exported WASM handle.
#[derive(Default)]
pub struct WebEditorCore {
    editor: AthenaEditor,
}

impl WebEditorCore {
    /// Creates an empty persistent application instance.
    #[must_use]
    pub fn new() -> Self {
        Self::default()
    }

    /// Dispatches one typed Rust message.
    pub fn dispatch_message(&mut self, message: AthenaMessage) -> Vec<AthenaFrontendMessage> {
        self.editor.handle_message(message)
    }

    /// Dispatches one serde-tagged JSON message and returns tagged JSON effects.
    pub fn dispatch_json(&mut self, message_json: &str) -> Result<String, String> {
        let message = serde_json::from_str::<AthenaMessage>(message_json)
            .map_err(|error| format!("invalid Athena message: {error}"))?;
        serde_json::to_string(&self.dispatch_message(message))
            .map_err(|error| format!("effect serialization failed: {error}"))
    }

    /// Delivers bytes selected by the browser open-file adapter.
    pub fn deliver_open_bytes(&mut self, bytes: Vec<u8>) -> Vec<AthenaFrontendMessage> {
        self.editor
            .handle_message(PortfolioMessage::OpenBytes { bytes })
    }

    /// Delivers a typed save result from the browser file adapter.
    pub fn deliver_save_result(
        &mut self,
        request_id: SaveRequestId,
        project_id: ProjectId,
        revision: DocumentRevision,
        outcome: SaveOutcome,
    ) -> Vec<AthenaFrontendMessage> {
        self.editor.handle_message(PortfolioMessage::SaveResult {
            request_id,
            project_id,
            revision,
            outcome,
        })
    }

    /// Returns an immutable clone for native protocol tests and diagnostics.
    #[must_use]
    pub fn state_snapshot(&self) -> Option<EditorSnapshot> {
        self.editor.state_snapshot()
    }

    /// Returns the shared Rust shell for protocol certification and diagnostics.
    #[must_use]
    pub fn shell_snapshot(&self) -> WorkspaceShell {
        self.editor.shell_snapshot()
    }
}

/// Browser-facing persistent editor handle.
#[wasm_bindgen]
pub struct WasmAthenaEditor {
    core: WebEditorCore,
    effect_callback: Option<Function>,
}

#[wasm_bindgen]
impl WasmAthenaEditor {
    /// Creates one empty persistent editor instance.
    #[wasm_bindgen(constructor)]
    pub fn new() -> Self {
        Self {
            core: WebEditorCore::new(),
            effect_callback: None,
        }
    }

    /// Registers a callback receiving each frontend effect as tagged JSON.
    pub fn set_effect_callback(&mut self, callback: Function) {
        self.effect_callback = Some(callback);
    }

    /// Dispatches one tagged message and returns the ordered effect array JSON.
    pub fn dispatch(&mut self, message_json: &str) -> Result<String, JsValue> {
        let message = serde_json::from_str::<AthenaMessage>(message_json)
            .map_err(|error| JsValue::from_str(&format!("invalid Athena message: {error}")))?;
        let effects = self.core.dispatch_message(message);
        self.emit(effects)
    }

    /// Delivers bytes from a browser open-file adapter.
    pub fn deliver_open_bytes(&mut self, bytes: Vec<u8>) -> Result<String, JsValue> {
        let effects = self.core.deliver_open_bytes(bytes);
        self.emit(effects)
    }

    /// Delivers a save result with the exact request identity and revision.
    pub fn deliver_save_result(
        &mut self,
        request_id: u64,
        project_id: &str,
        revision: u64,
        outcome_json: &str,
    ) -> Result<String, JsValue> {
        let project_id = Uuid::parse_str(project_id)
            .map(ProjectId::from_uuid)
            .map_err(|_| JsValue::from_str("project_id is not a UUID"))?;
        let outcome = serde_json::from_str::<SaveOutcome>(outcome_json)
            .map_err(|error| JsValue::from_str(&format!("invalid save outcome: {error}")))?;
        let effects = self.core.deliver_save_result(
            SaveRequestId(request_id),
            project_id,
            DocumentRevision(revision),
            outcome,
        );
        self.emit(effects)
    }

    fn emit(&self, effects: Vec<AthenaFrontendMessage>) -> Result<String, JsValue> {
        if let Some(callback) = &self.effect_callback {
            for effect in &effects {
                let json = serde_json::to_string(effect)
                    .map_err(|error| JsValue::from_str(&error.to_string()))?;
                callback.call1(&JsValue::NULL, &JsValue::from_str(&json))?;
            }
        }
        serde_json::to_string(&effects).map_err(|error| JsValue::from_str(&error.to_string()))
    }
}

impl Default for WasmAthenaEditor {
    fn default() -> Self {
        Self::new()
    }
}
