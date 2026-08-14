//! Native/WASM protocol equivalence for the canonical M005 authoring workflow.

use athena_application::{
    AthenaEditor, AthenaFrontendMessage, PortfolioMessage, SaveOutcome, WorkspaceShell,
    canonical_m005_messages, canonical_m006_shell_messages,
};
use athena_domain::{FolioId, ProjectId};
use athena_web_core::WebEditorCore;
use uuid::Uuid;

fn fixture_ids() -> (ProjectId, FolioId, FolioId, FolioId) {
    (
        ProjectId::from_uuid(Uuid::from_u128(0x10)),
        FolioId::from_uuid(Uuid::from_u128(0x40)),
        FolioId::from_uuid(Uuid::from_u128(0x20)),
        FolioId::from_uuid(Uuid::from_u128(0x30)),
    )
}

fn stable_hash(bytes: &[u8]) -> u64 {
    bytes.iter().fold(0xcbf2_9ce4_8422_2325, |hash, byte| {
        (hash ^ u64::from(*byte)).wrapping_mul(0x0000_0100_0000_01b3)
    })
}

fn shell_hash(shell: &WorkspaceShell) -> u64 {
    stable_hash(&serde_json::to_vec(shell).expect("shell serializes"))
}

fn save_identity(
    effects: &[AthenaFrontendMessage],
) -> Option<(
    athena_application::SaveRequestId,
    ProjectId,
    athena_editor::DocumentRevision,
)> {
    effects.iter().find_map(|effect| match effect {
        AthenaFrontendMessage::SaveRequested {
            request_id,
            project_id,
            revision,
            ..
        } => Some((*request_id, *project_id, *revision)),
        _ => None,
    })
}

fn run_native() -> (Vec<u8>, Vec<AthenaFrontendMessage>) {
    let (project_id, initial, control, io) = fixture_ids();
    let mut editor = AthenaEditor::default();
    let mut trace = Vec::new();
    let mut save = None;
    for message in canonical_m005_messages(project_id, initial, control, io) {
        let effects = editor.handle_message(message);
        save = save.or_else(|| save_identity(&effects));
        trace.extend(effects);
    }
    let (request_id, project_id, revision) = save.expect("fixture emits one save request");
    trace.extend(editor.handle_message(PortfolioMessage::SaveResult {
        request_id,
        project_id,
        revision,
        outcome: SaveOutcome::Success,
    }));
    let state = serde_json::to_vec(
        &editor
            .state_snapshot()
            .expect("native project remains open"),
    )
    .expect("native state serializes");
    (state, trace)
}

fn run_web() -> (Vec<u8>, Vec<AthenaFrontendMessage>) {
    let (project_id, initial, control, io) = fixture_ids();
    let mut editor = WebEditorCore::new();
    let mut trace = Vec::new();
    let mut save = None;
    for message in canonical_m005_messages(project_id, initial, control, io) {
        let message = serde_json::to_string(&message).expect("fixture message serializes");
        let effects = serde_json::from_str::<Vec<AthenaFrontendMessage>>(
            &editor
                .dispatch_json(&message)
                .expect("web dispatch succeeds"),
        )
        .expect("web effects deserialize");
        save = save.or_else(|| save_identity(&effects));
        trace.extend(effects);
    }
    let (request_id, project_id, revision) = save.expect("fixture emits one save request");
    trace.extend(editor.deliver_save_result(
        request_id,
        project_id,
        revision,
        SaveOutcome::Success,
    ));
    let state = serde_json::to_vec(&editor.state_snapshot().expect("web project remains open"))
        .expect("web state serializes");
    (state, trace)
}

#[test]
fn native_and_web_protocols_produce_identical_state_hashes_and_effect_traces() {
    let (native_state, native_trace) = run_native();
    let (web_state, web_trace) = run_web();

    assert_eq!(stable_hash(&native_state), stable_hash(&web_state));
    assert_eq!(native_state, web_state);
    assert_eq!(native_trace, web_trace);
}

#[test]
fn native_and_web_shell_protocols_produce_identical_hashes_and_effect_traces() {
    let messages = canonical_m006_shell_messages();
    let mut native = AthenaEditor::default();
    let mut web = WebEditorCore::new();
    let mut native_trace = Vec::new();
    let mut web_trace = Vec::new();

    for message in messages {
        native_trace.extend(native.handle_message(message.clone()));
        let message_json = serde_json::to_string(&message).expect("shell message serializes");
        let effects = web
            .dispatch_json(&message_json)
            .expect("web shell dispatch succeeds");
        web_trace.extend(
            serde_json::from_str::<Vec<AthenaFrontendMessage>>(&effects)
                .expect("web shell effects deserialize"),
        );
    }

    assert_eq!(native_trace, web_trace);
    assert_eq!(
        shell_hash(&native.shell_snapshot()),
        shell_hash(&web.shell_snapshot())
    );
    assert_eq!(native.shell_snapshot(), web.shell_snapshot());
}
