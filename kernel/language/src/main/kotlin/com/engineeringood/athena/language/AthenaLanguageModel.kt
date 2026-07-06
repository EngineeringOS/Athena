package com.engineeringood.athena.language

/** Describes a single character position in a source file. */
data class SourcePosition(
    val offset: Int,
    val line: Int,
    val column: Int,
)

/**
 * Marks the half-open source range used by syntax nodes and diagnostics.
 *
 * The [start] position points at the first authored character in the span and the [end] position points
 * immediately after the final authored character in the span.
 */
data class SourceSpan(
    val start: SourcePosition,
    val end: SourcePosition,
)

/** Root AST node for one authored Athena source file. */
data class SourceFileAst(
    val system: SystemDeclaration,
    val declarations: List<Declaration>,
    val span: SourceSpan,
)

/** Declares the single system block that owns all top-level M0 declarations. */
data class SystemDeclaration(
    val name: String,
    val span: SourceSpan,
)

/** Base contract for all top-level syntax declarations inside a system block. */
sealed interface Declaration {
    val span: SourceSpan
}

/** Syntax node for a `device` declaration and its authored property fields. */
data class DeviceDeclaration(
    val name: String,
    val fields: List<PropertyAssignment>,
    override val span: SourceSpan,
) : Declaration

/** Syntax node for a `port` declaration addressed by a qualified authored name. */
data class PortDeclaration(
    val qualifiedName: QualifiedName,
    val fields: List<PropertyAssignment>,
    override val span: SourceSpan,
) : Declaration

/** Syntax node for a `connect` declaration between two qualified endpoints. */
data class ConnectionDeclaration(
    val from: QualifiedName,
    val to: QualifiedName,
    override val span: SourceSpan,
) : Declaration

/** Preserves a dotted authored reference such as `PLC1.out`. */
data class QualifiedName(
    val parts: List<String>,
    val span: SourceSpan,
)

/** Represents one authored field assignment inside a `device` or `port` block. */
data class PropertyAssignment(
    val name: String,
    val value: ScalarValue,
    val span: SourceSpan,
)

/** Base contract for scalar field values supported by the M0 syntax layer. */
sealed interface ScalarValue {
    val span: SourceSpan

    /** Identifier-valued field such as a model code or symbolic mode. */
    data class Identifier(
        val text: String,
        override val span: SourceSpan,
    ) : ScalarValue

    /** String literal field value as authored in the source file. */
    data class StringLiteral(
        val text: String,
        override val span: SourceSpan,
    ) : ScalarValue
}

/** Result of parsing a single Athena source file. */
sealed interface ParseResult

/** Successful parse containing the syntax-only AST. */
data class ParseSuccess(val ast: SourceFileAst) : ParseResult

/** Failed parse containing one or more syntax diagnostics. */
data class ParseFailure(val diagnostics: List<SyntaxDiagnostic>) : ParseResult

/** Provenance-rich syntax error emitted during tokenization or parsing. */
data class SyntaxDiagnostic(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val span: SourceSpan,
)
