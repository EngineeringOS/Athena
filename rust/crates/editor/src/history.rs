//! Transactional command history and revision-bound save checkpoints.

use std::collections::VecDeque;

use athena_domain::FolioId;
use athena_format::{FormatError, PersistedProject, SnapshotStore};
use thiserror::Error;

use crate::{AppliedCommand, ApplyError, DocumentRevision, EditorCommand, EditorState};

/// Immutable bytes generated for one semantic document revision.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct SaveSnapshot {
    /// Revision represented by the encoded bytes.
    pub revision: DocumentRevision,
    /// Deterministic persisted project bytes for that revision.
    pub bytes: Vec<u8>,
}

/// Bounded document history with one revision-bound pending save.
#[derive(Clone, Debug)]
pub struct History {
    limit: usize,
    undo: VecDeque<AppliedCommand>,
    redo: VecDeque<AppliedCommand>,
    saved_revision: DocumentRevision,
    pending_save_revision: Option<DocumentRevision>,
}

impl History {
    /// Creates an empty history whose initial revision is clean.
    #[must_use]
    pub fn new(limit: usize) -> Self {
        Self {
            limit,
            undo: VecDeque::new(),
            redo: VecDeque::new(),
            saved_revision: DocumentRevision::INITIAL,
            pending_save_revision: None,
        }
    }

    /// Returns the number of undoable committed transactions.
    #[must_use]
    pub fn undo_len(&self) -> usize {
        self.undo.len()
    }

    /// Returns the number of redoable committed transactions.
    #[must_use]
    pub fn redo_len(&self) -> usize {
        self.redo.len()
    }

    /// Returns whether the current semantic revision differs from the checkpoint.
    #[must_use]
    pub fn is_dirty(&self, state: &EditorState) -> bool {
        state.revision() != self.saved_revision
    }

    /// Returns whether a revision is waiting for a platform save result.
    #[must_use]
    pub const fn save_is_pending(&self) -> bool {
        self.pending_save_revision.is_some()
    }

    /// Commits one validated command as one history transaction.
    pub fn apply(
        &mut self,
        state: &mut EditorState,
        command: EditorCommand,
    ) -> Result<(), HistoryError> {
        let applied = state.apply(command)?;
        self.redo.clear();
        if self.limit == 0 {
            return Ok(());
        }
        self.undo.push_back(applied);
        while self.undo.len() > self.limit {
            self.undo.pop_front();
        }
        Ok(())
    }

    /// Restores the state and revision identity before the latest transaction.
    pub fn undo(&mut self, state: &mut EditorState) -> Result<bool, HistoryError> {
        let Some(applied) = self.undo.back().cloned() else {
            return Ok(false);
        };
        state.apply_existing(applied.inverse.clone(), applied.before_revision)?;
        self.undo.pop_back();
        self.redo.push_back(applied);
        Ok(true)
    }

    /// Restores the state and revision identity after the latest undone transaction.
    pub fn redo(&mut self, state: &mut EditorState) -> Result<bool, HistoryError> {
        let Some(applied) = self.redo.back().cloned() else {
            return Ok(false);
        };
        state.apply_existing(applied.command.clone(), applied.after_revision)?;
        self.redo.pop_back();
        self.undo.push_back(applied);
        Ok(true)
    }

    /// Generates immutable save bytes without mutating history or dirty state.
    pub fn begin_save(
        &mut self,
        state: &EditorState,
        active_folio_id: FolioId,
    ) -> Result<SaveSnapshot, HistoryError> {
        if self.pending_save_revision.is_some() {
            return Err(HistoryError::SaveAlreadyPending);
        }
        let revision = state.revision();
        let document = PersistedProject::new(state.project().clone(), active_folio_id)?;
        let bytes = SnapshotStore::encode_snapshot(&document)?;
        self.pending_save_revision = Some(revision);
        Ok(SaveSnapshot { revision, bytes })
    }

    /// Establishes a clean checkpoint only for the matching pending revision.
    pub fn complete_save(&mut self, revision: DocumentRevision) -> bool {
        if self.pending_save_revision != Some(revision) {
            return false;
        }
        self.pending_save_revision = None;
        self.saved_revision = revision;
        self.undo
            .retain(|transaction| transaction.after_revision > revision);
        self.redo.clear();
        true
    }

    /// Releases a matching cancelled save without changing history or checkpoint.
    pub fn cancel_save(&mut self, revision: DocumentRevision) -> bool {
        self.release_failed_save(revision)
    }

    /// Releases a matching failed save without changing history or checkpoint.
    pub fn fail_save(&mut self, revision: DocumentRevision) -> bool {
        self.release_failed_save(revision)
    }

    fn release_failed_save(&mut self, revision: DocumentRevision) -> bool {
        if self.pending_save_revision != Some(revision) {
            return false;
        }
        self.pending_save_revision = None;
        true
    }
}

/// Failures produced by command history and save-byte generation.
#[derive(Debug, Error)]
pub enum HistoryError {
    /// A command could not be applied atomically.
    #[error(transparent)]
    Apply(#[from] ApplyError),
    /// Save bytes could not be encoded or validated.
    #[error(transparent)]
    Format(#[from] FormatError),
    /// Only one save operation may be active for a document.
    #[error("a save is already pending for this document")]
    SaveAlreadyPending,
}

/// Encodes a project using its first folio as local active view state.
pub fn snapshot_bytes(project: &athena_domain::Project) -> Result<Vec<u8>, FormatError> {
    let active_folio_id = project.folio_order()[0];
    let document = PersistedProject::new(project.clone(), active_folio_id)?;
    SnapshotStore::encode_snapshot(&document)
}

/// Returns whether two projects have identical persisted snapshot bytes.
pub fn snapshots_equal(
    left: &athena_domain::Project,
    right: &athena_domain::Project,
) -> Result<bool, FormatError> {
    Ok(snapshot_bytes(left)? == snapshot_bytes(right)?)
}
