package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM31SampleProjectCompilerTest {
    @Test
    fun `m31 customer sample compiles as governed authoring workspace`() {
        val compiler = AthenaCompiler()
        val source = sampleProjectSource()
        assertTrue(Files.exists(source), "Missing M31 sample source: $source")
        val sourceText = Files.readString(source)

        assertTrue(sourceText.contains("    port "), "M31 sample must use nested device-owned ports.")
        assertTrue(sourceText.contains("connect supply_feed {"), "M31 sample must use grouped relationship provenance.")
        assertFalse(
            Regex("""(?m)^\s*port\s+[A-Za-z0-9_]+\.[A-Za-z0-9_]+\s*\{""").containsMatchIn(sourceText),
            "M31 sample must not prefer legacy top-level generated device-port declarations.",
        )
        assertNoVisualOrQetSourceSyntax(sourceText)

        val result = compiler.compile(source)
        val success = assertSuccess(result)
        assertTrue(
            success.semanticResult.diagnostics.isEmpty(),
            success.semanticResult.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.ruleId.value}: ${diagnostic.message}"
            },
        )

        val documentation = success.projections.single { projection -> projection.view.id == "documentation" }
        assertEquals(
            listOf(
                "control-and-plc-logic",
                "field-wiring-and-terminal-transition",
            ),
            documentation.sheets.map { sheet -> sheet.policyEvidence?.sheetViewRole },
            "M31 sample must expose exactly the governed two-sheet customer roles.",
        )
        val crossSheetLinks = documentation.crossReferences.flatMap { reference ->
            reference.links.map { link -> reference to link }
        }
        assertTrue(
            crossSheetLinks.any { (_, link) ->
                link.sourceSheetId.value == "documentation/sheet/01-control" &&
                    link.targetSheetId.value == "documentation/sheet/02-field-device"
            },
            "M31 sample must include typed semantic references from the control sheet to the field-device sheet.",
        )
        assertTrue(documentation.electricalAnchors.isNotEmpty(), "M31 sample must publish terminal anchors.")
        assertEquals(
            documentation.connections.size * 2,
            documentation.electricalConnectionEndpoints.size,
            "Every accepted relationship projection must have source and target endpoint anchor facts.",
        )
        assertTrue(
            documentation.electricalConnectionEndpoints
                .map { endpoint -> endpoint.anchorId }
                .all(documentation.electricalAnchors.map { anchor -> anchor.anchorId }.toSet()::contains),
            "Relationship endpoints must reference governed terminal anchors, not component centers.",
        )
        assertEquals(
            documentation.connections.map { connection -> connection.semanticId }.toSet(),
            documentation.electricalRoutingCorridors.map { corridor -> corridor.connectionSemanticId }.toSet(),
            "Every accepted relationship projection must publish a routing corridor from terminal anchors.",
        )
        assertEquals(
            emptyMap(),
            documentation.nodes
                .groupBy { node -> node.semanticId }
                .filterValues { nodes -> nodes.size > 1 },
            "M31 sample must not create duplicate off-sheet visual occurrences.",
        )
        val maxContentX = maxOf(
            documentation.nodes.maxOf { node -> node.bounds.x + node.bounds.width },
            documentation.labels.maxOf { label -> label.bounds.x + label.bounds.width },
            documentation.connections.maxOf { connection -> maxOf(connection.start.x, connection.end.x) },
        )
        val maxContentY = maxOf(
            documentation.nodes.maxOf { node -> node.bounds.y + node.bounds.height },
            documentation.labels.maxOf { label -> label.bounds.y + label.bounds.height },
            documentation.connections.maxOf { connection -> maxOf(connection.start.y, connection.end.y) },
        )
        assertTrue(
            documentation.canvasWidth <= maxContentX + 40,
            "M31 sample canvas width must derive from actual content bounds plus governed margin.",
        )
        assertTrue(
            documentation.canvasHeight <= maxContentY + 40,
            "M31 sample canvas height must derive from actual content bounds plus governed margin.",
        )
    }

    @Test
    fun `m31 sample project sheets come from policy not source files`() {
        val compiler = AthenaCompiler()
        val sampleProjectRoot = resolveRepoRoot().resolve("examples/m31/sample-project")
        compiler.materializeRepositoryLock(sampleProjectRoot)

        val publication = compiler.publishRepositoryGraphReport(sampleProjectRoot)
        val graph = assertNotNull(publication.graph)
        assertEquals(1, graph.packages.size)
        val sources = graph.packages.flatMap { resolvedPackage ->
            val sourceRoot = sampleProjectRoot.resolve(resolvedPackage.sourceRoot).toAbsolutePath().normalize()
            Files.walk(sourceRoot).use { stream ->
                stream
                    .filter(Files::isRegularFile)
                    .filter { path -> path.fileName.toString().endsWith(".athena") }
                    .map { path ->
                        ProjectSemanticSourceInput(
                            packageId = resolvedPackage.packageId,
                            sourceRootRelativePath = sourceRoot.relativize(path.toAbsolutePath().normalize())
                                .toString()
                                .replace('\\', '/'),
                            sourceContent = Files.readString(path),
                        )
                    }
                    .toList()
            }
        }
        assertEquals(1, sources.size, "M31 sample keeps source count separate from governed sheet count.")
    }

    private fun assertNoVisualOrQetSourceSyntax(sourceText: String) {
        val forbidden = listOf("qelectrotech", ".elmt", "svg", "path", "viewBox", "rectangle", "circle", "stroke")
        val lowered = sourceText.lowercase()
        forbidden.forEach { token ->
            assertTrue(token.lowercase() !in lowered, "M31 semantic source must not contain visual/QET token `$token`.")
        }
    }

    private fun assertSuccess(result: CompilerCompilationResult): CompilerCompilationSuccess {
        return when (result) {
            is CompilerCompilationSuccess -> result
            is CompilerCompilationParseFailure -> fail(
                buildString {
                    appendLine("M31 sample source fails to parse:")
                    result.diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.line}:${diagnostic.column} ${diagnostic.message}")
                    }
                },
            )
        }
    }

    private fun sampleProjectSource(): Path =
        resolveRepoRoot().resolve("examples/m31/sample-project/src/01-governed-authoring-customer-source.athena")

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
