package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Story 4.2 parity baseline: pins AST-dependent navigation, symbol, and source-range behavior.
 *
 * These assertions must keep passing after Epic 2 replaces the handwritten parser with ANTLR4-backed
 * parsing and after Epic 3 adds Tree-sitter for editor syntax UX, because every asserted result is
 * derived only from the authored `SourceFileAst` and its `SourceSpan`s (AD-109 / AD-106).
 */
class AthenaSourceNavigationParityTest {
    @Test
    @Suppress("DEPRECATION")
    fun `document symbols mirror the authored declarations of the demo cabinet fixture`() {
        withOpenedDemoCabinet { server, documentUri ->
            val symbols = server.textDocumentService
                .documentSymbol(DocumentSymbolParams(TextDocumentIdentifier(documentUri)))
                .get()

            assertEquals(1, symbols.size)
            val systemSymbol = symbols.single().right
            assertEquals("DemoCabinet", systemSymbol.name)
            assertEquals(SymbolKind.Module, systemSymbol.kind)

            val children = systemSymbol.children
            assertEquals(
                listOf("PLC1", "M1", "PLC1.out", "M1.in", "connect PLC1.out -> M1.in"),
                children.map { child -> child.name },
            )
            assertEquals(
                listOf(
                    SymbolKind.Class,
                    SymbolKind.Class,
                    SymbolKind.Field,
                    SymbolKind.Field,
                    SymbolKind.Operator,
                ),
                children.map { child -> child.kind },
            )
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `definition and references resolve a device across authored source spans`() {
        withOpenedDemoCabinet { server, documentUri ->
            // Cursor on the `PLC1` owner segment inside `connect PLC1.out -> M1.in` (0-based line 20).
            val ownerReferencePosition = Position(20, 11)

            val definitions = server.textDocumentService
                .definition(DefinitionParams(TextDocumentIdentifier(documentUri), ownerReferencePosition))
                .get()
                .left
            assertEquals(1, definitions.size)
            assertEquals(1, definitions.single().range.start.line)

            val references = server.textDocumentService
                .references(
                    ReferenceParams(
                        TextDocumentIdentifier(documentUri),
                        ownerReferencePosition,
                        ReferenceContext(true),
                    ),
                )
                .get()
            assertEquals(
                setOf(1, 10, 20),
                references.map { location -> location.range.start.line }.toSet(),
            )
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `navigation source ranges are non-null for every component port and connection`() {
        withOpenedDemoCabinet { server, documentUri ->
            val inspection = assertNotNull(
                server.semanticInspection(
                    AthenaSemanticInspectionParams(AthenaSemanticInspectionTextDocument(documentUri)),
                ).get(),
            )
            assertEquals(2, inspection.componentCount)
            assertEquals(2, inspection.portCount)
            assertEquals(1, inspection.connectionCount)
            // `sourceRange` is a non-nullable lsp4j Range, so the parity guarantee is that every entry
            // carries a well-formed, non-negative span anchored to the authored AST (AD-109 / AD-106).
            assertTrue(inspection.components.all { component -> component.sourceRange.isWellFormedSpan() })
            assertTrue(inspection.ports.all { port -> port.sourceRange.isWellFormedSpan() })
            assertTrue(inspection.connections.all { connection -> connection.sourceRange.isWellFormedSpan() })
        }
    }

    @Suppress("DEPRECATION")
    private fun withOpenedDemoCabinet(block: (server: AthenaLanguageServer, documentUri: String) -> Unit) {
        val fixtureText = Files.readString(resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena"))
        val repository = createGovernedTestRepository("athena-lsp-navigation-parity-")
        val repositoryRoot = repository.repositoryRoot
        val documentUri = repository.seedSourcePath.toUri().toString()

        val server = AthenaLanguageServer()
        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(documentUri, "athena", 1, fixtureText),
                ),
            )

            block(server, documentUri)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }

    private fun Range.isWellFormedSpan(): Boolean {
        val startsInBounds = start.line >= 0 && start.character >= 0
        val endsInBounds = end.line >= 0 && end.character >= 0
        val endNotBeforeStart = end.line > start.line ||
            (end.line == start.line && end.character >= start.character)
        return startsInBounds && endsInBounds && endNotBeforeStart
    }
}
