package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticSchematicLayoutFactDeriver
import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionCompiler
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionPolicy
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionRequest
import com.engineeringood.athena.drawing.composition.DrawingSheetCompositionResult
import com.engineeringood.athena.drawing.composition.DrawingSheetTitleFieldInput
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.DrawingGridPosition
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis
import com.engineeringood.athena.layout.engine.SchematicPlacementFact
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingAuthorities
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationDrawingComposition
import com.engineeringood.athena.presentation.PresentationDrawingCoordinateZone
import com.engineeringood.athena.presentation.PresentationDrawingPoint
import com.engineeringood.athena.presentation.PresentationDrawingReferencePlacement
import com.engineeringood.athena.presentation.PresentationDrawingStructureFact
import com.engineeringood.athena.presentation.PresentationDrawingStructureSubject
import com.engineeringood.athena.presentation.PresentationDrawingTitle
import com.engineeringood.athena.presentation.PresentationDrawingTitleField
import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationConnectionMarkerId
import com.engineeringood.athena.presentation.PresentationConnectionMarkerKind
import com.engineeringood.athena.presentation.PresentationGraphicLabel
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPlacedAnchor
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.projection.ProjectionSheetFrame
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetRevisionMetadata
import com.engineeringood.athena.projection.ProjectionSheetTitleBlock
import com.engineeringood.athena.projection.ProjectionSheetViewComposition
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.routing.AthenaRouteEngineInput
import com.engineeringood.athena.routing.AthenaRouteEngine
import com.engineeringood.athena.routing.AthenaRouteRequest
import com.engineeringood.athena.routing.ConnectionPresentationLineEvidence
import com.engineeringood.athena.routing.DrawingBounds
import com.engineeringood.athena.routing.DrawingProfileCompiler
import com.engineeringood.athena.routing.DrawingProfileResolution
import com.engineeringood.athena.routing.DrawingStandardProfile
import com.engineeringood.athena.routing.ElectricalConnectionEndpointKind
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalConnectionRoleClassifier
import com.engineeringood.athena.routing.ElectricalConnectionRoleInput
import com.engineeringood.athena.routing.ElectricalConnectionPortRef
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.EndpointAttachmentFact
import com.engineeringood.athena.routing.EndpointAttachmentRequest
import com.engineeringood.athena.routing.EndpointAttachmentValidator
import com.engineeringood.athena.routing.JunctionCrossingMarker
import com.engineeringood.athena.routing.JunctionCrossingMarkerCompilation
import com.engineeringood.athena.routing.JunctionCrossingMarkerCompiler
import com.engineeringood.athena.routing.JunctionCrossingMarkerKind
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteLabelPlacementEvidence
import com.engineeringood.athena.routing.RouteLabelPlacementCompiler
import com.engineeringood.athena.routing.RouteLabelPlacementRequest
import com.engineeringood.athena.routing.SchematicComponentBounds
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRoutingLayoutContext
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide
import kotlin.math.abs
import kotlin.math.roundToInt

