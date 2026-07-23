package com.engineeringood.athena.packageplatform

@JvmInline
value class EngineeringPackageId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringPackageGroupId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringPackageArtifactId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringPackageVersion(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringConceptId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringProductId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringPackageModelName(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringTemplateId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringParameterId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringPackageScalarValue(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringValidationRuleRef(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringRelationshipCapabilityRef(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class EngineeringDocumentationRef(val value: String) {
    override fun toString(): String = value
}

data class EngineeringPackageCoordinates(
    val groupId: EngineeringPackageGroupId,
    val artifactId: EngineeringPackageArtifactId,
    val version: EngineeringPackageVersion,
)

enum class EngineeringPackageKind {
    UNSPECIFIED,
    CATALOG,
}

enum class EngineeringParameterValueType {
    TEXT,
}

enum class EngineeringPackageLifecycleState {
    ACTIVE,
    DEPRECATED,
}

enum class EngineeringPackageAuthority {
    REPRESENTATION,
    PRESENTATION,
    SOURCE_MUTATION,
    GRAPHIC_RESOURCE,
    RENDERER,
}

data class EngineeringPackageForbiddenAuthorityField(
    val field: String,
    val authority: EngineeringPackageAuthority,
)

data class EngineeringPackageProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

data class EngineeringPackageLifecycle(
    val state: EngineeringPackageLifecycleState,
    val sinceVersion: EngineeringPackageVersion,
)

data class EngineeringProductDefinition(
    val productId: EngineeringProductId,
    val model: EngineeringPackageModelName,
)

data class EngineeringTemplateDefinition(
    val templateId: EngineeringTemplateId,
    val defaultValues: Map<String, EngineeringPackageScalarValue> = emptyMap(),
)

data class EngineeringParameterDefinition(
    val parameterId: EngineeringParameterId,
    val valueType: EngineeringParameterValueType,
    val required: Boolean,
)

data class EngineeringConceptDefinition(
    val conceptId: EngineeringConceptId,
    val productDefinitions: List<EngineeringProductDefinition> = emptyList(),
    val templates: List<EngineeringTemplateDefinition> = emptyList(),
    val parameters: List<EngineeringParameterDefinition> = emptyList(),
    val validationRuleRefs: List<EngineeringValidationRuleRef> = emptyList(),
    val relationshipCapabilities: List<EngineeringRelationshipCapabilityRef> = emptyList(),
)

data class EngineeringPackageDescriptor(
    val packageId: EngineeringPackageId,
    val coordinates: EngineeringPackageCoordinates,
    val kind: EngineeringPackageKind,
    val concepts: List<EngineeringConceptDefinition>,
    val lifecycle: EngineeringPackageLifecycle,
    val documentationRefs: List<EngineeringDocumentationRef> = emptyList(),
    val provenance: EngineeringPackageProvenance,
    val forbiddenAuthorityFields: List<EngineeringPackageForbiddenAuthorityField> = emptyList(),
) {
    fun hasRepresentationAuthority(): Boolean = forbiddenAuthorityFields.any { field ->
        when (field.authority) {
            EngineeringPackageAuthority.REPRESENTATION,
            EngineeringPackageAuthority.GRAPHIC_RESOURCE,
            EngineeringPackageAuthority.RENDERER,
                -> true
            EngineeringPackageAuthority.PRESENTATION,
            EngineeringPackageAuthority.SOURCE_MUTATION,
                -> false
        }
    }
}
