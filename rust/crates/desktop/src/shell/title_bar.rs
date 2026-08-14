//! Compact native title/menu/actions region following Zed workspace ownership.

use athena_application::{PanelRole, ShellFocus, ShellMessage, WorkspaceShell};
use gpui::{AnyElement, div, prelude::*, px, rgb};
use gpui_component::{
    Disableable, IconName, Selectable, Sizable,
    button::{Button, ButtonVariants as _},
};

use crate::{panels::NativeShell, shell::tokens};

pub(crate) fn render(shell: &WorkspaceShell, cx: &mut gpui::Context<NativeShell>) -> AnyElement {
    let focused = shell.focus == ShellFocus::Document;
    let project_open = contains_role(&shell.root, PanelRole::Project);
    let properties_open = contains_role(&shell.root, PanelRole::SelectionProperties);
    let history_open = contains_role(&shell.root, PanelRole::History);
    let history_selector = if history_open {
        "close-history"
    } else {
        "reopen-history"
    };

    div()
        .id("athena-title-bar")
        .debug_selector(|| "athena-title-bar".into())
        .h(px(tokens::TITLE_BAR_HEIGHT))
        .flex_none()
        .flex()
        .items_center()
        .bg(rgb(tokens::MILD_BLACK))
        .child(menu_command("File"))
        .child(menu_command("Edit"))
        .child(menu_command("View"))
        .child(
            div()
                .flex_1()
                .flex()
                .justify_center()
                .items_center()
                .text_color(rgb(tokens::LIGHT_GRAY))
                .child(shell.title_bar.document_label.clone()),
        )
        .child(control(
            "toggle-project",
            IconName::PanelLeft,
            "Toggle project panels",
            project_open,
            cx.listener(|this, _, _, cx| this.toggle_role(PanelRole::Project, cx)),
        ))
        .child(control(
            "toggle-properties",
            IconName::PanelRight,
            "Toggle properties panels",
            properties_open,
            cx.listener(|this, _, _, cx| this.toggle_role(PanelRole::SelectionProperties, cx)),
        ))
        .child(control(
            history_selector,
            IconName::Undo2,
            if history_open {
                "Close history"
            } else {
                "Reopen history"
            },
            history_open,
            cx.listener(|this, _, _, cx| this.toggle_role(PanelRole::History, cx)),
        ))
        .child(control(
            "focus-document",
            if focused {
                IconName::Minimize
            } else {
                IconName::Maximize
            },
            if focused {
                "Restore workspace"
            } else {
                "Focus document"
            },
            focused,
            cx.listener(move |this, _, _, cx| {
                this.handle_shell_message(ShellMessage::SetDocumentFocus { focused: !focused }, cx)
            }),
        ))
        .children([
            disabled_window_control("window-minimize", IconName::WindowMinimize, "Minimize"),
            disabled_window_control("window-maximize", IconName::WindowMaximize, "Maximize"),
            disabled_window_control("window-close", IconName::WindowClose, "Close"),
        ])
        .into_any_element()
}

fn menu_command(label: &'static str) -> AnyElement {
    div()
        .h_full()
        .px_2()
        .flex()
        .items_center()
        .rounded(px(tokens::CONTROL_RADIUS))
        .child(label)
        .into_any_element()
}

fn control(
    selector: &'static str,
    icon: IconName,
    tooltip: &'static str,
    selected: bool,
    listener: impl Fn(&gpui::ClickEvent, &mut gpui::Window, &mut gpui::App) + 'static,
) -> AnyElement {
    div()
        .debug_selector(move || selector.into())
        .h_full()
        .flex()
        .items_center()
        .child(
            Button::new(selector)
                .icon(icon)
                .ghost()
                .xsmall()
                .selected(selected)
                .tooltip(tooltip)
                .on_click(listener),
        )
        .into_any_element()
}

fn disabled_window_control(
    selector: &'static str,
    icon: IconName,
    tooltip: &'static str,
) -> AnyElement {
    div()
        .debug_selector(move || selector.into())
        .h_full()
        .flex()
        .items_center()
        .child(
            Button::new(selector)
                .icon(icon)
                .ghost()
                .xsmall()
                .tooltip(tooltip)
                .disabled(true),
        )
        .into_any_element()
}

fn contains_role(node: &athena_application::ShellNode, role: PanelRole) -> bool {
    match node {
        athena_application::ShellNode::PanelGroup(group) => {
            group.tabs.iter().any(|tab| tab.role == role)
        }
        athena_application::ShellNode::Split(split) => split
            .children
            .iter()
            .any(|child| contains_role(&child.node, role)),
    }
}
