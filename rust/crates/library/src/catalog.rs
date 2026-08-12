use std::collections::BTreeMap;

use athena_domain::{ElectricalKind, SymbolDefinition, SymbolDefinitionId};
use athena_geometry::{Rect, WorldPoint};
use thiserror::Error;
use uuid::Uuid;

/// Geometry used to display a symbol definition before it becomes a placed instance.
#[derive(Clone, Debug, PartialEq)]
pub enum Primitive {
    Line { start: WorldPoint, end: WorldPoint },
    Rectangle { bounds: Rect },
    Circle { center: WorldPoint, radius: f64 },
}

/// A terminal's local anchor and electrical metadata in a symbol definition.
#[derive(Clone, Debug, PartialEq)]
pub struct TerminalTemplate {
    pub key: String,
    pub name: String,
    pub electrical_kind: ElectricalKind,
    pub position: WorldPoint,
}

impl TerminalTemplate {
    #[must_use]
    pub fn new(
        key: impl Into<String>,
        name: impl Into<String>,
        electrical_kind: ElectricalKind,
        position: WorldPoint,
    ) -> Self {
        Self {
            key: key.into(),
            name: name.into(),
            electrical_kind,
            position,
        }
    }
}

/// A catalog-owned definition that adds drawing data and terminal templates to
/// the persisted-domain metadata.
#[derive(Clone, Debug, PartialEq)]
pub struct SymbolDefinitionRecord {
    pub definition: SymbolDefinition,
    pub terminals: Vec<TerminalTemplate>,
    pub primitives: Vec<Primitive>,
}

impl SymbolDefinitionRecord {
    #[must_use]
    pub fn new(definition: SymbolDefinition) -> Self {
        Self {
            definition,
            terminals: Vec::new(),
            primitives: Vec::new(),
        }
    }

    pub fn register_terminal(&mut self, terminal: TerminalTemplate) {
        self.terminals.push(terminal);
    }

    pub fn register_primitive(&mut self, primitive: Primitive) {
        self.primitives.push(primitive);
    }
}

/// A terminal anchor used while previewing a library symbol before placement.
#[derive(Clone, Debug, PartialEq)]
pub struct PlacementTerminalAnchor {
    pub key: String,
    pub name: String,
    pub electrical_kind: ElectricalKind,
    pub position: WorldPoint,
}

/// Complete read-only data needed to render and connect a placement preview.
#[derive(Clone, Debug, PartialEq)]
pub struct PlacementPreview {
    pub definition_id: SymbolDefinitionId,
    pub primitives: Vec<Primitive>,
    pub terminal_anchors: Vec<PlacementTerminalAnchor>,
}

#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum CatalogError {
    #[error("symbol definition {0} is already registered")]
    DuplicateDefinitionId(SymbolDefinitionId),
}

/// In-memory library of electrical symbol definitions.
#[derive(Clone, Debug, Default)]
pub struct SymbolCatalog {
    definitions: BTreeMap<SymbolDefinitionId, SymbolDefinitionRecord>,
}

impl SymbolCatalog {
    #[must_use]
    pub fn new() -> Self {
        Self::default()
    }

    #[must_use]
    pub fn with_built_ins() -> Self {
        let mut catalog = Self::new();
        for definition in built_in_definitions() {
            catalog
                .register(definition)
                .expect("built-in symbol definition IDs are unique");
        }
        catalog
    }

    pub fn register(&mut self, record: SymbolDefinitionRecord) -> Result<(), CatalogError> {
        let definition_id = record.definition.id;
        if self.definitions.contains_key(&definition_id) {
            return Err(CatalogError::DuplicateDefinitionId(definition_id));
        }

        self.definitions.insert(definition_id, record);
        Ok(())
    }

    #[must_use]
    pub fn get(&self, definition_id: SymbolDefinitionId) -> Option<&SymbolDefinitionRecord> {
        self.definitions.get(&definition_id)
    }

    #[must_use]
    pub fn placement_preview(&self, definition_id: SymbolDefinitionId) -> Option<PlacementPreview> {
        self.get(definition_id).map(|record| PlacementPreview {
            definition_id,
            primitives: record.primitives.clone(),
            terminal_anchors: record
                .terminals
                .iter()
                .map(|terminal| PlacementTerminalAnchor {
                    key: terminal.key.clone(),
                    name: terminal.name.clone(),
                    electrical_kind: terminal.electrical_kind,
                    position: terminal.position,
                })
                .collect(),
        })
    }

