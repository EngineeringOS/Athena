package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationConnectionMarkerId
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationConnectorLabelDisplay
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.spatial.SpatialRoute

data class ConnectionPaint(
    val line: PresentationConnectorLine,
    val labels: List<PresentationConnectorLabel>,
    val markers: List<PresentationConnectionMarker> = emptyList(),
) {
    val markerIds: List<PresentationConnectionMarkerId> = markers.map { marker -> marker.markerId }
}

data class ConnectionPaintOverride(
    val style: String? = null,
    val label: String? = null,
    val position: String? = null,
) {
    init {
        require(style == null || style.isNotBlank()) { "Connection paint style must not be blank." }
        require(label == null || label.isNotBlank()) { "Connection paint label must not be blank." }
        require(position == null || position.isNotBlank()) { "Connection paint label position must not be blank." }
    }
}

class ConnectionPaintCompiler(
    private val overrides: Map<String, ConnectionPaintOverride> = emptyMap(),
) {
    fun compile(
        route: SpatialRoute,
        routePoints: List<PresentationPoint>,
        connectorId: PresentationOccurrenceId,
    ): ConnectionPaint {
        require(routePoints.size >= 2) { "Connection paint requires at least two route points." }
        val override = overrides[route.routeId.value]
        val style = override?.style ?: "solid"
        val lineStyleId = override?.style?.let { overrideStyle -> "line:$overrideStyle" } ?: "line:connection"
        val labelText = override?.label ?: route.connectionId.value.substringAfterLast(':')
        val labelPoint = labelPoint(routePoints, override?.position)
        return ConnectionPaint(
            line = PresentationConnectorLine(
                classId = "spatial-route",
                lineKind = "CONNECTION",
                lineStyleId = lineStyleId,
                weight = 1.5,
                style = style,
                colorKey = "connection",
                endpointBehavior = "attached",
                labelPolicy = "route-label",
                crossingBehavior = "recorded-by-spatial",
                policyId = "presentation:default",
                compilerSnapshotId = "connection-paint",
            ),
            labels = listOf(
                PresentationConnectorLabel(
                    labelId = "label:${route.routeId.value}",
                    targetId = connectorId.value,
                    text = labelText,
                    point = labelPoint,
                    bounds = PresentationDrawingBounds(
                        x = labelPoint.x,
                        y = (labelPoint.y - 12).coerceAtLeast(0),
                        width = (labelText.length * 7).coerceAtLeast(28),
                        height = 14,
                    ),
                    labelClassId = "label:route",
                    display = PresentationConnectorLabelDisplay.ALWAYS,
                    sourceProvenance = listOf(route.routeId.value),
                    compilerSnapshotId = "connection-paint",
                ),
            ),
            markers = emptyList(),
        )
    }

    private fun labelPoint(points: List<PresentationPoint>, position: String?): PresentationPoint =
        when (position?.lowercase()) {
            "right top" -> {
                val right = points.maxOf { point -> point.x }
                val top = points.minOf { point -> point.y }
                PresentationPoint(x = right + 8, y = (top - 12).coerceAtLeast(0))
            }
            "right" -> {
                val right = points.maxOf { point -> point.x }
                val center = points[points.size / 2]
                PresentationPoint(x = right + 8, y = center.y)
            }
            "bottom" -> {
                val bottom = points.maxOf { point -> point.y }
                val center = points[points.size / 2]
                PresentationPoint(x = center.x, y = bottom + 12)
            }
            null -> points[points.size / 2]
            else -> error("Unsupported connection paint label position: $position")
        }
}
