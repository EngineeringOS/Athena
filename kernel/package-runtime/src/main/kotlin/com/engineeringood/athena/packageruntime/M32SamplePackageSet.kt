package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.BindingManifestId
import com.engineeringood.athena.packageplatform.BindingManifestProvenance
import com.engineeringood.athena.packageplatform.BindingPolicyTag
import com.engineeringood.athena.packageplatform.EngineeringConceptDefinition
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.EngineeringPackageArtifactId
import com.engineeringood.athena.packageplatform.EngineeringPackageCoordinates
import com.engineeringood.athena.packageplatform.EngineeringPackageGroupId
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.EngineeringPackageKind
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycle
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycleState
import com.engineeringood.athena.packageplatform.EngineeringPackageProvenance
import com.engineeringood.athena.packageplatform.EngineeringPackageVersion
import com.engineeringood.athena.packageplatform.EngineeringProductDefinition
import com.engineeringood.athena.packageplatform.EngineeringProductId
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
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.listDirectoryEntries

data class M32SamplePackageSet(
    val projectRoot: Path,
    val sourceFile: Path,
    val readmeFile: Path,
    val resourceFiles: List<Path>,
    val engineeringPackages: List<EngineeringPackageDescriptor>,
    val presentationProfiles: List<PresentationProfileDescriptor>,
    val representationPackages: List<RepresentationPackageDescriptor>,
    val manifests: List<BindingManifest>,
    val descriptors: List<RepresentationDescriptor>,
) {
    val semanticSubjectIds: List<String>
        get() = subjects.keys.sorted()

    fun resolveSubject(
        semanticSubjectId: String,
        profileId: PresentationProfileId,
    ): BindingResolutionResult = BindingResolver().resolve(bindingRequestForSubject(semanticSubjectId, profileId))

    fun bindingRequestForSubject(
        semanticSubjectId: String,
        profileId: PresentationProfileId,
    ): BindingResolutionRequest {
        val subject = subjects[semanticSubjectId] ?: error("M32 sample subject not found: $semanticSubjectId")
        val engineeringPackage = engineeringPackages.singleOrNull { it.packageId.value == subject.engineeringPackageId.value }
            ?: error("M32 sample engineering package not found: ${subject.engineeringPackageId.value}")
        val manifest = manifests.singleOrNull { it.engineeringPackageId == engineeringPackage.packageId.value }
            ?: error("M32 sample binding manifest not found: ${engineeringPackage.packageId.value}")
        val profile = presentationProfiles.singleOrNull { it.profileId == profileId }
            ?: error("M32 sample presentation profile not found: ${profileId.value}")
        return BindingResolutionRequest(
            subject = BindingSubject(
                semanticSubjectId = semanticSubjectId,
                conceptId = subject.conceptId,
                requiredAnchorBindings = subject.anchorBindings,
                requiredLabelBindings = mapOf(RepresentationLabelSlotId("device-tag") to subject.label),
            ),
            projectionContext = ProjectionContextId("schematic-sheet"),
            engineeringPackage = engineeringPackage,
            manifest = manifest,
            activeProfile = profile,
            representationPackages = representationPackages,
            descriptors = descriptors,
        )
    }

    fun descriptorFor(descriptorId: String?): RepresentationDescriptor? =
        descriptorId?.let { id -> descriptors.firstOrNull { descriptor -> descriptor.descriptorId.value == id } }

    private val subjects: Map<String, M32SampleSubject> = mapOf(
        subject(
            semanticSubjectId = "device:MainPowerSupplyPS32",
            packageId = "power.supply-24v",
            conceptId = "PowerSupply",
            label = "PS32",
            port = "port:MainPowerSupplyPS32.lplus",
            anchor = "lplus",
        ),
        subject(
            semanticSubjectId = "device:ControlRelayK32",
            packageId = "control.roller-relay",
            conceptId = "RollerRelay",
            label = "K32",
            port = "port:ControlRelayK32.supply",
            anchor = "supply",
        ),
        subject(
            semanticSubjectId = "device:ShutterMotorM32",
            packageId = "motion.shutter-motor",
            conceptId = "ShutterMotor",
            label = "M32",
            port = "port:ShutterMotorM32.up",
            anchor = "up",
        ),
    )

    companion object {
        fun loadDefault(projectRoot: Path = Path.of("").absolute()): M32SamplePackageSet {
            val repoRoot = findRepoRoot(projectRoot)
            val sampleRoot = repoRoot.resolve("examples/m32/sample-project")
            val resourcesRoot = sampleRoot.resolve("packages/resources")
            return M32SamplePackageSet(
                projectRoot = sampleRoot,
                sourceFile = sampleRoot.resolve("src/01-package-platform-demo.athena"),
                readmeFile = sampleRoot.resolve("README.md"),
                resourceFiles = resourcesRoot.listDirectoryEntries("*.svg").sortedBy { it.fileName.toString() },
                engineeringPackages = listOf(
                    engineeringPackage("power.supply-24v", "PowerSupply", "M32DemoPowerSupply"),
                    engineeringPackage("control.roller-relay", "RollerRelay", "M32DemoRollerRelay"),
                    engineeringPackage("motion.shutter-motor", "ShutterMotor", "M32DemoShutterMotor"),
                ),
                presentationProfiles = listOf(profile("m32-iec", "iec"), profile("m32-compact", "compact")),
                representationPackages = listOf(
                    representationPackage("power.supply.iec", "m32-iec", "descriptor.power-supply.iec", "resource.power-supply.iec", "lplus"),
                    representationPackage("control.relay.iec", "m32-iec", "descriptor.relay.iec", "resource.relay.iec", "supply"),
                    representationPackage("motion.motor.iec", "m32-iec", "descriptor.motor.iec", "resource.motor.iec", "up"),
                    representationPackage("motion.motor.compact", "m32-compact", "descriptor.motor.compact", "resource.motor.compact", "up"),
                ),
                manifests = listOf(
                    manifest("power.supply-24v", "PowerSupply", "power.supply.iec", emptyList()),
                    manifest("control.roller-relay", "RollerRelay", "control.relay.iec", emptyList()),
                    manifest("motion.shutter-motor", "ShutterMotor", "motion.motor.iec", listOf("com.athena.sample.representation.motion.motor.compact")),
                ),
                descriptors = listOf(
                    descriptor("descriptor.power-supply.iec", "resource.power-supply.iec", "lplus", 76.0, 52.0),
                    descriptor("descriptor.relay.iec", "resource.relay.iec", "supply", 92.0, 64.0),
                    descriptor("descriptor.motor.iec", "resource.motor.iec", "up", 84.0, 64.0),
                    descriptor("descriptor.motor.compact", "resource.motor.compact", "up", 64.0, 44.0),
                ),
            )
        }
    }
}

