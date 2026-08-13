//! Electrical-schematic domain entities stored in a platform-neutral document.

use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{AnnotationId, JunctionId, TerminalId, WireId};

/// An integer document-space point. Geometry conversion and transforms live in
/// `athena-geometry`; this type keeps saved-domain coordinates platform-neutral.
#[derive(
    Clone, Copy, Debug, Default, Deserialize, Eq, Hash, Ord, PartialEq, PartialOrd, Serialize,
)]
pub struct Point {
    /// Horizontal coordinate in integer document space.
    pub x: i64,
    /// Vertical coordinate in integer document space.
    pub y: i64,
}

impl Point {
    /// Creates a point in integer document space.
    #[must_use]
    pub const fn new(x: i64, y: i64) -> Self {
        Self { x, y }
    }
}

/// A typed, persisted value attached to a schematic entity field.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum FieldValue {
    /// Free-form text such as a reference designator or description.
    Text(String),
    /// A signed integer property.
    Integer(i64),
    /// A binary property.
    Boolean(bool),
}

/// Electrical role assigned to a terminal for schematic semantics and rendering.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum ElectricalKind {
    /// A bidirectional passive connection.
    Passive,
    /// A signal input.
    Input,
    /// A signal output.
    Output,
    /// A power-source connection.
    Power,
    /// A ground-reference connection.
    Ground,
}

/// A persisted join point that can own one or more wire endpoints.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Junction {
    /// Stable junction identity.
    pub id: JunctionId,
    /// Document-space position owned by this junction.
    pub position: Point,
}

impl Junction {
    /// Creates a junction at `position` with a new stable identity.
    #[must_use]
    pub fn new(position: Point) -> Self {
        Self {
            id: JunctionId::new(),
            position,
        }
    }
}

/// An entity that owns one endpoint of a persisted wire route.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum WireEndpoint {
    /// A symbol terminal owns the endpoint position.
    Terminal(TerminalId),
    /// A junction owns the endpoint position.
    Junction(JunctionId),
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Wire {
    /// Stable identity of this electrical connection.
    pub id: WireId,
    /// Entity that owns the first route point.
    pub start: WireEndpoint,
    /// Entity that owns the last route point.
    pub end: WireEndpoint,
    /// Canonical, orthogonal route including both endpoint-owned points.
    pub route: Vec<Point>,
    #[serde(default)]
    pub fields: BTreeMap<String, FieldValue>,
}

impl Wire {
    /// Creates a wire whose route is later admitted only when it is anchored to
    /// `start` and `end`, contains at least two points, and has orthogonal,
    /// non-zero-length segments. The editor canonicalizes redundant bends
    /// before persistence; callers retain ownership of endpoint selection.
    #[must_use]
    pub fn new(start: WireEndpoint, end: WireEndpoint, route: Vec<Point>) -> Self {
        Self {
            id: WireId::new(),
            start,
            end,
            route,
            fields: BTreeMap::new(),
        }
    }
}

/// Returns the deterministic route form stored by Athena.
///
/// Consecutive duplicate points and collinear interior bends are removed. The
/// caller remains responsible for validating endpoint ownership and segment
/// geometry after canonicalization.
#[must_use]
pub fn canonical_wire_route(route: &[Point]) -> Vec<Point> {
    let without_duplicates = route.iter().copied().fold(Vec::new(), |mut points, point| {
        if points.last().copied() != Some(point) {
            points.push(point);
        }
        points
    });
    let mut normalized: Vec<Point> = Vec::with_capacity(without_duplicates.len());
    for point in without_duplicates {
        if let [.., previous, current] = normalized.as_slice()
            && (previous.x == current.x && current.x == point.x
                || previous.y == current.y && current.y == point.y)
        {
            normalized.pop();
        }
        normalized.push(point);
    }
    normalized
}

/// A persisted free-text schematic annotation.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Annotation {
    /// Stable annotation identity.
    pub id: AnnotationId,
    /// Visible annotation content.
    pub text: String,
    /// Document-space anchor position.
    pub position: Point,
    /// User-defined typed metadata keyed by stable field names.
    #[serde(default)]
    pub fields: BTreeMap<String, FieldValue>,
}

impl Annotation {
    /// Creates an annotation with an empty field map.
    #[must_use]
    pub fn new(text: impl Into<String>, position: Point) -> Self {
        Self {
            id: AnnotationId::new(),
            text: text.into(),
            position,
            fields: BTreeMap::new(),
        }
    }
}
