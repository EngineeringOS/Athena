package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetCompositionPolicy
import com.engineeringood.athena.drawing.composition.CabinetCompositionCompiler
import com.engineeringood.athena.drawing.composition.CabinetCompositionRequest
import com.engineeringood.athena.drawing.composition.CabinetCompositionResult
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
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.InstallationChannelDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.InstallationDuctDeclaration
import com.engineeringood.athena.language.InstallationEnclosureDeclaration
import com.engineeringood.athena.language.InstallationKind
import com.engineeringood.athena.language.InstallationLengthLiteral
import com.engineeringood.athena.language.InstallationMountDeclaration
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
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalConstraintEvaluatorV0
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalContractSource
import com.engineeringood.athena.physical.PhysicalContractSourceKind
import com.engineeringood.athena.physical.PhysicalDuctV0
import com.engineeringood.athena.physical.PhysicalEnclosureIntent
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationClearanceV0
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenanceV0
import com.engineeringood.athena.physical.PhysicalInstallationContractV0
import com.engineeringood.athena.physical.PhysicalInstallationIRV0
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationIntentV0
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSizeV0
import com.engineeringood.athena.physical.PhysicalInstallationTopologyCompilation
import com.engineeringood.athena.physical.PhysicalInstallationTopologyCompiler
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountedOccurrenceV0
import com.engineeringood.athena.physical.PhysicalMountedOccurrenceIntent
import com.engineeringood.athena.physical.PhysicalMountingSurfaceIntent
import com.engineeringood.athena.physical.PhysicalMountingSurfaceV0
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRailIntent
import com.engineeringood.athena.physical.PhysicalRailV0
import com.engineeringood.athena.physical.PhysicalRouteChannelIntent
import com.engineeringood.athena.physical.PhysicalRouteIntentSource
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalTerminalGroupIntent
import com.engineeringood.athena.physical.PhysicalTerminalGroupV0
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
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicTransform
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteQuality
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

data class AthenaM35CabinetProjectionDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

data class AthenaM35CabinetProjectionResult(
    val presentation: PresentationDocument? = null,
    val diagnostics: List<AthenaM35CabinetProjectionDiagnostic> = emptyList(),
)

