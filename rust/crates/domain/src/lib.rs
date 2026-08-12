//! Platform-neutral canonical state for Athena electrical schematic projects.

mod electrical;
mod ids;
mod project;
mod sheet;
mod symbol;

pub use electrical::{Annotation, ElectricalKind, FieldValue, Junction, Point, Wire, WireEndpoint};
pub use ids::{
    AnnotationId, JunctionId, ProjectId, SheetId, SymbolDefinitionId, SymbolInstanceId, TerminalId,
    WireId,
};
pub use project::{DomainError, Project, ProjectSettings};
pub use sheet::{Sheet, SheetSettings};
pub use symbol::{SymbolDefinition, SymbolInstance, Terminal};
