package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionCrossReference
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
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheet
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSurfaceMapping
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeViewerComponentBox
import com.engineeringood.athena.runtime.AthenaRuntimeViewerConnectionLine
import com.engineeringood.athena.runtime.AthenaRuntimeViewerLabel
import com.engineeringood.athena.runtime.AthenaRuntimeViewerScene
import com.engineeringood.athena.presentation.PresentationDocument

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
                ?.takeIf { tracked -> tracked.path.normalize() == sourcePath.normalize() }
        }

        else -> null
    } ?: sourcePath?.let { path -> languageFeatures?.trackedDocumentByPath(path) }
    return trackedDocument?.let { tracked ->
        context.previewProjectionSession(tracked.compilation)
    } ?: context.projectProjectionSession()
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
        sheetIds = sheetIds,
        occurrenceIds = occurrenceIds,
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
