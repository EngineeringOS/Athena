package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * One canonical engineering subject as it appears on a governed projection sheet.
 *
 * The subject keeps canonical semantic identity while the referenced projection ids remain
 * sheet-local placements owned by the projection layer.
 */
data class ProjectionSheetSubject(
    val semanticId: StableSemanticIdentity,
    val nodeIds: List<ProjectionNodeId> = emptyList(),
    val connectionIds: List<ProjectionConnectionId> = emptyList(),
    val labelIds: List<ProjectionLabelId> = emptyList(),
)

/**
 * One governed sheet in a derived projection document.
 *
 * Sheet ordering and navigation remain projection-owned metadata. They never replace or redefine
 * canonical engineering identity.
 */
data class ProjectionSheet(
    val sheetId: ProjectionSheetId,
    val displayName: String,
    val order: Int,
    val previousSheetId: ProjectionSheetId? = null,
    val nextSheetId: ProjectionSheetId? = null,
    val subjects: List<ProjectionSheetSubject> = emptyList(),
)
