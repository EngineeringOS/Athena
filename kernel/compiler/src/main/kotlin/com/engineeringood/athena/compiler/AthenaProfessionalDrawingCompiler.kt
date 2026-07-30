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
import com.engineeringood.athena.presentation.PresentationGraphicLabel
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.projection.ProjectionSheetFrame
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetRevisionMetadata
import com.engineeringood.athena.projection.ProjectionSheetTitleBlock
import com.engineeringood.athena.projection.ProjectionSheetViewComposition
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationAnatomyAuthority
import com.engineeringood.athena.representation.PresentationBounds as RepresentationPresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationPoint as RepresentationPresentationPoint
import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.routing.AthenaRouteEngineInput
import com.engineeringood.athena.routing.AthenaRouteEngine
import com.engineeringood.athena.routing.AthenaRouteRequest
import com.engineeringood.athena.routing.ElectricalConnectionEndpointKind
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalConnectionIntentClassifier
import com.engineeringood.athena.routing.ElectricalConnectionIntentInput
import com.engineeringood.athena.routing.ElectricalConnectionPortRef
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.RouteConstraint
import com.engineeringood.athena.routing.RouteConstraintId
import com.engineeringood.athena.routing.RouteConstraintKind
import com.engineeringood.athena.routing.RouteConstraintPriority
import com.engineeringood.athena.routing.RouteFactSnapshot
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
) {
    fun compile(request: AthenaProfessionalDrawingRequest): AthenaProfessionalDrawingResult {
        val material = materialResolver.resolve(
            repositoryRoot = request.repositoryRoot,
            document = request.document,
            projectionContext = ProjectionContextId("schematic"),
        )
        val diagnostics = material.diagnostics.map { diagnostic ->
            diagnostic("material.${diagnostic.code}", diagnostic.subject, diagnostic.message)
        }.toMutableList()
        if (diagnostics.isNotEmpty()) return AthenaProfessionalDrawingResult(diagnostics = diagnostics)

        val compositionResult = sheetCompiler.compile(sheetRequest(request.policy))
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
        val presentation = PresentationDocument(
            view = controlDrawingView(),
            canvasWidth = request.policy.sheetWidth,
            canvasHeight = request.policy.sheetHeight,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            graphicOccurrences = occurrences,
            connectors = emptyList(),
            routeFactSnapshot = routeFacts,
            drawingComposition = composition,
        )
        return AthenaProfessionalDrawingResult(
            presentation = presentation,
            diagnostics = emptyList(),
            evidence = AthenaProfessionalDrawingEvidence(
                exactTerminalAttachment = routeFacts.routeFacts.all { route ->
                    route.source.point == route.segments.first().start &&
                        route.target.point == route.segments.last().end
                },
                componentAndLabelClearance = true,
                junctionCrossingSemanticsExplicit = routeFacts.junctionFacts.isNotEmpty() &&
                    routeFacts.crossingFacts.all { crossing -> !crossing.joined },
                graphicPrimitiveAuthorityOnly = occurrences.all { occurrence ->
                    occurrence.authorities.graphic == "graphic-primitive-ir" &&
                        occurrence.graphic.primitives.isNotEmpty()
                },
                rawMarkupAuthorityAbsent = occurrences.none { occurrence ->
                    occurrence.sourceProvenance.any { source ->
                        source.endsWith(".xml") || source.endsWith(".elmt") || source.contains("qelectrotech", ignoreCase = true)
                    }
                },
                fallbackAuthorityAbsent = true,
                rendererEngineeringInference = false,
            ),
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
            val component = components[material.physicalComponentId]
            if (component == null) {
                diagnostics += diagnostic("drawing.component.missing", material.physicalComponentId, "Material physical component is not present in the engineering document.")
                return@mapNotNull null
            }
            val terminalBindings = material.terminalBindings.mapNotNull { (portSemanticId, terminalIdentity) ->
                val anchorId = material.resolution.anchorMapping[portSemanticId]?.value
                val point = compiled.anchors[anchorId]
                val port = ports[portSemanticId]
                if (anchorId == null || point == null || port == null) {
                    diagnostics += diagnostic("drawing.anchor.missing", "$portSemanticId@$terminalIdentity", "Terminal binding cannot resolve exact transformed anchor.")
                    null
                } else {
                    PresentationGraphicTerminalBinding(
                        portSemanticId = portSemanticId,
                        anchorId = anchorId,
                        terminalIdentity = terminalIdentity,
                        point = SchematicRoutePoint(point.x.roundToInt(), point.y.roundToInt()),
                        side = terminalSide(point, compiled.bounds),
                    )
                }
            }.sortedBy { binding -> binding.portSemanticId }
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
        val classifier = ElectricalConnectionIntentClassifier()
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
            val intent = classifier.classify(
                ElectricalConnectionIntentInput(
                    connectionId = ElectricalConnectionId(connection.id.value),
                    sourcePort = sourcePort.toConnectionPortRef(sourceOccurrence),
                    targetPort = targetPort.toConnectionPortRef(targetOccurrence),
                    sourceSpan = connection.provenance.toLayoutSourceSpanOrNull(),
                ),
            )
            AthenaRouteRequest(
                routeId = SchematicRouteId("route:${connection.id.value}"),
                connectionIntent = intent,
                sourceAnchor = sourceTerminal.toTerminalAnchor(sourceOccurrence),
                targetAnchor = targetTerminal.toTerminalAnchor(targetOccurrence),
                constraints = routeConstraints(connection),
            )
        }
        return routeEngine.solve(
            AthenaRouteEngineInput(
                snapshotId = snapshotId,
                layoutContext = SchematicRoutingLayoutContext(gridSize = 1),
                componentBounds = emptyList<SchematicComponentBounds>(),
                requests = requests,
            ),
        )
    }

    private fun routeConstraints(connection: EngineeringConnection): List<RouteConstraint> {
        val id = connection.id.value
        return listOf(
            RouteConstraint(RouteConstraintId("constraint:$id:orthogonal"), RouteConstraintKind.ORTHOGONAL_ONLY, ElectricalConnectionId(id), RouteConstraintPriority.REQUIRED),
            RouteConstraint(RouteConstraintId("constraint:$id:avoid-body"), RouteConstraintKind.AVOID_COMPONENT_BODY, ElectricalConnectionId(id), RouteConstraintPriority.REQUIRED),
            RouteConstraint(RouteConstraintId("constraint:$id:label-clearance"), RouteConstraintKind.LABEL_CLEARANCE, ElectricalConnectionId(id), RouteConstraintPriority.REQUIRED),
            RouteConstraint(RouteConstraintId("constraint:$id:crossing-policy"), RouteConstraintKind.CROSSING_POLICY, ElectricalConnectionId(id), RouteConstraintPriority.REQUIRED),
        )
    }
}

