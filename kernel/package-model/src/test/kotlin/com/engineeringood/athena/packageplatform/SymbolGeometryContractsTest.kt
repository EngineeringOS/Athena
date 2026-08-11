package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SymbolGeometryContractsTest {
    @Test
    fun `symbol geometry keeps anchors compatibility-only and deterministic`() {
        val geometry = SymbolGeometry(
            resource = "resources/contactor.svg",
            viewBox = SymbolBounds(100, 80),
            center = SymbolPoint(50, 40),
            anchors = listOf(
                SymbolAnchor(
                    "power.in",
                    SymbolPoint(0, 40),
                    PortCompatibilityContract(
                        "power.in",
                        PortCompatibilityDirection.IN,
                        PortCompatibilityDomain.ELECTRICAL,
                        PortCompatibilityFlowKind.POWER,
                    ),
                ),
            ),
        )
        assertEquals("power.in", geometry.anchors.single().compatibility.key)
    }

    @Test
    fun `symbol geometry rejects duplicate anchors and out of bounds points`() {
        assertFails {
            SymbolGeometry(
                "resources/contact.svg",
                SymbolBounds(10, 10),
                SymbolPoint(5, 5),
                listOf(
                    SymbolAnchor("in", SymbolPoint(0, 0), PortCompatibilityContract("in", PortCompatibilityDirection.IN, PortCompatibilityDomain.ELECTRICAL, PortCompatibilityFlowKind.POWER)),
                    SymbolAnchor("in", SymbolPoint(10, 10), PortCompatibilityContract("in", PortCompatibilityDirection.IN, PortCompatibilityDomain.ELECTRICAL, PortCompatibilityFlowKind.POWER)),
                ),
            )
        }
    }
}
