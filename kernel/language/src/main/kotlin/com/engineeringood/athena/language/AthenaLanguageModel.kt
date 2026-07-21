package com.engineeringood.athena.language

/**
 * Describes a single character position in a source file.
 *
 * This type is part of Athena's frozen public authored syntax contract in `:kernel:language`.
 * It is syntax-only and remains stable across future compiler-parser implementation changes,
 * including ANTLR4 migration. It does not carry semantic or engineering meaning.
 */
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
 *
 * This type is part of Athena's frozen public authored syntax contract in `:kernel:language`.
 * It is syntax-only and remains stable across future compiler-parser implementation changes,
 * including ANTLR4 migration. Downstream code must depend on this Athena-owned span shape rather
 * than on parser-generator position types.
 */
data class SourceSpan(
    val start: SourcePosition,
    val end: SourcePosition,
)

/**
 * Root AST node for one authored Athena source file.
 *
 * This type is Athena's frozen public authored AST root in `:kernel:language`. It is syntax-only
 * and remains the only supported parse-success payload across future compiler-parser changes,
 * including ANTLR4 migration. Lowering and other semantic consumers must depend on this contract
 * rather than on generated parse-tree types.
 */
data class SourceFileAst(
    val system: SystemDeclaration,
    val declarations: List<Declaration>,
    val span: SourceSpan,
    val packageDeclaration: PackageDeclaration? = null,
    val imports: List<ImportDeclaration> = emptyList(),
)

/**
 * Declares one file-level qualified target for later semantic graph resolution.
 *
 * This syntax-only node preserves authored package-or-symbol intent without classifying or
 * resolving the target.
 */
data class ImportDeclaration(
    val target: QualifiedName,
    val span: SourceSpan,
)

/**
 * Declares the governed package namespace authored for one source file.
 *
 * This is syntax-only package intent. Repository admission, package identity validation, and
 * semantic binding remain downstream compiler responsibilities.
 */
data class PackageDeclaration(
    val name: QualifiedName,
    val span: SourceSpan,
)

/**
 * Declares the single system block that owns all top-level M0 declarations.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class SystemDeclaration(
    val name: String,
    val span: SourceSpan,
)

/**
 * Base contract for all top-level syntax declarations inside a system block.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes. Future declaration kinds land as additional sealed
 * variants rather than as parser-generator-specific types.
 *
 * ## Future system-body syntax landing zone
 *
 * New constructs authored inside a system block are added as sealed variants on this hierarchy
 * without widening [DeviceDeclaration], [PortDeclaration], or [ConnectionDeclaration], and without
 * making Engineering IR lowering depend on parser-tree types. File-header metadata such as
 * [PackageDeclaration] and [ImportDeclaration] remains on [SourceFileAst].
 *
 * Required landing pattern for a future contributor:
 * 1. Add the new sealed variant here (authored AST only).
 * 2. Adapt source-to-AST for the new construct inside the internal
 *    `com.engineeringood.athena.language.antlr` ParseAdapter (Epic 2 ANTLR path).
 * 3. Handle the new variant through an **exhaustive** `when` at every consumer that lowers
 *    or classifies [Declaration] values (compile-time failure on unhandled variants is intentional).
 *
 * Import resolution and package-aware authored semantics remain downstream compiler concerns.
 */
sealed interface Declaration {
    val span: SourceSpan
}

/**
 * Syntax node for a `device` declaration and its authored property fields.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class DeviceDeclaration(
    val name: String,
    val fields: List<PropertyAssignment>,
    override val span: SourceSpan,
    val nestedPorts: List<PortDeclaration> = emptyList(),
) : Declaration

/**
 * Syntax node for a `port` declaration addressed by a qualified authored name.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class PortDeclaration(
    val qualifiedName: QualifiedName,
    val fields: List<PropertyAssignment>,
    override val span: SourceSpan,
) : Declaration

/**
 * Syntax node for a `connect` declaration between two qualified endpoints.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class ConnectionDeclaration(
    val from: QualifiedName,
    val to: QualifiedName,
    override val span: SourceSpan,
) : Declaration

/**
 * Syntax node for a readable source grouping of repeated `connect` edges.
 *
 * This is authoring structure only. The group name is preserved for outline/folding/provenance,
 * while semantic lowering keeps canonical [com.engineeringood.athena.ir.EngineeringConnection]
 * facts flat.
 */
data class ConnectionGroupDeclaration(
    val name: String,
    val connections: List<ConnectionDeclaration>,
    override val span: SourceSpan,
) : Declaration

/**
 * Syntax node for a `layout <view-family> { ... }` declaration authored inside a system block.
 *
 * This is the M23 language admission surface for layout intent. It preserves authored layout
 * statements without resolving subjects, generating constraints, or assigning renderer-owned
 * coordinates.
 */
data class LayoutDeclaration(
    val viewFamily: String,
    val statements: List<LayoutStatement>,
    override val span: SourceSpan,
) : Declaration

/** Syntax-only authored statements inside a [LayoutDeclaration]. */
sealed interface LayoutStatement {
    val subject: String
    val target: String
    val span: SourceSpan

    data class PlaceNear(
        override val subject: String,
        override val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class PlaceBelow(
        override val subject: String,
        override val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class AlignWith(
        override val subject: String,
        override val target: String,
        val axis: LayoutAxis,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class GroupWith(
        override val subject: String,
        override val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement
}

enum class LayoutAxis {
    Horizontal,
    Vertical,
}

/**
 * Preserves a dotted authored reference such as `PLC1.out`.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class QualifiedName(
    val parts: List<String>,
    val span: SourceSpan,
)

/**
 * Represents one authored field assignment inside a `device` or `port` block.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes.
 */
data class PropertyAssignment(
    val name: String,
    val value: ScalarValue,
    val span: SourceSpan,
)

/**
 * Base contract for scalar field values supported by the M0 syntax layer.
 *
 * Part of the frozen Athena-owned authored syntax contract; syntax-only and stable across
 * future parser implementation changes. New literal kinds land as additional sealed variants
 * (field-level extensibility). Top-level authored constructs extend neither this hierarchy nor
 * system-body [Declaration].
 */
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

/**
 * Result of parsing a single Athena source file.
 *
 * This sealed contract is Athena's frozen public parse-result surface in `:kernel:language`.
 * Callers must treat [ParseSuccess] and [ParseFailure] as the only supported outcomes.
 * The contract remains stable across future compiler-parser implementation changes,
 * including ANTLR4 migration, and must not expose parser-generator internals.
 */
sealed interface ParseResult

/**
 * Successful parse containing the syntax-only AST.
 *
 * Part of the frozen Athena-owned parse-result contract; the [ast] payload remains the only
 * supported authored-AST carrier for lowering and downstream consumers.
 */
data class ParseSuccess(val ast: SourceFileAst) : ParseResult

/**
 * Failed parse containing one or more syntax diagnostics.
 *
 * Part of the frozen Athena-owned parse-result contract; failures remain typed and
 * provenance-rich rather than opaque parser crashes.
 */
data class ParseFailure(val diagnostics: List<SyntaxDiagnostic>) : ParseResult

/**
 * Provenance-rich syntax error emitted during tokenization or parsing.
 *
 * This type is part of Athena's frozen public authored syntax contract in `:kernel:language`.
 * It is syntax-only and remains stable across future compiler-parser implementation changes,
 * including ANTLR4 migration. It carries file identity, line, column, message, and span only—
 * never parser-internal token or generator types.
 */
data class SyntaxDiagnostic(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val span: SourceSpan,
)
