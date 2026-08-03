package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceLink
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNotationPack
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetPolicyEvidence
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
        policyEvidence = policyEvidence?.toRuntimeProjectionSheetPolicyEvidence(),
        publication = publication.toRuntimeProjectionSheetPublication(),
        composition = composition.toRuntimeProjectionSheetComposition(),
    )
}

private fun ProjectionSheetPolicyEvidence.toRuntimeProjectionSheetPolicyEvidence(): AthenaRuntimeProjectionSheetPolicyEvidence {
    return AthenaRuntimeProjectionSheetPolicyEvidence(
        policyId = policyId,
        policyVersion = policyVersion,
        policyDeterministicIdentity = policyDeterministicIdentity,
        sheetViewRole = sheetViewRole,
        sheetViewRoleOrder = sheetViewRoleOrder,
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
        crossReferenceId = crossReferenceId.value,
        sheetIds = sheetIds.map { sheetId -> sheetId.value },
        occurrenceIds = occurrenceIds.sorted(),
        links = links.map(ProjectionCrossReferenceLink::toRuntimeProjectionCrossReferenceLink),
    )
}

private fun ProjectionCrossReferenceLink.toRuntimeProjectionCrossReferenceLink(): AthenaRuntimeProjectionCrossReferenceLink {
    return AthenaRuntimeProjectionCrossReferenceLink(
        semanticId = semanticId.value,
        sourceSheetId = sourceSheetId.value,
        targetSheetId = targetSheetId.value,
        sourceOccurrenceId = sourceOccurrenceId,
        targetOccurrenceId = targetOccurrenceId,
        compactNotation = compactNotation,
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
