package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationPackageDescriptorContractTest {
    @Test
    fun `representation package descriptor carries profile resource variant preview and provenance facts`() {
        val descriptor = validDescriptor()

        val result = RepresentationPackageDescriptorValidator.validate(descriptor)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
        assertFalse(descriptor.hasSemanticAuthority())
        assertEquals("com.athena.example.representation.drive.iec", descriptor.packageId.value)
        assertEquals("com.athena.example.representation.drive", descriptor.coordinates.groupId.value)
        assertEquals("iec-drive-symbols", descriptor.coordinates.artifactId.value)
        assertEquals("1.0.0", descriptor.coordinates.version.value)
        assertEquals("iec60617", descriptor.supportedProfiles.single().profileId.value)
        assertEquals("iec", descriptor.supportedProfiles.single().tags.single().value)
        assertEquals("iec.frequency-drive.compact", descriptor.descriptorEntries.single().descriptorId.value)
        assertEquals(GraphicResourceKind.VECTOR_DOCUMENT, descriptor.resourceReferences.single().kind)
        assertEquals("style.stroke.device", descriptor.styleTokenRefs.single().value)
        assertEquals("compact", descriptor.variants.single().variantId.value)
        assertEquals("previews/iec-frequency-drive-compact.png", descriptor.previews.single().path)
    }

    @Test
    fun `representation package validation rejects semantic source compiler and engineering truth leaks`() {
        val descriptor = validDescriptor().copy(
            forbiddenAuthorityFields = listOf(
                RepresentationPackageForbiddenAuthorityField(
                    field = "source.connectSyntax",
                    authority = RepresentationPackageAuthority.SOURCE_MUTATION,
                ),
                RepresentationPackageForbiddenAuthorityField(
                    field = "engineering.products",
                    authority = RepresentationPackageAuthority.ENGINEERING_TRUTH,
                ),
                RepresentationPackageForbiddenAuthorityField(
                    field = "compiler.bindingRule",
                    authority = RepresentationPackageAuthority.COMPILER_BEHAVIOR,
                ),
                RepresentationPackageForbiddenAuthorityField(
                    field = "athenaSyntax.device",
                    authority = RepresentationPackageAuthority.ATHENA_SOURCE_SYNTAX,
                ),
            ),
        )

        val result = RepresentationPackageDescriptorValidator.validate(descriptor)

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.representation.semantic-leak-forbidden",
                "package.representation.semantic-leak-forbidden",
                "package.representation.semantic-leak-forbidden",
                "package.representation.semantic-leak-forbidden",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
        assertEquals(
            listOf(
                "source.connectSyntax",
                "engineering.products",
                "compiler.bindingRule",
                "athenaSyntax.device",
            ),
            result.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `representation package validation diagnoses unsupported graphic resource kinds`() {
        val descriptor = validDescriptor().copy(
            resourceReferences = listOf(
                validResource().copy(kind = GraphicResourceKind.VECTOR_DOCUMENT),
                validResource().copy(
                    resourceId = GraphicResourceId("resource.frequency-drive.mesh"),
                    kind = GraphicResourceKind.THREE_DIMENSIONAL_MESH,
                    path = "resources/frequency-drive.glb",
                ),
            ),
        )

        val result = RepresentationPackageDescriptorValidator.validate(descriptor)

        assertFalse(result.isValid)
        assertEquals(
            listOf("package.representation.resource-kind.unsupported"),
            result.diagnostics.map { it.code.wireValue },
        )
        assertEquals("resourceReferences[1].kind", result.diagnostics.single().subject)
        assertTrue(result.diagnostics.single().message.contains("deferred"))
    }

    private fun validDescriptor(): RepresentationPackageDescriptor = RepresentationPackageDescriptor(
        packageId = RepresentationPackageId("com.athena.example.representation.drive.iec"),
        coordinates = RepresentationPackageCoordinates(
            groupId = RepresentationPackageGroupId("com.athena.example.representation.drive"),
            artifactId = RepresentationPackageArtifactId("iec-drive-symbols"),
            version = RepresentationPackageVersion("1.0.0"),
        ),
        supportedProfiles = listOf(
            RepresentationSupportedProfile(
                profileId = PresentationProfileId("iec60617"),
                tags = listOf(PresentationProfileTag("iec")),
            ),
        ),
        descriptorEntries = listOf(
            RepresentationPackageDescriptorEntry(
                descriptorId = RepresentationDescriptorId("iec.frequency-drive.compact"),
                resourceId = GraphicResourceId("resource.frequency-drive.vector"),
                variants = listOf(RepresentationVariantId("compact")),
                styleTokenRefs = listOf(RepresentationStyleTokenRef("style.stroke.device")),
            ),
        ),
        resourceReferences = listOf(validResource()),
        styleTokenRefs = listOf(RepresentationStyleTokenRef("style.stroke.device")),
        variants = listOf(
            RepresentationVariantDefinition(
                variantId = RepresentationVariantId("compact"),
                displayName = "Compact",
            ),
        ),
        previews = listOf(
            RepresentationPackagePreviewRef(
                variantId = RepresentationVariantId("compact"),
                path = "previews/iec-frequency-drive-compact.png",
            ),
        ),
        lifecycle = RepresentationPackageLifecycle(
            state = RepresentationPackageLifecycleState.ACTIVE,
            sinceVersion = RepresentationPackageVersion("1.0.0"),
        ),
        provenance = RepresentationPackageProvenance(
            sources = listOf("athena-m32-demo"),
            reviewedBy = "Athena M32",
        ),
    )

    private fun validResource(): GraphicResourceRef = GraphicResourceRef(
        resourceId = GraphicResourceId("resource.frequency-drive.vector"),
        kind = GraphicResourceKind.VECTOR_DOCUMENT,
        path = "resources/frequency-drive.svg",
    )
}
