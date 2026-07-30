package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the first M4 authoring and navigation feature set served through Athena LSP.
 */
class AthenaAuthoringSupportTest {
    @Test
    @Suppress("DEPRECATION")
    fun `completion document symbols definition and references are served from Athena LSP`() {
        val repository = createGovernedTestRepository("athena-lsp-authoring-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val incompleteSourceText = """
            system FactoryLine {
              device PLC1 {
                type Switch
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              con
              connect plc1_out_to_plc1_out PLC1.out -> PLC1.out
            }
        """.trimIndent()
        val validSourceText = """
            system FactoryLine {
              device PLC1 {
                type Switch
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              connect plc1_out_to_plc1_out_2 PLC1.out -> PLC1.out
            }
        """.trimIndent()
        sourcePath.writeText(governedAthenaSource(validSourceText))

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        incompleteSourceText,
                    ),
                ),
            )

            val completion = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(9, 5)
                },
            ).get()
            val completionItems = completion.right.items
            assertTrue(completionItems.any { item -> item.label == "connect" })

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams().apply {
                    textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                    contentChanges = listOf(TextDocumentContentChangeEvent(validSourceText))
                },
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            assertEquals("FactoryLine", systemSymbol.name)
            assertEquals(listOf("PLC1", "PLC1.out", "connect plc1_out_to_plc1_out_2 PLC1.out -> PLC1.out"), systemSymbol.children.map { child -> child.name })

            val definition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(10, 40)
                },
            ).get()
            val definitionRange = definition.left.single().range
            assertEquals(5, definitionRange.start.line)

            val references = server.textDocumentService.references(
                ReferenceParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(10, 40)
                    context = ReferenceContext(true)
                },
            ).get()
            assertEquals(3, references.size)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `document symbols nest compact authored ports under their owning device`() {
        val repository = createGovernedTestRepository("athena-lsp-nested-port-outline-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system CompactDeviceOutline {
              device SpareTerminalXT99 {
                type Switch
                model "SPARE-XT"

                port in1 {
                  direction in
                  signal Digital
                }
              }
            }
        """.trimIndent()
        sourcePath.writeText(governedAthenaSource(sourceText))

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            val deviceSymbol = systemSymbol.children.single { child -> child.name == "SpareTerminalXT99" }
            val portSymbol = deviceSymbol.children.single { child -> child.name == "in1" }

            assertEquals(
                listOf("type", "model", "in1"),
                deviceSymbol.children.map { child -> child.name },
            )
            assertEquals(SymbolKind.Field, portSymbol.kind)
            assertEquals("port", portSymbol.detail)
            assertEquals(
                listOf("direction", "signal"),
                portSymbol.children.map { child -> child.name },
            )
            assertTrue(systemSymbol.children.none { child -> child.name == "SpareTerminalXT99.in1" })
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `document symbols expose grouped connect block and child edges`() {
        val repository = createGovernedTestRepository("athena-lsp-grouped-connect-outline-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system GroupedConnectOutline {
              device PLC1 {
                type Switch
              }
              device M1 {
                type Motor
              }
              port PLC1.out {
                direction out
                signal Digital
              }
              port M1.in {
                direction in
                signal Digital
              }

              connect control_feed {
                plc1_out_to_m1_in PLC1.out -> M1.in
              }
            }
        """.trimIndent()
        sourcePath.writeText(governedAthenaSource(sourceText))

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            val groupSymbol = systemSymbol.children.single { child -> child.name == "connect control_feed" }

            assertEquals(SymbolKind.Module, groupSymbol.kind)
            assertEquals("connect group", groupSymbol.detail)
            assertEquals(
                listOf("plc1_out_to_m1_in PLC1.out -> M1.in"),
                groupSymbol.children.map { child -> child.name },
            )
            assertEquals("connect edge", groupSymbol.children.single().detail)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `document symbols and definitions expose nested function and fixed placement subjects`() {
        val repository = createGovernedTestRepository("athena-lsp-function-placement-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system FunctionPlacement {
              device KM1 {
                type Contactor
                port A1 {
                  direction in
                  signal Control
                }
                port A2 {
                  direction out
                  signal Control
                }
                function coil {
                  role coil
                  ports (A1, A2)
                }
              }
              layout schematic {
                place KM1.coil at (7, 4) orientation vertical
              }
            }
        """.trimIndent()
        sourcePath.writeText(governedAthenaSource(sourceText))

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()
            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(TextDocumentItem(documentUri, "athena", 1, sourceText)),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams(TextDocumentIdentifier(documentUri)),
            ).get()
            val system = symbols.single().right
            val device = system.children.single { child -> child.name == "KM1" }
            val function = device.children.single { child -> child.name == "coil" }
            val layout = system.children.single { child -> child.name == "schematic" }

            assertEquals(SymbolKind.Function, function.kind)
            assertEquals("engineering function: coil", function.detail)
            assertEquals(listOf("role", "ports"), function.children.map { child -> child.name })
            assertEquals("place KM1.coil at (7, 4) orientation vertical", layout.children.single().name)

            val functionBodyCompletions = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(11, 4)
                },
            ).get().right.items.map { item -> item.label }
            assertTrue(functionBodyCompletions.containsAll(listOf("role", "ports")))

            val orientationCompletions = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = sourceText.positionOf("vertical", occurrence = 0)
                },
            ).get().right.items.map { item -> item.label }
            assertTrue(orientationCompletions.containsAll(listOf("horizontal", "vertical")))

