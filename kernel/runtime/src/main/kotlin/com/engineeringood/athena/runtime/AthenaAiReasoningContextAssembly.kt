package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.DerivedEngineeringInput
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.DerivedEngineeringSubjectContext
import com.engineeringood.athena.ir.DerivedEngineeringValue
import com.engineeringood.athena.ir.EngineeringCapabilityFact
import com.engineeringood.athena.ir.EngineeringCapabilitySubjectFacts
import com.engineeringood.athena.ir.EngineeringConstraintEvaluation
import com.engineeringood.athena.ir.EngineeringConstraintSubjectEvaluations
import com.engineeringood.athena.ir.EngineeringImpactConsequence
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.scm.SemanticReviewSummary

/**
 * Runtime-owned request for deterministic AI reasoning context assembly.
 */
data class AthenaAiReasoningContextRequest(
    val requestCategory: AthenaAiReasoningRequestCategory,
    val subjectSemanticIds: List<String> = emptyList(),
    val reviewSummary: SemanticReviewSummary? = null,
)

/**
 * Internal deterministic assembler for the first M10 reasoning-context package.
 *
 * The assembler stays runtime-owned and consumes existing governed semantic outputs only.
 */
internal class AthenaAiReasoningContextAssembler {
    fun assemble(
        context: AthenaExecutionContext,
        request: AthenaAiReasoningContextRequest,
    ): AthenaAiReasoningContext {
        val normalizedRequestedSubjects = request.subjectSemanticIds.distinct().sorted()
        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationSuccess -> assembleFromCompilation(
                projectName = context.project.name,
                compilation = compilation,
                request = request.copy(subjectSemanticIds = normalizedRequestedSubjects),
            )

            is CompilerCompilationParseFailure -> AthenaAiReasoningContext(
                projectName = context.project.name,
                requestCategory = request.requestCategory,
                subjectSemanticIds = normalizedRequestedSubjects,
                evidence = normalizedRequestedSubjects.map { subjectSemanticId ->
                    AthenaAiReasoningEvidenceRef(
                        kind = AthenaAiReasoningEvidenceKind.SEMANTIC_IDENTITY,
                        referenceId = subjectSemanticId,
                        summary = "canonical subject $subjectSemanticId",
                    )
                },
            )
        }
    }

    private fun assembleFromCompilation(
        projectName: String,
        compilation: CompilerCompilationSuccess,
        request: AthenaAiReasoningContextRequest,
    ): AthenaAiReasoningContext {
        val focusSubjects = request.subjectSemanticIds.toSet()
        val evidence = buildList {
            addAll(semanticIdentityEvidence(compilation, focusSubjects))
            addAll(derivedContextEvidence(compilation, focusSubjects))
            addAll(capabilityFactEvidence(compilation, focusSubjects))
            addAll(constraintEvaluationEvidence(compilation, focusSubjects))
            addAll(diagnosticEvidence(compilation, focusSubjects))
            when (request.requestCategory) {
                AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION -> Unit
                AthenaAiReasoningRequestCategory.IMPACT_SUMMARY,
                AthenaAiReasoningRequestCategory.NEXT_CHECK,
                -> addAll(reviewEvidence(request.reviewSummary, focusSubjects))
            }
        }.distinct().sortedWith(reasoningEvidenceComparator())

        val subjectSemanticIds = buildSet {
            addAll(request.subjectSemanticIds)
            evidence.forEach { evidenceRef ->
                evidenceRef.referenceId.substringAfter(':', missingDelimiterValue = "")
                when (evidenceRef.kind) {
                    AthenaAiReasoningEvidenceKind.SEMANTIC_IDENTITY -> add(evidenceRef.referenceId)
                    else -> Unit
                }
            }
            collectCompilationSubjects(compilation, focusSubjects).forEach(::add)
            collectReviewSubjects(request.reviewSummary, focusSubjects).forEach(::add)
        }.toList().sorted()

        return AthenaAiReasoningContext(
            projectName = projectName,
            requestCategory = request.requestCategory,
            subjectSemanticIds = subjectSemanticIds,
            evidence = evidence,
        )
    }

    private fun semanticIdentityEvidence(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        val subjectIds = collectCompilationSubjects(compilation, focusSubjects)
        return subjectIds.map { subjectSemanticId ->
            AthenaAiReasoningEvidenceRef(
                kind = AthenaAiReasoningEvidenceKind.SEMANTIC_IDENTITY,
                referenceId = subjectSemanticId,
                summary = "canonical subject $subjectSemanticId",
            )
        }
    }

    private fun derivedContextEvidence(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        return compilation.derivedContext.subjects
            .filter { subject -> matchesSubjectFocus(subject.subjectIdentity.value, focusSubjects) }
            .flatMap { subject ->
                subject.inputs.map { input -> input.toEvidenceRef() } +
                    subject.derivedValues.map { value -> value.toEvidenceRef() }
            }
    }

    private fun capabilityFactEvidence(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        return compilation.capabilityFacts.subjects
            .filter { subject -> matchesSubjectFocus(subject.subjectIdentity.value, focusSubjects) }
            .flatMap(EngineeringCapabilitySubjectFacts::toEvidenceRefs)
    }

    private fun constraintEvaluationEvidence(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        return compilation.constraintEvaluations.subjects
            .filter { subject -> matchesSubjectFocus(subject.subjectIdentity.value, focusSubjects) }
            .flatMap(EngineeringConstraintSubjectEvaluations::toEvidenceRefs)
    }

    private fun diagnosticEvidence(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        return (compilation.semanticResult.diagnostics + compilation.validationBreakdown.engineeringSufficiencyDiagnostics)
            .distinct()
            .filter { diagnostic ->
                diagnostic.subjectIdentity?.value?.let { subjectSemanticId ->
                    matchesSubjectFocus(subjectSemanticId, focusSubjects)
                } ?: focusSubjects.isEmpty()
            }
            .map(SemanticDiagnostic::toEvidenceRef)
    }

    private fun reviewEvidence(
        reviewSummary: SemanticReviewSummary?,
        focusSubjects: Set<String>,
    ): List<AthenaAiReasoningEvidenceRef> {
        if (reviewSummary == null) {
            return emptyList()
        }
        val impactEvidence = reviewSummary.engineeringImpactConsequences.consequences
            .filter { consequence ->
                matchesSubjectFocus(consequence.affectedSubjectIdentity.value, focusSubjects) ||
                    consequence.triggerSubjectIdentities.any { identity ->
                        matchesSubjectFocus(identity.value, focusSubjects)
                    } ||
                    focusSubjects.isEmpty()
            }
            .map(EngineeringImpactConsequence::toEvidenceRef)
        val reviewFactEvidence = reviewSummary.entries.flatMap { entry ->
            if (
                focusSubjects.isNotEmpty() &&
                entry.subjectIdentity?.value?.let { subjectSemanticId ->
                    !matchesSubjectFocus(subjectSemanticId, focusSubjects)
                } == true
            ) {
                emptyList()
            } else {
                entry.factReferences.map { factReference ->
                    AthenaAiReasoningEvidenceRef(
                        kind = AthenaAiReasoningEvidenceKind.REVIEW_ENTRY,
                        referenceId = factReference.identifier,
                        summary = "${entry.kind.name}: ${entry.message}",
                    )
                }
            }
        }
        return impactEvidence + reviewFactEvidence
    }

    private fun collectCompilationSubjects(
        compilation: CompilerCompilationSuccess,
        focusSubjects: Set<String>,
    ): List<String> {
        return buildSet {
            compilation.derivedContext.subjects
                .map(DerivedEngineeringSubjectContext::subjectIdentity)
                .map { identity -> identity.value }
                .filter { subjectSemanticId -> matchesSubjectFocus(subjectSemanticId, focusSubjects) }
                .forEach(::add)
            compilation.capabilityFacts.subjects
                .map(EngineeringCapabilitySubjectFacts::subjectIdentity)
                .map { identity -> identity.value }
                .filter { subjectSemanticId -> matchesSubjectFocus(subjectSemanticId, focusSubjects) }
                .forEach(::add)
            compilation.constraintEvaluations.subjects
                .map(EngineeringConstraintSubjectEvaluations::subjectIdentity)
                .map { identity -> identity.value }
                .filter { subjectSemanticId -> matchesSubjectFocus(subjectSemanticId, focusSubjects) }
                .forEach(::add)
            (compilation.semanticResult.diagnostics + compilation.validationBreakdown.engineeringSufficiencyDiagnostics)
                .mapNotNull { diagnostic -> diagnostic.subjectIdentity?.value }
                .filter { subjectSemanticId -> matchesSubjectFocus(subjectSemanticId, focusSubjects) }
                .forEach(::add)
        }.toList().sorted()
    }

    private fun collectReviewSubjects(
        reviewSummary: SemanticReviewSummary?,
        focusSubjects: Set<String>,
    ): List<String> {
        if (reviewSummary == null) {
            return emptyList()
        }
        return buildSet {
            reviewSummary.engineeringImpactConsequences.consequences.forEach { consequence ->
                val consequenceMatchesFocus = matchesSubjectFocus(consequence.affectedSubjectIdentity.value, focusSubjects) ||
                    consequence.triggerSubjectIdentities.any { identity ->
                        matchesSubjectFocus(identity.value, focusSubjects)
                    } ||
                    focusSubjects.isEmpty()
                if (consequenceMatchesFocus) {
                    add(consequence.affectedSubjectIdentity.value)
                    consequence.triggerSubjectIdentities
                        .map { identity -> identity.value }
                        .forEach(::add)
                }
            }
            reviewSummary.entries
                .mapNotNull { entry -> entry.subjectIdentity?.value }
                .filter { subjectSemanticId -> matchesSubjectFocus(subjectSemanticId, focusSubjects) || focusSubjects.isEmpty() }
                .forEach(::add)
        }.toList().sorted()
    }
}

