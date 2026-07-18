package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.layout.AuthoredLayoutAxis.HORIZONTAL
import com.engineeringood.athena.layout.AuthoredLayoutAxis.VERTICAL
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority.PREFERENCE
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.ALIGNED_WITH
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.BELOW
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.GROUPED_WITH
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation.NEAR
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthoredLayoutIntentMapperTest {
    @Test
    fun `maps layout declaration into source-owned layout intent without semantic binding`() {
        val source = java.nio.file.Files.readString(
            repoRoot().resolve("examples/m23/parser-parity-proof/valid-layout-block.athena"),
        )
        val success = assertIs<ParseSuccess>(AthenaLanguageParser().parse("valid-layout-block.athena", source))
        val declaration = assertIs<LayoutDeclaration>(success.ast.declarations[1])

        val intent = AuthoredLayoutIntentMapper().map(declaration)

        assertEquals("schematic-sheet", intent.viewFamily)
        assertEquals(
            listOf(NEAR, BELOW, ALIGNED_WITH, ALIGNED_WITH, GROUPED_WITH),
            intent.statements.map { statement -> statement.relation },
        )
        assertEquals(listOf("HMI1", "XT1", "HMI1", "HMI2", "HMI1"), intent.statements.map { statement -> statement.subject })
        assertEquals(listOf("PLC1", "PLC1", "PLC1", "PLC1", "PLC1"), intent.statements.map { statement -> statement.target })
        assertEquals(listOf(null, null, VERTICAL, HORIZONTAL, null), intent.statements.map { statement -> statement.axis })
        assertEquals(List(5) { PREFERENCE }, intent.statements.map { statement -> statement.priority })
        assertEquals(declaration.span.start.line, intent.sourceSpan.startLine)
        assertEquals(declaration.statements[2].span.start.line, intent.statements[2].sourceSpan.startLine)
    }

    private fun repoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !java.nio.file.Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        return current
    }
}
