//! Platform-neutral application coordination for Athena's typed editor boundary.
//!
//! Platform adapters submit typed messages and reduce ordered frontend effects;
//! project, history, layout, and widget authority remain inside this crate.

mod application;
mod dispatcher;
mod document;
mod frontend_message;
mod layout;
mod message;
mod portfolio;
mod widget;

pub use application::{AthenaEditor, EditorSnapshot};
pub use dispatcher::AthenaDispatcher;
pub use frontend_message::{AthenaFrontendMessage, ResolvedTitleBlockDisplay};
pub use layout::{LayoutTarget, PanelId, PanelState, WorkspaceLayout};
pub use message::{
    AthenaMessage, DocumentMessage, LayoutMessage, OpenOutcome, PortfolioMessage, SaveOutcome,
};
pub use portfolio::SaveRequestId;
pub use widget::{Widget, WidgetCallback, WidgetId, WidgetKind, WidgetValue};

pub(crate) use document::{DocumentHandler, outline_effect};
pub(crate) use layout::LayoutHandler;
pub(crate) use portfolio::PortfolioHandler;
