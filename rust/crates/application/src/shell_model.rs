//! Platform-neutral workspace shell tree shared by native and browser adapters.
//!
//! Graphite's recursive subdivision and panel-group behavior define the shape
//! of this contract. The electrical labels come from QElectroTech's panel
//! taxonomy. No UI toolkit, document state, or platform service enters here.

use std::{collections::BTreeSet, fmt};

use serde::{Deserialize, Serialize};
use thiserror::Error;
use uuid::Uuid;

/// Minimum rendered panel extent in logical pixels.
pub const MIN_PANEL_PX: u32 = 100;

macro_rules! shell_id {
    ($name:ident, $summary:literal) => {
        #[doc = $summary]
        #[derive(
            Clone, Copy, Debug, Deserialize, Eq, Hash, Ord, PartialEq, PartialOrd, Serialize,
        )]
        #[serde(transparent)]
        pub struct $name(Uuid);

        impl $name {
            /// Creates a new identity for a user-created shell item.
            #[must_use]
            pub fn new() -> Self {
                Self(Uuid::new_v4())
            }

            /// Wraps an explicit UUID for deterministic fixtures and defaults.
            #[must_use]
            pub const fn from_uuid(value: Uuid) -> Self {
                Self(value)
            }

            /// Returns the underlying UUID for persistence and diagnostics.
            #[must_use]
            pub const fn as_uuid(self) -> Uuid {
                self.0
            }
        }

        impl Default for $name {
            fn default() -> Self {
                Self::new()
            }
        }

        impl fmt::Display for $name {
            fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
                self.0.fmt(formatter)
            }
        }
    };
}

shell_id!(SplitId, "Stable identity of one recursive split node.");
shell_id!(GroupId, "Stable identity of one docked panel group.");
shell_id!(TabId, "Stable identity of one panel tab.");

/// One complete backend-owned editor shell.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct WorkspaceShell {
    /// Compact window title state rendered above the workspace.
    pub title_bar: TitleBarState,
    /// Recursive docked workspace.
    pub root: ShellNode,
    /// Compact hints and document information rendered below the workspace.
    pub status_bar: StatusBarState,
    /// Overlays and docking feedback rendered above the workspace geometry.
    pub floating_layers: FloatingLayersState,
    /// Whether the complete workspace or only the document is presented.
    pub focus: ShellFocus,
}

impl WorkspaceShell {
    /// Validates structural identities, active tabs, shares, and document count.
    pub fn validate(&self) -> Result<(), ShellModelError> {
        let mut identities = BTreeSet::new();
        let mut document_tabs = 0;
        validate_node(&self.root, &mut identities, &mut document_tabs)?;
        if document_tabs != 1 {
            return Err(ShellModelError::DocumentTabCount {
                actual: document_tabs,
            });
        }
        Ok(())
    }
}

impl Default for WorkspaceShell {
    fn default() -> Self {
        let left_upper = group(
            4,
            &[
                (9, PanelRole::Project, "Project", true),
                (10, PanelRole::Folios, "Folios", true),
            ],
        );
        let left_lower = group(
            5,
            &[
                (11, PanelRole::Elements, "Elements", true),
                (12, PanelRole::TitleBlocks, "Title Blocks", true),
            ],
        );
        let document = group(6, &[(13, PanelRole::FolioDocument, "Folio 1", false)]);
        let right_upper = group(
            7,
            &[
                (
                    14,
                    PanelRole::SelectionProperties,
                    "Selection Properties",
                    true,
                ),
                (15, PanelRole::FolioProperties, "Folio Properties", true),
            ],
        );
        let right_lower = group(
            8,
            &[
                (16, PanelRole::Diagnostics, "Diagnostics", true),
                (17, PanelRole::History, "History", true),
            ],
        );

        let left = split(
            2,
            SplitAxis::Vertical,
            vec![child(50, left_upper), child(50, left_lower)],
        );
        let right = split(
            3,
            SplitAxis::Vertical,
            vec![child(50, right_upper), child(50, right_lower)],
        );
        let root = split(
            1,
            SplitAxis::Horizontal,
            vec![child(20, left), child(64, document), child(16, right)],
        );

        Self {
            title_bar: TitleBarState {
                document_label: "Folio 1".into(),
                dirty: false,
            },
            root,
            status_bar: StatusBarState {
                hint: "Ready".into(),
                document_info: vec!["Folio 1".into(), "100%".into(), "Grid 10 mm".into()],
            },
            floating_layers: FloatingLayersState::default(),
            focus: ShellFocus::Workspace,
        }
    }
}

/// Compact title information shared by both platform shells.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct TitleBarState {
    /// Active document label displayed in the window chrome.
    pub document_label: String,
    /// Whether the active document has unsaved edits.
    pub dirty: bool,
}

/// Compact status information shared by both platform shells.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct StatusBarState {
    /// Context hint clipped on the left when space is constrained.
    pub hint: String,
    /// Ordered document, zoom, and grid information shown on the right.
    pub document_info: Vec<String>,
}

/// Overlay state that never participates in split geometry.
#[derive(Clone, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub struct FloatingLayersState {
    /// Panel group presented as a narrow-viewport overlay.
    pub overlay: Option<GroupId>,
    /// Current tab docking feedback target.
    pub dock_preview: Option<DockTarget>,
}

/// Recursive shell node matching Graphite's split-or-panel-group model.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum ShellNode {
    /// A row or column of two or more child nodes.
    Split(SplitNode),
    /// A tabbed group with one active panel.
    PanelGroup(PanelGroup),
}

/// One horizontal or vertical subdivision.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SplitNode {
    /// Stable split identity used by resize messages.
    pub id: SplitId,
    /// Direction in which children are laid out.
    pub axis: SplitAxis,
    /// Ordered child nodes and their relative shares.
    pub children: Vec<SplitChild>,
}

