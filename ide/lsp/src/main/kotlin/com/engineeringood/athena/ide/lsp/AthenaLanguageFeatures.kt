package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.semantic.CanonicalSemanticIdentityBuilder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticBinding
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclaration
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnostic
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import com.engineeringood.athena.compiler.semantic.SourceUnitId
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.BindingSelectorKind
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.NetDeclaration
import com.engineeringood.athena.language.ConnectionSpecificationDeclaration
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.ElementAnchorExportDeclaration
import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ElementLabelExportDeclaration
import com.engineeringood.athena.language.ElementNumberField
import com.engineeringood.athena.language.EngineeringFunctionDeclaration
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.MacroDeclaration
import com.engineeringood.athena.language.PlaceholderDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.ProjectionConstructDeclaration
import com.engineeringood.athena.language.ProjectionPolicyDeclaration
import com.engineeringood.athena.language.RegionDeclaration
import com.engineeringood.athena.language.ProfileDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.RepresentationDeclaration
import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolDynamicLabelDeclaration
import com.engineeringood.athena.language.SymbolGraphicDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import com.engineeringood.athena.language.SymbolIdentifierField
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.language.VariantDeclaration
import com.engineeringood.athena.language.SheetDeclaration
import com.engineeringood.athena.language.GridDeclaration
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionParseFailure
import com.engineeringood.athena.language.SheetCompanionParseResult
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.repository.PackageIdentifier
import java.io.File
import java.nio.file.Files
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.nio.file.Path

/**
 * Tracks the latest in-memory editor state that Athena LSP owns for one authored document.
 */
data class AthenaTrackedDocument(
    val uri: String,
    val path: Path,
    val version: Int,
    val text: String,
    val compilation: CompilerCompilationResult,
    val navigationIndex: AthenaNavigationIndex?,
    val projectSemanticGraphId: String? = null,
    val projectSemanticSourceUnitId: SourceUnitId? = null,
    val projectSemanticSourceUnitUris: Map<SourceUnitId, String> = emptyMap(),
    val projectSemanticDiagnostics: List<ProjectSemanticDiagnostic> = emptyList(),
    val projectSemanticNavigation: AthenaProjectSemanticNavigationSnapshot? = null,
    val sheetCompanion: SheetCompanionParseResult? = null,
)

data class AthenaProjectSemanticDiagnosticsSnapshot(
    val graphId: String?,
    val sourceUnitId: SourceUnitId?,
    val sourceUnitUris: Map<SourceUnitId, String>,
    val diagnostics: List<ProjectSemanticDiagnostic>,
    val navigation: AthenaProjectSemanticNavigationSnapshot? = null,
)

data class AthenaProjectSemanticNavigationSnapshot(
    val graphId: String,
    val currentSourceUnitId: SourceUnitId?,
    val sourceUnitUris: Map<SourceUnitId, String>,
    val declarations: List<ProjectSemanticDeclaration>,
    val bindings: List<ProjectSemanticBinding>,
)

private data class ProjectSemanticSourceLocation(
    val packageId: PackageIdentifier,
    val sourceRoot: Path,
    val sourceRootRelativePath: String,
)

/**
 * Parameters for the Athena-owned semantic inspection request.
 */
data class AthenaSemanticInspectionParams(
    val textDocument: AthenaSemanticInspectionTextDocument,
)

/**
 * One text-document handle used by Athena-owned inspection requests.
 */
data class AthenaSemanticInspectionTextDocument(
    val uri: String,
)

/**
 * Read-only semantic inspection payload returned through the Athena LSP boundary.
 */
data class AthenaSemanticInspectionPayload(
    val uri: String,
    val version: Int,
    val status: String,
    val systemName: String?,
    val diagnosticsCount: Int,
    val diagnosticSummaries: List<String>,
    val entityCount: Int,
    val portCount: Int,
    val relationshipCount: Int,
    val entities: List<AthenaSemanticInspectionEntity>,
    val ports: List<AthenaSemanticInspectionPort>,
    val relationships: List<AthenaSemanticInspectionRelationship>,
)

/**
 * One read-only entity entry inside the semantic inspection payload.
 */
data class AthenaSemanticInspectionEntity(
    val semanticId: String,
    val name: String,
    val concept: String,
    val properties: String,
    val authoredProperties: List<AthenaSemanticInspectionProperty>,
    val sourceRange: Range,
)

/**
 * One structured authored entity property published for guided inspector flows.
 */
data class AthenaSemanticInspectionProperty(
    val name: String,
    val valueKind: String,
    val valueText: String,
)

/**
 * One read-only port entry inside the semantic inspection payload.
 */
data class AthenaSemanticInspectionPort(
    val semanticId: String,
    val path: String,
    val properties: String,
    val authoredProperties: List<AthenaSemanticInspectionProperty>,
    val sourceRange: Range,
)

/**
 * One read-only Relationship entry inside the semantic inspection payload.
 */
data class AthenaSemanticInspectionRelationship(
    val semanticId: String,
    val definition: String,
    val participants: List<AthenaSemanticInspectionParticipant>,
    val sourceRange: Range,
)

data class AthenaSemanticInspectionParticipant(
    val role: String,
    val subjectPath: String,
)

/**
 * Owns document-scoped language analysis inside the Athena LSP process.
 *
 * The service keeps authored buffers, the latest compiler result, and the small navigation index needed
 * for M4 completion, symbols, definition lookup, and references. Frontend code stays downstream of these
 * server-owned results.
 */
