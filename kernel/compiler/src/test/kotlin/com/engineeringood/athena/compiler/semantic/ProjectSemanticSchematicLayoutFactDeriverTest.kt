package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.layout.LayoutConstraintKind
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectSemanticSchematicLayoutFactDeriverTest {
    @Test
    fun `feeds admitted layout constraints into deterministic schematic layout facts`() {
        val content =
            """
            package com.root
            system Root {
              device PLC1 {}
              device HMI1 {}
              device XT1 {}
              layout schematic-sheet {
                place HMI1 near PLC1
                place XT1 below PLC1
                group HMI1 grouped-with PLC1
              }
            }
            """.trimIndent()
        val fixture = semanticFixture("layout-facts.athena", content, declarations("layout-facts.athena", content))
        val bound = ProjectSemanticLayoutHintBinder().bind(ProjectSemanticDeclarationIndexer().index(fixture.snapshot))

        val deriver = ProjectSemanticSchematicLayoutFactDeriver()
        val first = deriver.derive(bound)
        val second = deriver.derive(bound)

        assertEquals(first, second)
        assertEquals(
            listOf(LayoutConstraintKind.NEAR, LayoutConstraintKind.BELOW, LayoutConstraintKind.GROUPED_WITH),
            deriver.constraints(bound).constraints.map { constraint -> constraint.kind },
        )
        assertEquals(3, first.placementFacts.size)
        assertEquals(3, first.appliedConstraintIds.size)
        assertTrue(first.groupFacts.isNotEmpty(), "Grouped-with constraints should surface as layout group facts.")
    }

    private fun declarations(path: String, content: String): List<Declaration> {
        return assertIs<ParseSuccess>(AthenaLanguageParser().parse(path, content)).ast.declarations
    }

    private fun semanticFixture(
        path: String,
        content: String,
        declarations: List<Declaration>,
    ): LayoutFactFixture {
        val packageId = PackageIdentifier("com.root", "1")
        val packageKey = CanonicalSemanticIdentityBuilder.packageKey(packageId)
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, path)
        val source = ProjectSemanticSourceUnit(
            sourceUnitId = sourceUnitId,
            packageKey = packageKey,
            sourceRootRelativePath = path,
            contentIdentity = CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, content),
            authoredDeclarations = declarations,
        )
        val namespace = ProjectSemanticNamespace(
            namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(packageKey, listOf("com", "root")),
            packageKey = packageKey,
            qualifiedName = listOf("com", "root"),
            sourceUnitIds = listOf(sourceUnitId),
            declarationIds = emptyList(),
        )
        val pkg = ProjectSemanticPackage(packageId, packageKey, "src", emptyList())
        val snapshot = ProjectSemanticGraphSnapshot.canonical(
            graphId = CanonicalSemanticIdentityBuilder.graphId(
                packageKey,
                listOf(GraphPackageIdentity(packageKey, "src", emptyList())),
                listOf(source.contentIdentity),
            ),
            rootPackageId = packageKey,
            packages = listOf(pkg),
            sourceUnits = listOf(source),
            namespaces = listOf(namespace),
            declarations = emptyList(),
            bindings = emptyList(),
            diagnostics = emptyList(),
        )
        return LayoutFactFixture(snapshot)
    }
}

private data class LayoutFactFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
)
