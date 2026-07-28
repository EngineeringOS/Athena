package com.engineeringood.athena.physical

enum class PhysicalConstraintEvaluationMode {
    ValidationOnly,
}

data class PhysicalRectV0(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int = x + width
    val bottom: Int = y + height

    fun contains(other: PhysicalRectV0): Boolean =
        other.x >= x && other.y >= y && other.right <= right && other.bottom <= bottom

    fun intersectsPositiveArea(other: PhysicalRectV0): Boolean =
        x < other.right && right > other.x && y < other.bottom && bottom > other.y

    fun inflate(clearance: PhysicalInstallationClearanceV0): PhysicalRectV0 = PhysicalRectV0(
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

data class PhysicalConstraintEvaluationProofV0(
    val mode: PhysicalConstraintEvaluationMode,
    val surfaceCount: Int,
    val railCount: Int,
    val ductCount: Int,
    val channelCount: Int,
    val terminalGroupCount: Int,
    val occurrenceCount: Int,
    val diagnosticCount: Int,
)

sealed interface PhysicalConstraintEvaluationV0 {
    data class Success(val proof: PhysicalConstraintEvaluationProofV0) : PhysicalConstraintEvaluationV0

    data class Failure(
        val proof: PhysicalConstraintEvaluationProofV0,
        val diagnostics: List<PhysicalConstraintEvaluationDiagnostic>,
    ) : PhysicalConstraintEvaluationV0
}

object PhysicalConstraintEvaluatorV0 {
    fun evaluate(ir: PhysicalInstallationIRV0): PhysicalConstraintEvaluationV0 {
        val diagnostics = mutableListOf<PhysicalConstraintEvaluationDiagnostic>()
        val enclosure = ir.space.enclosure
        val enclosureRect = PhysicalRectV0(0, 0, enclosure.size.width, enclosure.size.height)
        val surfaces = ir.space.surfaces.associateBy { surface -> surface.id }
        val rails = ir.space.rails.associateBy { rail -> rail.id }
        val ducts = ir.space.ducts.associateBy { duct -> duct.id }
        val terminalGroups = ir.space.terminalGroups.associateBy { group -> group.id }
        val occurrenceRects = mutableListOf<Pair<PhysicalMountedOccurrenceV0, PhysicalRectV0>>()

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
            val interior = PhysicalRectV0(
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

        occurrenceRects.withIndex().forEach { (index, first) ->
            occurrenceRects.drop(index + 1).forEach { second ->
                validateCollision(first, second, diagnostics)
            }
        }

        val proof = ir.toProof(diagnostics.size)
        return if (diagnostics.isEmpty()) {
            PhysicalConstraintEvaluationV0.Success(proof)
        } else {
            PhysicalConstraintEvaluationV0.Failure(
                proof = proof,
                diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }
    }
}

private fun validateOccurrence(
    occurrence: PhysicalMountedOccurrenceV0,
    enclosure: PhysicalEnclosureV0,
    enclosureRect: PhysicalRectV0,
    surfaces: Map<PhysicalObjectId, PhysicalMountingSurfaceV0>,
    rails: Map<PhysicalObjectId, PhysicalRailV0>,
    terminalGroups: Map<PhysicalObjectId, PhysicalTerminalGroupV0>,
    diagnostics: MutableList<PhysicalConstraintEvaluationDiagnostic>,
): PhysicalRectV0? {
    val size = occurrence.orientedSize()
    val localRect = PhysicalRectV0(occurrence.at.x, occurrence.at.y, size.width, size.height)
    val absoluteRect = when (val target = occurrence.target) {
        is PhysicalMountTargetRef.Surface -> {
            val surface = surfaces[target.id] ?: return null
            validateBoundedTarget(
                occurrence = occurrence,
                targetId = surface.id,
                targetRect = PhysicalRectV0(0, 0, surface.size.width, surface.size.height),
                localRect = localRect,
                acceptedMountingTypes = surface.acceptedMountingTypes,
                diagnostics = diagnostics,
            )
            localRect.translate(surface.at)
        }
        is PhysicalMountTargetRef.TerminalGroup -> {
            val group = terminalGroups[target.id] ?: return null
            validateBoundedTarget(
                occurrence = occurrence,
                targetId = group.id,
                targetRect = PhysicalRectV0(0, 0, group.size.width, group.size.height),
                localRect = localRect,
                acceptedMountingTypes = group.acceptedMountingTypes,
                diagnostics = diagnostics,
            )
            localRect.translate(group.at)
        }
        is PhysicalMountTargetRef.Rail -> {
            val rail = rails[target.id] ?: return null
            validateRailTarget(occurrence, rail, size, diagnostics)
            localRect.translate(rail.frame.origin)
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
    occurrence: PhysicalMountedOccurrenceV0,
    targetId: PhysicalObjectId,
    targetRect: PhysicalRectV0,
    localRect: PhysicalRectV0,
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
    occurrence: PhysicalMountedOccurrenceV0,
    rail: PhysicalRailV0,
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
    val normalOffset = when (rail.orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> occurrence.at.y
        PhysicalInfrastructureOrientation.Vertical -> occurrence.at.x
    }
    if (normalOffset != 0) {
        diagnostics += diagnostic(
            code = "physical.constraint.rail.normal_offset",
            subject = occurrence.occurrenceId.value,
            span = occurrence.provenance.span,
            measured = normalOffset.toString(),
            expected = "0",
        )
    }
    val (alongStart, alongSize, leadingClearance, trailingClearance) = when (rail.orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> listOf(
            occurrence.at.x,
            size.width,
            occurrence.contract.clearance.left.value,
            occurrence.contract.clearance.right.value,
        )
        PhysicalInfrastructureOrientation.Vertical -> listOf(
            occurrence.at.y,
            size.height,
            occurrence.contract.clearance.top.value,
            occurrence.contract.clearance.bottom.value,
        )
    }
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
    first: Pair<PhysicalMountedOccurrenceV0, PhysicalRectV0>,
    second: Pair<PhysicalMountedOccurrenceV0, PhysicalRectV0>,
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

private fun PhysicalMountedOccurrenceV0.orientedSize(): PhysicalSize2i =
    when (selectedOrientation) {
        PhysicalInstallationOrientation.Deg0,
        PhysicalInstallationOrientation.Deg180,
        -> PhysicalSize2i(contract.size.width.value, contract.size.height.value)
        PhysicalInstallationOrientation.Deg90,
        PhysicalInstallationOrientation.Deg270,
        -> PhysicalSize2i(contract.size.height.value, contract.size.width.value)
    }

private fun PhysicalRectV0.translate(point: PhysicalPoint2i): PhysicalRectV0 = copy(
    x = x + point.x,
    y = y + point.y,
)

private fun rect(point: PhysicalPoint2i, size: PhysicalSize2i): PhysicalRectV0 =
    PhysicalRectV0(point.x, point.y, size.width, size.height)

private fun PhysicalRectV0.toMeasured(): String = "x=$x,y=$y,w=$width,h=$height"

private fun PhysicalInstallationIRV0.toProof(diagnosticCount: Int): PhysicalConstraintEvaluationProofV0 =
    PhysicalConstraintEvaluationProofV0(
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
