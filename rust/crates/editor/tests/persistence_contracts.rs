use athena_domain::{Project, SheetSettings};
use athena_editor::{
    CommandEnvelope, EditorCommand, InMemoryPersistence, OutboxStore, SnapshotSink, SnapshotSource,
};
use uuid::Uuid;

fn envelope(project: &Project, sequence: u64) -> CommandEnvelope {
    let sheet_id = project.sheet_order()[0];
    CommandEnvelope {
        operation_id: Uuid::new_v4(),
        project_id: project.id,
        sheet_id,
        base_revision: sequence,
        author_id: Uuid::new_v4(),
        session_id: Uuid::new_v4(),
        command_version: 1,
        payload: EditorCommand::ApplySheetSettings {
            sheet_id,
            settings: SheetSettings::default(),
        },
    }
}

#[test]
fn in_memory_snapshot_reopens_the_bytes_written_to_it() {
    let snapshot = br#"{"format":"athena-electrical-project"}"#.to_vec();
    let mut storage = InMemoryPersistence::new();

    storage
        .write_snapshot(&snapshot)
        .expect("in-memory snapshot write should succeed");

    assert_eq!(
        storage
            .read_snapshot()
            .expect("written snapshot should reopen"),
        snapshot
    );
}

#[test]
fn in_memory_outbox_preserves_unsynced_command_order() {
    let project = Project::new("Persistence");
    let first = envelope(&project, 1);
    let second = envelope(&project, 2);
    let mut storage = InMemoryPersistence::new();

    storage
        .enqueue(first.clone())
        .expect("first command should enqueue");
    storage
        .enqueue(second.clone())
        .expect("second command should enqueue");

    assert_eq!(
        storage
            .pending()
            .expect("pending commands should be readable"),
        vec![first, second]
    );
}
