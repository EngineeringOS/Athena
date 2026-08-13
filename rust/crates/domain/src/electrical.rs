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
    pub x: i64,
    pub y: i64,
}

impl Point {
    #[must_use]
    pub const fn new(x: i64, y: i64) -> Self {
        Self { x, y }
    }
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum FieldValue {
    Text(String),
    Integer(i64),
    Boolean(bool),
}

#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum ElectricalKind {
    Passive,
    Input,
    Output,
    Power,
    Ground,
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Junction {
    pub id: JunctionId,
    pub position: Point,
}

impl Junction {
    #[must_use]
    pub fn new(position: Point) -> Self {
        Self {
            id: JunctionId::new(),
            position,
        }
    }
}

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum WireEndpoint {
    Terminal(TerminalId),
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

#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct Annotation {
    pub id: AnnotationId,
    pub text: String,
    pub position: Point,
    #[serde(default)]
    pub fields: BTreeMap<String, FieldValue>,
}

impl Annotation {
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
