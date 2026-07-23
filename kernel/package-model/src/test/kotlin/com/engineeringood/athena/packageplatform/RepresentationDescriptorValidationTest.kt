package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationDescriptorValidationTest {
    @Test
    fun `representation descriptor validation accepts resource bounds anchors labels hotspots transforms variants and style facts`() {
        val descriptor = validDescriptor()

        val result = RepresentationDescriptorValidator.validate(descriptor, validContext())

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals(GraphicResourceId("resource.frequency-drive.vector"), descriptor.resource.resourceId)
        assertEquals(GraphicResourceKind.VECTOR_DOCUMENT, descriptor.resource.kind)
        assertEquals(80.0, descriptor.bounds.width)
        assertEquals(48.0, descriptor.bounds.height)
        assertEquals("powerIn", descriptor.anchors.single().anchorId.value)
        assertEquals("tag", descriptor.labelSlots.single().slotId.value)
        assertEquals("hitbox.primary", descriptor.hotspots.single().hotspotId.value)
        assertEquals(RepresentationTransformKind.TRANSLATE, descriptor.transforms.single().kind)
        assertEquals("compact", descriptor.variants.single().value)
        assertEquals("style.stroke.device", descriptor.styleTokenRefs.single().value)
        assertEquals("descriptor.anchor.required", descriptor.validationRuleRefs.single().value)
    }

    @Test
    fun `representation descriptor validation diagnoses missing resource duplicate anchors invalid bounds missing labels and unsupported variants`() {
        val descriptor = validDescriptor().copy(
            resource = RepresentationDescriptorResourceBinding(
                resourceId = GraphicResourceId("resource.missing"),
                kind = GraphicResourceKind.VECTOR_DOCUMENT,
            ),
            bounds = RepresentationDescriptorBounds(width = 0.0, height = -1.0),
            anchors = listOf(
                validAnchor(),
                validAnchor().copy(x = 20.0),
            ),
            labelSlots = emptyList(),
            variants = listOf(RepresentationVariantId("detailed")),
        )

        val result = RepresentationDescriptorValidator.validate(descriptor, validContext())

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.representation.descriptor.resource-missing",
                "package.representation.descriptor.bounds-invalid",
                "package.representation.descriptor.anchor-duplicate",
                "package.representation.descriptor.label-slot-missing",
                "package.representation.descriptor.variant-unsupported",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    @Test
    fun `representation descriptor validation rejects graphic resource fields as engineering truth`() {
        val descriptor = validDescriptor().copy(
            forbiddenSemanticAuthorityClaims = listOf(
                RepresentationDescriptorForbiddenSemanticAuthorityClaim(
                    source = RepresentationDescriptorSemanticAuthoritySource.GRAPHIC_RESOURCE_ID,
                    field = "resource.frequency-drive.vector",
                ),
                RepresentationDescriptorForbiddenSemanticAuthorityClaim(
                    source = RepresentationDescriptorSemanticAuthoritySource.VISIBLE_LABEL_TEXT,
                    field = "MOTOR",
                ),
                RepresentationDescriptorForbiddenSemanticAuthorityClaim(
                    source = RepresentationDescriptorSemanticAuthoritySource.CSS_CLASS,
                    field = "motor-symbol",
                ),
                RepresentationDescriptorForbiddenSemanticAuthorityClaim(
                    source = RepresentationDescriptorSemanticAuthoritySource.FILE_NAME,
                    field = "motor.svg",
                ),
            ),
        )

        val result = RepresentationDescriptorValidator.validate(descriptor, validContext())

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.representation.descriptor.semantic-authority-forbidden",
                "package.representation.descriptor.semantic-authority-forbidden",
                "package.representation.descriptor.semantic-authority-forbidden",
                "package.representation.descriptor.semantic-authority-forbidden",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    private fun validDescriptor(): RepresentationDescriptor = RepresentationDescriptor(
        descriptorId = RepresentationDescriptorId("iec.frequency-drive.compact"),
        resource = RepresentationDescriptorResourceBinding(
            resourceId = GraphicResourceId("resource.frequency-drive.vector"),
            kind = GraphicResourceKind.VECTOR_DOCUMENT,
        ),
        bounds = RepresentationDescriptorBounds(width = 80.0, height = 48.0),
        anchors = listOf(validAnchor()),
        labelSlots = listOf(
            RepresentationLabelSlotDefinition(
                slotId = RepresentationLabelSlotId("tag"),
                role = RepresentationLabelSlotRole.DEVICE_TAG,
                required = true,
            ),
        ),
        hotspots = listOf(
            RepresentationHotspotDefinition(
                hotspotId = RepresentationHotspotId("hitbox.primary"),
                bounds = RepresentationDescriptorBounds(width = 80.0, height = 48.0),
            ),
        ),
        transforms = listOf(
            RepresentationTransformDefinition(
                kind = RepresentationTransformKind.TRANSLATE,
                x = 10.0,
                y = 12.0,
            ),
        ),
        variants = listOf(RepresentationVariantId("compact")),
        styleTokenRefs = listOf(RepresentationStyleTokenRef("style.stroke.device")),
        validationRuleRefs = listOf(RepresentationDescriptorValidationRuleRef("descriptor.anchor.required")),
    )

    private fun validAnchor(): RepresentationAnchorDefinition = RepresentationAnchorDefinition(
        anchorId = RepresentationAnchorId("powerIn"),
        x = 0.0,
        y = 24.0,
        side = RepresentationAnchorSide.LEFT,
    )

    private fun validContext(): RepresentationDescriptorValidationContext = RepresentationDescriptorValidationContext(
        resourceReferences = listOf(
            GraphicResourceRef(
                resourceId = GraphicResourceId("resource.frequency-drive.vector"),
                kind = GraphicResourceKind.VECTOR_DOCUMENT,
                path = "resources/frequency-drive.svg",
            ),
        ),
        requiredLabelSlots = setOf(RepresentationLabelSlotId("tag")),
        supportedVariants = setOf(RepresentationVariantId("compact")),
    )
}
