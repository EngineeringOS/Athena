package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM32SampleProjectCompilerTest {
    @Test
    fun `m32 package platform sample is an ide openable athena repository with compilable source`() {
        val compiler = AthenaCompiler()
        val sampleRoot = resolveRepoRoot().resolve("examples/m32/sample-project")
        val source = sampleRoot.resolve("src/com/engineeringood/m32/sample/01-package-platform-demo.athena")

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M32 sample must include athena.yaml for LSP repository initialization.")
        assertTrue(Files.exists(sampleRoot.resolve("athena.lock")), "M32 sample must include athena.lock for governed repository validation.")
        assertTrue(Files.exists(source), "Missing M32 sample source: $source")

        val contractValidation = compiler.validateRepositoryContract(sampleRoot)
        assertTrue(
            contractValidation.isValid,
            contractValidation.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code}: ${diagnostic.message}"
            },
        )

        val compilation = compiler.compile(source)
        when (compilation) {
            is CompilerCompilationSuccess -> assertTrue(
                compilation.semanticResult.diagnostics.isEmpty(),
                compilation.semanticResult.diagnostics.joinToString(separator = "\n") { diagnostic ->
                    "${diagnostic.ruleId.value}: ${diagnostic.message}"
                },
            )

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString(
                    separator = "\n",
                    prefix = "M32 sample syntax diagnostics:\n",
                ) { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts")) && Files.exists(current.resolve("examples"))) {
                return current
            }
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
