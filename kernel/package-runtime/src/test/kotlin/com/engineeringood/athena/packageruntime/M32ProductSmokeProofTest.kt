package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class M32ProductSmokeProofTest {
    @Test
    fun `structured smoke proves package binding rendering routing bounds profile switch and no fallback`() {
        val proof = M32ProductSmokeProofRunner().run(M32SamplePackageSet.loadDefault())

        assertTrue(proof.isValid)
        assertEquals("structured-proof", proof.acceptanceAuthority)
        assertTrue(proof.subjects.size >= 3)
        assertTrue(proof.subjects.all { it.engineeringPackageResolved })
        assertTrue(proof.subjects.all { it.representationPackageResolved })
        assertTrue(proof.subjects.all { it.manifestSelected })
        assertTrue(proof.subjects.all { it.descriptorValidated })
        assertTrue(proof.subjects.all { it.anchorMapped })
        assertTrue(proof.subjects.all { it.labelBound })
        assertTrue(proof.subjects.all { it.occurrenceCreated })
        assertTrue(proof.subjects.all { it.derivedBounds })
        assertTrue(proof.subjects.all { !it.rendererFallbackAccepted })
        assertTrue(proof.routes.any { it.routeAnchored })
        assertTrue(proof.routes.all { !it.centerFallbackAccepted })
        assertTrue(proof.profileSwitch.sourceUnchanged)
        assertTrue(proof.profileSwitch.representationChanged)
        assertFalse(proof.visualEvidence.satisfiesPackageClaimsWithoutStructuredProof)
        assertEquals("secondary-human-review", proof.visualEvidence.role)
    }
}