class AthenaProfessionalDrawingCompiler(
    private val materialResolver: AthenaRepresentationMaterialResolver = AthenaRepresentationMaterialResolver(),
    private val layoutDeriver: ProjectSemanticSchematicLayoutFactDeriver = ProjectSemanticSchematicLayoutFactDeriver(),
    private val sheetCompiler: DrawingSheetCompositionCompiler = DrawingSheetCompositionCompiler(),
    private val routeEngine: AthenaRouteEngine = AthenaRouteEngine(),
    private val drawingProfileCompiler: DrawingProfileCompiler = DrawingProfileCompiler(),
    private val endpointAttachmentValidator: EndpointAttachmentValidator = EndpointAttachmentValidator(),
    private val routeLabelPlacementCompiler: RouteLabelPlacementCompiler = RouteLabelPlacementCompiler(),
    private val presentationConnectorCompiler: PresentationConnectorCompiler = PresentationConnectorCompiler(),
    private val junctionCrossingMarkerCompiler: JunctionCrossingMarkerCompiler = JunctionCrossingMarkerCompiler(),
) {
    fun compile(request: AthenaProfessionalDrawingRequest): AthenaProfessionalDrawingResult {
        val material = materialResolver.resolve(
            repositoryRoot = request.repositoryRoot,
            document = request.document,
            projectionContext = ProjectionContextId(
                request.selectedProjectionPolicy?.materialProjectionContext ?: request.policy.materialProjectionContext,
            ),
        )
        val diagnostics = material.diagnostics.map { diagnostic ->
            diagnostic("material.${diagnostic.code}", diagnostic.subject, diagnostic.message)
        }.toMutableList()
        if (diagnostics.isNotEmpty()) return AthenaProfessionalDrawingResult(diagnostics = diagnostics)

        val compositionResult = sheetCompiler.compile(sheetRequest(request))
        diagnostics += compositionResult.diagnostics.map { diagnostic ->
            diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
        }
        val compositionPlan = compositionResult.plan
        if (compositionPlan == null || diagnostics.isNotEmpty()) {
            return AthenaProfessionalDrawingResult(diagnostics = diagnostics.sorted())
        }

        val placementFacts = layoutDeriver.derive(request.semanticSnapshot).placementFacts
        val occurrences = compileOccurrences(
            document = request.document,
            materials = material.materials,
            placements = placementFacts,
            drawingArea = compositionPlan.drawingAreaBounds.toPresentationBounds(),
            policy = request.policy,
            diagnostics = diagnostics,
        )
        if (diagnostics.isNotEmpty()) return AthenaProfessionalDrawingResult(diagnostics = diagnostics.sorted())

        val routeFacts = route(
            document = request.document,
            occurrences = occurrences,
            snapshotId = placementFacts.first().snapshotId,
            diagnostics = diagnostics,
        )
        if (diagnostics.isNotEmpty()) return AthenaProfessionalDrawingResult(diagnostics = diagnostics.sorted())

        val composition = compositionResult.toPresentationComposition(request.policy, occurrences)
        val placedConnectionLabels = placeConnectionLabels(
            routeFacts = routeFacts,
            occurrences = occurrences,
            composition = composition,
            diagnostics = diagnostics,
        )
        val classifiedLines = when (val profileResolution = drawingProfileCompiler.resolveRoutes(
            profile = DrawingStandardProfile.standardProfessional(),
            routeFacts = placedConnectionLabels.snapshot.routeFacts,
            selectedPolicyId = request.selectedProjectionPolicy?.name ?: request.policy.policyId,
        )) {
            is DrawingProfileResolution.Success -> profileResolution.lines
            is DrawingProfileResolution.Failure -> {
                diagnostics += profileResolution.diagnostics.map { diagnostic ->
                    diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
                }
                emptyList()
            }
        }
        val connectors = presentationConnectorCompiler.compile(
            routeFacts = placedConnectionLabels.snapshot.routeFacts,
            occurrences = occurrences,
            lineEvidence = classifiedLines,
        )
        diagnostics += connectors.diagnostics
        val connectionMarkers = compileConnectionMarkers(
            routeFacts = placedConnectionLabels.snapshot,
            policyId = request.selectedProjectionPolicy?.name ?: request.policy.policyId,
            diagnostics = diagnostics,
        )
        val connectorsWithMarkers = connectors.connectors.withMarkerReferences(connectionMarkers)
        val evidence = validateSurface(
            request = request,
            occurrences = occurrences,
            routeFacts = placedConnectionLabels.snapshot,
            composition = composition,
            classifiedLines = classifiedLines,
            labelEvidence = placedConnectionLabels.evidence,
            diagnostics = diagnostics,
        )
        if (diagnostics.isNotEmpty()) {
            return AthenaProfessionalDrawingResult(diagnostics = diagnostics.sorted(), evidence = evidence)
        }
        val presentationWithoutPaintPlan = PresentationDocument(
            view = request.selectedProjectionPolicy?.toViewDefinition(request.policy) ?: controlDrawingView(request.policy),
            canvasWidth = request.policy.sheetWidth,
            canvasHeight = request.policy.sheetHeight,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            graphicOccurrences = occurrences,
            connectors = connectorsWithMarkers,
            connectionMarkers = connectionMarkers,
            drawingComposition = composition,
        )
        val presentation = presentationWithoutPaintPlan.copy(
            paintPlan = PresentationPaintCompiler().compile(presentationWithoutPaintPlan),
        )
        diagnostics += PresentationPublicationValidator.validate(presentation).map { issue ->
            diagnostic(issue.code, issue.subject, issue.message)
        }
        if (diagnostics.isNotEmpty()) {
            return AthenaProfessionalDrawingResult(diagnostics = diagnostics.sorted(), evidence = evidence)
        }
        return AthenaProfessionalDrawingResult(
            presentation = presentation,
            diagnostics = emptyList(),
            evidence = evidence,
        )
    }

    private fun validateSurface(
        request: AthenaProfessionalDrawingRequest,
        occurrences: List<PresentationGraphicOccurrence>,
        routeFacts: RouteFactSnapshot,
        composition: PresentationDrawingComposition,
        classifiedLines: List<ConnectionPresentationLineEvidence>,
        labelEvidence: List<RouteLabelPlacementEvidence>,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): AthenaProfessionalDrawingEvidence {
        val componentBounds = occurrences.toComponentBounds()
        val endpointResult = endpointAttachmentValidator.validate(
            routeFacts.routeFacts.map { route ->
                EndpointAttachmentRequest.engineeringRoute(
                    routeFact = route,
                    sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source)),
                    targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
                    componentBounds = emptyList(),
                )
            },
        )
        diagnostics += endpointResult.diagnostics.map { diagnostic ->
            diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
        }

        val classifiedRouteIds = classifiedLines.map { line -> line.routeId }.toSet()

        val routeBodyIntersections = routeFacts.routeFacts.flatMap { route ->
            route.segments.mapNotNull { segment ->
                val intersectsBody = componentBounds
                    .filterNot { bounds ->
                        (bounds.subjectId == route.source.subjectId && bounds.occurrenceId == route.source.occurrenceId) ||
                            (bounds.subjectId == route.target.subjectId && bounds.occurrenceId == route.target.occurrenceId)
                    }
                    .any { bounds -> bounds.intersectsInterior(segment) }
                if (intersectsBody) route.routeId.value else null
            }
        }.distinct()
        diagnostics += routeBodyIntersections.map { routeId ->
            diagnostic("drawing.route.body-intersection", routeId, "Route intersects a component body.")
        }

        val exactTerminalAttachment = routeFacts.routeFacts.all { route ->
            route.source.point == route.segments.first().start &&
                route.target.point == route.segments.last().end
        }
        val allRoutesClassified = routeFacts.routeFacts.map { route -> route.routeId }.toSet() == classifiedRouteIds
        val noFallback = routeFacts.routeFacts.all { route ->
            route.quality.isSatisfied &&
                route.quality.failedConstraintIds.isEmpty() &&
                route.source.anchorId.value.contains("fallback", ignoreCase = true).not() &&
                route.target.anchorId.value.contains("fallback", ignoreCase = true).not()
        }
        val routeBodiesClear = routeBodyIntersections.isEmpty()
        val labelsClear = labelEvidence.all { label -> label.collisions.isEmpty() }
        val crossingsClear = routeFacts.crossingFacts.all { crossing -> !crossing.joined }
        val evidence = AthenaProfessionalDrawingEvidence(
            exactTerminalAttachment = exactTerminalAttachment && endpointResult.successful,
            componentAndLabelClearance = routeBodiesClear && labelsClear,
            junctionCrossingSemanticsExplicit = routeFacts.junctionFacts.isNotEmpty() && crossingsClear,
            graphicPrimitiveAuthorityOnly = occurrences.all { occurrence ->
                occurrence.authorities.graphic == "graphic-primitive-ir" &&
                    occurrence.graphic.primitives.isNotEmpty()
            },
            rawMarkupAuthorityAbsent = occurrences.none { occurrence ->
                occurrence.sourceProvenance.any { source ->
                    source.endsWith(".xml") || source.endsWith(".elmt") || source.contains("qelectrotech", ignoreCase = true)
                }
            },
            fallbackAuthorityAbsent = noFallback,
            connectionPresentationClassified = allRoutesClassified,
            looseEndpointsAbsent = endpointResult.successful,
            routeBodyIntersectionsAbsent = routeBodiesClear,
            ambiguousCrossingsAbsent = crossingsClear,
            labelCollisionsAbsent = labelsClear,
            unclassifiedRoutesAbsent = allRoutesClassified,
            rendererEngineeringInference = false,
            trace = buildTrace(
                request = request,
                occurrences = occurrences,
                routeFacts = routeFacts,
                composition = composition,
                classifiedLines = classifiedLines,
                labelEvidence = labelEvidence,
                diagnostics = diagnostics,
            ),
        )
        diagnostics += AthenaProfessionalDrawingTraceValidator.validate(evidence.trace)
        return evidence
    }

    private data class PlacedConnectionLabels(
        val snapshot: RouteFactSnapshot,
        val evidence: List<RouteLabelPlacementEvidence>,
    )

    private fun compileConnectionMarkers(
        routeFacts: RouteFactSnapshot,
        policyId: String,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): List<PresentationConnectionMarker> {
        return when (val result = junctionCrossingMarkerCompiler.compile(
            snapshot = routeFacts,
            profile = DrawingStandardProfile.standardProfessional(),
            selectedPolicyId = policyId,
        )) {
            is JunctionCrossingMarkerCompilation.Success -> result.markers.map { marker -> marker.toPresentationMarker() }
            is JunctionCrossingMarkerCompilation.Failure -> {
                diagnostics += result.diagnostics.map { diagnostic ->
                    diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
                }
                emptyList()
            }
        }
    }

    private fun List<com.engineeringood.athena.presentation.PresentationConnector>.withMarkerReferences(
        markers: List<PresentationConnectionMarker>,
    ): List<com.engineeringood.athena.presentation.PresentationConnector> {
        val markerIdsByConnector = markers
            .flatMap { marker -> marker.connectorIds.map { connectorId -> connectorId to marker.markerId } }
            .groupBy({ it.first }, { it.second })
        return map { connector ->
            connector.copy(
                markerIds = markerIdsByConnector[connector.occurrenceId]
                    .orEmpty()
                    .distinct()
                    .sortedBy { markerId -> markerId.value },
            )
        }
    }

    private fun JunctionCrossingMarker.toPresentationMarker(): PresentationConnectionMarker =
        PresentationConnectionMarker(
            markerId = PresentationConnectionMarkerId(markerId),
            kind = when (kind) {
                JunctionCrossingMarkerKind.JUNCTION_DOT -> PresentationConnectionMarkerKind.JUNCTION
                JunctionCrossingMarkerKind.DISCONNECTED_CROSSING,
                JunctionCrossingMarkerKind.WIRE_HOP,
                -> PresentationConnectionMarkerKind.NO_CONNECT_CROSSING
            },
            point = PresentationPoint(point.x, point.y),
            routeIds = routeIds.sorted(),
            connectorIds = routeIds.map(::PresentationOccurrenceId).sortedBy { connectorId -> connectorId.value },
            semanticId = semanticId?.let(::StableSemanticIdentity),
            joined = joined,
            appearanceClassId = markerClassId,
            sourceProjectionIds = listOf(markerId) + routeIds.sorted(),
            sourceProvenance = listOfNotNull(provenance?.let { source -> "${source.file}:${source.startLine}:${source.startColumn}" })
                .ifEmpty { listOf(markerId) },
            compilerSnapshotId = compilerSnapshotId,
        )

    private fun placeConnectionLabels(
        routeFacts: RouteFactSnapshot,
        occurrences: List<PresentationGraphicOccurrence>,
        composition: PresentationDrawingComposition,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): PlacedConnectionLabels {
        val labelResult = routeLabelPlacementCompiler.compile(
            RouteLabelPlacementRequest(
                snapshot = routeFacts,
                frameBounds = composition.frameBounds.toDrawingBounds(),
                componentBounds = occurrences.map { occurrence -> occurrence.bounds.toDrawingBounds() },
                titleBlockBounds = listOf(composition.titleBlockBounds.toDrawingBounds()),
                textBounds = occurrences.flatMap { occurrence ->
                    occurrence.labels.map { label -> label.bounds.toDrawingBounds().padded(horizontal = 14, vertical = 10) } +
                        occurrence.terminalBindings.map { terminal -> terminal.toTerminalTextBounds() }
                },
            ),
        )
        diagnostics += labelResult.diagnostics.map { diagnostic ->
            diagnostic(diagnostic.code, diagnostic.subject, diagnostic.message)
        }
        val placementById = labelResult.labels.associateBy { label -> label.labelId }
        val placedRoutes = routeFacts.routeFacts.map { route ->
            route.copy(
                labels = route.labels.map { label ->
                    val placed = placementById[label.labelId] ?: return@map label
                    label.copy(
                        placement = label.placement.copy(
                            origin = placed.attachmentPoint,
                        ),
                    )
                },
            )
        }
        return PlacedConnectionLabels(
            snapshot = RouteFactSnapshot.canonical(
                snapshotId = routeFacts.snapshotId,
                family = routeFacts.family,
                routeFacts = placedRoutes,
                junctionFacts = routeFacts.junctionFacts,
                crossingFacts = routeFacts.crossingFacts,
                laneDiagnostics = routeFacts.laneDiagnostics,
            ),
            evidence = labelResult.labels,
        )
    }

    private fun buildTrace(
        request: AthenaProfessionalDrawingRequest,
        occurrences: List<PresentationGraphicOccurrence>,
        routeFacts: RouteFactSnapshot,
        composition: PresentationDrawingComposition,
        classifiedLines: List<ConnectionPresentationLineEvidence>,
        labelEvidence: List<RouteLabelPlacementEvidence>,
        diagnostics: List<AthenaProfessionalDrawingDiagnostic>,
    ): AthenaProfessionalDrawingTrace {
        val linesByRoute = classifiedLines.associateBy { line -> line.routeId.value }
        val sourceSpan = request.defaultSourceSpan()

        val occurrenceTraces = occurrences.map { occurrence ->
            val packageResources = occurrence.sourceProvenance.distinct().sorted()
            AthenaProfessionalOccurrenceTrace(
                occurrenceId = occurrence.occurrenceId.value,
                semanticSubjectId = occurrence.semanticSubjectId,
                physicalComponentId = occurrence.physicalComponentId,
                functionId = occurrence.functionId,
                packageId = occurrence.packageId,
                definitionId = occurrence.definitionId,
                bindingRuleId = occurrence.bindingRuleId,
                packageResourceIds = packageResources,
                anchorIds = occurrence.terminalBindings.map { binding -> binding.anchorId }.distinct().sorted(),
                labelIds = occurrence.labels.map { label -> label.labelId }.distinct().sorted(),
                sourceSpan = packageResources.firstOrNull()?.toSourceSpan() ?: sourceSpan,
            )
        }.sortedBy { occurrence -> occurrence.occurrenceId }

        val routeTraces = routeFacts.routeFacts.map { route ->
            val line = linesByRoute[route.routeId.value]
            AthenaProfessionalRouteTrace(
                routeId = route.routeId.value,
                connectionId = route.connectionId.value,
                routeContractId = route.routeIntentId.value,
                sourcePortSemanticId = route.source.portSemanticId?.value ?: route.source.portId.value,
                targetPortSemanticId = route.target.portSemanticId?.value ?: route.target.portId.value,
                sourceAnchorId = route.source.anchorId.value,
                targetAnchorId = route.target.anchorId.value,
                laneId = route.laneAssignment.laneId.value,
                routeLabelIds = route.labels.map { label -> label.labelId.value }.distinct().sorted(),
                lineClassId = line?.lineClassId?.value.orEmpty(),
                projectionPolicyId = line?.selectedPolicyId ?: request.selectedProjectionPolicy?.name ?: request.policy.policyId,
                compilerSnapshotId = route.compilerSnapshotId,
                sourceSpan = route.provenance.toProfessionalSourceSpan(),
            )
        }.sortedBy { route -> route.routeId }

        val labelTraces = labelEvidence.map { label ->
            AthenaProfessionalRouteLabelTrace(
                labelId = label.labelId.value,
                routeId = label.labelId.value.removePrefix("label:"),
                bounds = label.bounds,
                sourceSpan = label.provenance.toProfessionalSourceSpan(),
            )
        }.sortedBy { label -> label.labelId }

        val markerTraces = routeFacts.junctionFacts.map { junction ->
            AthenaProfessionalRouteMarkerTrace(
                markerId = junction.junctionId,
                kind = "junction",
                routeIds = junction.routeIds.map { routeId -> routeId.value }.sorted(),
                sourceSpan = sourceSpan,
            )
        } + routeFacts.crossingFacts.map { crossing ->
            AthenaProfessionalRouteMarkerTrace(
                markerId = crossing.crossingId,
                kind = "crossing",
                routeIds = crossing.routeIds.map { routeId -> routeId.value }.sorted(),
                sourceSpan = sourceSpan,
            )
        }

        val sheetStructureTraces = composition.structureFacts.map { structure ->
            AthenaProfessionalSheetStructureTrace(
                structureId = structure.factId,
                kind = structure.kind,
                memberIds = structure.memberIds.sorted(),
                sourceSpan = sourceSpan,
            )
        }.sortedBy { structure -> structure.structureId }

        val forbidden = forbiddenAuthorityKinds(
            occurrenceTraces = occurrenceTraces,
            routes = routeFacts.routeFacts,
        )

        return AthenaProfessionalDrawingTrace(
            occurrences = occurrenceTraces,
            routes = routeTraces,
            connectionLabels = labelTraces,
            routeMarkers = markerTraces.sortedBy { marker -> marker.markerId },
            sheetStructures = sheetStructureTraces,
            evidenceInputs = evidenceInputs(
                occurrences = occurrenceTraces,
                routes = routeTraces,
                connectionLabels = labelTraces,
                routeMarkers = markerTraces,
                sheetStructures = sheetStructureTraces,
                diagnostics = diagnostics,
            ),
            forbiddenAuthorityKinds = forbidden,
        )
    }

    private fun compileOccurrences(
        document: EngineeringDocument,
        materials: List<AthenaResolvedRepresentationMaterial>,
        placements: List<SchematicPlacementFact>,
        drawingArea: PresentationDrawingBounds,
        policy: AthenaProfessionalDrawingPolicy,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): List<PresentationGraphicOccurrence> {
        val placementsBySubject = placements.associateBy { placement -> placement.materialSubjectId() }
        val duplicatePlacementSubjects = placements.groupingBy { placement -> placement.materialSubjectId() }.eachCount().filterValues { it > 1 }
        duplicatePlacementSubjects.keys.sorted().forEach { subject ->
            diagnostics += diagnostic("drawing.placement.duplicate", subject, "Professional drawing placement subject must be unique.")
        }
        val components = document.components.associateBy { component -> component.id.value }
        val ports = document.ports.associateBy { port -> port.id.value }
        return materials.sortedBy { material -> material.semanticSubjectId }.mapNotNull { material ->
            val placement = placementsBySubject[material.semanticSubjectId]
            if (placement == null) {
                diagnostics += diagnostic("drawing.placement.missing", material.semanticSubjectId, "Resolved material has no exact semantic placement.")
                return@mapNotNull null
            }
            val grid = placement.gridPosition
            val orientation = placement.orientation
            if (grid == null || orientation == null) {
                diagnostics += diagnostic("drawing.placement.grid-missing", material.semanticSubjectId, "Professional drawing placement requires explicit grid and orientation.")
                return@mapNotNull null
            }
            if (grid.column !in 1..policy.columnLabels.size || grid.row !in 1..policy.rowLabels.size) {
                diagnostics += diagnostic("drawing.placement.grid-out-of-range", material.semanticSubjectId, "Professional drawing placement is outside the policy grid.")
                return@mapNotNull null
            }
            val compiled = AthenaProfessionalGraphicOccurrenceCompiler.compile(
                occurrenceId = material.semanticSubjectId,
                material = material,
                gridPosition = grid,
                orientation = orientation,
                drawingArea = drawingArea,
                policy = policy,
            )
            val placedAnchors = material.definition.anchors.mapNotNull { anchor ->
                val point = compiled.anchors[anchor.anchorId.value]
                if (point == null) {
                    diagnostics += diagnostic("drawing.anchor.missing", anchor.anchorId.value, "Compiled occurrence is missing placed Anchor evidence.")
                    null
                } else {
                    PresentationPlacedAnchor(
                        anchorId = anchor.anchorId,
                        geometryRef = anchor.geometryRef,
                        primitiveId = anchor.primitiveId,
                        point = SchematicRoutePoint(point.x.roundToInt(), point.y.roundToInt()),
                        role = anchor.role,
                        required = anchor.required,
                        sourceProvenance = listOf(material.definition.lifecycle.provenance.source) +
                            material.definition.graphicBody.provenanceSources,
                    )
                }
            }
            val placedAnchorsById = placedAnchors.associateBy { anchor -> anchor.anchorId.value }
            val component = components[material.physicalComponentId]
            if (component == null) {
                diagnostics += diagnostic("drawing.component.missing", material.physicalComponentId, "Material physical component is not present in the engineering document.")
                return@mapNotNull null
            }
            val terminalBindings = material.terminalBindings.mapNotNull { (portSemanticId, terminalIdentity) ->
                val anchorId = material.resolution.anchorMapping[portSemanticId]?.value
                val placedAnchor = placedAnchorsById[anchorId]
                val port = ports[portSemanticId]
                if (anchorId == null || placedAnchor == null || port == null) {
                    diagnostics += diagnostic("drawing.anchor.missing", "$portSemanticId@$terminalIdentity", "Terminal binding cannot resolve exact transformed anchor.")
                    null
                } else {
                    val side = terminalSide(placedAnchor.point.toGraphicPoint(), compiled.bounds)
                    PresentationGraphicTerminalBinding(
                        portSemanticId = portSemanticId,
                        bindingId = RepresentationPortAnchorBindingId(
                            "binding:${material.resolution.bindingRuleId?.value ?: material.semanticSubjectId}:$portSemanticId:$anchorId",
                        ),
                        anchorId = anchorId,
                        terminalIdentity = terminalIdentity,
                        point = placedAnchor.point,
                        labelPoint = placedAnchor.point.terminalLabelPoint(side),
                        side = side,
                    )
                }
            }.staggerTerminalLabels().sortedBy { binding -> binding.portSemanticId }
            PresentationGraphicOccurrence(
                occurrenceId = RepresentationOccurrenceId("drawing:$${material.semanticSubjectId}".replace("$", "")),
                semanticSubjectId = material.semanticSubjectId,
                physicalComponentId = material.physicalComponentId,
                functionId = material.functionId,
                bounds = compiled.bounds,
                orientation = orientation,
                deviceLabel = component.propertyText("tag") ?: component.name,
                modelLabel = component.propertyText("model"),
                packageId = material.definition.libraryId.value,
                definitionId = material.definition.symbolId.value,
                bindingRuleId = material.resolution.bindingRuleId?.value ?: "unbound",
                graphic = compiled.graphic,
                placedAnchors = placedAnchors.sortedBy { anchor -> anchor.anchorId.value },
                terminalBindings = terminalBindings,
                labels = labelsFor(material.semanticSubjectId, component, compiled.labelSlots, compiled.bounds),
                sourceProvenance = material.definition.graphicBody.provenanceSources +
                    material.definition.lifecycle.provenance.source,
            )
        }.sortedBy { occurrence -> occurrence.occurrenceId.value }
    }

    private fun route(
        document: EngineeringDocument,
        occurrences: List<PresentationGraphicOccurrence>,
        snapshotId: com.engineeringood.athena.layout.LayoutSnapshotId,
        diagnostics: MutableList<AthenaProfessionalDrawingDiagnostic>,
    ): RouteFactSnapshot {
        val terminals = occurrences.flatMap { occurrence ->
            occurrence.terminalBindings.map { terminal -> (terminal.portSemanticId to occurrence) to terminal }
        }
        val terminalByPort = terminals.groupBy({ it.first.first }, { it.first.second to it.second })
        val ports = document.ports.associateBy { port -> port.id.value }
        val classifier = ElectricalConnectionRoleClassifier()
        val requests = document.connections.sortedBy { connection -> connection.id.value }.mapNotNull { connection ->
            val sourcePortId = connection.from.resolvedIdentity?.value
            val targetPortId = connection.to.resolvedIdentity?.value
            val sourceCandidates = sourcePortId?.let { terminalByPort[it].orEmpty() }.orEmpty()
            val targetCandidates = targetPortId?.let { terminalByPort[it].orEmpty() }.orEmpty()
            if (sourcePortId == null || targetPortId == null || sourceCandidates.size != 1 || targetCandidates.size != 1) {
                diagnostics += diagnostic("drawing.route.endpoint.unresolved", connection.id.value, "Route endpoint must resolve to exactly one compiled terminal anchor.")
                return@mapNotNull null
            }
            val sourcePort = ports[sourcePortId]
            val targetPort = ports[targetPortId]
            if (sourcePort == null || targetPort == null) {
                diagnostics += diagnostic("drawing.route.port.missing", connection.id.value, "Route endpoint port is missing from Engineering IR.")
                return@mapNotNull null
            }
            val (sourceOccurrence, sourceTerminal) = sourceCandidates.single()
            val (targetOccurrence, targetTerminal) = targetCandidates.single()
            val sourceRef = sourcePort.toConnectionPortRef(sourceOccurrence)
            val targetRef = targetPort.toConnectionPortRef(targetOccurrence)
            val connectionRoleFact = classifier.classify(
                ElectricalConnectionRoleInput(
                    connectionId = ElectricalConnectionId(connection.id.value),
                    sourcePort = sourceRef,
                    targetPort = targetRef,
                    sourceSpan = connection.provenance.toLayoutSourceSpanOrNull(),
                ),
            )
            AthenaRouteRequest(
                routeId = SchematicRouteId("route:${connection.id.value}"),
                connectionRoleFact = connectionRoleFact,
                sourceAnchor = sourceTerminal.toTerminalAnchor(sourceOccurrence),
                targetAnchor = targetTerminal.toTerminalAnchor(targetOccurrence),
                constraints = ProfessionalDrawingRouteHardRules.constraintsFor(connection),
            )
        }
        return routeEngine.solve(
            AthenaRouteEngineInput(
                snapshotId = snapshotId,
                layoutContext = SchematicRoutingLayoutContext(gridSize = 1),
                componentBounds = occurrences.toComponentBounds(),
                requests = requests,
            ),
        )
    }

}