class AthenaLanguageFeatures(
    private val compiler: com.engineeringood.athena.compiler.AthenaCompiler,
    private val repositoryRoot: Path? = null,
    private val sourceRootPath: Path? = null,
) {
    private val documentsByUri = linkedMapOf<String, AthenaTrackedDocument>()
    /**
     * Recompiles [text] as the current content for [uri] and stores the result as the latest server-owned state.
     */
    fun trackDocument(
        uri: String,
        path: Path,
        version: Int,
        text: String,
    ): AthenaTrackedDocument {
        val existing = documentsByUri[uri]
        if (existing != null) {
            if (existing.version > version) {
                return existing
            }
            if (existing.version == version && existing.text == text) {
                return existing
            }
        }

        val sheetCompanion = if (path.fileName.toString().endsWith(".sheet.athena")) {
            AthenaSheetCompanionParser().parse(path.toString(), text)
        } else {
            null
        }
        val compilation = compiler.compile(path, text)
        val success = compilation as? CompilerCompilationSuccess
        val projectSemanticSnapshot = success?.let {
            projectSemanticDiagnostics(path, text, it)
        }
        val tracked = AthenaTrackedDocument(
            uri = uri,
            path = path,
            version = version,
            text = text,
            compilation = compilation,
            navigationIndex = success?.let { result ->
                AthenaNavigationIndex(uri, result.source.ast)
            },
            projectSemanticGraphId = projectSemanticSnapshot?.graphId,
            projectSemanticSourceUnitId = projectSemanticSnapshot?.sourceUnitId,
            projectSemanticSourceUnitUris = projectSemanticSnapshot?.sourceUnitUris.orEmpty(),
            projectSemanticDiagnostics = projectSemanticSnapshot?.diagnostics.orEmpty(),
            projectSemanticNavigation = projectSemanticSnapshot?.navigation,
            sheetCompanion = sheetCompanion,
        )
        documentsByUri[uri] = tracked
        return tracked
    }

    private fun projectSemanticDiagnostics(
        path: Path,
        text: String,
        success: CompilerCompilationSuccess?,
    ): AthenaProjectSemanticDiagnosticsSnapshot? {
        if (!path.isProjectSemanticSource()) {
            return null
        }
        if (!text.hasPackageAwareSyntax()) {
            return null
        }
        val root = repositoryRoot ?: return null
        val publication = compiler.publishRepositoryGraphReport(root)
        val graph = publication.graph ?: return AthenaProjectSemanticDiagnosticsSnapshot(
            graphId = null,
            sourceUnitId = null,
            sourceUnitUris = emptyMap(),
            diagnostics = compiler.buildProjectSemanticGraph(publication, emptyList()).diagnostics,
        )
        val sourceRoot = sourceRootPath ?: return null
        val currentSourceLocation = path.projectSemanticSourceLocation(publication)
            ?: path.sourceRootRelativePath(sourceRoot)?.let { relativePath ->
                ProjectSemanticSourceLocation(
                    packageId = graph.rootPackage,
                    sourceRoot = sourceRoot.toAbsolutePath().normalize(),
                    sourceRootRelativePath = relativePath,
                )
            }
            ?: return null
        val sourceRootRelativePath = currentSourceLocation.sourceRootRelativePath
        val rootPackageId = graph.rootPackage
        val inputPackageId = sourcePackageId(success, graph.packages.map { resolvedPackage ->
            resolvedPackage.packageId
        }) ?: currentSourceLocation.packageId
        val sourceInputs = projectSemanticSourceInputs(
            publication = publication,
            currentPath = path,
            currentText = text,
            currentPackageId = inputPackageId,
            currentSourceRootRelativePath = sourceRootRelativePath,
        )
        val buildResult = compiler.buildProjectSemanticGraph(
            publication = publication,
            sources = sourceInputs,
        )
        val builtSnapshot = buildResult.snapshot ?: return AthenaProjectSemanticDiagnosticsSnapshot(
            graphId = null,
            sourceUnitId = null,
            sourceUnitUris = emptyMap(),
            diagnostics = buildResult.diagnostics,
        )
        val inputPackageKey = builtSnapshot.packages.firstOrNull { semanticPackage ->
            semanticPackage.packageId == inputPackageId
        }?.packageKey
        val sourceUnitId = builtSnapshot.sourceUnits.firstOrNull { sourceUnit ->
            sourceUnit.packageKey == inputPackageKey &&
                sourceUnit.sourceRootRelativePath == sourceRootRelativePath
        }?.sourceUnitId ?: builtSnapshot.sourceUnits.firstOrNull { sourceUnit ->
            sourceUnit.sourceRootRelativePath == sourceRootRelativePath
        }?.sourceUnitId ?: runCatching {
            CanonicalSemanticIdentityBuilder.sourceUnitId(
                CanonicalSemanticIdentityBuilder.packageKey(inputPackageId),
                sourceRootRelativePath,
            )
        }.getOrNull()
        val resolvedSnapshot = compiler.resolveProjectSemanticImports(builtSnapshot)
        val diagnosticSnapshot = compiler.emitProjectSemanticDiagnostics(resolvedSnapshot)
        val indexedSnapshot = compiler.indexProjectSemanticDeclarations(diagnosticSnapshot)
        val layoutBoundSnapshot = compiler.bindProjectSemanticLayoutHints(indexedSnapshot)
        val linkedSnapshot = compiler.linkProjectSemanticReferences(layoutBoundSnapshot)
        val packageSourceRoots = packageSourceRoots(publication)
        val sourceUnitUris = linkedSnapshot.sourceUnits.mapNotNull { sourceUnit ->
            val packageSourceRoot = packageSourceRoots[sourceUnit.packageKey] ?: return@mapNotNull null
            sourceUnit.sourceUnitId to packageSourceRoot.resolveSourceUnitUri(sourceUnit.sourceRootRelativePath)
        }.toMap()
        return AthenaProjectSemanticDiagnosticsSnapshot(
            graphId = linkedSnapshot.graphId.value,
            sourceUnitId = sourceUnitId,
            sourceUnitUris = sourceUnitUris,
            diagnostics = linkedSnapshot.diagnostics,
            navigation = AthenaProjectSemanticNavigationSnapshot(
                graphId = linkedSnapshot.graphId.value,
                currentSourceUnitId = sourceUnitId,
                sourceUnitUris = sourceUnitUris,
                declarations = linkedSnapshot.declarations,
                bindings = linkedSnapshot.bindings,
            ),
        )
    }

    private fun projectSemanticSourceInputs(
        publication: AthenaRepositoryReportPublicationResult,
        currentPath: Path,
        currentText: String,
        currentPackageId: PackageIdentifier,
        currentSourceRootRelativePath: String,
    ): List<ProjectSemanticSourceInput> {
        val graph = publication.graph ?: return emptyList()
        val normalizedCurrentPath = currentPath.toAbsolutePath().normalize()
        val packageSourceRoots = packageSourceRoots(publication)
        val packageIds = graph.packages.map { resolvedPackage -> resolvedPackage.packageId }
        val discovered = graph.packages.flatMap { resolvedPackage ->
            val packageKey = runCatching {
                CanonicalSemanticIdentityBuilder.packageKey(resolvedPackage.packageId)
            }.getOrNull() ?: return@flatMap emptyList()
            val packageSourceRoot = packageSourceRoots[packageKey] ?: return@flatMap emptyList()
            if (!Files.isDirectory(packageSourceRoot)) {
                return@flatMap emptyList()
            }
            Files.walk(packageSourceRoot).use { stream ->
                stream
                    .filter { candidate -> Files.isRegularFile(candidate) }
                    .filter { candidate -> candidate.isProjectSemanticSource() }
                    .toList()
                    .mapNotNull { candidate ->
                        val normalizedCandidate = candidate.toAbsolutePath().normalize()
                        val tracked = trackedDocumentByPath(normalizedCandidate)
                        val sourceText = when {
                            normalizedCandidate == normalizedCurrentPath -> currentText
                            tracked != null -> tracked.text
                            else -> runCatching { Files.readString(normalizedCandidate) }.getOrNull()
                        } ?: return@mapNotNull null
                        val parsedPackageId = sourcePackageId(
                            success = compiler.parse(normalizedCandidate, sourceText) as? com.engineeringood.athena.compiler.CompilerParseSuccess,
                            packageIds = packageIds,
                        ) ?: resolvedPackage.packageId
                        ProjectSemanticSourceInput(
                            packageId = parsedPackageId,
                            sourceRootRelativePath = packageSourceRoot.relativize(normalizedCandidate)
                                .toString()
                                .replace(File.separatorChar, '/'),
                            sourceContent = sourceText,
                        )
                    }
                    .toList()
            }
        }
        val currentAlreadyIncluded = discovered.any { input ->
            input.packageId == currentPackageId &&
                input.sourceRootRelativePath == currentSourceRootRelativePath
        }
        if (currentAlreadyIncluded) {
            return discovered
        }
        return discovered + ProjectSemanticSourceInput(
            packageId = currentPackageId,
            sourceRootRelativePath = currentSourceRootRelativePath,
            sourceContent = currentText,
        )
    }

    private fun packageSourceRoots(
        publication: AthenaRepositoryReportPublicationResult,
    ): Map<com.engineeringood.athena.compiler.semantic.PackageKey, Path> {
        val graph = publication.graph ?: return emptyMap()
        return graph.packages.associate { resolvedPackage ->
            CanonicalSemanticIdentityBuilder.packageKey(resolvedPackage.packageId) to
                publication.repositoryRoot.resolve(resolvedPackage.sourceRoot).toAbsolutePath().normalize()
        }
    }

    private fun Path.projectSemanticSourceLocation(
        publication: AthenaRepositoryReportPublicationResult,
    ): ProjectSemanticSourceLocation? {
        val graph = publication.graph ?: return null
        val normalizedPath = toAbsolutePath().normalize()
        return graph.packages
            .map { resolvedPackage ->
                ProjectSemanticSourceLocation(
                    packageId = resolvedPackage.packageId,
                    sourceRoot = publication.repositoryRoot
                        .resolve(resolvedPackage.sourceRoot)
                        .toAbsolutePath()
                        .normalize(),
                    sourceRootRelativePath = "",
                )
            }
            .filter { candidate -> normalizedPath.startsWith(candidate.sourceRoot) }
            .maxByOrNull { candidate -> candidate.sourceRoot.nameCount }
            ?.let { candidate ->
                val relativePath = candidate.sourceRoot.relativize(normalizedPath)
                    .toString()
                    .replace(File.separatorChar, '/')
                candidate.copy(sourceRootRelativePath = relativePath)
            }
            ?.takeIf { candidate -> candidate.sourceRootRelativePath.isNotBlank() && !candidate.sourceRootRelativePath.startsWith("..") }
    }

    private fun sourcePackageId(
        success: CompilerCompilationSuccess?,
        packageIds: List<PackageIdentifier>,
    ): PackageIdentifier? {
        val declaredPackageName = success
            ?.source
            ?.ast
            ?.packageDeclaration
            ?.name
            ?.parts
            ?.joinToString(".")
            ?: return null
        return packageIds.firstOrNull { packageId -> packageId.name == declaredPackageName }
            ?: PackageIdentifier(declaredPackageName)
    }

    private fun sourcePackageId(
        success: com.engineeringood.athena.compiler.CompilerParseSuccess?,
        packageIds: List<PackageIdentifier>,
    ): PackageIdentifier? {
        val declaredPackageName = success
            ?.source
            ?.ast
            ?.packageDeclaration
            ?.name
            ?.parts
            ?.joinToString(".")
            ?: return null
        return packageIds.firstOrNull { packageId -> packageId.name == declaredPackageName }
            ?: PackageIdentifier(declaredPackageName)
    }

    /**
     * Removes any tracked state for [uri] after the frontend closes that document.
     */
    fun closeDocument(uri: String) {
        documentsByUri.remove(uri)
    }

    /**
     * Returns the tracked document for [uri], if the frontend has already opened it through Athena LSP.
     */
    fun trackedDocument(uri: String): AthenaTrackedDocument? = documentsByUri[uri]

    /**
     * Returns the latest tracked document for [path], if Athena LSP currently owns an in-memory buffer for it.
     */
    fun trackedDocumentByPath(path: Path): AthenaTrackedDocument? {
        val normalizedPath = path.normalize()
        return documentsByUri.values
            .asSequence()
            .filter { tracked -> tracked.path.normalize() == normalizedPath }
            .maxByOrNull(AthenaTrackedDocument::version)
    }

    /**
     * Builds M4 completion results for the document and cursor in [params].
     */
    fun completion(params: org.eclipse.lsp4j.CompletionParams): CompletionList {
        val tracked = trackedDocument(params.textDocument.uri)
            ?: return CompletionList(false, defaultKeywordCompletions())
        val success = tracked.compilation as? CompilerCompilationSuccess
        val cursor = tracked.text.cursorContext(
            position = params.position,
            ast = success?.source?.ast,
        )
        val completions = when {
            cursor.expectsDirectionValue -> completionItems(
                "in" to CompletionItemKind.EnumMember,
                "out" to CompletionItemKind.EnumMember,
            )

            cursor.expectsFlowValue -> completionItems(
                "Digital" to CompletionItemKind.EnumMember,
            )

            cursor.expectsOrientationValue -> completionItems(
                "horizontal" to CompletionItemKind.EnumMember,
                "vertical" to CompletionItemKind.EnumMember,
            )

            cursor.expectsConceptValue -> {
                val knownConcepts = success
                    ?.source
                    ?.ast
                    ?.declarations
                    ?.asSequence()
                    ?.filterIsInstance<EntityDeclaration>()
                    ?.flatMap { declaration ->
                        declaration.fields.asSequence()
                            .filter { field -> field.name == "concept" }
                            .mapNotNull { field -> (field.value as? ScalarValue.Symbol)?.text }
                    }
                    ?.distinct()
                    ?.toList()
                    .orEmpty()
                completionItems(*(knownConcepts.ifEmpty { listOf("Motor", "Switch") }
                    .map { concept -> concept to CompletionItemKind.Class }
                    .toTypedArray()))
            }

            cursor.expectsQualifiedPort -> {
                val ports = success
                    ?.source
                    ?.ast
                    ?.declarations
                    ?.filterIsInstance<PortDeclaration>()
                    .orEmpty()
                completionItems(*(ports
                    .map { declaration -> declaration.qualifiedName.parts.joinToString(".") to CompletionItemKind.Reference }
                    .distinctBy { it.first }
                    .toTypedArray()))
            }

            cursor.insideEntityBlock -> completionItems(
                "concept" to CompletionItemKind.Property,
                "model" to CompletionItemKind.Property,
                "port" to CompletionItemKind.Keyword,
                "function" to CompletionItemKind.Keyword,
            )

            cursor.insidePortBlock -> completionItems(
                "direction" to CompletionItemKind.Property,
                "flow" to CompletionItemKind.Property,
            )

            cursor.insideFunctionBlock -> completionItems(
                "role" to CompletionItemKind.Property,
                "port" to CompletionItemKind.Keyword,
            )

            else -> defaultKeywordCompletions()
        }

        val filtered = if (cursor.fragment.isBlank()) {
            completions
        } else {
            completions.filter { item ->
                item.label.lowercase().startsWith(cursor.fragment.lowercase())
            }
        }

        return CompletionList(false, filtered)
    }

    /**
     * Builds hierarchical document symbols for the current tracked document.
     */
    fun documentSymbols(params: DocumentSymbolParams): List<Either<org.eclipse.lsp4j.SymbolInformation, DocumentSymbol>> {
        val tracked = trackedDocument(params.textDocument.uri) ?: return emptyList()
        tracked.sheetCompanion?.let { result ->
            return result.toDocumentSymbols()
        }
        val success = tracked.compilation as? CompilerCompilationSuccess ?: return emptyList()
        val ast = success.source.ast
        val children = ast.declarations.map { declaration -> declaration.toDocumentSymbol() }
        val systemSymbol = DocumentSymbol().apply {
            name = ast.system.name
            kind = SymbolKind.Module
            detail = "system"
            range = ast.system.span.toLspRange()
            selectionRange = ast.system.span.toLspRange()
            this.children = children
        }
        ast.packageDeclaration?.let { packageDeclaration ->
            if (tracked.projectSemanticNavigation != null) {
                val packageRange = packageDeclaration.span.toLspRange()
                val packageSymbol = DocumentSymbol().apply {
                    name = packageDeclaration.name.parts.joinToString(".")
                    kind = SymbolKind.Package
                    detail = "package"
                    range = packageRange
                    selectionRange = packageRange
                    this.children = listOf(systemSymbol)
                }
                return listOf(Either.forRight(packageSymbol))
            }
        }
        return listOf(Either.forRight(systemSymbol))
    }

    /**
     * Resolves the definition locations for the symbol addressed by [uri] and [position].
     */
    fun definition(uri: String, position: Position): List<Location> {
        val tracked = trackedDocument(uri) ?: return emptyList()
        val offset = tracked.text.offsetAt(position)
        tracked.projectSemanticNavigation
            ?.let { navigation -> AthenaProjectSemanticNavigationIndex(navigation).definition(offset) }
            ?.takeIf { locations -> locations.isNotEmpty() }
            ?.let { locations -> return locations }
        val index = tracked.navigationIndex ?: return emptyList()
        return index.definition(offset)
    }

    /**
     * Resolves references for the symbol addressed by [params].
     */
    fun references(params: ReferenceParams): List<Location> {
        val tracked = trackedDocument(params.textDocument.uri) ?: return emptyList()
        val offset = tracked.text.offsetAt(params.position)
        val includeDeclaration = params.context?.isIncludeDeclaration ?: false
        tracked.projectSemanticNavigation
            ?.let { navigation -> AthenaProjectSemanticNavigationIndex(navigation).references(offset, includeDeclaration) }
            ?.takeIf { locations -> locations.isNotEmpty() }
            ?.let { locations -> return locations }
        val index = tracked.navigationIndex ?: return emptyList()
        return index.references(
            offset = offset,
            includeDeclaration = includeDeclaration,
        )
    }

    fun formatting(uri: String): List<TextEdit> {
        val tracked = trackedDocument(uri) ?: return emptyList()
        val formatted = (tracked.compilation as? CompilerCompilationSuccess)
            ?.source
            ?.ast
            ?.let(AthenaProjectSourceFormatter::format)
            ?: return emptyList()
        return listOf(
            TextEdit(
                tracked.text.fullDocumentRange(),
                formatted,
            ),
        )
    }

    fun semanticTokens(uri: String): SemanticTokens {
        val tracked = trackedDocument(uri) ?: return SemanticTokens(emptyList())
        return SemanticTokens(tracked.text.semanticTokenData())
    }

    /**
     * Builds a read-only semantic inspection snapshot for the latest tracked document at [uri].
     *
     * Semantic-authority guardrail: this payload is built purely from
     * `CompilerCompilationParseFailure` / `CompilerCompilationSuccess` fields
     * (`semanticResult.diagnostics`) plus `AthenaNavigationIndex` source ranges derived from the
     * authored `SourceFileAst`. It must never grow a second,
     * Tree-sitter-backed code path (Epic 3) or read an ANTLR4 parse-tree/visitor type (Epic 2) as a
     * semantic-truth or diagnostics source; semantic meaning stays compiler-owned.
     */
    fun semanticInspection(uri: String): AthenaSemanticInspectionPayload? {
        val tracked = trackedDocument(uri) ?: return null
        return when (val compilation = tracked.compilation) {
            is com.engineeringood.athena.compiler.CompilerCompilationParseFailure -> AthenaSemanticInspectionPayload(
                uri = tracked.uri,
                version = tracked.version,
                status = "parse-failure",
                systemName = null,
                diagnosticsCount = compilation.diagnostics.size,
                diagnosticSummaries = compilation.diagnostics.map { diagnostic ->
                    "L${diagnostic.line}:${diagnostic.column} ${diagnostic.message}"
                },
                entityCount = 0,
                portCount = 0,
                relationshipCount = 0,
                entities = emptyList(),
                ports = emptyList(),
                relationships = emptyList(),
            )

            is CompilerCompilationSuccess -> {
                val document = compilation.document
                val navigationIndex = tracked.navigationIndex
                val visibleDiagnostics = compilation.semanticResult.diagnostics.distinct()
                AthenaSemanticInspectionPayload(
                    uri = tracked.uri,
                    version = tracked.version,
                    status = if (visibleDiagnostics.isEmpty()) "ready" else "diagnostics",
                    systemName = document.system.name,
                    diagnosticsCount = visibleDiagnostics.size,
                    diagnosticSummaries = visibleDiagnostics.map { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                    entityCount = document.entities.size,
                    portCount = document.ports.size,
                    relationshipCount = document.relationships.size,
                    entities = document.entities
                        .sortedBy { entity -> entity.name }
                        .map { entity ->
                            AthenaSemanticInspectionEntity(
                                semanticId = entity.id.value,
                                name = entity.name,
                                concept = entity.conceptReference.authoredName.joinToString("."),
                                properties = entity.properties.summaryText(),
                                authoredProperties = entity.properties.map { property -> property.toInspectionProperty() },
                                sourceRange = requireSourceRange(
                                    semanticId = entity.id.value,
                                    kind = "entity",
                                    range = navigationIndex?.entitySourceRange(entity.name),
                                ),
                            )
                        },
                    ports = document.ports
                        .sortedBy { port -> port.summaryPath() }
                        .map { port ->
                            AthenaSemanticInspectionPort(
                                semanticId = port.id.value,
                                path = port.summaryPath(),
                                properties = port.properties.summaryText(),
                                authoredProperties = port.properties.map { property -> property.toInspectionProperty() },
                                sourceRange = requireSourceRange(
                                    semanticId = port.id.value,
                                    kind = "port",
                                    range = navigationIndex?.portSourceRange(port.summaryPath()),
                                ),
                            )
                        },
                    relationships = document.relationships
                        .sortedBy { relationship -> relationship.id.value }
                        .map { relationship ->
                            val definition = relationship.definitionReference.authoredName.joinToString(".")
                            val participants = relationship.participants.map { participant ->
                                AthenaSemanticInspectionParticipant(
                                    role = participant.role,
                                    subjectPath = participant.subject.reference.authoredPath(),
                                )
                            }
                            AthenaSemanticInspectionRelationship(
                                semanticId = relationship.id.value,
                                definition = definition,
                                participants = participants,
                                sourceRange = requireSourceRange(
                                    semanticId = relationship.id.value,
                                    kind = "relationship",
                                    range = navigationIndex?.relationshipSourceRange(
                                        definition = definition,
                                        participantPaths = participants.map { participant -> participant.subjectPath },
                                    ),
                                ),
                            )
                        },
                )
            }
        }
    }

    private fun defaultKeywordCompletions(): List<CompletionItem> {
        return completionItems(
            "system" to CompletionItemKind.Keyword,
            "entity" to CompletionItemKind.Keyword,
            "port" to CompletionItemKind.Keyword,
            "function" to CompletionItemKind.Keyword,
            "role" to CompletionItemKind.Property,
            "ports" to CompletionItemKind.Property,
            "connect" to CompletionItemKind.Keyword,
            "net" to CompletionItemKind.Keyword,
            "connection-spec" to CompletionItemKind.Keyword,
            "source" to CompletionItemKind.Keyword,
            "sink" to CompletionItemKind.Keyword,
            "pass" to CompletionItemKind.Keyword,
            "layout" to CompletionItemKind.Keyword,
            "place" to CompletionItemKind.Keyword,
            "at" to CompletionItemKind.Keyword,
            "orientation" to CompletionItemKind.Property,
            "horizontal" to CompletionItemKind.EnumMember,
            "vertical" to CompletionItemKind.EnumMember,
        )
    }

    private fun representationKeywordCompletions(): List<CompletionItem> {
        return completionItems(
            "package" to CompletionItemKind.Keyword,
            "symbol" to CompletionItemKind.Keyword,
            "element" to CompletionItemKind.Keyword,
            "resource" to CompletionItemKind.Keyword,
            "identity" to CompletionItemKind.Property,
            "version" to CompletionItemKind.Property,
            "graphic" to CompletionItemKind.Keyword,
            "svg" to CompletionItemKind.Keyword,
            "kind" to CompletionItemKind.Property,
            "path" to CompletionItemKind.Property,
            "bounds" to CompletionItemKind.Property,
            "line" to CompletionItemKind.Keyword,
            "polyline" to CompletionItemKind.Keyword,
            "points" to CompletionItemKind.Keyword,
            "arc" to CompletionItemKind.Keyword,
            "center" to CompletionItemKind.Keyword,
            "radius" to CompletionItemKind.Keyword,
            "sweep" to CompletionItemKind.Keyword,
            "circle" to CompletionItemKind.Keyword,
            "rectangle" to CompletionItemKind.Keyword,
            "label" to CompletionItemKind.Keyword,
            "size" to CompletionItemKind.Property,
            "from" to CompletionItemKind.Keyword,
            "to" to CompletionItemKind.Keyword,
            "style" to CompletionItemKind.Property,
            "anchor" to CompletionItemKind.Keyword,
            "ref" to CompletionItemKind.Property,
            "point" to CompletionItemKind.Property,
            "role" to CompletionItemKind.Property,
            "accepts" to CompletionItemKind.Keyword,
            "direction" to CompletionItemKind.Property,
            "flow" to CompletionItemKind.Property,
            "child" to CompletionItemKind.Keyword,
            "translate" to CompletionItemKind.Property,
            "rotate" to CompletionItemKind.Property,
            "scale" to CompletionItemKind.Property,
            "zOrder" to CompletionItemKind.Property,
            "export" to CompletionItemKind.Keyword,
            "profile" to CompletionItemKind.Keyword,
            "projection" to CompletionItemKind.Property,
            "standard" to CompletionItemKind.Property,
            "fallback" to CompletionItemKind.Property,
            "fail-closed" to CompletionItemKind.EnumMember,
            "binding" to CompletionItemKind.Keyword,
            "priority" to CompletionItemKind.Property,
            "select" to CompletionItemKind.Keyword,
            "where" to CompletionItemKind.Keyword,
            "use" to CompletionItemKind.Keyword,
            "variant" to CompletionItemKind.Property,
            "entity" to CompletionItemKind.Class,
            "function" to CompletionItemKind.Class,
        )
    }

    private fun completionItems(vararg items: Pair<String, CompletionItemKind>): List<CompletionItem> {
        return items.map { (label, kind) ->
            CompletionItem(label).apply {
                this.kind = kind
                detail = "Athena LSP"
                insertText = label
            }
        }
    }
}

/**
 * Projects compiler-owned project semantic declarations and bindings into LSP navigation locations.
 */
class AthenaProjectSemanticNavigationIndex(
    private val snapshot: AthenaProjectSemanticNavigationSnapshot,
) {
    private val declarationsById = snapshot.declarations.associateBy { declaration -> declaration.declarationId }
    private val currentBindings = snapshot.bindings.filter { binding ->
        binding.sourceUnitId == snapshot.currentSourceUnitId
    }
    private val currentDeclarations = snapshot.declarations.filter { declaration ->
        declaration.sourceUnitId == snapshot.currentSourceUnitId
    }

    fun definition(offset: Int): List<Location> {
        val binding = currentBindings.firstOrNull { candidate -> candidate.referenceSpan.contains(offset) }
            ?: return emptyList()
        val declaration = declarationsById[binding.resolvedDeclarationId] ?: return emptyList()
        return location(declaration.sourceUnitId, declaration.authoredSpan)
            ?.let(::listOf)
            .orEmpty()
    }

    fun references(offset: Int, includeDeclaration: Boolean): List<Location> {
        val declarationId = currentBindings
            .firstOrNull { binding -> binding.referenceSpan.contains(offset) }
            ?.resolvedDeclarationId
            ?: currentDeclarations
                .firstOrNull { declaration -> declaration.authoredSpan.contains(offset) }
                ?.declarationId
            ?: return emptyList()
        val declaration = declarationsById[declarationId]
        return buildList {
            if (includeDeclaration && declaration != null) {
                location(declaration.sourceUnitId, declaration.authoredSpan)?.let(::add)
            }
            snapshot.bindings
                .filter { binding -> binding.resolvedDeclarationId == declarationId }
                .mapNotNullTo(this) { binding -> location(binding.sourceUnitId, binding.referenceSpan) }
        }
    }

    private fun location(sourceUnitId: SourceUnitId, span: SourceSpan): Location? {
        val uri = snapshot.sourceUnitUris[sourceUnitId] ?: return null
        return Location(uri, span.toLspRange())
    }
}

class AthenaRepresentationNavigationIndex(
    private val uri: String,
    private val ast: SourceFileAst,
) {
    private val declarations = ast.representationDeclarations
    private val resourcesById = declarations.flatMap { declaration ->
        when (declaration) {
            is SymbolDeclaration -> declaration.resources.map { resource -> resource.id to resource }
            is ElementDeclaration -> declaration.resources.map { resource -> resource.id to resource }
            is VariantDeclaration -> declaration.resources.map { resource -> resource.id to resource }
            else -> emptyList()
        }
    }.toMap()
    private val symbolsByIdentity = declarations
        .filterIsInstance<SymbolDeclaration>()
        .mapNotNull { declaration -> declaration.identity?.value?.let { identity -> identity to declaration } }
        .toMap()
    private val elementsByIdentity = declarations
        .filterIsInstance<ElementDeclaration>()
        .mapNotNull { declaration -> declaration.identity?.value?.let { identity -> identity to declaration } }
        .toMap()
    private val profilesByName = declarations.filterIsInstance<ProfileDeclaration>().associateBy { profile -> profile.name }

    fun definition(offset: Int): List<Location> {
        declarations.filterIsInstance<ElementDeclaration>().forEach { element ->
            val childById = element.children.associateBy { child -> child.id }
            val childSymbolIdentityById = childById.mapValues { (_, child) -> child.symbolIdentity?.value }
            element.children.firstOrNull { child -> child.symbolIdentity?.span?.contains(offset) == true }
                ?.symbolIdentity
                ?.value
                ?.let { identity -> symbolsByIdentity[identity] }
                ?.let { symbol -> return listOf(Location(uri, symbol.span.toLspRange())) }

            element.exportedAnchors.firstOrNull { export -> export.childId.span.contains(offset) }
                ?.childId
                ?.value
                ?.let { childId -> childById[childId] }
                ?.let { child -> return listOf(Location(uri, child.headerSpan.toLspRange())) }

            element.exportedAnchors.firstOrNull { export -> export.childAnchorId.span.contains(offset) }
                ?.let { export ->
                    val identity = childSymbolIdentityById[export.childId.value] ?: return@let null
                    symbolsByIdentity[identity]?.anchors?.firstOrNull { anchor -> anchor.id == export.childAnchorId.value }
                }
                ?.let { anchor -> return listOf(Location(uri, anchor.span.toLspRange())) }

            element.exportedLabels.firstOrNull { export -> export.childId.span.contains(offset) }
                ?.childId
                ?.value
                ?.let { childId -> childById[childId] }
                ?.let { child -> return listOf(Location(uri, child.headerSpan.toLspRange())) }

            element.exportedLabels.firstOrNull { export -> export.childLabelId.span.contains(offset) }
                ?.let { export ->
                    val identity = childSymbolIdentityById[export.childId.value] ?: return@let null
                    symbolsByIdentity[identity]?.graphic?.labels?.firstOrNull { label -> label.id == export.childLabelId.value }
                }
                ?.let { label -> return listOf(Location(uri, label.span.toLspRange())) }

            element.graphic?.svgResource
                ?.takeIf { field -> field.span.contains(offset) }
                ?.value
                ?.let { resourceId -> resourcesById[resourceId] }
                ?.let { resource -> return listOf(Location(uri, resource.span.toLspRange())) }
        }
        declarations.filterIsInstance<SymbolDeclaration>().forEach { symbol ->
            symbol.graphic?.svgResource
                ?.takeIf { field -> field.span.contains(offset) }
                ?.value
                ?.let { resourceId -> resourcesById[resourceId] }
                ?.let { resource -> return listOf(Location(uri, resource.span.toLspRange())) }
        }
        declarations.filterIsInstance<BindingDeclaration>().forEach { binding ->
            binding.profile
                ?.takeIf { field -> field.span.contains(offset) }
                ?.value
                ?.let { profileName -> profilesByName[profileName] }
                ?.let { profile -> return listOf(Location(uri, profile.span.toLspRange())) }

            binding.useElement
                ?.takeIf { field -> field.span.contains(offset) }
                ?.value
                ?.let { identity -> elementsByIdentity[identity] }
                ?.let { element -> return listOf(Location(uri, element.span.toLspRange())) }
        }
        return emptyList()
    }
}

/**
 * Small server-owned navigation index derived from the current source AST.
 *
 * Migration-continuity guardrail: every lookup here depends only on the
 * authored `SourceFileAst` (`EntityDeclaration`, `PortDeclaration`, `RelationDeclaration`,
 * `QualifiedName`) and its `SourceSpan`/`SourcePosition` values.
 * Now that Epic 2 has replaced the handwritten parser with ANTLR4-backed parsing,
 * `documentSymbols`, `definition`, `references`, and the
 * `entitySourceRange`/`portSourceRange`/`connectionSourceRange` helpers must keep working
 * unchanged as long as the resulting `SourceFileAst` and its spans are populated correctly,
 * because they read the authored AST contract, never parser internals. After Epic 3's Tree-sitter
 * integration, these utilities stay LSP-served and AST-backed; Tree-sitter must not become an
 * alternative implementation of navigation, symbols, or source-range computation.
 */
class AthenaNavigationIndex(
    private val documentUri: String,
    private val ast: SourceFileAst,
) {
    private val entityDeclarations = ast.declarations.filterIsInstance<EntityDeclaration>().associateBy { declaration -> declaration.name }
    private val portDeclarations = (
        ast.declarations.filterIsInstance<PortDeclaration>() +
            ast.declarations.filterIsInstance<EntityDeclaration>().flatMap { declaration ->
                declaration.nestedPorts +
                    declaration.nestedFunctions.flatMap { function -> function.nestedPorts }
            }
        ).associateBy { declaration ->
        declaration.qualifiedName.parts.joinToString(".")
    }
    private val groupedInterfacePortSpans = ast.declarations
        .filterIsInstance<EntityDeclaration>()
        .flatMap { entity ->
            entity.interfaces.flatMap { connectivityInterface ->
                connectivityInterface.ports.map { port ->
                    "${entity.name}.${port.name}" to port.span
                }
            }
        }
        .toMap()
    private val functionDeclarations = ast.declarations
        .filterIsInstance<EntityDeclaration>()
        .flatMap { entity ->
            entity.nestedFunctions.map { function -> "${entity.name}.${function.name}" to function }
        }
        .toMap()
    private val relationDeclarations = ast.declarations.filterIsInstance<RelationDeclaration>()
    private val connectionDeclarations = ast.declarations.filterIsInstance<ConnectionDeclaration>()
    private val functionReferences = ast.declarations
        .filterIsInstance<LayoutDeclaration>()
        .flatMap { declaration -> declaration.statements }
        .filterIsInstance<LayoutStatement.PlaceAt>()
        .mapNotNull { statement ->
            statement.subject.parts
                .takeIf { parts -> parts.size == 2 }
                ?.joinToString(".")
                ?.let { qualifiedName -> AthenaFunctionReference(qualifiedName, statement.subject.span) }
        }
    private val ownerReferences = buildList {
        ast.declarations.filterIsInstance<PortDeclaration>().forEach { declaration ->
            add(AthenaOwnerReference(declaration.qualifiedName.parts.first(), declaration.ownerSpan()))
        }
        relationDeclarations.forEach { declaration ->
            add(AthenaOwnerReference(declaration.source.parts.first(), declaration.source.ownerSpan()))
            declaration.targets.forEach { target ->
                add(AthenaOwnerReference(target.parts.first(), target.ownerSpan()))
            }
        }
        connectionDeclarations.forEach { declaration ->
            add(AthenaOwnerReference(declaration.source.parts.first(), declaration.source.ownerSpan()))
            add(AthenaOwnerReference(declaration.target.parts.first(), declaration.target.ownerSpan()))
        }
    }
    private val portReferences = buildList {
        relationDeclarations.forEach { declaration ->
            add(AthenaPortReference(declaration.source.parts.joinToString("."), declaration.source.span))
            declaration.targets.forEach { target ->
                add(AthenaPortReference(target.parts.joinToString("."), target.span))
            }
        }
        connectionDeclarations.forEach { declaration ->
            add(AthenaPortReference(declaration.source.parts.joinToString("."), declaration.source.span))
            add(AthenaPortReference(declaration.target.parts.joinToString("."), declaration.target.span))
        }
    }

    /**
     * Resolves the definition target at [offset], if any.
     */
    fun definition(offset: Int): List<Location> {
        val target = targetAt(offset) ?: return emptyList()
        return when (target) {
            is AthenaTarget.Entity -> entityDeclarations[target.name]
                ?.let { declaration -> listOf(documentLocation(declaration.nameSpan())) }
                .orEmpty()

            is AthenaTarget.Port -> portDeclarations[target.qualifiedName]
                ?.qualifiedName
                ?.span
                ?.let { span -> listOf(documentLocation(span)) }
                ?: groupedInterfacePortSpans[target.qualifiedName]
                    ?.let { span -> listOf(documentLocation(span)) }
                .orEmpty()

            is AthenaTarget.Function -> functionDeclarations[target.qualifiedName]
                ?.let { declaration -> listOf(documentLocation(declaration.nameSpan())) }
                .orEmpty()
        }
    }

    /**
     * Resolves all references for the target at [offset].
     */
    fun references(offset: Int, includeDeclaration: Boolean): List<Location> {
        val target = targetAt(offset) ?: return emptyList()
        return when (target) {
            is AthenaTarget.Entity -> buildList {
                if (includeDeclaration) {
                    entityDeclarations[target.name]?.let { declaration -> add(documentLocation(declaration.nameSpan())) }
                }
                ownerReferences
                    .filter { reference -> reference.entityName == target.name }
                    .mapTo(this) { reference -> documentLocation(reference.span) }
            }

            is AthenaTarget.Port -> buildList {
                if (includeDeclaration) {
                    portDeclarations[target.qualifiedName]
                        ?.qualifiedName
                        ?.span
                        ?.let { span -> add(documentLocation(span)) }
                    groupedInterfacePortSpans[target.qualifiedName]?.let { span -> add(documentLocation(span)) }
                }
                portReferences
                    .filter { reference -> reference.qualifiedName == target.qualifiedName }
                    .mapTo(this) { reference -> documentLocation(reference.span) }
            }

            is AthenaTarget.Function -> buildList {
                if (includeDeclaration) {
                    functionDeclarations[target.qualifiedName]
                        ?.let { declaration -> add(documentLocation(declaration.nameSpan())) }
                }
                functionReferences
                    .filter { reference -> reference.qualifiedName == target.qualifiedName }
                    .mapTo(this) { reference -> documentLocation(reference.span) }
            }
        }
    }

    /**
     * Resolves the full authored declaration range for one inspected entity.
     */
    fun entitySourceRange(entityName: String): Range? {
        return entityDeclarations[entityName]?.span?.toLspRange()
    }

    /**
     * Resolves the full authored declaration range for one inspected port.
     */
    fun portSourceRange(qualifiedName: String): Range? {
        return portDeclarations[qualifiedName]?.span?.toLspRange()
            ?: groupedInterfacePortSpans[qualifiedName]?.toLspRange()
    }

    /**
     * Resolves the full authored declaration range for one inspected Relationship.
     */
    fun relationshipSourceRange(definition: String, participantPaths: List<String>): Range? {
        return relationDeclarations
            .firstOrNull { declaration ->
                declaration.word.value == definition &&
                    (listOf(declaration.source) + declaration.targets).map { path -> path.parts.joinToString(".") } == participantPaths
            }
            ?.span
            ?.toLspRange()
    }

    private fun targetAt(offset: Int): AthenaTarget? {
        functionDeclarations.entries
            .firstOrNull { (_, declaration) -> declaration.nameSpan().contains(offset) }
            ?.let { (qualifiedName) -> return AthenaTarget.Function(qualifiedName) }
        functionReferences.firstOrNull { reference -> reference.span.contains(offset) }?.let { reference ->
            return AthenaTarget.Function(reference.qualifiedName)
        }
        entityDeclarations.values.firstOrNull { declaration -> declaration.nameSpan().contains(offset) }?.let { declaration ->
            return AthenaTarget.Entity(declaration.name)
        }
        ownerReferences.firstOrNull { reference -> reference.span.contains(offset) }?.let { reference ->
            return AthenaTarget.Entity(reference.entityName)
        }
        portReferences.firstOrNull { reference -> reference.span.contains(offset) }?.let { reference ->
            return AthenaTarget.Port(reference.qualifiedName)
        }
        return null
    }

    private fun documentLocation(span: SourceSpan): Location {
        return Location(documentUri, span.toLspRange())
    }
}

private fun SourceFileAst.toRepresentationDocumentSymbols(): List<Either<org.eclipse.lsp4j.SymbolInformation, DocumentSymbol>> {
    val declarationSymbols = representationDeclarations.map { declaration -> declaration.toDocumentSymbol() }
    val packageDeclaration = packageDeclaration
    if (packageDeclaration != null) {
        val packageRange = packageDeclaration.span.toLspRange()
        return listOf(
            Either.forRight(
                DocumentSymbol().apply {
                    name = packageDeclaration.name.parts.joinToString(".")
                    kind = SymbolKind.Package
                    detail = "package"
                    range = packageRange
                    selectionRange = packageRange
                    children = declarationSymbols
                },
            ),
        )
    }
    return declarationSymbols.map { symbol -> Either.forRight(symbol) }
}

private fun RepresentationDeclaration.toDocumentSymbol(): DocumentSymbol = when (this) {
    is SymbolDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Class
        detail = "symbol"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            identity?.let { field -> add(field.toDocumentSymbol("identity")) }
            version?.let { field -> add(field.toDocumentSymbol("version")) }
            resources.mapTo(this) { resource -> resource.toResourceDocumentSymbol() }
            graphic?.let { graphic -> add(graphic.toDocumentSymbol()) }
            anchors.mapTo(this) { anchor -> anchor.toDocumentSymbol() }
        }
    }

    is ElementDeclaration -> {
        val element = this
        DocumentSymbol().apply {
        name = element.name
        kind = SymbolKind.Class
        detail = "element"
        range = element.span.toLspRange()
        selectionRange = element.span.toLspRange()
        children = buildList {
            element.identity?.let { field -> add(field.toDocumentSymbol("identity")) }
            element.version?.let { field -> add(field.toDocumentSymbol("version")) }
            element.resources.mapTo(this) { resource -> resource.toResourceDocumentSymbol() }
            element.bounds?.let { bounds ->
                add(
                    DocumentSymbol().apply {
                        name = "bounds"
                        kind = SymbolKind.Property
                        detail = "bounds"
                        range = bounds.span.toLspRange()
                        selectionRange = bounds.span.toLspRange()
                    },
                )
            }
            element.graphic?.let { graphic -> add(graphic.toDocumentSymbol()) }
            element.children.mapTo(this) { child -> child.toDocumentSymbol() }
            element.exportedAnchors.mapTo(this) { export -> export.toDocumentSymbol() }
            element.exportedLabels.mapTo(this) { export -> export.toDocumentSymbol() }
        }
        }
    }

    is MacroDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Module
        detail = "macro"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            identity?.let { add(it.toDocumentSymbol("identity")) }
            version?.let { add(it.toDocumentSymbol("version")) }
            this@toDocumentSymbol.children.mapTo(this) { child ->
                DocumentSymbol().apply {
                    name = child.id
                    kind = SymbolKind.Object
                    detail = "macro child"
                    range = child.span.toLspRange()
                    selectionRange = child.span.toLspRange()
                }
            }
        }
    }

    is VariantDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.EnumMember
        detail = "variant"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            identity?.let { add(it.toDocumentSymbol("identity")) }
            version?.let { add(it.toDocumentSymbol("version")) }
            elementIdentity?.let { add(it.toDocumentSymbol("element")) }
            resources.mapTo(this) { it.toResourceDocumentSymbol() }
        }
    }

    is PlaceholderDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Variable
        detail = "placeholder"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            identity?.let { add(it.toDocumentSymbol("identity")) }
            version?.let { add(it.toDocumentSymbol("version")) }
            targetPath?.let { add(it.toDocumentSymbol("target")) }
            valueType?.let { add(it.toDocumentSymbol("type")) }
        }
    }

    is ProfileDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Namespace
        detail = "profile"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            projection?.let { field -> add(field.toDocumentSymbol("projection")) }
            standard?.let { field -> add(field.toDocumentSymbol("standard")) }
            style?.let { field -> add(field.toDocumentSymbol("style")) }
            fallback?.let { field -> add(field.toDocumentSymbol("fallback")) }
        }
    }

    is BindingDeclaration -> DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Function
        detail = "binding"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
        children = buildList {
            profile?.let { field -> add(field.toDocumentSymbol("profile")) }
            priority?.let { field -> add(field.toDocumentSymbol("priority")) }
            if (selectorKind != null || selectorFacts.isNotEmpty()) {
                val selectorRange = selectorFacts.firstOrNull()?.span ?: span
                add(
                    DocumentSymbol().apply {
                        name = "select ${selectorKind.renderName()}"
                        kind = SymbolKind.Interface
                        detail = "binding selector"
                        range = selectorRange.toLspRange()
                        selectionRange = selectorRange.toLspRange()
                        children = selectorFacts.map { fact -> fact.toDocumentSymbol() }
                    },
                )
            }
            useElement?.let { field -> add(field.toDocumentSymbol("use element")) }
            variant?.let { field -> add(field.toDocumentSymbol("variant")) }
        }
    }
}

