use athena_application::{
    AthenaEditor, AthenaFrontendMessage, AthenaMessage, DocumentMessage, OpenOutcome,
    PortfolioMessage, SaveOutcome,
};
use athena_domain::{FolioId, ProjectId};
use athena_editor::DocumentRevision;
use uuid::Uuid;

#[test]
fn typed_messages_produce_ordered_effects_and_canonical_state() {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(1));
    let second = FolioId::from_uuid(Uuid::from_u128(2));
    let mut editor = AthenaEditor::default();

    let effects = editor.handle_message(PortfolioMessage::CreateProject {
        project_id,
        initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1001)),
        name: "Main Distribution".into(),
    });
    assert!(matches!(
        effects.as_slice(),
        [
            AthenaFrontendMessage::ProjectOpened { project_id: opened, .. },
            AthenaFrontendMessage::ActiveFolioChanged { .. },
            AthenaFrontendMessage::OutlineChanged { .. },
            AthenaFrontendMessage::DirtyStateChanged { dirty: false },
            AthenaFrontendMessage::WorkspaceLayoutUpdated(_),
            AthenaFrontendMessage::ResolvedTitleBlockUpdated { .. },
            AthenaFrontendMessage::PanelLayoutUpdated { .. },
        ] if *opened == project_id
    ));

    editor.handle_message(DocumentMessage::AddFolio {
        folio_id: second,
        label: "Control".into(),
    });
    editor.handle_message(DocumentMessage::ActivateFolio { folio_id: second });
    editor.handle_message(DocumentMessage::RenameProject {
        name: "Plant A Distribution".into(),
    });

    let state = editor.state_snapshot().expect("project remains open");
    assert_eq!(state.project.id, project_id);
    assert_eq!(state.project.folio_order()[1], second);
    assert_eq!(state.active_folio_id, second);
    assert_eq!(state.project.name, "Plant A Distribution");
    assert!(state.dirty);
}

#[test]
fn save_results_are_bound_to_request_project_and_revision() {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(10));
    let mut editor = AthenaEditor::default();
    editor.handle_message(PortfolioMessage::CreateProject {
        project_id,
        initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1010)),
        name: "Main Distribution".into(),
    });
    editor.handle_message(DocumentMessage::RenameProject {
        name: "Plant A".into(),
    });

    let effects = editor.handle_message(PortfolioMessage::RequestSave);
    let (request_id, revision) = effects
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                project_id: saved_project,
                revision,
                bytes,
            } if *saved_project == project_id && !bytes.is_empty() => {
                Some((*request_id, *revision))
            }
            _ => None,
        })
        .expect("save request effect");

    let stale = editor.handle_message(PortfolioMessage::SaveResult {
        request_id,
        project_id,
        revision: DocumentRevision(revision.0 + 99),
        outcome: SaveOutcome::Success,
    });
    assert!(matches!(
        stale.as_slice(),
        [AthenaFrontendMessage::Diagnostic { .. }]
    ));
    assert!(editor.state_snapshot().unwrap().save_pending);

    let success = editor.handle_message(PortfolioMessage::SaveResult {
        request_id,
        project_id,
        revision,
        outcome: SaveOutcome::Success,
    });
    assert!(success.iter().any(|effect| matches!(
        effect,
        AthenaFrontendMessage::DirtyStateChanged { dirty: false }
    )));
    let state = editor.state_snapshot().unwrap();
    assert!(!state.dirty);
    assert!(!state.save_pending);
}

#[test]
fn post_request_edits_remain_dirty_and_cancelled_or_failed_requests_can_retry() {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(20));
    let mut editor = AthenaEditor::default();
    editor.handle_message(PortfolioMessage::CreateProject {
        project_id,
        initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1020)),
        name: "Main Distribution".into(),
    });
    editor.handle_message(DocumentMessage::RenameProject {
        name: "Plant A".into(),
    });
    let save = editor.handle_message(PortfolioMessage::RequestSave);
    let (request_id, revision) = save
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                revision,
                ..
            } => Some((*request_id, *revision)),
            _ => None,
        })
        .unwrap();
    editor.handle_message(DocumentMessage::SetProjectVariable {
        key: "plant".into(),
        value: Some("PLANT-A".into()),
    });
    editor.handle_message(PortfolioMessage::SaveResult {
        request_id,
        project_id,
        revision,
        outcome: SaveOutcome::Success,
    });
    assert!(editor.state_snapshot().unwrap().dirty);

    let retry = editor.handle_message(PortfolioMessage::RequestSave);
    let (retry_id, retry_revision) = retry
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                revision,
                ..
            } => Some((*request_id, *revision)),
            _ => None,
        })
        .unwrap();
    editor.handle_message(PortfolioMessage::SaveResult {
        request_id: retry_id,
        project_id,
        revision: retry_revision,
        outcome: SaveOutcome::Cancelled,
    });
    assert!(!editor.state_snapshot().unwrap().save_pending);

    let failed = editor.handle_message(PortfolioMessage::RequestSave);
    let (failed_id, failed_revision) = failed
        .iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                revision,
                ..
            } => Some((*request_id, *revision)),
            _ => None,
        })
        .unwrap();
    editor.handle_message(PortfolioMessage::SaveResult {
        request_id: failed_id,
        project_id,
        revision: failed_revision,
        outcome: SaveOutcome::Failed {
            message: "disk full".into(),
        },
    });
    assert!(!editor.state_snapshot().unwrap().save_pending);
    assert!(editor.state_snapshot().unwrap().dirty);
}

#[test]
fn dispatcher_accepts_only_root_message_families() {
    fn accepts_root(_: impl Into<AthenaMessage>) {}
    accepts_root(PortfolioMessage::RequestOpen);
    accepts_root(DocumentMessage::Undo);
}

#[test]
fn native_open_cancellation_and_read_failure_return_typed_diagnostics() {
    let mut editor = AthenaEditor::default();
    assert!(matches!(
        editor
            .handle_message(PortfolioMessage::OpenResult {
                outcome: OpenOutcome::Cancelled,
            })
            .as_slice(),
        [AthenaFrontendMessage::Diagnostic { code, .. }] if code == "open-cancelled"
    ));
    assert!(matches!(
        editor
            .handle_message(PortfolioMessage::OpenResult {
                outcome: OpenOutcome::Failed {
                    message: "permission denied".into(),
                },
            })
            .as_slice(),
        [AthenaFrontendMessage::Diagnostic { code, message }]
            if code == "open-failed" && message == "permission denied"
    ));
}

#[test]
fn public_application_source_exposes_no_mutable_domain_or_session_reference() {
    for source in [
        include_str!("../src/lib.rs"),
        include_str!("../src/application.rs"),
        include_str!("../src/portfolio.rs"),
        include_str!("../src/document.rs"),
    ] {
        assert!(!source.contains("-> &mut Project"));
        assert!(!source.contains("-> &mut EditorSession"));
    }
}
