package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringConstraintStatus
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringConstraintEvaluatorTest {
    @Test
    fun `compile evaluates the first fixed rule slice and emits typed engineering sufficiency diagnostics`() {
        val repoRoot = resolveRepoRoot()
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(repoRoot.resolve("extensions/knowledge-electrical-basic")),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(
            compiler.compile(repoRoot.resolve("examples/m9/motor-derived-context.athena")),
        )

        val subjectEvaluations = result.constraintEvaluations.subjects.single()
        assertEquals(
            listOf(
                EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                EngineeringConstraintRuleKind.CABLE_SUFFICIENCY,
                EngineeringConstraintRuleKind.RELAY_SUFFICIENCY,
            ),
            subjectEvaluations.evaluations.map { evaluation -> evaluation.ruleKind },
        )
        assertEquals(
            listOf(
                EngineeringConstraintStatus.ERROR,
                EngineeringConstraintStatus.WARNING,
                EngineeringConstraintStatus.WARNING,
            ),
            subjectEvaluations.evaluations.map { evaluation -> evaluation.status },
        )
        assertEquals(
            listOf(SemanticDiagnosticCategory.KNOWLEDGE, SemanticDiagnosticCategory.KNOWLEDGE, SemanticDiagnosticCategory.KNOWLEDGE),
            result.validationBreakdown.engineeringSufficiencyDiagnostics.map { diagnostic -> diagnostic.category },
        )
        assertEquals(
            listOf("component:M1", "component:M1", "component:M1"),
            result.validationBreakdown.engineeringSufficiencyDiagnostics.mapNotNull { diagnostic -> diagnostic.subjectIdentity?.value },
        )
        assertTrue(result.semanticResult.isSemanticallyValid)
        assertTrue(result.diagnosticMessages().any { message -> message.contains("Breaker current 10A is below required 18A", ignoreCase = true) })
    }

    @Test
    fun `recompute preserves the same engineering insufficiency output for the same canonical state`() {
        val repoRoot = resolveRepoRoot()
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(repoRoot.resolve("extensions/knowledge-electrical-basic")),
            ),
        )
        val first = assertIs<CompilerCompilationSuccess>(
            compiler.compile(repoRoot.resolve("examples/m9/motor-derived-context.athena")),
        )

        val recomputed = compiler.recompute(
            source = first.source,
            document = first.document,
            affectedScope = CompilerAffectedScope(
                changedSemanticIds = listOf("component:M1"),
                validationSemanticIds = listOf("component:M1"),
                renderComponentSemanticIds = listOf("component:M1"),
                renderConnectionSemanticIds = emptyList(),
            ),
            previousLayouts = first.layouts,
            previousGeometries = first.geometries,
            previousRendering = first.rendering,
        )

        assertEquals(first.constraintEvaluations, recomputed.constraintEvaluations)
        assertEquals(
            first.validationBreakdown.engineeringSufficiencyDiagnostics,
            recomputed.validationBreakdown.engineeringSufficiencyDiagnostics,
        )
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
