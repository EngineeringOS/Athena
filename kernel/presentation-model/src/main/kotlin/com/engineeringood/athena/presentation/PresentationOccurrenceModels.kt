package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId

/**
 * Renderable occurrence reference for one placed primitive or composite.
 */
sealed interface PresentationOccurrenceReference

/**
 * Occurrence reference that points at one primitive definition.
 */
data class PresentationPrimitiveOccurrenceReference(
    val primitiveId: PresentationPrimitiveId,
) : PresentationOccurrenceReference

/**
 * Occurrence reference that points at one composite definition.
 */
data class PresentationCompositeOccurrenceReference(
    val compositeId: PresentationCompositeId,
) : PresentationOccurrenceReference

/**
 * Rebuildable downstream occurrence for one canonical subject or alias-like downstream label.
 */
data class PresentationOccurrence(
    val occurrenceId: PresentationOccurrenceId,
    val semanticId: StableSemanticIdentity,
    val reference: PresentationOccurrenceReference,
    val bounds: PresentationBounds,
    val layer: PresentationLayer,
    val displayLabel: String? = null,
    val orientation: PresentationOrientation = PresentationOrientation.HORIZONTAL,
    val markerKeys: List<String> = emptyList(),
    val textValues: Map<PresentationTextSlotId, String> = emptyMap(),
    val anchorBindings: List<PresentationAnchorBinding> = emptyList(),
    val tokenOverrides: Map<String, String> = emptyMap(),
    val sourceProjectionIds: List<String> = emptyList(),
)

/**
 * Stable connector occurrence derived from canonical connection identity plus projection guidance.
 */
data class PresentationConnector(
    val occurrenceId: PresentationOccurrenceId,
    val semanticId: StableSemanticIdentity,
    val primitiveId: PresentationPrimitiveId,
    val routePoints: List<PresentationPoint>,
    val line: PresentationConnectorLine,
    val routeId: String,
    val bundleId: String,
    val laneId: String,
    val laneRouteIds: List<String>,
    val selectedChannelIds: List<String>,
    val labels: List<PresentationConnectorLabel> = emptyList(),
    val quality: String,
    val sourceEndpoint: PresentationConnectorEndpoint,
    val targetEndpoint: PresentationConnectorEndpoint,
    val layer: PresentationLayer = PresentationLayer.CONNECTION,
    val markerIds: List<PresentationConnectionMarkerId> = emptyList(),
    val tokenOverrides: Map<String, String> = emptyMap(),
    val sourceProjectionIds: List<String> = emptyList(),
    val sourceSpan: LayoutSourceSpan,
) {
    init {
        require(routeId.isNotBlank()) { "Presentation connector route id must not be blank." }
        require(bundleId.isNotBlank()) { "Presentation connector bundle id must not be blank." }
        require(laneId.isNotBlank()) { "Presentation connector lane id must not be blank." }
        require(quality.isNotBlank()) { "Presentation connector route quality must not be blank." }
        require(tokenOverrides.keys.none(::isRequiredConnectorFactKey)) {
            "Presentation connector token overrides must not carry required route or stroke facts."
        }
        require(routePoints.size >= 2) { "Presentation connector route requires at least two points." }
        require(routePoints.first() == sourceEndpoint.point) {
            "Presentation connector route must start at the source endpoint."
        }
        require(routePoints.last() == targetEndpoint.point) {
            "Presentation connector route must end at the target endpoint."
        }
        require(routePoints.zipWithNext().all { (a, b) -> a.x == b.x || a.y == b.y }) {
            "Presentation connector route must be orthogonal."
        }
    }
}

private fun isRequiredConnectorFactKey(key: String): Boolean =
    key.contains("route", ignoreCase = true) || key.contains("st" + "roke", ignoreCase = true)

