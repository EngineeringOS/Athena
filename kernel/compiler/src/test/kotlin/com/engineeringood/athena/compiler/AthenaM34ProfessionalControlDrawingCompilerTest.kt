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
    fun `compiles the governed rolling shutter project into one professional control drawing`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val sourcePath = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val semanticSnapshot = semanticSnapshot(compilation, Files.readString(sourcePath))

        val result = AthenaProfessionalDrawingCompiler().compile(
            AthenaProfessionalDrawingRequest(
                repositoryRoot = projectRoot,
                document = compilation.document,
                semanticSnapshot = semanticSnapshot,
                policy = AthenaProfessionalDrawingPolicy.m34RollingShutter(),
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val presentation = assertNotNull(result.presentation)
        val composition = assertNotNull(presentation.drawingComposition)
        assertEquals("schematic", presentation.view.id)
        assertEquals(1050, presentation.canvasWidth)
        assertEquals(720, presentation.canvasHeight)
        assertEquals(1050, composition.sheetBounds.width)
        assertEquals(720, composition.sheetBounds.height)
        assertEquals((1..17).map(Int::toString), composition.coordinateZones.filter { it.axis == "COLUMN" }.map { it.label })
        assertEquals(('A'..'H').map(Char::toString), composition.coordinateZones.filter { it.axis == "ROW" }.map { it.label })
        assertEquals(setOf("power-region", "control-region"), composition.structureFacts.filter { it.kind == "drawing-region" }.map { it.factId }.toSet())
        assertTrue(
            composition.structureFacts.map { it.kind }.toSet().containsAll(
                setOf("rail", "lane", "terminal-strip", "label-band", "route-channel"),
            ),
        )
        assertTrue(composition.referencePlacements.any { it.representationIdentity == "iec.folio-continuation-reference" })
        assertEquals(setOf("author", "title", "file", "date", "folio"), composition.title.fields.map { it.fieldId }.toSet())

        assertEquals(22, presentation.graphicOccurrences.size)
        assertEquals(22, presentation.graphicOccurrences.map { it.occurrenceId.value }.distinct().size)
        assertEquals(16, presentation.graphicOccurrences.map { it.physicalComponentId }.distinct().size)
        assertEquals(8, presentation.graphicOccurrences.count { it.functionId != null })
        assertEquals(
            setOf("G34", "QF34", "QS34", "T34", "KM1", "KM2", "S0", "S1", "S2", "SQ1", "SQ2", "HL1", "HL2", "XT1", "M34", "PE34"),
            presentation.graphicOccurrences.map { it.deviceLabel }.toSet(),
        )
        assertTrue(presentation.graphicOccurrences.all { occurrence ->
            occurrence.graphic.primitives.isNotEmpty() &&
                occurrence.packageId.isNotBlank() &&
                occurrence.definitionId.isNotBlank() &&
                occurrence.bindingRuleId.isNotBlank() &&
                occurrence.terminalBindings.all { terminal -> composition.drawingAreaBounds.contains(terminal.point) }
        })
        assertTrue(presentation.graphicOccurrences.all { occurrence -> composition.drawingAreaBounds.contains(occurrence.bounds) })

        val routes = assertNotNull(presentation.routeFactSnapshot)
        assertEquals(34, routes.routeFacts.size)
        assertEquals(34, routes.routeFacts.map { it.connectionId }.distinct().size)
        assertTrue(routes.routeFacts.all { route ->
            route.quality.isSatisfied &&
                route.source.point == route.segments.first().start &&
                route.target.point == route.segments.last().end &&
                route.segments.all { segment -> segment.start.x == segment.end.x || segment.start.y == segment.end.y }
        })
        assertTrue(routes.junctionFacts.isNotEmpty())
        assertTrue(routes.crossingFacts.all { crossing -> crossing.joined.not() })
        assertTrue(result.proof.exactTerminalAttachment)
        assertTrue(result.proof.componentAndLabelClearance)
        assertTrue(result.proof.junctionCrossingSemanticsExplicit)
        assertTrue(result.proof.graphicPrimitiveAuthorityOnly)
        assertTrue(result.proof.rawMarkupAuthorityAbsent)
        assertTrue(result.proof.fallbackAuthorityAbsent)
        assertFalse(result.proof.rendererEngineeringInference)
    }

    @Test
    fun `professional drawing output is deterministic when input facts are shuffled`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val sourcePath = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val semanticSnapshot = semanticSnapshot(compilation, Files.readString(sourcePath))
        val compiler = AthenaProfessionalDrawingCompiler()
        val policy = AthenaProfessionalDrawingPolicy.m34RollingShutter()

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
