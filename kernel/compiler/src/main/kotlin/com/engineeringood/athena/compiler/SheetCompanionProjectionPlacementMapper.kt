package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SheetCompanionSource
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.SheetPlacementConstraint
import com.engineeringood.athena.layout.SheetPlacementPoint
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.LogicalRouteConstraint
import com.engineeringood.athena.projection.LogicalRoutePoint
import com.engineeringood.athena.projection.LogicalRouteTargetId
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionSheet

internal data class SheetPlacementMappingDiagnostic(
    val message: String,
    val sourceSpan: LayoutSourceSpan,
)

internal data class SheetPlacementMappingResult(
    val constraints: List<SheetPlacementConstraint>,
    val diagnostics: List<SheetPlacementMappingDiagnostic>,
)

internal data class SheetRouteConstraintMappingResult(
    val connections: List<ConnectionProjection>,
    val diagnostics: List<SheetPlacementMappingDiagnostic>,
)

/** Binds Sheet Companion occurrence intent to existing Projection occurrences only. */
internal class SheetCompanionProjectionPlacementMapper {
    fun map(
        source: SheetCompanionSource,
        sheet: ProjectionSheet,
        projection: ProjectionDocument,
    ): SheetPlacementMappingResult {
        val nodesByLabel = projection.nodes
            .filter { node -> nodeBelongsToSheet(node, sheet) }
            .associateBy(ProjectionNode::label)
        val constraints = mutableListOf<SheetPlacementConstraint>()
        val diagnostics = mutableListOf<SheetPlacementMappingDiagnostic>()
        source.placements.forEach { placement ->
            val node = nodesByLabel[placement.occurrence]
            val span = placement.span.toLayoutSourceSpan()
            if (node == null) {
                diagnostics += SheetPlacementMappingDiagnostic(
                    message = "Sheet placement occurrence `${placement.occurrence}` does not resolve to a Projection occurrence.",
                    sourceSpan = span,
                )
            } else {
                constraints += SheetPlacementConstraint(
                    sheetId = sheet.sheetId.value,
                    occurrenceName = placement.occurrence,
                    occurrenceId = LayoutOccurrenceId(node.projectionId.value),
                    point = SheetPlacementPoint(placement.point.x, placement.point.y),
                    snapStep = source.snap.step,
                    locked = placement.locked,
                    sourceSpan = span,
                )
            }
        }
        return SheetPlacementMappingResult(
            constraints = constraints.sortedBy { constraint -> constraint.occurrenceId.value },
            diagnostics = diagnostics.sortedBy { diagnostic -> diagnostic.sourceSpan.startLine },
        )
    }

    fun mapRouteConstraints(
        source: SheetCompanionSource,
        sheet: ProjectionSheet,
        projection: ProjectionDocument,
    ): SheetRouteConstraintMappingResult {
        val connectionsById = projection.connections.associateBy { connection -> connection.projectionId.value }
        val constraintsByProjection = mutableMapOf<String, MutableList<LogicalRouteConstraint>>()
        val diagnostics = mutableListOf<SheetPlacementMappingDiagnostic>()
        source.routeConstraints.forEach { route ->
            val connection = connectionsById[route.projectionId]
            val span = route.span.toLayoutSourceSpan()
            when {
                connection == null -> diagnostics += SheetPlacementMappingDiagnostic(
                    message = "Sheet route projection `${route.projectionId}` does not resolve to a Connection Projection.",
                    sourceSpan = span,
                )
                sheet.sheetId.value !in projection.connectionSheetIds(connection) ->
                    diagnostics += SheetPlacementMappingDiagnostic(
                        message = "Connection Projection `${route.projectionId}` does not belong to Sheet `${sheet.sheetId.value}`.",
                        sourceSpan = span,
                    )
                else -> constraintsByProjection.getOrPut(route.projectionId, ::mutableListOf) +=
                    LogicalRouteConstraint.Via(
                        targetId = LogicalRouteTargetId(route.targetId),
                        point = LogicalRoutePoint(route.point.x, route.point.y),
                    )
            }
        }
        return SheetRouteConstraintMappingResult(
            connections = projection.connections.map { connection ->
                connection.copy(logicalRouteConstraints = constraintsByProjection[connection.projectionId.value].orEmpty())
            },
            diagnostics = diagnostics.sortedBy { diagnostic -> diagnostic.sourceSpan.startLine },
        )
    }

    private fun nodeBelongsToSheet(node: ProjectionNode, sheet: ProjectionSheet): Boolean {
        val subjectIds = sheet.subjects.map { subject -> subject.semanticId }.toSet()
        return node.semanticId in subjectIds || sheet.subjects.any { subject -> node.projectionId in subject.nodeIds }
    }

    private fun com.engineeringood.athena.language.SourceSpan.toLayoutSourceSpan(): LayoutSourceSpan = LayoutSourceSpan(
        sourceUnitId = "sheet-companion",
        startLine = start.line,
        startColumn = start.column,
        endLine = end.line,
        endColumn = end.column,
    )
}
