//! Versioned, serde-backed project persistence contracts.

mod envelope;
mod migrations;
mod snapshot;

pub use envelope::{
    CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError, decode_json,
    encode_json,
};
pub use migrations::migrate;
pub use snapshot::SnapshotStore;

/// Compile-time marker for the format crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct FormatContract;

#[cfg(test)]
mod tests {
    use super::FormatContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = FormatContract;
        assert_eq!(marker, FormatContract);
    }
}
