package com.engineeringood.athena.compiler

import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaCompilerSpatialPipelineTest {

    @Test
    fun `actual compiler retains validated M41 Spatial used by Presentation`() {
        val source = loadDedicatedM41ExampleSource()

        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(source.sourcePath, source.bytes.toString(StandardCharsets.UTF_8)),
        )

        val spatial = result.spatialDocuments.single()
        assertEquals("schematic/sheet/S1", spatial.sheets.single().sheetId)
        assertDedicatedM41SpatialGolden(spatial.sheets.single())
        val presentation = result.presentations.single { candidate -> candidate.view.id == "schematic" }
        assertEquals("schematic/sheet/S1", presentation.drawingComposition?.sheetId)
        assertEquals(
            spatial.sheets.single().routes.map { route -> route.routeId.value }.toSet(),
            presentation.connectors.map { connector -> connector.routeId }.toSet(),
        )

        val repeated = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(source.sourcePath, source.bytes.toString(StandardCharsets.UTF_8)),
        )
        assertEquals(result.spatialDocuments, repeated.spatialDocuments)
        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (repeated.spatialDocuments as java.util.List<com.engineeringood.athena.spatial.SpatialDocument>).clear()
        }
    }

    @Test
    fun `invalid authored Projection retains no partial Spatial document`() {
        val source = loadDedicatedM41ExampleSource()
        val sourceText = source.bytes.toString(StandardCharsets.UTF_8)
            .replace(
                "region \"Power Distribution\" { occurrences [Supply, Breaker] }",
                "region \"Power Distribution\" { occurrences [] }",
            )

        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(source.sourcePath, sourceText),
        )

        assertEquals(
            listOf("View 'schematic' declares empty region 'Power Distribution'. Add at least one occurrence."),
            result.authoredProjectionDiagnostics,
        )
        assertTrue(result.realityTransformationDiagnostics.isEmpty())
        assertTrue(result.spatialDocuments.isEmpty())
        assertTrue(result.presentations.none { presentation -> presentation.view.id == "schematic" })
    }

    @Test
    fun `one failed authored Spatial view prevents partial retained collection`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                java.nio.file.Path.of("multi-view-spatial-failure.athena"),
                """
                system Demo {
                  device Good { }
                  device Broken { }
                  view good {
                    sheet S1
                    grid G1 { rows 3 columns 4 }
                    region "Good" { occurrences [Good] }
                  }
                  view broken {
                    sheet S2
                    region "Broken" { occurrences [Broken] }
                  }
                }
                """.trimIndent(),
            ),
        )

        assertTrue(result.realityTransformationDiagnostics.any { diagnostic ->
            diagnostic.subject == "Sheet broken/sheet/S2 grid"
        })
        assertTrue(result.spatialDocuments.isEmpty())
        assertTrue(result.presentations.none { presentation -> presentation.view.id in setOf("good", "broken") })
    }
}
