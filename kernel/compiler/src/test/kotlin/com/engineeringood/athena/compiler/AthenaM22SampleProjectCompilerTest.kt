package com.engineeringood.athena.compiler

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import java.nio.file.Files
import java.nio.file.Path

class AthenaM22SampleProjectCompilerTest {
    @Test
    fun `m22 sample project source files compile without diagnostics`() {
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
                    }
                )
            }
        }
    }

    private fun sampleProjectSources(): List<Path> {
        val repoRoot = resolveRepoRoot()
        return listOf(
            repoRoot.resolve("examples/m22/sample-project/src/01-baseline-sheet.athena"),
            repoRoot.resolve("examples/m22/sample-project/src/02-layout-optimization-acceptance.athena"),
            repoRoot.resolve("examples/m22/sample-project/src/03-component-round-trip.athena"),
            repoRoot.resolve("examples/m22/sample-project/src/04-boundary-scope.athena"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
