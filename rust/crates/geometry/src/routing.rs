use crate::{Rect, WorldPoint};
use serde::{Deserialize, Serialize};
use thiserror::Error;

/// Configuration reserved for deterministic routing policy evolution.
#[derive(Clone, Copy, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub struct RoutingSettings;

/// A deterministic routing failure.
#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum RouteError {
    #[error("no valid orthogonal path exists")]
    NoValidPath,
    #[error("route contains a zero-length segment at index {index}")]
    ZeroLengthSegment { index: usize },
    #[error("route segment {index} is not orthogonal")]
    NonOrthogonalSegment { index: usize },
}

/// Routes using the stable horizontal-then-vertical bend, then a vertical-first fallback.
pub fn orthogonal_route(
    start: WorldPoint,
    end: WorldPoint,
    obstacles: &[Rect],
    _settings: RoutingSettings,
) -> Result<Vec<WorldPoint>, RouteError> {
    if start == end {
        return Err(RouteError::ZeroLengthSegment { index: 0 });
    }

    let horizontal_then_vertical =
        normalize_route(vec![start, WorldPoint::new(end.x, start.y), end]);
    if route_is_clear(&horizontal_then_vertical, obstacles) {
        return Ok(horizontal_then_vertical);
    }

    let vertical_then_horizontal =
        normalize_route(vec![start, WorldPoint::new(start.x, end.y), end]);
    if route_is_clear(&vertical_then_horizontal, obstacles) {
        return Ok(vertical_then_horizontal);
    }

    Err(RouteError::NoValidPath)
}

/// Verifies that every route segment is non-zero-length and axis-aligned.
pub fn validate_orthogonal_route(route: &[WorldPoint]) -> Result<(), RouteError> {
    for (index, segment) in route.windows(2).enumerate() {
        let start = segment[0];
        let end = segment[1];

        if start == end {
            return Err(RouteError::ZeroLengthSegment { index });
        }
        if start.x != end.x && start.y != end.y {
            return Err(RouteError::NonOrthogonalSegment { index });
        }
    }

    Ok(())
}

fn normalize_route(route: Vec<WorldPoint>) -> Vec<WorldPoint> {
    route.into_iter().fold(Vec::new(), |mut normalized, point| {
        if normalized.last().copied() != Some(point) {
            normalized.push(point);
        }
        normalized
    })
}

fn route_is_clear(route: &[WorldPoint], obstacles: &[Rect]) -> bool {
    validate_orthogonal_route(route).is_ok()
        && route.windows(2).all(|segment| {
            obstacles
                .iter()
                .all(|obstacle| !obstacle.intersects_axis_aligned_segment(segment[0], segment[1]))
        })
}
