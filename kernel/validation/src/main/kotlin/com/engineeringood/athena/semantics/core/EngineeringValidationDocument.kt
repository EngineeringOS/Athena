package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.SourceProvenance

enum class EngineeringValidationState { READY, INCOMPLETE, INVALID }

enum class EngineeringRequirementStatus { SATISFIED, UNSATISFIED, UNRESOLVED }

data class EngineeringRequirement(
    val id: String,
    val subject: EngineeringReference,
    val capability: EngineeringDefinitionId,
    val provenance: SourceProvenance,
)

data class EngineeringSatisfactionEvidence(
    val requirementId: String,
    val provider: EngineeringReference?,
    val capability: EngineeringDefinitionId,
    val status: EngineeringRequirementStatus,
    val provenance: List<SourceProvenance>,
)

data class EngineeringValidationDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val provenance: List<SourceProvenance>,
)

data class EngineeringValidationDocument(
    val state: EngineeringValidationState,
    val requirements: List<EngineeringRequirement>,
    val satisfaction: List<EngineeringSatisfactionEvidence>,
    val diagnostics: List<EngineeringValidationDiagnostic>,
    val provenance: List<SourceProvenance>,
    val judgements: List<EngineeringJudgement> = emptyList(),
    val correctionOptions: List<EngineeringCorrectionOption> = emptyList(),
    val derivation: List<EngineeringDerivationStep> = emptyList(),
    val impact: List<EngineeringImpactEntry> = emptyList(),
) {
    init {
        require(requirements.map { it.id } == requirements.map { it.id }.sorted())
        require(satisfaction.map { it.requirementId } == satisfaction.map { it.requirementId }.sorted())
        if (state == EngineeringValidationState.INVALID) {
            require(satisfaction.isEmpty()) { "INVALID validation cannot publish satisfaction evidence." }
        }
        require(judgements == judgements.sortedBy { it.id })
        require(correctionOptions == correctionOptions.sortedBy { it.id })
        require(derivation == derivation.sortedBy { it.order })
        require(impact == impact.sortedBy { it.id })
    }
}

enum class EngineeringJudgementStatus { UNSATISFIED, UNRESOLVED, INVALID }

data class EngineeringJudgement(
    val id: String,
    val status: EngineeringJudgementStatus,
    val subject: EngineeringReference,
    val capability: EngineeringDefinitionId,
    val problem: String,
    val expected: EngineeringValue? = null,
    val actual: EngineeringValue? = null,
    val authority: EngineeringDefinitionId? = capability,
    val correctionDirection: String,
    val provenance: List<SourceProvenance>,
)

sealed interface EngineeringCorrectionOption {
    val id: String
    val subject: EngineeringReference
    val evidence: List<SourceProvenance>
    val description: String

    data class ChangeValue(
        override val id: String,
        override val subject: EngineeringReference,
        val property: String,
        val value: EngineeringValue,
        override val evidence: List<SourceProvenance>,
        override val description: String,
    ) : EngineeringCorrectionOption

    data class AddRelationship(
        override val id: String,
        override val subject: EngineeringReference,
        val relationship: EngineeringDefinitionId,
        override val evidence: List<SourceProvenance>,
        override val description: String,
    ) : EngineeringCorrectionOption

    data class SelectExistingProvider(
        override val id: String,
        override val subject: EngineeringReference,
        val provider: EngineeringReference,
        override val evidence: List<SourceProvenance>,
        override val description: String,
    ) : EngineeringCorrectionOption

    data class BindPart(
        override val id: String,
        override val subject: EngineeringReference,
        val part: EngineeringDefinitionId,
        override val evidence: List<SourceProvenance>,
        override val description: String,
    ) : EngineeringCorrectionOption

    data class AddRequiredFunction(
        override val id: String,
        override val subject: EngineeringReference,
        val functionName: String,
        override val evidence: List<SourceProvenance>,
        override val description: String,
    ) : EngineeringCorrectionOption
}

/** Portable source reference. Paths are package/project relative and never absolute. */
data class EngineeringPortableProvenance(
    val file: String,
    val startLine: Int,
    val startUtf16Column: Int,
    val endLine: Int,
    val endUtf16Column: Int,
    val packageName: String? = null,
    val packageVersion: String? = null,
) {
    init {
        require(file.isNotBlank() && !file.contains('\\') && !file.startsWith('/') &&
            !Regex("^[A-Za-z]:").containsMatchIn(file) && file.split('/').none { it == ".." || it.isBlank() })
        require(startLine >= 0 && endLine >= startLine && startUtf16Column >= 0 && endUtf16Column >= 0)
    }

    companion object {
        fun from(source: SourceProvenance, packageName: String? = null, packageVersion: String? = null) =
            EngineeringPortableProvenance(source.file.replace('\\', '/'), source.startLine - 1,
                source.startColumn - 1, source.endLine - 1, source.endColumn - 1, packageName, packageVersion)
    }
}

data class EngineeringDerivationStep(val order: Int, val kind: String, val subject: String, val provenance: List<SourceProvenance>)

enum class EngineeringImpactStatus { CHANGED, UNCHANGED }

data class EngineeringImpactEntry(val id: String, val kind: String, val status: EngineeringImpactStatus, val subject: String)
