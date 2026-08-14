//! Canonical shell protocol fixture shared by native and browser certification.
//!
//! Stable identities make complete effect traces and serialized shell hashes
//! comparable across platform adapters without UI-toolkit state entering the
//! application contract.

use uuid::Uuid;

use crate::{AthenaMessage, DockPlacement, DockTarget, GroupId, ShellMessage, SplitId, TabId};

/// Builds the canonical M006 recursive-shell transition sequence.
#[must_use]
pub fn canonical_m006_shell_messages() -> Vec<AthenaMessage> {
    let group = |value| GroupId::from_uuid(Uuid::from_u128(value));
    let split = |value| SplitId::from_uuid(Uuid::from_u128(value));
    let tab = |value| TabId::from_uuid(Uuid::from_u128(value));

    vec![
        ShellMessage::Request.into(),
        ShellMessage::ActivateTab {
            group_id: group(4),
            tab_id: tab(10),
        }
        .into(),
        ShellMessage::ReorderTab {
            group_id: group(4),
            tab_id: tab(10),
            to: 0,
        }
        .into(),
        ShellMessage::MoveTab {
            tab_id: tab(12),
            target_group_id: group(8),
            to: 1,
        }
        .into(),
        ShellMessage::SplitGroup {
            tab_id: tab(9),
            target_group_id: group(6),
            placement: DockPlacement::Left,
            new_group_id: group(100),
            new_split_id: split(101),
        }
        .into(),
        ShellMessage::BeginResize {
            split_id: split(1),
            before_index: 0,
            available_px: 1_000,
        }
        .into(),
        ShellMessage::ResizeAdjacent {
            split_id: split(1),
            before_index: 0,
            delta_px: 50,
        }
        .into(),
        ShellMessage::AbortResize.into(),
        ShellMessage::ResetAdjacent {
            split_id: split(1),
            before_index: 0,
        }
        .into(),
        ShellMessage::ClosePanel { tab_id: tab(17) }.into(),
        ShellMessage::ReopenPanel { tab_id: tab(17) }.into(),
        ShellMessage::SetDocumentFocus { focused: true }.into(),
        ShellMessage::SetDocumentFocus { focused: false }.into(),
        ShellMessage::OpenOverlay { group_id: group(5) }.into(),
        ShellMessage::CloseOverlay.into(),
        ShellMessage::SetDockPreview {
            target: Some(DockTarget {
                group_id: group(6),
                placement: DockPlacement::Left,
            }),
        }
        .into(),
        ShellMessage::SetDockPreview { target: None }.into(),
    ]
}
