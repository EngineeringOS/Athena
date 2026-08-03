package com.engineeringood.athena.composeruntime

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeRuntimeModuleMarkerTest {
    @Test
    fun `reports the compose runtime module name`() {
        assertEquals("ui:compose-workbench", ComposeRuntimeModuleMarker().moduleName)
    }

    @Test
    fun `uses the default shell descriptor`() {
        val descriptor = AthenaComposeShellDescriptor()

        assertEquals("Athena", descriptor.windowTitle)
        assertEquals("Compose runtime shell bootstrap ready.", descriptor.statusLine)
    }
}
