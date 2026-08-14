use athena_domain::{
    ElectricalKind, FieldValue, Junction, Point, Project, SchematicSettings, SymbolDefinition,
    SymbolInstance, Terminal, Wire, WireEndpoint,
};
use athena_editor::{
    CommandEnvelope, EditorCommand, EditorState, FieldTarget, History, ItemId, WireSide,
    snapshot_bytes,
};
use uuid::Uuid;

fn state_with_definition() -> (
    EditorState,
    athena_domain::FolioId,
    athena_domain::SymbolDefinitionId,
) {
    let mut project = Project::new("Motor control");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition IDs are unique");
    let folio_id = project.folio_order()[0];

    (EditorState::new(project), folio_id, definition_id)
}

fn symbol(definition_id: athena_domain::SymbolDefinitionId, position: Point) -> SymbolInstance {
    let mut symbol = SymbolInstance::new(definition_id);
    symbol.position = position;
    symbol.add_terminal(Terminal::new(
        symbol.id,
        "1",
        ElectricalKind::Passive,
        position,
    ));
    symbol
}

fn place(state: &mut EditorState, folio_id: athena_domain::FolioId, symbol: SymbolInstance) {
    state
        .apply(EditorCommand::PlaceSymbol { folio_id, symbol })
        .expect("placement should be valid");
}

#[test]
fn places_moves_and_deletes_a_symbol() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;

    place(&mut state, folio_id, symbol);
    state
        .apply(EditorCommand::MoveItems {
            folio_id,
            items: vec![ItemId::Symbol(symbol_id)],
            delta: Point::new(5, -10),
        })
        .expect("move should be valid");

    assert_eq!(
        state
            .project()
            .symbol_instance(folio_id, symbol_id)
            .unwrap()
            .position,
        Point::new(15, 0)
    );

    state
        .apply(EditorCommand::DeleteItems {
            folio_id,
            items: vec![ItemId::Symbol(symbol_id)],
        })
        .expect("delete should be valid");

    assert!(
        state
            .project()
            .symbol_instance(folio_id, symbol_id)
            .is_none()
    );
}

#[test]
fn creates_splits_and_deletes_a_wire() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first_symbol = symbol(definition_id, Point::new(0, 0));
    let first_terminal = *first_symbol.terminals.keys().next().unwrap();
    let second_symbol = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second_symbol.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first_symbol);
    place(&mut state, folio_id, second_symbol);

    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");

    let junction = Junction::new(Point::new(10, 0));
    let first_wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Junction(junction.id),
        vec![Point::new(0, 0), Point::new(10, 0)],
    );
    let second_wire = Wire::new(
        WireEndpoint::Junction(junction.id),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(10, 0), Point::new(20, 0)],
    );
    let first_wire_id = first_wire.id;
    let second_wire_id = second_wire.id;
    state
        .apply(EditorCommand::SplitWire {
            folio_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        })
        .expect("split should replace the original wire");

    assert!(state.project().wire(folio_id, wire_id).is_none());
    assert!(state.project().wire(folio_id, first_wire_id).is_some());
    assert!(state.project().wire(folio_id, second_wire_id).is_some());

    state
        .apply(EditorCommand::DeleteWire {
            folio_id,
            wire_id: first_wire_id,
        })
        .expect("wire exists");
    assert!(state.project().wire(folio_id, first_wire_id).is_none());
}

#[test]
fn sets_symbol_field_values_and_sheet_settings() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(0, 0));
    let symbol_id = symbol.id;
    place(&mut state, folio_id, symbol);

    state
        .apply(EditorCommand::SetFieldValue {
            folio_id,
            target: FieldTarget::Symbol(symbol_id),
            field: "reference".into(),
            value: Some(FieldValue::Text("R1".into())),
        })
        .expect("symbol field can be set");
    assert_eq!(
        state
            .project()
            .symbol_instance(folio_id, symbol_id)
            .unwrap()
            .fields["reference"],
        FieldValue::Text("R1".into())
    );

    let settings = SchematicSettings {
        page_width: 594,
        page_height: 420,
        grid_spacing: 5,
        grid_visible: true,
        snap_enabled: false,
    };
    state
        .apply(EditorCommand::ApplySchematicSettings {
            folio_id,
            settings: settings.clone(),
        })
        .expect("folio settings can be applied");

    assert_eq!(state.project().folio(folio_id).unwrap().settings, settings);
}

