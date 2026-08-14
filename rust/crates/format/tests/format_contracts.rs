use athena_domain::{
    Annotation, ElectricalKind, Junction, Point, Project, SymbolDefinition, SymbolInstance,
    Terminal, Wire, WireEndpoint,
};
use athena_format::{
    CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError, PersistedProject,
    SnapshotStore, decode_json, encode_json, migrate,
};
use serde_json::Value;

fn populated_document() -> PersistedProject {
    let mut project = Project::new("Motor control");
    let definition = SymbolDefinition::new("Motor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition should be unique");

    let folio_id = project.folio_order()[0];
    let folio = project.folio_mut(folio_id).expect("initial folio exists");

    let mut instance = SymbolInstance::new(definition_id);
    let terminal = Terminal::new(
        instance.id,
        "A1",
        ElectricalKind::Passive,
        Point::new(10, 20),
    );
    let terminal_id = terminal.id;
    instance.add_terminal(terminal);
    folio
        .add_symbol(instance)
        .expect("instance should be unique");

    let junction = Junction::new(Point::new(40, 20));
    let junction_id = junction.id;
    folio.schematic.junctions.insert(junction_id, junction);

    let wire = Wire::new(
        WireEndpoint::Terminal(terminal_id),
        WireEndpoint::Junction(junction_id),
        vec![Point::new(10, 20), Point::new(40, 20)],
    );
    folio.add_wire(wire).expect("wire should be unique");

    let annotation = Annotation::new("M1", Point::new(30, 30));
    folio
        .schematic
        .annotations
        .insert(annotation.id, annotation);
    PersistedProject::new(project, folio_id).expect("valid active folio")
}

#[test]
fn project_round_trips_through_json() {
    let document = populated_document();

    let encoded = encode_json(&document).expect("project should encode");
    let decoded = decode_json(&encoded).expect("project should decode");

    assert_eq!(decoded, document);
}

#[test]
fn identical_projects_serialize_deterministically() {
    let document = populated_document();

    let first = encode_json(&document).expect("project should encode");
    let second = encode_json(&document).expect("project should encode");

    assert_eq!(first, second);
}

#[test]
fn future_nested_fields_are_ignored() {
    let document = populated_document();
    let encoded = encode_json(&document).expect("project should encode");
    let mut value: Value = serde_json::from_str(&encoded).expect("valid JSON");

    value["project"]["future_project_flag"] = Value::Bool(true);
    let folio_id = document.active_folio_id.to_string();
    value["project"]["folios"][folio_id]["future_folio_metadata"] = Value::String("x".into());

    let decoded = decode_json(value.to_string()).expect("future nested fields are ignored");

    assert_eq!(decoded, document);
}

#[test]
fn unknown_envelope_fields_are_rejected() {
    let document = populated_document();
    let encoded = encode_json(&document).expect("project should encode");
    let mut value: Value = serde_json::from_str(&encoded).expect("valid JSON");
    value["unknown_envelope_field"] = Value::Bool(true);

    assert!(matches!(
        decode_json(value.to_string()),
        Err(FormatError::InvalidJson(_))
    ));
}

#[test]
fn malformed_active_folio_identity_returns_typed_error() {
    let document = populated_document();
    let mut value = serde_json::to_value(DocumentEnvelope::current(document)).unwrap();
    value["active_folio_id"] = Value::String("not-a-uuid".into());

    assert!(matches!(
        decode_json(value.to_string()),
        Err(FormatError::MalformedIdentity { path, .. }) if path == "active_folio_id"
    ));
}

#[test]
fn unsupported_schema_version_returns_typed_error() {
    let document = populated_document();
    let future = DocumentEnvelope {
        format: DOCUMENT_FORMAT.into(),
        schema_version: CURRENT_SCHEMA_VERSION + 1,
        project: document.project,
        active_folio_id: document.active_folio_id,
    };

    assert!(matches!(
        migrate(future),
        Err(FormatError::UnsupportedSchemaVersion { .. })
    ));
}

#[test]
fn snapshots_are_a_pure_json_byte_codec() {
    let document = populated_document();

    let bytes = SnapshotStore::encode_snapshot(&document).expect("snapshot should encode");
    let decoded = SnapshotStore::decode_snapshot(&bytes).expect("snapshot should decode");

    assert_eq!(decoded, document);
}
