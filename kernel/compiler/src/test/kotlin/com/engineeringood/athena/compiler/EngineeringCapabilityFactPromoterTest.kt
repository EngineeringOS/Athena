package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringCapabilityFactPromoterTest {
    @Test
    fun `compile promotes capability facts through the fixed electrical knowledge pack`() {
        val repoRoot = resolveRepoRoot()
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(
                    repoRoot.resolve("extensions/knowledge-electrical-basic"),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(
            compiler.compile(
                repoRoot.resolve("examples/m9/motor-derived-context.athena"),
            ),
        )

        assertEquals(
            listOf("com.engineeringood.athena.knowledge.pack.electrical-basic"),
            result.knowledgeContext.activeArtifacts.map { artifact -> artifact.artifactId },
        )
        val subjectFacts = result.capabilityFacts.subjects.single()
        assertEquals(
            listOf(
                EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_CABLE_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_RELAY_SIZING_CURRENT,
            ),
            subjectFacts.facts.map { fact -> fact.kind },
        )
        assertEquals(
            listOf("18", "16", "14"),
            subjectFacts.facts.map { fact ->
                (fact.quantity as DerivedEngineeringQuantity.Decimal).canonicalText
            },
        )
        assertTrue(
            subjectFacts.facts.all { fact ->
                fact.trace.knowledgeArtifactId == "com.engineeringood.athena.knowledge.pack.electrical-basic" &&
                    fact.trace.knowledgeArtifactVersion == "0.1.0" &&
                    fact.trace.knowledgeEntryId == "capability"
            },
        )
    }

    @Test
    fun `compile keeps capability facts empty when the fixed knowledge pack is absent`() {
        val repoRoot = resolveRepoRoot()
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                repoRoot.resolve("examples/m9/motor-derived-context.athena"),
            ),
        )

        assertTrue(result.capabilityFacts.subjects.isEmpty())
        assertTrue(result.derivedContext.subjects.isNotEmpty())
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
