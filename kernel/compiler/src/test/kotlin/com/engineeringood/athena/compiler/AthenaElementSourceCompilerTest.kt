package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicTransform
import com.engineeringood.athena.representation.RepresentationBodyAuthority
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaElementSourceCompilerTest {
    private val compiler = AthenaRepresentationSourceCompiler()

    @Test
    fun `mixed symbols and element compile into one deterministic intrinsic composition`() {
        val parsed = assertIs<ParseSuccess>(AthenaLanguageParser().parse("mixed.athena", VALID_MIXED_SOURCE))
        assertIs<ElementDeclaration>(parsed.ast.representationDeclarations.last())

        val result = compiler.compile("mixed.athena", VALID_MIXED_SOURCE)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf("iec.indicator", "iec.switch_contact", "iec.switch_module"),
            result.definitions.map { it.symbolId.value },
        )
        val element = result.definitions.single { it.definitionKind == RepresentationDefinitionKind.ELEMENT }
        assertEquals(RepresentationBodyAuthority.GRAPHIC_PRIMITIVE, element.bodyAuthority)
        assertEquals(GraphicBounds(0.0, 0.0, 180.0, 100.0), element.graphicBody.bounds)
        assertEquals(listOf("primary", "secondary"), element.intrinsicComposition?.children?.map { it.childId.value })
        assertEquals(listOf(0, 1), element.intrinsicComposition?.children?.map { it.zOrder })
        assertEquals(
            listOf(
                GraphicTransform.Scale(1.0, 1.0, com.engineeringood.athena.representation.GraphicPoint(0.0, 0.0)),
                GraphicTransform.Rotation(0.0, com.engineeringood.athena.representation.GraphicPoint(0.0, 0.0)),
                GraphicTransform.Translation(10.0, 10.0),
            ),
            element.intrinsicComposition?.children?.first()?.transforms,
        )
        assertEquals(
            mapOf(
                "primaryLine" to (50.0 to 10.0),
                "primaryLoad" to (50.0 to 90.0),
                "secondaryInput" to (90.0 to 50.0),
                "secondaryOutput" to (170.0 to 50.0),
            ),
            element.anchors.associate { it.anchorId.value to (it.point.x to it.point.y) },
        )
        val primitiveIds = element.graphicBody.primitives.flatMap(::primitiveIds)
        assertTrue(primitiveIds.containsAll(listOf("primary.line", "secondary.body")), primitiveIds.toString())
        assertEquals(primitiveIds.size, primitiveIds.distinct().size)
        assertEquals(1, element.graphicBody.styleTokens.count { it.styleTokenId.value == "conductor" })
    }

    @Test
    fun `compile and format are deterministic across source input order and repeated formatting`() {
        val forward = compiler.compile(
            listOf(
                AthenaRepresentationSourceInput("symbols.athena", SYMBOL_SOURCE),
                AthenaRepresentationSourceInput("element.athena", ELEMENT_SOURCE),
            ),
        )
        val reverse = compiler.compile(
            listOf(
                AthenaRepresentationSourceInput("element.athena", ELEMENT_SOURCE),
                AthenaRepresentationSourceInput("symbols.athena", SYMBOL_SOURCE),
            ),
        )

        assertTrue(forward.diagnostics.isEmpty(), forward.diagnostics.toString())
        assertEquals(forward.definitions, reverse.definitions)
        val first = compiler.format("mixed.athena", VALID_MIXED_SOURCE)
        val second = compiler.format("mixed.athena", assertNotNull(first.formattedSource))
        assertEquals(first.formattedSource, second.formattedSource)
        assertTrue(first.diagnostics.isEmpty(), first.diagnostics.toString())
    }

    @Test
    fun `duplicate symbol identity diagnostics do not depend on referenced duplicate file order`() {
        val forwardInputs = listOf(
            AthenaRepresentationSourceInput("symbols.athena", SYMBOL_SOURCE),
            AthenaRepresentationSourceInput("duplicate.athena", DUPLICATE_SYMBOL_SOURCE),
            AthenaRepresentationSourceInput("element.athena", ELEMENT_SOURCE),
        )

        val forward = compiler.compile(forwardInputs)
        val reverse = compiler.compile(forwardInputs.reversed())

        assertEquals(forward.diagnostics, reverse.diagnostics)
        assertEquals(
            setOf("representation.identity.duplicate"),
            forward.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
        assertEquals(2, forward.diagnostics.size)
    }

    @Test
    fun `M34 sample native representation package compiles without XML authority`() {
        val source = repositoryRoot().resolve(
            Path.of(
                "examples",
                "m34",
                "sample-project",
                "packages",
                "representation",
                "athena",
                "iec",
                "epic1-native-elements.athena",
            ),
        ).readText()

        val result = compiler.compile("examples/m34/sample-project/packages/representation/athena/iec/epic1-native-elements.athena", source)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf("iec.indicator_lamp", "iec.switch_contact", "iec.switch_module"),
            result.definitions.map { it.symbolId.value },
        )
        assertEquals(
            RepresentationDefinitionKind.ELEMENT,
            result.definitions.single { it.symbolId.value == "iec.switch_module" }.definitionKind,
        )
    }

    @Test
    fun `invalid element classes emit exact source diagnostics and admit no definitions`() {
        invalidCases().forEach { case ->
            val result = compiler.compile(case.file, case.source)
            val diagnostic = result.diagnostics.singleOrNull { it.code == case.code && it.subject == case.subject }

            assertTrue(result.definitions.isEmpty(), case.name)
            assertNotNull(diagnostic, "$case\n${result.diagnostics}")
            assertEquals(case.file, diagnostic.file, case.name)
            assertEquals(case.expectedSpan(), diagnostic.span, case.name)
            assertEquals(case.subject, diagnostic.subject, case.name)
            assertEquals(case.message, diagnostic.message, case.name)
            result.diagnostics.forEach { actual ->
                assertTrue(actual.span.end.offset > actual.span.start.offset, "$case produced an empty span: $actual")
                assertTrue(actual.subject.isNotBlank() && actual.message.isNotBlank(), "$case produced incomplete evidence: $actual")
            }
        }
    }

    private fun invalidCases(): List<InvalidCase> = listOf(
        invalid(
            "missing child", VALID_MIXED_SOURCE.replace("symbol \"iec.indicator\"", "symbol \"iec.missing\""),
            "element.child.symbol.unresolved", "\"iec.missing\"", "element.iec_switch_module.children.secondary.symbol",
            "Element child `secondary` references missing same-library Symbol `iec.missing`.",
        ),
        invalid(
            "duplicate child id", VALID_MIXED_SOURCE.replace("child secondary", "child primary"),
            "element.child.id.duplicate", "child primary", "element.iec_switch_module.children.primary",
            "Element child ids must be unique.", occurrence = 2,
        ),
        invalid(
            "duplicate z-order", VALID_MIXED_SOURCE.replace("zOrder 1", "zOrder 0"),
            "element.child.z-order.duplicate", "zOrder 0", "element.iec_switch_module.children.secondary.zOrder",
            "Element child z-orders must be unique.", occurrence = 2,
        ),
        invalid(
            "missing translate", VALID_MIXED_SOURCE.replace("    translate (10, 10)\n", ""),
            "element.child.translate.missing", "child primary", "element.iec_switch_module.children.primary.translate",
            "Element child requires one explicit translate transform.",
        ),
        invalid(
            "missing rotate", VALID_MIXED_SOURCE.replaceFirst("    rotate 0\n", ""),
            "element.child.rotate.missing", "child primary", "element.iec_switch_module.children.primary.rotate",
            "Element child requires one explicit rotate transform.",
        ),
        invalid(
            "missing scale", VALID_MIXED_SOURCE.replaceFirst("    scale (1, 1)\n", ""),
            "element.child.scale.missing", "child primary", "element.iec_switch_module.children.primary.scale",
            "Element child requires one explicit scale transform.",
        ),
        invalid(
            "missing z-order", VALID_MIXED_SOURCE.replace("    zOrder 0\n", ""),
            "element.child.z-order.missing", "child primary", "element.iec_switch_module.children.primary.zOrder",
            "Element child requires one explicit integer zOrder.",
        ),
        invalid(
            "invalid scale", VALID_MIXED_SOURCE.replaceFirst("scale (1, 1)", "scale (0, 1)"),
            "element.child.scale.invalid", "(0, 1)", "element.iec_switch_module.children.primary.scale",
            "Element child scale values must be finite and positive.",
        ),
        invalid(
            "invalid z-order", VALID_MIXED_SOURCE.replace("zOrder 0", "zOrder 0.5"),
            "element.child.z-order.invalid", "zOrder 0.5", "element.iec_switch_module.children.primary.zOrder",
            "Element child zOrder must be a bounded integer.",
        ),
        invalid(
            "missing child anchor", VALID_MIXED_SOURCE.replace("from primary.line", "from primary.missing"),
            "element.export.child-anchor.unresolved", "primary.missing", "element.iec_switch_module.exports.primaryLine",
            "Element export `primaryLine` references missing child anchor `primary.missing`.",
        ),
        invalid(
            "duplicate export", VALID_MIXED_SOURCE.replace(
                "  export anchor primaryLoad from primary.load",
                "  export anchor primaryLine from primary.load\n  export anchor primaryLoad from primary.load",
            ),
            "element.export.id.duplicate", "export anchor primaryLine from primary.load", "element.iec_switch_module.exports.primaryLine",
            "Element exported anchor ids must be unique.",
        ),
        invalid(
            "unexported connectable anchor", VALID_MIXED_SOURCE.replace("  export anchor primaryLoad from primary.load\n", ""),
            "element.child.anchor.unexported", "child primary", "element.iec_switch_module.children.primary.anchors.load",
            "Connectable child anchor `primary.load` must be exported exactly once.",
        ),
        invalid(
            "child outside bounds", VALID_MIXED_SOURCE.replace("translate (90, 10)", "translate (150, 10)"),
            "element.child.bounds.out-of-element", "(150, 10)", "element.iec_switch_module.children.secondary.bounds",
            "Transformed child `secondary` must lie inside Element bounds.",
        ),
        invalid(
            "non-positive bounds", VALID_MIXED_SOURCE.replace("bounds (0, 0, 180, 100)", "bounds (0, 0, 0, 100)"),
            "element.bounds.invalid", "bounds (0, 0, 0, 100)", "element.iec_switch_module.bounds",
            "Element bounds must be finite and positive.",
        ),
        invalid(
            "duplicate symbol element identity", VALID_MIXED_SOURCE.replace("identity \"iec.switch_module\"", "identity \"iec.switch_contact\""),
            "representation.identity.duplicate", "\"iec.switch_contact\"", "element.iec_switch_module.identity",
            "Representation identity `iec.switch_contact` is duplicated in this compilation batch.", occurrence = 2,
        ),
        nestedElementCase(),
        directCycleCase(),
        indirectCycleCase(),
    )

    private fun nestedElementCase(): InvalidCase {
        val source = VALID_MIXED_SOURCE.replace(
            "element iec_switch_module {",
            "element nested {\n  identity \"iec.nested\"\n  version \"1.0.0\"\n  bounds (0, 0, 80, 80)\n" +
                "  child inner {\n    symbol \"iec.switch_contact\"\n    translate (0, 0)\n    rotate 0\n    scale (1, 1)\n    zOrder 0\n  }\n" +
                "  export anchor line from inner.line\n  export anchor load from inner.load\n}\n\nelement iec_switch_module {",
        ).replace("symbol \"iec.indicator\"", "symbol \"iec.nested\"")
        return invalid(
            "nested element", source, "element.child.kind.invalid", "\"iec.nested\"",
            "element.iec_switch_module.children.secondary.symbol",
            "Element child `secondary` must resolve to an atomic Symbol, not Element `iec.nested`.", occurrence = 2,
        )
    }

    private fun directCycleCase(): InvalidCase {
        val source = ELEMENT_SOURCE.replaceFirst("\"iec.switch_contact\"", "\"iec.switch_module\"")
        return invalid(
            "direct cycle", source, "element.composition.cycle", "\"iec.switch_module\"",
            "element.iec_switch_module.composition", "Element composition cycle: iec.switch_module -> iec.switch_module.",
            occurrence = 2,
        )
    }

    private fun indirectCycleCase(): InvalidCase {
        val source = """
            package athena.iec

            element first {
              identity "iec.first"
              version "1.0.0"
              bounds (0, 0, 80, 80)
              child second { symbol "iec.second" translate (0, 0) rotate 0 scale (1, 1) zOrder 0 }
              export anchor line from second.line
            }

            element second {
              identity "iec.second"
              version "1.0.0"
              bounds (0, 0, 80, 80)
              child first { symbol "iec.first" translate (0, 0) rotate 0 scale (1, 1) zOrder 0 }
              export anchor line from first.line
            }
        """.trimIndent()
        return invalid(
            "indirect cycle", source, "element.composition.cycle", "\"iec.first\"",
            "element.first.composition", "Element composition cycle: iec.first -> iec.second -> iec.first.",
            occurrence = 2,
        )
    }

    private fun invalid(
        name: String,
        source: String,
        code: String,
        token: String,
        subject: String,
        message: String,
        occurrence: Int = 1,
    ) = InvalidCase(name, "invalid-$name.athena", source, code, token, occurrence, subject, message)

    private data class InvalidCase(
        val name: String,
        val file: String,
        val source: String,
        val code: String,
        val token: String,
        val occurrence: Int,
        val subject: String,
        val message: String,
    ) {
        fun expectedSpan(): SourceSpan = source.spanOf(token, occurrence)
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
            current = current.parent
        }
        return current
    }

    companion object {
        internal val VALID_MIXED_SOURCE: String
            get() = "$SYMBOL_SOURCE\n\n${ELEMENT_SOURCE.substringAfter("package athena.iec\n\n")}"

        internal val SYMBOL_SOURCE = """
            package athena.iec

            symbol iec_switch_contact {
              identity "iec.switch_contact"
              version "1.0.0"
              graphic {
                bounds (0, 0, 80, 80)
                line line from (40, 0) to (40, 80) style conductor
              }
              anchor line { ref "line" point (40, 0) role terminal direction in signal Power.family }
              anchor load { ref "line" point (40, 80) role terminal direction out signal Power.family }
            }

            symbol iec_indicator {
              identity "iec.indicator"
              version "1.0.0"
              graphic {
                bounds (0, 0, 80, 80)
                line body from (0, 40) to (80, 40) style conductor
              }
              anchor input { ref "body" point (0, 40) role terminal direction in signal Digital.family }
              anchor output { ref "body" point (80, 40) role terminal direction out signal Digital.family }
            }
        """.trimIndent()

        internal val ELEMENT_SOURCE = """
            package athena.iec

            element iec_switch_module {
              identity "iec.switch_module"
              version "1.0.0"
              bounds (0, 0, 180, 100)

              child primary {
                symbol "iec.switch_contact"
                translate (10, 10)
                rotate 0
                scale (1, 1)
                zOrder 0
              }

              child secondary {
                symbol "iec.indicator"
                translate (90, 10)
                rotate 0
                scale (1, 1)
                zOrder 1
              }

              export anchor primaryLine from primary.line
              export anchor primaryLoad from primary.load
              export anchor secondaryInput from secondary.input
              export anchor secondaryOutput from secondary.output
            }
        """.trimIndent()

        private val DUPLICATE_SYMBOL_SOURCE = """
            package athena.iec

            symbol duplicate_switch_contact {
              identity "iec.switch_contact"
              version "1.0.0"
              graphic {
                bounds (0, 0, 80, 80)
                line line from (40, 0) to (40, 80) style conductor
              }
              anchor otherLine { ref "line" point (40, 0) role terminal direction in signal Power.family }
              anchor otherLoad { ref "line" point (40, 80) role terminal direction out signal Power.family }
            }
        """.trimIndent()
    }
}

private fun primitiveIds(primitive: GraphicPrimitive): List<String> = when (primitive) {
    is GraphicPrimitive.Group -> listOf(primitive.primitiveId.value) + primitive.children.flatMap(::primitiveIds)
    is GraphicPrimitive.Transformed -> listOf(primitive.primitiveId.value) + primitiveIds(primitive.child)
    else -> listOf(primitive.primitiveId.value)
}

private fun String.spanOf(token: String, occurrence: Int): SourceSpan {
    var start = -1
    var cursor = 0
    repeat(occurrence) {
        start = indexOf(token, cursor)
        require(start >= 0) { "Token `$token` occurrence $occurrence is missing." }
        cursor = start + token.length
    }
    fun position(offset: Int): SourcePosition {
        val prefix = substring(0, offset)
        return SourcePosition(
            offset = offset,
            line = prefix.count { it == '\n' } + 1,
            column = offset - prefix.lastIndexOf('\n'),
        )
    }
    return SourceSpan(position(start), position(start + token.length))
}
