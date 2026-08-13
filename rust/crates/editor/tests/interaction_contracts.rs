use athena_domain::{
    ElectricalKind, Point, Project, SymbolDefinition, SymbolInstance, Terminal, Wire, WireEndpoint,
};
use athena_editor::{EditorCommand, EditorSession, PointerModifiers};
use athena_geometry::WorldPoint;

fn world(x: i64, y: i64) -> WorldPoint {
    WorldPoint::new(x as f64, y as f64)
}

fn fixture_session_with_two_symbols() -> EditorSession {
    fixture_session_with_symbols([(100, 80), (200, 80)])
}

fn fixture_session_with_offset_wire_and_symbol() -> EditorSession {
    let mut session = fixture_session_with_symbols([(100, 80), (220, 120)]);
    let sheet_id = session.active_sheet_id();
    let symbol_ids = session
        .state()
        .project()
        .sheet(sheet_id)
        .expect("sheet exists")
        .symbol_instances
        .keys()
        .copied()
        .collect::<Vec<_>>();
    let sheet = session
        .state()
        .project()
        .sheet(sheet_id)
        .expect("sheet exists");
    let first_terminal = *sheet
        .symbol_instances
        .get(&symbol_ids[0])
        .and_then(|symbol| symbol.terminals.keys().next())
        .expect("first terminal exists");
    let second_terminal = *sheet
        .symbol_instances
        .get(&symbol_ids[1])
        .and_then(|symbol| symbol.terminals.keys().next())
        .expect("second terminal exists");
    session
        .apply_command(EditorCommand::CreateWire {
            sheet_id,
            wire: Wire::new(
                WireEndpoint::Terminal(first_terminal),
                WireEndpoint::Terminal(second_terminal),
                vec![
                    Point::new(120, 80),
                    Point::new(120, 120),
                    Point::new(200, 120),
                ],
            ),
        })
        .expect("wire is valid");
    session.refresh_scene().expect("scene refreshes");
    session
}

fn fixture_session_with_symbols(positions: [(i64, i64); 2]) -> EditorSession {
    let mut project = Project::new("Interaction contracts");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition IDs are unique");
    let sheet_id = project.sheet_order()[0];

    for (x, y) in positions {
        let mut symbol = SymbolInstance::new(definition_id);
        symbol.position = Point::new(x, y);
        symbol.add_terminal(Terminal::new(
            symbol.id,
            "1",
            ElectricalKind::Passive,
            Point::new(x + 20, y),
        ));
        project
            .sheet_mut(sheet_id)
            .expect("sheet exists")
            .add_symbol(symbol)
            .expect("symbol IDs are unique");
    }

    let mut session = EditorSession::new(project, sheet_id);
    session.refresh_scene().expect("scene refreshes");
    session
}

#[test]
fn empty_click_clears_selection() {
    let mut session = fixture_session_with_two_symbols();
    let first = session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists")
        .symbol_instances
        .keys()
        .next()
        .copied()
        .expect("first symbol exists");

    session
        .select_only_symbol(first)
        .expect("symbol can be selected");
    session
        .pointer_down(world(300, 300), PointerModifiers::default())
        .expect("pointer down succeeds");
    session
        .pointer_up(world(300, 300), PointerModifiers::default())
        .expect("pointer up succeeds");

    assert!(session.presentation().selected.is_empty());
}

#[test]
fn shift_click_toggles_symbol_selection() {
    let mut session = fixture_session_with_two_symbols();
    let ids = session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists")
        .symbol_instances
        .keys()
        .copied()
        .collect::<Vec<_>>();

    session
        .pointer_down(world(100, 80), PointerModifiers::default())
        .expect("first symbol hit");
    session
        .pointer_up(world(100, 80), PointerModifiers::default())
        .expect("first symbol release");
    session
        .pointer_down(
            world(200, 80),
            PointerModifiers {
                shift: true,
                ..PointerModifiers::default()
            },
        )
        .expect("second symbol hit");
    session
        .pointer_up(
            world(200, 80),
            PointerModifiers {
                shift: true,
                ..PointerModifiers::default()
            },
        )
        .expect("second symbol release");

    assert_eq!(session.presentation().selected.len(), 2);
    assert!(session.is_symbol_selected(ids[0]));
    assert!(session.is_symbol_selected(ids[1]));
}

#[test]
fn marquee_direction_changes_selection_rule() {
    let mut session = fixture_session_with_offset_wire_and_symbol();

    session
        .pointer_down(world(0, 0), PointerModifiers::default())
        .expect("marquee starts");
    session
        .pointer_move(world(80, 40), PointerModifiers::default())
        .expect("marquee updates");
    assert_eq!(
        session.debug_marquee_mode(),
        Some(athena_editor::DebugMarqueeMode::Enclosed)
    );
    session
        .pointer_up(world(80, 40), PointerModifiers::default())
        .expect("marquee completes");

    session
        .pointer_down(world(80, 40), PointerModifiers::default())
        .expect("reverse marquee starts");
    session
        .pointer_move(world(0, 0), PointerModifiers::default())
        .expect("reverse marquee updates");
    assert_eq!(
        session.debug_marquee_mode(),
        Some(athena_editor::DebugMarqueeMode::Touched)
    );
}
