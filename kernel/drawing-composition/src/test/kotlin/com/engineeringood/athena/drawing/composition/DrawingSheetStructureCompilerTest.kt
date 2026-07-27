package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.projection.ProjectionSheetFrame
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetRevisionMetadata
import com.engineeringood.athena.projection.ProjectionSheetTitleBlock
import com.engineeringood.athena.projection.ProjectionSheetViewComposition
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

class DrawingSheetStructureCompilerTest {
    @Test
    fun `compiler emits deterministic professional structure facts and derived terminal strip`() {
        val request = request()

        val result = DrawingSheetStructureCompiler().compile(request)

        assertTrue(result.isValid, result.diagnostics.toString())
        val plan = requireNotNull(result.plan)
        assertEquals(listOf("rail:control", "rail:supply"), plan.rails.map { it.railId })
        assertEquals(listOf("lane:control", "lane:power"), plan.lanes.map { it.laneId })
        assertEquals(GraphicBounds(-6.0, 4.0, 32.0, 32.0), plan.terminalStrips.single().bounds)
        assertEquals(listOf("band:control", "band:power"), plan.labelBands.map { it.bandId })
        assertEquals(listOf("channel:control", "channel:power"), plan.routeChannels.map { it.channelId })
        assertEquals(listOf("device:breaker", "device:terminal"), plan.subjects.map { it.subjectId })
        assertEquals(result, DrawingSheetStructureCompiler().compile(request.copy(
            subjects = request.subjects.reversed().map { it.copy(anchors = it.anchors.reversed(), labels = it.labels.reversed()) },
            rails = request.rails.reversed(),
            lanes = request.lanes.reversed(),
            labelBands = request.labelBands.reversed(),
            routeChannels = request.routeChannels.reversed(),
        )))
    }

    @Test
    fun `compiler preserves package representation anchors and label slots through transport`() {
        val result = DrawingSheetStructureCompiler().compile(request())

        assertTrue(result.isValid, result.diagnostics.toString())
        val subject = requireNotNull(result.plan).subjects.first { it.subjectId == "device:breaker" }
        assertEquals("iec.protective-device", subject.representationIdentity.value)
        assertEquals(listOf("line", "load"), subject.anchors.map { it.anchorId.value })
        assertEquals(listOf(GraphicPoint(4.0, 20.0), GraphicPoint(20.0, 20.0)), subject.anchors.map { it.point })
        assertEquals(listOf("device-tag"), subject.labels.map { it.slotId.value })
        assertEquals("drawing-symbol-anatomy", subject.representationAuthority)
        assertEquals("graphic-primitive-ir", subject.boundsAuthority)

        val proof = requireNotNull(result.proof)
        assertEquals("drawing-composition", proof.boundsAuthority)
        assertEquals("drawing-symbol-anatomy", proof.representationAuthority)
        assertEquals("drawing-structure-intent", proof.structureIntentAuthority)
        assertEquals("presentation-profile-policy", proof.policyAuthority)
        val payload = requireNotNull(result.toTransportPayload())
        assertEquals("iec.protective-device", payload.subjects.first { it.subjectId == "device:breaker" }.representationIdentity)
        assertEquals(listOf("line", "load"), payload.subjects.first { it.subjectId == "device:breaker" }.anchors.map { it.anchorId })
        assertEquals(payload, DrawingSheetStructureCompiler().compile(request()).toTransportPayload())
    }

