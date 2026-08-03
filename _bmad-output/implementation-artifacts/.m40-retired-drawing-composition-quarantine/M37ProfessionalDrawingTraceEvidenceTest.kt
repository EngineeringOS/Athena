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
import com.engineeringood.athena.routing.DrawingBounds
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class M37ProfessionalDrawingTraceEvidenceTest {
    @Test
    fun `professional drawing exposes computed source route label policy and package trace`() {
        val repoRoot = repositoryRoot()
        val projectRoot = repoRoot.resolve("examples/m37/professional-control-drawing")
        val sourcePath = projectRoot.resolve(
            "src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val sourceText = Files.readString(sourcePath)
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val result = AthenaProfessionalDrawingCompiler().compile(
            AthenaProfessionalDrawingRequest(
                repositoryRoot = projectRoot,
                document = compilation.document,
                semanticSnapshot = semanticSnapshot(compilation, sourceText),
                policy = AthenaProfessionalDrawingPolicy.rollingShutterControlDrawing(),
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val trace = result.evidence.trace
        assertFalse(trace.occurrences.isEmpty())
        assertFalse(trace.routes.isEmpty())
        assertFalse(trace.connectionLabels.isEmpty())
        assertFalse(trace.sheetStructures.isEmpty())
        assertTrue(trace.evidenceInputs.all { input -> input.evidenceIds.isNotEmpty() && !input.constant })
        assertTrue(trace.forbiddenAuthorityKinds.isEmpty())

        val route = trace.routes.single { route -> route.connectionId.endsWith(":power_TerminalX37_motorDownOut_to_MotorM37_down") }
        assertTrue(route.routeId.startsWith("route:"))
        assertTrue(route.routeContractId.contains("power_TerminalX37_motorDownOut_to_MotorM37_down"))
        assertTrue(route.sourcePortSemanticId.contains("TerminalX37.motorDownOut"))
        assertTrue(route.targetPortSemanticId.contains("MotorM37.down"))
        assertTrue(route.laneId.startsWith("lane:"))
        assertTrue(route.routeLabelIds.isNotEmpty())
        assertTrue(route.lineClassId == "line:power")
        assertTrue(route.compilerSnapshotId.isNotBlank())
        assertTrue(route.sourceSpan.file.endsWith("01-professional-control-drawing.athena"))

        val occurrence = trace.occurrences.single { occurrence -> occurrence.semanticSubjectId == "component:MotorM37" }
        assertTrue(occurrence.packageId.contains("m37"))
        assertTrue(occurrence.definitionId == "m37.motor.element")
        assertTrue(occurrence.packageResourceIds.any { resource -> resource.contains("m37-surface-elements.athena") })
        assertTrue(occurrence.anchorIds.any { anchor -> anchor == "down" })

        val label = trace.connectionLabels.single { label -> label.routeId == route.routeId }
        assertTrue(label.labelId.startsWith("label:"))
        assertTrue(label.bounds.width > 0 && label.bounds.height > 0)
        assertTrue(label.sourceSpan.file.endsWith("01-professional-control-drawing.athena"))

        val payloadText = trace.toString()
        listOf("<svg", "<xml", "<definition", ".elmt", "qelectrotech", "org.eclipse.elk", "DOM").forEach { forbidden ->
            assertFalse(payloadText.contains(forbidden, ignoreCase = true), "Trace must not leak `$forbidden`.")
        }
        assertNotNull(result.presentation)
    }

    @Test
    fun `professional drawing fails closed when route trace evidence is missing`() {
        val repoRoot = repositoryRoot()
        val projectRoot = repoRoot.resolve("examples/m37/professional-control-drawing")
        val sourcePath = projectRoot.resolve(
            "src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val sourceText = Files.readString(sourcePath)
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(sourcePath))
        val result = AthenaProfessionalDrawingCompiler().compile(
            AthenaProfessionalDrawingRequest(
                repositoryRoot = projectRoot,
                document = compilation.document.copy(connections = emptyList()),
                semanticSnapshot = semanticSnapshot(compilation, sourceText),
                policy = AthenaProfessionalDrawingPolicy.rollingShutterControlDrawing(),
            ),
        )

        assertNull(result.presentation)
        assertTrue(result.diagnostics.any { diagnostic -> diagnostic.code == "drawing.trace.missing" })
        assertTrue(result.evidence.trace.occurrences.isNotEmpty())
        assertTrue(result.evidence.trace.routes.isEmpty())
    }

    @Test
    fun `trace validator rejects missing class label package resource raw authority and constant evidence input`() {
        val trace = validTrace()
        val corrupted = trace.copy(
            occurrences = trace.occurrences.map { occurrence ->
                occurrence.copy(packageResourceIds = emptyList(), sourceSpan = occurrence.sourceSpan.copy(file = ""))
            },
            routes = trace.routes.map { route ->
                route.copy(routeContractId = "", laneId = "", routeLabelIds = emptyList(), lineClassId = "")
            },
            evidenceInputs = listOf(AthenaProfessionalEvidenceInputTrace("evidence:constant", emptyList(), emptyList(), constant = true)),
            forbiddenAuthorityKinds = listOf("qelectrotech"),
        )

        val codes = AthenaProfessionalDrawingTraceValidator.validate(corrupted).map { diagnostic -> diagnostic.code }.toSet()

        assertTrue("drawing.trace.source-missing" in codes)
        assertTrue("drawing.trace.route-fact-missing" in codes)
        assertTrue("drawing.trace.label-fact-missing" in codes)
        assertTrue("drawing.trace.presentation-class-missing" in codes)
        assertTrue("drawing.trace.package-resource-missing" in codes)
        assertTrue("drawing.trace.raw-authority" in codes)
        assertTrue("drawing.evidence.constant-forbidden" in codes)
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

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }

    private fun validTrace(): AthenaProfessionalDrawingTrace {
        val span = AthenaProfessionalSourceSpan("source.athena", 1, 1, 1, 10)
        return AthenaProfessionalDrawingTrace(
            occurrences = listOf(
                AthenaProfessionalOccurrenceTrace(
                    occurrenceId = "occurrence:a",
                    semanticSubjectId = "component:A",
                    physicalComponentId = "component:A",
                    functionId = null,
                    packageId = "pkg",
                    definitionId = "element:a",
                    bindingRuleId = "binding:a",
                    packageResourceIds = listOf("element.athena"),
                    anchorIds = listOf("left"),
                    labelIds = listOf("label:a"),
                    sourceSpan = span,
                ),
            ),
            routes = listOf(
                AthenaProfessionalRouteTrace(
                    routeId = "route:a",
                    connectionId = "connection:a",
                    routeContractId = "route-contract:a",
                    sourcePortSemanticId = "port:a",
                    targetPortSemanticId = "port:b",
                    sourceAnchorId = "anchor:a",
                    targetAnchorId = "anchor:b",
                    laneId = "lane:a",
                    routeLabelIds = listOf("label:route:a"),
                    lineClassId = "line:power",
                    projectionPolicyId = "policy:a",
                    compilerSnapshotId = "snapshot:a",
                    sourceSpan = span,
                ),
            ),
            connectionLabels = listOf(
                AthenaProfessionalRouteLabelTrace(
                    labelId = "label:route:a",
                    routeId = "route:a",
                    bounds = DrawingBounds(1, 1, 10, 10),
                    sourceSpan = span,
                ),
            ),
            sheetStructures = listOf(
                AthenaProfessionalSheetStructureTrace(
                    structureId = "sheet:a",
                    kind = "drawing-region",
                    memberIds = listOf("occurrence:a"),
                    sourceSpan = span,
                ),
            ),
            evidenceInputs = listOf(
                AthenaProfessionalEvidenceInputTrace(
                    evidenceId = "evidence:a",
                    evidenceIds = listOf("route:a"),
                    diagnosticCodes = emptyList(),
                ),
            ),
        )
    }
}
