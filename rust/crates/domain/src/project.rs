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
    Annotation, AnnotationId, Folio, FolioId, Junction, JunctionId, Point, ProjectId,
    SymbolDefinition, SymbolDefinitionId, SymbolInstance, SymbolInstanceId, Terminal, TerminalId,
    TitleBlockValues, Wire, WireEndpoint, WireId, canonical_wire_route,
    variables::normalize_variable_key,
};

/// Project-wide defaults persisted alongside schematic folios.
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

/// Values copied into each newly created folio.
#[derive(Clone, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub struct ProjectFolioDefaults {
    /// Title-block values copied into each newly created folio.
    pub title_block: TitleBlockValues,
    /// Folio-scoped variables copied into each newly created folio.
    pub variables: BTreeMap<String, String>,
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
    pub folio_defaults: ProjectFolioDefaults,
    #[serde(default)]
    pub variables: BTreeMap<String, String>,
    #[serde(default)]
    pub folios: BTreeMap<FolioId, Folio>,
    #[serde(default)]
    pub folio_order: Vec<FolioId>,
}

impl Project {
    /// Creates a named project with one default folio.
    #[must_use]
    pub fn new(name: impl Into<String>) -> Self {
        let mut project = Self {
            id: ProjectId::new(),
            name: name.into(),
            settings: ProjectSettings::default(),
            symbol_definitions: BTreeMap::new(),
            folio_defaults: ProjectFolioDefaults::default(),
            variables: BTreeMap::new(),
            folios: BTreeMap::new(),
            folio_order: Vec::new(),
        };
        project
            .add_folio("Folio 1")
            .expect("built-in folio label is valid");
        project
    }

    /// Returns folios keyed by their stable IDs in deterministic order.
    #[must_use]
    pub fn folios(&self) -> &BTreeMap<FolioId, Folio> {
        &self.folios
    }

    /// Returns the user-facing folio order independently of map-key order.
    #[must_use]
    pub fn folio_order(&self) -> &[FolioId] {
        &self.folio_order
    }

    /// Returns reusable symbol definitions keyed by stable IDs.
    #[must_use]
    pub fn symbol_definitions(&self) -> &BTreeMap<SymbolDefinitionId, SymbolDefinition> {
        &self.symbol_definitions
    }

    /// Looks up a folio by its stable ID.
    #[must_use]
    pub fn folio(&self, folio_id: FolioId) -> Option<&Folio> {
        self.folios.get(&folio_id)
    }

    /// Returns mutable access to one folio for aggregate-scoped mutation.
    pub fn folio_mut(&mut self, folio_id: FolioId) -> Option<&mut Folio> {
        self.folios.get_mut(&folio_id)
    }

    /// Looks up a reusable symbol definition by its stable ID.
    #[must_use]
    pub fn symbol_definition(
        &self,
        definition_id: SymbolDefinitionId,
    ) -> Option<&SymbolDefinition> {
        self.symbol_definitions.get(&definition_id)
    }

    /// Looks up a symbol instance within one folio.
    #[must_use]
    pub fn symbol_instance(
        &self,
        folio_id: FolioId,
        instance_id: SymbolInstanceId,
    ) -> Option<&SymbolInstance> {
        self.folio(folio_id)?.instance(instance_id)
    }

    /// Looks up a terminal within one folio's symbol instances.
    #[must_use]
    pub fn terminal(&self, folio_id: FolioId, terminal_id: TerminalId) -> Option<&Terminal> {
        self.folio(folio_id)?
            .symbol_instances()
            .values()
            .find_map(|instance| instance.terminals.get(&terminal_id))
    }

    /// Looks up a wire within one folio.
    #[must_use]
    pub fn wire(&self, folio_id: FolioId, wire_id: WireId) -> Option<&Wire> {
        self.folio(folio_id)?.wire(wire_id)
    }

    /// Looks up a junction within one folio.
    #[must_use]
    pub fn junction(&self, folio_id: FolioId, junction_id: JunctionId) -> Option<&Junction> {
        self.folio(folio_id)?.junction(junction_id)
    }

    /// Looks up an annotation within one folio.
    #[must_use]
    pub fn annotation(
        &self,
        folio_id: FolioId,
        annotation_id: AnnotationId,
    ) -> Option<&Annotation> {
        self.folio(folio_id)?.annotation(annotation_id)
    }

    /// Appends a new folio by copying the current project folio defaults.
    pub fn add_folio(&mut self, label: impl Into<String>) -> Result<FolioId, DomainError> {
        let label = label.into();
        if label.trim().is_empty() {
            return Err(DomainError::EmptyFolioLabel);
        }
        let folio = Folio::new(
            label,
            self.folio_defaults.title_block.clone(),
            self.folio_defaults.variables.clone(),
        );
        let folio_id = folio.id;
        self.folios.insert(folio_id, folio);
        self.folio_order.push(folio_id);
        Ok(folio_id)
    }

