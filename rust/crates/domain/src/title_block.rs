//! Typed title-block values for electrical folios.
//!
//! Variable-bearing text is stored as structured segments so persistence and
//! rendering never depend on an implicit formula grammar.

use serde::{Deserialize, Serialize};

/// Placement edge for the selected title-block template.
#[derive(Clone, Copy, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub enum TitleBlockPlacement {
    /// Place the title block along the lower folio edge.
    #[default]
    Bottom,
    /// Place the title block along the right folio edge.
    Right,
}

/// Text that may contain typed project or folio variable references.
#[derive(Clone, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
#[serde(transparent)]
pub struct TemplateText(pub Vec<TemplateSegment>);

impl TemplateText {
    /// Creates template text containing one literal segment.
    #[must_use]
    pub fn literal(value: impl Into<String>) -> Self {
        Self(vec![TemplateSegment::Literal(value.into())])
    }
}

/// One persisted segment of variable-bearing title-block text.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum TemplateSegment {
    /// Text rendered verbatim.
    Literal(String),
    /// A typed variable lookup resolved against explicit scopes.
    Variable(VariableReference),
}

/// Supported variable scopes for M005 title-block resolution.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum VariableReference {
    /// Resolve a name from project-scoped custom variables.
    Project(String),
    /// Resolve a name from the active folio's custom variables.
    Folio(String),
}

/// Persisted standard values selected by a folio title block.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct TitleBlockValues {
    /// Stable template identity selected for this folio.
    pub template_id: String,
    /// Edge where the title block is placed.
    pub placement: TitleBlockPlacement,
    /// Drawing title text.
    pub title: TemplateText,
    /// Drawing author text.
    pub author: TemplateText,
    /// User-controlled date display text.
    pub date_text: TemplateText,
    /// File or document label text.
    pub file_label: TemplateText,
    /// Folio label displayed inside the title block.
    pub folio_label: TemplateText,
    /// Plant designation text.
    pub plant: TemplateText,
    /// Installation location text.
    pub location: TemplateText,
    /// Revision text.
    pub revision: TemplateText,
    /// User-configured page number text, independent of folio order.
    pub page_number: TemplateText,
}

impl Default for TitleBlockValues {
    fn default() -> Self {
        Self {
            template_id: "athena.standard".into(),
            placement: TitleBlockPlacement::Bottom,
            title: TemplateText::default(),
            author: TemplateText::default(),
            date_text: TemplateText::default(),
            file_label: TemplateText::default(),
            folio_label: TemplateText::default(),
            plant: TemplateText::default(),
            location: TemplateText::default(),
            revision: TemplateText::default(),
            page_number: TemplateText::default(),
        }
    }
}