private fun SymbolGraphicDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = if (svgResource != null) "graphic svg resource" else "graphic"
    kind = SymbolKind.Object
    detail = "graphic"
    range = span.toLspRange()
    selectionRange = span.toLspRange()
    children = buildList {
        bounds?.let { bounds ->
            add(
                DocumentSymbol().apply {
                    name = "bounds"
                    kind = SymbolKind.Property
                    detail = "bounds"
                    range = bounds.span.toLspRange()
                    selectionRange = bounds.span.toLspRange()
                },
            )
        }
        primitives.mapTo(this) { primitive -> primitive.toDocumentSymbol() }
        labels.mapTo(this) { label -> label.toDocumentSymbol() }
    }
}

private fun RepresentationResourceDeclaration.toResourceDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = "resource $id"
    kind = SymbolKind.Object
    detail = "resource ${kind.name.lowercase()}"
    range = span.toLspRange()
    selectionRange = span.toLspRange()
    children = buildList {
        add(
            DocumentSymbol().apply {
                name = "kind"
                kind = SymbolKind.Property
                detail = this@toResourceDocumentSymbol.kind.name.lowercase()
                range = span.toLspRange()
                selectionRange = span.toLspRange()
            },
        )
        add(path.toDocumentSymbol("path"))
    }
}

private fun SymbolGraphicPrimitiveDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    val type = when (this@toDocumentSymbol) {
        is SymbolGraphicPrimitiveDeclaration.Line -> "line"
        is SymbolGraphicPrimitiveDeclaration.Polyline -> "polyline"
        is SymbolGraphicPrimitiveDeclaration.Arc -> "arc"
        is SymbolGraphicPrimitiveDeclaration.Circle -> "circle"
        is SymbolGraphicPrimitiveDeclaration.Rectangle -> "rectangle"
    }
    name = "$type ${this@toDocumentSymbol.id}"
    kind = SymbolKind.Field
    detail = type
    range = span.toLspRange()
    selectionRange = span.toLspRange()
}

private fun SymbolDynamicLabelDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = "label ${this@toDocumentSymbol.id}"
    kind = SymbolKind.Field
    detail = "dynamic label: ${role.value}"
    range = span.toLspRange()
    selectionRange = span.toLspRange()
}

private fun com.engineeringood.athena.language.SymbolStringField.toDocumentSymbol(name: String): DocumentSymbol =
    DocumentSymbol().apply {
        this.name = name
        kind = SymbolKind.Property
        detail = value
        range = span.toLspRange()
        selectionRange = span.toLspRange()
    }

private fun SymbolIdentifierField.toDocumentSymbol(name: String): DocumentSymbol = DocumentSymbol().apply {
    this.name = name
    kind = SymbolKind.Property
    detail = value
    range = span.toLspRange()
    selectionRange = span.toLspRange()
}

