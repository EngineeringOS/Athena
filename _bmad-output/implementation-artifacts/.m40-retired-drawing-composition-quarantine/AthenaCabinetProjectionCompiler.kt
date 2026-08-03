package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetCompositionPolicy
import com.engineeringood.athena.drawing.composition.CabinetCompositionCompiler
import com.engineeringood.athena.drawing.composition.CabinetCompositionRequest
import com.engineeringood.athena.drawing.composition.CabinetCompositionResult
import com.engineeringood.athena.drawing.composition.CabinetAnchorDirection
import com.engineeringood.athena.drawing.composition.CabinetConnectionEndpointBinding
import com.engineeringood.athena.drawing.composition.CabinetIntrinsicAnchor
import com.engineeringood.athena.drawing.composition.CabinetPhysicalOccurrenceInput
import com.engineeringood.athena.drawing.composition.CabinetPointD
import com.engineeringood.athena.drawing.composition.CabinetRectD
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceId
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceInput
import com.engineeringood.athena.drawing.composition.CabinetRouteEndpointRef
import com.engineeringood.athena.drawing.composition.CabinetRouteFact
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompilation
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompiler
import com.engineeringood.athena.drawing.composition.CabinetRoutingRequest
import com.engineeringood.athena.drawing.composition.CabinetSizeD
import com.engineeringood.athena.drawing.composition.CabinetTargetFrame
import com.engineeringood.athena.drawing.composition.CabinetVectorD
import com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompilation
import com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompiler
import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.InstallationChannelDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.InstallationDuctDeclaration
import com.engineeringood.athena.language.InstallationEnclosureDeclaration
import com.engineeringood.athena.language.InstallationKind
import com.engineeringood.athena.language.InstallationLengthLiteral
import com.engineeringood.athena.language.InstallationMountDeclaration
import com.engineeringood.athena.language.InstallationMountOrientation
import com.engineeringood.athena.language.InstallationOrientation
import com.engineeringood.athena.language.InstallationPointLiteral
import com.engineeringood.athena.language.InstallationRailDeclaration
import com.engineeringood.athena.language.InstallationRouteDeclaration
import com.engineeringood.athena.language.InstallationSize3Literal
import com.engineeringood.athena.language.InstallationSizeLiteral
import com.engineeringood.athena.language.InstallationSurfaceDeclaration
import com.engineeringood.athena.language.InstallationTerminalGroupDeclaration
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.LayoutGraph
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalConstraintEvaluator
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalContractSource
import com.engineeringood.athena.physical.PhysicalContractSourceKind
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalEnclosureIntent
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationContractFact
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractResolution
import com.engineeringood.athena.physical.PhysicalInstallationContractResolver
import com.engineeringood.athena.physical.PhysicalInstallationContractValue
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationIntent
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationTopologyCompilation
import com.engineeringood.athena.physical.PhysicalInstallationTopologyCompiler
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalMountedOccurrenceIntent
import com.engineeringood.athena.physical.PhysicalMountingSurfaceIntent
import com.engineeringood.athena.physical.PhysicalMountingSurface
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRailIntent
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRouteChannelIntent
import com.engineeringood.athena.physical.PhysicalRouteIntentSource
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalTerminalGroupIntent
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.physical.PhysicalVector2i
import com.engineeringood.athena.physical.RouteChannelTopologyCompiler
import com.engineeringood.athena.physical.RouteChannelTopologyCompilation
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingAuthorities
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationDrawingComposition
import com.engineeringood.athena.presentation.PresentationDrawingPoint
import com.engineeringood.athena.presentation.PresentationDrawingStructureFact
import com.engineeringood.athena.presentation.PresentationDrawingTitle
import com.engineeringood.athena.presentation.PresentationGraphicLabel
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationPlacedAnchor
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicTransform
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalConnectionEndpointKind
import com.engineeringood.athena.routing.ElectricalConnectionRoleClassifier
import com.engineeringood.athena.routing.ElectricalConnectionRoleInput
import com.engineeringood.athena.routing.ElectricalConnectionPortRef
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.DrawingProfileCompiler
import com.engineeringood.athena.routing.DrawingProfileResolution
import com.engineeringood.athena.routing.DrawingStandardProfile
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteBundleId
import com.engineeringood.athena.routing.RouteConstraintId
import com.engineeringood.athena.routing.RouteIntentCompilation
import com.engineeringood.athena.routing.RouteIntentConstraint
import com.engineeringood.athena.routing.RouteIntentConstraintId
import com.engineeringood.athena.routing.RouteIntentConstraintKind
import com.engineeringood.athena.routing.RouteIntentConstraintOwner
import com.engineeringood.athena.routing.RouteIntentConstraintStrength
import com.engineeringood.athena.routing.RouteIntentConstraintTarget
import com.engineeringood.athena.routing.RouteIntentDeclaration
import com.engineeringood.athena.routing.RouteIntentId
import com.engineeringood.athena.routing.RouteQuality
import com.engineeringood.athena.routing.RouteQualityMetrics
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRouteLane
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRouteSegment
import com.engineeringood.athena.routing.SchematicRouteSegmentOrientation
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class AthenaCabinetProjectionDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

data class AthenaCabinetProjectionResult(
    val presentation: PresentationDocument? = null,
    val layoutGraph: LayoutGraph? = null,
    val diagnostics: List<AthenaCabinetProjectionDiagnostic> = emptyList(),
)

