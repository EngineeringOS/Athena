package com.engineeringood.athena.language

enum class ConnectionSourceKind(val authoredText: String) {
    CONDUCTOR("conductor"),
    WIRE("wire"),
    CABLE_CORE("cable-core"),
    JUMPER("jumper"),
    BUSBAR("busbar"),
    SIGNAL("signal"),
}

enum class ConnectionSourceEndpoint {
    SOURCE,
    TARGET,
}

sealed interface ConnectionSourceValue {
    data class Symbol(val value: String) : ConnectionSourceValue {
        init {
            ConnectionSourceEditor.requireSourceIdentifier(value, "Connection property symbol")
        }
    }

    data class Text(val value: String) : ConnectionSourceValue {
        init {
            require('"' !in value && '\n' !in value && '\r' !in value) {
                "Connection property text cannot contain quotes or line breaks."
            }
        }
    }

    data class Number(val exactText: String) : ConnectionSourceValue {
        init {
            ConnectionSourceEditor.requireSourceNumber(exactText, "Connection property number")
        }
    }

    data class Quantity(val exactText: String, val unit: String) : ConnectionSourceValue {
        init {
            ConnectionSourceEditor.requireSourceNumber(exactText, "Connection property quantity")
            ConnectionSourceEditor.requireQualifiedSourceName(unit, "Connection property quantity unit")
        }
    }

    data class Boolean(val value: kotlin.Boolean) : ConnectionSourceValue

    data class Reference(val target: String) : ConnectionSourceValue {
        init {
            ConnectionSourceEditor.requireQualifiedSourceName(target, "Connection property reference")
        }
    }
}

data class ConnectionSourceProperty(
    val name: String,
    val value: ConnectionSourceValue,
) {
    init {
        ConnectionSourceEditor.requireSourceIdentifier(name, "Connection property name")
    }
}

data class ConnectionSourceInsertion(
    val kind: ConnectionSourceKind,
    val sourcePort: String,
    val targetPort: String,
    val properties: List<ConnectionSourceProperty> = emptyList(),
) {
    init {
        ConnectionSourceEditor.requirePortSourceName(sourcePort, "Connection source Port")
        ConnectionSourceEditor.requirePortSourceName(targetPort, "Connection target Port")
        require(sourcePort != targetPort) { "Connection endpoints must reference distinct Ports." }
        require(properties.map(ConnectionSourceProperty::name).distinct().size == properties.size) {
            "Connection properties must have unique names."
        }
    }
}

data class ConnectionSourceReconnect(
    val declarationSpan: SourceSpan,
    val endpoint: ConnectionSourceEndpoint,
    val replacementPort: String,
) {
    init {
        require(declarationSpan.start.offset >= 0 && declarationSpan.end.offset > declarationSpan.start.offset) {
            "Connection declaration span must be a positive half-open source range."
        }
        ConnectionSourceEditor.requirePortSourceName(replacementPort, "Replacement Connection Port")
    }
}

data class ConnectionSourceEditResult(
    val updatedSource: String,
    val changed: Boolean,
)

