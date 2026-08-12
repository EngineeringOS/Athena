use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{
    Annotation, AnnotationId, DomainError, Junction, JunctionId, SheetId, SymbolInstance,
    SymbolInstanceId, Wire, WireId,
};

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct SheetSettings {
    pub page_width: i64,
    pub page_height: i64,
    pub grid_spacing: i64,
    pub snap_enabled: bool,
}

impl Default for SheetSettings {
    fn default() -> Self {
        Self {
            page_width: 420,
            page_height: 297,
            grid_spacing: 10,
            snap_enabled: true,
        }
    }
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Sheet {
    pub id: SheetId,
    pub name: String,
    #[serde(default)]
    pub settings: SheetSettings,
    #[serde(default)]
    pub symbol_instances: BTreeMap<SymbolInstanceId, SymbolInstance>,
    #[serde(default)]
    pub wires: BTreeMap<WireId, Wire>,
    #[serde(default)]
    pub junctions: BTreeMap<JunctionId, Junction>,
    #[serde(default)]
    pub annotations: BTreeMap<AnnotationId, Annotation>,
}

impl Sheet {
    #[must_use]
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            id: SheetId::new(),
            name: name.into(),
            settings: SheetSettings::default(),
            symbol_instances: BTreeMap::new(),
            wires: BTreeMap::new(),
            junctions: BTreeMap::new(),
            annotations: BTreeMap::new(),
        }
    }

    #[must_use]
    pub fn symbol_instances(&self) -> &BTreeMap<SymbolInstanceId, SymbolInstance> {
        &self.symbol_instances
    }

    #[must_use]
    pub fn symbol_instances_mut(&mut self) -> &mut BTreeMap<SymbolInstanceId, SymbolInstance> {
        &mut self.symbol_instances
    }

    #[must_use]
    pub fn instance(&self, instance_id: SymbolInstanceId) -> Option<&SymbolInstance> {
        self.symbol_instances.get(&instance_id)
    }

    #[must_use]
    pub fn wires(&self) -> &BTreeMap<WireId, Wire> {
        &self.wires
    }

    #[must_use]
    pub fn wire(&self, wire_id: WireId) -> Option<&Wire> {
        self.wires.get(&wire_id)
    }

    #[must_use]
    pub fn junctions(&self) -> &BTreeMap<JunctionId, Junction> {
        &self.junctions
    }

    #[must_use]
    pub fn junction(&self, junction_id: JunctionId) -> Option<&Junction> {
        self.junctions.get(&junction_id)
    }

    #[must_use]
    pub fn annotation(&self, annotation_id: AnnotationId) -> Option<&Annotation> {
        self.annotations.get(&annotation_id)
    }

    pub fn add_symbol(&mut self, symbol: SymbolInstance) -> Result<(), DomainError> {
        if self.symbol_instances.contains_key(&symbol.id) {
            return Err(DomainError::DuplicateSymbolInstance {
                symbol_instance_id: symbol.id,
            });
        }
        self.symbol_instances.insert(symbol.id, symbol);
        Ok(())
    }

    pub fn add_wire(&mut self, wire: Wire) -> Result<(), DomainError> {
        if self.wires.contains_key(&wire.id) {
            return Err(DomainError::DuplicateWire { wire_id: wire.id });
        }
        self.wires.insert(wire.id, wire);
        Ok(())
    }

    pub fn add_junction(&mut self, position: crate::Point) -> JunctionId {
        let junction = Junction::new(position);
        let junction_id = junction.id;
        self.junctions.insert(junction_id, junction);
        junction_id
    }
}
