package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RepresentationDiagnosticSerializationTest {
    @Test
    fun `validator emits every mandatory m30 diagnostic code deterministically`() {
        val motorDefinition = definition(
            symbolId = "iec.motor.compact",
            libraryId = "athena.native.iec-v0",
            lifecycleState = RepresentationLifecycleState.ACTIVE,
            kind = RepresentationSymbolKind.MOTOR_LOAD,
        )
        val deprecatedDefinition = definition(
            symbolId = "iec.deprecated",
            libraryId = "athena.native.iec-v0",
            lifecycleState = RepresentationLifecycleState.DEPRECATED,
            kind = RepresentationSymbolKind.MOTOR_LOAD,
        )
        val externalDefinition = definition(
            symbolId = "external.bad",
            libraryId = "external.untrusted",
            lifecycleState = RepresentationLifecycleState.ACTIVE,
            kind = RepresentationSymbolKind.LAMP_INDICATOR,
        )
        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(RepresentationLibraryId("athena.native.iec-v0")),
                supportedLifecycleStates = setOf(RepresentationLifecycleState.ACTIVE),
                policies = listOf(
                    policy("policy:missing-symbol", "missing.symbol", RepresentationOccurrenceRole.LOAD_SYMBOL),
                    policy("policy:unsupported-role", "iec.motor.compact", RepresentationOccurrenceRole.COIL_ACTUATOR),
                ),
                definitions = listOf(motorDefinition, motorDefinition, deprecatedDefinition, externalDefinition),
                occurrences = listOf(
                    occurrence(
                        occurrenceId = "occurrence:bad-bindings",
                        symbolId = "iec.motor.compact",
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        labelSlotId = "missing-label-slot",
                        terminalId = "missing-terminal",
                        semanticPortId = "MotorM1.power",
                        compositionMembership = "missing-lane",
                    ),
                    occurrence(
                        occurrenceId = "occurrence:no-policy",
                        symbolId = "iec.deprecated",
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        labelSlotId = "device-tag",
                        terminalId = "terminal-1",
                        semanticPortId = "MotorM1.power",
                        compositionMembership = null,
                    ),
                    occurrence(
                        occurrenceId = "occurrence:missing-symbol",
                        symbolId = "unknown.symbol",
                        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        labelSlotId = null,
                        terminalId = null,
                        semanticPortId = null,
                        compositionMembership = null,
                    ),
                ),
                compatibleTerminalBindings = emptySet(),
                compositionMemberships = emptySet(),
            ),
        )

        assertFalse(result.accepted)
        assertEquals(
            listOf(
                "representation.anchor.missing",
                "representation.binding.ambiguous",
                "representation.composition.unsatisfied",
                "representation.label-slot.missing",
                "representation.library.invalid",
                "representation.lifecycle.unsupported",
                "representation.policy.missing",
                "representation.symbol.missing",
                "representation.symbol.unsupported-role",
                "representation.terminal.incompatible",
            ),
            result.toTransportPayload().map { payload -> payload.getValue("code") }.distinct(),
        )
        assertEquals(
            result.toTransportPayload().map { payload -> payload.getValue("code") },
            result.toTransportPayload().map { payload -> payload.getValue("code") }.sorted(),
        )
    }

    @Test
    fun `explicit fallback policy still fails proof when symbol is missing`() {
        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(RepresentationLibraryId("athena.native.iec-v0")),
                policies = listOf(
                    policy(
                        policyId = "policy:fallback",
                        symbolId = "missing.symbol",
                        role = RepresentationOccurrenceRole.LOAD_SYMBOL,
                        fallback = RepresentationFallbackBehavior.ALLOW_EXPLICIT_FALLBACK,
                    ),
                ),
                definitions = emptyList(),
                occurrences = emptyList(),
            ),
        )

        assertFalse(result.accepted)
        assertEquals(
            listOf("representation.symbol.missing"),
            result.toTransportPayload().map { payload -> payload.getValue("code") },
        )
    }

    private fun policy(
        policyId: String,
        symbolId: String,
        role: RepresentationOccurrenceRole,
        fallback: RepresentationFallbackBehavior = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
    ): RepresentationPolicy = RepresentationPolicy(
        policyId = RepresentationPolicyId(policyId),
        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
        subjectKind = RepresentationSubjectKind.COMPONENT,
        semanticRole = RepresentationSemanticRole("demo"),
        occurrenceRole = role,
        symbolFamilyId = SymbolFamilyId(symbolId.removeSuffix(".compact")),
        symbolId = RepresentationSymbolId(symbolId),
        fallback = fallback,
        priority = RepresentationPolicyPriority(10),
    )

    private fun definition(
        symbolId: String,
        libraryId: String,
        lifecycleState: RepresentationLifecycleState,
        kind: RepresentationSymbolKind,
    ): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId(symbolId),
        libraryId = RepresentationLibraryId(libraryId),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = lifecycleState,
            provenance = RepresentationProvenance("test"),
        ),
        kind = kind,
        anatomy = anatomy(symbolId),
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
    )

    private fun occurrence(
        occurrenceId: String,
        symbolId: String,
        occurrenceRole: RepresentationOccurrenceRole,
        labelSlotId: String?,
        terminalId: String?,
        semanticPortId: String?,
        compositionMembership: String?,
    ): RepresentationOccurrence = RepresentationOccurrence(
        occurrenceId = RepresentationOccurrenceId(occurrenceId),
        canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
        projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet1/$occurrenceId"),
        occurrenceRole = occurrenceRole,
        symbolId = RepresentationSymbolId(symbolId),
        labelBindings = labelSlotId?.let { slotId ->
            listOf(RepresentationLabelBinding(RepresentationLabelSlotId(slotId), LabelValue("M1")))
        }.orEmpty(),
        terminalBindings = if (terminalId != null && semanticPortId != null) {
            listOf(RepresentationTerminalBinding(PresentationTerminalId(terminalId), SemanticPortId(semanticPortId)))
        } else {
            emptyList()
        },
        compositionIntentMembership = compositionMembership?.let { id ->
            listOf(CompositionIntentMembershipId(id))
        }.orEmpty(),
    )

    private fun anatomy(symbolId: String): PresentationAnatomy = PresentationAnatomy(
        representationId = RepresentationId(symbolId),
        context = RepresentationContext.ELECTRICAL_SCHEMATIC,
        bounds = PresentationBounds(width = GridUnit(32), height = GridUnit(32)),
        hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
        primitives = listOf(
            PresentationPrimitive.Rectangle(
                primitiveId = PresentationPrimitiveId("$symbolId-body"),
                origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                size = PresentationSize(GridUnit(32), GridUnit(32)),
            ),
        ),
        terminals = listOf(
            PresentationTerminalPoint(
                terminalId = PresentationTerminalId("terminal-1"),
                role = TerminalPresentationRole.POWER_INPUT,
                localPoint = PresentationPoint(GridUnit(0), GridUnit(16)),
                side = PresentationSide.LEFT,
                notation = TerminalNotation(TerminalMarker.CIRCLE, TerminalNumber("1")),
            ),
        ),
        labelAnchors = listOf(
            PresentationLabelAnchor(
                anchorId = PresentationLabelAnchorId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
                point = PresentationPoint(GridUnit(0), GridUnit(-8)),
            ),
        ),
    )
}
