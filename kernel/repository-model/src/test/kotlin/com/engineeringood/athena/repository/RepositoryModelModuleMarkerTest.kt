package com.engineeringood.athena.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryModelModuleMarkerTest {
    @Test
    fun `reports repository-model module marker`() {
        assertEquals("kernel:repository-model", RepositoryModelModuleMarker().moduleName)
    }
}
