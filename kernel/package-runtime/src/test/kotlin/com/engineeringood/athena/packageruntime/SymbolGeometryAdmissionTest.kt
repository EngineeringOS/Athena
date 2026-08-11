package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolGeometryAdmissionTest {
    @Test
    fun `undeclared resource fails closed`() {
        val result = SymbolGeometryAdmission.admit(geometry(), declaredResourcePaths = emptySet())
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "symbol.resource.undeclared" })
    }

    @Test
    fun `declared resource admits without engineering semantics`() {
        val result = SymbolGeometryAdmission.admit(geometry())
        assertTrue(result.isValid)
    }

    private fun geometry() = SymbolGeometry(
        "resources/contact.svg",
        SymbolBounds(100, 80),
        SymbolPoint(50, 40),
        listOf(
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
}
