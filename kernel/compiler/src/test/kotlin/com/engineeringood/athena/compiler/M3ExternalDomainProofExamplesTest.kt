package com.engineeringood.athena.compiler

import com.engineeringood.athena.domain.dummyruntime.DummyRuntimeDomainPlugin
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import kotlin.io.path.name
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class M3ExternalDomainProofExamplesTest {
    private val expectedInventory = listOf("dual-domain-proof", "dummy-proof", "electrical-proof")

    @Test
    fun `m3 proof corpus keeps the expected inventory`() {
        val sourceNames = loadExamples().map(M3ProofExample::name)
        val expectationNames = loadExampleNames(".expectation.txt")

        assertEquals(expectedInventory, sourceNames)
        assertEquals(expectedInventory, expectationNames)
    }

    @Test
    fun `m3 proof corpus exercises hosted external domains through explicit plugin sets`() {
        val repoRoot = resolveRepoRoot()

        loadExamples().forEach { example ->
            val compiler = compilerFor(example.pluginSet)
            val sourcePath = repoRoot.resolve("examples/m3/${example.name}.athena")
            val result = assertIs<CompilerCompilationSuccess>(compiler.compile(sourcePath))

            assertTrue(result.semanticResult.isSemanticallyValid, "Expected semantically valid proof example `${example.name}`.")
            assertEquals(example.approvedPlugins, compiler.pluginInventory.approvedPlugins.map { plugin ->
                plugin.candidate.manifest.pluginId
            })
            assertEquals(example.views, compiler.supportedViewDefinitions().map { definition -> definition.id })
            assertEquals(example.renderContributions, compiler.supportedRenderContributions().map { contribution ->
                contribution.contributionId
            })
            assertEquals(example.components, result.document.components.size)
            assertEquals(example.ports, result.document.ports.size)
            assertEquals(example.connections, result.document.connections.size)
            assertEquals(example.views.size, result.layouts.size)
            assertEquals(example.views.size, result.geometries.size)

            when (example.renderStatus) {
                M3RenderStatus.EMITTED -> {
                    val rendering = assertIs<CompilerRenderingSuccess>(result.rendering)
                    assertEquals(example.renderView, rendering.viewId)
                    assertEquals(example.activeRenderContributions, rendering.activeRenderContributions.map { contribution ->
                        contribution.contributionId
                    })
                }

                M3RenderStatus.BLOCKED -> {
                    val rendering = assertIs<CompilerRenderingBlocked>(result.rendering)
                    assertEquals(example.renderBlockedBy, rendering.blockedByPass)
                }
            }
        }
    }

    private fun compilerFor(pluginSet: M3PluginSet): AthenaCompiler {
        val plugins = when (pluginSet) {
            M3PluginSet.ELECTRICAL_ONLY -> listOf(ElectricalRuntimeDomainPlugin())
            M3PluginSet.DUMMY_ONLY -> listOf(DummyRuntimeDomainPlugin())
            M3PluginSet.BOTH -> listOf(DummyRuntimeDomainPlugin(), ElectricalRuntimeDomainPlugin())
        }
        return AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(plugins),
            ),
        )
    }

    private fun loadExamples(): List<M3ProofExample> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m3")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { path -> path.fileName.toString().endsWith(".expectation.txt") }
                    .sorted(compareBy<Path> { path -> path.fileName.toString() })
                    .map { path -> parseExample(path) }
                    .toList()
            }
    }

    private fun loadExampleNames(suffix: String): List<String> {
        val repoRoot = resolveRepoRoot()
        val exampleDir = repoRoot.resolve("examples/m3")
        return Files.list(exampleDir)
            .use { paths ->
                paths
                    .filter { path -> path.fileName.toString().endsWith(suffix) }
                    .sorted(compareBy<Path> { path -> path.fileName.toString() })
                    .map { path -> path.fileName.toString().removeSuffix(suffix) }
                    .toList()
            }
    }

    private fun parseExample(path: Path): M3ProofExample {
        val values = Files.readAllLines(path)
            .map(String::trim)
            .filter { line -> line.isNotEmpty() && !line.startsWith("#") }
            .associate { line ->
                val separator = line.indexOf('=')
                require(separator > 0) { "Expectation line must be key=value in ${path.fileName}: $line" }
                line.substring(0, separator) to line.substring(separator + 1)
            }

        return M3ProofExample(
            name = path.name.removeSuffix(".expectation.txt"),
            pluginSet = parsePluginSet(values.getValue("plugin_set")),
            approvedPlugins = parseCsv(values.getValue("approved_plugins")),
            components = values.getValue("components").toInt(),
            ports = values.getValue("ports").toInt(),
            connections = values.getValue("connections").toInt(),
            views = parseCsv(values.getValue("views")),
            renderContributions = parseCsv(values.getValue("render_contributions")),
            renderStatus = parseRenderStatus(values.getValue("render_status")),
            renderView = values["render_view"],
            activeRenderContributions = parseCsv(values["active_render_contributions"].orEmpty()),
            renderBlockedBy = values["render_blocked_by"]?.let(CompilerPassId::valueOf),
        )
    }

    private fun parsePluginSet(value: String): M3PluginSet {
        return when (value) {
            "electrical-only" -> M3PluginSet.ELECTRICAL_ONLY
            "dummy-only" -> M3PluginSet.DUMMY_ONLY
            "both" -> M3PluginSet.BOTH
            else -> error("Unsupported M3 plugin set `$value`.")
        }
    }

    private fun parseRenderStatus(value: String): M3RenderStatus {
        return when (value) {
            "emitted" -> M3RenderStatus.EMITTED
            "blocked" -> M3RenderStatus.BLOCKED
            else -> error("Unsupported render status `$value`.")
        }
    }

    private fun parseCsv(value: String): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }
        return value.split(',').map(String::trim)
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        require(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }

    private data class M3ProofExample(
        val name: String,
        val pluginSet: M3PluginSet,
        val approvedPlugins: List<String>,
        val components: Int,
        val ports: Int,
        val connections: Int,
        val views: List<String>,
        val renderContributions: List<String>,
        val renderStatus: M3RenderStatus,
        val renderView: String?,
        val activeRenderContributions: List<String>,
        val renderBlockedBy: CompilerPassId?,
    )

    private enum class M3PluginSet {
        ELECTRICAL_ONLY,
        DUMMY_ONLY,
        BOTH,
    }

    private enum class M3RenderStatus {
        EMITTED,
        BLOCKED,
    }
}
