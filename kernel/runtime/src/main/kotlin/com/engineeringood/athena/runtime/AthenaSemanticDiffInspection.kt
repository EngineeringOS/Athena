package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPropertyValue

/**
 * Runtime-owned source that produced one semantic diff inspection result.
 */
enum class AthenaSemanticDiffInspectionSource {
    COMMAND,
    UNDO,
    REDO,
    REPLAY,
}

/**
 * Runtime-owned change classification for one inspected semantic identity.
 */
enum class AthenaSemanticDiffChangeKind {
    ADDED,
    REMOVED,
    MODIFIED,
    CONTEXT,
}

/**
 * One semantic diff entry tied to a stable canonical semantic identity.
 */
data class AthenaSemanticDiffEntry(
    val semanticId: String,
    val semanticKind: String,
    val changeKind: AthenaSemanticDiffChangeKind,
    val beforeSummary: String?,
    val afterSummary: String?,
)

/**
 * One command-linked history consequence derived for semantic diff inspection.
 */
data class AthenaSemanticHistoryConsequence(
    val commandId: String,
    val commandKind: AthenaCommandKind,
    val status: AthenaCommandHistoryRecordStatus,
    val changedSemanticIds: List<String>,
)

/**
 * Runtime-owned downstream projection layer classifications attached to semantic review.
 */
enum class AthenaProjectionRefreshConsequenceLayer {
    LAYOUT,
    GEOMETRY,
    RENDERING,
}

/**
 * One runtime-owned projection refresh consequence anchored to canonical semantic identities.
 */
data class AthenaProjectionRefreshConsequence(
    val layer: AthenaProjectionRefreshConsequenceLayer,
    val mode: String?,
    val affectedViewIds: List<String>,
    val affectedSemanticIds: List<String>,
)

/**
 * Runtime-owned inspection artifact for one semantic change or history consequence review.
 */
data class AthenaSemanticDiffInspection(
    val projectName: String,
    val source: AthenaSemanticDiffInspectionSource,
    val affectedCommandIds: List<String>,
    val affectedSemanticIds: List<String>,
    val entries: List<AthenaSemanticDiffEntry>,
    val historyConsequences: List<AthenaSemanticHistoryConsequence>,
    val projectionConsequences: List<AthenaProjectionRefreshConsequence>,
)

internal fun buildSemanticDiffInspection(
    projectName: String,
    source: AthenaSemanticDiffInspectionSource,
    affectedCommandIds: List<String>,
    affectedSemanticIds: List<String>,
    beforeDocument: EngineeringDocument,
    afterDocument: EngineeringDocument,
    history: AthenaCommandHistory,
    projectionConsequences: List<AthenaProjectionRefreshConsequence> = emptyList(),
): AthenaSemanticDiffInspection {
    val normalizedAffectedSemanticIds = affectedSemanticIds.distinct().sorted()
    return AthenaSemanticDiffInspection(
        projectName = projectName,
        source = source,
        affectedCommandIds = affectedCommandIds.distinct(),
        affectedSemanticIds = normalizedAffectedSemanticIds,
        entries = normalizedAffectedSemanticIds.map { semanticId ->
            val beforeSnapshot = beforeDocument.semanticSnapshot(semanticId)
            val afterSnapshot = afterDocument.semanticSnapshot(semanticId)
            AthenaSemanticDiffEntry(
                semanticId = semanticId,
                semanticKind = afterSnapshot?.kind ?: beforeSnapshot?.kind ?: "unknown",
                changeKind = when {
                    beforeSnapshot == null && afterSnapshot != null -> AthenaSemanticDiffChangeKind.ADDED
                    beforeSnapshot != null && afterSnapshot == null -> AthenaSemanticDiffChangeKind.REMOVED
                    beforeSnapshot != afterSnapshot -> AthenaSemanticDiffChangeKind.MODIFIED
                    else -> AthenaSemanticDiffChangeKind.CONTEXT
                },
                beforeSummary = beforeSnapshot?.summary,
                afterSummary = afterSnapshot?.summary,
                )
            },
        projectionConsequences = projectionConsequences.map { consequence ->
            consequence.copy(
                affectedViewIds = consequence.affectedViewIds.distinct().sorted(),
                affectedSemanticIds = consequence.affectedSemanticIds.distinct().sorted(),
            )
        }.sortedBy { consequence -> consequence.layer.name },
        historyConsequences = history.records
            .filter { record -> record.commandId in affectedCommandIds }
            .map { record ->
                AthenaSemanticHistoryConsequence(
                    commandId = record.commandId,
                    commandKind = record.commandKind,
                    status = record.status,
                    changedSemanticIds = record.changedSemanticIds.sorted(),
                )
            },
    )
}

private data class AthenaSemanticObjectSnapshot(
    val kind: String,
    val summary: String,
)

private fun EngineeringDocument.semanticSnapshot(semanticId: String): AthenaSemanticObjectSnapshot? {
    if (system.id.value == semanticId) {
        return AthenaSemanticObjectSnapshot(
            kind = "system",
            summary = "system ${system.name}",
        )
    }

    components.firstOrNull { component -> component.id.value == semanticId }?.let { component ->
        return AthenaSemanticObjectSnapshot(
            kind = "component",
            summary = "component ${component.name} (${component.kind})",
        )
    }

    ports.firstOrNull { port -> port.id.value == semanticId }?.let { port ->
        return AthenaSemanticObjectSnapshot(
            kind = "port",
            summary = "port ${port.summaryPath()} [${port.properties.summaryText()}]",
        )
    }

    connections.firstOrNull { connection -> connection.id.value == semanticId }?.let { connection ->
        return AthenaSemanticObjectSnapshot(
            kind = "connection",
            summary = "connection ${connection.from.authoredPath()} -> ${connection.to.authoredPath()}",
        )
    }

    return null
}

private fun List<com.engineeringood.athena.ir.EngineeringProperty>.summaryText(): String {
    return joinToString(separator = ", ") { property ->
        "${property.name}=${property.value.summaryText()}"
    }.ifBlank { "no properties" }
}

private fun com.engineeringood.athena.ir.EngineeringPropertyValue.summaryText(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> text
    }
}

internal fun AthenaRuntimeIncrementalUpdateReport.toProjectionConsequences(): List<AthenaProjectionRefreshConsequence> {
    val normalizedSemanticIds = changedSemanticIds.distinct().sorted()
    return buildList {
        add(
            AthenaProjectionRefreshConsequence(
                layer = AthenaProjectionRefreshConsequenceLayer.LAYOUT,
                mode = layoutMode,
                affectedViewIds = layoutScopedViewIds,
                affectedSemanticIds = normalizedSemanticIds,
            ),
        )
        add(
            AthenaProjectionRefreshConsequence(
                layer = AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
                mode = geometryMode,
                affectedViewIds = geometryScopedViewIds,
                affectedSemanticIds = normalizedSemanticIds,
            ),
        )
        if (renderingViewIds.isNotEmpty()) {
            add(
                AthenaProjectionRefreshConsequence(
                    layer = AthenaProjectionRefreshConsequenceLayer.RENDERING,
                    mode = renderingMode,
                    affectedViewIds = renderingViewIds,
                    affectedSemanticIds = normalizedSemanticIds,
                ),
            )
        }
    }
}

private fun com.engineeringood.athena.ir.EngineeringReference.authoredPath(): String = authoredPath.joinToString(".")

private fun com.engineeringood.athena.ir.EngineeringPort.summaryPath(): String = (ownerReference.authoredPath + name).joinToString(".")
