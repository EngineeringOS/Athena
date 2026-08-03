package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance

enum class JunctionCrossingMarkerKind {
    JUNCTION_DOT,
    DISCONNECTED_CROSSING,
    WIRE_HOP,
}

data class JunctionCrossingMarker(
    val markerId: String,
    val markerClassId: String,
    val kind: JunctionCrossingMarkerKind,
    val point: SchematicRoutePoint,
    val routeIds: List<String>,
    val semanticId: String?,
    val joined: Boolean,
    val selectedPolicyId: String,
    val compilerSnapshotId: String,
    val provenance: SourceProvenance?,
)

data class JunctionCrossingMarkerDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val affectedRouteIds: List<String>,
    val provenance: SourceProvenance?,
) {
    init {
        require(code.isNotBlank()) { "Junction/crossing marker diagnostic code must not be blank." }
        require(subject.isNotBlank()) { "Junction/crossing marker diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Junction/crossing marker diagnostic message must not be blank." }
    }
}

sealed interface JunctionCrossingMarkerCompilation {
    data class Success(val markers: List<JunctionCrossingMarker>) : JunctionCrossingMarkerCompilation
    data class Failure(val diagnostics: List<JunctionCrossingMarkerDiagnostic>) : JunctionCrossingMarkerCompilation
}

data class JunctionCrossingMarkerPayloadItem(
    val markerId: String,
    val markerClassId: String,
    val kind: String,
    val point: SchematicRoutePoint,
    val routeIds: List<String>,
    val semanticId: String?,
    val joined: Boolean,
    val selectedPolicyId: String,
    val compilerSnapshotId: String,
)

data class JunctionCrossingMarkerPayload(
    val authority: String,
    val markers: List<JunctionCrossingMarkerPayloadItem>,
    val rendererTopologyInferences: List<String> = emptyList(),
    val rawGeometryFragments: List<String> = emptyList(),
)

