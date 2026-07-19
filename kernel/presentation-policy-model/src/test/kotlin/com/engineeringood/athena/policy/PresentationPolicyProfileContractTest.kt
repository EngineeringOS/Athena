package com.engineeringood.athena.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PresentationPolicyProfileContractTest {
    @Test
    fun `athena industrial control v0 covers the six M25 sample families`() {
        val profile = AthenaIndustrialControlV0Profile.profile()
        val families = listOf(
            "plc-controller",
            "hmi-operator",
            "terminal-block",
            "power-supply",
            "protection-device",
            "load-actuator",
        )

        val selections = families.map { profile.selectRepresentation(ComponentFamilyKey(it)) }

        assertEquals("athena-industrial-control-v0", profile.profileId.value)
        assertFalse(profile.claimsIecCompleteness)
        assertTrue(selections.all { it is RepresentationSelection.Supported })
    }

    @Test
    fun `unsupported families produce diagnosable fallback metadata`() {
        val profile = AthenaIndustrialControlV0Profile.profile()
        val selection = profile.selectRepresentation(ComponentFamilyKey("servo-drive"))

        assertTrue(selection is RepresentationSelection.Fallback)
        assertEquals("servo-drive", selection.family.value)
        assertEquals("athena-industrial-control-v0", selection.profileId.value)
        assertNotNull(selection.diagnostic)
        assertTrue(selection.diagnostic.message.contains("servo-drive"))
    }

    @Test
    fun `accepted proof can assert zero fallback symbols`() {
        val profile = AthenaIndustrialControlV0Profile.profile()
        val proof = RepresentationPolicyCoverageProof(
            profile = profile,
            mandatoryFamilies = listOf(
                ComponentFamilyKey("plc-controller"),
                ComponentFamilyKey("terminal-block"),
                ComponentFamilyKey("power-supply"),
                ComponentFamilyKey("load-actuator"),
            ),
        )

        assertTrue(proof.hasZeroFallbackSymbols())
        assertEquals(emptyList(), proof.fallbackSelections())
    }
}
