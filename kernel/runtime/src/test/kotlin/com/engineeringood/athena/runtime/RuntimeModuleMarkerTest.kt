package com.engineeringood.athena.runtime

import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeModuleMarkerTest {
    @Test
    fun `reports runtime module marker`() {
        assertEquals("kernel:runtime", RuntimeModuleMarker().moduleName)
    }
}
