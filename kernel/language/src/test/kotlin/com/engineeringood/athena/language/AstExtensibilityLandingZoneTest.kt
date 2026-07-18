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
    fun `Declaration consumers see exactly the four current sealed variants`() {
        val span = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(1, 1, 2))
        val declarations: List<Declaration> = listOf(
            DeviceDeclaration(name = "PLC1", fields = emptyList(), span = span),
            PortDeclaration(
                qualifiedName = QualifiedName(listOf("PLC1", "out"), span),
                fields = emptyList(),
                span = span,
            ),
            ConnectionDeclaration(
                from = QualifiedName(listOf("PLC1", "out"), span),
                to = QualifiedName(listOf("M1", "in"), span),
                span = span,
            ),
            LayoutDeclaration(
                viewFamily = "schematic-sheet",
                statements = listOf(LayoutStatement.PlaceNear("HMI1", "PLC1", span)),
                span = span,
            ),
        )

        assertEquals(
            listOf("device", "port", "connect", "layout"),
            declarations.map { declaration -> classifyDeclaration(declaration) },
        )
    }

    @Test
    fun `ScalarValue consumers see exactly the two current sealed variants`() {
        val span = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(1, 1, 2))
        val values: List<ScalarValue> = listOf(
            ScalarValue.Identifier("Switch", span),
            ScalarValue.StringLiteral("S7-1200", span),
        )

        assertEquals(
            listOf("identifier", "string"),
            values.map { value -> classifyScalarValue(value) },
        )
    }

    /**
     * Exhaustive classifier: adding a new `Declaration` sealed variant must break this
     * `when` at compile time. That is the structural extensibility proof Story `1.3`
     * requires. Do not add an `else` branch.
     */
    private fun classifyDeclaration(declaration: Declaration): String {
        return when (declaration) {
            is DeviceDeclaration -> "device"
            is PortDeclaration -> "port"
            is ConnectionDeclaration -> "connect"
            is LayoutDeclaration -> "layout"
        }
    }

    /**
     * Exhaustive classifier for `ScalarValue`. Do not add an `else` branch.
     */
    private fun classifyScalarValue(value: ScalarValue): String {
        return when (value) {
            is ScalarValue.Identifier -> "identifier"
            is ScalarValue.StringLiteral -> "string"
        }
    }
}
