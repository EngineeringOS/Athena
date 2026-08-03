package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationConnectorLabelDisplay
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.routing.ConnectionPresentationLineEvidence
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteLabelFact
import com.engineeringood.athena.routing.SchematicRoutePoint

data class PresentationConnectorCompilationResult(
    val connectors: List<PresentationConnector>,
    val diagnostics: List<AthenaProfessionalDrawingDiagnostic>,
)

class PresentationConnectorCompiler {
    fun compile(
        routeFacts: List<RouteFact>,
        occurrences: List<PresentationGraphicOccurrence>,
        lineEvidence: List<ConnectionPresentationLineEvidence>,
    ): PresentationConnectorCompilationResult {
        val diagnostics = mutableListOf<AthenaProfessionalDrawingDiagnostic>()
        val occurrenceBySubject = occurrences.associateBy { occurrence -> occurrence.semanticSubjectId }
        val lineByRoute = lineEvidence.associateBy { line -> line.routeId }
        val connectors = routeFacts.sortedBy { route -> route.routeId.value }.mapNotNull { route ->
            val sourceOccurrence = occurrenceBySubject[route.source.subjectId.value]
            val targetOccurrence = occurrenceBySubject[route.target.subjectId.value]
            val line = lineByRoute[route.routeId]
            if (sourceOccurrence == null || targetOccurrence == null || line == null) {
                diagnostics += diagnostic(
                    "drawing.connector.evidence.missing",
                    route.routeId.value,
                    "Visible Connection requires source occurrence, target occurrence, and line class evidence.",
                )
                return@mapNotNull null
            }
            val sourceTerminal = sourceOccurrence.terminalBindingFor(route.source.portSemanticId?.value, route.source.anchorId.value)
            val targetTerminal = targetOccurrence.terminalBindingFor(route.target.portSemanticId?.value, route.target.anchorId.value)
            if (sourceTerminal == null || targetTerminal == null) {
                diagnostics += diagnostic(
                    "drawing.connector.endpoint.missing",
                    route.routeId.value,
                    "Visible Connection endpoint must resolve to exactly one placed Port-to-Anchor binding.",
                )
                return@mapNotNull null
            }
            val sourceEndpoint = sourceTerminal.toEndpoint(sourceOccurrence)
            val targetEndpoint = targetTerminal.toEndpoint(targetOccurrence)
            val normalizedRoute = normalizeRoute(
                route = route,
                sourcePoint = sourceEndpoint.point,
                targetPoint = targetEndpoint.point,
                diagnostics = diagnostics,
            ) ?: return@mapNotNull null
            PresentationConnector(
                occurrenceId = PresentationOccurrenceId(route.routeId.value),
                semanticId = StableSemanticIdentity(route.connectionId.value),
                primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
                routePoints = normalizedRoute,
                line = line.toPresentationLine(route),
                routeId = route.routeId.value,
                bundleId = route.bundleId.value,
                laneId = route.laneAssignment.laneId.value,
                laneRouteIds = route.laneAssignment.occupancy.routeIds.map { routeId -> routeId.value }.sorted(),
                selectedChannelIds = route.selectedChannelIds.sorted(),
                labels = route.labels.map { label -> label.toPresentationLabel(route) },
                quality = route.quality.state.name,
                sourceEndpoint = sourceEndpoint,
                targetEndpoint = targetEndpoint,
                sourceProjectionIds = listOf(
                    route.snapshotId.value,
                    route.routeId.value,
                    route.connectionId.value,
                    line.lineClassId.value,
                ).distinct().sorted(),
                sourceSpan = com.engineeringood.athena.layout.LayoutSourceSpan(
                    sourceUnitId = route.provenance.file,
                    startLine = route.provenance.startLine,
                    startColumn = route.provenance.startColumn,
                    endLine = route.provenance.endLine,
                    endColumn = route.provenance.endColumn,
                ),
            )
        }
        return PresentationConnectorCompilationResult(
            connectors = connectors,
            diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
        )
    }

