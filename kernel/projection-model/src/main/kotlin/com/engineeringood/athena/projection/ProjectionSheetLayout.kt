package com.engineeringood.athena.projection

/**
 * Governed sheet layout facts positioned between projection/presentation and rendering.
 */
data class ProjectionSheetLayout(
    val sheetId: ProjectionSheetId,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val frame: ProjectionSheetLayoutFrame,
    val placements: List<ProjectionSheetLayoutPlacement> = emptyList(),
    val routingGuidance: List<ProjectionSheetLayoutRoutingGuidance> = emptyList(),
    val labelLayouts: List<ProjectionSheetLayoutLabelLayout> = emptyList(),
)

/**
 * Governed sheet frame facts used by the renderer as facts, not authority.
 */
data class ProjectionSheetLayoutFrame(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val gridMajorStep: Int = 120,
    val gridMinorStep: Int = 24,
)

/**
 * Governed placement fact for one rendered occurrence.
 */
data class ProjectionSheetLayoutPlacement(
    val projectionId: ProjectionNodeId,
    val semanticId: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Governed routing guidance for one projected connection.
 */
data class ProjectionSheetLayoutRoutingGuidance(
    val projectionConnectionId: ProjectionConnectionId,
    val connectionSemanticId: String,
    val sourcePoint: ProjectionPoint,
    val targetPoint: ProjectionPoint,
    val routingStyle: String = "orthogonal",
    val bendPoints: List<ProjectionPoint> = emptyList(),
)

/**
 * Governed label layout facts for one projected text subject.
 */
data class ProjectionSheetLayoutLabelLayout(
    val projectionId: ProjectionLabelId,
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Derives the active sheet layout facts from one projection document.
 */
fun ProjectionDocument.toProjectionSheetLayout(): ProjectionSheetLayout? {
    val sheet = sheets.firstOrNull() ?: return null
    val placements = nodes
        .sortedBy { node -> node.projectionId.value }
        .map { node ->
            ProjectionSheetLayoutPlacement(
                projectionId = node.projectionId,
                semanticId = node.semanticId.value,
                x = node.bounds.x,
                y = node.bounds.y,
                width = node.bounds.width,
                height = node.bounds.height,
            )
        }
    val labelLayouts = labels
        .sortedBy { label -> label.projectionId.value }
        .map { label ->
            ProjectionSheetLayoutLabelLayout(
                projectionId = label.projectionId,
                semanticId = label.semanticId.value,
                label = label.label,
                x = label.bounds.x,
                y = label.bounds.y,
                width = label.bounds.width,
                height = label.bounds.height,
            )
        }
    val connectionById = connections.associateBy { connection -> connection.projectionId }
    val routingGuidance = electricalRoutingCorridors
        .sortedBy { corridor -> corridor.corridorId.value }
        .mapNotNull { corridor ->
            val connection = connectionById[corridor.projectionConnectionId] ?: return@mapNotNull null
            ProjectionSheetLayoutRoutingGuidance(
                projectionConnectionId = corridor.projectionConnectionId,
                connectionSemanticId = corridor.connectionSemanticId.value,
                sourcePoint = connection.start,
                targetPoint = connection.end,
                routingStyle = corridor.routingStyle.name.lowercase(),
                bendPoints = corridor.preferredBendPoints,
            )
        }
    return ProjectionSheetLayout(
        sheetId = sheet.sheetId,
        displayName = sheet.displayName,
        order = sheet.order,
        subjectSemanticIds = sheet.subjects.map { subject -> subject.semanticId.value },
        representationFamilyId = sheet.composition.representationFamilyId,
        frame = ProjectionSheetLayoutFrame(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
        ),
        placements = placements,
        routingGuidance = routingGuidance,
        labelLayouts = labelLayouts,
    )
}
