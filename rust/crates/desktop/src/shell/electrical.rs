//! Inert QElectroTech-evidenced electrical surfaces for the native M006 shell.

use athena_application::PanelRole;
use gpui::{AnyElement, div, prelude::*, px, relative, rgb};
use gpui_component::{
    Disableable, IconName, Sizable,
    button::{Button, ButtonVariants as _},
};

use crate::shell::tokens;

/// One disabled electrical command rendered around the empty native folio.
pub struct ElectricalToolSpec {
    /// Accessible command name and tooltip text.
    pub label: &'static str,
    /// Stable GPUI test/debug selector.
    pub selector: &'static str,
    /// `gpui-component` icon representing the electrical command.
    pub icon: IconName,
    /// Whether the M006 inert shell prevents command activation.
    pub disabled: bool,
}

/// QET toolbar vocabulary retained as inert M006 native controls.
pub const ELECTRICAL_TOOLS: [ElectricalToolSpec; 10] = [
    tool("Select", "tool-select", IconName::Inspector),
    tool("Move view", "tool-move", IconName::Map),
    tool("Place conductor", "tool-conductor", IconName::Minus),
    tool("Place element", "tool-element", IconName::LayoutDashboard),
    tool("Add annotation", "tool-annotation", IconName::ALargeSmall),
    tool("Display grid", "tool-grid", IconName::GalleryVerticalEnd),
    tool("Zoom content", "tool-zoom-content", IconName::Search),
    tool("Fit in view", "tool-fit", IconName::Frame),
    tool("Reset zoom", "tool-reset-zoom", IconName::Redo2),
    tool(
        "Automatic conductor creation",
        "tool-auto-conductor",
        IconName::Asterisk,
    ),
];

const fn tool(label: &'static str, selector: &'static str, icon: IconName) -> ElectricalToolSpec {
    ElectricalToolSpec {
        label,
        selector,
        icon,
        disabled: true,
    }
}

pub(crate) fn render(role: PanelRole) -> AnyElement {
    match role {
        PanelRole::FolioDocument => render_folio(),
        _ => render_empty_state(role),
    }
}

fn render_empty_state(role: PanelRole) -> AnyElement {
    let (heading, empty) = match role {
        PanelRole::Project => ("Project", "No project structure"),
        PanelRole::Folios => ("Folios", "No additional folios"),
        PanelRole::Elements => ("Elements", "No electrical elements"),
        PanelRole::TitleBlocks => ("Title Blocks", "No title block templates"),
        PanelRole::SelectionProperties => ("Selection Properties", "Nothing selected"),
        PanelRole::FolioProperties => ("Folio Properties", "No folio properties"),
        PanelRole::Diagnostics => ("Diagnostics", "No diagnostics"),
        PanelRole::History => ("History", "No history entries"),
        PanelRole::FolioDocument => unreachable!(),
    };
    div()
        .flex_1()
        .p_3()
        .flex()
        .flex_col()
        .gap_3()
        .bg(rgb(tokens::DARK_GRAY))
        .text_color(rgb(tokens::SOFT_GRAY))
        .child(
            div()
                .pb_2()
                .border_b_1()
                .border_color(rgb(tokens::DIM_GRAY))
                .text_color(rgb(tokens::MILD_WHITE))
                .child(heading),
        )
        .child(empty)
        .into_any_element()
}

fn render_folio() -> AnyElement {
    div()
        .flex_1()
        .min_w(px(0.0))
        .min_h(px(0.0))
        .flex()
        .child(
            div()
                .w(px(34.0))
                .flex_none()
                .flex()
                .flex_col()
                .bg(rgb(tokens::NEAR_BLACK))
                .children(ELECTRICAL_TOOLS[..5].iter().map(render_tool)),
        )
        .child(
            div()
                .flex_1()
                .min_w(px(0.0))
                .flex()
                .flex_col()
                .child(
                    div()
                        .h(px(30.0))
                        .flex_none()
                        .flex()
                        .items_center()
                        .bg(rgb(tokens::NEAR_BLACK))
                        .children(ELECTRICAL_TOOLS[5..].iter().map(render_tool)),
                )
                .child(render_page_surface()),
        )
        .into_any_element()
}

fn render_tool(tool: &ElectricalToolSpec) -> AnyElement {
    let selector = tool.selector;
    div()
        .debug_selector(move || selector.into())
        .size_8()
        .flex()
        .items_center()
        .justify_center()
        .child(
            Button::new(selector)
                .icon(tool.icon.clone())
                .ghost()
                .xsmall()
                .tooltip(tool.label)
                .disabled(tool.disabled),
        )
        .into_any_element()
}

fn render_page_surface() -> AnyElement {
    let mut page = div()
        .id("folio-page")
        .debug_selector(|| "folio-page".into())
        .relative()
        .w(relative(0.92))
        .max_w(px(960.0));
    page.style().aspect_ratio = Some(1.414);

    div()
        .flex_1()
        .min_h(px(0.0))
        .p_8()
        .flex()
        .items_center()
        .justify_center()
        .overflow_hidden()
        .bg(rgb(tokens::DIM_GRAY))
        .child(
            page.border_1()
                .border_color(rgb(tokens::MIDDLE_GRAY))
                .bg(rgb(0xf8f8f6))
                .child(render_grid())
                .child(
                    div()
                        .debug_selector(|| "title-block-frame".into())
                        .absolute()
                        .right_4()
                        .bottom_4()
                        .w(relative(0.46))
                        .h(relative(0.13))
                        .border_1()
                        .border_color(rgb(tokens::DARK_GRAY)),
                ),
        )
        .into_any_element()
}

fn render_grid() -> AnyElement {
    let mut lines = Vec::new();
    for index in 1..20 {
        let offset = relative(index as f32 / 20.0);
        lines.push(
            div()
                .absolute()
                .top_0()
                .bottom_0()
                .left(offset)
                .w(px(1.0))
                .bg(rgb(0xe1e1dd))
                .into_any_element(),
        );
    }
    for index in 1..14 {
        let offset = relative(index as f32 / 14.0);
        lines.push(
            div()
                .absolute()
                .left_0()
                .right_0()
                .top(offset)
                .h(px(1.0))
                .bg(rgb(0xe1e1dd))
                .into_any_element(),
        );
    }
    div()
        .debug_selector(|| "folio-grid".into())
        .absolute()
        .inset_4()
        .children(lines)
        .into_any_element()
}
