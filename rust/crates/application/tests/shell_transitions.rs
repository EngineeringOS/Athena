use athena_application::{
    AthenaEditor, AthenaFrontendMessage, DockPlacement, GroupId, PanelGroup, PanelRole,
    ShellEffect, ShellFocus, ShellMessage, ShellNode, SplitId, TabId, WorkspaceShell,
};
use uuid::Uuid;

fn groups(node: &ShellNode) -> Vec<&PanelGroup> {
    match node {
        ShellNode::PanelGroup(group) => vec![group],
        ShellNode::Split(split) => split
            .children
            .iter()
            .flat_map(|child| groups(&child.node))
            .collect(),
    }
}

fn group_for_role(shell: &WorkspaceShell, role: PanelRole) -> &PanelGroup {
    groups(&shell.root)
        .into_iter()
        .find(|group| group.tabs.iter().any(|tab| tab.role == role))
        .expect("default shell contains requested role")
}

fn tab_for_role(shell: &WorkspaceShell, role: PanelRole) -> TabId {
    group_for_role(shell, role)
        .tabs
        .iter()
        .find(|tab| tab.role == role)
        .expect("group contains requested role")
        .id
}

fn root_split(shell: &WorkspaceShell) -> &athena_application::SplitNode {
    let ShellNode::Split(root) = &shell.root else {
        panic!("default shell root is split");
    };
    root
}

fn request(editor: &mut AthenaEditor) -> WorkspaceShell {
    let effects = editor.handle_message(ShellMessage::Request);
    match effects.as_slice() {
        [AthenaFrontendMessage::Shell(ShellEffect::Replaced(shell))] => shell.clone(),
        other => panic!("unexpected shell request effects: {other:?}"),
    }
}

fn assert_replaced(effects: &[AthenaFrontendMessage]) -> &WorkspaceShell {
    match effects {
        [AthenaFrontendMessage::Shell(ShellEffect::Replaced(shell))] => shell,
        other => panic!("expected one replacement effect, got {other:?}"),
    }
}

fn assert_values_changed(effects: &[AthenaFrontendMessage]) {
    assert!(matches!(
        effects,
        [AthenaFrontendMessage::Shell(
            ShellEffect::ValuesChanged { .. }
        )]
    ));
}

#[test]
fn request_activation_and_reorder_emit_ordered_semantic_effects() {
    let mut editor = AthenaEditor::default();
    let shell = request(&mut editor);
    assert_eq!(shell, WorkspaceShell::default());
    let project_group = group_for_role(&shell, PanelRole::Project).id;
    let folios = tab_for_role(&shell, PanelRole::Folios);

    let activated = editor.handle_message(ShellMessage::ActivateTab {
        group_id: project_group,
        tab_id: folios,
    });
    assert_values_changed(&activated);
    assert_eq!(
        group_for_role(&request(&mut editor), PanelRole::Project).active_tab,
        folios
    );

    let reordered = editor.handle_message(ShellMessage::ReorderTab {
        group_id: project_group,
        tab_id: folios,
        to: 0,
    });
    let shell = assert_replaced(&reordered);
    let group = group_for_role(shell, PanelRole::Project);
    assert_eq!(group.tabs[0].role, PanelRole::Folios);
    assert_eq!(group.tabs[1].role, PanelRole::Project);
    assert_eq!(group.active_tab, folios);
}

#[test]
fn tabs_move_between_groups_and_edge_drops_create_a_document_priority_split() {
    let mut editor = AthenaEditor::default();
    let shell = request(&mut editor);
    let title_blocks = tab_for_role(&shell, PanelRole::TitleBlocks);
    let diagnostics_group = group_for_role(&shell, PanelRole::Diagnostics).id;

    let moved = editor.handle_message(ShellMessage::MoveTab {
        tab_id: title_blocks,
        target_group_id: diagnostics_group,
        to: 1,
    });
    let shell = assert_replaced(&moved);
    let group = group_for_role(shell, PanelRole::Diagnostics);
    assert_eq!(group.tabs[1].role, PanelRole::TitleBlocks);
    assert_eq!(
        group_for_role(shell, PanelRole::Elements).tabs.len(),
        1,
        "source group keeps its remaining tab"
    );

    let project = tab_for_role(shell, PanelRole::Project);
    let document_group = group_for_role(shell, PanelRole::FolioDocument).id;
    let split = editor.handle_message(ShellMessage::SplitGroup {
        tab_id: project,
        target_group_id: document_group,
        placement: DockPlacement::Left,
        new_group_id: GroupId::from_uuid(Uuid::from_u128(900)),
        new_split_id: SplitId::from_uuid(Uuid::from_u128(901)),
    });
    let shell = assert_replaced(&split);
    shell.validate().expect("edge split remains valid");
    let new_group = groups(&shell.root)
        .into_iter()
        .find(|group| group.id == GroupId::from_uuid(Uuid::from_u128(900)))
        .expect("new group exists");
    assert_eq!(new_group.tabs[0].role, PanelRole::Project);

    fn find_split(node: &ShellNode, id: SplitId) -> Option<&athena_application::SplitNode> {
        match node {
            ShellNode::PanelGroup(_) => None,
            ShellNode::Split(split) if split.id == id => Some(split),
            ShellNode::Split(split) => split
                .children
                .iter()
                .find_map(|child| find_split(&child.node, id)),
        }
    }
    let document_split = find_split(&shell.root, SplitId::from_uuid(Uuid::from_u128(901)))
        .expect("new document split exists");
    assert_eq!(
        document_split
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![20, 80]
    );
}

