package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerSourceDocument
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectSemanticLinkedLowererTest {
    @Test
    fun `lowers linked source units through the canonical lowerer with graph identity`() {
        val fixture = linkedLoweringFixture()

        val result = ProjectSemanticLinkedLowerer().lower(fixture.linkedSnapshot, fixture.documentsBySourceUnit)
        val compilerResult = AthenaCompiler().lowerLinkedProjectSemanticSources(fixture.linkedSnapshot, fixture.documentsBySourceUnit)

        assertEquals(
            result.loweredSourceUnits.map { Triple(it.graphId, it.sourceUnitId, it.bindingIds) },
            compilerResult.loweredSourceUnits.map { Triple(it.graphId, it.sourceUnitId, it.bindingIds) },
        )
        assertEquals(emptyList(), result.diagnostics)
        assertTrue(result.loweredSourceUnits.all { it.graphId == fixture.linkedSnapshot.graphId })
        assertEquals(listOf("Consumer", "Provider"), result.loweredSourceUnits.map { it.document.system.name }.sorted())
    }

    @Test
    fun `preserves source unit binding ids beside lowered documents`() {
        val fixture = linkedLoweringFixture()

        val result = ProjectSemanticLinkedLowerer().lower(fixture.linkedSnapshot, fixture.documentsBySourceUnit)
        val consumerLowering = result.loweredSourceUnits.single { it.sourceUnitId == fixture.consumerSourceUnitId }

        assertEquals(fixture.linkedSnapshot.bindings.map { it.bindingId }, consumerLowering.bindingIds)
    }

    @Test
    fun `reports missing linked source documents deterministically`() {
        val fixture = linkedLoweringFixture()

        val result = ProjectSemanticLinkedLowerer().lower(
            fixture.linkedSnapshot,
            fixture.documentsBySourceUnit - fixture.consumerSourceUnitId,
        )

        assertEquals(1, result.diagnostics.size)
        assertEquals("semantic.lowering.source.missing", result.diagnostics.single().code.value)
        assertEquals(fixture.consumerSourceUnitId, result.diagnostics.single().sourceUnitId)
    }

    @Test
    fun `lowers source unit documents independently without AST paste`() {
        val fixture = linkedLoweringFixture()

        val result = ProjectSemanticLinkedLowerer().lower(fixture.linkedSnapshot, fixture.documentsBySourceUnit)

        assertEquals(2, result.loweredSourceUnits.size)
        assertEquals(
            listOf("Consumer", "Provider"),
            result.loweredSourceUnits.map { it.document.system.name }.sorted(),
        )
    }

    @Test
    fun `keeps linked lowering deterministic from reversed raw collections`() {
        val fixture = linkedLoweringFixture()
        val reversed = ProjectSemanticGraphSnapshot.canonical(
            fixture.linkedSnapshot.graphId,
            fixture.linkedSnapshot.rootPackageId,
            fixture.linkedSnapshot.packages.reversed(),
            fixture.linkedSnapshot.sourceUnits.reversed(),
            fixture.linkedSnapshot.namespaces.reversed(),
            fixture.linkedSnapshot.declarations.reversed(),
            fixture.linkedSnapshot.bindings.reversed(),
            fixture.linkedSnapshot.diagnostics.reversed(),
        )

        val forward = ProjectSemanticLinkedLowerer().lower(fixture.linkedSnapshot, fixture.documentsBySourceUnit)
        val reversedResult = ProjectSemanticLinkedLowerer().lower(reversed, fixture.documentsBySourceUnit)

        assertEquals(forward.loweredSourceUnits, reversedResult.loweredSourceUnits)
        assertEquals(forward.diagnostics, reversedResult.diagnostics)
    }

    private fun linkedLoweringFixture(): LinkedLoweringFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val providerSource = parse("provider.athena", "package com.root\nsystem Provider {\n  port PLC1.out {}\n}")
        val consumerSource = parse(
            "consumer.athena",
            "package com.root\nsystem Consumer {\n  connect PLC1.out -> PLC1.out\n}",
        )
        val providerUnit = sourceUnit(rootKey, "provider.athena", "provider", providerSource.source.ast.declarations)
        val consumerUnit = sourceUnit(rootKey, "consumer.athena", "consumer", consumerSource.source.ast.declarations)
        val namespace = ProjectSemanticNamespace(
            CanonicalSemanticIdentityBuilder.namespaceId(rootKey, listOf("com", "root")),
            rootKey,
            listOf("com", "root"),
            listOf(providerUnit.sourceUnitId, consumerUnit.sourceUnitId),
            emptyList(),
        )
        val packages = listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList()))
        val indexed = ProjectSemanticDeclarationIndexer().index(
            snapshot(rootKey, packages, listOf(providerUnit, consumerUnit), listOf(namespace)),
        )
        val linked = ProjectSemanticReferenceLinker().link(indexed)
        return LinkedLoweringFixture(
            linked,
            mapOf(
                providerUnit.sourceUnitId to providerSource.source,
                consumerUnit.sourceUnitId to consumerSource.source,
            ),
            consumerUnit.sourceUnitId,
        )
    }

    private fun parse(path: String, content: String): com.engineeringood.athena.compiler.CompilerParseSuccess {
        return assertIs<com.engineeringood.athena.compiler.CompilerParseSuccess>(
            AthenaCompiler().parse(java.nio.file.Path.of(path), content),
        )
    }

    private fun sourceUnit(
        packageKey: PackageKey,
        path: String,
        content: String,
        declarations: List<Declaration>,
    ): ProjectSemanticSourceUnit {
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, path)
        return ProjectSemanticSourceUnit(
            sourceUnitId,
            packageKey,
            path,
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, content),
            authoredDeclarations = declarations,
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
}

private data class LinkedLoweringFixture(
    val linkedSnapshot: ProjectSemanticGraphSnapshot,
    val documentsBySourceUnit: Map<SourceUnitId, CompilerSourceDocument>,
    val consumerSourceUnitId: SourceUnitId,
)
