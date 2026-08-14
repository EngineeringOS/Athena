//! Compact native hint and document-information region.

use athena_application::StatusBarState;
use gpui::{AnyElement, div, prelude::*, px, rgb};

use crate::shell::tokens;

pub(crate) fn render(state: &StatusBarState) -> AnyElement {
    div()
        .id("athena-status-bar")
        .debug_selector(|| "athena-status-bar".into())
        .h(px(tokens::STATUS_BAR_HEIGHT))
        .w_full()
        .flex_none()
        .flex()
        .items_center()
        .justify_between()
        .px_1()
        .bg(rgb(tokens::MILD_BLACK))
        .text_color(rgb(tokens::LIGHT_GRAY))
        .child(
            div()
                .flex_1()
                .overflow_hidden()
                .whitespace_nowrap()
                .child(state.hint.clone()),
        )
        .child(
            div()
                .flex()
                .gap_3()
                .children(state.document_info.iter().cloned()),
        )
        .into_any_element()
}
