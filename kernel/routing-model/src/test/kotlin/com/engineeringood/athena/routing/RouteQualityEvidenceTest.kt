package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteQualityEvidenceTest {
    private val compiler = RouteQualityEvidenceCompiler(RouteQualityPolicyCompiler())
    private val policy = RouteQualityPolicy.standardProfessional()

    @Test
    fun `accepted route evidence exposes quality lane channel planner snapshot and provenance`() {
        val snapshot = RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            family = "schematic",
            routeFacts = listOf(routeFact()),
        )

        val result = compiler.compile(snapshot, policy)

        assertTrue(result.successful)
        val evidence = result.routes.single()
        assertEquals(SchematicRouteId("route:a"), evidence.routeId)
        assertEquals(RouteQualityState.SATISFIED, evidence.grade)
        assertEquals(
            listOf("bends", "crossings", "density", "label-collisions", "lane-changes", "length", "stable-tie-breaker"),
            evidence.scoreComponents.map { component -> component.criterion },
        )
        assertEquals(RouteLaneId("lane:0"), evidence.laneAssignment.laneId)
        assertEquals(listOf("channel:main"), evidence.selectedChannelIds)
        assertEquals("athena-route-engine", evidence.plannerId)
        assertEquals("snapshot:evidence", evidence.compilerSnapshotId)
        assertEquals(SourceProvenance("routes.athena", 1, 1, 1, 20), evidence.provenance)
    }

    @Test
    fun `candidate rejections normalize to machine-readable diagnostics`() {
        val comparison = RoutePlannerCandidateCompiler().compare(
            listOf(
                plannerCandidate(
                    evidence = null,
                ),
            ),
            policy,
        )

        val diagnostics = compiler.diagnosticsFor(comparison)

        val diagnostic = diagnostics.single()
        assertEquals("route.quality.evidence.candidate.rejected", diagnostic.code)
        assertEquals(RouteQualityEvidenceSeverity.BLOCKING, diagnostic.severity)
        assertEquals("experimental-router", diagnostic.plannerId)
        assertEquals("snapshot:evidence", diagnostic.snapshotId)
        assertEquals(listOf("route:a"), diagnostic.affectedRouteIds)
        assertContains(diagnostic.violatedFacts, "planner.candidate.proof.missing")
        assertEquals(SourceProvenance("routes.athena", 1, 1, 1, 20), diagnostic.provenance)
    }

    @Test
    fun `hard reject or unresolved route evidence blocks success`() {
        val hardRejectSnapshot = RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            family = "schematic",
            routeFacts = listOf(
                routeFact(
                    quality = RouteQuality.fallback(
                        failedConstraintIds = listOf(RouteConstraintId("constraint:missing-anchor")),
                        message = "Missing anchor.",
                    ),
                ),
            ),
        )
        val missingSnapshotEvidence = RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            family = "schematic",
            routeFacts = listOf(routeFact(compilerSnapshotId = "snapshot:other")),
        )

        val hardReject = compiler.compile(hardRejectSnapshot, policy)
        val missingSnapshot = compiler.compile(missingSnapshotEvidence, policy)

        assertFalse(hardReject.successful)
        assertContains(hardReject.diagnostics.map { diagnostic -> diagnostic.code }, "route.quality.evidence.hard-reject")
        assertFalse(missingSnapshot.successful)
        assertContains(missingSnapshot.diagnostics.map { diagnostic -> diagnostic.code }, "route.quality.evidence.snapshot.missing")
    }

    @Test
    fun `normalized payload carries Athena-owned facts only`() {
        val snapshot = RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            family = "schematic",
            routeFacts = listOf(routeFact()),
        )
        val result = compiler.compile(snapshot, policy)

        val payload = compiler.normalize(result)

        assertEquals("athena", payload.authority)
        assertEquals(listOf("route:a"), payload.routes.map { route -> route.routeId })
        assertTrue(payload.rejectedPlannerObjects.isEmpty())
        assertTrue(payload.rawMarkupFragments.isEmpty())
    }

    private fun routeFact(
        compilerSnapshotId: String = "snapshot:evidence",
        quality: RouteQuality = RouteQuality.satisfied(),
    ): RouteFact {
        val routeId = SchematicRouteId("route:a")
        val connectionId = ElectricalConnectionId("connection:a")
        return RouteFact(
            routeId = routeId,
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            connectionId = connectionId,
            routeIntentId = RouteIntentId("route-intent:a"),
            bundleId = RouteBundleId("bundle:a"),
            selectedChannelIds = listOf("channel:main"),
            plannerId = "athena-route-engine",
            compilerSnapshotId = compilerSnapshotId,
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
            qualityMetrics = RouteQualityMetrics(
                crossingCount = 0,
                bendCount = 1,
                length = 40,
                channelChangeCount = 0,
                bundleContinuityPenalty = 0,
                labelClearanceViolationCount = 0,
            ),
            source = anchor("source"),
            target = anchor("target"),
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
            quality = quality,
        )
    }

    private fun plannerCandidate(
        evidence: RoutePlannerCandidateRouteEvidence?,
    ): RoutePlannerCandidate {
        return RoutePlannerCandidate(
            candidateId = RoutePlannerCandidateId("candidate:a"),
            plannerId = "experimental-router",
            snapshotId = LayoutSnapshotId("snapshot:evidence"),
            routes = listOf(
                RoutePlannerRouteCandidate(
                    routeId = SchematicRouteId("route:a"),
                    connectionId = ElectricalConnectionId("connection:a"),
                    sourceAnchor = anchor("source"),
                    targetAnchor = anchor("target"),
                    laneAssignment = RouteLaneAssignment(
                        laneId = RouteLaneId("lane:0"),
                        lane = SchematicRouteLane(0),
                        orientation = RouteLaneOrientation.HORIZONTAL,
                        capacity = RouteLaneCapacity(1),
                        occupancy = RouteLaneOccupancy(1, listOf(SchematicRouteId("route:a"))),
                    ),
                    constraints = listOf(
                        RouteConstraint(
                            constraintId = RouteConstraintId("constraint:orthogonal"),
                            kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                            connectionId = ElectricalConnectionId("connection:a"),
                            priority = RouteConstraintPriority.REQUIRED,
                        ),
                    ),
                    qualityMetrics = RouteQualityMetrics(0, 1, 40, 0, 0, 0),
                    evidence = evidence,
                ),
            ),
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
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
            policySource = "candidate-test",
        )
    }
}
