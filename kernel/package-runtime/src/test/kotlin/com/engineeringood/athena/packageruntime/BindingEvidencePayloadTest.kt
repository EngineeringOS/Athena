package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.BindingManifestId
import com.engineeringood.athena.packageplatform.BindingManifestProvenance
import com.engineeringood.athena.packageplatform.EngineeringConceptDefinition
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.EngineeringPackageArtifactId
import com.engineeringood.athena.packageplatform.EngineeringPackageCoordinates
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.EngineeringPackageGroupId
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.EngineeringPackageKind
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycle
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycleState
import com.engineeringood.athena.packageplatform.EngineeringPackageProvenance
import com.engineeringood.athena.packageplatform.EngineeringPackageVersion
import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.GraphicResourceRef
import com.engineeringood.athena.packageplatform.PresentationPackageCompatibilityConstraint
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackMode
import com.engineeringood.athena.packageplatform.PresentationProfileFallbackPolicy
import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.PresentationProfileProvenance
import com.engineeringood.athena.packageplatform.PresentationProfileTag
import com.engineeringood.athena.packageplatform.PresentationProfileVersion
import com.engineeringood.athena.packageplatform.PresentationStyleProfileId
import com.engineeringood.athena.packageplatform.ProjectionContextId
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
import com.engineeringood.athena.packageplatform.RepresentationPackageArtifactId
import com.engineeringood.athena.packageplatform.RepresentationPackageCoordinates
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptorEntry
import com.engineeringood.athena.packageplatform.RepresentationPackageGroupId
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycle
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationPackageProvenance
import com.engineeringood.athena.packageplatform.RepresentationPackageVersion
import com.engineeringood.athena.packageplatform.RepresentationStandardTag
import com.engineeringood.athena.packageplatform.RepresentationSupportedProfile
import com.engineeringood.athena.packageplatform.RepresentationVariantDefinition
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BindingEvidencePayloadTest {
    @Test
    fun `binding evidence payload maps successful resolution into transport safe facts`() {
        val request = request()
        val resolution = BindingResolver().resolve(request)

        val payload = BindingEvidencePayloadMapper.from(request, resolution)

        assertEquals("device:DriveA", payload.semanticSubjectId)
        assertEquals("com.athena.example.engineering.drive.compact-vfd", payload.engineeringPackageId)
        assertEquals("1.0.0", payload.engineeringPackageVersion)
        assertEquals("iec", payload.presentationProfileId)
        assertEquals("binding.drive.frequency-drive", payload.bindingManifestId)
        assertEquals("com.athena.example.representation.drive.iec", payload.representationPackageId)
        assertEquals("1.0.0", payload.representationPackageVersion)
        assertEquals("descriptor.drive.iec.standard", payload.descriptorId)
        assertEquals("standard", payload.variant)
        assertEquals(listOf("port:DriveA.power=power"), payload.anchorMapSummary)
        assertEquals(listOf("device-tag=DriveA"), payload.labelBindingSummary)
        assertEquals("binding-resolver-v0", payload.resolverStage)
        assertEquals(emptyList(), payload.diagnostics)
        assertFalse(payload.rendererFallbackAccepted)
    }

    @Test
    fun `binding evidence payload preserves failed authority diagnostics without fallback success`() {
        val request = request().copy(descriptors = emptyList())
        val resolution = BindingResolver().resolve(request)

        val payload = BindingEvidencePayloadMapper.from(request, resolution)

        assertEquals("device:DriveA", payload.semanticSubjectId)
        assertNull(payload.descriptorId)
        assertNull(payload.variant)
        assertFalse(payload.rendererFallbackAccepted)
        assertEquals(
            listOf("binding.resolution.descriptor.missing", "binding.resolution.anchor.missing", "binding.resolution.label-slot.missing"),
            payload.diagnostics.map { it.code },
        )
        assertEquals(listOf("descriptor", "anchor", "label-slot"), payload.diagnostics.map { it.authority })
    }

    private fun request(): BindingResolutionRequest = BindingResolutionRequest(
        subject = BindingSubject(
            semanticSubjectId = "device:DriveA",
            conceptId = EngineeringConceptId("FrequencyDrive"),
            requiredAnchorBindings = mapOf("port:DriveA.power" to RepresentationAnchorId("power")),
            requiredLabelBindings = mapOf(RepresentationLabelSlotId("device-tag") to "DriveA"),
        ),
        projectionContext = ProjectionContextId("schematic-sheet"),
        engineeringPackage = engineeringPackage(),
        manifest = BindingManifest(
            manifestId = BindingManifestId("binding.drive.frequency-drive"),
            engineeringPackageId = "com.athena.example.engineering.drive.compact-vfd",
            engineeringPackageVersionRange = "1.0.0+",
            conceptId = EngineeringConceptId("FrequencyDrive"),
            defaultRepresentationPackageId = "com.athena.example.representation.drive.iec",
            compatibleProfileTags = listOf(PresentationProfileTag("iec")),
            provenance = BindingManifestProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
        ),
        activeProfile = profile(),
        representationPackages = listOf(representationPackage()),
        descriptors = listOf(descriptor()),
    )

    private fun engineeringPackage(): EngineeringPackageDescriptor = EngineeringPackageDescriptor(
        packageId = EngineeringPackageId("com.athena.example.engineering.drive.compact-vfd"),
        coordinates = EngineeringPackageCoordinates(
            groupId = EngineeringPackageGroupId("com.athena.example.engineering.drive"),
            artifactId = EngineeringPackageArtifactId("compact-vfd"),
            version = EngineeringPackageVersion("1.0.0"),
        ),
        kind = EngineeringPackageKind.CATALOG,
        concepts = listOf(EngineeringConceptDefinition(EngineeringConceptId("FrequencyDrive"))),
        lifecycle = EngineeringPackageLifecycle(EngineeringPackageLifecycleState.ACTIVE, EngineeringPackageVersion("1.0.0")),
        provenance = EngineeringPackageProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
    )

    private fun profile(): PresentationProfileDescriptor = PresentationProfileDescriptor(
        profileId = PresentationProfileId("iec"),
        version = PresentationProfileVersion("1.0.0"),
        projectionContexts = listOf(ProjectionContextId("schematic-sheet")),
        styleProfile = PresentationStyleProfileId("industrial-print"),
        standardTags = listOf(RepresentationStandardTag("iec")),
        compatibilityConstraints = listOf(
            PresentationPackageCompatibilityConstraint(
                packageId = "com.athena.example.engineering.drive.compact-vfd",
                versionRange = "1.0.0+",
            ),
        ),
        fallbackPolicy = PresentationProfileFallbackPolicy(PresentationProfileFallbackMode.FAIL_CLOSED),
        provenance = PresentationProfileProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
    )

    private fun representationPackage(): RepresentationPackageDescriptor = RepresentationPackageDescriptor(
        packageId = RepresentationPackageId("com.athena.example.representation.drive.iec"),
        coordinates = RepresentationPackageCoordinates(
            groupId = RepresentationPackageGroupId("com.athena.example.representation.drive"),
            artifactId = RepresentationPackageArtifactId("iec"),
            version = RepresentationPackageVersion("1.0.0"),
        ),
        supportedProfiles = listOf(RepresentationSupportedProfile(PresentationProfileId("iec"), listOf(PresentationProfileTag("iec")))),
        descriptorEntries = listOf(
            RepresentationPackageDescriptorEntry(
                descriptorId = RepresentationDescriptorId("descriptor.drive.iec.standard"),
                resourceId = GraphicResourceId("resource.drive.iec"),
                variants = listOf(RepresentationVariantId("standard")),
            ),
        ),
        resourceReferences = listOf(GraphicResourceRef(GraphicResourceId("resource.drive.iec"), GraphicResourceKind.VECTOR_DOCUMENT, "drive.svg")),
        variants = listOf(RepresentationVariantDefinition(RepresentationVariantId("standard"), "standard")),
        lifecycle = RepresentationPackageLifecycle(RepresentationPackageLifecycleState.ACTIVE, RepresentationPackageVersion("1.0.0")),
        provenance = RepresentationPackageProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
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
