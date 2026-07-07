package com.engineeringood.athena.layout

import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutModelMarkerTest {
    @Test
    fun `reports layout model marker`() {
        assertEquals("kernel:layout-model", LayoutModelMarker().moduleName)
    }
}
