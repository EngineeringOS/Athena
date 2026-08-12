use athena_domain::{DomainError, Project};
use serde::{Deserialize, Serialize};
use thiserror::Error;

use crate::migrate;

/// Stable identifier for Athena electrical project documents.
pub const DOCUMENT_FORMAT: &str = "athena-electrical-project";

/// Most recent document schema understood by this build.
pub const CURRENT_SCHEMA_VERSION: u32 = 2;

/// The versioned outer record for a persisted project.
///
/// Deliberately denies unknown top-level fields so callers can detect a wrong
/// document type, while nested domain records remain serde-forward-compatible.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(deny_unknown_fields)]
pub struct DocumentEnvelope {
    pub format: String,
    pub schema_version: u32,
    pub project: Project,
}

impl DocumentEnvelope {
    #[must_use]
    pub fn current(project: Project) -> Self {
        Self {
            format: DOCUMENT_FORMAT.into(),
            schema_version: CURRENT_SCHEMA_VERSION,
            project,
        }
    }
}

#[derive(Debug, Error)]
pub enum FormatError {
    #[error("document JSON is invalid: {0}")]
    InvalidJson(#[from] serde_json::Error),
    #[error("unsupported document format {actual:?}; expected {expected:?}")]
    UnsupportedFormat {
        actual: String,
        expected: &'static str,
    },
    #[error(
        "unsupported document schema version {actual}; this build supports versions through {current}"
    )]
    UnsupportedSchemaVersion { actual: u32, current: u32 },
    #[error(transparent)]
    InvalidProject(#[from] DomainError),
}

/// Encode a validated project as its current versioned JSON document.
pub fn encode_json(project: &Project) -> Result<String, FormatError> {
    project.validate()?;
    Ok(serde_json::to_string(&DocumentEnvelope::current(
        project.clone(),
    ))?)
}

/// Decode a versioned JSON document, migrating it to the current schema first.
pub fn decode_json(json: impl AsRef<str>) -> Result<Project, FormatError> {
    let envelope = serde_json::from_str(json.as_ref())?;
    Ok(migrate(envelope)?.project)
}
