package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.presentation.PresentationAnchorAlias
import com.engineeringood.athena.presentation.PresentationBounds
import com.engineeringood.athena.presentation.PresentationCircle
import com.engineeringood.athena.presentation.PresentationCompositeDefinition
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationCompositePart
import com.engineeringood.athena.presentation.PresentationCompositeId
import com.engineeringood.athena.presentation.PresentationOrientation
import com.engineeringood.athena.presentation.PresentationPackId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveDefinition
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationStrokeLine
import com.engineeringood.athena.presentation.PresentationStrokeRectangle
import com.engineeringood.athena.presentation.PresentationSvgPath
import com.engineeringood.athena.presentation.PresentationTextSlot
import com.engineeringood.athena.presentation.PresentationTextSlotId

private val ELECTRICAL_PRESENTATION_FAMILY_IDS = setOf(
    "electrical/cabinet",
    "electrical/wiring",
    "electrical/schematic",
    "electrical/documentation",
)

private val ELECTRICAL_PANEL_PRESENTATION_FAMILY_IDS = setOf(
    "electrical/cabinet",
    "electrical/wiring",
    "electrical/documentation",
)

private val ELECTRICAL_SCHEMATIC_PRESENTATION_FAMILY_IDS = setOf(
    "electrical/schematic",
)

