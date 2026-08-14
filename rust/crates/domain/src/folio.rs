//! Electrical folio ownership and its embedded schematic payload.
//!
//! A folio is the sole project-page identity. Its title-block data, custom
//! variables, page settings, and electrical entities travel together.

use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{
    Annotation, AnnotationId, DomainError, FolioId, Junction, JunctionId, SymbolInstance,
    SymbolInstanceId, TitleBlockValues, Wire, WireId,
};

/// Durable page and grid settings for a folio's schematic payload.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SchematicSettings {
    /// Persisted page width in document units.
    pub page_width: i64,
    /// Persisted page height in document units.
    pub page_height: i64,
    /// Distance between adjacent schematic grid points.
    pub grid_spacing: i64,
    /// Whether the grid is rendered in the editor viewport.
    #[serde(default = "default_grid_visible")]
    pub grid_visible: bool,
    /// Whether pointer-derived coordinates snap to the grid.
    pub snap_enabled: bool,
}

const fn default_grid_visible() -> bool {
    true
}

impl Default for SchematicSettings {
    fn default() -> Self {
        Self {
            page_width: 420,
            page_height: 297,
            grid_spacing: 10,
            grid_visible: true,
            snap_enabled: true,
        }
    }
}

/// Platform-neutral electrical entities drawn on one folio.
#[derive(Clone, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub struct SchematicContent {
    /// Folio-local page and grid settings.
    #[serde(default)]
    pub settings: SchematicSettings,
    /// Placed electrical symbols keyed by stable identity.
    #[serde(default)]
    pub symbol_instances: BTreeMap<SymbolInstanceId, SymbolInstance>,
    /// Electrical connections keyed by stable identity.
    #[serde(default)]
    pub wires: BTreeMap<WireId, Wire>,
    /// Explicit wire junctions keyed by stable identity.
    #[serde(default)]
    pub junctions: BTreeMap<JunctionId, Junction>,
    /// Free annotations keyed by stable identity.
    #[serde(default)]
    pub annotations: BTreeMap<AnnotationId, Annotation>,
}

/// One ordered electrical project page with stable identity.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Folio {
    /// Stable page identity used by commands, persistence, and layout state.
    pub id: FolioId,
    /// User-facing folio label independent of title-block page number.
    pub label: String,
    /// Persisted title-block template selection and values.
    pub title_block: TitleBlockValues,
    /// Folio-scoped variables available to typed template references.
    #[serde(default)]
    pub variables: BTreeMap<String, String>,
    /// Electrical entities and geometry owned by this folio.
    #[serde(default)]
    pub schematic: SchematicContent,
}

impl Folio {
    /// Creates a folio from already-copied project defaults.
    #[must_use]
    pub fn new(
        label: impl Into<String>,
        title_block: TitleBlockValues,
        variables: BTreeMap<String, String>,
    ) -> Self {
        Self {
            id: FolioId::new(),
            label: label.into(),
            title_block,
            variables,
            schematic: SchematicContent::default(),
        }
    }

    #[must_use]
    /// Returns placed symbols keyed by stable identity.
    pub fn symbol_instances(&self) -> &BTreeMap<SymbolInstanceId, SymbolInstance> {
        &self.schematic.symbol_instances
    }

    #[must_use]
    /// Returns mutable symbol storage for aggregate-scoped editing.
    pub fn symbol_instances_mut(&mut self) -> &mut BTreeMap<SymbolInstanceId, SymbolInstance> {
        &mut self.schematic.symbol_instances
    }

    #[must_use]
    /// Looks up one placed symbol by stable identity.
    pub fn instance(&self, instance_id: SymbolInstanceId) -> Option<&SymbolInstance> {
        self.schematic.symbol_instances.get(&instance_id)
    }

    #[must_use]
    /// Returns electrical wires keyed by stable identity.
    pub fn wires(&self) -> &BTreeMap<WireId, Wire> {
        &self.schematic.wires
    }

    #[must_use]
    /// Looks up one wire by stable identity.
    pub fn wire(&self, wire_id: WireId) -> Option<&Wire> {
        self.schematic.wires.get(&wire_id)
    }

    #[must_use]
    /// Returns explicit junctions keyed by stable identity.
    pub fn junctions(&self) -> &BTreeMap<JunctionId, Junction> {
        &self.schematic.junctions
    }

    #[must_use]
    /// Looks up one junction by stable identity.
    pub fn junction(&self, junction_id: JunctionId) -> Option<&Junction> {
        self.schematic.junctions.get(&junction_id)
    }

    #[must_use]
    /// Looks up one annotation by stable identity.
    pub fn annotation(&self, annotation_id: AnnotationId) -> Option<&Annotation> {
        self.schematic.annotations.get(&annotation_id)
    }

    /// Adds a placed symbol when its stable identity is unique in the folio.
    pub fn add_symbol(&mut self, symbol: SymbolInstance) -> Result<(), DomainError> {
        if self.schematic.symbol_instances.contains_key(&symbol.id) {
            return Err(DomainError::DuplicateSymbolInstance {
                symbol_instance_id: symbol.id,
            });
        }
        self.schematic.symbol_instances.insert(symbol.id, symbol);
        Ok(())
    }

    /// Adds a wire when its stable identity is unique in the folio.
    pub fn add_wire(&mut self, wire: Wire) -> Result<(), DomainError> {
        if self.schematic.wires.contains_key(&wire.id) {
            return Err(DomainError::DuplicateWire { wire_id: wire.id });
        }
        self.schematic.wires.insert(wire.id, wire);
        Ok(())
    }

    /// Creates and inserts an explicit junction at a document-space point.
    pub fn add_junction(&mut self, position: crate::Point) -> JunctionId {
        let junction = Junction::new(position);
        let junction_id = junction.id;
        self.schematic.junctions.insert(junction_id, junction);
        junction_id
    }
}
