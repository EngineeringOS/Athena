package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationPaintPlan
import com.engineeringood.athena.presentation.PresentationPhysicalSize
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationResolvedSubject
import com.engineeringood.athena.projection.ProjectionDocument

/**
 * Derives Presentation Reality from projection metadata that is still available before the full
 * Projection -> Spatial -> Presentation chain is introduced.
 *
 * It deliberately does not reconstruct geometry from Projection Reality. Placement, labels, anchors,
 * and routes must come through Spatial and Presentation ownership, not from stale projection fields.
 */
class PresentationModelDeriver {
    fun derive(
        document: EngineeringDocument,
        projection: ProjectionDocument,
        primitivePacks: List<PresentationPrimitivePack>,
        compositePacks: List<PresentationCompositePack>,
    ): PresentationDocument {
        val familyId = projection.view.familyContract.toPresentationFamilyId()
        val activePrimitivePacks = primitivePacks.filter { pack -> pack.familyIds.isEmpty() || familyId in pack.familyIds }
        val activeCompositePacks = compositePacks.filter { pack -> pack.familyIds.isEmpty() || familyId in pack.familyIds }
        return PresentationDocument(
            view = projection.view,
            canvasWidth = EMPTY_PRESENTATION_CANVAS_WIDTH,
            canvasHeight = EMPTY_PRESENTATION_CANVAS_HEIGHT,
            primitivePacks = activePrimitivePacks.sortedBy { pack -> pack.packId.value },
            compositePacks = activeCompositePacks.sortedBy { pack -> pack.packId.value },
            resolvedSubjects = projection.resolvedSubjects.map { resolved -> resolved.toPresentationResolvedSubject() },
            occurrences = emptyList(),
            connectors = emptyList(),
            paintPlan = PresentationPaintPlan(emptyList()),
        )
    }
}

private fun com.engineeringood.athena.projection.ProjectionResolvedSubject.toPresentationResolvedSubject(): PresentationResolvedSubject {
    return PresentationResolvedSubject(
        semanticId = semanticId,
        conceptId = conceptId,
        classificationKeys = classificationKeys,
        implementationId = implementationId,
        vendorPartNumber = vendorPartNumber,
        physicalSize = physicalSize?.let { size ->
            PresentationPhysicalSize(
                widthMillimeters = size.widthMillimeters,
                heightMillimeters = size.heightMillimeters,
                depthMillimeters = size.depthMillimeters,
            )
        },
        mountingTypeId = mountingTypeId,
        installationMarkerIds = installationMarkerIds,
    )
}

private fun com.engineeringood.athena.layout.ProjectionFamilyContract?.toPresentationFamilyId(): String? {
    return when (this) {
        is com.engineeringood.athena.layout.ElectricalProjectionDescriptor -> "electrical/${family.name.lowercase()}"
        null -> null
    }
}

private const val EMPTY_PRESENTATION_CANVAS_WIDTH = 1
private const val EMPTY_PRESENTATION_CANVAS_HEIGHT = 1
