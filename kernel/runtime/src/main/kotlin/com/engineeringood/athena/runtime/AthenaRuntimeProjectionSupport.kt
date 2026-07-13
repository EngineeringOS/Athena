package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ProjectionCrossReference
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
    )
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
