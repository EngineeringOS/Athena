package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Story 5.1 proof: exercises the checked-in `examples/m17` parser-parity corpus (AD-113) end to end
 * through the live compiler parser path (ANTLR4-backed since Epic 2), reusing the exact identity
 * scheme and continuity contract `AthenaParserContinuityTest` (Story 4.3) pins for `examples/m0`, so
 * both stories reinforce one shared parity definition rather than two divergent ones.
 *
 * This test is the milestone's primary parser-parity evidence per AD-113: it supersedes any
 * inline-only parser demo. See `examples/m17/README.md` for the corpus layout and scope.
 */
class AthenaM17ParserParityProofTest {
    private val compiler = AthenaCompiler()
    private val expectedInventory = listOf(
        "dense-qualified-names",
        "parity-cabinet",
    )

    @Test
    fun `m17 parser-parity corpus keeps the expected inventory`() {
        val sourceNames = loadExampleNames(".athena")
        val expectationNames = loadExampleNames(".expectation.txt")

        assertEquals(expectedInventory, sourceNames)
        assertEquals(expectedInventory, expectationNames)
    }

    @Test
    fun `m17 parser-parity fixtures compile through the live compiler parser path with expected shape`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m17/parser-parity-proof/${example.name}.athena")
            val result = compiler.compile(sourcePath)
            val success = assertIs<CompilerCompilationSuccess>(result, "Expected parse-success compile result for ${example.name}")
            val document = success.document

            assertEquals(example.componentCount, document.components.size, "component count mismatch for ${example.name}")
            assertEquals(example.portCount, document.ports.size, "port count mismatch for ${example.name}")
            assertEquals(example.connectionCount, document.connections.size, "connection count mismatch for ${example.name}")

            assertTrue(success.semanticResult.isSemanticallyValid, "expected semantic validity for ${example.name}")
            assertEquals(example.diagnosticCount, success.semanticResult.diagnostics.size, "diagnostic count mismatch for ${example.name}")
            assertIs<CompilerRenderingSuccess>(success.rendering, "expected emitted svg for ${example.name}")

            // Story 4.3's shared identity scheme: system:/component:/port:/connection: prefixes.
            assertTrue(document.system.id.value.startsWith("system:"), "system identity scheme for ${example.name}")
            document.components.forEach { component ->
                assertEquals("component:${component.name}", component.id.value, "component identity scheme for ${example.name}")
            }
            document.ports.forEach { port ->
                val owner = port.ownerReference.authoredPath.joinToString(".")
                assertEquals("port:$owner.${port.name}", port.id.value, "port identity scheme for ${example.name}")
            }
            document.connections.forEach { connection ->
                val from = connection.from.authoredPath.joinToString(".")
                val to = connection.to.authoredPath.joinToString(".")
                assertEquals("connection:$from->$to", connection.id.value, "connection identity scheme for ${example.name}")
            }
        }
    }

    @Test
    fun `m17 parser-parity fixtures lower deterministically across repeated compilations`() {
        val repoRoot = resolveRepoRoot()
        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m17/parser-parity-proof/${example.name}.athena")
            val first = assertIs<CompilerCompilationSuccess>(compiler.compile(sourcePath)).document
            val second = assertIs<CompilerCompilationSuccess>(compiler.compile(sourcePath)).document
            assertEquals(first, second, "lowering must be deterministic for ${example.name}")
        }
    }

    @Test
    fun `m17 repository-parity fixture resolves cleanly through the governed repository graph seam`() {
        val repositoryRoot = resolveRepoRoot().resolve("examples/m17/repository-parity-proof")
        val sourcePath = repositoryRoot.resolve("src/parity-repo.athena")
        assertTrue(Files.exists(sourcePath), "Expected M17 repository-parity proof source at `$sourcePath`.")

        val contractValidation = compiler.validateRepositoryContract(repositoryRoot)
        assertTrue(contractValidation.isValid, contractValidation.diagnostics.joinToString("\n") { it.message })

        val lockValidation = compiler.validateRepositoryLock(repositoryRoot)
        assertTrue(lockValidation.isValid, lockValidation.diagnostics.joinToString("\n") { it.message })

        val graphResolution = compiler.resolveRepositoryGraph(repositoryRoot)
        assertTrue(graphResolution.isValid, graphResolution.diagnostics.joinToString("\n") { it.message })
        assertEquals("com.engineeringood.m17.parity", graphResolution.repository?.manifest?.primaryPackage?.id?.name)

        val secondResolution = compiler.resolveRepositoryGraph(repositoryRoot)
        assertEquals(graphResolution.report, secondResolution.report, "repository graph resolution must stay deterministic")

        val compileResult = compiler.compile(sourcePath)
        val success = assertIs<CompilerCompilationSuccess>(compileResult, "Expected the repository-parity source to compile")
        assertEquals(2, success.document.components.size)
        assertEquals(2, success.document.ports.size)
        assertEquals(1, success.document.connections.size)
    }

    private fun loadExamples(): List<ConformanceExample> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m17/parser-parity-proof")
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
        val exampleDir = repoRoot.resolve("examples/m17/parser-parity-proof")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { it.fileName.toString().endsWith(suffix) }
                    .sorted(compareBy<Path> { it.fileName.toString() })
                    .map { it.fileName.toString().removeSuffix(suffix) }
                    .toList()
            }
    }

    private fun parseExample(path: Path): ConformanceExample {
        val values = Files.readAllLines(path)
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .associate { line ->
                val separator = line.indexOf('=')
                require(separator > 0) { "Expectation line must be key=value in ${path.fileName}: $line" }
                line.substring(0, separator) to line.substring(separator + 1)
            }

        require(values.getValue("status") == "valid") {
            "Expected `status=valid` for parser-parity fixture ${path.fileName}"
        }
        require(values.getValue("svg") == "emitted") {
            "Expected `svg=emitted` for parser-parity fixture ${path.fileName}"
        }

        return ConformanceExample(
            name = path.name.removeSuffix(".expectation.txt"),
            componentCount = values.getValue("components").toInt(),
            portCount = values.getValue("ports").toInt(),
            connectionCount = values.getValue("connections").toInt(),
            diagnosticCount = values.getValue("diagnostics").toInt(),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        require(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }

    private data class ConformanceExample(
        val name: String,
        val componentCount: Int,
        val portCount: Int,
        val connectionCount: Int,
        val diagnosticCount: Int,
    )
}
