package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElementCompositionAdmissionTest {
    @Test
    fun `unresolved child and anchor fail closed`() {
        val child = PackageItemIdentity(PackageIdentifier("com.athena.iec", "1.0.0"), "contactor", "1.0.0")
        val result = ElementCompositionAdmission.admit(
            ElementComposition(
                listOf(ElementChildReference(child, 0)),
                listOf(FunctionSlot("main", "switching")),
                listOf(ElementPort("line.in", child, "missing", "main")),
            ),
            admittedChildren = emptySet(),
            declaredAnchorKeys = emptyMap(),
        )
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "element.child.unresolved" })
        assertTrue(result.diagnostics.any { it.code == "element.port.anchor-unresolved" })
    }
}