    /// Moves a folio to an explicit zero-based order position.
    pub fn move_folio(&mut self, folio_id: FolioId, position: usize) -> Result<(), DomainError> {
        if !self.folios.contains_key(&folio_id) {
            return Err(DomainError::UnknownFolio { folio_id });
        }
        if position >= self.folio_order.len() {
            return Err(DomainError::InvalidFolioPosition { position });
        }
        self.folio_order.retain(|id| *id != folio_id);
        self.folio_order.insert(position, folio_id);
        Ok(())
    }

    /// Removes a folio unless it is the sole remaining folio.
    pub fn remove_folio(&mut self, folio_id: FolioId) -> Option<Folio> {
        if self.folios.len() <= 1 {
            return None;
        }
        let removed = self.folios.remove(&folio_id)?;
        self.folio_order.retain(|id| *id != folio_id);
        Some(removed)
    }

    /// Sets a project-scoped variable after canonical key validation.
    pub fn set_project_variable(
        &mut self,
        key: &str,
        value: impl Into<String>,
    ) -> Result<(), DomainError> {
        let key = normalize_variable_key(key).ok_or_else(|| DomainError::InvalidVariableKey {
            key: key.to_owned(),
        })?;
        self.variables.insert(key, value.into());
        Ok(())
    }

    /// Sets a folio-scoped variable after canonical key validation.
    pub fn set_folio_variable(
        &mut self,
        folio_id: FolioId,
        key: &str,
        value: impl Into<String>,
    ) -> Result<(), DomainError> {
        let key = normalize_variable_key(key).ok_or_else(|| DomainError::InvalidVariableKey {
            key: key.to_owned(),
        })?;
        let folio = self
            .folio_mut(folio_id)
            .ok_or(DomainError::UnknownFolio { folio_id })?;
        folio.variables.insert(key, value.into());
        Ok(())
    }

    /// Adds a reusable symbol definition when its ID is not already present.
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
        if self.folios.is_empty() {
            return Err(DomainError::ProjectHasNoFolios {
                project_id: self.id,
            });
        }
        if self.settings.grid_spacing <= 0 {
            return Err(DomainError::InvalidGridSpacing {
                project_id: self.id,
                grid_spacing: self.settings.grid_spacing,
            });
        }

        validate_variable_keys(&self.variables)?;
        validate_variable_keys(&self.folio_defaults.variables)?;

