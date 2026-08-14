//! Stable widget identities, values, and callbacks owned by Rust layout state.

use std::collections::BTreeMap;

use athena_domain::{FolioId, TemplateText};
use athena_editor::TitleBlockField;
use serde::{Deserialize, Serialize};

/// Stable widget identity retained across value-only refreshes.
#[derive(Clone, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
#[serde(transparent)]
pub struct WidgetId(pub String);

impl WidgetId {
    /// Creates a stable widget ID from a namespaced string.
    #[must_use]
    pub fn new(value: impl Into<String>) -> Self {
        Self(value.into())
    }
}

/// Rendering control kind understood by native and browser adapters.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum WidgetKind {
    TextInput,
    Select { options: Vec<String> },
    VariableTable,
}

/// Platform-neutral widget value.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum WidgetValue {
    Text(String),
    Choice(String),
    Template(TemplateText),
    Variables(BTreeMap<String, String>),
    VariableEdit { key: String, value: Option<String> },
}

/// One backend-owned property control rendered by both shells.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Widget {
    /// Stable callback and rendering identity.
    pub id: WidgetId,
    /// User-facing electrical field label.
    pub label: String,
    /// Cross-platform control kind.
    pub kind: WidgetKind,
    /// Current Rust-owned control value.
    pub value: WidgetValue,
}

/// Typed action associated with a stable widget identity.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum WidgetCallback {
    RenameProject,
    ProjectVariables,
    DefaultTemplate,
    DefaultPlacement,
    DefaultPageNumber,
    RenameFolio {
        folio_id: FolioId,
    },
    FolioVariables {
        folio_id: FolioId,
    },
    TitleBlock {
        folio_id: FolioId,
        field: TitleBlockField,
    },
}
