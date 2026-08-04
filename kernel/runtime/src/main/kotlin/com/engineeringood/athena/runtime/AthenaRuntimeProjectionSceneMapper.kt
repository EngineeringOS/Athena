package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialSheet

internal fun ProjectionDocument.toViewerScene(
    systemName: String,
    document: EngineeringDocument,
    spatialDocument: SpatialDocument,
    activeSheetId: String?,
    placementOverrides: Map<String, AthenaGraphPlacement> = emptyMap(),
): AthenaRuntimeViewerScene {
    val sheet = spatialDocument.sheets.firstOrNull { candidate ->
        candidate.sheetId == activeSheetId
    } ?: spatialDocument.sheets.firstOrNull()
    if (sheet == null) {
        return AthenaRuntimeViewerScene(
            systemName = systemName,
            canvasWidth = 0,
            canvasHeight = 0,
            components = emptyList(),
            connections = emptyList(),
            labels = emptyList(),
        )
    }
    return sheet.toViewerScene(systemName)
}

private fun SpatialSheet.toViewerScene(systemName: String): AthenaRuntimeViewerScene {
    return AthenaRuntimeViewerScene(
        systemName = systemName,
        canvasWidth = extent.width,
        canvasHeight = extent.height,
        components = occurrences.map { occurrence ->
            AthenaRuntimeViewerComponentBox(
                projectionId = occurrence.occurrenceId.projectionId,
                semanticId = occurrence.subjectId.value,
                label = occurrence.subjectId.value.substringAfterLast(':'),
                x = occurrence.rectangle.x,
                y = occurrence.rectangle.y,
                width = occurrence.rectangle.width,
                height = occurrence.rectangle.height,
            )
        },
        connections = routes.map { route ->
            AthenaRuntimeViewerConnectionLine(
                projectionId = route.routeId.projectionConnectionId,
                semanticId = route.connectionId.value,
                x1 = route.points.first().x,
                y1 = route.points.first().y,
                x2 = route.points.last().x,
                y2 = route.points.last().y,
            )
        },
        labels = occurrences.map { occurrence ->
            AthenaRuntimeViewerLabel(
                projectionId = "label:${occurrence.occurrenceId.projectionId}",
                semanticId = occurrence.subjectId.value,
                label = occurrence.subjectId.value.substringAfterLast(':'),
                x = occurrence.rectangle.x,
                y = occurrence.rectangle.y,
                width = occurrence.rectangle.width,
                height = occurrence.rectangle.height,
            )
        },
    )
}
