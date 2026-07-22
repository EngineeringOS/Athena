package com.engineeringood.athena.authoring

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.component.EngineeringConceptPortDirection
import com.engineeringood.athena.component.EngineeringSignalOrMedium

/**
 * Stable identifier for one inspectable authoring preview.
 *
 * A preview is the review-first artifact emitted after intent expansion and before any later M8
 * acceptance handoff.
 */
@JvmInline
value class AuthoringPreviewId(val value: String) {
    override fun toString(): String = value
}

/** Enumerates the first narrow semantic consequence kinds exposed through guided authoring preview. */
enum class AuthoringPreviewChangeKind {
    CREATE,
    UPDATE,
    REMOVE,
    CONNECT,
    REVEAL,
}

/** Enumerates the lifecycle state of one authoring preview contract. */
enum class AuthoringPreviewStatus {
    PENDING_REVIEW,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    BLOCKED,
    STALE,
}

/**
 * One inspectable semantic consequence within an authoring preview.
 *
 * The preview may reference one or more canonical semantic identities so later review and reveal
 * stay canonical-identity-first.
 */
data class AuthoringPreviewChange(
    val kind: AuthoringPreviewChangeKind,
    val title: String,
    val summary: String? = null,
    val affectedSubjectIdentities: Set<StableSemanticIdentity> = emptySet(),
)

/**
 * Review-first preview emitted for one guided authoring intent.
 *
 * This contract remains inspectable and deterministic. It does not execute mutation by itself.
 */
data class AuthoringPreview(
    val previewId: AuthoringPreviewId,
    val intentId: AuthoringIntentId,
    val title: String,
    val status: AuthoringPreviewStatus = AuthoringPreviewStatus.PENDING_REVIEW,
    val changes: List<AuthoringPreviewChange>,
    val warnings: List<String> = emptyList(),
    val revisionGuard: AuthoringRevisionGuard? = null,
    val dependencyImpact: AuthoringDependencyImpact = AuthoringDependencyImpact(),
    val acceptanceEligibility: AuthoringAcceptanceEligibility = AuthoringAcceptanceEligibility(),
    val entityCreationEvidence: GovernedEntityCreationPreviewEvidence? = null,
    val relationshipEvidence: GovernedRelationshipPreviewEvidence? = null,
)

/** One nested semantic port that will be created with its owning entity. */
data class AuthoringNestedPortCreationEvidence(
    val name: String,
    val direction: EngineeringConceptPortDirection,
    val signalOrMedium: EngineeringSignalOrMedium,
    val semanticId: String,
)

/** Exact backend-authored edit admitted for one revision-bound preview. */
data class AuthoringSourceEditEvidence(
    val revisionGuard: AuthoringRevisionGuard,
    val sourceUri: String,
    val startOffset: Int,
    val endOffset: Int,
    val admittedText: String,
    val selectionStartOffset: Int? = null,
    val selectionEndOffset: Int? = null,
    val affectedSemanticIds: List<String>,
) {
    init {
        require(startOffset >= 0 && endOffset >= startOffset) { "Authoring source edit offsets are invalid." }
        require(sourceUri == revisionGuard.sourceUri) { "Authoring source edit URI must match its Revision Guard." }
        require(affectedSemanticIds.isNotEmpty()) { "Authoring source edit evidence requires affected semantic ids." }
    }
}

/** Semantic and projection facts resolved before an entity-creation preview can be accepted. */
data class GovernedEntityCreationPreviewEvidence(
    val canonicalTag: String,
    val semanticType: String,
    val model: String?,
    val nestedPorts: List<AuthoringNestedPortCreationEvidence>,
    val affectedSemanticIds: List<String>,
    val sourceEdit: AuthoringSourceEditEvidence,
    val representationId: String,
    val compositionTargetId: String,
    val projectionOccurrenceIds: List<String>,
)

enum class AuthoringRelationshipCompatibility {
    NOT_EVALUATED,
    COMPATIBLE,
    INCOMPATIBLE,
}

/** Route-derived evidence shown during relationship review; it never defines semantic truth. */
data class AuthoringRelationshipRoutePreviewEvidence(
    val routeId: String,
    val quality: String,
    val sourceAnchorId: String? = null,
    val targetAnchorId: String? = null,
    val pointCount: Int,
) {
    init {
        require(routeId.isNotBlank()) { "Relationship route preview id must not be blank." }
        require(quality.isNotBlank()) { "Relationship route preview quality must not be blank." }
        require(pointCount >= 0) { "Relationship route preview point count must not be negative." }
    }
}

/** Semantic, source, and optional downstream route facts for one governed relationship preview. */
data class GovernedRelationshipPreviewEvidence(
    val sourceSubjectId: String,
    val targetSubjectId: String,
    val relationshipType: SemanticRelationshipType,
    val compatibility: AuthoringRelationshipCompatibility,
    val affectedSemanticIds: List<String>,
    val sourceEdit: AuthoringSourceEditEvidence? = null,
    val routePreview: AuthoringRelationshipRoutePreviewEvidence? = null,
)

data class AuthoringDependencyImpact(
    val dependentRelationshipIds: List<String> = emptyList(),
    val projectionOccurrenceIds: List<String> = emptyList(),
) {
    val allDependencyIds: List<String>
        get() = (dependentRelationshipIds + projectionOccurrenceIds).distinct().sorted()
}

data class AuthoringAcceptanceEligibility(
    val eligible: Boolean = true,
    val diagnostics: List<AuthoringDiagnostic> = emptyList(),
) {
    init {
        require(eligible || diagnostics.isNotEmpty()) { "Ineligible authoring preview requires diagnostics." }
    }
}

/** Enumerates the explicit decisions available for one authoring preview. */
enum class AuthoringPreviewDecisionKind {
    ACCEPT,
    REJECT,
    CANCEL,
}

/**
 * Explicit review decision applied to one authoring preview.
 *
 * Acceptance means the preview is eligible for later handoff into M8 mutation authority.
 * Rejection means canonical state remains unchanged.
 */
sealed interface AuthoringPreviewDecision {
    val kind: AuthoringPreviewDecisionKind
    val previewId: AuthoringPreviewId
    val intentId: AuthoringIntentId
}

/**
 * Accepts one preview for later mutation handoff.
 *
 * This contract records approval only. It does not execute the mutation itself.
 */
data class AcceptAuthoringPreviewDecision(
    override val previewId: AuthoringPreviewId,
    override val intentId: AuthoringIntentId,
    val note: String? = null,
) : AuthoringPreviewDecision {
    override val kind: AuthoringPreviewDecisionKind = AuthoringPreviewDecisionKind.ACCEPT
}

/**
 * Rejects one preview and records an optional reason.
 *
 * Rejection explicitly leaves canonical engineering state unchanged.
 */
data class RejectAuthoringPreviewDecision(
    override val previewId: AuthoringPreviewId,
    override val intentId: AuthoringIntentId,
    val reason: String? = null,
) : AuthoringPreviewDecision {
    override val kind: AuthoringPreviewDecisionKind = AuthoringPreviewDecisionKind.REJECT
}

/** Cancels one preview without accepting or rejecting its engineering proposal. */
data class CancelAuthoringPreviewDecision(
    override val previewId: AuthoringPreviewId,
    override val intentId: AuthoringIntentId,
    val reason: String? = null,
) : AuthoringPreviewDecision {
    override val kind: AuthoringPreviewDecisionKind = AuthoringPreviewDecisionKind.CANCEL
}
