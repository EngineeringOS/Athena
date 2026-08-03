package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.projection.ProjectionSheetFrame
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetRevisionMetadata
import com.engineeringood.athena.projection.ProjectionSheetTitleBlock
import com.engineeringood.athena.projection.ProjectionSheetViewComposition
import com.engineeringood.athena.representation.GraphicBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DrawingSheetCompositionCompilerTest {
    @Test
    fun `compiler derives frame title block sheet margins and coordinate zones from content`() {
        val request = request(
            contentBounds = GraphicBounds(10.0, -5.0, 80.0, 40.0),
            policy = policy(columnLabels = listOf("A", "B", "C", "D"), rowLabels = listOf("1", "2")),
        )

        val result = DrawingSheetCompositionCompiler().compile(request)

        assertTrue(result.isValid, result.diagnostics.toString())
        val plan = requireNotNull(result.plan)
        assertEquals(GraphicBounds(-4.0, -19.0, 108.0, 84.0), plan.sheetBounds)
        assertEquals(GraphicBounds(0.0, -15.0, 100.0, 76.0), plan.frame.bounds)
        assertEquals(GraphicBounds(0.0, -15.0, 100.0, 60.0), plan.drawingAreaBounds)
        assertEquals(GraphicBounds(0.0, 45.0, 100.0, 16.0), plan.titleBlock.bounds)
        assertEquals(
            DrawingSheetMarginFact(
                contentToFrame = 10.0,
                frameToSheet = 4.0,
                authority = "presentation-profile-policy",
            ),
            plan.margins,
        )
        assertEquals(
            listOf(
                GraphicBounds(0.0, -15.0, 25.0, 60.0),
                GraphicBounds(25.0, -15.0, 25.0, 60.0),
                GraphicBounds(50.0, -15.0, 25.0, 60.0),
                GraphicBounds(75.0, -15.0, 25.0, 60.0),
            ),
            plan.coordinateZones.filter { it.axis == DrawingSheetZoneAxis.COLUMN }.map { it.bounds },
        )
        assertEquals(
            listOf(
                GraphicBounds(0.0, -15.0, 100.0, 30.0),
                GraphicBounds(0.0, 15.0, 100.0, 30.0),
            ),
            plan.coordinateZones.filter { it.axis == DrawingSheetZoneAxis.ROW }.map { it.bounds },
        )
        assertEquals(result, DrawingSheetCompositionCompiler().compile(request))
    }

    @Test
    fun `compiler derives a fixed professional sheet and typed title fields from policy`() {
        val request = DrawingSheetCompositionRequest(
            sheetId = ProjectionSheetId("control-drawing/01"),
            publication = publication(),
            contentBounds = GraphicBounds(30.0, 30.0, 1000.0, 620.0),
            policy = policy(
                contentToFrame = 0.0,
                frameToSheet = 5.0,
                coordinateBandSize = 20.0,
                titleBlockHeight = 50.0,
                maximumSheetWidth = 1050.0,
                maximumSheetHeight = 720.0,
                columnLabels = (1..17).map(Int::toString),
                rowLabels = ('A'..'H').map(Char::toString),
                fixedSheetBounds = GraphicBounds(0.0, 0.0, 1050.0, 720.0),
            ),
            titleFields = listOf(
                DrawingSheetTitleFieldInput("author", "Author", "EngineeringOS"),
                DrawingSheetTitleFieldInput("title", "Title", "Rolling Shutter Control"),
                DrawingSheetTitleFieldInput("file", "File", "01-control-drawing.athena"),
                DrawingSheetTitleFieldInput("date", "Date", "2026-07-26"),
                DrawingSheetTitleFieldInput("folio", "Folio", "1/1"),
            ),
        )

        val result = DrawingSheetCompositionCompiler().compile(request)

        assertTrue(result.isValid, result.diagnostics.toString())
        val plan = requireNotNull(result.plan)
        assertEquals(GraphicBounds(0.0, 0.0, 1050.0, 720.0), plan.sheetBounds)
        assertEquals(GraphicBounds(5.0, 5.0, 1040.0, 710.0), plan.frame.bounds)
        assertEquals(GraphicBounds(25.0, 25.0, 1020.0, 640.0), plan.drawingAreaBounds)
        assertEquals(GraphicBounds(5.0, 665.0, 1040.0, 50.0), plan.titleBlock.bounds)
        assertEquals((1..17).map(Int::toString), plan.coordinateZones.filter { it.axis == DrawingSheetZoneAxis.COLUMN }.map { it.label })
        assertEquals(('A'..'H').map(Char::toString), plan.coordinateZones.filter { it.axis == DrawingSheetZoneAxis.ROW }.map { it.label })
        assertEquals(setOf("author", "title", "file", "date", "folio"), plan.titleBlock.fields.map { it.fieldId }.toSet())
    }

    @Test
    fun `compiler preserves projection publication fields as projection authority`() {
        val result = DrawingSheetCompositionCompiler().compile(request())

        assertTrue(result.isValid, result.diagnostics.toString())
        val plan = requireNotNull(result.plan)
        assertEquals("projection.sheet.control", plan.sheetId)
        assertEquals("engineering-sheet-frame", plan.frame.frameId)
        assertEquals("schematic", plan.frame.style)
        assertEquals("projection-sheet-publication", plan.frame.metadataAuthority)
        assertEquals("drawing-composition", plan.frame.boundsAuthority)
        assertEquals("Control Circuit", plan.titleBlock.sheetTitle)
        assertEquals("schematic", plan.titleBlock.sheetFamily)
        assertEquals("01", plan.titleBlock.sheetNumber)
        assertEquals("A", plan.titleBlock.revisionCode)
        assertEquals("Initial issue", plan.titleBlock.revisionNote)
        assertEquals("A3", plan.titleBlock.pageFormat)
        assertEquals("landscape", plan.titleBlock.orientation)
        assertEquals("projection-sheet-publication", plan.titleBlock.metadataAuthority)
        assertEquals("drawing-composition", plan.titleBlock.boundsAuthority)
        assertEquals(listOf("header", "body", "title-block"), plan.namedZones.map { it.zoneId })
        assertTrue(plan.namedZones.all { it.authority == "projection-sheet-publication" })
        assertEquals("presentation-profile-policy", plan.margins.authority)
        assertTrue(plan.coordinateZones.all { it.labelAuthority == "presentation-profile-policy" })
        assertTrue(plan.coordinateZones.all { it.boundsAuthority == "drawing-composition" })

        val evidence = requireNotNull(result.evidence)
        assertEquals("graphic-primitive-ir", evidence.contentBoundsAuthority)
        assertEquals("drawing-composition", evidence.boundsAuthority)
        assertEquals("projection-sheet-publication", evidence.projectionAuthority)
        assertEquals("presentation-profile-policy", evidence.policyAuthority)
        assertEquals("m33.iec-compact.sheet-v1", evidence.policyId)
        assertEquals(listOf("header", "body", "title-block"), evidence.namedZoneIds)
        assertEquals(plan.coordinateZones.map { it.zoneId }, evidence.coordinateZoneIds)
        val payload = requireNotNull(result.toTransportPayload())
        assertEquals(DrawingSheetBoundsPayload(-14.0, -14.0, 108.0, 92.0), payload.sheetBounds)
        assertEquals("Control Circuit", payload.title.sheetTitle)
        assertEquals(listOf("header", "body", "title-block"), payload.namedZones.map { it.zoneId })
        assertEquals(listOf("header", "body", "title-block"), payload.namedZoneIds)
        assertEquals(
            listOf("column:A", "column:B", "column:C", "column:D", "row:1", "row:2", "row:3"),
            payload.coordinateZones.map { it.zoneId },
        )
        assertEquals(payload, DrawingSheetCompositionCompiler().compile(request()).toTransportPayload())
    }

    @Test
    fun `compiler fails closed for invalid bounds policy projection overflow and out of sheet content`() {
        val compiler = DrawingSheetCompositionCompiler()
        val cases = listOf(
            request(contentBounds = null) to "drawing.composition.content-bounds.missing",
            request(contentBounds = GraphicBounds(0.0, 0.0, -1.0, 20.0)) to "drawing.composition.content-bounds.invalid",
            request(policy = policy(contentToFrame = -1.0)) to "drawing.composition.policy.margin.invalid",
            request(policy = policy(titleBlockHeight = 0.0)) to "drawing.composition.policy.title-block.invalid",
            request(policy = policy(maximumSheetHeight = Double.NaN)) to "drawing.composition.policy.maximum-sheet.invalid",
            request(policy = policy(columnLabels = listOf("A", " "))) to "drawing.composition.policy.zone-label.invalid",
            request(policy = policy(columnLabels = listOf("A", "A"))) to "drawing.composition.policy.zone-label.duplicate",
            request(publication = publication(sheetTitle = "")) to "drawing.composition.projection.title-block.invalid",
            request(publication = publication(pageFormat = "")) to "drawing.composition.projection.page-size.invalid",
            request(publication = publication(duplicateNamedZone = true)) to "drawing.composition.projection.zone.duplicate",
            request(policy = policy(maximumSheetWidth = 50.0)) to "drawing.composition.content.out-of-sheet",
            request(
                contentBounds = GraphicBounds(0.0, 0.0, Double.MAX_VALUE, 20.0),
                policy = policy(contentToFrame = Double.MAX_VALUE, maximumSheetWidth = Double.MAX_VALUE),
            ) to "drawing.composition.bounds.derived-invalid",
            request(
                contentBounds = GraphicBounds(0.0, 0.0, Double.MIN_VALUE, 1.0),
                policy = policy(
                    contentToFrame = 0.0,
                    frameToSheet = 0.0,
                    titleBlockHeight = 1.0,
                    maximumSheetWidth = 10.0,
                    maximumSheetHeight = 10.0,
                    columnLabels = listOf("A", "B"),
                    rowLabels = listOf("1"),
                ),
            ) to "drawing.composition.zone.bounds.derived-invalid",
        )

        cases.forEach { (request, expectedCode) ->
            val result = compiler.compile(request)
            assertFalse(result.isValid, expectedCode)
            assertNull(result.plan, expectedCode)
            assertNull(result.evidence, expectedCode)
            assertTrue(result.diagnostics.any { it.code == expectedCode }, result.diagnostics.toString())
            assertTrue(result.diagnostics.all { it.authority.isNotBlank() && it.subject.isNotBlank() })
        }
    }

    private fun request(
        contentBounds: GraphicBounds? = GraphicBounds(0.0, 0.0, 80.0, 48.0),
        policy: DrawingSheetCompositionPolicy = policy(),
        publication: ProjectionSheetPublication = publication(),
    ) = DrawingSheetCompositionRequest(
        sheetId = ProjectionSheetId("projection.sheet.control"),
        publication = publication,
        contentBounds = contentBounds,
        policy = policy,
    )

    private fun policy(
        contentToFrame: Double = 10.0,
        frameToSheet: Double = 4.0,
        coordinateBandSize: Double = 0.0,
        titleBlockHeight: Double = 16.0,
        maximumSheetWidth: Double = 200.0,
        maximumSheetHeight: Double = 160.0,
        columnLabels: List<String> = listOf("A", "B", "C", "D"),
        rowLabels: List<String> = listOf("1", "2", "3"),
        fixedSheetBounds: GraphicBounds? = null,
    ) = DrawingSheetCompositionPolicy(
        policyId = "m33.iec-compact.sheet-v1",
        contentToFrame = contentToFrame,
        frameToSheet = frameToSheet,
        coordinateBandSize = coordinateBandSize,
        titleBlockHeight = titleBlockHeight,
        maximumSheetWidth = maximumSheetWidth,
        maximumSheetHeight = maximumSheetHeight,
        columnLabels = columnLabels,
        rowLabels = rowLabels,
        fixedSheetBounds = fixedSheetBounds,
    )

    private fun publication(
        sheetTitle: String = "Control Circuit",
        pageFormat: String = "A3",
        duplicateNamedZone: Boolean = false,
    ) = ProjectionSheetPublication(
        pageSize = ProjectionSheetPageSize(pageFormat, "landscape"),
        frame = ProjectionSheetFrame("engineering-sheet-frame", "schematic"),
        coordinateZones = listOf(
            ProjectionSheetCoordinateZone("header", "Header", 0),
            ProjectionSheetCoordinateZone("body", "Body", 1),
            ProjectionSheetCoordinateZone(if (duplicateNamedZone) "body" else "title-block", "Title Block", 2),
        ),
        titleBlock = ProjectionSheetTitleBlock(sheetTitle, "schematic", "01"),
        revisionMetadata = ProjectionSheetRevisionMetadata("A", "Initial issue"),
        viewComposition = ProjectionSheetViewComposition("schematic", 0),
    )
}
