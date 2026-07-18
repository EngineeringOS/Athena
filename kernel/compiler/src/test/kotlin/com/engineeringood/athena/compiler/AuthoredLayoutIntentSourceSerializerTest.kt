package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.layout.AuthoredLayoutAxis.VERTICAL
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.ALIGNED_WITH
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.BELOW
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.GROUPED_WITH
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.NEAR
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement
import com.engineeringood.athena.layout.LayoutSourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthoredLayoutIntentSourceSerializerTest {
    @Test
    fun `serializes all admitted layout intent statements as stable parseable source`() {
        val intent = AuthoredLayoutIntent(
            viewFamily = "schematic-sheet",
            statements = listOf(
                statement("HMI1", NEAR, "PLC1"),
                statement("XT1", BELOW, "PLC1"),
                statement("HMI1", ALIGNED_WITH, "PLC1", VERTICAL),
                statement("HMI1", GROUPED_WITH, "PLC1"),
            ),
            sourceSpan = span(),
        )

        val serializer = AuthoredLayoutIntentSourceSerializer()
        val rendered = serializer.serialize(intent)

        assertEquals(
            """
            layout schematic-sheet {
              place HMI1 near PLC1
              place XT1 below PLC1
              align HMI1 aligned-with PLC1 axis vertical
              group HMI1 grouped-with PLC1
            }
            """.trimIndent(),
            rendered,
        )
        assertEquals(rendered, serializer.serialize(intent))

        val parsed = assertIs<ParseSuccess>(
            AthenaLanguageParser().parse(
                "serialized-layout.athena",
                """
                system Demo {
                ${rendered.prependIndent("  ")}
                }
                """.trimIndent(),
            ),
        )
        assertIs<LayoutDeclaration>(parsed.ast.declarations.single())
    }

    private fun statement(
        subject: String,
        relation: com.engineeringood.athena.layout.AuthoredLayoutIntentRelation,
        target: String,
        axis: com.engineeringood.athena.layout.AuthoredLayoutAxis? = null,
    ): AuthoredLayoutIntentStatement = AuthoredLayoutIntentStatement(
        subject = subject,
        relation = relation,
        target = target,
        axis = axis,
        sourceSpan = span(),
    )

    private fun span(): LayoutSourceSpan = LayoutSourceSpan(
        sourceUnitId = "src/01-layout-hints.athena",
        startLine = 1,
        startColumn = 1,
        endLine = 1,
        endColumn = 2,
    )
}
