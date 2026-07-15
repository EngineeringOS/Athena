package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM18SyntaxProofTest {
    private val validNames = listOf("valid-package-import", "valid-package-only")
    private val invalidNames = listOf("invalid-alias", "invalid-missing-target", "invalid-visibility", "invalid-wildcard")

    @Test
    fun `m18 syntax proof corpus keeps the expected inventory`() {
        assertEquals((validNames + invalidNames).sorted(), sourceNames())
        assertEquals(invalidNames.sorted(), expectationNames())
    }

    @Test
    fun `valid m18 syntax fixtures preserve package and import intent`() {
        val packageImport = parseSuccess("valid-package-import")
        assertEquals(listOf("com", "engineeringood", "m18-root"), packageImport.ast.packageDeclaration?.name?.parts)
        assertEquals(
            listOf(
                listOf("com", "engineeringood", "controls"),
                listOf("com", "engineeringood", "controls", "Switch2"),
            ),
            packageImport.ast.imports.map { it.target.parts },
        )

        val packageOnly = parseSuccess("valid-package-only")
        assertEquals(listOf("com", "engineeringood", "standalone"), packageOnly.ast.packageDeclaration?.name?.parts)
        assertTrue(packageOnly.ast.imports.isEmpty())
    }

    @Test
    fun `invalid m18 syntax fixtures fail with deterministic typed diagnostics`() {
        invalidNames.forEach { name ->
            val path = syntaxProofDir().resolve("$name.athena")
            val source = Files.readString(path)
            val expectation = readExpectation(name)
            val first = AthenaLanguageParser().parse(path.toString(), source)
            val second = AthenaLanguageParser().parse(path.toString(), source)

            val failure = assertIs<ParseFailure>(first, "Expected $name to fail")
            assertEquals(first, second, "Expected deterministic failure for $name")
            assertEquals(1, failure.diagnostics.size)
            val diagnostic = failure.diagnostics.single()
            assertEquals(path.toString(), diagnostic.file)
            assertEquals(expectation.line, diagnostic.line)
            assertEquals(expectation.column, diagnostic.column)
            assertTrue(diagnostic.message.contains(expectation.messageContains), diagnostic.message)
            assertTrue(diagnostic.span.start.offset >= 0)
            assertEquals(sourceOffset(source, expectation.line, expectation.column), diagnostic.span.start.offset)
            assertTrue(diagnostic.span.end.offset >= diagnostic.span.start.offset)
            assertTrue(diagnostic.span.end.offset <= source.length)
            assertEquals(diagnostic.line, diagnostic.span.start.line)
            assertEquals(diagnostic.column, diagnostic.span.start.column)
        }
    }

    private fun parseSuccess(name: String): ParseSuccess {
        val path = syntaxProofDir().resolve("$name.athena")
        return assertIs(AthenaLanguageParser().parse(path.toString(), Files.readString(path)))
    }

    private fun sourceNames(): List<String> = namesWithSuffix(".athena")

    private fun expectationNames(): List<String> = namesWithSuffix(".expectation.txt")

    private fun namesWithSuffix(suffix: String): List<String> = Files.list(syntaxProofDir()).use { paths ->
        paths.filter { it.fileName.toString().endsWith(suffix) }
            .sorted()
            .map { it.fileName.toString().removeSuffix(suffix) }
            .toList()
    }

    private fun readExpectation(name: String): InvalidExpectation {
        val entries = Files.readAllLines(syntaxProofDir().resolve("$name.expectation.txt"))
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { line ->
                require('=' in line) { "Expectation line must be key=value for $name: $line" }
                line.substringBefore('=') to line.substringAfter('=')
            }
        require(entries.map { it.first }.distinct().size == entries.size) { "Duplicate expectation key for $name" }
        val values = entries.toMap()
        require(
            values.keys == setOf("status", "syntaxErrorLine", "syntaxErrorColumn", "syntaxErrorMessageContains"),
        ) { "Unexpected expectation keys for $name: ${values.keys}" }
        require(values.getValue("status") == "syntax-failure") { "Expected syntax-failure status for $name" }
        require(values.getValue("syntaxErrorMessageContains").isNotBlank()) { "Expected nonblank message fragment for $name" }
        return InvalidExpectation(
            line = values.getValue("syntaxErrorLine").toInt(),
            column = values.getValue("syntaxErrorColumn").toInt(),
            messageContains = values.getValue("syntaxErrorMessageContains"),
        )
    }

    private fun syntaxProofDir(): Path = resolveRepoRoot().resolve("examples/m18/syntax-proof")

    private fun sourceOffset(source: String, line: Int, column: Int): Int {
        var currentLine = 1
        var lineStart = 0
        source.forEachIndexed { index, character ->
            if (currentLine == line) return lineStart + column - 1
            if (character == '\n') {
                currentLine += 1
                lineStart = index + 1
            }
        }
        require(currentLine == line) { "Line $line is outside source" }
        return lineStart + column - 1
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }

    private data class InvalidExpectation(val line: Int, val column: Int, val messageContains: String)
}
