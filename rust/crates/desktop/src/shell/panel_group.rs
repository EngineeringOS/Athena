//! Native Graphite-measured panel tabs, docking targets, and electrical body.

use std::rc::Rc;

use athena_application::{
    DockPlacement, DockTarget, GroupId, PanelGroup, PanelRole, ShellMessage, SplitId,
};
use gpui::{
    AnyElement, Context, Empty, IntoElement, KeyDownEvent, MouseButton, MouseDownEvent,
    MouseMoveEvent, Render, SharedString, Window, div, prelude::*, px, relative, rgb, rgba,
};
use gpui_component::{
    IconName, Sizable,
    button::{Button, ButtonVariants as _},
};

use crate::{
    panels::{NativeShell, TabDragState},
    shell::{electrical, tokens},
};

impl Render for TabDragState {
    fn render(&mut self, _: &mut Window, _: &mut Context<Self>) -> impl IntoElement {
        Empty
    }
}

pub(crate) fn render(
    group: &PanelGroup,
    dock_preview: Option<DockTarget>,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    let group_id = group.id;
    let active = group
        .tabs
        .iter()
        .find(|tab| tab.id == group.active_tab)
        .expect("validated groups contain their active tab");
    let dock_preview = dock_preview.filter(|target| target.group_id == group.id);
    let tab_ids: Rc<[athena_application::TabId]> = group.tabs.iter().map(|tab| tab.id).collect();
    let active_index = group
        .tabs
        .iter()
        .position(|tab| tab.id == group.active_tab)
        .expect("validated group contains its active tab");

    div()
        .flex_1()
        .min_w(px(0.0))
        .min_h(px(0.0))
        .flex()
        .flex_col()
        .overflow_hidden()
        .rounded(px(tokens::PANEL_RADIUS))
        .bg(rgb(tokens::NEAR_BLACK))
        .child(
            div()
                .id(SharedString::from(format!("tab-group-{group_id}")))
                .tab_index(0)
                .tab_group()
                .focus(|style| style.border_b_1().border_color(rgb(tokens::ACCENT)))
                .on_key_down(cx.listener({
                    let keyboard_tab_ids = tab_ids.clone();
                    move |this, event: &KeyDownEvent, _, cx| {
                        if event.keystroke.modifiers.modified() {
                            return;
                        }
                        let target = match event.keystroke.key.as_str() {
                            "right" => (active_index + 1) % keyboard_tab_ids.len(),
                            "left" => {
                                (active_index + keyboard_tab_ids.len() - 1) % keyboard_tab_ids.len()
                            }
                            "home" => 0,
                            "end" => keyboard_tab_ids.len() - 1,
                            _ => return,
                        };
                        if target != active_index {
                            this.handle_shell_message(
                                ShellMessage::ActivateTab {
                                    group_id,
                                    tab_id: keyboard_tab_ids[target],
                                },
                                cx,
                            );
                        }
                        cx.stop_propagation();
                    }
                }))
                .h(px(tokens::TAB_BAR_HEIGHT))
                .flex_none()
                .flex()
                .overflow_hidden()
                .children(group.tabs.iter().map(|tab| {
                    let role = tab.role;
                    let tab_id = tab.id;
                    let target_index = group
                        .tabs
                        .iter()
                        .position(|candidate| candidate.id == tab_id)
                        .expect("rendered tab belongs to its group");
                    let active = tab.id == group.active_tab;
                    let selector = tab_selector(role);
                    div()
                        .id(SharedString::from(format!("tab-{}", tab.id)))
                        .debug_selector(move || selector.into())
                        .h_full()
                        .px_2()
                        .flex()
                        .items_center()
                        .gap_1()
                        .rounded_t(px(tokens::PANEL_RADIUS))
                        .text_color(rgb(if active {
                            tokens::NEAR_WHITE
                        } else {
                            tokens::SOFT_GRAY
                        }))
                        .when(active, |this| this.bg(rgb(tokens::DARK_GRAY)))
                        .on_click(cx.listener(move |this, _, _, cx| {
                            this.handle_shell_message(
                                ShellMessage::ActivateTab { group_id, tab_id },
                                cx,
                            )
                        }))
                        .on_mouse_down(
                            MouseButton::Left,
                            cx.listener(move |this, _: &MouseDownEvent, _, _| {
                                this.begin_tab_drag(TabDragState {
                                    tab_id,
                                    source_group_id: group_id,
                                });
                            }),
                        )
                        .on_drag(
                            TabDragState {
                                tab_id,
                                source_group_id: group_id,
                            },
                            |drag, _, _, cx| cx.new(|_| *drag),
                        )
                        .drag_over::<TabDragState>(|style, _, _, _| {
                            style.border_l_2().border_color(rgb(tokens::ACCENT))
                        })
                        .on_drop(cx.listener(move |this, dragged: &TabDragState, _, cx| {
                            let message = if dragged.source_group_id == group_id {
                                ShellMessage::ReorderTab {
                                    group_id,
                                    tab_id: dragged.tab_id,
                                    to: target_index,
                                }
                            } else {
                                ShellMessage::MoveTab {
                                    tab_id: dragged.tab_id,
                                    target_group_id: group_id,
                                    to: target_index,
                                }
                            };
                            this.handle_shell_message(message, cx);
                            this.end_tab_drag(cx);
                        }))
                        .child(tab.label.clone())
                        .when(tab.closeable, |this| {
                            this.child(
                                Button::new(SharedString::from(format!("close-tab-{}", tab.id)))
                                    .icon(IconName::Close)
                                    .ghost()
                                    .xsmall()
                                    .tab_index(-1)
                                    .tooltip("Close panel")
                                    .on_click(cx.listener(move |this, _, _, cx| {
                                        this.handle_shell_message(
                                            ShellMessage::ClosePanel { tab_id },
                                            cx,
                                        )
                                    })),
                            )
                        })
                })),
        )
        .child(render_body(group.id, active.role, dock_preview, cx))
        .into_any_element()
}

