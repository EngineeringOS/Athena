package com.engineeringood.athena.policy

import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.LabelFactId
import com.engineeringood.athena.representation.LabelPolicy
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.PhysicalTerminalId
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationRouteAnchorId
import com.engineeringood.athena.representation.PresentationSide
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.PresentationTerminalId
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationVersion
import com.engineeringood.athena.representation.SemanticPortId
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
    val definition: RepresentationDefinition,
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

    fun acceptanceSatisfied(): Boolean = hasZeroFallbackSymbols()

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
        val definition = definitionFor(selection, terminal)
        val tagAnchor = labelPolicy.anchorFor(
            role = PresentationLabelRole.DEVICE_TAG,
            subjectId = subjectId,
            occurrenceId = occurrenceId,
        )
        return ComponentRepresentationFact(
            subject = subject,
            family = family,
            selection = selection,
            definition = definition,
            terminals = listOf(terminal),
            labels = listOf(
                LabelFact(
                    labelId = LabelFactId("label:${subject.value}:device-tag"),
                    subjectId = subjectId,
                    occurrenceId = occurrenceId,
                    role = PresentationLabelRole.DEVICE_TAG,
                    value = LabelValue(subject.displayTag()),
                    anchor = tagAnchor,
                ),
            ),
        )
    }

    private fun definitionFor(
        selection: RepresentationSelection.Supported,
        terminal: PresentationTerminalFact,
    ): RepresentationDefinition {
        val styleTokenId = GraphicStyleTokenId("policy.stroke")
        return RepresentationDefinition(
            symbolId = RepresentationSymbolId(selection.representationId.value),
            libraryId = RepresentationLibraryId("athena.presentation-policy"),
            version = RepresentationVersion("1.0.0"),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance("presentation-policy:${selection.profileId.value}"),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            labelSlots = emptyList(),
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = GraphicPrimitiveDocument(
                documentId = GraphicPrimitiveDocumentId(selection.representationId.value),
                bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
                primitives = listOf(
                    GraphicPrimitive.Rectangle(
                        primitiveId = GraphicPrimitiveId("${selection.family.value}:body"),
                        bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
                        cornerRadius = 0.0,
                        styleTokenId = styleTokenId,
                    ),
                    GraphicPrimitive.Line(
                        primitiveId = GraphicPrimitiveId("${selection.family.value}:terminal-line"),
                        bounds = GraphicBounds(60.0, 24.0, 20.0, 0.001),
                        start = GraphicPoint(60.0, 24.0),
                        end = GraphicPoint(
                            terminal.routeAnchor.point.x.value.toDouble(),
                            terminal.routeAnchor.point.y.value.toDouble(),
                        ),
                        styleTokenId = styleTokenId,
                    ),
                ),
                styleTokens = listOf(
                    GraphicStyleToken(
                        styleTokenId = styleTokenId,
                        stroke = GraphicPaintToken("foreground"),
                        strokeWidth = 1.0,
                        fill = GraphicFill.TRANSPARENT,
                        lineCap = GraphicLineCap.BUTT,
                        lineJoin = GraphicLineJoin.MITER,
                    ),
                ),
                provenanceSources = listOf("presentation-policy:${selection.profileId.value}"),
            ),
            anchors = listOf(
                RepresentationAnchorContract(
                    anchorId = RepresentationAnchorId(terminal.presentationTerminalId.value),
                    geometryRef = terminal.routeAnchor.anchorId.value,
                    primitiveId = GraphicPrimitiveId("${selection.family.value}:terminal-line"),
                    point = GraphicPoint(
                        terminal.routeAnchor.point.x.value.toDouble(),
                        terminal.routeAnchor.point.y.value.toDouble(),
                    ),
                    role = RepresentationAnchorRole.TERMINAL,
                    required = true,
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

    private fun ComponentSubjectKey.displayTag(): String =
        value.substringAfterLast(':').ifBlank { value }
}
