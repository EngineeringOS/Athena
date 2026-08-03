package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.projection.ProjectionDocument

internal fun ProjectionDocument.toViewerScene(
    systemName: String,
    document: EngineeringDocument,
    placementOverrides: Map<String, AthenaGraphPlacement> = emptyMap(),
): AthenaRuntimeViewerScene {
    return AthenaRuntimeViewerScene(
        systemName = systemName,
        canvasWidth = EMPTY_VIEWER_CANVAS_WIDTH,
        canvasHeight = EMPTY_VIEWER_CANVAS_HEIGHT,
        components = emptyList(),
        connections = emptyList(),
        labels = emptyList(),
    )
}

private const val EMPTY_VIEWER_CANVAS_WIDTH = 1
private const val EMPTY_VIEWER_CANVAS_HEIGHT = 1
