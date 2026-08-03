package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance

data class DrawingBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0) { "Drawing bounds must use positive size." }
    }

    fun intersects(other: DrawingBounds): Boolean {
        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
    }

    fun contains(other: DrawingBounds): Boolean {
        return other.x >= x &&
            other.y >= y &&
            other.x + other.width <= x + width &&
            other.y + other.height <= y + height
    }

    fun intersects(segment: SchematicRouteSegment): Boolean {
        return when (segment.orientation) {
            SchematicRouteSegmentOrientation.HORIZONTAL -> {
                val minX = minOf(segment.start.x, segment.end.x)
                val maxX = maxOf(segment.start.x, segment.end.x)
                segment.start.y in y until (y + height) && maxX > x && minX < x + width
            }
            SchematicRouteSegmentOrientation.VERTICAL -> {
                val minY = minOf(segment.start.y, segment.end.y)
                val maxY = maxOf(segment.start.y, segment.end.y)
                segment.start.x in x until (x + width) && maxY > y && minY < y + height
            }
        }
    }
}

data class RouteLabelPlacementRequest(
    val snapshot: RouteFactSnapshot,
    val frameBounds: DrawingBounds? = null,
    val componentBounds: List<DrawingBounds> = emptyList(),
    val routeSegments: List<SchematicRouteSegment> = emptyList(),
    val gridLabelBounds: List<DrawingBounds> = emptyList(),
    val titleBlockBounds: List<DrawingBounds> = emptyList(),
    val textBounds: List<DrawingBounds> = emptyList(),
)

data class RouteLabelCollision(
    val code: String,
    val subject: String,
) {
    init {
        require(code.isNotBlank()) { "Route label collision code must not be blank." }
        require(subject.isNotBlank()) { "Route label collision subject must not be blank." }
    }
}

data class RouteLabelPlacementEvidence(
    val labelId: SchematicLabelId,
    val text: String,
    val bounds: DrawingBounds,
    val attachmentPoint: SchematicRoutePoint,
    val labelClassId: String,
    val collisions: List<RouteLabelCollision>,
    val provenance: SourceProvenance,
    val compilerSnapshotId: String,
)

data class RouteLabelPlacementDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val provenance: SourceProvenance?,
) {
    init {
        require(code.isNotBlank()) { "Route label placement diagnostic code must not be blank." }
        require(subject.isNotBlank()) { "Route label placement diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Route label placement diagnostic message must not be blank." }
    }
}

data class RouteLabelPlacementResult(
    val labels: List<RouteLabelPlacementEvidence>,
    val diagnostics: List<RouteLabelPlacementDiagnostic>,
) {
    val successful: Boolean
        get() = diagnostics.isEmpty()
}

data class RouteLabelPlacementPayloadItem(
    val labelId: String,
    val text: String,
    val bounds: DrawingBounds,
    val attachmentPoint: SchematicRoutePoint,
    val labelClassId: String,
    val collisionCodes: List<String>,
    val compilerSnapshotId: String,
)

data class RouteLabelPlacementPayload(
    val authority: String,
    val labels: List<RouteLabelPlacementPayloadItem>,
    val rendererPlacements: List<String> = emptyList(),
    val rawMarkupFragments: List<String> = emptyList(),
)

