use std::collections::BTreeSet;

use athena_domain::{
    AnnotationId, FieldValue, JunctionId, Point, Project, SheetId, SymbolInstance,
    SymbolInstanceId, TerminalId, WireId,
};
use athena_geometry::{Rect, WorldPoint};
use serde::{Deserialize, Serialize};

const SYMBOL_HALF_WIDTH: f64 = 8.0;
const SYMBOL_HALF_HEIGHT: f64 = 6.0;
const CONNECTION_HANDLE_RADIUS: f64 = 3.0;
const JUNCTION_RADIUS: f64 = 2.5;
const TRANSFORM_HANDLE_RADIUS: f64 = 2.5;
const ANNOTATION_HEIGHT: f64 = 8.0;
const CHARACTER_WIDTH: f64 = 6.0;

/// A canvas-to-world transform owned by presentation state.
///
/// `origin` is the viewport position of world coordinate `(0, 0)`. Zoom is
/// world units to viewport units.
#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Serialize)]
pub struct Viewport {
    pub origin: WorldPoint,
    pub zoom: f64,
}

impl Default for Viewport {
    fn default() -> Self {
        Self {
            origin: WorldPoint::default(),
            zoom: 1.0,
        }
    }
}

impl Viewport {
    #[must_use]
    pub fn viewport_to_world(self, point: WorldPoint) -> WorldPoint {
        let zoom = self.effective_zoom();
        WorldPoint::new(
            (point.x - self.origin.x) / zoom,
            (point.y - self.origin.y) / zoom,
        )
    }

    #[must_use]
    pub fn world_to_viewport(self, point: WorldPoint) -> WorldPoint {
        let zoom = self.effective_zoom();
        WorldPoint::new(
            point.x.mul_add(zoom, self.origin.x),
            point.y.mul_add(zoom, self.origin.y),
        )
    }

    #[must_use]
    pub fn viewport_tolerance_to_world(self, tolerance: f64) -> f64 {
        tolerance.max(0.0) / self.effective_zoom()
    }

    fn effective_zoom(self) -> f64 {
        if self.zoom.is_finite() && self.zoom > 0.0 {
            self.zoom
        } else {
            1.0
        }
    }
}

/// A saved entity that may be selected, without making selection part of the
/// saved project state.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum PresentationItemId {
    Symbol(SymbolInstanceId),
    Wire(WireId),
    Junction(JunctionId),
    Annotation(AnnotationId),
}

/// A temporary alignment guide drawn by editor tooling.
#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Serialize)]
pub struct Guide {
    pub start: WorldPoint,
    pub end: WorldPoint,
}

/// A non-persistent validation message associated with a world coordinate.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub struct ValidationMessage {
    pub position: WorldPoint,
    pub message: String,
}

/// UI-owned state that influences a scene but is never written into
/// `athena_domain::Project`.
#[derive(Clone, Debug, Default, Deserialize, PartialEq, Serialize)]
pub struct EditorPresentation {
    pub viewport: Viewport,
    #[serde(default)]
    pub selected: BTreeSet<PresentationItemId>,
    #[serde(default)]
    pub guides: Vec<Guide>,
    #[serde(default)]
    pub validation_messages: Vec<ValidationMessage>,
}

/// A scene ready for a platform renderer to turn into pixels.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub struct Scene {
    pub sheet_id: SheetId,
    pub viewport: Viewport,
    pub layers: Vec<SceneLayer>,
    pub hit_regions: Vec<HitRegion>,
}

/// A fixed-order drawing layer.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub struct SceneLayer {
    pub kind: SceneLayerKind,
    pub primitives: Vec<DrawPrimitive>,
}

/// Scene layers ordered back-to-front.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum SceneLayerKind {
    PageAndGrid,
    WiresAndJunctions,
    Symbols,
    FieldsAndAnnotations,
    Overlays,
}

/// Platform-neutral instructions used by both native and browser renderers.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub enum DrawPrimitive {
    Page {
        bounds: Rect,
    },
    Grid {
        bounds: Rect,
        spacing: f64,
    },
    Polyline {
        points: Vec<WorldPoint>,
    },
    Circle {
        center: WorldPoint,
        radius: f64,
    },
    SymbolBody {
        symbol_id: SymbolInstanceId,
        bounds: Rect,
    },
    ConnectionHandle {
        terminal_id: TerminalId,
        position: WorldPoint,
        radius: f64,
    },
    Text {
        text: String,
        position: WorldPoint,
    },
    Overlay {
        overlay: Overlay,
    },
}

/// An editor-only visual shown above saved schematic content.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub enum Overlay {
    SelectionBounds {
        item: PresentationItemId,
        bounds: Rect,
    },
    TransformHandle {
        item: PresentationItemId,
        position: WorldPoint,
        radius: f64,
    },
    GuideLine {
        start: WorldPoint,
        end: WorldPoint,
    },
    ValidationMarker {
        position: WorldPoint,
        message: String,
    },
}

