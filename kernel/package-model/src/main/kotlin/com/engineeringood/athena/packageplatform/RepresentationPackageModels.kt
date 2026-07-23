package com.engineeringood.athena.packageplatform

@JvmInline
value class RepresentationPackageId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationPackageGroupId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationPackageArtifactId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationPackageVersion(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class PresentationProfileId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class PresentationProfileTag(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationDescriptorId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class GraphicResourceId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationStyleTokenRef(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationVariantId(val value: String) {
    override fun toString(): String = value
}

data class RepresentationPackageCoordinates(
    val groupId: RepresentationPackageGroupId,
    val artifactId: RepresentationPackageArtifactId,
    val version: RepresentationPackageVersion,
)

enum class RepresentationPackageAuthority {
    SOURCE_MUTATION,
    ENGINEERING_TRUTH,
    COMPILER_BEHAVIOR,
    ATHENA_SOURCE_SYNTAX,
}

data class RepresentationPackageForbiddenAuthorityField(
    val field: String,
    val authority: RepresentationPackageAuthority,
)

enum class GraphicResourceKind {
    VECTOR_DOCUMENT,
    RASTER_IMAGE,
    THREE_DIMENSIONAL_MESH,
    CANVAS_PROGRAM,
}

data class RepresentationSupportedProfile(
    val profileId: PresentationProfileId,
    val tags: List<PresentationProfileTag> = emptyList(),
)

data class GraphicResourceRef(
    val resourceId: GraphicResourceId,
    val kind: GraphicResourceKind,
    val path: String,
)

data class RepresentationPackageDescriptorEntry(
    val descriptorId: RepresentationDescriptorId,
    val resourceId: GraphicResourceId,
    val variants: List<RepresentationVariantId> = emptyList(),
    val styleTokenRefs: List<RepresentationStyleTokenRef> = emptyList(),
)

data class RepresentationVariantDefinition(
    val variantId: RepresentationVariantId,
    val displayName: String,
)

data class RepresentationPackagePreviewRef(
    val variantId: RepresentationVariantId,
    val path: String,
)

enum class RepresentationPackageLifecycleState {
    ACTIVE,
    DEPRECATED,
}

data class RepresentationPackageLifecycle(
    val state: RepresentationPackageLifecycleState,
    val sinceVersion: RepresentationPackageVersion,
)

data class RepresentationPackageProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

data class RepresentationPackageDescriptor(
    val packageId: RepresentationPackageId,
    val coordinates: RepresentationPackageCoordinates,
    val supportedProfiles: List<RepresentationSupportedProfile>,
    val descriptorEntries: List<RepresentationPackageDescriptorEntry>,
    val resourceReferences: List<GraphicResourceRef>,
    val styleTokenRefs: List<RepresentationStyleTokenRef> = emptyList(),
    val variants: List<RepresentationVariantDefinition> = emptyList(),
    val previews: List<RepresentationPackagePreviewRef> = emptyList(),
    val lifecycle: RepresentationPackageLifecycle,
    val provenance: RepresentationPackageProvenance,
    val forbiddenAuthorityFields: List<RepresentationPackageForbiddenAuthorityField> = emptyList(),
) {
    fun hasSemanticAuthority(): Boolean = forbiddenAuthorityFields.isNotEmpty()
}
