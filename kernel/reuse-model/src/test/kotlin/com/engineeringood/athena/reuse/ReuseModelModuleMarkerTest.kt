package com.engineeringood.athena.reuse

import kotlin.test.Test
import kotlin.test.assertEquals

class ReuseModelModuleMarkerTest {
    @Test
    fun `reports reuse-model module marker`() {
        assertEquals("kernel:reuse-model", ReuseModelModuleMarker().moduleName)
    }
}
