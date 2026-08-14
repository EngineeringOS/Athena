//! Deterministic state transitions for Athena's platform-neutral editor shell.
//!
//! Platform adapters translate pointer, keyboard, and touch input into
//! [`ShellMessage`](crate::ShellMessage) values. This handler is the only owner
//! allowed to mutate the recursive shell tree.

use std::collections::BTreeMap;

use crate::{
    AthenaFrontendMessage, DockPlacement, GroupId, MIN_PANEL_PX, PanelGroup, PanelRole, PanelTab,
    ShellEffect, ShellFocus, ShellMessage, ShellNode, SplitAxis, SplitChild, SplitId, SplitNode,
    TabId, WorkspaceShell,
};

#[derive(Clone, Debug)]
struct ResizeTransaction {
    split_id: SplitId,
    before_index: usize,
    available_px: u32,
    shares: Vec<u32>,
}

#[derive(Clone, Debug)]
struct ClosedPanel {
    tab: PanelTab,
    group_id: GroupId,
    index: usize,
    active_before: TabId,
    before_root: ShellNode,
    after_root: ShellNode,
}

#[derive(Clone, Debug)]
struct TakenTab {
    tab: PanelTab,
    group_id: GroupId,
    index: usize,
    active_before: TabId,
}

/// Owns the recursive shell tree, resize transaction, and restorable panels.
#[derive(Default)]
pub(crate) struct ShellHandler {
    shell: WorkspaceShell,
    resize: Option<ResizeTransaction>,
    closed: BTreeMap<TabId, ClosedPanel>,
}

impl ShellHandler {
    pub(crate) fn handle(&mut self, message: ShellMessage) -> Vec<AthenaFrontendMessage> {
        match message {
            ShellMessage::Request => vec![self.replaced()],
            ShellMessage::BeginResize {
                split_id,
                before_index,
                available_px,
            } => self.begin_resize(split_id, before_index, available_px),
            ShellMessage::CommitResize => {
                self.resize = None;
                Vec::new()
            }
            ShellMessage::AbortResize => self.abort_resize(),
            ShellMessage::SetDockPreview { target } => {
                self.shell.floating_layers.dock_preview = target;
                vec![AthenaFrontendMessage::Shell(ShellEffect::DockPreview(
                    target,
                ))]
            }
            message => self.apply_mutation(message),
        }
    }

    pub(crate) fn snapshot(&self) -> WorkspaceShell {
        self.shell.clone()
    }

    fn begin_resize(
        &mut self,
        split_id: SplitId,
        before_index: usize,
        available_px: u32,
    ) -> Vec<AthenaFrontendMessage> {
        let Some(split) = find_split(&self.shell.root, split_id) else {
            return vec![diagnostic("resize split does not exist")];
        };
        if before_index + 1 >= split.children.len() {
            return vec![diagnostic(
                "resize gutter does not have two adjacent children",
            )];
        }
        if available_px < MIN_PANEL_PX.saturating_mul(2) {
            return vec![diagnostic(
                "resize viewport is smaller than two minimum panels",
            )];
        }
        self.resize = Some(ResizeTransaction {
            split_id,
            before_index,
            available_px,
            shares: split.children.iter().map(|child| child.share).collect(),
        });
        Vec::new()
    }

    fn abort_resize(&mut self) -> Vec<AthenaFrontendMessage> {
        let Some(transaction) = self.resize.take() else {
            return vec![diagnostic("no shell resize is active")];
        };
        let Some(split) = find_split_mut(&mut self.shell.root, transaction.split_id) else {
            return vec![diagnostic("active resize split no longer exists")];
        };
        for (child, share) in split.children.iter_mut().zip(transaction.shares) {
            child.share = share;
        }
        vec![self.values_changed()]
    }

