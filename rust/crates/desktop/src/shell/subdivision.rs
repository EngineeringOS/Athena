//! Recursive GPUI split/group renderer and native gutter event mapping.

use athena_application::{PanelGroup, PanelRole, ShellFocus, ShellNode, SplitAxis, WorkspaceShell};
use gpui::{
    AnyElement, Context, DragMoveEvent, Empty, IntoElement, MouseButton, MouseDownEvent,
    MouseUpEvent, Render, SharedString, Window, div, prelude::*, px,
};
use gpui_component::PixelsExt;

use crate::{
    panels::NativeShell,
    shell::{panel_group, tokens},
};

#[derive(Clone)]
struct DraggedGutter {
    split_id: athena_application::SplitId,
    before_index: usize,
}

impl Render for DraggedGutter {
    fn render(&mut self, _: &mut Window, _: &mut Context<Self>) -> impl IntoElement {
        Empty
    }
}

pub(crate) fn render_workspace(
    shell: &WorkspaceShell,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    let (content, focus_selector) = if shell.focus == ShellFocus::Document {
        (
            find_document_group(&shell.root)
                .map(|group| panel_group::render(group, shell.floating_layers.dock_preview, cx))
                .unwrap_or_else(|| {
                    render_node(&shell.root, 0, shell.floating_layers.dock_preview, cx)
                }),
            "document-focus-view",
        )
    } else {
        (
            render_node(&shell.root, 0, shell.floating_layers.dock_preview, cx),
            "workspace-focus-view",
        )
    };
    div()
        .id("athena-workspace")
        .debug_selector(|| "athena-workspace".into())
        .flex_1()
        .w_full()
        .min_h(px(0.0))
        .overflow_hidden()
        .p_1()
        .child(
            div()
                .debug_selector(move || focus_selector.into())
                .flex_1()
                .min_w(px(0.0))
                .min_h(px(0.0))
                .child(content),
        )
        .into_any_element()
}

fn render_node(
    node: &ShellNode,
    depth: usize,
    dock_preview: Option<athena_application::DockTarget>,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    match node {
        ShellNode::PanelGroup(group) => panel_group::render(group, dock_preview, cx),
        ShellNode::Split(split) => {
            let mut children = Vec::new();
            for (index, child) in split.children.iter().enumerate() {
                if index > 0 {
                    children.push(render_gutter(split.id, index - 1, split.axis, depth, cx));
                }
                let mut region = div().flex_basis(px(0.0));
                region.style().flex_grow = Some(child.share as f32);
                children.push(
                    region
                        .min_w(px(0.0))
                        .min_h(px(0.0))
                        .child(render_node(&child.node, depth + 1, dock_preview, cx))
                        .into_any_element(),
                );
            }
            div()
                .flex_1()
                .min_w(px(0.0))
                .min_h(px(0.0))
                .flex()
                .when(split.axis == SplitAxis::Vertical, |this| this.flex_col())
                .children(children)
                .into_any_element()
        }
    }
}

fn render_gutter(
    split_id: athena_application::SplitId,
    before_index: usize,
    axis: SplitAxis,
    depth: usize,
    cx: &mut gpui::Context<NativeShell>,
) -> AnyElement {
    let selector = if depth == 0 {
        format!("gutter-root-{before_index}")
    } else {
        format!("gutter-{split_id}-{before_index}")
    };
    let debug_selector = selector.clone();
    let drag = DraggedGutter {
        split_id,
        before_index,
    };
    div()
        .id(SharedString::from(format!(
            "gutter-{split_id}-{before_index}"
        )))
        .debug_selector(move || debug_selector.clone())
        .flex_none()
        .when(axis == SplitAxis::Horizontal, |this| {
            this.w(px(tokens::GUTTER_SIZE)).h_full().cursor_col_resize()
        })
        .when(axis == SplitAxis::Vertical, |this| {
            this.h(px(tokens::GUTTER_SIZE)).w_full().cursor_row_resize()
        })
        .hover(|this| this.bg(gpui::rgb(tokens::DULL_GRAY)))
        .on_mouse_down(
            MouseButton::Left,
            cx.listener(move |this, event: &MouseDownEvent, window, cx| {
                if event.click_count >= 2 {
                    this.handle_shell_message(
                        athena_application::ShellMessage::ResetAdjacent {
                            split_id,
                            before_index,
                        },
                        cx,
                    );
                    return;
                }
                let bounds = window.bounds();
                let available = match axis {
                    SplitAxis::Horizontal => bounds.size.width,
                    SplitAxis::Vertical => bounds.size.height,
                };
                this.begin_resize(
                    split_id,
                    before_index,
                    axis,
                    event.position,
                    available.as_f32().max(1.0).round() as u32,
                    cx,
                );
            }),
        )
        .on_mouse_up(
            MouseButton::Left,
            cx.listener(|this, _: &MouseUpEvent, _, cx| this.commit_resize(cx)),
        )
        .on_mouse_down(
            MouseButton::Right,
            cx.listener(|this, _: &MouseDownEvent, _, cx| this.abort_resize(cx)),
        )
        .on_drag(drag, |drag, _, _, cx| cx.new(|_| drag.clone()))
        .on_drag_move(
            cx.listener(move |this, event: &DragMoveEvent<DraggedGutter>, _, cx| {
                let dragged = event.drag(cx);
                if dragged.split_id == split_id && dragged.before_index == before_index {
                    this.update_resize(event.event.position, cx);
                }
            }),
        )
        .into_any_element()
}

fn find_document_group(node: &ShellNode) -> Option<&PanelGroup> {
    match node {
        ShellNode::PanelGroup(group)
            if group
                .tabs
                .iter()
                .any(|tab| tab.role == PanelRole::FolioDocument) =>
        {
            Some(group)
        }
        ShellNode::PanelGroup(_) => None,
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| find_document_group(&child.node)),
    }
}