impl SplitNode {
    /// Builds a locally valid split while preserving the supplied share total.
    pub fn try_new(
        id: SplitId,
        axis: SplitAxis,
        children: Vec<SplitChild>,
    ) -> Result<Self, ShellModelError> {
        validate_split_children(id, &children)?;
        Ok(Self { id, axis, children })
    }
}

/// One weighted child of a split.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SplitChild {
    /// Positive relative size preserved across adjacent resizing.
    pub share: u32,
    /// Nested split or panel group.
    pub node: Box<ShellNode>,
}

/// One docked tab group.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct PanelGroup {
    /// Stable group identity used by tab and docking messages.
    pub id: GroupId,
    /// Ordered tabs owned by this group.
    pub tabs: Vec<PanelTab>,
    /// Tab currently visible in the group body.
    pub active_tab: TabId,
}

/// One tab and its electrical surface role.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct PanelTab {
    /// Stable tab identity preserved while tabs move between groups.
    pub id: TabId,
    /// Semantic electrical surface rendered in the panel body.
    pub role: PanelRole,
    /// User-visible tab label.
    pub label: String,
    /// Whether M006 permits the tab to be closed.
    pub closeable: bool,
}

/// Electrical surface taxonomy shown inside the Graphite-derived shell.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum PanelRole {
    Project,
    Folios,
    Elements,
    TitleBlocks,
    FolioDocument,
    SelectionProperties,
    FolioProperties,
    Diagnostics,
    History,
}

/// Direction in which a split lays out its children.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum SplitAxis {
    Horizontal,
    Vertical,
}

/// Workspace presentation mode independent of document state.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum ShellFocus {
    Workspace,
    Document,
}

/// One panel docking preview target.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct DockTarget {
    /// Group receiving the moved tab.
    pub group_id: GroupId,
    /// Edge or center operation previewed for the group.
    pub placement: DockPlacement,
}

/// Position of a tab drop relative to a target group.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum DockPlacement {
    Left,
    Right,
    Top,
    Bottom,
    Center,
}

/// Structural reason a shell tree cannot be accepted.
#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum ShellModelError {
    #[error("split {split_id} must contain at least two children")]
    TooFewSplitChildren { split_id: SplitId },
    #[error("split {split_id} contains a zero share")]
    ZeroShare { split_id: SplitId },
    #[error("panel group {group_id} has no tabs")]
    EmptyPanelGroup { group_id: GroupId },
    #[error("panel group {group_id} does not contain active tab {active_tab}")]
    MissingActiveTab {
        group_id: GroupId,
        active_tab: TabId,
    },
    #[error("shell identity {identity} is reused")]
    DuplicateIdentity { identity: Uuid },
    #[error("workspace must contain exactly one document tab, found {actual}")]
    DocumentTabCount { actual: usize },
    #[error("the M006 document tab cannot be closeable")]
    CloseableDocument,
}

fn validate_node(
    node: &ShellNode,
    identities: &mut BTreeSet<Uuid>,
    document_tabs: &mut usize,
) -> Result<(), ShellModelError> {
    match node {
        ShellNode::Split(split) => {
            insert_identity(identities, split.id.as_uuid())?;
            validate_split_children(split.id, &split.children)?;
            for child in &split.children {
                validate_node(&child.node, identities, document_tabs)?;
            }
        }
        ShellNode::PanelGroup(group) => {
            insert_identity(identities, group.id.as_uuid())?;
            if group.tabs.is_empty() {
                return Err(ShellModelError::EmptyPanelGroup { group_id: group.id });
            }
            if !group.tabs.iter().any(|tab| tab.id == group.active_tab) {
                return Err(ShellModelError::MissingActiveTab {
                    group_id: group.id,
                    active_tab: group.active_tab,
                });
            }
            for tab in &group.tabs {
                insert_identity(identities, tab.id.as_uuid())?;
                if tab.role == PanelRole::FolioDocument {
                    *document_tabs += 1;
                    if tab.closeable {
                        return Err(ShellModelError::CloseableDocument);
                    }
                }
            }
        }
    }
    Ok(())
}

fn validate_split_children(id: SplitId, children: &[SplitChild]) -> Result<(), ShellModelError> {
    if children.len() < 2 {
        return Err(ShellModelError::TooFewSplitChildren { split_id: id });
    }
    if children.iter().any(|child| child.share == 0) {
        return Err(ShellModelError::ZeroShare { split_id: id });
    }
    Ok(())
}

fn insert_identity(identities: &mut BTreeSet<Uuid>, identity: Uuid) -> Result<(), ShellModelError> {
    if identities.insert(identity) {
        Ok(())
    } else {
        Err(ShellModelError::DuplicateIdentity { identity })
    }
}

fn split(id: u128, axis: SplitAxis, children: Vec<SplitChild>) -> ShellNode {
    ShellNode::Split(
        SplitNode::try_new(SplitId::from_uuid(Uuid::from_u128(id)), axis, children)
            .expect("built-in split is structurally valid"),
    )
}

fn child(share: u32, node: ShellNode) -> SplitChild {
    SplitChild {
        share,
        node: Box::new(node),
    }
}

fn group(id: u128, tabs: &[(u128, PanelRole, &str, bool)]) -> ShellNode {
    let tabs = tabs
        .iter()
        .map(|(id, role, label, closeable)| PanelTab {
            id: TabId::from_uuid(Uuid::from_u128(*id)),
            role: *role,
            label: (*label).into(),
            closeable: *closeable,
        })
        .collect::<Vec<_>>();
    ShellNode::PanelGroup(PanelGroup {
        id: GroupId::from_uuid(Uuid::from_u128(id)),
        active_tab: tabs[0].id,
        tabs,
    })
}
