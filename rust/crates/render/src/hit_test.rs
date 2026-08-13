//! Viewport-aware prioritization of the interactive regions projected by scenes.

use athena_geometry::{WorldPoint, distance_to_segment, point_within_tolerance};

use crate::{HitRegion, Scene};

/// Returns the deterministically topmost region at a viewport-space point.
///
/// Hit priority follows schematic editing semantics: selected wire endpoint
/// handles and vertices win over terminals and ordinary wire segments.
/// Equal-priority ties keep the first region emitted by deterministic projection.
#[must_use]
pub fn hit_test(scene: &Scene, viewport_point: WorldPoint, tolerance: f64) -> Option<HitRegion> {
    let world_point = scene.viewport.viewport_to_world(viewport_point);
    let world_tolerance = scene.viewport.viewport_tolerance_to_world(tolerance);
    let mut best: Option<(u8, f64, &HitRegion)> = None;

    for region in &scene.hit_regions {
        let Some(distance) = hit_distance(region, world_point, world_tolerance) else {
            continue;
        };
        let priority = hit_priority(region);
        let replaces_best = best.is_none_or(|(best_priority, best_distance, _)| {
            priority > best_priority || (priority == best_priority && distance < best_distance)
        });
        if replaces_best {
            best = Some((priority, distance, region));
        }
    }

    best.map(|(_, _, region)| region.clone())
}

fn hit_priority(region: &HitRegion) -> u8 {
    match region {
        HitRegion::WireEndpointHandle { .. } => 7,
        HitRegion::WireVertex { .. } => 6,
        HitRegion::Terminal { .. } => 5,
        HitRegion::WireSegment { .. } => 4,
        HitRegion::SymbolBody { .. } => 3,
        HitRegion::Annotation { .. } => 2,
        HitRegion::Junction { .. } => 1,
    }
}

fn hit_distance(region: &HitRegion, point: WorldPoint, tolerance: f64) -> Option<f64> {
    match region {
        HitRegion::WireEndpointHandle {
            position, radius, ..
        }
        | HitRegion::Terminal {
            position, radius, ..
        }
        | HitRegion::Junction {
            position, radius, ..
        } => {
            let allowed = radius + tolerance;
            point_within_tolerance(point, *position, allowed).then(|| point.distance_to(*position))
        }
        HitRegion::WireVertex { position, .. } => {
            point_within_tolerance(point, *position, tolerance)
                .then(|| point.distance_to(*position))
        }
        HitRegion::WireSegment { start, end, .. } => {
            let distance = distance_to_segment(point, *start, *end);
            (distance <= tolerance).then_some(distance)
        }
        HitRegion::SymbolBody { bounds, .. } | HitRegion::Annotation { bounds, .. } => {
            rect_distance(*bounds, point).filter(|distance| *distance <= tolerance)
        }
    }
}

fn rect_distance(bounds: athena_geometry::Rect, point: WorldPoint) -> Option<f64> {
    if bounds.contains(point) {
        return Some(0.0);
    }

    let nearest = WorldPoint::new(
        point.x.clamp(bounds.min.x, bounds.max.x),
        point.y.clamp(bounds.min.y, bounds.max.y),
    );
    Some(point.distance_to(nearest))
}
