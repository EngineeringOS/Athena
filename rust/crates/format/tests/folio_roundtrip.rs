use athena_domain::{
    DomainError, Project, TemplateSegment, TemplateText, VariableReference, resolve_template_text,
};
use athena_format::{
    CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError, PersistedProject,
    SnapshotStore, decode_json, encode_json, migrate,
};

fn persisted_project() -> PersistedProject {
    let mut project = Project::new("Main Distribution");
    project
        .set_project_variable("plant", "PLANT-A")
        .expect("valid project variable");
    project
        .set_project_variable("designer", "A. Engineer")
        .expect("valid project variable");
    project.folio_defaults.title_block.page_number = TemplateText::literal("A-02");

    let control = project.add_folio("Control").expect("valid folio");
    let io = project.add_folio("I/O").expect("valid folio");
    project.move_folio(io, 1).expect("valid order");
    let active = project.folio_mut(control).expect("control folio");
    active.title_block.author = TemplateText(vec![TemplateSegment::Variable(
        VariableReference::Project("designer".into()),
    )]);
    active.title_block.location = TemplateText::literal("L1");
    active.title_block.page_number = TemplateText::literal("C-07");
    active.variables.insert("cabinet".into(), "C01".into());

    PersistedProject::new(project, control).expect("active folio belongs to project")
}

#[test]
fn schema_three_round_trips_project_identity_order_active_folio_and_display_data() {
    let persisted = persisted_project();

    let encoded = encode_json(&persisted).expect("document encodes");
    let decoded = decode_json(&encoded).expect("document decodes");

    assert_eq!(decoded, persisted);
    assert!(
        decoded
            .project
            .folio_order()
            .contains(&decoded.active_folio_id)
    );
    let active = decoded
        .project
        .folio(decoded.active_folio_id)
        .expect("active folio exists");
    assert_eq!(active.label, "Control");
    let resolved = resolve_template_text(
        &active.title_block.author,
        &decoded.project.variables,
        &active.variables,
    );
    assert_eq!(resolved.text, "A. Engineer");
    assert_eq!(
        active.title_block.page_number,
        TemplateText::literal("C-07")
    );
}

#[test]
fn equal_documents_encode_deterministically_and_snapshot_bytes_are_pure() {
    let persisted = persisted_project();

    let first = encode_json(&persisted).expect("document encodes");
    let second = encode_json(&persisted).expect("document re-encodes");
    let bytes = SnapshotStore::encode_snapshot(&persisted).expect("snapshot encodes");

    assert_eq!(first, second);
    assert_eq!(bytes, first.as_bytes());
    assert_eq!(SnapshotStore::decode_snapshot(&bytes).unwrap(), persisted);
}

#[test]
fn invalid_active_folio_is_rejected_with_a_typed_error() {
    let persisted = persisted_project();
    let missing = athena_domain::FolioId::new();

    assert!(matches!(
        PersistedProject::new(persisted.project, missing),
        Err(FormatError::InvalidActiveFolio { active_folio_id })
            if active_folio_id == missing
    ));
}

#[test]
fn invalid_project_data_is_rejected_after_decode() {
    let persisted = persisted_project();
    let mut value = serde_json::to_value(DocumentEnvelope::current(persisted)).unwrap();
    value["project"]["name"] = serde_json::Value::String(String::new());

    assert!(matches!(
        decode_json(value.to_string()),
        Err(FormatError::InvalidProject(
            DomainError::EmptyProjectName { .. }
        ))
    ));
}

#[test]
fn invalid_folio_labels_and_order_are_rejected_after_decode() {
    let persisted = persisted_project();
    let mut empty_label =
        serde_json::to_value(DocumentEnvelope::current(persisted.clone())).unwrap();
    let active = persisted.active_folio_id.to_string();
    empty_label["project"]["folios"][&active]["label"] = serde_json::Value::String(String::new());
    assert!(matches!(
        decode_json(empty_label.to_string()),
        Err(FormatError::InvalidProject(DomainError::EmptyFolioLabel))
    ));

    let mut duplicate_order = serde_json::to_value(DocumentEnvelope::current(persisted)).unwrap();
    let first = duplicate_order["project"]["folio_order"][0].clone();
    duplicate_order["project"]["folio_order"]
        .as_array_mut()
        .expect("folio order array")
        .push(first);
    assert!(matches!(
        decode_json(duplicate_order.to_string()),
        Err(FormatError::InvalidProject(
            DomainError::DuplicateFolioOrder { .. }
        ))
    ));
}

#[test]
fn prototype_and_future_schemas_are_rejected_without_sheet_migration() {
    let persisted = persisted_project();
    for prototype in [1, 2] {
        let envelope = DocumentEnvelope {
            format: DOCUMENT_FORMAT.into(),
            schema_version: prototype,
            project: persisted.project.clone(),
            active_folio_id: persisted.active_folio_id,
        };
        assert!(matches!(
            migrate(envelope),
            Err(FormatError::UnsupportedPrototypeSchema {
                actual,
                minimum_supported: 3
            }) if actual == prototype
        ));
    }

    let future = DocumentEnvelope {
        format: DOCUMENT_FORMAT.into(),
        schema_version: CURRENT_SCHEMA_VERSION + 1,
        project: persisted.project,
        active_folio_id: persisted.active_folio_id,
    };
    assert!(matches!(
        migrate(future),
        Err(FormatError::UnsupportedSchemaVersion { .. })
    ));
}

#[test]
fn incompatible_prototype_sheet_payload_is_rejected_from_its_header() {
    let prototype = format!(
        r#"{{"format":"{DOCUMENT_FORMAT}","schema_version":2,"project":{{"sheets":{{}}}}}}"#
    );

    assert!(matches!(
        decode_json(prototype),
        Err(FormatError::UnsupportedPrototypeSchema {
            actual: 2,
            minimum_supported: 3
        })
    ));
}

#[test]
fn canonical_fixture_reencodes_byte_for_byte() {
    let fixture = include_bytes!("fixtures/m005-project.json");
    let decoded = SnapshotStore::decode_snapshot(fixture).expect("canonical fixture decodes");
    let encoded = SnapshotStore::encode_snapshot(&decoded).expect("canonical fixture encodes");

    assert_eq!(encoded, fixture);
}