class AthenaCabinetProjectionCompiler(
    private val materialResolver: AthenaRepresentationMaterialResolver = AthenaRepresentationMaterialResolver(),
) {
    fun compile(
        repositoryRoot: Path,
        sourcePath: Path,
        success: CompilerCompilationSuccess,
    ): AthenaCabinetProjectionResult {
        val diagnostics = mutableListOf<AthenaCabinetProjectionDiagnostic>()
        val installation = success.source.ast.declarations.filterIsInstance<InstallationDeclaration>().singleOrNull()
        if (installation == null || installation.kind != InstallationKind.Cabinet) {
            return failure("cabinet.installation.missing", sourcePath.toString(), "Expected exactly one `installation cabinet` declaration.")
        }
        val connectionIr = success.connectionIr
            ?: return failure("cabinet.connection-ir.missing", sourcePath.toString(), "Cabinet compilation requires validated Connection IR.")

        val materialResolution = materialResolver.resolve(
            repositoryRoot = repositoryRoot,
            document = success.document,
            projectionContext = ProjectionContextId("cabinet"),
        )
        diagnostics += materialResolution.diagnostics.map { diagnostic ->
            diagnostic("cabinet.material.${diagnostic.code}", diagnostic.subject, diagnostic.message)
        }
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())

        val materialsBySubject = materialResolution.materials.associateBy { material -> material.semanticSubjectId }
        val sourceUnitId = PhysicalSourceUnitId(sourcePath.toAbsolutePath().normalize().toString().replace('\\', '/'))
        val contracts = installation.mounts.mapNotNull { mount ->
            val subject = StableSemanticIdentity("component:${mount.deviceId}")
            val material = materialsBySubject[subject.value]
            if (material == null) {
                diagnostics += diagnostic("cabinet.material.mount.unresolved", subject.value, "Mounted device has no cabinet representation material.")
                null
            } else {
                when (val resolution = contractFromMount(subject, mount, sourceUnitId)) {
                    is PhysicalInstallationContractResolution.Success -> resolution.contract
                    is PhysicalInstallationContractResolution.Failure -> {
                        diagnostics += resolution.diagnostics.map { issue ->
                            diagnostic(issue.code, issue.subjectIdentity.value, issue.expected)
                        }
                        null
                    }
                }
            }
        }
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())

        val topology = PhysicalInstallationTopologyCompiler.compile(installation.toPhysicalIntent(sourceUnitId), contracts)
        val physicalIr = when (topology) {
            is PhysicalInstallationTopologyCompilation.Success -> topology.ir
            is PhysicalInstallationTopologyCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = topology.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }
        val routeIntentCompilation = lowerRouteIntent(
            installation = installation,
            connectionIr = connectionIr,
            diagnostics = diagnostics,
        )
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())
        when (val constraintEvaluation = PhysicalConstraintEvaluator.evaluate(physicalIr)) {
            is com.engineeringood.athena.physical.PhysicalConstraintEvaluation.Success -> Unit
            is com.engineeringood.athena.physical.PhysicalConstraintEvaluation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = constraintEvaluation.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }

        val layoutGraphResult = AthenaLayoutGraphLowerer.lower(
            physical = physicalIr,
            materials = materialResolution.materials,
        )
        if (layoutGraphResult.diagnostics.isNotEmpty()) {
            return AthenaCabinetProjectionResult(
                diagnostics = layoutGraphResult.diagnostics.map { issue ->
                    diagnostic(issue.code, issue.subject, issue.message)
                }.sorted(),
            )
        }

        val representationInputs = materialResolution.materials
            .filter { material -> physicalIr.space.mountedOccurrences.any { occurrence -> occurrence.semanticSubjectId.value == material.semanticSubjectId } }
            .map { material -> material.toCabinetRepresentationInput(sourceUnitId, physicalIr.installationId) }
        val enclosureToDrawing = CabinetTargetFrame(
            origin = CabinetPointD(40.0, 40.0),
            alongAxis = CabinetVectorD(1.0, 0.0),
            normalAxis = CabinetVectorD(0.0, 1.0),
        )
        val placementCompilation = CabinetPlacementCompiler.compile(
            CabinetPlacementRequest(
                layoutGraph = requireNotNull(layoutGraphResult.graph),
                plannerId = "athena-native",
                plannerVersion = "1.0.0",
                physicalOccurrences = physicalIr.toCabinetPhysicalInputs(),
                representationOccurrences = representationInputs,
                enclosureToDrawing = enclosureToDrawing,
            ),
        )
        val placements = when (placementCompilation) {
            is CabinetPlacementCompilation.Success -> placementCompilation.placements
            is CabinetPlacementCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = placementCompilation.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.message)
                    }.sorted(),
                )
            }
        }

        val policyCompilation = CabinetPlacementPolicyCompiler.evaluate(
            CabinetPlacementPolicyRequest(
                layoutGraph = requireNotNull(layoutGraphResult.graph),
                physicalIr = physicalIr,
                placements = placements,
            ),
        )
        val joins = when (policyCompilation) {
            is CabinetPlacementPolicyCompilation.Success -> policyCompilation.placements.map { placement -> placement.join }
            is CabinetPlacementPolicyCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = policyCompilation.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.message)
                    }.sorted(),
                )
            }
        }

        val endpointBindings = routeEndpointBindings(
            ir = physicalIr,
            document = success.document,
            materials = materialResolution.materials,
            routeIntents = routeIntentCompilation,
            joins = joins,
            diagnostics = diagnostics,
        )
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())

        val routeRealization = when (
            val realizationCompilation = CabinetRouteRealizationCompiler.compile(
                CabinetRouteRealizationRequest(
                    physicalIr = physicalIr,
                    routeIntents = routeIntentCompilation,
                    joins = joins,
                    endpoints = endpointBindings,
                    enclosureToDrawing = enclosureToDrawing,
                ),
            )
        ) {
            is CabinetRouteRealizationCompilation.Success -> realizationCompilation.ir
            is CabinetRouteRealizationCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = realizationCompilation.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.message)
                    }.sorted(),
                )
            }
        }

        val routeTopology = when (val topologyCompilation = RouteChannelTopologyCompiler.compile(routeRealization.space.channels, routeRealization.routes)) {
            is RouteChannelTopologyCompilation.Success -> topologyCompilation.topology
            is RouteChannelTopologyCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = topologyCompilation.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }
        val routing = CabinetRoutingCompiler.compile(
            CabinetRoutingRequest(
                ir = routeRealization,
                topology = routeTopology,
                joins = joins,
                endpoints = endpointBindings,
                enclosureToDrawing = enclosureToDrawing,
            ),
        )
        val routes = when (routing) {
            is CabinetRoutingCompilation.Success -> routing.routes
            is CabinetRoutingCompilation.Failure -> {
                return AthenaCabinetProjectionResult(
                    diagnostics = routing.diagnostics.map { issue ->
                        diagnostic(issue.code, issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }

        val composition = CabinetCompositionCompiler.compile(
            CabinetCompositionRequest(
                ir = physicalIr,
                joins = joins,
                routes = routes,
                policy = CabinetCompositionPolicy(documentId = "cabinet-installation", padding = 20.0),
            ),
        )
        if (composition is CabinetCompositionResult.Failure) {
            return AthenaCabinetProjectionResult(
                diagnostics = composition.diagnostics.map { issue ->
                    diagnostic(issue.code, issue.subject, issue.message)
                }.sorted(),
            )
        }

        val mountedByKey = physicalIr.space.mountedOccurrences.associateBy { occurrence -> occurrence.key }
        val placementByKey = placements.associateBy { placement -> placement.key }
        val materialBySubject = materialResolution.materials.associateBy { material -> material.semanticSubjectId }
        val componentsById = success.document.components.associateBy { component -> component.id.value }
        val graphicOccurrences = joins.map { join ->
            val mounted = mountedByKey.getValue(join.key)
            val material = materialBySubject.getValue(join.key.canonicalSemanticSubjectId.value)
            val component = componentsById.getValue(material.physicalComponentId)
            val graphic = material.definition.graphicBody.transformedTo(join.body.bounds, "${join.physicalOccurrenceId.value}:")
            val definitionAnchorsById = material.definition.anchors.associateBy { anchor -> anchor.anchorId.value }
            val placedAnchors = join.anchors.mapNotNull { transformedAnchor ->
                val definitionAnchor = definitionAnchorsById[transformedAnchor.id]
                if (definitionAnchor == null) {
                    diagnostics += diagnostic("cabinet.graphic.anchor.stale", transformedAnchor.id, "Transformed cabinet Anchor has no matching Representation Definition Anchor.")
                    null
                } else {
                    PresentationPlacedAnchor(
                        anchorId = definitionAnchor.anchorId,
                        geometryRef = definitionAnchor.geometryRef,
                        primitiveId = definitionAnchor.primitiveId,
                        point = transformedAnchor.point.toSchematicPoint(),
                        role = definitionAnchor.role,
                        required = definitionAnchor.required,
                        sourceProvenance = listOf(material.definition.lifecycle.provenance.source) +
                            material.definition.graphicBody.provenanceSources,
                    )
                }
            }.sortedBy { anchor -> anchor.anchorId.value }
            val placedAnchorsById = placedAnchors.associateBy { anchor -> anchor.anchorId.value }
            PresentationGraphicOccurrence(
                occurrenceId = RepresentationOccurrenceId("cabinet:${join.physicalOccurrenceId.value}"),
                semanticSubjectId = material.semanticSubjectId,
                physicalComponentId = mounted.occurrenceId.value,
                functionId = material.functionId,
                bounds = placementByKey.getValue(join.key).proposedPhysicalOccurrence
                    .drawingFootprintBounds(enclosureToDrawing)
                    .toPresentationBounds(),
                orientation = mounted.selectedOrientation.toLayoutOrientation(),
                deviceLabel = component.propertyText("tag") ?: component.name,
                modelLabel = component.propertyText("model"),
                packageId = material.definition.libraryId.value,
                definitionId = material.definition.symbolId.value,
                bindingRuleId = material.resolution.bindingRuleId?.value ?: "unbound",
                graphic = graphic,
                placedAnchors = placedAnchors,
                terminalBindings = material.terminalBindings.mapNotNull { (portSemanticId, terminalIdentity) ->
                    val anchorId = material.resolution.anchorMapping[portSemanticId]?.value
                    val anchor = placedAnchorsById[anchorId]
                    if (anchorId == null || anchor == null) {
                        diagnostics += diagnostic("cabinet.graphic.anchor.missing", "$portSemanticId@$terminalIdentity", "Missing transformed cabinet anchor.")
                        null
                    } else {
                        val side = terminalSide(anchor.point.toCabinetPoint(), join.body.bounds)
                        PresentationGraphicTerminalBinding(
                            portSemanticId = portSemanticId,
                            bindingId = RepresentationPortAnchorBindingId(
                                "binding:${material.resolution.bindingRuleId?.value ?: material.semanticSubjectId}:$portSemanticId:$anchorId",
                            ),
                            anchorId = anchorId,
                            terminalIdentity = terminalIdentity,
                            point = anchor.point,
                            labelPoint = anchor.point.terminalLabelPoint(side),
                            side = side,
                        )
                    }
                }.staggerTerminalLabels().sortedBy { binding -> binding.portSemanticId },
                labels = labelsFor(material.semanticSubjectId, component.propertyText("tag") ?: component.name, component.propertyText("model"), join.body.bounds),
                sourceProvenance = material.definition.graphicBody.provenanceSources +
                    material.definition.lifecycle.provenance.source +
                    mounted.provenance.declarationId,
            )
        }.sortedBy { occurrence -> occurrence.occurrenceId.value }
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())

        val routeSnapshot = routes.toRouteFactSnapshot(
            ir = physicalIr,
            document = success.document,
            occurrences = graphicOccurrences,
            routeIntents = routeIntentCompilation,
            compilerSnapshotId = connectionIr.snapshot.semanticSnapshotId,
        )
        val classifiedLines = when (val profileResolution = DrawingProfileCompiler().resolveRoutes(
            profile = DrawingStandardProfile.standardProfessional(),
            routeFacts = routeSnapshot.routeFacts,
            selectedPolicyId = "cabinet",
        )) {
            is DrawingProfileResolution.Success -> profileResolution.lines
            is DrawingProfileResolution.Failure -> {
                diagnostics += profileResolution.diagnostics.map { diagnostic ->
                    diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
                }
                emptyList()
            }
        }
        val connectorResult = PresentationConnectorCompiler().compile(
            routeFacts = routeSnapshot.routeFacts,
            occurrences = graphicOccurrences,
            lineEvidence = classifiedLines,
        )
        diagnostics += connectorResult.diagnostics.map { diagnostic ->
            diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
        }
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())
        val drawingComposition = physicalIr.toDrawingComposition(routes)
        val presentationWithoutPaintPlan = PresentationDocument(
            view = cabinetViewDefinition(),
            canvasWidth = drawingComposition.sheetBounds.width,
            canvasHeight = drawingComposition.sheetBounds.height,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            graphicOccurrences = graphicOccurrences,
            connectors = connectorResult.connectors,
            drawingComposition = drawingComposition,
        )
        val presentation = presentationWithoutPaintPlan.copy(
            paintPlan = PresentationPaintCompiler().compile(presentationWithoutPaintPlan),
        )
        diagnostics += PresentationPublicationValidator.validate(presentation).map { issue ->
            diagnostic(issue.code, issue.subject, issue.message)
        }
        if (diagnostics.isNotEmpty()) return AthenaCabinetProjectionResult(diagnostics = diagnostics.sorted())
        return AthenaCabinetProjectionResult(
            presentation = presentation,
            layoutGraph = layoutGraphResult.graph,
        )
    }
}

