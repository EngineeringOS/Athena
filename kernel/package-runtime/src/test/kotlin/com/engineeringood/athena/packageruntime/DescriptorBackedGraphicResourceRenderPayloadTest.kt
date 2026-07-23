package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationDescriptorBounds
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptorResourceBinding
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotDefinition
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DescriptorBackedGraphicResourceRenderPayloadTest {
    @Test
    fun `render payload carries descriptor resource bounds anchors labels and transient chrome`() {
        val payload = DescriptorBackedGraphicResourceRenderPayloadMapper.from(
            evidence = evidence(),
            descriptor = descriptor(),
            governedMargin = 16.0,
            interactionState = "normal",
        )

        assertEquals("device:DriveA", payload.semanticSubjectId)
        assertEquals("resource.drive.iec", payload.resourceHandle)
        assertEquals("VECTOR_DOCUMENT", payload.resourceKind)
        assertEquals(80.0, payload.bounds.width)
        assertEquals(48.0, payload.bounds.height)
        assertEquals(listOf("power=8.0,24.0,LEFT"), payload.anchorSummary)
        assertEquals(listOf("device-tag=DriveA"), payload.labelSummary)
        assertEquals("normal", payload.interactionState)
        assertFalse(payload.normalBackgroundVisible)
        assertFalse(payload.normalHitboxVisible)
        assertEquals("transient", payload.interactionChrome)
        assertEquals(DescriptorBackedViewBoxPayload(0.0, 0.0, 112.0, 80.0), payload.viewBox)
    }

    @Test
    fun `render payload proof prevents duplicate labels and resource semantic inference`() {
        val payload = DescriptorBackedGraphicResourceRenderPayloadMapper.from(
            evidence = evidence().copy(labelBindingSummary = listOf("device-tag=DriveA", "device-tag=DriveA")),
            descriptor = descriptor(),
            governedMargin = 16.0,
            interactionState = "selected",
        )

        assertEquals(listOf("device-tag=DriveA"), payload.labelSummary)
        assertEquals(0, payload.duplicateVisibleLabelCount)
        assertTrue(payload.resourceSemanticInferenceForbidden)
        assertEquals("transient", payload.interactionChrome)
    }

    private fun evidence(): BindingEvidencePayload = BindingEvidencePayload(
        semanticSubjectId = "device:DriveA",
        engineeringPackageId = "com.athena.example.engineering.drive.compact-vfd",
        engineeringPackageVersion = "1.0.0",
        presentationProfileId = "iec",
        representationPackageId = "com.athena.example.representation.drive.iec",
        representationPackageVersion = "1.0.0",
        descriptorId = "descriptor.drive.iec.standard",
        variant = "standard",
        anchorMapSummary = listOf("port:DriveA.power=power"),
        labelBindingSummary = listOf("device-tag=DriveA"),
        resolverStage = "binding-resolver-v0",
        diagnostics = emptyList(),
        rendererFallbackAccepted = false,
    )

    private fun descriptor(): RepresentationDescriptor = RepresentationDescriptor(
        descriptorId = RepresentationDescriptorId("descriptor.drive.iec.standard"),
        resource = RepresentationDescriptorResourceBinding(GraphicResourceId("resource.drive.iec"), GraphicResourceKind.VECTOR_DOCUMENT),
        bounds = RepresentationDescriptorBounds(width = 80.0, height = 48.0),
        anchors = listOf(RepresentationAnchorDefinition(RepresentationAnchorId("power"), x = 8.0, y = 24.0, side = RepresentationAnchorSide.LEFT)),
        labelSlots = listOf(RepresentationLabelSlotDefinition(RepresentationLabelSlotId("device-tag"), RepresentationLabelSlotRole.DEVICE_TAG, required = true)),
    )
}
