//! Platform-neutral application coordination for Athena's typed editor boundary.
//!
//! Platform adapters submit typed messages and reduce ordered frontend effects;
//! project, history, layout, and widget authority remain inside this crate.

mod application;
mod dispatcher;
mod document;
mod fixture;
mod frontend_message;
mod message;
mod plate;
mod portfolio;
mod shell;
mod shell_fixture;
mod shell_model;
mod widget;

pub use application::{AthenaEditor, EditorSnapshot};
pub use dispatcher::AthenaDispatcher;
pub use fixture::canonical_m005_messages;
pub use frontend_message::{AthenaFrontendMessage, ResolvedTitleBlockDisplay, ShellEffect};
pub use message::{
    AthenaMessage, DocumentMessage, LayoutMessage, OpenOutcome, PortfolioMessage, SaveOutcome,
    ShellMessage,
};
pub use plate::LayoutTarget;
pub use portfolio::SaveRequestId;
pub use shell_fixture::canonical_m006_shell_messages;
pub use shell_model::{
    DockPlacement, DockTarget, FloatingLayersState, GroupId, MIN_PANEL_PX, PanelGroup, PanelRole,
    PanelTab, ShellFocus, ShellModelError, ShellNode, SplitAxis, SplitChild, SplitId, SplitNode,
    StatusBarState, TabId, TitleBarState, WorkspaceShell,
};
pub use widget::{Widget, WidgetCallback, WidgetId, WidgetKind, WidgetValue};

pub(crate) use document::{DocumentHandler, outline_effect};
pub(crate) use plate::PlateHandler;
pub(crate) use portfolio::PortfolioHandler;
pub(crate) use shell::ShellHandler;
