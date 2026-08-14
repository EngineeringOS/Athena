//! GPUI lifecycle and message adapter for Athena's native shell.
//!
//! Rendering is decomposed under [`crate::shell`]. This view owns only the
//! desktop effect cache and transient pointer-drag coordinates; the recursive
//! topology remains owned by `athena-application`.

use athena_application::{
    GroupId, PanelRole, ShellMessage, ShellNode, SplitAxis, SplitId, TabId, WorkspaceShell,
};
use gpui::{
    App, Application, Bounds, Context, Entity, FocusHandle, Focusable, IntoElement, KeyDownEvent,
    MouseButton, MouseUpEvent, Point, Render, Window, WindowBounds, WindowOptions, div, prelude::*,
    px, size,
};
use gpui_component::{PixelsExt, Root};

use crate::app::DesktopEditor;

/// Active GPUI gutter transaction coordinates kept outside persistent shell state.
#[derive(Clone, Copy, Debug)]
pub(crate) struct ResizeDragState {
    pub(crate) split_id: SplitId,
    pub(crate) before_index: usize,
    pub(crate) axis: SplitAxis,
    pub(crate) last_position: Point<gpui::Pixels>,
}

/// Identity of the GPUI tab drag currently crossing native drop targets.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct TabDragState {
    pub(crate) tab_id: TabId,
    pub(crate) source_group_id: GroupId,
}

/// Native GPUI projection of the effect-derived desktop adapter state.
pub struct NativeShell {
    editor: DesktopEditor,
    resize_drag: Option<ResizeDragState>,
    tab_drag: Option<TabDragState>,
    focus_handle: FocusHandle,
}

impl NativeShell {
    /// Creates an empty electrical workbench over the default Rust shell.
    pub fn new(_window: &mut Window, cx: &mut Context<Self>) -> Self {
        Self {
            editor: DesktopEditor::default(),
            resize_drag: None,
            tab_drag: None,
            focus_handle: cx.focus_handle(),
        }
    }

    /// Routes one shell interaction through the shared application and repaints.
    pub fn handle_shell_message(&mut self, message: ShellMessage, cx: &mut Context<Self>) {
        self.editor.dispatch(message);
        cx.notify();
    }

    /// Returns the effect-derived recursive shell rendered by GPUI.
    #[must_use]
    pub fn shell_snapshot(&self) -> &WorkspaceShell {
        self.editor.shell_snapshot()
    }

    /// Reports whether the currently open recursive tree contains a panel role.
    #[must_use]
    pub fn contains_role(&self, role: PanelRole) -> bool {
        tab_for_role(&self.shell_snapshot().root, role).is_some()
    }

    /// Returns the active role in the group containing `role`.
    #[must_use]
    pub fn active_role_in_group(&self, role: PanelRole) -> Option<PanelRole> {
        let group = group_for_role(&self.shell_snapshot().root, role)?;
        group
            .tabs
            .iter()
            .find(|tab| tab.id == group.active_tab)
            .map(|tab| tab.role)
    }

    /// Returns the root split identity for resize adapters and diagnostics.
    #[must_use]
    pub fn root_split_id(&self) -> SplitId {
        match &self.shell_snapshot().root {
            ShellNode::Split(split) => split.id,
            ShellNode::PanelGroup(_) => unreachable!("the M006 root is a split"),
        }
    }

    /// Returns current root shares in presentation order.
    #[must_use]
    pub fn root_shares(&self) -> Vec<u32> {
        match &self.shell_snapshot().root {
            ShellNode::Split(split) => split.children.iter().map(|child| child.share).collect(),
            ShellNode::PanelGroup(_) => Vec::new(),
        }
    }

    pub(crate) fn toggle_role(&mut self, role: PanelRole, cx: &mut Context<Self>) {
        let open = tab_for_role(&self.shell_snapshot().root, role);
        let stable = open.or_else(|| tab_for_role(&WorkspaceShell::default().root, role));
        let Some(tab_id) = stable else { return };
        let message = if open.is_some() {
            ShellMessage::ClosePanel { tab_id }
        } else {
            ShellMessage::ReopenPanel { tab_id }
        };
        self.handle_shell_message(message, cx);
    }

    pub(crate) fn begin_resize(
        &mut self,
        split_id: SplitId,
        before_index: usize,
        axis: SplitAxis,
        position: Point<gpui::Pixels>,
        available_px: u32,
        cx: &mut Context<Self>,
    ) {
        self.handle_shell_message(
            ShellMessage::BeginResize {
                split_id,
                before_index,
                available_px,
            },
            cx,
        );
        self.resize_drag = Some(ResizeDragState {
            split_id,
            before_index,
            axis,
            last_position: position,
        });
    }

