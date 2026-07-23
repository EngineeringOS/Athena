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
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.RepresentationDiagnosticCode
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSubjectKind
import com.engineeringood.athena.representation.SemanticPortId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PackageBackedRepresentationOccurrenceFactoryTest {
    @Test
    fun `factory creates M30 compatible occurrence from binding evidence and descriptor`() {
        val result = PackageBackedRepresentationOccurrenceFactory().create(
            request(
                evidence = evidence(),
                descriptor = descriptor(),
            ),
        )

        assertFalse(result.rendererFallbackAccepted)
        assertEquals(emptyList(), result.diagnostics)
        val occurrence = assertNotNull(result.occurrence)
        assertEquals(RepresentationSubjectId("device:DriveA"), occurrence.canonicalSemanticId)
        assertEquals(RepresentationProjectionOccurrenceId("sheet:control/device:DriveA"), occurrence.projectionOccurrenceId)
        assertEquals("descriptor.drive.iec.standard", occurrence.symbolId.value)
        assertEquals("standard", occurrence.variant?.value)
        assertEquals(LabelValue("DriveA"), occurrence.labelBindings.single().value)
        assertEquals(SemanticPortId("port:DriveA.power"), occurrence.terminalBindings.single().semanticPortId)
    }

    @Test
    fun `factory preserves semantic identity and keeps descriptor identifiers as representation identity only`() {
        val result = PackageBackedRepresentationOccurrenceFactory().create(request(evidence(), descriptor()))

        val occurrence = assertNotNull(result.occurrence)
        assertEquals("device:DriveA", occurrence.canonicalSemanticId.value)
        assertEquals("descriptor.drive.iec.standard", occurrence.symbolId.value)
        assertFalse(occurrence.canonicalSemanticId.value.contains("descriptor.drive"))
    }

    @Test
    fun `factory fails closed when binding evidence or descriptor facts are incomplete`() {
        val result = PackageBackedRepresentationOccurrenceFactory().create(
            request(
                evidence = evidence().copy(
                    descriptorId = null,
                    representationPackageId = null,
                    anchorMapSummary = listOf("port:DriveA.power=missing"),
                    labelBindingSummary = listOf("rating=3kW"),
                    diagnostics = listOf(
                        BindingEvidenceDiagnosticPayload(
                            severity = "error",
                            code = "binding.resolution.descriptor.missing",
                            authority = "descriptor",
                            subject = "device:DriveA",
                            message = "Missing descriptor.",
                        ),
                    ),
                ),
                descriptor = descriptor(),
            ),
        )

        assertFalse(result.rendererFallbackAccepted)
        assertNull(result.occurrence)
        assertEquals(
            listOf(
                RepresentationDiagnosticCode.ANCHOR_MISSING.wireValue,
                RepresentationDiagnosticCode.LABEL_SLOT_MISSING.wireValue,
                RepresentationDiagnosticCode.SYMBOL_MISSING.wireValue,
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    private fun request(
        evidence: BindingEvidencePayload,
        descriptor: RepresentationDescriptor?,
    ): PackageBackedRepresentationOccurrenceRequest = PackageBackedRepresentationOccurrenceRequest(
        bindingEvidence = evidence,
        descriptor = descriptor,
        projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/device:DriveA"),
        subjectKind = RepresentationSubjectKind.COMPONENT,
        semanticRole = RepresentationSemanticRole("power-load"),
        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
        occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
    )

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
        variants = listOf(RepresentationVariantId("standard")),
    )
}
