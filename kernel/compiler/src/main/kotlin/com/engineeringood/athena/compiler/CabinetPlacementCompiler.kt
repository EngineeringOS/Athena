package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetOccurrenceVisualJoin
import com.engineeringood.athena.drawing.composition.CabinetPointD
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceInput
import com.engineeringood.athena.drawing.composition.CabinetSizeD
import com.engineeringood.athena.drawing.composition.CabinetTargetFrame
import com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompilation
import com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompiler
import com.engineeringood.athena.drawing.composition.CabinetPhysicalOccurrenceInput
import com.engineeringood.athena.layout.LayoutGraph
import com.engineeringood.athena.physical.InstallationOccurrenceKey

data class CabinetPlacementRequest(
    val layoutGraph: LayoutGraph,
    val plannerId: String,
    val plannerVersion: String,
    val physicalOccurrences: List<CabinetPhysicalOccurrenceInput>,
    val representationOccurrences: List<CabinetRepresentationOccurrenceInput>,
    val enclosureToDrawing: CabinetTargetFrame,
)

data class CabinetPlacementFact(
    val key: InstallationOccurrenceKey,
    val snapshotId: String,
    val plannerId: String,
    val plannerVersion: String,
    val compilerSnapshotId: String,
    val proposedPhysicalOccurrence: CabinetPhysicalOccurrenceInput,
    val join: CabinetOccurrenceVisualJoin,
)

data class CabinetPlacementEvidence(
    val snapshotId: String,
    val plannerId: String,
    val plannerVersion: String,
    val placementCount: Int,
    val placementKeys: List<String>,
)

data class CabinetPlacementDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

sealed interface CabinetPlacementCompilation {
    data class Success(
        val placements: List<CabinetPlacementFact>,
        val evidence: CabinetPlacementEvidence,
    ) : CabinetPlacementCompilation

    data class Failure(val diagnostics: List<CabinetPlacementDiagnostic>) : CabinetPlacementCompilation
}

object CabinetPlacementCompiler {
    fun compile(request: CabinetPlacementRequest): CabinetPlacementCompilation {
        val diagnostics = mutableListOf<CabinetPlacementDiagnostic>()
        val expectedSnapshotPrefix = "layout:${request.layoutGraph.sourceUnitId.value}:${request.layoutGraph.installationId}:"
        if (!request.layoutGraph.snapshotId.value.startsWith(expectedSnapshotPrefix) || request.layoutGraph.compilerSnapshotId.isBlank()) {
            diagnostics += diagnostic(
                code = "cabinet.placement.snapshot.mismatch",
                subject = request.layoutGraph.snapshotId.value,
                message = "Layout graph snapshot must match installation and source unit identity.",
            )
        }

        val graphKeys = request.layoutGraph.occurrences.map { occurrence -> occurrence.semanticSubjectId }.sorted()
        val physicalKeys = request.physicalOccurrences.map { occurrence -> occurrence.key.canonicalSemanticSubjectId.value }.sorted()
        val representationKeys = request.representationOccurrences.map { occurrence -> occurrence.key.canonicalSemanticSubjectId.value }.sorted()
        if (graphKeys != physicalKeys || graphKeys != representationKeys) {
            diagnostics += diagnostic(
                code = "cabinet.placement.layout_graph.mismatch",
                subject = request.layoutGraph.snapshotId.value,
                message = "Layout graph occurrences must match the physical and representation placement inputs.",
            )
        }

        val graphBySubject = request.layoutGraph.occurrences.associateBy { it.semanticSubjectId }
        val proposedPhysicalOccurrences = request.physicalOccurrences.map { physical ->
            val graphOccurrence = graphBySubject[physical.key.canonicalSemanticSubjectId.value] ?: return@map physical
            val localFootprint = physical.footprint.oriented(physical.orientation)
            val expectedBoundsSize = physical.targetFrame.axisAlignedSize(localFootprint)
            if (
                kotlin.math.abs(expectedBoundsSize.width - graphOccurrence.bounds.width) > 1e-9 ||
                kotlin.math.abs(expectedBoundsSize.height - graphOccurrence.bounds.height) > 1e-9
            ) {
                diagnostics += diagnostic(
                    code = "cabinet.placement.physical_footprint.mismatch",
                    subject = physical.key.canonicalSemanticSubjectId.value,
                    message = "Layout proposals may move physical occurrences but must not resize their footprint.",
                )
                return@map physical
            }
            val localPosition = physical.targetFrame.localOriginForBounds(
                boundsOrigin = CabinetPointD(graphOccurrence.bounds.x.toDouble(), graphOccurrence.bounds.y.toDouble()),
                localFootprint = localFootprint,
            )
            physical.copy(
                targetLocalPosition = localPosition,
            )
        }
        val proposedByKey = proposedPhysicalOccurrences.associateBy { it.key }
        val transformCompilation = CabinetVisualTransformCompiler.compile(
            physicalOccurrences = proposedPhysicalOccurrences,
            representationOccurrences = request.representationOccurrences,
            enclosureToDrawing = request.enclosureToDrawing,
        )
        when (transformCompilation) {
            is CabinetVisualTransformCompilation.Failure -> {
                diagnostics += transformCompilation.diagnostics.map { issue ->
                    diagnostic(
                        code = "cabinet.placement.transform.${issue.code}",
                        subject = issue.subject,
                        message = issue.message,
                    )
                }
            }
            is CabinetVisualTransformCompilation.Success -> Unit
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetPlacementCompilation.Failure(
                diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
            )
        }

        val joins = (transformCompilation as CabinetVisualTransformCompilation.Success).joins.sortedBy { join ->
            join.key.canonicalSemanticSubjectId.value
        }
        val placements = joins.map { join ->
            CabinetPlacementFact(
                key = join.key,
                snapshotId = request.layoutGraph.snapshotId.value,
                plannerId = request.plannerId,
                plannerVersion = request.plannerVersion,
                compilerSnapshotId = request.layoutGraph.compilerSnapshotId,
                proposedPhysicalOccurrence = proposedByKey.getValue(join.key),
                join = join,
            )
        }
        return CabinetPlacementCompilation.Success(
            placements = placements,
            evidence = CabinetPlacementEvidence(
                snapshotId = request.layoutGraph.snapshotId.value,
                plannerId = request.plannerId,
                plannerVersion = request.plannerVersion,
                placementCount = placements.size,
                placementKeys = placements.map { placement -> placement.key.canonicalSemanticSubjectId.value },
            ),
        )
    }
}

