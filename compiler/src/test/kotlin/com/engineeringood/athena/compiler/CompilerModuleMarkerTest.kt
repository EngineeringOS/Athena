package com.engineeringood.athena.compiler

import kotlin.test.Test
import kotlin.test.assertEquals

class CompilerModuleMarkerTest {
    @Test
    fun `reports compiler module marker`() {
        assertEquals("compiler", CompilerModuleMarker().moduleName)
    }
}