class JunctionCrossingMarkerCompiler {
    fun compile(
        snapshot: RouteFactSnapshot,
        profile: DrawingStandardProfile,
        selectedPolicyId: String,
    ): JunctionCrossingMarkerCompilation {
        require(selectedPolicyId.isNotBlank()) { "Selected marker policy id must not be blank." }
        val routeFactsById = snapshot.routeFacts.associateBy { route -> route.routeId }
        val diagnostics = mutableListOf<JunctionCrossingMarkerDiagnostic>()
        diagnostics += missingRoutes(snapshot, routeFactsById.keys)
        diagnostics += contradictoryTopology(snapshot)
        if (diagnostics.isNotEmpty()) {
            return JunctionCrossingMarkerCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject })),
            )
        }
        val markers = buildList {
            snapshot.junctionFacts.forEach { junction ->
                add(
                    JunctionCrossingMarker(
                        markerId = junction.junctionId,
                        markerClassId = markerClassForJunction(profile),
                        kind = JunctionCrossingMarkerKind.JUNCTION_DOT,
                        point = junction.point,
                        routeIds = junction.routeIds.map { routeId -> routeId.value },
                        semanticId = junction.semanticPortId,
                        joined = true,
                        selectedPolicyId = selectedPolicyId,
                        compilerSnapshotId = snapshot.snapshotId.value,
                        provenance = junction.routeIds.firstNotNullOfOrNull { routeId -> routeFactsById[routeId]?.provenance },
                    ),
                )
            }
            snapshot.crossingFacts.forEach { crossing ->
                add(
                    JunctionCrossingMarker(
                        markerId = crossing.crossingId,
                        markerClassId = markerClassForCrossing(profile),
                        kind = crossingKind(profile),
                        point = crossing.point,
                        routeIds = crossing.routeIds.map { routeId -> routeId.value },
                        semanticId = null,
                        joined = false,
                        selectedPolicyId = selectedPolicyId,
                        compilerSnapshotId = snapshot.snapshotId.value,
                        provenance = crossing.routeIds.firstNotNullOfOrNull { routeId -> routeFactsById[routeId]?.provenance },
                    ),
                )
            }
        }.sortedWith(compareBy({ it.point.y }, { it.point.x }, { it.markerId }))
        return JunctionCrossingMarkerCompilation.Success(markers)
    }

    fun normalize(compilation: JunctionCrossingMarkerCompilation.Success): JunctionCrossingMarkerPayload {
        return JunctionCrossingMarkerPayload(
            authority = "athena",
            markers = compilation.markers.map { marker ->
                JunctionCrossingMarkerPayloadItem(
                    markerId = marker.markerId,
                    markerClassId = marker.markerClassId,
                    kind = marker.kind.name,
                    point = marker.point,
                    routeIds = marker.routeIds,
                    semanticId = marker.semanticId,
                    joined = marker.joined,
                    selectedPolicyId = marker.selectedPolicyId,
                    compilerSnapshotId = marker.compilerSnapshotId,
                )
            },
        )
    }

    private fun missingRoutes(
        snapshot: RouteFactSnapshot,
        knownRouteIds: Set<SchematicRouteId>,
    ): List<JunctionCrossingMarkerDiagnostic> {
        return (snapshot.junctionFacts.flatMap { junction -> junction.routeIds.map { routeId -> junction.junctionId to routeId } } +
            snapshot.crossingFacts.flatMap { crossing -> crossing.routeIds.map { routeId -> crossing.crossingId to routeId } })
            .filter { (_, routeId) -> routeId !in knownRouteIds }
            .map { (subject, routeId) ->
                JunctionCrossingMarkerDiagnostic(
                    code = "drawing.marker.route.missing",
                    subject = subject,
                    message = "Marker topology references route '${routeId.value}' that is not in the route snapshot.",
                    affectedRouteIds = listOf(routeId.value),
                    provenance = null,
                )
            }
    }

    private fun contradictoryTopology(snapshot: RouteFactSnapshot): List<JunctionCrossingMarkerDiagnostic> {
        val junctionKeys = snapshot.junctionFacts.associateBy { junction -> topologyKey(junction.point, junction.routeIds) }
        return snapshot.crossingFacts.mapNotNull { crossing ->
            if (topologyKey(crossing.point, crossing.routeIds) in junctionKeys.keys) {
                JunctionCrossingMarkerDiagnostic(
                    code = "drawing.marker.topology.contradictory",
                    subject = crossing.crossingId,
                    message = "Same route pair and point cannot be both joined and disconnected.",
                    affectedRouteIds = crossing.routeIds.map { routeId -> routeId.value },
                    provenance = null,
                )
            } else {
                null
            }
        }
    }

    private fun topologyKey(
        point: SchematicRoutePoint,
        routeIds: List<SchematicRouteId>,
    ): String = "${point.x}:${point.y}:${routeIds.map { routeId -> routeId.value }.sorted().joinToString("|")}"

    private fun markerClassForJunction(profile: DrawingStandardProfile): String = when (profile.junctionRule) {
        DrawingCrossingBehavior.JUNCTION_REQUIRED -> "marker:junction-dot"
        DrawingCrossingBehavior.DISCONNECTED_CROSSING -> "marker:junction-dot"
        DrawingCrossingBehavior.WIRE_HOP -> "marker:junction-dot"
    }

    private fun markerClassForCrossing(profile: DrawingStandardProfile): String = when (profile.crossingRule) {
        DrawingCrossingBehavior.JUNCTION_REQUIRED -> "marker:disconnected-crossing"
        DrawingCrossingBehavior.DISCONNECTED_CROSSING -> "marker:disconnected-crossing"
        DrawingCrossingBehavior.WIRE_HOP -> "marker:wire-hop"
    }

    private fun crossingKind(profile: DrawingStandardProfile): JunctionCrossingMarkerKind = when (profile.crossingRule) {
        DrawingCrossingBehavior.WIRE_HOP -> JunctionCrossingMarkerKind.WIRE_HOP
        DrawingCrossingBehavior.JUNCTION_REQUIRED,
        DrawingCrossingBehavior.DISCONNECTED_CROSSING,
        -> JunctionCrossingMarkerKind.DISCONNECTED_CROSSING
    }
}
