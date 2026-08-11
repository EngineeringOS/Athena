package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.MacroInsertionRequest
import com.engineeringood.athena.packageplatform.MacroTransform
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryLockedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LockedRepresentationPackageRuntimeTest {
    private val packageId = PackageIdentifier("com.athena.iec", "1.0.0")

    @Test
    fun `lock backed Macro decodes admits and resolves explicit Function slots`() {
        val item = RepositoryLockedItem(
            itemId = "starter",
            itemVersion = "1.0.0",
            kind = "MACRO",
            digest = "sha256:${"a".repeat(64)}",
            sourcePath = "src/reuse.athena",
            attributes = mapOf(
                "child.000.element" to "breaker_element",
                "child.000.x" to "0",
                "child.000.y" to "0",
                "child.000.rotation" to "0",
                "child.000.functionSlot" to "protection",
                "child.000.bindingRole" to "primary",
                "child.001.element" to "motor_element",
                "child.001.x" to "12",
                "child.001.y" to "4",
                "child.001.rotation" to "90",
                "child.001.functionSlot" to "load",
                "child.001.bindingRole" to "primary",
            ),
        )
        val ready = setOf(
            PackageItemIdentity(packageId, "breaker_element", "1.0.0").key,
            PackageItemIdentity(packageId, "motor_element", "1.0.0").key,
        )

        val decoded = LockedRepresentationPackageRuntime.decodeMacro(packageId, item, ready)
        val macro = assertNotNull(decoded.macro, decoded.diagnostics.joinToString())
        val resolved = RepresentationMacroResolver.resolve(
            macro,
            MacroInsertionRequest(
                macroId = macro.macroId,
                sheetId = "sheet-1",
                functionIdsBySlot = mapOf("protection" to "function:Q1.protection", "load" to "function:M1.drive"),
                transform = MacroTransform(20, 30),
                operationId = "operation-1",
            ),
        )

        assertTrue(resolved.isValid, resolved.diagnostics.joinToString())
        val occurrences = assertNotNull(resolved.occurrences)
        assertEquals(listOf(20 to 30, 32 to 34), occurrences.map { it.x to it.y })
        assertEquals(listOf("function:Q1.protection", "function:M1.drive"), occurrences.map { it.functionId })
    }

    @Test
    fun `Macro decoder rejects incomplete child payload and Variant target mismatch`() {
        val incomplete = RepositoryLockedItem(
            itemId = "starter",
            itemVersion = "1.0.0",
            kind = "MACRO",
            digest = "sha256:${"a".repeat(64)}",
            sourcePath = "src/reuse.athena",
            attributes = mapOf("child.000.element" to "breaker_element"),
        )
        assertFalse(LockedRepresentationPackageRuntime.decodeMacro(packageId, incomplete, emptySet()).isValid)

        val element = RepositoryLockedItem("contactor_element", "1.0.0", "ELEMENT", "sha256:${"b".repeat(64)}", "src/contactor.athena", attributes = mapOf("interfaceFingerprint" to "same"))
        val wrongVariant = RepositoryLockedItem("motor_variant", "1.0.0", "VARIANT", "sha256:${"c".repeat(64)}", "src/variant.athena", attributes = mapOf("element" to "motor_element", "interfaceFingerprint" to "same"))
        assertFalse(LockedRepresentationPackageRuntime.variantMatches(packageId, element, packageId, wrongVariant))
    }
}