/** Applies source edits through Athena-owned Connection AST spans, never text matching. */
class ConnectionSourceEditor(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun insertConnection(
        file: String,
        existingSource: String,
        insertion: ConnectionSourceInsertion,
    ): ConnectionSourceEditResult {
        val ast = parseProject(file, existingSource)
        val identity = identityOf(insertion)
        require(ast.declarations.filterIsInstance<ConnectionDeclaration>().none { identityOf(it) == identity }) {
            "Connection `${identity.kind} ${identity.sourcePort} to ${identity.targetPort}` is already authored."
        }

        val newline = newlineOf(existingSource)
        val indentation = declarationIndentation(ast)
        val rendered = render(insertion, indentation, newline)
        val anchor = ast.declarations.firstOrNull { it.isProjectionDeclaration() }?.span?.start?.offset
            ?: closingBraceOffset(ast, existingSource)
        val lineStart = lineStartOf(existingSource, anchor)
        val anchorStartsOwnLine = existingSource.substring(lineStart, anchor).all { it == ' ' || it == '\t' }
        val insertionOffset = if (anchorStartsOwnLine) lineStart else anchor
        val leadingNewline = if (insertionOffset > 0 && existingSource[insertionOffset - 1] !in setOf('\n', '\r')) newline else ""
        val updated = buildString(existingSource.length + rendered.length + (2 * newline.length)) {
            append(existingSource, 0, insertionOffset)
            append(leadingNewline)
            append(rendered)
            append(newline)
            append(existingSource, insertionOffset, existingSource.length)
        }
        requireGeneratedSource(file, updated)
        return ConnectionSourceEditResult(updatedSource = updated, changed = true)
    }

    fun reconnectConnection(
        file: String,
        existingSource: String,
        reconnect: ConnectionSourceReconnect,
    ): ConnectionSourceEditResult {
        val ast = parseProject(file, existingSource)
        val declaration = ast.declarations.filterIsInstance<ConnectionDeclaration>().singleOrNull {
            it.span.start.offset == reconnect.declarationSpan.start.offset &&
                it.span.end.offset == reconnect.declarationSpan.end.offset
        } ?: throw IllegalArgumentException("Selected source span does not identify one Connection declaration.")
        val endpoint = when (reconnect.endpoint) {
            ConnectionSourceEndpoint.SOURCE -> declaration.source
            ConnectionSourceEndpoint.TARGET -> declaration.target
        }
        val otherEndpoint = when (reconnect.endpoint) {
            ConnectionSourceEndpoint.SOURCE -> declaration.target
            ConnectionSourceEndpoint.TARGET -> declaration.source
        }
        val currentPort = authoredText(endpoint)
        if (currentPort == reconnect.replacementPort) {
            return ConnectionSourceEditResult(existingSource, changed = false)
        }
        require(authoredText(otherEndpoint) != reconnect.replacementPort) {
            "Connection endpoints must reference distinct Ports."
        }
        require(endpoint.span.start.offset >= 0 && endpoint.span.end.offset <= existingSource.length) {
            "Connection endpoint span lies outside current source."
        }

        val updated = existingSource.replaceRange(
            endpoint.span.start.offset,
            endpoint.span.end.offset,
            reconnect.replacementPort,
        )
        requireGeneratedSource(file, updated)
        return ConnectionSourceEditResult(updatedSource = updated, changed = true)
    }

    private fun parseProject(file: String, source: String): SourceFileAst {
        val ast = when (val parsed = parser.parse(file, source)) {
            is ParseSuccess -> parsed.ast
            is ParseFailure -> throw IllegalArgumentException(parsed.diagnostics.first().message)
        }
        require(ast.unit is ProjectSourceUnit) { "Connection source edits require an Athena project source file." }
        return ast
    }

    private fun requireGeneratedSource(file: String, source: String) {
        check(parser.parse(file, source) is ParseSuccess) {
            "Generated Connection source must parse successfully."
        }
    }

    private fun declarationIndentation(ast: SourceFileAst): String {
        val system = ast.system
        val column = ast.declarations
            .asSequence()
            .filter { it.span.start.line > system.span.start.line }
            .map { it.span.start.column }
            .filter { it > system.span.start.column }
            .minOrNull()
            ?: system.span.start.column + 2
        return " ".repeat((column - 1).coerceAtLeast(0))
    }

    private fun closingBraceOffset(ast: SourceFileAst, source: String): Int {
        val offset = ast.system.span.end.offset - 1
        require(offset in source.indices && source[offset] == '}') {
            "Athena system source span does not end at its closing brace."
        }
        return offset
    }

    private fun render(insertion: ConnectionSourceInsertion, indentation: String, newline: String): String = buildString {
        append(indentation)
        append("connect ")
        append(insertion.kind.authoredText)
        append(' ')
        append(insertion.sourcePort)
        append(" to ")
        append(insertion.targetPort)
        if (insertion.properties.isNotEmpty()) {
            append(" {")
            insertion.properties.sortedBy(ConnectionSourceProperty::name).forEach { property ->
                append(newline)
                append(indentation)
                append("  ")
                append(property.name)
                append(' ')
                append(authoredText(property.value))
            }
            append(newline)
            append(indentation)
            append('}')
        }
    }

    private fun Declaration.isProjectionDeclaration(): Boolean = when (this) {
        is ViewDeclaration, is LayoutDeclaration, is InstallationDeclaration -> true
        else -> false
    }

    private fun identityOf(insertion: ConnectionSourceInsertion): ConnectionSourceIdentity = ConnectionSourceIdentity(
        insertion.kind.authoredText,
        insertion.sourcePort,
        insertion.targetPort,
    )

    private fun identityOf(declaration: ConnectionDeclaration): ConnectionSourceIdentity = ConnectionSourceIdentity(
        declaration.kind.value,
        authoredText(declaration.source),
        authoredText(declaration.target),
    )

    private fun authoredText(name: QualifiedName): String = name.parts.joinToString(".")

    private fun authoredText(value: ConnectionSourceValue): String = when (value) {
        is ConnectionSourceValue.Symbol -> value.value
        is ConnectionSourceValue.Text -> "\"${value.value}\""
        is ConnectionSourceValue.Number -> value.exactText
        is ConnectionSourceValue.Quantity -> "${value.exactText} [${value.unit}]"
        is ConnectionSourceValue.Boolean -> value.value.toString()
        is ConnectionSourceValue.Reference -> "@${value.target}"
    }

    private fun newlineOf(source: String): String = when {
        "\r\n" in source -> "\r\n"
        '\r' in source -> "\r"
        else -> "\n"
    }

    private fun lineStartOf(source: String, offset: Int): Int {
        require(offset in 0..source.length) { "Source insertion offset lies outside current source." }
        val startIndex = (offset - 1).coerceAtLeast(0)
        val newline = source.lastIndexOf('\n', startIndex)
        val carriageReturn = source.lastIndexOf('\r', startIndex)
        return maxOf(newline, carriageReturn) + 1
    }

    private data class ConnectionSourceIdentity(
        val kind: String,
        val sourcePort: String,
        val targetPort: String,
    )

    companion object {
        private val sourceIdentifier = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
        private val sourceNumber = Regex("^-?[0-9]+(?:\\.[0-9]+)?$")

        internal fun requirePortSourceName(value: String, subject: String) {
            requireQualifiedSourceName(value, subject)
            require(value.count { it == '.' } >= 1) { "$subject must identify an owned Port." }
        }

        internal fun requireQualifiedSourceName(value: String, subject: String) {
            require(value.isNotBlank() && value.split('.').all(sourceIdentifier::matches)) {
                "$subject must use an Athena qualified name."
            }
        }

        internal fun requireSourceIdentifier(value: String, subject: String) {
            require(sourceIdentifier.matches(value)) { "$subject must use an Athena identifier." }
        }

        internal fun requireSourceNumber(value: String, subject: String) {
            require(sourceNumber.matches(value)) { "$subject must use Athena decimal syntax." }
        }
    }
}