private fun cabinetViewDefinition(): ViewDefinition = ViewDefinition(
    id = "cabinet",
    displayName = "Cabinet",
    description = "Physical installation Cabinet projection.",
)

private fun failure(code: String, subject: String, message: String): AthenaCabinetProjectionResult =
    AthenaCabinetProjectionResult(diagnostics = listOf(diagnostic(code, subject, message)))

private fun diagnostic(code: String, subject: String, message: String): AthenaCabinetProjectionDiagnostic =
    AthenaCabinetProjectionDiagnostic(code, subject, message)

private fun List<AthenaCabinetProjectionDiagnostic>.sorted(): List<AthenaCabinetProjectionDiagnostic> =
    sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))

private fun InstallationDeclaration.toPhysicalIntent(sourceUnitId: PhysicalSourceUnitId): PhysicalInstallationIntent {
    val ductOrientationById = ducts.associate { duct -> duct.id to duct.orientation.toPhysicalInfrastructureOrientation() }
    return PhysicalInstallationIntent(
        sourceUnitId = sourceUnitId,
        installationId = PhysicalInstallationId(name),
        enclosures = enclosures.map { enclosure -> enclosure.toPhysical(sourceUnitId) },
        surfaces = surfaces.map { surface -> surface.toPhysical(sourceUnitId) },
        rails = rails.map { rail -> rail.toPhysical(sourceUnitId) },
        ducts = ducts.map { duct -> duct.toPhysical(sourceUnitId) },
        channels = channels.map { channel -> channel.toPhysical(sourceUnitId, ductOrientationById[channel.ductId]) },
        terminalGroups = terminalGroups.map { group -> group.toPhysical(sourceUnitId) },
        mounts = mounts.map { mount -> mount.toPhysical(sourceUnitId) },
        routes = routes.map { route -> route.toPhysical(sourceUnitId) },
    )
}

