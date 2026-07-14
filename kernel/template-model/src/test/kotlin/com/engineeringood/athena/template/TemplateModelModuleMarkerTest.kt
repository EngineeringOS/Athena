package com.engineeringood.athena.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateModelModuleMarkerTest {
    @Test
    fun `reports template-model module marker`() {
        assertEquals("kernel:template-model", TemplateModelModuleMarker().moduleName)
    }
}
