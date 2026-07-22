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

class AthenaM30SampleProjectCompilerTest {
    @Test
    fun `m30 customer demo sample compiles without diagnostics and exposes governed projection`() {
        val compiler = AthenaCompiler()
        val source = sampleProjectSource()
        assertTrue(Files.exists(source), "Missing M30 sample source: $source")
        val sourceText = Files.readString(source)

        assertTrue(sourceText.contains("    port "), "M30 sample must use compact nested device-owned ports.")
        assertNoVisualOrQetSourceSyntax(sourceText)

        val result = compiler.compile(source)
        when (result) {
            is CompilerCompilationSuccess -> {
                assertTrue(
                    result.semanticResult.diagnostics.isEmpty(),
                    result.semanticResult.diagnostics.joinToString(separator = "\n") { diagnostic ->
                        "${diagnostic.ruleId.value}: ${diagnostic.message}"
                    },
                )
                assertTrue(result.projections.isNotEmpty(), "M30 sample projection must be available.")
                assertTrue(
                    result.projections.any { projection ->
                        projection.view.id == "documentation" &&
                            projection.sheets.map { sheet -> sheet.sheetId.value } == listOf(
                            "documentation/sheet/01-control",
                            "documentation/sheet/02-field-device",
                        )
                    },
                    "M30 sample must expose the M31 governed two-sheet customer projection policy.",
                )
                assertTrue(
                    result.projections
                        .flatMap { projection -> projection.crossReferences }
                        .isNotEmpty(),
                    "M30 sample must include semantic references for professional representation proof.",
                )
            }

            is CompilerCompilationParseFailure -> fail(
                buildString {
                    appendLine("${source.fileName} fails to parse:")
                    result.diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.line}:${diagnostic.column} ${diagnostic.message}")
                    }
                },
            )
        }
    }

    @Test
    fun `m30 sample project links semantic references without treating source files as sheets`() {
        val compiler = AthenaCompiler()
        val sampleProjectRoot = resolveRepoRoot().resolve("examples/m30/sample-project")
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
        assertEquals(1, sources.size, "M30 sample has one semantic source file; sheets come from projection facts.")

        val built = compiler.buildProjectSemanticGraph(publication, sources)
        val snapshot = assertNotNull(
            built.snapshot,
            built.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code.value}: ${diagnostic.message}"
            },
        )
        val linked = compiler.linkProjectSemanticReferences(
            compiler.bindProjectSemanticLayoutHints(
                compiler.indexProjectSemanticDeclarations(
                    compiler.emitProjectSemanticDiagnostics(
                        compiler.resolveProjectSemanticImports(snapshot),
                    ),
                ),
            ),
        )

        assertTrue(
            linked.diagnostics.isEmpty(),
            linked.diagnostics.joinToString(separator = "\n") { diagnostic ->
                "${diagnostic.code.value}: ${diagnostic.message}"
            },
        )
    }

    @Test
    fun `m31 documentation projection rederives occurrences without offsheet reference copies`() {
        val compiler = AthenaCompiler()
        val source = sampleProjectSource()

        val result = compiler.compile(source)
        val success = assertSuccess(result)
        val documentation = success.projections.single { projection -> projection.view.id == "documentation" }

        assertFalse(
            documentation.nodes.any { node -> node.projectionId.value.endsWith("_reference") },
            "Documentation projection must not create duplicate off-sheet `_reference` node copies.",
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
            "Documentation canvas width must derive from actual node, label, and route bounds plus governed margin.",
        )
        assertTrue(
            documentation.canvasHeight <= maxContentY + 40,
            "Documentation canvas height must derive from actual node, label, and route bounds plus governed margin.",
        )
        val duplicatedVisualSubjects = documentation.nodes
            .groupBy { node -> node.semanticId }
            .filterValues { nodes -> nodes.size > 1 }
        assertEquals(
            emptyMap(),
            duplicatedVisualSubjects,
            "Each semantic component must have one visual occurrence per documentation projection; cross references are separate facts.",
        )
    }

    @Test
    fun `m31 documentation relationships publish governed terminal anchors without center fallback`() {
        val compiler = AthenaCompiler()
        val result = compiler.compile(sampleProjectSource())
        val success = assertSuccess(result)
        val documentation = success.projections.single { projection -> projection.view.id == "documentation" }

        assertTrue(documentation.electricalAnchors.isNotEmpty(), "Documentation projection must publish terminal anchors.")
        assertEquals(
            documentation.connections.size * 2,
            documentation.electricalConnectionEndpoints.size,
            "Each accepted relationship projection must have source and target endpoint anchor facts.",
        )
        val endpointAnchorIds = documentation.electricalConnectionEndpoints.map { endpoint -> endpoint.anchorId }.toSet()
        val governedAnchorIds = documentation.electricalAnchors.map { anchor -> anchor.anchorId }.toSet()
        assertTrue(
            endpointAnchorIds.all(governedAnchorIds::contains),
            "Relationship endpoints must reference governed terminal anchors, not component centers.",
        )
        assertEquals(
            documentation.connections.map { connection -> connection.semanticId }.toSet(),
            documentation.electricalRoutingCorridors.map { corridor -> corridor.connectionSemanticId }.toSet(),
            "Every accepted relationship projection must publish a routing corridor from terminal anchors.",
        )
    }

    @Test
    fun `m31 documentation projection emits typed cross sheet reference links`() {
        val compiler = AthenaCompiler()
        val result = compiler.compile(sampleProjectSource())
        val success = assertSuccess(result)
        val documentation = success.projections.single { projection -> projection.view.id == "documentation" }
        val expectedSheetIds = listOf(
            "documentation/sheet/01-control",
            "documentation/sheet/02-field-device",
        )
        assertEquals(
            expectedSheetIds,
            documentation.sheets.map { sheet -> sheet.sheetId.value },
            "M31 reference proof requires the governed control and field-device sheets.",
        )

        val links = documentation.crossReferences.flatMap { reference ->
            reference.links.map { link -> reference to link }
        }
        val link = links.firstOrNull { (_, link) ->
            link.sourceSheetId.value == "documentation/sheet/01-control" &&
                link.targetSheetId.value == "documentation/sheet/02-field-device"
        } ?: fail("Documentation projection must expose at least one typed control-to-field cross-sheet reference link.")

        val (reference, crossSheetLink) = link
        assertTrue(reference.crossReferenceId.value.isNotBlank(), "Cross-reference id must be stable and inspectable.")
        assertEquals(
            reference.semanticId,
            crossSheetLink.semanticId,
            "Cross-sheet link must carry the same canonical semantic id as its owning reference.",
        )
        assertTrue(
            crossSheetLink.sourceOccurrenceId.isNotBlank(),
            "Cross-sheet link must expose a stable source occurrence id.",
        )
        assertTrue(
            crossSheetLink.targetOccurrenceId.isNotBlank(),
            "Cross-sheet link must expose a stable target occurrence id.",
        )
        assertFalse(
            crossSheetLink.sourceOccurrenceId.endsWith("_reference") ||
                crossSheetLink.targetOccurrenceId.endsWith("_reference"),
            "Cross-sheet references must not depend on duplicate `_reference` visual occurrence ids.",
        )
        assertEquals(
            "01-control -> 02-field-device",
            crossSheetLink.compactNotation,
            "Compact notation must derive from governed sheet identity/order, not visible labels or DOM/SVG state.",
        )
    }

    private fun assertNoVisualOrQetSourceSyntax(sourceText: String) {
        val forbidden = listOf("qelectrotech", ".elmt", "svg", "path", "viewBox", "rectangle", "circle", "stroke")
        val lowered = sourceText.lowercase()
        forbidden.forEach { token ->
            assertTrue(token.lowercase() !in lowered, "M30 semantic source must not contain visual/QET token `$token`.")
        }
    }

    private fun assertSuccess(result: CompilerCompilationResult): CompilerCompilationSuccess {
        return when (result) {
            is CompilerCompilationSuccess -> result
            is CompilerCompilationParseFailure -> fail(
                buildString {
                    appendLine("Sample source fails to parse:")
                    result.diagnostics.forEach { diagnostic ->
                        appendLine("- ${diagnostic.line}:${diagnostic.column} ${diagnostic.message}")
                    }
                },
            )
        }
    }

    private fun sampleProjectSource(): Path =
        resolveRepoRoot().resolve("examples/m30/sample-project/src/01-rolling-shutter-control-source.athena")

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
