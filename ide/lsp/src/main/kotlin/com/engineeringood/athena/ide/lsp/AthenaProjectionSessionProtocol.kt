package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.compiler.AthenaProfessionalDrawingCompiler
import com.engineeringood.athena.compiler.AthenaProfessionalDrawingPolicy
import com.engineeringood.athena.compiler.AthenaProfessionalDrawingRequest
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.semantic.CanonicalSemanticIdentityBuilder
import com.engineeringood.athena.compiler.semantic.GraphPackageIdentity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLayoutHintBinder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticNamespace
import com.engineeringood.athena.compiler.semantic.ProjectSemanticPackage
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceUnit
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionCrossReference
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionCrossReferenceLink
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionDiagnostic
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionElectricalAnchor
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionElectricalConnectionEndpoint
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionElectricalRoutingCorridor
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionNotationPack
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionNotationSubject
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionPoint
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionRenderContribution
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSession
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionView
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheet
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetPolicyEvidence
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetLayout
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetLayoutFrame
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetLayoutLabelLayout
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetLayoutPlacement
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetLayoutRoutingGuidance
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSurfaceMapping
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeViewerComponentBox
import com.engineeringood.athena.runtime.AthenaRuntimeViewerConnectionLine
import com.engineeringood.athena.runtime.AthenaRuntimeViewerLabel
import com.engineeringood.athena.runtime.AthenaRuntimeViewerScene
import java.nio.file.Files
import com.engineeringood.athena.presentation.PresentationDocument
import java.nio.file.Path

internal fun AthenaLspSessionHostReady.toProjectionSessionPayload(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures? = null,
): AthenaProjectionSessionPayload {
    return currentProjectionSession(
        snapshot = snapshot,
        languageFeatures = languageFeatures,
    ).toPayload(
        semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
    )
}

internal fun AthenaLspSessionHostReady.currentProjectionSession(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures?,
): AthenaRuntimeProjectionSession {
    val sourcePath = snapshot?.sourcePath
    val trackedDocument = when {
        languageFeatures == null || sourcePath == null -> null
        snapshot.lastOpenedDocumentUri != null -> {
            languageFeatures.trackedDocument(snapshot.lastOpenedDocumentUri)
                ?.takeIf { tracked -> tracked.path.isWithinSourceRoot(snapshot.sourceRootPath) }
        }

        else -> null
    } ?: sourcePath?.let { path -> languageFeatures?.trackedDocumentByPath(path) }
    val professionalTrackedDocument = trackedDocument ?: professionalM34TrackedDocument(snapshot, languageFeatures)
    professionalTrackedDocument?.professionalM34ProjectionSession(snapshot)?.let { professional ->
        return professional
    }
    return trackedDocument?.let { tracked ->
        context.previewProjectionSession(tracked.compilation)
    } ?: context.projectProjectionSession()
}

private fun professionalM34TrackedDocument(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures?,
): AthenaTrackedDocument? {
    val repositoryRoot = snapshot?.repositoryRoot ?: return null
    if (!repositoryRoot.isProfessionalM34RepositoryRoot()) {
        return null
    }
    val sourcePath = repositoryRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena").toAbsolutePath().normalize()
    languageFeatures ?: return null
    return languageFeatures.trackedDocumentByPath(sourcePath) ?: runCatching {
        languageFeatures.trackDocument(
            uri = sourcePath.toUri().toString(),
            path = sourcePath,
            version = 0,
            text = Files.readString(sourcePath),
        )
    }.getOrNull()
}

