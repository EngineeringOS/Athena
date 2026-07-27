package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceLink
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint

class DrawingSheetReferenceCompiler {
    fun compile(request: DrawingSheetReferenceRequest): DrawingSheetReferenceResult {
        val diagnostics = validate(request)
        if (diagnostics.isNotEmpty()) return failed(diagnostics)

        val targets = request.references
            .flatMap { reference -> reference.links.map { link -> reference to link } }
            .sortedWith(compareBy({ it.first.crossReferenceId.value }, { it.second.sourceOccurrenceId }, { it.second.targetOccurrenceId }))
            .map { (reference, link) ->
                DrawingSheetReferenceTargetFact(
                    targetId = "${reference.crossReferenceId.value}:${link.sourceOccurrenceId}->${link.targetOccurrenceId}",
                    referenceId = reference.crossReferenceId.value,
                    semanticSubjectId = reference.semanticId.value,
                    sourceSheetId = link.sourceSheetId.value,
                    targetSheetId = link.targetSheetId.value,
                    sourceOccurrenceId = link.sourceOccurrenceId,
                    targetOccurrenceId = link.targetOccurrenceId,
                    compactNotation = link.compactNotation,
                    authority = PROJECTION_AUTHORITY,
                )
            }
        val referencesById = request.references.associateBy { it.crossReferenceId }
        val placements = request.placements.sortedBy { it.placementId }.map { placement ->
            val reference = requireNotNull(referencesById[placement.crossReferenceId])
            val link = reference.links.first { it.sourceOccurrenceId == placement.linkSourceOccurrenceId }
            DrawingSheetReferencePlacementFact(
                placementId = placement.placementId,
                referenceId = reference.crossReferenceId.value,
                occurrenceId = placement.occurrenceId,
                subjectId = placement.subjectId,
                role = placement.role,
                representationIdentity = placement.representationIdentity,
                bounds = placement.bounds,
                anchorId = placement.anchorId,
                anchorPoint = placement.anchorPoint,
                labelSlotId = placement.labelSlotId,
                sheetReferenceSlotId = placement.sheetReferenceSlotId,
                zoneReferenceSlotId = placement.zoneReferenceSlotId,
                availableReferenceSlotIds = placement.availableReferenceSlotIds.sortedBy { it.value },
                zoneId = placement.zoneId,
                compactNotation = link.compactNotation,
                projectionAuthority = PROJECTION_AUTHORITY,
                representationAuthority = REPRESENTATION_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
            )
        }
        val plan = DrawingSheetReferencePlan(request.sheetPlan.sheetId, targets, placements)
        return DrawingSheetReferenceResult(
            plan = plan,
            proof = DrawingSheetReferenceProof(
                sheetId = plan.sheetId,
                referenceIds = targets.map { it.referenceId }.distinct(),
                placementIds = placements.map { it.placementId },
                markerRepresentationIdentities = placements.map { it.representationIdentity.value }.distinct().sorted(),
                projectionAuthority = PROJECTION_AUTHORITY,
                representationAuthority = REPRESENTATION_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
                structureAuthority = STRUCTURE_AUTHORITY,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun validate(request: DrawingSheetReferenceRequest): List<DrawingSheetReferenceDiagnostic> {
        val diagnostics = mutableListOf<DrawingSheetReferenceDiagnostic>()
        if (request.sheetPlan.sheetId.isBlank() || request.sheetPlan.sheetId != request.structurePlan.sheetId ||
            !request.sheetPlan.drawingAreaBounds.isValid()
        ) {
            diagnostics += diagnostic("drawing.reference.sheet.invalid", BOUNDS_AUTHORITY, request.sheetPlan.sheetId, "Sheet and structure plans must identify the same valid drawing area.")
        }
        if (request.references.isEmpty() || request.placements.isEmpty()) {
            diagnostics += diagnostic("drawing.reference.collection.empty", STRUCTURE_AUTHORITY, "references", "Reference proof requires projection links and placements.")
        }
        duplicateIds(request.references.map { it.crossReferenceId.value }).forEach { id ->
            diagnostics += diagnostic("drawing.reference.projection.duplicate", PROJECTION_AUTHORITY, id, "Projection cross-reference ids must be unique.")
        }
        duplicateIds(request.placements.map { it.placementId }).forEach { id ->
            diagnostics += diagnostic("drawing.reference.placement.duplicate", STRUCTURE_AUTHORITY, id, "Reference placement ids must be unique.")
        }
        request.references.forEach { reference -> diagnostics += validateReference(reference) }
        if (diagnostics.isNotEmpty()) return ordered(diagnostics)

        val referencesById = request.references.associateBy { it.crossReferenceId }
        val subjectsById = request.structurePlan.subjects.associateBy { it.subjectId }
        val zoneIds = request.sheetPlan.namedZones.map { it.zoneId }.toSet()
        request.placements.forEach { placement ->
            val reference = referencesById[placement.crossReferenceId]
            val link = reference?.links?.singleOrNull { it.sourceOccurrenceId == placement.linkSourceOccurrenceId }
            if (reference == null || link == null) {
                diagnostics += diagnostic("drawing.reference.placement.link-mismatch", PROJECTION_AUTHORITY, placement.placementId, "Placement must resolve one typed projection link.")
                return@forEach
            }
            val expectedSheet = if (placement.role == DrawingSheetReferencePlacementRole.SOURCE) link.sourceSheetId.value else link.targetSheetId.value
            val expectedOccurrence = if (placement.role == DrawingSheetReferencePlacementRole.SOURCE) link.sourceOccurrenceId else link.targetOccurrenceId
            if (request.sheetPlan.sheetId != expectedSheet || placement.occurrenceId != expectedOccurrence || placement.subjectId != reference.semanticId.value) {
                diagnostics += diagnostic("drawing.reference.placement.link-mismatch", PROJECTION_AUTHORITY, placement.placementId, "Placement role, sheet, occurrence, and semantic subject must match the projection link.")
            }
            if (placement.subjectId !in subjectsById) {
                diagnostics += diagnostic("drawing.reference.subject.missing", STRUCTURE_AUTHORITY, placement.subjectId, "Reference placement subject must exist in the structure plan.")
            }
            val anatomyMissing = placement.anchorId !in placement.availableAnchorIds ||
                placement.labelSlotId !in placement.availableLabelSlotIds ||
                placement.sheetReferenceSlotId !in placement.availableReferenceSlotIds ||
                (placement.zoneReferenceSlotId != null && placement.zoneReferenceSlotId !in placement.availableReferenceSlotIds)
            if (anatomyMissing) {
                diagnostics += diagnostic("drawing.reference.anatomy.missing", REPRESENTATION_AUTHORITY, placement.placementId, "Selected anchor, label slot, and reference slots must exist in marker anatomy.")
            }
            if (placement.zoneId !in zoneIds) {
                diagnostics += diagnostic("drawing.reference.zone.missing", PROJECTION_AUTHORITY, placement.zoneId, "Placement zone must exist in projection sheet facts.")
            }
            if (!placement.bounds.isValid() || !placement.anchorPoint.isFinite() || !placement.bounds.contains(placement.anchorPoint)) {
                diagnostics += diagnostic("drawing.reference.bounds.invalid", BOUNDS_AUTHORITY, placement.placementId, "Marker bounds must be finite and contain the declared anchor point.")
            } else if (!request.sheetPlan.drawingAreaBounds.contains(placement.bounds)) {
                diagnostics += diagnostic("drawing.reference.content.out-of-sheet", BOUNDS_AUTHORITY, placement.placementId, "Marker placement must remain inside the drawing area.")
            }
        }
        if (diagnostics.isNotEmpty()) return ordered(diagnostics)

        diagnostics += completenessDiagnostics(request)
        diagnostics += collisionDiagnostics(request)
        return ordered(diagnostics)
    }

    private fun validateReference(reference: ProjectionCrossReference): List<DrawingSheetReferenceDiagnostic> = buildList {
        if (reference.links.isEmpty()) {
            add(diagnostic("drawing.reference.projection.malformed", PROJECTION_AUTHORITY, reference.crossReferenceId.value, "Projection cross-reference requires at least one link."))
            return@buildList
        }
        val linkKeys = reference.links.map { "${it.sourceSheetId.value}:${it.sourceOccurrenceId}->${it.targetSheetId.value}:${it.targetOccurrenceId}" }
        duplicateIds(linkKeys).forEach { key ->
            add(diagnostic("drawing.reference.projection.duplicate-link", PROJECTION_AUTHORITY, "${reference.crossReferenceId.value}:$key", "Projection links must be unique."))
        }
        reference.links.forEach { link ->
            val malformed = link.semanticId != reference.semanticId || link.sourceSheetId !in reference.sheetIds ||
                link.targetSheetId !in reference.sheetIds || link.sourceOccurrenceId !in reference.occurrenceIds ||
                link.targetOccurrenceId !in reference.occurrenceIds
            if (malformed) {
                add(diagnostic("drawing.reference.projection.malformed", PROJECTION_AUTHORITY, reference.crossReferenceId.value, "Projection link must preserve declared semantic, sheet, and occurrence identities."))
            }
            if (link.sourceSheetId == link.targetSheetId && link.sourceOccurrenceId == link.targetOccurrenceId) {
                add(diagnostic("drawing.reference.projection.cyclic", PROJECTION_AUTHORITY, reference.crossReferenceId.value, "A reference link cannot target its own sheet occurrence."))
            }
        }
    }

    private fun completenessDiagnostics(request: DrawingSheetReferenceRequest): List<DrawingSheetReferenceDiagnostic> = buildList {
        request.references.forEach { reference ->
            reference.links.forEach { link ->
                val expectedRoles = buildList {
                    if (request.sheetPlan.sheetId == link.sourceSheetId.value) add(DrawingSheetReferencePlacementRole.SOURCE)
                    if (request.sheetPlan.sheetId == link.targetSheetId.value) add(DrawingSheetReferencePlacementRole.TARGET)
                }
                expectedRoles.forEach { expectedRole ->
                    val count = request.placements.count {
                        it.crossReferenceId == reference.crossReferenceId &&
                            it.linkSourceOccurrenceId == link.sourceOccurrenceId && it.role == expectedRole
                    }
                    if (count != 1) {
                        add(diagnostic("drawing.reference.placement.incomplete", STRUCTURE_AUTHORITY, "${reference.crossReferenceId.value}:${link.sourceOccurrenceId}", "Each current-sheet projection link requires exactly one matching placement."))
                    }
                }
            }
        }
    }

    private fun collisionDiagnostics(request: DrawingSheetReferenceRequest): List<DrawingSheetReferenceDiagnostic> = buildList {
        val occupied = request.structurePlan.subjects.flatMap { subject -> listOf(subject.subjectId to subject.bounds) + subject.labels.map { it.labelId to it.bounds } }
        request.placements.forEach { placement ->
            occupied.filter { (_, bounds) -> placement.bounds.overlaps(bounds) }.forEach { (id, _) ->
                add(diagnostic("drawing.reference.placement.collision", BOUNDS_AUTHORITY, "${placement.placementId}:$id", "Reference marker must not overlap structure subjects or labels."))
            }
        }
        pairs(request.placements.sortedBy { it.placementId }).filter { (left, right) -> left.bounds.overlaps(right.bounds) }.forEach { (left, right) ->
            add(diagnostic("drawing.reference.placement.collision", BOUNDS_AUTHORITY, "${left.placementId}|${right.placementId}", "Reference marker placements must not overlap."))
        }
    }

    private fun duplicateIds(ids: List<String>): List<String> = ids.groupingBy { it }.eachCount().filter { it.key.isBlank() || it.value > 1 }.keys.sorted()

    private fun failed(diagnostics: List<DrawingSheetReferenceDiagnostic>) = DrawingSheetReferenceResult(null, null, ordered(diagnostics))

    private fun ordered(diagnostics: List<DrawingSheetReferenceDiagnostic>) = diagnostics.distinct().sortedWith(
        compareBy({ it.code }, { it.authority }, { it.subject }, { it.message }),
    )

    private fun diagnostic(code: String, authority: String, subject: String, message: String) =
        DrawingSheetReferenceDiagnostic(code, authority, subject.ifBlank { "unknown" }, message)

    private val GraphicBounds.right get() = x + width
    private val GraphicBounds.bottom get() = y + height
    private fun GraphicBounds.isValid() = x.isFinite() && y.isFinite() && width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0 && right.isFinite() && bottom.isFinite()
    private fun GraphicPoint.isFinite() = x.isFinite() && y.isFinite()
    private fun GraphicBounds.contains(point: GraphicPoint) = point.x in x..right && point.y in y..bottom
    private fun GraphicBounds.contains(other: GraphicBounds) = other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
    private fun GraphicBounds.overlaps(other: GraphicBounds) = x < other.right && other.x < right && y < other.bottom && other.y < bottom

    private fun <T> pairs(values: List<T>): List<Pair<T, T>> = buildList {
        values.indices.forEach { left -> ((left + 1)..<values.size).forEach { right -> add(values[left] to values[right]) } }
    }

    private companion object {
        const val PROJECTION_AUTHORITY = "projection-cross-reference"
        const val REPRESENTATION_AUTHORITY = "drawing-symbol-anatomy"
        const val BOUNDS_AUTHORITY = "drawing-composition"
        const val STRUCTURE_AUTHORITY = "drawing-structure-intent"
    }
}