private fun ElementNumberField.toDocumentSymbol(name: String): DocumentSymbol = DocumentSymbol().apply {
    this.name = name
    kind = SymbolKind.Number
    detail = value.toString()
    range = span.toLspRange()
    selectionRange = span.toLspRange()
}

private fun SymbolAnchorDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = id
    kind = SymbolKind.Field
    detail = "anchor"
    range = span.toLspRange()
    selectionRange = span.toLspRange()
}

private fun ElementChildDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = "child $id"
    kind = SymbolKind.Object
    detail = symbolIdentity?.value ?: "child"
    range = span.toLspRange()
    selectionRange = headerSpan.toLspRange()
}

private fun ElementAnchorExportDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = "export anchor $id"
    kind = SymbolKind.Field
    detail = "${childId.value}.${childAnchorId.value}"
    range = span.toLspRange()
    selectionRange = referenceSpan.toLspRange()
}

private fun ElementLabelExportDeclaration.toDocumentSymbol(): DocumentSymbol = DocumentSymbol().apply {
    name = "export label $id"
    kind = SymbolKind.Field
    detail = "${childId.value}.${childLabelId.value}"
    range = span.toLspRange()
    selectionRange = referenceSpan.toLspRange()
}

private fun BindingSelectorKind?.renderName(): String = when (this) {
    BindingSelectorKind.Function -> "function"
    BindingSelectorKind.Entity,
    null,
        -> "entity"
}

