use std::fs;

use athena_application::{
    DocumentMessage, LayoutMessage, LayoutTarget, PanelId, PortfolioMessage, WidgetId, WidgetValue,
};
use athena_desktop::app::DesktopEditor;
use athena_domain::{FolioId, ProjectId, TemplateSegment, TemplateText, VariableReference};
use uuid::Uuid;

fn project_id(value: u128) -> ProjectId {
    ProjectId::from_uuid(Uuid::from_u128(value))
}

fn folio_id(value: u128) -> FolioId {
    FolioId::from_uuid(Uuid::from_u128(value))
}

fn desktop_with_project() -> DesktopEditor {
    let mut desktop = DesktopEditor::default();
    desktop.dispatch(PortfolioMessage::CreateProject {
        project_id: project_id(1),
        initial_folio_id: folio_id(100),
        name: "Main Distribution".into(),
    });
    desktop.dispatch(LayoutMessage::RequestProjectPlate);
    desktop
}

#[test]
fn typed_effects_build_the_desktop_workbench_without_duplicate_document_state() {
    let mut desktop = desktop_with_project();
    let control = folio_id(2);

    desktop.dispatch(DocumentMessage::AddFolio {
        folio_id: control,
        label: "Control".into(),
    });
    desktop.dispatch(DocumentMessage::ActivateFolio { folio_id: control });

    let view = desktop.view_state();
    assert_eq!(view.project_name.as_deref(), Some("Main Distribution"));
    assert_eq!(view.active_folio_id, Some(control));
    assert_eq!(view.outline.len(), 2);
    assert_eq!(view.workspace.left_panel.id, PanelId::ProjectOutline);
    assert_eq!(view.workspace.center_panel.id, PanelId::DocumentViewport);
    assert_eq!(view.workspace.right_panel.id, PanelId::Properties);
    assert!(view.workspace.left_panel.open);
    assert!(view.workspace.right_panel.open);
    assert!(
        view.widgets(LayoutTarget::Folio(control))
            .iter()
            .any(|widget| widget.id == WidgetId::new("folio.title"))
    );
}

#[test]
fn stable_widget_callbacks_commit_rust_owned_electrical_plate_values() {
    let mut desktop = desktop_with_project();
    let active = desktop
        .view_state()
        .active_folio_id
        .expect("new project exposes its first folio");

    desktop.dispatch(LayoutMessage::CommitWidget {
        target: LayoutTarget::Project,
        widget_id: WidgetId::new("project.name"),
        value: WidgetValue::Text("Plant A Distribution".into()),
    });
    desktop.dispatch(LayoutMessage::CommitWidget {
        target: LayoutTarget::Folio(active),
        widget_id: WidgetId::new("folio.title"),
        value: WidgetValue::Template(TemplateText::literal("Main control")),
    });

    let snapshot = desktop
        .state_snapshot()
        .expect("project remains open behind the facade");
    assert_eq!(snapshot.project.name, "Plant A Distribution");
    assert_eq!(
        snapshot.project.folio(active).unwrap().title_block.title,
        TemplateText::literal("Main control")
    );
    assert!(desktop.view_state().dirty);
}

#[test]
fn save_and_open_platform_effects_round_trip_through_atomic_file_storage() {
    let directory = std::env::temp_dir().join(format!("athena-desktop-m005-{}", Uuid::new_v4()));
    fs::create_dir_all(&directory).expect("temporary directory is created");
    let path = directory.join("motor-control.athena.json");
    let mut desktop = desktop_with_project();
    desktop.dispatch(DocumentMessage::RenameProject {
        name: "Motor Control".into(),
    });

    desktop.dispatch(PortfolioMessage::RequestSave);
    assert!(desktop.view_state().save_dialog_pending());
    desktop
        .complete_save_dialog(Some(path.clone()))
        .expect("native adapter writes the save effect");
    assert!(!desktop.view_state().dirty);
    assert!(!desktop.view_state().save_dialog_pending());

    desktop.dispatch(PortfolioMessage::CloseProject);
    desktop.dispatch(PortfolioMessage::RequestOpen);
    assert!(desktop.view_state().open_dialog_pending());
    desktop
        .complete_open_dialog(Some(path.clone()))
        .expect("native adapter supplies open bytes through a typed message");

    let reopened = desktop.state_snapshot().expect("saved project reopens");
    assert_eq!(reopened.project.name, "Motor Control");
    assert!(!reopened.dirty);
    assert_eq!(
        desktop.view_state().status.as_deref(),
        Some("Opened motor-control.athena.json")
    );

    fs::remove_dir_all(directory).expect("temporary directory is removed");
}

