package com.engineeringood.athena.apps.composeviewer

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ComposeViewerAppModuleMarkerTest {
    @Test
    fun `reports the compose viewer app module name`() {
        assertEquals("apps:desktop-viewer", ComposeViewerAppModuleMarker().moduleName)
    }

    @Test
    fun `produces a deterministic bootstrap smoke message`() {
        assertContains(AthenaComposeViewerBootstrap.smokeMessage(), "Athena")
    }
}
