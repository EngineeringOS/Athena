package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticSeverity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticRelatedLocation
import com.engineeringood.athena.compiler.semantic.SourceUnitId
import com.engineeringood.athena.runtime.AthenaAuthoringPreviewSubmitted
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.runtime.AthenaAiDeterministicProofProvider
import com.engineeringood.athena.runtime.AthenaAiReasoningProvider
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticRelatedInformation
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.net.URI
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

/**
 * Minimal Athena LSP server for the governed M5 repository-open milestone.
 *
 * The server owns the runtime-backed repository session so Theia reaches semantics only through
 * the LSP boundary, not by importing kernel modules directly into the Node product shell.
 */
class AthenaLanguageServer(
    private val sessionHost: AthenaLspSessionHost = AthenaLspSessionHost(),
    private val aiReasoningProvider: AthenaAiReasoningProvider = AthenaAiDeterministicProofProvider(),
) : LanguageServer, LanguageClientAware {
    private var languageClient: LanguageClient? = null
    private var sessionSnapshot: AthenaLspSessionSnapshot? = null
    private var activeSession: AthenaLspSessionHostReady? = null
    private var languageFeatures: AthenaLanguageFeatures? = null

    private val textDocumentService = AthenaTextDocumentService(
        onDidOpen = { documentUri, text, version ->
            updateSnapshot(documentUri)
            publishDiagnostics(documentUri, text, version)
            languageClient?.logMessage(
                MessageParams(
                    MessageType.Info,
                    "Athena semantic path active: textDocument/didOpen -> runtime/compiler :: $documentUri",
                ),
            )
        },
        onDidChange = { documentUri, text, version ->
            publishDiagnostics(documentUri, text, version)
        },
        onDidClose = { documentUri ->
            languageFeatures?.closeDocument(documentUri)
            languageClient?.publishDiagnostics(
                PublishDiagnosticsParams().apply {
                    uri = documentUri
                    diagnostics = emptyList()
                },
            )
        },
        onCompletion = { params ->
            languageFeatures?.completion(params) ?: org.eclipse.lsp4j.CompletionList(false, emptyList())
        },
        onDocumentSymbols = { params ->
            languageFeatures?.documentSymbols(params).orEmpty()
        },
        onDefinition = { documentUri, position ->
            languageFeatures?.definition(documentUri, position).orEmpty()
        },
        onReferences = { params ->
            languageFeatures?.references(params).orEmpty()
        },
    )

    private val workspaceService = AthenaWorkspaceBridge()

    /**
     * Returns the current session snapshot for tests and transport adapters.
     */
    fun currentSessionSnapshot(): AthenaLspSessionSnapshot? = sessionSnapshot

    /**
     * Returns the current tracked document state for tests that verify repeated-edit continuity.
     */
    fun trackedDocument(uri: String): AthenaTrackedDocument? = languageFeatures?.trackedDocument(uri)

    override fun connect(client: LanguageClient) {
        languageClient = client
    }

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFuture.supplyAsync {
            val initializeStartedAt = System.nanoTime()
            sessionHost.shutdown()

            val repositoryRoot = resolveRepositoryRoot(params)
                ?: throw ResponseErrorException(
                    ResponseError(
                        ResponseErrorCode.InvalidParams,
                        "Athena LSP requires a repository root in rootUri, workspaceFolders, or initializationOptions.repositoryRoot.",
                        null,
                    ),
                )

            when (val activation = sessionHost.activateRepository(repositoryRoot)) {
                is AthenaLspSessionHostReady -> {
                    val languageFeatureInitializationMs: Long
                    activeSession = activation
                    languageFeatureInitializationMs = measureTimeMillis {
                        languageFeatures = AthenaLanguageFeatures(
                            compiler = activation.context.compiler(),
                            repositoryRoot = activation.repositoryRoot,
                            sourceRootPath = activation.sourceRootPath,
                        )
                    }
                    sessionSnapshot = AthenaLspSessionSnapshot(
                        repositoryRoot = activation.repositoryRoot,
                        manifestPath = activation.manifestPath,
                        lockPath = activation.lockPath,
                        sourceRootPath = activation.sourceRootPath,
                        sourcePath = activation.sourcePath,
                        projectName = activation.projectName,
                        primaryPackageName = activation.primaryPackageName,
                    )
                    val totalInitializationMs = (System.nanoTime() - initializeStartedAt) / 1_000_000.0
                    languageClient?.logMessage(
                        MessageParams(
                            MessageType.Info,
                            "Athena LSP initialize timings: total=${"%.1f".format(Locale.US, totalInitializationMs)}ms, " +
                                "languageFeatures=${languageFeatureInitializationMs}ms, repositoryRoot=${activation.repositoryRoot}",
                        ),
                    )

                    InitializeResult(
                        ServerCapabilities().apply {
                            textDocumentSync = Either.forRight(
                                TextDocumentSyncOptions().apply {
                                    openClose = true
                                    change = TextDocumentSyncKind.Full
                                },
                            )
                            completionProvider = org.eclipse.lsp4j.CompletionOptions(
                                /* resolveProvider = */ false,
                                listOf(".", " "),
                            )
                            documentSymbolProvider = Either.forLeft(true)
                            definitionProvider = Either.forLeft(true)
                            referencesProvider = Either.forLeft(true)
                            experimental = sessionSnapshot!!.toTransportPayload()
                        },
                    )
                }

                is AthenaLspSessionHostUnavailable -> {
                    throw ResponseErrorException(
                        ResponseError(
                            ResponseErrorCode.InvalidParams,
                            activation.reason,
                            null,
                        ),
                    )
                }
            }
        }
    }

    override fun shutdown(): CompletableFuture<Any> {
        sessionHost.shutdown()
        activeSession = null
        languageFeatures = null
        sessionSnapshot = null
        return CompletableFuture.completedFuture(Any())
    }

    override fun exit() {
        sessionHost.shutdown()
        activeSession = null
        languageFeatures = null
        sessionSnapshot = null
    }

    override fun getTextDocumentService(): TextDocumentService = textDocumentService

    override fun getWorkspaceService(): WorkspaceService = workspaceService

    /**
     * Returns a read-only semantic inspection snapshot for the latest Athena-owned tracked document state.
     */
    @JsonRequest("athena/semanticInspection")
    fun semanticInspection(params: AthenaSemanticInspectionParams): CompletableFuture<AthenaSemanticInspectionPayload?> {
        return CompletableFuture.completedFuture(
            languageFeatures?.semanticInspection(params.textDocument.uri),
        )
    }

    /**
     * Returns the runtime-owned source mutation evaluation for the latest tracked dirty document state.
     */
    @JsonRequest("athena/sourceMutationEvaluation")
    fun sourceMutationEvaluation(params: AthenaSourceMutationParams): CompletableFuture<AthenaSourceMutationPayload?> {
        val activation = activeSession
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        val trackedDocument = languageFeatures?.trackedDocument(params.textDocument.uri)
        if (activation == null) {
            return CompletableFuture.completedFuture(
                unavailableSourceMutationPayload(
                    projectName = sessionSnapshot?.projectName ?: inferredSourceMutationProjectName(params.textDocument.uri),
                    semanticPath = semanticPath,
                    uri = params.textDocument.uri,
                    version = trackedDocument?.version ?: 0,
                    reason = "Athena LSP session is inactive, so source mutation evaluation is unavailable for `${params.textDocument.uri}`.",
                ),
            )
        }

        val document = trackedDocument ?: return CompletableFuture.completedFuture(
                unavailableSourceMutationPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                    uri = params.textDocument.uri,
                    version = 0,
                    reason = "Athena LSP has no tracked dirty document for `${params.textDocument.uri}`.",
                ),
            )

        return CompletableFuture.completedFuture(
            activation.context.sourceMutationRuntime()
                .evaluate(
                    context = activation.context,
                    sourcePath = document.path,
                    compilation = document.compilation,
                )
                .toPayload(
                    uri = document.uri,
                    version = document.version,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns the current runtime-owned repository graph session state through the Athena LSP boundary.
     */
    @JsonRequest("athena/repositoryGraphSession")
    fun repositoryGraphSession(params: AthenaRepositoryGraphSessionParams): CompletableFuture<AthenaRepositoryGraphSessionPayload?> {
        @Suppress("UnusedParameter")
        val ignored = params
        return CompletableFuture.completedFuture(
            activeSession?.toRepositoryGraphSessionPayload(sessionSnapshot),
        )
    }

    /**
     * Returns the current runtime-owned component-knowledge session through the Athena LSP boundary.
     */
    @JsonRequest("athena/componentKnowledgeSession")
    fun componentKnowledgeSession(params: AthenaComponentKnowledgeSessionParams): CompletableFuture<AthenaComponentKnowledgeSessionPayload?> {
        @Suppress("UnusedParameter")
        val ignored = params
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.componentKnowledgeRuntime().inspect(activation.context).toPayload(semanticPath),
        )
    }

    /**
     * Returns the runtime-owned Semantic Macro catalog seam through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticMacroCatalog")
    fun semanticMacroCatalog(params: AthenaSemanticMacroCatalogParams): CompletableFuture<AthenaSemanticMacroCatalogPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.reuseRuntime()
                .catalog(
                    context = activation.context,
                    request = params.toRuntimeRequest(),
                )
                .toPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns the runtime-owned Semantic Macro validation seam through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticMacroValidation")
    fun semanticMacroValidation(params: AthenaSemanticMacroValidationParams): CompletableFuture<AthenaSemanticMacroValidationPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.reuseRuntime()
                .validate(
                    context = activation.context,
                    request = params.toRuntimeRequest(),
                )
                .toPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns the runtime-owned Semantic Macro preview seam through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticMacroPreview")
    fun semanticMacroPreview(params: AthenaSemanticMacroPreviewParams): CompletableFuture<AthenaSemanticMacroPreviewPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.reuseRuntime()
                .preview(
                    context = activation.context,
                    request = params.toRuntimeRequest(),
                )
                .toPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns the runtime-owned Semantic Macro acceptance seam through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticMacroAccept")
    fun semanticMacroAccept(params: AthenaSemanticMacroAcceptanceParams): CompletableFuture<AthenaSemanticMacroAcceptancePayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.reuseRuntime()
                .accept(
                    context = activation.context,
                    request = params.toRuntimeRequest(),
                )
                .toPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns the runtime-owned Semantic Macro origin-inspection seam through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticMacroOriginInspection")
    fun semanticMacroOriginInspection(params: AthenaSemanticMacroOriginInspectionParams): CompletableFuture<AthenaSemanticMacroOriginInspectionPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.reuseRuntime()
                .inspectOrigin(
                    context = activation.context,
                    request = params.toRuntimeRequest(),
                )
                .toPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                ),
        )
    }

    /**
     * Returns baseline-driven semantic review and commit-preparation state through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticScmState")
    fun semanticScmState(params: AthenaSemanticScmStateParams): CompletableFuture<AthenaSemanticScmStatePayload?> {
        val activation = activeSession
        return CompletableFuture.completedFuture(
            activation?.context
                ?.services
                ?.semanticScmStates()
                ?.inspect(
                    session = activation.session,
                    descriptor = params.toBaselineDescriptor(),
                    locator = params.toBaselineLocator(),
                )
                ?.toPayload(sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"),
        )
    }

    /**
     * Returns package-aware semantic history through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticHistoryState")
    fun semanticHistoryState(params: AthenaSemanticHistoryStateParams): CompletableFuture<AthenaSemanticHistoryStatePayload?> {
        val activation = activeSession
        return CompletableFuture.completedFuture(
            activation?.context
                ?.services
                ?.semanticHistoryStates()
                ?.inspect(
                    session = activation.session,
                    packageId = params.toPackageId(),
                    baselineRequests = params.toBaselineRequests(),
                )
                ?.toPayload(sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"),
        )
    }

    /**
     * Returns the current runtime-owned projection session through the Athena LSP boundary.
     */
    @JsonRequest("athena/projectionSession")
    fun projectionSession(params: AthenaProjectionSessionParams): CompletableFuture<AthenaProjectionSessionPayload?> {
        @Suppress("UnusedParameter")
        val ignored = params
        return CompletableFuture.completedFuture(
            activeSession?.toProjectionSessionPayload(
                snapshot = sessionSnapshot,
                languageFeatures = languageFeatures,
            ),
        )
    }

    /**
     * Executes one explicit allowlisted projection command through the Athena LSP boundary.
     */
    @JsonRequest("athena/projectionCommand")
    fun projectionCommand(params: AthenaProjectionCommandParams): CompletableFuture<AthenaProjectionCommandPayload?> {
        val activation = activeSession
        return CompletableFuture.completedFuture(
            activation?.executeProjectionCommand(
                params = params,
                snapshot = sessionSnapshot,
                languageFeatures = languageFeatures,
            ),
        )
    }

    /**
     * Returns the runtime-owned graph command-intent evaluation for one graph-originated action.
     */
    @JsonRequest("athena/graphCommandIntent")
    fun graphCommandIntent(params: AthenaGraphCommandIntentParams): CompletableFuture<AthenaGraphCommandIntentPayload?> {
        val activation = activeSession
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        if (activation == null) {
            return CompletableFuture.completedFuture(
                unavailableGraphCommandIntentPayload(
                    projectName = sessionSnapshot?.projectName ?: "Athena",
                    semanticPath = semanticPath,
                    params = params,
                    reason = "Athena LSP session is inactive, so graph command intent is unavailable for `${params.viewId}`.",
                ),
            )
        }

        val intent = params.toRuntimeIntent()
            ?: return CompletableFuture.completedFuture(
                AthenaGraphCommandIntentPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                    status = "rejected",
                    intentId = params.intentId,
                    mutationCategory = params.defaultMutationCategory().name.lowercase().replace('_', '-'),
                    viewId = params.viewId,
                    source = params.source,
                    target = params.target,
                    requestedPlacement = params.requestedPlacement,
                    reason = "Graph command intent `${params.intentId}` is invalid or missing required typed arguments.",
                ),
            )

        val trackedDocument = sessionSnapshot
            ?.sourcePath
            ?.let { sourcePath -> languageFeatures?.trackedDocumentByPath(sourcePath) }
        val preflightSourceEdit = if (params.authoredLayoutIntent != null) {
            if (trackedDocument == null) {
                return CompletableFuture.completedFuture(
                    AthenaGraphCommandIntentPayload(
                        projectName = activation.context.project.name,
                        semanticPath = semanticPath,
                        status = "rejected",
                        intentId = params.intentId,
                        mutationCategory = params.defaultMutationCategory().name.lowercase().replace('_', '-'),
                        viewId = params.viewId,
                        source = params.source,
                        target = params.target,
                        requestedPlacement = params.requestedPlacement,
                        reason = "Accepted authored layout commands require an active source document to produce a durable source edit.",
                    ),
                )
            }
            val revisionGuard = trackedDocument.toAuthoringRevisionGuard()
            val authoredIntent = params.authoredLayoutIntent.toBackendIntent(trackedDocument.path.fileName.toString())
            val document = trackedDocument.toBackendSourceDocument(revisionGuard)
            val plannedSourceEdit = if (authoredIntent != null && document != null) {
                (com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanner().plan(
                    com.engineeringood.athena.compiler.BackendAuthoredLayoutPlanningRequest(
                        document = document,
                        revisionGuard = revisionGuard,
                        subjectSemanticId = params.target.semanticId,
                        intent = authoredIntent,
                    ),
                ) as? com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanned)
                    ?.plan
                    ?.toPayload(trackedDocument.text)
            } else {
                null
            }
            plannedSourceEdit ?: return CompletableFuture.completedFuture(
                AthenaGraphCommandIntentPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                    status = "rejected",
                    intentId = params.intentId,
                    mutationCategory = params.defaultMutationCategory().name.lowercase().replace('_', '-'),
                    viewId = params.viewId,
                    source = params.source,
                    target = params.target,
                    requestedPlacement = params.requestedPlacement,
                    reason = "Backend source edit planning failed for authored layout command; no durable source edit was produced.",
                ),
            )
        } else {
            null
        }
        val runtimeResult = activation.context.graphCommandIntentRuntime()
            .submit(
                context = activation.context,
                intent = intent,
            )
        val payload = runtimeResult.toPayload(semanticPath = semanticPath)
        val sourceEdit = if (payload.status == "accepted") preflightSourceEdit else null
        return CompletableFuture.completedFuture(payload.copy(sourceEdit = sourceEdit))
    }

    /**
     * Routes one guided authoring intent through Athena LSP into runtime-owned preview state.
     */
    @JsonRequest("athena/authoringPreview")
    fun authoringPreview(params: AthenaAuthoringPreviewParams): CompletableFuture<AthenaAuthoringPreviewSubmissionPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        val trackedDocument = sessionSnapshot
            ?.sourcePath
            ?.let { sourcePath -> languageFeatures?.trackedDocumentByPath(sourcePath) }
        val revisionGuard = trackedDocument?.toAuthoringRevisionGuard() ?: run {
            val sourcePath = activation.context.project.sourcePath
            AuthoringRevisionGuard.from(
                semanticSnapshotId = "project:${activation.context.project.name}",
                sourceUri = sourcePath.toUri().toString(),
                documentVersion = 0,
                sourceText = Files.readString(sourcePath),
            )
        }
        val runtimeIntent = runCatching { params.toRuntimeIntent(revisionGuard) }.getOrElse { failure ->
            return CompletableFuture.completedFuture(
                params.toInvalidSubmissionPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                    reason = failure.message ?: "Athena authoring request is malformed.",
                ),
            )
        }
        val governedIntent = (
            runtimeIntent is com.engineeringood.athena.authoring.CreateSemanticEntityIntent &&
                com.engineeringood.athena.domain.electricalruntime.ElectricalEntityCreationProjectionAuthority
                    .supports(runtimeIntent.conceptTemplateId.value)
            ) || runtimeIntent is com.engineeringood.athena.authoring.SemanticRelationshipIntent
        val capabilityDiscovery = if (governedIntent && trackedDocument != null) {
            discoverGovernedAuthoringCapability(trackedDocument, runtimeIntent)
        } else {
            null
        }
        val capabilityEvidence = capabilityDiscovery?.evidence?.singleOrNull()
        val previewFactory: ((com.engineeringood.athena.authoring.AuthoringPreviewId) ->
            com.engineeringood.athena.authoring.AuthoringPreview)? =
            if (governedIntent && capabilityEvidence == null) {
                { previewId: com.engineeringood.athena.authoring.AuthoringPreviewId ->
                    capabilityBlockedPreview(previewId, runtimeIntent, revisionGuard, capabilityDiscovery)
                }
            } else {
                null
            }
        val governedPreviewFactory: ((com.engineeringood.athena.authoring.AuthoringPreviewId) ->
            com.engineeringood.athena.runtime.AthenaGovernedAuthoringPreviewContext)? =
            if (runtimeIntent is com.engineeringood.athena.authoring.CreateSemanticEntityIntent &&
                trackedDocument != null && capabilityEvidence != null
            ) {
                { previewId: com.engineeringood.athena.authoring.AuthoringPreviewId ->
                    requireNotNull(
                        governedCreateEntityPreview(
                            trackedDocument = trackedDocument,
                            intent = runtimeIntent,
                            previewId = previewId,
                            capabilityEvidence = capabilityEvidence,
                        ),
                    ).toSessionContext()
                }
            } else if (runtimeIntent is com.engineeringood.athena.authoring.SemanticRelationshipIntent &&
                trackedDocument != null && capabilityEvidence != null
            ) {
                { previewId: com.engineeringood.athena.authoring.AuthoringPreviewId ->
                    requireNotNull(governedSemanticRelationshipPreview(
                        trackedDocument = trackedDocument,
                        intent = runtimeIntent,
                        previewId = previewId,
                        provenance = com.engineeringood.athena.authoring.AuthoringTransactionProvenance(
                            actor = params.actor?.takeIf(String::isNotBlank) ?: "user:authoring",
                            origin = runtimeIntent.origin,
                            reason = runtimeIntent.provenance,
                        ),
                        capabilityEvidence = capabilityEvidence,
                    )).toSessionContext()
                }
            } else {
                null
            }
        val result = activation.context.authoringSessions()
            .submit(
                context = activation.context,
                intent = runtimeIntent,
                previewFactory = previewFactory,
                governedPreviewFactory = governedPreviewFactory,
            )
            .let { submission -> submission as AthenaAuthoringPreviewSubmitted }
        val componentKnowledge = activation.context.componentKnowledgeRuntime()
            .inspect(activation.context) as? com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady
        val sourceImpact = trackedDocument?.let { currentTrackedDocument ->
            result.record.preview.relationshipEvidence?.sourceEdit?.toPayload(currentTrackedDocument.text)
                ?: previewCreateEntitySourceEdit(
                trackedDocument = currentTrackedDocument,
                record = result.record,
                componentKnowledge = componentKnowledge,
            )
        }
        return CompletableFuture.completedFuture(
            result.toPayload(
                projectName = activation.context.project.name,
                semanticPath = semanticPath,
                sourceImpact = sourceImpact,
            ),
        )
    }

    /**
     * Returns the current runtime-owned guided authoring state through the Athena LSP boundary.
     */
    @JsonRequest("athena/authoringState")
    fun authoringState(params: AthenaAuthoringStateParams): CompletableFuture<AthenaAuthoringStatePayload?> {
        @Suppress("UnusedParameter")
        val ignored = params
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.authoringStatePayload(semanticPath),
        )
    }

    /**
     * Applies one explicit guided authoring preview decision through the Athena LSP boundary.
     */
    @JsonRequest("athena/authoringDecision")
    fun authoringDecision(params: AthenaAuthoringDecisionParams): CompletableFuture<AthenaAuthoringPreviewDecisionPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        val decision = runCatching { params.toRuntimeDecision() }.getOrElse { failure ->
            return CompletableFuture.completedFuture(
                AthenaAuthoringPreviewDecisionPayload(
                    projectName = activation.context.project.name,
                    semanticPath = semanticPath,
                    status = "unavailable",
                    reason = failure.message ?: "Athena authoring decision is malformed.",
                ),
            )
        }
        val trackedDocument = sessionSnapshot
            ?.sourcePath
            ?.let { sourcePath -> languageFeatures?.trackedDocumentByPath(sourcePath) }
        val storedRecord = activation.context.authoringSessions()
            .state(activation.context)
            .records
            .firstOrNull { record -> record.preview.previewId.value == params.previewId }
        val governedAuthorities = storedRecord?.governedContext?.let { governedContext ->
            trackedDocument?.let { currentTrackedDocument ->
                governedDecisionAuthorities(
                    trackedDocument = currentTrackedDocument,
                    compiler = activation.context.compiler(),
                    governedContext = governedContext,
                    sourceMutationAuthority = { sourceEdit, proposedSource ->
                        applyAuthoringWorkspaceMutation(
                            client = languageClient,
                            trackedDocument = currentTrackedDocument,
                            sourceEdit = sourceEdit,
                            proposedSource = proposedSource,
                        )
                    },
                    onSourceMutated = { proposedSource ->
                        languageFeatures?.trackDocument(
                            uri = currentTrackedDocument.uri,
                            path = currentTrackedDocument.path,
                            version = currentTrackedDocument.version + 1,
                            text = proposedSource,
                        )
                    },
                )
            }
        }
        val result = activation.context.authoringSessions()
            .applyDecision(
                context = activation.context,
                decision = decision,
                governedAuthorities = governedAuthorities,
            )
        val componentKnowledge = activation.context.componentKnowledgeRuntime()
            .inspect(activation.context) as? com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady
        val sourceEdit = when (result) {
            is com.engineeringood.athena.runtime.AthenaAuthoringPreviewDecisionUpdated -> {
                val governedLifecycle = result.transaction?.lifecycleState
                if (governedLifecycle != null && governedLifecycle !in setOf(
                        com.engineeringood.athena.authoring.AuthoringLifecycleState.REPROJECTED,
                        com.engineeringood.athena.authoring.AuthoringLifecycleState.PROJECTION_FAILED,
                    )
                ) {
                    null
                } else {
                val currentTrackedDocument = trackedDocument ?: return CompletableFuture.completedFuture(
                    result.toPayload(
                        projectName = activation.context.project.name,
                        semanticPath = semanticPath,
                    ),
                )
                result.record.governedContext?.sourceEditPlan
                    ?.toPayload(currentTrackedDocument.text)
                    ?.copy(appliedByAuthority = true)
                    ?: when (result.record.intent) {
                    is com.engineeringood.athena.authoring.CreateSemanticEntityIntent -> acceptedCreateEntitySourceEdit(
                        trackedDocument = currentTrackedDocument,
                        record = result.record,
                        componentKnowledge = componentKnowledge,
                    )

                    is com.engineeringood.athena.authoring.UpdateSemanticEntityPropertiesIntent -> acceptedUpdateSemanticEntityPropertiesSourceEdit(
                        trackedDocument = currentTrackedDocument,
                        record = result.record,
                        componentKnowledge = componentKnowledge,
                    )

                    is com.engineeringood.athena.authoring.SemanticRelationshipIntent -> acceptedSemanticRelationshipSourceEdit(
                        trackedDocument = currentTrackedDocument,
                        record = result.record,
                    )

                    is com.engineeringood.athena.authoring.RemoveSemanticRelationshipIntent -> null

                    else -> null
                    }
                }
            }

            else -> null
        }
        return CompletableFuture.completedFuture(
            result.toPayload(
                projectName = activation.context.project.name,
                semanticPath = semanticPath,
                sourceEdit = sourceEdit,
            ),
        )
    }

    /**
     * Routes one governed AI reasoning request through Athena LSP into runtime-owned reasoning sessions.
     */
    @JsonRequest("athena/aiReasoning")
    fun aiReasoning(params: AthenaAiReasoningRequestParams): CompletableFuture<AthenaAiReasoningSubmissionPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.context.aiReasoningSessions()
                .submit(
                    context = activation.context,
                    request = params.toRuntimeRequest(activation),
                    provider = aiReasoningProvider,
                )
                .let { result -> result as com.engineeringood.athena.runtime.AthenaAiReasoningSessionSubmitted }
                .toPayload(semanticPath = semanticPath),
        )
    }

    /**
     * Returns stored runtime-owned reasoning sessions and proposals through the Athena LSP boundary.
     */
    @JsonRequest("athena/aiReasoningState")
    fun aiReasoningState(): CompletableFuture<AthenaAiReasoningStatePayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        val semanticPath = sessionSnapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler"
        return CompletableFuture.completedFuture(
            activation.reasoningStatePayload(semanticPath),
        )
    }

    /**
     * Applies one explicit reasoning proposal decision through the Athena LSP boundary.
     */
    @JsonRequest("athena/aiReasoningDecision")
    fun aiReasoningDecision(params: AthenaAiReasoningDecisionParams): CompletableFuture<AthenaAiReasoningProposalPayload?> {
        val activation = activeSession ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.completedFuture(
            params.applyTo(activation).toPayload(),
        )
    }

    @Suppress("DEPRECATION")
    private fun resolveRepositoryRoot(params: InitializeParams): Path? {
        val workspaceUri = params.workspaceFolders
            ?.firstOrNull()
            ?.uri
            ?.toRepositoryRootPath()
        if (workspaceUri != null) {
            return workspaceUri
        }

        val rootUri = params.rootUri?.toRepositoryRootPath()
        if (rootUri != null) {
            return rootUri
        }

        val initializationOptions = params.initializationOptions as? Map<*, *>
        val repositoryRoot = initializationOptions
            ?.get("repositoryRoot")
            ?.toString()
            ?.takeIf { it.isNotBlank() }
        return repositoryRoot?.let(Path::of)
    }

    private fun inferredSourceMutationProjectName(documentUri: String): String {
        return runCatching {
            Paths.get(URI(documentUri)).fileName.toString().substringBeforeLast('.')
        }.getOrDefault("unknown")
    }

    private fun updateSnapshot(documentUri: String) {
        sessionSnapshot = sessionSnapshot?.copy(lastOpenedDocumentUri = documentUri)
    }

    /**
     * M17 semantic-authority guardrail (AD-108 / AD-107).
     *
     * This is the single diagnostics-publishing path for the Athena IDE. Every published
     * [Diagnostic] must originate from compiler-owned parsing and later compiler/runtime stages,
     * threaded through `languageFeatures.trackDocument(...) -> CompilerCompilationResult ->
     * toLspDiagnostics()`. `trackDocument` is the only place a document's compiled state is produced.
     *
     * When Epic 2 swaps the parser implementation to ANTLR4 and Epic 3 adds Tree-sitter for editor
     * syntax UX, this contract must not change: Tree-sitter trees, queries, or CST nodes must never
     * be read to build a `Diagnostic`. Diagnostics stay derived exclusively from
     * `CompilerCompilationResult` (`CompilerCompilationParseFailure.diagnostics`, or
     * `CompilerCompilationSuccess.semanticResult.diagnostics` plus
     * `validationBreakdown.engineeringSufficiencyDiagnostics`). `AthenaSemanticAuthorityBoundaryTest`
     * mechanically enforces this boundary.
     */
    private fun publishDiagnostics(
        documentUri: String,
        documentText: String,
        version: Int,
    ) {
        val activation = activeSession ?: return
        val features = languageFeatures ?: return
        val documentPath = documentUri.toDocumentPath() ?: activation.sourcePath
        val trackedDocument = features.trackDocument(
            uri = documentUri,
            path = documentPath,
            version = version,
            text = documentText,
        )
        val diagnostics = trackedDocument.compilation.toLspDiagnostics() +
            trackedDocument.projectSemanticDiagnostics.toLspDiagnostics(
                documentUri = documentUri,
                currentSourceUnitId = trackedDocument.projectSemanticSourceUnitId,
                sourceUnitUris = trackedDocument.projectSemanticSourceUnitUris,
            )
        languageClient?.publishDiagnostics(
            PublishDiagnosticsParams().apply {
                uri = documentUri
                this.version = trackedDocument.version
                this.diagnostics = diagnostics
            },
        )
        languageClient?.logMessage(
            MessageParams(
                MessageType.Info,
                "Athena diagnostics published from JVM stack: ${diagnostics.size} item(s) for $documentUri",
            ),
        )
    }
}