#[test]
fn failed_and_cancelled_native_saves_return_typed_results_to_the_application() {
    let directory =
        std::env::temp_dir().join(format!("athena-desktop-m005-failure-{}", Uuid::new_v4()));
    fs::create_dir_all(&directory).expect("temporary directory is created");
    let mut desktop = desktop_with_project();
    desktop.dispatch(DocumentMessage::RenameProject {
        name: "Dirty project".into(),
    });

    desktop.dispatch(PortfolioMessage::RequestSave);
    assert!(desktop.complete_save_dialog(None).is_ok());
    assert!(desktop.view_state().dirty);
    assert!(!desktop.state_snapshot().unwrap().save_pending);

    desktop.dispatch(PortfolioMessage::RequestSave);
    let error = desktop
        .complete_save_dialog(Some(directory.clone()))
        .expect_err("a directory cannot be replaced by snapshot bytes");
    assert!(!error.is_empty());
    assert!(desktop.view_state().dirty);
    assert!(!desktop.state_snapshot().unwrap().save_pending);
    assert!(
        desktop
            .view_state()
            .status
            .as_deref()
            .unwrap_or_default()
            .contains("Save failed")
    );

    fs::remove_dir_all(directory).expect("temporary directory is removed");
}

#[test]
fn failed_native_open_reports_the_platform_error_in_status_feedback() {
    let mut desktop = DesktopEditor::default();
    let missing = std::env::temp_dir().join(format!("missing-{}.athena.json", Uuid::new_v4()));

    desktop.dispatch(PortfolioMessage::RequestOpen);
    let error = desktop
        .complete_open_dialog(Some(missing))
        .expect_err("missing project cannot be opened");

    assert!(!error.is_empty());
    assert!(
        desktop
            .view_state()
            .status
            .as_deref()
            .unwrap_or_default()
            .contains("Open failed")
    );
    assert!(!desktop.view_state().open_dialog_pending());
}

#[test]
fn resolved_title_block_effect_is_cached_for_the_native_viewport() {
    let mut desktop = desktop_with_project();
    let folio_id = desktop.view_state().active_folio_id.unwrap();
    desktop.dispatch(DocumentMessage::SetProjectVariable {
        key: "plant".into(),
        value: Some("PLANT-A".into()),
    });
    desktop.dispatch(DocumentMessage::SetFolioVariable {
        folio_id,
        key: "area".into(),
        value: Some("MCC-01".into()),
    });
    desktop.dispatch(LayoutMessage::CommitWidget {
        target: LayoutTarget::Folio(folio_id),
        widget_id: WidgetId::new("folio.title"),
        value: WidgetValue::Template(TemplateText(vec![
            TemplateSegment::Variable(VariableReference::Project("plant".into())),
            TemplateSegment::Literal(" / ".into()),
            TemplateSegment::Variable(VariableReference::Folio("area".into())),
        ])),
    });

    assert_eq!(
        desktop
            .view_state()
            .resolved_title_block(folio_id)
            .expect("resolved display effect is cached")
            .title
            .text,
        "PLANT-A / MCC-01"
    );
}

#[test]
fn desktop_open_adapter_reports_every_completion_through_typed_outcomes() {
    let app = include_str!("../src/app.rs");
    assert!(app.contains("OpenOutcome::Success"));
    assert!(app.contains("OpenOutcome::Cancelled"));
    assert!(app.contains("OpenOutcome::Failed"));
    assert!(!app.contains("self.view.status = Some(\"Open cancelled\""));
    assert!(!app.contains("self.view.status = Some(format!(\"Open failed:"));
}

#[test]
fn desktop_adapter_source_has_no_legacy_direct_editor_session_path() {
    let app = include_str!("../src/app.rs");
    let panels = include_str!("../src/panels.rs");
    for source in [app, panels] {
        assert!(!source.contains("EditorSession"));
        assert!(!source.contains("athena_editor::EditorCommand"));
        assert!(!source.contains("session."));
    }
    assert!(app.contains("AthenaEditor"));
    assert!(app.contains("AthenaFrontendMessage"));
    assert!(panels.contains("LayoutMessage::CommitWidget"));
}