private fun DerivedEngineeringInput.toEvidenceRef(): AthenaAiReasoningEvidenceRef {
    return AthenaAiReasoningEvidenceRef(
        kind = AthenaAiReasoningEvidenceKind.DERIVED_CONTEXT,
        referenceId = "derived-input:${trace.subjectIdentity.value}:${kind.name}",
        summary = "input ${kind.name}=${authoredValue.renderText()}",
    )
}

private fun DerivedEngineeringValue.toEvidenceRef(): AthenaAiReasoningEvidenceRef {
    return AthenaAiReasoningEvidenceRef(
        kind = AthenaAiReasoningEvidenceKind.DERIVED_CONTEXT,
        referenceId = "derived-value:${subjectIdentity.value}:${kind.name}",
        summary = "derived ${kind.name}=${quantity.renderText()}",
    )
}

private fun EngineeringCapabilitySubjectFacts.toEvidenceRefs(): List<AthenaAiReasoningEvidenceRef> {
    return facts.map { fact ->
        AthenaAiReasoningEvidenceRef(
            kind = AthenaAiReasoningEvidenceKind.CAPABILITY_FACT,
            referenceId = "capability-fact:${fact.subjectIdentity.value}:${fact.kind.name}",
            summary = "fact ${fact.kind.name} ${fact.comparison.name} ${fact.quantity.renderText()}",
        )
    }
}

