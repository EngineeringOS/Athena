package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AthenaSemanticDiffInspectionTest {
    @Test
    fun `latest inspection exposes added connection diff and linked command consequence`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            executeConnect(context, "port:PLC1.out1", "port:M1.in")

            val latestInspection = assertNotNull(context.latestSemanticDiffInspection())
            assertEquals(AthenaSemanticDiffInspectionSource.COMMAND, latestInspection.source)
            assertEquals(listOf("command-0001"), latestInspection.affectedCommandIds)
            assertEquals(
                listOf("connection:PLC1.out1->M1.in", "port:M1.in", "port:PLC1.out1"),
                latestInspection.affectedSemanticIds.sorted(),
            )

            val connectionEntry = latestInspection.entries.first { entry ->
                entry.semanticId == "connection:PLC1.out1->M1.in"
            }
            assertEquals(AthenaSemanticDiffChangeKind.ADDED, connectionEntry.changeKind)
            assertEquals(null, connectionEntry.beforeSummary)
            assertContains(connectionEntry.afterSummary.orEmpty(), "PLC1.out1")
            assertContains(connectionEntry.afterSummary.orEmpty(), "M1.in")
            assertEquals(
                listOf(
                    AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
                    AthenaProjectionRefreshConsequenceLayer.LAYOUT,
                    AthenaProjectionRefreshConsequenceLayer.RENDERING,
                ),
                latestInspection.projectionConsequences.map { consequence -> consequence.layer }.sortedBy { layer -> layer.name },
            )
            val latestLayoutConsequence = latestInspection.projectionConsequences.first { consequence ->
                consequence.layer == AthenaProjectionRefreshConsequenceLayer.LAYOUT
            }
            assertEquals("scoped", latestLayoutConsequence.mode)
            assertEquals(listOf("cabinet", "wiring"), latestLayoutConsequence.affectedViewIds)
            assertEquals(
                listOf("connection:PLC1.out1->M1.in", "port:M1.in", "port:PLC1.out1"),
                latestLayoutConsequence.affectedSemanticIds.sorted(),
            )
            val latestRenderingConsequence = latestInspection.projectionConsequences.first { consequence ->
                consequence.layer == AthenaProjectionRefreshConsequenceLayer.RENDERING
            }
            assertEquals("scoped", latestRenderingConsequence.mode)
            assertEquals(listOf("cabinet"), latestRenderingConsequence.affectedViewIds)

            val commandInspection = assertNotNull(
                context.commandRuntime().inspectCommandHistoryConsequence(
                    context = context,
                    commandId = "command-0001",
                ),
            )
            assertEquals(listOf("command-0001"), commandInspection.affectedCommandIds)
            assertEquals(AthenaCommandHistoryRecordStatus.APPLIED, commandInspection.historyConsequences.single().status)
            assertEquals(AthenaSemanticDiffChangeKind.ADDED, commandInspection.entries.first { entry ->
                entry.semanticId == "connection:PLC1.out1->M1.in"
            }.changeKind)
            assertTrue(commandInspection.projectionConsequences.isNotEmpty())
            assertTrue(commandInspection.projectionConsequences.all { consequence ->
                "connection:PLC1.out1->M1.in" in consequence.affectedSemanticIds
            })
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `undo and replay inspections expose resulting diff and updated command consequences`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            executeConnect(context, "port:PLC1.out1", "port:M1.in")
            executeConnect(context, "port:PLC1.out2", "port:M2.in")

            context.commandRuntime().undo(context)

            val latestAfterUndo = assertNotNull(context.latestSemanticDiffInspection())
            assertEquals(AthenaSemanticDiffInspectionSource.UNDO, latestAfterUndo.source)
            assertEquals(listOf("command-0002"), latestAfterUndo.affectedCommandIds)
            assertEquals(AthenaSemanticDiffChangeKind.REMOVED, latestAfterUndo.entries.first { entry ->
                entry.semanticId == "connection:PLC1.out2->M2.in"
            }.changeKind)
            assertEquals(
                listOf("cabinet", "wiring"),
                latestAfterUndo.projectionConsequences.first { consequence ->
                    consequence.layer == AthenaProjectionRefreshConsequenceLayer.LAYOUT
                }.affectedViewIds,
            )
            assertEquals(
                listOf("cabinet"),
                latestAfterUndo.projectionConsequences.first { consequence ->
                    consequence.layer == AthenaProjectionRefreshConsequenceLayer.RENDERING
                }.affectedViewIds,
            )

            val commandInspectionAfterUndo = assertNotNull(
                context.commandRuntime().inspectCommandHistoryConsequence(
                    context = context,
                    commandId = "command-0002",
                ),
            )
            assertEquals(AthenaCommandHistoryRecordStatus.UNDONE, commandInspectionAfterUndo.historyConsequences.single().status)
            assertEquals(AthenaSemanticDiffChangeKind.ADDED, commandInspectionAfterUndo.entries.first { entry ->
                entry.semanticId == "connection:PLC1.out2->M2.in"
            }.changeKind)
            assertTrue(commandInspectionAfterUndo.projectionConsequences.isNotEmpty())

            context.commandRuntime().replay(context)

            val latestAfterReplay = assertNotNull(context.latestSemanticDiffInspection())
            assertEquals(AthenaSemanticDiffInspectionSource.REPLAY, latestAfterReplay.source)
            assertEquals(listOf("command-0001", "command-0002"), latestAfterReplay.affectedCommandIds)
            assertEquals(
                listOf(AthenaCommandHistoryRecordStatus.APPLIED, AthenaCommandHistoryRecordStatus.APPLIED),
                latestAfterReplay.historyConsequences.map { consequence -> consequence.status },
            )
            assertTrue(latestAfterReplay.projectionConsequences.any { consequence ->
                consequence.layer == AthenaProjectionRefreshConsequenceLayer.GEOMETRY &&
                    consequence.affectedViewIds == listOf("cabinet", "wiring")
            })
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun executeConnect(
        context: AthenaExecutionContext,
        sourcePortSemanticId: String,
        targetPortSemanticId: String,
    ) {
        val result = context.commandRuntime().execute(
            context = context,
            command = AthenaConnectPortsCommand(
                sourcePortSemanticId = sourcePortSemanticId,
                targetPortSemanticId = targetPortSemanticId,
            ),
        )

        check(result is AthenaCommandExecutionSuccess)
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-diff-inspection-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun twoConnectionFixture(): String {
        return """
            system HistoryDemo {
              device PLC1 {
                type PLC
              }

              device M1 {
                type Motor
              }

              device M2 {
                type Motor
              }

              port PLC1.out1 {
                direction out
                signal Digital
              }

              port PLC1.out2 {
                direction out
                signal Digital
              }

              port M1.in {
                direction in
                signal Digital
              }

              port M2.in {
                direction in
                signal Digital
              }
            }
        """.trimIndent()
    }
}
