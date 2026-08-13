//! Platform-neutral canonical state for Athena electrical schematic projects.

mod electrical;
mod ids;
mod project;
mod sheet;
mod symbol;

pub use electrical::{
    Annotation, ElectricalKind, FieldValue, Junction, Point, Wire, WireEndpoint,
    canonical_wire_route,
};
pub use ids::{
    AnnotationId, JunctionId, ProjectId, SheetId, SymbolDefinitionId, SymbolInstanceId, TerminalId,
    WireId,
};
pub use project::{DomainError, Project, ProjectSettings, WireRouteEndpoint};
pub use sheet::{Sheet, SheetSettings};
pub use symbol::{SymbolDefinition, SymbolInstance, Terminal};
