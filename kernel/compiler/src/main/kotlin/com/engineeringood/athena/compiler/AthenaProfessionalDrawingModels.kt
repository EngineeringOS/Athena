package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.routing.DrawingBounds
import java.nio.file.Path

/** Renderer-neutral policy for compiling one governed engineering drawing sheet. */
data class AthenaProfessionalDrawingPolicy(
    val policyId: String,
    val sheetWidth: Int,
    val sheetHeight: Int,
    val frameToSheet: Int,
    val coordinateBandSize: Int,
    val titleBlockHeight: Int,
    val columnLabels: List<String>,
    val rowLabels: List<String>,
    val occurrenceHorizontalPadding: Int,
    val occurrenceVerticalPadding: Int,
    val powerRegionColumnCount: Int,
    val author: String,
    val title: String,
    val publicationDate: String,
    val folio: String,
    val viewId: String = "schematic",
    val viewDisplayName: String = "Control Drawing",
    val materialProjectionContext: String = "schematic",
) {
    init {
        require(policyId.isNotBlank()) { "Professional drawing policy id must not be blank." }
        require(viewId.isNotBlank()) { "Professional drawing view id must not be blank." }
        require(viewDisplayName.isNotBlank()) { "Professional drawing view display name must not be blank." }
        require(sheetWidth > 0 && sheetHeight > 0) { "Professional drawing sheet dimensions must be positive." }
        require(frameToSheet >= 0 && coordinateBandSize >= 0 && titleBlockHeight > 0) {
            "Professional drawing frame, coordinate band, and title dimensions are invalid."
        }
        require(columnLabels.isNotEmpty() && rowLabels.isNotEmpty()) {
            "Professional drawing policy requires column and row labels."
        }
        require(powerRegionColumnCount in 1 until columnLabels.size) {
            "Power region must leave at least one column for the control region."
        }
    }

    companion object {
        fun rollingShutterControlDrawing(): AthenaProfessionalDrawingPolicy = AthenaProfessionalDrawingPolicy(
            policyId = "rolling-shutter-control-drawing",
            sheetWidth = 1050,
            sheetHeight = 720,
            frameToSheet = 5,
            coordinateBandSize = 20,
            titleBlockHeight = 50,
            columnLabels = (1..17).map(Int::toString),
            rowLabels = ('A'..'H').map(Char::toString),
            occurrenceHorizontalPadding = 7,
            occurrenceVerticalPadding = 8,
            powerRegionColumnCount = 8,
            author = "EngineeringOS",
            title = "Equipment d'un volet roulant",
            publicationDate = "2026-07-26",
            folio = "1/1",
        )
    }
}

data class AthenaProfessionalDrawingRequest(
    val repositoryRoot: Path,
    val document: EngineeringDocument,
    val semanticSnapshot: ProjectSemanticGraphSnapshot,
    val policy: AthenaProfessionalDrawingPolicy,
    val selectedProjectionPolicy: AthenaProjectionPolicySelection? = null,
)

data class AthenaProfessionalDrawingDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
) {
    override fun toString(): String = "[$code] $subject: $message"
}

data class AthenaProfessionalDrawingEvidence(
    val exactTerminalAttachment: Boolean = false,
    val componentAndLabelClearance: Boolean = false,
    val junctionCrossingSemanticsExplicit: Boolean = false,
    val graphicPrimitiveAuthorityOnly: Boolean = false,
    val rawMarkupAuthorityAbsent: Boolean = false,
    val fallbackAuthorityAbsent: Boolean = false,
    val connectionPresentationClassified: Boolean = false,
    val looseEndpointsAbsent: Boolean = false,
    val routeBodyIntersectionsAbsent: Boolean = false,
    val ambiguousCrossingsAbsent: Boolean = false,
    val labelCollisionsAbsent: Boolean = false,
    val unclassifiedRoutesAbsent: Boolean = false,
    val rendererEngineeringInference: Boolean = true,
    val trace: AthenaProfessionalDrawingTrace = AthenaProfessionalDrawingTrace(),
)

data class AthenaProfessionalDrawingTrace(
    val occurrences: List<AthenaProfessionalOccurrenceTrace> = emptyList(),
    val routes: List<AthenaProfessionalRouteTrace> = emptyList(),
    val connectionLabels: List<AthenaProfessionalRouteLabelTrace> = emptyList(),
    val routeMarkers: List<AthenaProfessionalRouteMarkerTrace> = emptyList(),
    val sheetStructures: List<AthenaProfessionalSheetStructureTrace> = emptyList(),
    val evidenceInputs: List<AthenaProfessionalEvidenceInputTrace> = emptyList(),
    val forbiddenAuthorityKinds: List<String> = emptyList(),
)

