package com.engineeringood.athena.ir

/**
 * Canonical kernel-owned container for one deterministic engineering capability-fact snapshot.
 *
 * Capability facts sit above derived engineering context and below later constraint results.
 */
data class EngineeringCapabilityFacts(
    val subjects: List<EngineeringCapabilitySubjectFacts>,
) {
    companion object {
        /**
         * Builds a deterministic capability-fact snapshot by sorting subjects and their nested facts in stable order.
         */
        fun canonical(subjects: List<EngineeringCapabilitySubjectFacts>): EngineeringCapabilityFacts {
            return EngineeringCapabilityFacts(
                subjects = subjects
                    .sortedBy { subject -> subject.subjectIdentity.value }
                    .map { subject -> subject.canonical() },
            )
        }
    }
}

/**
 * Capability facts scoped to one canonical semantic subject.
 */
data class EngineeringCapabilitySubjectFacts(
    val subjectIdentity: StableSemanticIdentity,
    val facts: List<EngineeringCapabilityFact>,
)

/**
 * Narrow first-wave capability facts published by the M9 electrical proof slice.
 */
enum class EngineeringCapabilityFactKind {
    REQUIRED_PROTECTION_CURRENT,
    REQUIRED_CABLE_CURRENT,
    REQUIRED_RELAY_SIZING_CURRENT,
}

/**
 * Comparison contract attached to one capability fact judgement.
 */
enum class EngineeringCapabilityComparison {
    MINIMUM_INCLUSIVE,
}

/**
 * Reference to one derived engineering value that contributed to a capability fact judgement.
 */
data class EngineeringCapabilityDerivedValueReference(
    val subjectIdentity: StableSemanticIdentity,
    val valueKind: DerivedEngineeringValueKind,
)

/**
 * Traceability record for one capability fact judgement produced through a governed knowledge artifact.
 */
data class EngineeringCapabilityFactTrace(
    val knowledgeArtifactId: String,
    val knowledgeArtifactVersion: String,
    val knowledgeEntryId: String,
    val sourceDerivedValues: List<EngineeringCapabilityDerivedValueReference>,
)

/**
 * Inspectable engineering judgement promoted above derived context through governed knowledge-pack semantics.
 */
data class EngineeringCapabilityFact(
    val kind: EngineeringCapabilityFactKind,
    val subjectIdentity: StableSemanticIdentity,
    val comparison: EngineeringCapabilityComparison,
    val quantity: DerivedEngineeringQuantity,
    val trace: EngineeringCapabilityFactTrace,
)

private fun EngineeringCapabilitySubjectFacts.canonical(): EngineeringCapabilitySubjectFacts {
    return copy(
        facts = facts.sortedWith(
            compareBy<EngineeringCapabilityFact>(
                { fact -> fact.kind.ordinal },
                { fact -> fact.subjectIdentity.value },
                { fact -> fact.comparison.ordinal },
                { fact -> fact.quantity.sortKey() },
                { fact -> fact.trace.sortKey() },
            ),
        ),
    )
}

private fun DerivedEngineeringQuantity.sortKey(): String {
    return when (this) {
        is DerivedEngineeringQuantity.Decimal -> "${canonicalText}:${unitSymbol.orEmpty()}"
    }
}

private fun EngineeringCapabilityFactTrace.sortKey(): String {
    return buildString {
        append(knowledgeArtifactId)
        append(':')
        append(knowledgeArtifactVersion)
        append(':')
        append(knowledgeEntryId)
        append(':')
        append(
            sourceDerivedValues
                .sortedWith(
                    compareBy<EngineeringCapabilityDerivedValueReference>(
                        { reference -> reference.subjectIdentity.value },
                        { reference -> reference.valueKind.ordinal },
                    ),
                )
                .joinToString(separator = "|") { reference ->
                    "${reference.subjectIdentity.value}:${reference.valueKind.name}"
                },
        )
    }
}