/// Stable, world-space interaction geometry emitted with a scene.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub enum HitRegion {
    Terminal {
        terminal_id: TerminalId,
        symbol_id: SymbolInstanceId,
        position: WorldPoint,
        radius: f64,
    },
    SymbolBody {
        symbol_id: SymbolInstanceId,
        bounds: Rect,
    },
    WireVertex {
        wire_id: WireId,
        vertex_index: usize,
        position: WorldPoint,
    },
    WireSegment {
        wire_id: WireId,
        segment_index: usize,
        start: WorldPoint,
        end: WorldPoint,
    },
    Junction {
        junction_id: JunctionId,
        position: WorldPoint,
        radius: f64,
    },
    Annotation {
        annotation_id: AnnotationId,
        bounds: Rect,
    },
}

/// Projects one saved sheet and presentation-only editor state into a layered,
/// deterministic render scene.
#[must_use]
pub fn project_sheet(
    project: &Project,
    sheet_id: SheetId,
    editor_presentation: &EditorPresentation,
) -> Option<Scene> {
    let sheet = project.sheet(sheet_id)?;
    let page_bounds = Rect::from_corners(
        WorldPoint::default(),
        WorldPoint::new(
            sheet.settings.page_width as f64,
            sheet.settings.page_height as f64,
        ),
    );
    let mut page_and_grid = vec![DrawPrimitive::Page {
        bounds: page_bounds,
    }];
    if sheet.settings.grid_spacing > 0 {
        page_and_grid.push(DrawPrimitive::Grid {
            bounds: page_bounds,
            spacing: sheet.settings.grid_spacing as f64,
        });
    }

    let mut wires_and_junctions = Vec::new();
    let mut hit_regions = Vec::new();
    for (wire_id, wire) in &sheet.wires {
        let points = wire
            .route
            .iter()
            .copied()
            .map(world_point)
            .collect::<Vec<_>>();
        wires_and_junctions.push(DrawPrimitive::Polyline {
            points: points.clone(),
        });
        for (vertex_index, position) in points.iter().copied().enumerate() {
            hit_regions.push(HitRegion::WireVertex {
                wire_id: *wire_id,
                vertex_index,
                position,
            });
        }
        for (segment_index, segment) in points.windows(2).enumerate() {
            hit_regions.push(HitRegion::WireSegment {
                wire_id: *wire_id,
                segment_index,
                start: segment[0],
                end: segment[1],
            });
        }
    }
    for (junction_id, junction) in &sheet.junctions {
        let position = world_point(junction.position);
        wires_and_junctions.push(DrawPrimitive::Circle {
            center: position,
            radius: JUNCTION_RADIUS,
        });
        hit_regions.push(HitRegion::Junction {
            junction_id: *junction_id,
            position,
            radius: JUNCTION_RADIUS,
        });
    }

    let mut symbols = Vec::new();
    for (symbol_id, symbol) in &sheet.symbol_instances {
        let bounds = symbol_body_bounds(symbol);
        symbols.push(DrawPrimitive::SymbolBody {
            symbol_id: *symbol_id,
            bounds,
        });
        hit_regions.push(HitRegion::SymbolBody {
            symbol_id: *symbol_id,
            bounds,
        });
        for (terminal_id, terminal) in &symbol.terminals {
            let position = world_point(terminal.position);
            symbols.push(DrawPrimitive::ConnectionHandle {
                terminal_id: *terminal_id,
                position,
                radius: CONNECTION_HANDLE_RADIUS,
            });
            hit_regions.push(HitRegion::Terminal {
                terminal_id: *terminal_id,
                symbol_id: *symbol_id,
                position,
                radius: CONNECTION_HANDLE_RADIUS,
            });
        }
    }

    let mut fields_and_annotations = Vec::new();
    for symbol in sheet.symbol_instances.values() {
        let origin = world_point(symbol.position);
        for (index, (field_name, value)) in symbol.fields.iter().enumerate() {
            fields_and_annotations.push(DrawPrimitive::Text {
                text: format!("{field_name}: {}", field_value_text(value)),
                position: WorldPoint::new(origin.x, origin.y - 12.0 - index as f64 * 10.0),
            });
        }
    }
    for wire in sheet.wires.values() {
        if let Some(position) = wire.route.first().copied().map(world_point) {
            for (index, (field_name, value)) in wire.fields.iter().enumerate() {
                fields_and_annotations.push(DrawPrimitive::Text {
                    text: format!("{field_name}: {}", field_value_text(value)),
                    position: WorldPoint::new(position.x, position.y - 12.0 - index as f64 * 10.0),
                });
            }
        }
    }
    for (annotation_id, annotation) in &sheet.annotations {
        let position = world_point(annotation.position);
        fields_and_annotations.push(DrawPrimitive::Text {
            text: annotation.text.clone(),
            position,
        });
        hit_regions.push(HitRegion::Annotation {
            annotation_id: *annotation_id,
            bounds: text_bounds(position, &annotation.text),
        });
        for (index, (field_name, value)) in annotation.fields.iter().enumerate() {
            fields_and_annotations.push(DrawPrimitive::Text {
                text: format!("{field_name}: {}", field_value_text(value)),
                position: WorldPoint::new(position.x, position.y + 12.0 + index as f64 * 10.0),
            });
        }
    }

    let overlays = overlay_primitives(sheet, editor_presentation);

    Some(Scene {
        sheet_id,
        viewport: editor_presentation.viewport,
        layers: vec![
            SceneLayer {
                kind: SceneLayerKind::PageAndGrid,
                primitives: page_and_grid,
            },
            SceneLayer {
                kind: SceneLayerKind::WiresAndJunctions,
                primitives: wires_and_junctions,
            },
            SceneLayer {
                kind: SceneLayerKind::Symbols,
                primitives: symbols,
            },
            SceneLayer {
                kind: SceneLayerKind::FieldsAndAnnotations,
                primitives: fields_and_annotations,
            },
            SceneLayer {
                kind: SceneLayerKind::Overlays,
                primitives: overlays,
            },
        ],
        hit_regions,
    })
}