    @Test
    fun `compiler fails closed for malformed collision whitespace overflow missing identity and out of sheet facts`() {
        val base = request()
        val cases = listOf(
            base.copy(subjects = base.subjects + base.subjects.first()) to "drawing.structure.subject.duplicate",
            base.copy(subjects = base.subjects.map { if (it.subjectId == "device:terminal") it.copy(bounds = GraphicBounds(Double.NaN, 10.0, 20.0, 20.0)) else it }) to "drawing.structure.bounds.invalid",
            base.copy(lanes = base.lanes.map { if (it.laneId == "lane:power") it.copy(subjectIds = listOf("device:missing")) else it }) to "drawing.structure.member.missing",
            base.copy(rails = base.rails.map { if (it.railId == "rail:supply") it.copy(subjectIds = listOf("device:breaker", "device:breaker")) else it }) to "drawing.structure.member.duplicate",
            base.copy(subjects = base.subjects.map { if (it.subjectId == "device:terminal") it.copy(bounds = GraphicBounds(12.0, 10.0, 20.0, 20.0)) else it }) to "drawing.structure.subject.collision",
            base.copy(subjects = base.subjects.map { subject ->
                if (subject.subjectId == "device:terminal") {
                    subject.copy(labels = subject.labels.map { it.copy(bounds = GraphicBounds(10.0, 2.0, 14.0, 6.0)) })
                } else {
                    subject
                }
            }) to "drawing.structure.label.collision",
            base.copy(policy = base.policy.copy(maximumSubjectGap = 1.0)) to "drawing.structure.whitespace.excessive",
            base.copy(labelBands = base.labelBands.map { if (it.bandId == "band:power") it.copy(bounds = GraphicBounds(0.0, 0.0, 4.0, 4.0)) else it }) to "drawing.structure.label.overflow",
            base.copy(subjects = base.subjects.map { if (it.subjectId == "device:breaker") it.copy(requiredAnchorIds = setOf(DrawingSymbolAnchorId("missing"))) else it }) to "drawing.structure.anchor.required-missing",
            base.copy(subjects = base.subjects.map { if (it.subjectId == "device:breaker") it.copy(requiredLabelSlotIds = setOf(DrawingSymbolSlotId("missing"))) else it }) to "drawing.structure.label-slot.required-missing",
            base.copy(routeChannels = base.routeChannels.map { if (it.channelId == "channel:power") it.copy(bounds = GraphicBounds(500.0, 0.0, 10.0, 10.0)) else it }) to "drawing.structure.content.out-of-sheet",
            base.copy(policy = base.policy.copy(terminalStripPadding = Double.MAX_VALUE)) to "drawing.structure.bounds.derived-invalid",
        )

        cases.forEach { (request, expectedCode) ->
            val result = DrawingSheetStructureCompiler().compile(request)
            assertFalse(result.isValid, expectedCode)
            assertNull(result.plan, expectedCode)
            assertNull(result.proof, expectedCode)
            assertNull(result.toTransportPayload(), expectedCode)
            assertTrue(result.diagnostics.any { it.code == expectedCode }, result.diagnostics.toString())
            assertTrue(result.diagnostics.all { it.authority.isNotBlank() && it.subject.isNotBlank() && it.message.isNotBlank() })
        }
    }

    @Test
    fun `touching subject bounds are not a collision`() {
        val base = request()
        val result = DrawingSheetStructureCompiler().compile(base.copy(
            subjects = base.subjects.map {
                if (it.subjectId == "device:terminal") it.copy(bounds = GraphicBounds(20.0, 10.0, 20.0, 20.0)) else it
            },
        ))

        assertTrue(result.diagnostics.none { it.code == "drawing.structure.subject.collision" }, result.diagnostics.toString())
    }

