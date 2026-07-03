package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringIrModuleMarkerTest {
    @Test
    fun `reports ir module marker`() {
        assertEquals("ir", EngineeringIrModuleMarker().moduleName)
    }
}
