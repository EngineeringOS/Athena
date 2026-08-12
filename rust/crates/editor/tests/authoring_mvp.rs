use athena_domain::{Point, Wire, WireEndpoint};
use athena_editor::{EditorCommand, EditorState, History, ItemId, snapshot_bytes, snapshots_equal};
use athena_format::SnapshotStore;
use athena_library::{SearchQuery, SymbolCatalog};
use athena_render::{EditorPresentation, project_sheet};

#[test]
fn authoring_mvp_round_trips_project_history_snapshot_and_scene() {
    let catalog = SymbolCatalog::with_built_ins();
    let resistor = catalog
        .search(&SearchQuery::new("resistor"))
        .into_iter()
        .next()
        .expect("built-in resistor exists")
        .definition
        .clone();
    let power = catalog
        .search(&SearchQuery::new("power terminal"))
        .into_iter()
        .find(|result| result.definition.name == "Power terminal")
        .expect("built-in power terminal exists")
        .definition
        .clone();
    let mut project = athena_domain::Project::new("MVP acceptance");
    project
        .add_symbol_definition(resistor)
        .expect("resistor definition registers");
    project
        .add_symbol_definition(power)
        .expect("power definition registers");
    let sheet_id = project.sheet_order()[0];
    let mut state = EditorState::new(project);
    let mut history = History::new(32);
    let resistor_definition = state
        .project()
        .symbol_definitions()
        .values()
        .find(|definition| definition.name == "Resistor")
        .expect("resistor definition available")
        .id;
    let power_definition = state
        .project()
        .symbol_definitions()
        .values()
        .find(|definition| definition.name == "Power terminal")
        .expect("power definition available")
        .id;
    let mut resistor_instance = athena_domain::SymbolInstance::new(resistor_definition);
    resistor_instance.position = Point::new(100, 80);
    let resistor_terminal_id = resistor_instance.add_terminal(athena_domain::Terminal::new(
        resistor_instance.id,
        "1",
        athena_domain::ElectricalKind::Passive,
        Point::new(120, 80),
    ));
    let mut power_instance = athena_domain::SymbolInstance::new(power_definition);
    power_instance.position = Point::new(200, 80);
    let power_terminal_id = power_instance.add_terminal(athena_domain::Terminal::new(
        power_instance.id,
        "PWR",
        athena_domain::ElectricalKind::Power,
        Point::new(200, 80),
    ));
    history
        .apply(
            &mut state,
            EditorCommand::PlaceSymbol {
                sheet_id,
                symbol: resistor_instance,
            },
        )
        .expect("resistor places");
    history
        .apply(
            &mut state,
            EditorCommand::PlaceSymbol {
                sheet_id,
                symbol: power_instance,
            },
        )
        .expect("power terminal places");
    history
        .apply(
            &mut state,
            EditorCommand::CreateWire {
                sheet_id,
                wire: Wire::new(
                    WireEndpoint::Terminal(resistor_terminal_id),
                    WireEndpoint::Terminal(power_terminal_id),
                    vec![Point::new(120, 80), Point::new(200, 80)],
                ),
            },
        )
        .expect("wire connects terminals");
    let before_move = state.project().clone();
    let resistor_id = state
        .project()
        .sheet(sheet_id)
        .expect("sheet exists")
        .symbol_instances
        .keys()
        .next()
        .copied()
        .expect("resistor exists");
    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                sheet_id,
                items: vec![ItemId::Symbol(resistor_id)],
                delta: Point::new(10, 0),
            },
        )
        .expect("resistor moves");
    history.undo(&mut state).expect("move undo succeeds");
    assert!(snapshots_equal(&before_move, state.project()).expect("snapshots compare"));
    history.redo(&mut state).expect("move redo succeeds");
    let bytes = snapshot_bytes(state.project()).expect("snapshot encodes");
    let decoded = SnapshotStore::decode_snapshot(&bytes).expect("snapshot decodes");
    assert!(snapshots_equal(state.project(), &decoded).expect("decoded snapshot matches"));
    let scene_a = project_sheet(state.project(), sheet_id, &EditorPresentation::default())
        .expect("scene projects");
    let scene_b = project_sheet(&decoded, sheet_id, &EditorPresentation::default())
        .expect("decoded scene projects");
    assert_eq!(scene_a, scene_b);
}
