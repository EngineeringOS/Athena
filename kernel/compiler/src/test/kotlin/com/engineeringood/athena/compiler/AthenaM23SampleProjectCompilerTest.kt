package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.LayoutDeclaration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM23SampleProjectCompilerTest {
    @Test
    fun `m23 sample project layout source compiles without diagnostics`() {
        val source = sampleProjectSource()
        val result = AthenaCompiler().compile(source)
        when (result) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    result.semanticResult.diagnostics.isEmpty(),
                    buildString {
                        appendLine("${source.fileName} still produces diagnostics:")
                        result.semanticResult.diagnostics.forEach { diagnostic ->
                            appendLine("- ${diagnostic.ruleId.value}: ${diagnostic.message}")
                        }
                    },
                )
                val layout = assertIs<LayoutDeclaration>(
                    result.source.ast.declarations.single { declaration -> declaration is LayoutDeclaration },
                )
                assertEquals("schematic-sheet", layout.viewFamily)
                assertEquals(4, layout.statements.size)
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

    private fun sampleProjectSource(): Path {
        return resolveRepoRoot().resolve("examples/m23/sample-project/src/01-layout-hints.athena")
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
