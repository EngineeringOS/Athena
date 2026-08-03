package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutSnapshotId

@JvmInline
value class RoutePlannerCandidateId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route planner candidate id must not be blank." }
    }
}

data class RoutePlannerCandidateRouteEvidence(
    val plannerRouteId: String,
    val compilerSnapshotId: String,
    val provenance: SourceProvenance?,
) {
    init {
        require(plannerRouteId.isNotBlank()) { "Planner route id must not be blank." }
        require(compilerSnapshotId.isNotBlank()) { "Candidate compiler snapshot id must not be blank." }
    }
}

data class RoutePlannerRouteCandidate(
    val routeId: SchematicRouteId?,
    val connectionId: ElectricalConnectionId?,
    val sourceAnchor: TerminalAnchorFact?,
    val targetAnchor: TerminalAnchorFact?,
    val laneAssignment: RouteLaneAssignment?,
    val constraints: List<RouteConstraint>,
    val qualityMetrics: RouteQualityMetrics?,
    val evidence: RoutePlannerCandidateRouteEvidence?,
)

data class RoutePlannerCandidate(
    val candidateId: RoutePlannerCandidateId,
    val plannerId: String,
    val snapshotId: LayoutSnapshotId?,
    val routes: List<RoutePlannerRouteCandidate>,
    val provenance: SourceProvenance? = null,
) {
    init {
        require(plannerId.isNotBlank()) { "Route planner id must not be blank." }
    }
}

data class RoutePlannerCandidateViolation(
    val code: String,
    val subject: String,
    val message: String,
) {
    init {
        require(code.isNotBlank()) { "Planner candidate violation code must not be blank." }
        require(subject.isNotBlank()) { "Planner candidate violation subject must not be blank." }
        require(message.isNotBlank()) { "Planner candidate violation message must not be blank." }
    }
}

data class RoutePlannerCandidateRejection(
    val candidateId: RoutePlannerCandidateId,
    val plannerId: String,
    val snapshotId: LayoutSnapshotId?,
    val affectedRouteIds: List<SchematicRouteId>,
    val provenance: SourceProvenance?,
    val violations: List<RoutePlannerCandidateViolation>,
)

data class RankedRoutePlannerCandidate(
    val candidateId: RoutePlannerCandidateId,
    val plannerId: String,
    val snapshotId: LayoutSnapshotId,
    val score: RouteQualityScore,
    val stableTieBreaker: String,
    val routes: List<RoutePlannerRouteCandidate>,
    val provenance: SourceProvenance?,
)

data class RoutePlannerCandidateComparison(
    val selected: RankedRoutePlannerCandidate?,
    val rankedCandidates: List<RankedRoutePlannerCandidate>,
    val rejections: List<RoutePlannerCandidateRejection>,
)

