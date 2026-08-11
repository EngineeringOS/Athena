package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PackageRepresentationReuseSyntaxTest {
    @Test
    fun `native macro variant and placeholder declarations preserve authored package intent`() {
        val source =
            """
            package com.vendor.controls
            macro starter {
              identity "starter"
              version "1.0.0"
              child switching {
                element "contactor_element"
                translate (4, 8)
                function switching
                role primary
              }
            }
            variant compact {
              identity "compact"
              version "1.0.0"
              element "contactor_element"
              resource geometry { kind svg path "resources/compact.svg" }
            }
            placeholder label {
              identity "label"
              version "1.0.0"
              target "label.text"
              type text
            }
            """.trimIndent()

        val parsed = assertIs<ParseSuccess>(AthenaLanguageParser().parse("reuse.athena", source))
        val declarations = assertIs<RepresentationSourceUnit>(parsed.ast.unit).declarations

        val macro = assertIs<MacroDeclaration>(declarations[0])
        assertEquals("starter", macro.identity?.value)
        assertEquals("contactor_element", macro.children.single().elementIdentity?.value)
        assertEquals("switching", macro.children.single().functionSlot?.value)
        assertEquals("primary", macro.children.single().bindingRole?.value)

        val variant = assertIs<VariantDeclaration>(declarations[1])
        assertEquals("contactor_element", variant.elementIdentity?.value)
        assertEquals("resources/compact.svg", variant.resources.single().path.value)

        val placeholder = assertIs<PlaceholderDeclaration>(declarations[2])
        assertEquals("label.text", placeholder.targetPath?.value)
        assertEquals("text", placeholder.valueType?.value)
    }
}