/**
 * Transport-safe snapshot of the active repository session owned by Athena LSP.
 */
data class AthenaLspSessionSnapshot(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val sourceRootPath: Path,
    val sourcePath: Path,
    val projectName: String,
    val primaryPackageName: String,
    val semanticPath: String = "frontend -> LSP -> runtime/compiler",
    val lastOpenedDocumentUri: String? = null,
) {
    /**
     * Renders the snapshot as a plain payload for `InitializeResult.capabilities.experimental`.
     */
    fun toTransportPayload(): Map<String, String> {
        val payload = linkedMapOf(
            "repositoryRoot" to repositoryRoot.toString(),
            "manifestPath" to manifestPath.toString(),
            "lockPath" to lockPath.toString(),
            "sourceRootPath" to sourceRootPath.toString(),
            "sourcePath" to sourcePath.toString(),
            "projectName" to projectName,
            "primaryPackageName" to primaryPackageName,
            "semanticPath" to semanticPath,
        )
        lastOpenedDocumentUri?.let { payload["lastOpenedDocumentUri"] = it }
        return payload
    }
}

/**
 * Handles the small M4 text-document surface needed to prove the semantic boundary.
 */
class AthenaTextDocumentService(
    private val onDidOpen: (documentUri: String, text: String, version: Int) -> Unit,
    private val onDidChange: (documentUri: String, text: String, version: Int) -> Unit,
    private val onDidClose: (documentUri: String) -> Unit,
    private val onCompletion: (params: org.eclipse.lsp4j.CompletionParams) -> org.eclipse.lsp4j.CompletionList = {
        org.eclipse.lsp4j.CompletionList(false, emptyList())
    },
    private val onDocumentSymbols: (params: org.eclipse.lsp4j.DocumentSymbolParams) -> List<Either<org.eclipse.lsp4j.SymbolInformation, org.eclipse.lsp4j.DocumentSymbol>> = {
        emptyList()
    },
    private val onDefinition: (documentUri: String, position: Position) -> List<Location> = { _, _ ->
        emptyList()
    },
    private val onReferences: (params: ReferenceParams) -> List<Location> = {
        emptyList()
    },
) : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams) {
        onDidOpen(
            params.textDocument.uri,
            params.textDocument.text,
            params.textDocument.version,
        )
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val latestChange = params.contentChanges.lastOrNull() ?: return
        onDidChange(
            params.textDocument.uri,
            latestChange.text,
            params.textDocument.version,
        )
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        onDidClose(params.textDocument.uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams) = Unit

    override fun completion(
        params: org.eclipse.lsp4j.CompletionParams,
    ): CompletableFuture<Either<MutableList<CompletionItem>, org.eclipse.lsp4j.CompletionList>> {
        return CompletableFuture.completedFuture(
            Either.forRight(onCompletion(params)),
        )
    }

    override fun documentSymbol(
        params: org.eclipse.lsp4j.DocumentSymbolParams,
    ): CompletableFuture<MutableList<Either<org.eclipse.lsp4j.SymbolInformation, org.eclipse.lsp4j.DocumentSymbol>>> {
        return CompletableFuture.completedFuture(onDocumentSymbols(params).toMutableList())
    }

    override fun definition(
        params: org.eclipse.lsp4j.DefinitionParams,
    ): CompletableFuture<Either<MutableList<out Location>, MutableList<out org.eclipse.lsp4j.LocationLink>>> {
        return CompletableFuture.completedFuture(
            Either.forLeft(
                onDefinition(params.textDocument.uri, params.position).toMutableList(),
            ),
        )
    }

    override fun references(params: ReferenceParams): CompletableFuture<MutableList<out Location>> {
        return CompletableFuture.completedFuture(onReferences(params).toMutableList())
    }
}

