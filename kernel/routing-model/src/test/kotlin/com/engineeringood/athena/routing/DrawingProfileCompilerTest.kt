package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DrawingProfileCompilerTest {
    private val compiler = DrawingProfileCompiler()

    @Test
    fun `drawing profile resolves sheet grammar and line classes`() {
        val profile = DrawingStandardProfile.standardProfessional()

        val result = assertIs<DrawingProfileCompilation.Success>(compiler.compile(listOf(profile)))

        val compiled = result.profiles.single()
        assertEquals(DrawingProfileId("control-drawing-iec"), compiled.profileId)
        assertEquals(DrawingFramePolicy(enabled = true), compiled.frame)
        assertEquals(DrawingCoordinateGridPolicy(enabled = true, spacing = 10), compiled.coordinateGrid)
        assertEquals(setOf("title-block"), compiled.titleBlockRegions.map { region -> region.regionId }.toSet())
        assertEquals(1.0, compiled.defaultTextScale)
        assertEquals(
            setOf(
                LineStyleId("stroke:power"),
                LineStyleId("stroke:control"),
                LineStyleId("stroke:protective-earth"),
                LineStyleId("stroke:safety"),
                LineStyleId("stroke:communication"),
            ),
            compiled.strokeClasses.map { stroke -> stroke.lineStyleId }.toSet(),
        )
        assertEquals(DrawingCrossingBehavior.DISCONNECTED_CROSSING, compiled.crossingRule)
        assertEquals(DrawingLabelPolicy.REFERENCE_DESIGNATION, compiled.referenceDesignationLabelPolicy)
    }

    @Test
    fun `route facts resolve exactly one connection presentation class`() {
        val profile = DrawingStandardProfile.standardProfessional()
        val route = routeFact(role = ElectricalConnectionRole.POWER_FEED)

        val resolution = assertIs<DrawingProfileResolution.Success>(
            compiler.resolveRoutes(profile, listOf(route), selectedPolicyId = "ControlDrawingProjection"),
        )

        val line = resolution.lines.single()
        assertEquals(route.routeId, line.routeId)
        assertEquals(ConnectionLineClassId("line:power"), line.lineClassId)
        assertEquals(LineStyleId("stroke:power"), line.lineStyleId)
        assertEquals(DrawingStyle.SOLID, line.style)
        assertEquals("drawing.power", line.colorKey)
        assertEquals(DrawingEndpointBehavior.ATTACH_TO_ANCHOR, line.endpointBehavior)
        assertEquals(DrawingLabelPolicy.TERMINAL_PAIR, line.labelPolicy)
        assertEquals(DrawingCrossingBehavior.JUNCTION_REQUIRED, line.crossingBehavior)
        assertEquals("ControlDrawingProjection", line.selectedPolicyId)
        assertEquals(SourceProvenance("routes.athena", 1, 1, 1, 20), line.provenance)
    }

    @Test
    fun `invalid drawing declarations fail before rendering`() {
        val duplicate = DrawingStandardProfile.standardProfessional().copy(
            presentationClasses = DrawingStandardProfile.standardProfessional().presentationClasses +
                DrawingStandardProfile.standardProfessional().presentationClasses.first(),
        )
        val invalidStroke = DrawingStandardProfile.standardProfessional().copy(
            strokeClasses = listOf(
                DrawingStrokeClass(
                    lineStyleId = LineStyleId("stroke:bad"),
                    weight = 0.0,
                    style = DrawingStyle.SOLID,
                    colorKey = "drawing.bad",
                ),
            ),
        )

        val diagnostics = assertIs<DrawingProfileCompilation.Failure>(
            compiler.compile(listOf(duplicate, invalidStroke)),
        ).diagnostics.map { diagnostic -> diagnostic.code }

        assertContains(diagnostics, "drawing.profile.line-class.duplicate")
        assertContains(diagnostics, "drawing.profile.stroke-class.invalid")
    }

    @Test
    fun `unclassified routes emit typed diagnostics`() {
        val profile = DrawingStandardProfile.standardProfessional().copy(
            presentationClasses = DrawingStandardProfile.standardProfessional().presentationClasses
                .filterNot { lineClass -> lineClass.lineKind == ConnectionLineKind.CONTROL },
        )
        val route = routeFact(role = ElectricalConnectionRole.CONTROL_SIGNAL)

        val diagnostics = assertIs<DrawingProfileResolution.Failure>(
            compiler.resolveRoutes(profile, listOf(route), selectedPolicyId = "ControlDrawingProjection"),
        ).diagnostics

        assertContains(diagnostics.map { diagnostic -> diagnostic.code }, "drawing.profile.line-class.unclassified")
        assertEquals(listOf("route:a"), diagnostics.single().affectedRouteIds)
    }

    @Test
    fun `presentation payload carries Athena line style evidence only`() {
        val profile = DrawingStandardProfile.standardProfessional()
        val route = routeFact(role = ElectricalConnectionRole.POWER_FEED)
        val resolution = assertIs<DrawingProfileResolution.Success>(
            compiler.resolveRoutes(profile, listOf(route), selectedPolicyId = "ControlDrawingProjection"),
        )

        val payload = compiler.normalize(resolution)

        assertEquals("athena", payload.authority)
        assertEquals(listOf("route:a"), payload.lines.map { line -> line.routeId })
        assertTrue(payload.rendererInferences.isEmpty())
        assertTrue(payload.rawMarkupFragments.isEmpty())
    }

    private fun routeFact(role: ElectricalConnectionRole): RouteFact {
        val routeId = SchematicRouteId("route:a")
        val connectionId = ElectricalConnectionId("connection:a")
        return RouteFact(
            routeId = routeId,
            snapshotId = LayoutSnapshotId("snapshot:drawing-profile"),
            connectionId = connectionId,
            routeIntentId = RouteIntentId("route-intent:a"),
            bundleId = RouteBundleId("bundle:a"),
            selectedChannelIds = listOf("channel:main"),
            plannerId = "athena-route-engine",
            compilerSnapshotId = "snapshot:drawing-profile",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
            qualityMetrics = RouteQualityMetrics(0, 1, 40, 0, 0, 0),
            source = anchor("source"),
            target = anchor("target"),
            connectionRole = role,
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(20, 20),
                    end = SchematicRoutePoint(80, 20),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            lane = SchematicRouteLane(0),
            laneAssignment = RouteLaneAssignment(
                laneId = RouteLaneId("lane:0"),
                lane = SchematicRouteLane(0),
                orientation = RouteLaneOrientation.HORIZONTAL,
                capacity = RouteLaneCapacity(1),
                occupancy = RouteLaneOccupancy(1, listOf(routeId)),
            ),
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:orthogonal"),
                    kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                    connectionId = connectionId,
                    priority = RouteConstraintPriority.REQUIRED,
                ),
            ),
        )
    }

    private fun anchor(name: String): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$name"),
            subjectId = StableSemanticIdentity("component:$name"),
            occurrenceId = LayoutOccurrenceId("occurrence:$name"),
            portId = ElectricalPortId("port:$name"),
            portRole = ElectricalPortRole.OUTPUT,
            side = TerminalSide.RIGHT,
            point = SchematicRoutePoint(20, 20),
            gridPoint = SchematicRoutePoint(20, 20),
            policySource = "drawing-profile-test",
        )
    }
}
