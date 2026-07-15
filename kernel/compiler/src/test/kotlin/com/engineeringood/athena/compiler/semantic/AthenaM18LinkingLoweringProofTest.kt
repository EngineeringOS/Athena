package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerParseSuccess
import com.engineeringood.athena.compiler.CompilerSourceDocument
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM18LinkingLoweringProofTest {
    @Test
    fun `single package fixture links and lowers deterministically`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val source = sourceUnit(rootKey, parseFixture("single-package-success.athena"))
        val linked = linkedSnapshot(
            rootKey,
            listOf(ProjectSemanticPackage(rootId, rootKey, "examples/m18/linking-lowering-proof", emptyList())),
            listOf(source),
            listOf(namespace(rootKey, listOf("com", "root"), listOf(source.sourceUnit.sourceUnitId))),
        )

        val lowered = ProjectSemanticLinkedLowerer().lower(linked, source.documentsBySourceUnit())

        assertEquals(2, linked.bindings.size)
        assertEquals(emptyList(), linked.diagnostics)
        assertEquals(1, lowered.loweredSourceUnits.size)
        assertEquals(linked.graphId, lowered.loweredSourceUnits.single().graphId)
    }

    @Test
    fun `cross source fixtures link and lower in one governed package`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val provider = sourceUnit(rootKey, parseFixture("cross-source-provider.athena"))
        val consumer = sourceUnit(rootKey, parseFixture("cross-source-consumer.athena"))
        val linked = linkedSnapshot(
            rootKey,
            listOf(ProjectSemanticPackage(rootId, rootKey, "examples/m18/linking-lowering-proof", emptyList())),
            listOf(consumer, provider),
            listOf(namespace(rootKey, listOf("com", "root"), listOf(provider.sourceUnit.sourceUnitId, consumer.sourceUnit.sourceUnitId))),
        )

        val lowered = ProjectSemanticLinkedLowerer().lower(linked, (provider + consumer).documentsBySourceUnit())

        assertEquals(2, linked.bindings.size)
        assertEquals(emptyList(), linked.diagnostics)
        assertEquals(2, lowered.loweredSourceUnits.size)
    }

    @Test
    fun `cross package fixtures link through governed import and lower`() {
        val rootId = PackageIdentifier("com.root", "1")
        val vendorId = PackageIdentifier("com.vendor", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val vendorKey = CanonicalSemanticIdentityBuilder.packageKey(vendorId)
        val vendor = sourceUnit(vendorKey, parseFixture("cross-package-vendor.athena"))
        val consumer = sourceUnit(rootKey, parseFixture("cross-package-consumer.athena"))
        val linked = linkedSnapshot(
            rootKey,
            listOf(
                ProjectSemanticPackage(rootId, rootKey, "examples/m18/linking-lowering-proof", listOf(vendorKey)),
                ProjectSemanticPackage(vendorId, vendorKey, "examples/m18/linking-lowering-proof/vendor", emptyList()),
            ),
            listOf(consumer, vendor),
            listOf(
                namespace(rootKey, listOf("com", "root"), listOf(consumer.sourceUnit.sourceUnitId)),
                namespace(vendorKey, listOf("com", "vendor"), listOf(vendor.sourceUnit.sourceUnitId)),
            ),
        )

        val lowered = ProjectSemanticLinkedLowerer().lower(linked, (consumer + vendor).documentsBySourceUnit())

        assertEquals(2, linked.bindings.size)
        assertEquals(emptyList(), linked.diagnostics)
        assertEquals(2, lowered.loweredSourceUnits.size)
        assertTrue(linked.bindings.any { binding -> linked.declarations.single { it.declarationId == binding.resolvedDeclarationId }.sourceUnitId == vendor.sourceUnit.sourceUnitId })
    }

    @Test
    fun `unresolved symbol fixture emits stable reference diagnostic`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val source = sourceUnit(rootKey, parseFixture("unresolved-symbol.athena"))
        val linked = linkedSnapshot(
            rootKey,
            listOf(ProjectSemanticPackage(rootId, rootKey, "examples/m18/linking-lowering-proof", emptyList())),
            listOf(source),
            listOf(namespace(rootKey, listOf("com", "root"), listOf(source.sourceUnit.sourceUnitId))),
        )

        assertEquals(1, linked.bindings.size)
        assertEquals(listOf("semantic.reference.unresolved"), linked.diagnostics.map { it.code.value })
    }

    @Test
    fun `invalid availability fixture does not link dependency without resolved import`() {
        val rootId = PackageIdentifier("com.root", "1")
        val vendorId = PackageIdentifier("com.vendor", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val vendorKey = CanonicalSemanticIdentityBuilder.packageKey(vendorId)
        val vendor = sourceUnit(vendorKey, parseFixture("cross-package-vendor.athena"))
        val consumer = sourceUnit(rootKey, parseFixture("invalid-availability-consumer.athena"))
        val linked = linkedSnapshot(
            rootKey,
            listOf(
                ProjectSemanticPackage(rootId, rootKey, "examples/m18/linking-lowering-proof", listOf(vendorKey)),
                ProjectSemanticPackage(vendorId, vendorKey, "examples/m18/linking-lowering-proof/vendor", emptyList()),
            ),
            listOf(consumer, vendor),
            listOf(
                namespace(rootKey, listOf("com", "root"), listOf(consumer.sourceUnit.sourceUnitId)),
                namespace(vendorKey, listOf("com", "vendor"), listOf(vendor.sourceUnit.sourceUnitId)),
            ),
        )

        assertEquals(1, linked.bindings.size)
        assertEquals(listOf("semantic.reference.unresolved"), linked.diagnostics.map { it.code.value })
    }

    private fun linkedSnapshot(
        rootKey: PackageKey,
        packages: List<ProjectSemanticPackage>,
        sources: List<ProofSourceUnit>,
        namespaces: List<ProjectSemanticNamespace>,
    ): ProjectSemanticGraphSnapshot {
        val snapshot = snapshot(rootKey, packages, sources.map { it.sourceUnit }, namespaces)
        return ProjectSemanticReferenceLinker().link(
            ProjectSemanticDeclarationIndexer().index(ProjectSemanticImportResolver().resolve(snapshot)),
        )
    }

    private fun sourceUnit(packageKey: PackageKey, fixture: ParsedFixture): ProofSourceUnit {
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, fixture.path.fileName.toString())
        return ProofSourceUnit(
            ProjectSemanticSourceUnit(
                sourceUnitId,
                packageKey,
                fixture.path.fileName.toString(),
                CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, fixture.content),
                authoredImports = fixture.source.ast.imports,
                authoredDeclarations = fixture.source.ast.declarations,
            ),
            fixture.source,
        )
    }

    private fun parseFixture(fileName: String): ParsedFixture {
        val path = FIXTURE_ROOT.resolve(fileName)
        val content = path.readText()
        val parsed = assertIs<CompilerParseSuccess>(AthenaCompiler().parse(path, content)).source
        return ParsedFixture(path, content, parsed)
    }

    private fun namespace(
        packageKey: PackageKey,
        qualifiedName: List<String>,
        sourceUnitIds: List<SourceUnitId>,
    ): ProjectSemanticNamespace {
        return ProjectSemanticNamespace(
            CanonicalSemanticIdentityBuilder.namespaceId(packageKey, qualifiedName),
            packageKey,
            qualifiedName,
            sourceUnitIds,
            emptyList(),
        )
    }

    private fun snapshot(
        rootKey: PackageKey,
        packages: List<ProjectSemanticPackage>,
        sourceUnits: List<ProjectSemanticSourceUnit>,
        namespaces: List<ProjectSemanticNamespace>,
    ): ProjectSemanticGraphSnapshot {
        val graphId = CanonicalSemanticIdentityBuilder.graphId(
            rootKey,
            packages.map { GraphPackageIdentity(it.packageKey, it.sourceRoot, it.directDependencies) },
            sourceUnits.map { it.contentIdentity },
        )
        return ProjectSemanticGraphSnapshot.canonical(
            graphId,
            rootKey,
            packages,
            sourceUnits,
            namespaces,
            emptyList(),
            emptyList(),
            emptyList(),
        )
    }

    private operator fun ProofSourceUnit.plus(other: ProofSourceUnit): List<ProofSourceUnit> = listOf(this, other)

    private fun List<ProofSourceUnit>.documentsBySourceUnit(): Map<SourceUnitId, CompilerSourceDocument> {
        return associate { it.sourceUnit.sourceUnitId to it.source }
    }

    private fun ProofSourceUnit.documentsBySourceUnit(): Map<SourceUnitId, CompilerSourceDocument> {
        return mapOf(sourceUnit.sourceUnitId to source)
    }

    private companion object {
        private val FIXTURE_ROOT = listOf(
            Path.of("examples", "m18", "linking-lowering-proof"),
            Path.of("..", "..", "examples", "m18", "linking-lowering-proof"),
        ).map { it.toAbsolutePath().normalize() }
            .first { java.nio.file.Files.isDirectory(it) }
    }
}

private data class ParsedFixture(
    val path: Path,
    val content: String,
    val source: CompilerSourceDocument,
)

private data class ProofSourceUnit(
    val sourceUnit: ProjectSemanticSourceUnit,
    val source: CompilerSourceDocument,
)
