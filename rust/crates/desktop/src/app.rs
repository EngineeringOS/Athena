use crate::storage::FileSnapshotStore;
use athena_domain::{Point, Project, SymbolInstance, Terminal};
use athena_editor::ItemId;
use athena_editor::{EditorCommand, EditorState, History};
use athena_editor::{SnapshotSink, SnapshotSource};
use athena_library::{SearchQuery, SymbolCatalog};
use athena_render::{EditorPresentation, PresentationItemId, Scene, project_sheet};

use crate::input::{ActiveTool, CanvasInput};

/// Catalog data rendered by the native symbol library panel.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct CatalogSymbol {
    pub definition_id: athena_domain::SymbolDefinitionId,
    pub name: String,
}

/// Desktop-owned presentation and tool state. The editable project, commands,
/// history, and scene projection remain in shared Rust crates.
#[derive(Clone, Debug)]
pub struct DesktopEditor {
    catalog: SymbolCatalog,
    state: EditorState,
    history: History,
    active_sheet_id: athena_domain::SheetId,
    presentation: EditorPresentation,
    active_tool: ActiveTool,
}

impl DesktopEditor {
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
            state: EditorState::new(project),
            history: History::new(200),
            active_sheet_id,
            presentation: EditorPresentation::default(),
            active_tool: ActiveTool::Select,
        }
    }

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

    #[must_use]
    pub fn project_name(&self) -> &str {
        &self.state.project().name
    }

    #[must_use]
    pub const fn active_sheet_id(&self) -> athena_domain::SheetId {
        self.active_sheet_id
    }

    #[must_use]
    pub fn active_tool(&self) -> &ActiveTool {
        &self.active_tool
    }

    pub fn begin_placement(&mut self, definition_id: athena_domain::SymbolDefinitionId) {
        self.active_tool = ActiveTool::PlaceSymbol(definition_id);
    }

    pub fn begin_wiring(&mut self) {
        self.active_tool = ActiveTool::Wire { start: None };
    }

    pub fn select(&mut self, item: Option<PresentationItemId>) {
        self.presentation.selected.clear();
        if let Some(item) = item {
            self.presentation.selected.insert(item);
        }
    }

    pub fn canvas_click(&mut self, point: Point) -> Result<(), String> {
        match self.active_tool.clone() {
            ActiveTool::PlaceSymbol(definition_id) => self.place_symbol(definition_id, point),
            ActiveTool::Wire { start } => self.handle_wire_click(start, point),
            ActiveTool::Select | ActiveTool::Pan => {
                let input = CanvasInput::new(point);
                self.select(input.hit_item(self.scene().as_ref()));
                Ok(())
            }
        }
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

    #[must_use]
    pub fn scene(&self) -> Option<Scene> {
        project_sheet(
            self.state.project(),
            self.active_sheet_id,
            &self.presentation,
        )
    }

    #[must_use]
    pub fn symbol_count(&self) -> usize {
        self.state
            .project()
            .sheet(self.active_sheet_id)
            .map_or(0, |sheet| sheet.symbol_instances.len())
    }

    #[must_use]
    pub fn wire_count(&self) -> usize {
        self.state
            .project()
            .sheet(self.active_sheet_id)
            .map_or(0, |sheet| sheet.wires.len())
    }

    #[must_use]
    pub fn history_lengths(&self) -> (usize, usize) {
        (self.history.undo_len(), self.history.redo_len())
    }

    pub fn move_selected(&mut self, delta: Point) -> Result<(), String> {
        let items = self
            .presentation
            .selected
            .iter()
            .copied()
            .map(|item| match item {
                PresentationItemId::Symbol(id) => ItemId::Symbol(id),
                PresentationItemId::Wire(id) => ItemId::Wire(id),
                PresentationItemId::Junction(id) => ItemId::Junction(id),
                PresentationItemId::Annotation(id) => ItemId::Annotation(id),
            })
            .collect::<Vec<_>>();
        if items.is_empty() {
            return Ok(());
        }
        self.history
            .apply(
                &mut self.state,
                EditorCommand::MoveItems {
                    sheet_id: self.active_sheet_id,
                    items,
                    delta,
                },
            )
            .map_err(|e| e.to_string())
    }

    pub fn rotate_selected(&mut self, quarter_turns: u8) -> Result<(), String> {
        let items = self
            .presentation
            .selected
            .iter()
            .copied()
            .map(|item| match item {
                PresentationItemId::Symbol(id) => ItemId::Symbol(id),
                PresentationItemId::Wire(id) => ItemId::Wire(id),
                PresentationItemId::Junction(id) => ItemId::Junction(id),
                PresentationItemId::Annotation(id) => ItemId::Annotation(id),
            })
            .collect::<Vec<_>>();
        if items.is_empty() {
            return Ok(());
        }
        self.history
            .apply(
                &mut self.state,
                EditorCommand::RotateItems {
                    sheet_id: self.active_sheet_id,
                    items,
                    quarter_turns,
                },
            )
            .map_err(|e| e.to_string())
    }

    pub fn mirror_selected(&mut self) -> Result<(), String> {
        let items = self
            .presentation
            .selected
            .iter()
            .copied()
            .map(|item| match item {
                PresentationItemId::Symbol(id) => ItemId::Symbol(id),
                PresentationItemId::Wire(id) => ItemId::Wire(id),
                PresentationItemId::Junction(id) => ItemId::Junction(id),
                PresentationItemId::Annotation(id) => ItemId::Annotation(id),
            })
            .collect::<Vec<_>>();
        if items.is_empty() {
            return Ok(());
        }
        self.history
            .apply(
                &mut self.state,
                EditorCommand::MirrorItems {
                    sheet_id: self.active_sheet_id,
                    items,
                },
            )
            .map_err(|e| e.to_string())
    }

    pub fn delete_selected(&mut self) -> Result<(), String> {
        let items = self
            .presentation
            .selected
            .iter()
            .copied()
            .map(|item| match item {
                PresentationItemId::Symbol(id) => ItemId::Symbol(id),
                PresentationItemId::Wire(id) => ItemId::Wire(id),
                PresentationItemId::Junction(id) => ItemId::Junction(id),
                PresentationItemId::Annotation(id) => ItemId::Annotation(id),
            })
            .collect::<Vec<_>>();
        if items.is_empty() {
            return Ok(());
        }
        self.history
            .apply(
                &mut self.state,
                EditorCommand::DeleteItems {
                    sheet_id: self.active_sheet_id,
                    items,
                },
            )
            .map_err(|e| e.to_string())?;
        self.presentation.selected.clear();
        Ok(())
    }

    pub fn pan(&mut self, delta: athena_geometry::WorldPoint) {
        self.presentation.viewport.origin.x += delta.x;
        self.presentation.viewport.origin.y += delta.y;
    }

    pub fn zoom(&mut self, factor: f64) {
        self.presentation.viewport.zoom =
            (self.presentation.viewport.zoom * factor).clamp(0.25, 4.0);
    }

    pub fn canvas_point_to_world(&self, point: Point) -> Point {
        let world = self
            .presentation
            .viewport
            .viewport_to_world(athena_geometry::WorldPoint::new(
                point.x as f64,
                point.y as f64,
            ));
        Point::new(world.x.round() as i64, world.y.round() as i64)
    }

    pub fn save_to_path(&self, path: impl Into<std::path::PathBuf>) -> Result<(), String> {
        let mut store = FileSnapshotStore::new(path);
        let bytes = athena_format::SnapshotStore::encode_snapshot(self.state.project())
            .map_err(|e| e.to_string())?;
        store.write_snapshot(&bytes).map_err(|e| e.to_string())
    }

    pub fn load_from_path(&mut self, path: impl Into<std::path::PathBuf>) -> Result<(), String> {
        let store = FileSnapshotStore::new(path);
        let bytes = store.read_snapshot().map_err(|e| e.to_string())?;
        let project =
            athena_format::SnapshotStore::decode_snapshot(&bytes).map_err(|e| e.to_string())?;
        self.active_sheet_id = project.sheet_order()[0];
        self.state = EditorState::new(project);
        self.history = History::new(200);
        self.presentation = EditorPresentation::default();
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
        self.history
            .apply(
                &mut self.state,
                EditorCommand::PlaceSymbol {
                    sheet_id: self.active_sheet_id,
                    symbol,
                },
            )
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
            let first = terminal_position(self.state.project(), self.active_sheet_id, start)?;
            let second = terminal_position(self.state.project(), self.active_sheet_id, terminal.0)?;
            let wire = athena_domain::Wire::new(
                athena_domain::WireEndpoint::Terminal(start),
                athena_domain::WireEndpoint::Terminal(terminal.0),
                vec![first, second],
            );
            self.history
                .apply(
                    &mut self.state,
                    EditorCommand::CreateWire {
                        sheet_id: self.active_sheet_id,
                        wire,
                    },
                )
                .map_err(|error| error.to_string())?;
            self.active_tool = ActiveTool::Select;
        } else {
            self.active_tool = ActiveTool::Wire {
                start: Some(terminal.0),
            };
        }
        Ok(())
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