#[test]
fn rejects_unknown_terminal_wire_without_changing_the_snapshot() {
    let (mut state, folio_id, _) = state_with_definition();
    let before = snapshot_bytes(state.project()).expect("valid initial state");
    let wire = Wire::new(
        WireEndpoint::Terminal(athena_domain::TerminalId::new()),
        WireEndpoint::Junction(athena_domain::JunctionId::new()),
        vec![Point::new(0, 0), Point::new(10, 0)],
    );

    assert!(
        state
            .apply(EditorCommand::CreateWire { folio_id, wire })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("failed command preserves a valid project"),
        before
    );
}

#[test]
fn moves_multiple_selected_symbols_in_one_command() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let second = symbol(definition_id, Point::new(20, 10));
    let second_id = second.id;
    place(&mut state, folio_id, first);
    place(&mut state, folio_id, second);

    state
        .apply(EditorCommand::MoveItems {
            folio_id,
            items: vec![ItemId::Symbol(first_id), ItemId::Symbol(second_id)],
            delta: Point::new(-5, 15),
        })
        .expect("both selected symbols move atomically");

    assert_eq!(
        state
            .project()
            .symbol_instance(folio_id, first_id)
            .unwrap()
            .position,
        Point::new(-5, 15)
    );
    assert_eq!(
        state
            .project()
            .symbol_instance(folio_id, second_id)
            .unwrap()
            .position,
        Point::new(15, 25)
    );
}

#[test]
fn moving_a_symbol_moves_attached_wire_endpoint_vertices() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first.clone());
    place(&mut state, folio_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(10, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");

    state
        .apply(EditorCommand::MoveItems {
            folio_id,
            items: vec![ItemId::Symbol(first_id)],
            delta: Point::new(5, 5),
        })
        .expect("moving the symbol should move its attached wire endpoint");

    assert_eq!(
        state
            .project()
            .terminal(folio_id, first_terminal)
            .unwrap()
            .position,
        Point::new(5, 5)
    );
    assert_eq!(
        state.project().wire(folio_id, wire_id).unwrap().route,
        vec![Point::new(5, 5), Point::new(20, 5), Point::new(20, 0)],
        "moving an anchored endpoint introduces a deterministic elbow rather than persisting a diagonal segment"
    );
}

#[test]
fn moving_a_wire_without_its_owners_is_rejected_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first);
    place(&mut state, folio_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .unwrap();
    let before = snapshot_bytes(state.project()).unwrap();
    assert!(
        state
            .apply(EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Wire(wire_id)],
                delta: Point::new(5, 0)
            })
            .is_err()
    );
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
}

#[test]
fn moving_a_junction_without_its_wires_is_rejected_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_terminal = *first.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first);
    let junction = Junction::new(Point::new(10, 0));
    let junction_id = junction.id;
    state
        .apply(EditorCommand::RestoreItems {
            folio_id,
            remove: vec![],
            restore: vec![athena_editor::StoredItem::Junction(junction.clone())],
        })
        .unwrap();
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Junction(junction_id),
        vec![Point::new(0, 0), Point::new(10, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .unwrap();
    let before = snapshot_bytes(state.project()).unwrap();
    assert!(
        state
            .apply(EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Junction(junction_id)],
                delta: Point::new(0, 5)
            })
            .is_err()
    );
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
    assert_eq!(
        state.project().wire(folio_id, wire_id).unwrap().route,
        vec![Point::new(0, 0), Point::new(10, 0)]
    );
}

#[test]
fn moving_a_wire_with_only_one_terminal_owner_is_rejected() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first);
    place(&mut state, folio_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .unwrap();
    let before = snapshot_bytes(state.project()).unwrap();
    assert!(
        state
            .apply(EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Wire(wire_id), ItemId::Symbol(first_id)],
                delta: Point::new(5, 0)
            })
            .is_err()
    );
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
}

#[test]
fn rotates_and_mirrors_a_selected_symbol() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(0, 0));
    let symbol_id = symbol.id;
    place(&mut state, folio_id, symbol);

    state
        .apply(EditorCommand::RotateItems {
            folio_id,
            items: vec![ItemId::Symbol(symbol_id)],
            quarter_turns: 1,
        })
        .expect("rotation should be valid");
    state
        .apply(EditorCommand::MirrorItems {
            folio_id,
            items: vec![ItemId::Symbol(symbol_id)],
        })
        .expect("mirroring should be valid");

    let placed = state
        .project()
        .symbol_instance(folio_id, symbol_id)
        .unwrap();
    assert_eq!(placed.rotation_quarter_turns, 1);
    assert!(placed.mirrored);
}

