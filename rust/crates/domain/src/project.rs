//! Project aggregate ownership and validation for electrical schematics.
//!
//! This module is the durable admission boundary for persisted projects: it
//! verifies entity references, identifier integrity, and canonical wire routes
//! before a project can be saved, replayed, rendered, or shared between shells.

use std::collections::{BTreeMap, BTreeSet};

use serde::{Deserialize, Serialize};
use thiserror::Error;
use uuid::Uuid;

use crate::{
    Annotation, AnnotationId, Junction, JunctionId, Point, ProjectId, Sheet, SheetId,
    SymbolDefinition, SymbolDefinitionId, SymbolInstance, SymbolInstanceId, Terminal, TerminalId,
    Wire, WireEndpoint, WireId, canonical_wire_route,
};

/// Project-wide defaults persisted alongside schematic sheets.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct ProjectSettings {
    /// Default document-space grid spacing.
    pub grid_spacing: i64,
    /// Whether the default grid snaps pointer-derived coordinates.
    pub snap_enabled: bool,
    /// Display unit label for project-facing values.
    pub units: String,
}

impl Default for ProjectSettings {
    fn default() -> Self {
        Self {
            grid_spacing: 10,
            snap_enabled: true,
            units: "mm".into(),
        }
    }
}

/// The complete platform-neutral persisted schematic project.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Project {
    /// Stable project identity used by persistence and synchronization.
    pub id: ProjectId,
    /// Human-facing project name.
    pub name: String,
    #[serde(default)]
    pub settings: ProjectSettings,
    #[serde(default)]
    pub symbol_definitions: BTreeMap<SymbolDefinitionId, SymbolDefinition>,
    #[serde(default)]
    pub sheets: BTreeMap<SheetId, Sheet>,
    #[serde(default)]
    pub sheet_order: Vec<SheetId>,
}

impl Project {
    /// Creates a named project with one default sheet.
    #[must_use]
    pub fn new(name: impl Into<String>) -> Self {
        let mut project = Self {
            id: ProjectId::new(),
            name: name.into(),
            settings: ProjectSettings::default(),
            symbol_definitions: BTreeMap::new(),
            sheets: BTreeMap::new(),
            sheet_order: Vec::new(),
        };
        project.add_sheet("Sheet 1");
        project
    }

    #[must_use]
    pub fn sheets(&self) -> &BTreeMap<SheetId, Sheet> {
        &self.sheets
    }

    #[must_use]
    pub fn sheet_order(&self) -> &[SheetId] {
        &self.sheet_order
    }

    #[must_use]
    pub fn symbol_definitions(&self) -> &BTreeMap<SymbolDefinitionId, SymbolDefinition> {
        &self.symbol_definitions
    }

    #[must_use]
    pub fn sheet(&self, sheet_id: SheetId) -> Option<&Sheet> {
        self.sheets.get(&sheet_id)
    }

    pub fn sheet_mut(&mut self, sheet_id: SheetId) -> Option<&mut Sheet> {
        self.sheets.get_mut(&sheet_id)
    }

    #[must_use]
    pub fn symbol_definition(
        &self,
        definition_id: SymbolDefinitionId,
    ) -> Option<&SymbolDefinition> {
        self.symbol_definitions.get(&definition_id)
    }

    #[must_use]
    pub fn symbol_instance(
        &self,
        sheet_id: SheetId,
        instance_id: SymbolInstanceId,
    ) -> Option<&SymbolInstance> {
        self.sheet(sheet_id)?.instance(instance_id)
    }

    #[must_use]
    pub fn terminal(&self, sheet_id: SheetId, terminal_id: TerminalId) -> Option<&Terminal> {
        self.sheet(sheet_id)?
            .symbol_instances()
            .values()
            .find_map(|instance| instance.terminals.get(&terminal_id))
    }

    #[must_use]
    pub fn wire(&self, sheet_id: SheetId, wire_id: WireId) -> Option<&Wire> {
        self.sheet(sheet_id)?.wire(wire_id)
    }

    #[must_use]
    pub fn junction(&self, sheet_id: SheetId, junction_id: JunctionId) -> Option<&Junction> {
        self.sheet(sheet_id)?.junction(junction_id)
    }

    #[must_use]
    pub fn annotation(
        &self,
        sheet_id: SheetId,
        annotation_id: AnnotationId,
    ) -> Option<&Annotation> {
        self.sheet(sheet_id)?.annotation(annotation_id)
    }