fn render_body(
    group_id: GroupId,
    active_role: PanelRole,
    dock_preview: Option<DockTarget>,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    let selector = body_selector(active_role);
    div()
        .id(SharedString::from(format!("panel-body-{group_id}")))
        .debug_selector(move || selector.into())
        .relative()
        .flex_1()
        .min_w(px(0.0))
        .min_h(px(0.0))
        .overflow_hidden()
        .child(electrical::render(active_role))
        .children(
            [
                DockPlacement::Left,
                DockPlacement::Right,
                DockPlacement::Top,
                DockPlacement::Bottom,
                DockPlacement::Center,
            ]
            .map(|placement| render_dock_target(group_id, active_role, placement, cx)),
        )
        .when_some(dock_preview, |this, target| {
            this.child(render_dock_ghost(target.placement))
        })
        .into_any_element()
}

fn group_tab_count(node: &athena_application::ShellNode, group_id: GroupId) -> Option<usize> {
    match node {
        athena_application::ShellNode::PanelGroup(group) if group.id == group_id => {
            Some(group.tabs.len())
        }
        athena_application::ShellNode::PanelGroup(_) => None,
        athena_application::ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| group_tab_count(&child.node, group_id)),
    }
}

fn render_dock_ghost(placement: DockPlacement) -> AnyElement {
    div()
        .debug_selector(move || dock_selector(placement).into())
        .absolute()
        .border_1()
        .border_color(rgb(tokens::ACCENT))
        .bg(rgba(0x5fb8a844))
        .when(placement == DockPlacement::Left, |this| {
            this.left_0().top_0().bottom_0().w(relative(0.25))
        })
        .when(placement == DockPlacement::Right, |this| {
            this.right_0().top_0().bottom_0().w(relative(0.25))
        })
        .when(placement == DockPlacement::Top, |this| {
            this.left_0().right_0().top_0().h(relative(0.25))
        })
        .when(placement == DockPlacement::Bottom, |this| {
            this.left_0().right_0().bottom_0().h(relative(0.25))
        })
        .when(placement == DockPlacement::Center, |this| {
            this.left(relative(0.25))
                .right(relative(0.25))
                .top(relative(0.25))
                .bottom(relative(0.25))
        })
        .into_any_element()
}

