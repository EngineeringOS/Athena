package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority.PREFERENCE
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutAxis
import com.engineeringood.athena.layout.LayoutConstraintKind
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProjectSemanticLayoutConstraintLowererTest {
    @Test
    fun `lowers admitted layout hints into deterministic governed constraints`() {
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
                align HMI1 aligned-with PLC1 axis vertical
                group HMI1 grouped-with PLC1
              }
            }
            """.trimIndent()
        val fixture = semanticFixture("layout-constraints.athena", content, declarations("layout-constraints.athena", content))
        val indexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)
        val bound = ProjectSemanticLayoutHintBinder().bind(indexed)

        val snapshot = ProjectSemanticLayoutConstraintLowerer().lower(bound)

        assertEquals(ElectricalProjectionFamily.SCHEMATIC, snapshot.family)
        assertEquals(
            listOf(
                LayoutConstraintKind.NEAR,
                LayoutConstraintKind.BELOW,
                LayoutConstraintKind.ALIGNED_WITH,
                LayoutConstraintKind.GROUPED_WITH,
            ),
            snapshot.constraints.map { constraint -> constraint.kind },
        )
        assertEquals(List(4) { PREFERENCE }, snapshot.constraints.map { constraint -> constraint.authoredPriority })
        assertEquals(List(4) { "schematic-sheet" }, snapshot.constraints.map { constraint -> constraint.subject.viewId })
        assertEquals(List(4) { snapshot.snapshotId }, snapshot.constraints.map { constraint -> constraint.snapshotId })
        assertEquals(LayoutAxis.VERTICAL, snapshot.constraints[1].axis)
        assertEquals(LayoutAxis.VERTICAL, snapshot.constraints[2].axis)
        assertEquals(7, snapshot.constraints.first().subject.sourceSpan?.startLine)
        assertEquals(emptyList(), bound.diagnostics)
    }

    private fun declarations(path: String, content: String): List<Declaration> {
        return assertIs<ParseSuccess>(AthenaLanguageParser().parse(path, content)).ast.declarations
    }

    private fun semanticFixture(
        path: String,
        content: String,
        declarations: List<Declaration>,
    ): ConstraintFixture {
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
        return ConstraintFixture(snapshot)
    }
}

private data class ConstraintFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
)
