use athena_domain::{
    Annotation, ElectricalKind, FieldValue, Junction, JunctionId, Point, Project, SheetId,
    SymbolDefinition, SymbolDefinitionId, SymbolInstance, SymbolInstanceId, Terminal, TerminalId,
    Wire, WireEndpoint,
};
use uuid::Uuid;

#[test]
fn new_project_starts_with_one_ordered_sheet() {
    let project = Project::new("Motor control");

    assert_eq!(project.sheet_order().len(), 1);
    assert_eq!(project.sheets().len(), 1);
    assert!(project.validate().is_ok());
}

#[test]
fn entity_storage_is_deterministic() {
    let mut project = Project::new("Deterministic");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let mut instance = SymbolInstance::new(definition_id);
    let instance_id = instance.id;
    let terminal_id = instance.add_terminal(Terminal::new(
        instance.id,
        "1",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_junction(Point::new(10, 10));

    assert_eq!(project.sheet_order()[0], sheet_id);
    assert_eq!(
        project.sheet(sheet_id).unwrap().symbol_instances()[&instance_id].terminals[&terminal_id]
            .id,
        terminal_id
    );
    assert!(project.validate().is_ok());
}

#[test]
fn validation_rejects_a_uuid_reused_by_different_entity_kinds() {
    let duplicate_uuid = Uuid::new_v4();
    let mut project = Project::new("Duplicate IDs");
    let mut definition = SymbolDefinition::new("Resistor");
    definition.id = SymbolDefinitionId::from_uuid(duplicate_uuid);
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let mut junction = Junction::new(Point::new(10, 10));
    junction.id = JunctionId::from_uuid(duplicate_uuid);
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .junctions
        .insert(junction.id, junction);

    assert_eq!(
        project.validate(),
        Err(athena_domain::DomainError::DuplicateEntityId {
            entity_id: duplicate_uuid
        })
    );
}

#[test]
fn validation_rejects_collection_keys_that_do_not_match_record_ids() {
    let mut project = Project::new("Collection keys");
    let original_sheet_id = project.sheet_order()[0];
    let mismatched_sheet_key = SheetId::new();
    let sheet = project.sheets.remove(&original_sheet_id).unwrap();
    project.sheets.insert(mismatched_sheet_key, sheet);
    project.sheet_order[0] = mismatched_sheet_key;

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::SheetKeyMismatch { .. })
    ));
}

#[test]
fn entity_lookup_paths_resolve_entities_within_a_sheet() {
    let mut project = Project::new("Lookups");
    let definition = SymbolDefinition::new("Lamp");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let mut instance = SymbolInstance::new(definition_id);
    let instance_id = instance.id;
    let terminal_id = instance.add_terminal(Terminal::new(
        instance_id,
        "A",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    let wire = Wire::new(
        WireEndpoint::Terminal(terminal_id),
        WireEndpoint::Terminal(terminal_id),
        vec![Point::new(0, 0), Point::new(10, 0)],
    );
    let wire_id = wire.id;
    let junction_id = project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_junction(Point::new(5, 0));
    let annotation = Annotation::new("Lamp", Point::new(0, 10));
    let annotation_id = annotation.id;
    let sheet = project.sheet_mut(sheet_id).unwrap();
    sheet.add_symbol(instance).unwrap();
    sheet.add_wire(wire).unwrap();
    sheet.annotations.insert(annotation_id, annotation);

    assert!(project.symbol_instance(sheet_id, instance_id).is_some());
    assert!(project.terminal(sheet_id, terminal_id).is_some());
    assert!(project.wire(sheet_id, wire_id).is_some());
    assert!(project.junction(sheet_id, junction_id).is_some());
    assert!(project.annotation(sheet_id, annotation_id).is_some());
    assert!(
        project
            .sheet(sheet_id)
            .unwrap()
            .instance(instance_id)
            .is_some()
    );
    assert!(project.sheet(sheet_id).unwrap().wire(wire_id).is_some());
    assert!(
        project
            .sheet(sheet_id)
            .unwrap()
            .junction(junction_id)
            .is_some()
    );
    assert!(
        project
            .sheet(sheet_id)
            .unwrap()
            .annotation(annotation_id)
            .is_some()
    );
}

#[test]
fn instance_must_reference_an_existing_definition() {
    let mut project = Project::new("References");
    let sheet_id = project.sheet_order()[0];
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_symbol(SymbolInstance::new(athena_domain::SymbolDefinitionId::new()))
        .unwrap();

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::MissingSymbolDefinition { .. })
    ));
}