private fun AthenaTrackedDocument.professionalM34ProjectionSession(
    snapshot: AthenaLspSessionSnapshot?,
): AthenaRuntimeProjectionSession? {
    val repositoryRoot = snapshot?.repositoryRoot ?: return null
    if (!repositoryRoot.isProfessionalM34RepositoryRoot()) {
        return null
    }
    val success = compilation as? CompilerCompilationSuccess ?: return null
    val semanticSnapshot = professionalM34SemanticSnapshot(success, text)
    val result = AthenaProfessionalDrawingCompiler().compile(
        AthenaProfessionalDrawingRequest(
            repositoryRoot = repositoryRoot,
            document = success.document,
            semanticSnapshot = semanticSnapshot,
            policy = AthenaProfessionalDrawingPolicy.m34RollingShutter(),
        ),
    )
    val presentation = result.presentation ?: return AthenaRuntimeProjectionSession(
        projectName = snapshot.projectName,
        supportedViews = listOf(controlDrawingView()),
        activeViewId = "schematic",
        activeProjection = AthenaRuntimeProjectionUnavailableSnapshot(
            viewId = "schematic",
            reason = result.diagnostics.joinToString("\n").ifBlank { "Professional Control Drawing compilation failed." },
            diagnostics = result.diagnostics.map { diagnostic ->
                AthenaRuntimeProjectionDiagnostic(
                    severity = "error",
                    code = diagnostic.code,
                    message = diagnostic.message,
                    provenance = diagnostic.subject,
                )
            },
        ),
    )
    return AthenaRuntimeProjectionSession(
        projectName = snapshot.projectName,
        supportedViews = listOf(controlDrawingView()),
        activeViewId = "schematic",
        activeProjection = AthenaRuntimeProjectionReadySnapshot(
            viewId = "schematic",
            familyId = "schematic",
            scene = AthenaRuntimeViewerScene(
                systemName = success.document.system.name,
                canvasWidth = presentation.canvasWidth,
                canvasHeight = presentation.canvasHeight,
                components = emptyList(),
                connections = emptyList(),
                labels = emptyList(),
            ),
            presentation = presentation,
            activeSheetId = "schematic/sheet/control-drawing",
            sheets = emptyList(),
        ),
    )
}

private fun Path.isProfessionalM34RepositoryRoot(): Boolean {
    return toAbsolutePath().normalize().toString().replace('\\', '/')
        .endsWith("examples/m34/professional-control-drawing")
}

private fun professionalM34SemanticSnapshot(
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

private fun controlDrawingView(): AthenaRuntimeProjectionView =
    AthenaRuntimeProjectionView(
        viewId = "schematic",
        displayName = "Control Drawing",
        description = "Focused professional rolling-shutter control drawing.",
        familyId = "schematic",
    )

private fun java.nio.file.Path.isWithinSourceRoot(sourceRootPath: java.nio.file.Path): Boolean {
    return toAbsolutePath().normalize().startsWith(sourceRootPath.toAbsolutePath().normalize())
}

internal fun AthenaRuntimeProjectionSession.toPayload(
    semanticPath: String,
): AthenaProjectionSessionPayload {
    val projection = activeProjection
    return AthenaProjectionSessionPayload(
        projectName = projectName,
        semanticPath = semanticPath,
        activeViewId = activeViewId,
        supportedViews = supportedViews.map { view ->
            AthenaProjectionViewPayload(
                viewId = view.viewId,
                displayName = view.displayName,
                description = view.description,
                familyId = view.familyId,
                ownershipContract = view.ownershipContract.toPayload(),
            )
        },
        governedCommands = projectionGovernedCommands(),
        status = projection.statusValue(),
        readyProjection = projection.toReadyPayload(),
        unavailableReason = projection.toUnavailableReason(),
        diagnostics = projection.toDiagnostics(),
    )
}

private fun ProjectionOwnershipContract.toPayload(): AthenaProjectionOwnershipContractPayload {
    return AthenaProjectionOwnershipContractPayload(
        interactivity = interactivity.name.lowercase(),
        displayScopes = displayScopes,
        semanticCommandIds = semanticCommandIds,
        projectionCommandIds = projectionCommandIds,
        transientInteractionKinds = transientInteractionKinds,
        persistedProjectionMetadataKeys = persistedProjectionMetadataKeys,
    )
}

private fun AthenaRuntimeProjectionSnapshot.statusValue(): String {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> "ready"
        is AthenaRuntimeProjectionUnavailableSnapshot -> "unavailable"
    }
}

private fun AthenaRuntimeProjectionSnapshot.toReadyPayload(): AthenaProjectionReadyPayload? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> scene.toPayload(
            viewId = viewId,
            familyId = familyId,
            presentation = presentation,
            activeSheetId = activeSheetId,
            sheets = sheets,
            sheetLayout = sheetLayout,
            notationPack = notationPack,
            crossReferences = crossReferences,
            electricalAnchors = electricalAnchors,
            electricalConnectionEndpoints = electricalConnectionEndpoints,
            electricalRoutingCorridors = electricalRoutingCorridors,
            activeRenderContributions = activeRenderContributions,
        )

        is AthenaRuntimeProjectionUnavailableSnapshot -> null
    }
}