fn overlay_primitives(
    sheet: &athena_domain::Sheet,
    presentation: &EditorPresentation,
) -> Vec<DrawPrimitive> {
    let mut overlays = Vec::new();
    for item in &presentation.selected {
        if let Some(bounds) = item_bounds(sheet, *item) {
            overlays.push(DrawPrimitive::Overlay {
                overlay: Overlay::SelectionBounds {
                    item: *item,
                    bounds,
                },
            });
            for position in rectangle_corners(bounds) {
                overlays.push(DrawPrimitive::Overlay {
                    overlay: Overlay::TransformHandle {
                        item: *item,
                        position,
                        radius: TRANSFORM_HANDLE_RADIUS,
                    },
                });
            }
        }
    }
    overlays.extend(
        presentation
            .guides
            .iter()
            .map(|guide| DrawPrimitive::Overlay {
                overlay: Overlay::GuideLine {
                    start: guide.start,
                    end: guide.end,
                },
            }),
    );
    overlays.extend(presentation.validation_messages.iter().map(|message| {
        DrawPrimitive::Overlay {
            overlay: Overlay::ValidationMarker {
                position: message.position,
                message: message.message.clone(),
            },
        }
    }));
    overlays
}

fn item_bounds(sheet: &athena_domain::Sheet, item: PresentationItemId) -> Option<Rect> {
    match item {
        PresentationItemId::Symbol(symbol_id) => sheet
            .symbol_instances
            .get(&symbol_id)
            .map(symbol_body_bounds),
        PresentationItemId::Wire(wire_id) => route_bounds(&sheet.wires.get(&wire_id)?.route),
        PresentationItemId::Junction(junction_id) => {
            let point = world_point(sheet.junctions.get(&junction_id)?.position);
            Some(Rect::from_corners(
                WorldPoint::new(point.x - JUNCTION_RADIUS, point.y - JUNCTION_RADIUS),
                WorldPoint::new(point.x + JUNCTION_RADIUS, point.y + JUNCTION_RADIUS),
            ))
        }
        PresentationItemId::Annotation(annotation_id) => {
            let annotation = sheet.annotations.get(&annotation_id)?;
            Some(text_bounds(
                world_point(annotation.position),
                &annotation.text,
            ))
        }
    }
}

fn route_bounds(route: &[Point]) -> Option<Rect> {
    let first = world_point(*route.first()?);
    let mut min = first;
    let mut max = first;
    for point in route.iter().skip(1).copied().map(world_point) {
        min.x = min.x.min(point.x);
        min.y = min.y.min(point.y);
        max.x = max.x.max(point.x);
        max.y = max.y.max(point.y);
    }
    Some(Rect::from_corners(min, max))
}

fn symbol_body_bounds(symbol: &SymbolInstance) -> Rect {
    let center = world_point(symbol.position);
    Rect::from_corners(
        WorldPoint::new(center.x - SYMBOL_HALF_WIDTH, center.y - SYMBOL_HALF_HEIGHT),
        WorldPoint::new(center.x + SYMBOL_HALF_WIDTH, center.y + SYMBOL_HALF_HEIGHT),
    )
}

fn text_bounds(position: WorldPoint, text: &str) -> Rect {
    Rect::from_corners(
        position,
        WorldPoint::new(
            position.x + text.chars().count() as f64 * CHARACTER_WIDTH,
            position.y + ANNOTATION_HEIGHT,
        ),
    )
}

fn rectangle_corners(bounds: Rect) -> [WorldPoint; 4] {
    [
        bounds.min,
        WorldPoint::new(bounds.max.x, bounds.min.y),
        bounds.max,
        WorldPoint::new(bounds.min.x, bounds.max.y),
    ]
}

fn field_value_text(value: &FieldValue) -> String {
    match value {
        FieldValue::Text(value) => value.clone(),
        FieldValue::Integer(value) => value.to_string(),
        FieldValue::Boolean(value) => value.to_string(),
    }
}

fn world_point(point: Point) -> WorldPoint {
    WorldPoint::new(point.x as f64, point.y as f64)
}