/**
 * Target kinds the M4 navigation layer can resolve in a single document.
 */
sealed interface AthenaTarget {
    /**
     * Entity declaration or owner reference target.
     */
    data class Entity(val name: String) : AthenaTarget

    /**
     * Port declaration or endpoint reference target.
     */
    data class Port(val qualifiedName: String) : AthenaTarget

    /** Nested engineering-function declaration or authored occurrence target. */
    data class Function(val qualifiedName: String) : AthenaTarget
}

/**
 * Parsed completion context around one cursor position in an authored document.
 */
data class AthenaCompletionContext(
    val fragment: String,
    val expectsQualifiedPort: Boolean,
    val expectsDirectionValue: Boolean,
    val expectsFlowValue: Boolean,
    val expectsConceptValue: Boolean,
    val expectsOrientationValue: Boolean,
    val insideEntityBlock: Boolean,
    val insidePortBlock: Boolean,
    val insideFunctionBlock: Boolean,
)

/**
 * One owner-segment reference to a entity symbol.
 */
data class AthenaOwnerReference(
    val entityName: String,
    val span: SourceSpan,
)

/**
 * One qualified port reference occurrence.
 */
data class AthenaPortReference(
    val qualifiedName: String,
    val span: SourceSpan,
)

/** One authored occurrence reference to a nested engineering function. */
data class AthenaFunctionReference(
    val qualifiedName: String,
    val span: SourceSpan,
)

private fun Declaration.toDocumentSymbol(): DocumentSymbol {
    return when (this) {
        is EntityDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Class
            detail = "entity"
            range = span.toLspRange()
            selectionRange = nameSpan().toLspRange()
            children = fields.map { field -> field.toDocumentSymbol() } +
                nestedPorts.map { port -> port.toDocumentSymbol(displayName = port.qualifiedName.parts.last()) } +
                nestedFunctions.map { function -> function.toDocumentSymbol() }
        }

        is PortDeclaration -> toDocumentSymbol(displayName = qualifiedName.parts.joinToString("."))

        is ConnectionDeclaration -> DocumentSymbol().apply {
            name = "connect ${this@toDocumentSymbol.kind.value} ${source.parts.joinToString(".")} to ${target.parts.joinToString(".")}"
            kind = SymbolKind.Interface
            detail = "engineering connection"
            range = span.toLspRange()
            selectionRange = this@toDocumentSymbol.kind.span.toLspRange()
        }

        is NetDeclaration -> DocumentSymbol().apply {
            name = "net ${this@toDocumentSymbol.name}"
            kind = SymbolKind.Interface
            detail = "engineering net"
            range = span.toLspRange()
            selectionRange = this@toDocumentSymbol.kind.span.toLspRange()
        }

        is ConnectionSpecificationDeclaration -> DocumentSymbol().apply {
            name = "connection-spec ${this@toDocumentSymbol.scope.name.lowercase()}"
            kind = SymbolKind.Property
            detail = "connection specification"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }

        is RelationDeclaration -> DocumentSymbol().apply {
            val targetLabel = if (targets.size == 1) {
                targets.single().parts.joinToString(".")
            } else {
                targets.joinToString(prefix = "[", postfix = "]") { target -> target.parts.joinToString(".") }
            }
            name = "${word.value} ${source.parts.joinToString(".")} to $targetLabel"
            kind = SymbolKind.Operator
            detail = "relation"
            range = span.toLspRange()
            selectionRange = word.span.toLspRange()
        }

        is ExternalEvidenceDeclaration -> DocumentSymbol().apply {
            name = "evidence ${this@toDocumentSymbol.name}"
            kind = SymbolKind.Object
            detail = "external evidence"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
            children = listOf(
                namespace.toDocumentSymbol("namespace"),
                reference.toDocumentSymbol("reference"),
                DocumentSymbol().apply {
                    name = "subject ${subject.kind.name.lowercase()} ${subject.target.parts.joinToString(".")}"
                    kind = SymbolKind.Key
                    detail = "subject"
                    range = subject.span.toLspRange()
                    selectionRange = subject.span.toLspRange()
                },
                provenance.toDocumentSymbol("provenance"),
            )
        }

        is ProjectionPolicyDeclaration -> DocumentSymbol().apply {
            name = "projection ${this@toDocumentSymbol.name}"
            kind = SymbolKind.Object
            detail = "projection policy"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
            children = listOfNotNull(
                target?.toDocumentSymbol("target"),
                layoutStrategy?.toDocumentSymbol("layout"),
                drawingProfile?.toDocumentSymbol("drawingProfile"),
                routeQualityPolicy?.toDocumentSymbol("routeQuality"),
            ) + proofObligations.map { proof -> proof.toDocumentSymbol("proof") }
        }

        is LayoutDeclaration -> DocumentSymbol().apply {
            name = viewFamily
            kind = SymbolKind.Module
            detail = "layout"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
            children = statements.map { statement -> statement.toDocumentSymbol() }
        }

        is InstallationDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Module
            detail = "installation cabinet"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
            children = buildList {
                enclosures.mapTo(this) { member -> member.toInstallationDocumentSymbol("enclosure") }
                surfaces.mapTo(this) { member -> member.toInstallationDocumentSymbol("surface") }
                rails.mapTo(this) { member -> member.toInstallationDocumentSymbol("rail") }
                ducts.mapTo(this) { member -> member.toInstallationDocumentSymbol("duct") }
                channels.mapTo(this) { member -> member.toInstallationDocumentSymbol("channel") }
                terminalGroups.mapTo(this) { member -> member.toInstallationDocumentSymbol("terminal-group") }
                mounts.mapTo(this) { member -> member.toInstallationDocumentSymbol("mount") }
                routes.mapTo(this) { route ->
                    DocumentSymbol().apply {
                        name = "route ${route.relationshipId}"
                        kind = SymbolKind.Operator
                        detail = "route"
                        range = route.span.toLspRange()
                        selectionRange = route.span.toLspRange()
                    }
                }
            }
        }

        is ViewDeclaration -> DocumentSymbol().apply {
            name = "view ${this@toDocumentSymbol.name}"
            kind = SymbolKind.Object
            detail = "projection view"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
            children = sheets.map { sheet -> sheet.toDocumentSymbol() }
        }

        is SheetDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Module
            detail = "projection sheet"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }

        is GridDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Module
            detail = "sheet grid"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }

        is RegionDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Object
            detail = "functional region"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }

        is ProjectionConstructDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name ?: this@toDocumentSymbol.kind
            kind = SymbolKind.Object
            detail = "projection construct (${this@toDocumentSymbol.kind})"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }
    }
}