private fun AthenaRuntimeProjectionSnapshot.toUnavailableReason(): String? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> null
        is AthenaRuntimeProjectionUnavailableSnapshot -> reason
    }
}

private fun AthenaRuntimeProjectionSnapshot.toDiagnostics(): List<AthenaProjectionDiagnosticPayload> {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> emptyList()
        is AthenaRuntimeProjectionUnavailableSnapshot -> diagnostics.map(AthenaRuntimeProjectionDiagnostic::toPayload)
    }
}

private fun AthenaRuntimeProjectionDiagnostic.toPayload(): AthenaProjectionDiagnosticPayload {
    return AthenaProjectionDiagnosticPayload(
        severity = severity,
        code = code,
        message = message,
        provenance = provenance,
    )
}

private fun AthenaRuntimeViewerScene.toPayload(
    viewId: String,
    familyId: String?,
    presentation: PresentationDocument?,
    activeSheetId: String?,
    sheets: List<AthenaRuntimeProjectionSheet>,
    sheetLayout: AthenaRuntimeProjectionSheetLayout?,
    notationPack: AthenaRuntimeProjectionNotationPack?,
    crossReferences: List<AthenaRuntimeProjectionCrossReference>,
    electricalAnchors: List<AthenaRuntimeProjectionElectricalAnchor>,
    electricalConnectionEndpoints: List<AthenaRuntimeProjectionElectricalConnectionEndpoint>,
    electricalRoutingCorridors: List<AthenaRuntimeProjectionElectricalRoutingCorridor>,
    activeRenderContributions: List<AthenaRuntimeProjectionRenderContribution>,
): AthenaProjectionReadyPayload {
    return AthenaProjectionReadyPayload(
        viewId = viewId,
        familyId = familyId,
        systemName = systemName,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        presentation = presentation?.toPayload(),
        activeSheetId = activeSheetId,
        sheets = sheets.map(AthenaRuntimeProjectionSheet::toPayload),
        sheetLayout = sheetLayout?.toPayload(),
        notationPack = notationPack?.toPayload(),
        crossReferences = crossReferences.map(AthenaRuntimeProjectionCrossReference::toPayload),
        electricalAnchors = electricalAnchors.map(AthenaRuntimeProjectionElectricalAnchor::toPayload),
        electricalConnectionEndpoints = electricalConnectionEndpoints.map(AthenaRuntimeProjectionElectricalConnectionEndpoint::toPayload),
        electricalRoutingCorridors = electricalRoutingCorridors.map(AthenaRuntimeProjectionElectricalRoutingCorridor::toPayload),
        activeRenderContributions = activeRenderContributions.map(AthenaRuntimeProjectionRenderContribution::toPayload),
        components = components.map(AthenaRuntimeViewerComponentBox::toPayload),
        connections = connections.map(AthenaRuntimeViewerConnectionLine::toPayload),
        labels = labels.map(AthenaRuntimeViewerLabel::toPayload),
    )
}

private fun AthenaRuntimeProjectionSheet.toPayload(): AthenaProjectionSheetPayload {
    return AthenaProjectionSheetPayload(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        previousSheetId = previousSheetId,
        nextSheetId = nextSheetId,
        subjectSemanticIds = subjectSemanticIds,
        policyEvidence = policyEvidence?.toPayload(),
        publication = publication.toPayload(),
        composition = composition.toPayload(),
    )
}

private fun AthenaRuntimeProjectionSheetPolicyEvidence.toPayload(): AthenaProjectionSheetPolicyEvidencePayload {
    return AthenaProjectionSheetPolicyEvidencePayload(
        policyId = policyId,
        policyVersion = policyVersion,
        policyDeterministicIdentity = policyDeterministicIdentity,
        sheetViewRole = sheetViewRole,
        sheetViewRoleOrder = sheetViewRoleOrder,
    )
}

