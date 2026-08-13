//! Platform-neutral schematic scene and hit-test projection contracts.

mod hit_test;
mod scene;

pub use hit_test::hit_test;
pub use scene::{
    DrawPrimitive, EditorPresentation, Guide, HitRegion, Marquee, Overlay, PresentationItemId,
    Scene, SceneLayer, SceneLayerKind, ValidationMessage, Viewport, project_sheet,
};

/// Compile-time marker for the render crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct RenderContract;

#[cfg(test)]
mod tests {
    use super::RenderContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = RenderContract;
        assert_eq!(marker, RenderContract);
    }
}
