package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance

enum class RouteQualityEvidenceSeverity {
    INFO,
    WARNING,
    BLOCKING,
}

data class RouteQualityEvidenceDiagnostic(
    val code: String,
    val severity: RouteQualityEvidenceSeverity,
    val subject: String,
    val message: String,
    val plannerId: String?,
    val snapshotId: String?,
    val affectedRouteIds: List<String>,
    val violatedFacts: List<String>,
    val provenance: SourceProvenance?,
) {
    init {
        require(code.isNotBlank()) { "Route quality evidence diagnostic code must not be blank." }
        require(subject.isNotBlank()) { "Route quality evidence diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Route quality evidence diagnostic message must not be blank." }
        require(affectedRouteIds.distinct().size == affectedRouteIds.size) {
            "Route quality evidence diagnostics must not duplicate affected route ids."
        }
    }
}

data class RouteQualityEvidence(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val grade: RouteQualityState,
    val scoreComponents: List<RouteQualityScoreComponent>,
    val degradedReasons: List<String>,
    val laneAssignment: RouteLaneAssignment,
    val selectedChannelIds: List<String>,
    val plannerId: String,
    val compilerSnapshotId: String,
    val provenance: SourceProvenance,
)

data class RouteQualityEvidenceCompilation(
    val routes: List<RouteQualityEvidence>,
    val diagnostics: List<RouteQualityEvidenceDiagnostic>,
) {
    val successful: Boolean
        get() = diagnostics.none { diagnostic -> diagnostic.severity == RouteQualityEvidenceSeverity.BLOCKING }
}

data class RouteQualityEvidenceRoutePayload(
    val routeId: String,
    val connectionId: String,
    val grade: String,
    val scoreComponents: List<RouteQualityScoreComponent>,
    val degradedReasons: List<String>,
    val laneId: String,
    val selectedChannelIds: List<String>,
    val plannerId: String,
    val compilerSnapshotId: String,
    val sourceFile: String,
)

data class RouteQualityEvidencePayload(
    val authority: String,
    val routes: List<RouteQualityEvidenceRoutePayload>,
    val diagnostics: List<RouteQualityEvidenceDiagnostic>,
    val rejectedPlannerObjects: List<String> = emptyList(),
    val rawMarkupFragments: List<String> = emptyList(),
)