private fun com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetPublication.toPayload(): AthenaProjectionSheetPublicationPayload {
    return AthenaProjectionSheetPublicationPayload(
        pageSize = AthenaProjectionSheetPageSizePayload(
            format = pageSize.format,
            orientation = pageSize.orientation,
        ),
        frame = AthenaProjectionSheetFramePayload(
            frameId = frame.frameId,
            style = frame.style,
        ),
        coordinateZones = coordinateZones.map { zone ->
            AthenaProjectionSheetCoordinateZonePayload(
                zoneId = zone.zoneId,
                label = zone.label,
                order = zone.order,
            )
        },
        titleBlock = AthenaProjectionSheetTitleBlockPayload(
            sheetTitle = titleBlock.sheetTitle,
            sheetFamily = titleBlock.sheetFamily,
            sheetNumber = titleBlock.sheetNumber,
        ),
        revisionMetadata = AthenaProjectionSheetRevisionMetadataPayload(
            revisionCode = revisionMetadata.revisionCode,
            revisionNote = revisionMetadata.revisionNote,
        ),
        viewComposition = AthenaProjectionSheetViewCompositionPayload(
            primaryViewId = viewComposition.primaryViewId,
            primarySheetOrder = viewComposition.primarySheetOrder,
            subjectSemanticIds = viewComposition.subjectSemanticIds,
        ),
    )
}

private fun com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetComposition.toPayload(): AthenaProjectionSheetCompositionPayload {
    return AthenaProjectionSheetCompositionPayload(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
        publication = publication.toPayload(),
    )
}

private fun AthenaRuntimeProjectionSheetLayout.toPayload(): AthenaProjectionSheetLayoutPayload {
    return AthenaProjectionSheetLayoutPayload(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
        representationFamilyId = representationFamilyId,
        frame = frame.toPayload(),
        placements = placements.map(AthenaRuntimeProjectionSheetLayoutPlacement::toPayload),
        routingGuidance = routingGuidance.map(AthenaRuntimeProjectionSheetLayoutRoutingGuidance::toPayload),
        labelLayouts = labelLayouts.map(AthenaRuntimeProjectionSheetLayoutLabelLayout::toPayload),
    )
}

private fun AthenaRuntimeProjectionSheetLayoutFrame.toPayload(): AthenaProjectionSheetLayoutFramePayload {
    return AthenaProjectionSheetLayoutFramePayload(
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        gridMajorStep = gridMajorStep,
        gridMinorStep = gridMinorStep,
    )
}

