package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM30SampleProjectCompilerTest {
    @Test
    fun `m30 customer demo sample compiles without diagnostics and exposes governed projection`() {
        val compiler = AthenaCompiler()
        val source = sampleProjectSource()
        assertTrue(Files.exists(source), "Missing M30 sample source: $source")
        val sourceText = Files.readString(source)

        assertTrue(sourceText.contains("    port "), "M30 sample must use compact nested device-owned ports.")
        assertNoVisualOrQetSourceSyntax(sourceText)

        val result = compiler.compile(source)
        when (result) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    result.semanticResult.diagnostics.isEmpty(),
                    result.semanticResult.diagnostics.joinToString(separator = "\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                assertTrue(result.projections.isNotEmpty(), "M30 sample projection must be available.")
                assertTrue(
                    result.projections.any { projection -> projection.view.id == "documentation" && projection.sheets.size >= 3 },
                    "M30 sample must expose governed documentation sheets for the customer demo.",
                )
                assertTrue(
                    result.projections
                        .flatMap { projection -> projection.crossReferences }
                        .isNotEmpty(),
                    "M30 sample must include semantic references for professional representation proof.",
                )
            }

            is CompilerCompilationParseFailure -> fail(
                buildString {
                    appendLine("${source.fileName} fails to parse:")
                    result.diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.line}:${diagnostic.column} ${diagnostic.message}")
                    }
                },
            )
        }
    }

    @Test
    fun `m30 sample project links semantic references without treating source files as sheets`() {
        val compiler = AthenaCompiler()
        val sampleProjectRoot = resolveRepoRoot().resolve("examples/m30/sample-project")
        compiler.materializeRepositoryLock(sampleProjectRoot)

        val publication = compiler.publishRepositoryGraphReport(sampleProjectRoot)
        val graph = assertNotNull(publication.graph)
        assertEquals(1, graph.packages.size)
        val sources = graph.packages.flatMap { resolvedPackage ->
            val sourceRoot = sampleProjectRoot.resolve(resolvedPackage.sourceRoot).toAbsolutePath().normalize()
            Files.walk(sourceRoot).use { stream ->
                stream
                    .filter(Files::isRegularFile)
                    .filter { path -> path.fileName.toString().endsWith(".athena") }
                    .map { path ->
                        ProjectSemanticSourceInput(
                            packageId = resolvedPackage.packageId,
                            sourceRootRelativePath = sourceRoot.relativize(path.toAbsolutePath().normalize())
                                .toString()
                                .replace('\\', '/'),
                            sourceContent = Files.readString(path),
                        )
                    }
                    .toList()
            }
        }
        assertEquals(1, sources.size, "M30 sample has one semantic source file; sheets come from projection facts.")

        val built = compiler.buildProjectSemanticGraph(publication, sources)
        val snapshot = assertNotNull(
            built.snapshot,
            built.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code.value}: ${diagnostic.message}"
            },
        )
        val linked = compiler.linkProjectSemanticReferences(
            compiler.bindProjectSemanticLayoutHints(
                compiler.indexProjectSemanticDeclarations(
                    compiler.emitProjectSemanticDiagnostics(
                        compiler.resolveProjectSemanticImports(snapshot),
                    ),
                ),
            ),
        )

        assertTrue(
            linked.diagnostics.isEmpty(),
            linked.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code.value}: ${diagnostic.message}"
            },
        )
    }

    private fun assertNoVisualOrQetSourceSyntax(sourceText: String) {
        val forbidden = listOf("qelectrotech", ".elmt", "svg", "path", "viewBox", "rectangle", "circle", "stroke")
        val lowered = sourceText.lowercase()
        forbidden.forEach { token ->
            assertTrue(token.lowercase() !in lowered, "M30 semantic source must not contain visual/QET token `$token`.")
        }
    }

    private fun sampleProjectSource(): Path =
        resolveRepoRoot().resolve("examples/m30/sample-project/src/01-rolling-shutter-control-source.athena")

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
