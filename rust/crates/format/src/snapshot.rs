//! Stateless UTF-8 byte codec shared by native and browser adapters.

use crate::{FormatError, PersistedProject, decode_json, encode_json};

/// Pure byte codec with no filesystem, browser, or cloud dependencies.
pub struct SnapshotStore;

impl SnapshotStore {
    /// Encodes validated local document state as deterministic UTF-8 JSON.
    pub fn encode_snapshot(document: &PersistedProject) -> Result<Vec<u8>, FormatError> {
        Ok(encode_json(document)?.into_bytes())
    }

    /// Decodes UTF-8 JSON bytes into validated local document state.
    pub fn decode_snapshot(bytes: &[u8]) -> Result<PersistedProject, FormatError> {
        let json = std::str::from_utf8(bytes).map_err(|error| {
            FormatError::InvalidJson(serde_json::Error::io(std::io::Error::new(
                std::io::ErrorKind::InvalidData,
                error,
            )))
        })?;
        decode_json(json)
    }
}
