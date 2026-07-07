package com.engineeringood.athena.composeruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaComposeShellStateTest {
    @Test
    fun `projection session state defaults to non-authoritative inspection values`() {
        val projectionSession = AthenaComposeProjectionSessionState()

        assertTrue(projectionSession.supportedViews.isEmpty())
        assertEquals(null, projectionSession.activeViewId)
        assertEquals(null, projectionSession.selectedSemanticId)
        assertFalse(projectionSession.activeProjectionAvailable)
        assertFalse(projectionSession.selectedSemanticVisibleInActiveView)
    }

    @Test
    fun `projection session state can describe one canonical selection across views`() {
        val projectionSession = AthenaComposeProjectionSessionState(
            supportedViews = listOf(
                AthenaComposeProjectionViewState(
                    viewId = "cabinet",
                    displayName = "Cabinet",
                    description = "Cabinet placement view",
                ),
                AthenaComposeProjectionViewState(
                    viewId = "wiring",
                    displayName = "Wiring",
                    description = "Wiring connectivity view",
                ),
            ),
            activeViewId = "wiring",
            activeViewDisplayName = "Wiring",
            activeProjectionAvailable = true,
            selectedSemanticId = "component:PLC1",
            selectedSemanticVisibleInActiveView = true,
        )

        assertEquals(listOf("cabinet", "wiring"), projectionSession.supportedViews.map { view -> view.viewId })
        assertEquals("wiring", projectionSession.activeViewId)
        assertEquals("component:PLC1", projectionSession.selectedSemanticId)
        assertTrue(projectionSession.selectedSemanticVisibleInActiveView)
    }
}