private fun InstallationEnclosureDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalEnclosureIntent(
    id = PhysicalObjectId(id),
    size = size.toPhysical(),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationSurfaceDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalMountingSurfaceIntent(
    id = PhysicalObjectId(id),
    enclosureId = PhysicalObjectId(enclosureId),
    at = at.toPhysical(),
    size = size.toPhysical(),
    acceptedMountingTypes = acceptedMountingTypes.map(::PhysicalMountingTypeId).toSet(),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationRailDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalRailIntent(
    id = PhysicalObjectId(id),
    surfaceId = PhysicalObjectId(surfaceId),
    at = at.toPhysical(),
    length = positive(length),
    orientation = orientation.toPhysicalInfrastructureOrientation(),
    mountingType = PhysicalMountingTypeId(mountingType),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationDuctDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = com.engineeringood.athena.physical.PhysicalDuctIntent(
    id = PhysicalObjectId(id),
    enclosureId = PhysicalObjectId(enclosureId),
    at = at.toPhysical(),
    size = size.toPhysical(),
    orientation = orientation.toPhysicalInfrastructureOrientation(),
    wall = nonNegative(wall),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationChannelDeclaration.toPhysical(
    sourceUnitId: PhysicalSourceUnitId,
    owningDuctOrientation: PhysicalInfrastructureOrientation?,
) = PhysicalRouteChannelIntent(
    id = PhysicalObjectId(id),
    ductId = PhysicalObjectId(ductId),
    at = at.toPhysical(),
    size = size.toPhysical(),
    orientation = owningDuctOrientation ?: PhysicalInfrastructureOrientation.Horizontal,
    lanes = lanes,
    margin = nonNegative(margin),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationTerminalGroupDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalTerminalGroupIntent(
    id = PhysicalObjectId(id),
    enclosureId = PhysicalObjectId(enclosureId),
    at = at.toPhysical(),
    size = size.toPhysical(),
    orientation = orientation.toPhysicalInfrastructureOrientation(),
    acceptedMountingTypes = acceptedMountingTypes.map(::PhysicalMountingTypeId).toSet(),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationMountDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalMountedOccurrenceIntent(
    occurrenceId = PhysicalObjectId(id),
    semanticSubjectId = StableSemanticIdentity("component:$deviceId"),
    targetId = PhysicalObjectId(targetId),
    at = at.toPhysical(),
    selectedOrientation = orientation.toPhysicalInstallationOrientation(),
    provenance = provenance(sourceUnitId, id, span),
)

private fun InstallationRouteDeclaration.toPhysical(sourceUnitId: PhysicalSourceUnitId) = PhysicalRouteIntentSource(
    connectionAlias = connectionAlias,
    channelIds = channelIds.map(::PhysicalObjectId),
    provenance = provenance(sourceUnitId, connectionAlias, span),
)

private fun InstallationSize3Literal.toPhysical() = PhysicalInstallationSize3i(
    width = width.millimeters(),
    height = height.millimeters(),
    depth = depth.millimeters(),
)

private fun InstallationSizeLiteral.toPhysical() = PhysicalSize2i(width.millimeters(), height.millimeters())

private fun InstallationPointLiteral.toPhysical() = PhysicalPoint2i(x.millimeters(), y.millimeters())

private fun positive(length: InstallationLengthLiteral): PhysicalPositiveMillimeters =
    requireNotNull(PhysicalPositiveMillimeters.from(length.millimeters())) { "Expected positive millimetres." }

private fun nonNegative(length: InstallationLengthLiteral): PhysicalNonNegativeMillimeters =
    requireNotNull(PhysicalNonNegativeMillimeters.from(length.millimeters())) { "Expected non-negative millimetres." }

private fun InstallationLengthLiteral.millimeters(): Int = value.roundToInt()

private fun InstallationOrientation.toPhysicalInfrastructureOrientation(): PhysicalInfrastructureOrientation = when (this) {
    InstallationOrientation.Horizontal -> PhysicalInfrastructureOrientation.Horizontal
    InstallationOrientation.Vertical -> PhysicalInfrastructureOrientation.Vertical
}

private fun InstallationMountOrientation.toPhysicalInstallationOrientation(): PhysicalInstallationOrientation = when (this) {
    InstallationMountOrientation.Deg0 -> PhysicalInstallationOrientation.Deg0
    InstallationMountOrientation.Deg90 -> PhysicalInstallationOrientation.Deg90
    InstallationMountOrientation.Deg180 -> PhysicalInstallationOrientation.Deg180
    InstallationMountOrientation.Deg270 -> PhysicalInstallationOrientation.Deg270
}

private fun provenance(sourceUnitId: PhysicalSourceUnitId, declarationId: String, span: SourceSpan): PhysicalSourceProvenance =
    PhysicalSourceProvenance(
        sourceUnitId = sourceUnitId,
        declarationId = declarationId,
        span = PhysicalSourceSpan(
            file = sourceUnitId.value,
            line = span.start.line,
            column = span.start.column,
        ),
    )

private fun SourceSpan.toProvenance(file: String): SourceProvenance = SourceProvenance(
    file = file,
    startLine = start.line,
    startColumn = start.column,
    endLine = end.line,
    endColumn = end.column,
)

private fun contractFromMount(
    subject: StableSemanticIdentity,
    mount: InstallationMountDeclaration,
    sourceUnitId: PhysicalSourceUnitId,
): PhysicalInstallationContractResolution {
    val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "installation:${mount.id}")
    val span = PhysicalSourceSpan(
        file = sourceUnitId.value,
        line = mount.span.start.line,
        column = mount.span.start.column,
    )
    fun fact(field: PhysicalInstallationContractField, value: PhysicalInstallationContractValue) =
        PhysicalInstallationContractFact(
            field = field,
            value = value,
            source = source,
            span = span,
        )
    val facts = listOf(
        fact(PhysicalInstallationContractField.Width, PhysicalInstallationContractValue.LengthMillimeters(mount.footprint.width.millimeters())),
        fact(PhysicalInstallationContractField.Height, PhysicalInstallationContractValue.LengthMillimeters(mount.footprint.height.millimeters())),
        fact(PhysicalInstallationContractField.Depth, PhysicalInstallationContractValue.LengthMillimeters(mount.footprint.depth.millimeters())),
        fact(
            PhysicalInstallationContractField.MountingType,
            PhysicalInstallationContractValue.MountingType(PhysicalMountingTypeId(mount.mountingType)),
        ),
        fact(
            PhysicalInstallationContractField.AllowedOrientations,
            PhysicalInstallationContractValue.Orientations(
                mount.allowedOrientations.map { orientation -> orientation.toPhysicalInstallationOrientation() }.toSet(),
            ),
        ),
        fact(PhysicalInstallationContractField.ClearanceTop, PhysicalInstallationContractValue.LengthMillimeters(mount.clearance.top.millimeters())),
        fact(PhysicalInstallationContractField.ClearanceRight, PhysicalInstallationContractValue.LengthMillimeters(mount.clearance.right.millimeters())),
        fact(PhysicalInstallationContractField.ClearanceBottom, PhysicalInstallationContractValue.LengthMillimeters(mount.clearance.bottom.millimeters())),
        fact(PhysicalInstallationContractField.ClearanceLeft, PhysicalInstallationContractValue.LengthMillimeters(mount.clearance.left.millimeters())),
        fact(
            PhysicalInstallationContractField.CompatibleContainerKinds,
            PhysicalInstallationContractValue.ContainerKinds(
                mount.compatibleContainerKinds.map(::PhysicalContainerKindId).toSet(),
            ),
        ),
    )
    return PhysicalInstallationContractResolver.resolve(subject, facts)
}

private fun PhysicalInstallationIR.toCabinetPhysicalInputs(): List<CabinetPhysicalOccurrenceInput> {
    val surfaces = space.surfaces.associateBy { surface -> surface.id }
    val rails = space.rails.associateBy { rail -> rail.id }
    val terminalGroups = space.terminalGroups.associateBy { group -> group.id }
    return space.mountedOccurrences.map { occurrence ->
        CabinetPhysicalOccurrenceInput(
            key = occurrence.key,
            occurrenceId = occurrence.occurrenceId,
            targetId = occurrence.target.objectId(),
            targetLocalPosition = CabinetPointD(occurrence.at.x.toDouble(), occurrence.at.y.toDouble()),
            footprint = CabinetSizeD(
                occurrence.contract.size.width.value.toDouble(),
                occurrence.contract.size.height.value.toDouble(),
            ),
            orientation = occurrence.selectedOrientation,
            targetFrame = occurrence.target.targetFrame(surfaces, rails, terminalGroups),
            provenance = occurrence.provenance,
        )
    }
}

private fun PhysicalMountTargetRef.objectId(): PhysicalObjectId = when (this) {
    is PhysicalMountTargetRef.Surface -> id
    is PhysicalMountTargetRef.Rail -> id
    is PhysicalMountTargetRef.TerminalGroup -> id
}

private fun PhysicalMountTargetRef.targetFrame(
    surfaces: Map<PhysicalObjectId, PhysicalMountingSurface>,
    rails: Map<PhysicalObjectId, PhysicalRail>,
    terminalGroups: Map<PhysicalObjectId, PhysicalTerminalGroup>,
): CabinetTargetFrame = when (this) {
    is PhysicalMountTargetRef.Surface -> {
        val surface = surfaces.getValue(id)
        CabinetTargetFrame(
            origin = CabinetPointD(surface.at.x.toDouble(), surface.at.y.toDouble()),
            alongAxis = CabinetVectorD(1.0, 0.0),
            normalAxis = CabinetVectorD(0.0, 1.0),
        )
    }
    is PhysicalMountTargetRef.Rail -> {
        val rail = rails.getValue(id)
        val surface = surfaces.getValue(rail.surfaceId)
        val origin = CabinetPointD((surface.at.x + rail.at.x).toDouble(), (surface.at.y + rail.at.y).toDouble())
        when (rail.orientation) {
            PhysicalInfrastructureOrientation.Horizontal -> CabinetTargetFrame(origin, CabinetVectorD(1.0, 0.0), CabinetVectorD(0.0, 1.0))
            PhysicalInfrastructureOrientation.Vertical -> CabinetTargetFrame(origin, CabinetVectorD(0.0, 1.0), CabinetVectorD(-1.0, 0.0))
        }
    }
    is PhysicalMountTargetRef.TerminalGroup -> {
        val group = terminalGroups.getValue(id)
        val origin = CabinetPointD(group.at.x.toDouble(), group.at.y.toDouble())
        when (group.orientation) {
            PhysicalInfrastructureOrientation.Horizontal -> CabinetTargetFrame(origin, CabinetVectorD(1.0, 0.0), CabinetVectorD(0.0, 1.0))
            PhysicalInfrastructureOrientation.Vertical -> CabinetTargetFrame(origin, CabinetVectorD(0.0, 1.0), CabinetVectorD(-1.0, 0.0))
        }
    }
}

private fun CabinetPhysicalOccurrenceInput.drawingFootprintBounds(
    enclosureToDrawing: CabinetTargetFrame,
): CabinetRectD {
    val orientedFootprint = when (orientation) {
        PhysicalInstallationOrientation.Deg0,
        PhysicalInstallationOrientation.Deg180,
        -> footprint
        PhysicalInstallationOrientation.Deg90,
        PhysicalInstallationOrientation.Deg270,
        -> CabinetSizeD(width = footprint.height, height = footprint.width)
    }
    val localCorners = listOf(
        targetLocalPosition,
        CabinetPointD(targetLocalPosition.x + orientedFootprint.width, targetLocalPosition.y),
        CabinetPointD(
            targetLocalPosition.x + orientedFootprint.width,
            targetLocalPosition.y + orientedFootprint.height,
        ),
        CabinetPointD(targetLocalPosition.x, targetLocalPosition.y + orientedFootprint.height),
    )
    val drawingCorners = localCorners.map { point ->
        enclosureToDrawing.applyPoint(targetFrame.applyPoint(point))
    }
    val left = drawingCorners.minOf { point -> point.x }
    val top = drawingCorners.minOf { point -> point.y }
    return CabinetRectD(
        x = left,
        y = top,
        width = drawingCorners.maxOf { point -> point.x } - left,
        height = drawingCorners.maxOf { point -> point.y } - top,
    )
}

private fun CabinetTargetFrame.applyPoint(point: CabinetPointD): CabinetPointD = CabinetPointD(
    x = origin.x + (alongAxis.x * point.x) + (normalAxis.x * point.y),
    y = origin.y + (alongAxis.y * point.x) + (normalAxis.y * point.y),
)

private fun AthenaResolvedRepresentationMaterial.toCabinetRepresentationInput(
    sourceUnitId: PhysicalSourceUnitId,
    installationId: PhysicalInstallationId,
): CabinetRepresentationOccurrenceInput {
    val bounds = requireNotNull(definition.graphicBody.bounds) { "Cabinet representation requires Graphic Primitive bounds." }
    return CabinetRepresentationOccurrenceInput(
        key = InstallationOccurrenceKey(sourceUnitId, installationId, StableSemanticIdentity(semanticSubjectId)),
        representationOccurrenceId = CabinetRepresentationOccurrenceId("representation:$semanticSubjectId"),
        intrinsicBounds = CabinetRectD(bounds.x, bounds.y, bounds.width, bounds.height),
        anchors = definition.anchors.map { anchor ->
            CabinetIntrinsicAnchor(anchor.anchorId.value, CabinetPointD(anchor.point.x, anchor.point.y))
        },
    )
}

private fun lowerRouteIntent(
    installation: InstallationDeclaration,
    connectionIr: ConnectionIr,
    diagnostics: MutableList<AthenaCabinetProjectionDiagnostic>,
): RouteIntentCompilation {
    val connectionsByAlias = connectionIr.connections.associateBy { connection -> connection.id.value.substringAfterLast(':') }
    val declarations = installation.routes.mapNotNull { route ->
        val connection = connectionsByAlias[route.connectionAlias]
        if (connection == null) {
            diagnostics += diagnostic(
                "cabinet.route.connection.missing",
                route.connectionAlias,
                "Physical route alias has no validated Connection IR fact.",
            )
            return@mapNotNull null
        }
        val provenance = route.span.toProvenance(connection.provenance.file)
        RouteIntentDeclaration(
            connectionId = ElectricalConnectionId(connection.id.value),
            constraints = route.channelIds.mapIndexed { index, channelId ->
                RouteIntentConstraint(
                    constraintId = RouteIntentConstraintId(
                        "route-intent:${connection.id.value}:through:${index.toString().padStart(3, '0')}:$channelId",
                    ),
                    kind = RouteIntentConstraintKind.THROUGH,
                    owner = RouteIntentConstraintOwner.PHYSICAL,
                    strength = RouteIntentConstraintStrength.REQUIRED,
                    target = RouteIntentConstraintTarget.Reference(
                        EngineeringReference(
                            authoredPath = listOf(channelId),
                            resolvedIdentity = StableSemanticIdentity("physical-channel:$channelId"),
                            provenance = provenance,
                        ),
                    ),
                    provenance = provenance,
                )
            },
            provenance = provenance,
        )
    }
    return RouteIntentLowerer().lower(connectionIr, declarations)
}

private fun routeEndpointBindings(
    ir: PhysicalInstallationIR,
    document: EngineeringDocument,
    materials: List<AthenaResolvedRepresentationMaterial>,
    routeIntents: RouteIntentCompilation,
    joins: List<com.engineeringood.athena.drawing.composition.CabinetOccurrenceVisualJoin>,
    diagnostics: MutableList<AthenaCabinetProjectionDiagnostic>,
): List<CabinetConnectionEndpointBinding> {
    val connectionsById = document.connections.associateBy { connection -> connection.id.value }
    val portToMaterial = materials.flatMap { material ->
        material.terminalBindings.keys.map { portId -> portId to material }
    }.toMap()
    val joinsByKey = joins.associateBy { join -> join.key }
    return routeIntents.routeIntents.mapNotNull { routeIntent ->
        val connection = connectionsById[routeIntent.connectionId.value]
        val connectionAlias = routeIntent.connectionId.value.substringAfterLast(':')
        if (connection == null) {
            diagnostics += diagnostic("cabinet.route.connection.missing", connectionAlias, "Route Intent has no governed connection.")
            return@mapNotNull null
        }
        val fromPort = connection.from.resolvedIdentity?.value
        val toPort = connection.to.resolvedIdentity?.value
        val fromMaterial = fromPort?.let(portToMaterial::get)
        val toMaterial = toPort?.let(portToMaterial::get)
        val fromAnchor = fromPort?.let { fromMaterial?.resolution?.anchorMapping?.get(it)?.value }
        val toAnchor = toPort?.let { toMaterial?.resolution?.anchorMapping?.get(it)?.value }
        if (fromPort == null || toPort == null || fromMaterial == null || toMaterial == null || fromAnchor == null || toAnchor == null) {
            diagnostics += diagnostic("cabinet.route.anchor.missing", connectionAlias, "Route endpoint ports must resolve to material anchors.")
            return@mapNotNull null
        }
        val fromKey = ir.keyFor(fromMaterial.semanticSubjectId)
        val toKey = ir.keyFor(toMaterial.semanticSubjectId)
        val fromJoin = joinsByKey[fromKey]
        val toJoin = joinsByKey[toKey]
        val transformedFromAnchor = fromJoin?.anchors?.firstOrNull { anchor -> anchor.id == fromAnchor }
        val transformedToAnchor = toJoin?.anchors?.firstOrNull { anchor -> anchor.id == toAnchor }
        if (fromJoin == null || toJoin == null || transformedFromAnchor == null || transformedToAnchor == null) {
            diagnostics += diagnostic("cabinet.route.anchor.missing", connectionAlias, "Route endpoint anchors must resolve after placement.")
            return@mapNotNull null
        }
        CabinetConnectionEndpointBinding(
            connectionAlias = connectionAlias,
            from = CabinetRouteEndpointRef(
                key = fromKey,
                anchorId = fromAnchor,
                direction = anchorDirection(transformedFromAnchor.point, fromJoin.body.bounds),
            ),
            to = CabinetRouteEndpointRef(
                key = toKey,
                anchorId = toAnchor,
                direction = anchorDirection(transformedToAnchor.point, toJoin.body.bounds),
            ),
        )
    }
}

private fun anchorDirection(point: CabinetPointD, bounds: CabinetRectD): CabinetAnchorDirection = when (terminalSide(point, bounds)) {
    TerminalSide.LEFT -> CabinetAnchorDirection.LEFT
    TerminalSide.RIGHT -> CabinetAnchorDirection.RIGHT
    TerminalSide.TOP -> CabinetAnchorDirection.UP
    TerminalSide.BOTTOM -> CabinetAnchorDirection.DOWN
}

private fun EngineeringConnection.alias(): String = id.value.substringAfterLast(':')

private fun PhysicalInstallationIR.keyFor(subjectId: String): InstallationOccurrenceKey =
    InstallationOccurrenceKey(sourceUnitId, installationId, StableSemanticIdentity(subjectId))

private fun GraphicPrimitiveDocument.transformedTo(
    targetBounds: CabinetRectD,
    idPrefix: String,
): GraphicPrimitiveDocument {
    val sourceBounds = requireNotNull(bounds) { "Graphic document requires bounds." }
    val scale = min(targetBounds.width / sourceBounds.width, targetBounds.height / sourceBounds.height)
    val targetWidth = sourceBounds.width * scale
    val targetHeight = sourceBounds.height * scale
    val target = GraphicBounds(
        x = targetBounds.x + (targetBounds.width - targetWidth) / 2.0,
        y = targetBounds.y + (targetBounds.height - targetHeight) / 2.0,
        width = targetWidth,
        height = targetHeight,
    )
    val transform = CabinetAffineTransform.translation(target.x, target.y)
        .compose(CabinetAffineTransform.scale(scale, scale))
        .compose(CabinetAffineTransform.translation(-sourceBounds.x, -sourceBounds.y))
    return copy(
        documentId = GraphicPrimitiveDocumentId(idPrefix.removeSuffix(":")),
        bounds = target,
        primitives = primitives.flatMap { primitive -> primitive.transform(transform, idPrefix) },
    )
}

private data class CabinetAffineTransform(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    fun compose(inner: CabinetAffineTransform): CabinetAffineTransform = CabinetAffineTransform(
        a = a * inner.a + c * inner.b,
        b = b * inner.a + d * inner.b,
        c = a * inner.c + c * inner.d,
        d = b * inner.c + d * inner.d,
        e = a * inner.e + c * inner.f + e,
        f = b * inner.e + d * inner.f + f,
    )

    fun point(point: GraphicPoint): GraphicPoint = GraphicPoint(
        x = a * point.x + c * point.y + e,
        y = b * point.x + d * point.y + f,
    )

    fun bounds(bounds: GraphicBounds): GraphicBounds {
        val points = listOf(
            point(GraphicPoint(bounds.x, bounds.y)),
            point(GraphicPoint(bounds.x + bounds.width, bounds.y)),
            point(GraphicPoint(bounds.x + bounds.width, bounds.y + bounds.height)),
            point(GraphicPoint(bounds.x, bounds.y + bounds.height)),
        )
        val left = points.minOf(GraphicPoint::x)
        val top = points.minOf(GraphicPoint::y)
        val right = points.maxOf(GraphicPoint::x)
        val bottom = points.maxOf(GraphicPoint::y)
        return GraphicBounds(left, top, right - left, bottom - top)
    }

    fun uniformScale(): Double = max(abs(a), abs(d))

    companion object {
        fun translation(dx: Double, dy: Double) = CabinetAffineTransform(1.0, 0.0, 0.0, 1.0, dx, dy)
        fun scale(x: Double, y: Double) = CabinetAffineTransform(x, 0.0, 0.0, y, 0.0, 0.0)
    }
}

private fun GraphicPrimitive.transform(transform: CabinetAffineTransform, idPrefix: String): List<GraphicPrimitive> = when (this) {
    is GraphicPrimitive.Group -> children.flatMap { child -> child.transform(transform, idPrefix) }
    is GraphicPrimitive.Transformed -> child.transform(transform, idPrefix)
    is GraphicPrimitive.Line -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), start = transform.point(start), end = transform.point(end)))
    is GraphicPrimitive.Polyline -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), points = points.map(transform::point)))
    is GraphicPrimitive.Arc -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), center = transform.point(center), radius = radius * transform.uniformScale()))
    is GraphicPrimitive.Circle -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), center = transform.point(center), radius = radius * transform.uniformScale()))
    is GraphicPrimitive.Rectangle -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), cornerRadius = cornerRadius * transform.uniformScale()))
    is GraphicPrimitive.Text -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), origin = transform.point(origin)))
    is GraphicPrimitive.Marker -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), origin = transform.point(origin)))
    is GraphicPrimitive.ConnectionDot -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), center = transform.point(center), radius = radius * transform.uniformScale()))
    is GraphicPrimitive.ReferenceArrow -> listOf(copy(primitiveId = primitiveId.namespaced(idPrefix), bounds = transform.bounds(bounds), start = transform.point(start), end = transform.point(end), headSize = headSize * transform.uniformScale()))
}