class RouteQualityEvidenceCompiler(
    private val qualityPolicyCompiler: RouteQualityPolicyCompiler = RouteQualityPolicyCompiler(),
) {
    fun compile(
        snapshot: RouteFactSnapshot,
        policy: RouteQualityPolicy,
    ): RouteQualityEvidenceCompilation {
        val routes = snapshot.routeFacts.map { route -> route.toEvidence(policy) }
        val diagnostics = buildList {
            snapshot.routeFacts.forEach { route ->
                addAll(route.diagnostics(snapshot))
            }
            snapshot.laneDiagnostics.forEach { diagnostic ->
                add(
                    RouteQualityEvidenceDiagnostic(
                        code = "route.quality.evidence.lane.blocking",
                        severity = RouteQualityEvidenceSeverity.BLOCKING,
                        subject = diagnostic.code,
                        message = diagnostic.message,
                        plannerId = null,
                        snapshotId = snapshot.snapshotId.value,
                        affectedRouteIds = diagnostic.affectedRouteIds.map { routeId -> routeId.value },
                        violatedFacts = listOf(diagnostic.code),
                        provenance = null,
                    ),
                )
            }
        }.sortedWith(compareBy({ it.code }, { it.subject }))
        return RouteQualityEvidenceCompilation(
            routes = routes.sortedBy { route -> route.routeId.value },
            diagnostics = diagnostics,
        )
    }

    fun diagnosticsFor(comparison: RoutePlannerCandidateComparison): List<RouteQualityEvidenceDiagnostic> {
        return comparison.rejections.map { rejection ->
            RouteQualityEvidenceDiagnostic(
                code = "route.quality.evidence.candidate.rejected",
                severity = RouteQualityEvidenceSeverity.BLOCKING,
                subject = rejection.candidateId.value,
                message = "Planner candidate was rejected before route quality scoring.",
                plannerId = rejection.plannerId,
                snapshotId = rejection.snapshotId?.value,
                affectedRouteIds = rejection.affectedRouteIds.map { routeId -> routeId.value },
                violatedFacts = rejection.violations.map { violation -> violation.code }.distinct().sorted(),
                provenance = rejection.provenance,
            )
        }.sortedWith(compareBy({ it.subject }, { it.plannerId.orEmpty() }))
    }

    fun normalize(compilation: RouteQualityEvidenceCompilation): RouteQualityEvidencePayload {
        return RouteQualityEvidencePayload(
            authority = "athena",
            routes = compilation.routes.map { evidence ->
                RouteQualityEvidenceRoutePayload(
                    routeId = evidence.routeId.value,
                    connectionId = evidence.connectionId.value,
                    grade = evidence.grade.name,
                    scoreComponents = evidence.scoreComponents,
                    degradedReasons = evidence.degradedReasons,
                    laneId = evidence.laneAssignment.laneId.value,
                    selectedChannelIds = evidence.selectedChannelIds,
                    plannerId = evidence.plannerId,
                    compilerSnapshotId = evidence.compilerSnapshotId,
                    sourceFile = evidence.provenance.file,
                )
            },
            diagnostics = compilation.diagnostics,
        )
    }

    private fun RouteFact.toEvidence(policy: RouteQualityPolicy): RouteQualityEvidence {
        val score = qualityPolicyCompiler.score(qualityMetrics, policy)
        return RouteQualityEvidence(
            routeId = routeId,
            connectionId = connectionId,
            grade = quality.state,
            scoreComponents = score.components,
            degradedReasons = quality.failedConstraintIds.map { constraintId -> constraintId.value },
            laneAssignment = laneAssignment,
            selectedChannelIds = selectedChannelIds,
            plannerId = plannerId,
            compilerSnapshotId = compilerSnapshotId,
            provenance = provenance,
        )
    }

    private fun RouteFact.diagnostics(snapshot: RouteFactSnapshot): List<RouteQualityEvidenceDiagnostic> {
        val diagnostics = mutableListOf<RouteQualityEvidenceDiagnostic>()
        if (!quality.isSatisfied) {
            diagnostics += diagnostic(
                code = "route.quality.evidence.hard-reject",
                message = quality.message ?: "Route quality hard reject or degradation is unresolved.",
                violatedFacts = quality.failedConstraintIds.map { constraintId -> constraintId.value },
                snapshot = snapshot,
            )
        }
        if (compilerSnapshotId != snapshot.snapshotId.value) {
            diagnostics += diagnostic(
                code = "route.quality.evidence.snapshot.missing",
                message = "Route compiler snapshot evidence does not match the accepted route snapshot.",
                violatedFacts = listOf("compilerSnapshotId"),
                snapshot = snapshot,
            )
        }
        if (routeId !in laneAssignment.occupancy.routeIds || laneAssignment.lane != lane) {
            diagnostics += diagnostic(
                code = "route.quality.evidence.proof.missing",
                message = "Route lane evidence does not match the accepted route fact.",
                violatedFacts = listOf("laneAssignment"),
                snapshot = snapshot,
            )
        }
        return diagnostics
    }

    private fun RouteFact.diagnostic(
        code: String,
        message: String,
        violatedFacts: List<String>,
        snapshot: RouteFactSnapshot,
    ): RouteQualityEvidenceDiagnostic = RouteQualityEvidenceDiagnostic(
        code = code,
        severity = RouteQualityEvidenceSeverity.BLOCKING,
        subject = routeId.value,
        message = message,
        plannerId = plannerId,
        snapshotId = snapshot.snapshotId.value,
        affectedRouteIds = listOf(routeId.value),
        violatedFacts = violatedFacts,
        provenance = provenance,
    )
}
