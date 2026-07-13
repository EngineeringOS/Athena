package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.EngineeringCapabilityComparison
import com.engineeringood.athena.ir.EngineeringCapabilityFact
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import com.engineeringood.athena.ir.EngineeringCapabilityFactTrace
import com.engineeringood.athena.ir.EngineeringConstraintEvaluation
import com.engineeringood.athena.ir.EngineeringConstraintEvaluationTrace
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringConstraintStatus
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class SemanticPortBoundaryContractTest {
    @Test
    fun `semantic port contract stays declarative while judgement stays in m9 models`() {
        val portDefinition = SemanticPortDefinition(
            portTypeId = SemanticPortTypeId("electrical.power.dc24"),
            displayName = "24V DC power port",
            roleId = SemanticPortRoleId("l+"),
            direction = SemanticPortDirection.OUTPUT,
            signalFamilyId = SemanticSignalFamilyId("electrical.power"),
            summary = "Declarative power-port meaning only.",
        )
        val subjectIdentity = StableSemanticIdentity("component:QF1")
        val capabilityFact = EngineeringCapabilityFact(
            kind = EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
            subjectIdentity = subjectIdentity,
            comparison = EngineeringCapabilityComparison.MINIMUM_INCLUSIVE,
            quantity = DerivedEngineeringQuantity.Decimal("18", "A"),
            trace = EngineeringCapabilityFactTrace(
                knowledgeArtifactId = "knowledge-electrical-basic",
                knowledgeArtifactVersion = "0.0.1",
                knowledgeEntryId = "protection.current.minimum",
                sourceDerivedValues = emptyList(),
            ),
        )
        val evaluation = EngineeringConstraintEvaluation(
            ruleKind = EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
            status = EngineeringConstraintStatus.WARNING,
            subjectIdentity = subjectIdentity,
            affectedSubjectIdentities = listOf(subjectIdentity),
            explanation = "Breaker current is below the required protection current.",
            requiredQuantity = DerivedEngineeringQuantity.Decimal("18", "A"),
            actualQuantity = DerivedEngineeringQuantity.Decimal("10", "A"),
            trace = EngineeringConstraintEvaluationTrace(
                knowledgeArtifactId = "knowledge-electrical-basic",
                knowledgeArtifactVersion = "0.0.1",
                knowledgeEntryId = "constraint.protection.sufficiency",
                requiredFactKind = EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                actualInputKind = DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
            ),
        )

        assertEquals("l+", portDefinition.roleId.value)
        assertEquals("electrical.power", portDefinition.signalFamilyId.value)
        assertEquals(EngineeringCapabilityComparison.MINIMUM_INCLUSIVE, capabilityFact.comparison)
        assertEquals(EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY, evaluation.ruleKind)
        assertEquals(EngineeringConstraintStatus.WARNING, evaluation.status)
    }
}
