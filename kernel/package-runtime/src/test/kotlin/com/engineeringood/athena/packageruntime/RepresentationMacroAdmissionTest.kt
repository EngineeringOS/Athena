package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class RepresentationMacroAdmissionTest {
    @Test
    fun `macro resolution uses explicit functions and deterministic ids`() {
        val child = id("motor-symbol")
        val macroId = id("motor-macro")
        val macro = RepresentationMacro(macroId, listOf(MacroChildPlacement(child, 4, 8)), listOf(MacroBindingReference(child, "power", "primary")))
        assertTrue(RepresentationMacroAdmission.admit(macro, setOf(child.key)).isValid)
        val request = MacroInsertionRequest(macroId, "sheet-1", mapOf("power" to "function:M1.drive"), MacroTransform(10, 20), "op-1")
        val first = RepresentationMacroResolver.resolve(macro, request)
        val second = RepresentationMacroResolver.resolve(macro, request)
        assertTrue(first.isValid)
        assertEquals(first.occurrences, second.occurrences)
        assertEquals("function:M1.drive", first.occurrences!!.single().functionId)
        assertEquals(14, first.occurrences.single().x)
        assertEquals(28, first.occurrences.single().y)
    }

    @Test
    fun `macro rejects missing function and non-ready child`() {
        val child = id("motor-symbol")
        val macroId = id("motor-macro")
        val macro = RepresentationMacro(macroId, listOf(MacroChildPlacement(child, 0, 0)), listOf(MacroBindingReference(child, "power", "primary")))
        assertFalse(RepresentationMacroAdmission.admit(macro, emptySet()).isValid)
        val result = RepresentationMacroResolver.resolve(macro, MacroInsertionRequest(macroId, "sheet-1", emptyMap(), MacroTransform(0, 0), "op-1"))
        assertFalse(result.isValid)
        assertEquals("macro.function.missing", result.diagnostics.single().code)
    }

    private fun id(item: String) = PackageItemIdentity(PackageIdentifier("com.athena.macros", "1"), item, "1")
}

