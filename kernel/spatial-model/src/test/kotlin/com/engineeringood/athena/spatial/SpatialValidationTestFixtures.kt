package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity

internal object SpatialValidationTestFixtures {
    fun validDocument(): SpatialDocument = SpatialDocument(listOf(validSheet()))

    fun SpatialSheet.withObstacle(obstacle: SpatialOccurrenceGeometry): SpatialSheet {
        val orderedOccurrences = listOf(occurrences.first(), obstacle, occurrences.last())
        val orderedIds = orderedOccurrences.map(SpatialOccurrenceGeometry::occurrenceId)
        val region = regions.single().let { original ->
            SpatialRegionGeometry(
                original.regionId,
                original.sheetId,
                orderedIds,
                original.bounds,
                trace(
                    sheetId,
                    original.regionId.projectionId,
                    *orderedIds.map(SpatialOccurrenceId::projectionId).toTypedArray(),
                ),
            )
        }
        val construct = constructs.single().let { original ->
            SpatialConstructGeometry(
                original.constructId,
                original.sheetId,
                original.kind,
                original.name,
                orderedIds,
                original.envelope,
                trace(
                    sheetId,
                    original.constructId.projectionId,
                    *orderedIds.map(SpatialOccurrenceId::projectionId).toTypedArray(),
                ),
            )
        }
        val updated = copy(
            occurrences = orderedOccurrences,
            regions = listOf(region),
            constructs = listOf(construct),
            alignments = listOf(
                alignment(sheetId, SpatialAlignmentSource.Region(region.regionId), orderedOccurrences, region.sourceTrace),
                alignment(
                    sheetId,
                    SpatialAlignmentSource.Construct(construct.constructId),
                    orderedOccurrences,
                    construct.sourceTrace,
                ),
            ),
            gridReferences = listOf(
                gridReferences[0],
                gridReference(
                    sheetId,
                    grid.gridId,
                    SpatialGridReferenceSubject.Occurrence(obstacle.occurrenceId),
                    0,
                    0,
                    obstacle.sourceTrace,
                ),
                gridReferences[1],
                gridReference(
                    sheetId,
                    grid.gridId,
                    SpatialGridReferenceSubject.Construct(construct.constructId),
                    0,
                    0,
                    construct.sourceTrace,
                ),
            ),
        )
        return updated.withCurrentQualityTrace()
    }

