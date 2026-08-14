//! Platform-neutral canonical state for Athena electrical schematic projects.

mod electrical;
mod folio;
mod ids;
mod project;
mod symbol;
mod title_block;
mod variables;

pub use electrical::{
    Annotation, ElectricalKind, FieldValue, Junction, Point, Wire, WireEndpoint,
    canonical_wire_route,
};
pub use folio::{Folio, SchematicContent, SchematicSettings};
pub use ids::{
    AnnotationId, FolioId, JunctionId, ProjectId, SymbolDefinitionId, SymbolInstanceId, TerminalId,
    WireId,
};
pub use project::{DomainError, Project, ProjectFolioDefaults, ProjectSettings, WireRouteEndpoint};
pub use symbol::{SymbolDefinition, SymbolInstance, Terminal};
pub use title_block::{
    TemplateSegment, TemplateText, TitleBlockPlacement, TitleBlockValues, VariableReference,
};
pub use variables::{ResolvedTemplateText, VariableDiagnostic, resolve_template_text};
