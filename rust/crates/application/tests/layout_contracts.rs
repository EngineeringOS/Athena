use athena_application::{
    AthenaEditor, AthenaFrontendMessage, DocumentMessage, LayoutMessage, LayoutTarget, PanelId,
    PortfolioMessage, WidgetId, WidgetValue,
};
use athena_domain::{FolioId, ProjectId};
use athena_domain::{TemplateSegment, TemplateText, VariableReference};
use athena_editor::{TitleBlockField, TitleBlockValue};
use uuid::Uuid;

fn editor() -> AthenaEditor {
    editor_and_effects().0
}

fn editor_and_effects() -> (AthenaEditor, Vec<AthenaFrontendMessage>) {
    let mut editor = AthenaEditor::default();
    let effects = editor.handle_message(PortfolioMessage::CreateProject {
        project_id: ProjectId::from_uuid(Uuid::from_u128(100)),
        initial_folio_id: FolioId::from_uuid(Uuid::from_u128(101)),
        name: "Main Distribution".into(),
    });
    (editor, effects)
}

#[test]
fn workspace_owns_outline_viewport_properties_and_panel_lifecycle() {
    let mut editor = editor();
    let effects = editor.handle_message(LayoutMessage::RequestWorkspace);
    let workspace = effects
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::WorkspaceLayoutUpdated(workspace) => Some(workspace),
            _ => None,
        })
        .expect("workspace effect");
    assert_eq!(workspace.left_panel.id, PanelId::ProjectOutline);
    assert_eq!(workspace.center_panel.id, PanelId::DocumentViewport);
    assert_eq!(workspace.right_panel.id, PanelId::Properties);

    editor.handle_message(LayoutMessage::SetPanelOpen {
        panel_id: PanelId::Properties,
        open: false,
    });
    let closed = editor.handle_message(LayoutMessage::RequestWorkspace);
    assert!(closed.iter().any(|effect| matches!(
        effect,
        AthenaFrontendMessage::WorkspaceLayoutUpdated(workspace) if !workspace.right_panel.open
    )));
    editor.handle_message(LayoutMessage::SetPanelOpen {
        panel_id: PanelId::Properties,
        open: true,
    });
}

#[test]
fn stable_project_widgets_emit_value_diffs_and_callbacks_route_to_commands() {
    let mut editor = editor();
    let first = editor.handle_message(LayoutMessage::RequestProjectPlate);
    let widgets = first
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::PanelLayoutUpdated {
                target: LayoutTarget::Project,
                widgets,
            } => Some(widgets.clone()),
            _ => None,
        })
        .expect("project plate structure");
    assert!(
        widgets
            .iter()
            .any(|widget| widget.id == WidgetId::new("project.name"))
    );

    let commit = editor.handle_message(LayoutMessage::CommitWidget {
        target: LayoutTarget::Project,
        widget_id: WidgetId::new("project.name"),
        value: WidgetValue::Text("Plant A".into()),
    });
    assert!(commit.iter().any(|effect| matches!(
        effect,
        AthenaFrontendMessage::WidgetValuesUpdated {
            target: LayoutTarget::Project,
            ..
        }
    )));
    assert_eq!(editor.state_snapshot().unwrap().project.name, "Plant A");

    let second = editor.handle_message(LayoutMessage::RequestProjectPlate);
    assert!(
        !second
            .iter()
            .any(|effect| matches!(effect, AthenaFrontendMessage::PanelLayoutUpdated { .. }))
    );
}

#[test]
fn folio_plate_contains_qet_evidenced_fields_and_unknown_callbacks_diagnose() {
    let (mut editor, effects) = editor_and_effects();
    let active = editor.state_snapshot().unwrap().active_folio_id;
    let widgets = effects
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::PanelLayoutUpdated {
                target: LayoutTarget::Folio(id),
                widgets,
            } if *id == active => Some(widgets),
            _ => None,
        })
        .expect("folio plate");
    for id in [
        "folio.label",
        "folio.template",
        "folio.placement",
        "folio.title",
        "folio.author",
        "folio.date",
        "folio.file",
        "folio.folio",
        "folio.plant",
        "folio.location",
        "folio.revision",
        "folio.page_number",
        "folio.variables",
    ] {
        assert!(
            widgets.iter().any(|widget| widget.id == WidgetId::new(id)),
            "{id}"
        );
    }

    let diagnostic = editor.handle_message(LayoutMessage::CommitWidget {
        target: LayoutTarget::Folio(active),
        widget_id: WidgetId::new("missing.widget"),
        value: WidgetValue::Text("x".into()),
    });
    assert!(matches!(
        diagnostic.as_slice(),
        [AthenaFrontendMessage::Diagnostic { .. }]
    ));
}

#[test]
fn invalid_widget_value_does_not_change_document_state() {
    let mut editor = editor();
    let before = editor.state_snapshot().unwrap();
    let effects = editor.handle_message(LayoutMessage::CommitWidget {
        target: LayoutTarget::Project,
        widget_id: WidgetId::new("project.name"),
        value: WidgetValue::Text(String::new()),
    });
    assert!(
        effects
            .iter()
            .any(|effect| matches!(effect, AthenaFrontendMessage::Diagnostic { .. }))
    );
    assert_eq!(editor.state_snapshot().unwrap(), before);
}

#[test]
fn direct_document_edits_refresh_existing_plate_values() {
    let mut editor = editor();
    editor.handle_message(LayoutMessage::RequestProjectPlate);
    let effects = editor.handle_message(DocumentMessage::RenameProject {
        name: "Plant B".into(),
    });
    assert!(effects.iter().any(|effect| matches!(
        effect,
        AthenaFrontendMessage::WidgetValuesUpdated {
            target: LayoutTarget::Project,
            ..
        }
    )));
}

#[test]
fn folio_plate_emits_resolved_title_block_display_for_scoped_references() {
    let mut editor = editor();
    let folio_id = editor.state_snapshot().unwrap().active_folio_id;
    editor.handle_message(DocumentMessage::SetProjectVariable {
        key: "plant".into(),
        value: Some("PLANT-A".into()),
    });
    editor.handle_message(DocumentMessage::SetFolioVariable {
        folio_id,
        key: "area".into(),
        value: Some("MCC-01".into()),
    });
    let effects = editor.handle_message(DocumentMessage::SetTitleBlockValue {
        folio_id,
        field: TitleBlockField::Title,
        value: TitleBlockValue::Template(TemplateText(vec![
            TemplateSegment::Variable(VariableReference::Project("plant".into())),
            TemplateSegment::Literal(" / ".into()),
            TemplateSegment::Variable(VariableReference::Folio("area".into())),
        ])),
    });

    assert!(effects.iter().any(|effect| matches!(
        effect,
        AthenaFrontendMessage::ResolvedTitleBlockUpdated { folio_id: id, display }
            if *id == folio_id && display.title.text == "PLANT-A / MCC-01"
    )));
}
