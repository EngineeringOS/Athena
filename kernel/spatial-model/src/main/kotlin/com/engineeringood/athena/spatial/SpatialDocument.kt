package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.RealityDeclaration
import com.engineeringood.athena.ir.RealityIdentityRule
import com.engineeringood.athena.ir.RealityValidationIssue
import com.engineeringood.athena.ir.RealityValidationResult
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Concrete root for Spatial Reality.
 *
 * Spatial Reality turns projected engineering content into geometry. Routing is a subsystem of this
 * reality, so lanes and routes live here with placement, bounds, anchors, and alignment.
 */
data class SpatialDocument(
    val occurrences: List<SpatialOccurrenceGeometry> = emptyList(),
    val regions: List<SpatialRegionGeometry> = emptyList(),
    val constructs: List<SpatialConstructGeometry> = emptyList(),
    val anchorPositions: List<SpatialAnchorPosition> = emptyList(),
    val alignments: List<SpatialAlignment> = emptyList(),
    val lanes: List<SpatialLane> = emptyList(),
    val routes: List<SpatialRoute> = emptyList(),
    val qualityMeasurements: List<SpatialQualityMeasurement> = emptyList(),
    val gridReferences: Map<String, String> = emptyMap(),
)

object SpatialReality {
    const val name: String = "Spatial Reality"
    const val rootName: String = "SpatialDocument"
    const val purpose: String = "Turns view-specific projection facts into geometry."
    const val authority: String = "spatial compiler"

    val ownedFacts: List<String> = listOf(
        "occurrence geometry",
        "Region geometry",
        "Construct geometry",
        "anchor position",
        "alignment",
        "lane",
        "route",
        "quality measurement",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("occurrence geometry", "Occurrence identity names its owning Sheet and Projection occurrence."),
        RealityIdentityRule("Region geometry", "Region identity names its owning Sheet and Projection Region."),
        RealityIdentityRule("Construct geometry", "Construct identity names its owning Sheet and Projection Construct."),
        RealityIdentityRule("anchor position", "Anchor position identity traces to projection occurrence plus anchor id."),
        RealityIdentityRule("lane", "Lane identity is spatial-local and owned by the spatial compiler."),
        RealityIdentityRule("route", "Route identity traces to the projection connection identity plus route id."),
        RealityIdentityRule("quality measurement", "Quality measurement identity traces to the spatial document and measured fact kind."),
    )

    val requiredFacts: List<String> = listOf(
        "occurrence geometry identity",
        "Region geometry identity",
        "anchor position identity",
        "lane identity",
        "route identity",
    )

    val declaration: RealityDeclaration = RealityDeclaration(
        name = name,
        rootName = rootName,
        purpose = purpose,
        authority = authority,
        ownedFacts = ownedFacts,
        identityRules = identityRules,
        requiredFacts = requiredFacts,
    )

    fun validate(document: SpatialDocument): RealityValidationResult {
        val laneIds = document.lanes.map { lane -> lane.laneId }.toSet()
        val anchorIds = document.anchorPositions.map { anchor -> anchor.anchorId }.toSet()
        val occurrenceIds = document.occurrences.map { occurrence -> occurrence.occurrenceId }.toSet()
        val projectionOccurrenceIds = occurrenceIds.map { occurrenceId -> occurrenceId.projectionId }.toSet()
        val issues = buildList {
            if (document.occurrences.isEmpty()) {
                add(RealityValidationIssue(name, "missing occurrence geometry facts"))
            }
            if (document.regions.isEmpty()) {
                add(RealityValidationIssue(name, "missing Region geometry facts"))
            }
            if (document.anchorPositions.isEmpty()) {
                add(RealityValidationIssue(name, "missing anchor position facts"))
            }
            if (document.occurrences.any { occurrence -> occurrence.subjectId.value.isBlank() }) {
                add(RealityValidationIssue(name, "missing occurrence geometry identity"))
            }
            if (document.routes.any { route -> route.connectionId.value.isBlank() }) {
                add(RealityValidationIssue(name, "missing route identity"))
            }
            if (document.routes.any { route -> route.laneId !in laneIds }) {
                add(RealityValidationIssue(name, "missing lane identity"))
            }
            if (document.routes.any { route ->
                    route.sourceAnchorId !in anchorIds || route.targetAnchorId !in anchorIds
                }
            ) {
                add(RealityValidationIssue(name, "route without anchor position facts"))
            }
            if (document.anchorPositions.any { anchor -> anchor.occurrenceId !in projectionOccurrenceIds }) {
                add(RealityValidationIssue(name, "anchor position without occurrence geometry"))
            }
            if (document.regions.any { region -> region.memberOccurrenceIds.any { member -> member !in occurrenceIds } }) {
                add(RealityValidationIssue(name, "Region without occurrence geometry"))
            }
            if (document.constructs.any { construct ->
                    construct.memberOccurrenceIds.any { member -> member !in occurrenceIds }
                }
            ) {
                add(RealityValidationIssue(name, "Construct without occurrence geometry"))
            }
            if (document.alignments.any { alignment -> alignment.occurrenceIds.any { member -> member !in occurrenceIds } }) {
                add(RealityValidationIssue(name, "alignment without occurrence geometry"))
            }
        }
        return RealityValidationResult(issues)
    }
}

data class SpatialAnchorPosition(
    val anchorId: String,
    val occurrenceId: String,
    val x: Double,
    val y: Double,
) {
    init {
        require(anchorId.isNotBlank()) { "Spatial anchor id must not be blank." }
        require(occurrenceId.isNotBlank()) { "Spatial anchor occurrence id must not be blank." }
    }
}

data class SpatialLane(
    val laneId: String,
    val direction: String,
) {
    init {
        require(laneId.isNotBlank()) { "Spatial lane id must not be blank." }
        require(direction.isNotBlank()) { "Spatial lane direction must not be blank." }
    }
}

data class SpatialPoint(
    val x: Double,
    val y: Double,
)

data class SpatialRoute(
    val routeId: String,
    val connectionId: StableSemanticIdentity,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val sourcePortId: String? = null,
    val targetPortId: String? = null,
    val laneId: String,
    val points: List<SpatialPoint>,
) {
    init {
        require(routeId.isNotBlank()) { "Spatial route id must not be blank." }
        require(sourceOccurrenceId.isNotBlank()) { "Spatial route source occurrence id must not be blank." }
        require(targetOccurrenceId.isNotBlank()) { "Spatial route target occurrence id must not be blank." }
        require(sourceAnchorId.isNotBlank()) { "Spatial route source anchor id must not be blank." }
        require(targetAnchorId.isNotBlank()) { "Spatial route target anchor id must not be blank." }
        require(laneId.isNotBlank()) { "Spatial route lane id must not be blank." }
        require(points.size >= 2) { "Spatial route must contain at least two points." }
    }
}

data class SpatialQualityMeasurement(
    val kind: String,
    val value: Double,
) {
    init {
        require(kind.isNotBlank()) { "Spatial quality measurement kind must not be blank." }
        require(value >= 0.0) { "Spatial quality measurement value must not be negative." }
    }
}
