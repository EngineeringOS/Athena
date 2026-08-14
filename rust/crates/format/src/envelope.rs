//! Schema-versioned persistence envelope and validated local document state.

use athena_domain::{DomainError, FolioId, Project};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use thiserror::Error;
use uuid::Uuid;

use crate::migrate;

/// Stable identifier for Athena electrical project documents.
pub const DOCUMENT_FORMAT: &str = "athena-electrical-project";

/// Most recent document schema understood by this build.
pub const CURRENT_SCHEMA_VERSION: u32 = 3;

/// Earliest non-prototype schema accepted by this build.
pub const MINIMUM_SUPPORTED_SCHEMA_VERSION: u32 = 3;

/// Persisted semantic project plus locally restorable document view state.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct PersistedProject {
    /// Platform-neutral electrical project aggregate.
    pub project: Project,
    /// Folio restored as active when the project is reopened.
    pub active_folio_id: FolioId,
}

impl PersistedProject {
    /// Creates a persisted document after validating project and active folio.
    pub fn new(project: Project, active_folio_id: FolioId) -> Result<Self, FormatError> {
        project.validate()?;
        if project.folio(active_folio_id).is_none() {
            return Err(FormatError::InvalidActiveFolio { active_folio_id });
        }
        Ok(Self {
            project,
            active_folio_id,
        })
    }

    fn validate(&self) -> Result<(), FormatError> {
        self.project.validate()?;
        if self.project.folio(self.active_folio_id).is_none() {
            return Err(FormatError::InvalidActiveFolio {
                active_folio_id: self.active_folio_id,
            });
        }
        Ok(())
    }
}

/// Versioned outer record for one persisted Athena project.
///
/// Unknown top-level fields are denied so callers can distinguish this format
/// from unrelated JSON while nested domain records remain forward-compatible.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
#[serde(deny_unknown_fields)]
pub struct DocumentEnvelope {
    /// Stable format discriminator.
    pub format: String,
    /// Schema version for migration and compatibility checks.
    pub schema_version: u32,
    /// Electrical project aggregate.
    pub project: Project,
    /// Locally restored active folio identity.
    pub active_folio_id: FolioId,
}

impl DocumentEnvelope {
    /// Wraps a validated document in the current schema.
    #[must_use]
    pub fn current(document: PersistedProject) -> Self {
        Self {
            format: DOCUMENT_FORMAT.into(),
            schema_version: CURRENT_SCHEMA_VERSION,
            project: document.project,
            active_folio_id: document.active_folio_id,
        }
    }

    /// Converts the envelope payload into validated local document state.
    pub fn into_persisted_project(self) -> Result<PersistedProject, FormatError> {
        PersistedProject::new(self.project, self.active_folio_id)
    }
}

/// Typed failures produced by the schema codec and validation boundary.
#[derive(Debug, Error)]
pub enum FormatError {
    /// Input is not syntactically valid JSON or does not match schema types.
    #[error("document JSON is invalid: {0}")]
    InvalidJson(#[from] serde_json::Error),
    /// A persisted identity cannot be parsed as a UUID.
    #[error("document identity at {path} is not a UUID: {value:?}")]
    MalformedIdentity { path: String, value: String },
    /// The outer format discriminator does not identify an Athena project.
    #[error("unsupported document format {actual:?}; expected {expected:?}")]
    UnsupportedFormat {
        actual: String,
        expected: &'static str,
    },
    /// A pre-M005 prototype schema is intentionally not migrated.
    #[error(
        "prototype schema {actual} is unsupported; minimum supported schema is {minimum_supported}"
    )]
    UnsupportedPrototypeSchema { actual: u32, minimum_supported: u32 },
    /// The document is newer than the current build or has schema zero.
    #[error(
        "unsupported document schema version {actual}; this build supports versions through {current}"
    )]
    UnsupportedSchemaVersion { actual: u32, current: u32 },
    /// The saved active folio does not exist in the project aggregate.
    #[error("active folio {active_folio_id} does not exist in the project")]
    InvalidActiveFolio { active_folio_id: FolioId },
    /// The decoded electrical aggregate violates domain invariants.
    #[error(transparent)]
    InvalidProject(#[from] DomainError),
}