private fun diagnostic(code: String, subject: String, message: String): CabinetPlacementDiagnostic =
    CabinetPlacementDiagnostic(code = code, subject = subject, message = message)

private fun CabinetTargetFrame.inverse(point: CabinetPointD): CabinetPointD {
    require(kotlin.math.abs(determinant) > 1e-9) { "Cabinet target frame must be invertible." }
    val dx = point.x - origin.x
    val dy = point.y - origin.y
    return CabinetPointD(
        x = (dx * normalAxis.y - dy * normalAxis.x) / determinant,
        y = (alongAxis.x * dy - alongAxis.y * dx) / determinant,
    )
}

private fun CabinetTargetFrame.localOriginForBounds(
    boundsOrigin: CabinetPointD,
    localFootprint: CabinetSizeD,
): CabinetPointD {
    val relativeCorners = localCorners(localFootprint).map { point ->
        CabinetPointD(
            x = (alongAxis.x * point.x) + (normalAxis.x * point.y),
            y = (alongAxis.y * point.x) + (normalAxis.y * point.y),
        )
    }
    val mappedLocalOrigin = CabinetPointD(
        x = boundsOrigin.x - relativeCorners.minOf { point -> point.x },
        y = boundsOrigin.y - relativeCorners.minOf { point -> point.y },
    )
    return inverse(mappedLocalOrigin)
}

private fun CabinetTargetFrame.axisAlignedSize(localFootprint: CabinetSizeD): CabinetSizeD {
    val corners = localCorners(localFootprint).map { point ->
        CabinetPointD(
            x = (alongAxis.x * point.x) + (normalAxis.x * point.y),
            y = (alongAxis.y * point.x) + (normalAxis.y * point.y),
        )
    }
    return CabinetSizeD(
        width = corners.maxOf { point -> point.x } - corners.minOf { point -> point.x },
        height = corners.maxOf { point -> point.y } - corners.minOf { point -> point.y },
    )
}

private fun CabinetSizeD.oriented(orientation: com.engineeringood.athena.physical.PhysicalInstallationOrientation): CabinetSizeD =
    when (orientation) {
        com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg0,
        com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg180,
        -> this
        com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg90,
        com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg270,
        -> CabinetSizeD(width = height, height = width)
    }

private fun localCorners(size: CabinetSizeD): List<CabinetPointD> = listOf(
    CabinetPointD(0.0, 0.0),
    CabinetPointD(size.width, 0.0),
    CabinetPointD(size.width, size.height),
    CabinetPointD(0.0, size.height),
)