internal val ELECTRICAL_PRIMITIVE_PRESENTATION_PACKS = listOf(
    PresentationPrimitivePack(
        packId = PresentationPackId("electrical-primitives/default-v1"),
        displayName = "Electrical primitives",
        familyIds = ELECTRICAL_PRESENTATION_FAMILY_IDS,
        primitives = listOf(
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.frame.device-box"),
                displayName = "Device box",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                commands = listOf(
                    PresentationStrokeRectangle(
                        bounds = PresentationBounds(x = 8, y = 12, width = 124, height = 48),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                displayName = "Open contact mark",
                viewBoxWidth = 32,
                viewBoxHeight = 32,
                commands = listOf(
                    PresentationSvgPath(
                        pathData = "M 8 8 L 8 24 M 24 8 L 24 24 M 8 20 L 24 12",
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.mark.motor"),
                displayName = "Motor mark",
                viewBoxWidth = 32,
                viewBoxHeight = 32,
                commands = listOf(
                    PresentationCircle(
                        center = PresentationPoint(x = 16, y = 16),
                        radius = 10,
                    ),
                    PresentationStrokeLine(
                        start = PresentationPoint(x = 11, y = 21),
                        end = PresentationPoint(x = 11, y = 11),
                    ),
                    PresentationStrokeLine(
                        start = PresentationPoint(x = 11, y = 11),
                        end = PresentationPoint(x = 16, y = 16),
                    ),
                    PresentationStrokeLine(
                        start = PresentationPoint(x = 16, y = 16),
                        end = PresentationPoint(x = 21, y = 11),
                    ),
                    PresentationStrokeLine(
                        start = PresentationPoint(x = 21, y = 11),
                        end = PresentationPoint(x = 21, y = 21),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.mark.breaker"),
                displayName = "Breaker mark",
                viewBoxWidth = 32,
                viewBoxHeight = 32,
                commands = listOf(
                    PresentationSvgPath(
                        pathData = "M 10 6 L 10 26 M 10 8 L 22 20",
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.mark.coil"),
                displayName = "Coil mark",
                viewBoxWidth = 32,
                viewBoxHeight = 32,
                commands = listOf(
                    PresentationCircle(
                        center = PresentationPoint(x = 12, y = 16),
                        radius = 6,
                    ),
                    PresentationCircle(
                        center = PresentationPoint(x = 20, y = 16),
                        radius = 6,
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.label.terminal"),
                displayName = "Terminal label",
                viewBoxWidth = 72,
                viewBoxHeight = 24,
                commands = listOf(
                    PresentationStrokeLine(
                        start = PresentationPoint(x = 0, y = 12),
                        end = PresentationPoint(x = 14, y = 12),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("terminal-label"),
                        origin = PresentationPoint(x = 20, y = 16),
                    ),
                ),
                anchors = listOf(
                    com.engineeringood.athena.presentation.PresentationAnchorDefinition(
                        alias = PresentationAnchorAlias("terminal"),
                        point = PresentationPoint(x = 0, y = 12),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
                displayName = "Orthogonal conductor",
                viewBoxWidth = 1,
                viewBoxHeight = 1,
                commands = emptyList(),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationPrimitiveDefinition(
                primitiveId = PresentationPrimitiveId("electrical.mark.reference"),
                displayName = "Reference mark",
                viewBoxWidth = 84,
                viewBoxHeight = 20,
                commands = listOf(
                    PresentationStrokeRectangle(
                        bounds = PresentationBounds(x = 0, y = 0, width = 84, height = 20),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("reference-label"),
                        origin = PresentationPoint(x = 8, y = 14),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
        ),
    ),
)

internal val ELECTRICAL_COMPOSITE_PRESENTATION_PACKS = listOf(
    PresentationCompositePack(
        packId = PresentationPackId("electrical-composites/panel-v1"),
        displayName = "Electrical panel composites",
        familyIds = ELECTRICAL_PANEL_PRESENTATION_FAMILY_IDS,
        composites = listOf(
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.generic-panel"),
                displayName = "Generic device panel",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "frame",
                        primitiveId = PresentationPrimitiveId("electrical.frame.device-box"),
                        bounds = PresentationBounds(x = 0, y = 0, width = 140, height = 72),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                displayName = "Switch device panel",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "frame",
                        primitiveId = PresentationPrimitiveId("electrical.frame.device-box"),
                        bounds = PresentationBounds(x = 0, y = 0, width = 140, height = 72),
                    ),
                    PresentationCompositePart(
                        partId = "contact-mark",
                        primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                        bounds = PresentationBounds(x = 94, y = 22, width = 24, height = 24),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.motor-panel"),
                displayName = "Motor device panel",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "frame",
                        primitiveId = PresentationPrimitiveId("electrical.frame.device-box"),
                        bounds = PresentationBounds(x = 0, y = 0, width = 140, height = 72),
                    ),
                    PresentationCompositePart(
                        partId = "motor-mark",
                        primitiveId = PresentationPrimitiveId("electrical.mark.motor"),
                        bounds = PresentationBounds(x = 94, y = 20, width = 28, height = 28),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
        ),
    ),
    PresentationCompositePack(
        packId = PresentationPackId("electrical-composites/schematic-v1"),
        displayName = "Electrical schematic composites",
        familyIds = ELECTRICAL_SCHEMATIC_PRESENTATION_FAMILY_IDS,
        composites = listOf(
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.generic-panel"),
                displayName = "Generic device symbol",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "frame",
                        primitiveId = PresentationPrimitiveId("electrical.frame.device-box"),
                        bounds = PresentationBounds(x = 24, y = 12, width = 92, height = 48),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                displayName = "Switch device symbol",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "contact-mark",
                        primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                        bounds = PresentationBounds(x = 50, y = 18, width = 40, height = 40),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
            PresentationCompositeDefinition(
                compositeId = PresentationCompositeId("electrical.device.motor-panel"),
                displayName = "Motor device symbol",
                viewBoxWidth = 140,
                viewBoxHeight = 72,
                parts = listOf(
                    PresentationCompositePart(
                        partId = "motor-mark",
                        primitiveId = PresentationPrimitiveId("electrical.mark.motor"),
                        bounds = PresentationBounds(x = 52, y = 18, width = 36, height = 36),
                    ),
                ),
                textSlots = listOf(
                    PresentationTextSlot(
                        slotId = PresentationTextSlotId("subject-label"),
                        origin = PresentationPoint(x = 12, y = 10),
                    ),
                ),
                tokenDefaults = electricalTokenDefaults(),
            ),
        ),
    ),
)

private fun electricalTokenDefaults(): Map<String, String> {
    return mapOf(
        "stroke" to "#202020",
        "strokeWidth" to "1.6",
        "label" to "#202020",
    )
}
