package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM24SampleProjectCompilerTest {
    @Test
    fun `m24 sample project source files compile without diagnostics`() {
        val compiler = AthenaCompiler()
        sampleProjectSources().forEach { source ->
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
    fun `m24 sample project binds project semantic layout references without diagnostics`() {
        val compiler = AthenaCompiler()
        val sampleProjectRoot = resolveRepoRoot().resolve("examples/m24/sample-project")
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
        val bound = compiler.bindProjectSemanticLayoutHints(
            compiler.indexProjectSemanticDeclarations(
                compiler.emitProjectSemanticDiagnostics(
                    compiler.resolveProjectSemanticImports(snapshot),
                ),
            ),
        )

        assertTrue(
            bound.diagnostics.isEmpty(),
            bound.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code.value}: ${diagnostic.message}"
            },
        )
    }

    private fun sampleProjectSources(): List<Path> {
        val repoRoot = resolveRepoRoot()
        return listOf(
            repoRoot.resolve("examples/m24/sample-project/src/01-control-route.athena"),
            repoRoot.resolve("examples/m24/sample-project/src/02-terminal-strip-routes.athena"),
            repoRoot.resolve("examples/m24/sample-project/src/03-power-protection-load.athena"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
