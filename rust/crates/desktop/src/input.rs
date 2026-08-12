use athena_domain::{Point, SymbolDefinitionId, TerminalId};
use athena_geometry::WorldPoint;
use athena_render::{HitRegion, PresentationItemId, Scene, hit_test};

/// The active desktop interaction mode.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum ActiveTool {
    Select,
    PlaceSymbol(SymbolDefinitionId),
    Wire { start: Option<TerminalId> },
    Pan,
}

/// A canvas-space pointer input, kept independent from GPUI's event types.
#[derive(Clone, Copy, Debug)]
pub struct CanvasInput {
    point: Point,
}

impl CanvasInput {
    #[must_use]
    pub const fn new(point: Point) -> Self {
        Self { point }
    }

    #[must_use]
    pub fn hit_item(self, scene: Option<&Scene>) -> Option<PresentationItemId> {
        match self.hit(scene)? {
            HitRegion::SymbolBody { symbol_id, .. } | HitRegion::Terminal { symbol_id, .. } => {
                Some(PresentationItemId::Symbol(symbol_id))
            }
            HitRegion::WireVertex { wire_id, .. } | HitRegion::WireSegment { wire_id, .. } => {
                Some(PresentationItemId::Wire(wire_id))
            }
            HitRegion::Junction { junction_id, .. } => {
                Some(PresentationItemId::Junction(junction_id))
            }
            HitRegion::Annotation { annotation_id, .. } => {
                Some(PresentationItemId::Annotation(annotation_id))
            }
        }
    }

    #[must_use]
    pub fn terminal_at(self, scene: Option<&Scene>) -> Option<(TerminalId, WorldPoint)> {
        match self.hit(scene)? {
            HitRegion::Terminal {
                terminal_id,
                position,
                ..
            } => Some((terminal_id, position)),
            _ => None,
        }
    }

    fn hit(self, scene: Option<&Scene>) -> Option<HitRegion> {
        hit_test(
            scene?,
            WorldPoint::new(self.point.x as f64, self.point.y as f64),
            6.0,
        )
    }
}
