package com.engineeringood.athena.reuse

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Stable identifier for one inspectable Semantic Macro preview.
 *
 * A preview is the review-first artifact emitted before any later accepted expansion or M8 handoff.
 */
@JvmInline
value class SemanticMacroPreviewId(val value: String) {
    override fun toString(): String = value
}

/** Enumerates the first semantic consequence kinds inspectable through Semantic Macro preview. */
enum class SemanticMacroPreviewChangeKind {
    CREATE,
    UPDATE,
    CONNECT,
    REVEAL,
}

/** Enumerates the lifecycle state of one Semantic Macro preview contract. */
enum class SemanticMacroPreviewStatus {
    PENDING_REVIEW,
    ACCEPTED,
    REJECTED,
}

/**
 * One inspectable semantic consequence within a Semantic Macro preview.
 *
 * The preview stays canonical-identity-first so renderer ids and widget ids remain downstream.
 */
data class SemanticMacroPreviewChange(
    val kind: SemanticMacroPreviewChangeKind,
    val title: String,
    val summary: String? = null,
    val affectedSubjectIdentities: Set<StableSemanticIdentity> = emptySet(),
)

/** Stable origin anchor published for one previewed consequence before canonical identities exist. */
data class SemanticMacroPreviewOriginAnchor(
    val anchorId: String,
    val subjectKind: String,
    val templateId: String,
    val derivedSubjectIdentity: StableSemanticIdentity? = null,
)

/** Inspectable component consequence included in one Semantic Macro preview. */
data class SemanticMacroPreviewComponent(
    val templateId: String,
    val conceptId: String,
    val implementationId: String? = null,
    val title: String,
    val summary: String? = null,
    val originAnchorId: String,
    val properties: Map<String, SemanticMacroParameterValue> = emptyMap(),
    val tags: Set<String> = emptySet(),
)

/** Inspectable semantic-port consequence included in one Semantic Macro preview. */
data class SemanticMacroPreviewPort(
    val componentTemplateId: String,
    val portRoleId: String,
    val title: String,
    val originAnchorId: String,
)

/** Inspectable connection consequence included in one Semantic Macro preview. */
data class SemanticMacroPreviewConnection(
    val templateId: String,
    val fromComponentTemplateId: String,
    val fromPortRoleId: String,
    val toComponentTemplateId: String,
    val toPortRoleId: String,
    val title: String,
    val summary: String? = null,
    val originAnchorId: String,
)

/** Inspectable presentation-facing consequence carried by one previewed semantic expansion. */
data class SemanticMacroPreviewPresentationConsequence(
    val scope: String,
    val templateId: String? = null,
    val hintType: String,
    val attributes: Map<String, String> = emptyMap(),
    val originAnchorId: String,
)

/**
 * Review-first preview published for one configured Semantic Macro instantiation.
 *
 * This contract records inspectable consequence meaning only. It does not execute mutation,
 * catalog loading, or renderer behavior by itself.
 */
data class SemanticMacroPreview(
    val previewId: SemanticMacroPreviewId,
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val title: String,
    val status: SemanticMacroPreviewStatus = SemanticMacroPreviewStatus.PENDING_REVIEW,
    val changes: List<SemanticMacroPreviewChange>,
    val components: List<SemanticMacroPreviewComponent> = emptyList(),
    val ports: List<SemanticMacroPreviewPort> = emptyList(),
    val connections: List<SemanticMacroPreviewConnection> = emptyList(),
    val originAnchors: List<SemanticMacroPreviewOriginAnchor> = emptyList(),
    val presentationConsequences: List<SemanticMacroPreviewPresentationConsequence> = emptyList(),
    val warnings: List<String> = emptyList(),
)