#[test]
fn failed_undo_keeps_its_history_entry() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first.clone());
    place(&mut state, folio_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::DeleteWire { folio_id, wire_id })
        .expect("deleting the wire should apply");
    state
        .apply(EditorCommand::DeleteItems {
            folio_id,
            items: vec![ItemId::Symbol(first_id)],
        })
        .expect("independent deletion should diverge the current state");

    assert!(history.undo(&mut state).is_err());
    assert_eq!(history.undo_len(), 1);
    assert_eq!(history.redo_len(), 0);

    place(&mut state, folio_id, first);
    assert!(
        history
            .undo(&mut state)
            .expect("retained undo should apply")
    );
    assert_eq!(history.undo_len(), 0);
    assert_eq!(history.redo_len(), 1);
}

#[test]
fn failed_redo_keeps_its_history_entry() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, folio_id, first.clone());
    place(&mut state, folio_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    let wire_for_restore = wire.clone();
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::DeleteWire { folio_id, wire_id })
        .expect("deleting the wire should apply");
    assert!(
        history
            .undo(&mut state)
            .expect("undo should restore the wire")
    );
    state
        .apply(EditorCommand::DeleteItems {
            folio_id,
            items: vec![ItemId::Symbol(first_id)],
        })
        .expect("independent deletion should remove the restored wire");

    assert!(history.redo(&mut state).is_err());
    assert_eq!(history.undo_len(), 0);
    assert_eq!(history.redo_len(), 1);

    place(&mut state, folio_id, first);
    state
        .apply(EditorCommand::CreateWire {
            folio_id,
            wire: wire_for_restore,
        })
        .expect("restoring the wire directly makes the retained redo valid");
    assert!(
        history
            .redo(&mut state)
            .expect("retained redo should apply")
    );
    assert_eq!(history.undo_len(), 1);
    assert_eq!(history.redo_len(), 0);
}

#[test]
fn undo_restores_the_exact_previous_snapshot() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { folio_id, symbol })
        .expect("place should apply");
    let before_move = snapshot_bytes(state.project()).expect("snapshot is valid");

    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Symbol(symbol_id)],
                delta: Point::new(10, 0),
            },
        )
        .expect("move should apply");
    history.undo(&mut state).expect("undo should apply");

    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before_move
    );
}

#[test]
fn undoing_a_delete_restores_the_exact_previous_snapshot() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { folio_id, symbol })
        .expect("place should apply");
    let before_delete = snapshot_bytes(state.project()).expect("snapshot is valid");

    history
        .apply(
            &mut state,
            EditorCommand::DeleteItems {
                folio_id,
                items: vec![ItemId::Symbol(symbol_id)],
            },
        )
        .expect("delete should apply");
    history.undo(&mut state).expect("undo should apply");

    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before_delete
    );
}

#[test]
fn redo_reapplies_the_exact_command_result() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { folio_id, symbol })
        .expect("place should apply");
    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Symbol(symbol_id)],
                delta: Point::new(10, 0),
            },
        )
        .expect("move should apply");
    let after_move = snapshot_bytes(state.project()).expect("snapshot is valid");

    history.undo(&mut state).expect("undo should apply");
    history.redo(&mut state).expect("redo should apply");

    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        after_move
    );
}

#[test]
fn history_is_bounded_and_a_new_edit_invalidates_redo() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let first_symbol = symbol(definition_id, Point::new(0, 0));
    let first_symbol_id = first_symbol.id;
    let second_symbol = symbol(definition_id, Point::new(20, 0));
    let mut history = History::new(1);

    history
        .apply(
            &mut state,
            EditorCommand::PlaceSymbol {
                folio_id,
                symbol: first_symbol,
            },
        )
        .expect("first placement should apply");
    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                folio_id,
                items: vec![ItemId::Symbol(first_symbol_id)],
                delta: Point::new(5, 0),
            },
        )
        .expect("move should apply");

    assert_eq!(history.undo_len(), 1);
    assert!(history.undo(&mut state).expect("undo should apply"));
    assert_eq!(history.redo_len(), 1);

    history
        .apply(
            &mut state,
            EditorCommand::PlaceSymbol {
                folio_id,
                symbol: second_symbol,
            },
        )
        .expect("new edit should apply");

    assert_eq!(history.redo_len(), 0);
    assert!(!history.redo(&mut state).expect("redo should be empty"));
}

