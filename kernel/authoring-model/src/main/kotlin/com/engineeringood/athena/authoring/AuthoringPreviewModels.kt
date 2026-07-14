package com.engineeringood.athena.authoring

import com.engineeringood.athena.ir.StableSemanticIdentity

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
    CONNECT,
    REVEAL,
}

/** Enumerates the lifecycle state of one authoring preview contract. */
enum class AuthoringPreviewStatus {
    PENDING_REVIEW,
    ACCEPTED,
    REJECTED,
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
)

/** Enumerates the explicit decisions available for one authoring preview. */
enum class AuthoringPreviewDecisionKind {
    ACCEPT,
    REJECT,
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