/**
 * Owns the minimal M4 workspace hooks required by the LSP contract.
 */
class AthenaWorkspaceBridge : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams) = Unit

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) = Unit
}

private fun String.toRepositoryRootPath(): Path? {
    return runCatching {
        if (startsWith("file:", ignoreCase = true)) {
            Path.of(URI.create(this))
        } else {
            Path.of(this)
        }
    }.getOrNull()
}

private fun String.toDocumentPath(): Path? {
    return runCatching {
        if (startsWith("file:", ignoreCase = true)) {
            Paths.get(URI.create(this))
        } else {
            Path.of(this)
        }
    }.getOrNull()
}

/**
 * Converts one compiler-owned [CompilerCompilationResult] into published LSP diagnostics.
 *
 * M17 semantic-authority guardrail (AD-108 / AD-107): this function may only ever pattern-match on
 * the compiler-owned result cases and read `CompilerSyntaxDiagnostic` / `SemanticDiagnostic` (from
 * `com.engineeringood.athena.compiler` and `com.engineeringood.athena.semantics.core`). It must never
 * be changed to accept, read, or merge a Tree-sitter tree/query result (Epic 3) or any ANTLR4
 * parse-tree/visitor type (Epic 2) as a diagnostics source. Tree-sitter owns syntax UX only and must
 * never become a second semantic-truth source.
 */
