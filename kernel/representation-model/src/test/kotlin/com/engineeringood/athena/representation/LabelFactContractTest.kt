package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LabelFactContractTest {
    @Test
    fun `label facts carry identity role value anchor and source identity`() {
        val label = LabelFact(
            labelId = LabelFactId("label:PLC1:tag"),
            subjectId = RepresentationSubjectId("PLC1"),
            occurrenceId = RepresentationOccurrenceId("PLC1@schematic-sheet"),
            role = PresentationLabelRole.DEVICE_TAG,
            value = LabelValue("PLC1"),
            anchor = PresentationLabelAnchor(
                anchorId = PresentationLabelAnchorId("PLC1-device-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
                point = PresentationPoint(GridUnit(0), GridUnit(-12)),
            ),
            sourceIdentity = PresentationSourceIdentity(
                sourceUnit = "src/main.athena",
                span = SourceSpanRef(startLine = 2, startColumn = 1, endLine = 2, endColumn = 12),
            ),
        )

        assertEquals("PLC1", label.subjectId.value)
        assertEquals(PresentationLabelRole.DEVICE_TAG, label.role)
        assertEquals("PLC1", label.value.value)
        assertEquals("src/main.athena", label.sourceIdentity?.sourceUnit)
        assertFalse(label.rendererTextAuthority)
    }

    @Test
    fun `label policy covers M25 label roles with deterministic anchors`() {
        val policy = LabelPolicy.defaultIndustrialControl()
        val roles = listOf(
            PresentationLabelRole.DEVICE_TAG,
            PresentationLabelRole.COMPONENT_LABEL,
            PresentationLabelRole.TERMINAL_LABEL,
            PresentationLabelRole.ROUTE_LABEL,
        )

        val anchors = roles.map { role ->
            policy.anchorFor(
                role = role,
                subjectId = RepresentationSubjectId("PLC1"),
                occurrenceId = RepresentationOccurrenceId("PLC1@schematic-sheet"),
            )
        }
        val repeatedAnchors = roles.map { role ->
            policy.anchorFor(
                role = role,
                subjectId = RepresentationSubjectId("PLC1"),
                occurrenceId = RepresentationOccurrenceId("PLC1@schematic-sheet"),
            )
        }

        assertEquals(roles, anchors.map { it.role })
        assertEquals(anchors, repeatedAnchors)
    }
}
