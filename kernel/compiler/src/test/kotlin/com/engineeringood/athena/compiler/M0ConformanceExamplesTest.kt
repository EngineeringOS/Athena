package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class M0ConformanceExamplesTest {
    private val compiler = AthenaCompiler()
    private val expectedInventory = listOf(
        "demo-cabinet",
        "dual-drive-cabinet",
        "duplicate-identity-cabinet",
        "invalid-direction-cabinet",
        "invalid-semantic-cabinet",
        "quoted-properties-cabinet",
    )

    @Test
    fun `m0 conformance suite keeps the expected inventory`() {
        val sourceNames = loadExampleNames(".athena")
        val expectationNames = loadExampleNames(".expectation.txt")
        val examples = loadExamples()

        assertEquals(expectedInventory, sourceNames)
        assertEquals(expectedInventory, expectationNames)
        assertEquals(expectedInventory, examples.map { it.name })
        assertTrue(examples.size in 5..10)
    }

    @Test
    fun `m0 conformance examples match expected compiler outcomes`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val sourcePath = repoRoot.resolve("examples/m0/${example.name}.athena")
            val result = compiler.compile(sourcePath)
            val success = assertIs<CompilerCompilationSuccess>(result, "Expected parse-success compile result for ${example.name}")

            assertEquals(example.componentCount, success.document.components.size, "component count mismatch for ${example.name}")
            assertEquals(example.portCount, success.document.ports.size, "port count mismatch for ${example.name}")
            assertEquals(example.connectionCount, success.document.connections.size, "connection count mismatch for ${example.name}")

            when (example.status) {
                ExampleStatus.VALID -> {
                    assertTrue(success.semanticResult.isSemanticallyValid, "expected semantic validity for ${example.name}")
                    assertEquals(emptyList(), success.semanticResult.diagnostics.map { it.ruleId.value })
                    assertIs<CompilerRenderingSuccess>(success.rendering)
                }

                ExampleStatus.SEMANTIC_INVALID -> {
                    assertTrue(!success.semanticResult.isSemanticallyValid, "expected semantic invalidity for ${example.name}")
                    assertEquals(example.diagnosticRuleIds, success.semanticResult.diagnostics.map { it.ruleId.value })
                    assertIs<CompilerRenderingBlocked>(success.rendering)
                }
            }

            when (example.svgExpectation) {
                SvgExpectation.EMITTED -> assertIs<CompilerRenderingSuccess>(success.rendering)
                SvgExpectation.BLOCKED -> assertIs<CompilerRenderingBlocked>(success.rendering)
            }

            example.publishedIrArtifact?.let { artifactName ->
                val expectedIr = Files.readString(repoRoot.resolve("examples/m0/$artifactName")).trimEnd()
                assertEquals(expectedIr, renderConformanceArtifact(success.document, repoRoot), "IR artifact mismatch for ${example.name}")
            }
            example.publishedSvgArtifact?.let { artifactName ->
                val expectedSvg = Files.readString(repoRoot.resolve("examples/m0/$artifactName")).trimEnd()
                assertEquals(
                    expectedSvg,
                    assertIs<CompilerRenderingSuccess>(success.rendering).svg,
                    "SVG artifact mismatch for ${example.name}",
                )
            }
        }
    }

    private fun loadExamples(): List<ConformanceExample> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m0")
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
        val exampleDir = repoRoot.resolve("examples/m0")
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

        return ConformanceExample(
            name = path.name.removeSuffix(".expectation.txt"),
            status = ExampleStatus.from(values.getValue("status")),
            componentCount = values.getValue("components").toInt(),
            portCount = values.getValue("ports").toInt(),
            connectionCount = values.getValue("connections").toInt(),
            svgExpectation = SvgExpectation.from(values.getValue("svg")),
            diagnosticRuleIds = values["diagnostics"]
                ?.takeIf(String::isNotEmpty)
                ?.split(',')
                ?.map(String::trim)
                ?: emptyList(),
            publishedIrArtifact = values["published_ir"]?.takeIf(String::isNotEmpty),
            publishedSvgArtifact = values["published_svg"]?.takeIf(String::isNotEmpty),
        )
    }

    private fun renderConformanceArtifact(document: EngineeringIrDocument, repoRoot: Path): String {
        fun renderPath(file: String): String {
            val path = Path.of(file)
            return runCatching { repoRoot.relativize(path).toString().replace('\\', '/') }
                .getOrElse { path.toString().replace('\\', '/') }
        }

        fun renderProvenance(provenance: SourceProvenance): String {
            return "${renderPath(provenance.file)}:${provenance.startLine}:${provenance.startColumn}-${provenance.endLine}:${provenance.endColumn}"
        }

        fun renderValue(value: EngineeringPropertyValue): String {
            return when (value) {
                is EngineeringPropertyValue.Symbol -> "symbol:${value.text}"
                is EngineeringPropertyValue.Text -> "text:${value.text}"
            }
        }

        return buildString {
            appendLine("system|id=${document.system.id}|name=${document.system.name}|provenance=${renderProvenance(document.system.provenance)}")
            document.components.forEach { component ->
                appendLine("component|id=${component.id}|name=${component.name}|kind=${component.kind}|provenance=${renderProvenance(component.provenance)}")
                component.properties.forEach { property ->
                    appendLine("property|name=${property.name}|value=${renderValue(property.value)}")
                }
            }
            document.ports.forEach { port ->
                appendLine(
                    "port|id=${port.id}|owner=${port.ownerReference.authoredPath.joinToString(".")}|ownerResolved=${port.ownerReference.resolvedIdentity}|ownerProvenance=${renderProvenance(port.ownerReference.provenance)}|name=${port.name}|provenance=${renderProvenance(port.provenance)}",
                )
                port.properties.forEach { property ->
                    appendLine("property|name=${property.name}|value=${renderValue(property.value)}")
                }
            }
            document.connections.forEach { connection ->
                appendLine(
                    "connection|id=${connection.id}|from=${connection.from.authoredPath.joinToString(".")}|fromResolved=${connection.from.resolvedIdentity}|fromProvenance=${renderProvenance(connection.from.provenance)}|to=${connection.to.authoredPath.joinToString(".")}|toResolved=${connection.to.resolvedIdentity}|toProvenance=${renderProvenance(connection.to.provenance)}|provenance=${renderProvenance(connection.provenance)}",
                )
            }
        }.trimEnd()
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
        val status: ExampleStatus,
        val componentCount: Int,
        val portCount: Int,
        val connectionCount: Int,
        val svgExpectation: SvgExpectation,
        val diagnosticRuleIds: List<String>,
        val publishedIrArtifact: String?,
        val publishedSvgArtifact: String?,
    )

    private enum class ExampleStatus {
        VALID,
        SEMANTIC_INVALID,
        ;

        companion object {
            fun from(value: String): ExampleStatus = when (value) {
                "valid" -> VALID
                "semantic-invalid" -> SEMANTIC_INVALID
                else -> error("Unsupported example status: $value")
            }
        }
    }

    private enum class SvgExpectation {
        EMITTED,
        BLOCKED,
        ;

        companion object {
            fun from(value: String): SvgExpectation = when (value) {
                "emitted" -> EMITTED
                "blocked" -> BLOCKED
                else -> error("Unsupported SVG expectation: $value")
            }
        }
    }
}