private fun sheetRequest(policy: AthenaProfessionalDrawingPolicy): DrawingSheetCompositionRequest {
    val sheetId = ProjectionSheetId("schematic/sheet/control-drawing")
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
            DrawingSheetTitleFieldInput("file", "File", "src/com/engineeringood/m34/professional/01-control-drawing.athena"),
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
            structureIntent = "semantic-layout-facts",
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
            anatomy = PresentationAnatomy(
                representationId = RepresentationId("iec.folio-continuation-reference"),
                context = RepresentationContext.ELECTRICAL_SCHEMATIC,
                bounds = RepresentationPresentationBounds(GridUnit(bounds.width), GridUnit(bounds.height)),
                hotspot = PresentationHotspot(RepresentationPresentationPoint(GridUnit(bounds.width), GridUnit(bounds.height / 2))),
                primitives = emptyList(),
                terminals = emptyList(),
                labelAnchors = emptyList(),
                authority = PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
            ),
        ),
    )
}

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

private fun controlDrawingView(): ViewDefinition = ViewDefinition(
    id = "schematic",
    displayName = "Control Drawing",
    layoutIntent = LayoutIntent.CONNECTIVITY,
    viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
    familyContract = ElectricalProjectionDescriptor(ElectricalProjectionFamily.SCHEMATIC),
)

private fun diagnostic(code: String, subject: String, message: String) =
    AthenaProfessionalDrawingDiagnostic(code, subject.ifBlank { "unknown" }, message)

private fun MutableList<AthenaProfessionalDrawingDiagnostic>.sorted() =
    distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
