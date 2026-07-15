package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerParseSuccess
import com.engineeringood.athena.compiler.CompilerSourceDocument
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM18RepositoryProofCorpusTest {
    @Test
    fun `repository backed m18 corpus contains required governed fixture inventory`() {
        val root = corpusRoot()
        val valid = root.resolve("valid-workspace")
        val graphInvalid = root.resolve("graph-invalid")

        listOf(
            valid.resolve("athena.yaml"),
            valid.resolve("athena.lock"),
            valid.resolve("src/single-package-success.athena"),
            valid.resolve("src/cross-package-consumer.athena"),
            valid.resolve("src/invalid-import.athena"),
            valid.resolve("src/unresolved-symbol.athena"),
            valid.resolve("vendor/controls/athena.yaml"),
            valid.resolve("vendor/controls/athena.lock"),
            valid.resolve("vendor/controls/src/vendor-controls.athena"),
            graphInvalid.resolve("athena.yaml"),
            graphInvalid.resolve("athena.lock"),
        ).forEach { path ->
            assertTrue(Files.isRegularFile(path), "Missing M18 corpus fixture: ${root.relativize(path)}")
        }
    }

    @Test
    fun `repository backed corpus links success fixtures and reports invalid authored behavior`() {
        val compiler = AthenaCompiler()
        val corpus = buildCorpus(compiler, corpusRoot().resolve("valid-workspace"))

        assertTrue(corpus.publicationDiagnostics.isEmpty(), corpus.publicationDiagnostics.joinToString())
        assertEquals(
            listOf("com.engineeringood.m18.root", "com.engineeringood.m18.vendor.controls"),
            corpus.linked.packages.map { semanticPackage -> semanticPackage.packageId.name },
        )
        assertEquals(
            listOf("com.engineeringood.m18.vendor.controls"),
            corpus.linked.packages.single { it.packageId.name == "com.engineeringood.m18.root" }
                .directDependencies
                .map { packageKey -> corpus.linked.packages.single { it.packageKey == packageKey }.packageId.name },
        )
        assertTrue(corpus.linked.bindings.any { binding ->
            val declaration = corpus.linked.declarations.single { it.declarationId == binding.resolvedDeclarationId }
            val sourceUnit = corpus.linked.sourceUnits.single { it.sourceUnitId == declaration.sourceUnitId }
            sourceUnit.sourceRootRelativePath == "vendor-controls.athena"
        })
        assertTrue(
            corpus.linked.diagnostics.any { it.code.value == "semantic.import.namespace.unavailable" },
            corpus.linked.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
        )
        assertTrue(
            corpus.linked.diagnostics.any { it.code.value == "semantic.reference.unresolved" },
            corpus.linked.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
        )
    }

    @Test
    fun `repository backed graph invalid fixture reports stable compiler owned diagnostic`() {
        val compiler = AthenaCompiler()
        val publication = compiler.publishRepositoryGraphReport(corpusRoot().resolve("graph-invalid"))
        val result = compiler.buildProjectSemanticGraph(publication, emptyList())

        assertEquals(null, result.snapshot)
        assertTrue(
            result.diagnostics.any { it.code.value == "semantic.repository.publication.invalid" },
            result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
        )
        assertTrue(
            publication.diagnostics.any { it.code == "repository.contract.root.missing" },
            publication.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
    }

    private fun buildCorpus(
        compiler: AthenaCompiler,
        repositoryRoot: Path,
    ): CorpusBuild {
        val publication = compiler.publishRepositoryGraphReport(repositoryRoot)
        val graph = assertNotNull(publication.graph)
        val sources = graph.packages.flatMap { resolvedPackage ->
            val sourceRoot = repositoryRoot.resolve(resolvedPackage.sourceRoot).toAbsolutePath().normalize()
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
                            sourceContent = path.readText(),
                        )
                    }
                    .toList()
            }
        }
        val build = compiler.buildProjectSemanticGraph(publication, sources)
        val snapshot = assertNotNull(
            build.snapshot,
            build.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
        )
        val linked = compiler.linkProjectSemanticReferences(
            compiler.indexProjectSemanticDeclarations(
                compiler.emitProjectSemanticDiagnostics(
                    compiler.resolveProjectSemanticImports(snapshot),
                ),
            ),
        )
        val documentsBySourceUnit = linked.sourceUnits.associate { sourceUnit ->
            val semanticPackage = linked.packages.single { it.packageKey == sourceUnit.packageKey }
            val sourcePath = repositoryRoot
                .resolve(semanticPackage.sourceRoot)
                .resolve(sourceUnit.sourceRootRelativePath)
                .toAbsolutePath()
                .normalize()
            val parsed = compiler.parse(sourcePath, sourcePath.readText()) as CompilerParseSuccess
            sourceUnit.sourceUnitId to parsed.source
        }
        return CorpusBuild(
            publicationDiagnostics = publication.diagnostics.map { diagnostic -> diagnostic.code },
            linked = linked,
            documentsBySourceUnit = documentsBySourceUnit,
        )
    }

    private fun corpusRoot(): Path {
        val root = listOf(
            Path.of("examples", "m18", "repository-proof"),
            Path.of("..", "..", "examples", "m18", "repository-proof"),
        ).map { it.toAbsolutePath().normalize() }
            .firstOrNull(Files::isDirectory)
        assertNotNull(root, "Missing examples/m18/repository-proof")
        return root
    }
}

private data class CorpusBuild(
    val publicationDiagnostics: List<String>,
    val linked: ProjectSemanticGraphSnapshot,
    val documentsBySourceUnit: Map<SourceUnitId, CompilerSourceDocument>,
)
