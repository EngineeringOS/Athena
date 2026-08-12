use std::collections::VecDeque;

use athena_format::{FormatError, SnapshotStore};
use thiserror::Error;

use crate::{AppliedCommand, ApplyError, EditorCommand, EditorState};

/// Bounded command history. It deliberately stores only document commands and
/// their inverses; tool, selection, viewport, and other UI state stay outside.
#[derive(Clone, Debug)]
pub struct History {
    limit: usize,
    undo: VecDeque<AppliedCommand>,
    redo: VecDeque<AppliedCommand>,
}

impl History {
    #[must_use]
    pub fn new(limit: usize) -> Self {
        Self {
            limit,
            undo: VecDeque::new(),
            redo: VecDeque::new(),
        }
    }

    #[must_use]
    pub fn undo_len(&self) -> usize {
        self.undo.len()
    }

    #[must_use]
    pub fn redo_len(&self) -> usize {
        self.redo.len()
    }

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

    pub fn undo(&mut self, state: &mut EditorState) -> Result<bool, HistoryError> {
        let Some(applied) = self.undo.back().cloned() else {
            return Ok(false);
        };
        state.apply(applied.inverse.clone())?;
        self.undo.pop_back();
        self.redo.push_back(applied);
        Ok(true)
    }

    pub fn redo(&mut self, state: &mut EditorState) -> Result<bool, HistoryError> {
        let Some(applied) = self.redo.back().cloned() else {
            return Ok(false);
        };
        state.apply(applied.command.clone())?;
        self.redo.pop_back();
        self.undo.push_back(applied);
        Ok(true)
    }
}

#[derive(Debug, Error)]
pub enum HistoryError {
    #[error(transparent)]
    Apply(#[from] ApplyError),
}

/// Encodes the complete persisted project for byte-exact test assertions.
pub fn snapshot_bytes(project: &athena_domain::Project) -> Result<Vec<u8>, FormatError> {
    SnapshotStore::encode_snapshot(project)
}

/// Returns whether two projects have identical persisted snapshot bytes.
pub fn snapshots_equal(
    left: &athena_domain::Project,
    right: &athena_domain::Project,
) -> Result<bool, FormatError> {
    Ok(snapshot_bytes(left)? == snapshot_bytes(right)?)
}
