//! Deterministic coordinates, transforms, snapping, and routing contracts.

mod point;
mod routing;
mod snap;
mod transform;

pub use point::{Rect, WorldPoint, distance_to_segment, point_within_tolerance};
pub use routing::{RouteError, RoutingSettings, orthogonal_route, validate_orthogonal_route};
pub use snap::{ConnectionAnchor, SnapSettings, snap_point};
pub use transform::{QuarterTurn, Transform};

/// Compile-time marker for the geometry crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct GeometryContract;

#[cfg(test)]
mod tests {
    use super::GeometryContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = GeometryContract;
        assert_eq!(marker, GeometryContract);
    }
}
