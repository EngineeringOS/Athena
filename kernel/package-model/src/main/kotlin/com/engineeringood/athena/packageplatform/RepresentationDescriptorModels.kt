package com.engineeringood.athena.packageplatform

@JvmInline
value class RepresentationAnchorId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationLabelSlotId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationHotspotId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RepresentationDescriptorValidationRuleRef(val value: String) {
    override fun toString(): String = value
}

data class RepresentationDescriptorBounds(
    val width: Double,
    val height: Double,
)

data class RepresentationDescriptorResourceBinding(
    val resourceId: GraphicResourceId,
    val kind: GraphicResourceKind,
)

enum class RepresentationAnchorSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CENTER,
}

data class RepresentationAnchorDefinition(
    val anchorId: RepresentationAnchorId,
    val x: Double,
    val y: Double,
    val side: RepresentationAnchorSide,
)

enum class RepresentationLabelSlotRole {
    DEVICE_TAG,
    MODEL,
    TERMINAL_NUMBER,
    REFERENCE,
}

data class RepresentationLabelSlotDefinition(
    val slotId: RepresentationLabelSlotId,
    val role: RepresentationLabelSlotRole,
    val required: Boolean,
)

data class RepresentationHotspotDefinition(
    val hotspotId: RepresentationHotspotId,
    val bounds: RepresentationDescriptorBounds,
)

enum class RepresentationTransformKind {
    TRANSLATE,
    ROTATE,
    SCALE,
}

data class RepresentationTransformDefinition(
    val kind: RepresentationTransformKind,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val angleDegrees: Double = 0.0,
    val scale: Double = 1.0,
)

enum class RepresentationDescriptorSemanticAuthoritySource {
    GRAPHIC_RESOURCE_ID,
    VISIBLE_LABEL_TEXT,
    CSS_CLASS,
    FILE_NAME,
}

data class RepresentationDescriptorForbiddenSemanticAuthorityClaim(
    val source: RepresentationDescriptorSemanticAuthoritySource,
    val field: String,
)

data class RepresentationDescriptor(
    val descriptorId: RepresentationDescriptorId,
    val resource: RepresentationDescriptorResourceBinding,
    val bounds: RepresentationDescriptorBounds,
    val anchors: List<RepresentationAnchorDefinition> = emptyList(),
    val labelSlots: List<RepresentationLabelSlotDefinition> = emptyList(),
    val hotspots: List<RepresentationHotspotDefinition> = emptyList(),
    val transforms: List<RepresentationTransformDefinition> = emptyList(),
    val variants: List<RepresentationVariantId> = emptyList(),
    val styleTokenRefs: List<RepresentationStyleTokenRef> = emptyList(),
    val validationRuleRefs: List<RepresentationDescriptorValidationRuleRef> = emptyList(),
    val forbiddenSemanticAuthorityClaims: List<RepresentationDescriptorForbiddenSemanticAuthorityClaim> = emptyList(),
)

data class RepresentationDescriptorValidationContext(
    val resourceReferences: List<GraphicResourceRef>,
    val requiredLabelSlots: Set<RepresentationLabelSlotId> = emptySet(),
    val supportedVariants: Set<RepresentationVariantId> = emptySet(),
)
