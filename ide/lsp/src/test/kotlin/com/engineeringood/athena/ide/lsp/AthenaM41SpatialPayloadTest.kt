package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaRuntime
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM41SpatialPayloadTest {
    @Test
    fun `LSP payload preserves Projection Spatial and Presentation identities`() {
        val root = resolveRepoRoot()
        val context = AthenaRuntime().openWorkspace(root).activateProject(
            projectName = "rolling-shutter",
            sourcePath = root.resolve("examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena"),
        )
        context.switchActiveProjectionView("schematic")
        val session = context.projectProjectionSession()
        val payload = session.toPayload("test")
        val ready = assertNotNull(payload.readyProjection)
        val spatial = assertNotNull(ready.spatialFacts)
        val sheet = spatial.sheets.single()
        val presentation = assertNotNull(ready.presentation)

        assertEquals("schematic", payload.activeViewId)
        assertEquals("schematic/sheet/S1", ready.activeSheetId)
        assertEquals(sheet.regions.map { region -> region.regionId }.toSet(), ready.projectionRegionIds.toSet())
        assertEquals(sheet.constructs.map { construct -> construct.constructId }.toSet(), ready.projectionConstructIds.toSet())
        assertEquals(1200, ready.canvasWidth)
        assertEquals(800, ready.canvasHeight)
        assertEquals(ready.canvasWidth, presentation.canvasWidth)
        assertEquals(ready.canvasHeight, presentation.canvasHeight)
        assertEquals(8, ready.components.size)
        assertEquals(9, ready.connections.size)
        assertEquals(8, spatial.sheets.single().occurrences.size)
        assertEquals(9, sheet.routes.size)
        assertEquals(sheet.occurrences.size, presentation.occurrences.size)
        assertEquals(sheet.routes.size, presentation.connectors.size)
        assertTrue(presentation.connectors.zip(sheet.routes).all { (connector, route) ->
            connector.routeId == route.routeId && connector.routePoints.map { point -> point.x to point.y } ==
                route.points.map { point -> point.x to point.y }
        })
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts")))
        return current
    }
}
