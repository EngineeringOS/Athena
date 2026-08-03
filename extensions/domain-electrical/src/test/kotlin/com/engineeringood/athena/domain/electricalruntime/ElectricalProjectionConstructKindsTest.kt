package com.engineeringood.athena.domain.electricalruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectricalProjectionConstructKindsTest {

    @Test
    fun `electrical package contributes the seven committed construct kinds`() {
        assertEquals(
            setOf(
                "power-rail",
                "rung",
                "branch",
                "wire-bundle",
                "terminal-strip",
                "contact-group",
                "coil-group",
            ),
            ElectricalProjectionConstructKinds.supportedKinds,
        )
    }

    @Test
    fun `implementations satisfy the ProjectionConstruct contract`() {
        val implementations = ElectricalProjectionConstructKinds.implementationsFor("schematic", "S1")
        assertEquals(7, implementations.size)
        assertTrue(implementations.all { implementation -> implementation.kind in ElectricalProjectionConstructKinds.supportedKinds })
        assertTrue(implementations.all { implementation -> implementation.sourceTrace.startsWith("electrical-package:") })
    }
}
