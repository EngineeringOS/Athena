use athena_domain::{
    ElectricalKind, FieldValue, Junction, Point, Project, SheetSettings, SymbolDefinition,
    SymbolInstance, Terminal, Wire, WireEndpoint,
};
use athena_editor::{
    CommandEnvelope, EditorCommand, EditorState, FieldTarget, History, ItemId, snapshot_bytes,
};
use uuid::Uuid;

fn state_with_definition() -> (
    EditorState,
    athena_domain::SheetId,
    athena_domain::SymbolDefinitionId,
) {
    let mut project = Project::new("Motor control");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition IDs are unique");
    let sheet_id = project.sheet_order()[0];

    (EditorState::new(project), sheet_id, definition_id)
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

fn place(state: &mut EditorState, sheet_id: athena_domain::SheetId, symbol: SymbolInstance) {
    state
        .apply(EditorCommand::PlaceSymbol { sheet_id, symbol })
        .expect("placement should be valid");
}

#[test]
fn places_moves_and_deletes_a_symbol() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;

    place(&mut state, sheet_id, symbol);
    state
        .apply(EditorCommand::MoveItems {
            sheet_id,
            items: vec![ItemId::Symbol(symbol_id)],
            delta: Point::new(5, -10),
        })
        .expect("move should be valid");

    assert_eq!(
        state
            .project()
            .symbol_instance(sheet_id, symbol_id)
            .unwrap()
            .position,
        Point::new(15, 0)
    );

    state
        .apply(EditorCommand::DeleteItems {
            sheet_id,
            items: vec![ItemId::Symbol(symbol_id)],
        })
        .expect("delete should be valid");

    assert!(
        state
            .project()
            .symbol_instance(sheet_id, symbol_id)
            .is_none()
    );
}

#[test]
fn creates_splits_and_deletes_a_wire() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first_symbol = symbol(definition_id, Point::new(0, 0));
    let first_terminal = *first_symbol.terminals.keys().next().unwrap();
    let second_symbol = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second_symbol.terminals.keys().next().unwrap();
    place(&mut state, sheet_id, first_symbol);
    place(&mut state, sheet_id, second_symbol);

    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { sheet_id, wire })
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
            sheet_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        })
        .expect("split should replace the original wire");

    assert!(state.project().wire(sheet_id, wire_id).is_none());
    assert!(state.project().wire(sheet_id, first_wire_id).is_some());
    assert!(state.project().wire(sheet_id, second_wire_id).is_some());

    state
        .apply(EditorCommand::DeleteWire {
            sheet_id,
            wire_id: first_wire_id,
        })
        .expect("wire exists");
    assert!(state.project().wire(sheet_id, first_wire_id).is_none());
}

#[test]
fn sets_symbol_field_values_and_sheet_settings() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(0, 0));
    let symbol_id = symbol.id;
    place(&mut state, sheet_id, symbol);

    state
        .apply(EditorCommand::SetFieldValue {
            sheet_id,
            target: FieldTarget::Symbol(symbol_id),
            field: "reference".into(),
            value: Some(FieldValue::Text("R1".into())),
        })
        .expect("symbol field can be set");
    assert_eq!(
        state
            .project()
            .symbol_instance(sheet_id, symbol_id)
            .unwrap()
            .fields["reference"],
        FieldValue::Text("R1".into())
    );

    let settings = SheetSettings {
        page_width: 594,
        page_height: 420,
        grid_spacing: 5,
        snap_enabled: false,
    };
    state
        .apply(EditorCommand::ApplySheetSettings {
            sheet_id,
            settings: settings.clone(),
        })
        .expect("sheet settings can be applied");

    assert_eq!(state.project().sheet(sheet_id).unwrap().settings, settings);
}

#[test]
fn rejects_unknown_terminal_wire_without_changing_the_snapshot() {
    let (mut state, sheet_id, _) = state_with_definition();
    let before = snapshot_bytes(state.project()).expect("valid initial state");
    let wire = Wire::new(
        WireEndpoint::Terminal(athena_domain::TerminalId::new()),
        WireEndpoint::Junction(athena_domain::JunctionId::new()),
        vec![Point::new(0, 0), Point::new(10, 0)],
    );

    assert!(
        state
            .apply(EditorCommand::CreateWire { sheet_id, wire })
            .is_err()
    );
    assert_eq!(
        snapshot_bytes(state.project()).expect("failed command preserves a valid project"),
        before
    );
}

#[test]
fn moves_multiple_selected_symbols_in_one_command() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let second = symbol(definition_id, Point::new(20, 10));
    let second_id = second.id;
    place(&mut state, sheet_id, first);
    place(&mut state, sheet_id, second);

    state
        .apply(EditorCommand::MoveItems {
            sheet_id,
            items: vec![ItemId::Symbol(first_id), ItemId::Symbol(second_id)],
            delta: Point::new(-5, 15),
        })
        .expect("both selected symbols move atomically");

    assert_eq!(
        state
            .project()
            .symbol_instance(sheet_id, first_id)
            .unwrap()
            .position,
        Point::new(-5, 15)
    );
    assert_eq!(
        state
            .project()
            .symbol_instance(sheet_id, second_id)
            .unwrap()
            .position,
        Point::new(15, 25)
    );
}