#[test]
fn command_envelope_round_trips_through_serde() {
    let (state, folio_id, definition_id) = state_with_definition();
    let command = EditorCommand::PlaceSymbol {
        folio_id,
        symbol: symbol(definition_id, Point::new(10, 20)),
    };
    let envelope = CommandEnvelope {
        operation_id: Uuid::new_v4(),
        project_id: state.project().id,
        folio_id,
        base_revision: state.revision(),
        author_id: Uuid::new_v4(),
        session_id: Uuid::new_v4(),
        command_version: 1,
        payload: command,
    };

    let encoded = serde_json::to_string(&envelope).expect("envelope should serialize");
    let decoded: CommandEnvelope =
        serde_json::from_str(&encoded).expect("envelope should deserialize");

    assert_eq!(decoded, envelope);
}

#[test]
fn wire_vertex_commands_normalize_routes_and_round_trip_through_history() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 20));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0), Point::new(20, 20)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");
    let mut history = History::new(8);

    history
        .apply(
            &mut state,
            EditorCommand::InsertWireVertex {
                folio_id,
                wire_id,
                segment_index: 0,
                position: Point::new(10, 0),
            },
        )
        .expect("an on-segment vertex can be inserted");
    assert_eq!(
        state
            .project()
            .wire(folio_id, wire_id)
            .expect("wire exists")
            .route,
        vec![Point::new(0, 0), Point::new(20, 0), Point::new(20, 20)],
        "route normalization removes the redundant colinear vertex"
    );

    history
        .apply(
            &mut state,
            EditorCommand::MoveWireVertex {
                folio_id,
                wire_id,
                vertex_index: 1,
                position: Point::new(10, 10),
            },
        )
        .expect("moving a bend preserves an orthogonal path");
    let moved_route = &state
        .project()
        .wire(folio_id, wire_id)
        .expect("wire exists")
        .route;
    assert!(
        moved_route
            .windows(2)
            .all(|segment| { segment[0].x == segment[1].x || segment[0].y == segment[1].y })
    );

    history
        .apply(
            &mut state,
            EditorCommand::DeleteWireVertex {
                folio_id,
                wire_id,
                vertex_index: 1,
            },
        )
        .expect("an interior wire vertex can be deleted");

    assert!(history.undo(&mut state).expect("delete can be undone"));
    assert!(history.undo(&mut state).expect("move can be undone"));
    assert!(history.undo(&mut state).expect("insert can be undone"));
    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before
    );
}

#[test]
fn reconnect_wire_endpoint_retargets_terminal_and_adds_a_minimal_elbow() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let original_end = symbol(definition_id, Point::new(20, 0));
    let original_end_terminal = *original_end
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let replacement_end = symbol(definition_id, Point::new(30, 10));
    let replacement_terminal = *replacement_end
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, original_end);
    place(&mut state, folio_id, replacement_end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(original_end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");

    let mut history = History::new(8);
    history
        .apply(
            &mut state,
            EditorCommand::ReconnectWireEndpoint {
                folio_id,
                wire_id,
                endpoint: WireSide::End,
                terminal_id: replacement_terminal,
            },
        )
        .expect("endpoint can be reconnected on the active folio");

    let wire = state
        .project()
        .wire(folio_id, wire_id)
        .expect("wire exists");
    assert_eq!(wire.end, WireEndpoint::Terminal(replacement_terminal));
    assert_eq!(
        wire.route,
        vec![Point::new(0, 0), Point::new(30, 0), Point::new(30, 10)]
    );
    assert!(history.undo(&mut state).expect("reconnect can be undone"));
    assert_eq!(
        state
            .project()
            .wire(folio_id, wire_id)
            .expect("wire exists")
            .end,
        WireEndpoint::Terminal(original_end_terminal)
    );
}

#[test]
fn reconnecting_to_the_other_endpoint_terminal_is_rejected_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start.terminals.keys().next().unwrap();
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().unwrap();
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .unwrap();
    let before = snapshot_bytes(state.project()).unwrap();
    assert!(
        state
            .apply(EditorCommand::ReconnectWireEndpoint {
                folio_id,
                wire_id,
                endpoint: WireSide::End,
                terminal_id: start_terminal
            })
            .is_err()
    );
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
}

