//! Typed semantic effects consumed by GPUI and browser platform adapters.

use athena_domain::{FolioId, ProjectId, ResolvedTemplateText};
use athena_editor::DocumentRevision;
use serde::{Deserialize, Serialize};

use crate::{
    DockTarget, GroupId, LayoutTarget, SaveRequestId, ShellFocus, SplitId, TabId, Widget, WidgetId,
    WidgetValue, WorkspaceShell,
};

/// Semantic shell effects reduced by both platform adapters.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum ShellEffect {
    /// Complete tree replacement after structural changes or an explicit request.
    Replaced(WorkspaceShell),
    /// Value-only update that preserves the recursive node structure.
    ValuesChanged {
        active_tabs: Vec<(GroupId, TabId)>,
        shares: Vec<(SplitId, Vec<u32>)>,
        focus: ShellFocus,
        overlay: Option<GroupId>,
    },
    /// Floating docking feedback that never changes split geometry.
    DockPreview(Option<DockTarget>),
}

/// Resolved QET-equivalent title-block values displayed in a folio viewport.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct ResolvedTitleBlockDisplay {
    pub title: ResolvedTemplateText,
    pub author: ResolvedTemplateText,
    pub date_text: ResolvedTemplateText,
    pub file_label: ResolvedTemplateText,
    pub folio_label: ResolvedTemplateText,
    pub plant: ResolvedTemplateText,
    pub location: ResolvedTemplateText,
    pub revision: ResolvedTemplateText,
    pub page_number: ResolvedTemplateText,
}

/// One ordered frontend or platform effect emitted by the dispatcher.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum AthenaFrontendMessage {
    ProjectOpened {
        project_id: ProjectId,
        name: String,
    },
    ProjectClosed,
    ActiveFolioChanged {
        folio_id: FolioId,
    },
    DirtyStateChanged {
        dirty: bool,
    },
    OutlineChanged {
        project_name: String,
        folios: Vec<(FolioId, String)>,
    },
    Shell(ShellEffect),
    PanelLayoutUpdated {
        target: LayoutTarget,
        widgets: Vec<Widget>,
    },
    WidgetValuesUpdated {
        target: LayoutTarget,
        values: Vec<(WidgetId, WidgetValue)>,
    },
    ResolvedTitleBlockUpdated {
        folio_id: FolioId,
        display: Box<ResolvedTitleBlockDisplay>,
    },
    SaveRequested {
        request_id: SaveRequestId,
        project_id: ProjectId,
        revision: DocumentRevision,
        bytes: Vec<u8>,
    },
    OpenRequested,
    Diagnostic {
        code: String,
        message: String,
    },
    Error {
        message: String,
    },
}
