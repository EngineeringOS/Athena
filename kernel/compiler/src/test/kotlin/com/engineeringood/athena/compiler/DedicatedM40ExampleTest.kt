package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.spatial.SpatialDocument
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class DedicatedM40ExampleTest {

    @Test
    fun `M40 example compiles through four realities with all constructs and a region`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m40/rolling-shutter-control")
        val source = sampleRoot.resolve("src/com/engineeringood/m40/rollingshutter/01-rolling-shutter-control.athena")

        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")), "M40 example must be IDE-openable.")
        assertTrue(Files.exists(source), "M40 source must follow package/path hierarchy.")
        val sourceText = source.readText()
        assertTrue(sourceText.contains("power Supply.L1 to Breaker.line"))
        assertTrue(sourceText.contains("control StartButton.contact13 to Contactor.coilA1"))
        assertTrue(sourceText.contains("earth EarthBar.PE to ["))
        assertTrue(!sourceText.contains("intent", ignoreCase = true))
        assertTrue(!sourceText.contains("ProfessionalControlDrawing"))

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(sampleRoot)
        assertTrue(
            lock.isValid,
            lock.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        assertTrue(compiler.validateRepositoryContract(sampleRoot).isValid)
        assertTrue(compiler.validateRepositoryLock(sampleRoot).isValid)

        val compilation = compiler.compile(source)
        when (compilation) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    compilation.semanticResult.diagnostics.isEmpty(),
                    compilation.semanticResult.diagnostics.joinToString("\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                assertTrue(
                    compilation.authoredProjectionDiagnostics.isEmpty(),
                    compilation.authoredProjectionDiagnostics.joinToString("\n"),
                )
                val projection = compilation.authoredProjectionViews.single()
                val sheet = projection.sheets.single()
                assertEquals(3, sheet.regions.size)
                assertEquals(
                    setOf("power-rail", "rung", "branch", "wire-bundle", "terminal-strip", "contact-group", "coil-group"),
                    sheet.constructs.map { construct -> construct.kind }.toSet(),
                )
                assertEquals(8, sheet.grid?.rows)
                assertEquals(10, sheet.grid?.columns)

                val spatialResult = ProjectionSpatialCompiler().transform(projection)
                val spatial = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
                    spatialResult,
                    (spatialResult as? RealityTransformationResult.Failure)?.diagnostics?.joinToString("\n") {
                        diagnostic -> "${diagnostic.reality}: ${diagnostic.message}"
                    },
                )
                val spatialSheet = spatial.output.sheets.single()
                assertTrue(spatialSheet.occurrences.isNotEmpty())

                val presentation = assertIs<RealityTransformationResult.Success<PresentationDocument>>(
                    SpatialToPresentationTransformation().transform(spatialSheet),
                )
                assertTrue(presentation.output.connectors.isNotEmpty() || presentation.output.occurrences.isNotEmpty())

                assertTrue(spatialSheet.quality.metrics.density >= 0.0)
            }

            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString("\n") { diagnostic ->
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
