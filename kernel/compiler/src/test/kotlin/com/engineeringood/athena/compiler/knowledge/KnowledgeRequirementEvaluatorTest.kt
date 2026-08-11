package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.KnowledgeDefinition
import com.engineeringood.athena.knowledge.KnowledgePackageIdentity
import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringPackageName
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.semantics.core.EngineeringRequirement
import com.engineeringood.athena.semantics.core.EngineeringRequirementStatus
import com.engineeringood.athena.semantics.core.EngineeringValidationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import com.engineeringood.athena.semantics.core.EngineeringCorrectionOption
import com.engineeringood.athena.semantics.core.EngineeringPortableProvenance
import com.engineeringood.athena.semantics.core.EngineeringImpactStatus

class KnowledgeRequirementEvaluatorTest {
    private val provenance = SourceProvenance("src/design.athena", 1, 1, 1, 8)
    private val capability = EngineeringDefinitionId(EngineeringPackageName("example.core"), "electrical.switchedPower")
    private val stableCapability = capability.copy(qualifiedName = "electrical.controlledPower")

    @Test
    fun `authored provider yields READY`() {
        val result = KnowledgeRequirementEvaluator().evaluate(
            knowledge(),
            listOf(EngineeringRequirement("r1", EngineeringReference(listOf("KM1"), null, provenance), capability, provenance)),
            listOf(EngineeringCapabilityProvision(EngineeringReference(listOf("KM1"), null, provenance), capability, provenance)),
        )
        assertEquals(EngineeringValidationState.READY, result.state)
        assertEquals(EngineeringRequirementStatus.SATISFIED, result.satisfaction.single().status)
    }

    @Test
    fun `missing provider yields INCOMPLETE without auto selection`() {
        val result = KnowledgeRequirementEvaluator().evaluate(
            knowledge(),
            listOf(EngineeringRequirement("r1", EngineeringReference(listOf("KM1"), null, provenance), capability, provenance)),
            emptyList(),
        )
        assertEquals(EngineeringValidationState.INCOMPLETE, result.state)
        assertEquals(EngineeringRequirementStatus.UNSATISFIED, result.satisfaction.single().status)
        assertEquals("r1", result.judgements.single().id)
        assertTrue(result.correctionOptions.isEmpty())
    }

    @Test
    fun `correction options only enumerate existing authored providers in stable order`() {
        val result = KnowledgeRequirementEvaluator().evaluate(
            knowledge(),
            listOf(EngineeringRequirement("r1", EngineeringReference(listOf("KM1"), null, provenance), capability, provenance)),
            emptyList(),
            candidateProviders = listOf(
                EngineeringReference(listOf("Z1"), null, provenance),
                EngineeringReference(listOf("A1"), null, provenance),
            ),
        )
        assertEquals(listOf("r1.provider.0", "r1.provider.1"), result.correctionOptions.map { it.id })
        assertEquals(listOf("A1", "Z1"), result.correctionOptions.map { (it as EngineeringCorrectionOption.SelectExistingProvider).provider.authoredPath.single() })
    }

    @Test
    fun `portable provenance rejects parent and absolute paths and converts spans`() {
        val portable = EngineeringPortableProvenance.from(provenance)
        assertEquals(0, portable.startLine)
        assertEquals(0, portable.startUtf16Column)
        assertFailsWith<IllegalArgumentException> { EngineeringPortableProvenance("../bad.athena", 0, 0, 0, 1) }
        assertFailsWith<IllegalArgumentException> { EngineeringPortableProvenance("C:/bad.athena", 0, 0, 0, 1) }
    }

    @Test
    fun `impact comparison emits stable changed and unchanged entries`() {
        val evaluator = KnowledgeRequirementEvaluator()
        val requirement = EngineeringRequirement("r1", EngineeringReference(listOf("KM1"), null, provenance), capability, provenance)
        val stableRequirement = EngineeringRequirement("r2", EngineeringReference(listOf("KM2"), null, provenance), stableCapability, provenance)
        val candidate = EngineeringReference(listOf("P1"), null, provenance)
        val before = evaluator.evaluate(knowledge(), listOf(requirement, stableRequirement), listOf(
            EngineeringCapabilityProvision(EngineeringReference(listOf("KM2"), null, provenance), stableCapability, provenance),
        ), candidateProviders = listOf(candidate))
        val after = evaluator.evaluate(knowledge(), listOf(requirement, stableRequirement), listOf(
            EngineeringCapabilityProvision(EngineeringReference(listOf("KM1"), null, provenance), capability, provenance),
            EngineeringCapabilityProvision(EngineeringReference(listOf("KM2"), null, provenance), stableCapability, provenance),
        ))
        val impact = EngineeringImpactCalculator.compare(before, after)
        assertEquals(listOf("correction:r1.provider.0", "judgement:r1", "requirement:r1", "requirement:r2"), impact.map { it.id })
        assertEquals(EngineeringImpactStatus.CHANGED, impact.first().status)
        assertEquals(listOf(EngineeringImpactStatus.CHANGED, EngineeringImpactStatus.CHANGED, EngineeringImpactStatus.CHANGED, EngineeringImpactStatus.UNCHANGED), impact.map { it.status })
    }

    @Test
    fun `unknown capability yields INVALID without satisfaction collection`() {
        val unknown = capability.copy(qualifiedName = "unknown")
        val result = KnowledgeRequirementEvaluator().evaluate(
            knowledge(),
            listOf(EngineeringRequirement("r1", EngineeringReference(listOf("KM1"), null, provenance), unknown, provenance)),
            emptyList(),
        )
        assertEquals(EngineeringValidationState.INVALID, result.state)
        assertTrue(result.satisfaction.isEmpty())
    }

    private fun knowledge() = EngineeringKnowledgeDocument(
        packages = listOf(KnowledgePackageIdentity("example.core", "1.0.0")),
        definitions = listOf(
            KnowledgeDefinition.Capability(stableCapability, provenance = provenance),
            KnowledgeDefinition.Capability(capability, provenance = provenance),
        ),
        provenance = listOf(provenance),
    )
}
