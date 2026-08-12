use crate::{CURRENT_SCHEMA_VERSION, DOCUMENT_FORMAT, DocumentEnvelope, FormatError};

/// Migrate an envelope to the latest schema without changing project identity.
pub fn migrate(mut envelope: DocumentEnvelope) -> Result<DocumentEnvelope, FormatError> {
    if envelope.format != DOCUMENT_FORMAT {
        return Err(FormatError::UnsupportedFormat {
            actual: envelope.format,
            expected: DOCUMENT_FORMAT,
        });
    }

    if envelope.schema_version > CURRENT_SCHEMA_VERSION || envelope.schema_version == 0 {
        return Err(FormatError::UnsupportedSchemaVersion {
            actual: envelope.schema_version,
            current: CURRENT_SCHEMA_VERSION,
        });
    }

    while envelope.schema_version < CURRENT_SCHEMA_VERSION {
        migrate_one_version(&mut envelope);
    }

    envelope.project.validate()?;
    Ok(envelope)
}

fn migrate_one_version(envelope: &mut DocumentEnvelope) {
    match envelope.schema_version {
        // Version 2 formalized the outer schema version. The nested domain
        // records already deserialize using defaults, so their entity IDs and
        // values pass through unchanged.
        1 => envelope.schema_version = 2,
        _ => unreachable!("versions are checked before migration"),
    }
}
