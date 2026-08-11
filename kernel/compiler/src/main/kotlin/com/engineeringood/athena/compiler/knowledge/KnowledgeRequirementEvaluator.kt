package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.KnowledgeDefinition
import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.semantics.core.EngineeringRequirement
import com.engineeringood.athena.semantics.core.EngineeringRequirementStatus
import com.engineeringood.athena.semantics.core.EngineeringSatisfactionEvidence
import com.engineeringood.athena.semantics.core.EngineeringValidationDiagnostic
import com.engineeringood.athena.semantics.core.EngineeringValidationDocument
import com.engineeringood.athena.semantics.core.EngineeringValidationState
import com.engineeringood.athena.semantics.core.EngineeringJudgement
import com.engineeringood.athena.semantics.core.EngineeringJudgementStatus
import com.engineeringood.athena.semantics.core.EngineeringCorrectionOption

data class EngineeringCapabilityProvision(
    val provider: EngineeringReference,
    val capability: EngineeringDefinitionId,
    val provenance: SourceProvenance,
)

/** Bounded evaluator. Provider list is authored input; evaluator never chooses or creates one. */
class KnowledgeRequirementEvaluator {
    fun evaluate(
        knowledge: EngineeringKnowledgeDocument,
        requirements: List<EngineeringRequirement>,
        provisions: List<EngineeringCapabilityProvision>,
        invalidDiagnostics: List<EngineeringValidationDiagnostic> = emptyList(),
        candidateProviders: List<EngineeringReference> = emptyList(),
    ): EngineeringValidationDocument {
        val orderedRequirements = requirements.sortedBy { it.id }
        if (invalidDiagnostics.isNotEmpty()) {
            return EngineeringValidationDocument(
                state = EngineeringValidationState.INVALID,
                requirements = orderedRequirements,
                satisfaction = emptyList(),
                diagnostics = invalidDiagnostics.sortedWith(compareBy({ it.code }, { it.subject })),
                provenance = invalidDiagnostics.flatMap { it.provenance },
            )
        }
        val knownCapabilities = knowledge.definitions.filterIsInstance<KnowledgeDefinition.Capability>().map { it.id }.toSet()
        val evidence = orderedRequirements.map { requirement ->
            val provider = provisions.firstOrNull { it.capability == requirement.capability }
            when {
                requirement.capability !in knownCapabilities -> EngineeringSatisfactionEvidence(
                    requirement.id, null, requirement.capability, EngineeringRequirementStatus.UNRESOLVED, listOf(requirement.provenance),
                )
                provider == null -> EngineeringSatisfactionEvidence(
                    requirement.id, null, requirement.capability, EngineeringRequirementStatus.UNSATISFIED, listOf(requirement.provenance),
                )
                else -> EngineeringSatisfactionEvidence(
                    requirement.id, provider.provider, requirement.capability, EngineeringRequirementStatus.SATISFIED,
                    listOf(requirement.provenance, provider.provenance),
                )
            }
        }
        val unresolved = evidence.filter { it.status == EngineeringRequirementStatus.UNRESOLVED }
        if (unresolved.isNotEmpty()) {
            return EngineeringValidationDocument(
                state = EngineeringValidationState.INVALID,
                requirements = orderedRequirements,
                satisfaction = emptyList(),
                diagnostics = unresolved.map { item ->
                    EngineeringValidationDiagnostic(
                        code = "validation.requirement.unresolved",
                        subject = item.requirementId,
                        message = "Requirement `${item.requirementId}` references unknown capability `${item.capability}`.",
                        provenance = item.provenance,
                    )
                },
                provenance = unresolved.flatMap { it.provenance },
            )
        }
        val state = when {
            evidence.any { it.status == EngineeringRequirementStatus.UNSATISFIED } -> EngineeringValidationState.INCOMPLETE
            else -> EngineeringValidationState.READY
        }
        val diagnostics = evidence.filter { it.status != EngineeringRequirementStatus.SATISFIED }.map { item ->
            EngineeringValidationDiagnostic(
                code = "validation.requirement.${item.status.name.lowercase()}",
                subject = item.requirementId,
                message = "Requirement `${item.requirementId}` is ${item.status.name.lowercase()} for capability `${item.capability}`.",
                provenance = item.provenance,
            )
        }
        val failed = evidence.filter { it.status != EngineeringRequirementStatus.SATISFIED }
        val judgements = failed.map { item ->
            val requirement = orderedRequirements.first { it.id == item.requirementId }
            EngineeringJudgement(
                id = item.requirementId,
                status = if (item.status == EngineeringRequirementStatus.UNRESOLVED) EngineeringJudgementStatus.UNRESOLVED else EngineeringJudgementStatus.UNSATISFIED,
                subject = requirement.subject,
                capability = item.capability,
                problem = if (item.status == EngineeringRequirementStatus.UNRESOLVED) "Capability definition is unavailable." else "No authored provider satisfies this capability.",
                correctionDirection = "Add or bind an existing provider, then re-evaluate.",
                provenance = item.provenance,
            )
        }
        val corrections = failed.flatMap { item ->
            val requirement = orderedRequirements.first { it.id == item.requirementId }
            candidateProviders.sortedBy { it.authoredPath.joinToString(".") }.mapIndexed { index, provider ->
                EngineeringCorrectionOption.SelectExistingProvider(
                    id = "${item.requirementId}.provider.$index", subject = requirement.subject, provider = provider,
                    evidence = item.provenance, description = "Use existing provider ${provider.authoredPath.joinToString(".")}.",
                )
            }
        }
        return EngineeringValidationDocument(state, orderedRequirements, evidence, diagnostics, evidence.flatMap { it.provenance }, judgements, corrections)
    }
}
