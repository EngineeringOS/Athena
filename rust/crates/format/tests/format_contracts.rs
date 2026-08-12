use athena_domain::{
    Annotation, ElectricalKind, Junction, Point, Project, SymbolDefinition, SymbolInstance,
    Terminal, Wire, WireEndpoint,
};
use athena_format::{
    CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError, SnapshotStore,
    decode_json, encode_json, migrate,
};
use serde_json::Value;

fn populated_project() -> Project {
    let mut project = Project::new("Motor control");
    let definition = SymbolDefinition::new("Motor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition should be unique");

    let sheet_id = project.sheet_order()[0];
    let sheet = project.sheet_mut(sheet_id).expect("initial sheet exists");

    let mut instance = SymbolInstance::new(definition_id);
    let terminal = Terminal::new(
        instance.id,
        "A1",
        ElectricalKind::Passive,
        Point::new(10, 20),
    );
    let terminal_id = terminal.id;
    instance.add_terminal(terminal);
    sheet
        .add_symbol(instance)
        .expect("instance should be unique");

    let junction = Junction::new(Point::new(40, 20));
    let junction_id = junction.id;
    sheet.junctions.insert(junction_id, junction);

    let wire = Wire::new(
        WireEndpoint::Terminal(terminal_id),
        WireEndpoint::Junction(junction_id),
        vec![Point::new(10, 20), Point::new(40, 20)],
    );
    sheet.add_wire(wire).expect("wire should be unique");

    let annotation = Annotation::new("M1", Point::new(30, 30));
    sheet.annotations.insert(annotation.id, annotation);
    project
}

#[test]
fn project_round_trips_through_json() {
    let project = populated_project();

    let encoded = encode_json(&project).expect("project should encode");
    let decoded = decode_json(&encoded).expect("project should decode");

    assert_eq!(decoded, project);
}

#[test]
fn identical_projects_serialize_deterministically() {
    let project = populated_project();

    let first = encode_json(&project).expect("project should encode");
    let second = encode_json(&project).expect("project should encode");

    assert_eq!(first, second);
}

#[test]
fn future_nested_fields_are_ignored() {
    let project = populated_project();
    let encoded = encode_json(&project).expect("project should encode");
    let mut document: Value = serde_json::from_str(&encoded).expect("valid JSON");

    document["project"]["future_project_flag"] = Value::Bool(true);
    let sheet_id = project.sheet_order()[0].to_string();
    document["project"]["sheets"][sheet_id]["future_sheet_metadata"] = Value::String("x".into());

    let decoded = decode_json(document.to_string()).expect("future nested fields are ignored");

    assert_eq!(decoded, project);
}

#[test]
fn unknown_envelope_fields_are_rejected() {
    let project = populated_project();
    let encoded = encode_json(&project).expect("project should encode");
    let mut document: Value = serde_json::from_str(&encoded).expect("valid JSON");
    document["unknown_envelope_field"] = Value::Bool(true);

    assert!(matches!(
        decode_json(document.to_string()),
        Err(FormatError::InvalidJson(_))
    ));
}

#[test]
fn unsupported_schema_version_returns_typed_error() {
    let project = populated_project();
    let future = DocumentEnvelope {
        format: DOCUMENT_FORMAT.into(),
        schema_version: CURRENT_SCHEMA_VERSION + 1,
        project,
    };

    assert!(matches!(
        migrate(future),
        Err(FormatError::UnsupportedSchemaVersion { .. })
    ));
}

#[test]
fn migrates_v1_without_changing_entity_uuids() {
    let project = populated_project();
    let v1 = DocumentEnvelope {
        format: DOCUMENT_FORMAT.into(),
        schema_version: 1,
        project: project.clone(),
    };

    let migrated = migrate(v1).expect("v1 should migrate");

    assert_eq!(migrated.schema_version, CURRENT_SCHEMA_VERSION);
    assert_eq!(migrated.project, project);
}

#[test]
fn snapshots_are_a_pure_json_byte_codec() {
    let project = populated_project();

    let bytes = SnapshotStore::encode_snapshot(&project).expect("snapshot should encode");
    let decoded = SnapshotStore::decode_snapshot(&bytes).expect("snapshot should decode");

    assert_eq!(decoded, project);
}
