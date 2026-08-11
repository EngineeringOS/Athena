package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import kotlin.test.*

class PlaceholderResolutionTest {
    private val schema = PlaceholderSchema(listOf(
        RepresentationPlaceholder("label", "label.text", PlaceholderValueType.TEXT),
        RepresentationPlaceholder("stroke", "style.stroke", PlaceholderValueType.TEXT),
    ))

    @Test
    fun `typed representation placeholders resolve deterministically`() {
        val result = PlaceholderResolver.resolve(schema, listOf(PlaceholderAssignment("stroke", PackageItemValue.TextValue("#000000")), PlaceholderAssignment("label", PackageItemValue.TextValue("M1"))))
        assertTrue(result.isValid)
        assertEquals(listOf("label", "stroke"), result.values!!.keys.toList())
    }

    @Test
    fun `missing and wrong placeholder values fail closed`() {
        val result = PlaceholderResolver.resolve(schema, listOf(PlaceholderAssignment("label", PackageItemValue.NumberValue("1"))))
        assertFalse(result.isValid)
        assertNull(result.values)
        assertTrue(result.diagnostics.any { it.code == "placeholder.value.type" })
        assertTrue(result.diagnostics.any { it.code == "placeholder.value.missing" })
    }
}

