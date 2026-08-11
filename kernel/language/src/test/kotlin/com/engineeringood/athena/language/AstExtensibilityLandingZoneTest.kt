package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Documents and structurally proves the Story `1.3` extensibility guarantee:
 * today's sealed `Declaration` / `ScalarValue` hierarchies are the exact set consumers
 * must handle exhaustively, so a future sealed variant fails loudly at compile time
 * wherever an exhaustive `when` is used (see also LSP `Declaration.toDocumentSymbol`
 * and domain lowering sites that should prefer exhaustive dispatch over
 * `filterIsInstance`-only drops).
 */
class AstExtensibilityLandingZoneTest {
    @Test
    fun `Declaration consumers see exactly the nine current sealed variants`() {
        val span = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(1, 1, 2))
        val declarations: List<Declaration> = listOf(
            EntityDeclaration(name = "PLC1", fields = emptyList(), span = span),
            PortDeclaration(
                qualifiedName = QualifiedName(listOf("PLC1", "out"), span),
                fields = emptyList(),
                span = span,
            ),
            RelationDeclaration(
                word = SymbolIdentifierField("power", span),
                source = QualifiedName(listOf("PLC1", "out"), span),
                targets = listOf(QualifiedName(listOf("M1", "in"), span)),
                span = span,
            ),
            ConnectionDeclaration(
                kind = SymbolIdentifierField("wire", span),
                source = QualifiedName(listOf("PLC1", "out"), span),
                target = QualifiedName(listOf("M1", "in"), span),
                properties = emptyList(),
                span = span,
            ),
            ExternalEvidenceDeclaration(
                name = "DriveEvidence",
                namespace = SymbolIdentifierField("iec", span),
                reference = SymbolStringField("IEC:60204-1:clause-13", span),
                subject = ExternalEvidenceSubjectDeclaration(
                    kind = ExternalEvidenceSubjectKind.CONTRACT,
                    target = QualifiedName(listOf("PLC1"), span),
                    span = span,
                ),
                provenance = SymbolStringField("test", span),
                span = span,
            ),
            ProjectionPolicyDeclaration(
                name = "ControlDrawingProjection",
                target = SymbolIdentifierField("professional-connection-drawing", span),
                layoutStrategy = SymbolIdentifierField("orthogonal-grid", span),
                drawingProfile = SymbolIdentifierField("ControlDrawingIEC", span),
                routeQualityPolicy = SymbolIdentifierField("ControlDrawingRouteQuality", span),
                proofObligations = listOf(SymbolIdentifierField("exact-endpoints", span)),
                forbiddenEngineeringTruth = emptyList(),
                span = span,
            ),
            LayoutDeclaration(
                viewFamily = "schematic-sheet",
                statements = listOf(LayoutStatement.PlaceNear("HMI1", "PLC1", span)),
                span = span,
            ),
            InstallationDeclaration(
                name = "MainCabinet",
                kind = InstallationKind.Cabinet,
                enclosures = emptyList(),
                surfaces = emptyList(),
                rails = emptyList(),
                ducts = emptyList(),
                channels = emptyList(),
                terminalGroups = emptyList(),
                mounts = emptyList(),
                routes = emptyList(),
                span = span,
            ),
        )

        assertEquals(
            listOf("entity", "port", "relation", "connection", "evidence", "projection", "layout", "installation"),
            declarations.map { declaration -> classifyDeclaration(declaration) },
        )
    }

    @Test
    fun `ScalarValue consumers see exactly the six current sealed variants`() {
        val span = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(1, 1, 2))
        val values: List<ScalarValue> = listOf(
            ScalarValue.Symbol("Switch", span),
            ScalarValue.Text("S7-1200", span),
            ScalarValue.Quantity("7.5", QualifiedName(listOf("unit", "kilowatt"), span), span),
            ScalarValue.Integer("4", span),
            ScalarValue.Boolean(true, span),
            ScalarValue.Reference(QualifiedName(listOf("M1", "main"), span), span),
        )

        assertEquals(
            listOf("symbol", "text", "quantity", "integer", "boolean", "reference"),
            values.map { value -> classifyScalarValue(value) },
        )
    }

    /**
     * Exhaustive classifier: adding a new `Declaration` sealed variant must break this
     * `when` at compile time. That is the structural extensibility evidence Story `1.3`
     * requires. Do not add an `else` branch.
     */
    private fun classifyDeclaration(declaration: Declaration): String {
        return when (declaration) {
            is EntityDeclaration -> "entity"
            is PortDeclaration -> "port"
            is RelationDeclaration -> "relation"
            is ConnectionDeclaration -> "connection"
            is NetDeclaration -> "net"
            is ConnectionSpecificationDeclaration -> "connection-spec"
            is ExternalEvidenceDeclaration -> "evidence"
            is ProjectionPolicyDeclaration -> "projection"
            is LayoutDeclaration -> "layout"
            is InstallationDeclaration -> "installation"
            is ViewDeclaration -> "view"
            is SheetDeclaration -> "sheet"
            is GridDeclaration -> "grid"
            is RegionDeclaration -> "region"
            is ProjectionConstructDeclaration -> "construct"
        }
    }

    /**
     * Exhaustive classifier for `ScalarValue`. Do not add an `else` branch.
     */
    private fun classifyScalarValue(value: ScalarValue): String {
        return when (value) {
            is ScalarValue.Symbol -> "symbol"
            is ScalarValue.Text -> "text"
            is ScalarValue.Quantity -> "quantity"
            is ScalarValue.Integer -> "integer"
            is ScalarValue.Boolean -> "boolean"
            is ScalarValue.Reference -> "reference"
        }
    }
}
