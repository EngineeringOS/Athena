package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.layout.ProjectionInteractivity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRuntimeProjectionSessionTest {
    @Test
    fun `runtime hosts supported projection views without projection owned geometry`() {
        val context = demoContext()

        val session = context.projectProjectionSession()
        val cabinetView = session.supportedViews.first { view -> view.viewId == "cabinet" }
        val wiringView = session.supportedViews.first { view -> view.viewId == "wiring" }
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(session.activeProjection)

        assertEquals("demo-cabinet", session.projectName)
        assertEquals(listOf("cabinet", "wiring", "schematic", "documentation"), session.supportedViews.map { view -> view.viewId })
        assertEquals("cabinet", session.activeViewId)
        assertEquals("electrical/cabinet", cabinetView.familyId)
        assertEquals("electrical/wiring", wiringView.familyId)
        assertEquals(ProjectionInteractivity.INTERACTIVE, cabinetView.ownershipContract.interactivity)
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, wiringView.ownershipContract.interactivity)
        assertEquals("cabinet", ready.viewId)
        assertEquals("electrical/cabinet", ready.familyId)
        assertEquals("DemoCabinet", ready.scene.systemName)
        assertEquals(1, ready.scene.canvasWidth)
        assertEquals(1, ready.scene.canvasHeight)
        assertEquals(emptyList(), ready.scene.components)
        assertEquals(emptyList(), ready.scene.connections)
        assertEquals(emptyList(), ready.scene.labels)
        assertEquals("cabinet/sheet/01-main", ready.activeSheetId)
        assertEquals(listOf("cabinet/sheet/01-main"), ready.sheets.map { sheet -> sheet.sheetId })
        val presentation = assertNotNull(ready.presentation)
        assertTrue(presentation.occurrences.isEmpty())
        assertTrue(presentation.connectors.isEmpty())
    }

    @Test
    fun `switching active view stays runtime owned and preserves canonical semantic state`() {
        val context = demoContext()
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document

        val switchResult = context.switchActiveProjectionView("wiring")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("wiring", success.requestedViewId)
        assertEquals("wiring", success.session.activeViewId)
        assertEquals("wiring", ready.viewId)
        assertEquals("electrical/wiring", ready.familyId)
        assertEquals(1, ready.scene.canvasWidth)
        assertEquals(1, ready.scene.canvasHeight)
        assertEquals(emptyList(), ready.scene.components)
        assertEquals(emptyList(), ready.scene.connections)
        assertEquals(baselineDocument, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document)
    }

    @Test
    fun `documentation sheet switch changes active sheet without sheet layout geometry`() {
        val context = demoContext()
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document

        val switchResult = context.switchActiveProjectionView("documentation/sheet/02-field-device")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("documentation/sheet/02-field-device", success.requestedViewId)
        assertEquals("documentation", success.session.activeViewId)
        assertEquals("documentation", ready.viewId)
        assertEquals("documentation/sheet/02-field-device", ready.activeSheetId)
        assertEquals(
            listOf("documentation/sheet/01-control", "documentation/sheet/02-field-device"),
            ready.sheets.map { sheet -> sheet.sheetId },
        )
        assertEquals(baselineDocument, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document)
    }

    @Test
    fun `projection session cache stays stable until runtime invalidates it`() {
        val context = demoContext()

        val firstSession = context.projectProjectionSession()
        val secondSession = context.projectProjectionSession()

        assertSame(firstSession, secondSession)
        val switchResult = assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("wiring"))
        assertNotSame(firstSession, switchResult.session)
        assertSame(switchResult.session, context.projectProjectionSession())
        assertEquals("wiring", switchResult.session.activeViewId)
    }

    @Test
    fun `projection preview can follow in memory compilation without mutating runtime owned cache`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val context = demoContext()
        val baselineSession = context.projectProjectionSession()
        val editedCompilation = context.compiler().compile(
            sourcePath,
            """
                system DemoCabinet {
                  device PLC1 {
                    type Switch
                    model "S7-1200"
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        val previewSession = context.previewProjectionSession(editedCompilation)

        assertEquals(0, assertIs<AthenaRuntimeProjectionReadySnapshot>(previewSession.activeProjection).scene.connections.size)
        assertSame(baselineSession, context.projectProjectionSession())
        assertEquals(0, assertIs<AthenaRuntimeProjectionReadySnapshot>(baselineSession.activeProjection).scene.connections.size)
    }

    @Test
    fun `unsupported active view ids are rejected explicitly`() {
        val context = demoContext()

        val switchResult = context.switchActiveProjectionView("missing")

        val rejected = assertIs<AthenaRuntimeProjectionSwitchRejected>(switchResult)
        assertEquals("demo-cabinet", rejected.projectName)
        assertEquals("missing", rejected.requestedViewId)
        assertEquals(listOf("cabinet", "wiring", "schematic", "documentation"), rejected.supportedViewIds)
        assertContains(rejected.reason, "missing")
        assertEquals("cabinet", context.projectProjectionSession().activeViewId)
    }

    private fun demoContext(): AthenaExecutionContext {
        return AthenaRuntime().openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
