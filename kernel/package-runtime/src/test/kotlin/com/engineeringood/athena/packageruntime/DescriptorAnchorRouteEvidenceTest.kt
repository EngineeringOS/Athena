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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DescriptorAnchorRouteEvidenceTest {
    @Test
    fun `route evidence uses mapped descriptor anchors for source and target terminals`() {
        val result = DescriptorAnchorRouteEvidenceMapper.map(
            request = DescriptorAnchorRouteEvidenceRequest(
                connectionSemanticId = "connection:DriveA.power->Breaker.line",
                sourceSemanticTerminalId = "port:DriveA.power",
                targetSemanticTerminalId = "port:Breaker.line",
                sourceBindingEvidence = evidence("device:DriveA", "port:DriveA.power=power"),
                targetBindingEvidence = evidence("device:Breaker", "port:Breaker.line=line"),
                sourceDescriptor = descriptor("descriptor.drive", "power", 8.0, 24.0),
                targetDescriptor = descriptor("descriptor.breaker", "line", 72.0, 24.0),
            ),
        )

        assertTrue(result.isValid)
        assertFalse(result.centerFallbackAccepted)
        val route = result.route
        assertEquals("connection:DriveA.power->Breaker.line", route?.connectionSemanticId)
        assertEquals("descriptor.drive#power", route?.sourceDescriptorAnchorEvidence)
        assertEquals("descriptor.breaker#line", route?.targetDescriptorAnchorEvidence)
        assertEquals(8.0, route?.sourcePoint?.x)
        assertEquals(72.0, route?.targetPoint?.x)
    }

    @Test
    fun `route evidence fails closed when descriptor anchors are missing and does not accept center fallback`() {
        val result = DescriptorAnchorRouteEvidenceMapper.map(
            request = DescriptorAnchorRouteEvidenceRequest(
                connectionSemanticId = "connection:DriveA.power->Breaker.line",
                sourceSemanticTerminalId = "port:DriveA.power",
                targetSemanticTerminalId = "port:Breaker.line",
                sourceBindingEvidence = evidence("device:DriveA", "port:DriveA.power=missing"),
                targetBindingEvidence = evidence("device:Breaker", "port:Breaker.line=line"),
                sourceDescriptor = descriptor("descriptor.drive", "power", 8.0, 24.0),
                targetDescriptor = null,
            ),
        )

        assertFalse(result.isValid)
        assertFalse(result.centerFallbackAccepted)
        assertNull(result.route)
        assertEquals(
            listOf("descriptor-route.anchor.missing", "descriptor-route.descriptor.missing"),
            result.diagnostics.map { it.code },
        )
    }

    private fun evidence(subject: String, anchorSummary: String): BindingEvidencePayload = BindingEvidencePayload(
        semanticSubjectId = subject,
        engineeringPackageId = "com.athena.example.engineering",
        engineeringPackageVersion = "1.0.0",
        presentationProfileId = "iec",
        representationPackageId = "com.athena.example.representation",
        representationPackageVersion = "1.0.0",
        descriptorId = "descriptor",
        variant = "standard",
        anchorMapSummary = listOf(anchorSummary),
        labelBindingSummary = emptyList(),
        resolverStage = "binding-resolver-v0",
        diagnostics = emptyList(),
        rendererFallbackAccepted = false,
    )

    private fun descriptor(id: String, anchorId: String, x: Double, y: Double): RepresentationDescriptor = RepresentationDescriptor(
        descriptorId = RepresentationDescriptorId(id),
        resource = RepresentationDescriptorResourceBinding(GraphicResourceId("resource.$id"), GraphicResourceKind.VECTOR_DOCUMENT),
        bounds = RepresentationDescriptorBounds(width = 80.0, height = 48.0),
        anchors = listOf(RepresentationAnchorDefinition(RepresentationAnchorId(anchorId), x = x, y = y, side = RepresentationAnchorSide.LEFT)),
    )
}