private fun AthenaProfessionalDrawingRequest.defaultSourceSpan(): AthenaProfessionalSourceSpan {
    val sourceUnit = semanticSnapshot.sourceUnits.firstOrNull()
    return AthenaProfessionalSourceSpan(
        file = sourceUnit?.sourceRootRelativePath ?: "unknown.athena",
        startLine = 1,
        startColumn = 1,
        endLine = 1,
        endColumn = 1,
    )
}

private fun String.toSourceSpan(): AthenaProfessionalSourceSpan = AthenaProfessionalSourceSpan(
    file = this,
    startLine = 1,
    startColumn = 1,
    endLine = 1,
    endColumn = 1,
)

private fun SourceProvenance.toProfessionalSourceSpan(): AthenaProfessionalSourceSpan = AthenaProfessionalSourceSpan(
    file = file,
    startLine = startLine,
    startColumn = startColumn,
    endLine = endLine,
    endColumn = endColumn,
)

private fun forbiddenAuthorityKinds(
    occurrenceTraces: List<AthenaProfessionalOccurrenceTrace>,
    routes: List<RouteFact>,
): List<String> {
    val scanned = occurrenceTraces.flatMap { occurrence -> occurrence.packageResourceIds } +
        routes.flatMap { route -> listOf(route.plannerId, route.provenance.file) }
    val forbidden = listOf("<svg", "<xml", "<definition", ".elmt", "qelectrotech", "org.eclipse.elk", "DOM")
    return scanned.flatMap { value ->
        forbidden.filter { token -> value.contains(token, ignoreCase = true) }
    }.distinct().sorted()
}