private fun CompilerCompilationResult.toLspDiagnostics(): List<Diagnostic> {
    return when (this) {
        is CompilerCompilationParseFailure -> diagnostics.map { diagnostic -> diagnostic.toLspDiagnostic() }
        is CompilerCompilationSuccess -> (
            semanticResult.diagnostics +
                validationBreakdown.engineeringSufficiencyDiagnostics
            ).distinct().map { diagnostic -> diagnostic.toLspDiagnostic() }
    }
}

private fun CompilerSyntaxDiagnostic.toLspDiagnostic(): Diagnostic {
    return Diagnostic().apply {
        severity = DiagnosticSeverity.Error
        source = "Athena syntax"
        code = Either.forLeft("syntax")
        message = this@toLspDiagnostic.message
        range = Range(
            Position((line - 1).coerceAtLeast(0), (column - 1).coerceAtLeast(0)),
            Position((endLine - 1).coerceAtLeast(0), (endColumn - 1).coerceAtLeast((column - 1).coerceAtLeast(0))),
        )
    }
}

private fun SemanticDiagnostic.toLspDiagnostic(): Diagnostic {
    return Diagnostic().apply {
        severity = when (this@toLspDiagnostic.severity) {
            SemanticDiagnosticSeverity.ERROR -> DiagnosticSeverity.Error
            SemanticDiagnosticSeverity.WARNING -> DiagnosticSeverity.Warning
        }
        source = if (this@toLspDiagnostic.category == SemanticDiagnosticCategory.KNOWLEDGE) {
            "Athena knowledge"
        } else {
            "Athena semantic"
        }
        code = Either.forLeft(ruleId.value)
        message = this@toLspDiagnostic.message
        range = Range(
            Position((provenance.startLine - 1).coerceAtLeast(0), (provenance.startColumn - 1).coerceAtLeast(0)),
            Position((provenance.endLine - 1).coerceAtLeast(0), (provenance.endColumn - 1).coerceAtLeast(0)),
        )
    }
}

