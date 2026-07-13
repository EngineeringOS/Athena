package com.engineeringood.athena.part

import kotlin.test.Test
import kotlin.test.assertEquals

class PartModelModuleMarkerTest {
    @Test
    fun `reports part-model module marker`() {
        assertEquals("kernel:part-model", PartModelModuleMarker().moduleName)
    }
}