private fun AthenaRuntimeProjectionSheetLayoutPlacement.toPayload(): AthenaProjectionSheetLayoutPlacementPayload {
    return AthenaProjectionSheetLayoutPlacementPayload(
        projectionId = projectionId,
        semanticId = semanticId,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun AthenaRuntimeProjectionSheetLayoutRoutingGuidance.toPayload(): AthenaProjectionSheetLayoutRoutingGuidancePayload {
    return AthenaProjectionSheetLayoutRoutingGuidancePayload(
        projectionConnectionId = projectionConnectionId,
        connectionSemanticId = connectionSemanticId,
        sourcePoint = sourcePoint.toPayload(),
        targetPoint = targetPoint.toPayload(),
        routingStyle = routingStyle,
        bendPoints = bendPoints.map(AthenaRuntimeProjectionPoint::toPayload),
    )
}

private fun AthenaRuntimeProjectionSheetLayoutLabelLayout.toPayload(): AthenaProjectionSheetLayoutLabelLayoutPayload {
    return AthenaProjectionSheetLayoutLabelLayoutPayload(
        projectionId = projectionId,
        semanticId = semanticId,
        label = label,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun AthenaRuntimeProjectionNotationPack.toPayload(): AthenaProjectionNotationPackPayload {
    return AthenaProjectionNotationPackPayload(
        packId = packId,
        displayName = displayName,
        subjects = subjects.map(AthenaRuntimeProjectionNotationSubject::toPayload),
    )
}

private fun AthenaRuntimeProjectionNotationSubject.toPayload(): AthenaProjectionNotationSubjectPayload {
    return AthenaProjectionNotationSubjectPayload(
        semanticId = semanticId,
        symbolKey = symbolKey,
        labelPolicy = labelPolicy,
        markerKeys = markerKeys,
    )
}

private fun AthenaRuntimeProjectionCrossReference.toPayload(): AthenaProjectionCrossReferencePayload {
    return AthenaProjectionCrossReferencePayload(
        semanticId = semanticId,
        kind = kind,
        crossReferenceId = crossReferenceId,
        sheetIds = sheetIds,
        occurrenceIds = occurrenceIds,
        links = links.map(AthenaRuntimeProjectionCrossReferenceLink::toPayload),
    )
}

private fun AthenaRuntimeProjectionCrossReferenceLink.toPayload(): AthenaProjectionCrossReferenceLinkPayload {
    return AthenaProjectionCrossReferenceLinkPayload(
        semanticId = semanticId,
        sourceSheetId = sourceSheetId,
        targetSheetId = targetSheetId,
        sourceOccurrenceId = sourceOccurrenceId,
        targetOccurrenceId = targetOccurrenceId,
        compactNotation = compactNotation,
    )
}

private fun AthenaRuntimeProjectionElectricalAnchor.toPayload(): AthenaProjectionElectricalAnchorPayload {
    return AthenaProjectionElectricalAnchorPayload(
        anchorId = anchorId,
        portSemanticId = portSemanticId,
        ownerSemanticId = ownerSemanticId,
        nodeId = nodeId,
        labelId = labelId,
        x = x,
        y = y,
        side = side,
    )
}

private fun AthenaRuntimeProjectionElectricalConnectionEndpoint.toPayload(): AthenaProjectionElectricalConnectionEndpointPayload {
    return AthenaProjectionElectricalConnectionEndpointPayload(
        endpointId = endpointId,
        projectionConnectionId = projectionConnectionId,
        connectionSemanticId = connectionSemanticId,
        endpointRole = endpointRole,
        portSemanticId = portSemanticId,
        anchorId = anchorId,
    )
}

private fun AthenaRuntimeProjectionElectricalRoutingCorridor.toPayload(): AthenaProjectionElectricalRoutingCorridorPayload {
    return AthenaProjectionElectricalRoutingCorridorPayload(
        corridorId = corridorId,
        projectionConnectionId = projectionConnectionId,
        connectionSemanticId = connectionSemanticId,
        sourceAnchorId = sourceAnchorId,
        targetAnchorId = targetAnchorId,
        routingStyle = routingStyle,
        preferredBendPoints = preferredBendPoints.map(AthenaRuntimeProjectionPoint::toPayload),
    )
}

private fun AthenaRuntimeProjectionRenderContribution.toPayload(): AthenaProjectionRenderContributionPayload {
    return AthenaProjectionRenderContributionPayload(
        pluginId = pluginId,
        contributionId = contributionId,
        displayName = displayName,
        description = description,
        rendererTarget = rendererTarget,
        surfaceMappings = surfaceMappings.map(AthenaRuntimeProjectionSurfaceMapping::toPayload),
    )
}

private fun AthenaRuntimeProjectionSurfaceMapping.toPayload(): AthenaProjectionSurfaceMappingPayload {
    return AthenaProjectionSurfaceMappingPayload(
        surface = surface,
        tokens = tokens.toSortedMap(),
    )
}

private fun AthenaRuntimeProjectionPoint.toPayload(): AthenaProjectionPointPayload {
    return AthenaProjectionPointPayload(
        x = x,
        y = y,
    )
}

private fun AthenaRuntimeViewerComponentBox.toPayload(): AthenaProjectionComponentPayload {
    return AthenaProjectionComponentPayload(
        projectionId = projectionId,
        semanticId = semanticId,
        label = label,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun AthenaRuntimeViewerConnectionLine.toPayload(): AthenaProjectionConnectionPayload {
    return AthenaProjectionConnectionPayload(
        projectionId = projectionId,
        semanticId = semanticId,
        x1 = x1,
        y1 = y1,
        x2 = x2,
        y2 = y2,
    )
}

private fun AthenaRuntimeViewerLabel.toPayload(): AthenaProjectionLabelPayload {
    return AthenaProjectionLabelPayload(
        projectionId = projectionId,
        semanticId = semanticId,
        label = label,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun projectionGovernedCommands(): List<AthenaProjectionGovernedCommandPayload> {
    return listOf(
        AthenaProjectionGovernedCommandPayload(
            commandId = SWITCH_ACTIVE_VIEW_COMMAND_ID,
            displayName = "Switch active view",
            description = "Switches the runtime-owned active projection view without opening a generic runtime tunnel.",
            requiredArguments = listOf("viewId"),
        ),
    )
}
