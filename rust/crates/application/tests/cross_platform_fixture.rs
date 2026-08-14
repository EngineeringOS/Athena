//! Canonical M005 semantic fixture for native/WASM adapter certification.

use std::collections::{BTreeMap, BTreeSet};

use athena_application::{
    AthenaEditor, AthenaFrontendMessage, PortfolioMessage, SaveOutcome, canonical_m005_messages,
};
use athena_domain::{FolioId, ProjectId};
use uuid::Uuid;

fn canonical_json<T: serde::Serialize>(value: &T) -> Vec<u8> {
    let mut value = serde_json::to_value(value).expect("fixture values serialize");
    let mut ids = BTreeMap::new();
    let mut next_id = 0;
    let known_ids = [0x10, 0x20, 0x30, 0x40]
        .into_iter()
        .map(|value| uuid::Uuid::from_u128(value).to_string())
        .collect::<BTreeSet<_>>();
    normalize_generated_ids(&mut value, &known_ids, &mut ids, &mut next_id);
    serde_json::to_vec(&value).expect("canonical fixture values serialize")
}

fn normalize_generated_ids(
    value: &mut serde_json::Value,
    known_ids: &BTreeSet<String>,
    ids: &mut BTreeMap<String, String>,
    next_id: &mut usize,
) {
    match value {
        serde_json::Value::String(text)
            if uuid::Uuid::parse_str(text).is_ok() && !known_ids.contains(text) =>
        {
            let canonical = ids.entry(text.clone()).or_insert_with(|| {
                let value = format!("generated-id-{next_id}");
                *next_id += 1;
                value
            });
            *text = canonical.clone();
        }
        serde_json::Value::Array(values) => {
            for value in values {
                normalize_generated_ids(value, known_ids, ids, next_id);
            }
        }
        serde_json::Value::Object(values) => {
            let previous = std::mem::take(values);
            for (key, mut value) in previous {
                let key = if uuid::Uuid::parse_str(&key).is_ok() && !known_ids.contains(&key) {
                    ids.entry(key)
                        .or_insert_with(|| {
                            let value = format!("generated-id-{next_id}");
                            *next_id += 1;
                            value
                        })
                        .clone()
                } else {
                    key
                };
                normalize_generated_ids(&mut value, known_ids, ids, next_id);
                values.insert(key, value);
            }
        }
        _ => {}
    }
}

fn semantic_trace(trace: Vec<AthenaFrontendMessage>) -> Vec<AthenaFrontendMessage> {
    trace
        .into_iter()
        .map(|effect| match effect {
            AthenaFrontendMessage::SaveRequested {
                request_id,
                project_id,
                revision,
                ..
            } => AthenaFrontendMessage::SaveRequested {
                request_id,
                project_id,
                revision,
                bytes: Vec::new(),
            },
            effect => effect,
        })
        .collect()
}

fn run_fixture() -> (
    athena_application::EditorSnapshot,
    Vec<AthenaFrontendMessage>,
) {
    let project_id = ProjectId::from_uuid(Uuid::from_u128(0x10));
    let initial = FolioId::from_uuid(Uuid::from_u128(0x40));
    let control = FolioId::from_uuid(Uuid::from_u128(0x20));
    let io = FolioId::from_uuid(Uuid::from_u128(0x30));
    let mut editor = AthenaEditor::default();
    let mut trace = Vec::new();
    let mut save_request = None;
    for message in canonical_m005_messages(project_id, initial, control, io) {
        let effects = editor.handle_message(message);
        if let Some(AthenaFrontendMessage::SaveRequested {
            request_id,
            project_id,
            revision,
            ..
        }) = effects
            .iter()
            .find(|effect| matches!(effect, AthenaFrontendMessage::SaveRequested { .. }))
        {
            save_request = Some((*request_id, *project_id, *revision));
        }
        trace.extend(effects);
    }
    let (request_id, project_id, revision) = save_request.expect("fixture requests one save");
    trace.extend(editor.handle_message(PortfolioMessage::SaveResult {
        request_id,
        project_id,
        revision,
        outcome: SaveOutcome::Success,
    }));
    (
        editor.state_snapshot().expect("fixture opens a project"),
        trace,
    )
}

#[test]
fn canonical_m005_messages_have_a_deterministic_native_state_and_trace() {
    let io = FolioId::from_uuid(Uuid::from_u128(0x30));
    let (snapshot, trace) = run_fixture();
    assert!(!snapshot.dirty);
    assert_eq!(snapshot.project.folio_order()[1], io);
    assert_eq!(snapshot.project.folio(io).unwrap().label, "I/O");
    assert_eq!(snapshot.project.variables["designer"], "A. Engineer");
    assert_eq!(
        snapshot.project.folio(io).unwrap().variables["area"],
        "MCC-01"
    );
    assert!(!trace.is_empty());
    assert!(!canonical_json(&semantic_trace(trace.clone())).is_empty());
    assert!(!canonical_json(&snapshot).is_empty());

    let (replay_snapshot, replay_trace) = run_fixture();
    assert_eq!(snapshot, replay_snapshot);
    assert_eq!(
        canonical_json(&semantic_trace(trace)),
        canonical_json(&semantic_trace(replay_trace))
    );
}
