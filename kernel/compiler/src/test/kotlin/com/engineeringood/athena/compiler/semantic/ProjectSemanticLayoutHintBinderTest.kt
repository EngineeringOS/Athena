package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProjectSemanticLayoutHintBinderTest {
    @Test
    fun `binds known layout hint subjects and reports unknown authored references`() {
        val content =
            """
            package com.root
            system Root {
              device PLC1 {}
              device HMI1 {}
              layout schematic-sheet {
                place HMI1 near PLC1
                place XT1 below PLC1
              }
            }
            """.trimIndent()
        val parsedDeclarations = declarations("layout-bind.athena", content)
        val layoutDeclaration = parsedDeclarations.last()
        val fixture = semanticFixture("layout-bind.athena", content, parsedDeclarations)
        val indexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)

        val bound = ProjectSemanticLayoutHintBinder().bind(indexed)

        assertEquals(
            listOf("HMI1", "PLC1", "PLC1"),
            bound.bindings.map { binding ->
                indexed.declarations.single { declaration -> declaration.declarationId == binding.resolvedDeclarationId }
                    .qualifiedAuthoredName
                    .single()
            },
        )
        assertEquals(listOf("semantic.layout.reference.unknown"), bound.diagnostics.map { it.code.value })
        assertEquals("Unknown layout reference `XT1` in `schematic-sheet`.", bound.diagnostics.single().message)
        assertEquals(fixture.sourceUnitId, bound.diagnostics.single().sourceUnitId)
        assertEquals(layoutDeclaration.span.start.line + 2, bound.diagnostics.single().sourceSpan?.start?.line)
    }

    @Test
    fun `reports duplicate and contradictory layout hints without blocking constraint lowering`() {
        val content =
            """
            package com.root
            system Root {
              device PLC1 {}
              device HMI1 {}
              layout schematic-sheet {
                place HMI1 near PLC1
                place HMI1 near PLC1
                place HMI1 below PLC1
              }
            }
            """.trimIndent()
        val fixture = semanticFixture("layout-conflict.athena", content, declarations("layout-conflict.athena", content))
        val indexed = ProjectSemanticDeclarationIndexer().index(fixture.snapshot)

        val bound = ProjectSemanticLayoutHintBinder().bind(indexed)
        val constraints = ProjectSemanticLayoutConstraintLowerer().lower(bound)

        assertEquals(
            listOf(
                "semantic.layout.hint.contradictory",
                "semantic.layout.hint.duplicate",
            ),
            bound.diagnostics.map { diagnostic -> diagnostic.code.value },
        )
        assertEquals(
            "Contradictory layout hints for `HMI1` -> `PLC1` in `schematic-sheet`: near, below at priority PREFERENCE.",
            bound.diagnostics.first { diagnostic -> diagnostic.code.value == "semantic.layout.hint.contradictory" }.message,
        )
        assertEquals(3, constraints.constraints.size)
    }

    private fun declarations(path: String, content: String): List<Declaration> {
        return assertIs<ParseSuccess>(AthenaLanguageParser().parse(path, content)).ast.declarations
    }

    private fun semanticFixture(
        path: String,
        content: String,
        declarations: List<Declaration>,
    ): SemanticFixture {
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
        return SemanticFixture(snapshot, sourceUnitId)
    }
}

private data class SemanticFixture(
    val snapshot: ProjectSemanticGraphSnapshot,
    val sourceUnitId: SourceUnitId,
)
