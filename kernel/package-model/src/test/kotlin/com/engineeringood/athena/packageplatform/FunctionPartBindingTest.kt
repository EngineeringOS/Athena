package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FunctionPartBindingTest {
    @Test
    fun `binding identity is function and implementation role independent from part selection`() {
        val first = binding(part = "motor-400w")
        val replacement = first.copy(part = part("motor-750w"))

        assertEquals("function:M1.drive#power", first.bindingId)
        assertEquals(first.bindingId, replacement.bindingId)
        assertEquals(first.functionId to first.implementationRole, replacement.functionId to replacement.implementationRole)
    }

    @Test
    fun `binding rejects entity targets invalid roles and non package part references`() {
        assertFailsWith<IllegalArgumentException> { binding(functionId = "entity:M1") }
        assertFailsWith<IllegalArgumentException> { binding(role = " ") }
        assertFailsWith<IllegalArgumentException> {
            PartItemReference(
                PackageItemIdentity(PackageIdentifier("com.vendor.motion", "1"), " ", "1"),
            )
        }
    }

    private fun binding(
        functionId: String = "function:M1.drive",
        role: String = "power",
        part: String = "motor-400w",
    ) = FunctionPartBinding(
        functionId = functionId,
        implementationRole = role,
        part = part(part),
    )

    private fun part(itemId: String) = PartItemReference(
        PackageItemIdentity(
            packageId = PackageIdentifier("com.vendor.motion", "1"),
            itemId = itemId,
            itemVersion = "1",
        ),
    )
}
