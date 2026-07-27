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
    val unit: AthenaSourceUnit,
    val span: SourceSpan,
    val packageDeclaration: PackageDeclaration? = null,
    val imports: List<ImportDeclaration> = emptyList(),
) {
    constructor(
        system: SystemDeclaration,
        declarations: List<Declaration>,
        span: SourceSpan,
        packageDeclaration: PackageDeclaration? = null,
        imports: List<ImportDeclaration> = emptyList(),
    ) : this(
        unit = ProjectSourceUnit(system, declarations),
        span = span,
        packageDeclaration = packageDeclaration,
        imports = imports,
    )

    val systemOrNull: SystemDeclaration?
        get() = (unit as? ProjectSourceUnit)?.system

    val system: SystemDeclaration
        get() = requireNotNull(systemOrNull) { "Representation source does not contain a project system." }

    val projectDeclarations: List<Declaration>
        get() = (unit as? ProjectSourceUnit)?.declarations.orEmpty()

    val declarations: List<Declaration>
        get() = projectDeclarations

    val representationDeclarations: List<RepresentationDeclaration>
        get() = (unit as? RepresentationSourceUnit)?.declarations.orEmpty()
}

sealed interface AthenaSourceUnit

data class ProjectSourceUnit(
    val system: SystemDeclaration,
    val declarations: List<Declaration>,
) : AthenaSourceUnit

data class RepresentationSourceUnit(
    val declarations: List<RepresentationDeclaration>,
) : AthenaSourceUnit {
    init {
        require(declarations.isNotEmpty()) { "Representation source requires at least one declaration." }
    }
}

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

sealed interface RepresentationDeclaration {
    val name: String
    val span: SourceSpan
}

data class SymbolDeclaration(
    override val name: String,
    val identity: SymbolStringField?,
    val version: SymbolStringField?,
    val resources: List<RepresentationResourceDeclaration>,
    val graphic: SymbolGraphicDeclaration?,
    val anchors: List<SymbolAnchorDeclaration>,
    override val span: SourceSpan,
) : RepresentationDeclaration

data class RepresentationResourceDeclaration(
    val id: String,
    val kind: RepresentationResourceKind,
    val path: SymbolStringField,
    val span: SourceSpan,
)

enum class RepresentationResourceKind {
    SVG,
}

data class SymbolStringField(
    val value: String,
    val span: SourceSpan,
)

data class SymbolPoint(
    val x: Double,
    val y: Double,
    val span: SourceSpan,
)

data class SymbolBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val span: SourceSpan,
)

data class SymbolSize(
    val width: Double,
    val height: Double,
    val span: SourceSpan,
)

data class SymbolGraphicDeclaration(
    val bounds: SymbolBounds?,
    val primitives: List<SymbolGraphicPrimitiveDeclaration>,
    val labels: List<SymbolDynamicLabelDeclaration>,
    val svgResource: SymbolIdentifierField? = null,
    val span: SourceSpan,
)

sealed interface SymbolGraphicPrimitiveDeclaration {
    val id: String
    val style: String
    val span: SourceSpan

    data class Line(
        override val id: String,
        val from: SymbolPoint,
        val to: SymbolPoint,
        override val style: String,
        override val span: SourceSpan,
    ) : SymbolGraphicPrimitiveDeclaration

    data class Polyline(
        override val id: String,
        val points: List<SymbolPoint>,
        override val style: String,
        override val span: SourceSpan,
    ) : SymbolGraphicPrimitiveDeclaration

    data class Arc(
        override val id: String,
        val center: SymbolPoint,
        val radius: Double,
        val startAngleDegrees: Double,
        val sweepAngleDegrees: Double,
        override val style: String,
        override val span: SourceSpan,
    ) : SymbolGraphicPrimitiveDeclaration

    data class Circle(
        override val id: String,
        val center: SymbolPoint,
        val radius: Double,
        override val style: String,
        override val span: SourceSpan,
    ) : SymbolGraphicPrimitiveDeclaration

