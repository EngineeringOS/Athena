use athena_application::{AthenaFrontendMessage, DocumentMessage, PortfolioMessage, SaveOutcome};
use athena_domain::{FolioId, ProjectId};
use athena_web_core::WebEditorCore;
use uuid::Uuid;

#[test]
fn web_core_routes_create_add_move_edit_save_and_open_through_messages() {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(1));
    let control = FolioId::from_uuid(Uuid::from_u128(2));
    let io = FolioId::from_uuid(Uuid::from_u128(3));
    let mut core = WebEditorCore::new();
    core.dispatch_message(
        PortfolioMessage::CreateProject {
            project_id,
            initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1001)),
            name: "Main Distribution".into(),
        }
        .into(),
    );
    core.dispatch_message(
        DocumentMessage::AddFolio {
            folio_id: control,
            label: "Control".into(),
        }
        .into(),
    );
    core.dispatch_message(
        DocumentMessage::AddFolio {
            folio_id: io,
            label: "I/O".into(),
        }
        .into(),
    );
    core.dispatch_message(
        DocumentMessage::MoveFolio {
            folio_id: io,
            to: 1,
        }
        .into(),
    );
    core.dispatch_message(DocumentMessage::ActivateFolio { folio_id: control }.into());
    core.dispatch_message(
        DocumentMessage::SetProjectVariable {
            key: "plant".into(),
            value: Some("PLANT-A".into()),
        }
        .into(),
    );

    let save = core.dispatch_message(PortfolioMessage::RequestSave.into());
    let (request_id, revision, bytes) = save
        .into_iter()
        .find_map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                revision,
                bytes,
                ..
            } => Some((request_id, revision, bytes)),
            _ => None,
        })
        .expect("save effect");
    core.deliver_save_result(request_id, project_id, revision, SaveOutcome::Success);

    let before = core.state_snapshot().unwrap();
    core.dispatch_message(PortfolioMessage::CloseProject.into());
    core.deliver_open_bytes(bytes);
    let reopened = core.state_snapshot().unwrap();
    assert_eq!(reopened.project, before.project);
    assert_eq!(reopened.active_folio_id, control);
    assert!(!reopened.dirty);
}

#[test]
fn json_protocol_is_tagged_and_rejects_untyped_snapshots() {
    let mut core = WebEditorCore::new();
    let message = serde_json::to_string(&athena_application::AthenaMessage::Portfolio(
        PortfolioMessage::CreateProject {
            project_id: ProjectId::from_uuid(Uuid::from_u128(10)),
            initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1010)),
            name: "Browser".into(),
        },
    ))
    .unwrap();
    assert!(message.contains("\"type\":\"Portfolio\""));
    let effects = core.dispatch_json(&message).unwrap();
    assert!(effects.contains("ProjectOpened"));
    assert!(core.dispatch_json(r#"{"project":{"name":"bad"}}"#).is_err());
}

#[test]
fn stale_save_result_does_not_release_the_active_request() {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(20));
    let mut core = WebEditorCore::new();
    core.dispatch_message(
        PortfolioMessage::CreateProject {
            project_id,
            initial_folio_id: FolioId::from_uuid(Uuid::from_u128(1020)),
            name: "Browser".into(),
        }
        .into(),
    );
    core.dispatch_message(
        DocumentMessage::RenameProject {
            name: "Edited".into(),
        }
        .into(),
    );
    let save = core.dispatch_message(PortfolioMessage::RequestSave.into());
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
    core.deliver_save_result(
        request_id,
        project_id,
        athena_editor::DocumentRevision(revision.0 + 1),
        SaveOutcome::Success,
    );
    assert!(core.state_snapshot().unwrap().save_pending);
    core.deliver_save_result(request_id, project_id, revision, SaveOutcome::Cancelled);
    assert!(!core.state_snapshot().unwrap().save_pending);
    assert!(core.state_snapshot().unwrap().dirty);
}
