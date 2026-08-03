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

class M37ProfessionalDrawingSurfaceTest {
    @Test
    fun `compiles dedicated M37 source into governed professional drawing surface`() {
        val repoRoot = repositoryRoot()
        val projectRoot = repoRoot.resolve("examples/m37/professional-control-drawing")
        val sourcePath = projectRoot.resolve(
            "src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val sourceText = Files.readString(sourcePath)
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val semanticSnapshot = semanticSnapshot(compilation, sourceText)

        val result = AthenaProfessionalDrawingCompiler().compile(
            AthenaProfessionalDrawingRequest(
                repositoryRoot = projectRoot,
                document = compilation.document,
                semanticSnapshot = semanticSnapshot,
                policy = AthenaProfessionalDrawingPolicy.rollingShutterControlDrawing(),
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val presentation = assertNotNull(result.presentation)
        val composition = assertNotNull(presentation.drawingComposition)

        assertEquals("schematic", presentation.view.id)
        assertEquals(setOf("author", "title", "file", "date", "folio"), composition.title.fields.map { it.fieldId }.toSet())
        assertTrue(
            composition.title.fields.single { it.fieldId == "file" }.value.contains("examples/m37/professional-control-drawing"),
            "Title block must trace the active M37 source, not a hardcoded legacy source.",
        )
        assertTrue(composition.coordinateZones.any { it.axis == "COLUMN" })
        assertTrue(composition.coordinateZones.any { it.axis == "ROW" })
        assertTrue(
            composition.structureFacts.map { it.kind }.toSet().containsAll(
                setOf("drawing-region", "rail", "lane", "terminal-strip", "label-band", "route-channel"),
            ),
        )

        val labels = presentation.graphicOccurrences.map { it.deviceLabel }.toSet()
        assertTrue(labels.any { it.contains("PE37") }, "Surface must include protective earth occurrence.")
        assertTrue(labels.any { it.contains("K37") }, "Surface must include relay/contact occurrence.")
        assertTrue(labels.any { it.contains("H37") }, "Surface must include indicator occurrence.")
        assertTrue(labels.any { it.contains("X37") || it.contains("T37") }, "Surface must include terminal group occurrence.")
        assertTrue(presentation.graphicOccurrences.all { occurrence ->
            occurrence.authorities.graphic == "graphic-primitive-ir" &&
                occurrence.graphic.primitives.isNotEmpty() &&
                occurrence.terminalBindings.isNotEmpty() &&
                composition.drawingAreaBounds.contains(occurrence.bounds)
        })

        assertTrue(presentation.connectors.isNotEmpty())
        assertTrue(presentation.connectors.all { connector ->
            connector.routeId.isNotBlank() &&
                connector.quality == "SATISFIED" &&
                connector.laneRouteIds.isNotEmpty() &&
                connector.sourceEndpoint.point == connector.routePoints.first() &&
                connector.targetEndpoint.point == connector.routePoints.last() &&
                connector.routePoints.zipWithNext().all { (start, end) -> start.x == end.x || start.y == end.y }
        })
        assertTrue(presentation.connectionMarkers.isNotEmpty())
        assertTrue(presentation.connectionMarkers.all { marker ->
            marker.routeIds.isNotEmpty() && marker.connectorIds.isNotEmpty() && marker.sourceProjectionIds.isNotEmpty()
        })

        assertTrue(result.evidence.exactTerminalAttachment)
        assertTrue(result.evidence.componentAndLabelClearance)
        assertTrue(result.evidence.junctionCrossingSemanticsExplicit)
        assertTrue(result.evidence.graphicPrimitiveAuthorityOnly)
        assertTrue(result.evidence.rawMarkupAuthorityAbsent)
        assertTrue(result.evidence.fallbackAuthorityAbsent)
        assertTrue(result.evidence.connectionPresentationClassified)
        assertTrue(result.evidence.looseEndpointsAbsent)
        assertTrue(result.evidence.routeBodyIntersectionsAbsent)
        assertTrue(result.evidence.ambiguousCrossingsAbsent)
        assertTrue(result.evidence.labelCollisionsAbsent)
        assertTrue(result.evidence.unclassifiedRoutesAbsent)
        assertFalse(result.evidence.rendererEngineeringInference)
    }

    private fun semanticSnapshot(
        compilation: CompilerCompilationSuccess,
        sourceContent: String,
    ): ProjectSemanticGraphSnapshot {
        val packageId = PackageIdentifier("com.engineeringood.m37.professionalcontroldrawing", "1.0.0")
        val packageKey = CanonicalSemanticIdentityBuilder.packageKey(packageId)
        val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "01-professional-control-drawing.athena")
        val source = ProjectSemanticSourceUnit(
            sourceUnitId = sourceUnitId,
            packageKey = packageKey,
            sourceRootRelativePath = "01-professional-control-drawing.athena",
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
                        listOf("com", "engineeringood", "m37", "professionalcontroldrawing"),
                    ),
                    packageKey = packageKey,
                    qualifiedName = listOf("com", "engineeringood", "m37", "professionalcontroldrawing"),
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
}
