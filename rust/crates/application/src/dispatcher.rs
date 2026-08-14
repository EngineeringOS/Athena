//! Deterministic FIFO routing between root message-handler families.

use std::collections::VecDeque;

use crate::{
    AthenaFrontendMessage, AthenaMessage, EditorSnapshot, PlateHandler, PortfolioHandler,
    ShellHandler, WorkspaceShell,
};

/// Owns handlers and prevents reentrant direct calls between sibling handlers.
#[derive(Default)]
pub struct AthenaDispatcher {
    portfolio: PortfolioHandler,
    plate: PlateHandler,
    shell: ShellHandler,
}

impl AthenaDispatcher {
    /// Routes one root message and all queued child messages in FIFO order.
    pub fn dispatch(&mut self, message: AthenaMessage) -> Vec<AthenaFrontendMessage> {
        let mut queue = VecDeque::from([message]);
        let mut effects = Vec::new();
        while let Some(message) = queue.pop_front() {
            match message {
                AthenaMessage::Portfolio(message) => {
                    let output = self.portfolio.handle(message);
                    effects.extend(output.effects);
                    queue.extend(output.messages);
                }
                AthenaMessage::Document(message) => {
                    let output = self.portfolio.handle_document(message);
                    effects.extend(output.effects);
                    queue.extend(output.messages);
                }
                AthenaMessage::Layout(message) => {
                    let snapshot = self.portfolio.snapshot();
                    let output = self.plate.handle(message, snapshot.as_ref());
                    effects.extend(output.effects);
                    queue.extend(output.messages);
                }
                AthenaMessage::Shell(message) => effects.extend(self.shell.handle(message)),
            }
        }
        effects
    }

    /// Returns an immutable clone of current application document state.
    #[must_use]
    pub fn state_snapshot(&self) -> Option<EditorSnapshot> {
        self.portfolio.snapshot()
    }

    /// Returns an immutable clone of the current recursive shell.
    #[must_use]
    pub fn shell_snapshot(&self) -> WorkspaceShell {
        self.shell.snapshot()
    }
}
