package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerIncrementalPassMode
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaCommandRuntimeTest {
    @Test
    fun `active execution context resolves the shared command runtime service`() {
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(Path.of("examples")).activateProject(
            projectName = "demo-cabinet",
            sourcePath = Path.of("examples/m0/demo-cabinet.athena"),
        )

        assertSame(runtime.serviceRegistry.commandRuntime(), context.commandRuntime())
    }

    @Test
    fun `connect ports command mutates runtime-owned canonical state and keeps projections aligned`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type PLC
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

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )

            val before = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())
            assertTrue(before.document.connections.isEmpty())

            val result = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = "port:PLC1.out",
                    targetPortSemanticId = "port:M1.in",
                ),
            )

            val success = assertIs<AthenaCommandExecutionSuccess>(result)
            assertEquals("connectable", success.projectName)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, success.commandKind)
            assertTrue(success.beforeDocument.connections.isEmpty())
            assertEquals(
                listOf("connection:PLC1.out->M1.in", "port:M1.in", "port:PLC1.out"),
                success.changedSemanticIds.sorted(),
            )
            assertEquals(1, success.afterDocument.connections.size)

            val updatedCompilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())
            assertEquals(1, updatedCompilation.document.connections.size)
            assertEquals("connection:PLC1.out->M1.in", updatedCompilation.document.connections.single().id.value)
            val incrementalReport = requireNotNull(updatedCompilation.incrementalUpdateReport)
            assertEquals(
                listOf("connection:PLC1.out->M1.in", "port:M1.in", "port:PLC1.out"),
                incrementalReport.affectedScope.changedSemanticIds.sorted(),
            )
            assertEquals(
                listOf("component:M1", "component:PLC1", "connection:PLC1.out->M1.in", "port:M1.in", "port:PLC1.out"),
                incrementalReport.affectedScope.validationSemanticIds.sorted(),
            )
            assertEquals(
                listOf("component:M1", "component:PLC1"),
                incrementalReport.affectedScope.renderComponentSemanticIds.sorted(),
            )
            assertEquals(
                listOf("connection:PLC1.out->M1.in"),
                incrementalReport.affectedScope.renderConnectionSemanticIds.sorted(),
            )
            assertEquals(CompilerIncrementalPassMode.SCOPED, incrementalReport.validationMode)
            assertEquals(CompilerIncrementalPassMode.SCOPED, incrementalReport.renderingMode)
            assertContains(updatedCompilation.pipeline.passes[2].outputSummary, "scoped")
            assertContains(updatedCompilation.pipeline.passes[3].outputSummary, "scoped")

            val graphProjection = assertIs<AthenaEngineeringGraphReadyProjection>(context.projectEngineeringGraphProjection())
            assertEquals(
                listOf("port:M1.in", "port:PLC1.out"),
                graphProjection.graph.referencedNodes("connection:PLC1.out->M1.in").map { it.semanticId }.sorted(),
            )

            val viewerProjection = assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection())
            assertEquals(1, viewerProjection.scene.connections.size)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `command runtime rejects unavailable projects before any mutation happens`() {
        val brokenPath = writeProject(
            """
                system Broken {
                  connect P1.out P2.in
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val result = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = "port:P1.out",
                    targetPortSemanticId = "port:P2.in",
                ),
            )

            val unavailable = assertIs<AthenaCommandExecutionUnavailable>(result)
            assertEquals("broken", unavailable.projectName)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, unavailable.commandKind)
            assertContains(unavailable.reason, "Expected")
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    @Test
    fun `command runtime rejects invalid connect requests through explicit command results`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type PLC
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )

            val result = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = "port:PLC1.out",
                    targetPortSemanticId = "port:Missing.in",
                ),
            )

            val rejected = assertIs<AthenaCommandExecutionRejected>(result)
            assertEquals("connectable", rejected.projectName)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, rejected.commandKind)
            assertContains(rejected.reason, "port:Missing.in")
            assertTrue(rejected.changedSemanticIds.isEmpty())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-command-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