private fun GraphicPrimitiveId.namespaced(prefix: String): GraphicPrimitiveId = GraphicPrimitiveId("$prefix$value")

private fun terminalSide(point: CabinetPointD, bounds: CabinetRectD): TerminalSide {
    val distances = mapOf(
        TerminalSide.LEFT to abs(point.x - bounds.x),
        TerminalSide.RIGHT to abs(point.x - bounds.right),
        TerminalSide.TOP to abs(point.y - bounds.y),
        TerminalSide.BOTTOM to abs(point.y - bounds.bottom),
    )
    return distances.minBy { it.value }.key
}

private fun labelsFor(
    semanticSubjectId: String,
    tag: String,
    model: String?,
    bounds: CabinetRectD,
): List<PresentationGraphicLabel> = buildList {
    add(
        PresentationGraphicLabel(
            labelId = "$semanticSubjectId:tag",
            role = "device-tag",
            value = tag,
            bounds = com.engineeringood.athena.presentation.PresentationDrawingBounds(
                x = bounds.x.roundToInt(),
                y = max(0, bounds.y.roundToInt() - 16),
                width = max(24, bounds.width.roundToInt()),
                height = 14,
            ),
        ),
    )
    if (!model.isNullOrBlank()) {
        add(
            PresentationGraphicLabel(
                labelId = "$semanticSubjectId:model",
                role = "model-tag",
                value = model,
                bounds = com.engineeringood.athena.presentation.PresentationDrawingBounds(
                    x = bounds.x.roundToInt(),
                    y = bounds.bottom.roundToInt() + 2,
                    width = max(24, bounds.width.roundToInt()),
                    height = 14,
                ),
            ),
        )
    }
}

