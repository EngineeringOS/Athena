package com.engineeringood.athena.connection

import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionModelModuleMarkerTest {
    @Test
    fun `reports connection-model module marker`() {
        assertEquals("kernel:connection-model", ConnectionModelModuleMarker().moduleName)
    }
}
