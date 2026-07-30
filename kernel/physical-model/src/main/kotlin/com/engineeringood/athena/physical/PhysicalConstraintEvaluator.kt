package com.engineeringood.athena.physical

enum class PhysicalConstraintEvaluationMode {
    ValidationOnly,
}

data class PhysicalRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int = x + width
    val bottom: Int = y + height

    fun contains(other: PhysicalRect): Boolean =
        other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom

    fun intersectsPositiveArea(other: PhysicalRect): Boolean =
        x < other.right && right > other.x && y < other.bottom && bottom > other.y

    fun inflate(clearance: PhysicalInstallationClearance): PhysicalRect = PhysicalRect(
        x = x - clearance.left.value,
        y = y - clearance.top.value,
        width = width + clearance.left.value + clearance.right.value,
        height = height + clearance.top.value + clearance.bottom.value,
    )
}

data class PhysicalConstraintEvaluationDiagnostic(
    val code: String,
    val subject: String,
    val span: PhysicalSourceSpan?,
    val measured: String?,
    val expected: String,
)

data class PhysicalConstraintEvaluationReport(
    val mode: PhysicalConstraintEvaluationMode,
    val surfaceCount: Int,
    val railCount: Int,
    val ductCount: Int,
    val channelCount: Int,
    val terminalGroupCount: Int,
    val occurrenceCount: Int,
    val diagnosticCount: Int,
)

sealed interface PhysicalConstraintEvaluation {
    data class Success(val evidence: PhysicalConstraintEvaluationReport) : PhysicalConstraintEvaluation

    data class Failure(
        val evidence: PhysicalConstraintEvaluationReport,
        val diagnostics: List<PhysicalConstraintEvaluationDiagnostic>,
    ) : PhysicalConstraintEvaluation
}

