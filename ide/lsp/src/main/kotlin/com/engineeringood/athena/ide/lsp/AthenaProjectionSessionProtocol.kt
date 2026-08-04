package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionCrossReference
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionCrossReferenceLink
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionDiagnostic
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionNotationPack
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionNotationSubject
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionRenderContribution
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSession
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionView
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheet
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSheetPolicyEvidence
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSurfaceMapping
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeViewerScene
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialSheetFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialOccurrenceFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialRegionFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialConstructFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialAnchorFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialRouteFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialLaneFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialGridReferenceFacts
import com.engineeringood.athena.runtime.AthenaRuntimeSpatialQualityFacts
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
        ?: primaryTrackedDocument(snapshot, languageFeatures)
    return trackedDocument?.let { tracked ->
        context.previewProjectionSession(tracked.compilation)
    } ?: context.projectProjectionSession()
}

private fun primaryTrackedDocument(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures?,
): AthenaTrackedDocument? {
    val sourcePath = snapshot?.sourcePath ?: return null
    val features = languageFeatures ?: return null
    return features.trackedDocumentByPath(sourcePath) ?: runCatching {
        features.trackDocument(
            uri = sourcePath.toUri().toString(),
            path = sourcePath,
            version = 0,
            text = Files.readString(sourcePath),
        )
    }.getOrNull()
}

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
            notationPack = notationPack,
            crossReferences = crossReferences,
            activeRenderContributions = activeRenderContributions,
            projectionRegionIds = projectionRegionIds,
            projectionConstructIds = projectionConstructIds,
            spatialFacts = spatialFacts,
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
    activeRenderContributions: List<AthenaRuntimeProjectionRenderContribution>,
    projectionRegionIds: List<String>,
    projectionConstructIds: List<String>,
    spatialFacts: AthenaRuntimeSpatialFacts?,
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
        activeRenderContributions = activeRenderContributions.map(AthenaRuntimeProjectionRenderContribution::toPayload),
        components = components.map { component ->
            AthenaProjectionComponentPayload(
                projectionId = component.projectionId,
                semanticId = component.semanticId,
                label = component.label,
                x = component.x,
                y = component.y,
                width = component.width,
                height = component.height,
            )
        },
        connections = connections.map { connection ->
            AthenaProjectionConnectionPayload(
                projectionId = connection.projectionId,
                semanticId = connection.semanticId,
                x1 = connection.x1,
                y1 = connection.y1,
                x2 = connection.x2,
                y2 = connection.y2,
            )
        },
        labels = labels.map { label ->
            AthenaProjectionLabelPayload(
                projectionId = label.projectionId,
                semanticId = label.semanticId,
                label = label.label,
                x = label.x,
                y = label.y,
                width = label.width,
                height = label.height,
            )
        },
        projectionRegionIds = projectionRegionIds.toList(),
        projectionConstructIds = projectionConstructIds.toList(),
        spatialFacts = spatialFacts?.toPayload(),
    )
}

private fun AthenaRuntimeSpatialFacts.toPayload(): AthenaSpatialFactsPayload =
    AthenaSpatialFactsPayload(
        viewId = viewId,
        activeSheetId = activeSheetId,
        sheets = sheets.map(AthenaRuntimeSpatialSheetFacts::toPayload),
    )

private fun AthenaRuntimeSpatialSheetFacts.toPayload(): AthenaSpatialSheetFactsPayload =
    AthenaSpatialSheetFactsPayload(
        sheetId = sheetId,
        extent = AthenaRectPayload(extent.x, extent.y, extent.width, extent.height),
        drawingArea = AthenaRectPayload(drawingArea.x, drawingArea.y, drawingArea.width, drawingArea.height),
        occurrences = occurrences.map(AthenaRuntimeSpatialOccurrenceFacts::toPayload),
        regions = regions.map(AthenaRuntimeSpatialRegionFacts::toPayload),
        constructs = constructs.map(AthenaRuntimeSpatialConstructFacts::toPayload),
        anchors = anchors.map(AthenaRuntimeSpatialAnchorFacts::toPayload),
        routes = routes.map(AthenaRuntimeSpatialRouteFacts::toPayload),
        lanes = lanes.map(AthenaRuntimeSpatialLaneFacts::toPayload),
        gridReferences = gridReferences.map(AthenaRuntimeSpatialGridReferenceFacts::toPayload),
        quality = quality.toPayload(),
    )

private fun AthenaRuntimeSpatialOccurrenceFacts.toPayload() =
    AthenaSpatialOccurrenceFactsPayload(occurrenceId, semanticId, regionId, bounds.toPayload())

private fun AthenaRuntimeSpatialRegionFacts.toPayload() =
    AthenaSpatialRegionFactsPayload(regionId, bounds.toPayload(), memberOccurrenceIds)

private fun AthenaRuntimeSpatialConstructFacts.toPayload() =
    AthenaSpatialConstructFactsPayload(constructId, kind, name, bounds.toPayload(), memberOccurrenceIds)

private fun AthenaRuntimeSpatialAnchorFacts.toPayload() =
    AthenaSpatialAnchorFactsPayload(anchorId, occurrenceId, portSemanticId, side, point.toPayload())

private fun AthenaRuntimeSpatialRouteFacts.toPayload() =
    AthenaSpatialRouteFactsPayload(
        routeId,
        projectionConnectionId,
        connectionId,
        sourceAnchorId,
        targetAnchorId,
        laneId,
        points.map { point -> point.toPayload() },
    )

private fun AthenaRuntimeSpatialLaneFacts.toPayload() =
    AthenaSpatialLaneFactsPayload(laneId, orientation, coordinate, routeIds)

private fun AthenaRuntimeSpatialGridReferenceFacts.toPayload() =
    AthenaSpatialGridReferenceFactsPayload(gridReferenceId, subjectId, cellReference, rowLabel, columnNumber)

private fun AthenaRuntimeSpatialQualityFacts.toPayload() =
    AthenaSpatialQualityFactsPayload(
        occurrenceOverlapCount,
        constructContainmentFailureCount,
        routeBodyIntersectionCount,
        routeCrossingCount,
        twistCount,
        usedLaneCount,
        peakRoutesPerLane,
        density,
        occupancy,
    )

private fun com.engineeringood.athena.runtime.AthenaRuntimeRect.toPayload() =
    AthenaRectPayload(x, y, width, height)

private fun com.engineeringood.athena.runtime.AthenaRuntimePoint.toPayload() =
    AthenaSpatialPointPayload(x, y)

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
