package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.layout.ProjectionInteractivity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRuntimeProjectionSessionTest {
    @Test
    fun `runtime hosts supported projection views with deterministic default active view`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val session = context.projectProjectionSession()
        val cabinetView = session.supportedViews.first { view -> view.viewId == "cabinet" }
        val wiringView = session.supportedViews.first { view -> view.viewId == "wiring" }

        assertEquals("demo-cabinet", session.projectName)
        assertEquals(listOf("cabinet", "wiring"), session.supportedViews.map { view -> view.viewId })
        assertEquals("cabinet", session.activeViewId)
        assertEquals(ProjectionInteractivity.INTERACTIVE, cabinetView.ownershipContract.interactivity)
        assertEquals(
            listOf("adjust-layout-placement", "adjust-layout-grouping"),
            cabinetView.ownershipContract.projectionCommandIds,
        )
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            cabinetView.ownershipContract.transientInteractionKinds,
        )
        assertEquals(
            listOf("layout-placement", "layout-group-membership"),
            cabinetView.ownershipContract.persistedProjectionMetadataKeys,
        )
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, wiringView.ownershipContract.interactivity)
        assertTrue(wiringView.ownershipContract.projectionCommandIds.isEmpty())
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            wiringView.ownershipContract.transientInteractionKinds,
        )
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(session.activeProjection)
        assertEquals("cabinet", ready.viewId)
        assertEquals("DemoCabinet", ready.scene.systemName)
        assertEquals(480, ready.scene.canvasWidth)
        assertEquals(172, ready.scene.canvasHeight)
        assertEquals(listOf("electrical-runtime.render.cabinet"), ready.activeRenderContributions.map { contribution -> contribution.contributionId })
        assertEquals(2, ready.scene.components.size)
        assertEquals(1, ready.scene.connections.size)
        assertEquals(2, ready.scene.labels.size)
    }

    @Test
    fun `switching active view stays runtime owned and preserves canonical semantic state`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document

        val switchResult = context.switchActiveProjectionView("wiring")

        val success = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
        assertEquals("demo-cabinet", success.projectName)
        assertEquals("wiring", success.requestedViewId)
        assertEquals("wiring", success.session.activeViewId)
        val ready = assertIs<AthenaRuntimeProjectionReadySnapshot>(success.session.activeProjection)
        assertEquals("wiring", ready.viewId)
        assertEquals(490, ready.scene.canvasWidth)
        assertEquals(244, ready.scene.canvasHeight)
        assertEquals(listOf("electrical-runtime.render.wiring"), ready.activeRenderContributions.map { contribution -> contribution.contributionId })
        assertEquals(2, ready.scene.components.size)
        assertEquals(1, ready.scene.connections.size)
        assertEquals(2, ready.scene.labels.size)

        val afterDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document
        assertEquals(baselineDocument, afterDocument)

        val legacyProjection = assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection())
        assertEquals(490, legacyProjection.scene.canvasWidth)
        assertEquals(244, legacyProjection.scene.canvasHeight)
    }

    @Test
    fun `projection session cache stays stable until runtime invalidates it`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

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
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
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
        assertEquals(1, assertIs<AthenaRuntimeProjectionReadySnapshot>(baselineSession.activeProjection).scene.connections.size)
    }

    @Test
    fun `unsupported active view ids are rejected explicitly`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val switchResult = context.switchActiveProjectionView("missing")

        val rejected = assertIs<AthenaRuntimeProjectionSwitchRejected>(switchResult)
        assertEquals("demo-cabinet", rejected.projectName)
        assertEquals("missing", rejected.requestedViewId)
        assertEquals(listOf("cabinet", "wiring"), rejected.supportedViewIds)
        assertContains(rejected.reason, "missing")
        assertEquals("cabinet", context.projectProjectionSession().activeViewId)
    }

    @Test
    fun `projection session supported views stay aligned with typed hosted plugin view definitions`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )

        val sessionViewIds = context.projectProjectionSession().supportedViews.map { view -> view.viewId }
        val hostedViewIds = runtime.serviceRegistry.pluginRuntimeServices().viewDefinitionContributions()
            .flatMap { contribution -> contribution.viewDefinitions }
            .map { definition -> definition.id }

        assertTrue(hostedViewIds.isNotEmpty())
        assertEquals(hostedViewIds, sessionViewIds)
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
