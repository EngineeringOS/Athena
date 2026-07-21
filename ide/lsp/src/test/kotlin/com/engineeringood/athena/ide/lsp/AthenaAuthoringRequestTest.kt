package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
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
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier

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
                            intentKind = "create-component",
                            originSurface = "palette",
                            parentIdentity = "system:FactoryLine",
                            conceptId = "electrical.plc.cpu",
                            preferredImplementationId = "impl/electrical/plc-cpu/siemens-proof-cpu313c",
                            suggestedName = "PLC2",
                        ),
                    ).get(),
                )

                assertEquals("factory-line", payload.projectName)
                assertEquals("frontend -> LSP -> runtime/compiler", payload.semanticPath)
                assertEquals("submitted", payload.status)
                assertEquals("create-component", payload.preview.intentKind)
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
                            intentKind = "update-component-properties",
                            originSurface = "inspector",
                            componentId = "component:PLC1",
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
                assertEquals("update-component-properties", initialState.previews.single().intentKind)

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
    fun `accepted create-component preview returns source edit and graph-source state can rebuild through tracked text`() {
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
                            intentKind = "create-component",
                            originSurface = "palette",
                            parentIdentity = "system:FactoryLine",
                            conceptId = "electrical.plc.cpu",
                            preferredImplementationId = "impl/electrical/plc-cpu/siemens-proof-cpu313c",
                            suggestedName = "PLC2",
                        ),
                    ).get(),
                )
                val previewSourceImpact = assertNotNull(submission.preview.sourceImpact)
                assertEquals(documentUri, previewSourceImpact.uri)
                assertEquals("component:PLC2", previewSourceImpact.suggestedSemanticId)
                assertTrue(previewSourceImpact.newText.contains("device PLC2"))
                assertTrue(previewSourceImpact.newText.contains("    port lplus {"))
                assertTrue(previewSourceImpact.newText.contains("    port mpi {"))
                assertFalse(previewSourceImpact.newText.contains("port PLC2.lplus"))
                assertTrue(submission.preview.changes.single().affectedSubjectIdentities.contains("component:PLC2"))

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
                assertEquals(documentUri, sourceEdit.uri)
                assertTrue(sourceEdit.newText.contains("device PLC2"))
                assertTrue(sourceEdit.newText.contains("componentRef \"electrical.plc.cpu\""))
                assertTrue(sourceEdit.newText.contains("vendorPartNumber \"proof.cpu.313c\""))
                assertTrue(sourceEdit.newText.contains("    port lplus {"))
                assertTrue(sourceEdit.newText.contains("    port mpi {"))
                assertFalse(sourceEdit.newText.contains("port PLC2.lplus"))
                assertFalse(sourceEdit.newText.contains("port PLC2.mpi"))
                val selectionRange = assertNotNull(sourceEdit.selectionRange)
                assertEquals("component:PLC2", sourceEdit.suggestedSemanticId)

                val updatedSource = applySourceEdit(
                    source = authoringSource,
                    edit = sourceEdit,
                )
                val selectedSlice = updatedSource.sliceRange(selectionRange)
                assertTrue(selectedSlice.startsWith("device PLC2"))
                assertTrue(selectedSlice.contains("componentRef \"electrical.plc.cpu\""))
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
                assertTrue(inspection.ports.any { port -> port.path == "PLC2.lplus" })
                assertTrue(inspection.components.any { component -> component.name == "PLC2" })

                val projection = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
                val readyProjection = assertNotNull(projection.readyProjection)
                assertEquals("ready", projection.status)
                assertTrue(readyProjection.components.any { component ->
                    component.semanticId == "component:PLC2"
                })
                assertTrue(readyProjection.labels.any { label ->
                    label.semanticId == "port:PLC2.lplus"
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
    fun `rejected create-component preview exposes source impact without mutating source or projection`() {
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
                            intentKind = "create-component",
                            originSurface = "graph",
                            parentIdentity = "system:FactoryLine",
                            conceptId = "electrical.plc.cpu",
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
                            intentKind = "update-component-properties",
                            originSurface = "inspector",
                            componentId = "component:PLC1",
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
    fun `accepted connect-ports preview returns a governed source edit and rebuilds canonical connection state`() {
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
                            intentKind = "connect-ports",
                            originSurface = "graph",
                            sourcePortId = "port:PLC1.out",
                            targetPortId = "port:M1.in",
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

                assertEquals("updated", decision.status)
                assertEquals("accepted", decision.preview?.status)
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
                    val sourceEdit = assertNotNull(decision.sourceEdit)
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
                            intentKind = "create-component",
                            originSurface = "palette",
                            parentIdentity = "system:GuidedAuthoringProof",
                            conceptId = "electrical.plc.cpu",
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
                            intentKind = "update-component-properties",
                            originSurface = "inspector",
                            componentId = "component:PLC1",
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
                            intentKind = "create-component",
                            originSurface = "palette",
                            parentIdentity = "system:GuidedAuthoringProof",
                            conceptId = "electrical.power-supply.dc24",
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
                            intentKind = "connect-ports",
                            originSurface = "graph",
                            sourcePortId = "port:PWR1.out",
                            targetPortId = "port:PLCMAIN.lplus",
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
