package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class M32ProductSmokeProofTest {
    @Test
    fun `structured smoke proves package binding rendering routing bounds profile switch and no fallback`() {
        val evidence = M32ProductSmokeProofRunner().run(M32SamplePackageSet.loadDefault())

        assertTrue(evidence.isValid)
        assertEquals("structured-evidence", evidence.acceptanceAuthority)
        assertTrue(evidence.subjects.size >= 3)
        assertTrue(evidence.subjects.all { it.engineeringPackageResolved })
        assertTrue(evidence.subjects.all { it.representationPackageResolved })
        assertTrue(evidence.subjects.all { it.manifestSelected })
        assertTrue(evidence.subjects.all { it.descriptorValidated })
        assertTrue(evidence.subjects.all { it.anchorMapped })
        assertTrue(evidence.subjects.all { it.labelBound })
        assertTrue(evidence.subjects.all { it.occurrenceCreated })
        assertTrue(evidence.subjects.all { it.derivedBounds })
        assertTrue(evidence.routes.any { it.routeAnchored })
        assertTrue(evidence.routes.all { !it.centerFallbackAccepted })
        assertTrue(evidence.profileSwitch.sourceUnchanged)
        assertTrue(evidence.profileSwitch.representationChanged)
        assertFalse(evidence.visualEvidence.satisfiesPackageClaimsWithoutStructuredProof)
        assertEquals("secondary-human-review", evidence.visualEvidence.role)
    }
}
