package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationModelContractTest {
    @Test
    fun `presentation anatomy can represent mandatory M25 families without fallback`() {
        val anatomies = listOf(
            schematicAnatomy("plc-controller"),
            schematicAnatomy("terminal-block"),
            schematicAnatomy("power-supply"),
            schematicAnatomy("load-actuator"),
        )

        assertEquals(4, anatomies.size)
        assertTrue(anatomies.all { it.context == RepresentationContext.ELECTRICAL_SCHEMATIC })
        assertTrue(anatomies.all { it.terminals.isNotEmpty() })
        assertTrue(anatomies.all { anatomy -> anatomy.terminals.all { it.notation.number.value.isNotBlank() } })
    }

    @Test
    fun `symbol anatomy is electrical schematic subset of presentation anatomy`() {
        val anatomy = schematicAnatomy("plc-controller")
        val symbol = SymbolAnatomy(
            familyId = SymbolFamilyId("plc-controller"),
            anatomy = anatomy,
        )

        assertEquals(anatomy, symbol.anatomy)
        assertEquals(RepresentationContext.ELECTRICAL_SCHEMATIC, symbol.anatomy.context)
        assertEquals("plc-controller", symbol.familyId.value)
    }

    @Test
    fun `representation model does not own renderer or qelectrotech truth`() {
        val anatomy = schematicAnatomy("terminal-block")

        assertFalse(anatomy.hasRendererTruth())
        assertFalse(anatomy.hasQElectroTechRuntimeDependency())
    }

    @Test
    fun `m30 contracts separate policy definition and occurrence`() {
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:electrical-schematic"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            standardProfile = RepresentationStandardProfileId("athena-industrial-control-v0"),
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("iec.motor"),
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            variant = RepresentationVariantId("compact"),
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val definition = RepresentationDefinition(
            symbolId = policy.symbolId,
            libraryId = RepresentationLibraryId("athena.native.iec-v0"),
            version = RepresentationVersion("1.0.0"),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance("athena-m30-native"),
            ),
            kind = RepresentationSymbolKind.MOTOR_LOAD,
            anatomy = schematicAnatomy("motor-load"),
            labelSlots = listOf(
                RepresentationLabelSlot(
                    slotId = RepresentationLabelSlotId("device-tag"),
                    role = PresentationLabelRole.DEVICE_TAG,
                ),
            ),
            styleTokens = listOf(RepresentationStyleToken("stroke", "iec.black")),
        )
        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId("occurrence:MotorM1@sheet1"),
            canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
            projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet1/node/MotorM1"),
            occurrenceRole = policy.occurrenceRole,
            symbolId = definition.symbolId,
            variant = policy.variant,
            labelBindings = listOf(
                RepresentationLabelBinding(
                    slotId = RepresentationLabelSlotId("device-tag"),
                    value = LabelValue("M1"),
                ),
            ),
            terminalBindings = listOf(
                RepresentationTerminalBinding(
                    terminalId = PresentationTerminalId("motor-load-t1"),
                    semanticPortId = SemanticPortId("MotorM1.power"),
                ),
            ),
            referenceBindings = listOf(
                RepresentationReferenceBinding(
                    referenceId = RepresentationReferenceId("reference:MotorM1:location"),
                    kind = RepresentationReferenceKind.COMPONENT_LOCATION,
                    targetSemanticId = RepresentationSubjectId("location:Field"),
                ),
            ),
            compositionIntentMembership = listOf(CompositionIntentMembershipId("lane:power-loads")),
        )

        assertEquals(SymbolFamilyId("iec.motor"), policy.symbolFamilyId)
        assertEquals(RepresentationOccurrenceRole.LOAD_SYMBOL, policy.occurrenceRole)
        assertEquals(RepresentationLifecycleState.ACTIVE, definition.lifecycle.state)
        assertEquals(definition.symbolId, occurrence.symbolId)
        assertEquals(RepresentationSubjectId("component:MotorM1"), occurrence.canonicalSemanticId)
        assertEquals(RepresentationProjectionOccurrenceId("sheet1/node/MotorM1"), occurrence.projectionOccurrenceId)
        assertEquals(RepresentationReferenceKind.COMPONENT_LOCATION, occurrence.referenceBindings.single().kind)
    }

    @Test
    fun `m30 diagnostics are stable transport safe contract values`() {
        val diagnostics = listOf(
            RepresentationDiagnosticCode.SYMBOL_MISSING,
            RepresentationDiagnosticCode.SYMBOL_UNSUPPORTED_ROLE,
            RepresentationDiagnosticCode.ANCHOR_MISSING,
            RepresentationDiagnosticCode.TERMINAL_INCOMPATIBLE,
            RepresentationDiagnosticCode.LABEL_SLOT_MISSING,
            RepresentationDiagnosticCode.BINDING_AMBIGUOUS,
            RepresentationDiagnosticCode.POLICY_MISSING,
            RepresentationDiagnosticCode.LIFECYCLE_UNSUPPORTED,
        ).map { code ->
            RepresentationDiagnostic(
                code = code,
                message = "diagnostic:${code.wireValue}",
                subjectId = RepresentationSubjectId("component:MotorM1"),
            )
        }

        assertTrue(diagnostics.all { diagnostic -> diagnostic.code.wireValue.startsWith("representation.") })
        assertEquals(diagnostics.map { it.code.wireValue }, diagnostics.map { it.toTransportMap()["code"] })
    }

    @Test
    fun `m30 policy definition and occurrence expose deterministic transport maps`() {
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:electrical-schematic"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            standardProfile = RepresentationStandardProfileId("athena-industrial-control-v0"),
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("iec.motor"),
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            variant = RepresentationVariantId("compact"),
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            libraryId = RepresentationLibraryId("athena.native.iec-v0"),
            version = RepresentationVersion("1.0.0"),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance("athena-m30-native"),
            ),
            kind = RepresentationSymbolKind.MOTOR_LOAD,
            anatomy = schematicAnatomy("motor-load"),
            labelSlots = listOf(
                RepresentationLabelSlot(
                    slotId = RepresentationLabelSlotId("device-tag"),
                    role = PresentationLabelRole.DEVICE_TAG,
                ),
            ),
        )
        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId("occurrence:MotorM1@sheet1"),
            canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
            projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet1/node/MotorM1"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            variant = RepresentationVariantId("compact"),
            labelBindings = listOf(
                RepresentationLabelBinding(
                    slotId = RepresentationLabelSlotId("device-tag"),
                    value = LabelValue("M1"),
                ),
            ),
            terminalBindings = listOf(
                RepresentationTerminalBinding(
                    terminalId = PresentationTerminalId("motor-load-t1"),
                    semanticPortId = SemanticPortId("MotorM1.power"),
                ),
            ),
        )

        assertEquals(
            listOf(
                "fallback",
                "occurrenceRole",
                "policyId",
                "priority",
                "projectionKind",
                "semanticRole",
                "standardProfile",
                "subjectKind",
                "symbolFamilyId",
                "symbolId",
                "variant",
            ),
            policy.toTransportMap().keys.toList(),
        )
        assertEquals("athena.native.iec-v0", definition.toTransportMap()["libraryId"])
        assertEquals("component:MotorM1", occurrence.toTransportMap()["canonicalSemanticId"])
    }

    private fun schematicAnatomy(family: String): PresentationAnatomy = PresentationAnatomy(
        representationId = RepresentationId("athena-industrial-control-v0:$family"),
        context = RepresentationContext.ELECTRICAL_SCHEMATIC,
        bounds = PresentationBounds(width = GridUnit(80), height = GridUnit(48)),
        hotspot = PresentationHotspot(point = PresentationPoint(GridUnit(0), GridUnit(0))),
        primitives = listOf(
            PresentationPrimitive.Rectangle(
                primitiveId = PresentationPrimitiveId("$family-body"),
                origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                size = PresentationSize(GridUnit(80), GridUnit(48)),
            ),
        ),
        terminals = listOf(
            PresentationTerminalPoint(
                terminalId = PresentationTerminalId("$family-t1"),
                role = TerminalPresentationRole.DIGITAL_OUTPUT,
                localPoint = PresentationPoint(GridUnit(80), GridUnit(24)),
                side = PresentationSide.RIGHT,
                notation = TerminalNotation(
                    marker = TerminalMarker.CIRCLE,
                    number = TerminalNumber("1"),
                ),
            ),
        ),
        labelAnchors = listOf(
            PresentationLabelAnchor(
                anchorId = PresentationLabelAnchorId("$family-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
                point = PresentationPoint(GridUnit(0), GridUnit(-12)),
            ),
        ),
    )
}
