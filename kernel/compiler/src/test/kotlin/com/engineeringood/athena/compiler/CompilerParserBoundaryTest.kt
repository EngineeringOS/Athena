package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards AD-106: canonical lowering into Engineering IR must consume the authored AST only. No
 * `kernel/compiler` source may depend on the generated ANTLR parse-tree types or the ANTLR runtime;
 * parse-tree-to-AST adaptation is isolated inside `kernel/language`'s internal `antlr` package.
 *
 * This is a source-scan boundary test (not reflection) so it also catches ANTLR references that
 * would live only inside method bodies, before they can ever reach a public signature.
 */
class CompilerParserBoundaryTest {
    @Test
    fun `no compiler source references generated antlr parse-tree or runtime types`() {
        val compilerMainSources = resolveRepoRoot().resolve("kernel/compiler/src/main")
        assertTrue(Files.isDirectory(compilerMainSources), "Expected compiler main sources at $compilerMainSources")

        val forbiddenReferences = listOf(
            "com.engineeringood.athena.language.antlr",
            "org.antlr",
        )

        val offenders = Files.walk(compilerMainSources).use { paths ->
            paths
                .filter { Files.isRegularFile(it) && it.extension == "kt" }
                .filter { source ->
                    val text = source.readText()
                    forbiddenReferences.any { forbidden -> text.contains(forbidden) }
                }
                .map { it.toString() }
                .toList()
        }

        assertTrue(
            offenders.isEmpty(),
            "Compiler sources must not reference ANTLR parse-tree or runtime types (AD-106). Offenders: $offenders",
        )
    }

    @Test
    fun `engineering ir lowerer and compiler source document consume authored ast only`() {
        val guardedFiles = listOf(
            "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt",
            "kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt",
        ).map { resolveRepoRoot().resolve(it) }

        // Reference-level guard: prose may name ANTLR to document the AD-106 boundary, but no source
        // line may actually import or address the ANTLR runtime or the internal generated-parser package.
        val forbiddenReferences = listOf(
            "com.engineeringood.athena.language.antlr",
            "org.antlr",
        )

        guardedFiles.forEach { file ->
            assertTrue(Files.isRegularFile(file), "Expected guarded compiler source at $file")
            val offendingLines = file.readText().lineSequence()
                .filter { line -> forbiddenReferences.any { forbidden -> line.contains(forbidden) } }
                .toList()
            assertTrue(
                offendingLines.isEmpty(),
                "$file must not reference any ANTLR type; authored AST is the only lowering input (AD-106). Offending lines: $offendingLines",
            )
        }
    }

    private fun resolveRepoRoot(): Path {
        var current: Path? = Path.of("").toAbsolutePath()
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts")) &&
                Files.isDirectory(current.resolve("kernel"))
            ) {
                return current
            }
            current = current.parent
        }
        error("Could not locate repository root from ${Path.of("").toAbsolutePath()}")
    }
}
