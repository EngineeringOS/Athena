package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BindingManifestContractTest {
    @Test
    fun `binding manifest links engineering package concept profiles and representation packages`() {
        val manifest = validManifest()

        val result = BindingManifestValidator.validate(manifest)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals("com.athena.example.engineering.drive.compact-vfd", manifest.engineeringPackageId)
        assertEquals("1.0.0+", manifest.engineeringPackageVersionRange)
        assertEquals("FrequencyDrive", manifest.conceptId.value)
        assertEquals("com.athena.example.representation.drive.iec", manifest.defaultRepresentationPackageId)
        assertEquals("com.athena.example.representation.drive.compact", manifest.alternativeRepresentationPackageIds.single())
        assertEquals("iec", manifest.compatibleProfileTags.single().value)
        assertEquals("industrial-compact", manifest.policyTags.single().value)
        assertEquals("Athena M32", manifest.provenance.reviewedBy)
    }

    @Test
    fun `binding manifest validation rejects geometry resource compiler and mutation authority leaks`() {
        val manifest = validManifest().copy(
            forbiddenAuthorityFields = listOf(
                BindingManifestForbiddenAuthorityField("geometry.bounds", BindingManifestAuthority.REPRESENTATION_GEOMETRY),
                BindingManifestForbiddenAuthorityField("resource.svgPath", BindingManifestAuthority.GRAPHIC_RESOURCE_INTERNALS),
                BindingManifestForbiddenAuthorityField("compiler.select", BindingManifestAuthority.COMPILER_BEHAVIOR),
                BindingManifestForbiddenAuthorityField("source.connect", BindingManifestAuthority.SOURCE_MUTATION),
            ),
        )

        val result = BindingManifestValidator.validate(manifest)

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.binding-manifest.authority-forbidden",
                "package.binding-manifest.authority-forbidden",
                "package.binding-manifest.authority-forbidden",
                "package.binding-manifest.authority-forbidden",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    private fun validManifest(): BindingManifest = BindingManifest(
        manifestId = BindingManifestId("binding.drive.compact-vfd.iec"),
        engineeringPackageId = "com.athena.example.engineering.drive.compact-vfd",
        engineeringPackageVersionRange = "1.0.0+",
        conceptId = EngineeringConceptId("FrequencyDrive"),
        defaultRepresentationPackageId = "com.athena.example.representation.drive.iec",
        alternativeRepresentationPackageIds = listOf("com.athena.example.representation.drive.compact"),
        compatibleProfileTags = listOf(PresentationProfileTag("iec")),
        policyTags = listOf(BindingPolicyTag("industrial-compact")),
        provenance = BindingManifestProvenance(
            sources = listOf("athena-m32-demo"),
            reviewedBy = "Athena M32",
        ),
    )
}
