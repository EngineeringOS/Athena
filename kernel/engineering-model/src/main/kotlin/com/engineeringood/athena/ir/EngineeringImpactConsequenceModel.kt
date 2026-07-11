package com.engineeringood.athena.ir

/**
 * Canonical kernel-owned container for one deterministic engineering impact-consequence snapshot.
 *
 * Impact consequences sit above constraint evaluation and capture which semantic subjects become
 * affected when governed engineering meaning changes between two canonical states.
 */
data class EngineeringImpactConsequences(
    val consequences: List<EngineeringImpactConsequence>,
) {
    companion object {
        /**
         * Builds a deterministic impact snapshot by sorting affected subjects and nested category lists in stable order.
         */
        fun canonical(consequences: List<EngineeringImpactConsequence>): EngineeringImpactConsequences {
            return EngineeringImpactConsequences(
                consequences = consequences
                    .sortedWith(
                        compareBy<EngineeringImpactConsequence>(
                            { consequence -> consequence.affectedSubjectIdentity.value },
                            { consequence -> consequence.triggerSortKey() },
                            { consequence -> consequence.reasonSortKey() },
                            { consequence -> consequence.inputSortKey() },
                            { consequence -> consequence.derivedValueSortKey() },
                            { consequence -> consequence.capabilityFactSortKey() },
                            { consequence -> consequence.constraintRuleSortKey() },
                        ),
                    )
                    .map { consequence -> consequence.canonical() },
            )
        }
    }
}

/**
 * Typed impact record for one affected canonical semantic subject.
 *
 * The consequence keeps the trigger subject identities and changed semantic layers explicit so
 * later review and runtime flows can explain engineering consequence without parsing prose.
 */
data class EngineeringImpactConsequence(
    val affectedSubjectIdentity: StableSemanticIdentity,
    val triggerSubjectIdentities: List<StableSemanticIdentity>,
    val reasonKinds: List<EngineeringImpactReasonKind>,
    val affectedInputKinds: List<DerivedEngineeringInputKind> = emptyList(),
    val affectedDerivedValueKinds: List<DerivedEngineeringValueKind> = emptyList(),
    val affectedCapabilityFactKinds: List<EngineeringCapabilityFactKind> = emptyList(),
    val affectedConstraintRuleKinds: List<EngineeringConstraintRuleKind> = emptyList(),
)

/**
 * Short categorized reason labels attached to one impact consequence.
 */
enum class EngineeringImpactReasonKind {
    GOVERNED_INPUT_CHANGED,
    DERIVED_CONTEXT_CHANGED,
    CAPABILITY_FACT_CHANGED,
    CONSTRAINT_EVALUATION_CHANGED,
}

private fun EngineeringImpactConsequence.canonical(): EngineeringImpactConsequence {
    return copy(
        triggerSubjectIdentities = triggerSubjectIdentities
            .distinct()
            .sortedBy { identity -> identity.value },
        reasonKinds = reasonKinds
            .distinct()
            .sortedBy(EngineeringImpactReasonKind::ordinal),
        affectedInputKinds = affectedInputKinds
            .distinct()
            .sortedBy(DerivedEngineeringInputKind::ordinal),
        affectedDerivedValueKinds = affectedDerivedValueKinds
            .distinct()
            .sortedBy(DerivedEngineeringValueKind::ordinal),
        affectedCapabilityFactKinds = affectedCapabilityFactKinds
            .distinct()
            .sortedBy(EngineeringCapabilityFactKind::ordinal),
        affectedConstraintRuleKinds = affectedConstraintRuleKinds
            .distinct()
            .sortedBy(EngineeringConstraintRuleKind::ordinal),
    )
}

private fun EngineeringImpactConsequence.triggerSortKey(): String {
    return triggerSubjectIdentities
        .distinct()
        .sortedBy { identity -> identity.value }
        .joinToString(separator = "|") { identity -> identity.value }
}

private fun EngineeringImpactConsequence.reasonSortKey(): String {
    return reasonKinds
        .distinct()
        .sortedBy(EngineeringImpactReasonKind::ordinal)
        .joinToString(separator = "|") { reason -> reason.name }
}

private fun EngineeringImpactConsequence.inputSortKey(): String {
    return affectedInputKinds
        .distinct()
        .sortedBy(DerivedEngineeringInputKind::ordinal)
        .joinToString(separator = "|") { kind -> kind.name }
}

private fun EngineeringImpactConsequence.derivedValueSortKey(): String {
    return affectedDerivedValueKinds
        .distinct()
        .sortedBy(DerivedEngineeringValueKind::ordinal)
        .joinToString(separator = "|") { kind -> kind.name }
}

private fun EngineeringImpactConsequence.capabilityFactSortKey(): String {
    return affectedCapabilityFactKinds
        .distinct()
        .sortedBy(EngineeringCapabilityFactKind::ordinal)
        .joinToString(separator = "|") { kind -> kind.name }
}

private fun EngineeringImpactConsequence.constraintRuleSortKey(): String {
    return affectedConstraintRuleKinds
        .distinct()
        .sortedBy(EngineeringConstraintRuleKind::ordinal)
        .joinToString(separator = "|") { kind -> kind.name }
}
