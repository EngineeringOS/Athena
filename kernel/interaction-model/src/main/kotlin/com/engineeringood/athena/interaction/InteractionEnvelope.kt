package com.engineeringood.athena.interaction

enum class InteractionPayloadKind {
    SUBJECTS,
    ACTIONS,
    COMMAND,
    PREVIEW,
    REVEAL,
    DIAGNOSTIC,
    PROOF,
}

data class InteractionEnvelope(
    val requestId: String,
    val activeSourceUri: String? = null,
    val activeSourceRevision: String? = null,
    val payloadKind: InteractionPayloadKind,
    val payload: Map<String, String>,
    val adapterMetadata: Map<String, String> = emptyMap(),
    val schemaVersion: String = M29_SCHEMA_VERSION,
) {
    init {
        require(requestId.isNotBlank()) { "Interaction envelope request id must not be blank." }
    }

    companion object {
        const val M29_SCHEMA_VERSION = "m29.interaction.v1"

        fun validateSchemaVersion(schemaVersion: String): List<InteractionDiagnostic> {
            return if (schemaVersion == M29_SCHEMA_VERSION) {
                emptyList()
            } else {
                listOf(
                    InteractionDiagnostic(
                        code = InteractionDiagnosticCode.TRANSPORT_UNSUPPORTED_VERSION,
                        severity = InteractionDiagnosticSeverity.ERROR,
                        message = "Unsupported interaction payload schema version: $schemaVersion.",
                        retryable = false,
                    ),
                )
            }
        }
    }
}
