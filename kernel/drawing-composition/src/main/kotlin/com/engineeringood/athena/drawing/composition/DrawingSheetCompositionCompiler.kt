package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.representation.GraphicBounds

class DrawingSheetCompositionCompiler {
    fun compile(request: DrawingSheetCompositionRequest): DrawingSheetCompositionResult {
        val diagnostics = validate(request)
        if (diagnostics.isNotEmpty()) return failed(diagnostics)

        val content = requireNotNull(request.contentBounds)
        val policy = request.policy
        val sheet = policy.fixedSheetBounds
        val frame = sheet?.let { fixed ->
            GraphicBounds(
                x = fixed.x + policy.frameToSheet,
                y = fixed.y + policy.frameToSheet,
                width = fixed.width - policy.frameToSheet * 2.0,
                height = fixed.height - policy.frameToSheet * 2.0,
            )
        } ?: GraphicBounds(
            x = content.x - policy.contentToFrame,
            y = content.y - policy.contentToFrame,
            width = content.width + policy.contentToFrame * 2.0,
            height = content.height + policy.contentToFrame * 2.0 + policy.titleBlockHeight,
        )
        val drawingArea = GraphicBounds(
            x = frame.x + policy.coordinateBandSize,
            y = frame.y + policy.coordinateBandSize,
            width = frame.width - policy.coordinateBandSize,
            height = frame.height - policy.titleBlockHeight - policy.coordinateBandSize,
        )
        val titleBlock = GraphicBounds(
            x = frame.x,
            y = frame.y + frame.height - policy.titleBlockHeight,
            width = frame.width,
            height = policy.titleBlockHeight,
        )
        val resolvedSheet = sheet ?: GraphicBounds(
            x = frame.x - policy.frameToSheet,
            y = frame.y - policy.frameToSheet,
            width = frame.width + policy.frameToSheet * 2.0,
            height = frame.height + policy.frameToSheet * 2.0,
        )
        val derivedBounds = listOf(frame, drawingArea, titleBlock, resolvedSheet)
        if (derivedBounds.any { !it.isValid() }) {
            return failed(
                listOf(
                    diagnostic(
                        "drawing.composition.bounds.derived-invalid",
                        BOUNDS_AUTHORITY,
                        request.sheetId.value,
                        "Derived drawing sheet bounds must remain finite with positive extents.",
                    ),
                ),
            )
        }
        if (resolvedSheet.width > policy.maximumSheetWidth || resolvedSheet.height > policy.maximumSheetHeight) {
            return failed(
                listOf(
                    diagnostic(
                        "drawing.composition.content.out-of-sheet",
                        POLICY_AUTHORITY,
                        request.sheetId.value,
                        "Derived sheet bounds exceed the presentation policy maximum dimensions.",
                    ),
                ),
            )
        }
        if (policy.fixedSheetBounds != null && !drawingArea.contains(content)) {
            return failed(
                listOf(
                    diagnostic(
                        "drawing.composition.content.out-of-sheet",
                        POLICY_AUTHORITY,
                        request.sheetId.value,
                        "Graphic Primitive content bounds must remain inside the fixed drawing area.",
                    ),
                ),
            )
        }

        val publication = request.publication
        val coordinateZones = coordinateZones(drawingArea, policy)
        if (coordinateZones.any { !it.bounds.isValid() }) {
            return failed(
                listOf(
                    diagnostic(
                        "drawing.composition.zone.bounds.derived-invalid",
                        POLICY_AUTHORITY,
                        request.sheetId.value,
                        "Derived coordinate-zone bounds must remain finite with positive extents.",
                    ),
                ),
            )
        }
        val plan = DrawingSheetCompositionPlan(
            sheetId = request.sheetId.value,
            sheetBounds = resolvedSheet,
            frame = DrawingSheetFrameFact(
                frameId = publication.frame.frameId,
                style = publication.frame.style,
                bounds = frame,
                metadataAuthority = PROJECTION_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
            ),
            drawingAreaBounds = drawingArea,
            titleBlock = DrawingSheetTitleBlockFact(
                bounds = titleBlock,
                sheetTitle = publication.titleBlock.sheetTitle,
                sheetFamily = publication.titleBlock.sheetFamily,
                sheetNumber = publication.titleBlock.sheetNumber,
                revisionCode = publication.revisionMetadata.revisionCode,
                revisionNote = publication.revisionMetadata.revisionNote,
                pageFormat = publication.pageSize.format,
                orientation = publication.pageSize.orientation,
                metadataAuthority = PROJECTION_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
                fields = titleFields(request.titleFields, titleBlock),
            ),
            namedZones = publication.coordinateZones
                .sortedWith(compareBy(ProjectionSheetCoordinateZone::order, ProjectionSheetCoordinateZone::zoneId))
                .map { zone -> DrawingSheetNamedZoneFact(zone.zoneId, zone.label, zone.order, PROJECTION_AUTHORITY) },
            coordinateZones = coordinateZones,
            margins = DrawingSheetMarginFact(policy.contentToFrame, policy.frameToSheet, POLICY_AUTHORITY),
        )
        return DrawingSheetCompositionResult(
            plan = plan,
            proof = DrawingSheetCompositionProof(
                policyId = policy.policyId,
                contentBounds = content,
                frameBounds = frame,
                drawingAreaBounds = drawingArea,
                titleBlockBounds = titleBlock,
                sheetBounds = resolvedSheet,
                namedZoneIds = plan.namedZones.map { it.zoneId },
                coordinateZoneIds = coordinateZones.map { it.zoneId },
                contentBoundsAuthority = CONTENT_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
                projectionAuthority = PROJECTION_AUTHORITY,
                policyAuthority = POLICY_AUTHORITY,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun validate(request: DrawingSheetCompositionRequest): List<DrawingSheetCompositionDiagnostic> {
        val diagnostics = mutableListOf<DrawingSheetCompositionDiagnostic>()
        if (request.sheetId.value.isBlank()) {
            diagnostics += diagnostic("drawing.composition.sheet-id.invalid", PROJECTION_AUTHORITY, "sheetId", "Sheet id must not be blank.")
        }
        if (request.contentBounds == null) {
            diagnostics += diagnostic(
                "drawing.composition.content-bounds.missing",
                CONTENT_AUTHORITY,
                request.sheetId.value.ifBlank { "contentBounds" },
                "Graphic Primitive content bounds are required for sheet composition.",
            )
        } else if (!request.contentBounds.isValid()) {
            diagnostics += diagnostic(
                "drawing.composition.content-bounds.invalid",
                CONTENT_AUTHORITY,
                request.sheetId.value.ifBlank { "contentBounds" },
                "Graphic Primitive content bounds must be finite with positive extents.",
            )
        }
        diagnostics += validatePolicy(request)
        diagnostics += validatePublication(request)
        return ordered(diagnostics)
    }

    private fun validatePolicy(request: DrawingSheetCompositionRequest): List<DrawingSheetCompositionDiagnostic> = buildList {
        val policy = request.policy
        if (policy.policyId.isBlank()) {
            add(diagnostic("drawing.composition.policy.id.invalid", POLICY_AUTHORITY, "policyId", "Policy id must not be blank."))
        }
        if (!policy.contentToFrame.isFinite() || policy.contentToFrame < 0.0 ||
            !policy.frameToSheet.isFinite() || policy.frameToSheet < 0.0 ||
            !policy.coordinateBandSize.isFinite() || policy.coordinateBandSize < 0.0
        ) {
            add(diagnostic("drawing.composition.policy.margin.invalid", POLICY_AUTHORITY, policy.policyId.ifBlank { "margins" }, "Sheet margins must be finite and non-negative."))
        }
        if (!policy.titleBlockHeight.isFinite() || policy.titleBlockHeight <= 0.0) {
            add(diagnostic("drawing.composition.policy.title-block.invalid", POLICY_AUTHORITY, policy.policyId.ifBlank { "titleBlockHeight" }, "Title-block height must be finite and positive."))
        }
        if (!policy.maximumSheetWidth.isFinite() || policy.maximumSheetWidth <= 0.0 ||
            !policy.maximumSheetHeight.isFinite() || policy.maximumSheetHeight <= 0.0
        ) {
            add(diagnostic("drawing.composition.policy.maximum-sheet.invalid", POLICY_AUTHORITY, policy.policyId.ifBlank { "maximumSheet" }, "Maximum sheet dimensions must be finite and positive."))
        }
        if (policy.fixedSheetBounds?.isValid() == false) {
            add(diagnostic("drawing.composition.policy.fixed-sheet.invalid", POLICY_AUTHORITY, policy.policyId.ifBlank { "fixedSheetBounds" }, "Fixed sheet bounds must be finite with positive extents."))
        }
        val labels = policy.columnLabels + policy.rowLabels
        if (policy.columnLabels.isEmpty() || policy.rowLabels.isEmpty() || labels.any(String::isBlank)) {
            add(diagnostic("drawing.composition.policy.zone-label.invalid", POLICY_AUTHORITY, policy.policyId.ifBlank { "zoneLabels" }, "Column and row zone labels must be non-empty and non-blank."))
        }
        if (policy.columnLabels.distinct().size != policy.columnLabels.size ||
            policy.rowLabels.distinct().size != policy.rowLabels.size
        ) {
            add(diagnostic("drawing.composition.policy.zone-label.duplicate", POLICY_AUTHORITY, policy.policyId.ifBlank { "zoneLabels" }, "Zone labels must be unique within each axis."))
        }
        val titleFields = request.titleFields
        if (titleFields.any { field -> field.fieldId.isBlank() || field.label.isBlank() || field.value.isBlank() }) {
            add(diagnostic("drawing.composition.title-field.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "titleFields" }, "Title fields require non-blank identity, label, and value."))
        }
        if (titleFields.map { field -> field.fieldId }.distinct().size != titleFields.size) {
            add(diagnostic("drawing.composition.title-field.duplicate", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "titleFields" }, "Title field identities must be unique."))
        }
    }

    private fun titleFields(
        fields: List<DrawingSheetTitleFieldInput>,
        titleBlock: GraphicBounds,
    ): List<DrawingSheetTitleFieldFact> {
        if (fields.isEmpty()) return emptyList()
        val fieldWidth = titleBlock.width / fields.size
        return fields.mapIndexed { index, field ->
            DrawingSheetTitleFieldFact(
                fieldId = field.fieldId,
                label = field.label,
                value = field.value,
                bounds = GraphicBounds(
                    x = titleBlock.x + fieldWidth * index,
                    y = titleBlock.y,
                    width = fieldWidth,
                    height = titleBlock.height,
                ),
                metadataAuthority = PROJECTION_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
            )
        }
    }

    private fun validatePublication(request: DrawingSheetCompositionRequest): List<DrawingSheetCompositionDiagnostic> = buildList {
        val publication = request.publication
        if (publication.pageSize.format.isBlank() || publication.pageSize.orientation.isBlank()) {
            add(diagnostic("drawing.composition.projection.page-size.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "pageSize" }, "Projection page format and orientation must not be blank."))
        }
        if (publication.frame.frameId.isBlank() || publication.frame.style.isBlank()) {
            add(diagnostic("drawing.composition.projection.frame.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "frame" }, "Projection frame id and style must not be blank."))
        }
        val title = publication.titleBlock
        if (title.sheetTitle.isBlank() || title.sheetFamily.isBlank() || title.sheetNumber.isBlank()) {
            add(diagnostic("drawing.composition.projection.title-block.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "titleBlock" }, "Projection title-block fields must not be blank."))
        }
        val revision = publication.revisionMetadata
        if (revision.revisionCode.isBlank() || revision.revisionNote.isBlank()) {
            add(diagnostic("drawing.composition.projection.revision.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "revision" }, "Projection revision fields must not be blank."))
        }
        val zones = publication.coordinateZones
        if (zones.isEmpty() || zones.any { it.zoneId.isBlank() || it.label.isBlank() || it.order < 0 }) {
            add(diagnostic("drawing.composition.projection.zone.invalid", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "zones" }, "Projection named zones require id, label, and non-negative order."))
        }
        if (zones.map { it.zoneId }.distinct().size != zones.size || zones.map { it.order }.distinct().size != zones.size) {
            add(diagnostic("drawing.composition.projection.zone.duplicate", PROJECTION_AUTHORITY, request.sheetId.value.ifBlank { "zones" }, "Projection named zone ids and orders must be unique."))
        }
    }

    private fun coordinateZones(
        drawingArea: GraphicBounds,
        policy: DrawingSheetCompositionPolicy,
    ): List<DrawingSheetCoordinateZoneFact> = buildList {
        val columnWidth = drawingArea.width / policy.columnLabels.size
        policy.columnLabels.forEachIndexed { index, label ->
            add(
                DrawingSheetCoordinateZoneFact(
                    zoneId = "column:$label",
                    axis = DrawingSheetZoneAxis.COLUMN,
                    label = label,
                    order = index,
                    bounds = GraphicBounds(drawingArea.x + columnWidth * index, drawingArea.y, columnWidth, drawingArea.height),
                    labelAuthority = POLICY_AUTHORITY,
                    boundsAuthority = BOUNDS_AUTHORITY,
                ),
            )
        }
        val rowHeight = drawingArea.height / policy.rowLabels.size
        policy.rowLabels.forEachIndexed { index, label ->
            add(
                DrawingSheetCoordinateZoneFact(
                    zoneId = "row:$label",
                    axis = DrawingSheetZoneAxis.ROW,
                    label = label,
                    order = index,
                    bounds = GraphicBounds(drawingArea.x, drawingArea.y + rowHeight * index, drawingArea.width, rowHeight),
                    labelAuthority = POLICY_AUTHORITY,
                    boundsAuthority = BOUNDS_AUTHORITY,
                ),
            )
        }
    }

    private fun failed(diagnostics: List<DrawingSheetCompositionDiagnostic>) = DrawingSheetCompositionResult(
        plan = null,
        proof = null,
        diagnostics = ordered(diagnostics),
    )

    private fun ordered(diagnostics: List<DrawingSheetCompositionDiagnostic>) = diagnostics.distinct().sortedWith(
        compareBy({ it.code }, { it.authority }, { it.subject }, { it.message }),
    )

    private fun diagnostic(code: String, authority: String, subject: String, message: String) =
        DrawingSheetCompositionDiagnostic(code, authority, subject.ifBlank { "unknown" }, message)

    private fun GraphicBounds.isValid(): Boolean =
        x.isFinite() && y.isFinite() && width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0

    private fun GraphicBounds.contains(other: GraphicBounds): Boolean =
        other.x >= x && other.y >= y &&
            other.x + other.width <= x + width &&
            other.y + other.height <= y + height

    private companion object {
        const val CONTENT_AUTHORITY = "graphic-primitive-ir"
        const val BOUNDS_AUTHORITY = "drawing-composition"
        const val PROJECTION_AUTHORITY = "projection-sheet-publication"
        const val POLICY_AUTHORITY = "presentation-profile-policy"
    }
}
