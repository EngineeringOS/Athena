package com.engineeringood.athena.packageplatform

@JvmInline
value class PresentationProfileVersion(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class ProjectionContextId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class PresentationStyleProfileId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationStandardTag(val value: String) {
    override fun toString(): String = value
}

data class PresentationPackageCompatibilityConstraint(
    val packageId: String,
    val versionRange: String,
)

enum class PresentationProfileFallbackMode {
    FAIL_CLOSED,
    ALLOW_DIAGNOSTIC_FALLBACK,
}

data class PresentationProfileFallbackPolicy(
    val mode: PresentationProfileFallbackMode,
)

data class PresentationProfileProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

enum class PresentationProfileAuthority {
    ENGINEERING_TRUTH,
    PRODUCT_PARAMETERS,
    GRAPHIC_RESOURCE_INTERNALS,
    SOURCE_MUTATION,
}

data class PresentationProfileForbiddenAuthorityField(
    val field: String,
    val authority: PresentationProfileAuthority,
)

enum class PresentationProfilePolicyFactKind {
    STANDARD,
    CUSTOMER,
    OUTPUT,
    THEME,
}

data class PresentationProfilePolicyFact(
    val kind: PresentationProfilePolicyFactKind,
    val value: String,
)

data class PresentationProfileDescriptor(
    val profileId: PresentationProfileId,
    val version: PresentationProfileVersion,
    val projectionContexts: List<ProjectionContextId>,
    val styleProfile: PresentationStyleProfileId,
    val standardTags: List<RepresentationStandardTag> = emptyList(),
    val compatibilityConstraints: List<PresentationPackageCompatibilityConstraint> = emptyList(),
    val fallbackPolicy: PresentationProfileFallbackPolicy,
    val provenance: PresentationProfileProvenance,
    val policyFacts: List<PresentationProfilePolicyFact> = emptyList(),
    val forbiddenAuthorityFields: List<PresentationProfileForbiddenAuthorityField> = emptyList(),
)
