package com.engineeringood.athena.authoring

import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@JvmInline
value class SemanticAuthoringTransactionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Semantic authoring transaction id must not be blank." }
    }

    override fun toString(): String = value
}

data class AuthoringRevisionGuard(
    val semanticSnapshotId: String,
    val sourceUri: String,
    val documentVersion: Int,
    val contentSha256: String,
) {
    init {
        require(semanticSnapshotId.isNotBlank()) { "Authoring semantic snapshot id must not be blank." }
        require(sourceUri.isNotBlank()) { "Authoring source URI must not be blank." }
        require(documentVersion >= 0) { "Authoring document version must not be negative." }
        require(SHA_256_PATTERN.matches(contentSha256)) { "Authoring content SHA-256 must be 64 lowercase hexadecimal characters." }
    }

    companion object {
        fun from(
            semanticSnapshotId: String,
            sourceUri: String,
            documentVersion: Int,
            sourceText: String,
        ): AuthoringRevisionGuard = AuthoringRevisionGuard(
            semanticSnapshotId = semanticSnapshotId,
            sourceUri = sourceUri,
            documentVersion = documentVersion,
            contentSha256 = MessageDigest.getInstance("SHA-256")
                .digest(sourceText.toByteArray(StandardCharsets.UTF_8))
                .joinToString(separator = "") { byte -> "%02x".format(byte) },
        )
    }
}

enum class AuthoringLifecycleState {
    REQUESTED,
    DISCOVERED,
    VALIDATED,
    PREVIEWING,
    ACCEPTED,
    REJECTED,
    MUTATION_PENDING,
    COMMITTED,
    RECOMPILED,
    REPROJECTED,
    BLOCKED,
    STALE,
    CANCELLED,
    PROJECTION_FAILED,
}

enum class AuthoringDiagnosticCode(val value: String) {
    TRANSACTION_INTENT_COUNT_UNSUPPORTED("authoring.transaction.intent-count-unsupported"),
    REVISION_GUARD_MISMATCH("authoring.preview.stale"),
    SOURCE_CONFLICT("authoring.source.conflict"),
    SOURCE_INVALID("authoring.source.invalid"),
    STOP_DOWNSTREAM("authoring.validation.stop-downstream"),
    PROJECTION_FAILED_AFTER_COMMIT("authoring.projection.failed-after-commit"),
    REMOVAL_DEPENDENCIES("authoring.removal.dependencies"),
    ENTITY_TAG_DUPLICATE("authoring.entity.tag-duplicate"),
    CONCEPT_TEMPLATE_MISSING("authoring.template.missing"),
    CONCEPT_TEMPLATE_IDENTITY_MISMATCH("authoring.template.identity-mismatch"),
    NESTED_PORT_ANATOMY_INVALID("authoring.entity.nested-port-anatomy-invalid"),
    REPRESENTATION_UNRESOLVED("authoring.representation.unresolved"),
    COMPOSITION_UNSATISFIED("authoring.composition.unsatisfied"),
    RELATIONSHIP_INCOMPATIBLE("authoring.relationship.incompatible"),
    RELATIONSHIP_SUBJECT_UNRESOLVED("authoring.relationship.subject-unresolved"),
    RELATIONSHIP_SELF("authoring.relationship.self"),
    RELATIONSHIP_DUPLICATE("authoring.relationship.duplicate"),
    RELATIONSHIP_TYPE_UNSUPPORTED("authoring.relationship.type-unsupported"),
}

enum class AuthoringDiagnosticAuthority {
    CAPABILITY_REGISTRY,
    TRANSACTION_RUNTIME,
    REVISION_GUARD,
    SEMANTIC_VALIDATION,
    SOURCE_PLANNING,
    PARSER,
    MUTATION_AUTHORITY,
    COMPILER,
    PROJECTION,
    REPRESENTATION,
    COMPOSITION,
}

enum class AuthoringDiagnosticSeverity {
    INFO,
    WARNING,
    ERROR,
}

enum class AuthoringRecoveryAction {
    REFRESH_PREVIEW,
    FIX_SOURCE,
    RETRY_PROJECTION,
}

