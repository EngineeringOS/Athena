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
        assertEquals(SemanticPortId("MotorM1.power"), occurrence.terminalBindings.single().semanticPortId)
    }

    @Test
    fun `binding validates authored project port facts against element anchor compatibility`() {
        val result = RepresentationBindingCompiler().bind(
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
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
        assertEquals(SemanticPortId("MotorM1.power"), result.occurrence.terminalBindings.single().semanticPortId)
    }

    @Test
    fun `binding rejects missing incompatible and unproven project port anchors without fallback`() {
        val cases = listOf(
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("missing") to SemanticPortId("MotorM1.power")),
                projectPorts = listOf(powerPort()),
            ) to "representation.anchor.missing",
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
                projectPorts = emptyList(),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
                projectPorts = listOf(powerPort(direction = RepresentationDirectionPredicate.OUT)),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
                projectPorts = listOf(powerPort(signal = RepresentationSignalPredicate("Digital"))),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(
                terminalPorts = mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
                projectPorts = listOf(powerPort(terminal = PhysicalTerminalId("V1"))),
            ) to "representation.terminal.incompatible",
            compatibleGraphicRequest(labelValues = emptyMap()) to "representation.label-slot.missing",
        )

        cases.forEach { (request, expectedCode) ->
            val result = RepresentationBindingCompiler().bind(request)

            assertEquals(null, result.occurrenceOrNull, expectedCode)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code.wireValue == expectedCode &&
                        diagnostic.provenance?.source == "test/element.athena"
                },
                "Expected $expectedCode with representation provenance, got ${result.diagnostics}",
            )
        }
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
                labelValues = mapOf(RepresentationLabelSlotId("device-tag") to LabelValue("M1")),
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
        libraryId = RepresentationLibraryId("athena.native.iec"),
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

    private fun compatibleGraphicRequest(
        labelValues: Map<RepresentationLabelSlotId, LabelValue> =
            mapOf(RepresentationLabelSlotId("device-tag") to LabelValue("M1")),
        terminalPorts: Map<PresentationTerminalId, SemanticPortId> =
            mapOf(PresentationTerminalId("u1") to SemanticPortId("MotorM1.power")),
        projectPorts: List<RepresentationProjectPortFact> = listOf(powerPort()),
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
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        ),
        definition = elementDefinition(),
        labelValues = labelValues,
        terminalPorts = terminalPorts,
        projectPorts = projectPorts,
        priority = RepresentationPolicyPriority(100),
    )

    private fun powerPort(
        direction: RepresentationDirectionPredicate = RepresentationDirectionPredicate.IN,
        signal: RepresentationSignalPredicate = RepresentationSignalPredicate("Power"),
        terminal: PhysicalTerminalId = PhysicalTerminalId("U1"),
    ): RepresentationProjectPortFact = RepresentationProjectPortFact(
        semanticPortId = SemanticPortId("MotorM1.power"),
        role = RepresentationAnchorRole.TERMINAL,
        direction = direction,
        signal = signal,
        terminal = terminal,
        provenance = RepresentationProvenance("src/01-native-cabinet-proof.athena:12:7"),
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
        anatomy = PresentationAnatomy(
            representationId = RepresentationId("athena.iec.motor-cabinet-element"),
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(100), GridUnit(60)),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = emptyList(),
            terminals = emptyList(),
            labelAnchors = emptyList(),
            authority = PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
        ),
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        variants = listOf(RepresentationVariantId("cabinet")),
        bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
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
                acceptedDirections = setOf(RepresentationDirectionPredicate.IN),
                acceptedSignals = setOf(RepresentationSignalPredicate("Power")),
                terminal = PhysicalTerminalId("U1"),
            ),
        ),
    )
}
