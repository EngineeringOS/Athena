package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM18SyntaxScopeTest {
    private val parser = AthenaLanguageParser()

    @Test
    fun `accepts only the supported package import and existing declaration slice`() {
        val source =
            """
            package com.engineeringood.m18-root
            import com.engineeringood.controls
            import com.engineeringood.controls.Switch2
            system import {
              device package {
                import import
              }
              port package.out {
                direction out
              }
              connect package.out -> package.out
            }
            """.trimIndent()

        val result = parser.parse("supported-slice.athena", source)
        val success = assertIs<ParseSuccess>(result, "Unexpected parse result: $result")

        assertEquals(listOf("com", "engineeringood", "m18-root"), success.ast.packageDeclaration?.name?.parts)
        assertEquals(2, success.ast.imports.size)
        assertEquals(listOf("com", "engineeringood", "controls", "Switch2"), success.ast.imports[1].target.parts)
        assertEquals(listOf("package", "package", "package"), success.ast.declarations.map {
            when (it) {
                is DeviceDeclaration -> it.name
                is PortDeclaration -> it.qualifiedName.parts.first()
                is ConnectionDeclaration -> it.from.parts.first()
                is LayoutDeclaration -> error("Layout declarations are outside this M18 compatibility fixture")
            }
        })
    }

    @Test
    fun `rejects broad file header syntax deterministically`() {
        val cases = mapOf(
            "alias" to "import com.controls as controls\nsystem Demo {}",
            "wildcard" to "import com.controls.*\nsystem Demo {}",
            "export-import" to "export import com.controls\nsystem Demo {}",
            "re-export" to "export com.controls\nsystem Demo {}",
            "public-import" to "public import com.controls\nsystem Demo {}",
            "private-package" to "private package com.root\nsystem Demo {}",
            "protected-import" to "protected import com.controls\nsystem Demo {}",
            "internal-import" to "internal import com.controls\nsystem Demo {}",
            "module" to "module com.root\nsystem Demo {}",
            "namespace" to "namespace com.root\nsystem Demo {}",
            "include" to "include com.root\nsystem Demo {}",
            "using" to "using com.root\nsystem Demo {}",
        )

        cases.forEach { (name, source) -> assertDeterministicFailure(name, source) }
    }

    @Test
    fun `rejects unrelated and visibility prefixed system declarations deterministically`() {
        val declarations = mapOf(
            "function" to "function Run {}",
            "type" to "type Alias {}",
            "enum" to "enum Mode {}",
            "service" to "service Control {}",
            "namespace" to "namespace controls {}",
            "export-device" to "export device PLC1 {}",
            "public-device" to "public device PLC1 {}",
            "private-port" to "private port PLC1.out {}",
            "internal-connect" to "internal connect PLC1.out -> PLC1.out",
        )

        declarations.forEach { (name, declaration) ->
            assertDeterministicFailure(
                name = "declaration-$name",
                source = "system Demo {\n  $declaration\n}",
            )
        }
    }

    @Test
    fun `preserves earliest diagnostics when recovery also finds a split import target`() {
        val source = "package com..broken\nimport\ncontrols\nsystem Demo {}"

        val failure = assertIs<ParseFailure>(parser.parse("earliest-error.athena", source))

        assertEquals(source.indexOf("..") + 1, failure.diagnostics.single().span.start.offset)
    }

    @Test
    fun `returns a typed failure for a bare import at end of file`() {
        val result = parser.parse("bare-import.athena", "import")

        val failure = assertIs<ParseFailure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("bare-import.athena", failure.diagnostics.single().file)
        assertTrue(failure.diagnostics.single().span.start.offset >= 0)
    }

    private fun assertDeterministicFailure(name: String, source: String) {
        val file = "$name.athena"
        val first = parser.parse(file, source)
        val second = parser.parse(file, source)

        val failure = assertIs<ParseFailure>(first, "Expected $name to remain outside the M18 syntax slice")
        assertEquals(first, second, "Expected deterministic failure for $name")
        assertEquals(1, failure.diagnostics.size, "diagnostic count for $name")
        val diagnostic = failure.diagnostics.single()
        assertEquals(file, diagnostic.file)
        assertTrue(diagnostic.line > 0, "diagnostic line for $name")
        assertTrue(diagnostic.column > 0, "diagnostic column for $name")
        assertTrue(diagnostic.message.isNotBlank(), "diagnostic message for $name")
        assertTrue(diagnostic.span.start.offset >= 0, "diagnostic start offset for $name")
        assertTrue(diagnostic.span.end.offset >= diagnostic.span.start.offset, "diagnostic span order for $name")
    }
}