    pub(crate) fn update_resize(&mut self, position: Point<gpui::Pixels>, cx: &mut Context<Self>) {
        let Some(drag) = self.resize_drag else { return };
        let delta = match drag.axis {
            SplitAxis::Horizontal => position.x - drag.last_position.x,
            SplitAxis::Vertical => position.y - drag.last_position.y,
        };
        let delta_px = delta.as_f32().round() as i32;
        if delta_px != 0 {
            self.handle_shell_message(
                ShellMessage::ResizeAdjacent {
                    split_id: drag.split_id,
                    before_index: drag.before_index,
                    delta_px,
                },
                cx,
            );
            if let Some(active) = &mut self.resize_drag {
                active.last_position = position;
            }
        }
    }

    pub(crate) fn commit_resize(&mut self, cx: &mut Context<Self>) {
        if self.resize_drag.take().is_some() {
            self.handle_shell_message(ShellMessage::CommitResize, cx);
        }
    }

    pub(crate) fn abort_resize(&mut self, cx: &mut Context<Self>) {
        if self.resize_drag.take().is_some() {
            self.handle_shell_message(ShellMessage::AbortResize, cx);
        }
    }

    pub(crate) fn begin_tab_drag(&mut self, drag: TabDragState) {
        self.tab_drag = Some(drag);
    }

    pub(crate) const fn tab_drag(&self) -> Option<TabDragState> {
        self.tab_drag
    }

    pub(crate) fn end_tab_drag(&mut self, cx: &mut Context<Self>) {
        self.tab_drag = None;
        if self.shell_snapshot().floating_layers.dock_preview.is_some() {
            self.handle_shell_message(ShellMessage::SetDockPreview { target: None }, cx);
        }
    }

    fn key_down(&mut self, event: &KeyDownEvent, window: &mut Window, cx: &mut Context<Self>) {
        if event.keystroke.key == "escape" {
            self.abort_resize(cx);
            if self.shell_snapshot().floating_layers.dock_preview.is_some() {
                self.handle_shell_message(ShellMessage::SetDockPreview { target: None }, cx);
            }
            cx.stop_active_drag(window);
            self.tab_drag = None;
        }
    }

    fn mouse_up(&mut self, _: &MouseUpEvent, cx: &mut Context<Self>) {
        self.end_tab_drag(cx);
    }
}

impl Focusable for NativeShell {
    fn focus_handle(&self, _cx: &App) -> FocusHandle {
        self.focus_handle.clone()
    }
}

impl Render for NativeShell {
    fn render(&mut self, _window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        let shell = self.shell_snapshot().clone();
        div()
            .id("athena-native-shell")
            .debug_selector(|| "athena-native-shell".into())
            .track_focus(&self.focus_handle)
            .on_key_down(cx.listener(|this, event, window, cx| this.key_down(event, window, cx)))
            .on_mouse_up(
                MouseButton::Left,
                cx.listener(|this, event, _, cx| this.mouse_up(event, cx)),
            )
            .size_full()
            .child(crate::shell::render(&shell, cx))
    }
}

fn group_for_role(node: &ShellNode, role: PanelRole) -> Option<&athena_application::PanelGroup> {
    match node {
        ShellNode::PanelGroup(group) if group.tabs.iter().any(|tab| tab.role == role) => {
            Some(group)
        }
        ShellNode::PanelGroup(_) => None,
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| group_for_role(&child.node, role)),
    }
}

fn tab_for_role(node: &ShellNode, role: PanelRole) -> Option<TabId> {
    group_for_role(node, role).and_then(|group| {
        group
            .tabs
            .iter()
            .find(|tab| tab.role == role)
            .map(|tab| tab.id)
    })
}

/// Starts the desktop application and installs gpui-component services.
pub fn run_native_shell() {
    Application::new().run(|cx: &mut App| {
        gpui_component::init(cx);
        let bounds = Bounds::centered(None, size(px(1440.0), px(900.0)), cx);
        let _ = cx.open_window(
            WindowOptions {
                window_bounds: Some(WindowBounds::Windowed(bounds)),
                ..WindowOptions::default()
            },
            |window, cx| {
                let view: Entity<NativeShell> = cx.new(|cx| NativeShell::new(window, cx));
                cx.new(|cx| Root::new(view, window, cx))
            },
        );
        cx.activate(true);
    });
}