#[test]
fn reconnecting_to_the_other_endpoint_terminal_rejects_a_noncollapsing_route() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start.terminals.keys().next().unwrap();
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().unwrap();
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![
            Point::new(0, 0),
            Point::new(0, 20),
            Point::new(20, 20),
            Point::new(20, 0),
        ],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .unwrap();
    let before = snapshot_bytes(state.project()).unwrap();

    assert!(matches!(
        state.apply(EditorCommand::ReconnectWireEndpoint {
            folio_id,
            wire_id,
            endpoint: WireSide::End,
            terminal_id: start_terminal,
        }),
        Err(athena_editor::ApplyError::WireEndpointsShareTerminal { .. })
    ));
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
}

#[test]
fn restoring_a_malformed_wire_is_rejected_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start.terminals.keys().next().unwrap();
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().unwrap();
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let malformed = Wire {
        route: vec![Point::new(1, 1)],
        ..wire
    };
    let before = snapshot_bytes(state.project()).unwrap();
    assert!(
        state
            .apply(EditorCommand::RestoreItems {
                folio_id,
                remove: vec![],
                restore: vec![athena_editor::StoredItem::Wire(malformed)]
            })
            .is_err()
    );
    assert_eq!(snapshot_bytes(state.project()).unwrap(), before);
}

#[test]
fn wire_vertex_commands_reject_invalid_indices_without_mutating_state() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("wire endpoints exist");
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");

    assert!(
        state
            .apply(EditorCommand::InsertWireVertex {
                folio_id,
                wire_id,
                segment_index: 1,
                position: Point::new(10, 0),
            })
            .is_err()
    );
    assert!(
        state
            .apply(EditorCommand::MoveWireVertex {
                folio_id,
                wire_id,
                vertex_index: 0,
                position: Point::new(0, 10),
            })
            .is_err()
    );
    assert!(
        state
            .apply(EditorCommand::DeleteWireVertex {
                folio_id,
                wire_id,
                vertex_index: 1,
            })
            .is_err()
    );
    assert!(
        state
            .apply(EditorCommand::MoveWireVertex {
                folio_id,
                wire_id,
                vertex_index: usize::MAX,
                position: Point::new(10, 10),
            })
            .is_err()
    );
    assert!(
        state
            .apply(EditorCommand::DeleteWireVertex {
                folio_id,
                wire_id,
                vertex_index: usize::MAX,
            })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before
    );
}

#[test]
fn commands_reject_an_invalid_initial_project_before_route_repair() {
    let mut project = Project::new("Invalid initial project");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition IDs are unique");
    let folio_id = project.folio_order()[0];
    let start = symbol(definition_id, Point::new(0, 0));
    let start_id = start.id;
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    let malformed_wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0)],
    );
    let folio = project.folio_mut(folio_id).expect("default folio exists");
    folio.add_symbol(start).expect("symbol ID is unique");
    folio.add_symbol(end).expect("symbol ID is unique");
    folio
        .add_wire(malformed_wire)
        .expect("wire ID is unique despite its invalid route");

    let mut state = EditorState::new(project);
    let before = state.project().clone();

    assert!(matches!(
        state.apply(EditorCommand::MoveItems {
            folio_id,
            items: vec![ItemId::Symbol(start_id)],
            delta: Point::new(10, 0),
        }),
        Err(athena_editor::ApplyError::InvalidProject(_))
    ));
    assert_eq!(state.project(), &before);
}

#[test]
fn create_wire_rejects_routes_that_are_unanchored_or_non_orthogonal_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 20));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");

    for route in [
        vec![Point::new(0, 0)],
        vec![Point::new(5, 0), Point::new(20, 0), Point::new(20, 20)],
        vec![Point::new(0, 0), Point::new(20, 20)],
    ] {
        let wire = Wire::new(
            WireEndpoint::Terminal(start_terminal),
            WireEndpoint::Terminal(end_terminal),
            route,
        );
        assert!(
            state
                .apply(EditorCommand::CreateWire { folio_id, wire })
                .is_err(),
            "invalid route must be rejected before state changes"
        );
        assert_eq!(
            snapshot_bytes(state.project()).expect("snapshot is valid"),
            before
        );
    }
}