private fun evidenceInputs(
    occurrences: List<AthenaProfessionalOccurrenceTrace>,
    routes: List<AthenaProfessionalRouteTrace>,
    connectionLabels: List<AthenaProfessionalRouteLabelTrace>,
    routeMarkers: List<AthenaProfessionalRouteMarkerTrace>,
    sheetStructures: List<AthenaProfessionalSheetStructureTrace>,
    diagnostics: List<AthenaProfessionalDrawingDiagnostic>,
): List<AthenaProfessionalEvidenceInputTrace> {
    val occurrenceIds = occurrences.map { occurrence -> occurrence.occurrenceId }
    val routeIds = routes.map { route -> route.routeId }
    val labelIds = connectionLabels.map { label -> label.labelId }
    val markerIds = routeMarkers.map { marker -> marker.markerId }
    val structureIds = sheetStructures.map { structure -> structure.structureId }
    val routeAndAnchorIds = routes.flatMap { route ->
        listOf(route.routeId, route.sourceAnchorId, route.targetAnchorId, route.laneId, route.lineClassId)
    }.filter { id -> id.isNotBlank() }
    return listOf(
        evidenceInput("exact-terminal-attachment", routeAndAnchorIds, diagnostics),
        evidenceInput("component-and-label-clearance", occurrenceIds + routeIds + labelIds, diagnostics),
        evidenceInput("junction-crossing-semantics-explicit", routeIds + markerIds.ifEmpty { structureIds }, diagnostics),
        evidenceInput("graphic-primitive-authority-only", occurrenceIds, diagnostics),
        evidenceInput("raw-markup-authority-absent", occurrenceIds + routeIds, diagnostics),
        evidenceInput("fallback-authority-absent", routeIds, diagnostics),
        evidenceInput("connection-presentation-classified", routes.map { route -> route.lineClassId }.filter(String::isNotBlank), diagnostics),
        evidenceInput("loose-endpoints-absent", routeAndAnchorIds, diagnostics),
        evidenceInput("route-body-intersections-absent", routeIds + occurrenceIds, diagnostics),
        evidenceInput("ambiguous-crossings-absent", markerIds.ifEmpty { routeIds }, diagnostics),
        evidenceInput("label-collisions-absent", labelIds, diagnostics),
        evidenceInput("unclassified-routes-absent", routeIds, diagnostics),
        evidenceInput("renderer-engineering-inference-absent", structureIds + routeIds + occurrenceIds, diagnostics),
    )
}

