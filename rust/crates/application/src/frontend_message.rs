//! Typed semantic effects consumed by GPUI and browser platform adapters.

use athena_domain::{FolioId, ProjectId};
use athena_editor::DocumentRevision;
use serde::{Deserialize, Serialize};

use crate::{LayoutTarget, SaveRequestId, Widget, WidgetId, WidgetValue, WorkspaceLayout};

/// One ordered frontend or platform effect emitted by the dispatcher.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
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
    WorkspaceLayoutUpdated(WorkspaceLayout),
    PanelLayoutUpdated {
        target: LayoutTarget,
        widgets: Vec<Widget>,
    },
    WidgetValuesUpdated {
        target: LayoutTarget,
        values: Vec<(WidgetId, WidgetValue)>,
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
