package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceId
import com.engineeringood.athena.projection.ProjectionCrossReferenceKind
import com.engineeringood.athena.projection.ProjectionCrossReferenceLink
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.representation.DrawingSymbolAnchorId
import com.engineeringood.athena.representation.DrawingSymbolIdentity
import com.engineeringood.athena.representation.DrawingSymbolSlotId
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DrawingSheetReferenceCompilerTest {
    @Test
    fun `compiler maps projection links to deterministic reference targets and placements`() {
        val request = request()

        val result = DrawingSheetReferenceCompiler().compile(request)

        assertTrue(result.isValid, result.diagnostics.toString())
        val plan = requireNotNull(result.plan)
        assertEquals(listOf("xref:breaker", "xref:terminal"), plan.targets.map { it.referenceId })
        assertEquals(listOf("placement:breaker", "placement:terminal"), plan.placements.map { it.placementId })
        val target = plan.targets.first()
        assertEquals("xref:breaker:occ:breaker:control->occ:breaker:field", target.targetId)
        assertEquals("device:breaker", target.semanticSubjectId)
        assertEquals("projection.sheet.control", target.sourceSheetId)
        assertEquals("projection.sheet.field", target.targetSheetId)
        assertEquals("02/A1", target.compactNotation)
        assertEquals("projection-cross-reference", target.authority)
        assertEquals(result, DrawingSheetReferenceCompiler().compile(request.copy(
            references = request.references.reversed(),
            placements = request.placements.reversed(),
        )))
    }

    @Test
    fun `evidence binds native folio marker anatomy zone and slots without inference`() {
        val result = DrawingSheetReferenceCompiler().compile(request())

        assertTrue(result.isValid, result.diagnostics.toString())
        val placement = requireNotNull(result.plan).placements.first()
        assertEquals("iec.folio-continuation-reference", placement.representationIdentity.value)
        assertEquals("continuation", placement.anchorId.value)
        assertEquals(GraphicPoint(80.0, 45.0), placement.anchorPoint)
        assertEquals("cross-reference", placement.labelSlotId.value)
        assertEquals("source-sheet", placement.sheetReferenceSlotId.value)
        assertEquals("source-zone", placement.zoneReferenceSlotId?.value)
        assertEquals(listOf("source-sheet", "source-zone", "target-sheet", "target-zone"), placement.availableReferenceSlotIds.map { it.value })
        assertEquals("body", placement.zoneId)
        assertEquals("drawing-symbol-anatomy", placement.representationAuthority)
        assertEquals("drawing-composition", placement.boundsAuthority)

        val evidence = requireNotNull(result.evidence)
        assertEquals(listOf("iec.folio-continuation-reference"), evidence.markerRepresentationIdentities)
        assertEquals("projection-cross-reference", evidence.projectionAuthority)
        assertEquals("drawing-symbol-anatomy", evidence.representationAuthority)
        val payload = requireNotNull(result.toTransportPayload())
        assertEquals("continuation", payload.placements.first().anchorId)
        assertEquals("cross-reference", payload.placements.first().labelSlotId)
        assertEquals(payload, DrawingSheetReferenceCompiler().compile(request()).toTransportPayload())
    }

    @Test
    fun `compiler fails closed for malformed projection anatomy placement collision and bounds`() {
        val base = request()
        val firstReference = base.references.first()
        val firstPlacement = base.placements.first()
        val cases = listOf(
            base.copy(references = base.references + firstReference) to "drawing.reference.projection.duplicate",
            base.copy(placements = base.placements + firstPlacement) to "drawing.reference.placement.duplicate",
            base.copy(references = base.references.map { reference ->
                if (reference.crossReferenceId == firstReference.crossReferenceId) reference.copy(links = emptyList()) else reference
            }) to "drawing.reference.projection.malformed",
            base.copy(references = base.references.map { reference ->
                if (reference.crossReferenceId == firstReference.crossReferenceId) reference.copy(links = reference.links + reference.links.first()) else reference
            }) to "drawing.reference.projection.duplicate-link",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(occurrenceId = "occ:wrong") else it }) to "drawing.reference.placement.link-mismatch",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(subjectId = "device:missing") else it }) to "drawing.reference.subject.missing",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(anchorId = DrawingSymbolAnchorId("missing")) else it }) to "drawing.reference.anatomy.missing",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(labelSlotId = DrawingSymbolSlotId("missing")) else it }) to "drawing.reference.anatomy.missing",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(sheetReferenceSlotId = DrawingSymbolSlotId("missing")) else it }) to "drawing.reference.anatomy.missing",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(zoneId = "missing") else it }) to "drawing.reference.zone.missing",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(bounds = GraphicBounds(Double.NaN, 0.0, 10.0, 10.0)) else it }) to "drawing.reference.bounds.invalid",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(bounds = GraphicBounds(0.0, 10.0, 20.0, 20.0), anchorPoint = GraphicPoint(10.0, 20.0)) else it }) to "drawing.reference.placement.collision",
            base.copy(placements = base.placements.map { if (it.placementId == firstPlacement.placementId) it.copy(bounds = GraphicBounds(500.0, 0.0, 10.0, 10.0), anchorPoint = GraphicPoint(505.0, 5.0)) else it }) to "drawing.reference.content.out-of-sheet",
            base.copy(references = base.references.map { reference ->
                if (reference.crossReferenceId == firstReference.crossReferenceId) {
                    reference.copy(links = reference.links.map { it.copy(targetSheetId = it.sourceSheetId, targetOccurrenceId = it.sourceOccurrenceId) })
                } else {
                    reference
                }
            }) to "drawing.reference.projection.cyclic",
        )

        cases.forEach { (request, expectedCode) ->
            val result = DrawingSheetReferenceCompiler().compile(request)
            assertFalse(result.isValid, expectedCode)
            assertNull(result.plan, expectedCode)
            assertNull(result.evidence, expectedCode)
            assertNull(result.toTransportPayload(), expectedCode)
            assertTrue(result.diagnostics.any { it.code == expectedCode }, result.diagnostics.toString())
            assertTrue(result.diagnostics.all { it.authority.isNotBlank() && it.subject.isNotBlank() && it.message.isNotBlank() })
        }
    }

    @Test
    fun `marker touching subject boundary is not a collision`() {
        val base = request()
        val result = DrawingSheetReferenceCompiler().compile(base.copy(
            placements = base.placements.map {
                if (it.placementId == "placement:terminal") it.copy(bounds = GraphicBounds(50.0, 10.0, 10.0, 10.0), anchorPoint = GraphicPoint(60.0, 15.0)) else it
            },
        ))

        assertTrue(result.diagnostics.none { it.code == "drawing.reference.placement.collision" }, result.diagnostics.toString())
    }

    @Test
    fun `same sheet links require source and target placements`() {
        val base = request()
        val sourceReference = base.references.first()
        val sourceLink = sourceReference.links.single()
        val sameSheetReference = sourceReference.copy(
            sheetIds = listOf(sourceLink.sourceSheetId),
            links = listOf(sourceLink.copy(targetSheetId = sourceLink.sourceSheetId)),
        )
        val incomplete = base.copy(references = listOf(sameSheetReference), placements = listOf(base.placements.first()))

        val failed = DrawingSheetReferenceCompiler().compile(incomplete)
        assertTrue(failed.diagnostics.any { it.code == "drawing.reference.placement.incomplete" }, failed.diagnostics.toString())

        val sourcePlacement = base.placements.first()
        val completed = DrawingSheetReferenceCompiler().compile(incomplete.copy(
            placements = listOf(
                sourcePlacement,
                sourcePlacement.copy(
                    placementId = "placement:breaker:target",
                    occurrenceId = sourceLink.targetOccurrenceId,
                    role = DrawingSheetReferencePlacementRole.TARGET,
                    bounds = GraphicBounds(60.0, 55.0, 20.0, 10.0),
                    anchorPoint = GraphicPoint(80.0, 60.0),
                ),
            ),
        ))
        assertTrue(completed.isValid, completed.diagnostics.toString())
    }

    private fun request(): DrawingSheetReferenceRequest {
        val sheetId = ProjectionSheetId("projection.sheet.control")
        val targetSheetId = ProjectionSheetId("projection.sheet.field")
        val breaker = reference("breaker", sheetId, targetSheetId, "02/A1")
        val terminal = reference("terminal", sheetId, targetSheetId, "02/B1")
        return DrawingSheetReferenceRequest(
            sheetPlan = sheetPlan(),
            structurePlan = structurePlan(),
            references = listOf(breaker, terminal),
            placements = listOf(
                placement("breaker", GraphicBounds(60.0, 40.0, 20.0, 10.0), GraphicPoint(80.0, 45.0)),
                placement("terminal", GraphicBounds(60.0, 55.0, 20.0, 10.0), GraphicPoint(80.0, 60.0)),
            ),
        )
    }

    private fun reference(
        suffix: String,
        sourceSheetId: ProjectionSheetId,
        targetSheetId: ProjectionSheetId,
        notation: String,
    ): ProjectionCrossReference {
        val semanticId = StableSemanticIdentity("device:$suffix")
        val sourceOccurrence = "occ:$suffix:control"
        val targetOccurrence = "occ:$suffix:field"
        return ProjectionCrossReference(
            semanticId = semanticId,
            kind = ProjectionCrossReferenceKind.REPEATED_REFERENCE,
            crossReferenceId = ProjectionCrossReferenceId("xref:$suffix"),
            sheetIds = listOf(sourceSheetId, targetSheetId),
            occurrenceIds = listOf(sourceOccurrence, targetOccurrence),
            links = listOf(ProjectionCrossReferenceLink(semanticId, sourceSheetId, targetSheetId, sourceOccurrence, targetOccurrence, notation)),
        )
    }

    private fun placement(suffix: String, bounds: GraphicBounds, anchorPoint: GraphicPoint) = DrawingSheetReferencePlacementInput(
        placementId = "placement:$suffix",
        crossReferenceId = ProjectionCrossReferenceId("xref:$suffix"),
        linkSourceOccurrenceId = "occ:$suffix:control",
        occurrenceId = "occ:$suffix:control",
        subjectId = "device:$suffix",
        role = DrawingSheetReferencePlacementRole.SOURCE,
        representationIdentity = DrawingSymbolIdentity("iec.folio-continuation-reference"),
        bounds = bounds,
        anchorId = DrawingSymbolAnchorId("continuation"),
        anchorPoint = anchorPoint,
        availableAnchorIds = setOf(DrawingSymbolAnchorId("continuation")),
        labelSlotId = DrawingSymbolSlotId("cross-reference"),
        availableLabelSlotIds = setOf(DrawingSymbolSlotId("cross-reference")),
        sheetReferenceSlotId = DrawingSymbolSlotId("source-sheet"),
        zoneReferenceSlotId = DrawingSymbolSlotId("source-zone"),
        availableReferenceSlotIds = setOf(
            DrawingSymbolSlotId("source-sheet"), DrawingSymbolSlotId("source-zone"),
            DrawingSymbolSlotId("target-sheet"), DrawingSymbolSlotId("target-zone"),
        ),
        zoneId = "body",
    )

    private fun sheetPlan(): DrawingSheetCompositionPlan = DrawingSheetCompositionPlan(
        sheetId = "projection.sheet.control",
        sheetBounds = GraphicBounds(-14.0, -14.0, 108.0, 108.0),
        frame = DrawingSheetFrameFact("frame", "schematic", GraphicBounds(-10.0, -10.0, 100.0, 100.0), "projection-sheet-publication", "drawing-composition"),
        drawingAreaBounds = GraphicBounds(-10.0, -10.0, 100.0, 80.0),
        titleBlock = DrawingSheetTitleBlockFact(GraphicBounds(-10.0, 70.0, 100.0, 20.0), "Control", "schematic", "01", "A", "Initial", "A3", "landscape", "projection-sheet-publication", "drawing-composition"),
        namedZones = listOf(DrawingSheetNamedZoneFact("body", "Body", 0, "projection-sheet-publication")),
        coordinateZones = emptyList(),
        margins = DrawingSheetMarginFact(10.0, 4.0, "presentation-profile-policy"),
    )

    private fun structurePlan(): DrawingSheetStructurePlan = DrawingSheetStructurePlan(
        sheetId = "projection.sheet.control",
        subjects = listOf(
            subject("breaker", GraphicBounds(0.0, 10.0, 20.0, 20.0)),
            subject("terminal", GraphicBounds(30.0, 10.0, 20.0, 20.0)),
        ),
        rails = emptyList(), lanes = emptyList(), terminalStrips = emptyList(), labelBands = emptyList(), routeChannels = emptyList(),
    )

    private fun subject(suffix: String, bounds: GraphicBounds) = DrawingSheetStructureSubjectFact(
        subjectId = "device:$suffix",
        representationIdentity = DrawingSymbolIdentity("iec.protective-device"),
        bounds = bounds,
        anchors = emptyList(),
        labels = listOf(DrawingSheetStructureLabelFact("label:$suffix", DrawingSymbolSlotId("device-tag"), GraphicBounds(bounds.x, 2.0, 14.0, 6.0))),
        representationAuthority = "drawing-symbol-anatomy",
        boundsAuthority = "graphic-primitive-ir",
    )
}
