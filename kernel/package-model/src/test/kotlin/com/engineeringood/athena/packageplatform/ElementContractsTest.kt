package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertFails

class ElementContractsTest {
    @Test
    fun `element requires unique children slots and ports`() {
        val child = PackageItemIdentity(PackageIdentifier("com.athena.iec", "1.0.0"), "contactor", "1.0.0")
        assertFails {
            ElementComposition(
                children = listOf(ElementChildReference(child, 0), ElementChildReference(child, 1)),
                functionSlots = listOf(FunctionSlot("main", "switching")),
                ports = listOf(ElementPort("line.in", child, "line.in", "main")),
            )
        }
    }
}