private fun findRepoRoot(start: Path): Path {
    var current: Path? = start
    while (current != null) {
        if (current.resolve("settings.gradle.kts").toFile().exists() && current.resolve("examples").toFile().exists()) {
            return current
        }
        current = current.parent
    }
    return start
}

private data class M32SampleSubject(
    val engineeringPackageId: EngineeringPackageId,
    val conceptId: EngineeringConceptId,
    val label: String,
    val anchorBindings: Map<String, RepresentationAnchorId>,
)

private fun subject(
    semanticSubjectId: String,
    packageId: String,
    conceptId: String,
    label: String,
    port: String,
    anchor: String,
): Pair<String, M32SampleSubject> = semanticSubjectId to M32SampleSubject(
    engineeringPackageId = EngineeringPackageId("com.athena.sample.engineering.$packageId"),
    conceptId = EngineeringConceptId(conceptId),
    label = label,
    anchorBindings = mapOf(port to RepresentationAnchorId(anchor)),
)

private fun engineeringPackage(
    artifact: String,
    conceptId: String,
    productId: String,
): EngineeringPackageDescriptor = EngineeringPackageDescriptor(
    packageId = EngineeringPackageId("com.athena.sample.engineering.$artifact"),
    coordinates = EngineeringPackageCoordinates(
        groupId = EngineeringPackageGroupId("com.athena.sample.engineering.${artifact.substringBeforeLast('.')}"),
        artifactId = EngineeringPackageArtifactId(artifact.substringAfterLast('.')),
        version = EngineeringPackageVersion("1.0.0"),
    ),
    kind = EngineeringPackageKind.CATALOG,
    concepts = listOf(
        EngineeringConceptDefinition(
            conceptId = EngineeringConceptId(conceptId),
            productDefinitions = listOf(
                EngineeringProductDefinition(
                    productId = EngineeringProductId(productId),
                    model = com.engineeringood.athena.packageplatform.EngineeringPackageModelName("$productId-SYNTH"),
                ),
            ),
        ),
    ),
    lifecycle = EngineeringPackageLifecycle(EngineeringPackageLifecycleState.ACTIVE, EngineeringPackageVersion("1.0.0")),
    provenance = EngineeringPackageProvenance(sources = listOf("athena-owned-m32-sample"), reviewedBy = "Athena M32"),
)

