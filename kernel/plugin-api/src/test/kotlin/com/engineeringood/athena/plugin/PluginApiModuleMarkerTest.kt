package com.engineeringood.athena.plugin

import kotlin.test.Test
import kotlin.test.assertEquals

class PluginApiModuleMarkerTest {
    @Test
    fun `reports plugin api module marker`() {
        assertEquals("kernel:plugin-api", PluginApiModuleMarker().moduleName)
    }
}