private fun Any.toInstallationDocumentSymbol(kindName: String): DocumentSymbol {
    val (id, span) = when (this) {
        is com.engineeringood.athena.language.InstallationEnclosureDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationSurfaceDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationRailDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationDuctDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationChannelDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationTerminalGroupDeclaration -> id to span
        is com.engineeringood.athena.language.InstallationMountDeclaration -> id to span
        else -> error("Unsupported installation document symbol member: ${this::class.simpleName}")
    }
    return DocumentSymbol().apply {
        name = "$kindName $id"
        kind = SymbolKind.Object
        detail = kindName
        range = span.toLspRange()
        selectionRange = span.toLspRange()
    }
}

private fun PortDeclaration.toDocumentSymbol(displayName: String): DocumentSymbol {
    return DocumentSymbol().apply {
        name = displayName
        kind = SymbolKind.Field
        detail = "port"
        range = span.toLspRange()
        selectionRange = qualifiedName.span.toLspRange()
        children = fields.map { field -> field.toDocumentSymbol() }
    }
}

private fun EngineeringFunctionDeclaration.toDocumentSymbol(): DocumentSymbol {
    val declaration = this
    return DocumentSymbol().apply {
        name = declaration.name
        kind = SymbolKind.Function
        detail = "engineering function: ${declaration.role.parts.joinToString(".")}"
        range = declaration.span.toLspRange()
        selectionRange = declaration.nameSpan().toLspRange()
        children = listOf(
            DocumentSymbol().apply {
                name = "role"
                kind = SymbolKind.Property
                detail = declaration.role.parts.joinToString(".")
                range = declaration.role.span.toLspRange()
                selectionRange = declaration.role.span.toLspRange()
            },
        ) + declaration.nestedPorts.map { port ->
            port.toDocumentSymbol(displayName = port.qualifiedName.parts.last())
        }
    }
}

private fun LayoutStatement.toDocumentSymbol(): DocumentSymbol {
    return DocumentSymbol().apply {
        name = when (val statement = this@toDocumentSymbol) {
            is LayoutStatement.PlaceNear -> "place ${statement.subject} near ${statement.target}"
            is LayoutStatement.PlaceBelow -> "place ${statement.subject} below ${statement.target}"
            is LayoutStatement.AlignWith ->
                "align ${statement.subject} aligned-with ${statement.target} axis ${statement.axis.name.lowercase()}"
            is LayoutStatement.GroupWith -> "group ${statement.subject} grouped-with ${statement.target}"
            is LayoutStatement.PlaceAt ->
                "place ${statement.subject.parts.joinToString(".")} at " +
                    "(${statement.position.column}, ${statement.position.row}) orientation " +
                    statement.orientation.name.lowercase()
        }
        kind = SymbolKind.Property
        detail = "layout statement"
        range = span.toLspRange()
        selectionRange = span.toLspRange()
    }
}

private fun PropertyAssignment.toDocumentSymbol(): DocumentSymbol {
    val propertyValue = value
    return DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Property
        detail = when (propertyValue) {
            is ScalarValue.Symbol -> propertyValue.text
            is ScalarValue.Text -> "\"${propertyValue.text}\""
            is ScalarValue.Quantity -> "${propertyValue.exactText} [${propertyValue.unit.parts.joinToString(".")}]"
            is ScalarValue.Integer -> propertyValue.exactText
            is ScalarValue.Boolean -> propertyValue.value.toString()
            is ScalarValue.Reference -> "@${propertyValue.target.parts.joinToString(".")}"
        }
        range = span.toLspRange()
        selectionRange = span.toLspRange()
    }
}

private fun EntityDeclaration.nameSpan(): SourceSpan {
    val start = SourcePosition(
        offset = span.start.offset + "entity ".length,
        line = span.start.line,
        column = span.start.column + "entity ".length,
    )
    val end = start.advanceBy(name)
    return SourceSpan(start, end)
}

private fun EngineeringFunctionDeclaration.nameSpan(): SourceSpan {
    val start = SourcePosition(
        offset = span.start.offset + "function ".length,
        line = span.start.line,
        column = span.start.column + "function ".length,
    )
    return SourceSpan(start, start.advanceBy(name))
}

private fun PortDeclaration.ownerSpan(): SourceSpan {
    val qualifiedStart = SourcePosition(
        offset = span.start.offset + "port ".length,
        line = span.start.line,
        column = span.start.column + "port ".length,
    )
    val owner = qualifiedName.parts.first()
    return SourceSpan(qualifiedStart, qualifiedStart.advanceBy(owner))
}

private fun QualifiedName.ownerSpan(): SourceSpan {
    val owner = parts.first()
    return SourceSpan(span.start, span.start.advanceBy(owner))
}

private fun List<com.engineeringood.athena.ir.EngineeringProperty>.summaryText(): String {
    return joinToString(separator = ", ") { property ->
        "${property.name}=${property.value.summaryText()}"
    }.ifBlank { "no properties" }
}

private fun EngineeringValue.summaryText(): String {
    return when (this) {
        is EngineeringValue.Quantity -> "$value [${unit.authoredName.joinToString(".")}]"
        is EngineeringValue.Integer -> value.toString()
        is EngineeringValue.Boolean -> value.toString()
        is EngineeringValue.Symbol -> text
        is EngineeringValue.Text -> "\"$text\""
        is EngineeringValue.Reference -> "@${reference.authoredPath.joinToString(".")}"
    }
}

private fun com.engineeringood.athena.ir.EngineeringProperty.toInspectionProperty(): AthenaSemanticInspectionProperty {
    return AthenaSemanticInspectionProperty(
        name = name,
        valueKind = when (value) {
            is EngineeringValue.Quantity -> "quantity"
            is EngineeringValue.Integer -> "integer"
            is EngineeringValue.Boolean -> "boolean"
            is EngineeringValue.Symbol -> "symbol"
            is EngineeringValue.Text -> "text"
            is EngineeringValue.Reference -> "reference"
        },
        valueText = when (val propertyValue = value) {
            is EngineeringValue.Quantity -> "${propertyValue.value} [${propertyValue.unit.authoredName.joinToString(".")}]"
            is EngineeringValue.Integer -> propertyValue.value.toString()
            is EngineeringValue.Boolean -> propertyValue.value.toString()
            is EngineeringValue.Symbol -> propertyValue.text
            is EngineeringValue.Text -> propertyValue.text
            is EngineeringValue.Reference -> propertyValue.reference.authoredPath.joinToString(".")
        },
    )
}

private fun com.engineeringood.athena.ir.EngineeringReference.authoredPath(): String = authoredPath.joinToString(".")

private fun com.engineeringood.athena.ir.EngineeringPort.summaryPath(): String =
    (owner.reference.authoredPath + name).joinToString(".")


private fun requireSourceRange(
    semanticId: String,
    kind: String,
    range: Range?,
): Range {
    return requireNotNull(range) {
        "Athena semantic inspection could not resolve the authored $kind range for `$semanticId`."
    }
}

private fun String.cursorContext(
    position: Position,
    ast: SourceFileAst?,
): AthenaCompletionContext {
    val offset = offsetAt(position)
    val lineText = lineTextAt(position.line)
    val prefixLength = position.character.coerceIn(0, lineText.length)
    val linePrefix = lineText.substring(0, prefixLength)
    val fragment = Regex("[A-Za-z0-9_.]+$").find(linePrefix)?.value.orEmpty()
    val trimmedPrefix = linePrefix.trimStart()
    val enclosingDeclaration = ast.declarationAt(offset)
    val enclosingPort = ast.nestedPortAt(offset)
    val enclosingFunction = ast.functionAt(offset)

    return AthenaCompletionContext(
        fragment = fragment,
        expectsQualifiedPort = trimmedPrefix.startsWith("port ") ||
            trimmedPrefix.startsWith("connect ") ||
            trimmedPrefix.contains("->"),
        expectsDirectionValue = trimmedPrefix.matches(Regex(".*\\bdirection\\s+[A-Za-z_]*$")),
        expectsFlowValue = trimmedPrefix.matches(Regex(".*\\bflow\\s+[A-Za-z_]*$")),
        expectsConceptValue = trimmedPrefix.matches(Regex(".*\\bconcept\\s+[A-Za-z_]*$")),
        expectsOrientationValue = trimmedPrefix.matches(Regex(".*\\borientation\\s+[A-Za-z_]*$")),
        insideEntityBlock = enclosingDeclaration is EntityDeclaration &&
            enclosingPort == null &&
            enclosingFunction == null,
        insidePortBlock = enclosingDeclaration is PortDeclaration || enclosingPort != null,
        insideFunctionBlock = enclosingFunction != null,
    )
}

private fun SourceFileAst?.declarationAt(offset: Int): Declaration? {
    return this?.declarations?.firstOrNull { declaration -> declaration.span.contains(offset) }
}

private fun SourceFileAst?.nestedPortAt(offset: Int): PortDeclaration? = this
    ?.declarations
    ?.filterIsInstance<EntityDeclaration>()
    ?.flatMap { declaration -> declaration.nestedPorts }
    ?.firstOrNull { declaration -> declaration.span.contains(offset) }

private fun SourceFileAst?.functionAt(offset: Int): EngineeringFunctionDeclaration? = this
    ?.declarations
    ?.filterIsInstance<EntityDeclaration>()
    ?.flatMap { declaration -> declaration.nestedFunctions }
    ?.firstOrNull { declaration -> declaration.span.contains(offset) }

