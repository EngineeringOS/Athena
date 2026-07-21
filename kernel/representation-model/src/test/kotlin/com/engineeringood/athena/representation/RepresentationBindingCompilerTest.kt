package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepresentationBindingCompilerTest {
    @Test
    fun `binding consumes policy and emits occurrence with separate semantic and projection identities`() {
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:motor"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("iec.motor"),
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            variant = RepresentationVariantId("compact"),
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val definition = motorDefinition()
        val result = RepresentationBindingCompiler().bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/occurrence:motor"),
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = RepresentationSemanticRole("power-load"),
                projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                policy = policy,
                definition = definition,
                labelValues = mapOf(RepresentationLabelSlotId("device-tag") to LabelValue("M1")),
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
                priority = RepresentationPolicyPriority(100),
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val occurrence = result.occurrence
        assertEquals(RepresentationSubjectId("component:MotorM1"), occurrence.canonicalSemanticId)
        assertEquals(
            RepresentationProjectionOccurrenceId("sheet:control/occurrence:motor"),
            occurrence.projectionOccurrenceId,
        )
        assertEquals(policy.symbolId, occurrence.symbolId)
        assertEquals(policy.variant, occurrence.variant)
        assertEquals(RepresentationOccurrenceRole.LOAD_SYMBOL, occurrence.occurrenceRole)
        assertEquals(LabelValue("M1"), occurrence.labelBindings.single().value)
        assertEquals(SemanticPortId("MotorM1.power"), occurrence.terminalBindings.single().semanticPortId)
    }

    @Test
    fun `binding emits diagnostics instead of renderer fallback when policy and definition disagree`() {
        val result = RepresentationBindingCompiler().bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/occurrence:motor"),
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = RepresentationSemanticRole("power-load"),
                projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                policy = RepresentationPolicy(
                    policyId = RepresentationPolicyId("policy:bad"),
                    projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                    subjectKind = RepresentationSubjectKind.COMPONENT,
                    semanticRole = RepresentationSemanticRole("power-load"),
                    occurrenceRole = RepresentationOccurrenceRole.COIL_ACTUATOR,
                    symbolFamilyId = SymbolFamilyId("iec.motor"),
                    symbolId = RepresentationSymbolId("iec.motor.compact"),
                    fallback = RepresentationFallbackBehavior.ALLOW_EXPLICIT_FALLBACK,
                    priority = RepresentationPolicyPriority(100),
                ),
                definition = motorDefinition(),
                labelValues = emptyMap(),
                terminalPorts = emptyMap(),
                priority = RepresentationPolicyPriority(100),
            ),
        )

        assertEquals(null, result.occurrenceOrNull)
        assertEquals(
            listOf("representation.symbol.unsupported-role"),
            result.diagnostics.map { diagnostic -> diagnostic.code.wireValue },
        )
    }

    private fun motorDefinition(): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId("iec.motor.compact"),
        libraryId = RepresentationLibraryId("athena.native.iec-v0"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("test"),
        ),
        kind = RepresentationSymbolKind.MOTOR_LOAD,
        anatomy = PresentationAnatomy(
            representationId = RepresentationId("iec.motor.compact"),
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(44), GridUnit(44)),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = listOf(
                PresentationPrimitive.Circle(
                    primitiveId = PresentationPrimitiveId("body"),
                    center = PresentationPoint(GridUnit(22), GridUnit(22)),
                    radius = GridUnit(15),
                ),
            ),
            terminals = listOf(
                PresentationTerminalPoint(
                    terminalId = PresentationTerminalId("u1"),
                    role = TerminalPresentationRole.POWER_INPUT,
                    localPoint = PresentationPoint(GridUnit(0), GridUnit(22)),
                    side = PresentationSide.LEFT,
                    notation = TerminalNotation(TerminalMarker.CIRCLE, TerminalNumber("U1")),
                ),
            ),
            labelAnchors = listOf(
                PresentationLabelAnchor(
                    anchorId = PresentationLabelAnchorId("device-tag"),
                    role = PresentationLabelRole.DEVICE_TAG,
                    point = PresentationPoint(GridUnit(0), GridUnit(-8)),
                ),
            ),
        ),
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        variants = listOf(RepresentationVariantId("compact")),
    )
}
