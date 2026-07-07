package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringModuleMarkerTest {
    @Test
    fun `reports engineering model module marker`() {
        assertEquals("kernel:engineering-model", EngineeringModuleMarker().moduleName)
    }
}