#[test]
fn resize_conserves_pair_share_clamps_to_minimum_and_abort_restores_snapshot() {
    let mut editor = AthenaEditor::default();
    let shell = request(&mut editor);
    let root_id = root_split(&shell).id;

    assert!(
        editor
            .handle_message(ShellMessage::BeginResize {
                split_id: root_id,
                before_index: 0,
                available_px: 1_000,
            })
            .is_empty()
    );
    let resized = editor.handle_message(ShellMessage::ResizeAdjacent {
        split_id: root_id,
        before_index: 0,
        delta_px: 100,
    });
    assert_values_changed(&resized);
    assert_eq!(
        root_split(&request(&mut editor))
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![30, 54, 16]
    );

    let aborted = editor.handle_message(ShellMessage::AbortResize);
    assert_values_changed(&aborted);
    assert_eq!(
        root_split(&request(&mut editor))
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![20, 64, 16]
    );

    editor.handle_message(ShellMessage::BeginResize {
        split_id: root_id,
        before_index: 0,
        available_px: 1_000,
    });
    editor.handle_message(ShellMessage::ResizeAdjacent {
        split_id: root_id,
        before_index: 0,
        delta_px: -10_000,
    });
    editor.handle_message(ShellMessage::CommitResize);
    let shares = root_split(&request(&mut editor))
        .children
        .iter()
        .map(|child| child.share)
        .collect::<Vec<_>>();
    assert_eq!(shares, vec![10, 74, 16]);
    assert_eq!(shares[0] + shares[1], 84);
}

#[test]
fn double_click_reset_uses_document_80_20_else_50_50() {
    let mut editor = AthenaEditor::default();
    let shell = request(&mut editor);
    let root_id = root_split(&shell).id;
    let reset = editor.handle_message(ShellMessage::ResetAdjacent {
        split_id: root_id,
        before_index: 0,
    });
    assert_values_changed(&reset);
    assert_eq!(
        root_split(&request(&mut editor))
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![17, 67, 16]
    );

    let shell = request(&mut editor);
    let right = root_split(&shell).children[2].node.as_ref();
    let ShellNode::Split(right) = right else {
        panic!("right side is split");
    };
    let reset = editor.handle_message(ShellMessage::ResetAdjacent {
        split_id: right.id,
        before_index: 0,
    });
    assert_values_changed(&reset);
    let shell = request(&mut editor);
    let ShellNode::Split(right) = root_split(&shell).children[2].node.as_ref() else {
        panic!("right side remains split");
    };
    assert_eq!(
        right
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![50, 50]
    );
}

#[test]
fn close_reopen_and_empty_group_pruning_preserve_valid_identity() {
    let mut editor = AthenaEditor::default();
    let initial = request(&mut editor);
    let history = tab_for_role(&initial, PanelRole::History);
    let closed = editor.handle_message(ShellMessage::ClosePanel { tab_id: history });
    let shell = assert_replaced(&closed);
    assert!(
        !groups(&shell.root)
            .iter()
            .flat_map(|group| &group.tabs)
            .any(|tab| tab.id == history)
    );

    let reopened = editor.handle_message(ShellMessage::ReopenPanel { tab_id: history });
    let shell = assert_replaced(&reopened);
    assert_eq!(shell, &initial);

    let diagnostics = tab_for_role(shell, PanelRole::Diagnostics);
    let properties_group = group_for_role(shell, PanelRole::SelectionProperties).id;
    editor.handle_message(ShellMessage::MoveTab {
        tab_id: history,
        target_group_id: properties_group,
        to: 0,
    });
    let moved = editor.handle_message(ShellMessage::MoveTab {
        tab_id: diagnostics,
        target_group_id: properties_group,
        to: 0,
    });
    let shell = assert_replaced(&moved);
    shell.validate().expect("pruned shell remains valid");
    assert_eq!(groups(&shell.root).len(), 4);

    let document = tab_for_role(shell, PanelRole::FolioDocument);
    let before = shell.clone();
    let rejected = editor.handle_message(ShellMessage::ClosePanel { tab_id: document });
    assert!(matches!(
        rejected.as_slice(),
        [AthenaFrontendMessage::Diagnostic { code, .. }] if code == "invalid-shell-message"
    ));
    assert_eq!(request(&mut editor), before);
}

#[test]
fn document_focus_overlay_and_invalid_ids_never_destroy_the_tree() {
    let mut editor = AthenaEditor::default();
    let initial = request(&mut editor);
    let project_group = group_for_role(&initial, PanelRole::Project).id;

    let focus = editor.handle_message(ShellMessage::SetDocumentFocus { focused: true });
    assert_values_changed(&focus);
    let shell = request(&mut editor);
    assert_eq!(shell.focus, ShellFocus::Document);
    assert_eq!(shell.root, initial.root);

    let overlay = editor.handle_message(ShellMessage::OpenOverlay {
        group_id: project_group,
    });
    assert_values_changed(&overlay);
    assert_eq!(
        request(&mut editor).floating_layers.overlay,
        Some(project_group)
    );
    assert_values_changed(&editor.handle_message(ShellMessage::CloseOverlay));
    assert!(request(&mut editor).floating_layers.overlay.is_none());

    let before = request(&mut editor);
    let rejected = editor.handle_message(ShellMessage::ActivateTab {
        group_id: GroupId::from_uuid(Uuid::from_u128(u128::MAX)),
        tab_id: tab_for_role(&before, PanelRole::Project),
    });
    assert!(matches!(
        rejected.as_slice(),
        [AthenaFrontendMessage::Diagnostic { code, .. }] if code == "invalid-shell-message"
    ));
    assert_eq!(request(&mut editor), before);
}
