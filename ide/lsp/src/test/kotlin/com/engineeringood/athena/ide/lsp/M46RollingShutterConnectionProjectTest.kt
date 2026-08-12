package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class M46RollingShutterConnectionProjectTest {
    @Test
    fun `M46 repository opens READY with package-backed connection scene`() {
        val repository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        val host = AthenaLspSessionHost()
        try {
            val ready = assertIs<AthenaLspSessionHostReady>(host.activateRepository(repository))
            assertEquals(repository.toAbsolutePath().normalize(), ready.repositoryRoot)
            assertEquals(listOf("power", "control_cpu"), ready.folioPageNames())
            val publication = ready.diagramScenePublication()
            assertEquals(
                PublicationState.READY,
                publication.state,
                publication.diagnostics.joinToString { diagnostic ->
                    "${diagnostic.subject}: ${diagnostic.problem}"
                },
            )
            val scene = assertNotNull(publication.scene)
            assertEquals("power", scene.snapGrid.sheetId)
            assertEquals(9, scene.occurrences.size)
            assertTrue(scene.connections.isNotEmpty())
            assertTrue(
                scene.occurrences.flatMap { occurrence -> occurrence.ports }
                    .any { port -> port.semanticPortId == "port:X2.powerTerminal.spare" },
                "Unused package-backed Ports must publish selectable Scene anchors for connection authoring.",
            )
            assertEquals(17, scene.plotFrame.columns)
            assertEquals(16, scene.plotFrame.rows)
            assertEquals(0, scene.page.pageBounds.x)
            assertTrue(scene.decorations.none { it.kind.name == "COORDINATE_LABEL" && it.text?.contains("anchor:") == true })
            assertTrue(scene.decorations.none { it.text?.contains(".athena") == true })

            ready.executionContext.switchActiveProjectionView("control_cpu")
            val controlPublication = ready.diagramScenePublication()
            assertEquals(PublicationState.READY, controlPublication.state, controlPublication.diagnostics.joinToString { "${it.subject}: ${it.problem} -> ${it.correction}" })
            val controlScene = assertNotNull(controlPublication.scene)
            assertEquals("control_cpu", controlScene.snapGrid.sheetId)
            assertEquals(13, controlScene.occurrences.size)
            assertTrue(controlScene.connections.any { it.annotations.isNotEmpty() })
            val folioConnections = scene.connections + controlScene.connections
            val topologyEvidence = folioConnections.joinToString("\n") { connection ->
                "${connection.connectionId}: markers=${connection.markers.map { it.kind }} " +
                    "segments=${connection.segments.map { segment -> "${segment.kind}:${segment.start}->${segment.end}" }}"
            }
            assertTrue(folioConnections.any { connection -> connection.markers.any { it.kind.name == "JUNCTION" } }, topologyEvidence)
            assertTrue(folioConnections.any { connection -> connection.markers.any { it.kind.name == "INTERRUPTION_START" } }, topologyEvidence)
            assertTrue(folioConnections.any { connection -> connection.markers.any { it.kind.name == "CROSSING" } }, topologyEvidence)
            assertTrue(folioConnections.any { connection -> connection.segments.any { it.kind.name == "SHARED" } }, topologyEvidence)
        } finally {
            host.shutdown()
        }
    }

    @Test
    fun `active page route staging retains every folio sheet grid`() {
        val repository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        val host = AthenaLspSessionHost()
        try {
            val ready = assertIs<AthenaLspSessionHostReady>(host.activateRepository(repository))
            val projectionId = assertIs<CompilerCompilationSuccess>(
                ready.executionContext.compiler().compile(ready.sourcePath),
            ).projections.single().connections.single { connection ->
                connection.semanticId.value.endsWith("wire:Q1.protection.line->KM1.mainContact.power")
            }.projectionId.value
            val powerSheet = repository.resolve(
                "src/com/engineeringood/m46/rollingshutter/rolling-shutter.power.sheet.athena",
            )
            val override = assertIs<SheetCompanionParseSuccess>(
                AthenaSheetCompanionParser().parse(
                    powerSheet.toString(),
                    Files.readString(powerSheet).replace(
                        "  \"T1.primary\" at (36, 32)",
                        """
                        |  "T1.primary" at (36, 32)
                        |  route "$projectionId" via "segment:1" at (15, 14)
                        """.trimMargin(),
                    ),
                ),
            ).source

            val staged = assertIs<CompilerCompilationSuccess>(
                ready.executionContext.compiler().compile(ready.sourcePath, override),
            )

            assertEquals(
                listOf("power", "control_cpu"),
                staged.projections.single().sheets.map { sheet -> sheet.displayName },
            )
            assertTrue(staged.projections.single().sheets.all { sheet -> sheet.grid != null })
            assertTrue(
                staged.projections.single().connections.any { connection ->
                    connection.logicalRouteConstraints.any { constraint ->
                        constraint.targetId.value == "segment:1"
                    }
                },
                staged.projections.single().connections.joinToString("\n") { connection ->
                    "${connection.projectionId.value}: ${connection.semanticId.value} -> " +
                        connection.logicalRouteConstraints.joinToString { it.targetId.value }
                },
            )
        } finally {
            host.shutdown()
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
