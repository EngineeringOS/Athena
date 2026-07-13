package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ViewDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PresentationModelContractTest {
    @Test
    fun `presentation document stays domain neutral and downstream of view contract`() {
        val document = PresentationDocument(
            view = ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
                layoutIntent = LayoutIntent.STRUCTURAL,
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = listOf(
                PresentationPrimitivePack(
                    packId = PresentationPackId("electrical-primitives/default-v1"),
                    displayName = "Electrical primitives",
                    familyIds = setOf("electrical/cabinet"),
                    primitives = listOf(
                        PresentationPrimitiveDefinition(
                            primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                            displayName = "Open contact mark",
                            viewBoxWidth = 24,
                            viewBoxHeight = 24,
                            commands = listOf(
                                PresentationStrokeLine(
                                    start = PresentationPoint(4, 12),
                                    end = PresentationPoint(20, 12),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            compositePacks = listOf(
                PresentationCompositePack(
                    packId = PresentationPackId("electrical-composites/default-v1"),
                    displayName = "Electrical composites",
                    familyIds = setOf("electrical/cabinet"),
                    composites = listOf(
                        PresentationCompositeDefinition(
                            compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                            displayName = "Switch panel",
                            viewBoxWidth = 140,
                            viewBoxHeight = 72,
                            parts = listOf(
                                PresentationCompositePart(
                                    partId = "contact",
                                    primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                                    bounds = PresentationBounds(x = 84, y = 24, width = 24, height = 24),
                                ),
                            ),
                            textSlots = listOf(
                                PresentationTextSlot(
                                    slotId = PresentationTextSlotId("subject-label"),
                                    origin = PresentationPoint(x = 8, y = 16),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            occurrences = listOf(
                PresentationOccurrence(
                    occurrenceId = PresentationOccurrenceId("cabinet/presentation/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    reference = PresentationCompositeOccurrenceReference(
                        compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                    ),
                    bounds = PresentationBounds(x = 40, y = 60, width = 140, height = 72),
                    layer = PresentationLayer.DEVICE,
                    textValues = mapOf(PresentationTextSlotId("subject-label") to "PLC1"),
                    sourceProjectionIds = listOf("cabinet/projection/node/component_PLC1"),
                ),
            ),
            connectors = listOf(
                PresentationConnector(
                    occurrenceId = PresentationOccurrenceId("cabinet/presentation/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
                    routePoints = listOf(
                        PresentationPoint(x = 104, y = 86),
                        PresentationPoint(x = 210, y = 86),
                        PresentationPoint(x = 316, y = 86),
                    ),
                    sourceProjectionIds = listOf("cabinet/projection/connection/connection_PLC1_out_M1_in"),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("electrical-primitives/default-v1", document.primitivePacks.single().packId.value)
        assertEquals("electrical-composites/default-v1", document.compositePacks.single().packId.value)
        assertEquals("component:PLC1", document.occurrences.single().semanticId.value)
        assertEquals(
            "cabinet/projection/node/component_PLC1",
            document.occurrences.single().sourceProjectionIds.single(),
        )
        assertEquals(
            "cabinet/projection/connection/connection_PLC1_out_M1_in",
            document.connectors.single().sourceProjectionIds.single(),
        )
        assertIs<PresentationCompositeOccurrenceReference>(document.occurrences.single().reference)
    }

    @Test
    fun `presentation layer does not absorb semantic macro or backend draw trees`() {
        val primitive = PresentationPrimitiveDefinition(
            primitiveId = PresentationPrimitiveId("electrical.mark.motor"),
            displayName = "Motor mark",
            viewBoxWidth = 32,
            viewBoxHeight = 32,
            commands = listOf(
                PresentationCircle(
                    center = PresentationPoint(x = 16, y = 16),
                    radius = 10,
                ),
                PresentationSvgPath(
                    pathData = "M 10 10 L 22 22",
                ),
            ),
            tokenDefaults = mapOf(
                "stroke" to "#1f1f1f",
                "strokeWidth" to "1.6",
            ),
        )

        assertTrue(primitive.tokenDefaults.containsKey("stroke"))
        assertEquals(2, primitive.commands.size)
        assertIs<PresentationCircle>(primitive.commands.first())
        assertIs<PresentationSvgPath>(primitive.commands.last())
    }
}
