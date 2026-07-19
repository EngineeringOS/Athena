package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.layout.ProjectionInteractivity
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.io.path.writeText

class AthenaProjectionRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `graph command intent request accepts interactive cabinet placement intent with inspectable payload`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-intent-accepted-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val payload = server.graphCommandIntent(
                    AthenaGraphCommandIntentParams(
                        intentId = "adjust-layout-placement",
                        viewId = "cabinet",
                        target = AthenaGraphCommandTargetPayload(
                            semanticId = "component:PLC1",
                            subjectKind = "component",
                        ),
                        requestedPlacement = AthenaGraphPlacementPayload(
                            x = 180,
                            y = 120,
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("accepted", payload.status)
                assertEquals("adjust-layout-placement", payload.intentId)
                assertEquals("projection-mutation", payload.mutationCategory)
                assertEquals("cabinet", payload.viewId)
                assertEquals("component:PLC1", payload.target.semanticId)
                assertEquals("component", payload.target.subjectKind)
                assertEquals(180, payload.requestedPlacement?.x)
                assertEquals(120, payload.requestedPlacement?.y)
                assertNull(payload.reason)

                val refreshedProjection = server.projectionSession(AthenaProjectionSessionParams()).get()
                val readyProjection = assertNotNull(refreshedProjection?.readyProjection)
                val plc = readyProjection.components.first { component -> component.semanticId == "component:PLC1" }
                assertEquals(180, plc.x)
                assertEquals(120, plc.y)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `graph command intent request rejects inspect only view ownership`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-intent-rejected-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "wiring",
                    ),
                ).get()

                val payload = server.graphCommandIntent(
                    AthenaGraphCommandIntentParams(
                        intentId = "adjust-layout-placement",
                        viewId = "wiring",
                        target = AthenaGraphCommandTargetPayload(
                            semanticId = "component:PLC1",
                            subjectKind = "component",
                        ),
                        requestedPlacement = AthenaGraphPlacementPayload(
                            x = 180,
                            y = 120,
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("rejected", payload.status)
                assertEquals("adjust-layout-placement", payload.intentId)
                assertEquals("projection-mutation", payload.mutationCategory)
                assertEquals("wiring", payload.viewId)
                assertTrue(payload.reason.orEmpty().contains("inspect", ignoreCase = true))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `projection session payload preserves governed sheet publication facts`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-session-publication-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val payload = server.projectionSession(AthenaProjectionSessionParams()).get()
                val ready = assertNotNull(payload?.readyProjection)
                val sheet = ready.sheets.single()

                assertEquals("cabinet/sheet/01-main", sheet.sheetId)
                assertEquals(
                    AthenaProjectionSheetPublicationPayload(
                        pageSize = AthenaProjectionSheetPageSizePayload(
                            format = "A3",
                            orientation = "landscape",
                        ),
                        frame = AthenaProjectionSheetFramePayload(
                            frameId = "engineering-sheet-frame",
                            style = "schematic",
                        ),
                        coordinateZones = listOf(
                            AthenaProjectionSheetCoordinateZonePayload(
                                zoneId = "header",
                                label = "Header",
                                order = 0,
                            ),
                            AthenaProjectionSheetCoordinateZonePayload(
                                zoneId = "body",
                                label = "Body",
                                order = 1,
                            ),
                            AthenaProjectionSheetCoordinateZonePayload(
                                zoneId = "title-block",
                                label = "Title Block",
                                order = 2,
                            ),
                        ),
                        titleBlock = AthenaProjectionSheetTitleBlockPayload(
                            sheetTitle = "Cabinet Main",
                            sheetFamily = "cabinet",
                            sheetNumber = "01-main",
                        ),
                        revisionMetadata = AthenaProjectionSheetRevisionMetadataPayload(
                            revisionCode = "A",
                            revisionNote = "Initial governed sheet publication",
                        ),
                        viewComposition = AthenaProjectionSheetViewCompositionPayload(
                            primaryViewId = "cabinet",
                            primarySheetOrder = 0,
                            subjectSemanticIds = listOf(
                                "component:M1",
                                "component:PLC1",
                                "connection:PLC1.out->M1.in",
                                "port:M1.in",
                                "port:PLC1.out",
                            ),
                        ),
                    ),
                    sheet.publication,
                )
                assertEquals(
                    AthenaProjectionSheetCompositionPayload(
                        sheetId = "cabinet/sheet/01-main",
                        displayName = "Cabinet Main",
                        order = 0,
                        representationFamilyId = "schematic-sheet",
                        publication = AthenaProjectionSheetPublicationPayload(
                            pageSize = AthenaProjectionSheetPageSizePayload(
                                format = "A3",
                                orientation = "landscape",
                            ),
                            frame = AthenaProjectionSheetFramePayload(
                                frameId = "engineering-sheet-frame",
                                style = "schematic",
                            ),
                            coordinateZones = listOf(
                                AthenaProjectionSheetCoordinateZonePayload(
                                    zoneId = "header",
                                    label = "Header",
                                    order = 0,
                                ),
                                AthenaProjectionSheetCoordinateZonePayload(
                                    zoneId = "body",
                                    label = "Body",
                                    order = 1,
                                ),
                                AthenaProjectionSheetCoordinateZonePayload(
                                    zoneId = "title-block",
                                    label = "Title Block",
                                    order = 2,
                                ),
                            ),
                            titleBlock = AthenaProjectionSheetTitleBlockPayload(
                                sheetTitle = "Cabinet Main",
                                sheetFamily = "cabinet",
                                sheetNumber = "01-main",
                            ),
                            revisionMetadata = AthenaProjectionSheetRevisionMetadataPayload(
                                revisionCode = "A",
                                revisionNote = "Initial governed sheet publication",
                            ),
                            viewComposition = AthenaProjectionSheetViewCompositionPayload(
                                primaryViewId = "cabinet",
                                primarySheetOrder = 0,
                                subjectSemanticIds = listOf(
                                    "component:M1",
                                    "component:PLC1",
                                    "connection:PLC1.out->M1.in",
                                    "port:M1.in",
                                    "port:PLC1.out",
                                ),
                            ),
                        ),
                        subjectSemanticIds = listOf(
                            "component:M1",
                            "component:PLC1",
                            "connection:PLC1.out->M1.in",
                            "port:M1.in",
                            "port:PLC1.out",
                        ),
                    ),
                    sheet.composition,
                )
                assertEquals("schematic-sheet", sheet.composition.representationFamilyId)
                val sheetLayout = assertNotNull(ready.sheetLayout)
                assertEquals("cabinet/sheet/01-main", sheetLayout.sheetId)
                assertEquals("Cabinet Main", sheetLayout.displayName)
                assertEquals(0, sheetLayout.order)
                assertEquals("schematic-sheet", sheetLayout.representationFamilyId)
                assertEquals(sheet.subjectSemanticIds, sheetLayout.subjectSemanticIds)
                assertEquals(ready.canvasWidth, sheetLayout.frame.canvasWidth)
                assertEquals(ready.canvasHeight, sheetLayout.frame.canvasHeight)
                assertEquals(ready.components.size, sheetLayout.placements.size)
                assertEquals(ready.labels.size, sheetLayout.labelLayouts.size)
                assertEquals(ready.electricalRoutingCorridors.size, sheetLayout.routingGuidance.size)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `projection session payload renders terminal anchor route facts instead of legacy graph edges`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-route-facts-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val payload = server.projectionSession(AthenaProjectionSessionParams()).get()
                val ready = assertNotNull(payload?.readyProjection)
                val presentation = assertNotNull(ready.presentation)
                val legacyConnection = ready.connections.single()
                val connector = presentation.connectors.single()
                val anchorIds = presentation.occurrences
                    .flatMap { occurrence -> occurrence.anchorBindings }
                    .map { binding -> binding.anchorId }
                    .toSet()

                assertNotNull(connector.sourceAnchorId)
                assertNotNull(connector.targetAnchorId)
                assertTrue(connector.sourceAnchorId in anchorIds)
                assertTrue(connector.targetAnchorId in anchorIds)
                assertEquals("port:PLC1.out", connector.sourcePortSemanticId)
                assertEquals("port:M1.in", connector.targetPortSemanticId)
                assertTrue(connector.routePoints.size >= 4)
                assertTrue(
                    connector.routePoints.zipWithNext().all { (start, end) ->
                        start.x == end.x || start.y == end.y
                    },
                )
                assertTrue(connector.routePoints.all { point -> point.x % 20 == 0 && point.y % 20 == 0 })
                assertNotEquals(legacyConnection.x1 to legacyConnection.y1, connector.routePoints.first().let { it.x to it.y })
                assertNotEquals(legacyConnection.x2 to legacyConnection.y2, connector.routePoints.last().let { it.x to it.y })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `graph command intent request executes semantic connect ports through runtime and refreshes projection state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-intent-connect-accepted-",
            sourceFileName = "operator-proof.athena",
            sourceText = operatorProofSource,
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

                val payload = server.graphCommandIntent(
                    AthenaGraphCommandIntentParams(
                        intentId = "connect-ports",
                        viewId = "cabinet",
                        source = AthenaGraphCommandTargetPayload(
                            semanticId = "port:PLC1.out",
                            subjectKind = "port",
                        ),
                        target = AthenaGraphCommandTargetPayload(
                            semanticId = "port:M1.in",
                            subjectKind = "port",
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("accepted", payload.status)
                assertEquals("connect-ports", payload.intentId)
                assertEquals("semantic-mutation", payload.mutationCategory)
                assertEquals("port:PLC1.out", payload.source?.semanticId)
                assertEquals("port:M1.in", payload.target.semanticId)
                assertEquals("connect-ports", payload.intentId)
                val execution = assertNotNull(payload.execution)
                assertEquals("connect-ports", execution.commandKind)
                assertEquals("accepted", execution.outcome)
                assertTrue(execution.changedSemanticIds.contains("connection:PLC1.out->M1.in"))
                val inspection = assertNotNull(payload.inspection)
                assertEquals("command", inspection.source)
                assertTrue(inspection.historyConsequences.isNotEmpty())
                val semanticReview = assertNotNull(payload.semanticReview)
                assertEquals(1, semanticReview.authoredChangeCount)
                assertTrue(semanticReview.reviewSummary.entryCount > 0)
                assertTrue(semanticReview.commitIntent.entryCount > 0)
                assertTrue(
                    semanticReview.reviewSummary.entries.none { entry ->
                        entry.message.contains("cabinet", ignoreCase = true) ||
                            entry.message.contains("wiring", ignoreCase = true) ||
                            entry.message.contains("renderer", ignoreCase = true)
                    },
                )

                val refreshedProjection = server.projectionSession(AthenaProjectionSessionParams()).get()
                val readyProjection = assertNotNull(refreshedProjection?.readyProjection)
                assertEquals(1, readyProjection.connections.size)
                assertEquals("connection:PLC1.out->M1.in", readyProjection.connections.single().semanticId)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `source and graph mutations publish compatible semantic review facts for the same engineering change`() {
        val sourceRepository = createGovernedTestRepository(
            prefix = "athena-lsp-m8-source-review-",
            sourceFileName = "operator-proof.athena",
            sourceText = operatorProofSource,
        )
        val graphRepository = createGovernedTestRepository(
            prefix = "athena-lsp-m8-graph-review-",
            sourceFileName = "operator-proof.athena",
            sourceText = operatorProofSource,
        )

        try {
            AthenaCompiler().materializeRepositoryLock(sourceRepository.repositoryRoot)
            AthenaCompiler().materializeRepositoryLock(graphRepository.repositoryRoot)

            val sourceServer = AthenaLanguageServer()
            val graphServer = AthenaLanguageServer()
            try {
                sourceServer.initialize(
                    InitializeParams().apply {
                        rootUri = sourceRepository.repositoryRoot.toUri().toString()
                    },
                ).get()
                graphServer.initialize(
                    InitializeParams().apply {
                        rootUri = graphRepository.repositoryRoot.toUri().toString()
                    },
                ).get()

                val sourceDocumentUri = sourceRepository.seedSourcePath.toUri().toString()
                sourceServer.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            sourceDocumentUri,
                            "athena",
                            1,
                            operatorProofSource,
                        ),
                    ),
                )
                sourceServer.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(sourceDocumentUri, 2),
                        listOf(
                            TextDocumentContentChangeEvent(
                                """
                                    system OperatorProof {
                                      device PLC1 {
                                        type Switch
                                        model "S7-1200"
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
                                """.trimIndent(),
                            ),
                        ),
                    ),
                )

                val sourcePayload = assertNotNull(
                    sourceServer.sourceMutationEvaluation(
                        AthenaSourceMutationParams(
                            textDocument = AthenaSourceMutationTextDocument(sourceDocumentUri),
                        ),
                    ).get(),
                )
                val graphPayload = assertNotNull(
                    graphServer.graphCommandIntent(
                        AthenaGraphCommandIntentParams(
                            intentId = "connect-ports",
                            viewId = "cabinet",
                            source = AthenaGraphCommandTargetPayload(
                                semanticId = "port:PLC1.out",
                                subjectKind = "port",
                            ),
                            target = AthenaGraphCommandTargetPayload(
                                semanticId = "port:M1.in",
                                subjectKind = "port",
                            ),
                        ),
                    ).get(),
                )

                val sourceReview = assertNotNull(sourcePayload.semanticReview)
                val graphReview = assertNotNull(graphPayload.semanticReview)
                assertEquals(sourceReview.authoredChangeCount, graphReview.authoredChangeCount)
                assertEquals(sourceReview.derivedConsequenceCount, graphReview.derivedConsequenceCount)
                assertEquals(
                    sourceReview.reviewSummary.entries.map { entry -> entry.kind to entry.message },
                    graphReview.reviewSummary.entries.map { entry -> entry.kind to entry.message },
                )
                assertEquals(
                    sourceReview.commitIntent.entries.map { entry -> entry.kind to entry.message },
                    graphReview.commitIntent.entries.map { entry -> entry.kind to entry.message },
                )
            } finally {
                sourceServer.shutdown().get()
                graphServer.shutdown().get()
            }
        } finally {
            sourceRepository.repositoryRoot.toFile().deleteRecursively()
            graphRepository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `graph command intent request rejects semantic connect ports from inspect only views`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-graph-intent-connect-rejected-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "wiring",
                    ),
                ).get()

                val payload = server.graphCommandIntent(
                    AthenaGraphCommandIntentParams(
                        intentId = "connect-ports",
                        viewId = "wiring",
                        source = AthenaGraphCommandTargetPayload(
                            semanticId = "port:PLC1.out",
                            subjectKind = "port",
                        ),
                        target = AthenaGraphCommandTargetPayload(
                            semanticId = "port:M1.in",
                            subjectKind = "port",
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("rejected", payload.status)
                assertEquals("connect-ports", payload.intentId)
                assertEquals("semantic-mutation", payload.mutationCategory)
                assertTrue(payload.reason.orEmpty().contains("inspect", ignoreCase = true))
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session request exposes runtime owned projection state and governed command allowlist`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-ready-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("factory-line", payload.projectName)
                assertEquals(
                    listOf("cabinet", "wiring", "schematic", "documentation"),
                    payload.supportedViews.map { view -> view.viewId },
                )
                assertEquals("cabinet", payload.activeViewId)
                val cabinetView = payload.supportedViews.first { view -> view.viewId == "cabinet" }
                val wiringView = payload.supportedViews.first { view -> view.viewId == "wiring" }
                assertEquals("electrical/cabinet", cabinetView.familyId)
                assertEquals("electrical/wiring", wiringView.familyId)
                assertEquals(ProjectionInteractivity.INTERACTIVE.name.lowercase(), cabinetView.ownershipContract.interactivity)
                assertEquals(
                    listOf(
                        "devices",
                        "ports",
                        "ownership-relationships",
                        "connectivity-relationships",
                        "grouped-placement",
                        "electrical-anchors",
                        "electrical-routing-corridors",
                    ),
                    cabinetView.ownershipContract.displayScopes,
                )
                assertEquals(
                    listOf("connect-ports"),
                    cabinetView.ownershipContract.semanticCommandIds,
                )
                assertEquals(
                    listOf("adjust-layout-placement", "adjust-layout-grouping"),
                    cabinetView.ownershipContract.projectionCommandIds,
                )
                assertEquals(
                    listOf("navigate-view", "inspect-selection", "preview-related-elements"),
                    cabinetView.ownershipContract.transientInteractionKinds,
                )
                assertEquals(
                    listOf("layout-placement", "layout-group-membership"),
                    cabinetView.ownershipContract.persistedProjectionMetadataKeys,
                )
                assertEquals(ProjectionInteractivity.INSPECT_ONLY.name.lowercase(), wiringView.ownershipContract.interactivity)
                assertEquals(
                    listOf(
                        "devices",
                        "ports",
                        "signal-groups",
                        "connectivity-relationships",
                        "electrical-anchors",
                        "electrical-routing-corridors",
                    ),
                    wiringView.ownershipContract.displayScopes,
                )
                assertTrue(wiringView.ownershipContract.projectionCommandIds.isEmpty())
                assertEquals(
                    listOf("navigate-view", "inspect-selection", "preview-related-elements"),
                    wiringView.ownershipContract.transientInteractionKinds,
                )
                assertTrue(wiringView.ownershipContract.persistedProjectionMetadataKeys.isEmpty())
                assertEquals(
                    listOf("switch-active-view"),
                    payload.governedCommands.map { command -> command.commandId },
                )
                assertTrue(payload.diagnostics.isEmpty())
                assertNull(payload.unavailableReason)

                val readyProjection = assertNotNull(payload.readyProjection)
                assertEquals("electrical/cabinet", readyProjection.familyId)
                assertEquals("DemoCabinet", readyProjection.systemName)
                assertEquals(480, readyProjection.canvasWidth)
                assertEquals(172, readyProjection.canvasHeight)
                assertEquals(
                    "cabinet/projection/node/component_PLC1",
                    readyProjection.components.first { component -> component.semanticId == "component:PLC1" }.projectionId,
                )
                assertEquals("electrical-notation/cabinet/default-v1", readyProjection.notationPack?.packId)
                assertTrue(
                    readyProjection.notationPack?.subjects?.any { subject ->
                        subject.semanticId == "component:PLC1" && subject.symbolKey == "device.cabinet.default"
                    } == true,
                )
                assertEquals("cabinet/sheet/01-main", readyProjection.activeSheetId)
                assertEquals(listOf("cabinet/sheet/01-main"), readyProjection.sheets.map { sheet -> sheet.sheetId })
                assertEquals(
                    listOf("component:M1", "component:PLC1", "connection:PLC1.out->M1.in", "port:M1.in", "port:PLC1.out"),
                    readyProjection.sheets.single().subjectSemanticIds,
                )
                assertEquals(
                    listOf("electrical-runtime.render.cabinet"),
                    readyProjection.activeRenderContributions.map { contribution -> contribution.contributionId },
                )
                val presentation = assertNotNull(readyProjection.presentation)
                assertEquals(1, presentation.compositePacks.count { pack -> "electrical/cabinet" in pack.familyIds })
                assertEquals(
                    "svg_path",
                    presentation.primitivePacks
                        .flatMap { pack -> pack.primitives }
                        .first { primitive -> primitive.primitiveId == "electrical.mark.contact-open" }
                        .commands
                        .first()
                        .kind,
                )
                assertEquals(2, readyProjection.components.size)
                assertEquals(1, readyProjection.connections.size)
                assertEquals(2, readyProjection.labels.size)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection command request switches active view through explicit allowlist`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-command-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val commandPayload = server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "wiring",
                    ),
                ).get()

                assertNotNull(commandPayload)
                assertEquals("applied", commandPayload.status)
                assertEquals("switch-active-view", commandPayload.commandId)
                assertNull(commandPayload.reason)

                val updatedSession = assertNotNull(commandPayload.session)
                assertEquals("wiring", updatedSession.activeViewId)
                assertEquals("ready", updatedSession.status)
                val readyProjection = assertNotNull(updatedSession.readyProjection)
                assertEquals("wiring", readyProjection.viewId)
                assertEquals(
                    listOf("electrical-runtime.render.wiring"),
                    readyProjection.activeRenderContributions.map { contribution -> contribution.contributionId },
                )

                val queriedSession = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(queriedSession)
                assertEquals("wiring", queriedSession.activeViewId)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session payload exposes documentation sheet navigation without redefining semantic identity`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-documentation-sheets-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val commandPayload = assertNotNull(
                    server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "documentation",
                    ),
                ).get(),
                )

                val session = assertNotNull(commandPayload.session)
                val readyProjection = assertNotNull(session.readyProjection)
                assertEquals("documentation", session.activeViewId)
                assertEquals("electrical/documentation", readyProjection.familyId)
                assertEquals("documentation/sheet/01-overview", readyProjection.activeSheetId)
                assertEquals(
                    listOf("documentation/sheet/01-overview", "documentation/sheet/02-reference"),
                    readyProjection.sheets.map { sheet -> sheet.sheetId },
                )
                assertEquals("documentation/sheet/02-reference", readyProjection.sheets.first().nextSheetId)
                assertEquals("documentation/sheet/01-overview", readyProjection.sheets.last().previousSheetId)
                assertTrue(readyProjection.sheets.first().subjectSemanticIds.contains("component:PLC1"))
                assertTrue(readyProjection.sheets.last().subjectSemanticIds.contains("component:PLC1"))
                assertEquals(2, readyProjection.components.count { component -> component.semanticId == "component:PLC1" })
                assertEquals(
                    2,
                    readyProjection.components
                        .filter { component -> component.semanticId == "component:PLC1" }
                        .map { component -> component.projectionId }
                        .distinct()
                        .size,
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
    fun `projection command request rejects unallowlisted command ids explicitly`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-command-rejected-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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

                val payload = server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "execute-plugin-command",
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("rejected", payload.status)
                assertEquals("execute-plugin-command", payload.commandId)
                assertTrue(payload.reason.orEmpty().contains("allowlist", ignoreCase = true))
                assertNull(payload.session)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session request surfaces unavailable state when canonical projection is invalid`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-unavailable-",
            sourceFileName = "broken-demo.athena",
            sourceText = "system Broken {",
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

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("unavailable", payload.status)
                assertNull(payload.readyProjection)
                assertTrue(payload.unavailableReason.orEmpty().isNotBlank())
                assertTrue(payload.diagnostics.isNotEmpty())
                assertEquals(
                    listOf("compiler.syntax"),
                    payload.diagnostics.map { diagnostic -> diagnostic.code }.distinct(),
                )
                assertTrue(
                    payload.diagnostics.all { diagnostic -> diagnostic.severity == "error" },
                )
                assertTrue(
                    payload.diagnostics.any { diagnostic ->
                        diagnostic.provenance.orEmpty().contains("broken-demo.athena")
                    },
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
    fun `projection session request follows the latest tracked dirty document state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-dirty-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val documentUri = sourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            demoCabinetSource,
                        ),
                    ),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(documentUri, 2),
                        listOf(
                            TextDocumentContentChangeEvent(
                                """
                                    system DemoCabinet {
                                      device PLC1 {
                                        type Switch
                                        model "S7-1200"
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
                            ),
                        ),
                    ),
                )

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("cabinet", payload.activeViewId)
                val readyProjection = assertNotNull(payload.readyProjection)
                assertEquals(2, readyProjection.components.size)
                assertEquals(0, readyProjection.connections.size)
                assertEquals(2, readyProjection.labels.size)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session request follows latest opened source file in governed repository`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-active-source-",
            sourceFileName = "01-baseline-sheet.athena",
            sourceText = demoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val layoutSource = """
            system LayoutAcceptance {
              device PLC2 {
                type Switch
              }

              device M2 {
                type Motor
              }

              port PLC2.out {
                direction out
                signal Digital
              }

              port M2.in {
                direction in
                signal Digital
              }

              connect PLC2.out -> M2.in
            }
        """.trimIndent()
        val layoutSourcePath = repository.sourceRoot.resolve("02-layout-intelligence-acceptance.athena")
        try {
            layoutSourcePath.writeText(layoutSource)
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val layoutDocumentUri = layoutSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            layoutDocumentUri,
                            "athena",
                            1,
                            layoutSource,
                        ),
                    ),
                )

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                val readyProjection = assertNotNull(payload.readyProjection)
                assertEquals("LayoutAcceptance", readyProjection.systemName)
                assertTrue(
                    readyProjection.components.any { component -> component.semanticId == "component:PLC2" },
                    "Projection should come from the latest opened source, not the repository seed source.",
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val demoCabinetSource = """
    system DemoCabinet {
      device PLC1 {
        type Switch
        model "S7-1200"
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

private val operatorProofSource = """
    system OperatorProof {
      device PLC1 {
        type Switch
        model "S7-1200"
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
