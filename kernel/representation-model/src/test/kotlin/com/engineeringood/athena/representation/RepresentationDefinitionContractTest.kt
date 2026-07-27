package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RepresentationDefinitionContractTest {
    @Test
    fun `canonical element definition owns typed body anchors composition and provenance`() {
        val definition = elementDefinition()

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = emptyList(),
                definitions = listOf(definition),
                occurrences = emptyList(),
            ),
        )

        assertTrue(result.accepted, result.diagnostics.toString())
        assertEquals(RepresentationDefinitionKind.ELEMENT, definition.definitionKind)
        assertEquals(listOf("body"), definition.graphicBody.primitives.map { it.primitiveId.value })
        assertEquals(listOf("line"), definition.anchors.map { it.anchorId.value })
        assertEquals(listOf("contact"), definition.intrinsicComposition?.children?.map { it.childId.value })
        assertEquals("packages/iec/switch.athena", definition.lifecycle.provenance.source)
        assertEquals(RepresentationBodyAuthority.GRAPHIC_PRIMITIVE, definition.bodyAuthority)
        assertEquals(RepresentationVersion("1.0.0"), definition.version)
        assertEquals(listOf("device-tag"), definition.labelSlots.map { it.slotId.value })
    }

    @Test
    fun `canonical symbol and element share one definition contract without legacy visual authority`() {
        val element = elementDefinition()
        val symbol = element.copy(
            symbolId = RepresentationSymbolId("iec.switch.contact"),
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            intrinsicComposition = null,
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(element.libraryId),
                policies = emptyList(),
                definitions = listOf(symbol, element),
                occurrences = emptyList(),
            ),
        )

        assertTrue(result.accepted, result.diagnostics.toString())
        assertEquals(
            listOf(RepresentationDefinitionKind.SYMBOL, RepresentationDefinitionKind.ELEMENT),
            listOf(symbol, element).map { it.definitionKind },
        )
        assertTrue(listOf(symbol, element).all { it.anatomy.primitives.isEmpty() })
        assertTrue(listOf(symbol, element).all { it.graphicBody.primitives.isNotEmpty() })
    }

    @Test
    fun `canonical graphic symbol may omit labels and retains anchor primitive reference`() {
        val symbol = elementDefinition().copy(
            symbolId = RepresentationSymbolId("iec.switch.contact"),
            kind = RepresentationSymbolKind.GENERIC,
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            labelSlots = emptyList(),
            intrinsicComposition = null,
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(symbol.libraryId),
                policies = emptyList(),
                definitions = listOf(symbol),
                occurrences = emptyList(),
            ),
        )

        assertTrue(result.accepted, result.diagnostics.toString())
        assertEquals(GraphicPrimitiveId("body"), symbol.anchors.single().primitiveId)
    }

    @Test
    fun `canonical anchor contract rejects non finite points`() {
        assertFailsWith<IllegalArgumentException> {
            anchor("line").copy(point = GraphicPoint(Double.NaN, 24.0))
        }
        assertFailsWith<IllegalArgumentException> {
            anchor("line").copy(point = GraphicPoint(8.0, Double.POSITIVE_INFINITY))
        }
    }

    @Test
    fun `canonical body authority rejects independently authored legacy primitives`() {
        assertFailsWith<IllegalArgumentException> {
            elementDefinition().copy(anatomy = legacyAnatomy())
        }
    }

    @Test
    fun `validator rejects project truth claims with stable source provenance`() {
        val definition = elementDefinition().copy(
            forbiddenAuthorityClaims = setOf(
                RepresentationDefinitionForbiddenAuthority.PROJECT_PORT,
                RepresentationDefinitionForbiddenAuthority.DEVICE_CLASSIFICATION,
            ),
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = emptyList(),
                definitions = listOf(definition),
                occurrences = emptyList(),
            ),
        )

        assertFalse(result.accepted)
        assertEquals(
            listOf(
                Triple(
                    "representation.source-authority.violation",
                    "Representation definition `iec.switch.element` cannot claim `DEVICE_CLASSIFICATION` authority.",
                    "packages/iec/switch.athena",
                ),
                Triple(
                    "representation.source-authority.violation",
                    "Representation definition `iec.switch.element` cannot claim `PROJECT_PORT` authority.",
                    "packages/iec/switch.athena",
                ),
            ),
            result.diagnostics.map { diagnostic ->
                Triple(diagnostic.code.wireValue, diagnostic.message, diagnostic.provenance?.source)
            },
        )
    }

    @Test
    fun `canonical graphic body diagnostics retain provenance and reject admission`() {
        val definition = elementDefinition().copy(
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = GraphicPrimitiveDocument(
                documentId = null,
                bounds = null,
                primitives = emptyList(),
                styleTokens = emptyList(),
            ),
            intrinsicComposition = null,
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = emptyList(),
                definitions = listOf(definition),
                occurrences = emptyList(),
            ),
        )

        assertFalse(result.accepted)
        assertEquals(
            listOf(
                "graphic.ir.bounds.missing",
                "graphic.ir.document-id.missing",
                "graphic.ir.primitive.missing",
                "graphic.ir.style-token.missing",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.message.substringBefore(':') },
        )
        assertTrue(result.diagnostics.all { it.code.wireValue == "representation.graphic-body.invalid" })
        assertTrue(result.diagnostics.all { it.provenance?.source == "packages/iec/switch.athena" })
    }

    @Test
    fun `canonical diagnostic ordering is independent of definition order`() {
        val first = elementDefinition().copy(
            forbiddenAuthorityClaims = setOf(RepresentationDefinitionForbiddenAuthority.PROJECT_PORT),
        )
        val second = first.copy(
            symbolId = RepresentationSymbolId("iec.switch.second"),
            lifecycle = first.lifecycle.copy(
                provenance = RepresentationProvenance("packages/iec/second.athena"),
            ),
        )

        fun validate(definitions: List<RepresentationDefinition>) = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(first.libraryId),
                policies = emptyList(),
                definitions = definitions,
                occurrences = emptyList(),
            ),
        ).toTransportPayload()

        assertEquals(validate(listOf(first, second)), validate(listOf(second, first)))
    }

    @Test
    fun `canonical terminal anchors are authoritative for occurrence validation`() {
        val definition = elementDefinition()
        val portId = SemanticPortId("SwitchS1.line")
        val terminalId = PresentationTerminalId("line")
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:switch"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("switch-contact"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolFamilyId = SymbolFamilyId("iec.switch"),
            symbolId = definition.symbolId,
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId("occurrence:switch"),
            canonicalSemanticId = RepresentationSubjectId("device:SwitchS1"),
            projectionOccurrenceId = RepresentationProjectionOccurrenceId("cabinet:switch"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolId = definition.symbolId,
            terminalBindings = listOf(RepresentationTerminalBinding(terminalId, portId)),
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = listOf(policy),
                definitions = listOf(definition),
                occurrences = listOf(occurrence),
                compatibleTerminalBindings = setOf(RepresentationCompatibleTerminalBinding(terminalId, portId)),
            ),
        )

        assertTrue(result.accepted, result.diagnostics.toString())
    }

    @Test
    fun `validator rejects duplicate anchor and child identities deterministically`() {
        val definition = elementDefinition().copy(
            anchors = listOf(anchor("line"), anchor("line")),
            intrinsicComposition = RepresentationIntrinsicComposition(
                children = listOf(child("contact"), child("contact")),
                exportedAnchors = emptyList(),
            ),
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = emptyList(),
                definitions = listOf(definition),
                occurrences = emptyList(),
            ),
        )

        assertEquals(
            listOf(
                "representation.anchor.duplicate",
                "representation.composition-child.duplicate",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    private fun elementDefinition(): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId("iec.switch.element"),
        libraryId = RepresentationLibraryId("athena.native.iec-v1"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("packages/iec/switch.athena"),
        ),
        kind = RepresentationSymbolKind.SWITCH_CONTACT,
        anatomy = compatibilityAnatomy(),
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
        bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
        definitionKind = RepresentationDefinitionKind.ELEMENT,
        graphicBody = graphicBody(),
        anchors = listOf(anchor("line")),
        intrinsicComposition = RepresentationIntrinsicComposition(
            children = listOf(child("contact")),
            exportedAnchors = listOf(
                RepresentationExportedAnchor(
                    anchorId = RepresentationAnchorId("line"),
                    childId = RepresentationCompositionChildId("contact"),
                    childAnchorId = RepresentationAnchorId("line"),
                ),
            ),
        ),
    )

    private fun graphicBody(): GraphicPrimitiveDocument = GraphicPrimitiveDocument(
        documentId = GraphicPrimitiveDocumentId("iec.switch.element"),
        bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
        primitives = listOf(
            GraphicPrimitive.Line(
                primitiveId = GraphicPrimitiveId("body"),
                bounds = GraphicBounds(8.0, 24.0, 64.0, 1.0),
                start = GraphicPoint(8.0, 24.0),
                end = GraphicPoint(72.0, 24.0),
                styleTokenId = GraphicStyleTokenId("drawing.default"),
            ),
        ),
        styleTokens = listOf(
            GraphicStyleToken(
                styleTokenId = GraphicStyleTokenId("drawing.default"),
                stroke = GraphicPaintToken("drawing.foreground"),
                strokeWidth = 1.5,
                fill = GraphicFill.TRANSPARENT,
                lineCap = GraphicLineCap.ROUND,
                lineJoin = GraphicLineJoin.ROUND,
            ),
        ),
    )

    private fun anchor(id: String): RepresentationAnchorContract = RepresentationAnchorContract(
        anchorId = RepresentationAnchorId(id),
        primitiveId = GraphicPrimitiveId("body"),
        point = GraphicPoint(8.0, 24.0),
        role = RepresentationAnchorRole.TERMINAL,
        required = true,
        acceptedDirections = setOf(RepresentationDirectionPredicate.IN),
        acceptedSignals = setOf(RepresentationSignalPredicate("Power")),
    )

    private fun child(id: String): RepresentationCompositionChild = RepresentationCompositionChild(
        childId = RepresentationCompositionChildId(id),
        symbolId = RepresentationSymbolId("iec.switch.contact"),
        zOrder = 0,
        transforms = emptyList(),
    )

    private fun compatibilityAnatomy(): PresentationAnatomy = PresentationAnatomy(
        representationId = RepresentationId("iec.switch.element"),
        context = RepresentationContext.ELECTRICAL_SCHEMATIC,
        bounds = PresentationBounds(GridUnit(80), GridUnit(48)),
        hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
        primitives = emptyList(),
        terminals = emptyList(),
        labelAnchors = emptyList(),
        authority = PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
    )

    private fun legacyAnatomy(): PresentationAnatomy = compatibilityAnatomy().copy(
        primitives = listOf(
            PresentationPrimitive.Line(
                primitiveId = PresentationPrimitiveId("legacy-body"),
                start = PresentationPoint(GridUnit(8), GridUnit(24)),
                end = PresentationPoint(GridUnit(72), GridUnit(24)),
            ),
        ),
    )
}