    pub(crate) fn records(&self) -> impl Iterator<Item = &SymbolDefinitionRecord> {
        self.definitions.values()
    }
}

const RESISTOR_NAME: &str = "Resistor";
const SWITCH_NAME: &str = "Switch";
const LAMP_NAME: &str = "Lamp";
const MOTOR_NAME: &str = "Motor";
const CONNECTOR_NAME: &str = "Connector";
const POWER_TERMINAL_NAME: &str = "Power terminal";
const RESISTOR_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347001";
const SWITCH_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347002";
const LAMP_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347003";
const MOTOR_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347004";
const CONNECTOR_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347005";
const POWER_TERMINAL_ID: &str = "5e16fd8e-33aa-43f2-bce3-65cbaf347006";

fn built_in_definitions() -> [SymbolDefinitionRecord; 6] {
    [
        two_terminal_record(
            RESISTOR_ID,
            RESISTOR_NAME,
            &["passive", "resistance"],
            Primitive::Rectangle {
                bounds: Rect::from_corners(
                    WorldPoint::new(-10.0, -5.0),
                    WorldPoint::new(10.0, 5.0),
                ),
            },
            ElectricalKind::Passive,
        ),
        two_terminal_record(
            SWITCH_ID,
            SWITCH_NAME,
            &["switching", "contact"],
            Primitive::Line {
                start: WorldPoint::new(-8.0, 0.0),
                end: WorldPoint::new(8.0, -8.0),
            },
            ElectricalKind::Passive,
        ),
        two_terminal_record(
            LAMP_ID,
            LAMP_NAME,
            &["indicator", "light"],
            Primitive::Circle {
                center: WorldPoint::default(),
                radius: 10.0,
            },
            ElectricalKind::Passive,
        ),
        two_terminal_record(
            MOTOR_ID,
            MOTOR_NAME,
            &["actuator", "power"],
            Primitive::Circle {
                center: WorldPoint::default(),
                radius: 12.0,
            },
            ElectricalKind::Passive,
        ),
        connector_record(CONNECTOR_ID),
        power_terminal_record(POWER_TERMINAL_ID),
    ]
}

fn two_terminal_record(
    id: &str,
    name: &str,
    tags: &[&str],
    primitive: Primitive,
    electrical_kind: ElectricalKind,
) -> SymbolDefinitionRecord {
    let mut record = record(id, name, tags);
    record.register_terminal(TerminalTemplate::new(
        "left",
        "1",
        electrical_kind,
        WorldPoint::new(-20.0, 0.0),
    ));
    record.register_terminal(TerminalTemplate::new(
        "right",
        "2",
        electrical_kind,
        WorldPoint::new(20.0, 0.0),
    ));
    record.register_primitive(primitive);
    record
}

fn connector_record(id: &str) -> SymbolDefinitionRecord {
    let mut record = record(id, CONNECTOR_NAME, &["connector", "terminal"]);
    record.register_terminal(TerminalTemplate::new(
        "pin-1",
        "1",
        ElectricalKind::Input,
        WorldPoint::new(0.0, 10.0),
    ));
    record.register_terminal(TerminalTemplate::new(
        "pin-2",
        "2",
        ElectricalKind::Input,
        WorldPoint::new(0.0, -10.0),
    ));
    record.register_primitive(Primitive::Rectangle {
        bounds: Rect::from_corners(WorldPoint::new(-6.0, -14.0), WorldPoint::new(6.0, 14.0)),
    });
    record
}

fn power_terminal_record(id: &str) -> SymbolDefinitionRecord {
    let mut record = record(id, POWER_TERMINAL_NAME, &["power", "supply"]);
    record.register_terminal(TerminalTemplate::new(
        "power",
        "PWR",
        ElectricalKind::Power,
        WorldPoint::new(0.0, 0.0),
    ));
    record.register_primitive(Primitive::Line {
        start: WorldPoint::new(0.0, 0.0),
        end: WorldPoint::new(0.0, -16.0),
    });
    record
}

fn record(id: &str, name: &str, tags: &[&str]) -> SymbolDefinitionRecord {
    let mut definition = SymbolDefinition::new(name);
    definition.id = SymbolDefinitionId::from_uuid(
        Uuid::parse_str(id).expect("built-in definition IDs must be valid UUIDs"),
    );
    definition.tags = tags.iter().map(|tag| (*tag).to_owned()).collect();
    SymbolDefinitionRecord::new(definition)
}
