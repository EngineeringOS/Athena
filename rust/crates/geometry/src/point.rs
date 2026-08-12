use serde::{Deserialize, Serialize};

/// A point in schematic world coordinates.
#[derive(Clone, Copy, Debug, Default, Deserialize, PartialEq, Serialize)]
pub struct WorldPoint {
    pub x: f64,
    pub y: f64,
}

impl WorldPoint {
    #[must_use]
    pub const fn new(x: f64, y: f64) -> Self {
        Self { x, y }
    }

    #[must_use]
    pub fn distance_to(self, other: Self) -> f64 {
        (self.x - other.x).hypot(self.y - other.y)
    }
}

/// A normalized axis-aligned world-space rectangle.
#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Serialize)]
pub struct Rect {
    pub min: WorldPoint,
    pub max: WorldPoint,
}

impl Rect {
    #[must_use]
    pub fn from_corners(first: WorldPoint, second: WorldPoint) -> Self {
        Self {
            min: WorldPoint::new(first.x.min(second.x), first.y.min(second.y)),
            max: WorldPoint::new(first.x.max(second.x), first.y.max(second.y)),
        }
    }

    #[must_use]
    pub fn contains(self, point: WorldPoint) -> bool {
        point.x >= self.min.x
            && point.x <= self.max.x
            && point.y >= self.min.y
            && point.y <= self.max.y
    }

    #[must_use]
    pub(crate) fn intersects_axis_aligned_segment(
        self,
        start: WorldPoint,
        end: WorldPoint,
    ) -> bool {
        if start.x == end.x {
            let segment_min = start.y.min(end.y);
            let segment_max = start.y.max(end.y);
            return start.x >= self.min.x
                && start.x <= self.max.x
                && segment_max >= self.min.y
                && segment_min <= self.max.y;
        }

        if start.y == end.y {
            let segment_min = start.x.min(end.x);
            let segment_max = start.x.max(end.x);
            return start.y >= self.min.y
                && start.y <= self.max.y
                && segment_max >= self.min.x
                && segment_min <= self.max.x;
        }

        false
    }
}

/// Returns the shortest Euclidean distance from `point` to the finite segment.
#[must_use]
pub fn distance_to_segment(point: WorldPoint, start: WorldPoint, end: WorldPoint) -> f64 {
    let delta_x = end.x - start.x;
    let delta_y = end.y - start.y;
    let length_squared = delta_x.mul_add(delta_x, delta_y * delta_y);

    if length_squared == 0.0 {
        return point.distance_to(start);
    }

    let projection =
        ((point.x - start.x) * delta_x + (point.y - start.y) * delta_y) / length_squared;
    let clamped = projection.clamp(0.0, 1.0);
    let closest = WorldPoint::new(start.x + clamped * delta_x, start.y + clamped * delta_y);

    point.distance_to(closest)
}

/// Tests whether `point` lies within the inclusive Euclidean tolerance of `target`.
#[must_use]
pub fn point_within_tolerance(point: WorldPoint, target: WorldPoint, tolerance: f64) -> bool {
    tolerance >= 0.0 && point.distance_to(target) <= tolerance
}
