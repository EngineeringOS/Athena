package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.BindingManifestId
import com.engineeringood.athena.packageplatform.BindingManifestProvenance
import com.engineeringood.athena.packageplatform.BindingPolicyTag
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
import com.engineeringood.athena.packageplatform.RepresentationStyleTokenRef
import com.engineeringood.athena.packageplatform.RepresentationSupportedProfile
import com.engineeringood.athena.packageplatform.RepresentationVariantDefinition
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BindingResolverSelectionTest {
    @Test
    fun `binding resolver selects package descriptor variant anchors labels and style`() {
        val result = BindingResolver().resolve(baseRequest(activeProfile = profile("iec")))

        assertTrue(result.isValid)
        assertFalse(result.rendererFallbackAccepted)
        val resolution = assertNotNull(result.resolution)
        assertEquals("device:DriveA", resolution.semanticSubjectId)
        assertEquals("com.athena.example.engineering.drive.compact-vfd", resolution.engineeringPackageId.value)
        assertEquals("iec", resolution.presentationProfileId.value)
        assertEquals("com.athena.example.representation.drive.iec", resolution.representationPackageId.value)
        assertEquals("descriptor.drive.iec.standard", resolution.descriptorId.value)
        assertEquals("standard", resolution.variantId.value)
        assertEquals(RepresentationAnchorId("power"), resolution.anchorMapping["port:DriveA.power"])
        assertEquals("DriveA", resolution.labelBinding[RepresentationLabelSlotId("device-tag")])
        assertEquals(PresentationStyleProfileId("industrial-print"), resolution.styleProfile)
    }

    @Test
    fun `binding resolver changes appearance when active presentation profile changes without source identity change`() {
        val iec = BindingResolver().resolve(baseRequest(activeProfile = profile("iec"))).resolution
        val compact = BindingResolver().resolve(baseRequest(activeProfile = profile("compact"))).resolution

        assertNotNull(iec)
        assertNotNull(compact)
        assertEquals(iec.semanticSubjectId, compact.semanticSubjectId)
        assertEquals(iec.engineeringPackageId, compact.engineeringPackageId)
        assertEquals("com.athena.example.representation.drive.iec", iec.representationPackageId.value)
        assertEquals("com.athena.example.representation.drive.compact", compact.representationPackageId.value)
        assertEquals("descriptor.drive.compact", compact.descriptorId.value)
        assertEquals("compact", compact.variantId.value)
    }

    @Test
    fun `binding resolver fails closed with authority diagnostics instead of fallback boxes`() {
        val result = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec").copy(
                    compatibilityConstraints = listOf(
                        PresentationPackageCompatibilityConstraint(
                            packageId = "com.athena.example.engineering.other",
                            versionRange = "1.0.0+",
                        ),
                    ),
                ),
                requiredAnchorBindings = mapOf("port:DriveA.power" to RepresentationAnchorId("missing")),
                requiredLabelBindings = mapOf(RepresentationLabelSlotId("rating") to "3kW"),
            ).copy(
                manifest = manifest().copy(defaultRepresentationPackageId = "com.athena.example.representation.missing"),
                descriptors = emptyList(),
            ),
        )

        assertFalse(result.isValid)
        assertFalse(result.rendererFallbackAccepted)
        assertEquals(null, result.resolution)
        assertEquals(
            listOf(
                "binding.resolution.presentation-profile.incompatible",
                "binding.resolution.representation-package.missing",
                "binding.resolution.descriptor.missing",
                "binding.resolution.anchor.missing",
                "binding.resolution.label-slot.missing",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
        assertEquals(
            listOf(
                BindingAuthority.PRESENTATION_PROFILE,
                BindingAuthority.REPRESENTATION_PACKAGE,
                BindingAuthority.DESCRIPTOR,
                BindingAuthority.ANCHOR,
                BindingAuthority.LABEL_SLOT,
            ),
            result.diagnostics.map { it.authority },
        )
    }

    private fun baseRequest(
        activeProfile: PresentationProfileDescriptor,
        requiredAnchorBindings: Map<String, RepresentationAnchorId> = mapOf("port:DriveA.power" to RepresentationAnchorId("power")),
        requiredLabelBindings: Map<RepresentationLabelSlotId, String> = mapOf(RepresentationLabelSlotId("device-tag") to "DriveA"),
    ): BindingResolutionRequest = BindingResolutionRequest(
        subject = BindingSubject(
            semanticSubjectId = "device:DriveA",
            conceptId = EngineeringConceptId("FrequencyDrive"),
            requiredAnchorBindings = requiredAnchorBindings,
            requiredLabelBindings = requiredLabelBindings,
        ),
        projectionContext = ProjectionContextId("schematic-sheet"),
        engineeringPackage = engineeringPackage(),
        manifest = manifest(),
        activeProfile = activeProfile,
        representationPackages = listOf(
            representationPackage("com.athena.example.representation.drive.iec", "iec", "descriptor.drive.iec.standard", "standard"),
            representationPackage("com.athena.example.representation.drive.compact", "compact", "descriptor.drive.compact", "compact"),
        ),
        descriptors = listOf(
            descriptor("descriptor.drive.iec.standard", "drive-iec", "standard"),
            descriptor("descriptor.drive.compact", "drive-compact", "compact"),
        ),
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

    private fun manifest(): BindingManifest = BindingManifest(
        manifestId = BindingManifestId("binding.drive.frequency-drive"),
        engineeringPackageId = "com.athena.example.engineering.drive.compact-vfd",
        engineeringPackageVersionRange = "1.0.0+",
        conceptId = EngineeringConceptId("FrequencyDrive"),
        defaultRepresentationPackageId = "com.athena.example.representation.drive.iec",
        alternativeRepresentationPackageIds = listOf("com.athena.example.representation.drive.compact"),
        compatibleProfileTags = listOf(PresentationProfileTag("iec"), PresentationProfileTag("compact")),
        policyTags = listOf(BindingPolicyTag("industrial")),
        provenance = BindingManifestProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
    )

    private fun profile(id: String): PresentationProfileDescriptor = PresentationProfileDescriptor(
        profileId = PresentationProfileId(id),
        version = PresentationProfileVersion("1.0.0"),
        projectionContexts = listOf(ProjectionContextId("schematic-sheet")),
        styleProfile = PresentationStyleProfileId(if (id == "compact") "industrial-compact" else "industrial-print"),
        standardTags = listOf(RepresentationStandardTag(id)),
        compatibilityConstraints = listOf(
            PresentationPackageCompatibilityConstraint(
                packageId = "com.athena.example.engineering.drive.compact-vfd",
                versionRange = "1.0.0+",
            ),
        ),
        fallbackPolicy = PresentationProfileFallbackPolicy(PresentationProfileFallbackMode.FAIL_CLOSED),
        provenance = PresentationProfileProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
    )

    private fun representationPackage(
        packageId: String,
        profileId: String,
        descriptorId: String,
        variant: String,
    ): RepresentationPackageDescriptor = RepresentationPackageDescriptor(
        packageId = RepresentationPackageId(packageId),
        coordinates = RepresentationPackageCoordinates(
            groupId = RepresentationPackageGroupId(packageId.substringBeforeLast('.')),
            artifactId = RepresentationPackageArtifactId(packageId.substringAfterLast('.')),
            version = RepresentationPackageVersion("1.0.0"),
        ),
        supportedProfiles = listOf(RepresentationSupportedProfile(PresentationProfileId(profileId), listOf(PresentationProfileTag(profileId)))),
        descriptorEntries = listOf(
            RepresentationPackageDescriptorEntry(
                descriptorId = RepresentationDescriptorId(descriptorId),
                resourceId = GraphicResourceId("resource.$variant"),
                variants = listOf(RepresentationVariantId(variant)),
                styleTokenRefs = listOf(RepresentationStyleTokenRef("line-work")),
            ),
        ),
        resourceReferences = listOf(GraphicResourceRef(GraphicResourceId("resource.$variant"), GraphicResourceKind.VECTOR_DOCUMENT, "$variant.svg")),
        variants = listOf(RepresentationVariantDefinition(RepresentationVariantId(variant), variant)),
        lifecycle = RepresentationPackageLifecycle(RepresentationPackageLifecycleState.ACTIVE, RepresentationPackageVersion("1.0.0")),
        provenance = RepresentationPackageProvenance(sources = listOf("m32-test"), reviewedBy = "Athena M32"),
    )

    private fun descriptor(id: String, resourceId: String, variant: String): RepresentationDescriptor = RepresentationDescriptor(
        descriptorId = RepresentationDescriptorId(id),
        resource = RepresentationDescriptorResourceBinding(GraphicResourceId("resource.$variant"), GraphicResourceKind.VECTOR_DOCUMENT),
        bounds = RepresentationDescriptorBounds(width = 80.0, height = 48.0),
        anchors = listOf(
            RepresentationAnchorDefinition(RepresentationAnchorId("power"), x = 8.0, y = 24.0, side = RepresentationAnchorSide.LEFT),
        ),
        labelSlots = listOf(
            RepresentationLabelSlotDefinition(RepresentationLabelSlotId("device-tag"), RepresentationLabelSlotRole.DEVICE_TAG, required = true),
        ),
        variants = listOf(RepresentationVariantId(variant)),
        styleTokenRefs = listOf(RepresentationStyleTokenRef(resourceId)),
    )
}