#[test]
fn terminal_ownership_is_stable() {
    let mut project = Project::new("Ownership");
    let definition = SymbolDefinition::new("Switch");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let instance_id = {
        let instance = SymbolInstance::new(definition_id);
        let id = instance.id;
        project
            .sheet_mut(sheet_id)
            .unwrap()
            .add_symbol(instance)
            .unwrap();
        id
    };
    let terminal_id = TerminalId::new();
    let mut terminal = Terminal::new(
        SymbolInstanceId::new(),
        "bad-owner",
        ElectricalKind::Passive,
        Point::new(0, 0),
    );
    terminal.id = terminal_id;
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .symbol_instances_mut()
        .get_mut(&instance_id)
        .unwrap()
        .terminals
        .insert(terminal_id, terminal);

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::TerminalOwnerMismatch { .. })
    ));
}

#[test]
fn wire_endpoints_must_resolve_to_terminals_or_junctions() {
    let mut project = Project::new("Connections");
    let definition = SymbolDefinition::new("Lamp");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let mut instance = SymbolInstance::new(definition_id);
    let terminal_id = instance.add_terminal(Terminal::new(
        instance.id,
        "A",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_wire(Wire::new(
            WireEndpoint::Terminal(terminal_id),
            WireEndpoint::Junction(athena_domain::JunctionId::new()),
            vec![Point::new(0, 0), Point::new(10, 0)],
        ))
        .unwrap();

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::MissingJunction { .. })
    ));
}

#[test]
fn validation_rejects_a_directly_inserted_wire_with_a_noncanonical_route() {
    let mut project = Project::new("Route validation");
    let definition = SymbolDefinition::new("Lamp");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.sheet_order()[0];
    let mut start = SymbolInstance::new(definition_id);
    let start_terminal = start.add_terminal(Terminal::new(
        start.id,
        "A",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    let mut end = SymbolInstance::new(definition_id);
    let end_terminal = end.add_terminal(Terminal::new(
        end.id,
        "B",
        ElectricalKind::Passive,
        Point::new(20, 0),
    ));
    let sheet = project.sheet_mut(sheet_id).unwrap();
    sheet.add_symbol(start).unwrap();
    sheet.add_symbol(end).unwrap();
    sheet
        .add_wire(Wire::new(
            WireEndpoint::Terminal(start_terminal),
            WireEndpoint::Terminal(end_terminal),
            vec![Point::new(0, 0), Point::new(10, 0), Point::new(20, 0)],
        ))
        .unwrap();

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::NonCanonicalWireRoute { .. })
    ));
}

#[test]
fn removing_sheet_removes_all_entities_reachable_only_from_that_sheet() {
    let mut project = Project::new("Removal");
    let definition = SymbolDefinition::new("Connector");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let sheet_id = project.add_sheet("Second");
    let instance = SymbolInstance::new(definition_id);
    let instance_id = instance.id;
    project
        .sheet_mut(sheet_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();

    assert!(project.remove_sheet(sheet_id).is_some());
    assert!(project.sheet(sheet_id).is_none());
    assert!(
        project
            .sheets()
            .values()
            .all(|sheet| !sheet.symbol_instances().contains_key(&instance_id))
    );
    assert!(project.validate().is_ok());
}

#[test]
fn field_values_are_serializable_domain_values() {
    let value = FieldValue::Text("Q1".into());
    let encoded = serde_json::to_string(&value).unwrap();
    assert_eq!(serde_json::from_str::<FieldValue>(&encoded).unwrap(), value);
}
