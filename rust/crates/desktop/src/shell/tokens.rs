//! Graphite-measured visual constants shared by native shell render modules.

/// Graphite title-bar height in logical pixels.
pub const TITLE_BAR_HEIGHT: f32 = 28.0;
/// Graphite status-bar height in logical pixels.
pub const STATUS_BAR_HEIGHT: f32 = 24.0;
/// Graphite panel tab-bar height in logical pixels.
pub const TAB_BAR_HEIGHT: f32 = 28.0;
/// Graphite recursive split gutter allocation in logical pixels.
pub const GUTTER_SIZE: f32 = 4.0;
/// Graphite panel corner radius in logical pixels.
pub const PANEL_RADIUS: f32 = 6.0;
/// Graphite compact rectangular control radius in logical pixels.
pub const CONTROL_RADIUS: f32 = 2.0;
/// Graphite baseline UI text size in logical pixels.
pub const BASE_FONT_SIZE: f32 = 14.0;

/// Graphite neutral ramp from black through near-white.
pub const BLACK: u32 = 0x000000;
pub const NEAR_BLACK: u32 = 0x111111;
pub const MILD_BLACK: u32 = 0x222222;
pub const DARK_GRAY: u32 = 0x333333;
pub const DIM_GRAY: u32 = 0x444444;
pub const DULL_GRAY: u32 = 0x555555;
pub const LOWER_GRAY: u32 = 0x666666;
pub const MIDDLE_GRAY: u32 = 0x777777;
pub const UPPER_GRAY: u32 = 0x888888;
pub const PALE_GRAY: u32 = 0x999999;
pub const SOFT_GRAY: u32 = 0xaaaaaa;
pub const LIGHT_GRAY: u32 = 0xbbbbbb;
pub const BRIGHT_GRAY: u32 = 0xcccccc;
pub const MILD_WHITE: u32 = 0xdddddd;
pub const NEAR_WHITE: u32 = 0xeeeeee;
pub const WHITE: u32 = 0xffffff;
/// Restrained interaction accent used for focus and docking feedback.
pub const ACCENT: u32 = 0x5fb8a8;