    pub fn add_sheet(&mut self, name: impl Into<String>) -> SheetId {
        let sheet = Sheet::new(name);
        let sheet_id = sheet.id;
        self.sheets.insert(sheet_id, sheet);
        self.sheet_order.push(sheet_id);
        sheet_id
    }

    pub fn remove_sheet(&mut self, sheet_id: SheetId) -> Option<Sheet> {
        if self.sheets.len() <= 1 {
            return None;
        }
        let removed = self.sheets.remove(&sheet_id)?;
        self.sheet_order.retain(|id| *id != sheet_id);
        Some(removed)
    }

    pub fn add_symbol_definition(
        &mut self,
        definition: SymbolDefinition,
    ) -> Result<(), DomainError> {
        if self.symbol_definitions.contains_key(&definition.id) {
            return Err(DomainError::DuplicateSymbolDefinition {
                symbol_definition_id: definition.id,
            });
        }
        self.symbol_definitions.insert(definition.id, definition);
        Ok(())
    }

    /// Validates every persisted cross-entity and electrical-route invariant.
    pub fn validate(&self) -> Result<(), DomainError> {
        if self.name.trim().is_empty() {
            return Err(DomainError::EmptyProjectName {
                project_id: self.id,
            });
        }
        if self.sheets.is_empty() {
            return Err(DomainError::ProjectHasNoSheets {
                project_id: self.id,
            });
        }
        if self.settings.grid_spacing <= 0 {
            return Err(DomainError::InvalidGridSpacing {
                project_id: self.id,
                grid_spacing: self.settings.grid_spacing,
            });
        }

        let mut seen_sheets = BTreeSet::new();
        for sheet_id in &self.sheet_order {
            if !seen_sheets.insert(*sheet_id) {
                return Err(DomainError::DuplicateSheetOrder {
                    sheet_id: *sheet_id,
                });
            }
            if !self.sheets.contains_key(sheet_id) {
                return Err(DomainError::OrderedSheetMissing {
                    sheet_id: *sheet_id,
                });
            }
        }
        for sheet_id in self.sheets.keys() {
            if !seen_sheets.contains(sheet_id) {
                return Err(DomainError::UnorderedSheet {
                    sheet_id: *sheet_id,
                });
            }
        }

        let mut all_ids = BTreeSet::new();
        self.ensure_unique_uuid(self.id.as_uuid(), &mut all_ids)?;
        for (definition_id, definition) in &self.symbol_definitions {
            if definition.id != *definition_id {
                return Err(DomainError::SymbolDefinitionKeyMismatch {
                    symbol_definition_id: *definition_id,
                    symbol_definition_record_id: definition.id,
                });
            }
            self.ensure_unique_uuid(definition.id.as_uuid(), &mut all_ids)?;
        }

        for (sheet_id, sheet) in &self.sheets {
            if sheet.id != *sheet_id {
                return Err(DomainError::SheetKeyMismatch {
                    sheet_id: *sheet_id,
                    sheet_record_id: sheet.id,
                });
            }
            self.ensure_unique_uuid(sheet.id.as_uuid(), &mut all_ids)?;
            let mut terminal_ids = BTreeSet::new();
            for (instance_id, instance) in &sheet.symbol_instances {
                if instance.id != *instance_id {
                    return Err(DomainError::SymbolInstanceKeyMismatch {
                        symbol_instance_id: *instance_id,
                        symbol_instance_record_id: instance.id,
                    });
                }
                self.ensure_unique_uuid(instance.id.as_uuid(), &mut all_ids)?;
                if !self
                    .symbol_definitions
                    .contains_key(&instance.definition_id)
                {
                    return Err(DomainError::MissingSymbolDefinition {
                        symbol_instance_id: instance.id,
                        symbol_definition_id: instance.definition_id,
                    });
                }
                for (terminal_id, terminal) in &instance.terminals {
                    self.ensure_unique_uuid(terminal.id.as_uuid(), &mut all_ids)?;
                    if terminal.id != *terminal_id {
                        return Err(DomainError::TerminalKeyMismatch {
                            terminal_id: *terminal_id,
                            terminal_record_id: terminal.id,
                        });
                    }
                    if terminal.owner != instance.id {
                        return Err(DomainError::TerminalOwnerMismatch {
                            terminal_id: *terminal_id,
                            expected_owner: instance.id,
                            actual_owner: terminal.owner,
                        });
                    }
                    terminal_ids.insert(*terminal_id);
                }
            }
            for (junction_id, junction) in &sheet.junctions {
                if junction.id != *junction_id {
                    return Err(DomainError::JunctionKeyMismatch {
                        junction_id: *junction_id,
                        junction_record_id: junction.id,
                    });
                }
                self.ensure_unique_uuid(junction.id.as_uuid(), &mut all_ids)?;
            }
            for (annotation_id, annotation) in &sheet.annotations {
                if annotation.id != *annotation_id {
                    return Err(DomainError::AnnotationKeyMismatch {
                        annotation_id: *annotation_id,
                        annotation_record_id: annotation.id,
                    });
                }
                self.ensure_unique_uuid(annotation.id.as_uuid(), &mut all_ids)?;
            }
            for (wire_id, wire) in &sheet.wires {
                if wire.id != *wire_id {
                    return Err(DomainError::WireKeyMismatch {
                        wire_id: *wire_id,
                        wire_record_id: wire.id,
                    });
                }
                self.ensure_unique_uuid(wire.id.as_uuid(), &mut all_ids)?;
                self.validate_wire_endpoint(wire.id, &wire.start, &terminal_ids, sheet)?;
                self.validate_wire_endpoint(wire.id, &wire.end, &terminal_ids, sheet)?;
                self.validate_wire_route(wire, sheet, &terminal_ids)?;
            }
        }

        Ok(())
    }

