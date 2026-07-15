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

/**
 * Story 2.1 smoke coverage for the generated ANTLR4 grammar only.
 * Does not exercise `AthenaLanguageParser` (still handwritten until Story 2.2).
 */
class AthenaGrammarSmokeTest {
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
