package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class DedicatedProfessionalDrawingSampleTest {
    @Test
    fun `dedicated professional drawing sample is source first and compiles offline`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m37/professional-control-drawing")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val profilePackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m37/professional/drawing-profile.athena",
        )
        val elementPackage = sampleRoot.resolve(
            "packages/representation/com/engineeringood/m37/professional/m37-surface-elements.athena",
        )

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M37 sample must be IDE-openable.")
        assertTrue(Files.exists(source), "M37 source must follow package/path hierarchy.")
        assertTrue(Files.exists(profilePackage), "M37 sample must include package-local drawing profile material.")
        assertTrue(Files.exists(elementPackage), "M37 sample must include package-local element material.")

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(sampleRoot)
        assertTrue(
            lock.isValid,
            lock.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        assertTrue(Files.exists(sampleRoot.resolve("athena.lock")), "M37 sample must carry compiler-materialized lock evidence.")
        assertTrue(
            compiler.validateRepositoryContract(sampleRoot).isValid,
            "M37 sample repository contract must validate.",
        )
        assertTrue(
            compiler.validateRepositoryLock(sampleRoot).isValid,
            "M37 sample lock must validate after materialization.",
        )

        when (val compilation = compiler.compile(source)) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    compilation.semanticResult.diagnostics.isEmpty(),
                    compilation.semanticResult.diagnostics.joinToString("\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                assertTrue(compilation.document.components.size >= 6)
                assertTrue(compilation.document.connections.size >= 6)
                assertTrue(compilation.document.externalEvidence.isNotEmpty())
                assertTrue(compilation.document.projectionPolicies.any { policy -> policy.targetSurface == "professional-connection-drawing" })
                assertTrue(
                    compilation.presentations.any { presentation ->
                            presentation.view.id == "schematic" &&
                            presentation.view.displayName == "Control Drawing" &&
                            presentation.graphicOccurrences.isNotEmpty() &&
                            presentation.connectors.isNotEmpty()
                    },
                    "M37 sample must compile the authored professional connection drawing into the schematic-backed Control Drawing presentation.",
                )
            }

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }

        val sourceText = source.readText()
        val allSampleText = Files.walk(sampleRoot).use { stream ->
            stream
                .filter(Files::isRegularFile)
                .map { path -> path.readText() }
                .toList()
                .joinToString("\n")
        }

        assertTrue(sourceText.contains("interface powerInput"))
        assertFalse(sourceText.contains("intent {"), "M37 sample must not keep removed connection intent source.")
        assertTrue(sourceText.contains("evidence RollingShutterIec"))
        assertTrue(sourceText.contains("projection ControlDrawing"))
        assertTrue(sourceText.contains("drawingProfile ControlDrawingIEC"))
        assertTrue(sourceText.contains("installation cabinet RollingShutterPanel"))
        assertTrue(sourceText.contains("ProtectiveEarthPE37"))
        assertTrue(sourceText.contains("RelayUpK37"))
        assertTrue(sourceText.contains("IndicatorUpH37"))

        listOf("<definition", ".elmt", "qelectrotech", "m36", "fallback card", "mock presentation").forEach { forbidden ->
            assertFalse(allSampleText.contains(forbidden, ignoreCase = true), "M37 sample must not contain `$forbidden`.")
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
