package com.engineeringood.athena.packageplatform

@JvmInline
value class BindingManifestId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class BindingPolicyTag(val value: String) {
    override fun toString(): String = value
}

data class BindingManifestProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

enum class BindingManifestAuthority {
    REPRESENTATION_GEOMETRY,
    GRAPHIC_RESOURCE_INTERNALS,
    COMPILER_BEHAVIOR,
    SOURCE_MUTATION,
}

data class BindingManifestForbiddenAuthorityField(
    val field: String,
    val authority: BindingManifestAuthority,
)

data class BindingManifest(
    val manifestId: BindingManifestId,
    val engineeringPackageId: String,
    val engineeringPackageVersionRange: String,
    val conceptId: EngineeringConceptId,
    val defaultRepresentationPackageId: String,
    val alternativeRepresentationPackageIds: List<String> = emptyList(),
    val compatibleProfileTags: List<PresentationProfileTag> = emptyList(),
    val policyTags: List<BindingPolicyTag> = emptyList(),
    val provenance: BindingManifestProvenance,
    val forbiddenAuthorityFields: List<BindingManifestForbiddenAuthorityField> = emptyList(),
)
