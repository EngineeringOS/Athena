package com.engineeringood.athena.policy

import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.LabelFactId
import com.engineeringood.athena.representation.LabelPolicy
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.PhysicalTerminalId
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationPrimitive
import com.engineeringood.athena.representation.PresentationPrimitiveId
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationRouteAnchorId
import com.engineeringood.athena.representation.PresentationSide
import com.engineeringood.athena.representation.PresentationSize
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.PresentationTerminalId
import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.SemanticPortId
import com.engineeringood.athena.representation.SymbolAnatomy
import com.engineeringood.athena.representation.SymbolFamilyId
import com.engineeringood.athena.representation.TerminalMarker
import com.engineeringood.athena.representation.TerminalNotation
import com.engineeringood.athena.representation.TerminalNumber
import com.engineeringood.athena.representation.TerminalPresentationRole

@JvmInline
value class ComponentSubjectKey(val value: String) {
    init {
        require(value.isNotBlank()) { "Component subject key must not be blank." }
    }
}

data class ComponentRepresentationRequest(
    val subject: ComponentSubjectKey,
    val family: ComponentFamilyKey,
)

data class ComponentRepresentationFact(
    val subject: ComponentSubjectKey,
    val family: ComponentFamilyKey,
    val selection: RepresentationSelection.Supported,
    val anatomy: PresentationAnatomy,
    val symbol: SymbolAnatomy,
    val terminals: List<PresentationTerminalFact>,
    val labels: List<LabelFact>,
)

data class ComponentRepresentationFallback(
    val subject: ComponentSubjectKey,
    val selection: RepresentationSelection.Fallback,
)

data class ComponentRepresentationSnapshot(
    val profileId: PresentationPolicyProfileId,
    val facts: List<ComponentRepresentationFact>,
    val fallbacks: List<ComponentRepresentationFallback>,
) {
    fun hasZeroFallbackSymbols(): Boolean = fallbacks.isEmpty()

    fun acceptedProofSatisfied(): Boolean = hasZeroFallbackSymbols()

    fun coverageDiagnostics(): List<PresentationPolicyDiagnostic> =
        fallbacks.map { fallback -> fallback.selection.diagnostic }

    fun fallbackSubjects(): List<String> =
        fallbacks.map { fallback -> fallback.subject.value }
}