internal fun List<ProjectSemanticDiagnostic>.toLspDiagnostics(
    documentUri: String,
    currentSourceUnitId: SourceUnitId?,
    sourceUnitUris: Map<SourceUnitId, String>,
): List<Diagnostic> {
    return mapNotNull { diagnostic ->
        diagnostic.toLspDiagnostic(
            documentUri = documentUri,
            currentSourceUnitId = currentSourceUnitId,
            sourceUnitUris = sourceUnitUris,
        )
    }.distinctBy { diagnostic ->
        listOf(
            diagnostic.code?.left,
            diagnostic.message,
            diagnostic.range.start.line,
            diagnostic.range.start.character,
            diagnostic.range.end.line,
            diagnostic.range.end.character,
        )
    }
}

private fun ProjectSemanticDiagnostic.toLspDiagnostic(
    documentUri: String,
    currentSourceUnitId: SourceUnitId?,
    sourceUnitUris: Map<SourceUnitId, String>,
): Diagnostic? {
    val diagnosticSourceUnitId = sourceUnitId
    if (diagnosticSourceUnitId != null && diagnosticSourceUnitId != currentSourceUnitId) {
        return null
    }
    return Diagnostic().apply {
        severity = when (this@toLspDiagnostic.severity) {
            ProjectSemanticDiagnosticSeverity.ERROR -> DiagnosticSeverity.Error
            ProjectSemanticDiagnosticSeverity.WARNING -> DiagnosticSeverity.Warning
            ProjectSemanticDiagnosticSeverity.INFO -> DiagnosticSeverity.Information
        }
        source = "Athena package semantic"
        code = Either.forLeft(this@toLspDiagnostic.code.value)
        message = this@toLspDiagnostic.message
        range = sourceSpan?.toLspRange() ?: Range(Position(0, 0), Position(0, 0))
        relatedInformation = this@toLspDiagnostic.relatedLocations
            .mapNotNull { relatedLocation ->
                relatedLocation.toLspRelatedInformation(
                    documentUri = documentUri,
                    currentSourceUnitId = currentSourceUnitId,
                    sourceUnitUris = sourceUnitUris,
                )
            }
    }
}

private fun ProjectSemanticRelatedLocation.toLspRelatedInformation(
    documentUri: String,
    currentSourceUnitId: SourceUnitId?,
    sourceUnitUris: Map<SourceUnitId, String>,
): DiagnosticRelatedInformation? {
    val uri = sourceUnitUris[sourceUnitId]
        ?: documentUri.takeIf { sourceUnitId == currentSourceUnitId }
        ?: return null
    return DiagnosticRelatedInformation(
        Location(uri, sourceSpan.toLspRange()),
        message ?: "Related Athena package semantic location",
    )
}

private fun SourceSpan.toLspRange(): Range {
    return Range(
        Position((start.line - 1).coerceAtLeast(0), (start.column - 1).coerceAtLeast(0)),
        Position((end.line - 1).coerceAtLeast(0), (end.column - 1).coerceAtLeast(0)),
    )
}
