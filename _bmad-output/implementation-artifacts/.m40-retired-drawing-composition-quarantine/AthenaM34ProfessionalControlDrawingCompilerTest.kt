package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.CanonicalSemanticIdentityBuilder
import com.engineeringood.athena.compiler.semantic.GraphPackageIdentity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLayoutHintBinder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticNamespace
import com.engineeringood.athena.compiler.semantic.ProjectSemanticPackage
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceUnit
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34ProfessionalControlDrawingCompilerTest {
    @Test
    fun `rejects the stale M34 professional sample under current drawing gates`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val sourcePath = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val semanticSnapshot = semanticSnapshot(compilation, Files.readString(sourcePath))

        val result = AthenaProfessionalDrawingCompiler().compile(
            AthenaProfessionalDrawingRequest(
                repositoryRoot = projectRoot,
                document = compilation.document,
                semanticSnapshot = semanticSnapshot,
                policy = AthenaProfessionalDrawingPolicy.rollingShutterControlDrawing(),
            ),
        )

        assertTrue(result.presentation == null)
        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(
            result.diagnostics.any { diagnostic ->
                diagnostic.code == "drawing.label.collision.component" ||
                    diagnostic.code == "drawing.route.body-intersection"
            },
            result.diagnostics.joinToString("\n"),
        )
        assertFalse(result.evidence.componentAndLabelClearance)
        assertFalse(result.evidence.routeBodyIntersectionsAbsent)
    }

    @Test
    fun `professional drawing output is deterministic when input facts are shuffled`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val sourcePath = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val semanticSnapshot = semanticSnapshot(compilation, Files.readString(sourcePath))
        val compiler = AthenaProfessionalDrawingCompiler()
        val policy = AthenaProfessionalDrawingPolicy.rollingShutterControlDrawing()

        val canonical = compiler.compile(
            AthenaProfessionalDrawingRequest(projectRoot, compilation.document, semanticSnapshot, policy),
        )
        val shuffled = compiler.compile(
            AthenaProfessionalDrawingRequest(
                projectRoot,
                compilation.document.copy(
                    components = compilation.document.components.reversed(),
                    ports = compilation.document.ports.reversed(),
                    connections = compilation.document.connections.reversed(),
                    functions = compilation.document.functions.reversed(),
                ),
                semanticSnapshot.withSourceUnitsReversed(),
                policy,
            ),
        )

        assertEquals(canonical, shuffled)
    }
}

private fun semanticSnapshot(
    compilation: CompilerCompilationSuccess,
    sourceContent: String,
): ProjectSemanticGraphSnapshot {
    val packageId = PackageIdentifier("com.engineeringood.m34.professional", "1.0.0")
    val packageKey = CanonicalSemanticIdentityBuilder.packageKey(packageId)
    val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "01-control-drawing.athena")
    val source = ProjectSemanticSourceUnit(
        sourceUnitId = sourceUnitId,
        packageKey = packageKey,
        sourceRootRelativePath = "01-control-drawing.athena",
        contentIdentity = CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, sourceContent),
        authoredDeclarations = compilation.source.ast.declarations,
    )
    val snapshot = ProjectSemanticGraphSnapshot.canonical(
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
                namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(
                    packageKey,
                    listOf("com", "engineeringood", "m34", "professional"),
                ),
                packageKey = packageKey,
                qualifiedName = listOf("com", "engineeringood", "m34", "professional"),
                sourceUnitIds = listOf(sourceUnitId),
                declarationIds = emptyList(),
            ),
        ),
        declarations = emptyList(),
        bindings = emptyList(),
        diagnostics = emptyList(),
    )
    return ProjectSemanticLayoutHintBinder().bind(ProjectSemanticDeclarationIndexer().index(snapshot))
}

private fun ProjectSemanticGraphSnapshot.withSourceUnitsReversed(): ProjectSemanticGraphSnapshot =
    ProjectSemanticGraphSnapshot.canonical(
        graphId = graphId,
        rootPackageId = rootPackageId,
        packages = packages,
        sourceUnits = sourceUnits.reversed(),
        namespaces = namespaces,
        declarations = declarations,
        bindings = bindings,
        diagnostics = diagnostics,
    )

private fun com.engineeringood.athena.presentation.PresentationDrawingBounds.contains(
    point: com.engineeringood.athena.routing.SchematicRoutePoint,
): Boolean = point.x in x..(x + width) && point.y in y..(y + height)

private fun com.engineeringood.athena.presentation.PresentationDrawingBounds.contains(
    bounds: com.engineeringood.athena.presentation.PresentationDrawingBounds,
): Boolean = bounds.x >= x && bounds.y >= y &&
    bounds.x + bounds.width <= x + width && bounds.y + bounds.height <= y + height

private fun repositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (current.parent != null) {
        if (Files.exists(current.resolve("settings.gradle.kts"))) return current
        current = current.parent
    }
    error("Could not locate Athena repository root.")
}