    private fun request(): DrawingSheetStructureRequest {
        val breaker = DrawingSheetStructureSubjectInput(
            subjectId = "device:breaker",
            representationIdentity = DrawingSymbolIdentity("iec.protective-device"),
            bounds = GraphicBounds(0.0, 10.0, 20.0, 20.0),
            anchors = listOf(
                DrawingSheetStructureAnchorInput(DrawingSymbolAnchorId("line"), GraphicPoint(4.0, 20.0)),
                DrawingSheetStructureAnchorInput(DrawingSymbolAnchorId("load"), GraphicPoint(20.0, 20.0)),
            ),
            requiredAnchorIds = setOf(DrawingSymbolAnchorId("line"), DrawingSymbolAnchorId("load")),
            labels = listOf(
                DrawingSheetStructureLabelInput("label:breaker", DrawingSymbolSlotId("device-tag"), GraphicBounds(2.0, 2.0, 14.0, 6.0)),
            ),
            requiredLabelSlotIds = setOf(DrawingSymbolSlotId("device-tag")),
        )
        val terminal = DrawingSheetStructureSubjectInput(
            subjectId = "device:terminal",
            representationIdentity = DrawingSymbolIdentity("iec.terminal"),
            bounds = GraphicBounds(30.0, 10.0, 20.0, 20.0),
            anchors = listOf(DrawingSheetStructureAnchorInput(DrawingSymbolAnchorId("terminal"), GraphicPoint(40.0, 20.0))),
            requiredAnchorIds = setOf(DrawingSymbolAnchorId("terminal")),
            labels = listOf(
                DrawingSheetStructureLabelInput("label:terminal", DrawingSymbolSlotId("terminal-number"), GraphicBounds(32.0, 2.0, 14.0, 6.0)),
            ),
            requiredLabelSlotIds = setOf(DrawingSymbolSlotId("terminal-number")),
        )
        return DrawingSheetStructureRequest(
            sheetPlan = sheetPlan(),
            subjects = listOf(breaker, terminal),
            rails = listOf(
                DrawingSheetRailIntent("rail:supply", DrawingSheetAxis.HORIZONTAL, GraphicPoint(-10.0, 0.0), GraphicPoint(60.0, 0.0), listOf("device:breaker")),
                DrawingSheetRailIntent("rail:control", DrawingSheetAxis.HORIZONTAL, GraphicPoint(-10.0, 40.0), GraphicPoint(60.0, 40.0), listOf("device:terminal")),
            ),
            lanes = listOf(
                DrawingSheetLaneIntent("lane:power", DrawingSheetAxis.HORIZONTAL, GraphicBounds(-10.0, -5.0, 80.0, 40.0), listOf("device:breaker", "device:terminal")),
                DrawingSheetLaneIntent("lane:control", DrawingSheetAxis.HORIZONTAL, GraphicBounds(-10.0, 40.0, 80.0, 30.0), emptyList()),
            ),
            terminalStrips = listOf(DrawingSheetTerminalStripIntent("strip:xt1", listOf("device:breaker"))),
            labelBands = listOf(
                DrawingSheetLabelBandIntent("band:power", GraphicBounds(-10.0, 0.0, 80.0, 10.0), listOf("label:breaker", "label:terminal")),
                DrawingSheetLabelBandIntent("band:control", GraphicBounds(-10.0, 60.0, 80.0, 10.0), emptyList()),
            ),
            routeChannels = listOf(
                DrawingSheetRouteChannelIntent("channel:power", DrawingSheetAxis.HORIZONTAL, GraphicBounds(-10.0, 30.0, 80.0, 8.0), listOf(DrawingSheetAnchorReference("device:breaker", DrawingSymbolAnchorId("load")))),
                DrawingSheetRouteChannelIntent("channel:control", DrawingSheetAxis.VERTICAL, GraphicBounds(52.0, 0.0, 8.0, 70.0), listOf(DrawingSheetAnchorReference("device:terminal", DrawingSymbolAnchorId("terminal")))),
            ),
            policy = DrawingSheetStructurePolicy("m33.compact-structure-v1", terminalStripPadding = 6.0, maximumSubjectGap = 12.0),
        )
    }

    private fun sheetPlan(): DrawingSheetCompositionPlan {
        val publication = ProjectionSheetPublication(
            pageSize = ProjectionSheetPageSize("A3", "landscape"),
            frame = ProjectionSheetFrame("frame", "schematic"),
            coordinateZones = listOf(ProjectionSheetCoordinateZone("body", "Body", 0)),
            titleBlock = ProjectionSheetTitleBlock("Control", "schematic", "01"),
            revisionMetadata = ProjectionSheetRevisionMetadata("A", "Initial"),
            viewComposition = ProjectionSheetViewComposition("schematic", 0),
        )
        return requireNotNull(DrawingSheetCompositionCompiler().compile(
            DrawingSheetCompositionRequest(
                ProjectionSheetId("projection.sheet.control"),
                publication,
                GraphicBounds(0.0, 0.0, 80.0, 60.0),
                DrawingSheetCompositionPolicy("sheet-policy", 10.0, 4.0, 16.0, 200.0, 180.0, listOf("A"), listOf("1")),
            ),
        ).plan)
    }
}