        let mut seen_folios = BTreeSet::new();
        for folio_id in &self.folio_order {
            if !seen_folios.insert(*folio_id) {
                return Err(DomainError::DuplicateFolioOrder {
                    folio_id: *folio_id,
                });
            }
            if !self.folios.contains_key(folio_id) {
                return Err(DomainError::OrderedFolioMissing {
                    folio_id: *folio_id,
                });
            }
        }
        for folio_id in self.folios.keys() {
            if !seen_folios.contains(folio_id) {
                return Err(DomainError::UnorderedFolio {
                    folio_id: *folio_id,
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

        for (folio_id, folio) in &self.folios {
            if folio.id != *folio_id {
                return Err(DomainError::FolioKeyMismatch {
                    folio_id: *folio_id,
                    folio_record_id: folio.id,
                });
            }
            if folio.label.trim().is_empty() {
                return Err(DomainError::EmptyFolioLabel);
            }
            if folio.schematic.settings.page_width <= 0 || folio.schematic.settings.page_height <= 0
            {
                return Err(DomainError::InvalidFolioPageSize {
                    folio_id: folio.id,
                    page_width: folio.schematic.settings.page_width,
                    page_height: folio.schematic.settings.page_height,
                });
            }
            if folio.schematic.settings.grid_spacing <= 0 {
                return Err(DomainError::InvalidFolioGridSpacing {
                    folio_id: folio.id,
                    grid_spacing: folio.schematic.settings.grid_spacing,
                });
            }
            validate_variable_keys(&folio.variables)?;
            self.ensure_unique_uuid(folio.id.as_uuid(), &mut all_ids)?;
            let mut terminal_ids = BTreeSet::new();
            for (instance_id, instance) in &folio.schematic.symbol_instances {
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
            for (junction_id, junction) in &folio.schematic.junctions {
                if junction.id != *junction_id {
                    return Err(DomainError::JunctionKeyMismatch {
                        junction_id: *junction_id,
                        junction_record_id: junction.id,
                    });
                }
                self.ensure_unique_uuid(junction.id.as_uuid(), &mut all_ids)?;
            }
            for (annotation_id, annotation) in &folio.schematic.annotations {
                if annotation.id != *annotation_id {
                    return Err(DomainError::AnnotationKeyMismatch {
                        annotation_id: *annotation_id,
                        annotation_record_id: annotation.id,
                    });
                }
                self.ensure_unique_uuid(annotation.id.as_uuid(), &mut all_ids)?;
            }
            for (wire_id, wire) in &folio.schematic.wires {
                if wire.id != *wire_id {
                    return Err(DomainError::WireKeyMismatch {
                        wire_id: *wire_id,
                        wire_record_id: wire.id,
                    });
                }
                self.ensure_unique_uuid(wire.id.as_uuid(), &mut all_ids)?;
                self.validate_wire_endpoint(wire.id, &wire.start, &terminal_ids, folio)?;
                self.validate_wire_endpoint(wire.id, &wire.end, &terminal_ids, folio)?;
                self.validate_wire_route(wire, folio, &terminal_ids)?;
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
        folio: &Folio,
    ) -> Result<(), DomainError> {
        match endpoint {
            WireEndpoint::Terminal(terminal_id) if !terminal_ids.contains(terminal_id) => {
                Err(DomainError::MissingTerminal {
                    wire_id,
                    terminal_id: *terminal_id,
                })
            }
            WireEndpoint::Junction(junction_id)
                if !folio.schematic.junctions.contains_key(junction_id) =>
            {
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
        folio: &Folio,
        terminal_ids: &BTreeSet<TerminalId>,
    ) -> Result<(), DomainError> {
        if wire.route.len() < 2 {
            return Err(DomainError::WireRouteTooShort { wire_id: wire.id });
        }
        let start = endpoint_position(&wire.start, folio, terminal_ids)
            .expect("validated wire start endpoint resolves");
        let end = endpoint_position(&wire.end, folio, terminal_ids)
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
    folio: &Folio,
    terminal_ids: &BTreeSet<TerminalId>,
) -> Option<Point> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id) if terminal_ids.contains(terminal_id) => folio
            .schematic
            .symbol_instances
            .values()
            .find_map(|symbol| {
                symbol
                    .terminals
                    .get(terminal_id)
                    .map(|terminal| terminal.position)
            }),
        WireEndpoint::Junction(junction_id) => folio
            .schematic
            .junctions
            .get(junction_id)
            .map(|junction| junction.position),
        _ => None,
    }
}

fn validate_variable_keys(variables: &BTreeMap<String, String>) -> Result<(), DomainError> {
    for key in variables.keys() {
        if normalize_variable_key(key).as_deref() != Some(key.as_str()) {
            return Err(DomainError::InvalidVariableKey { key: key.clone() });
        }
    }
    Ok(())
}

#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum DomainError {
    #[error("project {project_id} has an empty name")]
    EmptyProjectName { project_id: ProjectId },
    #[error("project {project_id} does not contain a folio")]
    ProjectHasNoFolios { project_id: ProjectId },
    #[error("project {project_id} has invalid grid spacing {grid_spacing}")]
    InvalidGridSpacing {
        project_id: ProjectId,
        grid_spacing: i64,
    },
    #[error("folio label must not be empty")]
    EmptyFolioLabel,
    #[error("folio {folio_id} does not exist")]
    UnknownFolio { folio_id: FolioId },
    #[error("folio position {position} is outside the project order")]
    InvalidFolioPosition { position: usize },
    #[error("variable key {key:?} is empty, contains control characters, or is not canonical")]
    InvalidVariableKey { key: String },
    #[error("folio {folio_id} appears more than once in project folio order")]
    DuplicateFolioOrder { folio_id: FolioId },
    #[error("folio order refers to missing folio {folio_id}")]
    OrderedFolioMissing { folio_id: FolioId },
    #[error("folio {folio_id} is not present in project folio order")]
    UnorderedFolio { folio_id: FolioId },
    #[error("entity UUID {entity_id} is used by multiple domain entities")]
    DuplicateEntityId { entity_id: Uuid },
    #[error(
        "symbol definition key {symbol_definition_id} does not match record {symbol_definition_record_id}"
    )]
    SymbolDefinitionKeyMismatch {
        symbol_definition_id: SymbolDefinitionId,
        symbol_definition_record_id: SymbolDefinitionId,
    },
    #[error("folio key {folio_id} does not match record {folio_record_id}")]
    FolioKeyMismatch {
        folio_id: FolioId,
        folio_record_id: FolioId,
    },
    #[error("folio {folio_id} has invalid page size {page_width}x{page_height}")]
    InvalidFolioPageSize {
        folio_id: FolioId,
        page_width: i64,
        page_height: i64,
    },
    #[error("folio {folio_id} has invalid grid spacing {grid_spacing}")]
    InvalidFolioGridSpacing {
        folio_id: FolioId,
        grid_spacing: i64,
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
