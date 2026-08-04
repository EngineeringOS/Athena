package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class ProjectionSpatialAuthorityValidator(
    private val planner: ProjectionPlacementPlanner,
    private val inventory: ProjectionSpatialCoverageInventory,
) {
    fun validate(
        projection: ProjectionDocument,
        expectedSheet: ProjectionSheet,
        actualSheets: List<SpatialSheet>,
        occurrences: List<SpatialOccurrenceGeometry>,
        routes: List<SpatialRoute>,
    ): List<SpatialDiagnostic> = buildList {
        if (actualSheets.size == 1) addAll(rootDiagnostics(expectedSheet, actualSheets.single()))
        addAll(occurrencePayloadDiagnostics(projection, expectedSheet, occurrences))
        addAll(routePayloadDiagnostics(projection, expectedSheet, routes))
    }

    private fun rootDiagnostics(expected: ProjectionSheet, actual: SpatialSheet): List<SpatialDiagnostic> = buildList {
        val expectedSheetTrace = sheetTrace(expected)
        if (actual.sourceTrace != expectedSheetTrace) {
            add(
                SpatialDiagnostic(
                    subject = "Sheet ${expected.sheetId.value}",
                    problem = "final Spatial Sheet Source Trace does not equal canonical Projection provenance",
                    correction = "Rebuild the final Spatial Sheet Source Trace from its canonical Projection Sheet.",
                    sourceTrace = actual.sourceTrace,
                ),
            )
        }
        val expectedGrid = expected.grid
        if (expectedGrid == null) {
            add(
                SpatialDiagnostic(
                    subject = "Grid ${actual.grid.gridId} on Sheet ${actual.sheetId}",
                    problem = "has no matching Projection grid",
                    correction = "Publish a Spatial grid only after its Projection Sheet defines one.",
                    sourceTrace = actual.grid.sourceTrace,
                ),
            )
        } else {
            if (
                actual.grid.gridId != expectedGrid.gridId ||
                actual.grid.rows != expectedGrid.rows ||
                actual.grid.columns != expectedGrid.columns
            ) {
                add(
                    SpatialDiagnostic(
                        subject = "Grid ${expectedGrid.gridId} on Sheet ${actual.sheetId}",
                        problem = "publishes grid ${actual.grid.gridId} with ${actual.grid.rows} rows and " +
                            "${actual.grid.columns} columns instead of Projection grid ${expectedGrid.gridId} with " +
                            "${expectedGrid.rows} rows and ${expectedGrid.columns} columns",
                        correction = "Publish the exact Projection-owned grid identity and dimensions on this Spatial Sheet.",
                        sourceTrace = actual.grid.sourceTrace,
                    ),
                )
            }
            val expectedGridTrace = SpatialSourceTrace(
                projectionIds = listOf(expected.sheetId.value, expectedGrid.gridId),
                geometryElementIds = listOf(expected.originGeometryElementId),
            )
            if (actual.grid.sourceTrace != expectedGridTrace) {
                add(
                    SpatialDiagnostic(
                        subject = "Grid ${expectedGrid.gridId} on Sheet ${actual.sheetId}",
                        problem = "final Spatial grid Source Trace does not equal canonical Projection provenance",
                        correction = "Rebuild the final Spatial grid Source Trace from its canonical Projection grid.",
                        sourceTrace = actual.grid.sourceTrace,
                    ),
                )
            }
        }
    }

    private fun occurrencePayloadDiagnostics(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        actual: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> {
        val expectedById = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
            .associateBy { node -> SpatialOccurrenceId(sheet.sheetId.value, node.projectionId.value) }
        val actualById = actual.groupBy(SpatialOccurrenceGeometry::occurrenceId)
        return expectedById.mapNotNull { (occurrenceId, node) ->
            val match = actualById[occurrenceId].orEmpty().singleOrNull() ?: return@mapNotNull null
            if (match.subjectId == node.semanticId) return@mapNotNull null
            SpatialDiagnostic(
                subject = "Occurrence ${occurrenceId.projectionId} on Sheet ${occurrenceId.sheetId}",
                problem = "semantic subject ${match.subjectId.value} does not equal Projection subject ${node.semanticId.value}",
                correction = "Preserve the canonical Projection semantic subject on the Spatial Occurrence.",
                sourceTrace = match.sourceTrace,
            )
        }
    }

    private fun routePayloadDiagnostics(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        actual: List<SpatialRoute>,
    ): List<SpatialDiagnostic> {
        val actualById = actual.groupBy(SpatialRoute::routeId)
        return inventory.visibleConnections(projection, sheet).flatMap { connection ->
            val routeId = SpatialRouteId(sheet.sheetId.value, connection.projectionId.value)
            val route = actualById[routeId].orEmpty().singleOrNull() ?: return@flatMap emptyList()
            val source = requireNotNull(connection.source).occurrencePortId
            val target = requireNotNull(connection.target).occurrencePortId
            val expectedSource = SpatialAnchorId(
                sheet.sheetId.value,
                SpatialOccurrenceId(sheet.sheetId.value, source.occurrenceId.value),
                source.portId,
            )
            val expectedTarget = SpatialAnchorId(
                sheet.sheetId.value,
                SpatialOccurrenceId(sheet.sheetId.value, target.occurrenceId.value),
                target.portId,
            )
            buildList {
                if (route.sourceAnchorId != expectedSource || route.targetAnchorId != expectedTarget) {
                    add(
                        SpatialDiagnostic(
                            subject = "Route ${route.routeId.value}",
                            problem = "ordered endpoint Anchors do not equal Projection source ${expectedSource.value} " +
                                "and target ${expectedTarget.value}",
                            correction =
                                "Preserve Projection source and target occurrence-port order in the Spatial Route.",
                            sourceTrace = route.sourceTrace,
                        ),
                    )
                }
                if (route.connectionId != connection.semanticId) {
                    add(
                        SpatialDiagnostic(
                            subject = "Route ${route.routeId.value}",
                            problem = "semantic Connection ${route.connectionId.value} does not equal Projection subject " +
                                connection.semanticId.value,
                            correction = "Preserve the canonical Projection semantic Connection on the Spatial Route.",
                            sourceTrace = route.sourceTrace,
                        ),
                    )
                }
            }
        }
    }
}
