package com.engineeringood.athena.ide.lsp

class AthenaProjectionSessionParams

data class AthenaProjectionSessionPayload(
    val projectName: String,
    val semanticPath: String,
    val activeViewId: String?,
    val supportedViews: List<AthenaProjectionViewPayload>,
    val status: String,
    val projection: AthenaProjectionDocumentPayload? = null,
    val spatial: AthenaSpatialDocumentSummaryPayload? = null,
    val unavailableReason: String? = null,
    val diagnostics: List<AthenaProjectionDiagnosticPayload> = emptyList(),
)

data class AthenaProjectionViewPayload(
    val viewId: String,
    val displayName: String,
    val description: String?,
)

data class AthenaProjectionDocumentPayload(
    val viewId: String,
    val activeSheetId: String?,
    val nodes: List<AthenaProjectionNodePayload>,
    val connections: List<AthenaProjectionConnectionPayload>,
    val sheets: List<AthenaProjectionSheetPayload>,
)

data class AthenaProjectionNodePayload(
    val projectionId: String,
    val semanticId: String,
    val label: String,
)

data class AthenaProjectionConnectionPayload(
    val projectionId: String,
    val semanticId: String,
    val sourceOccurrenceId: String?,
    val sourcePortId: String?,
    val targetOccurrenceId: String?,
    val targetPortId: String?,
)

data class AthenaProjectionSheetPayload(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String>,
)

data class AthenaSpatialDocumentSummaryPayload(
    val sheets: List<AthenaSpatialSheetSummaryPayload>,
)

data class AthenaSpatialSheetSummaryPayload(
    val sheetId: String,
    val occurrenceCount: Int,
    val regionCount: Int,
    val constructCount: Int,
    val anchorCount: Int,
    val routeCount: Int,
    val gridReferenceCount: Int,
)

data class AthenaProjectionDiagnosticPayload(
    val severity: String,
    val code: String,
    val message: String,
    val provenance: String? = null,
)
