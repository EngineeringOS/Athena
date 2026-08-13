use athena_domain::{
    ElectricalKind, Point, Project, SymbolDefinition, SymbolInstance, Terminal, Wire, WireEndpoint,
};
use athena_editor::{
    EditorCommand, EditorSession, InteractionState, MarqueeSelectionMode, PointerModifiers,
    PresentationPointer,
};
use athena_geometry::WorldPoint;
use athena_render::{PresentationItemId, Viewport};

fn canvas(x: i64, y: i64) -> PresentationPointer {
    PresentationPointer::new(x as f64, y as f64)
}

fn fixture_session_with_two_symbols() -> EditorSession {
    fixture_session_with_symbols([(100, 80), (200, 80)])
}

fn fixture_session_with_offset_wire_and_symbol() -> EditorSession {
    let mut session = fixture_session_with_symbols([(100, 80), (220, 120)]);
    let sheet_id = session.active_sheet_id();
    let sheet = session
        .state()
        .project()
        .sheet(sheet_id)
        .expect("sheet exists");
    // BTreeMap iteration is UUID order, not placement order. Choose the left
    // and right symbols by their actual document positions so the fixture wire
    // consistently crosses the marquee rectangle.
    let first_terminal = *sheet
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(100, 80))
        .and_then(|symbol| symbol.terminals.keys().next())
        .expect("left terminal exists");
    let second_terminal = *sheet
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(220, 120))
        .and_then(|symbol| symbol.terminals.keys().next())
        .expect("right terminal exists");
    let first_position = sheet
        .symbol_instances
        .values()
        .flat_map(|symbol| symbol.terminals.values())
        .find(|terminal| terminal.id == first_terminal)
        .expect("first terminal position exists")
        .position;
    let second_position = sheet
        .symbol_instances
        .values()
        .flat_map(|symbol| symbol.terminals.values())
        .find(|terminal| terminal.id == second_terminal)
        .expect("second terminal position exists")
        .position;
    session
        .apply_command(EditorCommand::CreateWire {
            sheet_id,
            wire: Wire::new(
                WireEndpoint::Terminal(first_terminal),
                WireEndpoint::Terminal(second_terminal),
                vec![
                    first_position,
                    Point::new(first_position.x, second_position.y),
                    second_position,
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
        .values()
        .find(|symbol| symbol.position == Point::new(100, 80))
        .map(|symbol| symbol.id)
        .expect("first symbol exists");

    session
        .select_only_symbol(first)
        .expect("symbol can be selected");
    session
        .pointer_down(canvas(300, 300), PointerModifiers::default())
        .expect("pointer down succeeds");
    session
        .pointer_up(canvas(300, 300), PointerModifiers::default())
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
        .pointer_down(canvas(100, 80), PointerModifiers::default())
        .expect("first symbol hit");
    session
        .pointer_up(canvas(100, 80), PointerModifiers::default())
        .expect("first symbol release");
    session
        .pointer_down(
            canvas(200, 80),
            PointerModifiers {
                shift: true,
                ..PointerModifiers::default()
            },
        )
        .expect("second symbol hit");
    session
        .pointer_up(
            canvas(200, 80),
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
        .pointer_down(canvas(0, 0), PointerModifiers::default())
        .expect("marquee starts");
    session
        .pointer_move(canvas(80, 40), PointerModifiers::default())
        .expect("marquee updates");
    assert!(matches!(
        session.interaction(),
        InteractionState::MarqueeSelecting(state)
            if state.start == PresentationPointer::new(0.0, 0.0)
                && state.current == PresentationPointer::new(80.0, 40.0)
    ));
    assert_eq!(
        session.marquee_selection_mode(),
        Some(MarqueeSelectionMode::Enclosed)
    );
    session
        .pointer_up(canvas(80, 40), PointerModifiers::default())
        .expect("marquee completes");

    session
        .pointer_down(canvas(80, 40), PointerModifiers::default())
        .expect("reverse marquee starts");
    session
        .pointer_move(canvas(0, 0), PointerModifiers::default())
        .expect("reverse marquee updates");
    assert_eq!(
        session.marquee_selection_mode(),
        Some(MarqueeSelectionMode::Touched)
    );
}

#[test]
fn marquee_selects_only_enclosed_items_left_to_right_and_touched_items_right_to_left() {
    let mut session = fixture_session_with_two_symbols();
    let partially_covered = session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists")
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(100, 80))
        .map(|symbol| symbol.id)
        .expect("partially covered symbol exists");

    session
        .pointer_down(canvas(80, 60), PointerModifiers::default())
        .expect("enclosed marquee starts on empty sheet");
    session
        .pointer_up(canvas(105, 85), PointerModifiers::default())
        .expect("enclosed marquee completes");
    assert!(
        !session.is_symbol_selected(partially_covered),
        "a partially covered symbol is not enclosed"
    );

    session
        .pointer_down(canvas(110, 60), PointerModifiers::default())
        .expect("touched marquee starts on empty sheet");
    session
        .pointer_up(canvas(90, 90), PointerModifiers::default())
        .expect("touched marquee completes");
    assert!(
        session.is_symbol_selected(partially_covered),
        "a partially covered symbol is touched"
    );
}

#[test]
fn shift_marquee_extends_and_toggles_the_existing_selection() {
    let mut session = fixture_session_with_two_symbols();
    let sheet = session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists");
    let first = sheet
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(100, 80))
        .map(|symbol| symbol.id)
        .expect("first symbol exists");
    let second = sheet
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(200, 80))
        .map(|symbol| symbol.id)
        .expect("second symbol exists");
    let shift = PointerModifiers {
        shift: true,
        ..PointerModifiers::default()
    };

    session
        .select_only_symbol(first)
        .expect("first symbol selects");
    session
        .pointer_down(canvas(190, 60), shift)
        .expect("shift marquee starts");
    session
        .pointer_up(canvas(210, 90), shift)
        .expect("shift marquee completes");
    assert_eq!(session.presentation().selected.len(), 2);
    assert!(session.is_symbol_selected(first));
    assert!(session.is_symbol_selected(second));

    session
        .pointer_down(canvas(190, 60), shift)
        .expect("toggle marquee starts");
    session
        .pointer_up(canvas(210, 90), shift)
        .expect("toggle marquee completes");
    assert_eq!(session.presentation().selected.len(), 1);
    assert!(session.is_symbol_selected(first));
    assert!(!session.is_symbol_selected(second));
}

#[test]
fn partial_wire_marquee_is_enclosed_only_right_to_left() {
    let mut session = fixture_session_with_offset_wire_and_symbol();
    let wire_id = *session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists")
        .wires
        .keys()
        .next()
        .expect("wire exists");

    session
        .pointer_down(canvas(100, 90), PointerModifiers::default())
        .expect("enclosed starts");
    session
        .pointer_up(canvas(130, 110), PointerModifiers::default())
        .expect("enclosed completes");
    assert!(
        !session
            .presentation()
            .selected
            .contains(&PresentationItemId::Wire(wire_id))
    );

    session
        .pointer_down(canvas(130, 90), PointerModifiers::default())
        .expect("touched starts");
    session
        .pointer_up(canvas(100, 110), PointerModifiers::default())
        .expect("touched completes");
    assert!(
        session
            .presentation()
            .selected
            .contains(&PresentationItemId::Wire(wire_id))
    );
}

#[test]
fn viewport_pointer_selects_and_marquees_using_the_scene_viewport() {
    let mut session = fixture_session_with_two_symbols();
    session.set_viewport(Viewport {
        origin: WorldPoint::new(50.0, 25.0),
        zoom: 2.0,
    });
    let viewport = session.presentation().viewport;
    let first_world = WorldPoint::new(100.0, 80.0);
    let first_canvas = viewport.world_to_viewport(first_world);
    let first = session
        .state()
        .project()
        .sheet(session.active_sheet_id())
        .expect("sheet exists")
        .symbol_instances
        .values()
        .find(|symbol| symbol.position == Point::new(100, 80))
        .map(|symbol| symbol.id)
        .expect("first symbol exists");

    session
        .pointer_down(
            PresentationPointer::new(first_canvas.x, first_canvas.y),
            PointerModifiers::default(),
        )
        .expect("viewport hit selects");
    session
        .pointer_up(
            PresentationPointer::new(first_canvas.x, first_canvas.y),
            PointerModifiers::default(),
        )
        .expect("viewport hit releases");
    assert!(session.is_symbol_selected(first));

    let start = viewport.world_to_viewport(WorldPoint::new(110.0, 60.0));
    let end = viewport.world_to_viewport(WorldPoint::new(90.0, 90.0));
    session
        .pointer_down(
            PresentationPointer::new(start.x, start.y),
            PointerModifiers::default(),
        )
        .expect("viewport marquee starts");
    session
        .pointer_up(
            PresentationPointer::new(end.x, end.y),
            PointerModifiers::default(),
        )
        .expect("viewport marquee completes");
    assert!(session.is_symbol_selected(first));
}

#[test]
fn dragging_a_selected_wire_vertex_commits_the_shared_vertex_command_on_release() {
    let mut session = fixture_session_with_offset_wire_and_symbol();
    let sheet_id = session.active_sheet_id();
    let (wire_id, original_vertex) = session
        .state()
        .project()
        .sheet(sheet_id)
        .expect("sheet exists")
        .wires
        .iter()
        .next()
        .map(|(id, wire)| (*id, wire.route[1]))
        .expect("fixture wire has an interior vertex");

    session
        .select_only(PresentationItemId::Wire(wire_id))
        .expect("wire selects before its handle is dragged");
    session
        .pointer_down(
            canvas(original_vertex.x, original_vertex.y),
            PointerModifiers::default(),
        )
        .expect("vertex handle starts a drag");
    assert!(matches!(
        session.interaction(),
        InteractionState::EditingWireVertex(state)
            if state.wire_id == wire_id && state.vertex_index == 1
    ));

    session
        .pointer_move(canvas(160, 120), PointerModifiers::default())
        .expect("vertex drag updates");
    session
        .pointer_up(canvas(160, 120), PointerModifiers::default())
        .expect("vertex drag commits");

    let wire = session
        .state()
        .project()
        .wire(sheet_id, wire_id)
        .expect("wire remains in the active sheet");
    assert!(wire.route.contains(&Point::new(160, 120)));
    assert_eq!(session.history_lengths(), (2, 0));
}
