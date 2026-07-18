package com.engineeringood.athena.language.antlr

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Grammar-level smoke coverage; the public ANTLR-backed parser is exercised separately. */
class AthenaGrammarSmokeTest {
    @Test
    fun `parses ordered import declarations before the system block`() {
        val source =
            """
            package com.engineeringood.root
            import com.engineeringood.controls
            import com.engineeringood.controls.Switch
            system Demo {}
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        assertEquals(
            listOf("com.engineeringood.controls", "com.engineeringood.controls.Switch"),
            parse.tree.importDecl().map { it.packageName().text },
        )
    }

    @Test
    fun `keeps import contextual in existing identifier positions`() {
        val source =
            """
            system import {
              device import {
                import import
              }
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        val system = parse.tree.systemDecl()
        assertEquals("import", system.ident().text)
        val device = system.declaration().single().deviceDecl()
        assertEquals("import", device.ident().text)
        val property = device.propertyAssignment().single()
        assertEquals("import", property.ident().text)
        assertEquals("import", property.scalarValue().ident().text)
    }

    @Test
    fun `accepts valid and rejects malformed hyphenated import targets`() {
        val valid = parseSource("import com.m18-controls.Switch2\nsystem Demo {}")
        assertTrue(valid.errors.isEmpty(), "Unexpected syntax errors: ${valid.errors}")
        assertEquals(listOf("com", "m18-controls", "Switch2"), valid.tree.importDecl().single().packageName().packageNameSegment().map { it.text })

        listOf(
            "import com.-controls\nsystem Demo {}",
            "import com.controls-\nsystem Demo {}",
            "import com.controls--switch\nsystem Demo {}",
        ).forEach { source ->
            assertTrue(parseSource(source).errors.isNotEmpty(), "Expected malformed import to fail: $source")
        }
    }

    @Test
    fun `parses a governed package declaration before the system block`() {
        val source =
            """
            package com.engineeringood.factory-line
            system Demo {
              connect plc.out -> plc.input
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        assertTrue(parse.tree.exception == null)
        assertEquals(
            listOf("com", "engineeringood", "factory-line"),
            parse.tree.packageDecl().packageName().packageNameSegment().map { it.text },
        )
    }

    @Test
    fun `keeps package contextual without breaking arrow or dotted reference parsing`() {
        val source =
            """
            system Demo {
              device package {
                package package
              }
              port plc.out {
                package package
              }
              connect plc.out -> plc.input
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        assertEquals(3, parse.tree.systemDecl().declaration().size)
        val connect = parse.tree.systemDecl().declaration(2).connectDecl()
        assertEquals("->", connect.ARROW().text)
        assertEquals(listOf("plc.out", "plc.input"), connect.twoPartName().map { it.text })
    }

    @Test
    fun `parses the demo cabinet fixture without syntax errors`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        assertTrue(parse.tree.exception == null)
        assertTrue(parse.tree.systemDecl().declaration().isNotEmpty())
    }

    @Test
    fun `accepts a leading UTF-8 BOM like the handwritten tokenizer`() {
        val source = "\uFEFF" +
            """
            system Demo {
              device PLC1 {
                type Switch
              }
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        assertEquals("Demo", parse.tree.systemDecl().ident().text)
    }

    @Test
    fun `accepts single-part device names while requiring two-part port names`() {
        val source =
            """
            system Demo {
              device PLC1 {
                type Switch
              }
              port PLC1.out {
                direction out
              }
              connect PLC1.out -> PLC1.out
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        val device = parse.tree.systemDecl().declaration(0).deviceDecl()
        assertEquals("PLC1", device.ident().text)
        val port = parse.tree.systemDecl().declaration(1).portDecl()
        assertEquals("PLC1.out", port.twoPartName().text.replace(" ", ""))
    }

    @Test
    fun `parses system-scoped layout block with admitted m23 statement vocabulary`() {
        val source =
            """
            system MachineNo000 {
              device PLC1 {
                type Switch
              }
              layout schematic-sheet {
                place HMI1 near PLC1
                place XT1 below PLC1
                align HMI1 aligned-with PLC1 axis vertical
                align HMI2 aligned-with PLC1 axis horizontal
                group HMI1 grouped-with PLC1
              }
            }
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isEmpty(), "Unexpected syntax errors: ${parse.errors}")
        val layout = parse.tree.systemDecl().declaration(1).layoutDecl()
        assertEquals("schematic-sheet", layout.viewFamilyName().text)
        assertEquals(5, layout.layoutStatement().size)
        assertEquals("placeHMI1nearPLC1", layout.layoutStatement(0).text)
        assertEquals("placeXT1belowPLC1", layout.layoutStatement(1).text)
        assertEquals("alignHMI1aligned-withPLC1axisvertical", layout.layoutStatement(2).text)
        assertEquals("alignHMI2aligned-withPLC1axishorizontal", layout.layoutStatement(3).text)
        assertEquals("groupHMI1grouped-withPLC1", layout.layoutStatement(4).text)
    }

    @Test
    fun `rejects file-global layout block before the system block`() {
        val source =
            """
            layout schematic-sheet {
              place HMI1 near PLC1
            }
            system MachineNo000 {}
            """.trimIndent()

        val parse = parseSource(source)

        assertTrue(parse.errors.isNotEmpty(), "Expected file-global layout to fail")
    }

    @Test
    fun `m23 parser parity corpus keeps the expected layout syntax inventory`() {
        val corpus = resolveRepoRoot().resolve("examples/m23/parser-parity-proof")
        val discovered = Files.newDirectoryStream(corpus, "*.athena").use { stream ->
            stream.map { path -> path.fileName.toString().removeSuffix(".athena") }.sorted()
        }

        assertEquals(
            listOf(
                "invalid-file-global-layout",
                "invalid-layout-bad-axis",
                "invalid-layout-malformed-place",
                "invalid-layout-missing-target",
                "valid-layout-block",
            ),
            discovered,
        )
    }

    @Test
    fun `m23 parser parity corpus accepts valid layout syntax and rejects invalid layout syntax`() {
        val corpus = resolveRepoRoot().resolve("examples/m23/parser-parity-proof")
        val valid = parseSource(Files.readString(corpus.resolve("valid-layout-block.athena")))

        assertTrue(valid.errors.isEmpty(), "Unexpected syntax errors: ${valid.errors}")

        listOf(
            "invalid-file-global-layout",
            "invalid-layout-bad-axis",
            "invalid-layout-malformed-place",
            "invalid-layout-missing-target",
        ).forEach { name ->
            val parse = parseSource(Files.readString(corpus.resolve("$name.athena")))
            assertTrue(parse.errors.isNotEmpty(), "Expected $name to fail")
        }
    }

    private fun parseSource(source: String): AntlrParse {
        val errors = mutableListOf<String>()
        val listener = object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?,
            ) {
                errors += "line $line:$charPositionInLine $msg"
            }
        }

        val lexer = AthenaLexer(CharStreams.fromString(source))
        lexer.removeErrorListeners()
        lexer.addErrorListener(listener)
        val tokens = CommonTokenStream(lexer)
        val parser = AthenaParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(listener)
        val tree = parser.sourceFile()
        return AntlrParse(tree = tree, errors = errors)
    }

    private data class AntlrParse(
        val tree: AthenaParser.SourceFileContext,
        val errors: List<String>,
    )

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts")) &&
                Files.isDirectory(current.resolve("examples"))
            ) {
                return current
            }
            current = current.parent
        }
        error("Could not locate repository root from ${Path.of("").toAbsolutePath()} (${Path.of("").name})")
    }
}
