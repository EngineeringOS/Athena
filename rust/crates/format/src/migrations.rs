//! Explicit schema admission and migration entrypoint.

use crate::{
    CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError,
    MINIMUM_SUPPORTED_SCHEMA_VERSION,
};

/// Admits the current schema and rejects prototypes or unsupported versions.
///
/// Schema 3 is the clean folio-based baseline. Future migrations begin from
/// this function without reinterpreting prototype sheet data.
pub fn migrate(envelope: DocumentEnvelope) -> Result<DocumentEnvelope, FormatError> {
    if envelope.format != DOCUMENT_FORMAT {
        return Err(FormatError::UnsupportedFormat {
            actual: envelope.format,
            expected: DOCUMENT_FORMAT,
        });
    }
    if envelope.schema_version < MINIMUM_SUPPORTED_SCHEMA_VERSION && envelope.schema_version > 0 {
        return Err(FormatError::UnsupportedPrototypeSchema {
            actual: envelope.schema_version,
            minimum_supported: MINIMUM_SUPPORTED_SCHEMA_VERSION,
        });
    }
    if envelope.schema_version == 0 || envelope.schema_version > CURRENT_SCHEMA_VERSION {
        return Err(FormatError::UnsupportedSchemaVersion {
            actual: envelope.schema_version,
            current: CURRENT_SCHEMA_VERSION,
        });
    }

    envelope.project.validate()?;
    if envelope.project.folio(envelope.active_folio_id).is_none() {
        return Err(FormatError::InvalidActiveFolio {
            active_folio_id: envelope.active_folio_id,
        });
    }
    Ok(envelope)
}