#[test]
fn moving_a_symbol_moves_attached_wire_endpoint_vertices() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, sheet_id, first.clone());
    place(&mut state, sheet_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(10, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { sheet_id, wire })
        .expect("wire endpoints exist");

    state
        .apply(EditorCommand::MoveItems {
            sheet_id,
            items: vec![ItemId::Symbol(first_id)],
            delta: Point::new(5, 5),
        })
        .expect("moving the symbol should move its attached wire endpoint");

    assert_eq!(
        state
            .project()
            .terminal(sheet_id, first_terminal)
            .unwrap()
            .position,
        Point::new(5, 5)
    );
    assert_eq!(
        state.project().wire(sheet_id, wire_id).unwrap().route,
        vec![Point::new(5, 5), Point::new(10, 0), Point::new(20, 0)]
    );
}

#[test]
fn rotates_and_mirrors_a_selected_symbol() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(0, 0));
    let symbol_id = symbol.id;
    place(&mut state, sheet_id, symbol);

    state
        .apply(EditorCommand::RotateItems {
            sheet_id,
            items: vec![ItemId::Symbol(symbol_id)],
            quarter_turns: 1,
        })
        .expect("rotation should be valid");
    state
        .apply(EditorCommand::MirrorItems {
            sheet_id,
            items: vec![ItemId::Symbol(symbol_id)],
        })
        .expect("mirroring should be valid");

    let placed = state
        .project()
        .symbol_instance(sheet_id, symbol_id)
        .unwrap();
    assert_eq!(placed.rotation_quarter_turns, 1);
    assert!(placed.mirrored);
}

#[test]
fn failed_undo_keeps_its_history_entry() {
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, sheet_id, first.clone());
    place(&mut state, sheet_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    state
        .apply(EditorCommand::CreateWire { sheet_id, wire })
        .expect("wire endpoints exist");
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::DeleteWire { sheet_id, wire_id })
        .expect("deleting the wire should apply");
    state
        .apply(EditorCommand::DeleteItems {
            sheet_id,
            items: vec![ItemId::Symbol(first_id)],
        })
        .expect("independent deletion should diverge the current state");

    assert!(history.undo(&mut state).is_err());
    assert_eq!(history.undo_len(), 1);
    assert_eq!(history.redo_len(), 0);

    place(&mut state, sheet_id, first);
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
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first = symbol(definition_id, Point::new(0, 0));
    let first_id = first.id;
    let first_terminal = *first.terminals.keys().next().unwrap();
    let second = symbol(definition_id, Point::new(20, 0));
    let second_terminal = *second.terminals.keys().next().unwrap();
    place(&mut state, sheet_id, first.clone());
    place(&mut state, sheet_id, second);
    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal),
        WireEndpoint::Terminal(second_terminal),
        vec![Point::new(0, 0), Point::new(20, 0)],
    );
    let wire_id = wire.id;
    let wire_for_restore = wire.clone();
    state
        .apply(EditorCommand::CreateWire { sheet_id, wire })
        .expect("wire endpoints exist");
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::DeleteWire { sheet_id, wire_id })
        .expect("deleting the wire should apply");
    assert!(
        history
            .undo(&mut state)
            .expect("undo should restore the wire")
    );
    state
        .apply(EditorCommand::DeleteItems {
            sheet_id,
            items: vec![ItemId::Symbol(first_id)],
        })
        .expect("independent deletion should remove the restored wire");

    assert!(history.redo(&mut state).is_err());
    assert_eq!(history.undo_len(), 0);
    assert_eq!(history.redo_len(), 1);

    place(&mut state, sheet_id, first);
    state
        .apply(EditorCommand::CreateWire {
            sheet_id,
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
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { sheet_id, symbol })
        .expect("place should apply");
    let before_move = snapshot_bytes(state.project()).expect("snapshot is valid");

    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                sheet_id,
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
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { sheet_id, symbol })
        .expect("place should apply");
    let before_delete = snapshot_bytes(state.project()).expect("snapshot is valid");

    history
        .apply(
            &mut state,
            EditorCommand::DeleteItems {
                sheet_id,
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
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let symbol = symbol(definition_id, Point::new(10, 10));
    let symbol_id = symbol.id;
    let mut history = History::new(8);
    history
        .apply(&mut state, EditorCommand::PlaceSymbol { sheet_id, symbol })
        .expect("place should apply");
    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                sheet_id,
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
    let (mut state, sheet_id, definition_id) = state_with_definition();
    let first_symbol = symbol(definition_id, Point::new(0, 0));
    let first_symbol_id = first_symbol.id;
    let second_symbol = symbol(definition_id, Point::new(20, 0));
    let mut history = History::new(1);

    history
        .apply(
            &mut state,
            EditorCommand::PlaceSymbol {
                sheet_id,
                symbol: first_symbol,
            },
        )
        .expect("first placement should apply");
    history
        .apply(
            &mut state,
            EditorCommand::MoveItems {
                sheet_id,
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
                sheet_id,
                symbol: second_symbol,
            },
        )
        .expect("new edit should apply");

    assert_eq!(history.redo_len(), 0);
    assert!(!history.redo(&mut state).expect("redo should be empty"));
}

#[test]
fn command_envelope_round_trips_through_serde() {
    let (state, sheet_id, definition_id) = state_with_definition();
    let command = EditorCommand::PlaceSymbol {
        sheet_id,
        symbol: symbol(definition_id, Point::new(10, 20)),
    };
    let envelope = CommandEnvelope {
        operation_id: Uuid::new_v4(),
        project_id: state.project().id,
        sheet_id,
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
