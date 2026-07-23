package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class M32DemoLayoutDensityProofTest {
    @Test
    fun `demo density proof rejects toy layout chrome duplicate labels and oversized viewbox`() {
        val proof = M32DemoLayoutDensityProofRunner().run(M32SamplePackageSet.loadDefault())

        assertTrue(proof.isValid)
        assertTrue(proof.components.size >= 3)
        assertTrue(proof.components.all { !it.normalBackgroundVisible })
        assertTrue(proof.components.all { !it.normalHitboxVisible })
        assertTrue(proof.components.all { it.duplicateVisibleLabelCount == 0 })
        assertTrue(proof.components.all { it.descriptorDrivenAnchors })
        assertTrue(proof.components.all { it.descriptorDrivenLabels })
        assertTrue(proof.components.all { !it.genericRectangleFallback })
        assertTrue(proof.compactComposition)
        assertTrue(proof.sheetNavigationVisible)
        assertTrue(proof.viewBox.tightToContent)
        assertNotEquals(1680.0, proof.viewBox.width)
        assertNotEquals(1188.0, proof.viewBox.height)
        assertFalse(proof.hardCodedViewBox)
    }
}
