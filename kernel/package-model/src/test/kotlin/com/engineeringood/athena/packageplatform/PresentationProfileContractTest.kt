package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresentationProfileContractTest {
    @Test
    fun `presentation profile carries context style standard compatibility fallback and provenance facts`() {
        val profile = validProfile()

        val result = PresentationProfileDescriptorValidator.validate(profile)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals("iec60617", profile.profileId.value)
        assertEquals("1.0.0", profile.version.value)
        assertEquals("electrical-schematic", profile.projectionContexts.single().value)
        assertEquals("style.industrial-compact", profile.styleProfile.value)
        assertEquals("IEC", profile.standardTags.single().value)
        assertEquals("com.athena.example.representation.drive.iec", profile.compatibilityConstraints.single().packageId)
        assertEquals(PresentationProfileFallbackMode.FAIL_CLOSED, profile.fallbackPolicy.mode)
        assertEquals("Athena M32", profile.provenance.reviewedBy)
    }

    @Test
    fun `presentation profile validation rejects engineering resource and source mutation authority leaks`() {
        val profile = validProfile().copy(
            forbiddenAuthorityFields = listOf(
                PresentationProfileForbiddenAuthorityField(
                    field = "engineering.productParameters",
                    authority = PresentationProfileAuthority.ENGINEERING_TRUTH,
                ),
                PresentationProfileForbiddenAuthorityField(
                    field = "graphicResource.svgPath",
                    authority = PresentationProfileAuthority.GRAPHIC_RESOURCE_INTERNALS,
                ),
                PresentationProfileForbiddenAuthorityField(
                    field = "source.mutationRule",
                    authority = PresentationProfileAuthority.SOURCE_MUTATION,
                ),
            ),
        )

        val result = PresentationProfileDescriptorValidator.validate(profile)

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.presentation-profile.authority-forbidden",
                "package.presentation-profile.authority-forbidden",
                "package.presentation-profile.authority-forbidden",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    @Test
    fun `presentation policy facts model standards customers outputs and themes as profile facts`() {
        val profile = validProfile().copy(
            policyFacts = listOf(
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.STANDARD, "IEC"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.STANDARD, "ANSI"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.CUSTOMER, "customer-demo"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.OUTPUT, "print"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.OUTPUT, "maintenance"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.OUTPUT, "training"),
                PresentationProfilePolicyFact(PresentationProfilePolicyFactKind.THEME, "compact"),
            ),
        )

        val result = PresentationProfileDescriptorValidator.validate(profile)

        assertTrue(result.isValid)
        assertEquals(
            listOf("STANDARD", "STANDARD", "CUSTOMER", "OUTPUT", "OUTPUT", "OUTPUT", "THEME"),
            profile.policyFacts.map { it.kind.name },
        )
    }

    private fun validProfile(): PresentationProfileDescriptor = PresentationProfileDescriptor(
        profileId = PresentationProfileId("iec60617"),
        version = PresentationProfileVersion("1.0.0"),
        projectionContexts = listOf(ProjectionContextId("electrical-schematic")),
        styleProfile = PresentationStyleProfileId("style.industrial-compact"),
        standardTags = listOf(RepresentationStandardTag("IEC")),
        compatibilityConstraints = listOf(
            PresentationPackageCompatibilityConstraint(
                packageId = "com.athena.example.representation.drive.iec",
                versionRange = "1.0.0+",
            ),
        ),
        fallbackPolicy = PresentationProfileFallbackPolicy(PresentationProfileFallbackMode.FAIL_CLOSED),
        provenance = PresentationProfileProvenance(
            sources = listOf("athena-m32-demo"),
            reviewedBy = "Athena M32",
        ),
    )
}
