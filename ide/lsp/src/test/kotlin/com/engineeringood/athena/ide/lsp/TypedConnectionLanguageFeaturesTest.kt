package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertIs

class TypedConnectionLanguageFeaturesTest {
    @Test
    fun `typed connection formats and appears in outline`() {
        val path = Path.of("src/control.athena").toAbsolutePath()
        val uri = path.toUri().toString()
        val source = """
            system Control {
              entity PLC1 { concept controller }
              entity KM1 { concept contactor }
              port PLC1.out { direction out flow control }
              port KM1.coil { direction in flow control }
              connect wire PLC1.out -> KM1.coil
            }
        """.trimIndent()
        val features = AthenaLanguageFeatures(AthenaCompiler())
        features.trackDocument(uri, path, 1, source)

        val formatted = features.formatting(uri).single().newText
        assertTrue(formatted.contains("connect wire PLC1.out to KM1.coil"))

        val symbols = features.documentSymbols(DocumentSymbolParams(TextDocumentIdentifier(uri)))
        assertEquals(1, symbols.size)
        assertTrue(symbols.single().isRight)
        val root = symbols.single().getRight()
        val connection = root.children.single { it.detail == "engineering connection" }
        assertEquals("connect wire PLC1.out to KM1.coil", connection.name)
        assertTrue(features.semanticTokens(uri).data.isNotEmpty())
        val tokenTypes = features.semanticTokens(uri).data.chunked(5).map { token -> athenaSemanticTokenTypes[token[3]] }
        assertTrue("athenaRelationshipKeyword" in tokenTypes)
        assertTrue("operator" in tokenTypes)

        val definition = features.definition(uri, Position(5, 21)).single()
        assertEquals(3, definition.range.start.line)
        val references = features.references(
            ReferenceParams(TextDocumentIdentifier(uri), Position(5, 21), ReferenceContext(false)),
        )
        assertEquals(listOf(5), references.map { it.range.start.line })
    }

    @Test
    fun `unresolved connection diagnostic names port and correction through lsp state`() {
        val path = Path.of("src/control.athena").toAbsolutePath()
        val uri = path.toUri().toString()
        val source = """
            system Control {
              entity PLC1 { concept controller }
              entity KM1 { concept contactor }
              port PLC1.out { direction out flow control }
              connect wire PLC1.out to KM1.missing
            }
        """.trimIndent()
        val features = AthenaLanguageFeatures(AthenaCompiler())

        val tracked = features.trackDocument(uri, path, 1, source)
        val compilation = assertIs<CompilerCompilationSuccess>(tracked.compilation)
        val diagnostic = compilation.semanticResult.diagnostics.single { it.ruleId.value == "reference.connection-endpoint.unresolved" }

        assertTrue(diagnostic.message.contains("KM1.missing"))
        assertTrue(diagnostic.message.contains("Declare Port `KM1.missing` or correct this connection path"))
        assertTrue(compilation.document.connections.isEmpty())
    }
}
