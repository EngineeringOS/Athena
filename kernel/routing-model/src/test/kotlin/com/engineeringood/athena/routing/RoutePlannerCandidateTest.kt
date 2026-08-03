package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoutePlannerCandidateTest {
    private val policy = RouteQualityPolicy.standardProfessional()
    private val compiler = RoutePlannerCandidateCompiler(RouteQualityPolicyCompiler())

    @Test
    fun `valid planner candidates are compared through route quality policy`() {
        val better = candidate(
            candidateId = "candidate:b",
            plannerId = "athena-native",
            metrics = metrics(bends = 1, length = 40),
        )
        val worse = candidate(
            candidateId = "candidate:a",
            plannerId = "athena-native",
            metrics = metrics(bends = 3, length = 90),
        )

        val result = compiler.compare(listOf(worse, better), policy)

        assertEquals(RoutePlannerCandidateId("candidate:b"), result.selected?.candidateId)
        assertEquals(emptyList(), result.rejections)
        assertTrue(result.rankedCandidates.first().score.totalPenalty < result.rankedCandidates.last().score.totalPenalty)
    }

    @Test
    fun `candidate validation rejects missing required evidence before scoring`() {
        val result = compiler.compare(
            listOf(
                candidate(candidateId = "candidate:snapshot", snapshotId = null),
                candidate(candidateId = "candidate:endpoint", routeId = null),
                candidate(candidateId = "candidate:anchor", sourceAnchor = null),
                candidate(candidateId = "candidate:lane", laneAssignment = null),
                candidate(candidateId = "candidate:constraint", constraints = emptyList()),
                candidate(candidateId = "candidate:proof", proof = null),
            ),
            policy,
        )

        val codes = result.rejections.flatMap { rejection -> rejection.violations.map { violation -> violation.code } }
        assertContains(codes, "planner.candidate.snapshot.missing")
        assertContains(codes, "planner.candidate.endpoint.missing")
        assertContains(codes, "planner.candidate.anchor.missing")
        assertContains(codes, "planner.candidate.lane.missing")
        assertContains(codes, "planner.candidate.constraint.missing")
        assertContains(codes, "planner.candidate.proof.missing")
        assertTrue(result.rankedCandidates.isEmpty())
    }

    @Test
    fun `candidate rejection records planner snapshot violated facts and provenance`() {
        val result = compiler.compare(
            listOf(candidate(candidateId = "candidate:bad", plannerId = "experimental-router", proof = null)),
            policy,
        )

        val rejection = result.rejections.single()
        assertEquals(RoutePlannerCandidateId("candidate:bad"), rejection.candidateId)
        assertEquals("experimental-router", rejection.plannerId)
        assertEquals(LayoutSnapshotId("snapshot:candidate"), rejection.snapshotId)
        assertEquals(SourceProvenance("routes.athena", 1, 1, 1, 20), rejection.provenance)
        assertEquals(listOf(SchematicRouteId("route:a")), rejection.affectedRouteIds)
        assertEquals("planner.candidate.proof.missing", rejection.violations.single().code)
    }

    @Test
    fun `candidate tie breaker is deterministic across candidate planner and route ids`() {
        val first = candidate(candidateId = "candidate:a", plannerId = "planner:b", routeId = SchematicRouteId("route:b"))
        val second = candidate(candidateId = "candidate:a", plannerId = "planner:a", routeId = SchematicRouteId("route:c"))
        val third = candidate(candidateId = "candidate:b", plannerId = "planner:a", routeId = SchematicRouteId("route:a"))

        val result = compiler.compare(listOf(third, first, second), policy)

        assertNotNull(result.selected)
        assertEquals(
            listOf("candidate:a|planner:a|route:c", "candidate:a|planner:b|route:b", "candidate:b|planner:a|route:a"),
            result.rankedCandidates.map { candidate -> candidate.stableTieBreaker },
        )
        assertEquals("planner:a", result.selected.plannerId)
    }

    private fun candidate(
        candidateId: String,
        plannerId: String = "athena-native",
        snapshotId: LayoutSnapshotId? = LayoutSnapshotId("snapshot:candidate"),
        routeId: SchematicRouteId? = SchematicRouteId("route:a"),
        sourceAnchor: TerminalAnchorFact? = anchor("source"),
        targetAnchor: TerminalAnchorFact? = anchor("target"),
        laneAssignment: RouteLaneAssignment? = routeId?.let { route ->
            RouteLaneAssignment(
                laneId = RouteLaneId("lane:0"),
                lane = SchematicRouteLane(0),
                orientation = RouteLaneOrientation.HORIZONTAL,
                capacity = RouteLaneCapacity(1),
                occupancy = RouteLaneOccupancy(1, listOf(route)),
            )
        },
        constraints: List<RouteConstraint> = routeId?.let { route ->
            listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:${route.value}:orthogonal"),
                    kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                    connectionId = ElectricalConnectionId("connection:a"),
                    priority = RouteConstraintPriority.REQUIRED,
                ),
            )
        }.orEmpty(),
        metrics: RouteQualityMetrics = metrics(),
        proof: RoutePlannerCandidateRouteEvidence? = RoutePlannerCandidateRouteEvidence(
            plannerRouteId = "planner-route:a",
            compilerSnapshotId = "snapshot:candidate",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
        ),
    ): RoutePlannerCandidate {
        return RoutePlannerCandidate(
            candidateId = RoutePlannerCandidateId(candidateId),
            plannerId = plannerId,
            snapshotId = snapshotId,
            routes = listOf(
                RoutePlannerRouteCandidate(
                    routeId = routeId,
                    connectionId = ElectricalConnectionId("connection:a"),
                    sourceAnchor = sourceAnchor,
                    targetAnchor = targetAnchor,
                    laneAssignment = laneAssignment,
                    constraints = constraints,
                    qualityMetrics = metrics,
                    evidence = proof,
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

    private fun metrics(
        bends: Int = 1,
        length: Int = 40,
    ): RouteQualityMetrics = RouteQualityMetrics(
        crossingCount = 0,
        bendCount = bends,
        length = length,
        channelChangeCount = 0,
        bundleContinuityPenalty = 0,
        labelClearanceViolationCount = 0,
    )
}
