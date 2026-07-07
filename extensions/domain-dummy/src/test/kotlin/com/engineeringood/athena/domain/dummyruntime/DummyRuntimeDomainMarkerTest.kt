package com.engineeringood.athena.domain.dummyruntime

import kotlin.test.Test
import kotlin.test.assertEquals

class DummyRuntimeDomainMarkerTest {
    @Test
    fun `reports dummy runtime module marker`() {
        assertEquals("extensions:domain-dummy", DummyRuntimeDomainMarker().moduleName)
    }
}
