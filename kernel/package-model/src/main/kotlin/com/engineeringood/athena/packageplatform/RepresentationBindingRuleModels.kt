package com.engineeringood.athena.packageplatform

@JvmInline
value class RepresentationBindingRuleId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationBindingPriority(val value: Int) {
    init {
        require(value >= 0) { "Representation binding priority must be non-negative." }
    }

    override fun toString(): String = value.toString()
}

data class RepresentationBindingSelectorFact(
    val name: String,
    val value: String,
)

enum class RepresentationBindingSubjectKind {
    DEVICE,
    FUNCTION,
}

data class RepresentationBindingTarget(
    val representationPackageId: RepresentationPackageId,
    val descriptorId: RepresentationDescriptorId,
    val packageVersion: RepresentationPackageVersion,
    val variantId: RepresentationVariantId? = null,
)

enum class RepresentationBindingRuleLifecycleState {
    ACTIVE,
    DEPRECATED,
}

data class RepresentationBindingRuleLifecycle(
    val state: RepresentationBindingRuleLifecycleState,
)

data class RepresentationBindingRuleProvenance(
    val sources: List<String>,
    val reviewedBy: String,
)

data class RepresentationBindingRule(
    val ruleId: RepresentationBindingRuleId,
    val profileId: PresentationProfileId,
    val projectionContext: ProjectionContextId,
    val conceptId: EngineeringConceptId,
    val selectorFacts: List<RepresentationBindingSelectorFact> = emptyList(),
    val target: RepresentationBindingTarget,
    val priority: RepresentationBindingPriority,
    val lifecycle: RepresentationBindingRuleLifecycle,
    val provenance: RepresentationBindingRuleProvenance,
    val subjectKind: RepresentationBindingSubjectKind = RepresentationBindingSubjectKind.DEVICE,
)
