//! Thin wasm-bindgen bridge over the shared authoring core.

use athena_domain::{Point, Project, SymbolInstance, Terminal};
use athena_editor::{EditorCommand, EditorState, History};
use athena_format::SnapshotStore;
use athena_library::{SearchQuery, SymbolCatalog};
use athena_render::{EditorPresentation, HitRegion, PresentationItemId, hit_test, project_sheet};
use serde::Serialize;
use wasm_bindgen::prelude::*;

fn normalize_snapshot_bytes(bytes: &[u8]) -> Result<Vec<u8>, String> {
    let project = SnapshotStore::decode_snapshot(bytes)
        .map_err(|error| format!("snapshot validation failed: {error}"))?;
    SnapshotStore::encode_snapshot(&project)
        .map_err(|error| format!("snapshot encoding failed: {error}"))
}

/// Validates browser-owned snapshot bytes and returns their canonical encoding.
///
/// JavaScript owns IndexedDB/localStorage. Rust owns the shared project codec.
#[wasm_bindgen]
pub fn validate_snapshot_bytes(bytes: &[u8]) -> Result<Vec<u8>, JsValue> {
    normalize_snapshot_bytes(bytes).map_err(|error| JsValue::from_str(&error))
}

/// A browser-neutral authoring controller. It is also directly testable on the
/// native target, which keeps the WASM export intentionally thin.
#[derive(Clone, Debug)]
pub struct WebEditorCore {
    catalog: SymbolCatalog,
    state: EditorState,
    history: History,
    active_sheet_id: athena_domain::SheetId,
    presentation: EditorPresentation,
    placement_definition: Option<athena_domain::SymbolDefinitionId>,
    wire_start: Option<athena_domain::TerminalId>,
}

impl WebEditorCore {
    #[must_use]
    pub fn new(project_name: impl Into<String>) -> Self {
        let catalog = SymbolCatalog::with_built_ins();
        let mut project = Project::new(project_name);
        for record in catalog.search(&SearchQuery::default()) {
            project
                .add_symbol_definition(record.definition.clone())
                .expect("built-in definitions are unique");
        }
        let active_sheet_id = project.sheet_order()[0];
        Self {
            catalog,
            state: EditorState::new(project),
            history: History::new(200),
            active_sheet_id,
            presentation: EditorPresentation::default(),
            placement_definition: None,
            wire_start: None,
        }
    }

    pub fn begin_placement_by_name(&mut self, name: &str) -> Result<(), String> {
        let definition = self
            .catalog
            .search(&SearchQuery::new(name))
            .into_iter()
            .find(|result| result.definition.name.eq_ignore_ascii_case(name))
            .ok_or_else(|| format!("unknown built-in symbol: {name}"))?;
        self.placement_definition = Some(definition.definition.id);
        Ok(())
    }

    #[must_use]
    pub const fn active_sheet_id(&self) -> athena_domain::SheetId {
        self.active_sheet_id
    }

    #[must_use]
    pub fn terminal_ids(&self) -> Vec<athena_domain::TerminalId> {
        self.state
            .project()
            .sheet(self.active_sheet_id)
            .into_iter()
            .flat_map(|sheet| sheet.symbol_instances.values())
            .flat_map(|symbol| symbol.terminals.keys().copied())
            .collect()
    }

    pub fn pointer_click(&mut self, x: i64, y: i64) -> Result<(), String> {
        if let Some(start) = self.wire_start {
            let end = self
                .terminal_at(x, y)
                .ok_or_else(|| "wire endpoints must be terminals".to_owned())?;
            if start == end {
                return Ok(());
            }
            let start_point = self
                .state
                .project()
                .terminal(self.active_sheet_id, start)
                .ok_or_else(|| "wire start terminal missing".to_owned())?
                .position;
            let end_point = self
                .state
                .project()
                .terminal(self.active_sheet_id, end)
                .ok_or_else(|| "wire end terminal missing".to_owned())?
                .position;
            self.history
                .apply(
                    &mut self.state,
                    EditorCommand::CreateWire {
                        sheet_id: self.active_sheet_id,
                        wire: athena_domain::Wire::new(
                            athena_domain::WireEndpoint::Terminal(start),
                            athena_domain::WireEndpoint::Terminal(end),
                            vec![start_point, end_point],
                        ),
                    },
                )
                .map_err(|error| error.to_string())?;
            self.wire_start = None;
            return Ok(());
        }
        if self.placement_definition.is_none() {
            self.wire_start = self.terminal_at(x, y);
            return Ok(());
        }
        let Some(definition_id) = self.placement_definition else {
            return Ok(());
        };
        let point = Point::new(x, y);
        let record = self
            .catalog
            .get(definition_id)
            .ok_or_else(|| "selected symbol definition is unavailable".to_owned())?;
        let mut symbol = SymbolInstance::new(definition_id);
        symbol.position = point;
        for template in &record.terminals {
            symbol.add_terminal(Terminal::new(
                symbol.id,
                template.name.clone(),
                template.electrical_kind,
                Point::new(
                    point.x + template.position.x.round() as i64,
                    point.y + template.position.y.round() as i64,
                ),
            ));
        }
        let symbol_id = symbol.id;
        self.history
            .apply(
                &mut self.state,
                EditorCommand::PlaceSymbol {
                    sheet_id: self.active_sheet_id,
                    symbol,
                },
            )
            .map_err(|error| error.to_string())?;
        self.presentation.selected.clear();
        self.presentation
            .selected
            .insert(PresentationItemId::Symbol(symbol_id));
        self.placement_definition = None;
        Ok(())
    }

    pub fn begin_wire(&mut self) {
        self.wire_start = None;
        self.placement_definition = None;
    }

