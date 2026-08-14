use athena_domain::{
    Annotation, ElectricalKind, FieldValue, FolioId, Junction, JunctionId, Point, Project,
    SymbolDefinition, SymbolDefinitionId, SymbolInstance, SymbolInstanceId, Terminal, TerminalId,
    Wire, WireEndpoint,
};
use uuid::Uuid;

#[test]
fn new_project_starts_with_one_ordered_folio() {
    let project = Project::new("Motor control");

    assert_eq!(project.folio_order().len(), 1);
    assert_eq!(project.folios().len(), 1);
    assert!(project.validate().is_ok());
}

#[test]
fn entity_storage_is_deterministic() {
    let mut project = Project::new("Deterministic");
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let folio_id = project.folio_order()[0];
    let mut instance = SymbolInstance::new(definition_id);
    let instance_id = instance.id;
    let terminal_id = instance.add_terminal(Terminal::new(
        instance.id,
        "1",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    project
        .folio_mut(folio_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();
    project
        .folio_mut(folio_id)
        .unwrap()
        .add_junction(Point::new(10, 10));

    assert_eq!(project.folio_order()[0], folio_id);
    assert_eq!(
        project.folio(folio_id).unwrap().symbol_instances()[&instance_id].terminals[&terminal_id]
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
    let folio_id = project.folio_order()[0];
    let mut junction = Junction::new(Point::new(10, 10));
    junction.id = JunctionId::from_uuid(duplicate_uuid);
    project
        .folio_mut(folio_id)
        .unwrap()
        .schematic
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
    let original_folio_id = project.folio_order()[0];
    let mismatched_folio_key = FolioId::new();
    let folio = project.folios.remove(&original_folio_id).unwrap();
    project.folios.insert(mismatched_folio_key, folio);
    project.folio_order[0] = mismatched_folio_key;

    assert!(matches!(
        project.validate(),
        Err(athena_domain::DomainError::FolioKeyMismatch { .. })
    ));
}

#[test]
fn entity_lookup_paths_resolve_entities_within_a_folio() {
    let mut project = Project::new("Lookups");
    let definition = SymbolDefinition::new("Lamp");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let folio_id = project.folio_order()[0];
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
        .folio_mut(folio_id)
        .unwrap()
        .add_junction(Point::new(5, 0));
    let annotation = Annotation::new("Lamp", Point::new(0, 10));
    let annotation_id = annotation.id;
    let folio = project.folio_mut(folio_id).unwrap();
    folio.add_symbol(instance).unwrap();
    folio.add_wire(wire).unwrap();
    folio
        .schematic
        .annotations
        .insert(annotation_id, annotation);

    assert!(project.symbol_instance(folio_id, instance_id).is_some());
    assert!(project.terminal(folio_id, terminal_id).is_some());
    assert!(project.wire(folio_id, wire_id).is_some());
    assert!(project.junction(folio_id, junction_id).is_some());
    assert!(project.annotation(folio_id, annotation_id).is_some());
    assert!(
        project
            .folio(folio_id)
            .unwrap()
            .instance(instance_id)
            .is_some()
    );
    assert!(project.folio(folio_id).unwrap().wire(wire_id).is_some());
    assert!(
        project
            .folio(folio_id)
            .unwrap()
            .junction(junction_id)
            .is_some()
    );
    assert!(
        project
            .folio(folio_id)
            .unwrap()
            .annotation(annotation_id)
            .is_some()
    );
}

#[test]
fn instance_must_reference_an_existing_definition() {
    let mut project = Project::new("References");
    let folio_id = project.folio_order()[0];
    project
        .folio_mut(folio_id)
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
    let folio_id = project.folio_order()[0];
    let instance_id = {
        let instance = SymbolInstance::new(definition_id);
        let id = instance.id;
        project
            .folio_mut(folio_id)
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
        .folio_mut(folio_id)
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
    let folio_id = project.folio_order()[0];
    let mut instance = SymbolInstance::new(definition_id);
    let terminal_id = instance.add_terminal(Terminal::new(
        instance.id,
        "A",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    project
        .folio_mut(folio_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();
    project
        .folio_mut(folio_id)
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
    let folio_id = project.folio_order()[0];
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
    let folio = project.folio_mut(folio_id).unwrap();
    folio.add_symbol(start).unwrap();
    folio.add_symbol(end).unwrap();
    folio
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
fn removing_folio_removes_all_entities_reachable_only_from_that_folio() {
    let mut project = Project::new("Removal");
    let definition = SymbolDefinition::new("Connector");
    let definition_id = definition.id;
    project.add_symbol_definition(definition).unwrap();
    let folio_id = project.add_folio("Second").unwrap();
    let instance = SymbolInstance::new(definition_id);
    let instance_id = instance.id;
    project
        .folio_mut(folio_id)
        .unwrap()
        .add_symbol(instance)
        .unwrap();

    assert!(project.remove_folio(folio_id).is_some());
    assert!(project.folio(folio_id).is_none());
    assert!(
        project
            .folios()
            .values()
            .all(|folio| !folio.symbol_instances().contains_key(&instance_id))
    );
    assert!(project.validate().is_ok());
}

#[test]
fn field_values_are_serializable_domain_values() {
    let value = FieldValue::Text("Q1".into());
    let encoded = serde_json::to_string(&value).unwrap();
    assert_eq!(serde_json::from_str::<FieldValue>(&encoded).unwrap(), value);
}