fn render_dock_target(
    group_id: GroupId,
    active_role: PanelRole,
    placement: DockPlacement,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    dock_region(div(), placement)
        .id(SharedString::from(format!(
            "dock-target-{group_id}-{placement:?}"
        )))
        .debug_selector(move || dock_target_selector(active_role, placement).into())
        .drag_over::<TabDragState>(|style, _, _, _| {
            style
                .border_1()
                .border_color(rgb(tokens::ACCENT))
                .bg(rgba(0x5fb8a844))
        })
        .on_mouse_move(cx.listener(move |this, event: &MouseMoveEvent, _, cx| {
            if event.pressed_button != Some(MouseButton::Left)
                || this.tab_drag().is_none()
                || !cx.has_active_drag()
            {
                return;
            }
            let target = DockTarget {
                group_id,
                placement,
            };
            if this.shell_snapshot().floating_layers.dock_preview != Some(target) {
                this.handle_shell_message(
                    ShellMessage::SetDockPreview {
                        target: Some(target),
                    },
                    cx,
                );
            }
        }))
        .on_drop(cx.listener(move |this, dragged: &TabDragState, _, cx| {
            match placement {
                DockPlacement::Center if dragged.source_group_id != group_id => {
                    let to = group_tab_count(&this.shell_snapshot().root, group_id).unwrap_or(0);
                    this.handle_shell_message(
                        ShellMessage::MoveTab {
                            tab_id: dragged.tab_id,
                            target_group_id: group_id,
                            to,
                        },
                        cx,
                    );
                }
                DockPlacement::Center => {}
                placement => this.handle_shell_message(
                    ShellMessage::SplitGroup {
                        tab_id: dragged.tab_id,
                        target_group_id: group_id,
                        placement,
                        new_group_id: GroupId::new(),
                        new_split_id: SplitId::new(),
                    },
                    cx,
                ),
            }
            this.end_tab_drag(cx);
        }))
        .into_any_element()
}

fn dock_region(element: gpui::Div, placement: DockPlacement) -> gpui::Div {
    element
        .absolute()
        .when(placement == DockPlacement::Left, |this| {
            this.left_0().top_0().bottom_0().w(relative(0.25))
        })
        .when(placement == DockPlacement::Right, |this| {
            this.right_0().top_0().bottom_0().w(relative(0.25))
        })
        .when(placement == DockPlacement::Top, |this| {
            this.left(relative(0.25))
                .right(relative(0.25))
                .top_0()
                .h(relative(0.25))
        })
        .when(placement == DockPlacement::Bottom, |this| {
            this.left(relative(0.25))
                .right(relative(0.25))
                .bottom_0()
                .h(relative(0.25))
        })
        .when(placement == DockPlacement::Center, |this| {
            this.left(relative(0.25))
                .right(relative(0.25))
                .top(relative(0.25))
                .bottom(relative(0.25))
        })
}

const fn body_selector(role: PanelRole) -> &'static str {
    match role {
        PanelRole::Project => "body-project",
        PanelRole::Folios => "body-folios",
        PanelRole::Elements => "body-elements",
        PanelRole::TitleBlocks => "body-title-blocks",
        PanelRole::FolioDocument => "body-folio-document",
        PanelRole::SelectionProperties => "body-selection-properties",
        PanelRole::FolioProperties => "body-folio-properties",
        PanelRole::Diagnostics => "body-diagnostics",
        PanelRole::History => "body-history",
    }
}

const fn dock_selector(placement: DockPlacement) -> &'static str {
    match placement {
        DockPlacement::Left => "dock-ghost-left",
        DockPlacement::Right => "dock-ghost-right",
        DockPlacement::Top => "dock-ghost-top",
        DockPlacement::Bottom => "dock-ghost-bottom",
        DockPlacement::Center => "dock-ghost-center",
    }
}

const fn dock_target_selector(role: PanelRole, placement: DockPlacement) -> &'static str {
    match (role, placement) {
        (PanelRole::FolioDocument, DockPlacement::Left) => "dock-target-folio-document-left",
        (PanelRole::FolioDocument, DockPlacement::Right) => "dock-target-folio-document-right",
        (PanelRole::FolioDocument, DockPlacement::Top) => "dock-target-folio-document-top",
        (PanelRole::FolioDocument, DockPlacement::Bottom) => "dock-target-folio-document-bottom",
        (PanelRole::FolioDocument, DockPlacement::Center) => "dock-target-folio-document-center",
        (_, DockPlacement::Left) => "dock-target-left",
        (_, DockPlacement::Right) => "dock-target-right",
        (_, DockPlacement::Top) => "dock-target-top",
        (_, DockPlacement::Bottom) => "dock-target-bottom",
        (_, DockPlacement::Center) => "dock-target-center",
    }
}

pub(crate) const fn tab_selector(role: PanelRole) -> &'static str {
    match role {
        PanelRole::Project => "tab-project",
        PanelRole::Folios => "tab-folios",
        PanelRole::Elements => "tab-elements",
        PanelRole::TitleBlocks => "tab-title-blocks",
        PanelRole::FolioDocument => "tab-folio-document",
        PanelRole::SelectionProperties => "tab-selection-properties",
        PanelRole::FolioProperties => "tab-folio-properties",
        PanelRole::Diagnostics => "tab-diagnostics",
        PanelRole::History => "tab-history",
    }
}
