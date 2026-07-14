package com.engineeringood.athena.authoring

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthoringModelModuleMarkerTest {
    @Test
    fun `reports authoring-model module marker`() {
        assertEquals("kernel:authoring-model", AuthoringModelModuleMarker().moduleName)
    }
}
