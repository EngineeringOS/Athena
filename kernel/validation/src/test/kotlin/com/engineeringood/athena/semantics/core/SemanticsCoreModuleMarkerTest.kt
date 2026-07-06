package com.engineeringood.athena.semantics.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SemanticsCoreModuleMarkerTest {
    @Test
    fun `reports semantics core module marker`() {
        assertEquals("kernel:validation", SemanticsCoreModuleMarker().moduleName)
    }
}
