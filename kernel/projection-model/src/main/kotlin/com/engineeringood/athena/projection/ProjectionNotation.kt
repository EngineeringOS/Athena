package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Projection-owned identifier for one governed notation pack.
 *
 * Notation-pack identity is stable for downstream projection delivery and remains separate from
 * canonical engineering identity.
 */
@JvmInline
value class ProjectionNotationPackId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-owned key selecting one downstream symbol variant.
 *
 * The key identifies presentation choice only. It never owns engineering meaning.
 */
@JvmInline
value class ProjectionSymbolKey(val value: String) {
    override fun toString(): String = value
}

/**
 * Governed label policy for one downstream notation subject.
 */
enum class ProjectionLabelPolicy {
    SUBJECT_LABEL,
    TERMINAL_LABEL,
    HIDDEN,
}

/**
 * Governed notation selection for one canonical subject inside a projection document.
 *
 * The subject remains anchored to canonical semantic identity while symbol choice, label policy,
 * and markers remain downstream notation decisions.
 */
data class ProjectionNotationSubject(
    val semanticId: StableSemanticIdentity,
    val symbolKey: ProjectionSymbolKey,
    val labelPolicy: ProjectionLabelPolicy,
    val markerKeys: List<String> = emptyList(),
)

/**
 * One governed notation pack attached to a derived projection document.
 *
 * The pack is inspectable downstream metadata that tells renderers and workbenches how to present
 * canonical subjects without promoting symbol choice into semantic truth.
 */
data class ProjectionNotationPack(
    val packId: ProjectionNotationPackId,
    val displayName: String,
    val subjects: List<ProjectionNotationSubject> = emptyList(),
)
