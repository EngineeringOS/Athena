package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint

class DrawingSheetStructureCompiler {
    fun compile(request: DrawingSheetStructureRequest): DrawingSheetStructureResult {
        val diagnostics = validate(request)
        if (diagnostics.isNotEmpty()) return failed(diagnostics)

        val terminalStrips = request.terminalStrips.map { intent ->
            val memberBounds = intent.subjectIds.map { id -> request.subjects.first { it.subjectId == id }.bounds }
            DrawingSheetTerminalStripFact(
                stripId = intent.stripId,
                bounds = union(memberBounds).expand(request.policy.terminalStripPadding),
                subjectIds = intent.subjectIds.sorted(),
                membershipAuthority = STRUCTURE_INTENT_AUTHORITY,
                boundsAuthority = BOUNDS_AUTHORITY,
            )
        }.sortedBy { it.stripId }
        val derivedDiagnostics = buildList {
            terminalStrips.filterNot { it.bounds.isValid() }.forEach { strip ->
                add(diagnostic("drawing.structure.bounds.derived-invalid", BOUNDS_AUTHORITY, strip.stripId, "Derived terminal-strip bounds must remain finite with positive extents."))
            }
            terminalStrips.filter { it.bounds.isValid() && !request.sheetPlan.drawingAreaBounds.contains(it.bounds) }.forEach { strip ->
                add(diagnostic("drawing.structure.content.out-of-sheet", BOUNDS_AUTHORITY, strip.stripId, "Derived terminal-strip bounds must remain inside the drawing area."))
            }
        }
        if (derivedDiagnostics.isNotEmpty()) return failed(derivedDiagnostics)

        val subjects = request.subjects.sortedBy { it.subjectId }.map { subject ->
            DrawingSheetStructureSubjectFact(
                subjectId = subject.subjectId,
                representationIdentity = subject.representationIdentity,
                bounds = subject.bounds,
                anchors = subject.anchors.sortedBy { it.anchorId.value }.map { DrawingSheetStructureAnchorFact(it.anchorId, it.point) },
                labels = subject.labels.sortedBy { it.labelId }.map { DrawingSheetStructureLabelFact(it.labelId, it.slotId, it.bounds) },
                representationAuthority = REPRESENTATION_AUTHORITY,
                boundsAuthority = PRIMITIVE_BOUNDS_AUTHORITY,
            )
        }
        val plan = DrawingSheetStructurePlan(
            sheetId = request.sheetPlan.sheetId,
            subjects = subjects,
            rails = request.rails.sortedBy { it.railId }.map {
                DrawingSheetRailFact(it.railId, it.axis, it.start, it.end, it.subjectIds.sorted(), STRUCTURE_INTENT_AUTHORITY)
            },
            lanes = request.lanes.sortedBy { it.laneId }.map {
                DrawingSheetLaneFact(it.laneId, it.axis, it.bounds, it.subjectIds.sorted(), STRUCTURE_INTENT_AUTHORITY)
            },
            terminalStrips = terminalStrips,
            labelBands = request.labelBands.sortedBy { it.bandId }.map {
                DrawingSheetLabelBandFact(it.bandId, it.bounds, it.labelIds.sorted(), STRUCTURE_INTENT_AUTHORITY)
            },
            routeChannels = request.routeChannels.sortedBy { it.channelId }.map {
                DrawingSheetRouteChannelFact(
                    it.channelId,
                    it.axis,
                    it.bounds,
                    it.anchorReferences.sortedWith(compareBy(DrawingSheetAnchorReference::subjectId, { reference -> reference.anchorId.value })),
                    STRUCTURE_INTENT_AUTHORITY,
                )
            },
        )
        return DrawingSheetStructureResult(
            plan = plan,
            evidence = DrawingSheetStructureEvidence(
                sheetId = plan.sheetId,
                policyId = request.policy.policyId,
                drawingAreaBounds = request.sheetPlan.drawingAreaBounds,
                subjectIds = plan.subjects.map { it.subjectId },
                railIds = plan.rails.map { it.railId },
                laneIds = plan.lanes.map { it.laneId },
                terminalStripIds = plan.terminalStrips.map { it.stripId },
                labelBandIds = plan.labelBands.map { it.bandId },
                routeChannelIds = plan.routeChannels.map { it.channelId },
                boundsAuthority = BOUNDS_AUTHORITY,
                representationAuthority = REPRESENTATION_AUTHORITY,
                structureIntentAuthority = STRUCTURE_INTENT_AUTHORITY,
                policyAuthority = POLICY_AUTHORITY,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun validate(request: DrawingSheetStructureRequest): List<DrawingSheetStructureDiagnostic> {
        val diagnostics = mutableListOf<DrawingSheetStructureDiagnostic>()
        val area = request.sheetPlan.drawingAreaBounds
        if (request.sheetPlan.sheetId.isBlank() || !area.isValid()) {
            diagnostics += diagnostic("drawing.structure.sheet.invalid", BOUNDS_AUTHORITY, request.sheetPlan.sheetId, "Story 4.1 sheet id and drawing-area bounds must be valid.")
        }
        val policy = request.policy
        if (policy.policyId.isBlank() || !policy.terminalStripPadding.isFinite() || policy.terminalStripPadding < 0.0 ||
            !policy.maximumSubjectGap.isFinite() || policy.maximumSubjectGap < 0.0
        ) {
            diagnostics += diagnostic("drawing.structure.policy.invalid", POLICY_AUTHORITY, policy.policyId, "Structure policy id, terminal-strip padding, and maximum subject gap must be valid.")
        }
        val collections = listOf(
            "subjects" to request.subjects,
            "rails" to request.rails,
            "lanes" to request.lanes,
            "terminalStrips" to request.terminalStrips,
            "labelBands" to request.labelBands,
            "routeChannels" to request.routeChannels,
        )
        collections.filter { it.second.isEmpty() }.forEach { (name, _) ->
            diagnostics += diagnostic("drawing.structure.collection.empty", STRUCTURE_INTENT_AUTHORITY, name, "Professional structure input must not omit required fact collections.")
        }
        diagnostics += duplicateDiagnostics("subject", request.subjects.map { it.subjectId })
        diagnostics += duplicateDiagnostics("rail", request.rails.map { it.railId })
        diagnostics += duplicateDiagnostics("lane", request.lanes.map { it.laneId })
        diagnostics += duplicateDiagnostics("terminal-strip", request.terminalStrips.map { it.stripId })
        diagnostics += duplicateDiagnostics("label-band", request.labelBands.map { it.bandId })
        diagnostics += duplicateDiagnostics("route-channel", request.routeChannels.map { it.channelId })
        if (diagnostics.isNotEmpty()) return ordered(diagnostics)

        val subjectsById = request.subjects.associateBy { it.subjectId }
        val labelsById = request.subjects.flatMap { it.labels }.associateBy { it.labelId }
        request.subjects.forEach { subject ->
            if (subject.subjectId.isBlank() || !subject.bounds.isValid()) {
                diagnostics += diagnostic("drawing.structure.bounds.invalid", PRIMITIVE_BOUNDS_AUTHORITY, subject.subjectId, "Subject id and bounds must be valid and finite.")
            }
            val anchorIds = subject.anchors.map { it.anchorId }
            if (anchorIds.distinct().size != anchorIds.size || subject.anchors.any { !it.point.isFinite() }) {
                diagnostics += diagnostic("drawing.structure.anchor.invalid", REPRESENTATION_AUTHORITY, subject.subjectId, "Subject anchors must be unique and finite.")
            }
            val missingAnchors = subject.requiredAnchorIds - anchorIds.toSet()
            missingAnchors.forEach { anchorId ->
                diagnostics += diagnostic("drawing.structure.anchor.required-missing", REPRESENTATION_AUTHORITY, "${subject.subjectId}:${anchorId.value}", "Required package-backed anchor is missing.")
            }
            val labelIds = subject.labels.map { it.labelId }
            val slotIds = subject.labels.map { it.slotId }
            if (labelIds.any(String::isBlank) || labelIds.distinct().size != labelIds.size || slotIds.distinct().size != slotIds.size || subject.labels.any { !it.bounds.isValid() }) {
                diagnostics += diagnostic("drawing.structure.label.invalid", REPRESENTATION_AUTHORITY, subject.subjectId, "Subject labels require unique ids/slots and valid bounds.")
            }
            val missingSlots = subject.requiredLabelSlotIds - slotIds.toSet()
            missingSlots.forEach { slotId ->
                diagnostics += diagnostic("drawing.structure.label-slot.required-missing", REPRESENTATION_AUTHORITY, "${subject.subjectId}:${slotId.value}", "Required package-backed label slot is missing.")
            }
        }
        if (labelsById.size != request.subjects.sumOf { it.labels.size }) {
            diagnostics += diagnostic("drawing.structure.label.duplicate", REPRESENTATION_AUTHORITY, "labels", "Label ids must be unique across the sheet.")
        }

        request.rails.forEach { rail ->
            if (rail.railId.isBlank() || !rail.start.isFinite() || !rail.end.isFinite() || rail.start == rail.end) {
                diagnostics += diagnostic("drawing.structure.rail.invalid", STRUCTURE_INTENT_AUTHORITY, rail.railId, "Rail id and finite distinct endpoints are required.")
            }
            diagnostics += missingMembers(rail.railId, rail.subjectIds, subjectsById.keys)
            diagnostics += duplicateMembers(rail.railId, rail.subjectIds)
        }
        request.lanes.forEach { lane ->
            if (lane.laneId.isBlank() || !lane.bounds.isValid()) diagnostics += invalidBounds(lane.laneId)
            diagnostics += missingMembers(lane.laneId, lane.subjectIds, subjectsById.keys)
            diagnostics += duplicateMembers(lane.laneId, lane.subjectIds)
        }
        request.terminalStrips.forEach { strip ->
            if (strip.stripId.isBlank() || strip.subjectIds.isEmpty()) {
                diagnostics += diagnostic("drawing.structure.terminal-strip.invalid", STRUCTURE_INTENT_AUTHORITY, strip.stripId, "Terminal strip id and at least one member are required.")
            }
            diagnostics += missingMembers(strip.stripId, strip.subjectIds, subjectsById.keys)
            diagnostics += duplicateMembers(strip.stripId, strip.subjectIds)
        }
        request.labelBands.forEach { band ->
            if (band.bandId.isBlank() || !band.bounds.isValid()) diagnostics += invalidBounds(band.bandId)
            diagnostics += missingMembers(band.bandId, band.labelIds, labelsById.keys)
            diagnostics += duplicateMembers(band.bandId, band.labelIds)
        }
        request.routeChannels.forEach { channel ->
            if (channel.channelId.isBlank() || !channel.bounds.isValid() || channel.anchorReferences.isEmpty()) {
                diagnostics += diagnostic("drawing.structure.route-channel.invalid", STRUCTURE_INTENT_AUTHORITY, channel.channelId, "Route channel id, bounds, and anchor references are required.")
            }
            channel.anchorReferences.forEach { reference ->
                val subject = subjectsById[reference.subjectId]
                if (subject == null || subject.anchors.none { it.anchorId == reference.anchorId }) {
                    diagnostics += diagnostic("drawing.structure.member.missing", REPRESENTATION_AUTHORITY, "${channel.channelId}:${reference.subjectId}:${reference.anchorId.value}", "Route channel references an unknown package-backed anchor.")
                }
            }
            diagnostics += duplicateMembers(
                channel.channelId,
                channel.anchorReferences.map { "${it.subjectId}:${it.anchorId.value}" },
            )
        }
        if (diagnostics.isNotEmpty()) return ordered(diagnostics)

        diagnostics += membershipDiagnostics("lane", subjectsById.keys, request.lanes.flatMap { it.subjectIds })
        diagnostics += membershipDiagnostics("label-band", labelsById.keys, request.labelBands.flatMap { it.labelIds })
        diagnostics += containmentDiagnostics(request, labelsById)
        diagnostics += collisionDiagnostics(request, subjectsById, labelsById)
        diagnostics += whitespaceDiagnostics(request, subjectsById)
        diagnostics += labelOverflowDiagnostics(request, labelsById)
        return ordered(diagnostics)
    }

    private fun containmentDiagnostics(
        request: DrawingSheetStructureRequest,
        labelsById: Map<String, DrawingSheetStructureLabelInput>,
    ): List<DrawingSheetStructureDiagnostic> = buildList {
        val area = request.sheetPlan.drawingAreaBounds
        request.subjects.filterNot { area.contains(it.bounds) }.forEach { add(outOfSheet(it.subjectId)) }
        request.rails.filterNot { area.contains(it.start) && area.contains(it.end) }.forEach { add(outOfSheet(it.railId)) }
        request.lanes.filterNot { area.contains(it.bounds) }.forEach { add(outOfSheet(it.laneId)) }
        request.labelBands.filterNot { area.contains(it.bounds) }.forEach { add(outOfSheet(it.bandId)) }
        request.routeChannels.filterNot { area.contains(it.bounds) }.forEach { add(outOfSheet(it.channelId)) }
        labelsById.values.filterNot { area.contains(it.bounds) }.forEach { add(outOfSheet(it.labelId)) }
    }

    private fun collisionDiagnostics(
        request: DrawingSheetStructureRequest,
        subjectsById: Map<String, DrawingSheetStructureSubjectInput>,
        labelsById: Map<String, DrawingSheetStructureLabelInput>,
    ): List<DrawingSheetStructureDiagnostic> = buildList {
        request.lanes.forEach { lane ->
            pairs(lane.subjectIds.mapNotNull(subjectsById::get).sortedBy { it.subjectId }).filter { (a, b) -> a.bounds.overlaps(b.bounds) }.forEach { (a, b) ->
                add(diagnostic("drawing.structure.subject.collision", BOUNDS_AUTHORITY, "${a.subjectId}|${b.subjectId}", "Subjects in the same lane must not overlap."))
            }
        }
        request.labelBands.forEach { band ->
            pairs(band.labelIds.mapNotNull(labelsById::get).sortedBy { it.labelId }).filter { (a, b) -> a.bounds.overlaps(b.bounds) }.forEach { (a, b) ->
                add(diagnostic("drawing.structure.label.collision", BOUNDS_AUTHORITY, "${a.labelId}|${b.labelId}", "Labels in the same band must not overlap."))
            }
        }
    }

    private fun whitespaceDiagnostics(
        request: DrawingSheetStructureRequest,
        subjectsById: Map<String, DrawingSheetStructureSubjectInput>,
    ): List<DrawingSheetStructureDiagnostic> = buildList {
        request.lanes.forEach { lane ->
            val orderedSubjects = lane.subjectIds.mapNotNull(subjectsById::get).sortedBy { if (lane.axis == DrawingSheetAxis.HORIZONTAL) it.bounds.x else it.bounds.y }
            orderedSubjects.zipWithNext().forEach { (left, right) ->
                val gap = if (lane.axis == DrawingSheetAxis.HORIZONTAL) right.bounds.x - left.bounds.right else right.bounds.y - left.bounds.bottom
                if (gap > request.policy.maximumSubjectGap) {
                    add(diagnostic("drawing.structure.whitespace.excessive", POLICY_AUTHORITY, "${lane.laneId}:${left.subjectId}|${right.subjectId}", "Neighboring subject gap exceeds the presentation policy maximum."))
                }
            }
        }
    }

    private fun labelOverflowDiagnostics(
        request: DrawingSheetStructureRequest,
        labelsById: Map<String, DrawingSheetStructureLabelInput>,
    ): List<DrawingSheetStructureDiagnostic> = buildList {
        request.labelBands.forEach { band ->
            band.labelIds.mapNotNull(labelsById::get).filterNot { band.bounds.contains(it.bounds) }.forEach { label ->
                add(diagnostic("drawing.structure.label.overflow", BOUNDS_AUTHORITY, "${band.bandId}:${label.labelId}", "Label bounds must remain inside the declared label band."))
            }
        }
    }

    private fun duplicateDiagnostics(kind: String, ids: List<String>): List<DrawingSheetStructureDiagnostic> =
        ids.groupingBy { it }.eachCount().filter { it.key.isBlank() || it.value > 1 }.keys.map { id ->
            diagnostic("drawing.structure.$kind.duplicate", STRUCTURE_INTENT_AUTHORITY, id, "$kind ids must be non-blank and unique.")
        }

    private fun missingMembers(owner: String, members: List<String>, available: Set<String>): List<DrawingSheetStructureDiagnostic> =
        members.filterNot(available::contains).distinct().map { member ->
            diagnostic("drawing.structure.member.missing", STRUCTURE_INTENT_AUTHORITY, "$owner:$member", "Structure intent references an unknown member.")
        }

    private fun duplicateMembers(owner: String, members: List<String>): List<DrawingSheetStructureDiagnostic> =
        members.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.map { member ->
            diagnostic("drawing.structure.member.duplicate", STRUCTURE_INTENT_AUTHORITY, "$owner:$member", "Structure intent members must be unique.")
        }

    private fun membershipDiagnostics(kind: String, expected: Set<String>, memberships: List<String>): List<DrawingSheetStructureDiagnostic> =
        expected.filter { id -> memberships.count { it == id } != 1 }.map { id ->
            diagnostic("drawing.structure.membership.invalid", STRUCTURE_INTENT_AUTHORITY, "$kind:$id", "Each fact must belong to exactly one $kind.")
        }

    private fun invalidBounds(subject: String) =
        diagnostic("drawing.structure.bounds.invalid", STRUCTURE_INTENT_AUTHORITY, subject, "Structure bounds must be finite with positive extents.")

    private fun outOfSheet(subject: String) =
        diagnostic("drawing.structure.content.out-of-sheet", BOUNDS_AUTHORITY, subject, "Structure fact must remain inside the drawing area.")

    private fun failed(diagnostics: List<DrawingSheetStructureDiagnostic>) =
        DrawingSheetStructureResult(null, null, ordered(diagnostics))

    private fun ordered(diagnostics: List<DrawingSheetStructureDiagnostic>) = diagnostics.distinct().sortedWith(
        compareBy({ it.code }, { it.authority }, { it.subject }, { it.message }),
    )

    private fun diagnostic(code: String, authority: String, subject: String, message: String) =
        DrawingSheetStructureDiagnostic(code, authority, subject.ifBlank { "unknown" }, message)

    private fun GraphicBounds.isValid(): Boolean =
        x.isFinite() && y.isFinite() && width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0 && right.isFinite() && bottom.isFinite()

    private fun GraphicPoint.isFinite() = x.isFinite() && y.isFinite()
    private val GraphicBounds.right get() = x + width
    private val GraphicBounds.bottom get() = y + height

    private fun GraphicBounds.contains(point: GraphicPoint) = point.x in x..right && point.y in y..bottom
    private fun GraphicBounds.contains(other: GraphicBounds) = other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom
    private fun GraphicBounds.overlaps(other: GraphicBounds) = x < other.right && other.x < right && y < other.bottom && other.y < bottom

    private fun union(bounds: List<GraphicBounds>): GraphicBounds {
        val left = bounds.minOf { it.x }
        val top = bounds.minOf { it.y }
        val right = bounds.maxOf { it.right }
        val bottom = bounds.maxOf { it.bottom }
        return GraphicBounds(left, top, right - left, bottom - top)
    }

    private fun GraphicBounds.expand(padding: Double) =
        GraphicBounds(x - padding, y - padding, width + padding * 2.0, height + padding * 2.0)

    private fun <T> pairs(values: List<T>): List<Pair<T, T>> = buildList {
        values.indices.forEach { left -> ((left + 1)..<values.size).forEach { right -> add(values[left] to values[right]) } }
    }

    private companion object {
        const val BOUNDS_AUTHORITY = "drawing-composition"
        const val REPRESENTATION_AUTHORITY = "drawing-symbol-anatomy"
        const val PRIMITIVE_BOUNDS_AUTHORITY = "graphic-primitive-ir"
        const val STRUCTURE_INTENT_AUTHORITY = "drawing-structure-intent"
        const val POLICY_AUTHORITY = "presentation-profile-policy"
    }
}
