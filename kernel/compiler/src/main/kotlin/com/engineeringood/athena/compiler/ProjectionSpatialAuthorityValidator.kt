package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRoutePlanId
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
        routes: List<ConnectionRoutePlan>,
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
        actual: List<ConnectionRoutePlan>,
    ): List<SpatialDiagnostic> {
        val actualById = actual.groupBy(ConnectionRoutePlan::routeId)
        return inventory.visibleConnections(projection, sheet).flatMap { connection ->
            connection.routeLegs().flatMap { leg ->
                val projectionRouteId = connection.projectionId.value + leg.routeProjectionIdSuffix
                val routeId = ConnectionRoutePlanId(sheet.sheetId.value, projectionRouteId)
                val route = actualById[routeId].orEmpty().singleOrNull() ?: return@flatMap emptyList()
                val expectedSource = SpatialAnchorId(
                    sheet.sheetId.value,
                    SpatialOccurrenceId(sheet.sheetId.value, leg.source.occurrenceId.value),
                    leg.source.portId,
                )
                val expectedTarget = SpatialAnchorId(
                    sheet.sheetId.value,
                    SpatialOccurrenceId(sheet.sheetId.value, leg.target.occurrenceId.value),
                    leg.target.portId,
                )
                buildList {
                    if (route.sourceAnchorId != expectedSource || route.targetAnchorId != expectedTarget) {
                        add(
                            SpatialDiagnostic(
                                subject = "Route ${route.routeId.value}",
                                problem = "ordered route-leg Anchors do not equal Projection route leg " +
                                    "${expectedSource.value} and ${expectedTarget.value}",
                                correction =
                                    "Preserve Projection route-leg occurrence-port order in the Spatial Route.",
                                sourceTrace = route.sourceTrace,
                            ),
                        )
                    }
                    if (route.projectionConnectionId != connection.projectionId.value) {
                        add(
                            SpatialDiagnostic(
                                subject = "Route ${route.routeId.value}",
                                problem = "Projection Connection ${route.projectionConnectionId} does not equal " +
                                    connection.projectionId.value,
                                correction = "Preserve the canonical Projection Connection on every Spatial Route leg.",
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
}