    fun SpatialSheet.withCurrentQualityTrace(): SpatialSheet {
        val contributors = listOf(grid.sourceTrace) +
            occurrences.map(SpatialOccurrenceGeometry::sourceTrace) +
            regions.map(SpatialRegionGeometry::sourceTrace) +
            constructs.map(SpatialConstructGeometry::sourceTrace) +
            alignments.map(SpatialAlignment::sourceTrace) +
            anchors.map(SpatialAnchorPosition::sourceTrace) +
            routes.map(SpatialRoute::sourceTrace) +
            gridReferences.map(SpatialGridReference::sourceTrace)
        val currentTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId) +
                (sourceTrace.projectionIds + contributors.flatMap(SpatialSourceTrace::projectionIds))
                    .filterNot { projectionId -> projectionId == sheetId }
                    .distinct()
                    .sorted(),
            geometryElementIds = (sourceTrace.geometryElementIds +
                contributors.flatMap(SpatialSourceTrace::geometryElementIds))
                .distinctBy(GeometryElementId::value)
                .sortedBy(GeometryElementId::value),
        )
        return copy(
            quality = SpatialQualitySnapshot(
                quality.qualitySnapshotId,
                quality.sheetId,
                quality.metrics,
                currentTrace,
            ),
        )
    }

    fun validSheet(sheetId: String = "sheet:main"): SpatialSheet {
        val drawingArea = SpatialRect(40, 60, 1120, 640)
        val source = occurrence(sheetId, "occurrence:source", 80, 100)
        val target = occurrence(sheetId, "occurrence:target", 400, 100)
        val occurrences = listOf(source, target)
        val regionId = SpatialRegionId(sheetId, "region:main")
        val regionTrace = trace(sheetId, "region:main", "occurrence:source", "occurrence:target")
        val region = SpatialRegionGeometry(
            regionId = regionId,
            sheetId = sheetId,
            memberOccurrenceIds = occurrences.map(SpatialOccurrenceGeometry::occurrenceId),
            bounds = SpatialRect(56, 76, 448, 88),
            sourceTrace = regionTrace,
        )
        val constructId = SpatialConstructId(sheetId, "construct:main")
        val constructTrace = trace(sheetId, "construct:main", "occurrence:source", "occurrence:target")
        val construct = SpatialConstructGeometry(
            constructId = constructId,
            sheetId = sheetId,
            kind = "control-chain",
            name = "Main",
            memberOccurrenceIds = occurrences.map(SpatialOccurrenceGeometry::occurrenceId),
            envelope = SpatialRect(56, 76, 448, 88),
            sourceTrace = constructTrace,
        )
        val sourceAnchor = anchor(
            sheetId,
            source.occurrenceId,
            "port:source",
            SpatialBoundarySide.RIGHT,
            SpatialPoint(160, 120),
        )
        val targetAnchor = anchor(
            sheetId,
            target.occurrenceId,
            "port:target",
            SpatialBoundarySide.LEFT,
            SpatialPoint(400, 120),
        )
        val routeId = SpatialRouteId(sheetId, "connection:main")
        val laneId = SpatialLaneId(sheetId, SpatialLaneOrientation.HORIZONTAL, 120)
        val route = SpatialRoute(
            routeId = routeId,
            sheetId = sheetId,
            connectionId = StableSemanticIdentity("connection:main"),
            sourceAnchorId = sourceAnchor.anchorId,
            targetAnchorId = targetAnchor.anchorId,
            laneId = laneId,
            sourceTrace = routeTrace(sheetId),
            points = listOf(sourceAnchor.point, targetAnchor.point),
        )
        val grid = SpatialGridDefinition(sheetId, "grid:main", drawingArea, 3, 4, trace(sheetId, "grid:main"))
        val alignments = listOf(
            alignment(sheetId, SpatialAlignmentSource.Region(regionId), occurrences, regionTrace),
            alignment(sheetId, SpatialAlignmentSource.Construct(constructId), occurrences, constructTrace),
        )
        val anchors = listOf(sourceAnchor, targetAnchor)
        val lanes = listOf(
            SpatialLane(laneId, sheetId, SpatialLaneOrientation.HORIZONTAL, 120, listOf(routeId)),
        )
        val routes = listOf(route)
        val gridReferences = listOf(
            gridReference(
                sheetId,
                grid.gridId,
                SpatialGridReferenceSubject.Occurrence(source.occurrenceId),
                0,
                0,
                source.sourceTrace,
            ),
            gridReference(
                sheetId,
                grid.gridId,
                SpatialGridReferenceSubject.Occurrence(target.occurrenceId),
                0,
                1,
                target.sourceTrace,
            ),
            gridReference(
                sheetId,
                grid.gridId,
                SpatialGridReferenceSubject.Construct(constructId),
                0,
                0,
                construct.sourceTrace,
            ),
        )
        val sheetTrace = trace(sheetId)
        val qualityContributors = listOf(grid.sourceTrace) +
            occurrences.map(SpatialOccurrenceGeometry::sourceTrace) +
            listOf(region.sourceTrace, construct.sourceTrace) +
            alignments.map(SpatialAlignment::sourceTrace) +
            anchors.map(SpatialAnchorPosition::sourceTrace) +
            routes.map(SpatialRoute::sourceTrace) +
            gridReferences.map(SpatialGridReference::sourceTrace)
        val qualityTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId) +
                (sheetTrace.projectionIds + qualityContributors.flatMap(SpatialSourceTrace::projectionIds))
                    .filterNot { projectionId -> projectionId == sheetId }
                    .distinct()
                    .sorted(),
            geometryElementIds = (sheetTrace.geometryElementIds +
                qualityContributors.flatMap(SpatialSourceTrace::geometryElementIds))
                .distinctBy(GeometryElementId::value)
                .sortedBy(GeometryElementId::value),
        )
        return SpatialSheet(
            sheetId = sheetId,
            extent = SpatialRect(0, 0, 1200, 800),
            drawingArea = drawingArea,
            grid = grid,
            occurrences = occurrences,
            regions = listOf(region),
            constructs = listOf(construct),
            alignments = alignments,
            anchors = anchors,
            lanes = lanes,
            routes = routes,
            gridReferences = gridReferences,
            quality = SpatialQualitySnapshot(
                SpatialQualitySnapshotId(sheetId),
                sheetId,
                zeroSpatialQualityMetrics(),
                qualityTrace,
            ),
            sourceTrace = sheetTrace,
        )
    }

    fun occurrence(sheetId: String, projectionId: String, x: Int, y: Int): SpatialOccurrenceGeometry =
        SpatialOccurrenceGeometry(
            occurrenceId = SpatialOccurrenceId(sheetId, projectionId),
            subjectId = StableSemanticIdentity("subject:$projectionId"),
            sheetId = sheetId,
            regionId = "region:main",
            rectangle = SpatialRect(x, y, 80, 40),
            placementReason = SpatialPlacementReason(listOf("authored Region order")),
            sourceTrace = trace(sheetId, "region:main", projectionId),
        )

    fun alignment(
        sheetId: String,
        source: SpatialAlignmentSource,
        occurrences: List<SpatialOccurrenceGeometry>,
        sourceTrace: SpatialSourceTrace,
    ): SpatialAlignment = SpatialAlignment(
        alignmentId = SpatialAlignmentId(sheetId, source),
        sheetId = sheetId,
        constraintSource = source,
        occurrenceIds = occurrences.map(SpatialOccurrenceGeometry::occurrenceId),
        sourceTrace = sourceTrace,
    )

    fun anchor(
        sheetId: String,
        occurrenceId: SpatialOccurrenceId,
        portId: String,
        side: SpatialBoundarySide,
        point: SpatialPoint,
    ): SpatialAnchorPosition {
        val port = StableSemanticIdentity(portId)
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId(sheetId, occurrenceId, port),
            sheetId = sheetId,
            subject = SpatialOccurrencePortSubject(occurrenceId, port),
            side = side,
            point = point,
            sourceTrace = trace(sheetId, occurrenceId.projectionId, portId),
        )
    }

    fun gridReference(
        sheetId: String,
        gridId: String,
        subject: SpatialGridReferenceSubject,
        rowIndex: Int,
        columnIndex: Int,
        sourceTrace: SpatialSourceTrace,
    ): SpatialGridReference {
        val rowLabel = spatialGridRowLabel(rowIndex)
        val columnNumber = columnIndex + 1
        return SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(sheetId, subject),
            sheetId = sheetId,
            gridId = gridId,
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            columnNumber = columnNumber,
            cellReference = "$rowLabel$columnNumber",
            sourceTrace = sourceTrace,
        )
    }

    fun routeTrace(sheetId: String): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(
            sheetId,
            "connection:main",
            "occurrence:source",
            "port:source",
            "occurrence:target",
            "port:target",
        ),
        geometryElementIds = listOf(GeometryElementId("geometry:connection:main")),
    )

    fun trace(sheetId: String, vararg projectionIds: String): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheetId) + projectionIds,
        geometryElementIds = listOf(GeometryElementId("geometry:${projectionIds.joinToString(":")}")),
    )
}

internal fun zeroSpatialQualityMetrics(): SpatialQualityMetrics = SpatialQualityMetrics(
    occurrenceOverlapCount = 0,
    constructContainmentFailureCount = 0,
    routeBodyIntersectionCount = 0,
    routeCrossingCount = 0,
    twistCount = 0,
    usedLaneCount = 0,
    peakRoutesPerLane = 0,
    density = 0.0,
    occupancy = 0.0,
)
