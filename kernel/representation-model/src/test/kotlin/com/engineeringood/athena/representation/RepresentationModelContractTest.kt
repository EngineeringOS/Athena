package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepresentationModelContractTest {
    @Test
    fun `policy definition and occurrence expose deterministic current transport maps`() {
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:electrical-schematic"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            standardProfile = RepresentationStandardProfileId("athena-industrial-control"),
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("iec.motor"),
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            variant = RepresentationVariantId("compact"),
            priority = RepresentationPolicyPriority(100),
        )
        val definition = currentDefinition(policy.symbolId)
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
            portAnchorBindings = listOf(
                RepresentationPortAnchorBinding(
                    bindingId = RepresentationPortAnchorBindingId("binding:MotorM1.power:motor-load-t1"),
                    semanticPortId = SemanticPortId("MotorM1.power"),
                    anchorId = RepresentationAnchorId("motor-load-t1"),
                    provenance = RepresentationProvenance("source:motor.athena:7:5"),
                ),
            ),
        )

        assertEquals(
            listOf(
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
        assertEquals("athena.native.iec", definition.toTransportMap()["libraryId"])
        assertEquals("component:MotorM1", occurrence.toTransportMap()["canonicalSemanticId"])
        assertTrue(definition.graphicBody.primitives.isNotEmpty())
        assertEquals(listOf("motor-load-t1"), definition.anchors.map { it.anchorId.value })
    }

    @Test
    fun `diagnostics remain stable transport safe contract values`() {
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

    private fun currentDefinition(symbolId: RepresentationSymbolId): RepresentationDefinition = RepresentationDefinition(
        symbolId = symbolId,
        libraryId = RepresentationLibraryId("athena.native.iec"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("athena-current-native"),
        ),
        kind = RepresentationSymbolKind.MOTOR_LOAD,
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(symbolId.value),
            bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
            primitives = listOf(
                GraphicPrimitive.Rectangle(
                    primitiveId = GraphicPrimitiveId("body"),
                    bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
                    cornerRadius = 0.0,
                    styleTokenId = GraphicStyleTokenId("stroke"),
                ),
            ),
            styleTokens = listOf(
                GraphicStyleToken(
                    styleTokenId = GraphicStyleTokenId("stroke"),
                    stroke = GraphicPaintToken("foreground"),
                    strokeWidth = 1.0,
                    fill = GraphicFill.TRANSPARENT,
                    lineCap = GraphicLineCap.BUTT,
                    lineJoin = GraphicLineJoin.MITER,
                ),
            ),
        ),
        anchors = listOf(
            RepresentationAnchorContract(
                anchorId = RepresentationAnchorId("motor-load-t1"),
                geometryRef = "body",
                primitiveId = GraphicPrimitiveId("body"),
                point = GraphicPoint(0.0, 24.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
        ),
    )
}