#[derive(Deserialize)]
struct EnvelopeHeader {
    format: String,
    schema_version: u32,
}

/// Encodes a validated persisted project as deterministic current-schema JSON.
pub fn encode_json(document: &PersistedProject) -> Result<String, FormatError> {
    document.validate()?;
    let mut json = serde_json::to_string(&DocumentEnvelope::current(document.clone()))?;
    json.push('\n');
    Ok(json)
}

/// Decodes and validates a versioned persisted project.
pub fn decode_json(json: impl AsRef<str>) -> Result<PersistedProject, FormatError> {
    let value: Value = serde_json::from_str(json.as_ref())?;
    let header: EnvelopeHeader = serde_json::from_value(value.clone())?;
    validate_header(&header)?;
    validate_identity_fields(&value)?;
    let envelope: DocumentEnvelope = serde_json::from_value(value)?;
    migrate(envelope)?.into_persisted_project()
}

fn validate_header(header: &EnvelopeHeader) -> Result<(), FormatError> {
    if header.format != DOCUMENT_FORMAT {
        return Err(FormatError::UnsupportedFormat {
            actual: header.format.clone(),
            expected: DOCUMENT_FORMAT,
        });
    }
    if header.schema_version < MINIMUM_SUPPORTED_SCHEMA_VERSION && header.schema_version > 0 {
        return Err(FormatError::UnsupportedPrototypeSchema {
            actual: header.schema_version,
            minimum_supported: MINIMUM_SUPPORTED_SCHEMA_VERSION,
        });
    }
    if header.schema_version == 0 || header.schema_version > CURRENT_SCHEMA_VERSION {
        return Err(FormatError::UnsupportedSchemaVersion {
            actual: header.schema_version,
            current: CURRENT_SCHEMA_VERSION,
        });
    }
    Ok(())
}

// Identity preflight turns common corrupted UUID fields into a stable error
// before serde reports a pathless data-shape failure.
fn validate_identity_fields(value: &Value) -> Result<(), FormatError> {
    validate_uuid_at(value, &["active_folio_id"])?;
    validate_uuid_at(value, &["project", "id"])?;
    if let Some(order) = value
        .get("project")
        .and_then(|project| project.get("folio_order"))
        .and_then(Value::as_array)
    {
        for (index, folio_id) in order.iter().enumerate() {
            validate_uuid_value(folio_id, format!("project.folio_order[{index}]"))?;
        }
    }
    if let Some(folios) = value
        .get("project")
        .and_then(|project| project.get("folios"))
        .and_then(Value::as_object)
    {
        for (key, folio) in folios {
            validate_uuid_string(key, format!("project.folios[{key:?}]"))?;
            if let Some(id) = folio.get("id") {
                validate_uuid_value(id, format!("project.folios[{key:?}].id"))?;
            }
        }
    }
    Ok(())
}

fn validate_uuid_at(value: &Value, path: &[&str]) -> Result<(), FormatError> {
    let mut current = value;
    for segment in path {
        let Some(next) = current.get(*segment) else {
            return Ok(());
        };
        current = next;
    }
    validate_uuid_value(current, path.join("."))
}

fn validate_uuid_value(value: &Value, path: String) -> Result<(), FormatError> {
    if let Some(value) = value.as_str() {
        validate_uuid_string(value, path)
    } else {
        Ok(())
    }
}

fn validate_uuid_string(value: &str, path: String) -> Result<(), FormatError> {
    Uuid::parse_str(value)
        .map(|_| ())
        .map_err(|_| FormatError::MalformedIdentity {
            path,
            value: value.to_owned(),
        })
}
