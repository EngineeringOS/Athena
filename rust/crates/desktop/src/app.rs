//! Native desktop adapter over the platform-neutral editor session.
//!
//! This module translates catalog choices, filesystem actions, and native input
//! into shared commands without owning schematic mutation or history itself.

use crate::storage::FileSnapshotStore;
use athena_domain::{FieldValue, Point, Project, SymbolInstance, Terminal};
use athena_editor::{
    EditorCommand, EditorSession, FieldTarget, PointerModifiers, PresentationPointer,
};
use athena_editor::{SnapshotSink, SnapshotSource};
use athena_library::{SearchQuery, SymbolCatalog};
use athena_render::{PresentationItemId, Scene};

use crate::input::{ActiveTool, CanvasInput};

/// Catalog data rendered by the native symbol library panel.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct CatalogSymbol {
    pub definition_id: athena_domain::SymbolDefinitionId,
    pub name: String,
}

/// Native tool selection around the shared, platform-neutral editor session.
pub struct DesktopEditor {
    catalog: SymbolCatalog,
    session: EditorSession,
    active_tool: ActiveTool,
}

/// Keyboard modifiers translated from the native desktop event loop.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct DesktopModifiers {
    /// Extends or toggles the shared selection.
    pub shift: bool,
    /// Reserved command modifier forwarded to the shared session.
    pub command: bool,
}

impl From<DesktopModifiers> for PointerModifiers {
    fn from(value: DesktopModifiers) -> Self {
        Self {
            shift: value.shift,
            command: value.command,
        }
    }
}

impl DesktopEditor {
    /// Creates a desktop adapter with built-in electrical symbols loaded.
    #[must_use]
    pub fn new(project_name: impl Into<String>) -> Self {
        let catalog = SymbolCatalog::with_built_ins();
        let mut project = Project::new(project_name);
        for record in catalog.search(&SearchQuery::default()) {
            project
                .add_symbol_definition(record.definition.clone())
                .expect("built-in definition IDs remain unique");
        }
        let active_sheet_id = project.sheet_order()[0];
        Self {
            catalog,
            session: EditorSession::new(project, active_sheet_id),
            active_tool: ActiveTool::Select,
        }
    }

    /// Returns catalog symbols available for native placement controls.
    #[must_use]
    pub fn catalog_symbols(&self) -> Vec<CatalogSymbol> {
        self.catalog
            .search(&SearchQuery::default())
            .into_iter()
            .map(|result| CatalogSymbol {
                definition_id: result.definition.id,
                name: result.definition.name.clone(),
            })
            .collect()
    }

    /// Returns the active project's display name.
    #[must_use]
    pub fn project_name(&self) -> &str {
        &self.session.state().project().name
    }

    /// Returns the shared session's active sheet ID.
    #[must_use]
    pub const fn active_sheet_id(&self) -> athena_domain::SheetId {
        self.session.active_sheet_id()
    }

    /// Returns the currently selected desktop tool.
    #[must_use]
    pub fn active_tool(&self) -> &ActiveTool {
        &self.active_tool
    }

    /// Activates native placement for one catalog definition.
    pub fn begin_placement(&mut self, definition_id: athena_domain::SymbolDefinitionId) {
        self.active_tool = ActiveTool::PlaceSymbol(definition_id);
    }

    /// Activates terminal-to-terminal wire creation.
    pub fn begin_wiring(&mut self) {
        self.active_tool = ActiveTool::Wire { start: None };
    }

    /// Returns the native tool state to selection without mutating the sheet.
    pub fn cancel_active_tool(&mut self) {
        self.active_tool = ActiveTool::Select;
    }

    /// Replaces shared selection when a native adapter resolves an item hit.
    pub fn select(&mut self, item: Option<PresentationItemId>) {
        if let Some(item) = item {
            let _ = self.session.select_only(item);
        }
    }

    /// Routes a native canvas click according to the active tool.
    pub fn canvas_click(&mut self, point: Point) -> Result<(), String> {
        match self.active_tool.clone() {
            ActiveTool::PlaceSymbol(definition_id) => self.place_symbol(definition_id, point),
            ActiveTool::Wire { start } => self.handle_wire_click(start, point),
            ActiveTool::Select | ActiveTool::Pan => {
                self.pointer_down(point, DesktopModifiers::default())?;
                self.pointer_up(point, DesktopModifiers::default())
            }
        }
    }

