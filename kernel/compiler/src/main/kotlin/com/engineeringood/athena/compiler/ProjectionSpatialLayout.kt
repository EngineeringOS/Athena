package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.engine.RuleBasedLayoutEngine
import com.engineeringood.athena.layout.engine.RuleBasedLayoutItem
import com.engineeringood.athena.layout.engine.RuleBasedLayoutPoint
import com.engineeringood.athena.layout.engine.RuleBasedLayoutSize
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace

data class SpatialLayoutResult(
    val occurrences: List<SpatialOccurrenceGeometry>,
    val diagnostics: List<SpatialDiagnostic> = emptyList(),
)

class ProjectionSpatialLayout {
    private val engine = RuleBasedLayoutEngine()
    private val planner = ProjectionPlacementPlanner()
    private val validator = ProjectionSpatialValidator(planner)

    fun place(projection: ProjectionDocument): SpatialLayoutResult {
        val sheets = projection.sheets
            .sortedWith(compareBy({ sheet -> sheet.order }, { sheet -> sheet.sheetId.value }))
        val diagnostics = validator.validate(projection, sheets)
        if (diagnostics.isNotEmpty()) {
            return SpatialLayoutResult(
                occurrences = emptyList(),
                diagnostics = diagnostics,
            )
        }
        return SpatialLayoutResult(
            occurrences = sheets.flatMap { sheet -> placeSheet(sheet, projection) },
        )
    }

    private fun placeSheet(
        sheet: ProjectionSheet,
        projection: ProjectionDocument,
    ): List<SpatialOccurrenceGeometry> {
        val regionGroups = planner.placementGroups(sheet, projection)
        if (regionGroups.isEmpty()) {
            return emptyList()
        }
        val columnWidth = (DRAWING_AREA.width - REGION_GUTTER * (regionGroups.size - 1)) / regionGroups.size
        val verticalStart = DRAWING_AREA.y + GROUPING_PADDING
        val verticalEnd = DRAWING_AREA.bottom - GROUPING_PADDING - NODE_HEIGHT
        val items = regionGroups.flatMapIndexed { regionIndex, group ->
            group.nodes.mapIndexed { nodeIndex, node ->
                RuleBasedLayoutItem(
                    stableId = node.projectionId.value,
                    groupId = group.regionId,
                    groupRank = regionIndex,
                    itemRank = nodeIndex,
                    size = RuleBasedLayoutSize(width = NODE_WIDTH, height = NODE_HEIGHT),
                    payload = ProjectionPlacementCandidate(group = group, node = node),
                )
            }
        }
        return engine.place(items) { context ->
            val x = DRAWING_AREA.x +
                context.groupIndex * (columnWidth + REGION_GUTTER) +
                (columnWidth - NODE_WIDTH) / 2
            val y = when {
                context.itemCount > 1 -> verticalStart +
                    (verticalEnd - verticalStart) * context.itemIndex / (context.itemCount - 1)
                context.groupCount > 1 -> verticalStart +
                    (verticalEnd - verticalStart) * context.groupIndex / (context.groupCount - 1)
                else -> (verticalStart + verticalEnd) / 2
            }
            RuleBasedLayoutPoint(x = x, y = y)
        }.map { placement ->
            val group = placement.payload.group
            val node = placement.payload.node
            SpatialOccurrenceGeometry(
                occurrenceId = SpatialOccurrenceId(
                    sheetId = sheet.sheetId.value,
                    projectionId = node.projectionId.value,
                ),
                subjectId = node.semanticId,
                sheetId = sheet.sheetId.value,
                regionId = group.regionId,
                rectangle = SpatialRect(
                    x = placement.position.x,
                    y = placement.position.y,
                    width = placement.size.width,
                    height = placement.size.height,
                ),
                placementReason = SpatialPlacementReason(
                    constraints = buildList {
                        add("owning Sheet ${sheet.sheetId.value}")
                        if (group.authored) {
                            add("Region ${group.regionName} in authored order ${placement.context.groupIndex + 1}")
                        } else {
                            add("explicit final Unassigned Region ${group.regionId}")
                        }
                        addAll(group.orderingConstraints.getValue(node.projectionId))
                        add(
                            "Drawing Area (${DRAWING_AREA.x},${DRAWING_AREA.y}," +
                                "${DRAWING_AREA.width},${DRAWING_AREA.height})",
                        )
                        if (placement.context.groupCount > 1) {
                            add("$REGION_GUTTER-unit Region gutter")
                        }
                        add("$GROUPING_PADDING-unit grouping padding")
                        if (placement.context.itemCount > 1) {
                            add("$OCCURRENCE_SEPARATION-unit minimum vertical separation")
                        }
                    },
                ),
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(
                        sheet.sheetId.value,
                        group.regionId,
                        node.projectionId.value,
                    ),
                    geometryElementIds = listOf(node.originGeometryElementId),
                ),
            )
        }
    }

    private data class ProjectionPlacementCandidate(
        val group: ProjectionPlacementGroup,
        val node: ProjectionNode,
    )

    companion object {
        val SHEET_EXTENT: SpatialRect = SpatialRect(x = 0, y = 0, width = 1200, height = 800)
        val DRAWING_AREA: SpatialRect = SpatialRect(x = 40, y = 60, width = 1120, height = 640)
        const val TITLE_BLOCK_START_Y: Int = 740
        const val REGION_GUTTER: Int = 32
        const val OCCURRENCE_SEPARATION: Int = 48
        const val GROUPING_PADDING: Int = 24

        internal const val NODE_WIDTH = 80
        internal const val NODE_HEIGHT = 40
    }
}
