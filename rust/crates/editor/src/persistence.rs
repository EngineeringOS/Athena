use std::collections::VecDeque;

use thiserror::Error;

use crate::CommandEnvelope;

/// Errors returned by platform persistence adapters.
#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum PersistenceError {
    #[error("no snapshot has been saved")]
    SnapshotMissing,
    #[error("persistence backend failed: {message}")]
    Backend { message: String },
}

impl PersistenceError {
    #[must_use]
    pub fn backend(error: impl std::fmt::Display) -> Self {
        Self::Backend {
            message: error.to_string(),
        }
    }
}

/// Writes one complete encoded project snapshot.
pub trait SnapshotSink {
    fn write_snapshot(&mut self, bytes: &[u8]) -> Result<(), PersistenceError>;
}

/// Reads the most recently persisted encoded project snapshot.
pub trait SnapshotSource {
    fn read_snapshot(&self) -> Result<Vec<u8>, PersistenceError>;
}

/// Stores local commands until a later sync adapter acknowledges them.
pub trait OutboxStore {
    fn enqueue(&mut self, command: CommandEnvelope) -> Result<(), PersistenceError>;

    fn pending(&self) -> Result<Vec<CommandEnvelope>, PersistenceError>;
}

/// In-process adapter for tests and hosts that have not attached durable storage.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct InMemoryPersistence {
    snapshot: Option<Vec<u8>>,
    outbox: VecDeque<CommandEnvelope>,
}

impl InMemoryPersistence {
    #[must_use]
    pub const fn new() -> Self {
        Self {
            snapshot: None,
            outbox: VecDeque::new(),
        }
    }
}

impl SnapshotSink for InMemoryPersistence {
    fn write_snapshot(&mut self, bytes: &[u8]) -> Result<(), PersistenceError> {
        self.snapshot = Some(bytes.to_vec());
        Ok(())
    }
}

impl SnapshotSource for InMemoryPersistence {
    fn read_snapshot(&self) -> Result<Vec<u8>, PersistenceError> {
        self.snapshot
            .clone()
            .ok_or(PersistenceError::SnapshotMissing)
    }
}

impl OutboxStore for InMemoryPersistence {
    fn enqueue(&mut self, command: CommandEnvelope) -> Result<(), PersistenceError> {
        self.outbox.push_back(command);
        Ok(())
    }

    fn pending(&self) -> Result<Vec<CommandEnvelope>, PersistenceError> {
        Ok(self.outbox.iter().cloned().collect())
    }
}
