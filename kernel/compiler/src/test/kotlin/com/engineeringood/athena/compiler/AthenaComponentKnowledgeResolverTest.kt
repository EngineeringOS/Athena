package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeCatalog
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeResolver
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeSeverity
import com.engineeringood.athena.compiler.knowledge.AthenaConceptDefinitionContribution
import com.engineeringood.athena.compiler.knowledge.AthenaPartImplementationContribution
import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.part.VendorId
import com.engineeringood.athena.part.VendorPartNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaComponentKnowledgeResolverTest {
    @Test
    fun `resolve returns unresolved diagnostic when no active definition matches`() {
        val result = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:M1"),
            authoredComponentReference = "unknown.part",
            catalog = catalog(),
        )

        assertTrue(!result.isResolved)
        assertNull(result.resolvedComponent)
        assertNull(result.resolvedImplementation)
        assertEquals(
            listOf("component.definition.unresolved"),
            result.diagnostics.map { diagnostic -> diagnostic.ruleId.value },
        )
        assertEquals(
            listOf(AthenaComponentKnowledgeSeverity.ERROR),
            result.diagnostics.map { diagnostic -> diagnostic.severity },
        )
        assertEquals(
            listOf("unknown.part"),
            result.diagnostics.map { diagnostic -> diagnostic.subject },
        )
    }

    @Test
    fun `resolve returns deterministic concept conflict diagnostic instead of pack-order precedence`() {
        val firstCatalog = AthenaComponentKnowledgeCatalog.canonical(
            concepts = listOf(
                conceptContribution("pack.beta", "1.0.0", concept("electrical.plc.cpu", "PLC CPU")),
                conceptContribution("pack.alpha", "1.0.0", concept("electrical.plc.cpu", "PLC CPU Alternate")),
            ),
            implementations = emptyList(),
        )
        val secondCatalog = AthenaComponentKnowledgeCatalog.canonical(
            concepts = firstCatalog.concepts.reversed(),
            implementations = emptyList(),
        )

        val first = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:CPU1"),
            authoredComponentReference = "electrical.plc.cpu",
            catalog = firstCatalog,
        )
        val second = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:CPU1"),
            authoredComponentReference = "electrical.plc.cpu",
            catalog = secondCatalog,
        )

        assertEquals(first, second)
        assertEquals(
            listOf("component.definition.conflict.concept"),
            first.diagnostics.map { diagnostic -> diagnostic.ruleId.value },
        )
    }

    @Test
    fun `resolve returns deterministic implementation conflict diagnostic instead of pack-order precedence`() {
        val firstCatalog = AthenaComponentKnowledgeCatalog.canonical(
            concepts = listOf(
                conceptContribution("pack.alpha", "1.0.0", concept("electrical.plc.cpu", "PLC CPU")),
            ),
            implementations = listOf(
                implementationContribution("pack.beta", "1.0.0", implementation("impl.a", "proof.cpu.313c", "CPU 313C")),
                implementationContribution("pack.alpha", "1.0.0", implementation("impl.b", "proof.cpu.313c", "CPU 313C Alternate")),
            ),
        )
        val secondCatalog = AthenaComponentKnowledgeCatalog.canonical(
            concepts = firstCatalog.concepts,
            implementations = firstCatalog.implementations.reversed(),
        )

        val first = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:CPU1"),
            authoredComponentReference = "proof.cpu.313c",
            catalog = firstCatalog,
        )
        val second = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:CPU1"),
            authoredComponentReference = "proof.cpu.313c",
            catalog = secondCatalog,
        )

        assertEquals(first, second)
        assertEquals(
            listOf("component.definition.conflict.implementation"),
            first.diagnostics.map { diagnostic -> diagnostic.ruleId.value },
        )
    }

    @Test
    fun `resolve returns component and implementation when one governed mapping is active`() {
        val result = AthenaComponentKnowledgeResolver().resolve(
            semanticSubjectId = StableSemanticIdentity("component:CPU1"),
            authoredComponentReference = "proof.cpu.313c",
            catalog = catalog(),
        )

        assertTrue(result.isResolved)
        assertEquals("electrical.plc.cpu", result.resolvedComponent?.concept?.conceptId?.value)
        assertEquals("proof.cpu.313c", result.resolvedImplementation?.implementation?.vendorPartNumber?.value)
        assertTrue(result.diagnostics.isEmpty())
    }

    private fun catalog(): AthenaComponentKnowledgeCatalog {
        return AthenaComponentKnowledgeCatalog.canonical(
            concepts = listOf(
                conceptContribution("pack.alpha", "1.0.0", concept("electrical.plc.cpu", "PLC CPU")),
            ),
            implementations = listOf(
                implementationContribution("pack.alpha", "1.0.0", implementation("impl.a", "proof.cpu.313c", "CPU 313C")),
            ),
        )
    }

    private fun conceptContribution(
        artifactId: String,
        artifactVersion: String,
        concept: EngineeringConceptDefinition,
    ): AthenaConceptDefinitionContribution {
        return AthenaConceptDefinitionContribution(
            artifactId = artifactId,
            artifactVersion = artifactVersion,
            concept = concept,
        )
    }

    private fun implementationContribution(
        artifactId: String,
        artifactVersion: String,
        implementation: PartImplementationDefinition,
    ): AthenaPartImplementationContribution {
        return AthenaPartImplementationContribution(
            artifactId = artifactId,
            artifactVersion = artifactVersion,
            implementation = implementation,
        )
    }

    private fun concept(
        conceptId: String,
        displayName: String,
    ): EngineeringConceptDefinition {
        return EngineeringConceptDefinition(
            conceptId = EngineeringConceptId(conceptId),
            displayName = displayName,
        )
    }

    private fun implementation(
        implementationId: String,
        vendorPartNumber: String,
        displayName: String,
    ): PartImplementationDefinition {
        return PartImplementationDefinition(
            implementationId = PartImplementationId(implementationId),
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber(vendorPartNumber),
            displayName = displayName,
        )
    }
}
