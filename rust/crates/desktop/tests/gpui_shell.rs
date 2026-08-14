use athena_application::{DockPlacement, PanelRole, ShellFocus, ShellNode};
use athena_desktop::{
    panels::NativeShell,
    shell::{electrical::ELECTRICAL_TOOLS, tokens},
};
use gpui::{
    Modifiers, MouseButton, MouseDownEvent, MouseUpEvent, TestAppContext, VisualTestContext, point,
    px,
};

fn setup(cx: &mut TestAppContext) -> (&mut VisualTestContext, gpui::Entity<NativeShell>) {
    cx.update(gpui_component::init);
    let (shell, cx) = cx.add_window_view(NativeShell::new);
    (cx, shell)
}

fn click(cx: &mut VisualTestContext, selector: &'static str) {
    let bounds = cx
        .debug_bounds(selector)
        .unwrap_or_else(|| panic!("missing rendered control {selector}"));
    cx.simulate_click(bounds.center(), Modifiers::none());
    cx.update(|window, _| window.refresh());
    cx.run_until_parked();
}

fn drag(cx: &mut VisualTestContext, from: &'static str, to: &'static str, release: bool) {
    let source = cx
        .debug_bounds(from)
        .unwrap_or_else(|| panic!("missing drag source {from}"));
    let target = cx
        .debug_bounds(to)
        .unwrap_or_else(|| panic!("missing drop target {to}"));
    let start = source.center();
    cx.simulate_mouse_down(start, MouseButton::Left, Modifiers::none());
    cx.simulate_mouse_move(
        point(start.x + px(5.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    cx.simulate_mouse_move(target.center(), Some(MouseButton::Left), Modifiers::none());
    if release {
        cx.simulate_mouse_up(target.center(), MouseButton::Left, Modifiers::none());
    }
    cx.update(|window, _| window.refresh());
    cx.run_until_parked();
}

fn group_roles(shell: &athena_application::WorkspaceShell, role: PanelRole) -> Vec<PanelRole> {
    fn find(node: &ShellNode, role: PanelRole) -> Option<&athena_application::PanelGroup> {
        match node {
            ShellNode::PanelGroup(group) if group.tabs.iter().any(|tab| tab.role == role) => {
                Some(group)
            }
            ShellNode::PanelGroup(_) => None,
            ShellNode::Split(split) => split
                .children
                .iter()
                .find_map(|child| find(&child.node, role)),
        }
    }

    find(&shell.root, role)
        .expect("role belongs to a rendered group")
        .tabs
        .iter()
        .map(|tab| tab.role)
        .collect()
}

#[gpui::test]
fn rendered_shell_exposes_graphite_regions_recursive_groups_and_empty_folio(
    cx: &mut TestAppContext,
) {
    let (cx, _shell) = setup(cx);

    let title = cx.debug_bounds("athena-title-bar").expect("title renders");
    let workspace = cx
        .debug_bounds("athena-workspace")
        .expect("workspace renders");
    let status = cx
        .debug_bounds("athena-status-bar")
        .expect("status renders");
    assert_eq!(title.size.height, px(tokens::TITLE_BAR_HEIGHT));
    assert_eq!(status.size.height, px(tokens::STATUS_BAR_HEIGHT));
    assert!(title.bottom() <= workspace.top());
    assert!(workspace.bottom() <= status.top());

    for selector in [
        "tab-project",
        "tab-folios",
        "tab-elements",
        "tab-title-blocks",
        "tab-folio-document",
        "tab-selection-properties",
        "tab-folio-properties",
        "tab-diagnostics",
        "tab-history",
        "gutter-root-0",
        "gutter-root-1",
        "folio-page",
        "folio-grid",
        "title-block-frame",
        "dock-target-folio-document-left",
        "dock-target-folio-document-right",
        "dock-target-folio-document-top",
        "dock-target-folio-document-bottom",
        "dock-target-folio-document-center",
    ] {
        assert!(cx.debug_bounds(selector).is_some(), "{selector} renders");
    }
    assert_eq!(ELECTRICAL_TOOLS.len(), 10);
    assert!(ELECTRICAL_TOOLS.iter().all(|tool| tool.disabled));
    for selector in ELECTRICAL_TOOLS.iter().map(|tool| tool.selector) {
        assert!(cx.debug_bounds(selector).is_some(), "{selector} renders");
    }
    assert!(cx.debug_bounds("demo-wire").is_none());
    assert!(cx.debug_bounds("demo-symbol").is_none());
    assert!(cx.debug_bounds("demo-selection").is_none());
}

#[gpui::test]
fn rendered_controls_dispatch_tab_panel_and_focus_transitions(cx: &mut TestAppContext) {
    let (cx, shell) = setup(cx);

    click(cx, "tab-folios");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell
            .active_role_in_group(PanelRole::Project)),
        Some(PanelRole::Folios)
    );

    click(cx, "toggle-project");
    assert!(!shell.read_with(cx, |shell, _| shell.contains_role(PanelRole::Project)));
    click(cx, "toggle-project");
    assert!(shell.read_with(cx, |shell, _| shell.contains_role(PanelRole::Project)));

    click(cx, "focus-document");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell.shell_snapshot().focus),
        ShellFocus::Document
    );
    assert!(cx.debug_bounds("document-focus-view").is_some());
    assert!(cx.debug_bounds("tab-folio-document").is_some());
    click(cx, "focus-document");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell.shell_snapshot().focus),
        ShellFocus::Workspace
    );
    assert!(cx.debug_bounds("workspace-focus-view").is_some());
}

