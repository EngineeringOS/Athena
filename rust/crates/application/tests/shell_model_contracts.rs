use std::collections::BTreeSet;

use athena_application::{
    GroupId, PanelGroup, PanelRole, PanelTab, ShellFocus, ShellNode, SplitAxis, SplitChild,
    SplitId, SplitNode, TabId, WorkspaceShell,
};
use proptest::prelude::*;
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

fn splits(node: &ShellNode) -> Vec<&SplitNode> {
    match node {
        ShellNode::PanelGroup(_) => Vec::new(),
        ShellNode::Split(split) => std::iter::once(split)
            .chain(split.children.iter().flat_map(|child| splits(&child.node)))
            .collect(),
    }
}

fn group_with_share(id: u128, share: u32) -> SplitChild {
    let tab_id = TabId::from_uuid(Uuid::from_u128(id + 100));
    SplitChild {
        share,
        node: Box::new(ShellNode::PanelGroup(PanelGroup {
            id: GroupId::from_uuid(Uuid::from_u128(id)),
            tabs: vec![PanelTab {
                id: tab_id,
                role: PanelRole::Diagnostics,
                label: format!("Panel {id}"),
                closeable: true,
            }],
            active_tab: tab_id,
        })),
    }
}

#[test]
fn default_shell_is_the_electrical_20_64_16_recursive_tree() {
    let shell = WorkspaceShell::default();
    assert_eq!(shell.focus, ShellFocus::Workspace);
    assert_eq!(shell.title_bar.document_label, "Folio 1");
    assert!(!shell.title_bar.dirty);
    assert_eq!(shell.status_bar.hint, "Ready");
    assert!(shell.floating_layers.overlay.is_none());
    assert!(shell.floating_layers.dock_preview.is_none());

    let ShellNode::Split(root) = &shell.root else {
        panic!("default root must be a horizontal split");
    };
    assert_eq!(root.axis, SplitAxis::Horizontal);
    assert_eq!(
        root.children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![20, 64, 16]
    );

    let ShellNode::Split(left) = root.children[0].node.as_ref() else {
        panic!("left subtree must split vertically");
    };
    let ShellNode::PanelGroup(center) = root.children[1].node.as_ref() else {
        panic!("center subtree must be the document group");
    };
    let ShellNode::Split(right) = root.children[2].node.as_ref() else {
        panic!("right subtree must split vertically");
    };
    assert_eq!(left.axis, SplitAxis::Vertical);
    assert_eq!(right.axis, SplitAxis::Vertical);
    assert_eq!(
        left.children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![50, 50]
    );
    assert_eq!(
        right
            .children
            .iter()
            .map(|child| child.share)
            .collect::<Vec<_>>(),
        vec![50, 50]
    );
    assert_eq!(center.tabs.len(), 1);
    assert_eq!(center.tabs[0].role, PanelRole::FolioDocument);
    assert_eq!(center.tabs[0].label, "Folio 1");
    assert!(!center.tabs[0].closeable);
}

#[test]
fn default_shell_has_the_qet_panel_taxonomy_and_valid_active_tabs() {
    let shell = WorkspaceShell::default();
    let groups = groups(&shell.root);
    assert_eq!(groups.len(), 5);
    let roles = groups
        .iter()
        .flat_map(|group| group.tabs.iter().map(|tab| tab.role))
        .collect::<Vec<_>>();
    assert_eq!(
        roles,
        vec![
            PanelRole::Project,
            PanelRole::Folios,
            PanelRole::Elements,
            PanelRole::TitleBlocks,
            PanelRole::FolioDocument,
            PanelRole::SelectionProperties,
            PanelRole::FolioProperties,
            PanelRole::Diagnostics,
            PanelRole::History,
        ]
    );
    for group in groups {
        assert!(group.tabs.iter().any(|tab| tab.id == group.active_tab));
    }
    assert_eq!(
        roles
            .iter()
            .filter(|role| **role == PanelRole::FolioDocument)
            .count(),
        1
    );
}

#[test]
fn default_ids_are_stable_unique_and_round_trip_without_tab_drift() {
    let first = WorkspaceShell::default();
    let second = WorkspaceShell::default();
    assert_eq!(first, second);
    first.validate().expect("built-in shell is valid");

    let split_ids = splits(&first.root)
        .into_iter()
        .map(|split| split.id.as_uuid())
        .collect::<BTreeSet<_>>();
    let group_ids = groups(&first.root)
        .into_iter()
        .map(|group| group.id.as_uuid())
        .collect::<BTreeSet<_>>();
    let tab_ids = groups(&first.root)
        .into_iter()
        .flat_map(|group| group.tabs.iter().map(|tab| tab.id.as_uuid()))
        .collect::<BTreeSet<_>>();
    assert_eq!(split_ids.len(), 3);
    assert_eq!(group_ids.len(), 5);
    assert_eq!(tab_ids.len(), 9);

    let json = serde_json::to_string(&first).expect("shell serializes");
    let decoded: WorkspaceShell = serde_json::from_str(&json).expect("shell deserializes");
    assert_eq!(decoded, first);
    assert_eq!(
        groups(&decoded.root)
            .iter()
            .map(|group| group.active_tab)
            .collect::<Vec<_>>(),
        groups(&first.root)
            .iter()
            .map(|group| group.active_tab)
            .collect::<Vec<_>>()
    );
}

proptest! {
    #[test]
    fn accepted_split_shares_remain_positive_and_conserve_their_total(
        left in 1_u32..10_000,
        right in 1_u32..10_000,
    ) {
        let split = SplitNode::try_new(
            SplitId::from_uuid(Uuid::from_u128(500)),
            SplitAxis::Horizontal,
            vec![group_with_share(501, left), group_with_share(502, right)],
        ).expect("positive two-child split is valid");

        prop_assert!(split.children.iter().all(|child| child.share > 0));
        prop_assert_eq!(
            split.children.iter().map(|child| u64::from(child.share)).sum::<u64>(),
            u64::from(left) + u64::from(right),
        );
    }
}
