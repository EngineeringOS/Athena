package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.presentation.PresentationDocument
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
) {
    init {
        require(policyId.isNotBlank()) { "Professional drawing policy id must not be blank." }
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
        fun m34RollingShutter(): AthenaProfessionalDrawingPolicy = AthenaProfessionalDrawingPolicy(
            policyId = "m34-rolling-shutter-control-drawing-v0",
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
)

data class AthenaProfessionalDrawingDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
) {
    override fun toString(): String = "[$code] $subject: $message"
}

data class AthenaProfessionalDrawingProof(
    val exactTerminalAttachment: Boolean = false,
    val componentAndLabelClearance: Boolean = false,
    val junctionCrossingSemanticsExplicit: Boolean = false,
    val graphicPrimitiveAuthorityOnly: Boolean = false,
    val rawMarkupAuthorityAbsent: Boolean = false,
    val fallbackAuthorityAbsent: Boolean = false,
    val rendererEngineeringInference: Boolean = true,
)

data class AthenaProfessionalDrawingResult(
    val presentation: PresentationDocument? = null,
    val diagnostics: List<AthenaProfessionalDrawingDiagnostic> = emptyList(),
    val proof: AthenaProfessionalDrawingProof = AthenaProfessionalDrawingProof(),
)