#[gpui::test]
fn native_tabs_follow_arrow_home_and_end_keyboard_traversal(cx: &mut TestAppContext) {
    let (cx, shell) = setup(cx);

    click(cx, "tab-project");
    cx.simulate_keystrokes("right");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell
            .active_role_in_group(PanelRole::Project)),
        Some(PanelRole::Folios)
    );

    cx.simulate_keystrokes("home");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell
            .active_role_in_group(PanelRole::Project)),
        Some(PanelRole::Project)
    );

    cx.simulate_keystrokes("end");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell
            .active_role_in_group(PanelRole::Project)),
        Some(PanelRole::Folios)
    );
}

#[gpui::test]
fn native_shell_routes_resize_abort_reset_and_close_reopen_to_rust(cx: &mut TestAppContext) {
    let (cx, shell) = setup(cx);
    let before = shell.read_with(cx, |shell, _| shell.root_shares());
    let gutter = cx
        .debug_bounds("gutter-root-0")
        .expect("root gutter renders");
    let start = gutter.center();

    cx.simulate_mouse_down(start, MouseButton::Left, Modifiers::none());
    cx.simulate_mouse_move(
        point(start.x + px(5.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    cx.simulate_mouse_move(
        point(start.x + px(85.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    cx.simulate_mouse_up(
        point(start.x + px(85.0), start.y),
        MouseButton::Left,
        Modifiers::none(),
    );
    cx.update(|window, _| window.refresh());
    cx.run_until_parked();
    let committed = shell.read_with(cx, |shell, _| shell.root_shares());
    assert_ne!(committed, before);

    let gutter = cx
        .debug_bounds("gutter-root-0")
        .expect("moved root gutter renders");
    let start = gutter.center();
    cx.simulate_mouse_down(start, MouseButton::Left, Modifiers::none());
    cx.simulate_mouse_move(
        point(start.x + px(5.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    cx.simulate_mouse_move(
        point(start.x + px(65.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    assert_ne!(
        shell.read_with(cx, |shell, _| shell.root_shares()),
        committed
    );
    cx.simulate_keystrokes("escape");
    assert_eq!(
        shell.read_with(cx, |shell, _| shell.root_shares()),
        committed
    );
    cx.simulate_mouse_up(
        point(start.x + px(65.0), start.y),
        MouseButton::Left,
        Modifiers::none(),
    );

    let gutter = cx
        .debug_bounds("gutter-root-0")
        .expect("aborted root gutter renders");
    cx.simulate_event(MouseDownEvent {
        position: gutter.center(),
        modifiers: Modifiers::none(),
        button: MouseButton::Left,
        click_count: 2,
        first_mouse: false,
    });
    cx.simulate_event(MouseUpEvent {
        position: gutter.center(),
        modifiers: Modifiers::none(),
        button: MouseButton::Left,
        click_count: 2,
    });
    assert_eq!(
        shell.read_with(cx, |shell, _| shell.root_shares()),
        vec![17, 67, 16]
    );

    click(cx, "close-history");
    assert!(!shell.read_with(cx, |shell, _| shell.contains_role(PanelRole::History)));
    click(cx, "reopen-history");
    assert!(shell.read_with(cx, |shell, _| shell.contains_role(PanelRole::History)));
}

#[gpui::test]
fn native_tab_drag_reorders_moves_and_edge_splits_through_rust(cx: &mut TestAppContext) {
    let (cx, shell) = setup(cx);

    drag(cx, "tab-folios", "tab-project", true);
    assert_eq!(
        shell.read_with(cx, |shell, _| group_roles(
            shell.shell_snapshot(),
            PanelRole::Project
        )),
        vec![PanelRole::Folios, PanelRole::Project]
    );

    drag(cx, "tab-title-blocks", "tab-diagnostics", true);
    assert_eq!(
        shell.read_with(cx, |shell, _| group_roles(
            shell.shell_snapshot(),
            PanelRole::Diagnostics
        )),
        vec![
            PanelRole::TitleBlocks,
            PanelRole::Diagnostics,
            PanelRole::History
        ]
    );

    let source = cx.debug_bounds("tab-folios").expect("folios tab renders");
    let document = cx
        .debug_bounds("body-folio-document")
        .expect("document body renders");
    let left_target_bounds = cx
        .debug_bounds("dock-target-folio-document-left")
        .expect("document left dock target renders");
    assert!(document.contains(&left_target_bounds.center()));
    let start = source.center();
    let left_target = left_target_bounds.center();
    cx.simulate_mouse_down(start, MouseButton::Left, Modifiers::none());
    cx.simulate_mouse_move(
        point(start.x + px(5.0), start.y),
        Some(MouseButton::Left),
        Modifiers::none(),
    );
    assert!(cx.update(|_, cx| cx.has_active_drag()));
    cx.simulate_mouse_move(left_target, Some(MouseButton::Left), Modifiers::none());
    cx.update(|window, _| window.refresh());
    cx.run_until_parked();
    assert_eq!(
        shell.read_with(cx, |shell, _| shell
            .shell_snapshot()
            .floating_layers
            .dock_preview
            .map(|target| target.placement)),
        Some(DockPlacement::Left)
    );
    assert!(cx.debug_bounds("dock-ghost-left").is_some());

    cx.simulate_mouse_up(left_target, MouseButton::Left, Modifiers::none());
    cx.update(|window, _| window.refresh());
    cx.run_until_parked();
    assert_eq!(
        shell.read_with(cx, |shell, _| group_roles(
            shell.shell_snapshot(),
            PanelRole::Folios
        )),
        vec![PanelRole::Folios]
    );
    assert!(shell.read_with(cx, |shell, _| {
        shell
            .shell_snapshot()
            .floating_layers
            .dock_preview
            .is_none()
    }));
}

#[test]
fn native_shell_source_contains_no_hard_coded_schematic_preview() {
    let source = [
        include_str!("../src/panels.rs"),
        include_str!("../src/shell/electrical.rs"),
        include_str!("../src/shell/subdivision.rs"),
    ]
    .join("\n");
    for forbidden in [
        "Q1",
        "K1",
        "M1",
        "schematic-preview",
        "demo-wire",
        "demo-symbol",
    ] {
        assert!(
            !source.contains(forbidden),
            "forbidden preview token: {forbidden}"
        );
    }
}
