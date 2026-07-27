package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.DrawingGridPosition
import com.engineeringood.athena.layout.LayoutConstraintKind
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34FunctionPlacementCompilerTest {
    @Test
    fun `lowers function placement as one hard grid constraint with physical and functional identity`() {
        val content = validSource()
        val fixture = semanticFixture("function-placement.athena", content, declarations(content))
        val indexed = ProjectSemanticDeclarationIndexer().index(fixture)
        val bound = ProjectSemanticLayoutHintBinder().bind(indexed)

        val constraints = ProjectSemanticLayoutConstraintLowerer().lower(bound)
        val placement = constraints.constraints.single()
        val facts = ProjectSemanticSchematicLayoutFactDeriver().derive(bound).placementFacts.single()

        assertEquals(emptyList(), bound.diagnostics)
        assertEquals(LayoutConstraintKind.AT_GRID, placement.kind)
        assertEquals(AuthoredLayoutIntentPriority.HARD, placement.authoredPriority)
        assertEquals(DrawingGridPosition(7, 4), placement.gridPosition)
        assertEquals(LayoutOrientation.VERTICAL, placement.orientation)
        assertNotNull(placement.subject.functionId)
        assertTrue(placement.subject.subjectId.value.contains("device"))
        assertTrue(placement.subject.functionId!!.value.contains("function"))
        assertEquals(placement.gridPosition, facts.gridPosition)
        assertEquals(placement.orientation, facts.orientation)
        assertEquals(placement.subject.subjectId, facts.subjectId)
        assertEquals(placement.subject.functionId, facts.functionId)
        assertEquals(19, placement.subject.sourceSpan?.startLine)
    }

    @Test
    fun `diagnoses conflicting hard placements at the later authored source span`() {
        val content = validSource().replace(
            "place KM1.coil at (7, 4) orientation vertical",
            """
            place KM1.coil at (7, 4) orientation vertical
                place KM1.coil at (8, 4) orientation vertical
            """.trimIndent(),
        )
        val fixture = semanticFixture("conflicting-placement.athena", content, declarations(content))
        val bound = ProjectSemanticLayoutHintBinder().bind(ProjectSemanticDeclarationIndexer().index(fixture))

        val diagnostic = bound.diagnostics.single { it.code.value == "semantic.layout.placement.conflicting" }

        assertEquals(20, diagnostic.sourceSpan?.start?.line)
        assertEquals(ProjectSemanticDiagnosticSeverity.ERROR, diagnostic.severity)
    }

    @Test
    fun `diagnoses duplicate hard placement and drawing grid cell collision`() {
        val content = twoDeviceSource(
            """
            place KM1 at (2, 3) orientation horizontal
            place KM1 at (2, 3) orientation horizontal
            place KM2 at (2, 3) orientation vertical
            """.trimIndent(),
        )
        val fixture = semanticFixture("duplicate-cell-placement.athena", content, declarations(content))
        val bound = ProjectSemanticLayoutHintBinder().bind(ProjectSemanticDeclarationIndexer().index(fixture))

        assertEquals(
            listOf("semantic.layout.placement.cell-collision", "semantic.layout.placement.duplicate"),
            bound.diagnostics.map { diagnostic -> diagnostic.code.value }.sorted(),
        )
        assertTrue(bound.diagnostics.all { diagnostic -> diagnostic.sourceSpan != null })
    }

    @Test
    fun `diagnoses provable hard placement conflict with relative placement`() {
        val content = twoDeviceSource(
            """
            place KM1 at (2, 2) orientation horizontal
            place KM2 at (2, 3) orientation horizontal
            place KM1 below KM2
            """.trimIndent(),
        )
        val fixture = semanticFixture("relative-placement-conflict.athena", content, declarations(content))
        val bound = ProjectSemanticLayoutHintBinder().bind(ProjectSemanticDeclarationIndexer().index(fixture))

        val diagnostic = bound.diagnostics.single { it.code.value == "semantic.layout.placement.relative-conflict" }

        assertEquals(ProjectSemanticDiagnosticSeverity.ERROR, diagnostic.severity)
        assertTrue(diagnostic.message.contains("below"))
        assertNotNull(diagnostic.sourceSpan)
    }

    private fun validSource(): String =
        """
        package com.root
        system Root {
          device KM1 {
            type Contactor
            port A1 {
              direction in
              signal Control
            }
            port A2 {
              direction out
              signal Control
            }
            function coil {
              role coil
              ports (A1, A2)
            }
          }
          layout schematic {
            place KM1.coil at (7, 4) orientation vertical
          }
        }
        """.trimIndent()

    private fun twoDeviceSource(layoutStatements: String): String =
        """
        package com.root
        system Root {
          device KM1 { type Contactor }
          device KM2 { type Contactor }
          layout schematic {
            ${layoutStatements.prependIndent("    ").trimStart()}
          }
        }
        """.trimIndent()

    private fun declarations(content: String): List<Declaration> =
        assertIs<ParseSuccess>(AthenaLanguageParser().parse("function-placement.athena", content)).ast.declarations

    private fun semanticFixture(
        path: String,
        content: String,
        declarations: List<Declaration>,
    ): ProjectSemanticGraphSnapshot {
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
        return ProjectSemanticGraphSnapshot.canonical(
            graphId = CanonicalSemanticIdentityBuilder.graphId(
                packageKey,
                listOf(GraphPackageIdentity(packageKey, "src", emptyList())),
                listOf(source.contentIdentity),
            ),
            rootPackageId = packageKey,
            packages = listOf(ProjectSemanticPackage(packageId, packageKey, "src", emptyList())),
            sourceUnits = listOf(source),
            namespaces = listOf(
                ProjectSemanticNamespace(
                    namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(packageKey, listOf("com", "root")),
                    packageKey = packageKey,
                    qualifiedName = listOf("com", "root"),
                    sourceUnitIds = listOf(sourceUnitId),
                    declarationIds = emptyList(),
                ),
            ),
            declarations = emptyList(),
            bindings = emptyList(),
            diagnostics = emptyList(),
        )
    }
}
