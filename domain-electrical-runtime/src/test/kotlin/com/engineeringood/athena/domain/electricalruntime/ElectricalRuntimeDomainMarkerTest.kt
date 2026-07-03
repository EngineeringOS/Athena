package com.engineeringood.athena.domain.electricalruntime

import kotlin.test.Test
import kotlin.test.assertEquals

class ElectricalRuntimeDomainMarkerTest {
    @Test
    fun `reports electrical runtime module marker`() {
        assertEquals("domain-electrical-runtime", ElectricalRuntimeDomainMarker().moduleName)
    }
}