class RoutePlannerCandidateCompiler(
    private val qualityPolicyCompiler: RouteQualityPolicyCompiler = RouteQualityPolicyCompiler(),
) {
    fun compare(
        candidates: List<RoutePlannerCandidate>,
        policy: RouteQualityPolicy,
    ): RoutePlannerCandidateComparison {
        val rejections = mutableListOf<RoutePlannerCandidateRejection>()
        val ranked = candidates.mapNotNull { candidate ->
            val violations = validate(candidate)
            if (violations.isNotEmpty()) {
                rejections += RoutePlannerCandidateRejection(
                    candidateId = candidate.candidateId,
                    plannerId = candidate.plannerId,
                    snapshotId = candidate.snapshotId,
                    affectedRouteIds = candidate.routes.mapNotNull { route -> route.routeId }.distinct(),
                    provenance = candidate.provenance,
                    violations = violations,
                )
                null
            } else {
                rank(candidate, policy)
            }
        }.sortedWith(
            compareBy<RankedRoutePlannerCandidate>(
                { candidate -> candidate.score.totalPenalty },
                { candidate -> candidate.stableTieBreaker },
            ),
        )
        return RoutePlannerCandidateComparison(
            selected = ranked.firstOrNull(),
            rankedCandidates = ranked,
            rejections = rejections.sortedWith(
                compareBy<RoutePlannerCandidateRejection>(
                    { rejection -> rejection.candidateId.value },
                    { rejection -> rejection.plannerId },
                ),
            ),
        )
    }

    private fun validate(candidate: RoutePlannerCandidate): List<RoutePlannerCandidateViolation> {
        val violations = mutableListOf<RoutePlannerCandidateViolation>()
        if (candidate.snapshotId == null) {
            violations += violation(
                code = "planner.candidate.snapshot.missing",
                subject = candidate.candidateId.value,
                message = "Planner candidate must be bound to a compiler snapshot before scoring.",
            )
        }
        if (candidate.routes.isEmpty()) {
            violations += violation(
                code = "planner.candidate.endpoint.missing",
                subject = candidate.candidateId.value,
                message = "Planner candidate must carry at least one route endpoint.",
            )
        }
        candidate.routes.forEachIndexed { index, route ->
            val subject = route.routeId?.value ?: "${candidate.candidateId.value}:route:$index"
            if (route.routeId == null || route.connectionId == null) {
                violations += violation(
                    code = "planner.candidate.endpoint.missing",
                    subject = subject,
                    message = "Planner candidate route must carry route and connection endpoints.",
                )
            }
            if (route.sourceAnchor == null || route.targetAnchor == null) {
                violations += violation(
                    code = "planner.candidate.anchor.missing",
                    subject = subject,
                    message = "Planner candidate route must carry both terminal anchors.",
                )
            }
            if (route.laneAssignment == null || route.routeId == null || route.routeId !in route.laneAssignment.occupancy.routeIds) {
                violations += violation(
                    code = "planner.candidate.lane.missing",
                    subject = subject,
                    message = "Planner candidate route must carry lane assignment evidence for its route.",
                )
            }
            if (route.constraints.isEmpty()) {
                violations += violation(
                    code = "planner.candidate.constraint.missing",
                    subject = subject,
                    message = "Planner candidate route must carry constraints before quality scoring.",
                )
            }
            if (route.qualityMetrics == null || route.evidence == null) {
                violations += violation(
                    code = "planner.candidate.proof.missing",
                    subject = subject,
                    message = "Planner candidate route must carry metrics and route evidence before scoring.",
                )
            }
            val evidence = route.evidence
            val snapshotId = candidate.snapshotId
            if (evidence != null && snapshotId != null && evidence.compilerSnapshotId != snapshotId.value) {
                violations += violation(
                    code = "planner.candidate.snapshot.missing",
                    subject = subject,
                    message = "Planner candidate route evidence must match candidate snapshot.",
                )
            }
        }
        return violations.sortedWith(compareBy({ it.code }, { it.subject }))
    }

    private fun rank(
        candidate: RoutePlannerCandidate,
        policy: RouteQualityPolicy,
    ): RankedRoutePlannerCandidate {
        val snapshotId = requireNotNull(candidate.snapshotId)
        val metrics = aggregateMetrics(candidate.routes.map { route -> requireNotNull(route.qualityMetrics) })
        return RankedRoutePlannerCandidate(
            candidateId = candidate.candidateId,
            plannerId = candidate.plannerId,
            snapshotId = snapshotId,
            score = qualityPolicyCompiler.score(metrics, policy),
            stableTieBreaker = stableTieBreaker(candidate),
            routes = candidate.routes.sortedBy { route -> requireNotNull(route.routeId).value },
            provenance = candidate.provenance,
        )
    }

    private fun aggregateMetrics(metrics: List<RouteQualityMetrics>): RouteQualityMetrics {
        return RouteQualityMetrics(
            crossingCount = metrics.sumOf { metric -> metric.crossingCount },
            bendCount = metrics.sumOf { metric -> metric.bendCount },
            length = metrics.sumOf { metric -> metric.length },
            channelChangeCount = metrics.sumOf { metric -> metric.channelChangeCount },
            bundleContinuityPenalty = metrics.sumOf { metric -> metric.bundleContinuityPenalty },
            labelClearanceViolationCount = metrics.sumOf { metric -> metric.labelClearanceViolationCount },
        )
    }

    private fun stableTieBreaker(candidate: RoutePlannerCandidate): String = listOf(
        candidate.candidateId.value,
        candidate.plannerId,
        candidate.routes.mapNotNull { route -> route.routeId?.value }.sorted().joinToString(separator = ","),
    ).joinToString(separator = "|")

    private fun violation(
        code: String,
        subject: String,
        message: String,
    ): RoutePlannerCandidateViolation = RoutePlannerCandidateViolation(code, subject, message)
}