private fun EngineeringConstraintSubjectEvaluations.toEvidenceRefs(): List<AthenaAiReasoningEvidenceRef> {
    return evaluations.map(EngineeringConstraintEvaluation::toEvidenceRef)
}

private fun EngineeringConstraintEvaluation.toEvidenceRef(): AthenaAiReasoningEvidenceRef {
    return AthenaAiReasoningEvidenceRef(
        kind = AthenaAiReasoningEvidenceKind.CONSTRAINT_EVALUATION,
        referenceId = "constraint:${subjectIdentity.value}:${ruleKind.name}",
        summary = "constraint ${ruleKind.name} ${status.name} required=${requiredQuantity.renderText()} actual=${actualQuantity.renderText()}",
    )
}

private fun SemanticDiagnostic.toEvidenceRef(): AthenaAiReasoningEvidenceRef {
    val subjectKey = subjectIdentity?.value ?: "global"
    return AthenaAiReasoningEvidenceRef(
        kind = AthenaAiReasoningEvidenceKind.DIAGNOSTIC,
        referenceId = "diagnostic:${ruleId.value}:${subjectKey}:${provenance.file}:${provenance.startLine}:${provenance.startColumn}",
        summary = message,
    )
}

private fun EngineeringImpactConsequence.toEvidenceRef(): AthenaAiReasoningEvidenceRef {
    return AthenaAiReasoningEvidenceRef(
        kind = AthenaAiReasoningEvidenceKind.IMPACT_CONSEQUENCE,
        referenceId = "impact:${affectedSubjectIdentity.value}:${reasonKinds.joinToString(separator = ",") { reason -> reason.name }}",
        summary = "impact ${affectedSubjectIdentity.value} reasons=${reasonKinds.joinToString(separator = ",") { reason -> reason.name }}",
    )
}

private fun EngineeringPropertyValue.renderText(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> text
    }
}

private fun DerivedEngineeringQuantity.renderText(): String {
    return when (this) {
        is DerivedEngineeringQuantity.Decimal -> canonicalText + unitSymbol.orEmpty()
    }
}

private fun matchesSubjectFocus(
    subjectSemanticId: String,
    focusSubjects: Set<String>,
): Boolean {
    return focusSubjects.isEmpty() || subjectSemanticId in focusSubjects
}

private fun reasoningEvidenceComparator(): Comparator<AthenaAiReasoningEvidenceRef> {
    return compareBy<AthenaAiReasoningEvidenceRef>(
        { evidence -> evidence.kind.ordinal },
        { evidence -> evidence.referenceId },
        { evidence -> evidence.summary },
    )
}
