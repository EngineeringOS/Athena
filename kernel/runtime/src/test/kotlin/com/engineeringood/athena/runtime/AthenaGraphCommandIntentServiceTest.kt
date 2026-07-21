package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
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

class AthenaGraphCommandIntentServiceTest {
    @Test
    fun `interactive cabinet placement intent updates runtime owned projection state and stays inspectable`() {
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
        val baselinePlc = baselineReady.scene.components.first { component -> component.semanticId == "component:PLC1" }

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

        val accepted = assertIs<AthenaGraphCommandIntentAccepted>(result)
        assertEquals("demo-cabinet", accepted.projectName)
        assertEquals(AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT, accepted.intentId)
        assertEquals(AthenaMutationCategory.PROJECTION_MUTATION, accepted.mutationCategory)
        assertEquals("cabinet", accepted.viewId)
        assertEquals("component:PLC1", accepted.target.semanticId)
        assertEquals(AthenaGraphCommandSubjectKind.COMPONENT, accepted.target.subjectKind)
        val placement = assertNotNull(accepted.requestedPlacement)
        assertEquals(180, placement.x)
        assertEquals(120, placement.y)

        val refreshedSession = context.projectProjectionSession()
        assertNotSame(baselineSession, refreshedSession)
        val refreshedReady = assertIs<AthenaRuntimeProjectionReadySnapshot>(refreshedSession.activeProjection)
        val refreshedPlc = refreshedReady.scene.components.first { component -> component.semanticId == "component:PLC1" }
        assertEquals(180, refreshedPlc.x)
        assertEquals(120, refreshedPlc.y)
        assertTrue(refreshedPlc.x != baselinePlc.x || refreshedPlc.y != baselinePlc.y)

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

    @Test
    fun `interactive cabinet semantic connect intent executes through the runtime command path`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m2/operator-proof.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("cabinet"))

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaConnectPortsIntent(
                viewId = "cabinet",
                source = AthenaGraphCommandTarget(
                    semanticId = "port:PLC1.out",
                    subjectKind = AthenaGraphCommandSubjectKind.PORT,
                ),
                target = AthenaGraphCommandTarget(
                    semanticId = "port:M1.in",
                    subjectKind = AthenaGraphCommandSubjectKind.PORT,
                ),
            ),
        )

        val accepted = assertIs<AthenaGraphCommandIntentAccepted>(result)
        assertEquals(AthenaGraphCommandIntentId.CONNECT_PORTS, accepted.intentId)
        assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, accepted.mutationCategory)
        assertEquals("port:PLC1.out", accepted.source?.semanticId)
        assertEquals("port:M1.in", accepted.target.semanticId)
        val execution = assertNotNull(accepted.execution)
        assertEquals(AthenaCommandKind.CONNECT_PORTS, execution.commandKind)
        assertEquals(AthenaMutationOutcome.ACCEPTED, execution.outcome)
        assertTrue(execution.changedSemanticIds.contains("connection:PLC1.out->M1.in"))

        val history = context.commandRuntime().history(context)
        assertEquals(1, history.appliedRecordCount)
        assertEquals(AthenaCommandKind.CONNECT_PORTS, history.records.single().commandKind)
    }

    @Test
    fun `inspect only views reject semantic connect intent explicitly`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        context.switchActiveProjectionView("wiring")

        val result = context.graphCommandIntentRuntime().submit(
            context = context,
            intent = AthenaConnectPortsIntent(
                viewId = "wiring",
                source = AthenaGraphCommandTarget(
                    semanticId = "port:PLC1.out",
                    subjectKind = AthenaGraphCommandSubjectKind.PORT,
                ),
                target = AthenaGraphCommandTarget(
                    semanticId = "port:M1.in",
                    subjectKind = AthenaGraphCommandSubjectKind.PORT,
                ),
            ),
        )

        val rejected = assertIs<AthenaGraphCommandIntentRejected>(result)
        assertEquals(AthenaGraphCommandIntentId.CONNECT_PORTS, rejected.intentId)
        assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, rejected.mutationCategory)
        assertContains(rejected.reason, "wiring")
        assertContains(rejected.reason, "inspect", ignoreCase = true)
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
