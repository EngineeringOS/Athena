use athena_geometry::{
    ConnectionAnchor, QuarterTurn, Rect, RouteError, RoutingSettings, SnapSettings, Transform,
    WorldPoint, distance_to_segment, orthogonal_route, point_within_tolerance, snap_point,
    validate_orthogonal_route,
};

fn point(x: f64, y: f64) -> WorldPoint {
    WorldPoint::new(x, y)
}

#[test]
fn snaps_to_the_nearest_integer_grid_intersection() {
    let settings = SnapSettings {
        grid_spacing: 10.0,
        terminal_tolerance: 3.0,
        grid_enabled: true,
    };

    assert_eq!(
        snap_point(point(14.9, -14.9), &[], settings),
        point(10.0, -10.0)
    );
    assert_eq!(
        snap_point(point(15.0, 15.0), &[], settings),
        point(20.0, 20.0)
    );
}

#[test]
fn terminal_snap_wins_over_the_grid_within_tolerance() {
    let settings = SnapSettings {
        grid_spacing: 10.0,
        terminal_tolerance: 3.0,
        grid_enabled: true,
    };
    let anchors = [ConnectionAnchor::new("terminal-b", point(12.0, 12.0))];

    assert_eq!(
        snap_point(point(10.5, 10.5), &anchors, settings),
        point(12.0, 12.0)
    );
}

#[test]
fn transforms_round_trip_through_rotation_and_mirroring() {
    let transform = Transform {
        translation: point(30.0, -15.0),
        rotation: QuarterTurn::Clockwise90,
        mirror_x: true,
        mirror_y: false,
    };
    let original = point(7.5, -4.0);

    assert_eq!(transform.inverse_apply(transform.apply(original)), original);
}

#[test]
fn orthogonal_routing_prefers_horizontal_then_vertical() {
    let route = orthogonal_route(point(0.0, 0.0), point(20.0, 10.0), &[], RoutingSettings)
        .expect("unobstructed route");

    assert_eq!(
        route,
        vec![point(0.0, 0.0), point(20.0, 0.0), point(20.0, 10.0)]
    );
}

#[test]
fn orthogonal_routing_falls_back_to_vertical_then_horizontal_when_needed() {
    let route = orthogonal_route(
        point(0.0, 0.0),
        point(20.0, 10.0),
        &[Rect::from_corners(point(8.0, -1.0), point(12.0, 1.0))],
        RoutingSettings,
    )
    .expect("fallback route");

    assert_eq!(
        route,
        vec![point(0.0, 0.0), point(0.0, 10.0), point(20.0, 10.0)]
    );
}

#[test]
fn orthogonal_routing_reports_no_valid_path_when_both_one_bend_paths_are_blocked() {
    let error = orthogonal_route(
        point(0.0, 0.0),
        point(20.0, 10.0),
        &[
            Rect::from_corners(point(8.0, -1.0), point(12.0, 1.0)),
            Rect::from_corners(point(-1.0, 4.0), point(1.0, 6.0)),
        ],
        RoutingSettings,
    )
    .expect_err("both deterministic paths are blocked");

    assert_eq!(error, RouteError::NoValidPath);
}

#[test]
fn rejects_routes_with_zero_length_segments() {
    let error = validate_orthogonal_route(&[point(0.0, 0.0), point(0.0, 0.0), point(10.0, 0.0)])
        .expect_err("zero-length segment");

    assert_eq!(error, RouteError::ZeroLengthSegment { index: 0 });
}

#[test]
fn tolerance_check_includes_the_boundary() {
    assert!(point_within_tolerance(
        point(3.0, 4.0),
        point(0.0, 0.0),
        5.0
    ));
}

#[test]
fn distance_to_segment_uses_the_nearest_finite_endpoint_or_projection() {
    assert_eq!(
        distance_to_segment(point(5.0, 3.0), point(0.0, 0.0), point(10.0, 0.0)),
        3.0
    );
    assert_eq!(
        distance_to_segment(point(15.0, 0.0), point(0.0, 0.0), point(10.0, 0.0)),
        5.0
    );
}
