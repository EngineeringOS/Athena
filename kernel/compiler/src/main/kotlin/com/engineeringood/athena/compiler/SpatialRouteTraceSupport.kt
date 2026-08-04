package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal fun canonicalRouteSourceTrace(
    sheetId: String,
    connection: ProjectionConnection,
    source: SpatialAnchorPosition,
    target: SpatialAnchorPosition,
): SpatialSourceTrace {
    val requiredProjectionIds = listOf(
        sheetId,
        connection.projectionId.value,
        source.subject.occurrenceId.projectionId,
        source.subject.portId.value,
        target.subject.occurrenceId.projectionId,
        target.subject.portId.value,
    )
    val requiredSet = requiredProjectionIds.toSet()
    val inheritedProjectionIds = (source.sourceTrace.projectionIds + target.sourceTrace.projectionIds)
        .filterNot(requiredSet::contains)
        .distinct()
        .sorted()
    val geometryElementIds = (
        listOf(connection.originGeometryElementId) +
            source.sourceTrace.geometryElementIds +
            target.sourceTrace.geometryElementIds
        ).distinctBy { geometryId -> geometryId.value }
        .sortedBy { geometryId -> geometryId.value }
    return SpatialSourceTrace(
        projectionIds = requiredProjectionIds + inheritedProjectionIds,
        geometryElementIds = geometryElementIds,
    )
}
