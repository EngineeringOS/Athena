package com.engineeringood.athena.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationPolicyDiagnosticsTest {
    @Test
    fun `fallback diagnostics include profile family and missing capability`() {
        val composer = ComponentRepresentationComposer(AthenaIndustrialControlV0Profile.profile())
        val snapshot = composer.compose(
            listOf(ComponentRepresentationRequest(ComponentSubjectKey("DRV1"), ComponentFamilyKey("servo-drive"))),
        )

        val diagnostic = snapshot.coverageDiagnostics().single()

        assertEquals("athena-industrial-control-v0", diagnostic.profileId.value)
        assertEquals("servo-drive", diagnostic.family.value)
        assertEquals("representation-family", diagnostic.missingCapability)
        assertTrue(diagnostic.message.contains("servo-drive"))
    }

    @Test
    fun `mandatory proof assertion fails when fallback is present`() {
        val composer = ComponentRepresentationComposer(AthenaIndustrialControlV0Profile.profile())
        val snapshot = composer.compose(
            listOf(
                ComponentRepresentationRequest(ComponentSubjectKey("PLC1"), ComponentFamilyKey("plc-controller")),
                ComponentRepresentationRequest(ComponentSubjectKey("DRV1"), ComponentFamilyKey("servo-drive")),
            ),
        )

        assertFalse(snapshot.hasZeroFallbackSymbols())
        assertFalse(snapshot.acceptedProofSatisfied())
        assertEquals(listOf("DRV1"), snapshot.fallbackSubjects())
    }
}