class AthenaM35CabinetProjectionCompiler(
    private val materialResolver: AthenaRepresentationMaterialResolver = AthenaRepresentationMaterialResolver(),
) {
    fun compile(
        repositoryRoot: Path,
        sourcePath: Path,
        success: CompilerCompilationSuccess,
    ): AthenaM35CabinetProjectionResult {
        val diagnostics = mutableListOf<AthenaM35CabinetProjectionDiagnostic>()
        val installation = success.source.ast.declarations.filterIsInstance<InstallationDeclaration>().singleOrNull()
        if (installation == null || installation.kind != InstallationKind.Cabinet) {
            return failure("m35.installation.missing", sourcePath.toString(), "Expected exactly one `installation cabinet` declaration.")
        }

        val materialResolution = materialResolver.resolve(
            repositoryRoot = repositoryRoot,
            document = success.document,
            projectionContext = ProjectionContextId("cabinet"),
        )
        diagnostics += materialResolution.diagnostics.map { diagnostic ->
            diagnostic("m35.material.${diagnostic.code}", diagnostic.subject, diagnostic.message)
        }
        if (diagnostics.isNotEmpty()) return AthenaM35CabinetProjectionResult(diagnostics = diagnostics.sorted())

        val materialsBySubject = materialResolution.materials.associateBy { material -> material.semanticSubjectId }
        val sourceUnitId = PhysicalSourceUnitId(sourcePath.toAbsolutePath().normalize().toString().replace('\\', '/'))
        val terminalGroupIds = installation.terminalGroups.map { group -> group.id }.toSet()
        val contracts = installation.mounts.mapNotNull { mount ->
            val subject = StableSemanticIdentity("component:${mount.deviceId}")
            val material = materialsBySubject[subject.value]
            if (material == null) {
                diagnostics += diagnostic("m35.material.mount.unresolved", subject.value, "Mounted device has no cabinet representation material.")
                null
            } else {
                contractFromMaterial(subject, material, mount, terminalGroupIds)
            }
        }
        if (diagnostics.isNotEmpty()) return AthenaM35CabinetProjectionResult(diagnostics = diagnostics.sorted())

        val topology = PhysicalInstallationTopologyCompiler.compile(installation.toPhysicalIntent(sourceUnitId), contracts)
        val physicalIr = when (topology) {
            is PhysicalInstallationTopologyCompilation.Success -> topology.ir
            is PhysicalInstallationTopologyCompilation.Failure -> {
                return AthenaM35CabinetProjectionResult(
                    diagnostics = topology.diagnostics.map { issue ->
                        diagnostic("m35.${issue.code}", issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }
        when (val constraintEvaluation = PhysicalConstraintEvaluatorV0.evaluate(physicalIr)) {
            is com.engineeringood.athena.physical.PhysicalConstraintEvaluationV0.Success -> Unit
            is com.engineeringood.athena.physical.PhysicalConstraintEvaluationV0.Failure -> {
                return AthenaM35CabinetProjectionResult(
                    diagnostics = constraintEvaluation.diagnostics.map { issue ->
                        diagnostic("m35.${issue.code}", issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }

        val representationInputs = materialResolution.materials
            .filter { material -> physicalIr.space.mountedOccurrences.any { occurrence -> occurrence.semanticSubjectId.value == material.semanticSubjectId } }
            .map { material -> material.toCabinetRepresentationInput(sourceUnitId, physicalIr.installationId) }
        val transformCompilation = CabinetVisualTransformCompiler.compile(
            physicalOccurrences = physicalIr.toCabinetPhysicalInputs(),
            representationOccurrences = representationInputs,
            enclosureToDrawing = CabinetTargetFrame(
                origin = CabinetPointD(40.0, 40.0),
                alongAxis = CabinetVectorD(1.0, 0.0),
                normalAxis = CabinetVectorD(0.0, 1.0),
            ),
        )
        val joins = when (transformCompilation) {
            is CabinetVisualTransformCompilation.Success -> transformCompilation.joins
            is CabinetVisualTransformCompilation.Failure -> {
                return AthenaM35CabinetProjectionResult(
                    diagnostics = transformCompilation.diagnostics.map { issue ->
                        diagnostic("m35.${issue.code}", issue.subject, issue.message)
                    }.sorted(),
                )
            }
        }

        val endpointBindings = routeEndpointBindings(physicalIr, success.document, materialResolution.materials, diagnostics)
        if (diagnostics.isNotEmpty()) return AthenaM35CabinetProjectionResult(diagnostics = diagnostics.sorted())

        val routeTopology = when (val topologyCompilation = RouteChannelTopologyCompiler.compile(physicalIr.space.channels, physicalIr.routes)) {
            is RouteChannelTopologyCompilation.Success -> topologyCompilation.topology
            is RouteChannelTopologyCompilation.Failure -> {
                return AthenaM35CabinetProjectionResult(
                    diagnostics = topologyCompilation.diagnostics.map { issue ->
                        diagnostic("m35.${issue.code}", issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }
        val routing = CabinetRoutingCompiler.compile(
            CabinetRoutingRequest(
                ir = physicalIr,
                topology = routeTopology,
                joins = joins,
                endpoints = endpointBindings,
            ),
        )
        val routes = when (routing) {
            is CabinetRoutingCompilation.Success -> routing.routes
            is CabinetRoutingCompilation.Failure -> {
                return AthenaM35CabinetProjectionResult(
                    diagnostics = routing.diagnostics.map { issue ->
                        diagnostic("m35.${issue.code}", issue.subject, issue.expected)
                    }.sorted(),
                )
            }
        }

        val composition = CabinetCompositionCompiler.compile(
            CabinetCompositionRequest(
                ir = physicalIr,
                joins = joins,
                routes = routes,
                policy = CabinetCompositionPolicy(documentId = "m35-cabinet", padding = 20.0),
            ),
        )
        if (composition is CabinetCompositionResult.Failure) {
            return AthenaM35CabinetProjectionResult(
                diagnostics = composition.diagnostics.map { issue ->
                    diagnostic("m35.${issue.code}", issue.subject, issue.message)
                }.sorted(),
            )
        }

        val mountedByKey = physicalIr.space.mountedOccurrences.associateBy { occurrence -> occurrence.key }
        val materialBySubject = materialResolution.materials.associateBy { material -> material.semanticSubjectId }
        val componentsById = success.document.components.associateBy { component -> component.id.value }
        val graphicOccurrences = joins.map { join ->
            val mounted = mountedByKey.getValue(join.key)
            val material = materialBySubject.getValue(join.key.canonicalSemanticSubjectId.value)
            val component = componentsById.getValue(material.physicalComponentId)
            val graphic = material.definition.graphicBody.transformedTo(join.body.bounds, "${join.physicalOccurrenceId.value}:")
            PresentationGraphicOccurrence(
                occurrenceId = RepresentationOccurrenceId("cabinet:${join.physicalOccurrenceId.value}"),
                semanticSubjectId = material.semanticSubjectId,
                physicalComponentId = mounted.occurrenceId.value,
                functionId = material.functionId,
                bounds = join.body.bounds.toPresentationBounds(),
                orientation = mounted.selectedOrientation.toLayoutOrientation(),
                deviceLabel = component.propertyText("tag") ?: component.name,
                modelLabel = component.propertyText("model"),
                packageId = material.definition.libraryId.value,
                definitionId = material.definition.symbolId.value,
                bindingRuleId = material.resolution.bindingRuleId?.value ?: "unbound",
                graphic = graphic,
                terminalBindings = material.terminalBindings.mapNotNull { (portSemanticId, terminalIdentity) ->
                    val anchorId = material.resolution.anchorMapping[portSemanticId]?.value
                    val anchor = join.anchors.firstOrNull { candidate -> candidate.id == anchorId }
                    if (anchorId == null || anchor == null) {
                        diagnostics += diagnostic("m35.graphic.anchor.missing", "$portSemanticId@$terminalIdentity", "Missing transformed cabinet anchor.")
                        null
                    } else {
                        PresentationGraphicTerminalBinding(
                            portSemanticId = portSemanticId,
                            anchorId = anchorId,
                            terminalIdentity = terminalIdentity,
                            point = anchor.point.toSchematicPoint(),
                            side = terminalSide(anchor.point, join.body.bounds),
                        )
                    }
                }.sortedBy { binding -> binding.portSemanticId },
                labels = labelsFor(material.semanticSubjectId, component.propertyText("tag") ?: component.name, component.propertyText("model"), join.body.bounds),
                sourceProvenance = material.definition.graphicBody.provenanceSources +
                    material.definition.lifecycle.provenance.source +
                    mounted.provenance.declarationId,
            )
        }.sortedBy { occurrence -> occurrence.occurrenceId.value }
        if (diagnostics.isNotEmpty()) return AthenaM35CabinetProjectionResult(diagnostics = diagnostics.sorted())

        val routeSnapshot = routes.toRouteFactSnapshot(physicalIr, success.document, graphicOccurrences)
        val drawingComposition = physicalIr.toDrawingComposition(routes)
        val presentation = PresentationDocument(
            view = m35CabinetViewDefinition(),
            canvasWidth = drawingComposition.sheetBounds.width,
            canvasHeight = drawingComposition.sheetBounds.height,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            graphicOccurrences = graphicOccurrences,
            connectors = emptyList(),
            routeFactSnapshot = routeSnapshot,
            drawingComposition = drawingComposition,
        )
        return AthenaM35CabinetProjectionResult(presentation = presentation)
    }
}

private fun m35CabinetViewDefinition(): ViewDefinition = ViewDefinition(
    id = "cabinet",
    displayName = "Cabinet",
    description = "M35 physical installation Cabinet.",
)

private fun failure(code: String, subject: String, message: String): AthenaM35CabinetProjectionResult =
    AthenaM35CabinetProjectionResult(diagnostics = listOf(diagnostic(code, subject, message)))

private fun diagnostic(code: String, subject: String, message: String): AthenaM35CabinetProjectionDiagnostic =
    AthenaM35CabinetProjectionDiagnostic(code, subject, message)

private fun List<AthenaM35CabinetProjectionDiagnostic>.sorted(): List<AthenaM35CabinetProjectionDiagnostic> =
    sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))

private fun InstallationDeclaration.toPhysicalIntent(sourceUnitId: PhysicalSourceUnitId): PhysicalInstallationIntentV0 {
    val ductOrientationById = ducts.associate { duct -> duct.id to duct.orientation.toPhysicalInfrastructureOrientation() }
    return PhysicalInstallationIntentV0(
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

private fun InstallationOrientation.toPhysicalInstallationOrientation(): PhysicalInstallationOrientation = when (this) {
    InstallationOrientation.Horizontal -> PhysicalInstallationOrientation.Deg0
    InstallationOrientation.Vertical -> PhysicalInstallationOrientation.Deg90
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

private fun contractFromMaterial(
    subject: StableSemanticIdentity,
    material: AthenaResolvedRepresentationMaterial,
    mount: InstallationMountDeclaration,
    terminalGroupIds: Set<String>,
): PhysicalInstallationContractV0 {
    val bounds = requireNotNull(material.definition.graphicBody.bounds) { "Representation material must have bounds." }
    val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "m35:representation-material:${material.definition.symbolId.value}")
    val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
        PhysicalInstallationContractFieldProvenance(
            field = field,
            source = source,
            span = PhysicalSourceSpan(file = source.id, line = mount.span.start.line, column = mount.span.start.column),
        )
    }
    val orientation = mount.orientation.toPhysicalInstallationOrientation()
    val mountingType = if (mount.targetId in terminalGroupIds) "terminal" else "din35"
    return PhysicalInstallationContractV0(
        subjectIdentity = subject,
        size = PhysicalInstallationSizeV0(
            width = requireNotNull(PhysicalPositiveMillimeters.from(max(1, bounds.width.roundToInt()))),
            height = requireNotNull(PhysicalPositiveMillimeters.from(max(1, bounds.height.roundToInt()))),
            depth = requireNotNull(PhysicalPositiveMillimeters.from(70)),
        ),
        mountingTypeId = PhysicalMountingTypeId(mountingType),
        allowedOrientations = setOf(orientation),
        clearance = PhysicalInstallationClearanceV0(
            top = requireNotNull(PhysicalNonNegativeMillimeters.from(0)),
            right = requireNotNull(PhysicalNonNegativeMillimeters.from(0)),
            bottom = requireNotNull(PhysicalNonNegativeMillimeters.from(0)),
            left = requireNotNull(PhysicalNonNegativeMillimeters.from(0)),
        ),
        compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
        provenance = PhysicalInstallationContractProvenanceV0(
            width = provenance.getValue(PhysicalInstallationContractField.Width),
            height = provenance.getValue(PhysicalInstallationContractField.Height),
            depth = provenance.getValue(PhysicalInstallationContractField.Depth),
            mountingType = provenance.getValue(PhysicalInstallationContractField.MountingType),
            allowedOrientations = provenance.getValue(PhysicalInstallationContractField.AllowedOrientations),
            clearanceTop = provenance.getValue(PhysicalInstallationContractField.ClearanceTop),
            clearanceRight = provenance.getValue(PhysicalInstallationContractField.ClearanceRight),
            clearanceBottom = provenance.getValue(PhysicalInstallationContractField.ClearanceBottom),
            clearanceLeft = provenance.getValue(PhysicalInstallationContractField.ClearanceLeft),
            compatibleContainerKinds = provenance.getValue(PhysicalInstallationContractField.CompatibleContainerKinds),
        ),
    )
}

private fun PhysicalInstallationIRV0.toCabinetPhysicalInputs(): List<CabinetPhysicalOccurrenceInput> {
    val surfaces = space.surfaces.associateBy { surface -> surface.id }
    val rails = space.rails.associateBy { rail -> rail.id }
    val terminalGroups = space.terminalGroups.associateBy { group -> group.id }
    return space.mountedOccurrences.map { occurrence ->
        CabinetPhysicalOccurrenceInput(
            key = occurrence.key,
            occurrenceId = occurrence.occurrenceId,
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

private fun PhysicalMountTargetRef.targetFrame(
    surfaces: Map<PhysicalObjectId, PhysicalMountingSurfaceV0>,
    rails: Map<PhysicalObjectId, PhysicalRailV0>,
    terminalGroups: Map<PhysicalObjectId, PhysicalTerminalGroupV0>,
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

private fun routeEndpointBindings(
    ir: PhysicalInstallationIRV0,
    document: EngineeringDocument,
    materials: List<AthenaResolvedRepresentationMaterial>,
    diagnostics: MutableList<AthenaM35CabinetProjectionDiagnostic>,
): List<CabinetConnectionEndpointBinding> {
    val connectionsByAlias = document.connections.associateBy { connection -> connection.alias() }
    val portToMaterial = materials.flatMap { material ->
        material.terminalBindings.keys.map { portId -> portId to material }
    }.toMap()
    return ir.routes.mapNotNull { route ->
        val connection = connectionsByAlias[route.connectionAlias]
        if (connection == null) {
            diagnostics += diagnostic("m35.route.connection.missing", route.connectionAlias, "Physical route alias has no governed connection.")
            return@mapNotNull null
        }
        val fromPort = connection.from.resolvedIdentity?.value
        val toPort = connection.to.resolvedIdentity?.value
        val fromMaterial = fromPort?.let(portToMaterial::get)
        val toMaterial = toPort?.let(portToMaterial::get)
        val fromAnchor = fromPort?.let { fromMaterial?.resolution?.anchorMapping?.get(it)?.value }
        val toAnchor = toPort?.let { toMaterial?.resolution?.anchorMapping?.get(it)?.value }
        if (fromPort == null || toPort == null || fromMaterial == null || toMaterial == null || fromAnchor == null || toAnchor == null) {
            diagnostics += diagnostic("m35.route.anchor.missing", route.connectionAlias, "Route endpoint ports must resolve to material anchors.")
            return@mapNotNull null
        }
        CabinetConnectionEndpointBinding(
            connectionAlias = route.connectionAlias,
            from = CabinetRouteEndpointRef(ir.keyFor(fromMaterial.semanticSubjectId), fromAnchor),
            to = CabinetRouteEndpointRef(ir.keyFor(toMaterial.semanticSubjectId), toAnchor),
        )
    }
}

private fun EngineeringConnection.alias(): String = id.value.substringAfterLast(':')

private fun PhysicalInstallationIRV0.keyFor(subjectId: String): InstallationOccurrenceKey =
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
    val transform = M35AffineTransform.translation(target.x, target.y)
        .compose(M35AffineTransform.scale(scale, scale))
        .compose(M35AffineTransform.translation(-sourceBounds.x, -sourceBounds.y))
    return copy(
        documentId = GraphicPrimitiveDocumentId(idPrefix.removeSuffix(":")),
        bounds = target,
        primitives = primitives.flatMap { primitive -> primitive.transform(transform, idPrefix) },
    )
}

private data class M35AffineTransform(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    fun compose(inner: M35AffineTransform): M35AffineTransform = M35AffineTransform(
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
        fun translation(dx: Double, dy: Double) = M35AffineTransform(1.0, 0.0, 0.0, 1.0, dx, dy)
        fun scale(x: Double, y: Double) = M35AffineTransform(x, 0.0, 0.0, y, 0.0, 0.0)
    }
}

private fun GraphicPrimitive.transform(transform: M35AffineTransform, idPrefix: String): List<GraphicPrimitive> = when (this) {
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

private fun CabinetRectD.toPresentationBounds(): com.engineeringood.athena.presentation.PresentationDrawingBounds =
    com.engineeringood.athena.presentation.PresentationDrawingBounds(
        x = max(0, x.roundToInt()),
        y = max(0, y.roundToInt()),
        width = max(1, width.roundToInt()),
        height = max(1, height.roundToInt()),
    )

private fun List<CabinetRouteFact>.toRouteFactSnapshot(
    ir: PhysicalInstallationIRV0,
    document: EngineeringDocument,
    occurrences: List<PresentationGraphicOccurrence>,
): RouteFactSnapshot {
    val snapshotId = LayoutSnapshotId("m35:cabinet:${ir.installationId.value}")
    val connectionsByAlias = document.connections.associateBy { connection -> connection.alias() }
    val occurrenceBySubject = occurrences.associateBy { occurrence -> occurrence.semanticSubjectId }
    val routeFacts = mapIndexed { index, route ->
        val connection = connectionsByAlias.getValue(route.connectionAlias)
        RouteFact(
            routeId = SchematicRouteId("cabinet-route:${route.connectionAlias}"),
            snapshotId = snapshotId,
            connectionId = ElectricalConnectionId(connection.id.value),
            source = route.from.toTerminalAnchor(occurrenceBySubject.getValue(route.from.key.canonicalSemanticSubjectId.value), connection.from.resolvedIdentity),
            target = route.to.toTerminalAnchor(occurrenceBySubject.getValue(route.to.key.canonicalSemanticSubjectId.value), connection.to.resolvedIdentity),
            segments = route.segments.mapNotNull { segment -> segment.toSchematicSegment() },
            lane = SchematicRouteLane(index),
            quality = RouteQuality.satisfied(),
        )
    }
    return RouteFactSnapshot.canonical(snapshotId, "cabinet", routeFacts)
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
    policySource = "m35:physical-cabinet-route",
)

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

private fun PhysicalInstallationIRV0.toDrawingComposition(routes: List<CabinetRouteFact>): PresentationDrawingComposition {
    val sheetWidth = space.enclosure.size.width + 100
    val sheetHeight = space.enclosure.size.height + 120
    val titleBounds = PresentationDrawingBounds(40, sheetHeight - 60, sheetWidth - 80, 40)
    return PresentationDrawingComposition(
        sheetId = "m35-cabinet",
        policyId = "m35-physical-cabinet-composition-v0",
        contentBounds = PresentationDrawingBounds(40, 40, space.enclosure.size.width, space.enclosure.size.height),
        frameBounds = PresentationDrawingBounds(20, 20, sheetWidth - 40, sheetHeight - 40),
        drawingAreaBounds = PresentationDrawingBounds(40, 40, space.enclosure.size.width, space.enclosure.size.height),
        titleBlockBounds = titleBounds,
        sheetBounds = PresentationDrawingBounds(0, 0, sheetWidth, sheetHeight),
        frameId = "m35-cabinet-frame",
        frameStyle = "m35-industrial-frame",
        title = PresentationDrawingTitle(
            sheetTitle = "M35 Physical Installation Cabinet",
            sheetFamily = "Cabinet",
            sheetNumber = "M35-001",
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
            contentBounds = "physical-installation-ir-v0",
            bounds = "cabinet-composition-compiler",
            projection = "m35-cabinet-projection",
            representation = "athena-representation-material",
            structureIntent = "physical-installation-ir-v0",
            policy = "presentation-profile-policy",
        ),
    )
}

private fun PhysicalInstallationIRV0.structureFacts(routes: List<CabinetRouteFact>): List<PresentationDrawingStructureFact> = buildList {
    space.rails.forEach { rail ->
        val surface = space.surfaces.first { surface -> surface.id == rail.surfaceId }
        val start = PresentationDrawingPoint(surface.at.x + rail.at.x + 40, surface.at.y + rail.at.y + 40)
        val end = when (rail.orientation) {
            PhysicalInfrastructureOrientation.Horizontal -> PresentationDrawingPoint(start.x + rail.length.value, start.y)
            PhysicalInfrastructureOrientation.Vertical -> PresentationDrawingPoint(start.x, start.y + rail.length.value)
        }
        add(PresentationDrawingStructureFact("rail:${rail.id.value}", "rail", null, null, start, end, emptyList(), "physical-installation-ir-v0", "cabinet-composition-compiler"))
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
                boundsAuthority = "physical-installation-ir-v0",
            ),
        )
    }
    space.terminalGroups.forEach { group ->
        add(PresentationDrawingStructureFact("terminal-group:${group.id.value}", "terminal-strip", group.orientation.name, PresentationDrawingBounds(group.at.x + 40, group.at.y + 40, group.size.width, group.size.height), null, null, group.orderedOccurrenceKeys.map { it.canonicalSemanticSubjectId.value }, "physical-installation-ir-v0", "physical-installation-ir-v0"))
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

private fun EngineeringPropertyValue.text(): String = when (this) {
    is EngineeringPropertyValue.Symbol -> text
    is EngineeringPropertyValue.Text -> text
}