    fn apply_mutation(&mut self, message: ShellMessage) -> Vec<AthenaFrontendMessage> {
        let before = self.shell.clone();
        let result = match message {
            ShellMessage::ActivateTab { group_id, tab_id } => {
                self.activate_tab(group_id, tab_id).map(|()| false)
            }
            ShellMessage::ReorderTab {
                group_id,
                tab_id,
                to,
            } => self.reorder_tab(group_id, tab_id, to).map(|()| true),
            ShellMessage::MoveTab {
                tab_id,
                target_group_id,
                to,
            } => self.move_tab(tab_id, target_group_id, to).map(|()| true),
            ShellMessage::SplitGroup {
                tab_id,
                target_group_id,
                placement,
                new_group_id,
                new_split_id,
            } => self
                .split_group(
                    tab_id,
                    target_group_id,
                    placement,
                    new_group_id,
                    new_split_id,
                )
                .map(|()| true),
            ShellMessage::ResizeAdjacent {
                split_id,
                before_index,
                delta_px,
            } => self
                .resize_adjacent(split_id, before_index, delta_px)
                .map(|()| false),
            ShellMessage::ResetAdjacent {
                split_id,
                before_index,
            } => self.reset_adjacent(split_id, before_index).map(|()| false),
            ShellMessage::ClosePanel { tab_id } => self.close_panel(tab_id).map(|()| true),
            ShellMessage::ReopenPanel { tab_id } => self.reopen_panel(tab_id).map(|()| true),
            ShellMessage::SetDocumentFocus { focused } => {
                self.shell.focus = if focused {
                    ShellFocus::Document
                } else {
                    ShellFocus::Workspace
                };
                Ok(false)
            }
            ShellMessage::OpenOverlay { group_id } => {
                match find_group(&self.shell.root, group_id) {
                    None => Err("overlay group does not exist".into()),
                    Some(group)
                        if group
                            .tabs
                            .iter()
                            .any(|tab| tab.role == PanelRole::FolioDocument) =>
                    {
                        Err("document group cannot open as a narrow overlay".into())
                    }
                    Some(_) => {
                        self.shell.floating_layers.overlay = Some(group_id);
                        Ok(false)
                    }
                }
            }
            ShellMessage::CloseOverlay => {
                self.shell.floating_layers.overlay = None;
                Ok(false)
            }
            ShellMessage::Request
            | ShellMessage::BeginResize { .. }
            | ShellMessage::CommitResize
            | ShellMessage::AbortResize
            | ShellMessage::SetDockPreview { .. } => unreachable!("handled before mutation"),
        };

        match result.and_then(|structural| {
            self.shell.validate().map_err(|error| error.to_string())?;
            Ok(structural)
        }) {
            Ok(true) => vec![self.replaced()],
            Ok(false) => vec![self.values_changed()],
            Err(message) => {
                self.shell = before;
                vec![diagnostic(&message)]
            }
        }
    }

    fn activate_tab(&mut self, group_id: GroupId, tab_id: TabId) -> Result<(), String> {
        let group = find_group_mut(&mut self.shell.root, group_id)
            .ok_or_else(|| "panel group does not exist".to_string())?;
        if !group.tabs.iter().any(|tab| tab.id == tab_id) {
            return Err("tab does not belong to panel group".into());
        }
        group.active_tab = tab_id;
        Ok(())
    }

    fn reorder_tab(&mut self, group_id: GroupId, tab_id: TabId, to: usize) -> Result<(), String> {
        let group = find_group_mut(&mut self.shell.root, group_id)
            .ok_or_else(|| "panel group does not exist".to_string())?;
        let from = group
            .tabs
            .iter()
            .position(|tab| tab.id == tab_id)
            .ok_or_else(|| "tab does not belong to panel group".to_string())?;
        if to >= group.tabs.len() {
            return Err("tab reorder index is outside the group".into());
        }
        let tab = group.tabs.remove(from);
        group.tabs.insert(to, tab);
        Ok(())
    }

    fn move_tab(
        &mut self,
        tab_id: TabId,
        target_group_id: GroupId,
        to: usize,
    ) -> Result<(), String> {
        let target_len = find_group(&self.shell.root, target_group_id)
            .ok_or_else(|| "target panel group does not exist".to_string())?
            .tabs
            .len();
        if to > target_len {
            return Err("tab move index is outside the target group".into());
        }
        let taken = take_tab(&mut self.shell.root, tab_id)
            .ok_or_else(|| "tab does not exist".to_string())?;
        if taken.group_id == target_group_id {
            return Err("use reorder for a move within one group".into());
        }
        let target = find_group_mut(&mut self.shell.root, target_group_id)
            .ok_or_else(|| "target group was removed with the source".to_string())?;
        target.tabs.insert(to.min(target.tabs.len()), taken.tab);
        target.active_tab = tab_id;
        Ok(())
    }