private fun evidenceInput(
    evidenceId: String,
    evidenceIds: List<String>,
    diagnostics: List<AthenaProfessionalDrawingDiagnostic>,
): AthenaProfessionalEvidenceInputTrace = AthenaProfessionalEvidenceInputTrace(
    evidenceId = evidenceId,
    evidenceIds = evidenceIds.distinct().sorted(),
    diagnosticCodes = diagnostics.map { diagnostic -> diagnostic.code }.distinct().sorted(),
    constant = false,
)

private fun sheetRequest(request: AthenaProfessionalDrawingRequest): DrawingSheetCompositionRequest {
    val policy = request.policy
    val sheetId = ProjectionSheetId("schematic/sheet/control-drawing")
    val sourceName = request.semanticSnapshot.sourceUnits.firstOrNull()?.sourceRootRelativePath ?: "unknown.athena"
    val sourceDisplay = request.repositoryRoot.resolve("src").resolve(sourceName).normalize().toString().replace('\\', '/')
    return DrawingSheetCompositionRequest(
        sheetId = sheetId,
        publication = ProjectionSheetPublication(
            pageSize = ProjectionSheetPageSize("A3", "landscape"),
            frame = ProjectionSheetFrame("professional-control-frame", "iec-control-drawing"),
            coordinateZones = listOf(
                ProjectionSheetCoordinateZone("power-region", "Power", 0),
                ProjectionSheetCoordinateZone("control-region", "Control", 1),
                ProjectionSheetCoordinateZone("title-block", "Title Block", 2),
            ),
            titleBlock = ProjectionSheetTitleBlock(policy.title, "Control Drawing", "1"),
            revisionMetadata = ProjectionSheetRevisionMetadata("A", "Initial professional control drawing"),
            viewComposition = ProjectionSheetViewComposition("schematic", 0),
        ),
        contentBounds = GraphicBounds(
            x = (policy.frameToSheet + policy.coordinateBandSize).toDouble(),
            y = (policy.frameToSheet + policy.coordinateBandSize).toDouble(),
            width = 1.0,
            height = 1.0,
        ),
        policy = DrawingSheetCompositionPolicy(
            policyId = policy.policyId,
            contentToFrame = 0.0,
            frameToSheet = policy.frameToSheet.toDouble(),
            titleBlockHeight = policy.titleBlockHeight.toDouble(),
            maximumSheetWidth = policy.sheetWidth.toDouble(),
            maximumSheetHeight = policy.sheetHeight.toDouble(),
            columnLabels = policy.columnLabels,
            rowLabels = policy.rowLabels,
            fixedSheetBounds = GraphicBounds(0.0, 0.0, policy.sheetWidth.toDouble(), policy.sheetHeight.toDouble()),
            coordinateBandSize = policy.coordinateBandSize.toDouble(),
        ),
        titleFields = listOf(
            DrawingSheetTitleFieldInput("author", "Author", policy.author),
            DrawingSheetTitleFieldInput("title", "Title", policy.title),
            DrawingSheetTitleFieldInput("file", "File", sourceDisplay),
            DrawingSheetTitleFieldInput("date", "Date", policy.publicationDate),
            DrawingSheetTitleFieldInput("folio", "Folio", policy.folio),
        ),
    )
}