    fn terminal_at(&self, x: i64, y: i64) -> Option<athena_domain::TerminalId> {
        let scene = project_sheet(
            self.state.project(),
            self.active_sheet_id,
            &self.presentation,
        )?;
        match hit_test(
            &scene,
            athena_geometry::WorldPoint::new(x as f64, y as f64),
            6.0,
        )? {
            HitRegion::Terminal { terminal_id, .. } => Some(terminal_id),
            _ => None,
        }
    }

    pub fn apply_command_json(&mut self, command_json: &str) -> Result<(), String> {
        let command = serde_json::from_str(command_json)
            .map_err(|error| format!("invalid editor command: {error}"))?;
        self.history
            .apply(&mut self.state, command)
            .map_err(|error| error.to_string())
    }

    pub fn undo(&mut self) -> Result<bool, String> {
        self.history
            .undo(&mut self.state)
            .map_err(|error| error.to_string())
    }

    pub fn redo(&mut self) -> Result<bool, String> {
        self.history
            .redo(&mut self.state)
            .map_err(|error| error.to_string())
    }

    pub fn render_active_sheet(&self) -> Result<String, String> {
        let scene = project_sheet(
            self.state.project(),
            self.active_sheet_id,
            &self.presentation,
        )
        .ok_or_else(|| "active sheet is unavailable".to_owned())?;
        serde_json::to_string(&scene).map_err(|error| format!("scene encoding failed: {error}"))
    }

    pub fn encode_snapshot(&self) -> Result<Vec<u8>, String> {
        SnapshotStore::encode_snapshot(self.state.project())
            .map_err(|error| format!("snapshot encoding failed: {error}"))
    }

    pub fn load_snapshot(&mut self, bytes: &[u8]) -> Result<(), String> {
        let project = SnapshotStore::decode_snapshot(bytes)
            .map_err(|error| format!("snapshot validation failed: {error}"))?;
        let active_sheet_id = project
            .sheet_order()
            .first()
            .copied()
            .ok_or_else(|| "loaded project has no sheets".to_owned())?;
        self.state = EditorState::new(project);
        self.history = History::new(200);
        self.active_sheet_id = active_sheet_id;
        self.presentation = EditorPresentation::default();
        self.placement_definition = None;
        self.wire_start = None;
        Ok(())
    }
}

/// WASM handle. Its exports contain no mutable domain record references.
#[wasm_bindgen]
pub struct WebEditor {
    core: WebEditorCore,
}

#[wasm_bindgen]
impl WebEditor {
    #[wasm_bindgen(constructor)]
    pub fn new(project_name: String) -> Self {
        Self {
            core: WebEditorCore::new(project_name),
        }
    }

    pub fn begin_placement(&mut self, symbol_name: String) -> Result<(), JsValue> {
        self.core
            .begin_placement_by_name(&symbol_name)
            .map_err(js_error)
    }

    pub fn pointer_click(&mut self, x: i64, y: i64) -> Result<(), JsValue> {
        self.core.pointer_click(x, y).map_err(js_error)
    }

    pub fn begin_wire(&mut self) {
        self.core.begin_wire();
    }

    pub fn apply_command(&mut self, command_json: String) -> Result<(), JsValue> {
        self.core
            .apply_command_json(&command_json)
            .map_err(js_error)
    }

    pub fn undo(&mut self) -> Result<bool, JsValue> {
        self.core.undo().map_err(js_error)
    }

    pub fn redo(&mut self) -> Result<bool, JsValue> {
        self.core.redo().map_err(js_error)
    }

    pub fn render_active_sheet(&self) -> Result<String, JsValue> {
        self.core.render_active_sheet().map_err(js_error)
    }

    pub fn encode_snapshot(&self) -> Result<Vec<u8>, JsValue> {
        self.core.encode_snapshot().map_err(js_error)
    }

    pub fn load_snapshot(&mut self, bytes: &[u8]) -> Result<(), JsValue> {
        self.core.load_snapshot(bytes).map_err(js_error)
    }
}

fn js_error(error: String) -> JsValue {
    JsValue::from_str(&error)
}

#[derive(Serialize)]
struct BuiltInSymbol<'a> {
    name: &'a str,
}

#[wasm_bindgen]
pub fn built_in_symbol_names() -> Result<String, JsValue> {
    let catalog = SymbolCatalog::with_built_ins();
    let names = catalog
        .search(&SearchQuery::default())
        .iter()
        .map(|result| BuiltInSymbol {
            name: result.definition.name.as_str(),
        })
        .collect::<Vec<_>>();
    serde_json::to_string(&names).map_err(|error| JsValue::from_str(&error.to_string()))
}

/// Compile-time marker for the web-core crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct WebCoreContract;

#[cfg(test)]
mod snapshot_tests {
    use athena_domain::Project;
    use athena_format::SnapshotStore;

    #[test]
    fn normalized_snapshot_bytes_round_trip_through_rust_validation() {
        let project = Project::new("Browser snapshot");
        let bytes = SnapshotStore::encode_snapshot(&project).expect("project encodes");

        let normalized =
            super::normalize_snapshot_bytes(&bytes).expect("valid snapshot is returned");

        assert_eq!(normalized, bytes);
    }

    #[test]
    fn invalid_snapshot_bytes_are_rejected_before_browser_storage() {
        let error =
            super::normalize_snapshot_bytes(b"not json").expect_err("invalid bytes must reject");

        assert!(error.contains("snapshot validation failed"));
    }
}

#[cfg(test)]
mod tests {
    use super::WebCoreContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = WebCoreContract;
        assert_eq!(marker, WebCoreContract);
    }
}