    fn ensure_unique_uuid(
        &self,
        id: Uuid,
        all_ids: &mut BTreeSet<Uuid>,
    ) -> Result<(), DomainError> {
        if all_ids.insert(id) {
            Ok(())
        } else {
            Err(DomainError::DuplicateEntityId { entity_id: id })
        }
    }

    fn validate_wire_endpoint(
        &self,
        wire_id: WireId,
        endpoint: &WireEndpoint,
        terminal_ids: &BTreeSet<TerminalId>,
        sheet: &Sheet,
    ) -> Result<(), DomainError> {
        match endpoint {
            WireEndpoint::Terminal(terminal_id) if !terminal_ids.contains(terminal_id) => {
                Err(DomainError::MissingTerminal {
                    wire_id,
                    terminal_id: *terminal_id,
                })
            }
            WireEndpoint::Junction(junction_id) if !sheet.junctions.contains_key(junction_id) => {
                Err(DomainError::MissingJunction {
                    wire_id,
                    junction_id: *junction_id,
                })
            }
            _ => Ok(()),
        }
    }

    /// Checks the route held in persistence, after endpoint references have
    /// been resolved. This is shared validation so replay and direct domain
    /// construction cannot admit geometry that editor commands would reject.
    fn validate_wire_route(
        &self,
        wire: &Wire,
        sheet: &Sheet,
        terminal_ids: &BTreeSet<TerminalId>,
    ) -> Result<(), DomainError> {
        if wire.route.len() < 2 {
            return Err(DomainError::WireRouteTooShort { wire_id: wire.id });
        }
        let start = endpoint_position(&wire.start, sheet, terminal_ids)
            .expect("validated wire start endpoint resolves");
        let end = endpoint_position(&wire.end, sheet, terminal_ids)
            .expect("validated wire end endpoint resolves");
        if wire.route[0] != start {
            return Err(DomainError::WireRouteEndpointMismatch {
                wire_id: wire.id,
                endpoint: WireRouteEndpoint::Start,
            });
        }
        if wire.route.last().copied() != Some(end) {
            return Err(DomainError::WireRouteEndpointMismatch {
                wire_id: wire.id,
                endpoint: WireRouteEndpoint::End,
            });
        }
        for (segment_index, segment) in wire.route.windows(2).enumerate() {
            if segment[0] == segment[1] {
                return Err(DomainError::ZeroLengthWireSegment {
                    wire_id: wire.id,
                    segment_index,
                });
            }
            if segment[0].x != segment[1].x && segment[0].y != segment[1].y {
                return Err(DomainError::NonOrthogonalWireSegment {
                    wire_id: wire.id,
                    segment_index,
                });
            }
        }
        if canonical_wire_route(&wire.route) != wire.route {
            return Err(DomainError::NonCanonicalWireRoute { wire_id: wire.id });
        }
        Ok(())
    }
}

/// The endpoint position whose stored route point failed validation.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum WireRouteEndpoint {
    /// The first route point.
    Start,
    /// The final route point.
    End,
}

fn endpoint_position(
    endpoint: &WireEndpoint,
    sheet: &Sheet,
    terminal_ids: &BTreeSet<TerminalId>,
) -> Option<Point> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id) if terminal_ids.contains(terminal_id) => {
            sheet.symbol_instances.values().find_map(|symbol| {
                symbol
                    .terminals
                    .get(terminal_id)
                    .map(|terminal| terminal.position)
            })
        }
        WireEndpoint::Junction(junction_id) => sheet
            .junctions
            .get(junction_id)
            .map(|junction| junction.position),
        _ => None,
    }
}

