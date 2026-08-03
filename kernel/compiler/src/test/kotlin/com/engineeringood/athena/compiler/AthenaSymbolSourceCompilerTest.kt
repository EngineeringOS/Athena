package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaRepresentationSourceCompilerSymbolTest {
    private val compiler = AthenaRepresentationSourceCompiler()

    @Test
    fun `compiles native symbol into one canonical graphic primitive definition`() {
        val result = compiler.compile("iec-switch.athena", VALID_SYMBOL)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val definition = result.definitions.single()
        assertEquals(RepresentationDefinitionKind.SYMBOL, definition.definitionKind)
        assertEquals("iec.switch_contact", definition.symbolId.value)
        assertEquals("athena.iec", definition.libraryId.value)
        assertEquals("1.0.0", definition.version.value)
        assertEquals(listOf("line", "load"), definition.graphicBody.primitives.map { it.primitiveId.value })
        assertEquals(listOf("line", "load"), definition.anchors.map { it.anchorId.value })
        assertEquals(listOf("line", "load"), definition.anchors.map { it.primitiveId.value })
        assertTrue(definition.labelSlots.isEmpty())
    }

    @Test
    fun `invalid symbol emits exact source diagnostics and no partial definition`() {
        val invalid = VALID_SYMBOL
            .replace("  identity \"iec.switch_contact\"\n", "")
            .replace("line load from (40, 60) to (40, 80) style conductor", "line line from (40, 60) to (40, 80) style unknown")
            .replace("ref load", "ref missing")

        val result = compiler.compile("invalid.athena", invalid)
        val ast = assertIs<ParseSuccess>(AthenaLanguageParser().parse("invalid.athena", invalid)).ast
        val symbol = assertIs<SymbolDeclaration>(ast.representationDeclarations.single())
        val duplicatePrimitive = assertNotNull(symbol.graphic).primitives[1]
        val unresolvedReference = assertNotNull(symbol.anchors[1].ref)

        assertTrue(result.definitions.isEmpty())
        assertEquals(
            listOf(
                AthenaRepresentationSourceDiagnostic(
                    code = "symbol.anchor.ref.unresolved",
                    file = "invalid.athena",
                    span = unresolvedReference.span,
                    subject = "anchors.load.ref",
                    message = "Symbol anchor references missing geometry primitive `load`.",
                ),
                AthenaRepresentationSourceDiagnostic(
                    code = "symbol.identity.missing",
                    file = "invalid.athena",
                    span = symbol.span,
                    subject = "symbol.iec_switch_contact.identity",
                    message = "Symbol requires an identity.",
                ),
                AthenaRepresentationSourceDiagnostic(
                    code = "symbol.primitive.id.duplicate",
                    file = "invalid.athena",
                    span = duplicatePrimitive.span,
                    subject = "graphic.primitives.line",
                    message = "Graphic primitive ids must be unique.",
                ),
                AthenaRepresentationSourceDiagnostic(
                    code = "symbol.style.unknown",
                    file = "invalid.athena",
                    span = duplicatePrimitive.span,
                    subject = "graphic.primitives.line.style",
                    message = "Unknown governed Symbol style `unknown`.",
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `rejects incomplete duplicate and out of bounds anchors without partial definitions`() {
        val invalid = VALID_SYMBOL
            .replaceFirst("ref \"line\"", "")
            .replaceFirst("    role terminal\n", "")
            .replace("    point (40, 80)", "    point (90, 80)")
            .replace("  anchor load {", "  anchor line {")

        val result = compiler.compile("invalid-anchors.athena", invalid)

        assertTrue(result.definitions.isEmpty())
        assertEquals(
            listOf(
                "symbol.anchor.id.duplicate",
                "symbol.anchor.point.out-of-bounds",
                "symbol.anchor.ref.missing",
                "symbol.anchor.role.missing",
            ),
            result.diagnostics.map { it.code },
        )
    }

    @Test
    fun `rejects missing graphic fields invalid bounds and project source`() {
        val invalidGraphic = VALID_SYMBOL
            .replace("    bounds (0, 0, 80, 80)\n", "")
            .replace("    line line from (40, 0) to (40, 20) style conductor\n", "")
            .replace("    line load from (40, 60) to (40, 80) style conductor\n", "")
        val projectSource = """
            system ExistingProject {
              device Motor1 {
                type Motor
              }
            }
        """.trimIndent()

        val invalidResult = compiler.compile("invalid-graphic.athena", invalidGraphic)
        val projectResult = compiler.compile("project.athena", projectSource)

        assertTrue(invalidResult.definitions.isEmpty())
        assertEquals(
            listOf(
                "symbol.anchor.ref.unresolved",
                "symbol.anchor.ref.unresolved",
                "symbol.graphic.bounds.missing",
                "symbol.graphic.primitive.missing",
            ),
            invalidResult.diagnostics.map { it.code },
        )
        assertTrue(projectResult.definitions.isEmpty())
        assertEquals(listOf("representation.source-unit.project-forbidden"), projectResult.diagnostics.map { it.code })
    }

    @Test
    fun `rejects duplicate symbol identities across one compilation batch`() {
        val result = compiler.compile(
            listOf(
                AthenaRepresentationSourceInput("first.athena", VALID_SYMBOL),
                AthenaRepresentationSourceInput("second.athena", VALID_SYMBOL.replace("iec_switch_contact", "other_name")),
            ),
        )

        assertTrue(result.definitions.isEmpty())
        assertEquals(listOf("representation.identity.duplicate", "representation.identity.duplicate"), result.diagnostics.map { it.code })
        assertEquals(listOf("first.athena", "second.athena"), result.diagnostics.map { it.file })
        assertTrue(result.diagnostics.all { it.subject.endsWith(".identity") })
    }

    @Test
    fun `reports unresolved primitive references even when the graphic has no primitives`() {
        val invalid = VALID_SYMBOL
            .replace("    line line from (40, 0) to (40, 20) style conductor\n", "")
            .replace("    line load from (40, 60) to (40, 80) style conductor\n", "")

        val result = compiler.compile("empty-graphic.athena", invalid)

        assertTrue(result.definitions.isEmpty())
        assertEquals(
            listOf(
                "symbol.anchor.ref.unresolved",
                "symbol.anchor.ref.unresolved",
                "symbol.graphic.primitive.missing",
            ),
            result.diagnostics.map { it.code },
        )
    }

    @Test
    fun `covers malformed numbers and unsupported geometry`() {
        for ((name, source) in listOf(
            "leading-plus" to VALID_SYMBOL.replace("(40, 0)", "(+40, 0)"),
            "leading-decimal" to VALID_SYMBOL.replace("(40, 0)", "(.5, 0)"),
            "exponent" to VALID_SYMBOL.replace("(40, 0)", "(4e1, 0)"),
            "unsupported-circle" to VALID_SYMBOL.replace(
                "line line from (40, 0) to (40, 20) style conductor",
                "circle body center (40, 40) radius 20",
            ),
        )) {
            val result = compiler.compile("$name.athena", source)
            assertTrue(result.definitions.isEmpty(), name)
            assertTrue(result.diagnostics.isNotEmpty(), name)
            assertTrue(result.diagnostics.all { it.code == "representation.syntax.invalid" }, name)
        }
    }

    @Test
    fun `rejects a zero length line even when endpoint spans differ`() {
        val invalid = VALID_SYMBOL.replace(
            "line line from (40, 0) to (40, 20) style conductor",
            "line line from (40, 0) to (40, 0) style conductor",
        )

        val result = compiler.compile("zero-length.athena", invalid)

        assertTrue(result.definitions.isEmpty())
        assertTrue(result.diagnostics.any { it.code == "symbol.primitive.geometry.invalid" })
    }

    @Test
    fun `accepts strict semantic versions and rejects malformed prerelease identifiers`() {
        val valid = compiler.compile(
            "build-metadata.athena",
            VALID_SYMBOL.replace("version \"1.0.0\"", "version \"1.0.0+build.1\""),
        )
        val invalid = compiler.compile(
            "malformed-version.athena",
            VALID_SYMBOL.replace("version \"1.0.0\"", "version \"1.0.0-..\""),
        )

        assertTrue(valid.diagnostics.isEmpty(), valid.diagnostics.toString())
        assertEquals(listOf("symbol.version.invalid"), invalid.diagnostics.map { it.code })
        assertTrue(invalid.definitions.isEmpty())
    }

    @Test
    fun `diagnostics remain canonical when source input order reverses`() {
        val first = AthenaRepresentationSourceInput("z.athena", VALID_SYMBOL.replace("style conductor", "style missing"))
        val second = AthenaRepresentationSourceInput("a.athena", VALID_SYMBOL.replace("  version \"1.0.0\"\n", ""))

        val forward = compiler.compile(listOf(first, second))
        val reversed = compiler.compile(listOf(second, first))

        assertEquals(forward.diagnostics, reversed.diagnostics)
        assertEquals(forward.definitions, reversed.definitions)
        assertTrue(forward.definitions.isEmpty())
    }

    @Test
    fun `existing project source still compiles through the project compiler`() {
        val source = """
            system ExistingProject {
              device MotorM1 {
                type Motor
                model "M1"
              }
            }
        """.trimIndent()

        val result = AthenaCompiler().compile(Path.of("project.athena"), source)

        assertIs<CompilerCompilationSuccess>(result)
    }

    @Test
    fun `generic project compiler rejects representation source with typed diagnostic`() {
        val result = AthenaCompiler().compile(Path.of("symbol.athena"), VALID_SYMBOL)

        val failure = assertIs<CompilerCompilationParseFailure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertTrue(failure.diagnostics.single().message.contains("AthenaRepresentationSourceCompiler"))
    }

    @Test
    fun `formatter and diagnostics are deterministic and idempotent`() {
        val first = compiler.format("iec-switch.athena", VALID_SYMBOL)
        val second = compiler.format("iec-switch.athena", first.formattedSource!!)

        assertEquals(first.formattedSource, second.formattedSource)
        assertEquals(first.diagnostics, second.diagnostics)
        assertNull(first.failure)
    }

    @Test
    fun `formatter emits grammar compatible plain decimals`() {
        val source = VALID_SYMBOL.replace("(40, 0)", "(0.0000001, 0)")

        val first = compiler.format("small-decimal.athena", source)
        val formatted = assertNotNull(first.formattedSource)
        val compiled = compiler.compile("small-decimal.athena", formatted)
        val second = compiler.format("small-decimal.athena", formatted)

        assertTrue(compiled.diagnostics.isEmpty(), compiled.diagnostics.toString())
        assertEquals(formatted, second.formattedSource)
        assertTrue(!Regex("""[0-9][eE][+-]?[0-9]""").containsMatchIn(formatted))
    }

    @Test
    fun `diagnostic ordering uses the complete source span as a tie breaker`() {
        fun diagnostic(endOffset: Int) = AthenaRepresentationSourceDiagnostic(
            code = "representation.syntax.invalid",
            file = "same.athena",
            span = SourceSpan(
                start = SourcePosition(offset = 0, line = 1, column = 1),
                end = SourcePosition(offset = endOffset, line = 1, column = endOffset + 1),
            ),
            subject = "source",
            message = "same",
        )
        val short = diagnostic(1)
        val long = diagnostic(2)

        assertEquals(
            listOf(short, long).canonicalRepresentationDiagnostics(),
            listOf(long, short).canonicalRepresentationDiagnostics(),
        )
    }

    @Test
    fun `rejects renderer xml and broader m34 declaration vocabulary in symbol source`() {
        val forbidden = listOf(
            "renderer svg",
            "xml \"symbol.xml\"",
            "descriptor box",
            "occurrence panel",
            "element Nested {}",
            "profile Cabinet {}",
            "binding Switch {}",
        )

        forbidden.forEach { statement ->
            val source = VALID_SYMBOL.replace(
                "  version \"1.0.0\"",
                "  version \"1.0.0\"\n  $statement",
            )
            val result = compiler.compile("forbidden.athena", source)
            assertTrue(result.definitions.isEmpty(), statement)
            assertTrue(result.diagnostics.isNotEmpty(), statement)
        }
    }

    companion object {
        private val VALID_SYMBOL = """
            package athena.iec

            symbol iec_switch_contact {
              identity "iec.switch_contact"
              version "1.0.0"

              graphic {
                bounds (0, 0, 80, 80)
                line line from (40, 0) to (40, 20) style conductor
                line load from (40, 60) to (40, 80) style conductor
              }

              anchor line {
                ref "line"
                point (40, 0)
                role terminal
                direction in
                signal Power.family
              }

              anchor load {
                ref "load"
                point (40, 80)
                role terminal
                direction out
                signal Power.family
              }
            }
        """.trimIndent()
    }
}