    fn split_group(
        &mut self,
        tab_id: TabId,
        target_group_id: GroupId,
        placement: DockPlacement,
        new_group_id: GroupId,
        new_split_id: SplitId,
    ) -> Result<(), String> {
        if placement == DockPlacement::Center {
            return Err("center docking moves into the existing group".into());
        }
        if find_group(&self.shell.root, new_group_id).is_some()
            || find_split(&self.shell.root, new_split_id).is_some()
        {
            return Err("new docking identities already exist".into());
        }
        let taken = take_tab(&mut self.shell.root, tab_id)
            .ok_or_else(|| "tab does not exist".to_string())?;
        let new_group = ShellNode::PanelGroup(PanelGroup {
            id: new_group_id,
            active_tab: tab_id,
            tabs: vec![taken.tab],
        });
        if !replace_group_with_split(
            &mut self.shell.root,
            target_group_id,
            new_group,
            new_split_id,
            placement,
        ) {
            return Err("target panel group does not exist after source removal".into());
        }
        self.shell.floating_layers.dock_preview = None;
        Ok(())
    }

    fn resize_adjacent(
        &mut self,
        split_id: SplitId,
        before_index: usize,
        delta_px: i32,
    ) -> Result<(), String> {
        let transaction = self
            .resize
            .as_ref()
            .ok_or_else(|| "no shell resize is active".to_string())?;
        if transaction.split_id != split_id || transaction.before_index != before_index {
            return Err("resize message does not match the active gutter".into());
        }
        let split = find_split_mut(&mut self.shell.root, split_id)
            .ok_or_else(|| "resize split does not exist".to_string())?;
        let total_share = split
            .children
            .iter()
            .map(|child| u64::from(child.share))
            .sum::<u64>();
        let pair_total = u64::from(split.children[before_index].share)
            + u64::from(split.children[before_index + 1].share);
        let minimum = total_share
            .saturating_mul(u64::from(MIN_PANEL_PX))
            .div_ceil(u64::from(transaction.available_px))
            .max(1)
            .min(pair_total / 2);
        let delta_share = i64::from(delta_px)
            .saturating_mul(i64::try_from(total_share).map_err(|_| "share total overflow")?)
            / i64::from(transaction.available_px);
        let current = i64::from(split.children[before_index].share);
        let lower = i64::try_from(minimum).map_err(|_| "minimum share overflow")?;
        let upper = i64::try_from(pair_total - minimum).map_err(|_| "maximum share overflow")?;
        let before = current.saturating_add(delta_share).clamp(lower, upper);
        let after = i64::try_from(pair_total).map_err(|_| "pair share overflow")? - before;
        split.children[before_index].share =
            u32::try_from(before).map_err(|_| "before share overflow")?;
        split.children[before_index + 1].share =
            u32::try_from(after).map_err(|_| "after share overflow")?;
        Ok(())
    }

    fn reset_adjacent(&mut self, split_id: SplitId, before_index: usize) -> Result<(), String> {
        let split = find_split_mut(&mut self.shell.root, split_id)
            .ok_or_else(|| "reset split does not exist".to_string())?;
        if before_index + 1 >= split.children.len() {
            return Err("reset gutter does not have two adjacent children".into());
        }
        let left_document = contains_document(&split.children[before_index].node);
        let right_document = contains_document(&split.children[before_index + 1].node);
        let total = split.children[before_index]
            .share
            .saturating_add(split.children[before_index + 1].share);
        let (left, right) = if left_document ^ right_document {
            let document = total.saturating_mul(80) / 100;
            let other = total - document;
            if left_document {
                (document, other)
            } else {
                (other, document)
            }
        } else {
            let left = total / 2;
            (left, total - left)
        };
        split.children[before_index].share = left.max(1);
        split.children[before_index + 1].share = right.max(1);
        Ok(())
    }