data class AthenaProfessionalSourceSpan(
    val file: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

data class AthenaProfessionalOccurrenceTrace(
    val occurrenceId: String,
    val semanticSubjectId: String,
    val physicalComponentId: String,
    val functionId: String?,
    val packageId: String,
    val definitionId: String,
    val bindingRuleId: String,
    val packageResourceIds: List<String>,
    val anchorIds: List<String>,
    val labelIds: List<String>,
    val sourceSpan: AthenaProfessionalSourceSpan,
)

data class AthenaProfessionalRouteTrace(
    val routeId: String,
    val connectionId: String,
    val routeContractId: String,
    val sourcePortSemanticId: String,
    val targetPortSemanticId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val laneId: String,
    val routeLabelIds: List<String>,
    val lineClassId: String,
    val projectionPolicyId: String,
    val compilerSnapshotId: String,
    val sourceSpan: AthenaProfessionalSourceSpan,
)

data class AthenaProfessionalRouteLabelTrace(
    val labelId: String,
    val routeId: String,
    val bounds: DrawingBounds,
    val sourceSpan: AthenaProfessionalSourceSpan,
)

data class AthenaProfessionalRouteMarkerTrace(
    val markerId: String,
    val kind: String,
    val routeIds: List<String>,
    val sourceSpan: AthenaProfessionalSourceSpan,
)

data class AthenaProfessionalSheetStructureTrace(
    val structureId: String,
    val kind: String,
    val memberIds: List<String>,
    val sourceSpan: AthenaProfessionalSourceSpan,
)

data class AthenaProfessionalEvidenceInputTrace(
    val evidenceId: String,
    val evidenceIds: List<String>,
    val diagnosticCodes: List<String>,
    val constant: Boolean = false,
)

internal object AthenaProfessionalDrawingTraceValidator {
    fun validate(trace: AthenaProfessionalDrawingTrace): List<AthenaProfessionalDrawingDiagnostic> {
        val diagnostics = mutableListOf<AthenaProfessionalDrawingDiagnostic>()
        if (trace.occurrences.isEmpty() || trace.routes.isEmpty() || trace.sheetStructures.isEmpty()) {
            diagnostics += diagnostic("drawing.trace.missing", "professional-drawing", "Professional drawing trace requires occurrence, route, and sheet evidence.")
        }
        trace.occurrences.filter { occurrence -> occurrence.sourceSpan.file.isBlank() }.forEach { occurrence ->
            diagnostics += diagnostic("drawing.trace.source-missing", occurrence.occurrenceId, "Occurrence trace requires source span evidence.")
        }
        trace.routes.filter { route -> route.routeContractId.isBlank() || route.laneId.isBlank() }.forEach { route ->
            diagnostics += diagnostic("drawing.trace.route-fact-missing", route.routeId, "Route trace requires route contract and lane evidence.")
        }
        trace.routes.filter { route -> route.lineClassId.isBlank() }.forEach { route ->
            diagnostics += diagnostic("drawing.trace.presentation-class-missing", route.routeId, "Route trace requires presentation class evidence.")
        }
        trace.routes.filter { route -> route.routeLabelIds.isEmpty() }.forEach { route ->
            diagnostics += diagnostic("drawing.trace.label-fact-missing", route.routeId, "Route trace requires label evidence.")
        }
        trace.occurrences.filter { occurrence -> occurrence.packageResourceIds.isEmpty() }.forEach { occurrence ->
            diagnostics += diagnostic("drawing.trace.package-resource-missing", occurrence.occurrenceId, "Occurrence trace requires package resource evidence.")
        }
        trace.evidenceInputs.filter { input -> input.constant || input.evidenceIds.isEmpty() }.forEach { input ->
            diagnostics += diagnostic("drawing.evidence.constant-forbidden", input.evidenceId, "Evidence gates must be derived from compiled evidence and diagnostics.")
        }
        trace.forbiddenAuthorityKinds.forEach { authority ->
            diagnostics += diagnostic("drawing.trace.raw-authority", authority, "Trace payload must not expose raw renderer, XML, SVG markup, DOM, or planner-native authority.")
        }
        return diagnostics.distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
    }

    private fun diagnostic(code: String, subject: String, message: String) =
        AthenaProfessionalDrawingDiagnostic(code, subject.ifBlank { "unknown" }, message)
}

data class AthenaProfessionalDrawingResult(
    val presentation: PresentationDocument? = null,
    val diagnostics: List<AthenaProfessionalDrawingDiagnostic> = emptyList(),
    val evidence: AthenaProfessionalDrawingEvidence = AthenaProfessionalDrawingEvidence(),
)
