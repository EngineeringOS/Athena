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
import com.engineeringood.athena.packageplatform.RepresentationBindingPriority
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleId
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycle
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleProvenance
import com.engineeringood.athena.packageplatform.RepresentationBindingSelectorFact
import com.engineeringood.athena.packageplatform.RepresentationBindingSubjectKind
import com.engineeringood.athena.packageplatform.RepresentationBindingTarget
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
    fun `binding resolver keeps device and function selector scopes disjoint`() {
        val deviceRule = rule(
            id = "binding.drive.device",
            descriptorId = "descriptor.drive.iec.standard",
            packageId = "com.athena.example.representation.drive.iec",
            variant = "standard",
            priority = 100,
            subjectKind = RepresentationBindingSubjectKind.DEVICE,
        )
        val functionRule = rule(
            id = "binding.drive.function",
            descriptorId = "descriptor.drive.iec.standard",
            packageId = "com.athena.example.representation.drive.iec",
            variant = "standard",
            priority = 100,
            subjectKind = RepresentationBindingSubjectKind.FUNCTION,
            selectorFacts = listOf(
                RepresentationBindingSelectorFact("type", "Drive"),
                RepresentationBindingSelectorFact("role", "coil"),
            ),
        )

        val device = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec"),
                bindingRules = listOf(deviceRule, functionRule),
            ),
        )
        val function = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec"),
                bindingRules = listOf(deviceRule, functionRule),
                subjectKind = RepresentationBindingSubjectKind.FUNCTION,
                semanticFacts = mapOf("type" to "Drive", "role" to "coil"),
            ),
        )

        assertEquals("descriptor.drive.iec.standard", device.resolution?.descriptorId?.value)
        assertEquals("binding.drive.device", device.resolution?.bindingRuleId?.value)
        assertEquals("descriptor.drive.iec.standard", function.resolution?.descriptorId?.value)
        assertEquals("binding.drive.function", function.resolution?.bindingRuleId?.value)
    }

    @Test
    fun `binding resolver selects descriptor by package entry resource instead of global descriptor id order`() {
        val sharedDescriptorId = "descriptor.drive.shared"
        val selectedPackage = representationPackage(
            "com.athena.example.representation.drive.iec",
            "iec",
            sharedDescriptorId,
            "standard",
        )
        val otherPackage = representationPackage(
            "com.athena.example.representation.drive.compact",
            "compact",
            sharedDescriptorId,
            "compact",
        )
        val wrongPackageDescriptor = descriptor(sharedDescriptorId, "drive-compact", "compact").copy(
            anchors = listOf(
                RepresentationAnchorDefinition(
                    RepresentationAnchorId("compact-only"),
                    x = 8.0,
                    y = 24.0,
                    side = RepresentationAnchorSide.LEFT,
                ),
            ),
        )
        val selectedDescriptor = descriptor(sharedDescriptorId, "drive-iec", "standard")
        val request = baseRequest(
            activeProfile = profile("iec"),
            bindingRules = listOf(
                rule(
                    "binding.drive.package-qualified",
                    sharedDescriptorId,
                    selectedPackage.packageId.value,
                    "standard",
                    100,
                ),
            ),
        ).copy(
            representationPackages = listOf(selectedPackage, otherPackage),
            descriptors = listOf(wrongPackageDescriptor, selectedDescriptor),
        )

        val result = BindingResolver().resolve(request)

        assertTrue(result.isValid, result.diagnostics.toString())
        assertEquals(selectedPackage.packageId, result.resolution?.representationPackageId)
        assertEquals(sharedDescriptorId, result.resolution?.descriptorId?.value)
    }

    @Test
    fun `binding resolver rejects duplicate descriptors for one selected package entry`() {
        val request = baseRequest(activeProfile = profile("iec"))
        val selected = request.descriptors.single { descriptor ->
            descriptor.descriptorId.value == "descriptor.drive.iec.standard"
        }

        val result = BindingResolver().resolve(request.copy(descriptors = listOf(selected, selected.copy())))

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { diagnostic ->
            diagnostic.code.wireValue == "binding.resolution.descriptor.ambiguous" &&
                diagnostic.authority == BindingAuthority.DESCRIPTOR
        })
    }

    @Test
    fun `binding resolver selects package descriptor variant anchors labels and style`() {
        val result = BindingResolver().resolve(baseRequest(activeProfile = profile("iec")))

        assertTrue(result.isValid)
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
    fun `binding resolver selects highest priority typed rule and requested variant`() {
        val result = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec"),
                bindingRules = listOf(
                    rule(
                        id = "binding.drive.low",
                        descriptorId = "descriptor.drive.compact",
                        packageId = "com.athena.example.representation.drive.compact",
                        variant = "compact",
                        priority = 10,
                    ),
                    rule(
                        id = "binding.drive.high",
                        descriptorId = "descriptor.drive.iec.standard",
                        packageId = "com.athena.example.representation.drive.iec",
                        variant = "standard",
                        priority = 100,
                    ),
                ),
            ).copy(manifest = manifest().copy(policyTags = listOf(BindingPolicyTag("wrong-legacy-tag")))),
        )

        assertTrue(result.isValid, result.diagnostics.toString())
        val resolution = assertNotNull(result.resolution)
        assertEquals("com.athena.example.representation.drive.iec", resolution.representationPackageId.value)
        assertEquals("descriptor.drive.iec.standard", resolution.descriptorId.value)
        assertEquals("standard", resolution.variantId.value)
    }

    @Test
    fun `binding resolver fails closed when typed rule is missing ambiguous or asks for a missing variant`() {
        val noRule = BindingResolver().resolve(baseRequest(activeProfile = profile("iec"), bindingRules = emptyList()))
        assertFalse(noRule.isValid)
        assertEquals(listOf("binding.resolution.rule.missing"), noRule.diagnostics.map { it.code.wireValue })

        val ambiguous = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec"),
                bindingRules = listOf(
                    rule("binding.drive.a", "descriptor.drive.iec.standard", "com.athena.example.representation.drive.iec", "standard", 100),
                    rule("binding.drive.b", "descriptor.drive.compact", "com.athena.example.representation.drive.compact", "compact", 100),
                ),
            ),
        )
        assertFalse(ambiguous.isValid)
        assertEquals(listOf("binding.resolution.rule.ambiguous"), ambiguous.diagnostics.map { it.code.wireValue })

        val missingVariant = BindingResolver().resolve(
            baseRequest(
                activeProfile = profile("iec"),
                bindingRules = listOf(
                    rule(
                        id = "binding.drive.missing-variant",
                        descriptorId = "descriptor.drive.iec.standard",
                        packageId = "com.athena.example.representation.drive.iec",
                        variant = "wide",
                        priority = 100,
                    ),
                ),
            ),
        )
        assertFalse(missingVariant.isValid)
        assertEquals(listOf("binding.resolution.variant.missing"), missingVariant.diagnostics.map { it.code.wireValue })
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

    @Test
    fun `binding resolver compares compatibility ranges as semantic versions`() {
        val newerMinor = BindingResolver().resolve(
            baseRequest(activeProfile = profile("iec", versionRange = "1.2.0+")).copy(
                engineeringPackage = engineeringPackage("1.10.0"),
            ),
        )
        val olderMajor = BindingResolver().resolve(
            baseRequest(activeProfile = profile("iec", versionRange = "10.0.0+")).copy(
                engineeringPackage = engineeringPackage("2.0.0"),
            ),
        )
        val malformed = BindingResolver().resolve(
            baseRequest(activeProfile = profile("iec", versionRange = "1.0.0+")).copy(
                engineeringPackage = engineeringPackage("not-semver"),
            ),
        )

        assertTrue(newerMinor.isValid, newerMinor.diagnostics.toString())
        listOf(olderMajor, malformed).forEach { result ->
            assertFalse(result.isValid)
            assertTrue(result.diagnostics.any { diagnostic ->
                diagnostic.code.wireValue == "binding.resolution.presentation-profile.incompatible"
            }, result.diagnostics.toString())
        }
    }

    private fun baseRequest(
        activeProfile: PresentationProfileDescriptor,
        requiredAnchorBindings: Map<String, RepresentationAnchorId> = mapOf("port:DriveA.power" to RepresentationAnchorId("power")),
        requiredLabelBindings: Map<RepresentationLabelSlotId, String> = mapOf(RepresentationLabelSlotId("device-tag") to "DriveA"),
        bindingRules: List<RepresentationBindingRule> = listOf(
            rule(
                id = "binding.drive.default",
                descriptorId = if (activeProfile.profileId.value == "compact") "descriptor.drive.compact" else "descriptor.drive.iec.standard",
                packageId = if (activeProfile.profileId.value == "compact") {
                    "com.athena.example.representation.drive.compact"
                } else {
                    "com.athena.example.representation.drive.iec"
                },
                variant = if (activeProfile.profileId.value == "compact") "compact" else "standard",
                priority = 100,
                profileId = activeProfile.profileId.value,
            ),
        ),
        subjectKind: RepresentationBindingSubjectKind = RepresentationBindingSubjectKind.DEVICE,
        semanticFacts: Map<String, String> = mapOf("type" to "Drive"),
    ): BindingResolutionRequest = BindingResolutionRequest(
        subject = BindingSubject(
            semanticSubjectId = "device:DriveA",
            conceptId = EngineeringConceptId("FrequencyDrive"),
            requiredAnchorBindings = requiredAnchorBindings,
            requiredLabelBindings = requiredLabelBindings,
            subjectKind = subjectKind,
            semanticFacts = semanticFacts,
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
        bindingRules = bindingRules,
    )

    private fun rule(
        id: String,
        descriptorId: String,
        packageId: String,
        variant: String,
        priority: Int,
        profileId: String = "iec",
        subjectKind: RepresentationBindingSubjectKind = RepresentationBindingSubjectKind.DEVICE,
        selectorFacts: List<RepresentationBindingSelectorFact> = listOf(RepresentationBindingSelectorFact("type", "Drive")),
    ): RepresentationBindingRule = RepresentationBindingRule(
        ruleId = RepresentationBindingRuleId(id),
        profileId = PresentationProfileId(profileId),
        projectionContext = ProjectionContextId("schematic-sheet"),
        conceptId = EngineeringConceptId("FrequencyDrive"),
        subjectKind = subjectKind,
        selectorFacts = selectorFacts,
        target = RepresentationBindingTarget(
            representationPackageId = RepresentationPackageId(packageId),
            descriptorId = RepresentationDescriptorId(descriptorId),
            packageVersion = RepresentationPackageVersion("1.0.0"),
            variantId = RepresentationVariantId(variant),
        ),
        priority = RepresentationBindingPriority(priority),
        lifecycle = RepresentationBindingRuleLifecycle(RepresentationBindingRuleLifecycleState.ACTIVE),
        provenance = RepresentationBindingRuleProvenance(listOf("m34-test"), "Athena M34"),
    )

    private fun engineeringPackage(version: String = "1.0.0"): EngineeringPackageDescriptor = EngineeringPackageDescriptor(
        packageId = EngineeringPackageId("com.athena.example.engineering.drive.compact-vfd"),
        coordinates = EngineeringPackageCoordinates(
            groupId = EngineeringPackageGroupId("com.athena.example.engineering.drive"),
            artifactId = EngineeringPackageArtifactId("compact-vfd"),
            version = EngineeringPackageVersion(version),
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

    private fun profile(
        id: String,
        versionRange: String = "1.0.0+",
    ): PresentationProfileDescriptor = PresentationProfileDescriptor(
        profileId = PresentationProfileId(id),
        version = PresentationProfileVersion("1.0.0"),
        projectionContexts = listOf(ProjectionContextId("schematic-sheet")),
        styleProfile = PresentationStyleProfileId(if (id == "compact") "industrial-compact" else "industrial-print"),
        standardTags = listOf(RepresentationStandardTag(id)),
        compatibilityConstraints = listOf(
            PresentationPackageCompatibilityConstraint(
                packageId = "com.athena.example.engineering.drive.compact-vfd",
                versionRange = versionRange,
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
