//! Root application messages accepted by the platform-neutral dispatcher.

use athena_domain::{FolioId, ProjectId, TemplateText, TitleBlockPlacement};
use athena_editor::{DocumentRevision, TitleBlockField, TitleBlockValue};
use serde::{Deserialize, Serialize};

use crate::{
    DockPlacement, DockTarget, GroupId, LayoutTarget, SaveRequestId, SplitId, TabId, WidgetId,
    WidgetValue,
};

/// Root protocol family routed by [`crate::AthenaDispatcher`].
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum AthenaMessage {
    Portfolio(PortfolioMessage),
    Document(DocumentMessage),
    Layout(LayoutMessage),
    Shell(ShellMessage),
}

impl From<PortfolioMessage> for AthenaMessage {
    fn from(value: PortfolioMessage) -> Self {
        Self::Portfolio(value)
    }
}
impl From<DocumentMessage> for AthenaMessage {
    fn from(value: DocumentMessage) -> Self {
        Self::Document(value)
    }
}
impl From<LayoutMessage> for AthenaMessage {
    fn from(value: LayoutMessage) -> Self {
        Self::Layout(value)
    }
}
impl From<ShellMessage> for AthenaMessage {
    fn from(value: ShellMessage) -> Self {
        Self::Shell(value)
    }
}

/// Project lifetime and platform persistence messages.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum PortfolioMessage {
    CreateProject {
        project_id: ProjectId,
        initial_folio_id: FolioId,
        name: String,
    },
    RequestOpen,
    OpenBytes {
        bytes: Vec<u8>,
    },
    OpenResult {
        outcome: OpenOutcome,
    },
    RequestSave,
    SaveResult {
        request_id: SaveRequestId,
        project_id: ProjectId,
        revision: DocumentRevision,
        outcome: SaveOutcome,
    },
    CloseProject,
}

/// Result reported by a desktop or browser open adapter.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum OpenOutcome {
    /// User selected a project and the adapter read its bytes.
    Success { bytes: Vec<u8> },
    /// User dismissed the platform open dialog.
    Cancelled,
    /// Platform storage could not read the selected project.
    Failed { message: String },
}

/// Result reported by a desktop or browser persistence adapter.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum SaveOutcome {
    Success,
    Cancelled,
    Failed { message: String },
}

/// Typed document editing messages translated into deterministic commands.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum DocumentMessage {
    AddFolio {
        folio_id: FolioId,
        label: String,
    },
    ActivateFolio {
        folio_id: FolioId,
    },
    MoveFolio {
        folio_id: FolioId,
        to: usize,
    },
    RenameProject {
        name: String,
    },
    RenameFolio {
        folio_id: FolioId,
        label: String,
    },
    SetProjectVariable {
        key: String,
        value: Option<String>,
    },
    SetFolioVariable {
        folio_id: FolioId,
        key: String,
        value: Option<String>,
    },
    SetTitleBlockValue {
        folio_id: FolioId,
        field: TitleBlockField,
        value: TitleBlockValue,
    },
    SetDefaultPlacement {
        placement: TitleBlockPlacement,
    },
    SetDefaultTemplate {
        template_id: String,
    },
    SetDefaultPageNumber {
        value: TemplateText,
    },
    Undo,
    Redo,
}

/// Workspace, panel, and backend-owned widget messages.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum LayoutMessage {
    RequestProjectPlate,
    RequestFolioPlate {
        folio_id: FolioId,
    },
    CommitWidget {
        target: LayoutTarget,
        widget_id: WidgetId,
        value: WidgetValue,
    },
}

/// Platform-neutral workspace shell messages.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum ShellMessage {
    Request,
    ActivateTab {
        group_id: GroupId,
        tab_id: TabId,
    },
    ReorderTab {
        group_id: GroupId,
        tab_id: TabId,
        to: usize,
    },
    MoveTab {
        tab_id: TabId,
        target_group_id: GroupId,
        to: usize,
    },
    SplitGroup {
        tab_id: TabId,
        target_group_id: GroupId,
        placement: DockPlacement,
        new_group_id: GroupId,
        new_split_id: SplitId,
    },
    BeginResize {
        split_id: SplitId,
        before_index: usize,
        available_px: u32,
    },
    ResizeAdjacent {
        split_id: SplitId,
        before_index: usize,
        delta_px: i32,
    },
    CommitResize,
    AbortResize,
    ResetAdjacent {
        split_id: SplitId,
        before_index: usize,
    },
    ClosePanel {
        tab_id: TabId,
    },
    ReopenPanel {
        tab_id: TabId,
    },
    SetDocumentFocus {
        focused: bool,
    },
    OpenOverlay {
        group_id: GroupId,
    },
    CloseOverlay,
    SetDockPreview {
        target: Option<DockTarget>,
    },
}