#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum DomainError {
    #[error("project {project_id} has an empty name")]
    EmptyProjectName { project_id: ProjectId },
    #[error("project {project_id} does not contain a sheet")]
    ProjectHasNoSheets { project_id: ProjectId },
    #[error("project {project_id} has invalid grid spacing {grid_spacing}")]
    InvalidGridSpacing {
        project_id: ProjectId,
        grid_spacing: i64,
    },
    #[error("sheet {sheet_id} appears more than once in project sheet order")]
    DuplicateSheetOrder { sheet_id: SheetId },
    #[error("sheet order refers to missing sheet {sheet_id}")]
    OrderedSheetMissing { sheet_id: SheetId },
    #[error("sheet {sheet_id} is not present in project sheet order")]
    UnorderedSheet { sheet_id: SheetId },
    #[error("entity UUID {entity_id} is used by multiple domain entities")]
    DuplicateEntityId { entity_id: Uuid },
    #[error(
        "symbol definition key {symbol_definition_id} does not match record {symbol_definition_record_id}"
    )]
    SymbolDefinitionKeyMismatch {
        symbol_definition_id: SymbolDefinitionId,
        symbol_definition_record_id: SymbolDefinitionId,
    },
    #[error("sheet key {sheet_id} does not match record {sheet_record_id}")]
    SheetKeyMismatch {
        sheet_id: SheetId,
        sheet_record_id: SheetId,
    },
    #[error(
        "symbol instance key {symbol_instance_id} does not match record {symbol_instance_record_id}"
    )]
    SymbolInstanceKeyMismatch {
        symbol_instance_id: SymbolInstanceId,
        symbol_instance_record_id: SymbolInstanceId,
    },
    #[error("wire key {wire_id} does not match record {wire_record_id}")]
    WireKeyMismatch {
        wire_id: WireId,
        wire_record_id: WireId,
    },
    #[error("junction key {junction_id} does not match record {junction_record_id}")]
    JunctionKeyMismatch {
        junction_id: JunctionId,
        junction_record_id: JunctionId,
    },
    #[error("annotation key {annotation_id} does not match record {annotation_record_id}")]
    AnnotationKeyMismatch {
        annotation_id: AnnotationId,
        annotation_record_id: AnnotationId,
    },
    #[error("symbol definition {symbol_definition_id} already exists")]
    DuplicateSymbolDefinition {
        symbol_definition_id: SymbolDefinitionId,
    },
    #[error("symbol instance {symbol_instance_id} already exists")]
    DuplicateSymbolInstance {
        symbol_instance_id: SymbolInstanceId,
    },
    #[error("wire {wire_id} already exists")]
    DuplicateWire { wire_id: WireId },
    #[error(
        "symbol instance {symbol_instance_id} refers to missing definition {symbol_definition_id}"
    )]
    MissingSymbolDefinition {
        symbol_instance_id: SymbolInstanceId,
        symbol_definition_id: SymbolDefinitionId,
    },
    #[error("terminal map key {terminal_id} does not match terminal record {terminal_record_id}")]
    TerminalKeyMismatch {
        terminal_id: TerminalId,
        terminal_record_id: TerminalId,
    },
    #[error(
        "terminal {terminal_id} is owned by {actual_owner}, expected symbol instance {expected_owner}"
    )]
    TerminalOwnerMismatch {
        terminal_id: TerminalId,
        expected_owner: SymbolInstanceId,
        actual_owner: SymbolInstanceId,
    },
    #[error("wire {wire_id} refers to missing terminal {terminal_id}")]
    MissingTerminal {
        wire_id: WireId,
        terminal_id: TerminalId,
    },
    #[error("wire {wire_id} refers to missing junction {junction_id}")]
    MissingJunction {
        wire_id: WireId,
        junction_id: JunctionId,
    },
    #[error("wire {wire_id} route must contain at least a start and end point")]
    WireRouteTooShort { wire_id: WireId },
    #[error("wire {wire_id} route {endpoint:?} point does not match its owner")]
    WireRouteEndpointMismatch {
        wire_id: WireId,
        endpoint: WireRouteEndpoint,
    },
    #[error("wire {wire_id} route segment {segment_index} has zero length")]
    ZeroLengthWireSegment {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("wire {wire_id} route segment {segment_index} is not orthogonal")]
    NonOrthogonalWireSegment {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("wire {wire_id} route contains redundant bends or points")]
    NonCanonicalWireRoute { wire_id: WireId },
}
