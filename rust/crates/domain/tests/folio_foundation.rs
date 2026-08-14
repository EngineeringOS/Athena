use std::collections::BTreeMap;

use athena_domain::{
    DomainError, Project, TemplateSegment, TemplateText, VariableReference, resolve_template_text,
};

#[test]
fn project_starts_with_one_folio_copied_from_project_defaults() {
    let project = Project::new("Main Distribution");

    assert_eq!(project.folio_order().len(), 1);
    let folio = project
        .folio(project.folio_order()[0])
        .expect("initial folio must exist");
    assert_eq!(folio.label, "Folio 1");
    assert_eq!(folio.title_block, project.folio_defaults.title_block);
    assert_eq!(folio.variables, project.folio_defaults.variables);
    assert_eq!(folio.schematic.settings.page_width, 420);
    assert_eq!(folio.schematic.settings.page_height, 297);
}

#[test]
fn adding_and_moving_folios_preserves_stable_identity_and_explicit_order() {
    let mut project = Project::new("Main Distribution");
    project.folio_defaults.title_block.page_number = TemplateText::literal("A-02");
    project
        .folio_defaults
        .variables
        .insert("area".into(), "North".into());

    let second = project.add_folio("Control").expect("valid folio");
    let third = project.add_folio("I/O").expect("valid folio");
    project.move_folio(third, 1).expect("valid move");

    assert_eq!(
        project.folio_order(),
        &[project.folio_order()[0], third, second]
    );
    let third_folio = project.folio(third).expect("third folio exists");
    assert_eq!(
        third_folio.title_block.page_number,
        TemplateText::literal("A-02")
    );
    assert_eq!(
        third_folio.variables.get("area").map(String::as_str),
        Some("North")
    );

    project.folio_defaults.title_block.page_number = TemplateText::literal("A-03");
    assert_eq!(
        project
            .folio(third)
            .expect("existing folio remains")
            .title_block
            .page_number,
        TemplateText::literal("A-02")
    );
}

#[test]
fn validation_rejects_duplicate_and_missing_folio_order_entries() {
    let mut duplicate = Project::new("Duplicate order");
    let initial = duplicate.folio_order()[0];
    duplicate.folio_order.push(initial);
    assert!(matches!(
        duplicate.validate(),
        Err(DomainError::DuplicateFolioOrder { folio_id }) if folio_id == initial
    ));

    let mut missing = Project::new("Missing order");
    let missing_id = missing.add_folio("Unordered").expect("valid folio");
    missing
        .folio_order
        .retain(|folio_id| *folio_id != missing_id);
    assert!(matches!(
        missing.validate(),
        Err(DomainError::UnorderedFolio { folio_id }) if folio_id == missing_id
    ));
}

#[test]
fn variable_keys_are_validated_per_scope() {
    let mut project = Project::new("Variables");
    let folio_id = project.folio_order()[0];

    project
        .set_project_variable(" plant ", "PLANT-A")
        .expect("trimmed project variable");
    project
        .set_folio_variable(folio_id, "designer", "A. Engineer")
        .expect("folio variable");

    assert_eq!(
        project.variables.get("plant").map(String::as_str),
        Some("PLANT-A")
    );
    assert_eq!(
        project
            .folio(folio_id)
            .expect("folio exists")
            .variables
            .get("designer")
            .map(String::as_str),
        Some("A. Engineer")
    );
    assert!(matches!(
        project.set_project_variable("   ", "invalid"),
        Err(DomainError::InvalidVariableKey { .. })
    ));
    assert!(matches!(
        project.set_project_variable("bad\nkey", "invalid"),
        Err(DomainError::InvalidVariableKey { .. })
    ));
}

#[test]
fn aggregate_validation_rejects_noncanonical_variables_and_page_settings() {
    let mut invalid_variable = Project::new("Invalid variable");
    invalid_variable
        .variables
        .insert(" plant ".into(), "A".into());
    assert!(matches!(
        invalid_variable.validate(),
        Err(DomainError::InvalidVariableKey { .. })
    ));

    let mut invalid_page = Project::new("Invalid page");
    let folio_id = invalid_page.folio_order()[0];
    invalid_page
        .folio_mut(folio_id)
        .expect("initial folio")
        .schematic
        .settings
        .page_width = 0;
    assert!(matches!(
        invalid_page.validate(),
        Err(DomainError::InvalidFolioPageSize { .. })
    ));
}

#[test]
fn typed_template_resolution_preserves_missing_references_with_diagnostics() {
    let template = TemplateText(vec![
        TemplateSegment::Literal("Designed by ".into()),
        TemplateSegment::Variable(VariableReference::Project("designer".into())),
        TemplateSegment::Literal(" / ".into()),
        TemplateSegment::Variable(VariableReference::Folio("location".into())),
        TemplateSegment::Literal(" / ".into()),
        TemplateSegment::Variable(VariableReference::Folio("missing".into())),
    ]);
    let project_variables = BTreeMap::from([("designer".into(), "A. Engineer".into())]);
    let folio_variables = BTreeMap::from([("location".into(), "L1".into())]);

    let resolved = resolve_template_text(&template, &project_variables, &folio_variables);

    assert_eq!(
        resolved.text,
        "Designed by A. Engineer / L1 / {Folio:missing}"
    );
    assert_eq!(resolved.diagnostics.len(), 1);
    assert_eq!(
        resolved.diagnostics[0].reference,
        VariableReference::Folio("missing".into())
    );
}
