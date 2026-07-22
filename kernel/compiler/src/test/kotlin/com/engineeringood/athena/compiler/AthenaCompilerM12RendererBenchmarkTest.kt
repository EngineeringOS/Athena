package com.engineeringood.athena.compiler

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaCompilerM12RendererBenchmarkTest {
    @Test
    fun `m12 renderer benchmark repository compiles into a larger electrical proof scene`() {
        val repoRoot = resolveRepoRoot()
        val sourcePath = repoRoot.resolve("examples/m12/renderer-benchmark-proof/src/expansion-line.athena")
        val compiler = AthenaCompiler(
            knowledgePackageSource = defaultAthenaKnowledgePackageSource(repoRoot),
        )

        val result = compiler.compile(sourcePath)
        if (result is CompilerCompilationParseFailure) {
            error(result.diagnostics.joinToString(separator = " | ") { diagnostic ->
                "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}:${diagnostic.message}"
            })
        }

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertEquals(24, success.document.components.size)
        assertEquals(74, success.document.ports.size)
        assertEquals(45, success.document.connections.size)
        assertEquals(
            listOf("cabinet", "documentation", "schematic", "wiring"),
            success.projections.map { projection -> projection.view.id }.sorted(),
        )

        val documentation = success.projections.first { projection -> projection.view.id == "documentation" }
        assertEquals(
            listOf(
                "documentation/sheet/01-control",
                "documentation/sheet/02-field-device",
            ),
            documentation.sheets.map { sheet -> sheet.sheetId.value },
        )
        assertTrue(documentation.crossReferences.size >= 16)
        assertEquals(1, documentation.nodes.count { node -> node.semanticId.value == "component:M1" })
        assertEquals(1, documentation.nodes.count { node -> node.semanticId.value == "component:M5" })
        assertTrue(
            documentation.nodes.none { node -> node.projectionId.value.endsWith("_reference") },
            "Documentation benchmark must prove professional cross references without duplicate off-sheet nodes.",
        )

        assertTrue(documentation.crossReferences.any { crossReference -> crossReference.sheetIds.size >= 2 })
        assertContains(
            success.validationBreakdown.engineeringSufficiencyDiagnostics.map { diagnostic -> diagnostic.ruleId.value },
            "knowledge.protection_sufficiency",
        )
    }

    private fun resolveRepoRoot(): Path {
        return generateSequence(Path.of("").toAbsolutePath().normalize()) { candidate -> candidate.parent }
            .first { candidate ->
                candidate.resolve("settings.gradle.kts").toFile().exists() &&
                    candidate.resolve("examples").toFile().exists()
            }
    }
}
