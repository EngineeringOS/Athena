package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageModuleMarkerTest {
    @Test
    fun `reports language module marker`() {
        assertEquals("kernel:language", LanguageModuleMarker().moduleName)
    }
}
