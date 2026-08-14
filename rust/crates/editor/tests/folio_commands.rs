use athena_domain::{
    Folio, Project, ProjectFolioDefaults, TemplateText, TitleBlockPlacement, TitleBlockValues,
};
use athena_editor::{
    DocumentRevision, EditorCommand, EditorSession, TitleBlockField, TitleBlockValue,
};

fn add_folio_command(project: &Project, label: &str, position: usize) -> EditorCommand {
    EditorCommand::AddFolio {
        folio: Folio::new(
            label,
            project.folio_defaults.title_block.clone(),
            project.folio_defaults.variables.clone(),
        ),
        position,
    }
}

#[test]
fn folio_metadata_commands_undo_and_redo_equal_canonical_states() {
    let project = Project::new("Main Distribution");
    let initial_project = project.clone();
    let initial_folio = project.folio_order()[0];
    let mut session = EditorSession::new(project, initial_folio);

    let add_control = add_folio_command(session.state().project(), "Control", 1);
    let control = match &add_control {
        EditorCommand::AddFolio { folio, .. } => folio.id,
        _ => unreachable!(),
    };
    session.apply_command(add_control).unwrap();
    let add_io = add_folio_command(session.state().project(), "I/O", 2);
    let io = match &add_io {
        EditorCommand::AddFolio { folio, .. } => folio.id,
        _ => unreachable!(),
    };
    session.apply_command(add_io).unwrap();
    session
        .apply_command(EditorCommand::MoveFolio {
            folio_id: io,
            from: 2,
            to: 1,
        })
        .unwrap();
    session
        .apply_command(EditorCommand::RenameProject {
            old_name: "Main Distribution".into(),
            new_name: "Plant A Distribution".into(),
        })
        .unwrap();
    session
        .apply_command(EditorCommand::RenameFolio {
            folio_id: control,
            old_label: "Control".into(),
            new_label: "Motor Control".into(),
        })
        .unwrap();

    let old_defaults = session.state().project().folio_defaults.clone();
    let mut new_defaults = old_defaults.clone();
    new_defaults.title_block.placement = TitleBlockPlacement::Right;
    new_defaults.variables.insert("area".into(), "North".into());
    session
        .apply_command(EditorCommand::UpdateFolioDefaults {
            old_defaults,
            new_defaults,
        })
        .unwrap();
    session
        .apply_command(EditorCommand::SetProjectVariable {
            key: "designer".into(),
            old_value: None,
            new_value: Some("A. Engineer".into()),
        })
        .unwrap();
    session
        .apply_command(EditorCommand::SetFolioVariable {
            folio_id: control,
            key: "cabinet".into(),
            old_value: None,
            new_value: Some("C01".into()),
        })
        .unwrap();
    session
        .apply_command(EditorCommand::SetTitleBlockValue {
            folio_id: control,
            field: TitleBlockField::PageNumber,
            old_value: TitleBlockValue::Template(TemplateText::default()),
            new_value: TitleBlockValue::Template(TemplateText::literal("C-07")),
        })
        .unwrap();
    session.activate_folio(control).unwrap();

    let final_project = session.state().project().clone();
    let final_revision = session.document_revision();
    assert_eq!(session.active_folio_id(), control);
    assert_eq!(
        session.state().project().folio_order(),
        &[initial_folio, io, control]
    );

    while session.undo().unwrap() {}
    assert_eq!(session.state().project(), &initial_project);
    assert_eq!(session.document_revision(), DocumentRevision::INITIAL);

    while session.redo().unwrap() {}
    assert_eq!(session.state().project(), &final_project);
    assert_eq!(session.document_revision(), final_revision);
}

#[test]
fn invalid_old_values_leave_state_history_revision_and_dirty_state_unchanged() {
    let project = Project::new("Main Distribution");
    let active = project.folio_order()[0];
    let mut session = EditorSession::new(project, active);
    let before = session.state().project().clone();

    assert!(
        session
            .apply_command(EditorCommand::RenameProject {
                old_name: "Wrong".into(),
                new_name: "Plant A".into(),
            })
            .is_err()
    );

    assert_eq!(session.state().project(), &before);
    assert_eq!(session.document_revision(), DocumentRevision::INITIAL);
    assert_eq!(session.history_lengths(), (0, 0));
    assert!(!session.is_dirty());
}

#[test]
fn revision_bound_save_checkpoint_preserves_post_request_edits() {
    let project = Project::new("Main Distribution");
    let active = project.folio_order()[0];
    let mut session = EditorSession::new(project, active);
    session
        .apply_command(EditorCommand::RenameProject {
            old_name: "Main Distribution".into(),
            new_name: "Plant A".into(),
        })
        .unwrap();

    let before_lengths = session.history_lengths();
    let save = session.begin_save().expect("save snapshot");
    assert_eq!(save.revision, DocumentRevision(1));
    assert!(!save.bytes.is_empty());
    assert_eq!(session.history_lengths(), before_lengths);
    assert_eq!(session.document_revision(), DocumentRevision(1));

    session
        .apply_command(EditorCommand::RenameFolio {
            folio_id: active,
            old_label: "Folio 1".into(),
            new_label: "Power".into(),
        })
        .unwrap();
    assert_eq!(session.document_revision(), DocumentRevision(2));
    assert!(session.complete_save(save.revision));
    assert_eq!(session.history_lengths(), (1, 0));
    assert!(session.is_dirty());

    assert!(session.undo().unwrap());
    assert_eq!(session.document_revision(), DocumentRevision(1));
    assert!(!session.is_dirty());
    assert!(session.redo().unwrap());
    assert_eq!(session.document_revision(), DocumentRevision(2));
    assert!(session.is_dirty());
}

#[test]
fn stale_cancelled_and_failed_save_results_preserve_history_and_dirty_state() {
    let project = Project::new("Main Distribution");
    let active = project.folio_order()[0];
    let mut session = EditorSession::new(project, active);
    session
        .apply_command(EditorCommand::SetProjectVariable {
            key: "plant".into(),
            old_value: None,
            new_value: Some("PLANT-A".into()),
        })
        .unwrap();
    let save = session.begin_save().unwrap();
    let before = session.history_lengths();

    assert!(!session.complete_save(DocumentRevision(999)));
    assert!(session.save_is_pending());
    assert_eq!(session.history_lengths(), before);
    assert!(session.is_dirty());

    assert!(session.cancel_save(save.revision));
    assert!(!session.save_is_pending());
    assert_eq!(session.history_lengths(), before);
    assert!(session.is_dirty());

    let retry = session.begin_save().unwrap();
    assert!(session.fail_save(retry.revision));
    assert!(!session.save_is_pending());
    assert_eq!(session.history_lengths(), before);
    assert!(session.is_dirty());
}

#[test]
fn full_title_block_values_remain_typed() {
    let values = TitleBlockValues::default();
    let defaults = ProjectFolioDefaults {
        title_block: values.clone(),
        variables: Default::default(),
    };
    assert_eq!(defaults.title_block, values);
}
