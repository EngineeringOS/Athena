package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaDiagramProtocolArchitectureTest {
    @Test
    fun `edit operation protocol replaces retired diagram commands without compatibility`() {
        val root = repositoryRoot()
        val server = Files.readString(root.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt"))
        assertTrue(server.contains("athena/diagramScene"))
        assertTrue(server.contains("athena/presentationEditContext"))
        assertTrue(server.contains("athena/applyEditOperation"))
        assertFalse(server.contains("athena/diagramStyleContext"))
        assertFalse(server.contains("athena/diagramConnectOptions"))
        assertFalse(server.contains("athena/applyDiagramCommand"))
        assertFalse(server.contains("athena/projectionSession"))
        assertFalse(Files.exists(root.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt")))
        assertFalse(Files.exists(root.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramCommandWireMapper.kt")))
        assertFalse(Files.exists(root.resolve("kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/DiagramCommandContracts.kt")))
        assertFalse(Files.exists(root.resolve("kernel/interaction-model/src/main/resources/schema/athena-diagram-command.schema.json")))
        assertFalse(Files.exists(root.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt")))
        assertFalse(Files.exists(root.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt")))
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
