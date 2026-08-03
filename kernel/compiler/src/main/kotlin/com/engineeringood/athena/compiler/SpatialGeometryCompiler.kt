package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionRegion
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialAlignment
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialSourceTrace
import java.util.Collections

class SpatialGeometryCompilationResult(
    regions: List<SpatialRegionGeometry> = emptyList(),
    constructs: List<SpatialConstructGeometry> = emptyList(),
    alignments: List<SpatialAlignment> = emptyList(),
    diagnostics: List<SpatialDiagnostic> = emptyList(),
) {
    val regions: List<SpatialRegionGeometry> = regions.immutableGeometryCopy()
    val constructs: List<SpatialConstructGeometry> = constructs.immutableGeometryCopy()
    val alignments: List<SpatialAlignment> = alignments.immutableGeometryCopy()
    val diagnostics: List<SpatialDiagnostic> = diagnostics.immutableGeometryCopy()

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialGeometryCompilationResult &&
            regions == other.regions &&
            constructs == other.constructs &&
            alignments == other.alignments &&
            diagnostics == other.diagnostics

    override fun hashCode(): Int = listOf(regions, constructs, alignments, diagnostics).hashCode()

    override fun toString(): String =
        "SpatialGeometryCompilationResult(regions=$regions, constructs=$constructs, " +
            "alignments=$alignments, diagnostics=$diagnostics)"
}

class SpatialGeometryCompiler {
    private val planner = ProjectionPlacementPlanner()
    private val validator = SpatialGroupingValidator(planner)

    fun compile(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): SpatialGeometryCompilationResult {
        val diagnostics = validator.validate(projection, occurrences)
        if (diagnostics.isNotEmpty()) {
            return SpatialGeometryCompilationResult(diagnostics = diagnostics)
        }
        val regions = mutableListOf<SpatialRegionGeometry>()
        val constructs = mutableListOf<SpatialConstructGeometry>()
        val alignments = mutableListOf<SpatialAlignment>()
        projection.sheets
            .sortedWith(compareBy({ sheet -> sheet.order }, { sheet -> sheet.sheetId.value }))
            .forEach { sheet ->
                val sheetNodes = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
                val nodesByLabel = sheetNodes.associateBy(ProjectionNode::label)
                val occurrencesByProjectionId = occurrences
                    .filter { occurrence -> occurrence.sheetId == sheet.sheetId.value }
                    .associateBy { occurrence -> occurrence.occurrenceId.projectionId }

                planner.placementGroups(sheet, projection).forEach { group ->
                    val authoredRegion = sheet.regions.singleOrNull { region -> region.regionId == group.regionId }
                    val memberNodes = authoredRegion?.occurrenceNames?.map(nodesByLabel::getValue) ?: group.nodes
                    val members = memberNodes.map { node -> occurrencesByProjectionId.getValue(node.projectionId.value) }
                    val regionId = SpatialRegionId(sheet.sheetId.value, group.regionId)
                    val sourceTrace = regionTrace(sheet, authoredRegion, memberNodes)
                    regions += SpatialRegionGeometry(
                        regionId = regionId,
                        sheetId = sheet.sheetId.value,
                        memberOccurrenceIds = members.map(SpatialOccurrenceGeometry::occurrenceId),
                        bounds = paddedGroupingUnion(members.map(SpatialOccurrenceGeometry::rectangle)),
                        sourceTrace = sourceTrace,
                    )
                    val alignmentSource = SpatialAlignmentSource.Region(regionId)
                    alignments += SpatialAlignment(
                        alignmentId = SpatialAlignmentId(sheet.sheetId.value, alignmentSource),
                        sheetId = sheet.sheetId.value,
                        constraintSource = alignmentSource,
                        occurrenceIds = group.nodes.map { node ->
                            occurrencesByProjectionId.getValue(node.projectionId.value).occurrenceId
                        },
                        sourceTrace = sourceTrace,
                    )
                }

                sheet.constructs.forEach { construct ->
                    val memberNodes = construct.memberNames.map(nodesByLabel::getValue)
                    val members = memberNodes.map { node -> occurrencesByProjectionId.getValue(node.projectionId.value) }
                    val constructId = SpatialConstructId(sheet.sheetId.value, construct.constructId.value)
                    val sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, construct.constructId.value) +
                            memberNodes.map { node -> node.projectionId.value },
                        geometryElementIds = listOf(construct.originGeometryElementId) +
                            memberNodes.map(ProjectionNode::originGeometryElementId),
                    )
                    constructs += SpatialConstructGeometry(
                        constructId = constructId,
                        sheetId = sheet.sheetId.value,
                        kind = construct.kind,
                        name = construct.name,
                        memberOccurrenceIds = members.map(SpatialOccurrenceGeometry::occurrenceId),
                        envelope = paddedGroupingUnion(members.map(SpatialOccurrenceGeometry::rectangle)),
                        sourceTrace = sourceTrace,
                    )
                    val alignmentSource = SpatialAlignmentSource.Construct(constructId)
                    alignments += SpatialAlignment(
                        alignmentId = SpatialAlignmentId(sheet.sheetId.value, alignmentSource),
                        sheetId = sheet.sheetId.value,
                        constraintSource = alignmentSource,
                        occurrenceIds = members.map(SpatialOccurrenceGeometry::occurrenceId),
                        sourceTrace = sourceTrace,
                    )
                }
            }
        return SpatialGeometryCompilationResult(
            regions = regions,
            constructs = constructs,
            alignments = alignments,
        )
    }

    private fun regionTrace(
        sheet: ProjectionSheet,
        region: ProjectionRegion?,
        memberNodes: List<ProjectionNode>,
    ): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheet.sheetId.value, region?.regionId ?: planner.unassignedRegionId(sheet)) +
            memberNodes.map { node -> node.projectionId.value },
        geometryElementIds = listOf(region?.originGeometryElementId ?: sheet.originGeometryElementId) +
            memberNodes.map(ProjectionNode::originGeometryElementId),
    )

}

private fun <T> List<T>.immutableGeometryCopy(): List<T> = Collections.unmodifiableList(toList())

internal fun paddedGroupingUnion(rectangles: List<SpatialRect>): SpatialRect {
    return requireNotNull(paddedGroupingUnionOrNull(rectangles)) {
        "Padded grouping geometry must fit within Int drawing units."
    }
}

internal fun paddedGroupingUnionOrNull(rectangles: List<SpatialRect>): SpatialRect? {
    if (rectangles.isEmpty()) return null
    val minX = rectangles.minOf { rectangle -> rectangle.x }.toLong()
    val minY = rectangles.minOf { rectangle -> rectangle.y }.toLong()
    val maxRight = rectangles.maxOf { rectangle -> rectangle.x.toLong() + rectangle.width.toLong() }
    val maxBottom = rectangles.maxOf { rectangle -> rectangle.y.toLong() + rectangle.height.toLong() }
    val padding = ProjectionSpatialLayout.GROUPING_PADDING.toLong()
    val x = minX - padding
    val y = minY - padding
    val right = maxRight + padding
    val bottom = maxBottom + padding
    val width = right - x
    val height = bottom - y
    if (x !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        y !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        right !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        bottom !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        width !in 1L..Int.MAX_VALUE.toLong() ||
        height !in 1L..Int.MAX_VALUE.toLong()
    ) {
        return null
    }
    return SpatialRect(
        x = x.toInt(),
        y = y.toInt(),
        width = width.toInt(),
        height = height.toInt(),
    )
}