private fun CabinetPointD.toSchematicPoint(): SchematicRoutePoint =
    SchematicRoutePoint(max(0, x.roundToInt()), max(0, y.roundToInt()))

private fun SchematicRoutePoint.terminalLabelPoint(side: TerminalSide): SchematicRoutePoint =
    when (side) {
        TerminalSide.LEFT -> SchematicRoutePoint(x - 34, y - 8)
        TerminalSide.RIGHT -> SchematicRoutePoint(x + 10, y - 8)
        TerminalSide.TOP -> SchematicRoutePoint(x + 10, y - 14)
        TerminalSide.BOTTOM -> SchematicRoutePoint(x + 10, y + 18)
    }

private fun List<PresentationGraphicTerminalBinding>.staggerTerminalLabels(): List<PresentationGraphicTerminalBinding> =
    groupBy { binding -> binding.side }.values.flatMap { sideBindings ->
        var lastY: Int? = null
        sideBindings.sortedWith(compareBy({ it.labelPoint.y }, { it.labelPoint.x }, { it.terminalIdentity })).map { binding ->
            val y = lastY?.let { previous -> maxOf(binding.labelPoint.y, previous + 30) } ?: binding.labelPoint.y
            lastY = y
            binding.copy(labelPoint = binding.labelPoint.copy(y = y))
        }
    }

