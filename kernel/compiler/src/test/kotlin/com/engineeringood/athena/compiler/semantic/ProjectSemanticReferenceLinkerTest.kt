package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ProjectSemanticReferenceLinkerTest {
    @Test
    fun `links connection endpoint references across source units in one namespace`() {
        val fixture = referenceFixture()

        val linked = ProjectSemanticReferenceLinker().link(fixture.indexedSnapshot)
        val compilerLinked = AthenaCompiler().linkProjectSemanticReferences(fixture.indexedSnapshot)
        val portDeclaration = linked.declarations.single { it.qualifiedAuthoredName == listOf("PLC1", "out") }

        assertEquals(linked.bindings, compilerLinked.bindings)
        assertEquals(2, linked.bindings.size)
        linked.bindings.forEach { binding ->
            assertEquals(fixture.consumerSourceUnitId, binding.sourceUnitId)
            assertEquals(portDeclaration.declarationId, binding.resolvedDeclarationId)
        }
        assertEquals(fixture.consumerEndpointSpans, linked.bindings.map { it.referenceSpan })
        assertEquals(emptyList(), linked.diagnostics)
    }

    @Test
    fun `keeps reference bindings deterministic from reversed raw collections`() {
        val fixture = referenceFixture()
        val reversed = ProjectSemanticGraphSnapshot.canonical(
            fixture.indexedSnapshot.graphId,
            fixture.indexedSnapshot.rootPackageId,
            fixture.indexedSnapshot.packages.reversed(),
            fixture.indexedSnapshot.sourceUnits.reversed(),
            fixture.indexedSnapshot.namespaces.reversed(),
            fixture.indexedSnapshot.declarations.reversed(),
            emptyList(),
            emptyList(),
        )

        val forwardLinked = ProjectSemanticReferenceLinker().link(fixture.indexedSnapshot)
        val reversedLinked = ProjectSemanticReferenceLinker().link(reversed)

        assertEquals(forwardLinked.bindings, reversedLinked.bindings)
        assertEquals(forwardLinked.diagnostics, reversedLinked.diagnostics)
    }

    @Test
    fun `reports unresolved endpoint references with stable diagnostics`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val provider = sourceUnit(
            rootKey,
            "provider.athena",
            "provider",
            declarations("provider.athena", "package com.root\nsystem Provider {\n  port PLC1.out {}\n}"),
        )
        val consumerDeclarations = declarations(
            "consumer.athena",
            "package com.root\nsystem Consumer {\n  connect Missing.out -> PLC1.out\n}",
        )
        val consumer = sourceUnit(rootKey, "consumer.athena", "consumer", consumerDeclarations)
        val namespace = namespace(rootKey, listOf("com", "root"), listOf(provider.sourceUnitId, consumer.sourceUnitId))

        val linked = ProjectSemanticReferenceLinker().link(
            indexedSnapshot(rootKey, listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList())), listOf(provider, consumer), listOf(namespace)),
        )

        assertEquals(1, linked.bindings.size)
        assertEquals(
            listOf("semantic.reference.unresolved"),
            linked.diagnostics.map { it.code.value },
        )
        assertEquals(consumer.sourceUnitId, linked.diagnostics.single().sourceUnitId)
        assertEquals(consumerEndpointSpans(consumerDeclarations).first(), linked.diagnostics.single().sourceSpan)
    }

    @Test
    fun `reports ambiguous endpoint references without selecting by caller order`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val firstProvider = sourceUnit(
            rootKey,
            "a.athena",
            "a",
            declarations("a.athena", "package com.root\nsystem A {\n  port PLC1.out {}\n}"),
        )
        val secondProvider = sourceUnit(
            rootKey,
            "b.athena",
            "b",
            declarations("b.athena", "package com.root\nsystem B {\n  port PLC1.out {}\n}"),
        )
        val consumerDeclarations = declarations(
            "consumer.athena",
            "package com.root\nsystem Consumer {\n  connect PLC1.out -> PLC2.out\n  port PLC2.out {}\n}",
        )
        val consumer = sourceUnit(rootKey, "consumer.athena", "consumer", consumerDeclarations)
        val namespace = namespace(
            rootKey,
            listOf("com", "root"),
            listOf(firstProvider.sourceUnitId, secondProvider.sourceUnitId, consumer.sourceUnitId),
        )

        val linked = ProjectSemanticReferenceLinker().link(
            indexedSnapshot(
                rootKey,
                listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList())),
                listOf(consumer, secondProvider, firstProvider),
                listOf(namespace),
            ),
        )

        assertEquals(1, linked.bindings.size)
        assertEquals(
            listOf("semantic.declaration.ambiguous", "semantic.reference.ambiguous"),
            linked.diagnostics.map { it.code.value },
        )
        assertEquals(consumer.sourceUnitId, linked.diagnostics.last().sourceUnitId)
        assertEquals(consumerEndpointSpans(consumerDeclarations).first(), linked.diagnostics.last().sourceSpan)
    }

    @Test
    fun `canonical snapshot rejects bindings outside known source units and declarations`() {
        val fixture = referenceFixture()
        val linked = ProjectSemanticReferenceLinker().link(fixture.indexedSnapshot)
        val binding = linked.bindings.first()

        assertFailsWith<IllegalArgumentException> {
            ProjectSemanticGraphSnapshot.canonical(
                linked.graphId,
                linked.rootPackageId,
                linked.packages,
                linked.sourceUnits,
                linked.namespaces,
                linked.declarations,
                linked.bindings + binding.copy(sourceUnitId = SourceUnitId("missing")),
                linked.diagnostics,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ProjectSemanticGraphSnapshot.canonical(
                linked.graphId,
                linked.rootPackageId,
                linked.packages,
                linked.sourceUnits,
                linked.namespaces,
                linked.declarations,
                linked.bindings + binding.copy(resolvedDeclarationId = DeclarationId("missing")),
                linked.diagnostics,
            )
        }
    }

    private fun referenceFixture(): ReferenceFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val provider = sourceUnit(
            rootKey,
            "provider.athena",
            "provider",
            declarations(
                "provider.athena",
                """
                package com.root
                system Provider {
                  device PLC1 {}
                  port PLC1.out {}
                }
                """.trimIndent(),
            ),
        )
        val consumerDeclarations = declarations(
            "consumer.athena",
            """
            package com.root
            system Consumer {
              connect PLC1.out -> PLC1.out
            }
            """.trimIndent(),
        )
        val consumer = sourceUnit(rootKey, "consumer.athena", "consumer", consumerDeclarations)
        val namespace = namespace(rootKey, listOf("com", "root"), listOf(provider.sourceUnitId, consumer.sourceUnitId))
        val packages = listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList()))
        val sourceUnits = listOf(provider, consumer)
        val namespaces = listOf(namespace)
        return ReferenceFixture(
            indexedSnapshot(rootKey, packages, sourceUnits, namespaces),
            consumer.sourceUnitId,
            consumerEndpointSpans(consumerDeclarations),
        )
    }

    private fun indexedSnapshot(
        rootKey: PackageKey,
        packages: List<ProjectSemanticPackage>,
        sourceUnits: List<ProjectSemanticSourceUnit>,
        namespaces: List<ProjectSemanticNamespace>,
    ): ProjectSemanticGraphSnapshot {
        return ProjectSemanticDeclarationIndexer().index(snapshot(rootKey, packages, sourceUnits, namespaces))
    }

    private fun declarations(path: String, content: String): List<Declaration> {
        return assertIs<ParseSuccess>(AthenaLanguageParser().parse(path, content)).ast.declarations
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

    private fun consumerEndpointSpans(declarations: List<Declaration>): List<com.engineeringood.athena.language.SourceSpan> {
        val connection = declarations.filterIsInstance<com.engineeringood.athena.language.ConnectionDeclaration>().single()
        return listOf(connection.from.span, connection.to.span)
    }
}

private data class ReferenceFixture(
    val indexedSnapshot: ProjectSemanticGraphSnapshot,
    val consumerSourceUnitId: SourceUnitId,
    val consumerEndpointSpans: List<com.engineeringood.athena.language.SourceSpan>,
)
