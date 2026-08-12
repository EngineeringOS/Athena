use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{ElectricalKind, FieldValue, Point, SymbolDefinitionId, SymbolInstanceId, TerminalId};

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SymbolDefinition {
    pub id: SymbolDefinitionId,
    pub name: String,
    #[serde(default)]
    pub description: String,
    #[serde(default)]
    pub tags: Vec<String>,
    #[serde(default)]
    pub default_fields: BTreeMap<String, FieldValue>,
}

impl SymbolDefinition {
    #[must_use]
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            id: SymbolDefinitionId::new(),
            name: name.into(),
            description: String::new(),
            tags: Vec::new(),
            default_fields: BTreeMap::new(),
        }
    }
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Terminal {
    pub id: TerminalId,
    pub owner: SymbolInstanceId,
    pub name: String,
    pub electrical_kind: ElectricalKind,
    pub position: Point,
}

impl Terminal {
    #[must_use]
    pub fn new(
        owner: SymbolInstanceId,
        name: impl Into<String>,
        electrical_kind: ElectricalKind,
        position: Point,
    ) -> Self {
        Self {
            id: TerminalId::new(),
            owner,
            name: name.into(),
            electrical_kind,
            position,
        }
    }
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SymbolInstance {
    pub id: SymbolInstanceId,
    pub definition_id: SymbolDefinitionId,
    pub position: Point,
    pub rotation_quarter_turns: u8,
    pub mirrored: bool,
    #[serde(default)]
    pub fields: BTreeMap<String, FieldValue>,
    #[serde(default)]
    pub terminals: BTreeMap<TerminalId, Terminal>,
}

impl SymbolInstance {
    #[must_use]
    pub fn new(definition_id: SymbolDefinitionId) -> Self {
        Self {
            id: SymbolInstanceId::new(),
            definition_id,
            position: Point::default(),
            rotation_quarter_turns: 0,
            mirrored: false,
            fields: BTreeMap::new(),
            terminals: BTreeMap::new(),
        }
    }

    pub fn add_terminal(&mut self, terminal: Terminal) -> TerminalId {
        let terminal_id = terminal.id;
        self.terminals.insert(terminal_id, terminal);
        terminal_id
    }
}
