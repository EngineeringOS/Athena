package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ImportDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ProjectSemanticCapabilityProvenanceProjectorTest {
    @Test
    fun `records package capability markers on namespaces owned by that package`() {
        val fixture = capabilityFixture()

        val projected = ProjectSemanticCapabilityProvenanceProjector().project(
            fixture.snapshot,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )
        val compilerProjected = AthenaCompiler().preserveProjectSemanticCapabilities(
            fixture.snapshot,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )

        assertEquals(projected.namespaces, compilerProjected.namespaces)
        assertEquals(emptyList(), projected.namespaces.single { it.packageKey == fixture.rootKey }.admittedCapabilities)
        assertEquals(
            listOf(COMPONENT_KNOWLEDGE_AVAILABLE),
            projected.namespaces.single { it.packageKey == fixture.vendorKey }.admittedCapabilities,
        )
    }

    @Test
    fun `imported namespace capability markers survive import resolution and reference linking`() {
        val fixture = capabilityFixture()
        val projected = ProjectSemanticCapabilityProvenanceProjector().project(
            fixture.snapshot,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )

        val linked = ProjectSemanticReferenceLinker().link(
            ProjectSemanticDeclarationIndexer().index(ProjectSemanticImportResolver().resolve(projected)),
        )

        assertEquals(
            listOf(COMPONENT_KNOWLEDGE_AVAILABLE),
            linked.namespaces.single { it.packageKey == fixture.vendorKey }.admittedCapabilities,
        )
        assertEquals(fixture.vendorSourceUnitId, linked.declarations.single { it.packageKey(linked) == fixture.vendorKey }.sourceUnitId)
        assertEquals(2, linked.bindings.size)
    }

    @Test
    fun `emits stable diagnostics that explain namespace capability availability`() {
        val fixture = capabilityFixture()

        val projected = ProjectSemanticCapabilityProvenanceProjector().project(
            fixture.snapshot,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )

        assertEquals(
            listOf("semantic.capability.namespace.available"),
            projected.diagnostics.map { it.code.value },
        )
        assertEquals(ProjectSemanticDiagnosticSeverity.INFO, projected.diagnostics.single().severity)
        assertEquals(fixture.vendorSourceUnitId, projected.diagnostics.single().sourceUnitId)
    }

    @Test
    fun `keeps capability projection deterministic and idempotent`() {
        val fixture = capabilityFixture()
        val reversed = ProjectSemanticGraphSnapshot.canonical(
            fixture.snapshot.graphId,
            fixture.snapshot.rootPackageId,
            fixture.snapshot.packages.reversed(),
            fixture.snapshot.sourceUnits.reversed(),
            fixture.snapshot.namespaces.reversed(),
            emptyList(),
            emptyList(),
            emptyList(),
        )

        val forward = ProjectSemanticCapabilityProvenanceProjector().project(
            fixture.snapshot,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )
        val reversedProjected = ProjectSemanticCapabilityProvenanceProjector().project(
            reversed,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )
        val repeated = ProjectSemanticCapabilityProvenanceProjector().project(
            forward,
            mapOf(fixture.vendorKey to listOf(COMPONENT_KNOWLEDGE_AVAILABLE)),
        )

        assertEquals(forward.namespaces, reversedProjected.namespaces)
        assertEquals(forward.diagnostics, reversedProjected.diagnostics)
        assertEquals(forward.namespaces, repeated.namespaces)
        assertEquals(forward.diagnostics, repeated.diagnostics)
    }

    @Test
    fun `canonical snapshot rejects blank capability markers`() {
        val fixture = capabilityFixture()

        assertFailsWith<IllegalArgumentException> {
            ProjectSemanticCapabilityProvenanceProjector().project(
                fixture.snapshot,
                mapOf(fixture.vendorKey to listOf(" ")),
            )
        }
    }

    private fun capabilityFixture(): CapabilityFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val vendorId = PackageIdentifier("com.vendor", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val vendorKey = CanonicalSemanticIdentityBuilder.packageKey(vendorId)
        val vendorAst = parse("vendor.athena", "package com.vendor\nsystem Vendor {\n  port Vendor.out {}\n}")
        val consumerAst = parse(
            "consumer.athena",
            """
            package com.root
            import com.vendor
            system Consumer {
              connect Vendor.out -> Local.out
              port Local.out {}
            }
            """.trimIndent(),
        )
        val vendorSource = sourceUnit(vendorKey, "vendor.athena", "vendor", vendorAst.declarations)
        val consumer = sourceUnit(
            rootKey,
            "consumer.athena",
            "consumer",
            consumerAst.declarations,
            consumerAst.imports,
        )
        val packages = listOf(
            ProjectSemanticPackage(rootId, rootKey, "src", listOf(vendorKey)),
            ProjectSemanticPackage(vendorId, vendorKey, "vendor", emptyList()),
        )
        val sourceUnits = listOf(consumer, vendorSource)
        val namespaces = listOf(
            namespace(rootKey, listOf("com", "root"), listOf(consumer.sourceUnitId)),
            namespace(vendorKey, listOf("com", "vendor"), listOf(vendorSource.sourceUnitId)),
        )
        return CapabilityFixture(snapshot(rootKey, packages, sourceUnits, namespaces), rootKey, vendorKey, vendorSource.sourceUnitId)
    }

    private fun parse(path: String, content: String): com.engineeringood.athena.language.SourceFileAst {
        return assertIs<ParseSuccess>(AthenaLanguageParser().parse(path, content)).ast
    }

    private fun sourceUnit(
        packageKey: PackageKey,
        path: String,
        content: String,
        declarations: List<Declaration>,
        imports: List<ImportDeclaration> = emptyList(),
    ): ProjectSemanticSourceUnit {
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, path)
        return ProjectSemanticSourceUnit(
            sourceUnitId,
            packageKey,
            path,
            CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, content),
            authoredImports = imports,
            authoredDeclarations = declarations,
        )
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

    private fun ProjectSemanticDeclaration.packageKey(snapshot: ProjectSemanticGraphSnapshot): PackageKey {
        return snapshot.namespaces.single { it.namespaceId == namespaceId }.packageKey
    }

    private companion object {
        private const val COMPONENT_KNOWLEDGE_AVAILABLE = "component-knowledge:available"
    }
}

private data class CapabilityFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
    val rootKey: PackageKey,
    val vendorKey: PackageKey,
    val vendorSourceUnitId: SourceUnitId,
)