private fun DrawingSheetCompositionResult.toPresentationComposition(
    policy: AthenaProfessionalDrawingPolicy,
    occurrences: List<PresentationGraphicOccurrence>,
): PresentationDrawingComposition {
    val plan = requireNotNull(plan)
    val evidence = requireNotNull(evidence)
    val drawingArea = evidence.drawingAreaBounds.toPresentationBounds()
    val columnWidth = drawingArea.width / policy.columnLabels.size
    val structureFacts = professionalStructureFacts(policy, drawingArea, occurrences)
    return PresentationDrawingComposition(
        sheetId = plan.sheetId,
        policyId = evidence.policyId,
        contentBounds = evidence.contentBounds.toPresentationBounds(),
        frameBounds = evidence.frameBounds.toPresentationBounds(),
        drawingAreaBounds = drawingArea,
        titleBlockBounds = evidence.titleBlockBounds.toPresentationBounds(),
        sheetBounds = evidence.sheetBounds.toPresentationBounds(),
        frameId = plan.frame.frameId,
        frameStyle = plan.frame.style,
        title = PresentationDrawingTitle(
            sheetTitle = plan.titleBlock.sheetTitle,
            sheetFamily = plan.titleBlock.sheetFamily,
            sheetNumber = plan.titleBlock.sheetNumber,
            revisionCode = plan.titleBlock.revisionCode,
            revisionNote = plan.titleBlock.revisionNote,
            pageFormat = plan.titleBlock.pageFormat,
            orientation = plan.titleBlock.orientation,
            fields = plan.titleBlock.fields.map { field ->
                PresentationDrawingTitleField(
                    fieldId = field.fieldId,
                    label = field.label,
                    value = field.value,
                    bounds = field.bounds.toPresentationBounds(),
                )
            },
        ),
        coordinateZones = plan.coordinateZones.map { zone ->
            PresentationDrawingCoordinateZone(
                zoneId = zone.zoneId,
                axis = zone.axis.name,
                label = zone.label,
                order = zone.order,
                bounds = zone.bounds.toPresentationBounds(),
            )
        },
        structureSubjects = occurrences.map { occurrence ->
            PresentationDrawingStructureSubject(
                subjectId = occurrence.occurrenceId.value,
                representationIdentity = occurrence.definitionId,
                bounds = occurrence.bounds,
                representationAuthority = "graphic-primitive-ir",
                boundsAuthority = "semantic-layout-facts",
            )
        },
        structureFacts = listOf(
            PresentationDrawingStructureFact(
                factId = "power-region",
                kind = "drawing-region",
                axis = "COLUMN",
                bounds = PresentationDrawingBounds(drawingArea.x, drawingArea.y, columnWidth * policy.powerRegionColumnCount, drawingArea.height),
                start = null,
                end = null,
                memberIds = occurrences.filter { occurrence -> occurrence.bounds.x < drawingArea.x + columnWidth * policy.powerRegionColumnCount }.map { it.occurrenceId.value },
                authority = "drawing-composition",
                boundsAuthority = "presentation-profile-policy",
            ),
            PresentationDrawingStructureFact(
                factId = "control-region",
                kind = "drawing-region",
                axis = "COLUMN",
                bounds = PresentationDrawingBounds(
                    drawingArea.x + columnWidth * policy.powerRegionColumnCount,
                    drawingArea.y,
                    drawingArea.width - columnWidth * policy.powerRegionColumnCount,
                    drawingArea.height,
                ),
                start = null,
                end = null,
                memberIds = occurrences.filter { occurrence -> occurrence.bounds.x >= drawingArea.x + columnWidth * policy.powerRegionColumnCount }.map { it.occurrenceId.value },
                authority = "drawing-composition",
                boundsAuthority = "presentation-profile-policy",
            ),
        ) + structureFacts,
        referencePlacements = professionalReferencePlacements(drawingArea),
        authorities = PresentationDrawingAuthorities(
            contentBounds = evidence.contentBoundsAuthority,
            bounds = evidence.boundsAuthority,
            projection = evidence.projectionAuthority,
            representation = "graphic-primitive-ir",
            structure = "semantic-layout-facts",
            policy = evidence.policyAuthority,
        ),
    )
}

