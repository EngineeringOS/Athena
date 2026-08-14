//! Zed-evidenced GPUI composition over Athena's recursive shell contract.
//!
//! The render order follows Zed's native workspace composition while visual
//! measurements follow the Graphite source ledger. These modules never own a
//! second panel topology or electrical document state.

pub mod electrical;
pub mod panel_group;
pub mod status_bar;
pub mod subdivision;
pub mod title_bar;
pub mod tokens;

use athena_application::WorkspaceShell;
use gpui::{Div, div, prelude::*};

use crate::panels::NativeShell;

/// Renders title, recursive workspace, status, and floating-layer order.
pub fn render(shell: &WorkspaceShell, cx: &mut gpui::Context<NativeShell>) -> Div {
    div()
        .relative()
        .size_full()
        .flex()
        .flex_col()
        .overflow_hidden()
        .font_family("Source Sans Pro")
        .text_size(gpui::px(tokens::BASE_FONT_SIZE))
        .line_height(gpui::relative(1.0))
        .text_color(gpui::rgb(tokens::NEAR_WHITE))
        .bg(gpui::rgb(tokens::MILD_BLACK))
        .child(title_bar::render(shell, cx))
        .child(subdivision::render_workspace(shell, cx))
        .child(status_bar::render(&shell.status_bar))
        .child(
            div()
                .absolute()
                .inset_0()
                .id("athena-floating-layers")
                .debug_selector(|| "athena-floating-layers".into()),
        )
}
