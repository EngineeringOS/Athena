package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier

class AthenaM28ProductAuthoringSmokeTest {
    @Test
    @Suppress("DEPRECATION")
    fun `m28 sample product path accepts valid relationship and rejects invalid attempts without source mutation`() {
        val repositoryRoot = copyM28SampleProject()
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val sourcePath = repositoryRoot.resolve("src/01-relationship-authoring-source.athena")
            val documentUri = sourcePath.toUri().toString()
            val originalSource = Files.readString(sourcePath)
            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()
                assertEquals(sourcePath.normalize(), server.currentSessionSnapshot()?.sourcePath?.normalize())

                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            originalSource,
                        ),
                    ),
                )

                var currentSource = originalSource
                var currentVersion = 1

                val validSubmission = submitSemanticRelationship(
                    server = server,
                    intentId = "intent-m28-product-valid",
                    sourceSubjectId = "port:ControllerPLC1.spareDo",
                    targetSubjectId = "port:SpareTerminalXT99.in1",
                    documentUri = documentUri,
                )
                assertEquals(
                    listOf("port:ControllerPLC1.spareDo", "port:SpareTerminalXT99.in1"),
                    validSubmission.preview.changes.single().affectedSubjectIdentities,
                )
                val accepted = decide(
                    server = server,
                    submission = validSubmission,
                    decision = "accepted",
                )
                val sourceEdit = assertNotNull(accepted.sourceEdit)
                assertEquals(documentUri, sourceEdit.uri)
                assertTrue(sourceEdit.newText.contains("connect ControllerPLC1.spareDo -> SpareTerminalXT99.in1"))
                currentSource = applySourceEdit(currentSource, sourceEdit)
                currentVersion += 1
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, currentVersion)
                        contentChanges = listOf(TextDocumentContentChangeEvent(currentSource))
                    },
                )

                assertTrue(currentSource.contains("connect ControllerPLC1.spareDo -> SpareTerminalXT99.in1"))
                val projection = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
                val readyProjection = assertNotNull(projection.readyProjection)
                assertTrue(readyProjection.connections.any { connection ->
                    connection.semanticId == "connection:ControllerPLC1.spareDo->SpareTerminalXT99.in1"
                })
                assertTrue(readyProjection.electricalRoutingCorridors.any { corridor ->
                    corridor.connectionSemanticId == "connection:ControllerPLC1.spareDo->SpareTerminalXT99.in1"
                })

                val sourceBeforeInvalidAttempts = currentSource
                val invalidCases = listOf(
                    "output-output" to ("port:ControllerPLC1.spareDo" to "port:ControllerPLC1.do1"),
                    "input-input" to ("port:ControllerPLC1.power" to "port:OperatorHMI1.status"),
                )
                invalidCases.forEachIndexed { index, (caseName, subjects) ->
                    val invalidSubmission = submitSemanticRelationship(
                        server = server,
                        intentId = "intent-m28-product-invalid-$index",
                        sourceSubjectId = subjects.first,
                        targetSubjectId = subjects.second,
                        documentUri = documentUri,
                    )
                    assertFalse(
                        invalidSubmission.preview.changes.single().title.contains("ControllerPLC1.do1 -> OperatorHMI1.status"),
                        "$caseName must not infer relationship identity from visible DOM text.",
                    )
                    val rejected = decide(
                        server = server,
                        submission = invalidSubmission,
                        decision = "rejected",
                    )
                    assertEquals("updated", rejected.status)
                    assertEquals("rejected", rejected.preview?.status)
                    assertNull(rejected.sourceEdit, "$caseName rejection must not return a source edit.")

                    val acceptedInvalid = decide(
                        server = server,
                        submission = submitSemanticRelationship(
                            server = server,
                            intentId = "intent-m28-product-invalid-accept-$index",
                            sourceSubjectId = subjects.first,
                            targetSubjectId = subjects.second,
                            documentUri = documentUri,
                        ),
                        decision = "accepted",
                    )
                    assertNull(acceptedInvalid.sourceEdit, "$caseName accepted invalid attempt must still be blocked at the backend edit gate.")
                }
                assertEquals(sourceBeforeInvalidAttempts, currentSource)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun submitSemanticRelationship(
        server: AthenaLanguageServer,
        intentId: String,
        sourceSubjectId: String,
        targetSubjectId: String,
        documentUri: String,
    ): AthenaAuthoringPreviewSubmissionPayload {
        return assertNotNull(
            server.authoringPreview(
                AthenaAuthoringPreviewParams(
                    intentId = intentId,
                    intentKind = "semantic-relationship",
                    originSurface = "graph",
                    originDetail = "graph:documentation",
                    relationshipType = "ElectricalConnectionRelationship",
                    sourceSubjectId = sourceSubjectId,
                    targetSubjectId = targetSubjectId,
                    projectionViewId = "documentation",
                    persistenceSourceUri = documentUri,
                    provenance = "projection-fact-terminal",
                ),
            ).get(),
        )
    }

    private fun decide(
        server: AthenaLanguageServer,
        submission: AthenaAuthoringPreviewSubmissionPayload,
        decision: String,
    ): AthenaAuthoringPreviewDecisionPayload {
        return assertNotNull(
            server.authoringDecision(
                AthenaAuthoringDecisionParams(
                    previewId = submission.preview.previewId,
                    intentId = submission.preview.intentId,
                    decision = decision,
                    note = "M28 product smoke $decision.",
                ),
            ).get(),
        )
    }

    private fun copyM28SampleProject(): Path {
        val sourceRoot = resolveRepoRoot().resolve("examples/m28/sample-project")
        val targetRoot = createTempDirectory("athena-m28-product-authoring-smoke-")
        Files.walk(sourceRoot).use { stream ->
            stream.forEach { source ->
                val relative = sourceRoot.relativize(source)
                val target = targetRoot.resolve(relative)
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(source, target)
                }
            }
        }
        return targetRoot
    }

    private fun applySourceEdit(
        source: String,
        edit: AthenaAuthoringSourceEditPayload,
    ): String {
        val startOffset = source.offsetAt(edit.range.start.line, edit.range.start.character)
        val endOffset = source.offsetAt(edit.range.end.line, edit.range.end.character)
        return source.substring(0, startOffset) + edit.newText + source.substring(endOffset)
    }

    private fun String.offsetAt(line: Int, character: Int): Int {
        var currentLine = 0
        var offset = 0
        while (currentLine < line && offset < length) {
            if (this[offset] == '\n') {
                currentLine += 1
            }
            offset += 1
        }
        return (offset + character).coerceAtMost(length)
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
