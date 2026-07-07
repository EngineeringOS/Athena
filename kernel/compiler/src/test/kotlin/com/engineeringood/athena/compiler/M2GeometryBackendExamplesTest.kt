package com.engineeringood.athena.compiler

import com.engineeringood.athena.renderer.svg.SvgRenderer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class M2GeometryBackendExamplesTest {
    private val compiler = AthenaCompiler()
    private val renderer = SvgRenderer()
    private val expectedInventory = listOf("demo-cabinet")

    @Test
    fun `m2 proof corpus keeps the expected inventory`() {
        val sourceNames = loadExamples().map(M2ProofExample::name)
        val expectationNames = loadExampleNames(".expectation.txt")

        assertEquals(expectedInventory, sourceNames)
        assertEquals(expectedInventory, expectationNames)
    }

    @Test
    fun `m2 proof corpus emits deterministic cabinet and wiring svg directly from geometry ir`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m2/${example.name}.athena")
            val result = assertIs<CompilerCompilationSuccess>(compiler.compile(sourcePath))

            val geometriesByView = result.geometries.associateBy { geometry -> geometry.viewId }
            val cabinetGeometry = requireNotNull(geometriesByView["cabinet"]) {
                "Expected cabinet geometry for ${example.name}"
            }
            val wiringGeometry = requireNotNull(geometriesByView["wiring"]) {
                "Expected wiring geometry for ${example.name}"
            }

            val expectedCabinetSvg = Files.readString(repoRoot.resolve("examples/m2/${example.cabinetSvgArtifact}")).trimEnd()
            val expectedWiringSvg = Files.readString(repoRoot.resolve("examples/m2/${example.wiringSvgArtifact}")).trimEnd()

            assertEquals(expectedCabinetSvg, assertIs<CompilerRenderingSuccess>(result.rendering).svg)
            assertEquals(expectedCabinetSvg, renderer.render(result.document.system.name, cabinetGeometry))
            assertEquals(expectedWiringSvg, renderer.render(result.document.system.name, wiringGeometry))
        }
    }

    private fun loadExamples(): List<M2ProofExample> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m2")
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
        val exampleDir = repoRoot.resolve("examples/m2")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { it.fileName.toString().endsWith(suffix) }
                    .sorted(compareBy<Path> { it.fileName.toString() })
                    .map { it.fileName.toString().removeSuffix(suffix) }
                    .toList()
            }
    }

    private fun parseExample(path: Path): M2ProofExample {
        val values = Files.readAllLines(path)
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .associate { line ->
                val separator = line.indexOf('=')
                require(separator > 0) { "Expectation line must be key=value in ${path.fileName}: $line" }
                line.substring(0, separator) to line.substring(separator + 1)
            }

        return M2ProofExample(
            name = path.name.removeSuffix(".expectation.txt"),
            cabinetSvgArtifact = values.getValue("cabinet_svg"),
            wiringSvgArtifact = values.getValue("wiring_svg"),
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

    private data class M2ProofExample(
        val name: String,
        val cabinetSvgArtifact: String,
        val wiringSvgArtifact: String,
    )
}
