package com.engineeringood.athena.integrations.scm.git

import kotlin.test.Test
import kotlin.test.assertEquals

class GitScmIntegrationModuleMarkerTest {
    @Test
    fun `reports scm-git integration module marker`() {
        assertEquals("integrations:scm-git", GitScmIntegrationModuleMarker().moduleName)
    }
}
