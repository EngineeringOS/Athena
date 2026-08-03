package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.spatial.SpatialDocument
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class DedicatedM41ExampleTest {

    @Test
    fun `M41 example compiles through four realities with spatial milestone facts`() {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m41/rolling-shutter")
        val source = sampleRoot.resolve("src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena")
        assertTrue(Files.exists(sampleRoot.resolve("athena.yaml")))
        assertTrue(Files.exists(source))

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(sampleRoot)
        assertTrue(lock.isValid, lock.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" })
        assertTrue(compiler.validateRepositoryContract(sampleRoot).isValid)
        assertTrue(compiler.validateRepositoryLock(sampleRoot).isValid)

        val compilation = compiler.compile(source)
        when (compilation) {
            is CompilerCompilationSuccess -> {
                assertTrue(compilation.semanticResult.diagnostics.isEmpty())
                assertTrue(compilation.authoredProjectionDiagnostics.isEmpty())
                val projection = compilation.authoredProjectionViews.single()
                val spatialResult = ProjectionSpatialCompiler().transform(projection)
                val spatial = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
                    spatialResult,
                    (spatialResult as? RealityTransformationResult.Failure)?.diagnostics?.joinToString("\n") {
                        diagnostic -> "${diagnostic.reality}: ${diagnostic.message}"
                    },
                ).output

                assertTrue(spatial.occurrences.size >= 7)
                assertTrue(spatial.occurrences.all { occurrence -> occurrence.placementReason.text.isNotBlank() })
                assertTrue(spatial.anchorPositions.isNotEmpty())
                assertTrue(spatial.routes.size >= 7)
                spatial.routes.forEach { route ->
                    assertTrue(route.sourceOccurrenceId.contains("/occurrence/"))
                    assertTrue(route.targetOccurrenceId.contains("/occurrence/"))
                }

                val presentation = assertIs<RealityTransformationResult.Success<PresentationDocument>>(
                    SpatialToPresentationTransformation().transform(spatial),
                )
                assertTrue(presentation.output.occurrences.isNotEmpty() || presentation.output.connectors.isNotEmpty())

                assertTrue(spatial.qualityMeasurements.isNotEmpty())
            }

            is CompilerCompilationParseFailure -> fail(compilation.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}" })
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
