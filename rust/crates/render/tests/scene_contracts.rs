use std::collections::BTreeSet;

use athena_domain::{
    Annotation, ElectricalKind, Point, Project, SymbolDefinition, SymbolInstance, Terminal, Wire,
    WireEndpoint,
};
use athena_geometry::WorldPoint;
use athena_render::{
    DrawPrimitive, EditorPresentation, HitRegion, Overlay, PresentationItemId, Scene,
    SceneLayerKind, Viewport, hit_test, project_folio,
};

fn project_with_symbol_and_wire() -> (Project, athena_domain::FolioId) {
    let mut project = Project::new("Motor control");
    let folio_id = project.folio_order()[0];
    let definition = SymbolDefinition::new("Resistor");
    let definition_id = definition.id;
    project
        .add_symbol_definition(definition)
        .expect("definition ID is unique");

    let mut first = SymbolInstance::new(definition_id);
    first.position = Point::new(0, 0);
    let first_terminal_id = first.add_terminal(Terminal::new(
        first.id,
        "1",
        ElectricalKind::Passive,
        Point::new(0, 0),
    ));
    let first_id = first.id;

    let mut second = SymbolInstance::new(definition_id);
    second.position = Point::new(40, 0);
    let second_terminal_id = second.add_terminal(Terminal::new(
        second.id,
        "2",
        ElectricalKind::Passive,
        Point::new(40, 0),
    ));

    let wire = Wire::new(
        WireEndpoint::Terminal(first_terminal_id),
        WireEndpoint::Terminal(second_terminal_id),
        vec![Point::new(0, 0), Point::new(20, 0), Point::new(40, 0)],
    );
    let annotation = Annotation::new("R1", Point::new(0, 20));

    let folio = project.folio_mut(folio_id).expect("initial folio exists");
    folio.add_symbol(first).expect("first instance is unique");
    folio.add_symbol(second).expect("second instance is unique");
    folio.add_wire(wire).expect("wire ID is unique");
    folio.annotations.insert(annotation.id, annotation);

    assert!(folio.symbol_instances.contains_key(&first_id));
    (project, folio_id)
}

fn selected_wire_scene_fixture() -> Scene {
    let (project, folio_id) = project_with_symbol_and_wire();
    let wire_id = *project
        .folio(folio_id)
        .expect("folio exists")
        .wires
        .keys()
        .next()
        .expect("fixture contains a wire");
    project_folio(
        &project,
        folio_id,
        &EditorPresentation::with_selected_wire(wire_id),
    )
    .expect("folio exists")
}

#[test]
fn projection_uses_a_deterministic_editor_layer_order() {
    let (project, folio_id) = project_with_symbol_and_wire();

    let scene =
        project_folio(&project, folio_id, &EditorPresentation::default()).expect("folio exists");

    assert_eq!(
        scene
            .layers
            .iter()
            .map(|layer| layer.kind)
            .collect::<Vec<_>>(),
        vec![
            SceneLayerKind::PageAndGrid,
            SceneLayerKind::WiresAndJunctions,
            SceneLayerKind::Symbols,
            SceneLayerKind::FieldsAndAnnotations,
            SceneLayerKind::Overlays,
        ]
    );
}

#[test]
fn symbol_terminals_produce_connection_handles() {
    let (project, folio_id) = project_with_symbol_and_wire();

    let scene =
        project_folio(&project, folio_id, &EditorPresentation::default()).expect("folio exists");

    let handle_count = scene
        .layers
        .iter()
        .flat_map(|layer| &layer.primitives)
        .filter(|primitive| matches!(primitive, DrawPrimitive::ConnectionHandle { .. }))
        .count();
    let terminal_count = project
        .folio(folio_id)
        .expect("folio exists")
        .symbol_instances
        .values()
        .map(|symbol| symbol.terminals.len())
        .sum::<usize>();

    assert_eq!(handle_count, terminal_count);
    assert_eq!(
        scene
            .hit_regions
            .iter()
            .filter(|region| matches!(region, HitRegion::Terminal { .. }))
            .count(),
        terminal_count
    );
}

#[test]
fn wire_routes_produce_a_hit_region_for_each_segment() {
    let (project, folio_id) = project_with_symbol_and_wire();

    let scene =
        project_folio(&project, folio_id, &EditorPresentation::default()).expect("folio exists");

    assert_eq!(
        scene
            .hit_regions
            .iter()
            .filter(|region| matches!(region, HitRegion::WireSegment { .. }))
            .count(),
        2
    );
}

