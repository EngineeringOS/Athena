package com.engineeringood.athena.packageplatform

import kotlin.test.*

class PlaceholderContractsTest {
    @Test
    fun `placeholder allows representation paths only`() {
        assertNotNull(RepresentationPlaceholder("label", "label.text", PlaceholderValueType.TEXT))
        assertFailsWith<IllegalArgumentException> { RepresentationPlaceholder("part", "part.article", PlaceholderValueType.TEXT) }
        assertFailsWith<IllegalArgumentException> { RepresentationPlaceholder("port", "port.power", PlaceholderValueType.TEXT) }
    }
}