#[test]
fn split_wire_rejects_an_invalid_product_route_atomically() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let original = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let original_id = original.id;
    state
        .apply(EditorCommand::CreateWire {
            folio_id,
            wire: original,
        })
        .expect("valid original wire");
    let junction = Junction::new(Point::new(0, 0));
    let first = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Junction(junction.id),
        vec![Point::new(0, 0), Point::new(10, 10)],
    );
    let second = Wire::new(
        WireEndpoint::Junction(junction.id),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(10, 0), Point::new(20, 0)],
    );
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");

    assert!(
        state
            .apply(EditorCommand::SplitWire {
                folio_id,
                wire_id: original_id,
                junction,
                first_wire: first,
                second_wire: second,
            })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before
    );
}

#[test]
fn each_wire_edit_command_has_exact_snapshot_undo_and_redo() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let original_end = symbol(definition_id, Point::new(20, 20));
    let original_end_terminal = *original_end
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let replacement_end = symbol(definition_id, Point::new(30, 10));
    let replacement_terminal = *replacement_end
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, original_end);
    place(&mut state, folio_id, replacement_end);
    let wire = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(original_end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0), Point::new(20, 20)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { folio_id, wire })
        .expect("valid wire");

    for command in [
        EditorCommand::InsertWireVertex {
            folio_id,
            wire_id,
            segment_index: 0,
            position: Point::new(10, 0),
        },
        EditorCommand::MoveWireVertex {
            folio_id,
            wire_id,
            vertex_index: 1,
            position: Point::new(10, 10),
        },
        EditorCommand::DeleteWireVertex {
            folio_id,
            wire_id,
            vertex_index: 1,
        },
        EditorCommand::ReconnectWireEndpoint {
            folio_id,
            wire_id,
            endpoint: WireSide::End,
            terminal_id: replacement_terminal,
        },
    ] {
        let before = snapshot_bytes(state.project()).expect("snapshot is valid");
        let mut history = History::new(1);
        history
            .apply(&mut state, command)
            .expect("wire edit applies to the fixture");
        let after = snapshot_bytes(state.project()).expect("snapshot is valid");
        assert!(history.undo(&mut state).expect("undo applies"));
        assert_eq!(
            snapshot_bytes(state.project()).expect("snapshot is valid"),
            before
        );
        assert!(history.redo(&mut state).expect("redo applies"));
        assert_eq!(
            snapshot_bytes(state.project()).expect("snapshot is valid"),
            after
        );
    }
}

#[test]
fn create_wire_rejects_a_route_that_collapses_during_canonicalization() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(0, 0));
    let terminal = *symbol
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    place(&mut state, folio_id, symbol);
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");
    let wire = Wire::new(
        WireEndpoint::Terminal(terminal),
        WireEndpoint::Terminal(terminal),
        vec![Point::new(0, 0), Point::new(10, 0), Point::new(0, 0)],
    );

    assert!(
        state
            .apply(EditorCommand::CreateWire { folio_id, wire })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before
    );
}

#[test]
fn split_wire_rejects_a_product_that_collapses_during_canonicalization() {
    let (mut state, folio_id, definition_id) = state_with_definition();
    let start = symbol(definition_id, Point::new(0, 0));
    let start_terminal = *start
        .terminals
        .keys()
        .next()
        .expect("symbol has a terminal");
    let end = symbol(definition_id, Point::new(20, 0));
    let end_terminal = *end.terminals.keys().next().expect("symbol has a terminal");
    place(&mut state, folio_id, start);
    place(&mut state, folio_id, end);
    let original = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = original.id;
    state
        .apply(EditorCommand::CreateWire {
            folio_id,
            wire: original,
        })
        .expect("valid source wire");
    let junction = Junction::new(Point::new(10, 0));
    let collapsing = Wire::new(
        WireEndpoint::Terminal(start_terminal),
        WireEndpoint::Junction(junction.id),
        vec![Point::new(0, 0), Point::new(10, 0), Point::new(0, 0)],
    );
    let valid = Wire::new(
        WireEndpoint::Junction(junction.id),
        WireEndpoint::Terminal(end_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let before = snapshot_bytes(state.project()).expect("snapshot is valid");

    assert!(
        state
            .apply(EditorCommand::SplitWire {
                folio_id,
                wire_id,
                junction,
                first_wire: collapsing,
                second_wire: valid,
            })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("snapshot is valid"),
        before
    );
}
