package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.ApplyWorkspaceEditParams
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.services.LanguageClient

@Suppress("DEPRECATION")
class AthenaAuthoringRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `guided authoring preview request flows through LSP into runtime owned preview state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-preview-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-0001",
                            intentKind = "create-entity",
                            originSurface = "palette",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.plc.cpu.default",
                            conceptId = "electrical.plc.cpu",
                            actor = "user:test",
                            preferredImplementationId = "impl/electrical/plc-cpu/siemens-proof-cpu313c",
                            suggestedName = "PLC2",
                        ),
                    ).get(),
                )

                assertEquals("factory-line", payload.projectName)
                assertEquals("frontend -> LSP -> runtime/compiler", payload.semanticPath)
                assertEquals("submitted", payload.status)
                assertEquals("create-entity", payload.preview.intentKind)
                assertEquals("palette", payload.preview.originSurface)
                assertEquals("pending-review", payload.preview.status)
                assertEquals(
                    listOf("component:PLC2", "system:FactoryLine"),
                    payload.preview.changes.single().affectedSubjectIdentities,
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `graph create preview activates canonical source without didOpen`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-create-without-editor-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            val client = RejectingWorkspaceEditClient()
            try {
                server.connect(client)
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-graph-create-no-editor",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "GraphMotorM32",
                        ),
                    ).get(),
                )

                assertTrue(submission.preview.acceptanceEligible, submission.preview.diagnostics.toString())
                val evidence = assertNotNull(submission.preview.entityCreationEvidence)
                assertEquals(repository.seedSourcePath.toUri().toString(), evidence.sourceEdit.uri)
                assertEquals("GraphMotorM32", evidence.canonicalTag)
                assertTrue(evidence.sourceEdit.admittedText.contains("device GraphMotorM32"))
                assertEquals(listOf("up", "down", "status"), evidence.nestedPorts.map { port -> port.name })

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Graph-first create without an open editor.",
                        ),
                    ).get(),
                )
                assertEquals("reprojected", assertNotNull(decision.transactionResult).lifecycleState)
                assertTrue(assertNotNull(decision.sourceEdit).appliedByAuthority)
                assertTrue(Files.readString(repository.seedSourcePath).contains("device GraphMotorM32"))
                assertTrue(assertNotNull(server.trackedDocument(evidence.sourceEdit.uri)).text.contains("device GraphMotorM32"))
                assertTrue(client.applyEditRequests.isEmpty(), "Unopened canonical source must not use editor workspace/applyEdit.")

                val projection = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
                val readyProjection = assertNotNull(projection.readyProjection)
                assertTrue(readyProjection.components.any { component ->
                    component.semanticId == "component:GraphMotorM32"
                })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `graph create rejects unopened source changed on disk after preview`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-create-stale-disk-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-graph-create-stale-disk",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "GraphMotorM32",
                        ),
                    ).get(),
                )
                assertTrue(submission.preview.acceptanceEligible, submission.preview.diagnostics.toString())

                val externallyChangedSource = authoringSource.replace("device M1", "device M2")
                Files.writeString(repository.seedSourcePath, externallyChangedSource)

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                        ),
                    ).get(),
                )

                assertEquals("unavailable", decision.status)
                assertEquals("blocked", decision.transactionResult?.lifecycleState)
                assertEquals("mutation-authority", decision.transactionResult?.diagnostics?.single()?.authority)
                assertNull(decision.sourceEdit)
                assertEquals(externallyChangedSource, Files.readString(repository.seedSourcePath))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `graph create returns governed rejection when canonical source is unreadable`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-create-unreadable-source-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()
                Files.delete(repository.seedSourcePath)

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-graph-create-unreadable-source",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "GraphMotorM32",
                        ),
                    ).get(),
                )

                assertEquals("blocked", submission.preview.status)
                assertFalse(submission.preview.acceptanceEligible)
                assertTrue(submission.preview.diagnostics.any { diagnostic ->
                    diagnostic.message.contains("canonical source", ignoreCase = true)
                })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `guided authoring state and decisions stay inspectable through LSP`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-state-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-0002",
                            intentKind = "update-entity-properties",
                            originSurface = "inspector",
                            entitySubjectId = "component:PLC1",
                            actor = "user:test",
                            properties = mapOf(
                                "vendorPart" to AthenaAuthoringValuePayload(
                                    kind = "symbol",
                                    text = "impl/electrical/plc-cpu/siemens-proof-cpu314c",
                                ),
                                "description" to AthenaAuthoringValuePayload(
                                    kind = "text",
                                    text = "Line controller",
                                ),
                            ),
                        ),
                    ).get(),
                )

                val initialState = assertNotNull(
                    server.authoringState(AthenaAuthoringStateParams()).get(),
                )
                assertEquals("ready", initialState.status)
                assertEquals(1, initialState.pendingPreviewCount)
                assertEquals(1, initialState.previews.size)
                assertEquals("update-entity-properties", initialState.previews.single().intentKind)

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Review complete.",
                        ),
                    ).get(),
                )
                assertEquals("updated", decision.status)
                assertEquals("accepted", decision.preview?.status)

                val updatedState = assertNotNull(
                    server.authoringState(AthenaAuthoringStateParams()).get(),
                )
                assertEquals(0, updatedState.pendingPreviewCount)
                assertEquals("accepted", updatedState.previews.single().status)
                assertTrue(updatedState.previews.single().warnings.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `accepted create-entity preview returns source edit and graph-source state can rebuild through tracked text`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-insert-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-0003",
                            intentKind = "create-entity",
                            originSurface = "palette",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "ShutterMotorM31",
                        ),
                    ).get(),
                )
                val previewSourceImpact = assertNotNull(
                    submission.preview.sourceImpact,
                    "Governed preview diagnostics: ${submission.preview.diagnostics}",
                )
                assertEquals(documentUri, previewSourceImpact.uri)
                assertEquals("component:ShutterMotorM31", previewSourceImpact.suggestedSemanticId)
                assertTrue(previewSourceImpact.newText.contains("device ShutterMotorM31"))
                assertTrue(previewSourceImpact.newText.contains("    port up {"))
                assertTrue(previewSourceImpact.newText.contains("    port down {"))
                assertTrue(previewSourceImpact.newText.contains("    port status {"))
                assertFalse(previewSourceImpact.newText.contains("port ShutterMotorM31.up"))
                assertTrue(submission.preview.changes.single().affectedSubjectIdentities.contains("component:ShutterMotorM31"))
                val governedEvidence = assertNotNull(submission.preview.entityCreationEvidence)
                assertEquals("ShutterMotorM31", governedEvidence.canonicalTag)
                assertEquals("Motor", governedEvidence.semanticType)
                assertEquals("MOTOR-AC", governedEvidence.model)
                assertEquals(listOf("up", "down", "status"), governedEvidence.nestedPorts.map { port -> port.name })
                assertEquals("iec.motor.compact", governedEvidence.representationId)
                assertEquals("composition:alignment_group", governedEvidence.compositionTargetId)
                assertEquals(previewSourceImpact.revisionGuard, governedEvidence.sourceEdit.revisionGuard)

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Apply source-backed component insertion.",
                        ),
                    ).get(),
                )

                val sourceEdit = assertNotNull(decision.sourceEdit)
                val transactionResult = assertNotNull(decision.transactionResult)
                assertEquals("reprojected", transactionResult.lifecycleState)
                assertTrue(transactionResult.mutationId.orEmpty().startsWith("authoring-mutation:"))
                assertNotNull(transactionResult.committedRevision)
                assertTrue("component:ShutterMotorM31" in transactionResult.affectedSemanticIds)
                assertTrue(transactionResult.projectionOccurrenceIds.any { occurrenceId ->
                    occurrenceId.contains("ShutterMotorM31")
                })
                assertEquals(documentUri, sourceEdit.uri)
                assertTrue(sourceEdit.newText.contains("device ShutterMotorM31"))
                assertTrue(sourceEdit.newText.contains("componentRef \"electrical.motor.ac\""))
                assertTrue(sourceEdit.newText.contains("model \"MOTOR-AC\""))
                assertNotNull(sourceEdit.revisionGuard)
                assertTrue(sourceEdit.newText.contains("    port up {"))
                assertTrue(sourceEdit.newText.contains("    port down {"))
                assertTrue(sourceEdit.newText.contains("    port status {"))
                assertFalse(sourceEdit.newText.contains("port ShutterMotorM31.up"))
                assertFalse(sourceEdit.newText.contains("port ShutterMotorM31.status"))
                assertTrue(
                    Files.readString(repository.seedSourcePath).contains("device ShutterMotorM31"),
                    "Mutation Authority must change canonical source before reporting reprojected.",
                )
                assertTrue(
                    assertNotNull(server.trackedDocument(documentUri)).text.contains("device ShutterMotorM31"),
                    "LSP tracked source must reflect the committed mutation before the decision returns.",
                )
                val selectionRange = assertNotNull(sourceEdit.selectionRange)
                assertEquals("component:ShutterMotorM31", sourceEdit.suggestedSemanticId)

                val updatedSource = applySourceEdit(
                    source = authoringSource,
                    edit = sourceEdit,
                )
                val selectedSlice = updatedSource.sliceRange(selectionRange)
                assertTrue(selectedSlice.startsWith("device ShutterMotorM31"))
                assertTrue(selectedSlice.contains("componentRef \"electrical.motor.ac\""))
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(updatedSource))
                    },
                )

                val inspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals(3, inspection.componentCount)
                assertTrue(inspection.ports.any { port -> port.path == "ShutterMotorM31.up" })
                assertTrue(inspection.ports.any { port -> port.path == "ShutterMotorM31.status" })
                assertTrue(inspection.components.any { component -> component.name == "ShutterMotorM31" })

                val projection = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
                val readyProjection = assertNotNull(projection.readyProjection)
                assertEquals("ready", projection.status)
                assertTrue(readyProjection.components.any { component ->
                    component.semanticId == "component:ShutterMotorM31"
                })
                assertTrue(readyProjection.labels.any { label ->
                    label.semanticId == "port:ShutterMotorM31.up"
                })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `stale governed entity preview returns no source edit`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-stale-m31-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply { rootUri = repositoryRoot.toUri().toString() },
                ).get()
                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(documentUri, "athena", 1, authoringSource),
                    ),
                )
                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-stale-m31",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "ShutterMotorM31",
                        ),
                    ).get(),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(""))
                    },
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                        ),
                    ).get(),
                )

                assertEquals("unavailable", decision.status)
                assertNull(decision.sourceEdit)
                assertTrue(decision.reason.orEmpty().contains("authoring.preview.stale"))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `connected editor rejection keeps governed entity transaction blocked`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-client-reject-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val server = AthenaLanguageServer()
            val client = RejectingWorkspaceEditClient()
            try {
                server.connect(client)
                server.initialize(
                    InitializeParams().apply { rootUri = repositoryRoot.toUri().toString() },
                ).get()
                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(documentUri, "athena", 1, authoringSource),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-client-reject-m31",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "ShutterMotorM31",
                        ),
                    ).get(),
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                        ),
                    ).get(),
                )

                val workspaceEdit = client.applyEditRequests.single().edit.documentChanges.single().left
                assertEquals(documentUri, workspaceEdit.textDocument.uri)
                assertEquals(1, workspaceEdit.textDocument.version)
                assertEquals("unavailable", decision.status)
                assertEquals("blocked", decision.preview?.status)
                assertEquals("blocked", decision.transactionResult?.lifecycleState)
                assertEquals("mutation-authority", decision.transactionResult?.diagnostics?.single()?.authority)
                assertNull(decision.sourceEdit)
                assertFalse(Files.readString(repository.seedSourcePath).contains("device ShutterMotorM31"))
                assertFalse(assertNotNull(server.trackedDocument(documentUri)).text.contains("device ShutterMotorM31"))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `rejected create-entity preview exposes source impact without mutating source or projection`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-insert-reject-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-insert-reject-0001",
                            intentKind = "create-entity",
                            originSurface = "graph",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.plc.cpu.default",
                            conceptId = "electrical.plc.cpu",
                            actor = "user:test",
                            preferredImplementationId = "impl/electrical/plc-cpu/siemens-proof-cpu313c",
                            suggestedName = "PLC2",
                        ),
                    ).get(),
                )
                assertNotNull(submission.preview.sourceImpact)

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "rejected",
                            note = "Do not apply insertion.",
                        ),
                    ).get(),
                )

                assertEquals("updated", decision.status)
                assertEquals("rejected", decision.preview?.status)
                assertNull(decision.sourceEdit)

                val inspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("ready", inspection.status)
                assertEquals(2, inspection.componentCount)
                assertFalse(inspection.components.any { component -> component.name == "PLC2" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `accepted update-component preview rewrites device references and keeps tracked semantic state coherent`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-update-",
            sourceText = authoringUpdateSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringUpdateSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-0004",
                            intentKind = "update-entity-properties",
                            originSurface = "inspector",
                            entitySubjectId = "component:PLC1",
                            actor = "user:test",
                            properties = mapOf(
                                "name" to AthenaAuthoringValuePayload(
                                    kind = "symbol",
                                    text = "PLC2",
                                ),
                                "label" to AthenaAuthoringValuePayload(
                                    kind = "text",
                                    text = "Main PLC 2",
                                ),
                                "description" to AthenaAuthoringValuePayload(
                                    kind = "text",
                                    text = "Updated line controller",
                                ),
                                "preferredImplementationId" to AthenaAuthoringValuePayload(
                                    kind = "symbol",
                                    text = "impl/electrical/plc-cpu/siemens-proof-cpu314c",
                                ),
                            ),
                        ),
                    ).get(),
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Apply governed inspector update.",
                        ),
                    ).get(),
                )

                val sourceEdit = assertNotNull(decision.sourceEdit)
                assertEquals(documentUri, sourceEdit.uri)
                assertTrue(sourceEdit.newText.contains("device PLC2"))
                assertTrue(sourceEdit.newText.contains("label \"Main PLC 2\""))
                assertTrue(sourceEdit.newText.contains("note \"Updated line controller\""))
                assertTrue(sourceEdit.newText.contains("vendorPartNumber \"proof.cpu.314c\""))
                assertEquals("component:PLC2", sourceEdit.suggestedSemanticId)

                val updatedSource = applySourceEdit(
                    source = authoringUpdateSource,
                    edit = sourceEdit,
                )
                assertTrue(updatedSource.contains("port PLC2.out"))
                assertTrue(updatedSource.contains("connect PLC2.out -> M1.in"))
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(updatedSource))
                    },
                )

                val inspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("ready", inspection.status)
                assertTrue(inspection.diagnosticSummaries.isEmpty())
                assertTrue(inspection.components.any { component -> component.name == "PLC2" })
                assertTrue(inspection.ports.any { port -> port.path == "PLC2.out" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `stale update-component preview returns no unreviewed source edit`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-update-stale-",
            sourceText = authoringUpdateSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringUpdateSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-update-stale",
                            intentKind = "update-entity-properties",
                            originSurface = "inspector",
                            entitySubjectId = "component:PLC1",
                            actor = "user:test",
                            properties = mapOf(
                                "label" to AthenaAuthoringValuePayload(
                                    kind = "text",
                                    text = "Main PLC stale edit",
                                ),
                            ),
                        ),
                    ).get(),
                )

                val externallyChangedSource = "\n$authoringUpdateSource"
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(externallyChangedSource))
                    },
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Attempt stale inspector update.",
                        ),
                    ).get(),
                )

                assertEquals("updated", decision.status)
                assertEquals("accepted", decision.preview?.status)
                assertNull(decision.sourceEdit)
                assertEquals(externallyChangedSource, assertNotNull(server.trackedDocument(documentUri)).text)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `accepted semantic relationship preview rebuilds canonical connection state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-connect-",
            sourceText = authoringConnectSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringConnectSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-0005",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:PLC1.out",
                            targetSubjectId = "port:M1.in",
                        ),
                    ).get(),
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Apply governed graph connect.",
                        ),
                    ).get(),
                )

                val sourceEdit = assertNotNull(decision.sourceEdit)
                assertEquals(documentUri, sourceEdit.uri)
                assertTrue(sourceEdit.newText.contains("connect PLC1.out -> M1.in"))
                assertEquals("connection:PLC1.out->M1.in", sourceEdit.suggestedSemanticId)
                assertNotNull(sourceEdit.revisionGuard)

                val updatedSource = applySourceEdit(
                    source = authoringConnectSource,
                    edit = sourceEdit,
                )
                assertTrue(updatedSource.contains("connect PLC1.out -> M1.in"))
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(updatedSource))
                    },
                )

                val inspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("ready", inspection.status)
                assertEquals(1, inspection.connectionCount)
                assertTrue(inspection.connections.any { connection -> connection.semanticId == "connection:PLC1.out->M1.in" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `accepted semantic-relationship preview returns governed electrical connection source edit`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-relationship-",
            sourceText = authoringConnectSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringConnectSource,
                        ),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-relationship-0001",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:PLC1.out",
                            targetSubjectId = "port:M1.in",
                            projectionViewId = "schematic",
                            persistenceSourceUri = documentUri,
                        ),
                    ).get(),
                )
                assertEquals("semantic-relationship", submission.preview.intentKind)
                val relationshipEvidence = assertNotNull(submission.preview.relationshipEvidence)
                assertEquals("port:PLC1.out", relationshipEvidence.sourceSubjectId)
                assertEquals("port:M1.in", relationshipEvidence.targetSubjectId)
                assertEquals("ElectricalConnectionRelationship", relationshipEvidence.relationshipType)
                assertEquals("compatible", relationshipEvidence.compatibility)
                assertEquals(listOf("connection:PLC1.out->M1.in"), relationshipEvidence.affectedSemanticIds)
                assertEquals(documentUri, assertNotNull(relationshipEvidence.sourceEdit).uri)
                assertNull(
                    relationshipEvidence.routePreview,
                    "Route evidence must be omitted when downstream routing facts are unavailable.",
                )

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Apply governed graph relationship.",
                        ),
                    ).get(),
                )

                val sourceEdit = assertNotNull(decision.sourceEdit)
                assertEquals(documentUri, sourceEdit.uri)
                assertEquals("connection:PLC1.out->M1.in", sourceEdit.suggestedSemanticId)
                assertTrue(sourceEdit.newText.contains("connect PLC1.out -> M1.in"))
                assertTrue(
                    Files.readString(repository.seedSourcePath).contains("connect PLC1.out -> M1.in"),
                    "Relationship Mutation Authority must change canonical source before reporting reprojected.",
                )
                val transactionResult = assertNotNull(decision.transactionResult)
                assertEquals("reprojected", transactionResult.lifecycleState)
                assertTrue(transactionResult.mutationId.orEmpty().startsWith("authoring-mutation:"))
                assertEquals(listOf("connection:PLC1.out->M1.in"), transactionResult.affectedSemanticIds)
                assertTrue(transactionResult.projectionOccurrenceIds.isNotEmpty())
                val repeatedDecision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                        ),
                    ).get(),
                )
                assertEquals("unavailable", repeatedDecision.status)
                assertNull(repeatedDecision.sourceEdit)

                val updatedSource = applySourceEdit(
                    source = authoringConnectSource,
                    edit = sourceEdit,
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams().apply {
                        textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                        contentChanges = listOf(TextDocumentContentChangeEvent(updatedSource))
                    },
                )

                val projection = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
                val readyProjection = assertNotNull(projection.readyProjection)
                assertEquals("ready", projection.status)
                assertTrue(readyProjection.connections.any { connection -> connection.semanticId == "connection:PLC1.out->M1.in" })
                assertTrue(readyProjection.electricalConnectionEndpoints.any { endpoint ->
                    endpoint.connectionSemanticId == "connection:PLC1.out->M1.in" &&
                        endpoint.portSemanticId == "port:PLC1.out"
                })
                assertTrue(readyProjection.electricalConnectionEndpoints.any { endpoint ->
                    endpoint.connectionSemanticId == "connection:PLC1.out->M1.in" &&
                        endpoint.portSemanticId == "port:M1.in"
                })
                assertTrue(readyProjection.electricalRoutingCorridors.any { corridor ->
                    corridor.connectionSemanticId == "connection:PLC1.out->M1.in"
                })

                val authoringState = assertNotNull(server.authoringState(AthenaAuthoringStateParams()).get())
                assertEquals(0, authoringState.pendingPreviewCount)
                assertEquals("accepted", authoringState.previews.single { preview ->
                    preview.intentId == submission.preview.intentId
                }.status)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `invalid semantic-relationship accept is blocked by backend source edit gate`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-relationship-invalid-",
            sourceText = authoringConnectSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            authoringConnectSource,
                        ),
                    ),
                )

                val beforeInspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals(0, beforeInspection.connectionCount)

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-relationship-invalid-0001",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:M1.in",
                            targetSubjectId = "port:PLC1.out",
                            projectionViewId = "schematic",
                            persistenceSourceUri = documentUri,
                        ),
                    ).get(),
                )
                assertEquals("semantic-relationship", submission.preview.intentKind)
                assertEquals(false, submission.preview.acceptanceEligible)
                assertEquals("incompatible", submission.preview.relationshipEvidence?.compatibility)
                assertEquals("authoring.relationship.incompatible", submission.preview.diagnostics.single().code)

                val decision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = submission.preview.previewId,
                            intentId = submission.preview.intentId,
                            decision = "accepted",
                            note = "Attempt invalid graph relationship.",
                        ),
                    ).get(),
                )

                assertEquals("unavailable", decision.status)
                assertNull(decision.sourceEdit)

                val afterInspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("ready", afterInspection.status)
                assertEquals(0, afterInspection.connectionCount)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `malformed relationship request returns structured blocked preview`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-relationship-malformed-",
            sourceText = authoringConnectSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(InitializeParams().apply { rootUri = repositoryRoot.toUri().toString() }).get()
                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(documentUri, "athena", 1, authoringConnectSource),
                    ),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-relationship-malformed",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = null,
                            targetSubjectId = "port:M1.in",
                            persistenceSourceUri = documentUri,
                        ),
                    ).get(),
                )

                assertEquals(false, submission.preview.acceptanceEligible)
                assertEquals("not-evaluated", submission.preview.relationshipEvidence?.compatibility)
                assertEquals("authoring.relationship.subject-unresolved", submission.preview.diagnostics.single().code)
                assertEquals("capability-registry", submission.preview.diagnostics.single().authority)

                val invalidOrigin = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-relationship-invalid-origin",
                            intentKind = "semantic-relationship",
                            originSurface = "unknown-surface",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:PLC1.out",
                            targetSubjectId = "port:M1.in",
                            persistenceSourceUri = documentUri,
                        ),
                    ).get(),
                )
                assertEquals("blocked", invalidOrigin.status)
                assertEquals(false, invalidOrigin.preview.acceptanceEligible)
                assertEquals("blocked", invalidOrigin.preview.status)
                assertEquals("authoring.source.invalid", invalidOrigin.preview.diagnostics.single().code)
                assertEquals("transaction-runtime", invalidOrigin.preview.diagnostics.single().authority)

                val validSubmission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-malformed-decision",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:PLC1.out",
                            targetSubjectId = "port:M1.in",
                            persistenceSourceUri = documentUri,
                        ),
                    ).get(),
                )
                val malformedDecision = assertNotNull(
                    server.authoringDecision(
                        AthenaAuthoringDecisionParams(
                            previewId = validSubmission.preview.previewId,
                            intentId = validSubmission.preview.intentId,
                            decision = "force-commit",
                        ),
                    ).get(),
                )
                assertEquals("unavailable", malformedDecision.status)
                assertTrue(malformedDecision.reason.orEmpty().contains("decision", ignoreCase = true))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `relationship negative matrix preserves structured LSP diagnostics`() {
        data class Case(
            val name: String,
            val relationshipType: String = "ElectricalConnectionRelationship",
            val sourceSubjectId: String = "port:PLC1.out",
            val targetSubjectId: String = "port:M1.in",
            val persistenceSourceUri: String? = null,
            val diagnosticCode: String,
            val compatibility: String,
        )

        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-relationship-matrix-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(InitializeParams().apply { rootUri = repositoryRoot.toUri().toString() }).get()
                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(TextDocumentItem(documentUri, "athena", 1, authoringSource)),
                )
                val cases = listOf(
                    Case(
                        name = "self",
                        targetSubjectId = "port:PLC1.out",
                        diagnosticCode = "authoring.relationship.self",
                        compatibility = "not-evaluated",
                    ),
                    Case(
                        name = "unsupported",
                        relationshipType = "FlowRelationship",
                        diagnosticCode = "authoring.relationship.type-unsupported",
                        compatibility = "not-evaluated",
                    ),
                    Case(
                        name = "persistence",
                        persistenceSourceUri = "file:///workspace/other.athena",
                        diagnosticCode = "authoring.source.conflict",
                        compatibility = "not-evaluated",
                    ),
                    Case(
                        name = "duplicate",
                        diagnosticCode = "authoring.relationship.duplicate",
                        compatibility = "compatible",
                    ),
                )

                cases.forEach { case ->
                    val submission = assertNotNull(
                        server.authoringPreview(
                            AthenaAuthoringPreviewParams(
                                intentId = "intent-matrix-${case.name}",
                                intentKind = "semantic-relationship",
                                originSurface = "graph",
                                relationshipType = case.relationshipType,
                                sourceSubjectId = case.sourceSubjectId,
                                targetSubjectId = case.targetSubjectId,
                                persistenceSourceUri = case.persistenceSourceUri ?: documentUri,
                            ),
                        ).get(),
                    )
                    assertEquals(false, submission.preview.acceptanceEligible, case.name)
                    assertEquals(case.diagnosticCode, submission.preview.diagnostics.single().code, case.name)
                    assertEquals(case.compatibility, submission.preview.relationshipEvidence?.compatibility, case.name)
                }
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `entity preview consumes registry actor policy`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-capability-policy-",
            sourceText = authoringSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(InitializeParams().apply { rootUri = repositoryRoot.toUri().toString() }).get()
                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(TextDocumentItem(documentUri, "athena", 1, authoringSource)),
                )

                val submission = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-capability-policy",
                            intentKind = "create-entity",
                            originSurface = "form",
                            parentSubjectId = "system:FactoryLine",
                            conceptTemplateId = "electrical.motor.ac.default",
                            conceptId = "electrical.motor.ac",
                            actor = "user:test",
                            suggestedName = "BlockedMotorM31",
                        ),
                    ).get(),
                )

                assertEquals(false, submission.preview.acceptanceEligible)
                assertEquals("authoring.validation.stop-downstream", submission.preview.diagnostics.single().code)
                assertEquals("capability-registry", submission.preview.diagnostics.single().authority)
                assertEquals(null, submission.preview.sourceImpact)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `guided authoring proof flow stays repository backed across create rename insert and connect`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-authoring-proof-",
            sourceText = guidedAuthoringProofSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            guidedAuthoringProofSource,
                        ),
                    ),
                )

                var currentSource = guidedAuthoringProofSource
                var currentVersion = 1

                fun applyDecision(decision: AthenaAuthoringPreviewDecisionPayload) {
                    val sourceEdit = assertNotNull(
                        decision.sourceEdit,
                        "${decision.reason}; ${decision.transactionResult?.diagnostics}",
                    )
                    currentSource = applySourceEdit(
                        source = currentSource,
                        edit = sourceEdit,
                    )
                    currentVersion += 1
                    server.textDocumentService.didChange(
                        DidChangeTextDocumentParams().apply {
                            textDocument = VersionedTextDocumentIdentifier(documentUri, currentVersion)
                            contentChanges = listOf(TextDocumentContentChangeEvent(currentSource))
                        },
                    )
                }

                val plcInsert = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-proof-0001",
                            intentKind = "create-entity",
                            originSurface = "palette",
                            parentSubjectId = "system:GuidedAuthoringProof",
                            conceptTemplateId = "electrical.plc.cpu.default",
                            conceptId = "electrical.plc.cpu",
                            actor = "user:test",
                            preferredImplementationId = "impl/electrical/plc-cpu/siemens-proof-cpu313c",
                            suggestedName = "PLC1",
                        ),
                    ).get(),
                )
                applyDecision(
                    assertNotNull(
                        server.authoringDecision(
                            AthenaAuthoringDecisionParams(
                                previewId = plcInsert.preview.previewId,
                                intentId = plcInsert.preview.intentId,
                                decision = "accepted",
                                note = "Insert PLC for guided proof.",
                            ),
                        ).get(),
                    ),
                )

                val plcRename = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-proof-0002",
                            intentKind = "update-entity-properties",
                            originSurface = "inspector",
                            entitySubjectId = "component:PLC1",
                            actor = "user:test",
                            properties = mapOf(
                                "name" to AthenaAuthoringValuePayload(
                                    kind = "symbol",
                                    text = "PLCMAIN",
                                ),
                            ),
                        ),
                    ).get(),
                )
                applyDecision(
                    assertNotNull(
                        server.authoringDecision(
                            AthenaAuthoringDecisionParams(
                                previewId = plcRename.preview.previewId,
                                intentId = plcRename.preview.intentId,
                                decision = "accepted",
                                note = "Rename PLC for guided proof.",
                            ),
                        ).get(),
                    ),
                )

                val powerInsert = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-proof-0003",
                            intentKind = "create-entity",
                            originSurface = "palette",
                            parentSubjectId = "system:GuidedAuthoringProof",
                            conceptTemplateId = "electrical.power-supply.dc24.default",
                            conceptId = "electrical.power-supply.dc24",
                            actor = "user:test",
                            preferredImplementationId = "impl/electrical/power-supply/siemens-proof-24vdc",
                            suggestedName = "PWR1",
                        ),
                    ).get(),
                )
                applyDecision(
                    assertNotNull(
                        server.authoringDecision(
                            AthenaAuthoringDecisionParams(
                                previewId = powerInsert.preview.previewId,
                                intentId = powerInsert.preview.intentId,
                                decision = "accepted",
                                note = "Insert power supply for guided proof.",
                            ),
                        ).get(),
                    ),
                )
                val connect = assertNotNull(
                    server.authoringPreview(
                        AthenaAuthoringPreviewParams(
                            intentId = "intent-proof-0004",
                            intentKind = "semantic-relationship",
                            originSurface = "graph",
                            relationshipType = "ElectricalConnectionRelationship",
                            sourceSubjectId = "port:PWR1.out",
                            targetSubjectId = "port:PLCMAIN.lplus",
                        ),
                    ).get(),
                )
                applyDecision(
                    assertNotNull(
                        server.authoringDecision(
                            AthenaAuthoringDecisionParams(
                                previewId = connect.preview.previewId,
                                intentId = connect.preview.intentId,
                                decision = "accepted",
                                note = "Connect compatible ports for guided proof.",
                            ),
                        ).get(),
                    ),
                )

                val inspection = assertNotNull(
                    server.semanticInspection(
                        AthenaSemanticInspectionParams(
                            textDocument = AthenaSemanticInspectionTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals("ready", inspection.status)
                assertEquals(2, inspection.componentCount)
                assertTrue(inspection.components.any { component -> component.name == "PLCMAIN" })
                assertTrue(inspection.components.any { component -> component.name == "PWR1" })
                assertTrue(inspection.ports.any { port -> port.path == "PLCMAIN.lplus" })
                assertTrue(inspection.ports.any { port -> port.path == "PWR1.out" })
                assertEquals(1, inspection.connectionCount)
                assertTrue(inspection.connections.any { connection -> connection.semanticId == "connection:PWR1.out->PLCMAIN.lplus" })
                assertTrue(currentSource.contains("device PLCMAIN"))
                assertTrue(currentSource.contains("device PWR1"))
                assertTrue(currentSource.contains("    port lplus {"))
                assertTrue(currentSource.contains("    port out {"))
                assertFalse(currentSource.contains("port PLCMAIN.lplus"))
                assertFalse(currentSource.contains("port PWR1.out"))
                assertTrue(currentSource.contains("connect PWR1.out -> PLCMAIN.lplus"))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val authoringSource = """
    system FactoryLine {
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

      connect PLC1.out -> M1.in
    }
""".trimIndent()

private val authoringUpdateSource = """
    system FactoryLine {
      device PLC1 {
        type Switch
        vendorPartNumber "proof.cpu.313c"
        label "Main PLC"
        note "Original line controller"
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

      connect PLC1.out -> M1.in
    }
""".trimIndent()

private val authoringConnectSource = """
    system FactoryLine {
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
""".trimIndent()

private val guidedAuthoringProofSource = """
    system GuidedAuthoringProof {
    }
""".trimIndent()

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

private fun String.sliceRange(range: AthenaAuthoringSourceRangePayload): String {
    val startOffset = offsetAt(range.start.line, range.start.character)
    val endOffset = offsetAt(range.end.line, range.end.character)
    return substring(startOffset, endOffset.coerceAtLeast(startOffset))
}

private class RejectingWorkspaceEditClient : LanguageClient {
    val applyEditRequests = mutableListOf<ApplyWorkspaceEditParams>()

    override fun telemetryEvent(`object`: Any?) = Unit

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) = Unit

    override fun showMessage(messageParams: MessageParams) = Unit

    override fun showMessageRequest(requestParams: ShowMessageRequestParams): CompletableFuture<MessageActionItem> =
        CompletableFuture.completedFuture(null)

    override fun logMessage(message: MessageParams) = Unit

    override fun applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture<ApplyWorkspaceEditResponse> {
        applyEditRequests += params
        return CompletableFuture.completedFuture(
            ApplyWorkspaceEditResponse(false).apply {
                failureReason = "client refused governed edit"
            },
        )
    }
}