object PhysicalConstraintEvaluator {
    fun evaluate(ir: PhysicalInstallationIR): PhysicalConstraintEvaluation {
        val diagnostics = mutableListOf<PhysicalConstraintEvaluationDiagnostic>()
        val enclosure = ir.space.enclosure
        val enclosureRect = PhysicalRect(0, 0, enclosure.size.width, enclosure.size.height)
        val surfaces = ir.space.surfaces.associateBy { surface -> surface.id }
        val rails = ir.space.rails.associateBy { rail -> rail.id }
        val ducts = ir.space.ducts.associateBy { duct -> duct.id }
        val terminalGroups = ir.space.terminalGroups.associateBy { group -> group.id }
        val occurrenceRects = mutableListOf<Pair<PhysicalMountedOccurrence, PhysicalRect>>()

        ir.space.surfaces.forEach { surface ->
            val rect = rect(surface.at, surface.size)
            if (!enclosureRect.contains(rect)) {
                diagnostics += diagnostic(
                    code = "physical.constraint.surface.outside_enclosure",
                    subject = surface.id.value,
                    span = surface.provenance.span,
                    measured = rect.toMeasured(),
                    expected = "inside enclosure ${enclosure.id.value}",
                )
            }
        }

        ir.space.ducts.forEach { duct ->
            val rect = rect(duct.at, duct.size)
            if (!enclosureRect.contains(rect)) {
                diagnostics += diagnostic(
                    code = "physical.constraint.duct.outside_enclosure",
                    subject = duct.id.value,
                    span = duct.provenance.span,
                    measured = rect.toMeasured(),
                    expected = "inside enclosure ${enclosure.id.value}",
                )
            }
        }

        ir.space.terminalGroups.forEach { group ->
            val rect = rect(group.at, group.size)
            if (!enclosureRect.contains(rect)) {
                diagnostics += diagnostic(
                    code = "physical.constraint.terminal_group.outside_enclosure",
                    subject = group.id.value,
                    span = group.provenance.span,
                    measured = rect.toMeasured(),
                    expected = "inside enclosure ${enclosure.id.value}",
                )
            }
        }

        ir.space.rails.forEach { rail ->
            val surface = surfaces[rail.surfaceId] ?: return@forEach
            val measured = "${rail.at.x},${rail.at.y},length=${rail.length.value}"
            val fits = when (rail.orientation) {
                PhysicalInfrastructureOrientation.Horizontal ->
                    rail.at.x >= 0 && rail.at.x + rail.length.value <= surface.size.width &&
                        rail.at.y >= 0 && rail.at.y <= surface.size.height
                PhysicalInfrastructureOrientation.Vertical ->
                    rail.at.y >= 0 && rail.at.y + rail.length.value <= surface.size.height &&
                        rail.at.x >= 0 && rail.at.x <= surface.size.width
            }
            if (!fits) {
                diagnostics += diagnostic(
                    code = "physical.constraint.rail.outside_surface",
                    subject = rail.id.value,
                    span = rail.provenance.span,
                    measured = measured,
                    expected = "rail interval inside surface ${surface.id.value}",
                )
            }
        }

        ir.space.channels.forEach { channel ->
            val duct = ducts[channel.ductId] ?: return@forEach
            val interior = PhysicalRect(
                x = 0,
                y = 0,
                width = duct.size.width - (duct.wall.value * 2),
                height = duct.size.height - (duct.wall.value * 2),
            )
            val channelRect = rect(channel.at, channel.size)
            if (!interior.contains(channelRect)) {
                diagnostics += diagnostic(
                    code = "physical.constraint.channel.outside_duct_interior",
                    subject = channel.id.value,
                    span = channel.provenance.span,
                    measured = channelRect.toMeasured(),
                    expected = "inside duct ${duct.id.value} wall-inset interior ${interior.toMeasured()}",
                )
            }
        }

        ir.space.mountedOccurrences.forEach { occurrence ->
            validateOccurrence(
                occurrence = occurrence,
                enclosure = enclosure,
                enclosureRect = enclosureRect,
                surfaces = surfaces,
                rails = rails,
                terminalGroups = terminalGroups,
                diagnostics = diagnostics,
            )?.let { rect -> occurrenceRects += occurrence to rect }
        }

        occurrenceRects.forEach { (occurrence, occurrenceRect) ->
            ir.space.ducts.forEach { duct ->
                val ductRect = rect(duct.at, duct.size)
                if (occurrenceRect.inflate(occurrence.contract.clearance).intersectsPositiveArea(ductRect)) {
                    diagnostics += diagnostic(
                        code = "physical.constraint.occurrence.infrastructure_collision",
                        subject = "${occurrence.occurrenceId.value},${duct.id.value}",
                        span = occurrence.provenance.span,
                        measured = "${occurrenceRect.toMeasured()} intersects duct ${ductRect.toMeasured()}",
                        expected = "mounted occurrence and clearance outside route duct",
                    )
                }
            }
        }

        occurrenceRects.withIndex().forEach { (index, first) ->
            occurrenceRects.drop(index + 1).forEach { second ->
                validateCollision(first, second, diagnostics)
            }
        }

        val evidence = ir.toEvaluationReport(diagnostics.size)
        return if (diagnostics.isEmpty()) {
            PhysicalConstraintEvaluation.Success(evidence)
        } else {
            PhysicalConstraintEvaluation.Failure(
                evidence = evidence,
                diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }
    }
}

private fun validateOccurrence(
    occurrence: PhysicalMountedOccurrence,
    enclosure: PhysicalEnclosure,
    enclosureRect: PhysicalRect,
    surfaces: Map<PhysicalObjectId, PhysicalMountingSurface>,
    rails: Map<PhysicalObjectId, PhysicalRail>,
    terminalGroups: Map<PhysicalObjectId, PhysicalTerminalGroup>,
    diagnostics: MutableList<PhysicalConstraintEvaluationDiagnostic>,
): PhysicalRect? {
    val size = occurrence.orientedSize()
    val localRect = PhysicalRect(occurrence.at.x, occurrence.at.y, size.width, size.height)
    val absoluteRect = when (val target = occurrence.target) {
        is PhysicalMountTargetRef.Surface -> {
            val surface = surfaces[target.id] ?: return null
            validateBoundedTarget(
                occurrence = occurrence,
                targetId = surface.id,
                targetRect = PhysicalRect(0, 0, surface.size.width, surface.size.height),
                localRect = localRect,
                acceptedMountingTypes = surface.acceptedMountingTypes,
                diagnostics = diagnostics,
            )
            localRect.translate(surface.at)
        }
        is PhysicalMountTargetRef.TerminalGroup -> {
            val group = terminalGroups[target.id] ?: return null
            val targetSize = when (group.orientation) {
                PhysicalInfrastructureOrientation.Horizontal -> group.size
                PhysicalInfrastructureOrientation.Vertical -> PhysicalSize2i(group.size.height, group.size.width)
            }
            validateBoundedTarget(
                occurrence = occurrence,
                targetId = group.id,
                targetRect = PhysicalRect(0, 0, targetSize.width, targetSize.height),
                localRect = localRect,
                acceptedMountingTypes = group.acceptedMountingTypes,
                diagnostics = diagnostics,
            )
            localRect.transform(group.frame())
        }
        is PhysicalMountTargetRef.Rail -> {
            val rail = rails[target.id] ?: return null
            val surface = surfaces[rail.surfaceId] ?: return null
            validateRailTarget(occurrence, rail, size, diagnostics)
            localRect.transform(rail.frame.translate(surface.at))
        }
    }

    if (occurrence.selectedOrientation !in occurrence.contract.allowedOrientations) {
        diagnostics += diagnostic(
            code = "physical.constraint.orientation.not_allowed",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = occurrence.selectedOrientation.name,
            expected = occurrence.contract.allowedOrientations.joinToString(",") { it.name },
        )
    }
    if (occurrence.contract.size.depth.value > enclosure.size.depth) {
        diagnostics += diagnostic(
            code = "physical.constraint.depth.exceeds_enclosure",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = occurrence.contract.size.depth.value.toString(),
            expected = "depth <= ${enclosure.size.depth}",
        )
    }
    if (PhysicalContainerKindId("cabinet") !in occurrence.contract.compatibleContainerKinds) {
        diagnostics += diagnostic(
            code = "physical.constraint.container.incompatible",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = occurrence.contract.compatibleContainerKinds.joinToString(",") { it.value },
            expected = "cabinet-compatible occurrence",
        )
    }
    if (!enclosureRect.contains(absoluteRect.inflate(occurrence.contract.clearance))) {
        diagnostics += diagnostic(
            code = "physical.constraint.occurrence.outside_enclosure",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = absoluteRect.inflate(occurrence.contract.clearance).toMeasured(),
            expected = "clearance-inflated footprint inside enclosure",
        )
    }
    return absoluteRect
}

private fun validateBoundedTarget(
    occurrence: PhysicalMountedOccurrence,
    targetId: PhysicalObjectId,
    targetRect: PhysicalRect,
    localRect: PhysicalRect,
    acceptedMountingTypes: Set<PhysicalMountingTypeId>,
    diagnostics: MutableList<PhysicalConstraintEvaluationDiagnostic>,
) {
    if (occurrence.contract.mountingTypeId !in acceptedMountingTypes) {
        diagnostics += diagnostic(
            code = "physical.constraint.mounting.incompatible",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = occurrence.contract.mountingTypeId.value,
            expected = "one of ${acceptedMountingTypes.sortedBy { it.value }.joinToString(",") { it.value }}",
        )
    }
    val inflated = localRect.inflate(occurrence.contract.clearance)
    if (!targetRect.contains(inflated)) {
        diagnostics += diagnostic(
            code = "physical.constraint.occurrence.outside_target",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = inflated.toMeasured(),
            expected = "inside target ${targetId.value}",
        )
    }
}

private fun validateRailTarget(
    occurrence: PhysicalMountedOccurrence,
    rail: PhysicalRail,
    size: PhysicalSize2i,
    diagnostics: MutableList<PhysicalConstraintEvaluationDiagnostic>,
) {
    if (occurrence.contract.mountingTypeId != rail.mountingType) {
        diagnostics += diagnostic(
            code = "physical.constraint.mounting.incompatible",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = occurrence.contract.mountingTypeId.value,
            expected = rail.mountingType.value,
        )
    }
    val normalOffset = occurrence.at.y
    if (normalOffset != 0) {
        diagnostics += diagnostic(
            code = "physical.constraint.rail.normal_offset",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = normalOffset.toString(),
            expected = "0",
        )
    }
    val alongStart = occurrence.at.x
    val alongSize = size.width
    val leadingClearance = occurrence.contract.clearance.left.value
    val trailingClearance = occurrence.contract.clearance.right.value
    val alongEnd = alongStart + alongSize + leadingClearance + trailingClearance
    if (alongStart - leadingClearance < 0 || alongEnd > rail.length.value) {
        diagnostics += diagnostic(
            code = "physical.constraint.rail.along_outside",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = "${alongStart - leadingClearance}..$alongEnd",
            expected = "inside rail interval 0..${rail.length.value}",
        )
    }
}

private fun validateCollision(
    first: Pair<PhysicalMountedOccurrence, PhysicalRect>,
    second: Pair<PhysicalMountedOccurrence, PhysicalRect>,
    diagnostics: MutableList<PhysicalConstraintEvaluationDiagnostic>,
) {
    val (firstOccurrence, firstRect) = first
    val (secondOccurrence, secondRect) = second
    if (firstRect.intersectsPositiveArea(secondRect)) {
        diagnostics += diagnostic(
            code = "physical.constraint.occurrence.collision",
            subject = "${firstOccurrence.occurrenceId.value},${secondOccurrence.occurrenceId.value}",
            span = firstOccurrence.provenance.span,
            measured = "${firstRect.toMeasured()} intersects ${secondRect.toMeasured()}",
            expected = "zero positive-area footprint intersection",
        )
    }
    if (
        firstRect.inflate(firstOccurrence.contract.clearance).intersectsPositiveArea(secondRect) ||
        secondRect.inflate(secondOccurrence.contract.clearance).intersectsPositiveArea(firstRect)
    ) {
        diagnostics += diagnostic(
            code = "physical.constraint.clearance.collision",
            subject = "${firstOccurrence.occurrenceId.value},${secondOccurrence.occurrenceId.value}",
            span = firstOccurrence.provenance.span,
            measured = "${firstRect.toMeasured()} near ${secondRect.toMeasured()}",
            expected = "clearance-inflated rectangles do not intersect opposite footprints",
        )
    }
}

private fun PhysicalMountedOccurrence.orientedSize(): PhysicalSize2i =
    when (selectedOrientation) {
        PhysicalInstallationOrientation.Deg0,
        PhysicalInstallationOrientation.Deg180,
        -> PhysicalSize2i(contract.size.width.value, contract.size.height.value)
        PhysicalInstallationOrientation.Deg90,
        PhysicalInstallationOrientation.Deg270,
        -> PhysicalSize2i(contract.size.height.value, contract.size.width.value)
    }

private fun PhysicalRect.translate(point: PhysicalPoint2i): PhysicalRect = copy(
    x = x + point.x,
    y = y + point.y,
)

private fun PhysicalRect.transform(frame: PhysicalRigidFrame2i): PhysicalRect {
    val corners = listOf(
        PhysicalPoint2i(x, y),
        PhysicalPoint2i(right, y),
        PhysicalPoint2i(right, bottom),
        PhysicalPoint2i(x, bottom),
    ).map(frame::toParent)
    val minimumX = corners.minOf { point -> point.x }
    val minimumY = corners.minOf { point -> point.y }
    val maximumX = corners.maxOf { point -> point.x }
    val maximumY = corners.maxOf { point -> point.y }
    return PhysicalRect(minimumX, minimumY, maximumX - minimumX, maximumY - minimumY)
}

private fun PhysicalRigidFrame2i.translate(point: PhysicalPoint2i): PhysicalRigidFrame2i = copy(
    origin = PhysicalPoint2i(origin.x + point.x, origin.y + point.y),
)

private fun PhysicalTerminalGroup.frame(): PhysicalRigidFrame2i = when (orientation) {
    PhysicalInfrastructureOrientation.Horizontal -> PhysicalRigidFrame2i(
        origin = at,
        alongAxis = PhysicalVector2i(1, 0),
        normalAxis = PhysicalVector2i(0, 1),
    )
    PhysicalInfrastructureOrientation.Vertical -> PhysicalRigidFrame2i(
        origin = at,
        alongAxis = PhysicalVector2i(0, 1),
        normalAxis = PhysicalVector2i(-1, 0),
    )
}

private fun rect(point: PhysicalPoint2i, size: PhysicalSize2i): PhysicalRect =
    PhysicalRect(point.x, point.y, size.width, size.height)

private fun PhysicalRect.toMeasured(): String = "x=$x,y=$y,w=$width,h=$height"

private fun PhysicalInstallationIR.toEvaluationReport(diagnosticCount: Int): PhysicalConstraintEvaluationReport =
    PhysicalConstraintEvaluationReport(
        mode = PhysicalConstraintEvaluationMode.ValidationOnly,
        surfaceCount = space.surfaces.size,
        railCount = space.rails.size,
        ductCount = space.ducts.size,
        channelCount = space.channels.size,
        terminalGroupCount = space.terminalGroups.size,
        occurrenceCount = space.mountedOccurrences.size,
        diagnosticCount = diagnosticCount,
    )

private fun diagnostic(
    code: String,
    subject: String,
    span: PhysicalSourceSpan?,
    measured: String?,
    expected: String,
): PhysicalConstraintEvaluationDiagnostic = PhysicalConstraintEvaluationDiagnostic(
    code = code,
    subject = subject,
    span = span,
    measured = measured,
    expected = expected,
)
