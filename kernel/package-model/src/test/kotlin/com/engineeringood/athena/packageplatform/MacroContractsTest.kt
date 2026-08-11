package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class MacroContractsTest {
    @Test
    fun `macro keeps relative representation children and explicit slots`() {
        val macroId = id("motor-macro")
        val child = id("motor-symbol")
        val macro = RepresentationMacro(
            macroId,
            listOf(MacroChildPlacement(child, 4, 8)),
            listOf(MacroBindingReference(child, "power", "primary")),
        )
        assertEquals(child, macro.bindings.single().childId)
        assertFailsWith<IllegalArgumentException> { MacroTransform(0, 0, 360) }
    }

    @Test
    fun `macro rejects duplicate children and unknown binding child`() {
        val child = id("motor-symbol")
        assertFailsWith<IllegalArgumentException> {
            RepresentationMacro(id("motor-macro"), listOf(MacroChildPlacement(child, 0, 0), MacroChildPlacement(child, 4, 0)), emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            RepresentationMacro(id("motor-macro"), listOf(MacroChildPlacement(child, 0, 0)), listOf(MacroBindingReference(id("other"), "power", "primary")))
        }
    }

    private fun id(item: String) = PackageItemIdentity(PackageIdentifier("com.athena.macros", "1"), item, "1")
}