            val semanticTokens = server.textDocumentService.semanticTokensFull(
                SemanticTokensParams(TextDocumentIdentifier(documentUri)),
            ).get()
            val functionTokens = sourceText.semanticTokenTexts(
                encodedTokens = semanticTokens.data,
                tokenType = athenaSemanticTokenTypes.indexOf("athenaFunctionKeyword"),
            )
            assertTrue(
                functionTokens.containsAll(listOf("function", "role", "ports")),
                functionTokens.toString(),
            )
            val layoutTokens = sourceText.semanticTokenTexts(
                encodedTokens = semanticTokens.data,
                tokenType = athenaSemanticTokenTypes.indexOf("athenaLayoutKeyword"),
            )
            assertTrue(
                layoutTokens.containsAll(listOf("place", "at", "orientation", "vertical")),
                layoutTokens.toString(),
            )

            val functionReference = sourceText.positionOf("KM1.coil", occurrence = 0)
            val definition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = functionReference
                },
            ).get()
            assertEquals(sourceText.positionOf("coil", occurrence = 0).line, definition.left.single().range.start.line)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `cabinet installation source exposes outline and semantic tokens without diagnostics`() {
        val packageName = "com.engineeringood.m35.installation"
        val rawSource = """
            system Demo {
              device QF1 {
                type Breaker
              }
              device M1 {
                type Motor
              }
              port QF1.line {
                direction out
              }
              port M1.line {
                direction in
              }
              connect feeder QF1.line -> M1.line

              installation cabinet MainCabinet {
                enclosure ENC1 size (800mm, 600mm, 250mm)
                surface Backplate in ENC1 at (20mm, 20mm) size (760mm, 560mm) accepts [din35, screw]
                rail DIN1 on Backplate at (60mm, 120mm) length 680mm orientation horizontal mounting din35
                duct D1 in ENC1 at (30mm, 60mm) size (40mm, 480mm) orientation vertical wall 2mm
                channel C1 in D1 at (4mm, 8mm) size (32mm, 464mm) lanes 4 margin 2mm
                terminal-group XT1 in ENC1 at (560mm, 420mm) size (160mm, 80mm) orientation horizontal accepts [terminal]
                mount QF1 as QF1Mount on DIN1 at (0mm, 0mm) {
                  footprint (45mm, 90mm, 70mm)
                  mounting din35
                  orientation deg0
                  allowed-orientations [deg0]
                  clearance (0mm, 0mm, 0mm, 0mm)
                  compatible-containers [cabinet]
                }
                mount M1 as M1Mount on DIN1 at (100mm, 0mm) {
                  footprint (120mm, 160mm, 100mm)
                  mounting din35
                  orientation deg0
                  allowed-orientations [deg0]
                  clearance (0mm, 0mm, 0mm, 0mm)
                  compatible-containers [cabinet]
                }
                route feeder through [C1]
              }
            }
        """.trimIndent()
        val sourceText = governedAthenaSource(rawSource, packageName)
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-m35-installation-",
            packageName = packageName,
            sourceFileName = "installation.athena",
            sourceText = sourceText,
        )
        val server = AthenaLanguageServer()
        val client = AthenaRecordingLanguageClient()
        server.connect(client)
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repository.repositoryRoot.toUri().toString()
            }).get()
            val documentUri = repository.seedSourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(TextDocumentItem(documentUri, "athena", 1, sourceText)),
            )

            val diagnostics = client.publishedDiagnostics.last().diagnostics
            assertEquals(
                0,
                diagnostics.size,
                diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.code?.left ?: "<no-code>"}: ${diagnostic.message}"
                },
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams(TextDocumentIdentifier(documentUri)),
            ).get()
            val installation = symbols.single().right.children.single().children.single { child ->
                child.name == "MainCabinet"
            }
            assertEquals(SymbolKind.Module, installation.kind)
            assertEquals("installation cabinet", installation.detail)
            assertEquals(
                listOf(
                    "enclosure ENC1",
                    "surface Backplate",
                    "rail DIN1",
                    "duct D1",
                    "channel C1",
                    "terminal-group XT1",
                    "mount QF1Mount",
                    "mount M1Mount",
                    "route feeder",
                ),
                installation.children.map { child -> child.name },
            )

            val semanticTokens = server.textDocumentService.semanticTokensFull(
                SemanticTokensParams(TextDocumentIdentifier(documentUri)),
            ).get()
            val installationTokens = sourceText.semanticTokenTexts(
                encodedTokens = semanticTokens.data,
                tokenType = athenaSemanticTokenTypes.indexOf("athenaLayoutKeyword"),
            )
            assertTrue(
                installationTokens.containsAll(
                    listOf(
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
                        "through",
                    ),
                ),
                installationTokens.toString(),
            )

            val formatted = server.textDocumentService.formatting(
                DocumentFormattingParams(TextDocumentIdentifier(documentUri), FormattingOptions()),
            ).get().single().newText
            assertEquals(
                """
                package com.engineeringood.m35.installation

                system Demo {
                  device QF1 {
                    type Breaker
                  }

                  device M1 {
                    type Motor
                  }

                  port QF1.line {
                    direction out
                  }

                  port M1.line {
                    direction in
                  }

                  connect feeder QF1.line -> M1.line

                  installation cabinet MainCabinet {
                    enclosure ENC1 size (800mm, 600mm, 250mm)
                    surface Backplate in ENC1 at (20mm, 20mm) size (760mm, 560mm) accepts [din35, screw]
                    rail DIN1 on Backplate at (60mm, 120mm) length 680mm orientation horizontal mounting din35
                    duct D1 in ENC1 at (30mm, 60mm) size (40mm, 480mm) orientation vertical wall 2mm
                    channel C1 in D1 at (4mm, 8mm) size (32mm, 464mm) lanes 4 margin 2mm
                    terminal-group XT1 in ENC1 at (560mm, 420mm) size (160mm, 80mm) orientation horizontal accepts [terminal]
                    mount QF1 as QF1Mount on DIN1 at (0mm, 0mm) {
                      footprint (45mm, 90mm, 70mm)
                      mounting din35
                      orientation deg0
                      allowed-orientations [deg0]
                      clearance (0mm, 0mm, 0mm, 0mm)
                      compatible-containers [cabinet]
                    }
                    mount M1 as M1Mount on DIN1 at (100mm, 0mm) {
                      footprint (120mm, 160mm, 100mm)
                      mounting din35
                      orientation deg0
                      allowed-orientations [deg0]
                      clearance (0mm, 0mm, 0mm, 0mm)
                      compatible-containers [cabinet]
                    }
                    route feeder through [C1]
                  }
                }
                """.trimIndent() + "\n",
                formatted,
            )

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams().apply {
                    textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                    contentChanges = listOf(TextDocumentContentChangeEvent(formatted))
                },
            )
            val secondFormat = server.textDocumentService.formatting(
                DocumentFormattingParams(TextDocumentIdentifier(documentUri), FormattingOptions()),
            ).get().single().newText
            assertEquals(formatted, secondFormat)
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private fun String.positionOf(needle: String, occurrence: Int): Position {
    var offset = -1
    repeat(occurrence + 1) {
        offset = indexOf(needle, offset + 1)
        require(offset >= 0) { "Missing `$needle` occurrence $occurrence" }
    }
    val prefix = substring(0, offset)
    return Position(prefix.count { character -> character == '\n' }, offset - (prefix.lastIndexOf('\n') + 1))
}

private fun String.semanticTokenTexts(encodedTokens: List<Int>, tokenType: Int): List<String> {
    val lines = lines()
    var line = 0
    var start = 0
    return encodedTokens.chunked(5).mapNotNull { token ->
        line += token[0]
        start = if (token[0] == 0) start + token[1] else token[1]
        if (token[3] == tokenType) lines[line].substring(start, start + token[2]) else null
    }
}