private fun profile(id: String, tag: String): PresentationProfileDescriptor = PresentationProfileDescriptor(
    profileId = PresentationProfileId(id),
    version = PresentationProfileVersion("1.0.0"),
    projectionContexts = listOf(ProjectionContextId("schematic-sheet")),
    styleProfile = PresentationStyleProfileId("industrial-$tag"),
    standardTags = listOf(RepresentationStandardTag(tag)),
    compatibilityConstraints = listOf(
        PresentationPackageCompatibilityConstraint("com.athena.sample.engineering.power.supply-24v", "1.0.0+"),
        PresentationPackageCompatibilityConstraint("com.athena.sample.engineering.control.roller-relay", "1.0.0+"),
        PresentationPackageCompatibilityConstraint("com.athena.sample.engineering.motion.shutter-motor", "1.0.0+"),
    ),
    fallbackPolicy = PresentationProfileFallbackPolicy(PresentationProfileFallbackMode.FAIL_CLOSED),
    provenance = PresentationProfileProvenance(sources = listOf("athena-owned-m32-sample"), reviewedBy = "Athena M32"),
)

private fun representationPackage(
    artifact: String,
    profileId: String,
    descriptorId: String,
    resourceId: String,
    anchor: String,
): RepresentationPackageDescriptor = RepresentationPackageDescriptor(
    packageId = RepresentationPackageId("com.athena.sample.representation.$artifact"),
    coordinates = RepresentationPackageCoordinates(
        groupId = RepresentationPackageGroupId("com.athena.sample.representation.${artifact.substringBeforeLast('.')}"),
        artifactId = RepresentationPackageArtifactId(artifact.substringAfterLast('.')),
        version = RepresentationPackageVersion("1.0.0"),
    ),
    supportedProfiles = listOf(RepresentationSupportedProfile(PresentationProfileId(profileId), listOf(PresentationProfileTag(profileId)))),
    descriptorEntries = listOf(
        RepresentationPackageDescriptorEntry(
            descriptorId = RepresentationDescriptorId(descriptorId),
            resourceId = GraphicResourceId(resourceId),
            variants = listOf(RepresentationVariantId(profileId.substringAfter("m32-"))),
            styleTokenRefs = listOf(RepresentationStyleTokenRef("line-work")),
        ),
    ),
    resourceReferences = listOf(GraphicResourceRef(GraphicResourceId(resourceId), GraphicResourceKind.VECTOR_DOCUMENT, "$resourceId.svg")),
    variants = listOf(RepresentationVariantDefinition(RepresentationVariantId(profileId.substringAfter("m32-")), anchor)),
    lifecycle = RepresentationPackageLifecycle(RepresentationPackageLifecycleState.ACTIVE, RepresentationPackageVersion("1.0.0")),
    provenance = RepresentationPackageProvenance(sources = listOf("athena-owned-m32-sample"), reviewedBy = "Athena M32"),
)

private fun manifest(
    engineeringArtifact: String,
    conceptId: String,
    defaultRepresentationArtifact: String,
    alternatives: List<String>,
): BindingManifest = BindingManifest(
    manifestId = BindingManifestId("binding.m32.${engineeringArtifact.replace('.', '-')}"),
    engineeringPackageId = "com.athena.sample.engineering.$engineeringArtifact",
    engineeringPackageVersionRange = "1.0.0+",
    conceptId = EngineeringConceptId(conceptId),
    defaultRepresentationPackageId = "com.athena.sample.representation.$defaultRepresentationArtifact",
    alternativeRepresentationPackageIds = alternatives,
    compatibleProfileTags = listOf(PresentationProfileTag("m32-iec"), PresentationProfileTag("m32-compact")),
    policyTags = listOf(BindingPolicyTag("m32-demo")),
    provenance = BindingManifestProvenance(sources = listOf("athena-owned-m32-sample"), reviewedBy = "Athena M32"),
)

private fun descriptor(
    descriptorId: String,
    resourceId: String,
    anchor: String,
    width: Double,
    height: Double,
): RepresentationDescriptor = RepresentationDescriptor(
    descriptorId = RepresentationDescriptorId(descriptorId),
    resource = RepresentationDescriptorResourceBinding(GraphicResourceId(resourceId), GraphicResourceKind.VECTOR_DOCUMENT),
    bounds = RepresentationDescriptorBounds(width = width, height = height),
    anchors = listOf(
        RepresentationAnchorDefinition(RepresentationAnchorId(anchor), x = 8.0, y = height / 2.0, side = RepresentationAnchorSide.LEFT),
    ),
    labelSlots = listOf(
        RepresentationLabelSlotDefinition(RepresentationLabelSlotId("device-tag"), RepresentationLabelSlotRole.DEVICE_TAG, required = true),
    ),
    variants = listOf(RepresentationVariantId(resourceId.substringAfterLast('.'))),
    styleTokenRefs = listOf(RepresentationStyleTokenRef("line-work")),
)
