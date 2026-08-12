use athena_domain::{ElectricalKind, SymbolDefinition};
use athena_geometry::{Rect, WorldPoint};
use athena_library::{
    Primitive, SearchQuery, SymbolCatalog, SymbolDefinitionRecord, TerminalTemplate,
};

fn record(name: &str, tags: &[&str]) -> SymbolDefinitionRecord {
    let mut definition = SymbolDefinition::new(name);
    definition.tags = tags.iter().map(ToString::to_string).collect();

    SymbolDefinitionRecord::new(definition)
}

#[test]
fn registers_definition_with_terminals_and_primitives() {
    let mut catalog = SymbolCatalog::new();
    let mut resistor = record("Resistor", &["passive", "iec"]);
    resistor.register_terminal(TerminalTemplate::new(
        "left",
        "1",
        ElectricalKind::Passive,
        WorldPoint::new(-20.0, 0.0),
    ));
    resistor.register_terminal(TerminalTemplate::new(
        "right",
        "2",
        ElectricalKind::Passive,
        WorldPoint::new(20.0, 0.0),
    ));
    resistor.register_primitive(Primitive::Rectangle {
        bounds: Rect::from_corners(WorldPoint::new(-10.0, -5.0), WorldPoint::new(10.0, 5.0)),
    });
    let resistor_id = resistor.definition.id;

    catalog.register(resistor).unwrap();

    let registered = catalog.get(resistor_id).unwrap();
    assert_eq!(registered.terminals.len(), 2);
    assert_eq!(registered.primitives.len(), 1);
}

#[test]
fn rejects_duplicate_definition_ids() {
    let mut catalog = SymbolCatalog::new();
    let first = record("Resistor", &["passive"]);
    let duplicate = SymbolDefinitionRecord::new(first.definition.clone());

    catalog.register(first).unwrap();

    assert!(catalog.register(duplicate).is_err());
}

#[test]
fn searches_names_and_tags_case_insensitively_in_deterministic_order() {
    let mut catalog = SymbolCatalog::new();
    catalog
        .register(record("Zener diode", &["semiconductor"]))
        .unwrap();
    catalog
        .register(record("Lamp", &["indicator", "light"]))
        .unwrap();
    catalog
        .register(record("Motor", &["power", "LIGHT duty"]))
        .unwrap();

    let names = catalog
        .search(&SearchQuery::new("LiGhT"))
        .iter()
        .map(|result| result.definition.name.as_str())
        .collect::<Vec<_>>();

    assert_eq!(names, vec!["Lamp", "Motor"]);
}

#[test]
fn placement_preview_contains_every_terminal_anchor() {
    let mut catalog = SymbolCatalog::new();
    let mut connector = record("Connector", &["terminal"]);
    connector.register_terminal(TerminalTemplate::new(
        "pin-1",
        "1",
        ElectricalKind::Input,
        WorldPoint::new(0.0, 10.0),
    ));
    connector.register_terminal(TerminalTemplate::new(
        "pin-2",
        "2",
        ElectricalKind::Output,
        WorldPoint::new(0.0, -10.0),
    ));
    let connector_id = connector.definition.id;
    catalog.register(connector).unwrap();

    let preview = catalog.placement_preview(connector_id).unwrap();

    assert_eq!(preview.definition_id, connector_id);
    assert_eq!(preview.terminal_anchors.len(), 2);
    assert_eq!(preview.terminal_anchors[0].key, "pin-1");
    assert_eq!(
        preview.terminal_anchors[0].position,
        WorldPoint::new(0.0, 10.0)
    );
    assert_eq!(preview.terminal_anchors[1].key, "pin-2");
    assert_eq!(
        preview.terminal_anchors[1].position,
        WorldPoint::new(0.0, -10.0)
    );
}

#[test]
fn built_in_catalog_contains_the_mvp_electrical_definitions() {
    let catalog = SymbolCatalog::with_built_ins();

    for name in [
        "Resistor",
        "Switch",
        "Lamp",
        "Motor",
        "Connector",
        "Power terminal",
    ] {
        assert_eq!(catalog.search(&SearchQuery::new(name)).len(), 1, "{name}");
    }
}

#[test]
fn built_in_definition_ids_are_stable_across_catalogs() {
    let first = SymbolCatalog::with_built_ins();
    let second = SymbolCatalog::with_built_ins();

    let first_id = first.search(&SearchQuery::new("Resistor"))[0].definition.id;
    let second_id = second.search(&SearchQuery::new("Resistor"))[0]
        .definition
        .id;

    assert_eq!(first_id, second_id);
}
