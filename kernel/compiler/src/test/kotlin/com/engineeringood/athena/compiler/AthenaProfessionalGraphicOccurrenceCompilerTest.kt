package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.DrawingGridPosition
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.PresentationStyleProfileId
import com.engineeringood.athena.packageplatform.RepresentationAnchorId as PackageAnchorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationVariantId as PackageVariantId
import com.engineeringood.athena.packageruntime.BindingResolution
import com.engineeringood.athena.presentation.PresentationDrawingBounds
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
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.RepresentationVersion
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaProfessionalGraphicOccurrenceCompilerTest {
    @Test
    fun `body and anchors use one sheet transform`() {
        val compiled = AthenaProfessionalGraphicOccurrenceCompiler.compile(
            occurrenceId = "component:K1",
            material = material(),
            gridPosition = DrawingGridPosition(column = 1, row = 1),
            orientation = LayoutOrientation.VERTICAL,
            drawingArea = PresentationDrawingBounds(100, 200, 200, 100),
            policy = AthenaProfessionalDrawingPolicy(
                policyId = "test",
                sheetWidth = 400,
                sheetHeight = 300,
                frameToSheet = 0,
                coordinateBandSize = 0,
                titleBlockHeight = 20,
                columnLabels = listOf("A", "B"),
                rowLabels = listOf("1"),
                occurrenceHorizontalPadding = 0,
                occurrenceVerticalPadding = 0,
                powerRegionColumnCount = 1,
                author = "test",
                title = "test",
                publicationDate = "2026-07-31",
                folio = "1",
            ),
        )

        assertEquals(PresentationDrawingBounds(100, 225, 100, 50), compiled.bounds)
        assertEquals(GraphicBounds(100.0, 225.0, 100.0, 50.0), compiled.graphic.bounds)
        assertEquals(GraphicPoint(100.0, 250.0), compiled.anchors.getValue("A1"))
        assertEquals(GraphicPoint(200.0, 250.0), compiled.anchors.getValue("A2"))
    }

    private fun material(): AthenaResolvedRepresentationMaterial = AthenaResolvedRepresentationMaterial(
        semanticSubjectId = "component:K1",
        physicalComponentId = "component:K1",
        functionId = null,
        definition = definition(),
        resolution = BindingResolution(
            semanticSubjectId = "component:K1",
            engineeringPackageId = EngineeringPackageId("engineering.test"),
            presentationProfileId = PresentationProfileId("profile.test"),
            representationPackageId = RepresentationPackageId("representation.test"),
            descriptorId = RepresentationDescriptorId("contactor"),
            variantId = PackageVariantId("standard"),
            anchorMapping = mapOf(
                "port:K1.A1" to PackageAnchorId("A1"),
                "port:K1.A2" to PackageAnchorId("A2"),
            ),
            labelBinding = emptyMap<com.engineeringood.athena.packageplatform.RepresentationLabelSlotId, String>(),
            styleProfile = PresentationStyleProfileId("style.test"),
        ),
        terminalBindings = mapOf(
            "port:K1.A1" to "A1",
            "port:K1.A2" to "A2",
        ),
    )

    private fun definition(): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId("test.contactor"),
        libraryId = RepresentationLibraryId("athena.test"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("source:test.athena:1:1"),
        ),
        kind = RepresentationSymbolKind.SWITCH_CONTACT,
        labelSlots = emptyList(),
        variants = listOf(RepresentationVariantId("standard")),
        definitionKind = RepresentationDefinitionKind.ELEMENT,
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("test.contactor"),
            bounds = GraphicBounds(0.0, 0.0, 100.0, 50.0),
            primitives = listOf(
                GraphicPrimitive.Rectangle(
                    primitiveId = GraphicPrimitiveId("body"),
                    bounds = GraphicBounds(0.0, 0.0, 100.0, 50.0),
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
                anchorId = RepresentationAnchorId("A1"),
                geometryRef = "anchor:A1",
                primitiveId = GraphicPrimitiveId("body"),
                point = GraphicPoint(0.0, 25.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
            RepresentationAnchorContract(
                anchorId = RepresentationAnchorId("A2"),
                geometryRef = "anchor:A2",
                primitiveId = GraphicPrimitiveId("body"),
                point = GraphicPoint(100.0, 25.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
        ),
    )
}
