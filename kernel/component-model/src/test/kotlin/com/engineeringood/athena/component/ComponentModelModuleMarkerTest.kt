package com.engineeringood.athena.component

import kotlin.test.Test
import kotlin.test.assertEquals

class ComponentModelModuleMarkerTest {
    @Test
    fun `reports component-model module marker`() {
        assertEquals("kernel:component-model", ComponentModelModuleMarker().moduleName)
    }
}
