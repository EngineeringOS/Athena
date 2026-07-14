package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.ir.EngineeringPropertyValue
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
    val knowledgeInspection: AthenaEngineeringKnowledgeInspectionPayload? = null,
    val componentCount: Int,
    val portCount: Int,
    val connectionCount: Int,
    val components: List<AthenaSemanticInspectionComponent>,
    val ports: List<AthenaSemanticInspectionPort>,
    val connections: List<AthenaSemanticInspectionConnection>,
)

/**
 * One read-only component entry inside the semantic inspection payload.
 */
data class AthenaSemanticInspectionComponent(
    val semanticId: String,
    val name: String,
    val kind: String,
    val properties: String,
    val authoredProperties: List<AthenaSemanticInspectionProperty>,
    val sourceRange: Range,
)

/**
 * One structured authored component property published for guided inspector flows.
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
 * One read-only connection entry inside the semantic inspection payload.
 */
data class AthenaSemanticInspectionConnection(
    val semanticId: String,
    val fromPath: String,
    val toPath: String,
    val sourceRange: Range,
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

        val compilation = compiler.compile(path, text)
        val success = compilation as? CompilerCompilationSuccess
        val tracked = AthenaTrackedDocument(
            uri = uri,
            path = path,
            version = version,
            text = text,
            compilation = compilation,
            navigationIndex = success?.let { result ->
                AthenaNavigationIndex(uri, result.source.ast)
            },
        )
        documentsByUri[uri] = tracked
        return tracked
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

            cursor.expectsSignalValue -> completionItems(
                "Digital" to CompletionItemKind.EnumMember,
            )

            cursor.expectsTypeValue -> {
                val knownTypes = success
                    ?.source
                    ?.ast
                    ?.declarations
                    ?.asSequence()
                    ?.filterIsInstance<DeviceDeclaration>()
                    ?.flatMap { declaration ->
                        declaration.fields.asSequence()
                            .filter { field -> field.name == "type" }
                            .mapNotNull { field -> (field.value as? ScalarValue.Identifier)?.text }
                    }
                    ?.distinct()
                    ?.toList()
                    .orEmpty()
                completionItems(*(knownTypes.ifEmpty { listOf("Motor", "Switch") }
                    .map { type -> type to CompletionItemKind.Class }
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

            cursor.insideDeviceBlock -> completionItems(
                "type" to CompletionItemKind.Property,
                "model" to CompletionItemKind.Property,
            )

            cursor.insidePortBlock -> completionItems(
                "direction" to CompletionItemKind.Property,
                "signal" to CompletionItemKind.Property,
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
        return listOf(Either.forRight(systemSymbol))
    }

    /**
     * Resolves the definition locations for the symbol addressed by [uri] and [position].
     */
    fun definition(uri: String, position: Position): List<Location> {
        val tracked = trackedDocument(uri) ?: return emptyList()
        val offset = tracked.text.offsetAt(position)
        val index = tracked.navigationIndex ?: return emptyList()
        return index.definition(offset)
    }

    /**
     * Resolves references for the symbol addressed by [params].
     */
    fun references(params: ReferenceParams): List<Location> {
        val tracked = trackedDocument(params.textDocument.uri) ?: return emptyList()
        val offset = tracked.text.offsetAt(params.position)
        val index = tracked.navigationIndex ?: return emptyList()
        return index.references(
            offset = offset,
            includeDeclaration = params.context?.isIncludeDeclaration ?: false,
        )
    }

    /**
     * Builds a read-only semantic inspection snapshot for the latest tracked document at [uri].
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
                knowledgeInspection = null,
                componentCount = 0,
                portCount = 0,
                connectionCount = 0,
                components = emptyList(),
                ports = emptyList(),
                connections = emptyList(),
            )

            is CompilerCompilationSuccess -> {
                val document = compilation.document
                val navigationIndex = tracked.navigationIndex
                val visibleDiagnostics = (
                    compilation.semanticResult.diagnostics +
                        compilation.validationBreakdown.engineeringSufficiencyDiagnostics
                    ).distinct()
                val knowledgeDiagnostics = compilation.validationBreakdown.engineeringSufficiencyDiagnostics
                    .distinct()
                    .sortedWith(knowledgeDiagnosticComparator())
                AthenaSemanticInspectionPayload(
                    uri = tracked.uri,
                    version = tracked.version,
                    status = if (visibleDiagnostics.isEmpty()) "ready" else "diagnostics",
                    systemName = document.system.name,
                    diagnosticsCount = visibleDiagnostics.size,
                    diagnosticSummaries = visibleDiagnostics.map { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                    knowledgeInspection = AthenaEngineeringKnowledgeInspectionPayload(
                        derivedSubjectCount = compilation.derivedContext.subjects.size,
                        capabilityFactCount = compilation.capabilityFacts.subjects.sumOf { subject -> subject.facts.size },
                        constraintEvaluationCount = compilation.constraintEvaluations.subjects.sumOf { subject -> subject.evaluations.size },
                        knowledgeDiagnosticsCount = knowledgeDiagnostics.size,
                        knowledgeDiagnostics = knowledgeDiagnostics.map { diagnostic -> diagnostic.toKnowledgePayload() },
                    ),
                    componentCount = document.components.size,
                    portCount = document.ports.size,
                    connectionCount = document.connections.size,
                    components = document.components
                        .sortedBy { component -> component.name }
                        .map { component ->
                            AthenaSemanticInspectionComponent(
                                semanticId = component.id.value,
                                name = component.name,
                                kind = component.kind,
                                properties = component.properties.summaryText(),
                                authoredProperties = component.properties.map { property -> property.toInspectionProperty() },
                                sourceRange = requireSourceRange(
                                    semanticId = component.id.value,
                                    kind = "component",
                                    range = navigationIndex?.componentSourceRange(component.name),
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
                    connections = document.connections
                        .sortedWith(compareBy(
                            { connection -> connection.from.authoredPath() },
                            { connection -> connection.to.authoredPath() },
                        ))
                        .map { connection ->
                            AthenaSemanticInspectionConnection(
                                semanticId = connection.id.value,
                                fromPath = connection.from.authoredPath(),
                                toPath = connection.to.authoredPath(),
                                sourceRange = requireSourceRange(
                                    semanticId = connection.id.value,
                                    kind = "connection",
                                    range = navigationIndex?.connectionSourceRange(
                                        fromPath = connection.from.authoredPath(),
                                        toPath = connection.to.authoredPath(),
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
            "device" to CompletionItemKind.Keyword,
            "port" to CompletionItemKind.Keyword,
            "connect" to CompletionItemKind.Keyword,
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
 * Small server-owned navigation index derived from the current source AST.
 */
class AthenaNavigationIndex(
    private val documentUri: String,
    private val ast: SourceFileAst,
) {
    private val deviceDeclarations = ast.declarations.filterIsInstance<DeviceDeclaration>().associateBy { declaration -> declaration.name }
    private val portDeclarations = ast.declarations.filterIsInstance<PortDeclaration>().associateBy { declaration ->
        declaration.qualifiedName.parts.joinToString(".")
    }
    private val ownerReferences = buildList {
        ast.declarations.filterIsInstance<PortDeclaration>().forEach { declaration ->
            add(AthenaOwnerReference(declaration.qualifiedName.parts.first(), declaration.ownerSpan()))
        }
        ast.declarations.filterIsInstance<ConnectionDeclaration>().forEach { declaration ->
            add(AthenaOwnerReference(declaration.from.parts.first(), declaration.from.ownerSpan()))
            add(AthenaOwnerReference(declaration.to.parts.first(), declaration.to.ownerSpan()))
        }
    }
    private val portReferences = buildList {
        ast.declarations.filterIsInstance<ConnectionDeclaration>().forEach { declaration ->
            add(AthenaPortReference(declaration.from.parts.joinToString("."), declaration.from.span))
            add(AthenaPortReference(declaration.to.parts.joinToString("."), declaration.to.span))
        }
    }

    /**
     * Resolves the definition target at [offset], if any.
     */
    fun definition(offset: Int): List<Location> {
        val target = targetAt(offset) ?: return emptyList()
        return when (target) {
            is AthenaTarget.Device -> deviceDeclarations[target.name]
                ?.let { declaration -> listOf(documentLocation(declaration.nameSpan())) }
                .orEmpty()

            is AthenaTarget.Port -> portDeclarations[target.qualifiedName]
                ?.let { declaration -> listOf(documentLocation(declaration.qualifiedName.span)) }
                .orEmpty()
        }
    }

    /**
     * Resolves all references for the target at [offset].
     */
    fun references(offset: Int, includeDeclaration: Boolean): List<Location> {
        val target = targetAt(offset) ?: return emptyList()
        return when (target) {
            is AthenaTarget.Device -> buildList {
                if (includeDeclaration) {
                    deviceDeclarations[target.name]?.let { declaration -> add(documentLocation(declaration.nameSpan())) }
                }
                ownerReferences
                    .filter { reference -> reference.deviceName == target.name }
                    .mapTo(this) { reference -> documentLocation(reference.span) }
            }

            is AthenaTarget.Port -> buildList {
                if (includeDeclaration) {
                    portDeclarations[target.qualifiedName]?.let { declaration -> add(documentLocation(declaration.qualifiedName.span)) }
                }
                portReferences
                    .filter { reference -> reference.qualifiedName == target.qualifiedName }
                    .mapTo(this) { reference -> documentLocation(reference.span) }
            }
        }
    }

    /**
     * Resolves the full authored declaration range for one inspected component.
     */
    fun componentSourceRange(componentName: String): Range? {
        return deviceDeclarations[componentName]?.span?.toLspRange()
    }

    /**
     * Resolves the full authored declaration range for one inspected port.
     */
    fun portSourceRange(qualifiedName: String): Range? {
        return portDeclarations[qualifiedName]?.span?.toLspRange()
    }

    /**
     * Resolves the full authored declaration range for one inspected connection.
     */
    fun connectionSourceRange(fromPath: String, toPath: String): Range? {
        return ast.declarations
            .filterIsInstance<ConnectionDeclaration>()
            .firstOrNull { declaration ->
                declaration.from.parts.joinToString(".") == fromPath &&
                    declaration.to.parts.joinToString(".") == toPath
            }
            ?.span
            ?.toLspRange()
    }

    private fun targetAt(offset: Int): AthenaTarget? {
        deviceDeclarations.values.firstOrNull { declaration -> declaration.nameSpan().contains(offset) }?.let { declaration ->
            return AthenaTarget.Device(declaration.name)
        }
        ownerReferences.firstOrNull { reference -> reference.span.contains(offset) }?.let { reference ->
            return AthenaTarget.Device(reference.deviceName)
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

/**
 * Target kinds the M4 navigation layer can resolve in a single document.
 */
sealed interface AthenaTarget {
    /**
     * Device declaration or owner reference target.
     */
    data class Device(val name: String) : AthenaTarget

    /**
     * Port declaration or endpoint reference target.
     */
    data class Port(val qualifiedName: String) : AthenaTarget
}

/**
 * Parsed completion context around one cursor position in an authored document.
 */
data class AthenaCompletionContext(
    val fragment: String,
    val expectsQualifiedPort: Boolean,
    val expectsDirectionValue: Boolean,
    val expectsSignalValue: Boolean,
    val expectsTypeValue: Boolean,
    val insideDeviceBlock: Boolean,
    val insidePortBlock: Boolean,
)

/**
 * One owner-segment reference to a device symbol.
 */
data class AthenaOwnerReference(
    val deviceName: String,
    val span: SourceSpan,
)

/**
 * One qualified port reference occurrence.
 */
data class AthenaPortReference(
    val qualifiedName: String,
    val span: SourceSpan,
)

private fun Declaration.toDocumentSymbol(): DocumentSymbol {
    return when (this) {
        is DeviceDeclaration -> DocumentSymbol().apply {
            name = this@toDocumentSymbol.name
            kind = SymbolKind.Class
            detail = "device"
            range = span.toLspRange()
            selectionRange = nameSpan().toLspRange()
            children = fields.map { field -> field.toDocumentSymbol() }
        }

        is PortDeclaration -> DocumentSymbol().apply {
            name = qualifiedName.parts.joinToString(".")
            kind = SymbolKind.Field
            detail = "port"
            range = span.toLspRange()
            selectionRange = qualifiedName.span.toLspRange()
            children = fields.map { field -> field.toDocumentSymbol() }
        }

        is ConnectionDeclaration -> DocumentSymbol().apply {
            name = "connect ${from.parts.joinToString(".")} -> ${to.parts.joinToString(".")}"
            kind = SymbolKind.Operator
            detail = "connect"
            range = span.toLspRange()
            selectionRange = span.toLspRange()
        }
    }
}

private fun PropertyAssignment.toDocumentSymbol(): DocumentSymbol {
    val propertyValue = value
    return DocumentSymbol().apply {
        name = this@toDocumentSymbol.name
        kind = SymbolKind.Property
        detail = when (propertyValue) {
            is ScalarValue.Identifier -> propertyValue.text
            is ScalarValue.StringLiteral -> "\"${propertyValue.text}\""
        }
        range = span.toLspRange()
        selectionRange = span.toLspRange()
    }
}

private fun DeviceDeclaration.nameSpan(): SourceSpan {
    val start = SourcePosition(
        offset = span.start.offset + "device ".length,
        line = span.start.line,
        column = span.start.column + "device ".length,
    )
    val end = start.advanceBy(name)
    return SourceSpan(start, end)
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

private fun EngineeringPropertyValue.summaryText(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> "\"$text\""
    }
}

private fun com.engineeringood.athena.ir.EngineeringProperty.toInspectionProperty(): AthenaSemanticInspectionProperty {
    return AthenaSemanticInspectionProperty(
        name = name,
        valueKind = when (value) {
            is EngineeringPropertyValue.Symbol -> "symbol"
            is EngineeringPropertyValue.Text -> "text"
        },
        valueText = when (val propertyValue = value) {
            is EngineeringPropertyValue.Symbol -> propertyValue.text
            is EngineeringPropertyValue.Text -> propertyValue.text
        },
    )
}

private fun com.engineeringood.athena.ir.EngineeringReference.authoredPath(): String = authoredPath.joinToString(".")

private fun com.engineeringood.athena.ir.EngineeringPort.summaryPath(): String = (ownerReference.authoredPath + name).joinToString(".")

private fun knowledgeDiagnosticComparator(): Comparator<com.engineeringood.athena.semantics.core.SemanticDiagnostic> {
    return compareBy<com.engineeringood.athena.semantics.core.SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}

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

    return AthenaCompletionContext(
        fragment = fragment,
        expectsQualifiedPort = trimmedPrefix.startsWith("port ") ||
            trimmedPrefix.startsWith("connect ") ||
            trimmedPrefix.contains("->"),
        expectsDirectionValue = trimmedPrefix.matches(Regex(".*\\bdirection\\s+[A-Za-z_]*$")),
        expectsSignalValue = trimmedPrefix.matches(Regex(".*\\bsignal\\s+[A-Za-z_]*$")),
        expectsTypeValue = trimmedPrefix.matches(Regex(".*\\btype\\s+[A-Za-z_]*$")),
        insideDeviceBlock = ast.declarationAt(offset).let { declaration -> declaration is DeviceDeclaration },
        insidePortBlock = ast.declarationAt(offset).let { declaration -> declaration is PortDeclaration },
    )
}

private fun SourceFileAst?.declarationAt(offset: Int): Declaration? {
    return this?.declarations?.firstOrNull { declaration -> declaration.span.contains(offset) }
}

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

private fun SourceSpan.toLspRange(): Range {
    return Range(
        Position((start.line - 1).coerceAtLeast(0), (start.column - 1).coerceAtLeast(0)),
        Position((end.line - 1).coerceAtLeast(0), (end.column - 1).coerceAtLeast(0)),
    )
}

private fun SourcePosition.advanceBy(text: String): SourcePosition {
    return copy(
        offset = offset + text.length,
        column = column + text.length,
    )
}

private fun SourceSpan.contains(offset: Int): Boolean = offset in start.offset until end.offset
