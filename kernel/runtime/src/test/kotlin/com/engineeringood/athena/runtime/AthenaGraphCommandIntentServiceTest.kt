package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaGraphCommandIntentServiceTest {
    @Test
    fun `interactive cabinet placement intent is rejected until spatial reality owns placement`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("cabinet"))
        val baselineDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document
        val baselineSession = context.projectProjectionSession()
        val baselineReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(baselineSession.activeProjection)
        assertEquals(emptyList(), baselineReady.scene.components)

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaAdjustLayoutPlacementIntent(
                viewId = "cabinet",
                target = AthenaGraphCommandTarget(
                    semanticId = "component:PLC1",
                    subjectKind = AthenaGraphCommandSubjectKind.COMPONENT,
                ),
                requestedPlacement = AthenaGraphPlacement(
                    x = 180,
                    y = 120,
                ),
            ),
        )

        val rejected = assertIs<AthenaGraphCommandIntentRejected>(result)
        assertEquals(AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT, rejected.intentId)
        assertEquals(AthenaMutationCategory.PROJECTION_MUTATION, rejected.mutationCategory)
        assertContains(rejected.reason, "component:PLC1")
        assertSame(baselineSession, context.projectProjectionSession())

        val afterDocument = assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document
        assertEquals(baselineDocument, afterDocument)
    }

    @Test
    fun `inspect only views reject projection placement intent explicitly`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        context.switchActiveProjectionView("wiring")

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaAdjustLayoutPlacementIntent(
                viewId = "wiring",
                target = AthenaGraphCommandTarget(
                    semanticId = "component:PLC1",
                    subjectKind = AthenaGraphCommandSubjectKind.COMPONENT,
                ),
                requestedPlacement = AthenaGraphPlacement(
                    x = 180,
                    y = 120,
                ),
            ),
        )

        val rejected = assertIs<AthenaGraphCommandIntentRejected>(result)
        assertEquals(AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT, rejected.intentId)
        assertEquals(AthenaMutationCategory.PROJECTION_MUTATION, rejected.mutationCategory)
        assertContains(rejected.reason, "wiring")
        assertContains(rejected.reason, "inspect", ignoreCase = true)
    }

    @Test
    fun `missing placement targets are rejected without changing projection state`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("cabinet"))
        val baselineSession = context.projectProjectionSession()

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaAdjustLayoutPlacementIntent(
                viewId = "cabinet",
                target = AthenaGraphCommandTarget(
                    semanticId = "component:missing",
                    subjectKind = AthenaGraphCommandSubjectKind.COMPONENT,
                ),
                requestedPlacement = AthenaGraphPlacement(
                    x = 180,
                    y = 120,
                ),
            ),
        )

        val rejected = assertIs<AthenaGraphCommandIntentRejected>(result)
        assertContains(rejected.reason, "component:missing")
        assertSame(baselineSession, context.projectProjectionSession())
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
