use athena_domain::Project;

use crate::{FormatError, decode_json, encode_json};

/// Stateless byte codec used by filesystem and browser persistence adapters.
pub struct SnapshotStore;

impl SnapshotStore {
    /// Encode a project as UTF-8 JSON bytes without performing any I/O.
    pub fn encode_snapshot(project: &Project) -> Result<Vec<u8>, FormatError> {
        Ok(encode_json(project)?.into_bytes())
    }

    /// Decode UTF-8 JSON bytes without performing any I/O.
    pub fn decode_snapshot(bytes: &[u8]) -> Result<Project, FormatError> {
        let json = std::str::from_utf8(bytes).map_err(|error| {
            FormatError::InvalidJson(serde_json::Error::io(std::io::Error::new(
                std::io::ErrorKind::InvalidData,
                error,
            )))
        })?;
        decode_json(json)
    }
}
