package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic
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
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

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
                    activeSession = activation
                    languageFeatures = AthenaLanguageFeatures(activation.context.compiler())
                    sessionSnapshot = AthenaLspSessionSnapshot(
                        repositoryRoot = activation.repositoryRoot,
                        manifestPath = activation.manifestPath,
                        lockPath = activation.lockPath,
                        sourceRootPath = activation.sourceRootPath,
                        sourcePath = activation.sourcePath,
                        projectName = activation.projectName,
                        primaryPackageName = activation.primaryPackageName,
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

    private fun updateSnapshot(documentUri: String) {
        sessionSnapshot = sessionSnapshot?.copy(lastOpenedDocumentUri = documentUri)
    }

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
        val diagnostics = trackedDocument.compilation.toLspDiagnostics()
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

private fun CompilerCompilationResult.toLspDiagnostics(): List<Diagnostic> {
    return when (this) {
        is CompilerCompilationParseFailure -> diagnostics.map { diagnostic -> diagnostic.toLspDiagnostic() }
        is CompilerCompilationSuccess -> semanticResult.diagnostics.map { diagnostic -> diagnostic.toLspDiagnostic() }
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
        source = "Athena semantic"
        code = Either.forLeft(ruleId.value)
        message = this@toLspDiagnostic.message
        range = Range(
            Position((provenance.startLine - 1).coerceAtLeast(0), (provenance.startColumn - 1).coerceAtLeast(0)),
            Position((provenance.endLine - 1).coerceAtLeast(0), (provenance.endColumn - 1).coerceAtLeast(0)),
        )
    }
}