private fun String.offsetAt(position: Position): Int {
    var line = 0
    var offset = 0
    while (line < position.line && offset < length) {
        if (this[offset] == '\n') {
            line += 1
        }
        offset += 1
    }
    return (offset + position.character).coerceAtMost(length)
}

private fun String.lineTextAt(lineNumber: Int): String {
    return lineSequence().drop(lineNumber).firstOrNull().orEmpty()
}

private fun String.hasPackageAwareSyntax(): Boolean {
    return lineSequence().any { line ->
        val trimmed = line.trimStart()
        trimmed.startsWith("package ") || trimmed.startsWith("import ")
    }
}

private fun String.hasRepresentationSyntax(): Boolean {
    return lineSequence().any { line ->
        val trimmed = line.trimStart()
        trimmed.startsWith("symbol ") ||
            trimmed.startsWith("element ") ||
            trimmed.startsWith("graphic svg ") ||
            trimmed.startsWith("descriptor ")
    }
}

private fun String.fullDocumentRange(): Range {
    val lines = split('\n')
    val lastLine = (lines.size - 1).coerceAtLeast(0)
    val lastCharacter = lines.lastOrNull()?.length ?: 0
    return Range(Position(0, 0), Position(lastLine, lastCharacter))
}

private val semanticTokenTypes = listOf(
    "keyword",
    "type",
    "property",
    "string",
    "number",
    "operator",
    "namespace",
    "variable",
    "athenaDeclarationKeyword",
    "athenaPortKeyword",
    "athenaRelationshipKeyword",
    "athenaFunctionKeyword",
    "athenaLayoutKeyword",
    "athenaLayoutOperator",
    "athenaRepresentationKeyword",
    "athenaPrimitiveKeyword",
    "athenaProfileKeyword",
    "athenaBindingKeyword",
)

internal val athenaSemanticTokenTypes: List<String> = semanticTokenTypes

private val declarationKeywordTokens = setOf(
    "package",
    "import",
    "system",
    "entity",
    "port",
    "concept",
    "model",
    "designationType",
    "designation",
)

private val portKeywordTokens = setOf(
    "direction",
    "flow",
    "in",
    "out",
    "bidirectional",
)

private val functionKeywordTokens = setOf(
    "function",
    "role",
    "port",
)

private val relationshipKeywordTokens = setOf(
    "connect",
    "connection-spec",
    "net",
    "source",
    "sink",
    "pass",
    "potential",
    "signal",
    "to",
)

private val layoutKeywordTokens = setOf(
    "layout",
    "place",
    "near",
    "below",
    "at",
    "orientation",
    "align",
    "axis",
    "group",
    "horizontal",
    "vertical",
)

private val installationKeywordTokens = setOf(
    "installation",
    "cabinet",
    "enclosure",
    "surface",
    "rail",
    "duct",
    "channel",
    "terminal-group",
    "mount",
    "route",
    "in",
    "on",
    "at",
    "size",
    "accepts",
    "length",
    "orientation",
    "horizontal",
    "vertical",
    "mounting",
    "wall",
    "lanes",
    "margin",
    "entity",
    "through",
)

private val layoutOperatorTokens = setOf("aligned-with", "grouped-with")

private val representationKeywordTokens = setOf(
    "symbol",
    "element",
    "resource",
    "identity",
    "version",
    "graphic",
    "svg",
    "kind",
    "path",
    "anchor",
    "ref",
    "accepts",
    "child",
    "translate",
    "rotate",
    "scale",
    "zOrder",
    "export",
)

private val primitiveKeywordTokens = setOf(
    "bounds",
    "line",
    "polyline",
    "points",
    "arc",
    "center",
    "radius",
    "sweep",
    "circle",
    "rectangle",
    "label",
    "from",
    "to",
    "at",
    "size",
    "style",
    "point",
)

private val profileKeywordTokens = setOf(
    "profile",
    "projection",
    "standard",
    "fallback",
    "fail-closed",
)

private val bindingKeywordTokens = setOf(
    "binding",
    "priority",
    "select",
    "where",
    "use",
    "variant",
)

private fun String.semanticTokenData(): List<Int> {
    data class Token(val line: Int, val start: Int, val length: Int, val typeIndex: Int)

    val tokens = mutableListOf<Token>()
    lineSequence().forEachIndexed { lineIndex, line ->
        Regex("\"[^\"\\n]*\"|->|-?\\d+(?:\\.\\d+)?|[A-Za-z_][A-Za-z0-9_.-]*").findAll(line).forEach { match ->
            val text = match.value
            val type = when {
                text == "->" -> "operator"
                text.startsWith("\"") -> "string"
                text.first().isDigit() || text.first() == '-' -> "number"
                text in layoutOperatorTokens -> "athenaLayoutOperator"
                line.isBindingSelectorLine() && text in setOf("select", "entity", "function", "where") ->
                    "athenaBindingKeyword"
                line.isBindingElementLine() && text in setOf("use", "element", "version") ->
                    "athenaBindingKeyword"
                line.isProfileStyleLine() && text == "style" -> "athenaProfileKeyword"
                line.isPrimitiveLine() && text in primitiveKeywordTokens -> "athenaPrimitiveKeyword"
                line.isInstallationLine() && text in installationKeywordTokens -> "athenaLayoutKeyword"
                text in declarationKeywordTokens -> "athenaDeclarationKeyword"
                text in portKeywordTokens -> "athenaPortKeyword"
                text in relationshipKeywordTokens -> "athenaRelationshipKeyword"
                text in functionKeywordTokens -> "athenaFunctionKeyword"
                text in layoutKeywordTokens -> "athenaLayoutKeyword"
                text in representationKeywordTokens -> "athenaRepresentationKeyword"
                text in primitiveKeywordTokens -> "athenaPrimitiveKeyword"
                text in profileKeywordTokens -> "athenaProfileKeyword"
                text in bindingKeywordTokens -> "athenaBindingKeyword"
                "." in text -> "namespace"
                else -> "variable"
            }
            tokens += Token(lineIndex, match.range.first, text.length, semanticTokenTypes.indexOf(type))
        }
    }
    var previousLine = 0
    var previousStart = 0
    return buildList {
        tokens.sortedWith(compareBy<Token> { it.line }.thenBy { it.start }).forEach { token ->
            val deltaLine = token.line - previousLine
            val deltaStart = if (deltaLine == 0) token.start - previousStart else token.start
            add(deltaLine)
            add(deltaStart)
            add(token.length)
            add(token.typeIndex)
            add(0)
            previousLine = token.line
            previousStart = token.start
        }
    }
}

private fun String.isBindingSelectorLine(): Boolean = trimStart().startsWith("select ")

private fun String.isBindingElementLine(): Boolean = trimStart().startsWith("use element ")

private fun String.isProfileStyleLine(): Boolean = trimStart().startsWith("style ")

private fun String.isPrimitiveLine(): Boolean {
    val firstWord = trimStart().substringBefore(' ')
    return firstWord in setOf("bounds", "line", "polyline", "arc", "circle", "rectangle", "label")
}

private fun String.isInstallationLine(): Boolean {
    val firstWord = trimStart().substringBefore(' ')
    return firstWord in setOf(
        "installation",
        "enclosure",
        "surface",
        "rail",
        "duct",
        "channel",
        "terminal-group",
        "mount",
        "route",
    )
}

private fun SourceSpan.toLspRange(): Range {
    return Range(
        Position((start.line - 1).coerceAtLeast(0), (start.column - 1).coerceAtLeast(0)),
        Position((end.line - 1).coerceAtLeast(0), (end.column - 1).coerceAtLeast(0)),
    )
}

private fun Path.sourceRootRelativePath(sourceRoot: Path): String? {
    val normalizedSourceRoot = sourceRoot.toAbsolutePath().normalize()
    val normalizedPath = toAbsolutePath().normalize()
    if (!normalizedPath.startsWith(normalizedSourceRoot)) {
        return null
    }
    val relative = normalizedSourceRoot.relativize(normalizedPath).toString()
        .replace(File.separatorChar, '/')
    return relative.takeIf { it.isNotBlank() && !it.startsWith("..") }
}

private fun Path.isProjectSemanticSource(): Boolean {
    val name = fileName.toString()
    return name.endsWith(".athena") &&
        !name.endsWith(".sheet.athena") &&
        !name.endsWith(".sheet.style.athena") &&
        !name.endsWith(".binding.athena")
}

private fun Path.resolveSourceUnitUri(sourceRootRelativePath: String): String {
    return resolve(sourceRootRelativePath.replace('/', File.separatorChar))
        .toAbsolutePath()
        .normalize()
        .toUri()
        .toString()
}

private fun SourcePosition.advanceBy(text: String): SourcePosition {
    return copy(
        offset = offset + text.length,
        column = column + text.length,
    )
}

private fun SourceSpan.contains(offset: Int): Boolean = offset in start.offset until end.offset

private fun SheetCompanionParseResult.toDocumentSymbols(): List<Either<org.eclipse.lsp4j.SymbolInformation, DocumentSymbol>> {
    val source = (this as? SheetCompanionParseSuccess)?.source ?: return emptyList()
    fun symbol(name: String, detail: String, span: SourceSpan, kind: SymbolKind): DocumentSymbol =
        DocumentSymbol().apply {
            this.name = name
            this.detail = detail
            this.kind = kind
            this.range = span.toLspRange()
            this.selectionRange = span.toLspRange()
        }

    val children = buildList {
        add(symbol("page ${source.page.format} ${source.page.orientation.name.lowercase()}", "page", source.page.span, SymbolKind.Namespace))
        add(symbol("frame ${source.frame.columns}x${source.frame.rows}", "frame", source.frame.span, SymbolKind.Struct))
        add(symbol("snap ${source.snap.step}", "snap", source.snap.span, SymbolKind.Struct))
        source.title?.let { title -> add(symbol("title ${title.text}", "title", title.span, SymbolKind.String)) }
        source.placements.forEach { placement ->
            add(symbol("${placement.occurrence} at (${placement.point.x}, ${placement.point.y})", "placement", placement.span, SymbolKind.Field))
        }
    }
    val root = symbol("sheet ${source.name}", "Sheet Companion", source.span, SymbolKind.File).apply {
        this.children = children
    }
    return listOf(Either.forRight(root))
}
