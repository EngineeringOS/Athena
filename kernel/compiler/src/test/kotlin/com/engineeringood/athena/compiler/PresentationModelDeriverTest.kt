package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PresentationModelDeriverTest {
    @Test
    fun `presentation derivation does not reconstruct geometry from projection reality`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val cabinet = success.presentations.first { presentation -> presentation.view.id == "cabinet" }
        val schematic = success.presentations.first { presentation -> presentation.view.id == "schematic" }

        assertEquals(emptyList(), cabinet.occurrences)
        assertEquals(emptyList(), cabinet.connectors)
        assertEquals(emptyList(), schematic.occurrences)
        assertEquals(emptyList(), schematic.connectors)
        assertTrue(cabinet.primitivePacks.isNotEmpty())
        assertTrue(cabinet.compositePacks.isNotEmpty())
        assertTrue(schematic.primitivePacks.isNotEmpty())
        assertTrue(schematic.compositePacks.isNotEmpty())
    }

    @Test
    fun `presentation derivation keeps projection resolved subject metadata without route facts`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val cabinet = success.presentations.first { presentation -> presentation.view.id == "cabinet" }

        assertEquals(emptyList(), cabinet.connectors)
        assertTrue(cabinet.resolvedSubjects.all { subject -> subject.semanticId.value.startsWith("component:") })
    }

    @Test
    fun `presentation derivation does not publish legacy M25 fallback representation facts`() {
        val sourcePath = resolveRepoRoot()
            .resolve("examples/m25/sample-project/src/com/engineeringood/m25/sample/01-professional-symbol-sheet.athena")

        val success = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val schematic = success.presentations.first { presentation -> presentation.view.id == "schematic" }
        val facts = schematic.representationFacts

        assertTrue(facts.none { fact -> fact.definition.libraryId.value == "athena-industrial-control" })
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
