package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringConstraintEvaluationContractTest {
    @Test
    fun `publishes the first fixed electrical rule vocabulary`() {
        assertEquals(
            listOf(
                EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                EngineeringConstraintRuleKind.CABLE_SUFFICIENCY,
                EngineeringConstraintRuleKind.RELAY_SUFFICIENCY,
            ),
            EngineeringConstraintRuleKind.entries,
        )
    }

    @Test
    fun `canonical constraint evaluations sort subjects and results deterministically`() {
        val evaluations = EngineeringConstraintEvaluations.canonical(
            listOf(
                EngineeringConstraintSubjectEvaluations(
                    subjectIdentity = StableSemanticIdentity("component:M2"),
                    evaluations = emptyList(),
                ),
                EngineeringConstraintSubjectEvaluations(
                    subjectIdentity = StableSemanticIdentity("component:M1"),
                    evaluations = listOf(
                        EngineeringConstraintEvaluation(
                            ruleKind = EngineeringConstraintRuleKind.CABLE_SUFFICIENCY,
                            status = EngineeringConstraintStatus.WARNING,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            affectedSubjectIdentities = listOf(StableSemanticIdentity("component:M1")),
                            explanation = "Cable current is below required demand.",
                            requiredQuantity = DerivedEngineeringQuantity.Decimal("16", "A"),
                            actualQuantity = DerivedEngineeringQuantity.Decimal("12", "A"),
                            trace = EngineeringConstraintEvaluationTrace(
                                knowledgeArtifactId = "com.engineeringood.athena.knowledge.pack.electrical-basic",
                                knowledgeArtifactVersion = "0.1.0",
                                knowledgeEntryId = "constraint",
                                requiredFactKind = EngineeringCapabilityFactKind.REQUIRED_CABLE_CURRENT,
                                actualInputKind = DerivedEngineeringInputKind.CABLE_ALLOWED_CURRENT,
                            ),
                        ),
                        EngineeringConstraintEvaluation(
                            ruleKind = EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                            status = EngineeringConstraintStatus.ERROR,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            affectedSubjectIdentities = listOf(StableSemanticIdentity("component:M1")),
                            explanation = "Breaker current is below required protection current.",
                            requiredQuantity = DerivedEngineeringQuantity.Decimal("18", "A"),
                            actualQuantity = DerivedEngineeringQuantity.Decimal("10", "A"),
                            trace = EngineeringConstraintEvaluationTrace(
                                knowledgeArtifactId = "com.engineeringood.athena.knowledge.pack.electrical-basic",
                                knowledgeArtifactVersion = "0.1.0",
                                knowledgeEntryId = "constraint",
                                requiredFactKind = EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                                actualInputKind = DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("component:M1", "component:M2"),
            evaluations.subjects.map { subject -> subject.subjectIdentity.value },
        )
        assertEquals(
            listOf(
                EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                EngineeringConstraintRuleKind.CABLE_SUFFICIENCY,
            ),
            evaluations.subjects.first().evaluations.map { evaluation -> evaluation.ruleKind },
        )
    }
}