class RouteLabelPlacementCompiler {
    fun compile(request: RouteLabelPlacementRequest): RouteLabelPlacementResult {
        val routeById = request.snapshot.routeFacts.associateBy { route -> route.routeId }
        val labelInputs = request.snapshot.routeFacts.flatMap { route -> route.labels.map { label -> route to label } }
        val preliminary = labelInputs.mapNotNull { (route, label) ->
            if (label.anchorRouteId !in routeById.keys) {
                null
            } else {
                route to label.toEvidence(route)
            }
        }
        val diagnostics = mutableListOf<RouteLabelPlacementDiagnostic>()
        labelInputs.filter { (_, label) -> label.anchorRouteId !in routeById.keys }.forEach { (route, label) ->
            diagnostics += diagnostic(
                code = "drawing.label.unresolved",
                subject = label.labelId.value,
                message = "Route label references missing route '${label.anchorRouteId.value}'.",
                provenance = route.provenance,
            )
        }
        val labels = resolveLabelCollisions(preliminary.map { it.second }, request)
        labels.forEach { label ->
            label.collisions.forEach { collision ->
                diagnostics += diagnostic(
                    code = collision.code,
                    subject = label.labelId.value,
                    message = "Route label '${label.labelId.value}' collides with ${collision.subject}.",
                    provenance = label.provenance,
                )
            }
        }
        return RouteLabelPlacementResult(
            labels = labels,
            diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject })),
        )
    }

    private fun resolveLabelCollisions(
        labels: List<RouteLabelPlacementEvidence>,
        request: RouteLabelPlacementRequest,
    ): List<RouteLabelPlacementEvidence> {
        val placed = mutableListOf<RouteLabelPlacementEvidence>()
        labels.sortedBy { label -> label.labelId.value }.forEach { label ->
            val candidate = label.candidates().firstOrNull { candidate ->
                collisionsFor(candidate, placed, request).isEmpty()
            } ?: label
            placed += candidate
        }
        return placed.map { label ->
            label.copy(collisions = collisionsFor(label, placed, request))
        }.sortedBy { label -> label.labelId.value }
    }

    private fun RouteLabelPlacementEvidence.candidates(): List<RouteLabelPlacementEvidence> {
        val offsets = buildList {
            add(0 to 0)
            listOf(14, 28, 42, 56, 84, 112, 140, 168, 224).forEach { distance ->
                add(0 to -distance)
                add(0 to distance)
                add(distance to 0)
                add(-distance to 0)
                add(distance to -distance)
                add(distance to distance)
                add(-distance to -distance)
                add(-distance to distance)
            }
        }
        return offsets.mapNotNull { (dx, dy) ->
            val shiftedX = attachmentPoint.x + dx
            val shiftedY = attachmentPoint.y + dy
            if (shiftedX < 0 || shiftedY < 0) {
                return@mapNotNull null
            }
            val shiftedPoint = SchematicRoutePoint(
                x = shiftedX,
                y = shiftedY,
            )
            val shifted = visualBounds(shiftedPoint, text)
            if (shifted.x >= 0 && shifted.y >= 0) {
                copy(
                    bounds = shifted,
                    attachmentPoint = shiftedPoint,
                    collisions = emptyList(),
                )
            } else {
                null
            }
        }
    }

    fun normalize(result: RouteLabelPlacementResult): RouteLabelPlacementPayload {
        return RouteLabelPlacementPayload(
            authority = "athena",
            labels = result.labels.map { label ->
                RouteLabelPlacementPayloadItem(
                    labelId = label.labelId.value,
                    text = label.text,
                    bounds = label.bounds,
                    attachmentPoint = label.attachmentPoint,
                    labelClassId = label.labelClassId,
                    collisionCodes = label.collisions.map { collision -> collision.code },
                    compilerSnapshotId = label.compilerSnapshotId,
                )
            },
        )
    }

    private fun RouteLabelFact.toEvidence(route: RouteFact): RouteLabelPlacementEvidence {
        return RouteLabelPlacementEvidence(
            labelId = labelId,
            text = text,
            bounds = visualBounds(placement.origin, text),
            attachmentPoint = placement.origin,
            labelClassId = "label:route",
            collisions = emptyList(),
            provenance = route.provenance,
            compilerSnapshotId = route.compilerSnapshotId,
        )
    }

    private fun collisionsFor(
        label: RouteLabelPlacementEvidence,
        allLabels: List<RouteLabelPlacementEvidence>,
        request: RouteLabelPlacementRequest,
    ): List<RouteLabelCollision> {
        val collisions = mutableListOf<RouteLabelCollision>()
        if (request.componentBounds.any { bounds -> label.bounds.intersects(bounds) }) {
            collisions += RouteLabelCollision("drawing.label.collision.component", "component")
        }
        if (request.routeSegments.any { segment -> label.bounds.intersects(segment) }) {
            collisions += RouteLabelCollision("drawing.label.collision.route", "route")
        }
        if (allLabels.any { other -> other.labelId != label.labelId && other.bounds.intersects(label.bounds) }) {
            collisions += RouteLabelCollision("drawing.label.collision.label", "label")
        }
        val frame = request.frameBounds
        if (frame != null && !frame.contains(label.bounds)) {
            collisions += RouteLabelCollision("drawing.label.collision.frame", "frame")
        }
        if (request.gridLabelBounds.any { bounds -> label.bounds.intersects(bounds) }) {
            collisions += RouteLabelCollision("drawing.label.collision.grid", "grid")
        }
        if (request.titleBlockBounds.any { bounds -> label.bounds.intersects(bounds) }) {
            collisions += RouteLabelCollision("drawing.label.collision.title-block", "title-block")
        }
        if (request.textBounds.any { bounds -> label.bounds.intersects(bounds) }) {
            collisions += RouteLabelCollision("drawing.label.collision.text", "text")
        }
        return collisions.sortedWith(compareBy({ it.code }, { it.subject }))
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
        provenance: SourceProvenance?,
    ): RouteLabelPlacementDiagnostic = RouteLabelPlacementDiagnostic(code, subject, message, provenance)

    private fun visualBounds(point: SchematicRoutePoint, text: String): DrawingBounds {
        val width = text.length * 6
        val height = 10
        return DrawingBounds(
            x = point.x - (width / 2),
            y = point.y - (height / 2),
            width = width,
            height = height,
        )
    }
}
