package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNotationPack
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.projection.ProjectionPoint
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

internal const val GRAPH_WORKBENCH_RENDERER_TARGET = "graph-workbench"

internal fun CompilerSyntaxDiagnostic.toProjectionDiagnostic(): AthenaRuntimeProjectionDiagnostic {
    return AthenaRuntimeProjectionDiagnostic(
        severity = "error",
        code = "compiler.syntax",
        message = message,
        provenance = "$file:$line:$column",
    )
}

internal fun SemanticDiagnostic.toProjectionDiagnostic(): AthenaRuntimeProjectionDiagnostic {
    return AthenaRuntimeProjectionDiagnostic(
        severity = severity.name.lowercase(),
        code = ruleId.value,
        message = message,
        provenance = "${provenance.file}:${provenance.startLine}:${provenance.startColumn}",
    )
}

internal fun AthenaExecutionContext.activeProjectionRenderContributions(
    viewId: String,
    rendererTarget: String,
): List<AthenaRuntimeProjectionRenderContribution> {
    return pluginRuntimeServices().renderContributions().flatMap { contributionSet ->
        contributionSet.renderContributions.mapNotNull { contribution ->
            val supportsView = contribution.viewIds.isEmpty() || viewId in contribution.viewIds
            val supportsTarget = contribution.rendererTargets.isEmpty() || rendererTarget in contribution.rendererTargets
            if (!supportsView || !supportsTarget) {
                null
            } else {
                AthenaRuntimeProjectionRenderContribution(
                    pluginId = contributionSet.pluginId,
                    contributionId = contribution.contributionId,
                    displayName = contribution.displayName,
                    description = contribution.description,
                    rendererTarget = rendererTarget,
                    surfaceMappings = contribution.surfaceMappings.map(AthenaRenderSurfaceMapping::toRuntimeProjectionSurfaceMapping),
                )
            }
        }
    }
}

private fun AthenaRenderSurfaceMapping.toRuntimeProjectionSurfaceMapping(): AthenaRuntimeProjectionSurfaceMapping {
    return AthenaRuntimeProjectionSurfaceMapping(
        surface = surface.name.lowercase(),
        tokens = tokens.toSortedMap(),
    )
}

internal fun ProjectionSheet.toRuntimeProjectionSheet(): AthenaRuntimeProjectionSheet {
    return AthenaRuntimeProjectionSheet(
        sheetId = sheetId.value,
        displayName = displayName,
        order = order,
        previousSheetId = previousSheetId?.value,
        nextSheetId = nextSheetId?.value,
        subjectSemanticIds = subjects.map { subject -> subject.semanticId.value },
        publication = publication.toRuntimeProjectionSheetPublication(),
        composition = composition.toRuntimeProjectionSheetComposition(),
    )
}

internal fun ProjectionDocument.toRuntimeProjectionSheetLayout(
    scene: AthenaRuntimeViewerScene,
): AthenaRuntimeProjectionSheetLayout? {
    val sheet = sheets.firstOrNull() ?: return null
    val connectionById = connections.associateBy { connection -> connection.projectionId.value }
    return AthenaRuntimeProjectionSheetLayout(
        sheetId = sheet.sheetId.value,
        displayName = sheet.displayName,
        order = sheet.order,
        subjectSemanticIds = sheet.subjects.map { subject -> subject.semanticId.value },
        representationFamilyId = sheet.composition.representationFamilyId,
        frame = AthenaRuntimeProjectionSheetLayoutFrame(
            canvasWidth = scene.canvasWidth,
            canvasHeight = scene.canvasHeight,
        ),
        placements = scene.components
            .sortedBy { component -> component.projectionId }
            .map { component ->
                AthenaRuntimeProjectionSheetLayoutPlacement(
                    projectionId = component.projectionId,
                    semanticId = component.semanticId,
                    x = component.x,
                    y = component.y,
                    width = component.width,
                    height = component.height,
                )
            },
        routingGuidance = electricalRoutingCorridors
            .sortedBy { corridor -> corridor.corridorId.value }
            .mapNotNull { corridor ->
                val connection = connectionById[corridor.projectionConnectionId.value] ?: return@mapNotNull null
                AthenaRuntimeProjectionSheetLayoutRoutingGuidance(
                    projectionConnectionId = corridor.projectionConnectionId.value,
                    connectionSemanticId = corridor.connectionSemanticId.value,
                    sourcePoint = connection.start.toRuntimeProjectionPoint(),
                    targetPoint = connection.end.toRuntimeProjectionPoint(),
                    routingStyle = corridor.routingStyle.name.lowercase(),
                    bendPoints = corridor.preferredBendPoints.map(ProjectionPoint::toRuntimeProjectionPoint),
                )
            },
        labelLayouts = scene.labels
            .sortedBy { label -> label.projectionId }
            .map { label ->
                AthenaRuntimeProjectionSheetLayoutLabelLayout(
                    projectionId = label.projectionId,
                    semanticId = label.semanticId,
                    label = label.label,
                    x = label.x,
                    y = label.y,
                    width = label.width,
                    height = label.height,
                )
            },
    )
}