private fun professionalStructureFacts(
    policy: AthenaProfessionalDrawingPolicy,
    drawingArea: PresentationDrawingBounds,
    occurrences: List<PresentationGraphicOccurrence>,
): List<PresentationDrawingStructureFact> {
    val columnWidth = drawingArea.width / policy.columnLabels.size
    val rowHeight = drawingArea.height / policy.rowLabels.size
    val powerBoundary = drawingArea.x + columnWidth * policy.powerRegionColumnCount
    val allOccurrenceIds = occurrences.map { occurrence -> occurrence.occurrenceId.value }.sorted()
    val terminalOccurrences = occurrences
        .filter { occurrence ->
            occurrence.definitionId.contains("terminal", ignoreCase = true) ||
                occurrence.physicalComponentId.contains("XT", ignoreCase = true) ||
                occurrence.deviceLabel.startsWith("XT")
        }
        .ifEmpty { occurrences.takeLast(1) }
    val terminalOccurrenceIds = terminalOccurrences.map { occurrence -> occurrence.occurrenceId.value }.sorted()
    val controlOccurrenceIds = occurrences
        .filter { occurrence -> occurrence.bounds.x >= powerBoundary }
        .map { occurrence -> occurrence.occurrenceId.value }
        .sorted()
    val labelIds = occurrences.flatMap { occurrence -> occurrence.labels.map { label -> label.labelId } }.sorted()
    val anchorMemberIds = occurrences
        .flatMap { occurrence ->
            occurrence.terminalBindings.map { terminal -> "${occurrence.occurrenceId.value}:${terminal.anchorId}" }
        }
        .sorted()
    val terminalStripBounds = terminalOccurrences.map { occurrence -> occurrence.bounds }.union().expandWithin(10, drawingArea)

    return listOf(
        PresentationDrawingStructureFact(
            factId = "rail:power",
            kind = "rail",
            axis = "HORIZONTAL",
            bounds = null,
            start = PresentationDrawingPoint(drawingArea.x + 18, drawingArea.y + rowHeight),
            end = PresentationDrawingPoint(powerBoundary - 18, drawingArea.y + rowHeight),
            memberIds = allOccurrenceIds.filter { id -> id.contains("Power", ignoreCase = true) || id.contains("Breaker", ignoreCase = true) || id.contains("Disconnect", ignoreCase = true) },
            authority = "semantic-layout-facts",
            boundsAuthority = null,
        ),
        PresentationDrawingStructureFact(
            factId = "lane:power",
            kind = "lane",
            axis = "VERTICAL",
            bounds = PresentationDrawingBounds(drawingArea.x, drawingArea.y, columnWidth * policy.powerRegionColumnCount, drawingArea.height),
            start = null,
            end = null,
            memberIds = allOccurrenceIds.filterNot(controlOccurrenceIds::contains),
            authority = "semantic-layout-facts",
            boundsAuthority = "presentation-profile-policy",
        ),
        PresentationDrawingStructureFact(
            factId = "lane:control",
            kind = "lane",
            axis = "VERTICAL",
            bounds = PresentationDrawingBounds(powerBoundary, drawingArea.y, drawingArea.x + drawingArea.width - powerBoundary, drawingArea.height),
            start = null,
            end = null,
            memberIds = controlOccurrenceIds,
            authority = "semantic-layout-facts",
            boundsAuthority = "presentation-profile-policy",
        ),
        PresentationDrawingStructureFact(
            factId = "terminal-strip:field",
            kind = "terminal-strip",
            axis = null,
            bounds = terminalStripBounds,
            start = null,
            end = null,
            memberIds = terminalOccurrenceIds,
            authority = "semantic-layout-facts",
            boundsAuthority = "graphic-primitive-ir",
        ),
        PresentationDrawingStructureFact(
            factId = "label-band:device-tags",
            kind = "label-band",
            axis = null,
            bounds = PresentationDrawingBounds(drawingArea.x + 8, drawingArea.y + 8, drawingArea.width - 16, rowHeight.coerceAtLeast(32)),
            start = null,
            end = null,
            memberIds = labelIds,
            authority = "semantic-layout-facts",
            boundsAuthority = "presentation-profile-policy",
        ),
        PresentationDrawingStructureFact(
            factId = "route-channel:control-wiring",
            kind = "route-channel",
            axis = "HORIZONTAL",
            bounds = PresentationDrawingBounds(powerBoundary - 18, drawingArea.y + rowHeight, drawingArea.width - (powerBoundary - drawingArea.x), rowHeight * 5),
            start = null,
            end = null,
            memberIds = anchorMemberIds,
            authority = "semantic-layout-facts",
            boundsAuthority = "route-facts",
        ),
    )
}

private fun professionalReferencePlacements(
    drawingArea: PresentationDrawingBounds,
): List<PresentationDrawingReferencePlacement> {
    val bounds = PresentationDrawingBounds(
        x = drawingArea.x + drawingArea.width - 34,
        y = drawingArea.y + 24,
        width = 24,
        height = 16,
    )
    return listOf(
        PresentationDrawingReferencePlacement(
            placementId = "reference:folio-continuation:control",
            referenceId = "folio-continuation:control",
            subjectId = "drawing:control-continuation",
            role = "folio-continuation",
            representationIdentity = "iec.folio-continuation-reference",
            bounds = bounds,
            anchor = PresentationDrawingPoint(bounds.x + bounds.width, bounds.y + bounds.height / 2),
            compactNotation = "2/1",
            graphicBody = referenceGraphicBody(bounds),
        ),
    )
}

private fun referenceGraphicBody(bounds: PresentationDrawingBounds): GraphicPrimitiveDocument =
    GraphicPrimitiveDocument(
        documentId = GraphicPrimitiveDocumentId("iec.folio-continuation-reference"),
        bounds = GraphicBounds(0.0, 0.0, bounds.width.toDouble(), bounds.height.toDouble()),
        primitives = listOf(
            GraphicPrimitive.Rectangle(
                primitiveId = GraphicPrimitiveId("reference-box"),
                bounds = GraphicBounds(0.0, 0.0, bounds.width.toDouble(), bounds.height.toDouble()),
                cornerRadius = 0.0,
                styleTokenId = GraphicStyleTokenId("reference.stroke"),
            ),
            GraphicPrimitive.Line(
                primitiveId = GraphicPrimitiveId("reference-arrow"),
                bounds = GraphicBounds((bounds.width - 8).toDouble(), (bounds.height / 2).toDouble(), 8.0, 0.001),
                start = GraphicPoint((bounds.width - 8).toDouble(), (bounds.height / 2).toDouble()),
                end = GraphicPoint(bounds.width.toDouble(), (bounds.height / 2).toDouble()),
                styleTokenId = GraphicStyleTokenId("reference.stroke"),
            ),
        ),
        styleTokens = listOf(
            GraphicStyleToken(
                styleTokenId = GraphicStyleTokenId("reference.stroke"),
                stroke = GraphicPaintToken("foreground"),
                strokeWidth = 1.0,
                fill = GraphicFill.TRANSPARENT,
                lineCap = GraphicLineCap.BUTT,
                lineJoin = GraphicLineJoin.MITER,
            ),
        ),
        provenanceSources = listOf("professional-drawing-reference"),
    )

private fun List<PresentationDrawingBounds>.union(): PresentationDrawingBounds {
    if (isEmpty()) return PresentationDrawingBounds(0, 0, 1, 1)
    val minX = minOf { it.x }
    val minY = minOf { it.y }
    val maxX = maxOf { it.x + it.width }
    val maxY = maxOf { it.y + it.height }
    return PresentationDrawingBounds(minX, minY, (maxX - minX).coerceAtLeast(1), (maxY - minY).coerceAtLeast(1))
}