#[test]
fn selection_overlay_does_not_mutate_the_saved_project() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let before = project.clone();
    let selected_symbol = *project
        .folio(folio_id)
        .expect("folio exists")
        .symbol_instances
        .keys()
        .next()
        .expect("fixture contains a symbol");
    let presentation = EditorPresentation {
        selected: BTreeSet::from([PresentationItemId::Symbol(selected_symbol)]),
        ..EditorPresentation::default()
    };

    let scene = project_folio(&project, folio_id, &presentation).expect("folio exists");

    assert_eq!(project, before);
    assert!(scene.layers.iter().any(|layer| {
        layer.kind == SceneLayerKind::Overlays
            && layer
                .primitives
                .iter()
                .any(|primitive| matches!(primitive, DrawPrimitive::Overlay { .. }))
    }));
}

#[test]
fn equal_project_snapshots_project_to_equal_scenes() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let presentation = EditorPresentation::default();

    let first = project_folio(&project, folio_id, &presentation).expect("folio exists");
    let second = project_folio(&project.clone(), folio_id, &presentation).expect("folio exists");

    assert_eq!(first, second);
}

#[test]
fn terminals_win_over_symbol_bodies_when_regions_overlap() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let scene =
        project_folio(&project, folio_id, &EditorPresentation::default()).expect("folio exists");

    assert!(matches!(
        hit_test(&scene, WorldPoint::new(0.0, 0.0), 1.0),
        Some(HitRegion::Terminal { .. })
    ));
}

#[test]
fn wire_vertices_win_over_wire_segments_when_regions_overlap() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let wire_id = *project
        .folio(folio_id)
        .expect("folio exists")
        .wires
        .keys()
        .next()
        .expect("fixture contains a wire");
    let scene = project_folio(
        &project,
        folio_id,
        &EditorPresentation::with_selected_wire(wire_id),
    )
    .expect("folio exists");

    assert!(matches!(
        hit_test(&scene, WorldPoint::new(20.0, 0.0), 1.0),
        Some(HitRegion::WireVertex {
            vertex_index: 1,
            ..
        })
    ));
}

#[test]
fn selected_wire_projects_highlight_and_vertex_overlays() {
    let scene = selected_wire_scene_fixture();

    assert!(
        scene
            .layers
            .iter()
            .flat_map(|layer| &layer.primitives)
            .any(|primitive| matches!(
                primitive,
                DrawPrimitive::Overlay {
                    overlay: Overlay::WirePathHighlight { .. }
                }
            ))
    );
    assert!(
        scene
            .layers
            .iter()
            .flat_map(|layer| &layer.primitives)
            .any(|primitive| matches!(
                primitive,
                DrawPrimitive::Overlay {
                    overlay: Overlay::WireVertexHandle {
                        vertex_index: 1,
                        ..
                    }
                }
            ))
    );
}

#[test]
fn endpoint_handles_win_over_vertices_terminals_and_segments() {
    let scene = selected_wire_scene_fixture();

    assert!(matches!(
        hit_test(&scene, WorldPoint::new(0.0, 0.0), 1.0),
        Some(HitRegion::WireEndpointHandle { .. })
    ));
}

#[test]
fn marquee_overlay_projects_translucent_rectangle() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let scene = project_folio(
        &project,
        folio_id,
        &EditorPresentation::with_marquee(WorldPoint::new(0.0, 0.0), WorldPoint::new(80.0, 40.0)),
    )
    .expect("folio exists");

    assert!(
        scene
            .layers
            .iter()
            .flat_map(|layer| &layer.primitives)
            .any(|primitive| matches!(
                primitive,
                DrawPrimitive::Overlay {
                    overlay: Overlay::MarqueeRect { .. }
                }
            ))
    );
}

#[test]
fn hit_test_converts_viewport_points_using_the_scene_viewport() {
    let (project, folio_id) = project_with_symbol_and_wire();
    let presentation = EditorPresentation {
        viewport: Viewport {
            origin: WorldPoint::new(100.0, 50.0),
            zoom: 2.0,
        },
        ..EditorPresentation::default()
    };
    let scene = project_folio(&project, folio_id, &presentation).expect("folio exists");

    assert!(matches!(
        hit_test(&scene, WorldPoint::new(100.0, 50.0), 1.0),
        Some(HitRegion::Terminal { .. })
    ));
}