    fn close_panel(&mut self, tab_id: TabId) -> Result<(), String> {
        let tab = find_tab(&self.shell.root, tab_id)
            .ok_or_else(|| "panel tab does not exist".to_string())?;
        if !tab.closeable || tab.role == PanelRole::FolioDocument {
            return Err("the document panel cannot be closed in M006".into());
        }
        let before_root = self.shell.root.clone();
        let taken = take_tab(&mut self.shell.root, tab_id)
            .ok_or_else(|| "panel tab disappeared during close".to_string())?;
        self.closed.insert(
            tab_id,
            ClosedPanel {
                tab: taken.tab,
                group_id: taken.group_id,
                index: taken.index,
                active_before: taken.active_before,
                before_root,
                after_root: self.shell.root.clone(),
            },
        );
        if self.shell.floating_layers.overlay == Some(taken.group_id) {
            self.shell.floating_layers.overlay = None;
        }
        Ok(())
    }

    fn reopen_panel(&mut self, tab_id: TabId) -> Result<(), String> {
        let closed = self
            .closed
            .get(&tab_id)
            .cloned()
            .ok_or_else(|| "panel is not closed".to_string())?;
        if let Some(group) = find_group_mut(&mut self.shell.root, closed.group_id) {
            group
                .tabs
                .insert(closed.index.min(group.tabs.len()), closed.tab);
            if closed.active_before == tab_id {
                group.active_tab = tab_id;
            }
        } else if self.shell.root == closed.after_root {
            self.shell.root = closed.before_root;
        } else {
            return Err("closed panel position conflicts with later layout edits".into());
        }
        self.closed.remove(&tab_id);
        Ok(())
    }

    fn replaced(&self) -> AthenaFrontendMessage {
        AthenaFrontendMessage::Shell(ShellEffect::Replaced(self.shell.clone()))
    }

    fn values_changed(&self) -> AthenaFrontendMessage {
        let mut active_tabs = Vec::new();
        let mut shares = Vec::new();
        collect_values(&self.shell.root, &mut active_tabs, &mut shares);
        AthenaFrontendMessage::Shell(ShellEffect::ValuesChanged {
            active_tabs,
            shares,
            focus: self.shell.focus,
            overlay: self.shell.floating_layers.overlay,
        })
    }
}

fn diagnostic(message: &str) -> AthenaFrontendMessage {
    AthenaFrontendMessage::Diagnostic {
        code: "invalid-shell-message".into(),
        message: message.into(),
    }
}

fn collect_values(
    node: &ShellNode,
    active_tabs: &mut Vec<(GroupId, TabId)>,
    shares: &mut Vec<(SplitId, Vec<u32>)>,
) {
    match node {
        ShellNode::PanelGroup(group) => active_tabs.push((group.id, group.active_tab)),
        ShellNode::Split(split) => {
            shares.push((
                split.id,
                split.children.iter().map(|child| child.share).collect(),
            ));
            for child in &split.children {
                collect_values(&child.node, active_tabs, shares);
            }
        }
    }
}

fn find_group(node: &ShellNode, id: GroupId) -> Option<&PanelGroup> {
    match node {
        ShellNode::PanelGroup(group) if group.id == id => Some(group),
        ShellNode::PanelGroup(_) => None,
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| find_group(&child.node, id)),
    }
}

fn find_group_mut(node: &mut ShellNode, id: GroupId) -> Option<&mut PanelGroup> {
    match node {
        ShellNode::PanelGroup(group) if group.id == id => Some(group),
        ShellNode::PanelGroup(_) => None,
        ShellNode::Split(split) => split
            .children
            .iter_mut()
            .find_map(|child| find_group_mut(&mut child.node, id)),
    }
}

fn find_split(node: &ShellNode, id: SplitId) -> Option<&SplitNode> {
    match node {
        ShellNode::PanelGroup(_) => None,
        ShellNode::Split(split) if split.id == id => Some(split),
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| find_split(&child.node, id)),
    }
}

