package com.engineeringood.athena.projection

import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectionModelMarkerTest {
    @Test
    fun `reports projection model marker`() {
        assertEquals("kernel:projection-model", ProjectionModelMarker().moduleName)
    }
}
