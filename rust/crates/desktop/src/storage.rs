//! Desktop-only filesystem adapter for application save/open effects.

use std::{
    fs::{self, OpenOptions},
    io::{self, Write},
    path::{Path, PathBuf},
    sync::atomic::{AtomicU64, Ordering},
};

static TEMPORARY_SEQUENCE: AtomicU64 = AtomicU64::new(1);

/// Local filesystem store used only after the application requests platform I/O.
#[derive(Clone, Debug)]
pub struct FileSnapshotStore {
    path: PathBuf,
}

impl FileSnapshotStore {
    /// Creates a store targeting one user-selected project path.
    #[must_use]
    pub fn new(path: impl Into<PathBuf>) -> Self {
        Self { path: path.into() }
    }

    /// Reads the selected project bytes without interpreting document state.
    pub fn read(&self) -> io::Result<Vec<u8>> {
        fs::read(&self.path)
    }

    /// Durably writes a sibling temporary file and atomically replaces the target.
    pub fn write(&self, bytes: &[u8]) -> io::Result<()> {
        write_snapshot_with_replacement(&self.path, bytes, replace_file)
    }
}

fn write_snapshot_with_replacement<F>(path: &Path, bytes: &[u8], replace: F) -> io::Result<()>
where
    F: FnOnce(&Path, &Path) -> io::Result<()>,
{
    let temporary_path = temporary_sibling_path(path)?;
    let write_result = (|| {
        let mut temporary = OpenOptions::new()
            .create_new(true)
            .write(true)
            .open(&temporary_path)?;
        temporary.write_all(bytes)?;
        temporary.sync_all()?;
        drop(temporary);
        replace(&temporary_path, path)
    })();
    if write_result.is_err() {
        let _ = fs::remove_file(&temporary_path);
    }
    write_result
}

fn temporary_sibling_path(path: &Path) -> io::Result<PathBuf> {
    let file_name = path.file_name().ok_or_else(|| {
        io::Error::new(io::ErrorKind::InvalidInput, "project path must name a file")
    })?;
    let sequence = TEMPORARY_SEQUENCE.fetch_add(1, Ordering::Relaxed);
    Ok(path.with_file_name(format!(
        ".{}.athena-tmp-{}-{sequence}",
        file_name.to_string_lossy(),
        std::process::id()
    )))
}

#[cfg(not(windows))]
fn replace_file(temporary: &Path, destination: &Path) -> io::Result<()> {
    fs::rename(temporary, destination)
}

#[cfg(windows)]
fn replace_file(temporary: &Path, destination: &Path) -> io::Result<()> {
    use std::os::windows::ffi::OsStrExt;
    use windows_sys::Win32::Storage::FileSystem::{
        MOVEFILE_REPLACE_EXISTING, MOVEFILE_WRITE_THROUGH, MoveFileExW,
    };

    let temporary = temporary
        .as_os_str()
        .encode_wide()
        .chain(Some(0))
        .collect::<Vec<_>>();
    let destination = destination
        .as_os_str()
        .encode_wide()
        .chain(Some(0))
        .collect::<Vec<_>>();
    // Windows' std::fs::rename cannot replace an existing file. MoveFileExW
    // preserves the atomic sibling-replacement contract used on Unix.
    let replaced = unsafe {
        MoveFileExW(
            temporary.as_ptr(),
            destination.as_ptr(),
            MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH,
        )
    };
    if replaced == 0 {
        Err(io::Error::last_os_error())
    } else {
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use std::{fs, io, path::PathBuf, time::SystemTime};

    use super::FileSnapshotStore;

    fn temporary_directory() -> PathBuf {
        let unique = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
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
    fn atomic_write_replaces_existing_bytes_and_removes_the_temporary_sibling() {
        let directory = temporary_directory();
        let path = directory.join("project.athena.json");
        let store = FileSnapshotStore::new(&path);
        store.write(b"first").expect("first snapshot writes");
        store
            .write(b"replacement")
            .expect("existing snapshot replaces");

        assert_eq!(store.read().expect("snapshot reopens"), b"replacement");
        assert!(fs::read_dir(&directory).unwrap().all(|entry| {
            !entry
                .unwrap()
                .file_name()
                .to_string_lossy()
                .contains(".athena-tmp-")
        }));
        fs::remove_dir_all(directory).expect("temporary directory removes");
    }

    #[test]
    fn failed_replacement_preserves_previous_bytes() {
        let directory = temporary_directory();
        let path = directory.join("project.athena.json");
        fs::write(&path, b"original").unwrap();

        let result = super::write_snapshot_with_replacement(&path, b"replacement", |_, _| {
            Err(io::Error::other("simulated replacement failure"))
        });

        assert!(result.is_err());
        assert_eq!(fs::read(&path).unwrap(), b"original");
        fs::remove_dir_all(directory).expect("temporary directory removes");
    }
}
