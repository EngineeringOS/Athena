package com.engineeringood.athena.ir

/**
 * Canonical kernel-owned container for one deterministic engineering constraint-evaluation snapshot.
 *
 * Constraint evaluation sits above capability facts and below later impact-consequence computation.
 */
data class EngineeringConstraintEvaluations(
    val subjects: List<EngineeringConstraintSubjectEvaluations>,
) {
    companion object {
        /**
         * Builds a deterministic evaluation snapshot by sorting subjects and their nested results in stable order.
         */
        fun canonical(subjects: List<EngineeringConstraintSubjectEvaluations>): EngineeringConstraintEvaluations {
            return EngineeringConstraintEvaluations(
                subjects = subjects
                    .sortedBy { subject -> subject.subjectIdentity.value }
                    .map { subject -> subject.canonical() },
            )
        }
    }
}

/**
 * Constraint-evaluation results scoped to one canonical semantic subject.
 */
data class EngineeringConstraintSubjectEvaluations(
    val subjectIdentity: StableSemanticIdentity,
    val evaluations: List<EngineeringConstraintEvaluation>,
)

/**
 * First-wave fixed electrical rule identifiers used by the M9 proof slice.
 */
enum class EngineeringConstraintRuleKind {
    PROTECTION_SUFFICIENCY,
    CABLE_SUFFICIENCY,
    RELAY_SUFFICIENCY,
}

/**
 * Status emitted by one deterministic engineering constraint evaluation.
 */
enum class EngineeringConstraintStatus {
    ACCEPTED,
    WARNING,
    ERROR,
}

/**
 * Traceability record for one engineering constraint evaluation.
 */
data class EngineeringConstraintEvaluationTrace(
    val knowledgeArtifactId: String,
    val knowledgeArtifactVersion: String,
    val knowledgeEntryId: String,
    val requiredFactKind: EngineeringCapabilityFactKind,
    val actualInputKind: DerivedEngineeringInputKind,
)

/**
 * One typed engineering sufficiency result produced from capability facts and authored governed inputs.
 */
data class EngineeringConstraintEvaluation(
    val ruleKind: EngineeringConstraintRuleKind,
    val status: EngineeringConstraintStatus,
    val subjectIdentity: StableSemanticIdentity,
    val affectedSubjectIdentities: List<StableSemanticIdentity>,
    val explanation: String,
    val requiredQuantity: DerivedEngineeringQuantity,
    val actualQuantity: DerivedEngineeringQuantity,
    val trace: EngineeringConstraintEvaluationTrace,
)

private fun EngineeringConstraintSubjectEvaluations.canonical(): EngineeringConstraintSubjectEvaluations {
    return copy(
        evaluations = evaluations.sortedWith(
            compareBy<EngineeringConstraintEvaluation>(
                { evaluation -> evaluation.ruleKind.ordinal },
                { evaluation -> evaluation.status.ordinal },
                { evaluation -> evaluation.subjectIdentity.value },
                { evaluation -> evaluation.explanation },
                { evaluation -> evaluation.requiredQuantity.sortKey() },
                { evaluation -> evaluation.actualQuantity.sortKey() },
                { evaluation -> evaluation.trace.sortKey() },
            ),
        ),
    )
}

private fun DerivedEngineeringQuantity.sortKey(): String {
    return when (this) {
        is DerivedEngineeringQuantity.Decimal -> "${canonicalText}:${unitSymbol.orEmpty()}"
    }
}

private fun EngineeringConstraintEvaluationTrace.sortKey(): String {
    return buildString {
        append(knowledgeArtifactId)
        append(':')
        append(knowledgeArtifactVersion)
        append(':')
        append(knowledgeEntryId)
        append(':')
        append(requiredFactKind.name)
        append(':')
        append(actualInputKind.name)
    }
}
