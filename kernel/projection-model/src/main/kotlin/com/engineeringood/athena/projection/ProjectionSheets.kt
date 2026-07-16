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
 * Governed publication semantics for one projection sheet.
 */
data class ProjectionSheetPublication(
    val pageSize: ProjectionSheetPageSize,
    val frame: ProjectionSheetFrame,
    val coordinateZones: List<ProjectionSheetCoordinateZone> = emptyList(),
    val titleBlock: ProjectionSheetTitleBlock,
    val revisionMetadata: ProjectionSheetRevisionMetadata,
    val viewComposition: ProjectionSheetViewComposition,
) {
    companion object {
        fun fromProjectionState(
            sheetId: ProjectionSheetId,
            displayName: String,
            order: Int,
            subjects: List<ProjectionSheetSubject>,
        ): ProjectionSheetPublication {
            val viewId = sheetId.value.substringBefore("/sheet/").ifBlank { sheetId.value }
            return ProjectionSheetPublication(
                pageSize = ProjectionSheetPageSize(format = "A3", orientation = "landscape"),
                frame = ProjectionSheetFrame(
                    frameId = "engineering-sheet-frame",
                    style = "schematic",
                ),
                coordinateZones = listOf(
                    ProjectionSheetCoordinateZone(zoneId = "header", label = "Header", order = 0),
                    ProjectionSheetCoordinateZone(zoneId = "body", label = "Body", order = 1),
                    ProjectionSheetCoordinateZone(zoneId = "title-block", label = "Title Block", order = 2),
                ),
                titleBlock = ProjectionSheetTitleBlock(
                    sheetTitle = displayName,
                    sheetFamily = viewId,
                    sheetNumber = sheetId.value.substringAfterLast("/"),
                ),
                revisionMetadata = ProjectionSheetRevisionMetadata(
                    revisionCode = "A",
                    revisionNote = "Initial governed sheet publication",
                ),
                viewComposition = ProjectionSheetViewComposition(
                    primaryViewId = viewId,
                    primarySheetOrder = order,
                    subjectSemanticIds = subjects.map { subject -> subject.semanticId.value },
                ),
            )
        }

        fun defaultFor(
            sheetId: ProjectionSheetId,
            displayName: String,
            order: Int,
            subjects: List<ProjectionSheetSubject>,
        ): ProjectionSheetPublication {
            return fromProjectionState(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjects = subjects,
            )
        }
    }
}

/**
 * Governed sheet composition for one projection sheet.
 */
data class ProjectionSheetComposition(
    val sheetId: ProjectionSheetId,
    val displayName: String,
    val order: Int,
    val subjects: List<ProjectionSheetSubject> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val publication: ProjectionSheetPublication,
) {
    companion object {
        fun fromProjectionState(
            sheetId: ProjectionSheetId,
            displayName: String,
            order: Int,
            subjects: List<ProjectionSheetSubject>,
            representationFamilyId: String = "schematic-sheet",
            publication: ProjectionSheetPublication = ProjectionSheetPublication.fromProjectionState(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjects = subjects,
            ),
        ): ProjectionSheetComposition {
            return ProjectionSheetComposition(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjects = subjects,
                representationFamilyId = representationFamilyId,
                publication = publication,
            )
        }
    }
}

/**
 * Governed sheet page size expressed in a renderer-neutral form.
 */
data class ProjectionSheetPageSize(
    val format: String,
    val orientation: String,
)

/**
 * Governed sheet frame semantics.
 */
data class ProjectionSheetFrame(
    val frameId: String,
    val style: String,
)

/**
 * Governed coordinate zone on the publication sheet.
 */
data class ProjectionSheetCoordinateZone(
    val zoneId: String,
    val label: String,
    val order: Int,
)

/**
 * Governed sheet title block semantics.
 */
data class ProjectionSheetTitleBlock(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
)

/**
 * Governed revision metadata for the publication sheet.
 */
data class ProjectionSheetRevisionMetadata(
    val revisionCode: String,
    val revisionNote: String,
)

/**
 * Governed view composition for the publication sheet.
 */
data class ProjectionSheetViewComposition(
    val primaryViewId: String,
    val primarySheetOrder: Int,
    val subjectSemanticIds: List<String> = emptyList(),
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
    val publication: ProjectionSheetPublication = ProjectionSheetPublication.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjects = subjects,
    ),
    val composition: ProjectionSheetComposition = ProjectionSheetComposition.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjects = subjects,
        publication = publication,
    ),
)
