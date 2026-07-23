package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.compiler.CompilerIncrementalPassMode
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.geometry.GeometryElementKind
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
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
                    type Switch
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
            val beforeCabinetLayout = before.layouts.first { layout -> layout.view.id == "cabinet" }
            val beforeWiringLayout = before.layouts.first { layout -> layout.view.id == "wiring" }
            val beforeCabinetComponentNode = beforeCabinetLayout.nodes.first { node ->
                node.semanticId.value == "component:PLC1"
            }
            val beforeWiringPortNode = beforeWiringLayout.nodes.first { node ->
                node.semanticId.value == "port:PLC1.out"
            }
            val beforeCabinetGeometry = before.geometries.first { geometry -> geometry.viewId == "cabinet" }
            val beforeWiringGeometry = before.geometries.first { geometry -> geometry.viewId == "wiring" }
            val beforeCabinetComponentBox = beforeCabinetGeometry.elements.first { element ->
                element.semanticId.value == "component:PLC1" && element.kind == GeometryElementKind.BOX
            }
            val beforeWiringPortLabel = beforeWiringGeometry.elements.first { element ->
                element.semanticId.value == "port:PLC1.out" && element.kind == GeometryElementKind.LABEL
            }
            val baselineProjectionSession = context.projectProjectionSession()

            val result = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = "port:PLC1.out",
                    targetPortSemanticId = "port:M1.in",
                ),
            )

            val success = assertIs<AthenaCommandExecutionSuccess>(result)
            assertEquals("connectable", success.projectName)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, success.mutationCategory)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, success.commandKind)
            assertEquals(AthenaMutationOutcome.ACCEPTED, success.outcome)
            assertTrue(success.validationFeedback.isEmpty())
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
            assertEquals(CompilerIncrementalPassMode.SCOPED, incrementalReport.layoutMode)
            assertEquals(
                listOf("cabinet", "documentation", "schematic", "wiring"),
                incrementalReport.layoutScopedViewIds.sorted(),
            )
            assertEquals(CompilerIncrementalPassMode.SCOPED, incrementalReport.geometryMode)
            assertEquals(
                listOf("cabinet", "documentation", "schematic", "wiring"),
                incrementalReport.geometryScopedViewIds.sorted(),
            )
            assertEquals(CompilerIncrementalPassMode.SCOPED, incrementalReport.renderingMode)
            assertEquals(listOf("cabinet"), incrementalReport.renderingViewIds)
            assertContains(
                updatedCompilation.pipeline.passes.first { it.pass.id == com.engineeringood.athena.compiler.CompilerPassId.VALIDATE }.outputSummary,
                "scoped",
            )
            assertContains(
                updatedCompilation.pipeline.passes.first { it.pass.id == com.engineeringood.athena.compiler.CompilerPassId.BACKEND_PREPARATION }.outputSummary,
                "scoped",
            )
            assertContains(
                updatedCompilation.pipeline.passes.first { it.pass.id == com.engineeringood.athena.compiler.CompilerPassId.BACKEND_EMISSION }.outputSummary,
                "scoped",
            )
            val updatedCabinetLayout = updatedCompilation.layouts.first { layout -> layout.view.id == "cabinet" }
            val updatedWiringLayout = updatedCompilation.layouts.first { layout -> layout.view.id == "wiring" }
            assertSame(
                beforeCabinetComponentNode,
                updatedCabinetLayout.nodes.first { node -> node.semanticId.value == "component:PLC1" },
            )
            assertSame(
                beforeWiringPortNode,
                updatedWiringLayout.nodes.first { node -> node.semanticId.value == "port:PLC1.out" },
            )
            assertTrue(
                updatedWiringLayout.groups.first { group -> group.label == "Digital" }.semanticIds
                    .any { semanticId -> semanticId.value == "connection:PLC1.out->M1.in" },
            )

            val updatedCabinetGeometry = updatedCompilation.geometries.first { geometry -> geometry.viewId == "cabinet" }
            val updatedWiringGeometry = updatedCompilation.geometries.first { geometry -> geometry.viewId == "wiring" }
            assertSame(
                beforeCabinetComponentBox,
                updatedCabinetGeometry.elements.first { element ->
                    element.semanticId.value == "component:PLC1" && element.kind == GeometryElementKind.BOX
                },
            )
            assertSame(
                beforeWiringPortLabel,
                updatedWiringGeometry.elements.first { element ->
                    element.semanticId.value == "port:PLC1.out" && element.kind == GeometryElementKind.LABEL
                },
            )
            assertTrue(
                updatedCabinetGeometry.elements.any { element ->
                    element.semanticId.value == "connection:PLC1.out->M1.in" && element.kind == GeometryElementKind.PATH
                },
            )

            val graphProjection = assertIs<AthenaEngineeringGraphReadyProjection>(context.projectEngineeringGraphProjection())
            assertEquals(
                listOf("port:M1.in", "port:PLC1.out"),
                graphProjection.graph.referencedNodes("connection:PLC1.out->M1.in").map { it.semanticId }.sorted(),
            )

            val viewerProjection = assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection())
            assertEquals(1, viewerProjection.scene.connections.size)
            val projectionSession = context.projectProjectionSession()
            assertNotSame(baselineProjectionSession, projectionSession)
            val cabinetProjection = assertIs<AthenaRuntimeProjectionReadySnapshot>(projectionSession.activeProjection)
            assertEquals(1, cabinetProjection.scene.connections.size)
            val switchResult = context.switchActiveProjectionView("wiring")
            val switchSuccess = assertIs<AthenaRuntimeProjectionSwitchSuccess>(switchResult)
            val wiringProjection = assertIs<AthenaRuntimeProjectionReadySnapshot>(switchSuccess.session.activeProjection)
            assertEquals(1, wiringProjection.scene.connections.size)
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
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, unavailable.mutationCategory)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, unavailable.commandKind)
            assertEquals(AthenaMutationOutcome.UNAVAILABLE, unavailable.outcome)
            assertTrue(unavailable.validationFeedback.isEmpty())
            assertContains(unavailable.reason, "missing '->'")
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
                    type Switch
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
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, rejected.mutationCategory)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, rejected.commandKind)
            assertEquals(AthenaMutationOutcome.REJECTED, rejected.outcome)
            assertContains(rejected.reason, "port:Missing.in")
            assertTrue(rejected.changedSemanticIds.isEmpty())
            assertTrue(rejected.validationFeedback.isEmpty())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `command runtime rejects semantically incompatible port connections when active component knowledge is available`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
                    vendorPartNumber "proof.cpu.313c"
                  }

                  port PLC1.lplus {
                    direction out
                    signal Digital
                  }

                  port PLC1.mpi {
                    direction inout
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
                    sourcePortSemanticId = "port:PLC1.lplus",
                    targetPortSemanticId = "port:PLC1.mpi",
                ),
            )

            val rejected = assertIs<AthenaCommandExecutionRejected>(result)
            assertEquals("connectable", rejected.projectName)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, rejected.mutationCategory)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, rejected.commandKind)
            assertEquals(AthenaMutationOutcome.REJECTED, rejected.outcome)
            assertContains(rejected.reason, "signal families differ")
            assertTrue(rejected.changedSemanticIds.isEmpty())
            assertTrue(rejected.validationFeedback.isEmpty())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `runtime-owned mutation contracts publish explicit category and validation feedback vocabulary`() {
        val feedback = AthenaMutationValidationFeedback(
            code = "validation.connection.missing-target",
            message = "Target port must be selected before Athena can connect ports.",
            severity = AthenaMutationValidationFeedbackSeverity.ERROR,
            relatedSemanticIds = listOf("port:PLC1.out"),
        )

        val command = AthenaConnectPortsCommand(
            sourcePortSemanticId = "port:PLC1.out",
            targetPortSemanticId = "port:M1.in",
        )
        val executionFeedback = AthenaCommandExecutionValidationFeedback(
            projectName = "contract-demo",
            commandKind = command.commandKind,
            commandOrigin = AthenaCommandOrigin.STANDARD,
            validationFeedback = listOf(feedback),
            changedSemanticIds = listOf("port:PLC1.out"),
        )
        val historyFeedback = AthenaCommandHistoryMutationValidationFeedback(
            projectName = "contract-demo",
            operation = AthenaCommandHistoryOperation.REPLAY,
            validationFeedback = listOf(feedback),
            affectedCommandIds = listOf("command-0001"),
        )

        assertEquals(
            listOf(
                AthenaMutationCategory.SEMANTIC_MUTATION,
                AthenaMutationCategory.PROJECTION_MUTATION,
                AthenaMutationCategory.TRANSIENT_INTERACTION,
            ),
            AthenaMutationCategory.entries,
        )
        assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, command.mutationCategory)
        assertEquals(AthenaMutationOutcome.VALIDATION_FEEDBACK, executionFeedback.outcome)
        assertEquals(listOf(feedback), executionFeedback.validationFeedback)
        assertEquals(listOf("port:PLC1.out"), executionFeedback.changedSemanticIds)
        assertEquals(AthenaMutationOutcome.VALIDATION_FEEDBACK, historyFeedback.outcome)
        assertEquals(listOf(feedback), historyFeedback.validationFeedback)
        assertEquals(listOf("command-0001"), historyFeedback.affectedCommandIds)
    }

    @Test
    fun `connect ports command publishes legacy runtime compatibility contract against semantic relationship intent`() {
        val command = AthenaConnectPortsCommand(
            sourcePortSemanticId = "port:PLC1.out",
            targetPortSemanticId = "port:M1.in",
        )

        val contract = command.compatibilityContract()

        assertEquals("legacy-connect-ports-runtime-command-v1", contract.contractId)
        assertEquals(AthenaCommandKind.CONNECT_PORTS, contract.retainedCommandKind)
        assertEquals(AthenaConnectPortsCommand::class.qualifiedName, contract.retainedRuntimeCommandClass)
        assertEquals(SemanticRelationshipIntent::class.qualifiedName, contract.productAuthoringIntentClass)
        assertEquals(false, contract.mutableSourceAuthority)
        assertEquals(
            setOf("cli", "desktop-compose", "domain-electrical-runtime"),
            contract.retainedSurfaces,
        )
        assertContains(contract.retainedSurfacePolicy, "runtime-owned")
        assertContains(contract.retainedSurfacePolicy, "not a product authoring contract")
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-command-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