class ComponentRepresentationComposer(
    private val profile: PresentationPolicyProfile,
    private val labelPolicy: LabelPolicy = LabelPolicy.defaultIndustrialControl(),
) {
    fun compose(requests: List<ComponentRepresentationRequest>): ComponentRepresentationSnapshot {
        val facts = mutableListOf<ComponentRepresentationFact>()
        val fallbacks = mutableListOf<ComponentRepresentationFallback>()
        requests.sortedBy { request -> request.subject.value }.forEach { request ->
            when (val selection = profile.selectRepresentation(request.family)) {
                is RepresentationSelection.Fallback -> fallbacks.add(ComponentRepresentationFallback(request.subject, selection))
                is RepresentationSelection.Supported -> facts.add(request.toFact(selection))
            }
        }
        return ComponentRepresentationSnapshot(
            profileId = profile.profileId,
            facts = facts,
            fallbacks = fallbacks,
        )
    }

    private fun ComponentRepresentationRequest.toFact(
        selection: RepresentationSelection.Supported,
    ): ComponentRepresentationFact {
        val occurrenceId = RepresentationOccurrenceId("${subject.value}@schematic-sheet")
        val subjectId = RepresentationSubjectId(subject.value)
        val terminal = terminalFor(subjectId, occurrenceId, family)
        val anatomy = anatomyFor(selection, terminal)
        val tagAnchor = labelPolicy.anchorFor(
            role = PresentationLabelRole.DEVICE_TAG,
            subjectId = subjectId,
            occurrenceId = occurrenceId,
        )
        return ComponentRepresentationFact(
            subject = subject,
            family = family,
            selection = selection,
            anatomy = anatomy,
            symbol = SymbolAnatomy(SymbolFamilyId(family.value), anatomy),
            terminals = listOf(terminal),
            labels = listOf(
                LabelFact(
                    labelId = LabelFactId("label:${subject.value}:device-tag"),
                    subjectId = subjectId,
                    occurrenceId = occurrenceId,
                    role = PresentationLabelRole.DEVICE_TAG,
                    value = LabelValue(subject.value),
                    anchor = tagAnchor,
                ),
            ),
        )
    }

    private fun anatomyFor(
        selection: RepresentationSelection.Supported,
        terminal: PresentationTerminalFact,
    ): PresentationAnatomy {
        return PresentationAnatomy(
            representationId = selection.representationId,
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(80), GridUnit(48)),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = listOf(
                PresentationPrimitive.Rectangle(
                    primitiveId = PresentationPrimitiveId("${selection.family.value}:body"),
                    origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                    size = PresentationSize(GridUnit(80), GridUnit(48)),
                ),
                PresentationPrimitive.Line(
                    primitiveId = PresentationPrimitiveId("${selection.family.value}:terminal-line"),
                    start = PresentationPoint(GridUnit(60), GridUnit(24)),
                    end = terminal.routeAnchor.point,
                ),
            ),
            terminals = listOf(
                com.engineeringood.athena.representation.PresentationTerminalPoint(
                    terminalId = terminal.presentationTerminalId,
                    role = terminalRoleFor(selection.family),
                    localPoint = terminal.routeAnchor.point,
                    side = terminal.side,
                    notation = terminal.notation,
                ),
            ),
            labelAnchors = listOf(
                labelPolicy.anchorFor(
                    role = PresentationLabelRole.DEVICE_TAG,
                    subjectId = terminal.subjectId,
                    occurrenceId = terminal.occurrenceId,
                ),
            ),
        )
    }

    private fun terminalFor(
        subjectId: RepresentationSubjectId,
        occurrenceId: RepresentationOccurrenceId,
        family: ComponentFamilyKey,
    ): PresentationTerminalFact {
        val number = when (family.value) {
            "power-supply" -> "L+"
            "terminal-block" -> "1"
            "load-actuator" -> "U1"
            "hmi-operator" -> "COM"
            "protection-device" -> "2"
            else -> "Q1.0"
        }
        val side = when (family.value) {
            "power-supply" -> PresentationSide.RIGHT
            "terminal-block" -> PresentationSide.LEFT
            "load-actuator" -> PresentationSide.LEFT
            else -> PresentationSide.RIGHT
        }
        return PresentationTerminalFact(
            presentationTerminalId = PresentationTerminalId("terminal:${subjectId.value}:$number"),
            subjectId = subjectId,
            occurrenceId = occurrenceId,
            portId = SemanticPortId(number),
            physicalTerminalId = PhysicalTerminalId("${subjectId.value}:$number"),
            side = side,
            routeAnchor = PresentationRouteAnchor(
                anchorId = PresentationRouteAnchorId("anchor:${subjectId.value}:$number"),
                point = PresentationPoint(
                    x = if (side == PresentationSide.LEFT) GridUnit(0) else GridUnit(80),
                    y = GridUnit(24),
                ),
            ),
            notation = TerminalNotation(
                marker = TerminalMarker.CIRCLE,
                number = TerminalNumber(number),
            ),
        )
    }

    private fun terminalRoleFor(family: ComponentFamilyKey): TerminalPresentationRole {
        return when (family.value) {
            "power-supply" -> TerminalPresentationRole.POWER_OUTPUT
            "terminal-block" -> TerminalPresentationRole.TERMINAL_TRANSITION
            "load-actuator" -> TerminalPresentationRole.POWER_INPUT
            "hmi-operator" -> TerminalPresentationRole.COMMUNICATION
            "protection-device" -> TerminalPresentationRole.POWER_OUTPUT
            else -> TerminalPresentationRole.DIGITAL_OUTPUT
        }
    }
}
