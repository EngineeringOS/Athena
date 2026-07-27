package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM34SymbolSyntaxTest {
    @Test
    fun `parses standalone typed symbol source without a fake system`() {
        val result = AthenaLanguageParser().parse("iec-switch.athena", VALID_SYMBOL)

        val ast = assertIs<ParseSuccess>(result).ast
        assertIs<RepresentationSourceUnit>(ast.unit)
        assertEquals(null, ast.systemOrNull)
        assertTrue(ast.projectDeclarations.isEmpty())
        val symbol = assertIs<SymbolDeclaration>(ast.representationDeclarations.single())
        assertEquals("iec_switch_contact", symbol.name)
        assertEquals("iec.switch_contact", symbol.identity?.value)
        assertEquals("1.0.0", symbol.version?.value)
        assertEquals(listOf("line", "load"), symbol.graphic?.primitives?.map { it.id })
        assertEquals(listOf("line", "load"), symbol.anchors.map { it.id })
        assertEquals(3, symbol.span.start.line)
        assertEquals(28, symbol.span.end.line)
    }

    @Test
    fun `existing system source remains a project source unit`() {
        val result = AthenaLanguageParser().parse(
            "project.athena",
            """
                system Demo {
                  device MotorM1 {
                    type Motor
                  }
                }
            """.trimIndent(),
        )

        val ast = assertIs<ParseSuccess>(result).ast
        assertIs<ProjectSourceUnit>(ast.unit)
        assertEquals("Demo", ast.system.name)
        assertEquals(1, ast.declarations.size)
        assertTrue(ast.representationDeclarations.isEmpty())
    }

    @Test
    fun `accepts bidirectional anchor direction as a typed symbol predicate`() {
        val result = AthenaLanguageParser().parse(
            "bidirectional.athena",
            VALID_SYMBOL.replace("accepts direction in", "accepts direction bidirectional"),
        )

        val ast = assertIs<ParseSuccess>(result).ast
        val symbol = assertIs<SymbolDeclaration>(ast.representationDeclarations.single())
        assertEquals("bidirectional", symbol.anchors.first().acceptedDirections.single().value)
    }

    @Test
    fun `parses governed svg graphic reference without svg-owned identity`() {
        val result = AthenaLanguageParser().parse("svg-symbol.athena", VALID_SVG_SYMBOL)

        val ast = assertIs<ParseSuccess>(result).ast
        val symbol = assertIs<SymbolDeclaration>(ast.representationDeclarations.single())
        assertEquals("vendor_drive", symbol.name)
        assertEquals("vendor.drive", symbol.identity?.value)
        assertEquals("vendor_drive_svg", symbol.graphic?.svgResource?.value)
        assertEquals("./vendor-drive.svg", symbol.resources.single().path.value)
        assertTrue(symbol.graphic?.primitives.orEmpty().isEmpty())
    }

    companion object {
        val VALID_SYMBOL = """
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
                primitiveRef line
                point (40, 0)
                role terminal
                accepts direction in
                accepts signal Power
              }

              anchor load {
                primitiveRef load
                point (40, 80)
                role terminal
                accepts direction out
                accepts signal Power
              }
            }
        """.trimIndent()

        val VALID_SVG_SYMBOL = """
            package athena.vendor

            symbol vendor_drive {
              identity "vendor.drive"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()
    }
}
