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
import kotlin.test.assertTrue

class ProjectSemanticDeclarationIndexerTest {
    @Test
    fun `indexes authored device and port declarations into their semantic namespace`() {
        val fixture = declarationFixture()

        val indexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)
        val compilerIndexed = AthenaCompiler().indexProjectSemanticDeclarations(fixture.snapshot)
        val rootNamespace = indexed.namespaces.single { it.packageKey == fixture.rootKey }

        assertEquals(indexed.declarations, compilerIndexed.declarations)
        assertEquals(
            listOf(
                "port:PLC1.out",
                "device:PLC1",
            ),
            indexed.declarations.map { "${it.kind}:${it.qualifiedAuthoredName.joinToString(".")}" },
        )
        assertEquals(indexed.declarations.map { it.declarationId }, rootNamespace.declarationIds)
        assertEquals(emptyList(), indexed.diagnostics)
        indexed.declarations.forEach { declaration ->
            assertEquals(rootNamespace.namespaceId, declaration.namespaceId)
            assertEquals(fixture.rootSourceUnitId, declaration.sourceUnitId)
        }
    }

    @Test
    fun `keeps declaration records deterministic from reversed raw collections`() {
        val fixture = declarationFixture()
        val reversed = snapshot(
            fixture.rootKey,
            fixture.packages.reversed(),
            fixture.sourceUnits.reversed(),
            fixture.namespaces.reversed(),
        )

        val forwardIndexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)
        val reversedIndexed = ProjectSemanticDeclarationIndexer().index(reversed)

        assertEquals(forwardIndexed.namespaces, reversedIndexed.namespaces)
        assertEquals(forwardIndexed.declarations, reversedIndexed.declarations)
        assertEquals(forwardIndexed.diagnostics, reversedIndexed.diagnostics)
    }

    @Test
    fun `reports duplicate authored declarations deterministically`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val duplicateDeclarations = declarations(
            "duplicates.athena",
            """
            package com.root
            system Root {
              device PLC1 {}
              device PLC1 {}
            }
            """.trimIndent(),
        )
        val source = sourceUnit(rootKey, "duplicates.athena", "duplicates", duplicateDeclarations)
        val namespace = namespace(rootKey, listOf("com", "root"), listOf(source.sourceUnitId))

        val indexed = ProjectSemanticDeclarationIndexer().index(
            snapshot(rootKey, listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList())), listOf(source), listOf(namespace)),
        )

        assertEquals(1, indexed.declarations.size)
        assertEquals(
            listOf("semantic.declaration.duplicate"),
            indexed.diagnostics.map { it.code.value },
        )
        assertEquals(source.sourceUnitId, indexed.diagnostics.single().sourceUnitId)
        assertEquals(duplicateDeclarations.last().span, indexed.diagnostics.single().sourceSpan)
    }

    @Test
    fun `reports ambiguous declarations across source units in the same namespace`() {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val firstSource = sourceUnit(
            rootKey,
            "a.athena",
            "a",
            declarations("a.athena", "package com.root\nsystem A {\n  device PLC1 {}\n}"),
        )
        val secondDeclarations = declarations("b.athena", "package com.root\nsystem B {\n  device PLC1 {}\n}")
        val secondSource = sourceUnit(rootKey, "b.athena", "b", secondDeclarations)
        val namespace = namespace(rootKey, listOf("com", "root"), listOf(firstSource.sourceUnitId, secondSource.sourceUnitId))

        val indexed = ProjectSemanticDeclarationIndexer().index(
            snapshot(
                rootKey,
                listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList())),
                listOf(secondSource, firstSource),
                listOf(namespace),
            ),
        )

        assertEquals(2, indexed.declarations.size)
        assertEquals(
            listOf("semantic.declaration.ambiguous"),
            indexed.diagnostics.map { it.code.value },
        )
        assertEquals(secondSource.sourceUnitId, indexed.diagnostics.single().sourceUnitId)
        assertEquals(secondDeclarations.single().span, indexed.diagnostics.single().sourceSpan)
    }

    @Test
    fun `canonical snapshot rejects declarations outside known namespaces and source units`() {
        val fixture = declarationFixture()
        val indexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)
        val declaration = indexed.declarations.single { it.kind == "device" }

        assertFailsWith<IllegalArgumentException> {
            ProjectSemanticGraphSnapshot.canonical(
                indexed.graphId,
                indexed.rootPackageId,
                indexed.packages,
                indexed.sourceUnits,
                indexed.namespaces,
                indexed.declarations + declaration.copy(namespaceId = NamespaceId("missing")),
                indexed.bindings,
                indexed.diagnostics,
            )
        }
    }

    private fun declarationFixture(): DeclarationFixture {
        val rootId = PackageIdentifier("com.root", "1")
        val rootKey = CanonicalSemanticIdentityBuilder.packageKey(rootId)
        val rootDeclarations = declarations(
            "main.athena",
            """
            package com.root
            system Root {
              device PLC1 {}
              port PLC1.out {}
              connect PLC1.out -> PLC1.out
            }
            """.trimIndent(),
        )
        val source = sourceUnit(rootKey, "main.athena", "root", rootDeclarations)
        val namespace = namespace(rootKey, listOf("com", "root"), listOf(source.sourceUnitId))
        val packages = listOf(ProjectSemanticPackage(rootId, rootKey, "src", emptyList()))
        val sourceUnits = listOf(source)
        val namespaces = listOf(namespace)
        return DeclarationFixture(
            snapshot(rootKey, packages, sourceUnits, namespaces),
            rootKey,
            source.sourceUnitId,
            packages,
            sourceUnits,
            namespaces,
        )
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
}

private data class DeclarationFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
    val rootKey: PackageKey,
    val rootSourceUnitId: SourceUnitId,
    val packages: List<ProjectSemanticPackage>,
    val sourceUnits: List<ProjectSemanticSourceUnit>,
    val namespaces: List<ProjectSemanticNamespace>,
)
