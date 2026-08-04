package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSpatialDocuments
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaRuntimeM41SpatialProofTest {
    @Test
    fun `M41 schematic session retains one compiler-backed Spatial proof and scene geometry`() {
        val root = resolveRepoRoot()
        val context = AthenaRuntime().openWorkspace(root).activateProject(
            projectName = "rolling-shutter",
            sourcePath = root.resolve("examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena"),
        )

        context.switchActiveProjectionView("schematic")
        val session = context.projectProjectionSession()
        val ready = session.activeProjection as AthenaRuntimeProjectionReadySnapshot
        val spatial = assertNotNull(ready.spatialFacts)
        val sheet = spatial.sheets.single()

        assertEquals("schematic", spatial.viewId)
        assertEquals("schematic/sheet/S1", spatial.activeSheetId)
        assertEquals(sheet.regions.map { region -> region.regionId }.toSet(), ready.projectionRegionIds.toSet())
        assertEquals(sheet.constructs.map { construct -> construct.constructId }.toSet(), ready.projectionConstructIds.toSet())
        assertEquals(8, sheet.occurrences.size)
        assertEquals(9, sheet.routes.size)
        assertEquals(16, sheet.anchors.size)
        assertEquals(7, sheet.lanes.size)
        assertEquals(15, sheet.gridReferences.size)
        assertEquals(0, sheet.quality.occurrenceOverlapCount)
        assertEquals(0, sheet.quality.constructContainmentFailureCount)
        assertEquals(0, sheet.quality.routeBodyIntersectionCount)
        assertEquals(0, sheet.quality.twistCount)
        assertEquals(7, sheet.quality.usedLaneCount)
        assertEquals(2, sheet.quality.peakRoutesPerLane)
        assertTrue(ready.scene.components.isNotEmpty())
        assertEquals(sheet.occurrences.size, ready.scene.components.size)
        assertEquals(sheet.routes.size, ready.scene.connections.size)
    }

    @Test
    fun `M41 schematic session fails closed when retained Spatial authority is missing`() {
        val root = resolveRepoRoot()
        val context = AthenaRuntime().openWorkspace(root).activateProject(
            projectName = "rolling-shutter",
            sourcePath = root.resolve("examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena"),
        )
        context.switchActiveProjectionView("schematic")
        val compilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())

        val session = context.previewProjectionSession(
            compilation.copy(spatialDocuments = CompilerSpatialDocuments.empty()),
        )

        val unavailable = assertIs<AthenaRuntimeProjectionUnavailableSnapshot>(session.activeProjection)
        assertEquals("spatial.missing", unavailable.diagnostics.single().code)
        assertTrue(unavailable.reason.contains("Spatial Reality"))
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts")))
        return current
    }
}
