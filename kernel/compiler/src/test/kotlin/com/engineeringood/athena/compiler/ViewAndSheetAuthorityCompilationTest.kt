package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.spatial.SpatialRect
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ViewAndSheetAuthorityCompilationTest {

    @Test
    fun `authored view compiles to one projection view with sheets and grid reference system`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "Main" { occurrences [D] }
              }
            }
            """.trimIndent(),
        )

        val document = success.authoredProjectionViews.single()
        assertEquals("schematic", document.view.id)
        assertEquals(1, document.sheets.size)
        val sheet = document.sheets.single()
        assertEquals("schematic/sheet/S1", sheet.sheetId.value)
        val grid = sheet.grid
        assertTrue(grid != null, "Sheet must expose a grid reference system.")
        assertEquals("schematic/G1", grid.gridId)
        assertEquals(3, grid.rows)
        assertEquals(4, grid.columns)
        assertEquals(listOf("component:D"), sheet.subjects.map { subject -> subject.semanticId.value })
        assertEquals(listOf("Main"), sheet.regions.map { region -> region.name })
        assertTrue(success.authoredProjectionDiagnostics.isEmpty())
        assertEquals(
            1,
            success.presentations.count { presentation ->
                presentation.view.id == "schematic" &&
                    presentation.drawingComposition?.sheetId == "schematic/sheet/S1"
            },
        )
    }

    @Test
    fun `authored multi Sheet presentation stays separate and is published once`() {
        val success = compile(
            """
            system Demo {
              device First { }
              device Second { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "First Region" { occurrences [First] }
                sheet S2
                region "Second Region" { occurrences [Second] }
              }
            }
            """.trimIndent(),
        )

        val expectedSheetIds = setOf("schematic/sheet/S1", "schematic/sheet/S2")
        val authoredPresentations = success.presentations.filter { presentation ->
            presentation.view.id == "schematic"
        }

        assertTrue(success.authoredProjectionDiagnostics.isEmpty())
        assertEquals(expectedSheetIds, authoredPresentations.mapNotNull { it.drawingComposition?.sheetId }.toSet())
        assertEquals(2, authoredPresentations.size)
        assertTrue(authoredPresentations.all { presentation -> presentation.drawingComposition != null })
        assertTrue(authoredPresentations.all { presentation ->
            presentation.canvasWidth == 1200 && presentation.canvasHeight == 800 &&
                presentation.occurrences.size == 1
        })
        assertEquals(2, authoredPresentations.flatMap { it.occurrences }.map { it.occurrenceId }.distinct().size)
        val spatialDocument = success.spatialDocuments.single()
        assertEquals(expectedSheetIds, spatialDocument.sheets.map { sheet -> sheet.sheetId }.toSet())
        assertEquals(
            authoredPresentations,
            assertIs<RealityTransformationResult.Success<List<com.engineeringood.athena.presentation.PresentationDocument>>>(
                canonicalPresentations(authoredPresentations.reversed()),
            ).output,
        )
        assertEquals(
            success.presentations.size,
            success.presentations.distinctBy { presentation ->
                presentation.view.id to presentation.drawingComposition?.sheetId
            }.size,
        )
    }

    @Test
    fun `presentation merge rejects unequal documents with one view and Sheet identity`() {
        val presentation = compile(
            """
            system Demo {
              device D { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "Main" { occurrences [D] }
              }
            }
            """.trimIndent(),
        ).presentations.single { candidate ->
            candidate.view.id == "schematic" && candidate.drawingComposition?.sheetId == "schematic/sheet/S1"
        }

        assertEquals(
            listOf(presentation),
            assertIs<RealityTransformationResult.Success<List<com.engineeringood.athena.presentation.PresentationDocument>>>(
                canonicalPresentations(listOf(presentation, presentation)),
            ).output,
        )
        val conflict = assertIs<RealityTransformationResult.Failure>(
            canonicalPresentations(
                listOf(presentation, presentation.copy(canvasWidth = presentation.canvasWidth + 1)),
            ),
        )
        assertEquals(
            listOf(
                RealityTransformationDiagnostic(
                    reality = "Presentation Reality",
                    message = "Presentation schematic on Sheet schematic/sheet/S1 has 2 unequal candidates. " +
                        "Publish one canonical Presentation document for each view and Sheet identity.",
                    subject = "Presentation schematic on Sheet schematic/sheet/S1",
                    problem = "has 2 unequal candidates",
                    correction = "Publish one canonical Presentation document for each view and Sheet identity.",
                ),
            ),
            conflict.diagnostics,
        )
        val duplicateConflict = assertIs<RealityTransformationResult.Failure>(
            canonicalPresentations(
                listOf(presentation, presentation, presentation.copy(canvasWidth = presentation.canvasWidth + 1)),
            ),
        )
        assertContains(duplicateConflict.diagnostics.single().message, "has 2 unequal candidates")
        val unrelated = presentation.copy(view = ViewDefinition("other", "Other"))
        val resolution = resolveCanonicalPresentations(
            listOf(presentation, presentation.copy(canvasWidth = presentation.canvasWidth + 1), unrelated),
        )
        assertEquals(listOf(unrelated), resolution.documents)
        assertEquals(conflict.diagnostics, resolution.diagnostics)

        val empty = presentation.copy(
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            graphicOccurrences = emptyList(),
            connectors = emptyList(),
            representationFacts = emptyList(),
            referenceMarkers = emptyList(),
            drawingComposition = null,
        )
        assertEquals(
            listOf(
                RealityTransformationDiagnostic(
                    reality = "Presentation Reality",
                    message = "Presentation schematic has 2 unequal candidates. " +
                        "Publish one canonical Presentation document for each view and Sheet identity.",
                    subject = "Presentation schematic",
                    problem = "has 2 unequal candidates",
                    correction = "Publish one canonical Presentation document for each view and Sheet identity.",
                ),
            ),
            assertIs<RealityTransformationResult.Failure>(
                canonicalPresentations(listOf(empty, empty.copy(canvasWidth = empty.canvasWidth + 1))),
            ).diagnostics,
        )
    }

    @Test
    fun `retained Spatial merge deduplicates equal documents and rejects shared Sheet authority`() {
        val spatial = compile(
            """
            system Demo {
              device D { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "Main" { occurrences [D] }
              }
            }
            """.trimIndent(),
        ).spatialDocuments.single()

        assertEquals(listOf(spatial), resolveCanonicalSpatialDocuments(listOf(spatial, spatial)).documents)
        val conflicting = com.engineeringood.athena.spatial.SpatialDocument(
            spatial.sheets.map { sheet ->
                sheet.copy(extent = SpatialRect(sheet.extent.x, sheet.extent.y, sheet.extent.width + 1, sheet.extent.height))
            },
        )
        val resolution = resolveCanonicalSpatialDocuments(listOf(spatial, conflicting))
        assertTrue(resolution.documents.isEmpty())
        val diagnostic = resolution.diagnostics.single()
        assertEquals("Spatial document on Sheet schematic/sheet/S1", diagnostic.subject)
        assertEquals("has 2 unequal candidates", diagnostic.problem)
        assertContains(diagnostic.message, "Publish one canonical Spatial document for each ordered Sheet identity set")
    }

    @Test
    fun `retained Spatial merge compares one whole ordered Sheet identity set`() {
        val spatial = compile(
            """
            system Demo {
              device First { }
              device Second { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "First Region" { occurrences [First] }
                sheet S2
                region "Second Region" { occurrences [Second] }
              }
            }
            """.trimIndent(),
        ).spatialDocuments.single()
        val changedSecondSheet = com.engineeringood.athena.spatial.SpatialDocument(
            spatial.sheets.map { sheet ->
                if (sheet.sheetId.endsWith("/S2")) {
                    sheet.copy(
                        extent = SpatialRect(
                            sheet.extent.x,
                            sheet.extent.y,
                            sheet.extent.width + 1,
                            sheet.extent.height,
                        ),
                    )
                } else {
                    sheet
                }
            },
        )

        val resolution = resolveCanonicalSpatialDocuments(listOf(spatial, changedSecondSheet))

        assertTrue(resolution.documents.isEmpty())
        val diagnostic = resolution.diagnostics.single()
        assertEquals(
            "Spatial document for ordered Sheets schematic/sheet/S1, schematic/sheet/S2",
            diagnostic.subject,
        )
        assertEquals("has 2 unequal candidates", diagnostic.problem)
    }

    @Test
    fun `retained Spatial merge rejects overlapping unequal Sheet identity sets once`() {
        val spatial = compile(
            """
            system Demo {
              device First { }
              device Second { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "First Region" { occurrences [First] }
                sheet S2
                region "Second Region" { occurrences [Second] }
              }
            }
            """.trimIndent(),
        ).spatialDocuments.single()
        val overlapping = com.engineeringood.athena.spatial.SpatialDocument(
            listOf(
                spatial.sheets.first(),
                spatial.sheets.last().copy(sheetId = "schematic/sheet/S3"),
            ),
        )

        val resolution = resolveCanonicalSpatialDocuments(listOf(spatial, overlapping))

        assertTrue(resolution.documents.isEmpty())
        val diagnostic = resolution.diagnostics.single()
        assertEquals("Spatial documents sharing Sheet schematic/sheet/S1", diagnostic.subject)
        assertContains(diagnostic.problem.orEmpty(), "overlapping but unequal ordered Sheet identity sets")
    }

    @Test
    fun `view with no sheets fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view emptyView { }
            }
            """.trimIndent(),
        )

        assertTrue(success.authoredProjectionViews.isEmpty())
        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "no sheets")
    }

    @Test
    fun `duplicate sheet identity fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view v {
                sheet A
                sheet A
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "duplicate sheet")
    }

    @Test
    fun `authored Spatial failure remains inspectable through compiler diagnostics`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view schematic {
                sheet S1
                region "Main" { occurrences [D] }
              }
            }
            """.trimIndent(),
        )

        val diagnostic = success.realityTransformationDiagnostics.single { candidate ->
            candidate.subject == "Sheet schematic/sheet/S1 grid"
        }
        assertEquals("is missing", diagnostic.problem)
        assertEquals(
            "Define a grid for Sheet schematic/sheet/S1 before compiling Grid References.",
            diagnostic.correction,
        )
        assertTrue(success.presentations.none { presentation -> presentation.view.id == "schematic" })
        assertTrue(success.spatialDocuments.isEmpty())
        assertContains(success.diagnosticMessages(), diagnostic.message)
    }

    @Test
    fun `projection reality validation rejects empty sheets at model level`() {
        val document = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = emptyList(),
            connections = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = ProjectionSheetId("schematic/sheet/1"),
                    displayName = "S1",
                    order = 1,
                ),
            ),
        )

        val result = ProjectionReality.validate(document)
        assertContains(result.issues.map { issue -> issue.message }.joinToString("\n"), "empty sheet")
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-view-authority", ".athena")
        val result = AthenaCompiler().compile(path, source)
        return assertIs<CompilerCompilationSuccess>(
            result,
            (result as? CompilerCompilationParseFailure)?.diagnostics?.joinToString("\n") { diagnostic ->
                "${diagnostic.line}:${diagnostic.column} ${diagnostic.message}"
            },
        )
    }
}
