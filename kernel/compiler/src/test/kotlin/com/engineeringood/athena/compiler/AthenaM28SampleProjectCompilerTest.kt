package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM28SampleProjectCompilerTest {
    @Test
    fun `m28 sample project source files compile without diagnostics and use nested ports`() {
        val compiler = AthenaCompiler()
        sampleProjectSources().forEach { source ->
            assertTrue(Files.exists(source), "Missing M28 sample source: $source")
            val sourceText = Files.readString(source)
            assertTrue(sourceText.contains("    port "), "M28 sample must use nested device-owned ports: $source")
            val result = compiler.compile(source)
            when (result) {
                is CompilerCompilationSuccess -> assertTrue(
                    result.semanticResult.diagnostics.isEmpty(),
                    buildString {
                        appendLine("${source.fileName} still produces diagnostics:")
                        result.semanticResult.diagnostics.forEach { diagnostic ->
                            appendLine("- ${diagnostic.ruleId.value}: ${diagnostic.message}")
                        }
                    },
                )

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
    }

    @Test
    fun `m28 sample project links authored references without ambiguity`() {
        val compiler = AthenaCompiler()
        val sampleProjectRoot = resolveRepoRoot().resolve("examples/m28/sample-project")
        compiler.materializeRepositoryLock(sampleProjectRoot)

        val publication = compiler.publishRepositoryGraphReport(sampleProjectRoot)
        val graph = assertNotNull(publication.graph)
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

    private fun sampleProjectSources(): List<Path> {
        val repoRoot = resolveRepoRoot()
        return listOf(
            repoRoot.resolve("examples/m28/sample-project/src/01-relationship-authoring-source.athena"),
            repoRoot.resolve("examples/m28/sample-project/src/02-relationship-candidates.athena"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