private fun PresentationDrawingBounds.expandWithin(
    padding: Int,
    container: PresentationDrawingBounds,
): PresentationDrawingBounds {
    val left = (x - padding).coerceAtLeast(container.x)
    val top = (y - padding).coerceAtLeast(container.y)
    val right = (x + width + padding).coerceAtMost(container.x + container.width)
    val bottom = (y + height + padding).coerceAtMost(container.y + container.height)
    return PresentationDrawingBounds(left, top, (right - left).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
}

private fun labelsFor(
    occurrenceId: String,
    component: EngineeringComponent,
    labelSlots: Map<String, GraphicBounds>,
    bounds: PresentationDrawingBounds,
): List<PresentationGraphicLabel> {
    val tag = component.propertyText("tag") ?: component.name
    val model = component.propertyText("model")
    return buildList {
        add(
            PresentationGraphicLabel(
                labelId = "$occurrenceId:label:tag",
                role = "device-tag",
                value = tag,
                bounds = (labelSlots["tag"] ?: GraphicBounds(bounds.x.toDouble(), (bounds.y - 12).coerceAtLeast(0).toDouble(), bounds.width.toDouble(), 10.0)).toPresentationBounds(),
            ),
        )
        if (model != null) {
            add(
                PresentationGraphicLabel(
                    labelId = "$occurrenceId:label:model",
                    role = "model",
                    value = model,
                    bounds = (labelSlots["model"] ?: GraphicBounds(bounds.x.toDouble(), (bounds.y + bounds.height + 2).toDouble(), bounds.width.toDouble(), 10.0)).toPresentationBounds(),
                ),
            )
        }
    }
}

private fun SchematicPlacementFact.materialSubjectId(): String {
    val authoredSubject = intentId.value.removePrefix("intent:layout:schematic:")
    return if ('.' in authoredSubject) {
        "function:$authoredSubject"
    } else {
        "component:$authoredSubject"
    }
}

private fun PresentationGraphicTerminalBinding.toTerminalAnchor(
    occurrence: PresentationGraphicOccurrence,
): TerminalAnchorFact = TerminalAnchorFact(
    anchorId = TerminalAnchorId("${occurrence.occurrenceId.value}:$anchorId"),
    subjectId = StableSemanticIdentity(occurrence.semanticSubjectId),
    occurrenceId = LayoutOccurrenceId(occurrence.occurrenceId.value),
    portId = ElectricalPortId(portSemanticId),
    portSemanticId = StableSemanticIdentity(portSemanticId),
    portRole = portRole(),
    side = side,
    point = point,
    gridPoint = point,
    policySource = "m34-professional-drawing",
    )

private fun List<PresentationGraphicOccurrence>.toComponentBounds(): List<SchematicComponentBounds> =
    map { occurrence ->
        SchematicComponentBounds(
            subjectId = StableSemanticIdentity(occurrence.semanticSubjectId),
            occurrenceId = LayoutOccurrenceId(occurrence.occurrenceId.value),
            topLeft = SchematicRoutePoint(occurrence.bounds.x, occurrence.bounds.y),
            width = occurrence.bounds.width,
            height = occurrence.bounds.height,
        )
    }

private fun PresentationDrawingBounds.toDrawingBounds(): DrawingBounds = DrawingBounds(x, y, width, height)

private fun SchematicRoutePoint.toGraphicPoint(): GraphicPoint = GraphicPoint(x.toDouble(), y.toDouble())

private fun PresentationGraphicTerminalBinding.toTerminalTextBounds(): DrawingBounds {
    return DrawingBounds(
        x = (labelPoint.x - 14).coerceAtLeast(0),
        y = (labelPoint.y - 10).coerceAtLeast(0),
        width = (terminalIdentity.length * 6).coerceAtLeast(6) + 28,
        height = 30,
    )
}

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

private fun DrawingBounds.padded(horizontal: Int, vertical: Int): DrawingBounds {
    val x = (this.x - horizontal).coerceAtLeast(0)
    val y = (this.y - vertical).coerceAtLeast(0)
    return DrawingBounds(
        x = x,
        y = y,
        width = width + (this.x - x) + horizontal,
        height = height + (this.y - y) + vertical,
    )
}

private fun SchematicComponentBounds.intersectsInterior(
    segment: com.engineeringood.athena.routing.SchematicRouteSegment,
): Boolean {
    val minX = topLeft.x
    val maxX = topLeft.x + width
    val minY = topLeft.y
    val maxY = topLeft.y + height
    return when (segment.orientation) {
        com.engineeringood.athena.routing.SchematicRouteSegmentOrientation.HORIZONTAL -> {
            val y = segment.start.y
            val segmentMinX = minOf(segment.start.x, segment.end.x)
            val segmentMaxX = maxOf(segment.start.x, segment.end.x)
            y > minY && y < maxY && segmentMaxX > minX && segmentMinX < maxX
        }
        com.engineeringood.athena.routing.SchematicRouteSegmentOrientation.VERTICAL -> {
            val x = segment.start.x
            val segmentMinY = minOf(segment.start.y, segment.end.y)
            val segmentMaxY = maxOf(segment.start.y, segment.end.y)
            x > minX && x < maxX && segmentMaxY > minY && segmentMinY < maxY
        }
    }
}

private fun PresentationGraphicTerminalBinding.portRole(): ElectricalPortRole =
    when (side) {
        TerminalSide.TOP,
        TerminalSide.LEFT,
        -> ElectricalPortRole.INPUT
        TerminalSide.RIGHT,
        TerminalSide.BOTTOM,
        -> ElectricalPortRole.OUTPUT
    }

private fun EngineeringPort.toConnectionPortRef(
    occurrence: PresentationGraphicOccurrence,
): ElectricalConnectionPortRef = ElectricalConnectionPortRef(
    subjectId = StableSemanticIdentity(occurrence.semanticSubjectId),
    portSemanticId = id,
    portId = ElectricalPortId(id.value),
    endpointKind = if (occurrence.definitionId.contains("terminal", ignoreCase = true)) {
        ElectricalConnectionEndpointKind.TERMINAL
    } else if (occurrence.definitionId.contains("motor", ignoreCase = true)) {
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

private fun terminalSide(point: GraphicPoint, bounds: PresentationDrawingBounds): TerminalSide {
    val distances = listOf(
        TerminalSide.LEFT to abs(point.x - bounds.x),
        TerminalSide.RIGHT to abs(point.x - (bounds.x + bounds.width)),
        TerminalSide.TOP to abs(point.y - bounds.y),
        TerminalSide.BOTTOM to abs(point.y - (bounds.y + bounds.height)),
    )
    return distances.minBy { it.second }.first
}

private fun EngineeringComponent.propertyText(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.asText()

private fun EngineeringPort.propertyText(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.asText()

private fun EngineeringConnection.propertyText(name: String): String? =
    properties.firstOrNull { property -> property.name == name }?.value?.asText()

private fun EngineeringPropertyValue.asText(): String = when (this) {
    is EngineeringPropertyValue.Symbol -> text
    is EngineeringPropertyValue.Text -> text
}

private fun GraphicBounds.toPresentationBounds() = PresentationDrawingBounds(
    x = x.roundToInt(),
    y = y.roundToInt(),
    width = width.roundToInt().coerceAtLeast(1),
    height = height.roundToInt().coerceAtLeast(1),
)

private fun AthenaProjectionPolicySelection.toViewDefinition(policy: AthenaProfessionalDrawingPolicy): ViewDefinition = ViewDefinition(
    id = policy.viewId,
    displayName = policy.viewDisplayName,
    layoutIntent = LayoutIntent.CONNECTIVITY,
    viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
    familyContract = ElectricalProjectionDescriptor(ElectricalProjectionFamily.SCHEMATIC),
)

private fun controlDrawingView(policy: AthenaProfessionalDrawingPolicy): ViewDefinition = ViewDefinition(
    id = policy.viewId,
    displayName = policy.viewDisplayName,
    layoutIntent = LayoutIntent.CONNECTIVITY,
    viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
    familyContract = ElectricalProjectionDescriptor(ElectricalProjectionFamily.SCHEMATIC),
)

private fun diagnostic(code: String, subject: String, message: String) =
    AthenaProfessionalDrawingDiagnostic(code, subject.ifBlank { "unknown" }, message)

private fun MutableList<AthenaProfessionalDrawingDiagnostic>.sorted() =
    distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
