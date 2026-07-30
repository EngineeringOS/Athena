package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaRepresentationSourceLspSupportTest {
    @Test
    @Suppress("DEPRECATION")
    fun `representation source outline completion formatting tokens and definition are LSP owned`() {
        val repository = createGovernedTestRepository("athena-representation-lsp-")
        repository.enableRepresentationPackageRoots()
        val representationPath = repository.repositoryRoot
            .resolve("packages")
            .resolve("representation")
            .resolve("athena")
            .resolve("vendor")
            .also { it.createDirectories() }
            .resolve("drive.athena")
        representationPath.writeText(UNFORMATTED_REPRESENTATION_SOURCE)
        representationPath.parent.resolve("vendor-drive.svg").writeText(VALID_VENDOR_DRIVE_SVG)
        val server = AthenaLanguageServer()
        try {
            val capabilities = server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repository.repositoryRoot.toUri().toString()
            }).get().capabilities
            assertTrue(capabilities.documentFormattingProvider.left)
            assertNotNull(capabilities.semanticTokensProvider)

            val documentUri = representationPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(documentUri, "athena", 1, UNFORMATTED_REPRESENTATION_SOURCE),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get().map { it.right }
            val packageSymbol = symbols.single()
            assertEquals("athena.vendor", packageSymbol.name)
            assertEquals("package", packageSymbol.detail)
            assertEquals(
                listOf("vendor_drive_symbol", "vendor_drive_label_symbol", "vendor_drive_element"),
                packageSymbol.children.map { it.name },
            )
            assertEquals(
                listOf("identity", "version", "resource vendor_drive_svg", "graphic svg resource"),
                packageSymbol.children.first().children.map { it.name },
            )
            assertEquals(listOf("identity", "version", "graphic"), packageSymbol.children[1].children.map { it.name })
            assertEquals(
                listOf("identity", "version", "bounds", "child label", "child body", "export label deviceTag"),
                packageSymbol.children.last().children.map { it.name },
            )

            val completion = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(16, 2)
                },
            ).get().right.items.map { it.label }
            assertTrue(completion.containsAll(listOf("symbol", "element", "graphic", "svg", "resource", "label", "child", "export")))
            assertFalse(completion.any { it in FORBIDDEN_RUNTIME_VOCABULARY }, completion.toString())

            val edits = server.textDocumentService.formatting(
                DocumentFormattingParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    options = FormattingOptions(2, true)
                },
            ).get()
            val formatted = edits.single().newText
            assertEquals(EXPECTED_FORMATTED_REPRESENTATION_SOURCE, formatted)
            val secondFormat = server.textDocumentService.formatting(
                DocumentFormattingParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    options = FormattingOptions(2, true)
                },
            ).get().single().newText
            assertEquals(formatted, secondFormat)

            val tokens = server.textDocumentService.semanticTokensFull(
                SemanticTokensParams(TextDocumentIdentifier(documentUri)),
            ).get()
            assertTrue(tokens.data.isNotEmpty(), "expected semantic tokens for representation source")

            assertDefinitionLine(
                server = server,
                documentUri = documentUri,
                source = UNFORMATTED_REPRESENTATION_SOURCE,
                needle = "graphic svg resource vendor_drive_svg",
                characterOffset = "graphic svg resource ".length,
                expectedDeclaration = "resource vendor_drive_svg",
            )
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `complete M34 representation vocabulary has typed LSP syntax UX`() {
        val repository = createGovernedTestRepository("athena-representation-vocabulary-lsp-")
        repository.enableRepresentationPackageRoots()
        val representationPath = repository.repositoryRoot
            .resolve("packages")
            .resolve("representation")
            .resolve("athena")
            .resolve("iec")
            .also { it.createDirectories() }
            .resolve("control.athena")
        representationPath.writeText(REPRESENTATION_VOCABULARY_SOURCE)
        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repository.repositoryRoot.toUri().toString()
            }).get()
            val documentUri = representationPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(documentUri, "athena", 1, REPRESENTATION_VOCABULARY_SOURCE),
                ),
            )

            val packageSymbol = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get().single().right
            assertEquals(
                listOf("iec_contactor_coil", "iec_contactor_coil_element", "ControlDrawingIEC", "ContactorCoilIEC"),
                packageSymbol.children.map { symbol -> symbol.name },
            )
            assertEquals(
                listOf("identity", "version", "graphic", "A1"),
                packageSymbol.children[0].children.map { symbol -> symbol.name },
            )
            assertEquals(
                listOf(
                    "bounds",
                    "line terminalA1",
                    "polyline reference",
                    "arc coilMark",
                    "circle terminalMark",
                    "rectangle coilBody",
                    "label deviceTag",
                ),
                packageSymbol.children[0].children[2].children.orEmpty().map { symbol -> symbol.name },
            )
            assertEquals(
                listOf("identity", "version", "bounds", "child glyph", "export anchor A1", "export label deviceTag"),
                packageSymbol.children[1].children.map { symbol -> symbol.name },
            )
            assertEquals(
                listOf("projection", "standard", "style", "fallback"),
                packageSymbol.children[2].children.map { symbol -> symbol.name },
            )
            assertEquals(
                listOf("profile", "priority", "select function", "use element", "variant"),
                packageSymbol.children[3].children.map { symbol -> symbol.name },
            )

            val completions = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(0, 0)
                },
            ).get().right.items.map { item -> item.label }.toSet()
            assertTrue(
                completions.containsAll(
                    setOf(
                        "polyline", "points", "arc", "center", "radius", "sweep", "circle", "rectangle",
                        "label", "size", "profile", "projection", "standard", "fallback", "binding", "priority",
                        "select", "where", "use", "variant", "function",
                    ),
                ),
                completions.toString(),
            )

            val formatted = server.textDocumentService.formatting(
                DocumentFormattingParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    options = FormattingOptions(2, true)
                },
            ).get().single().newText
            assertEquals(REPRESENTATION_VOCABULARY_SOURCE + "\n", formatted)

            val decodedTokens = decodeSemanticTokens(
                source = REPRESENTATION_VOCABULARY_SOURCE,
                data = server.textDocumentService.semanticTokensFull(
                    SemanticTokensParams(TextDocumentIdentifier(documentUri)),
                ).get().data,
            )
            assertTrue("athenaRepresentationKeyword:symbol" in decodedTokens, decodedTokens.toString())
            assertTrue("athenaPrimitiveKeyword:polyline" in decodedTokens, decodedTokens.toString())
            assertTrue("athenaProfileKeyword:profile" in decodedTokens, decodedTokens.toString())
            assertTrue("athenaBindingKeyword:binding" in decodedTokens, decodedTokens.toString())
            assertTrue("athenaBindingKeyword:function" in decodedTokens, decodedTokens.toString())

            assertDefinitionLine(
                server = server,
                documentUri = documentUri,
                source = REPRESENTATION_VOCABULARY_SOURCE,
                needle = "symbol \"iec.contactor.coil.symbol\"",
                characterOffset = "symbol \"".length,
                expectedDeclaration = "symbol iec_contactor_coil",
            )
            assertDefinitionLine(
                server = server,
                documentUri = documentUri,
                source = REPRESENTATION_VOCABULARY_SOURCE,
                needle = "profile ControlDrawingIEC",
                occurrence = 1,
                characterOffset = "profile ".length,
                expectedDeclaration = "profile ControlDrawingIEC",
            )
            assertDefinitionLine(
                server = server,
                documentUri = documentUri,
                source = REPRESENTATION_VOCABULARY_SOURCE,
                needle = "use element \"iec.contactor.coil.element\"",
                characterOffset = "use element \"".length,
                expectedDeclaration = "element iec_contactor_coil_element",
            )
            assertDefinitionLine(
                server = server,
                documentUri = documentUri,
                source = REPRESENTATION_VOCABULARY_SOURCE,
                needle = "from glyph.deviceTag",
                characterOffset = "from glyph.".length,
                expectedDeclaration = "label deviceTag",
            )
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `representation package diagnostics resolve sibling profiles and elements`() {
        val repository = createGovernedTestRepository("athena-representation-package-lsp-")
        repository.enableRepresentationPackageRoots()
        val packageDir = repository.repositoryRoot
            .resolve("packages")
            .resolve("representation")
            .resolve("com")
            .resolve("engineeringood")
            .resolve("m34")
            .resolve("control")
            .resolve("field")
            .also { it.createDirectories() }
        packageDir.resolve("profile.athena").writeText(FIELD_PROFILE_SOURCE)
        packageDir.resolve("material.athena").writeText(FIELD_MATERIAL_SOURCE)
        val fieldBindings = packageDir.resolve("field-bindings.athena")
        fieldBindings.writeText(FIELD_BINDING_SOURCE)
        val features = AthenaLanguageFeatures(
            compiler = AthenaCompiler(),
            repositoryRoot = repository.repositoryRoot,
            sourceRootPath = repository.repositoryRoot.resolve("src"),
        )

        val tracked = features.trackDocument(
            uri = fieldBindings.toUri().toString(),
            path = fieldBindings,
            version = 1,
            text = FIELD_BINDING_SOURCE,
        )

        val diagnosticCodes = tracked.representation?.diagnostics.orEmpty().map { diagnostic -> diagnostic.code }
        assertFalse("binding.profile.unresolved" in diagnosticCodes, diagnosticCodes.toString())
        assertFalse("binding.target.element.unresolved" in diagnosticCodes, diagnosticCodes.toString())
        assertTrue(diagnosticCodes.isEmpty(), diagnosticCodes.toString())
        repository.repositoryRoot.toFile().deleteRecursively()
    }

    @Test
    fun `M34 field bindings resolve package profile and element declarations`() {
        val repositoryRoot = workspaceRoot()
            .resolve("examples/m34/professional-control-drawing")
            .toAbsolutePath()
            .normalize()
        val fieldBindings = repositoryRoot.resolve(
            "packages/representation/com/engineeringood/m34/control/field/field-bindings.athena",
        )
        val text = Files.readString(fieldBindings)
        val features = AthenaLanguageFeatures(
            compiler = AthenaCompiler(),
            repositoryRoot = repositoryRoot,
            sourceRootPath = repositoryRoot.resolve("src"),
        )

        val tracked = features.trackDocument(
            uri = fieldBindings.toUri().toString(),
            path = fieldBindings,
            version = 1,
            text = text,
        )

        val diagnosticCodes = tracked.representation?.diagnostics.orEmpty().map { diagnostic -> diagnostic.code }
        assertFalse("binding.profile.unresolved" in diagnosticCodes, diagnosticCodes.toString())
        assertFalse("binding.target.element.unresolved" in diagnosticCodes, diagnosticCodes.toString())
        assertTrue(diagnosticCodes.isEmpty(), diagnosticCodes.toString())
    }

    @Test
    @Suppress("DEPRECATION")
    fun `representation diagnostics use compiler vocabulary for governed SVG and reject runtime names`() {
        val repository = createGovernedTestRepository("athena-representation-svg-lsp-")
        repository.enableRepresentationPackageRoots()
        val packageDir = repository.repositoryRoot
            .resolve("packages")
            .resolve("representation")
            .resolve("athena")
            .resolve("vendor")
            .also { it.createDirectories() }
        val representationPath = packageDir.resolve("invalid.athena")
        val svgPath = packageDir.resolve("vendor-drive.svg")
        representationPath.writeText(INVALID_REPRESENTATION_SOURCE)
        svgPath.writeText(
            """
                <svg xmlns="http://www.w3.org/2000/svg" data-athena-schema="representation/v1" data-athena-kind="symbol">
                  <line id="line" x1="0" y1="0" x2="10" y2="10" data-athena-role="terminal"/>
                </svg>
            """.trimIndent(),
        )
        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        try {
            server.connect(client)
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repository.repositoryRoot.toUri().toString()
            }).get()
            val documentUri = representationPath.toUri().toString()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(documentUri, "athena", 1, INVALID_REPRESENTATION_SOURCE),
                ),
            )
            val svgUri = svgPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(svgUri, "svg", 1, svgPath.toFile().readText()),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last { it.uri == documentUri }.diagnostics
            val codes = diagnostics.mapNotNull { it.code?.left }.toSet()
            assertTrue("representation.syntax.invalid" in codes || "representation.source-unit.project-forbidden" in codes, codes.toString())
            assertFalse(diagnostics.any { it.message.contains("XML", ignoreCase = true) }, diagnostics.toString())
            val svgDiagnostics = client.publishedDiagnostics.last { it.uri == svgUri }.diagnostics
            assertEquals(setOf("svg.metadata.forbidden", "svg.viewbox.invalid"), svgDiagnostics.mapNotNull { it.code?.left }.toSet())

            val completion = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(2, 2)
                },
            ).get().right.items.map { it.label }
            assertFalse(completion.any { it in FORBIDDEN_RUNTIME_VOCABULARY }, completion.toString())
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `invalid svg-backed geometry bindings publish unresolved anchor diagnostics through lsp`() {
        val repository = createGovernedTestRepository("athena-representation-invalid-binding-lsp-")
        repository.enableRepresentationPackageRoots()
        val packageDir = repository.repositoryRoot
            .resolve("packages")
            .resolve("representation")
            .resolve("athena")
            .resolve("vendor")
            .also { it.createDirectories() }
        val representationPath = packageDir.resolve("invalid-binding.athena")
        val svgPath = packageDir.resolve("vendor-drive.svg")
        representationPath.writeText(INVALID_BINDING_REPRESENTATION_SOURCE)
        svgPath.writeText(UNMARKED_VENDOR_DRIVE_SVG)
        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        try {
            server.connect(client)
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repository.repositoryRoot.toUri().toString()
            }).get()
            val documentUri = representationPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(documentUri, "athena", 1, INVALID_BINDING_REPRESENTATION_SOURCE),
                ),
            )
            val svgUri = svgPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(svgUri, "svg", 1, svgPath.toFile().readText()),
                ),
            )

            val diagnostics = client.publishedDiagnostics.last { it.uri == documentUri }.diagnostics
            assertTrue(
                diagnostics.any { it.code?.left == "symbol.anchor.ref.unresolved" },
                diagnostics.toString(),
            )
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    private companion object {
        val FORBIDDEN_RUNTIME_VOCABULARY = setOf(
            "descriptor",
            "occurrence",
            "renderer",
            "transport",
            "DOM",
            "XML",
            "package snapshot",
            "Graphic Primitive",
        )

        val UNFORMATTED_REPRESENTATION_SOURCE = """
            package athena.vendor
            symbol vendor_drive_symbol {
            identity "vendor.drive.symbol"
            version "1.0.0"
            resource vendor_drive_svg {
            kind svg
            path "./vendor-drive.svg"
            }
            graphic svg resource vendor_drive_svg
            }
            symbol vendor_drive_label_symbol {
            identity "vendor.drive.label.symbol"
            version "1.0.0"
            graphic {
            bounds (0, 0, 120, 14)
            rectangle plate at (0, 0) size (120, 14) style symbol
            label deviceTag at (4, 1) size (112, 12) role device-tag style device-label
            }
            }
            element vendor_drive_element {
            identity "vendor.drive.element"
            version "1.0.0"
            bounds (0, 0, 120, 78)
            child label { symbol "vendor.drive.label.symbol" translate (0, 0) rotate 0 scale (1, 1) zOrder 0 }
            child body { symbol "vendor.drive.symbol" translate (0, 18) rotate 0 scale (1, 1) zOrder 1 }
            export label deviceTag from label.deviceTag
            }
        """.trimIndent()

        val EXPECTED_FORMATTED_REPRESENTATION_SOURCE = """
            package athena.vendor

            symbol vendor_drive_symbol {
              identity "vendor.drive.symbol"
              version "1.0.0"
              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }

            symbol vendor_drive_label_symbol {
              identity "vendor.drive.label.symbol"
              version "1.0.0"

              graphic {
                bounds (0, 0, 120, 14)
                rectangle plate at (0, 0) size (120, 14) style symbol
                label deviceTag at (4, 1) size (112, 12) role device-tag style device-label
              }
            }

            element vendor_drive_element {
              identity "vendor.drive.element"
              version "1.0.0"
              bounds (0, 0, 120, 78)

              child label {
                symbol "vendor.drive.label.symbol"
                translate (0, 0)
                rotate 0
                scale (1, 1)
                zOrder 0
              }

              child body {
                symbol "vendor.drive.symbol"
                translate (0, 18)
                rotate 0
                scale (1, 1)
                zOrder 1
              }

              export label deviceTag from label.deviceTag
            }
        """.trimIndent() + "\n"

        val INVALID_REPRESENTATION_SOURCE = """
            package athena.vendor

            descriptor not_allowed {
              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        val VALID_VENDOR_DRIVE_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 20">
              <line id="line" x1="0" y1="10" x2="20" y2="10" data-athena-ref="anchor:line"/>
              <line id="load" x1="60" y1="10" x2="80" y2="10" data-athena-ref="anchor:load"/>
            </svg>
        """.trimIndent()

        val REPRESENTATION_VOCABULARY_SOURCE = """
            package athena.iec

            symbol iec_contactor_coil {
              identity "iec.contactor.coil.symbol"
              version "1.0.0"

              graphic {
                bounds (0, 0, 80, 80)
                line terminalA1 from (40, 0) to (40, 14) style conductor
                polyline reference points ((56, 28), (64, 40), (56, 52)) style reference
                arc coilMark center (40, 40) radius 12 from 180 sweep 180 style symbol
                circle terminalMark center (40, 4) radius 2 style terminal
                rectangle coilBody at (20, 14) size (40, 52) style symbol
                label deviceTag at (0, 0) size (80, 12) role device-tag style device-label
              }

              anchor A1 {
                ref "terminalA1"
                direction in
                signal Control.family
                point (40, 0)
                role terminal
              }
            }

            element iec_contactor_coil_element {
              identity "iec.contactor.coil.element"
              version "1.0.0"
              bounds (0, 0, 80, 80)

              child glyph {
                symbol "iec.contactor.coil.symbol"
                translate (0, 0)
                rotate 0
                scale (1, 1)
                zOrder 0
              }

              export anchor A1 from glyph.A1
              export label deviceTag from glyph.deviceTag
            }

            profile ControlDrawingIEC {
              projection schematic
              standard IEC
              style athena-industrial-iec-v1
              fallback fail-closed
            }

            binding ContactorCoilIEC {
              profile ControlDrawingIEC
              priority 200

              select function where {
                type Contactor
                role coil
              }

              use element "iec.contactor.coil.element" version "1.0.0"
              variant "standard"
            }
        """.trimIndent()

        val FIELD_PROFILE_SOURCE = """
            package com.engineeringood.m34.control.field

            profile ControlDrawingIEC {
              projection schematic
              standard IEC
              style athena-industrial-iec-v1
              fallback fail-closed
            }
        """.trimIndent()

        val FIELD_MATERIAL_SOURCE = """
            package com.engineeringood.m34.control.field

            symbol iec_protective_earth_symbol {
              identity "iec.protective-earth.symbol"
              version "1.0.0"

              graphic {
                bounds (0, 0, 80, 80)
                line terminalPe from (40, 0) to (40, 80) style conductor
              }

              anchor PE {
                ref "terminalPe"
                point (40, 0)
                role terminal
                direction out
                signal PE.family
              }
            }

            element iec_protective_earth_element {
              identity "iec.protective-earth.element"
              version "1.0.0"
              bounds (0, 0, 80, 80)

              child glyph {
                symbol "iec.protective-earth.symbol"
                translate (0, 0)
                rotate 0
                scale (1, 1)
                zOrder 0
              }

              export anchor PE from glyph.PE
            }
        """.trimIndent()

        val FIELD_BINDING_SOURCE = """
            package com.engineeringood.m34.control.field

            binding IecProtectiveEarth {
              profile ControlDrawingIEC
              priority 200
              select device where { type ProtectiveEarth }
              use element "iec.protective-earth.element" version "1.0.0"
              variant "standard"
            }
        """.trimIndent()

        val INVALID_BINDING_REPRESENTATION_SOURCE = """
            package athena.vendor

            symbol vendor_drive_svg_symbol {
              identity "vendor.drive.svg.symbol"
              version "1.0.0"

              resource vendor_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_svg

              anchor powerIn {
                ref "svg-0001"
                point (20, 10)
                role terminal
                direction in
                signal Power.family
              }
            }
        """.trimIndent()

        val UNMARKED_VENDOR_DRIVE_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 60">
              <rect id="body" x="8" y="8" width="104" height="44"/>
            </svg>
        """.trimIndent()
    }

    @Suppress("DEPRECATION")
    private fun assertDefinitionLine(
        server: AthenaLanguageServer,
        documentUri: String,
        source: String,
        needle: String,
        characterOffset: Int,
        expectedDeclaration: String,
        occurrence: Int = 0,
    ) {
        val definition = server.textDocumentService.definition(
            org.eclipse.lsp4j.DefinitionParams().apply {
                textDocument = TextDocumentIdentifier(documentUri)
                position = source.positionOf(needle, occurrence, characterOffset)
            },
        ).get().left.single()
        assertEquals(source.lineOf(expectedDeclaration), definition.range.start.line)
    }

    private fun decodeSemanticTokens(source: String, data: List<Int>): Set<String> {
        var line = 0
        var character = 0
        val lines = source.lines()
        return buildSet {
            data.chunked(5).forEach { token ->
                line += token[0]
                character = if (token[0] == 0) character + token[1] else token[1]
                val text = lines[line].substring(character, character + token[2])
                add("${athenaSemanticTokenTypes[token[3]]}:$text")
            }
        }
    }

    private fun workspaceRoot(): java.nio.file.Path {
        var current = java.nio.file.Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts"))) {
                return current
            }
            current = current.parent
        }
        error("Could not locate Athena workspace root from test working directory.")
    }
}

private fun String.positionOf(needle: String, occurrence: Int, characterOffset: Int): Position {
    var index = -1
    repeat(occurrence + 1) {
        index = indexOf(needle, startIndex = index + 1)
        require(index >= 0) { "Missing occurrence ${it + 1} of `$needle`." }
    }
    val target = index + characterOffset
    val line = substring(0, target).count { character -> character == '\n' }
    val previousLineEnd = lastIndexOf('\n', startIndex = target - 1)
    return Position(line, target - previousLineEnd - 1)
}

private fun String.lineOf(needle: String): Int {
    val index = indexOf(needle)
    require(index >= 0) { "Missing `$needle`." }
    return substring(0, index).count { character -> character == '\n' }
}

private fun AthenaLspTestRepository.enableRepresentationPackageRoots() {
    repositoryRoot.resolve("athena.yaml").writeText(
        """
            primaryPackage:
              name: com.engineeringood.factoryline
              version: 0.1.0
              sourceRoot: src
            representationPackageRoots:
              - packages/representation
        """.trimIndent(),
    )
}