    private fun normalizeRoute(
        route: RouteFact,
        sourcePoint: PresentationPoint,
        targetPoint: PresentationPoint,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): List<PresentationPoint>? {
        val points = buildList {
            if (route.segments.isNotEmpty()) add(route.segments.first().start.toPresentationPoint())
            route.segments.forEach { segment -> add(segment.end.toPresentationPoint()) }
        }.toMutableList()
        if (points.isNotEmpty()) {
            points[0] = sourcePoint
            points[points.lastIndex] = targetPoint
        }
        val normalized = mergeOrthogonal(removeZeroLength(points))
        if (normalized.size < 2) {
            diagnostics += diagnostic(
                "drawing.connector.route.too-short",
                route.routeId.value,
                "Visible Connection route needs at least two distinct endpoint points.",
            )
            return null
        }
        val nonOrthogonal = normalized.zipWithNext().any { (a, b) -> a.x != b.x && a.y != b.y }
        if (nonOrthogonal) {
            diagnostics += diagnostic(
                "drawing.connector.route.non-orthogonal",
                route.routeId.value,
                "Visible Connection route must be orthogonal after exact endpoint attachment.",
            )
            return null
        }
        return normalized
    }

    private fun removeZeroLength(points: List<PresentationPoint>): List<PresentationPoint> =
        points.fold(emptyList()) { acc, point ->
            if (acc.lastOrNull() == point) acc else acc + point
        }

    private fun mergeOrthogonal(points: List<PresentationPoint>): List<PresentationPoint> {
        if (points.size < 3) return points
        val result = mutableListOf(points.first())
        points.drop(1).forEach { point ->
            result += point
            while (result.size >= 3) {
                val a = result[result.lastIndex - 2]
                val b = result[result.lastIndex - 1]
                val c = result[result.lastIndex]
                val sameHorizontal = a.y == b.y && b.y == c.y
                val sameVertical = a.x == b.x && b.x == c.x
                if (sameHorizontal || sameVertical) {
                    result.removeAt(result.lastIndex - 1)
                } else {
                    break
                }
            }
        }
        return result
    }

    private fun PresentationGraphicOccurrence.terminalBindingFor(
        portSemanticId: String?,
        routeAnchorId: String,
    ): PresentationGraphicTerminalBinding? {
        val anchorId = localAnchorId(routeAnchorId)
        val matches = terminalBindings.filter { binding ->
            binding.anchorId == anchorId && (portSemanticId == null || binding.portSemanticId == portSemanticId)
        }
        return matches.singleOrNull()
    }

    private fun PresentationGraphicOccurrence.localAnchorId(routeAnchorId: String): String =
        routeAnchorId.removePrefix("${occurrenceId.value}:")

    private fun PresentationGraphicTerminalBinding.toEndpoint(
        occurrence: PresentationGraphicOccurrence,
    ): PresentationConnectorEndpoint = PresentationConnectorEndpoint(
        portSemanticId = StableSemanticIdentity(portSemanticId),
        bindingId = bindingId,
        occurrenceId = occurrence.occurrenceId,
        anchorId = RepresentationAnchorId(anchorId),
        point = point.toPresentationPoint(),
        sourceProvenance = occurrence.sourceProvenance,
    )

    private fun SchematicRoutePoint.toPresentationPoint(): PresentationPoint = PresentationPoint(x = x, y = y)

    private fun ConnectionPresentationLineEvidence.toPresentationLine(route: RouteFact): PresentationConnectorLine =
        PresentationConnectorLine(
            classId = lineClassId.value,
            lineKind = lineClassId.value.removePrefix("line:").uppercase().replace('-', '_'),
            lineStyleId = lineStyleId.value,
            weight = weight,
            style = style.name,
            colorKey = colorKey,
            endpointBehavior = endpointBehavior.name,
            labelPolicy = labelPolicy.name,
            crossingBehavior = crossingBehavior.name,
            policyId = selectedPolicyId,
            compilerSnapshotId = compilerSnapshotId,
        )

    private fun RouteLabelFact.toPresentationLabel(route: RouteFact): PresentationConnectorLabel =
        PresentationConnectorLabel(
            labelId = labelId.value,
            targetId = route.routeId.value,
            text = text,
            point = placement.origin.toPresentationPoint(),
            bounds = com.engineeringood.athena.presentation.PresentationDrawingBounds(
                x = (placement.origin.x - (text.length * 3)).coerceAtLeast(0),
                y = (placement.origin.y - 5).coerceAtLeast(0),
                width = (text.length * 6).coerceAtLeast(6),
                height = 10,
            ),
            labelClassId = "label:route",
            display = if (text.trim().length <= 3 || text.contains("->") || text.length > 24) {
                PresentationConnectorLabelDisplay.SELECTION
            } else {
                PresentationConnectorLabelDisplay.ALWAYS
            },
            sourceProvenance = listOf("${route.provenance.file}:${route.provenance.startLine}:${route.provenance.startColumn}"),
            compilerSnapshotId = route.compilerSnapshotId,
        )
}

private fun diagnostic(code: String, subject: String, message: String) =
    AthenaProfessionalDrawingDiagnostic(code, subject, message)
