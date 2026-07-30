package com.engineeringood.athena.packageruntime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class M32DemoLayoutDensityProofTest {
    @Test
    fun `demo density evidence rejects toy layout chrome duplicate labels and oversized viewbox`() {
        val evidence = M32DemoLayoutDensityProofRunner().run(M32SamplePackageSet.loadDefault())

        assertTrue(evidence.isValid)
        assertTrue(evidence.components.size >= 3)
        assertTrue(evidence.components.all { !it.normalBackgroundVisible })
        assertTrue(evidence.components.all { !it.normalHitboxVisible })
        assertTrue(evidence.components.all { it.duplicateVisibleLabelCount == 0 })
        assertTrue(evidence.components.all { it.descriptorDrivenAnchors })
        assertTrue(evidence.components.all { it.descriptorDrivenLabels })
        assertTrue(evidence.components.all { !it.genericRectangleFallback })
        assertTrue(evidence.compactComposition)
        assertTrue(evidence.sheetNavigationVisible)
        assertTrue(evidence.viewBox.tightToContent)
        assertNotEquals(1680.0, evidence.viewBox.width)
        assertNotEquals(1188.0, evidence.viewBox.height)
        assertFalse(evidence.hardCodedViewBox)
    }
}