private fun com.engineeringood.athena.projection.ProjectionSheetPublication.toRuntimeProjectionSheetPublication(): AthenaRuntimeProjectionSheetPublication {
    return AthenaRuntimeProjectionSheetPublication(
        pageSize = AthenaRuntimeProjectionSheetPageSize(
            format = pageSize.format,
            orientation = pageSize.orientation,
        ),
        frame = AthenaRuntimeProjectionSheetFrame(
            frameId = frame.frameId,
            style = frame.style,
        ),
        coordinateZones = coordinateZones.map { zone ->
            AthenaRuntimeProjectionSheetCoordinateZone(
                zoneId = zone.zoneId,
                label = zone.label,
                order = zone.order,
            )
        },
        titleBlock = AthenaRuntimeProjectionSheetTitleBlock(
            sheetTitle = titleBlock.sheetTitle,
            sheetFamily = titleBlock.sheetFamily,
            sheetNumber = titleBlock.sheetNumber,
        ),
        revisionMetadata = AthenaRuntimeProjectionSheetRevisionMetadata(
            revisionCode = revisionMetadata.revisionCode,
            revisionNote = revisionMetadata.revisionNote,
        ),
        viewComposition = AthenaRuntimeProjectionSheetViewComposition(
            primaryViewId = viewComposition.primaryViewId,
            primarySheetOrder = viewComposition.primarySheetOrder,
            subjectSemanticIds = viewComposition.subjectSemanticIds,
        ),
    )
}

private fun com.engineeringood.athena.projection.ProjectionSheetComposition.toRuntimeProjectionSheetComposition(): AthenaRuntimeProjectionSheetComposition {
    return AthenaRuntimeProjectionSheetComposition(
        sheetId = sheetId.value,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds(),
        representationFamilyId = representationFamilyId,
        publication = publication.toRuntimeProjectionSheetPublication(),
    )
}

private fun com.engineeringood.athena.projection.ProjectionSheetComposition.subjectSemanticIds(): List<String> {
    return subjects.map { subject -> subject.semanticId.value }
}

internal fun ProjectionNotationPack.toRuntimeProjectionNotationPack(): AthenaRuntimeProjectionNotationPack {
    return AthenaRuntimeProjectionNotationPack(
        packId = packId.value,
        displayName = displayName,
        subjects = subjects.map(ProjectionNotationSubject::toRuntimeProjectionNotationSubject),
    )
}

internal fun ProjectionCrossReference.toRuntimeProjectionCrossReference(): AthenaRuntimeProjectionCrossReference {
    return AthenaRuntimeProjectionCrossReference(
        semanticId = semanticId.value,
        kind = kind.name.lowercase(),
        sheetIds = sheetIds.map { sheetId -> sheetId.value },
        occurrenceIds = occurrenceIds.sorted(),
    )
}

internal fun ElectricalAnchor.toRuntimeProjectionElectricalAnchor(): AthenaRuntimeProjectionElectricalAnchor {
    return AthenaRuntimeProjectionElectricalAnchor(
        anchorId = anchorId.value,
        portSemanticId = portSemanticId.value,
        ownerSemanticId = ownerSemanticId.value,
        nodeId = nodeId.value,
        labelId = labelId?.value,
        x = position.x,
        y = position.y,
        side = side.name.lowercase(),
    )
}

internal fun ElectricalConnectionEndpoint.toRuntimeProjectionElectricalConnectionEndpoint(): AthenaRuntimeProjectionElectricalConnectionEndpoint {
    return AthenaRuntimeProjectionElectricalConnectionEndpoint(
        endpointId = endpointId.value,
        projectionConnectionId = projectionConnectionId.value,
        connectionSemanticId = connectionSemanticId.value,
        endpointRole = endpointRole.name.lowercase(),
        portSemanticId = portSemanticId.value,
        anchorId = anchorId.value,
    )
}

internal fun ElectricalRoutingCorridor.toRuntimeProjectionElectricalRoutingCorridor(): AthenaRuntimeProjectionElectricalRoutingCorridor {
    return AthenaRuntimeProjectionElectricalRoutingCorridor(
        corridorId = corridorId.value,
        projectionConnectionId = projectionConnectionId.value,
        connectionSemanticId = connectionSemanticId.value,
        sourceAnchorId = sourceAnchorId.value,
        targetAnchorId = targetAnchorId.value,
        routingStyle = routingStyle.name.lowercase(),
        preferredBendPoints = preferredBendPoints.map(ProjectionPoint::toRuntimeProjectionPoint),
    )
}

internal fun com.engineeringood.athena.layout.ProjectionFamilyContract?.toRuntimeProjectionFamilyId(): String? {
    return when (this) {
        is ElectricalProjectionDescriptor -> "electrical/${family.name.lowercase()}"
        null -> null
    }
}

private fun ProjectionNotationSubject.toRuntimeProjectionNotationSubject(): AthenaRuntimeProjectionNotationSubject {
    return AthenaRuntimeProjectionNotationSubject(
        semanticId = semanticId.value,
        symbolKey = symbolKey.value,
        labelPolicy = labelPolicy.name.lowercase(),
        markerKeys = markerKeys,
    )
}

private fun ProjectionPoint.toRuntimeProjectionPoint(): AthenaRuntimeProjectionPoint {
    return AthenaRuntimeProjectionPoint(
        x = x,
        y = y,
    )
}
