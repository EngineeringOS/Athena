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
        assertEquals(RepresentationVersion("1.0.0"), definition.version)
        assertEquals(listOf("device-tag"), definition.labelSlots.map { it.slotId.value })
    }

    @Test
    fun `representation contract has no stale authority fork or semantic anchor fields`() {
        val staleTopLevelTypes = listOf(
            "Presentation" + "Anatomy",
            "Presentation" + "Anatomy" + "Authority",
            "Representation" + "Body" + "Authority",
            "Representation" + "Fallback" + "Behavior",
        )

        staleTopLevelTypes.forEach { typeName ->
            assertFailsWith<ClassNotFoundException>(typeName) {
                Class.forName("com.engineeringood.athena.representation.$typeName")
            }
        }

        val definitionParameterNames = RepresentationDefinition::class.java.declaredFields.map { it.name }.toSet()
        val anchorParameterNames = RepresentationAnchorContract::class.java.declaredFields.map { it.name }.toSet()

        val staleDefinitionFields = setOf("ana" + "tomy", "body" + "Authority")
        val staleAnchorFields = setOf(
            "accepted" + "Directions",
            "accepted" + "Signals",
            "terminal",
            "port",
        )

        assertTrue(definitionParameterNames.intersect(staleDefinitionFields).isEmpty(), definitionParameterNames.toString())
        assertTrue(anchorParameterNames.intersect(staleAnchorFields).isEmpty(), anchorParameterNames.toString())
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
        val anchorId = RepresentationAnchorId("line")
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:switch"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            subjectKind = RepresentationSubjectKind.COMPONENT,
            semanticRole = RepresentationSemanticRole("switch-contact"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolFamilyId = SymbolFamilyId("iec.switch"),
            symbolId = definition.symbolId,
            priority = RepresentationPolicyPriority(100),
        )
        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId("occurrence:switch"),
            canonicalSemanticId = RepresentationSubjectId("device:SwitchS1"),
            projectionOccurrenceId = RepresentationProjectionOccurrenceId("cabinet:switch"),
            occurrenceRole = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolId = definition.symbolId,
            portAnchorBindings = listOf(
                RepresentationPortAnchorBinding(
                    bindingId = RepresentationPortAnchorBindingId("binding:SwitchS1.line:line"),
                    semanticPortId = portId,
                    anchorId = anchorId,
                    provenance = RepresentationProvenance("source:switch.athena:4:5"),
                ),
            ),
        )

        val result = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = listOf(policy),
                definitions = listOf(definition),
                occurrences = listOf(occurrence),
                compatiblePortAnchorBindings = setOf(RepresentationCompatiblePortAnchorBinding(portId, anchorId)),
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
        labelSlots = listOf(
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId("device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
            ),
        ),
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
        geometryRef = "body",
        primitiveId = GraphicPrimitiveId("body"),
        point = GraphicPoint(8.0, 24.0),
        role = RepresentationAnchorRole.TERMINAL,
        required = true,
    )

    private fun child(id: String): RepresentationCompositionChild = RepresentationCompositionChild(
        childId = RepresentationCompositionChildId(id),
        symbolId = RepresentationSymbolId("iec.switch.contact"),
        zOrder = 0,
        transforms = emptyList(),
    )

}