private fun SchematicRoutePoint.toCabinetPoint(): CabinetPointD = CabinetPointD(x.toDouble(), y.toDouble())

private fun CabinetRectD.toPresentationBounds(): com.engineeringood.athena.presentation.PresentationDrawingBounds =
    com.engineeringood.athena.presentation.PresentationDrawingBounds(
        x = max(0, x.roundToInt()),
        y = max(0, y.roundToInt()),
        width = max(1, width.roundToInt()),
        height = max(1, height.roundToInt()),
    )

private fun List<CabinetRouteFact>.toRouteFactSnapshot(
    ir: PhysicalInstallationIR,
    document: EngineeringDocument,
    occurrences: List<PresentationGraphicOccurrence>,
    routeIntents: RouteIntentCompilation,
    compilerSnapshotId: String,
): RouteFactSnapshot {
    val snapshotId = LayoutSnapshotId("cabinet:${ir.installationId.value}")
    val connectionsByAlias = document.connections.associateBy { connection -> connection.alias() }
    val componentsById = document.components.associateBy { component -> component.id }
    val occurrenceBySubject = occurrences.associateBy { occurrence -> occurrence.semanticSubjectId }
    val intentsByConnectionId = routeIntents.routeIntents.associateBy { intent -> intent.connectionId }
    val bundleByIntentId = routeIntents.routeBundles.flatMap { bundle ->
        bundle.members.map { member -> member.routeIntentId to bundle.bundleId }
    }.toMap()
    val routeFacts = mapIndexed { index, route ->
        val connection = connectionsByAlias.getValue(route.connectionAlias)
        val connectionId = ElectricalConnectionId(connection.id.value)
        val routeIntent = intentsByConnectionId.getValue(connectionId)
        val sourceOccurrence = occurrenceBySubject.getValue(route.from.key.canonicalSemanticSubjectId.value)
        val targetOccurrence = occurrenceBySubject.getValue(route.to.key.canonicalSemanticSubjectId.value)
        val sourcePort = document.ports.singleOrNull { port -> port.id == connection.from.resolvedIdentity }
        val targetPort = document.ports.singleOrNull { port -> port.id == connection.to.resolvedIdentity }
        val sourceKind = sourcePort?.ownerReference?.resolvedIdentity
            ?.let(componentsById::get)
            ?.let { component -> component.propertyText("type") ?: component.kind }
        val targetKind = targetPort?.ownerReference?.resolvedIdentity
            ?.let(componentsById::get)
            ?.let { component -> component.propertyText("type") ?: component.kind }
        val sourceAnchor = route.from.toTerminalAnchor(sourceOccurrence, connection.from.resolvedIdentity)
        val targetAnchor = route.to.toTerminalAnchor(targetOccurrence, connection.to.resolvedIdentity)
        val connectionRole = if (sourcePort == null || targetPort == null) {
            null
        } else {
            ElectricalConnectionRoleClassifier().classify(
                ElectricalConnectionRoleInput(
                    connectionId = connectionId,
                    sourcePort = sourcePort.toConnectionPortRef(sourceOccurrence, sourceKind),
                    targetPort = targetPort.toConnectionPortRef(targetOccurrence, targetKind),
                ),
            ).role
        }
        RouteFact(
            routeId = SchematicRouteId("cabinet-route:${route.connectionAlias}"),
            snapshotId = snapshotId,
            connectionId = connectionId,
            routeIntentId = routeIntent.intentId,
            bundleId = bundleByIntentId.getValue(routeIntent.intentId),
            selectedChannelIds = route.orderedChannelIds.map { channelId -> channelId.value },
            plannerId = "athena-cabinet-route-realizer",
            compilerSnapshotId = compilerSnapshotId,
            provenance = routeIntent.provenance,
            qualityMetrics = route.qualityMetrics(),
            source = sourceAnchor,
            target = targetAnchor,
            connectionRole = connectionRole,
            segments = route.segments.mapNotNull { segment -> segment.toSchematicSegment() },
            lane = SchematicRouteLane(index),
            quality = route.quality(),
        )
    }
    return RouteFactSnapshot.canonical(snapshotId, "cabinet", routeFacts)
}

private fun CabinetRouteFact.qualityMetrics(): RouteQualityMetrics {
    val schematicSegments = segments.mapNotNull { segment -> segment.toSchematicSegment() }
    return RouteQualityMetrics(
        crossingCount = 0,
        bendCount = schematicSegments.zipWithNext().count { (left, right) -> left.orientation != right.orientation },
        length = schematicSegments.sumOf { segment ->
            abs(segment.end.x - segment.start.x) + abs(segment.end.y - segment.start.y)
        },
        channelChangeCount = (orderedChannelIds.size - 1).coerceAtLeast(0),
        bundleContinuityPenalty = 0,
        labelClearanceViolationCount = 0,
    )
}

private fun CabinetRouteFact.quality(): RouteQuality {
    val failed = buildList {
        if (segments.isEmpty() || segments.any { segment -> segment.toSchematicSegment() == null }) {
            add(RouteConstraintId("route:$connectionAlias:orthogonal"))
        }
        if (orderedChannelIds.isEmpty()) {
            add(RouteConstraintId("route:$connectionAlias:channel"))
        }
    }
    return if (failed.isEmpty()) {
        RouteQuality.satisfied()
    } else {
        RouteQuality.degraded(failed, "Accepted route does not satisfy all required Cabinet route constraints.")
    }
}

