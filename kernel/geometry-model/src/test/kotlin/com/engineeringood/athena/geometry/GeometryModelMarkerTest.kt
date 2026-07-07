package com.engineeringood.athena.geometry

import kotlin.test.Test
import kotlin.test.assertEquals

class GeometryModelMarkerTest {
    @Test
    fun `reports geometry model marker`() {
        assertEquals("kernel:geometry-model", GeometryModelMarker().moduleName)
    }
}
