use crate::WorldPoint;
use serde::{Deserialize, Serialize};

/// The supported 90-degree schematic symbol rotations.
#[derive(Clone, Copy, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub enum QuarterTurn {
    #[default]
    None,
    Clockwise90,
    Clockwise180,
    Clockwise270,
}

impl QuarterTurn {
    #[must_use]
    pub const fn inverse(self) -> Self {
        match self {
            Self::None => Self::None,
            Self::Clockwise90 => Self::Clockwise270,
            Self::Clockwise180 => Self::Clockwise180,
            Self::Clockwise270 => Self::Clockwise90,
        }
    }

    #[must_use]
    pub(crate) const fn apply(self, point: WorldPoint) -> WorldPoint {
        match self {
            Self::None => point,
            Self::Clockwise90 => WorldPoint::new(-point.y, point.x),
            Self::Clockwise180 => WorldPoint::new(-point.x, -point.y),
            Self::Clockwise270 => WorldPoint::new(point.y, -point.x),
        }
    }
}

/// A local-to-world transform using axis mirrors followed by a quarter-turn rotation.
#[derive(Clone, Copy, Debug, Default, Deserialize, PartialEq, Serialize)]
pub struct Transform {
    pub translation: WorldPoint,
    pub rotation: QuarterTurn,
    pub mirror_x: bool,
    pub mirror_y: bool,
}

impl Transform {
    #[must_use]
    pub fn apply(self, point: WorldPoint) -> WorldPoint {
        let mirrored = WorldPoint::new(
            if self.mirror_x { -point.x } else { point.x },
            if self.mirror_y { -point.y } else { point.y },
        );
        let rotated = self.rotation.apply(mirrored);

        WorldPoint::new(
            rotated.x + self.translation.x,
            rotated.y + self.translation.y,
        )
    }

    #[must_use]
    pub fn inverse_apply(self, point: WorldPoint) -> WorldPoint {
        let translated =
            WorldPoint::new(point.x - self.translation.x, point.y - self.translation.y);
        let rotated = self.rotation.inverse().apply(translated);

        WorldPoint::new(
            if self.mirror_x { -rotated.x } else { rotated.x },
            if self.mirror_y { -rotated.y } else { rotated.y },
        )
    }
}
