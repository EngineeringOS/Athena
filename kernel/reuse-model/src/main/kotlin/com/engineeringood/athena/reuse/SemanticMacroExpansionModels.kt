package com.engineeringood.athena.reuse

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Stable identifier for one accepted Semantic Macro expansion record.
 *
 * Accepted expansion is a reusable traceability contract. It is not the mutation engine itself.
 */
@JvmInline
value class SemanticMacroExpansionId(val value: String) {
    override fun toString(): String = value
}

/**
 * Semantic origin facts preserved for one accepted expansion.
 *
 * This captures which macro definition, instantiation, package source, and parameter values led to
 * later canonical consequences without redefining package resolution or runtime orchestration.
 */
data class ExpansionOrigin(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val packageBinding: SemanticMacroPackageBinding,
    val parameterValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue> = emptyMap(),
)

/**
 * Membership fact connecting one accepted expansion to one canonical semantic subject.
 *
 * Membership stays semantic-first so future inspection surfaces can reason about accepted expansion
 * without depending on renderer-local or widget-local state.
 */
data class ExpansionMembership(
    val instantiationId: SemanticMacroInstantiationId,
    val subjectId: StableSemanticIdentity,
    val role: String? = null,
)

/**
 * Accepted expansion contract preserved after preview approval.
 *
 * This records traceability facts only. Later stories may consume it to drive M8 handoff, but this
 * model does not create a second mutation path.
 */
data class SemanticMacroAcceptedExpansion(
    val expansionId: SemanticMacroExpansionId,
    val previewId: SemanticMacroPreviewId,
    val origin: ExpansionOrigin,
    val memberships: List<ExpansionMembership> = emptyList(),
)
