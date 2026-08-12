//! Desktop-only filesystem adapters for local project snapshots.

use std::{
    fs, io,
    path::{Path, PathBuf},
};

use athena_editor::{PersistenceError, SnapshotSink, SnapshotSource};

/// Local filesystem snapshot adapter for the native desktop shell.
#[derive(Clone, Debug)]
pub struct FileSnapshotStore {
    path: PathBuf,
}

impl FileSnapshotStore {
    #[must_use]
    pub fn new(path: impl Into<PathBuf>) -> Self {
        Self { path: path.into() }
    }
}

impl SnapshotSink for FileSnapshotStore {
    fn write_snapshot(&mut self, bytes: &[u8]) -> Result<(), PersistenceError> {
        write_snapshot_with_replacement(&self.path, bytes, |temporary, destination| {
            fs::rename(temporary, destination)
        })
        .map_err(PersistenceError::backend)
    }
}

impl SnapshotSource for FileSnapshotStore {
    fn read_snapshot(&self) -> Result<Vec<u8>, PersistenceError> {
        fs::read(&self.path).map_err(PersistenceError::backend)
    }
}

fn write_snapshot_with_replacement<F>(path: &Path, bytes: &[u8], replace: F) -> io::Result<()>
where
    F: FnOnce(&Path, &Path) -> io::Result<()>,
{
    let temporary_path = temporary_sibling_path(path)?;
    let write_result = (|| {
        fs::write(&temporary_path, bytes)?;
        replace(&temporary_path, path)
    })();
    if write_result.is_err() {
        let _ = fs::remove_file(&temporary_path);
    }
    write_result
}

fn temporary_sibling_path(path: &Path) -> io::Result<PathBuf> {
    let file_name = path.file_name().ok_or_else(|| {
        io::Error::new(
            io::ErrorKind::InvalidInput,
            "snapshot path must name a file",
        )
    })?;
    Ok(path.with_file_name(format!(
        ".{}.athena-tmp-{}",
        file_name.to_string_lossy(),
        std::process::id()
    )))
}

#[cfg(test)]
mod tests {
    use std::{
        fs,
        path::PathBuf,
        time::{SystemTime, UNIX_EPOCH},
    };

    use athena_editor::{SnapshotSink, SnapshotSource};

    use super::FileSnapshotStore;

    fn temporary_directory() -> PathBuf {
        let unique = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("the clock is after the Unix epoch")
            .as_nanos();
        let directory = std::env::temp_dir().join(format!(
            "athena-desktop-storage-{}-{unique}",
            std::process::id()
        ));
        fs::create_dir_all(&directory).expect("test directory should be created");
        directory
    }

    #[test]
    fn snapshot_write_uses_a_temporary_sibling_and_reopens() {
        let directory = temporary_directory();
        let path = directory.join("project.athena.json");
        let snapshot = br#"{"project":"motor-control"}"#;
        let mut store = FileSnapshotStore::new(&path);

        store
            .write_snapshot(snapshot)
            .expect("snapshot should write atomically");
        store
            .write_snapshot(br#"{"project":"replacement"}"#)
            .expect("existing snapshot should atomically replace");

        assert_eq!(
            store.read_snapshot().expect("saved snapshot should reopen"),
            br#"{"project":"replacement"}"#
        );
        assert!(
            fs::read_dir(&directory)
                .expect("test directory is readable")
                .all(|entry| {
                    !entry
                        .expect("directory entry is readable")
                        .file_name()
                        .to_string_lossy()
                        .contains(".athena-tmp-")
                }),
            "temporary sibling must be renamed away"
        );
        fs::remove_dir_all(directory).expect("test directory should be removed");
    }

    #[test]
    fn failed_replacement_preserves_the_previous_snapshot_bytes() {
        let directory = temporary_directory();
        let path = directory.join("project.athena.json");
        let original = b"original snapshot";
        fs::write(&path, original).expect("previous snapshot should exist");

        let result = super::write_snapshot_with_replacement(&path, b"replacement", |_, _| {
            Err(std::io::Error::other("simulated rename failure"))
        });

        assert!(result.is_err());
        assert_eq!(
            fs::read(&path).expect("previous snapshot should remain readable"),
            original
        );
        fs::remove_dir_all(directory).expect("test directory should be removed");
    }
}
