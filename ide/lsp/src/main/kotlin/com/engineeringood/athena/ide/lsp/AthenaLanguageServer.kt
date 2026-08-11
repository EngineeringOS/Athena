package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticSeverity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticRelatedLocation
import com.engineeringood.athena.compiler.semantic.SourceUnitId
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SheetCompanionParseFailure
import com.engineeringood.athena.language.SheetCompanionParseResult
import com.engineeringood.athena.language.SyntaxDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
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
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextEdit
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
import java.nio.file.Paths
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

/**
 * Minimal Athena LSP server for the governed M5 repository-open milestone.
 *
 * The server owns the runtime-backed repository session so Theia reaches semantics only through
 * the LSP boundary, not by importing kernel modules directly into the Node product shell.
 */
class AthenaLanguageServer(
    private val sessionHost: AthenaLspSessionHost = AthenaLspSessionHost(),
) : LanguageServer, LanguageClientAware {
    private var languageClient: LanguageClient? = null
    private var sessionSnapshot: AthenaLspSessionSnapshot? = null
    private var activeSession: AthenaLspSessionHostReady? = null
    private var editOperationService: EditOperationService? = null
    private var diagramPublicationService: AthenaDiagramPublicationService? = null
    private var connectionReadModelService: ConnectionReadModelService? = null
    private var languageFeatures: AthenaLanguageFeatures? = null
    private val openDocumentUris = ConcurrentHashMap.newKeySet<String>()

    private val textDocumentService = AthenaTextDocumentService(
        onDidOpen = { documentUri, text, version ->
            openDocumentUris += documentUri
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
            openDocumentUris -= documentUri
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
        onFormatting = { params ->
            languageFeatures?.formatting(params.textDocument.uri).orEmpty()
        },
        onSemanticTokens = { params ->
            languageFeatures?.semanticTokens(params.textDocument.uri) ?: SemanticTokens(emptyList())
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
            openDocumentUris.clear()

            val repositoryRoot = resolveRepositoryRoot(params)
                ?: throw ResponseErrorException(
                    ResponseError(
                        ResponseErrorCode.InvalidParams,
                        "Athena LSP requires a repository root in workspaceFolders or initializationOptions.repositoryRoot.",
                        null,
                    ),
                )

            when (val activation = sessionHost.activateRepository(repositoryRoot)) {
                is AthenaLspSessionHostReady -> {
                    val languageFeatureInitializationMs: Long
                    activeSession = activation
                    diagramPublicationService = AthenaDiagramPublicationService(activation)
                    connectionReadModelService = ConnectionReadModelService(activation.repositoryRoot)
                    editOperationService = EditOperationService(activation, requireNotNull(diagramPublicationService))
                    languageFeatureInitializationMs = measureTimeMillis {
                        languageFeatures = AthenaLanguageFeatures(
                            compiler = activation.executionContext.compiler(),
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
                            documentFormattingProvider = Either.forLeft(true)
                            semanticTokensProvider = SemanticTokensWithRegistrationOptions(
                                SemanticTokensLegend(athenaSemanticTokenTypes, emptyList()),
                                true,
                            )
                            experimental = sessionSnapshot!!.toTransportPayload()
                        },
                    )
                }

                is AthenaLspSessionHostUnavailable -> {
                    publishRepositoryContractDiagnostics(
                        repositoryRoot = activation.repositoryRoot,
                        diagnostics = activation.diagnostics,
                    )
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

    private fun publishRepositoryContractDiagnostics(
        repositoryRoot: Path,
        diagnostics: List<RepositoryDiagnostic>,
    ) {
        diagnostics
            .filter { diagnostic -> diagnostic.sourcePath != null }
            .groupBy { diagnostic -> repositoryRoot.resolve(diagnostic.sourcePath!!).normalize().toUri().toString() }
            .forEach { (uri, groupedDiagnostics) ->
                languageClient?.publishDiagnostics(
                    PublishDiagnosticsParams().apply {
                        this.uri = uri
                        this.diagnostics = groupedDiagnostics.map { diagnostic -> diagnostic.toLspDiagnostic() }
                    },
                )
            }
    }

    override fun shutdown(): CompletableFuture<Any> {
        sessionHost.shutdown()
        openDocumentUris.clear()
        activeSession = null
        editOperationService = null
        diagramPublicationService = null
        connectionReadModelService = null
        languageFeatures = null
        sessionSnapshot = null
        return CompletableFuture.completedFuture(Any())
    }

    override fun exit() {
        sessionHost.shutdown()
        openDocumentUris.clear()
        activeSession = null
        editOperationService = null
        diagramPublicationService = null
        connectionReadModelService = null
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

    /** Publishes accepted semantic connectivity independently from its drawing projections. */
    @JsonRequest("athena/connectionReadModel")
    fun connectionReadModel(params: AthenaConnectionReadModelParams): CompletableFuture<Map<String, Any?>?> =
        CompletableFuture.completedFuture(connectionReadModelDomain(params)?.toWirePayload())

    internal fun connectionReadModelDomain(params: AthenaConnectionReadModelParams): ConnectionReadModelPublication? {
        val session = activeSession ?: return null
        val tracked = params.textDocument
            ?.uri
            ?.let { uri -> languageFeatures?.trackedDocument(uri) }
            ?.takeIf { document -> document.path.toAbsolutePath().normalize() == session.sourcePath.toAbsolutePath().normalize() }
        val revision = SourceRevisionService(session)
            .attempted(tracked?.text?.toByteArray(Charsets.UTF_8))
            .sceneInputRevision
        val compilation = tracked?.compilation ?: session.executionContext.compiler().compile(session.sourcePath)
        return connectionReadModelService?.publish(revision, compilation)
    }

    /**
     * Returns baseline-driven semantic review and commit-preparation state through the Athena LSP boundary.
     */
    @JsonRequest("athena/semanticScmState")
    fun semanticScmState(params: AthenaSemanticScmStateParams): CompletableFuture<AthenaSemanticScmStatePayload?> {
        val activation = activeSession
        return CompletableFuture.completedFuture(
            activation?.executionContext
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
            activation?.executionContext
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

    /** Returns one closed M43 scene publication. */
    @JsonRequest("athena/diagramScene")
    fun diagramScene(params: AthenaDiagramSceneParams): CompletableFuture<Map<String, Any?>?> {
        @Suppress("UnusedParameter")
        val ignored = params
        return CompletableFuture.completedFuture(diagramPublicationService?.current()?.toDiagramScenePayload())
    }

    @JsonRequest("athena/presentationEditContext")
    fun presentationEditContext(params: AthenaDiagramSceneParams): CompletableFuture<Map<String, Any?>?> {
        @Suppress("UnusedParameter")
        val ignored = params
        return CompletableFuture.completedFuture(activeSession?.presentationEditContext()?.toWirePayload())
    }

    internal fun presentationEditContextDomain(): AthenaPresentationEditContextPayload? = activeSession?.presentationEditContext()

    /** Applies one typed edit intent through source revision and transaction authority. */
    @JsonRequest("athena/applyEditOperation")
    fun applyEditOperation(payload: Map<String, Any?>): CompletableFuture<Map<String, Any?>?> {
        val session = activeSession ?: return CompletableFuture.completedFuture(null)
        val operation = runCatching { editOperationFromWire(payload) }.getOrElse { failure ->
            val operationId = (payload["operationId"] as? String)
                ?.takeIf { value -> runCatching { java.util.UUID.fromString(value) }.getOrNull()?.version() == 4 }
                ?: java.util.UUID.randomUUID().toString()
            return CompletableFuture.completedFuture(
                com.engineeringood.athena.interaction.EditOperationResult.rejected(
                    operationId,
                    SourceRevisionService(session).current(),
                    com.engineeringood.athena.interaction.OperationRejectionReason.INVALID,
                    listOf(com.engineeringood.athena.interaction.OperationDiagnostic("Edit operation", failure.message ?: "Edit Operation payload is invalid.", "Refresh and submit one generated typed Edit Operation.", "edit.operation.payload-invalid")),
                ).toWirePayload(),
            )
        }
        return CompletableFuture.completedFuture(applyEditOperation(operation).get()?.toWirePayload())
    }

    fun applyEditOperation(operation: com.engineeringood.athena.interaction.EditOperationEnvelope): CompletableFuture<com.engineeringood.athena.interaction.EditOperationResult?> =
        CompletableFuture.completedFuture(
            activeSession?.let { session ->
                editOperationService?.apply(operation) ?: com.engineeringood.athena.interaction.EditOperationResult.rejected(
                    operation.operationId,
                    SourceRevisionService(session).current(),
                    com.engineeringood.athena.interaction.OperationRejectionReason.UNAVAILABLE,
                    listOf(com.engineeringood.athena.interaction.OperationDiagnostic(session.projectName, "Edit Operation service is unavailable.", "Reactivate Athena repository session.", "edit.operation.unavailable")),
                )
            },
        )

    internal fun acceptedOperationJournal() = editOperationService?.journalEntries().orEmpty()

    private fun resolveRepositoryRoot(params: InitializeParams): Path? {
        val workspaceUri = params.workspaceFolders
            ?.firstOrNull()
            ?.uri
            ?.toRepositoryRootPath()
        if (workspaceUri != null) {
            return workspaceUri
        }

        val initializationOptions = params.initializationOptions as? Map<*, *>
        val repositoryRoot = initializationOptions
            ?.get("repositoryRoot")
            ?.toString()
            ?.takeIf { it.isNotBlank() }
        return repositoryRoot?.let(Path::of)
    }

    private fun updateSnapshot(documentUri: String) {
        sessionSnapshot = sessionSnapshot?.copy(lastOpenedDocumentUri = documentUri)
    }

    /**
     * Semantic-authority guardrail.
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
     * `CompilerCompilationSuccess.semanticResult.diagnostics`). `AthenaSemanticAuthorityBoundaryTest`
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
        val diagnostics = trackedDocument.sheetCompanion?.toLspDiagnostics()
            ?: (trackedDocument.compilation.toLspDiagnostics() +
                trackedDocument.projectSemanticDiagnostics.toLspDiagnostics(
                    documentUri = documentUri,
                    currentSourceUnitId = trackedDocument.projectSemanticSourceUnitId,
                    sourceUnitUris = trackedDocument.projectSemanticSourceUnitUris,
                ))
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

/** Handles the Athena text-document LSP surface. */
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
    private val onFormatting: (params: DocumentFormattingParams) -> List<TextEdit> = {
        emptyList()
    },
    private val onSemanticTokens: (params: SemanticTokensParams) -> SemanticTokens = {
        SemanticTokens(emptyList())
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

    override fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
        return CompletableFuture.completedFuture(onFormatting(params))
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        return CompletableFuture.completedFuture(onSemanticTokens(params))
    }
}

/** Owns workspace hooks required by the LSP contract. */
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
 * Semantic-authority guardrail: this function may only ever pattern-match on
 * the compiler-owned result cases and read `CompilerSyntaxDiagnostic` / `SemanticDiagnostic` (from
 * `com.engineeringood.athena.compiler` and `com.engineeringood.athena.semantics.core`). It must never
 * be changed to accept, read, or merge a Tree-sitter tree/query result (Epic 3) or any ANTLR4
 * parse-tree/visitor type (Epic 2) as a diagnostics source. Tree-sitter owns syntax UX only and must
 * never become a second semantic-truth source.
 */
private fun CompilerCompilationResult.toLspDiagnostics(): List<Diagnostic> {
    return when (this) {
        is CompilerCompilationParseFailure -> diagnostics.map { diagnostic -> diagnostic.toLspDiagnostic() }
        is CompilerCompilationSuccess -> semanticResult.diagnostics
            .distinct()
            .map { diagnostic -> diagnostic.toLspDiagnostic() }
    }
}

private fun SheetCompanionParseResult.toLspDiagnostics(): List<Diagnostic> = when (this) {
    is SheetCompanionParseFailure -> diagnostics.map(SyntaxDiagnostic::toLspDiagnostic)
    else -> emptyList()
}

private fun SyntaxDiagnostic.toLspDiagnostic(): Diagnostic = Diagnostic().apply {
    severity = DiagnosticSeverity.Error
    source = "Athena Sheet Companion"
    code = Either.forLeft("sheet-companion")
    message = this@toLspDiagnostic.message
    range = Range(
        Position((span.start.line - 1).coerceAtLeast(0), (span.start.column - 1).coerceAtLeast(0)),
        Position((span.end.line - 1).coerceAtLeast(0), (span.end.column - 1).coerceAtLeast(0)),
    )
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

private fun RepositoryDiagnostic.toLspDiagnostic(): Diagnostic {
    return Diagnostic().apply {
        severity = when (this@toLspDiagnostic.severity) {
            RepositoryDiagnosticSeverity.ERROR -> DiagnosticSeverity.Error
            RepositoryDiagnosticSeverity.WARNING -> DiagnosticSeverity.Warning
            RepositoryDiagnosticSeverity.INFO -> DiagnosticSeverity.Information
        }
        source = "Athena repository"
        code = Either.forLeft(this@toLspDiagnostic.code)
        message = this@toLspDiagnostic.message
        range = Range(
            Position(((startLine ?: 1) - 1).coerceAtLeast(0), ((startColumn ?: 1) - 1).coerceAtLeast(0)),
            Position(((endLine ?: startLine ?: 1) - 1).coerceAtLeast(0), ((endColumn ?: startColumn ?: 1) - 1).coerceAtLeast(0)),
        )
    }
}

internal fun List<ProjectSemanticDiagnostic>.toLspDiagnostics(
    documentUri: String,
    currentSourceUnitId: SourceUnitId?,
    sourceUnitUris: Map<SourceUnitId, String>,
): List<Diagnostic> {
    return filter { diagnostic -> diagnostic.severity != ProjectSemanticDiagnosticSeverity.INFO }
        .mapNotNull { diagnostic ->
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
