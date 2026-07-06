package com.engineeringood.athena.cli

import kotlin.test.Test
import kotlin.test.assertEquals

class CliModuleMarkerTest {
    @Test
    fun `reports cli module marker`() {
        assertEquals("apps:cli", CliModuleMarker().moduleName)
    }
}
