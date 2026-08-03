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
                portAnchorBindings = listOf(portAnchorBinding("MotorM1.power", "u1")),
                projectPorts = listOf(
                    RepresentationProjectPortFact(
                        semanticPortId = SemanticPortId("MotorM1.power"),
                        role = RepresentationAnchorRole.TERMINAL,
                        direction = RepresentationDirectionPredicate.IN,
                        signal = RepresentationSignalPredicate("Power"),
                        terminal = PhysicalTerminalId("U1"),
                        provenance = RepresentationProvenance("src/motor.athena:7:5"),
                    ),
                ),
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
        assertEquals(SemanticPortId("MotorM1.power"), occurrence.portAnchorBindings.single().semanticPortId)
        assertEquals(RepresentationAnchorId("u1"), occurrence.portAnchorBindings.single().anchorId)
    }

    @Test
    fun `binding validates authored project port facts against element anchors`() {
        val result = RepresentationBindingCompiler().bind(
            compatibleGraphicRequest(
                portAnchorBindings = listOf(portAnchorBinding("MotorM1.power", "u1")),
                projectPorts = listOf(
                    RepresentationProjectPortFact(
                        semanticPortId = SemanticPortId("MotorM1.power"),
                        role = RepresentationAnchorRole.TERMINAL,
                        direction = RepresentationDirectionPredicate.IN,
                        signal = RepresentationSignalPredicate("Power"),
                        terminal = PhysicalTerminalId("U1"),
                        provenance = RepresentationProvenance("src/01-native-cabinet-proof.athena:12:7"),
                    ),
                ),
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(SemanticPortId("MotorM1.power"), result.occurrence.portAnchorBindings.single().semanticPortId)
    }

    @Test
    fun `binding rejects missing and unproven project port anchors without fallback`() {
        val cases = listOf(
            compatibleGraphicRequest(
                portAnchorBindings = listOf(portAnchorBinding("MotorM1.power", "missing")),
                projectPorts = listOf(powerPort()),
            ) to "representation.anchor.missing",
            compatibleGraphicRequest(
                portAnchorBindings = listOf(portAnchorBinding("MotorM1.power", "u1")),
                projectPorts = emptyList(),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(
                portAnchorBindings = emptyList(),
                projectPorts = listOf(powerPort()),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(labelValues = emptyMap()) to "representation.label-slot.missing",
        )

        cases.forEach { (request, expectedCode) ->
            val result = RepresentationBindingCompiler().bind(request)

            assertEquals(null, result.occurrenceOrNull, expectedCode)
            assertTrue(
                result.diagnostics.any { diagnostic -> diagnostic.code.wireValue == expectedCode },
                "Expected $expectedCode, got ${result.diagnostics}",
            )
        }
    }

    @Test
    fun `binding rejects duplicate explicit port or anchor bindings`() {
        val cases = listOf(
            compatibleGraphicRequest(
                portAnchorBindings = listOf(
                    portAnchorBinding("MotorM1.power", "u1", "bind:1"),
                    portAnchorBinding("MotorM1.power", "u2", "bind:2"),
                ),
                projectPorts = listOf(powerPort()),
                definition = elementDefinition().copy(
                    anchors = elementDefinition().anchors + RepresentationAnchorContract(
                        anchorId = RepresentationAnchorId("u2"),
                        geometryRef = "u2-terminal",
                        primitiveId = GraphicPrimitiveId("u1-terminal"),
                        point = GraphicPoint(100.0, 30.0),
                        role = RepresentationAnchorRole.TERMINAL,
                        required = true,
                    ),
                ),
            ) to "representation.binding.ambiguous",
            compatibleGraphicRequest(
                portAnchorBindings = listOf(
                    portAnchorBinding("MotorM1.power", "u1", "bind:1"),
                    portAnchorBinding("MotorM1.control", "u1", "bind:2"),
                ),
                projectPorts = listOf(powerPort(), powerPort("MotorM1.control")),
            ) to "representation.anchor.duplicate",
            compatibleGraphicRequest(
                portAnchorBindings = listOf(
                    portAnchorBinding("MotorM1.power", "u1", "bind:1"),
                    portAnchorBinding("MotorM1.control", "u2", "bind:1"),
                ),
                projectPorts = listOf(powerPort(), powerPort("MotorM1.control")),
                definition = elementDefinition().copy(
                    anchors = elementDefinition().anchors + RepresentationAnchorContract(
                        anchorId = RepresentationAnchorId("u2"),
                        geometryRef = "u2-terminal",
                        primitiveId = GraphicPrimitiveId("u1-terminal"),
                        point = GraphicPoint(100.0, 30.0),
                        role = RepresentationAnchorRole.TERMINAL,
                        required = true,
                    ),
                ),
            ) to "representation.binding.ambiguous",
        )

        cases.forEach { (request, expectedCode) ->
            val result = RepresentationBindingCompiler().bind(request)

            assertEquals(null, result.occurrenceOrNull)
            assertTrue(
                result.diagnostics.any { diagnostic -> diagnostic.code.wireValue == expectedCode },
                "Expected $expectedCode, got ${result.diagnostics}",
            )
        }
    }

    @Test
    fun `binding never infers port anchor binding from matching names`() {
        val result = RepresentationBindingCompiler().bind(
            compatibleGraphicRequest(
                portAnchorBindings = emptyList(),
                projectPorts = listOf(powerPort(portId = "u1")),
            ),
        )

        assertEquals(null, result.occurrenceOrNull)
        assertTrue(
            result.diagnostics.any { diagnostic ->
                diagnostic.code.wireValue == "representation.terminal.incompatible" &&
                    diagnostic.message.contains("requires one explicit Port-to-Anchor binding")
            },
            "Expected explicit binding diagnostic, got ${result.diagnostics}",
        )
    }

    @Test
    fun `binding rejects representation definitions that claim project port authority`() {
        val result = RepresentationBindingCompiler().bind(
            compatibleGraphicRequest(
                projectPorts = listOf(powerPort()),
            ).copy(
                definition = elementDefinition().copy(
                    forbiddenAuthorityClaims = setOf(RepresentationDefinitionForbiddenAuthority.PROJECT_PORT),
                ),
            ),
        )

        assertEquals(null, result.occurrenceOrNull)
        assertEquals(
            listOf("representation.source-authority.violation"),
            result.diagnostics.map { diagnostic -> diagnostic.code.wireValue },
        )
    }

    @Test
    fun `binding emits diagnostics when policy and definition disagree`() {
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
                    priority = RepresentationPolicyPriority(100),
                ),
                definition = motorDefinition(),
                labelValues = mapOf(RepresentationLabelSlotId("device-tag") to LabelValue("M1")),
                portAnchorBindings = emptyList(),
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
        libraryId = RepresentationLibraryId("athena.native.iec"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("test"),
        ),
        kind = RepresentationSymbolKind.MOTOR_LOAD,
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        variants = listOf(RepresentationVariantId("compact")),
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("iec.motor.compact"),
            bounds = GraphicBounds(0.0, 0.0, 44.0, 44.0),
            primitives = listOf(
                GraphicPrimitive.Circle(
                    primitiveId = GraphicPrimitiveId("body"),
                    bounds = GraphicBounds(7.0, 7.0, 30.0, 30.0),
                    center = GraphicPoint(22.0, 22.0),
                    radius = 15.0,
                    styleTokenId = GraphicStyleTokenId("stroke"),
                ),
            ),
            styleTokens = listOf(defaultStroke()),
        ),
        anchors = listOf(
            RepresentationAnchorContract(
                anchorId = RepresentationAnchorId("u1"),
                geometryRef = "body",
                primitiveId = GraphicPrimitiveId("body"),
                point = GraphicPoint(0.0, 22.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
        ),
    )

    private fun compatibleGraphicRequest(
        labelValues: Map<RepresentationLabelSlotId, LabelValue> =
            mapOf(RepresentationLabelSlotId("device-tag") to LabelValue("M1")),
        portAnchorBindings: List<RepresentationPortAnchorBinding> =
            listOf(portAnchorBinding("MotorM1.power", "u1")),
        projectPorts: List<RepresentationProjectPortFact> = listOf(powerPort()),
        definition: RepresentationDefinition = elementDefinition(),
    ): RepresentationBindingRequest = RepresentationBindingRequest(
        canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
        projectionOccurrenceId = RepresentationProjectionOccurrenceId("cabinet:main/occurrence:motor"),
        subjectKind = RepresentationSubjectKind.COMPONENT,
        semanticRole = RepresentationSemanticRole("power-load"),
        projectionKind = RepresentationProjectionKind.CABINET,
        policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:motor-cabinet"),
            projectionKind = RepresentationProjectionKind.CABINET,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("power-load"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolFamilyId = SymbolFamilyId("athena.element.motor"),
            symbolId = RepresentationSymbolId("athena.iec.motor-cabinet-element"),
            variant = RepresentationVariantId("cabinet"),
            priority = RepresentationPolicyPriority(100),
        ),
        definition = definition,
        labelValues = labelValues,
        portAnchorBindings = portAnchorBindings,
        projectPorts = projectPorts,
        priority = RepresentationPolicyPriority(100),
    )

    private fun powerPort(
        portId: String = "MotorM1.power",
        direction: RepresentationDirectionPredicate = RepresentationDirectionPredicate.IN,
        signal: RepresentationSignalPredicate = RepresentationSignalPredicate("Power"),
        terminal: PhysicalTerminalId = PhysicalTerminalId("U1"),
    ): RepresentationProjectPortFact = RepresentationProjectPortFact(
        semanticPortId = SemanticPortId(portId),
        role = RepresentationAnchorRole.TERMINAL,
        direction = direction,
        signal = signal,
        terminal = terminal,
        provenance = RepresentationProvenance("src/01-native-cabinet-proof.athena:12:7"),
    )

    private fun portAnchorBinding(
        portId: String,
        anchorId: String,
        bindingId: String = "binding:$portId:$anchorId",
    ): RepresentationPortAnchorBinding = RepresentationPortAnchorBinding(
        bindingId = RepresentationPortAnchorBindingId(bindingId),
        semanticPortId = SemanticPortId(portId),
        anchorId = RepresentationAnchorId(anchorId),
        provenance = RepresentationProvenance("src/01-native-cabinet-proof.athena:13:9"),
    )

    private fun elementDefinition(): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId("athena.iec.motor-cabinet-element"),
        libraryId = RepresentationLibraryId("athena.native.iec"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("test/element.athena"),
        ),
        kind = RepresentationSymbolKind.MOTOR_LOAD,
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        variants = listOf(RepresentationVariantId("cabinet")),
        definitionKind = RepresentationDefinitionKind.ELEMENT,
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("athena.iec.motor-cabinet-element"),
            bounds = GraphicBounds(0.0, 0.0, 100.0, 60.0),
            primitives = listOf(
                GraphicPrimitive.Line(
                    primitiveId = GraphicPrimitiveId("u1-terminal"),
                    bounds = GraphicBounds(0.0, 29.5, 20.0, 1.0),
                    start = GraphicPoint(0.0, 30.0),
                    end = GraphicPoint(20.0, 30.0),
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
                anchorId = RepresentationAnchorId("u1"),
                geometryRef = "u1-terminal",
                primitiveId = GraphicPrimitiveId("u1-terminal"),
                point = GraphicPoint(0.0, 30.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
        ),
    )

    private fun defaultStroke(): GraphicStyleToken = GraphicStyleToken(
        styleTokenId = GraphicStyleTokenId("stroke"),
        stroke = GraphicPaintToken("foreground"),
        strokeWidth = 1.0,
        fill = GraphicFill.TRANSPARENT,
        lineCap = GraphicLineCap.BUTT,
        lineJoin = GraphicLineJoin.MITER,
    )
}
