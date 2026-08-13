//! Transient selection state for the shared schematic editor session.

use std::collections::BTreeSet;

use athena_render::PresentationItemId;

/// Selection state is presentation-only and is never persisted with a project.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct SelectionState {
    items: BTreeSet<PresentationItemId>,
}

impl SelectionState {
    /// Returns the currently selected render items.
    #[must_use]
    pub fn items(&self) -> &BTreeSet<PresentationItemId> {
        &self.items
    }

    /// Clears the transient selection.
    pub fn clear(&mut self) {
        self.items.clear();
    }

    /// Replaces the selection with one item.
    pub fn replace(&mut self, item: PresentationItemId) {
        self.items.clear();
        self.items.insert(item);
    }

    /// Replaces the selection with the supplied presentation items.
    pub fn replace_all(&mut self, items: impl IntoIterator<Item = PresentationItemId>) {
        self.items.clear();
        self.items.extend(items);
    }

    /// Toggles every supplied item while preserving unrelated selection items.
    pub fn toggle_all(&mut self, items: impl IntoIterator<Item = PresentationItemId>) {
        for item in items {
            self.toggle(item);
        }
    }

    /// Toggles one item while preserving the rest of the selection.
    pub fn toggle(&mut self, item: PresentationItemId) {
        if !self.items.insert(item) {
            self.items.remove(&item);
        }
    }
}