data class AuthoringSourceRange(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

data class AuthoringDiagnostic(
    val code: AuthoringDiagnosticCode,
    val message: String,
    val authority: AuthoringDiagnosticAuthority = AuthoringDiagnosticAuthority.TRANSACTION_RUNTIME,
    val lifecycleStage: AuthoringLifecycleState = AuthoringLifecycleState.BLOCKED,
    val severity: AuthoringDiagnosticSeverity = AuthoringDiagnosticSeverity.ERROR,
    val subjectId: String? = null,
    val sourceRange: AuthoringSourceRange? = null,
    val relatedIds: List<String> = emptyList(),
    val recoveryAction: AuthoringRecoveryAction? = null,
)

enum class AuthoringValidationStage {
    INTENT_SHAPE,
    CAPABILITY_EVIDENCE,
    ACTOR_SUBJECT_ELIGIBILITY,
    REVISION_GUARD,
    SEMANTIC_RULES,
    SOURCE_PLANNING,
    PARSER_VALIDATION,
    SEMANTIC_VALIDATION,
    PREVIEW_ELIGIBILITY,
}

enum class AuthoringTransactionValidationStatus {
    NOT_VALIDATED,
    VALID,
    BLOCKED,
}

data class AuthoringTransactionValidation(
    val status: AuthoringTransactionValidationStatus = AuthoringTransactionValidationStatus.NOT_VALIDATED,
    val completedStages: List<AuthoringValidationStage> = emptyList(),
    val diagnostics: List<AuthoringDiagnostic> = emptyList(),
)

data class AuthoringTransactionProvenance(
    val actor: String,
    val origin: AuthoringOrigin,
    val reason: String? = null,
    val timestamp: String? = null,
    val confidence: Double? = null,
) {
    init {
        require(actor.isNotBlank()) { "Authoring transaction actor must not be blank." }
    }
}

data class SemanticAuthoringResult(
    val lifecycleState: AuthoringLifecycleState,
    val committedRevision: AuthoringRevisionGuard? = null,
    val mutationId: String? = null,
    val affectedSemanticIds: List<String> = emptyList(),
    val projectionOccurrenceIds: List<String> = emptyList(),
    val diagnostics: List<AuthoringDiagnostic> = emptyList(),
) {
    init {
        if (lifecycleState == AuthoringLifecycleState.PROJECTION_FAILED) {
            require(!mutationId.isNullOrBlank()) { "Projection failure after commit requires a mutation id." }
            require(committedRevision != null) { "Projection failure after commit requires a committed revision." }
            require(diagnostics.any { it.code == AuthoringDiagnosticCode.PROJECTION_FAILED_AFTER_COMMIT }) {
                "Projection failure after commit requires its stable diagnostic."
            }
        }
    }
}

data class SemanticAuthoringTransaction(
    val transactionId: SemanticAuthoringTransactionId,
    val intent: AuthoringIntent,
    val capabilityEvidence: AuthoringCapabilityEvidence,
    val revisionGuard: AuthoringRevisionGuard,
    val preview: AuthoringPreview?,
    val validation: AuthoringTransactionValidation,
    val provenance: AuthoringTransactionProvenance,
    val decision: AuthoringPreviewDecision? = null,
    val lifecycleState: AuthoringLifecycleState,
    val mutationId: String? = null,
    val result: SemanticAuthoringResult? = null,
    val diagnostics: List<AuthoringDiagnostic> = emptyList(),
)

sealed interface SemanticAuthoringTransactionCreationResult

data class SemanticAuthoringTransactionCreated(
    val transaction: SemanticAuthoringTransaction,
) : SemanticAuthoringTransactionCreationResult

data class SemanticAuthoringTransactionRejected(
    val transactionId: SemanticAuthoringTransactionId,
    val diagnostic: AuthoringDiagnostic,
) : SemanticAuthoringTransactionCreationResult

object SemanticAuthoringTransactionFactory {
    fun create(
        transactionId: SemanticAuthoringTransactionId,
        intents: List<AuthoringIntent>,
        capabilityEvidence: AuthoringCapabilityEvidence,
        revisionGuard: AuthoringRevisionGuard,
        preview: AuthoringPreview?,
        provenance: AuthoringTransactionProvenance,
    ): SemanticAuthoringTransactionCreationResult {
        if (intents.size != 1) {
            return SemanticAuthoringTransactionRejected(
                transactionId = transactionId,
                diagnostic = AuthoringDiagnostic(
                    code = AuthoringDiagnosticCode.TRANSACTION_INTENT_COUNT_UNSUPPORTED,
                    message = "Semantic authoring transaction v0 requires exactly one mutable intent; received ${intents.size}.",
                    authority = AuthoringDiagnosticAuthority.TRANSACTION_RUNTIME,
                    lifecycleStage = AuthoringLifecycleState.BLOCKED,
                ),
            )
        }
        if (preview?.revisionGuard != null && preview.revisionGuard != revisionGuard) {
            return SemanticAuthoringTransactionRejected(
                transactionId = transactionId,
                diagnostic = AuthoringDiagnostic(
                    code = AuthoringDiagnosticCode.REVISION_GUARD_MISMATCH,
                    message = "Authoring preview Revision Guard does not match the transaction Revision Guard.",
                    authority = AuthoringDiagnosticAuthority.REVISION_GUARD,
                    lifecycleStage = AuthoringLifecycleState.STALE,
                    recoveryAction = AuthoringRecoveryAction.REFRESH_PREVIEW,
                ),
            )
        }

        return SemanticAuthoringTransactionCreated(
            transaction = SemanticAuthoringTransaction(
                transactionId = transactionId,
                intent = intents.single(),
                capabilityEvidence = capabilityEvidence,
                revisionGuard = revisionGuard,
                preview = preview,
                validation = AuthoringTransactionValidation(),
                provenance = provenance,
                lifecycleState = if (preview == null) AuthoringLifecycleState.REQUESTED else AuthoringLifecycleState.PREVIEWING,
            ),
        )
    }
}

private val SHA_256_PATTERN = Regex("[0-9a-f]{64}")
