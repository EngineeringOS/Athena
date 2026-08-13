//! GPUI paint adapter for the shared, platform-neutral scene projection.

use athena_geometry::{Rect, WorldPoint};
use athena_render::{DrawPrimitive, Overlay, Scene, Viewport};
use gpui::{
    Bounds, IntoElement, PathBuilder, Pixels, Styled, Window, canvas, point, px, quad, rgb, rgba,
    size, transparent_black,
};

/// Returns a native canvas that paints only shared scene primitives. It does
/// not inspect or mutate project state.
pub fn render_scene(scene: Option<Scene>) -> impl IntoElement {
    let viewport = scene
        .as_ref()
        .map_or_else(Viewport::default, |scene| scene.viewport);
    let primitives = scene
        .into_iter()
        .flat_map(|scene| scene.layers)
        .flat_map(|layer| layer.primitives)
        .collect::<Vec<_>>();

    canvas(
        |_, _, _| (),
        move |bounds, _, window, _| {
            for primitive in &primitives {
                paint_primitive(bounds, primitive, viewport, window);
            }
        },
    )
    .size_full()
}

fn paint_primitive(
    bounds: Bounds<Pixels>,
    primitive: &DrawPrimitive,
    viewport: Viewport,
    window: &mut Window,
) {
    match primitive {
        DrawPrimitive::Page { bounds: page } => {
            paint_rect(bounds, *page, viewport, rgb(0xffffff), window);
        }
        DrawPrimitive::Grid {
            bounds: page,
            spacing,
        } => paint_grid(bounds, *page, *spacing, viewport, window),
        DrawPrimitive::Polyline { points } => {
            paint_polyline(bounds, points, viewport, rgb(0x334155), window);
        }
        DrawPrimitive::Circle { center, radius } => {
            paint_rect(
                bounds,
                Rect::from_corners(
                    WorldPoint::new(center.x - radius, center.y - radius),
                    WorldPoint::new(center.x + radius, center.y + radius),
                ),
                viewport,
                rgb(0x0f172a),
                window,
            );
        }
        DrawPrimitive::SymbolBody { bounds: symbol, .. } => {
            paint_rect(bounds, *symbol, viewport, rgb(0x94a3b8), window);
        }
        DrawPrimitive::ConnectionHandle {
            position, radius, ..
        } => paint_rect(
            bounds,
            Rect::from_corners(
                WorldPoint::new(position.x - radius, position.y - radius),
                WorldPoint::new(position.x + radius, position.y + radius),
            ),
            viewport,
            rgb(0x0ea5e9),
            window,
        ),
        DrawPrimitive::Text { .. } => {}
        DrawPrimitive::Overlay { overlay } => paint_overlay(bounds, overlay, viewport, window),
    }
}

fn paint_grid(
    bounds: Bounds<Pixels>,
    page: Rect,
    spacing: f64,
    viewport: Viewport,
    window: &mut Window,
) {
    if spacing <= 0.0 {
        return;
    }
    let mut x = page.min.x;
    while x <= page.max.x {
        paint_polyline(
            bounds,
            &[
                WorldPoint::new(x, page.min.y),
                WorldPoint::new(x, page.max.y),
            ],
            viewport,
            rgb(0xe2e8f0),
            window,
        );
        x += spacing;
    }
    let mut y = page.min.y;
    while y <= page.max.y {
        paint_polyline(
            bounds,
            &[
                WorldPoint::new(page.min.x, y),
                WorldPoint::new(page.max.x, y),
            ],
            viewport,
            rgb(0xe2e8f0),
            window,
        );
        y += spacing;
    }
}

fn paint_overlay(
    bounds: Bounds<Pixels>,
    overlay: &Overlay,
    viewport: Viewport,
    window: &mut Window,
) {
    match overlay {
        Overlay::SelectionBounds {
            bounds: selected, ..
        } => {
            paint_polyline(
                bounds,
                &[
                    selected.min,
                    WorldPoint::new(selected.max.x, selected.min.y),
                    selected.max,
                    WorldPoint::new(selected.min.x, selected.max.y),
                    selected.min,
                ],
                viewport,
                rgb(0x2563eb),
                window,
            );
        }
        Overlay::TransformHandle {
            position, radius, ..
        } => paint_rect(
            bounds,
            Rect::from_corners(
                WorldPoint::new(position.x - radius, position.y - radius),
                WorldPoint::new(position.x + radius, position.y + radius),
            ),
            viewport,
            rgb(0x2563eb),
            window,
        ),
        Overlay::WirePathHighlight { points, .. } => {
            paint_polyline(bounds, points, viewport, rgb(0x0ea5e9), window);
        }
        Overlay::WireVertexHandle {
            position, radius, ..
        }
        | Overlay::WireEndpointHandle {
            position, radius, ..
        } => paint_rect(
            bounds,
            Rect::from_corners(
                WorldPoint::new(position.x - radius, position.y - radius),
                WorldPoint::new(position.x + radius, position.y + radius),
            ),
            viewport,
            rgb(0x0ea5e9),
            window,
        ),
        // Keep the live selection gesture visible without obscuring the schematic.
        Overlay::MarqueeRect { start, end, .. } => paint_rect(
            bounds,
            Rect::from_corners(*start, *end),
            viewport,
            rgba(0x93c5fd55),
            window,
        ),
        Overlay::GuideLine { start, end } => {
            paint_polyline(bounds, &[*start, *end], viewport, rgb(0xf97316), window);
        }
        Overlay::ValidationMarker { position, .. } => paint_rect(
            bounds,
            Rect::from_corners(
                WorldPoint::new(position.x - 3.0, position.y - 3.0),
                WorldPoint::new(position.x + 3.0, position.y + 3.0),
            ),
            viewport,
            rgb(0xdc2626),
            window,
        ),
    }
}

fn paint_rect(
    bounds: Bounds<Pixels>,
    rect: Rect,
    viewport: Viewport,
    color: impl Into<gpui::Background>,
    window: &mut Window,
) {
    let start = world_to_canvas(bounds, viewport, rect.min);
    let end = world_to_canvas(bounds, viewport, rect.max);
    window.paint_quad(quad(
        Bounds {
            origin: start,
            size: size(end.x - start.x, end.y - start.y),
        },
        px(0.0),
        color,
        px(0.0),
        transparent_black(),
        Default::default(),
    ));
}

fn paint_polyline(
    bounds: Bounds<Pixels>,
    points: &[WorldPoint],
    viewport: Viewport,
    color: impl Into<gpui::Background>,
    window: &mut Window,
) {
    if points.len() < 2 {
        return;
    }
    let mut path = PathBuilder::stroke(px(1.0));
    for (index, point) in points.iter().enumerate() {
        let point = world_to_canvas(bounds, viewport, *point);
        if index == 0 {
            path.move_to(point);
        } else {
            path.line_to(point);
        }
    }
    if let Ok(path) = path.build() {
        window.paint_path(path, color);
    }
}

fn world_to_canvas(
    bounds: Bounds<Pixels>,
    viewport: Viewport,
    world_point: WorldPoint,
) -> gpui::Point<Pixels> {
    let transformed = viewport.world_to_viewport(world_point);
    point(
        bounds.origin.x + px(24.0 + transformed.x as f32),
        bounds.origin.y + px(24.0 + transformed.y as f32),
    )
}