    data class Rectangle(
        override val id: String,
        val origin: SymbolPoint,
        val size: SymbolSize,
        override val style: String,
        override val span: SourceSpan,
    ) : SymbolGraphicPrimitiveDeclaration
}

data class SymbolDynamicLabelDeclaration(
    val id: String,
    val origin: SymbolPoint,
    val size: SymbolSize,
    val role: SymbolIdentifierField,
    val style: String,
    val span: SourceSpan,
)

data class SymbolAnchorDeclaration(
    val id: String,
    val primitiveRef: SymbolIdentifierField?,
    val point: SymbolPoint?,
    val role: SymbolIdentifierField?,
    val acceptedDirections: List<SymbolIdentifierField>,
    val acceptedSignals: List<SymbolIdentifierField>,
    val span: SourceSpan,
)

data class SymbolIdentifierField(
    val value: String,
    val span: SourceSpan,
)

data class ElementDeclaration(
    override val name: String,
    val identity: SymbolStringField?,
    val version: SymbolStringField?,
    val bounds: SymbolBounds?,
    val resources: List<RepresentationResourceDeclaration>,
    val graphic: SymbolGraphicDeclaration?,
    val children: List<ElementChildDeclaration>,
    val exportedAnchors: List<ElementAnchorExportDeclaration>,
    val exportedLabels: List<ElementLabelExportDeclaration>,
    override val span: SourceSpan,
) : RepresentationDeclaration

data class ElementNumberField(
    val value: Double,
    val span: SourceSpan,
)

data class ElementChildDeclaration(
    val id: String,
    val headerSpan: SourceSpan,
    val symbolIdentity: SymbolStringField?,
    val translate: SymbolPoint?,
    val rotate: ElementNumberField?,
    val scale: SymbolPoint?,
    val zOrder: ElementNumberField?,
    val span: SourceSpan,
)

data class ElementAnchorExportDeclaration(
    val id: String,
    val childId: SymbolIdentifierField,
    val childAnchorId: SymbolIdentifierField,
    val referenceSpan: SourceSpan,
    val span: SourceSpan,
)

data class ElementLabelExportDeclaration(
    val id: String,
    val childId: SymbolIdentifierField,
    val childLabelId: SymbolIdentifierField,
    val referenceSpan: SourceSpan,
    val span: SourceSpan,
)

enum class BindingSelectorKind {
    Device,
    Function,
}

data class ProfileDeclaration(
    override val name: String,
    val projection: SymbolIdentifierField?,
    val standard: SymbolIdentifierField?,
    val style: SymbolIdentifierField?,
    val fallback: SymbolIdentifierField?,
    override val span: SourceSpan,
) : RepresentationDeclaration

data class BindingDeclaration(
    override val name: String,
    val profile: SymbolIdentifierField?,
    val priority: ElementNumberField?,
    val selectorKind: BindingSelectorKind?,
    val selectorFacts: List<PropertyAssignment>,
    val useElement: SymbolStringField?,
    val useVersion: SymbolStringField?,
    val variant: SymbolStringField?,
    override val span: SourceSpan,
) : RepresentationDeclaration

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
    val nestedFunctions: List<EngineeringFunctionDeclaration> = emptyList(),
) : Declaration

/** Syntax-only functional partition of one authored physical device. */
data class EngineeringFunctionDeclaration(
    val name: String,
    val role: SymbolIdentifierField,
    val portReferences: List<QualifiedName>,
    val span: SourceSpan,
)

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
    val span: SourceSpan

    data class PlaceNear(
        val subject: String,
        val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class PlaceBelow(
        val subject: String,
        val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class AlignWith(
        val subject: String,
        val target: String,
        val axis: LayoutAxis,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class GroupWith(
        val subject: String,
        val target: String,
        override val span: SourceSpan,
    ) : LayoutStatement

    data class PlaceAt(
        val subject: QualifiedName,
        val position: DrawingGridPosition,
        val orientation: LayoutOrientation,
        override val span: SourceSpan,
    ) : LayoutStatement
}

data class DrawingGridPosition(
    val column: Int,
    val row: Int,
    val span: SourceSpan,
)

enum class LayoutOrientation {
    Horizontal,
    Vertical,
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
