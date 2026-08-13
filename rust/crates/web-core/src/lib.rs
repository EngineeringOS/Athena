//! Thin wasm-bindgen bridge over Athena's shared editor session.
//!
//! Browser storage and DOM plumbing live outside this crate. This bridge owns
//! only browser tool selection and forwards every schematic edit to the same
//! platform-neutral controller used by the native workbench.

use athena_domain::{FieldValue, Point, Project, SymbolInstance, Terminal};
use athena_editor::{
    EditorCommand, EditorSession, FieldTarget, PointerModifiers, PresentationPointer,
};
use athena_format::SnapshotStore;
use athena_library::{SearchQuery, SymbolCatalog};
use athena_render::{HitRegion, PresentationItemId, hit_test};
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
pub struct WebEditorCore {
    catalog: SymbolCatalog,
    session: EditorSession,
    placement_definition: Option<athena_domain::SymbolDefinitionId>,
    wire_start: Option<athena_domain::TerminalId>,
    wire_mode: bool,
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
            session: EditorSession::new(project, active_sheet_id),
            placement_definition: None,
            wire_start: None,
            wire_mode: false,
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
        self.session.active_sheet_id()
    }

    #[must_use]
    pub fn terminal_ids(&self) -> Vec<athena_domain::TerminalId> {
        self.session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .into_iter()
            .flat_map(|sheet| sheet.symbol_instances.values())
            .flat_map(|symbol| symbol.terminals.keys().copied())
            .collect()
    }

    pub fn pointer_click(&mut self, x: i64, y: i64) -> Result<(), String> {
        if self.wire_mode {
            return self.handle_wire_click(x, y);
        }
        let Some(definition_id) = self.placement_definition else {
            let _ = self.pointer_down(x, y, false, false)?;
            return self.pointer_up(x, y, false, false);
        };
        let point = self.world_point(x, y);
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
        self.session
            .apply_command(EditorCommand::PlaceSymbol {
                sheet_id: self.active_sheet_id(),
                symbol,
            })
            .map_err(|error| error.to_string())?;
        self.session
            .select_only(PresentationItemId::Symbol(symbol_id))
            .map_err(|error| error.to_string())?;
        self.placement_definition = None;
        Ok(())
    }

    pub fn begin_wire(&mut self) {
        self.wire_start = None;
        self.placement_definition = None;
        self.wire_mode = true;
    }

    /// Cancels browser-owned placement or wiring without mutating the project.
    pub fn cancel_active_tool(&mut self) -> Result<(), String> {
        self.placement_definition = None;
        self.wire_start = None;
        self.wire_mode = false;
        Ok(())
    }

    /// Rotates the shared selection by one quarter turn.
    pub fn rotate_selection_90(&mut self) -> Result<(), String> {
        self.session
            .rotate_selection_90()
            .map_err(|error| error.to_string())
    }

    /// Mirrors the shared selection.
    pub fn mirror_selection(&mut self) -> Result<(), String> {
        self.session
            .mirror_selection()
            .map_err(|error| error.to_string())
    }

    /// Deletes the shared selection.
    pub fn delete_selection(&mut self) -> Result<(), String> {
        self.session
            .delete_selection()
            .map_err(|error| error.to_string())
    }

    /// Inserts an interior vertex on the selected wire through shared history.
    pub fn insert_selected_wire_vertex(
        &mut self,
        segment_index: usize,
        position: Point,
    ) -> Result<(), String> {
        let wire_id = self.selected_wire_id()?;
        self.session
            .insert_wire_vertex(wire_id, segment_index, position)
            .map_err(|error| error.to_string())
    }

    /// Deletes an interior vertex on the selected wire through shared history.
    pub fn delete_selected_wire_vertex(&mut self, vertex_index: usize) -> Result<(), String> {
        let wire_id = self.selected_wire_id()?;
        self.session
            .delete_wire_vertex(wire_id, vertex_index)
            .map_err(|error| error.to_string())
    }

    /// Starts a shared selection, marquee, or selected-wire handle gesture.
    pub fn pointer_down(
        &mut self,
        x: i64,
        y: i64,
        shift: bool,
        command: bool,
    ) -> Result<bool, String> {
        if self.placement_definition.is_some() || self.wire_mode {
            return Ok(false);
        }
        self.session
            .pointer_down(
                PresentationPointer::new(x as f64, y as f64),
                pointer_modifiers(shift, command),
            )
            .map_err(|error| error.to_string())?;
        Ok(true)
    }

    /// Updates the current shared pointer gesture.
    pub fn pointer_move(
        &mut self,
        x: i64,
        y: i64,
        shift: bool,
        command: bool,
    ) -> Result<(), String> {
        self.session
            .pointer_move(
                PresentationPointer::new(x as f64, y as f64),
                pointer_modifiers(shift, command),
            )
            .map_err(|error| error.to_string())
    }

    /// Completes the current shared pointer gesture.
    pub fn pointer_up(&mut self, x: i64, y: i64, shift: bool, command: bool) -> Result<(), String> {
        self.session
            .pointer_up(
                PresentationPointer::new(x as f64, y as f64),
                pointer_modifiers(shift, command),
            )
            .map_err(|error| error.to_string())
    }

    /// Returns the shared selection size for browser status and tests.
    #[must_use]
    pub fn selection_count(&self) -> usize {
        self.session.selected_count()
    }

    /// Updates the selected symbol reference through shared command history.
    pub fn update_selected_symbol_reference(&mut self, value: String) -> Result<(), String> {
        let symbol_id = self.selected_symbol_id()?;
        self.session
            .set_field_value(
                FieldTarget::Symbol(symbol_id),
                "reference",
                Some(FieldValue::Text(value)),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates the selected symbol description through shared command history.
    pub fn update_selected_symbol_description(&mut self, value: String) -> Result<(), String> {
        let symbol_id = self.selected_symbol_id()?;
        self.session
            .set_field_value(
                FieldTarget::Symbol(symbol_id),
                "description",
                Some(FieldValue::Text(value)),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates the selected wire label through shared command history.
    pub fn update_selected_wire_label(&mut self, value: String) -> Result<(), String> {
        let wire_id = self.selected_wire_id()?;
        self.session
            .set_field_value(
                FieldTarget::Wire(wire_id),
                "label",
                Some(FieldValue::Text(value)),
            )
            .map_err(|error| error.to_string())
    }

    /// Replaces active-sheet grid settings through shared command history.
    pub fn update_sheet_grid(&mut self, enabled: bool, spacing: i64) -> Result<(), String> {
        let mut settings = self
            .session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .ok_or_else(|| "active sheet is unavailable".to_owned())?
            .settings
            .clone();
        settings.grid_visible = enabled;
        settings.grid_spacing = spacing;
        self.session
            .apply_sheet_settings(settings)
            .map_err(|error| error.to_string())
    }

    /// Renames the active sheet through shared command history.
    pub fn update_sheet_name(&mut self, name: String) -> Result<(), String> {
        self.session
            .rename_active_sheet(name)
            .map_err(|error| error.to_string())
    }

    /// Returns the selected symbol reference when one is selected.
    #[must_use]
    pub fn selected_symbol_reference(&self) -> Option<String> {
        self.selected_symbol_field("reference")
    }

    /// Returns the selected symbol description when one is selected.
    #[must_use]
    pub fn selected_symbol_description(&self) -> Option<String> {
        self.selected_symbol_field("description")
    }

    /// Returns the selected wire label when one is selected.
    #[must_use]
    pub fn selected_wire_label(&self) -> Option<String> {
        let wire_id = self.selected_wire_id().ok()?;
        field_text(
            self.session
                .state()
                .project()
                .wire(self.active_sheet_id(), wire_id)?
                .fields
                .get("label")?,
        )
    }

    /// Serializes the active property values needed by the thin browser inspector.
    pub fn inspector_state_json(&self) -> Result<String, String> {
        let sheet = self
            .session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .ok_or_else(|| "active sheet is unavailable".to_owned())?;
        serde_json::to_string(&InspectorState {
            selected_count: self.selection_count(),
            symbol_reference: self.selected_symbol_reference(),
            symbol_description: self.selected_symbol_description(),
            wire_label: self.selected_wire_label(),
            sheet_name: sheet.name.clone(),
            grid_visible: sheet.settings.grid_visible,
            grid_spacing: sheet.settings.grid_spacing,
        })
        .map_err(|error| format!("inspector encoding failed: {error}"))
    }

    /// Selects a stable sorted wire for browser-only controller contracts.
    pub fn select_wire_for_test(&mut self, index: usize) -> Result<(), String> {
        let wire_id = self
            .session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .and_then(|sheet| sheet.wires.keys().nth(index).copied())
            .ok_or_else(|| "wire fixture index is unavailable".to_owned())?;
        self.session
            .select_only(PresentationItemId::Wire(wire_id))
            .map_err(|error| error.to_string())
    }

    /// Selects a stable sorted symbol for browser-only controller contracts.
    pub fn select_symbol_for_test(&mut self, index: usize) -> Result<(), String> {
        let symbol_id = self
            .session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .and_then(|sheet| sheet.symbol_instances.keys().nth(index).copied())
            .ok_or_else(|| "symbol fixture index is unavailable".to_owned())?;
        self.session
            .select_only(PresentationItemId::Symbol(symbol_id))
            .map_err(|error| error.to_string())
    }

    fn terminal_at(&self, x: i64, y: i64) -> Option<athena_domain::TerminalId> {
        let scene = self.session.scene().ok()?;
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
        self.session
            .apply_command(command)
            .map_err(|error| error.to_string())
    }

    pub fn undo(&mut self) -> Result<bool, String> {
        self.session.undo().map_err(|error| error.to_string())
    }

    pub fn redo(&mut self) -> Result<bool, String> {
        self.session.redo().map_err(|error| error.to_string())
    }

    pub fn render_active_sheet(&self) -> Result<String, String> {
        let scene = self.session.scene().map_err(|error| error.to_string())?;
        serde_json::to_string(&scene).map_err(|error| format!("scene encoding failed: {error}"))
    }

    pub fn encode_snapshot(&self) -> Result<Vec<u8>, String> {
        SnapshotStore::encode_snapshot(self.session.state().project())
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
        self.session = EditorSession::new(project, active_sheet_id);
        self.placement_definition = None;
        self.wire_start = None;
        self.wire_mode = false;
        Ok(())
    }

    fn handle_wire_click(&mut self, x: i64, y: i64) -> Result<(), String> {
        let terminal = self
            .terminal_at(x, y)
            .ok_or_else(|| "wire endpoints must be terminals".to_owned())?;
        let Some(start) = self.wire_start else {
            self.wire_start = Some(terminal);
            return Ok(());
        };
        if start == terminal {
            return Ok(());
        }
        let project = self.session.state().project();
        let first = project
            .terminal(self.active_sheet_id(), start)
            .ok_or_else(|| "wire start terminal missing".to_owned())?
            .position;
        let second = project
            .terminal(self.active_sheet_id(), terminal)
            .ok_or_else(|| "wire end terminal missing".to_owned())?
            .position;
        // Keep the route canonical when terminals are not axis-aligned.
        let route = if first.x == second.x || first.y == second.y {
            vec![first, second]
        } else {
            vec![first, Point::new(second.x, first.y), second]
        };
        self.session
            .apply_command(EditorCommand::CreateWire {
                sheet_id: self.active_sheet_id(),
                wire: athena_domain::Wire::new(
                    athena_domain::WireEndpoint::Terminal(start),
                    athena_domain::WireEndpoint::Terminal(terminal),
                    route,
                ),
            })
            .map_err(|error| error.to_string())?;
        self.wire_start = None;
        self.wire_mode = false;
        Ok(())
    }

    fn world_point(&self, x: i64, y: i64) -> Point {
        let world = self
            .session
            .presentation()
            .viewport
            .viewport_to_world(athena_geometry::WorldPoint::new(x as f64, y as f64));
        Point::new(world.x.round() as i64, world.y.round() as i64)
    }

    fn selected_symbol_id(&self) -> Result<athena_domain::SymbolInstanceId, String> {
        self.session
            .presentation()
            .selected
            .iter()
            .find_map(|item| match item {
                PresentationItemId::Symbol(id) => Some(*id),
                _ => None,
            })
            .ok_or_else(|| "a symbol must be selected before editing its properties".to_owned())
    }

    fn selected_wire_id(&self) -> Result<athena_domain::WireId, String> {
        self.session
            .presentation()
            .selected
            .iter()
            .find_map(|item| match item {
                PresentationItemId::Wire(id) => Some(*id),
                _ => None,
            })
            .ok_or_else(|| "a wire must be selected before editing its properties".to_owned())
    }

    fn selected_symbol_field(&self, field: &str) -> Option<String> {
        let symbol_id = self.selected_symbol_id().ok()?;
        field_text(
            self.session
                .state()
                .project()
                .symbol_instance(self.active_sheet_id(), symbol_id)?
                .fields
                .get(field)?,
        )
    }
}

fn pointer_modifiers(shift: bool, command: bool) -> PointerModifiers {
    PointerModifiers { shift, command }
}

fn field_text(value: &FieldValue) -> Option<String> {
    match value {
        FieldValue::Text(value) => Some(value.clone()),
        FieldValue::Integer(value) => Some(value.to_string()),
        FieldValue::Boolean(value) => Some(value.to_string()),
    }
}

#[derive(Serialize)]
struct InspectorState {
    selected_count: usize,
    symbol_reference: Option<String>,
    symbol_description: Option<String>,
    wire_label: Option<String>,
    sheet_name: String,
    grid_visible: bool,
    grid_spacing: i64,
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

    pub fn pointer_click(&mut self, x: i32, y: i32) -> Result<(), JsValue> {
        self.core
            .pointer_click(i64::from(x), i64::from(y))
            .map_err(js_error)
    }

    pub fn pointer_down(
        &mut self,
        x: i32,
        y: i32,
        shift: bool,
        command: bool,
    ) -> Result<bool, JsValue> {
        self.core
            .pointer_down(i64::from(x), i64::from(y), shift, command)
            .map_err(js_error)
    }

    pub fn pointer_move(
        &mut self,
        x: i32,
        y: i32,
        shift: bool,
        command: bool,
    ) -> Result<(), JsValue> {
        self.core
            .pointer_move(i64::from(x), i64::from(y), shift, command)
            .map_err(js_error)
    }

    pub fn pointer_up(
        &mut self,
        x: i32,
        y: i32,
        shift: bool,
        command: bool,
    ) -> Result<(), JsValue> {
        self.core
            .pointer_up(i64::from(x), i64::from(y), shift, command)
            .map_err(js_error)
    }

    pub fn begin_wire(&mut self) {
        self.core.begin_wire();
    }

    pub fn cancel_active_tool(&mut self) -> Result<(), JsValue> {
        self.core.cancel_active_tool().map_err(js_error)
    }

    pub fn rotate_selection_90(&mut self) -> Result<(), JsValue> {
        self.core.rotate_selection_90().map_err(js_error)
    }

    pub fn mirror_selection(&mut self) -> Result<(), JsValue> {
        self.core.mirror_selection().map_err(js_error)
    }

    pub fn delete_selection(&mut self) -> Result<(), JsValue> {
        self.core.delete_selection().map_err(js_error)
    }

    pub fn insert_selected_wire_vertex(
        &mut self,
        segment_index: usize,
        x: i32,
        y: i32,
    ) -> Result<(), JsValue> {
        self.core
            .insert_selected_wire_vertex(segment_index, Point::new(i64::from(x), i64::from(y)))
            .map_err(js_error)
    }

    pub fn delete_selected_wire_vertex(&mut self, vertex_index: usize) -> Result<(), JsValue> {
        self.core
            .delete_selected_wire_vertex(vertex_index)
            .map_err(js_error)
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

    pub fn update_selected_symbol_reference(&mut self, value: String) -> Result<(), JsValue> {
        self.core
            .update_selected_symbol_reference(value)
            .map_err(js_error)
    }

    pub fn update_selected_symbol_description(&mut self, value: String) -> Result<(), JsValue> {
        self.core
            .update_selected_symbol_description(value)
            .map_err(js_error)
    }

    pub fn update_selected_wire_label(&mut self, value: String) -> Result<(), JsValue> {
        self.core
            .update_selected_wire_label(value)
            .map_err(js_error)
    }

    pub fn update_sheet_name(&mut self, value: String) -> Result<(), JsValue> {
        self.core.update_sheet_name(value).map_err(js_error)
    }

    pub fn update_sheet_grid(&mut self, enabled: bool, spacing: i32) -> Result<(), JsValue> {
        self.core
            .update_sheet_grid(enabled, i64::from(spacing))
            .map_err(js_error)
    }

    pub fn selection_count(&self) -> usize {
        self.core.selection_count()
    }

    pub fn inspector_state_json(&self) -> Result<String, JsValue> {
        self.core.inspector_state_json().map_err(js_error)
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
