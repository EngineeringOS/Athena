package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Story 5.2 proof: exercises the checked-in `examples/m17/invalid-and-incomplete-proof` malformed and
 * incomplete syntax-level fixtures through the live compiler parser path (ANTLR4-backed since Epic 2).
 *
 * Each fixture isolates exactly one syntax failure mode (AD-109): an unterminated string literal, a
 * missing closing brace, a missing `->` in a `connect` declaration, and an over-qualified port
 * reference. Every fixture must surface as a typed [SyntaxDiagnostic] via [ParseFailure] with real file,
 * line, and message provenance -- never an uncaught exception or a positionless error.
 *
 * This test verifies compiler-diagnostic quality only. Tree-sitter-backed syntax-UX tolerance for the
 * same fixtures is a separate, independently-run check (see `examples/m17/README.md`); this test must
 * never be read as a proxy for that check, or vice versa.
 */
class AthenaM17InvalidSourceProofTest {
    private val expectedInventory = listOf(
        "incomplete-brace",
        "missing-arrow",
        "over-qualified-port",
        "unterminated-string",
    )

    @Test
    fun `m17 invalid-and-incomplete corpus keeps the expected inventory`() {
        val sourceNames = loadExampleNames(".athena")
        val expectationNames = loadExampleNames(".expectation.txt")

        assertEquals(expectedInventory, sourceNames)
        assertEquals(expectedInventory, expectationNames)
    }

    @Test
    fun `every m17 invalid-and-incomplete fixture fails as a typed positioned syntax diagnostic`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m17/invalid-and-incomplete-proof/${example.name}.athena")
            val source = Files.readString(sourcePath)

            val result = AthenaLanguageParser().parse(sourcePath.toString(), source)

            val failure = assertIs<ParseFailure>(result, "Expected `${example.name}` to fail to parse")
            assertTrue(failure.diagnostics.isNotEmpty(), "Expected at least one diagnostic for ${example.name}")

            val diagnostic = failure.diagnostics.first()
            assertEquals(sourcePath.toString(), diagnostic.file, "diagnostic file for ${example.name}")
            assertEquals(example.syntaxErrorLine, diagnostic.line, "diagnostic line for ${example.name}")
            assertEquals(example.syntaxErrorColumn, diagnostic.column, "diagnostic column for ${example.name}")
            assertTrue(
                diagnostic.message.contains(example.syntaxErrorMessageContains),
                "Expected `${example.name}` diagnostic message [`${diagnostic.message}`] to contain `${example.syntaxErrorMessageContains}`",
            )
        }
    }

    @Test
    fun `every m17 invalid-and-incomplete fixture fails deterministically on repeated parses`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m17/invalid-and-incomplete-proof/${example.name}.athena")
            val source = Files.readString(sourcePath)
            val parser = AthenaLanguageParser()

            val first = parser.parse(sourcePath.toString(), source)
            val second = parser.parse(sourcePath.toString(), source)

            assertIs<ParseFailure>(first, "Expected `${example.name}` to fail to parse")
            assertEquals(first, second, "parsing must fail deterministically for ${example.name}")
        }
    }

    private fun loadExamples(): List<InvalidFixture> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m17/invalid-and-incomplete-proof")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { it.fileName.toString().endsWith(".expectation.txt") }
                    .sorted(compareBy<Path> { it.fileName.toString() })
                    .map { parseExample(it) }
                    .toList()
            }
    }

    private fun loadExampleNames(suffix: String): List<String> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m17/invalid-and-incomplete-proof")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { it.fileName.toString().endsWith(suffix) }
                    .sorted(compareBy<Path> { it.fileName.toString() })
                    .map { it.fileName.toString().removeSuffix(suffix) }
                    .toList()
            }
    }

    private fun parseExample(path: Path): InvalidFixture {
        val values = Files.readAllLines(path)
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .associate { line ->
                val separator = line.indexOf('=')
                require(separator > 0) { "Expectation line must be key=value in ${path.fileName}: $line" }
                line.substring(0, separator) to line.substring(separator + 1)
            }

        require(values.getValue("status") == "syntax-failure") {
            "Expected `status=syntax-failure` for invalid/incomplete fixture ${path.fileName}"
        }

        return InvalidFixture(
            name = path.name.removeSuffix(".expectation.txt"),
            syntaxErrorLine = values.getValue("syntaxErrorLine").toInt(),
            syntaxErrorColumn = values.getValue("syntaxErrorColumn").toInt(),
            syntaxErrorMessageContains = values.getValue("syntaxErrorMessageContains"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }

    private data class InvalidFixture(
        val name: String,
        val syntaxErrorLine: Int,
        val syntaxErrorColumn: Int,
        val syntaxErrorMessageContains: String,
    )
}
