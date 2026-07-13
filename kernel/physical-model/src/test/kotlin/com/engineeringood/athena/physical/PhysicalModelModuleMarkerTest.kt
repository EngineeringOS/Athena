package com.engineeringood.athena.physical

import kotlin.test.Test
import kotlin.test.assertEquals

class PhysicalModelModuleMarkerTest {
    @Test
    fun `reports physical-model module marker`() {
        assertEquals("kernel:physical-model", PhysicalModelModuleMarker().moduleName)
    }
}