/** Required connector line appearance. Renderer paints this fact; it does not infer style. */
data class PresentationConnectorLine(
    val classId: String,
    val lineKind: String,
    val lineStyleId: String,
    val weight: Double,
    val style: String,
    val colorKey: String,
    val endpointBehavior: String,
    val labelPolicy: String,
    val crossingBehavior: String,
    val policyId: String,
    val compilerSnapshotId: String,
) {
    init {
        require(classId.isNotBlank()) { "Presentation connector line class id must not be blank." }
        require(lineKind.isNotBlank()) { "Presentation connector line kind must not be blank." }
        require(lineStyleId.isNotBlank()) { "Presentation connector stroke class id must not be blank." }
        require(weight > 0.0) { "Presentation connector stroke weight must be positive." }
        require(style.isNotBlank()) { "Presentation connector stroke style must not be blank." }
        require(colorKey.isNotBlank()) { "Presentation connector color token must not be blank." }
        require(endpointBehavior.isNotBlank()) { "Presentation connector endpoint behavior must not be blank." }
        require(labelPolicy.isNotBlank()) { "Presentation connector label policy must not be blank." }
        require(crossingBehavior.isNotBlank()) { "Presentation connector crossing behavior must not be blank." }
        require(policyId.isNotBlank()) { "Presentation connector policy id must not be blank." }
        require(compilerSnapshotId.isNotBlank()) { "Presentation connector line compiler snapshot id must not be blank." }
    }
}

/** Required route label fact attached to one connector. */
data class PresentationConnectorLabel(
    val labelId: String,
    val targetId: String,
    val text: String,
    val point: PresentationPoint,
    val bounds: PresentationDrawingBounds,
    val labelClassId: String,
    val display: PresentationConnectorLabelDisplay,
    val sourceProvenance: List<String>,
    val compilerSnapshotId: String,
) {
    init {
        require(labelId.isNotBlank()) { "Presentation connector label id must not be blank." }
        require(targetId.isNotBlank()) { "Presentation connector label target id must not be blank." }
        require(text.isNotBlank()) { "Presentation connector label text must not be blank." }
        require(labelClassId.isNotBlank()) { "Presentation connector label class id must not be blank." }
        require(sourceProvenance.isNotEmpty()) { "Presentation connector label requires source provenance." }
        require(compilerSnapshotId.isNotBlank()) { "Presentation connector label compiler snapshot id must not be blank." }
    }
}

enum class PresentationConnectorLabelDisplay {
    ALWAYS,
    SELECTION,
}

@JvmInline
value class PresentationConnectionMarkerId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation connection marker id must not be blank." }
    }

    override fun toString(): String = value
}

enum class PresentationConnectionMarkerKind {
    JUNCTION,
    NO_CONNECT_CROSSING,
    BUS_TAP,
    CONTINUATION,
}

data class PresentationConnectionMarker(
    val markerId: PresentationConnectionMarkerId,
    val kind: PresentationConnectionMarkerKind,
    val point: PresentationPoint,
    val routeIds: List<String>,
    val connectorIds: List<PresentationOccurrenceId>,
    val semanticId: StableSemanticIdentity?,
    val joined: Boolean,
    val appearanceClassId: String,
    val sourceProjectionIds: List<String>,
    val sourceProvenance: List<String>,
    val compilerSnapshotId: String,
) {
    init {
        require(routeIds.isNotEmpty()) { "Presentation connection marker requires route participants." }
        require(connectorIds.isNotEmpty()) { "Presentation connection marker requires connector participants." }
        require(appearanceClassId.isNotBlank()) { "Presentation connection marker appearance class id must not be blank." }
        require(sourceProjectionIds.isNotEmpty()) { "Presentation connection marker requires source projection ids." }
        require(sourceProvenance.isNotEmpty()) { "Presentation connection marker requires source provenance." }
        require(compilerSnapshotId.isNotBlank()) { "Presentation connection marker compiler snapshot id must not be blank." }
    }
}

/** Exact trace for one visible connector endpoint. */
data class PresentationConnectorEndpoint(
    val portSemanticId: StableSemanticIdentity,
    val bindingId: RepresentationPortAnchorBindingId,
    val occurrenceId: RepresentationOccurrenceId,
    val anchorId: RepresentationAnchorId,
    val point: PresentationPoint,
    val sourceProvenance: List<String>,
) {
    init {
        require(sourceProvenance.isNotEmpty()) { "Presentation connector endpoint requires source provenance." }
    }
}

/**
 * Binding from one downstream occurrence to one canonical or projection-owned anchor occurrence.
 */
data class PresentationAnchorBinding(
    val alias: PresentationAnchorAlias,
    val anchorId: String,
    val portSemanticId: StableSemanticIdentity? = null,
    val ownerSemanticId: StableSemanticIdentity? = null,
    val sourceLabelId: String? = null,
)