    /// Starts a shared selection or marquee pointer gesture from desktop input.
    pub fn pointer_down(
        &mut self,
        point: Point,
        modifiers: DesktopModifiers,
    ) -> Result<(), String> {
        self.session
            .pointer_down(
                PresentationPointer::new(point.x as f64, point.y as f64),
                modifiers.into(),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates a shared selection or marquee pointer gesture from desktop input.
    pub fn pointer_move(
        &mut self,
        point: Point,
        modifiers: DesktopModifiers,
    ) -> Result<(), String> {
        self.session
            .pointer_move(
                PresentationPointer::new(point.x as f64, point.y as f64),
                modifiers.into(),
            )
            .map_err(|error| error.to_string())
    }

    /// Completes a shared selection or marquee pointer gesture from desktop input.
    pub fn pointer_up(&mut self, point: Point, modifiers: DesktopModifiers) -> Result<(), String> {
        self.session
            .pointer_up(
                PresentationPointer::new(point.x as f64, point.y as f64),
                modifiers.into(),
            )
            .map_err(|error| error.to_string())
    }

    /// Undoes one shared persistent command.
    pub fn undo(&mut self) -> Result<bool, String> {
        self.session.undo().map_err(|error| error.to_string())
    }

    /// Redoes one shared persistent command.
    pub fn redo(&mut self) -> Result<bool, String> {
        self.session.redo().map_err(|error| error.to_string())
    }

    /// Projects the active shared sheet for native painting.
    #[must_use]
    pub fn scene(&self) -> Option<Scene> {
        self.session.scene().ok()
    }

    /// Returns active-sheet symbol count for native status display.
    #[must_use]
    pub fn symbol_count(&self) -> usize {
        self.session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .map_or(0, |sheet| sheet.symbol_instances.len())
    }

    /// Returns active-sheet wire count for native status display.
    #[must_use]
    pub fn wire_count(&self) -> usize {
        self.session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .map_or(0, |sheet| sheet.wires.len())
    }

    /// Returns shared undo and redo depths for native status display.
    #[must_use]
    pub fn history_lengths(&self) -> (usize, usize) {
        self.session.history_lengths()
    }

    /// Returns how many entities are currently selected in the shared session.
    #[must_use]
    pub fn selected_count(&self) -> usize {
        self.session.selected_count()
    }

    /// Moves shared selection by a document-space delta.
    pub fn move_selected(&mut self, delta: Point) -> Result<(), String> {
        self.session
            .move_selection(delta)
            .map_err(|error| error.to_string())
    }

    /// Rotates shared selection through the supported quarter-turn command.
    pub fn rotate_selected(&mut self, quarter_turns: u8) -> Result<(), String> {
        if quarter_turns % 4 != 1 {
            return Err("desktop rotation currently supports one quarter turn".to_owned());
        }
        self.session
            .rotate_selection_90()
            .map_err(|error| error.to_string())
    }

    /// Mirrors shared selection through command history.
    pub fn mirror_selected(&mut self) -> Result<(), String> {
        self.session
            .mirror_selection()
            .map_err(|error| error.to_string())
    }

    /// Deletes shared selection through command history.
    pub fn delete_selected(&mut self) -> Result<(), String> {
        self.session
            .delete_selection()
            .map_err(|error| error.to_string())
    }

    /// Pans presentation only; saved geometry remains unchanged.
    pub fn pan(&mut self, delta: athena_geometry::WorldPoint) {
        self.session.pan_viewport(delta);
    }

    /// Zooms presentation only within shared editor bounds.
    pub fn zoom(&mut self, factor: f64) {
        self.session.zoom_viewport(factor);
    }

    /// Converts native canvas coordinates to rounded document coordinates.
    pub fn canvas_point_to_world(&self, point: Point) -> Point {
        let world = self.session.presentation().viewport.viewport_to_world(
            athena_geometry::WorldPoint::new(point.x as f64, point.y as f64),
        );
        Point::new(world.x.round() as i64, world.y.round() as i64)
    }

    /// Test adapter for a complete pointer marquee gesture.
    pub fn pointer_drag_for_test(
        &mut self,
        start: Point,
        end: Point,
        modifiers: DesktopModifiers,
    ) -> Result<(), String> {
        self.pointer_down(start, modifiers)?;
        self.pointer_move(end, modifiers)?;
        self.pointer_up(end, modifiers)
    }

    /// Test adapter that selects a deterministic wire by sorted collection index.
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

    /// Test adapter that selects a deterministic symbol by sorted collection index.
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

    /// Test adapter for moving one selected wire vertex through shared history.
    pub fn drag_wire_vertex_for_test(
        &mut self,
        vertex_index: usize,
        _start: Point,
        end: Point,
    ) -> Result<(), String> {
        let wire_id = self
            .session
            .presentation()
            .selected
            .iter()
            .find_map(|item| match item {
                PresentationItemId::Wire(wire_id) => Some(*wire_id),
                _ => None,
            })
            .ok_or_else(|| "a wire must be selected before moving a vertex".to_owned())?;
        self.session
            .move_wire_vertex(wire_id, vertex_index, end)
            .map_err(|error| error.to_string())
    }

    /// Inserts an interior vertex on the selected wire through shared history.
    pub fn insert_selected_wire_vertex_for_test(
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
    pub fn delete_selected_wire_vertex_for_test(
        &mut self,
        vertex_index: usize,
    ) -> Result<(), String> {
        let wire_id = self.selected_wire_id()?;
        self.session
            .delete_wire_vertex(wire_id, vertex_index)
            .map_err(|error| error.to_string())
    }

    /// Test adapter for the inspector's selected-symbol reference field.
    pub fn set_selected_symbol_reference_for_test(&mut self, value: &str) -> Result<(), String> {
        let symbol_id = self
            .session
            .presentation()
            .selected
            .iter()
            .find_map(|item| match item {
                PresentationItemId::Symbol(symbol_id) => Some(*symbol_id),
                _ => None,
            })
            .ok_or_else(|| "a symbol must be selected before editing its reference".to_owned())?;
        self.session
            .set_field_value(
                FieldTarget::Symbol(symbol_id),
                "reference",
                Some(FieldValue::Text(value.to_owned())),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates the selected symbol reference from a native inspector control.
    pub fn set_selected_symbol_reference(&mut self, value: &str) -> Result<(), String> {
        self.set_selected_symbol_reference_for_test(value)
    }

    /// Returns the selected symbol's inspector reference value.
    #[must_use]
    pub fn selected_symbol_reference_for_test(&self) -> Option<String> {
        let symbol_id =
            self.session
                .presentation()
                .selected
                .iter()
                .find_map(|item| match item {
                    PresentationItemId::Symbol(symbol_id) => Some(*symbol_id),
                    _ => None,
                })?;
        match self
            .session
            .state()
            .project()
            .symbol_instance(self.active_sheet_id(), symbol_id)?
            .fields
            .get("reference")?
        {
            FieldValue::Text(value) => Some(value.clone()),
            FieldValue::Integer(value) => Some(value.to_string()),
            FieldValue::Boolean(value) => Some(value.to_string()),
        }
    }

    /// Test adapter for the inspector's selected-symbol description field.
    pub fn set_selected_symbol_description_for_test(&mut self, value: &str) -> Result<(), String> {
        let symbol_id = self.selected_symbol_id()?;
        self.session
            .set_field_value(
                FieldTarget::Symbol(symbol_id),
                "description",
                Some(FieldValue::Text(value.to_owned())),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates the selected symbol description from a native inspector control.
    pub fn set_selected_symbol_description(&mut self, value: &str) -> Result<(), String> {
        self.set_selected_symbol_description_for_test(value)
    }

    /// Returns the selected symbol's inspector description value.
    #[must_use]
    pub fn selected_symbol_description_for_test(&self) -> Option<String> {
        self.selected_symbol_field("description")
    }

    /// Test adapter for the inspector's selected-wire label field.
    pub fn set_selected_wire_label_for_test(&mut self, value: &str) -> Result<(), String> {
        let wire_id = self.selected_wire_id()?;
        self.session
            .set_field_value(
                FieldTarget::Wire(wire_id),
                "label",
                Some(FieldValue::Text(value.to_owned())),
            )
            .map_err(|error| error.to_string())
    }

    /// Updates the selected wire label from a native inspector control.
    pub fn set_selected_wire_label(&mut self, value: &str) -> Result<(), String> {
        self.set_selected_wire_label_for_test(value)
    }

    /// Returns the selected wire's inspector label value.
    #[must_use]
    pub fn selected_wire_label_for_test(&self) -> Option<String> {
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

    /// Test adapter for sheet name and grid settings owned by shared history.
    pub fn set_sheet_properties_for_test(
        &mut self,
        name: &str,
        grid_visible: bool,
        grid_spacing: i64,
    ) -> Result<(), String> {
        let mut settings = self
            .session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .ok_or_else(|| "active sheet no longer exists".to_owned())?
            .settings
            .clone();
        settings.grid_visible = grid_visible;
        settings.grid_spacing = grid_spacing;
        self.session
            .rename_active_sheet(name)
            .map_err(|error| error.to_string())?;
        self.session
            .apply_sheet_settings(settings)
            .map_err(|error| error.to_string())
    }

    /// Updates active sheet name and visible grid settings from native controls.
    pub fn set_sheet_properties(
        &mut self,
        name: &str,
        grid_visible: bool,
        grid_spacing: i64,
    ) -> Result<(), String> {
        self.set_sheet_properties_for_test(name, grid_visible, grid_spacing)
    }

    /// Returns the current active sheet name.
    #[must_use]
    pub fn sheet_name_for_test(&self) -> String {
        self.session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .map_or_else(String::new, |sheet| sheet.name.clone())
    }

    /// Returns the active sheet grid enablement and spacing.
    #[must_use]
    pub fn sheet_grid_for_test(&self) -> (bool, i64) {
        self.session
            .state()
            .project()
            .sheet(self.active_sheet_id())
            .map_or((false, 0), |sheet| {
                (sheet.settings.grid_visible, sheet.settings.grid_spacing)
            })
    }

    /// Saves the shared project snapshot to a native filesystem path.
    pub fn save_to_path(&self, path: impl Into<std::path::PathBuf>) -> Result<(), String> {
        let mut store = FileSnapshotStore::new(path);
        let bytes = athena_format::SnapshotStore::encode_snapshot(self.session.state().project())
            .map_err(|e| e.to_string())?;
        store.write_snapshot(&bytes).map_err(|e| e.to_string())
    }

    /// Loads a validated project snapshot into a fresh shared session.
    pub fn load_from_path(&mut self, path: impl Into<std::path::PathBuf>) -> Result<(), String> {
        let store = FileSnapshotStore::new(path);
        let bytes = store.read_snapshot().map_err(|e| e.to_string())?;
        let project =
            athena_format::SnapshotStore::decode_snapshot(&bytes).map_err(|e| e.to_string())?;
        let active_sheet_id = project.sheet_order()[0];
        self.session = EditorSession::new(project, active_sheet_id);
        Ok(())
    }

    fn place_symbol(
        &mut self,
        definition_id: athena_domain::SymbolDefinitionId,
        point: Point,
    ) -> Result<(), String> {
        let record = self
            .catalog
            .get(definition_id)
            .ok_or_else(|| "selected symbol definition is unavailable".to_owned())?;
        let mut symbol = SymbolInstance::new(definition_id);
        symbol.position = point;
        for template in &record.terminals {
            let terminal = Terminal::new(
                symbol.id,
                template.name.clone(),
                template.electrical_kind,
                Point::new(
                    point.x + template.position.x.round() as i64,
                    point.y + template.position.y.round() as i64,
                ),
            );
            symbol.add_terminal(terminal);
        }
        let symbol_id = symbol.id;
        self.session
            .apply_command(EditorCommand::PlaceSymbol {
                sheet_id: self.active_sheet_id(),
                symbol,
            })
            .map_err(|error| error.to_string())?;
        self.select(Some(PresentationItemId::Symbol(symbol_id)));
        self.active_tool = ActiveTool::Select;
        Ok(())
    }

    fn handle_wire_click(
        &mut self,
        start: Option<athena_domain::TerminalId>,
        point: Point,
    ) -> Result<(), String> {
        let terminal = CanvasInput::new(point)
            .terminal_at(self.scene().as_ref())
            .ok_or_else(|| "wire endpoints must be symbol terminals".to_owned())?;
        if let Some(start) = start {
            if start == terminal.0 {
                return Ok(());
            }
            let first = terminal_position(
                self.session.state().project(),
                self.active_sheet_id(),
                start,
            )?;
            let second = terminal_position(
                self.session.state().project(),
                self.active_sheet_id(),
                terminal.0,
            )?;
            let route = if first.x == second.x || first.y == second.y {
                vec![first, second]
            } else {
                vec![first, Point::new(second.x, first.y), second]
            };
            let wire = athena_domain::Wire::new(
                athena_domain::WireEndpoint::Terminal(start),
                athena_domain::WireEndpoint::Terminal(terminal.0),
                route,
            );
            self.session
                .apply_command(EditorCommand::CreateWire {
                    sheet_id: self.active_sheet_id(),
                    wire,
                })
                .map_err(|error| error.to_string())?;
            self.active_tool = ActiveTool::Select;
        } else {
            self.active_tool = ActiveTool::Wire {
                start: Some(terminal.0),
            };
        }
        Ok(())
    }

    fn selected_symbol_id(&self) -> Result<athena_domain::SymbolInstanceId, String> {
        self.session
            .presentation()
            .selected
            .iter()
            .find_map(|item| match item {
                PresentationItemId::Symbol(symbol_id) => Some(*symbol_id),
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
                PresentationItemId::Wire(wire_id) => Some(*wire_id),
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

fn field_text(value: &FieldValue) -> Option<String> {
    match value {
        FieldValue::Text(value) => Some(value.clone()),
        FieldValue::Integer(value) => Some(value.to_string()),
        FieldValue::Boolean(value) => Some(value.to_string()),
    }
}

fn terminal_position(
    project: &Project,
    sheet_id: athena_domain::SheetId,
    terminal_id: athena_domain::TerminalId,
) -> Result<Point, String> {
    project
        .terminal(sheet_id, terminal_id)
        .map(|terminal| terminal.position)
        .ok_or_else(|| "selected terminal no longer exists".to_owned())
}

/// Launches the native GPUI workbench.
pub fn run() {
    crate::panels::run_native_shell();
}