fn find_split_mut(node: &mut ShellNode, id: SplitId) -> Option<&mut SplitNode> {
    let ShellNode::Split(split) = node else {
        return None;
    };
    if split.id == id {
        Some(split)
    } else {
        split
            .children
            .iter_mut()
            .find_map(|child| find_split_mut(&mut child.node, id))
    }
}

fn find_tab(node: &ShellNode, id: TabId) -> Option<&PanelTab> {
    match node {
        ShellNode::PanelGroup(group) => group.tabs.iter().find(|tab| tab.id == id),
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| find_tab(&child.node, id)),
    }
}

fn take_tab(node: &mut ShellNode, tab_id: TabId) -> Option<TakenTab> {
    match node {
        ShellNode::PanelGroup(group) => {
            let index = group.tabs.iter().position(|tab| tab.id == tab_id)?;
            let active_before = group.active_tab;
            let tab = group.tabs.remove(index);
            if group.active_tab == tab_id && !group.tabs.is_empty() {
                group.active_tab = group.tabs[index.min(group.tabs.len() - 1)].id;
            }
            Some(TakenTab {
                tab,
                group_id: group.id,
                index,
                active_before,
            })
        }
        ShellNode::Split(split) => {
            let mut found = None;
            let mut empty_index = None;
            for (index, child) in split.children.iter_mut().enumerate() {
                if let Some(taken) = take_tab(&mut child.node, tab_id) {
                    if node_is_empty(&child.node) {
                        empty_index = Some(index);
                    }
                    found = Some(taken);
                    break;
                }
            }
            if let Some(index) = empty_index {
                split.children.remove(index);
            }
            if found.is_some() && split.children.len() == 1 {
                let only = split.children.remove(0);
                *node = *only.node;
            }
            found
        }
    }
}

fn node_is_empty(node: &ShellNode) -> bool {
    match node {
        ShellNode::PanelGroup(group) => group.tabs.is_empty(),
        ShellNode::Split(split) => split.children.is_empty(),
    }
}

fn replace_group_with_split(
    node: &mut ShellNode,
    target_group_id: GroupId,
    new_group: ShellNode,
    new_split_id: SplitId,
    placement: DockPlacement,
) -> bool {
    match node {
        ShellNode::PanelGroup(group) if group.id == target_group_id => {
            let target = node.clone();
            let axis = match placement {
                DockPlacement::Left | DockPlacement::Right => SplitAxis::Horizontal,
                DockPlacement::Top | DockPlacement::Bottom => SplitAxis::Vertical,
                DockPlacement::Center => return false,
            };
            let (first, second) = match placement {
                DockPlacement::Left | DockPlacement::Top => (new_group, target),
                DockPlacement::Right | DockPlacement::Bottom => (target, new_group),
                DockPlacement::Center => return false,
            };
            let (first_share, second_share) = split_pair_shares(&first, &second);
            *node = ShellNode::Split(
                SplitNode::try_new(
                    new_split_id,
                    axis,
                    vec![
                        SplitChild {
                            share: first_share,
                            node: Box::new(first),
                        },
                        SplitChild {
                            share: second_share,
                            node: Box::new(second),
                        },
                    ],
                )
                .expect("two positive docking children are valid"),
            );
            true
        }
        ShellNode::PanelGroup(_) => false,
        ShellNode::Split(split) => split.children.iter_mut().any(|child| {
            replace_group_with_split(
                &mut child.node,
                target_group_id,
                new_group.clone(),
                new_split_id,
                placement,
            )
        }),
    }
}

fn split_pair_shares(first: &ShellNode, second: &ShellNode) -> (u32, u32) {
    let first_document = contains_document(first);
    let second_document = contains_document(second);
    if first_document ^ second_document {
        if first_document { (80, 20) } else { (20, 80) }
    } else {
        (50, 50)
    }
}

fn contains_document(node: &ShellNode) -> bool {
    match node {
        ShellNode::PanelGroup(group) => group
            .tabs
            .iter()
            .any(|tab| tab.role == PanelRole::FolioDocument),
        ShellNode::Split(split) => split
            .children
            .iter()
            .any(|child| contains_document(&child.node)),
    }
}
