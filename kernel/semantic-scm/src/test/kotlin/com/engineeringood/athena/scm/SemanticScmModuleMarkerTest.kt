package com.engineeringood.athena.scm

import kotlin.test.Test
import kotlin.test.assertEquals

class SemanticScmModuleMarkerTest {
    @Test
    fun `reports semantic scm module marker`() {
        assertEquals("kernel:semantic-scm", SemanticScmModuleMarker().moduleName)
    }
}
