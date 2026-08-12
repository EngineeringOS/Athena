use crate::{WorldPoint, point_within_tolerance};
use serde::{Deserialize, Serialize};

/// A wire-connectable point owned by a terminal or explicit junction.
#[derive(Clone, Debug, Deserialize, PartialEq, Serialize)]
pub struct ConnectionAnchor {
    pub id: String,
    pub position: WorldPoint,
}

impl ConnectionAnchor {
    #[must_use]
    pub fn new(id: impl Into<String>, position: WorldPoint) -> Self {
        Self {
            id: id.into(),
            position,
        }
    }
}

/// Deterministic snapping configuration for schematic editing.
#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Serialize)]
pub struct SnapSettings {
    pub grid_spacing: f64,
    pub terminal_tolerance: f64,
    pub grid_enabled: bool,
}

impl Default for SnapSettings {
    fn default() -> Self {
        Self {
            grid_spacing: 10.0,
            terminal_tolerance: 6.0,
            grid_enabled: true,
        }
    }
}

/// Snaps to the closest eligible anchor first, then to the nearest integer grid point.
#[must_use]
pub fn snap_point(
    point: WorldPoint,
    anchors: &[ConnectionAnchor],
    settings: SnapSettings,
) -> WorldPoint {
    let terminal = anchors
        .iter()
        .filter(|anchor| {
            point_within_tolerance(point, anchor.position, settings.terminal_tolerance)
        })
        .min_by(|left, right| {
            point
                .distance_to(left.position)
                .total_cmp(&point.distance_to(right.position))
                .then_with(|| left.id.cmp(&right.id))
        });

    if let Some(anchor) = terminal {
        return anchor.position;
    }

    if settings.grid_enabled && settings.grid_spacing.is_finite() && settings.grid_spacing > 0.0 {
        return WorldPoint::new(
            (point.x / settings.grid_spacing).round() * settings.grid_spacing,
            (point.y / settings.grid_spacing).round() * settings.grid_spacing,
        );
    }

    point
}
