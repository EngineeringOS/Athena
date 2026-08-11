package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.Range

/**
 * Verifies that Athena LSP exposes a read-only semantic inspection snapshot for the latest tracked document state.
 */
class AthenaSemanticInspectionTest {
    @Test
    fun `semantic inspection follows latest tracked document state`() {
        val repository = createGovernedTestRepository("athena-lsp-inspection-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val invalidText = """
            system FactoryLine {
              control Motor1.out to Missing.in
            }
        """.trimIndent()
        val validText = """
            system FactoryLine {
              entity Motor1 {
                concept Motor
              }

              entity Missing {
                concept Motor
              }

              port Motor1.out {
                direction out
                flow Digital
              }

              port Missing.in {
                direction in
                flow Digital
              }

              control Motor1.out to Missing.in
            }
        """.trimIndent()

        val server = AthenaLanguageServer()
        try {
            server.initialize(workspaceInitializeParams(repositoryRoot)).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        invalidText,
                    ),
                ),
            )

            val invalidInspection = server.semanticInspection(
                AthenaSemanticInspectionParams(
                    AthenaSemanticInspectionTextDocument(documentUri),
                ),
            ).get()
            assertEquals("diagnostics", invalidInspection?.status)
            assertEquals(1, invalidInspection?.version)
            assertEquals(2, invalidInspection?.diagnosticsCount)
            assertEquals(0, invalidInspection?.entityCount)
            assertEquals(1, invalidInspection?.relationshipCount)

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 2),
                    listOf(TextDocumentContentChangeEvent(validText)),
                ),
            )

            val validInspection = server.semanticInspection(
                AthenaSemanticInspectionParams(
                    AthenaSemanticInspectionTextDocument(documentUri),
                ),
            ).get()
            assertNotNull(validInspection)
            assertEquals("ready", validInspection.status)
            assertEquals(2, validInspection.version)
            assertEquals("FactoryLine", validInspection.systemName)
            assertEquals(0, validInspection.diagnosticsCount)
            assertEquals(2, validInspection.entityCount)
            assertEquals(2, validInspection.portCount)
            assertEquals(1, validInspection.relationshipCount)
            assertEquals(listOf("Missing", "Motor1"), validInspection.entities.map { entity -> entity.name })
            assertTrue(validInspection.ports.any { port -> port.path == "Motor1.out" })
            assertEquals("control", validInspection.relationships.single().definition)
            assertEquals(
                listOf("participant-1" to "Motor1.out", "participant-2" to "Missing.in"),
                validInspection.relationships.single().participants.map { participant ->
                    participant.role to participant.subjectPath
                },
            )
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(5, 2),
                    org.eclipse.lsp4j.Position(7, 3),
                ),
                validInspection.entities.first { entity -> entity.name == "Missing" }.sourceRange,
            )
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(9, 2),
                    org.eclipse.lsp4j.Position(12, 3),
                ),
                validInspection.ports.first { port -> port.path == "Motor1.out" }.sourceRange,
            )
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(19, 2),
                    org.eclipse.lsp4j.Position(19, 34),
                ),
                validInspection.relationships.single().sourceRange,
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `semantic inspection resolves source ranges for grouped interface ports`() {
        val repository = createGovernedTestRepository("athena-lsp-interface-port-inspection-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system InterfacePortInspection {
              entity TerminalX37 {
                concept Terminal
                port motorDownOut { direction out flow Power role switched terminal "3O" }
              }

              entity MotorM37 {
                concept Motor
                interface powerInput {
                  ports {
                    down { direction in flow Power role switched terminal "D" }
                  }
                }
              }

              power TerminalX37.motorDownOut to MotorM37.down
            }
        """.trimIndent()

        val server = AthenaLanguageServer()
        try {
            server.initialize(workspaceInitializeParams(repositoryRoot)).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val inspection = server.semanticInspection(
                AthenaSemanticInspectionParams(
                    AthenaSemanticInspectionTextDocument(documentUri),
                ),
            ).get()

            assertNotNull(inspection)
            assertEquals("ready", inspection.status)
            assertEquals(2, inspection.portCount)
            val interfacePort = inspection.ports.single { port -> port.path == "MotorM37.down" }
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(10, 8),
                    org.eclipse.lsp4j.Position(10, 67),
                ),
                interfacePort.sourceRange,
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

}