private fun com.engineeringood.athena.drawing.composition.CabinetRouteEndpointPoint.toTerminalAnchor(
    occurrence: PresentationGraphicOccurrence,
    portId: StableSemanticIdentity?,
): TerminalAnchorFact = TerminalAnchorFact(
    anchorId = TerminalAnchorId("${occurrence.occurrenceId.value}:$anchorId"),
    subjectId = StableSemanticIdentity(occurrence.semanticSubjectId),
    occurrenceId = LayoutOccurrenceId(occurrence.occurrenceId.value),
    portId = ElectricalPortId(portId?.value ?: anchorId),
    portSemanticId = portId,
    portRole = ElectricalPortRole.TERMINAL,
    side = occurrence.terminalBindings.firstOrNull { binding -> binding.anchorId == anchorId }?.side ?: TerminalSide.RIGHT,
    point = point.toSchematicPoint(),
    policySource = "physical-cabinet-route",
)

private fun EngineeringPort.toConnectionPortRef(
    occurrence: PresentationGraphicOccurrence,
    ownerKind: String?,
): ElectricalConnectionPortRef = ElectricalConnectionPortRef(
    subjectId = StableSemanticIdentity(occurrence.semanticSubjectId),
    portSemanticId = id,
    portId = ElectricalPortId(id.value),
    endpointKind = if (ownerKind.equals("Terminal", ignoreCase = true)) {
        ElectricalConnectionEndpointKind.TERMINAL
    } else if (ownerKind.equals("Motor", ignoreCase = true) || occurrence.definitionId.contains("motor", ignoreCase = true)) {
        ElectricalConnectionEndpointKind.LOAD
    } else {
        ElectricalConnectionEndpointKind.DEVICE
    },
    direction = semanticDirection(),
    signalFamilyId = SemanticSignalFamilyId(propertyText("signal") ?: "Unknown"),
)

private fun EngineeringPort.semanticDirection(): SemanticPortDirection =
    when (propertyText("direction")?.lowercase()) {
        "in" -> SemanticPortDirection.INPUT
        "out" -> SemanticPortDirection.OUTPUT
        "bidirectional" -> SemanticPortDirection.BIDIRECTIONAL
        else -> SemanticPortDirection.PASSIVE
    }

private fun com.engineeringood.athena.drawing.composition.CabinetRouteSegment.toSchematicSegment(): SchematicRouteSegment? {
    val start = from.toSchematicPoint()
    val end = to.toSchematicPoint()
    return when {
        start == end -> null
        start.y == end.y -> SchematicRouteSegment(start, end, SchematicRouteSegmentOrientation.HORIZONTAL)
        start.x == end.x -> SchematicRouteSegment(start, end, SchematicRouteSegmentOrientation.VERTICAL)
        else -> null
    }
}

private fun PhysicalInstallationIR.toDrawingComposition(routes: List<CabinetRouteFact>): PresentationDrawingComposition {
    val sheetWidth = space.enclosure.size.width + 100
    val sheetHeight = space.enclosure.size.height + 120
    val titleBounds = PresentationDrawingBounds(40, sheetHeight - 60, sheetWidth - 80, 40)
    return PresentationDrawingComposition(
        sheetId = "cabinet-installation",
        policyId = "physical-cabinet-composition",
        contentBounds = PresentationDrawingBounds(40, 40, space.enclosure.size.width, space.enclosure.size.height),
        frameBounds = PresentationDrawingBounds(20, 20, sheetWidth - 40, sheetHeight - 40),
        drawingAreaBounds = PresentationDrawingBounds(40, 40, space.enclosure.size.width, space.enclosure.size.height),
        titleBlockBounds = titleBounds,
        sheetBounds = PresentationDrawingBounds(0, 0, sheetWidth, sheetHeight),
        frameId = "cabinet-frame",
        frameStyle = "industrial-cabinet-frame",
        title = PresentationDrawingTitle(
            sheetTitle = "Physical Installation Cabinet",
            sheetFamily = "Cabinet",
            sheetNumber = "CAB-001",
            revisionCode = "A",
            revisionNote = "Physical Installation Model foundation",
            pageFormat = "CABINET-900x720",
            orientation = "landscape",
        ),
        coordinateZones = emptyList(),
        structureSubjects = emptyList(),
        structureFacts = structureFacts(routes),
        referencePlacements = emptyList(),
        authorities = PresentationDrawingAuthorities(
            contentBounds = "physical-installation-ir",
            bounds = "cabinet-composition-compiler",
            projection = "cabinet-projection",
            representation = "athena-representation-material",
            structure = "physical-installation-ir",
            policy = "presentation-profile-policy",
        ),
    )
}

private fun PhysicalInstallationIR.structureFacts(routes: List<CabinetRouteFact>): List<PresentationDrawingStructureFact> = buildList {
    space.rails.forEach { rail ->
        val surface = space.surfaces.first { surface -> surface.id == rail.surfaceId }
        val start = PresentationDrawingPoint(surface.at.x + rail.at.x + 40, surface.at.y + rail.at.y + 40)
        val end = when (rail.orientation) {
            PhysicalInfrastructureOrientation.Horizontal -> PresentationDrawingPoint(start.x + rail.length.value, start.y)
            PhysicalInfrastructureOrientation.Vertical -> PresentationDrawingPoint(start.x, start.y + rail.length.value)
        }
        add(PresentationDrawingStructureFact("rail:${rail.id.value}", "rail", null, null, start, end, emptyList(), "physical-installation-ir", "cabinet-composition-compiler"))
    }
    val ducts = space.ducts.associateBy { duct -> duct.id }
    space.channels.forEach { channel ->
        val duct = ducts.getValue(channel.ductId)
        add(
            PresentationDrawingStructureFact(
                factId = "channel:${channel.id.value}",
                kind = "route-channel",
                axis = channel.orientation.name,
                bounds = PresentationDrawingBounds(duct.at.x + duct.wall.value + channel.at.x + 40, duct.at.y + duct.wall.value + channel.at.y + 40, channel.size.width, channel.size.height),
                start = null,
                end = null,
                memberIds = routes.filter { route -> channel.id in route.orderedChannelIds }.map { route -> route.connectionAlias },
                authority = "physical-route-channel-topology",
                boundsAuthority = "physical-installation-ir",
            ),
        )
    }
    space.terminalGroups.forEach { group ->
        add(PresentationDrawingStructureFact("terminal-group:${group.id.value}", "terminal-strip", group.orientation.name, PresentationDrawingBounds(group.at.x + 40, group.at.y + 40, group.size.width, group.size.height), null, null, group.orderedOccurrenceKeys.map { it.canonicalSemanticSubjectId.value }, "physical-installation-ir", "physical-installation-ir"))
    }
}

private fun PhysicalInstallationOrientation.toLayoutOrientation(): LayoutOrientation = when (this) {
    PhysicalInstallationOrientation.Deg0,
    PhysicalInstallationOrientation.Deg180,
    -> LayoutOrientation.VERTICAL
    PhysicalInstallationOrientation.Deg90,
    PhysicalInstallationOrientation.Deg270,
    -> LayoutOrientation.HORIZONTAL
}

private fun EngineeringDocument.componentProperty(componentId: String, name: String): String? =
    components.firstOrNull { component -> component.id.value == componentId }?.propertyText(name)

private fun com.engineeringood.athena.ir.EngineeringComponent.propertyText(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.text()

private fun EngineeringPort.propertyText(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.text()

private fun EngineeringPropertyValue.text(): String = when (this) {
    is EngineeringPropertyValue.Symbol -> text
    is EngineeringPropertyValue.Text -> text
}
