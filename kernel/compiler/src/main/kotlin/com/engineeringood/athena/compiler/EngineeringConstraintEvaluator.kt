package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactPackage
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePayloadEntry
import com.engineeringood.athena.ir.DerivedEngineeringContext
import com.engineeringood.athena.ir.DerivedEngineeringInput
import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.EngineeringCapabilityComparison
import com.engineeringood.athena.ir.EngineeringCapabilityFact
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import com.engineeringood.athena.ir.EngineeringCapabilityFacts
import com.engineeringood.athena.ir.EngineeringConstraintEvaluation
import com.engineeringood.athena.ir.EngineeringConstraintEvaluationTrace
import com.engineeringood.athena.ir.EngineeringConstraintEvaluations
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringConstraintStatus
import com.engineeringood.athena.ir.EngineeringConstraintSubjectEvaluations
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import java.math.BigDecimal
import java.nio.file.Files
import java.util.Properties

/**
 * Evaluates the first fixed electrical knowledge-pack rule slice over capability facts and governed authored inputs.
 */
class EngineeringConstraintEvaluator {
    /**
     * Evaluates engineering sufficiency and returns both typed results and renderer-independent diagnostics.
     */
    fun evaluate(
        derivedContext: DerivedEngineeringContext,
        capabilityFacts: EngineeringCapabilityFacts,
        knowledgeContext: AthenaCompilationKnowledgeContext,
    ): ConstraintEvaluationOutcome {
        val semantics = resolveConstraintSemantics(knowledgeContext)
            ?: return ConstraintEvaluationOutcome(
                evaluations = EngineeringConstraintEvaluations.canonical(emptyList()),
                diagnostics = emptyList(),
            )
        val contextSubjects = derivedContext.subjects.associateBy { subject -> subject.subjectIdentity }
        val evaluations = capabilityFacts.subjects.mapNotNull { subjectFacts ->
            val subjectContext = contextSubjects[subjectFacts.subjectIdentity] ?: return@mapNotNull null
            val subjectEvaluations = semantics.rules.mapNotNull { rule ->
                val requiredFact = subjectFacts.facts.firstOrNull { fact -> fact.kind == rule.requiredFactKind } ?: return@mapNotNull null
                val actualInput = subjectContext.inputs.firstOrNull { input -> input.kind == rule.actualInputKind } ?: return@mapNotNull null
                evaluateRule(subjectFacts.subjectIdentity, requiredFact, actualInput, rule, semantics.metadata)
            }
            if (subjectEvaluations.isEmpty()) {
                null
            } else {
                EngineeringConstraintSubjectEvaluations(
                    subjectIdentity = subjectFacts.subjectIdentity,
                    evaluations = subjectEvaluations,
                )
            }
        }
        val canonicalEvaluations = EngineeringConstraintEvaluations.canonical(evaluations)
        val diagnostics = canonicalEvaluations.subjects.flatMap { subject ->
            subject.evaluations.mapNotNull { evaluation -> evaluation.toDiagnostic(subjectProvenance = contextSubjects.getValue(subject.subjectIdentity).provenanceFor(evaluation.trace.actualInputKind)) }
        }
        return ConstraintEvaluationOutcome(
            evaluations = canonicalEvaluations,
            diagnostics = diagnostics,
        )
    }

    private fun evaluateRule(
        subjectIdentity: com.engineeringood.athena.ir.StableSemanticIdentity,
        requiredFact: EngineeringCapabilityFact,
        actualInput: DerivedEngineeringInput,
        rule: ElectricalConstraintRule,
        metadata: ElectricalConstraintMetadata,
    ): EngineeringConstraintEvaluation? {
        if (requiredFact.comparison != EngineeringCapabilityComparison.MINIMUM_INCLUSIVE) {
            return null
        }
        val requiredQuantity = requiredFact.quantity as? DerivedEngineeringQuantity.Decimal ?: return null
        val actualQuantity = actualInput.authoredValue.toDecimalQuantity(defaultUnitSymbol = "A") ?: return null
        val requiredNumeric = requiredQuantity.canonicalText.toBigDecimalOrNull() ?: return null
        val actualNumeric = actualQuantity.canonicalText.toBigDecimalOrNull() ?: return null
        val status = if (actualNumeric >= requiredNumeric) {
            EngineeringConstraintStatus.ACCEPTED
        } else {
            rule.failureStatus
        }
        val explanation = if (status == EngineeringConstraintStatus.ACCEPTED) {
            "${rule.displaySubject} current ${actualQuantity.canonicalText}${actualQuantity.unitSymbol.orEmpty()} satisfies required ${requiredQuantity.canonicalText}${requiredQuantity.unitSymbol.orEmpty()} for ${subjectIdentity.value}."
        } else {
            "${rule.displaySubject} current ${actualQuantity.canonicalText}${actualQuantity.unitSymbol.orEmpty()} is below required ${requiredQuantity.canonicalText}${requiredQuantity.unitSymbol.orEmpty()} for ${subjectIdentity.value}."
        }
        return EngineeringConstraintEvaluation(
            ruleKind = rule.ruleKind,
            status = status,
            subjectIdentity = subjectIdentity,
            affectedSubjectIdentities = listOf(subjectIdentity),
            explanation = explanation,
            requiredQuantity = requiredQuantity,
            actualQuantity = actualQuantity,
            trace = EngineeringConstraintEvaluationTrace(
                knowledgeArtifactId = metadata.artifactId,
                knowledgeArtifactVersion = metadata.artifactVersion,
                knowledgeEntryId = metadata.entryId,
                requiredFactKind = rule.requiredFactKind,
                actualInputKind = rule.actualInputKind,
            ),
        )
    }

    private fun EngineeringConstraintEvaluation.toDiagnostic(subjectProvenance: com.engineeringood.athena.ir.SourceProvenance?): SemanticDiagnostic? {
        val provenance = subjectProvenance ?: return null
        val severity = when (status) {
            EngineeringConstraintStatus.ACCEPTED -> return null
            EngineeringConstraintStatus.WARNING -> SemanticDiagnosticSeverity.WARNING
            EngineeringConstraintStatus.ERROR -> SemanticDiagnosticSeverity.ERROR
        }
        return SemanticDiagnostic(
            severity = severity,
            ruleId = SemanticRuleId("knowledge.${ruleKind.name.lowercase()}"),
            category = SemanticDiagnosticCategory.KNOWLEDGE,
            subjectIdentity = subjectIdentity,
            provenance = provenance,
            message = explanation,
        )
    }

    private fun resolveConstraintSemantics(knowledgeContext: AthenaCompilationKnowledgeContext): ElectricalConstraintSemantics? {
        val activePack = knowledgeContext.candidates.firstOrNull { candidate ->
            candidate.artifactPackage.manifest.artifactKind == AthenaKnowledgeArtifactKind.KNOWLEDGE_PACK &&
                candidate.artifactPackage.manifest.artifactId == ELECTRICAL_BASIC_KNOWLEDGE_PACK_ID &&
                knowledgeContext.activeArtifacts.any { artifact ->
                    artifact.artifactId == candidate.artifactPackage.manifest.artifactId &&
                        artifact.artifactVersion == candidate.artifactPackage.manifest.artifactVersion &&
                        artifact.artifactKind == AthenaKnowledgeArtifactKind.KNOWLEDGE_PACK
                }
        }?.artifactPackage ?: return null
        return loadConstraintSemantics(activePack)
    }

    private fun loadConstraintSemantics(artifactPackage: AthenaKnowledgeArtifactPackage): ElectricalConstraintSemantics? {
        val payloadEntry = artifactPackage.payloadEntries.singleOrNull { entry ->
            entry.entryKind == CONSTRAINT_SLICE_ENTRY_KIND
        } ?: return null
        val properties = loadProperties(payloadEntry) ?: return null
        val ruleIds = properties.stringPropertyNames()
            .mapNotNull { key -> CONSTRAINT_PROPERTY_KEY.matchEntire(key)?.groupValues?.get(1) }
            .toSortedSet()
        if (ruleIds.isEmpty()) {
            return null
        }
        val rules = ruleIds.mapNotNull { ruleId ->
            loadRule(ruleId, properties)
        }
        if (rules.isEmpty()) {
            return null
        }
        return ElectricalConstraintSemantics(
            metadata = ElectricalConstraintMetadata(
                artifactId = artifactPackage.manifest.artifactId,
                artifactVersion = artifactPackage.manifest.artifactVersion,
                entryId = payloadEntry.entryId,
            ),
            rules = rules,
        )
    }

    private fun loadRule(ruleId: String, properties: Properties): ElectricalConstraintRule? {
        val ruleKind = properties.getProperty("rule.$ruleId.kind")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(EngineeringConstraintRuleKind::valueOf)
            ?: return null
        val requiredFactKind = properties.getProperty("rule.$ruleId.requiredFact")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(EngineeringCapabilityFactKind::valueOf)
            ?: return null
        val actualInputKind = properties.getProperty("rule.$ruleId.actualInput")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(DerivedEngineeringInputKind::valueOf)
            ?: return null
        val failureStatus = properties.getProperty("rule.$ruleId.failureStatus")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(EngineeringConstraintStatus::valueOf)
            ?: return null
        val displaySubject = properties.getProperty("rule.$ruleId.displaySubject")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        return ElectricalConstraintRule(
            ruleKind = ruleKind,
            requiredFactKind = requiredFactKind,
            actualInputKind = actualInputKind,
            failureStatus = failureStatus,
            displaySubject = displaySubject,
        )
    }

    private fun loadProperties(payloadEntry: AthenaKnowledgePayloadEntry): Properties? {
        return runCatching {
            Properties().apply {
                Files.newInputStream(payloadEntry.resolvedPath).use(::load)
            }
        }.getOrNull()
    }
}

/**
 * Stable result of evaluating the first engineering sufficiency rule slice.
 */
data class ConstraintEvaluationOutcome(
    val evaluations: EngineeringConstraintEvaluations,
    val diagnostics: List<SemanticDiagnostic>,
)

private data class ElectricalConstraintSemantics(
    val metadata: ElectricalConstraintMetadata,
    val rules: List<ElectricalConstraintRule>,
)

private data class ElectricalConstraintMetadata(
    val artifactId: String,
    val artifactVersion: String,
    val entryId: String,
)

private data class ElectricalConstraintRule(
    val ruleKind: EngineeringConstraintRuleKind,
    val requiredFactKind: EngineeringCapabilityFactKind,
    val actualInputKind: DerivedEngineeringInputKind,
    val failureStatus: EngineeringConstraintStatus,
    val displaySubject: String,
)

private const val CONSTRAINT_SLICE_ENTRY_KIND = "constraint-slice"
private val CONSTRAINT_PROPERTY_KEY = Regex("""rule\.([^.]+)\.(kind|requiredFact|actualInput|failureStatus|displaySubject)""")

private fun DerivedEngineeringInput.authoredValueToDecimalQuantity(): DerivedEngineeringQuantity.Decimal? {
    return authoredValue.toDecimalQuantity(defaultUnitSymbol = "A")
}

private fun com.engineeringood.athena.ir.EngineeringPropertyValue.toDecimalQuantity(
    defaultUnitSymbol: String,
): DerivedEngineeringQuantity.Decimal? {
    val rawText = when (this) {
        is com.engineeringood.athena.ir.EngineeringPropertyValue.Symbol -> text
        is com.engineeringood.athena.ir.EngineeringPropertyValue.Text -> text
    }.trim()
    val match = Regex("""^([0-9]+(?:\.[0-9]+)?)([A-Za-z%]+)?$""").matchEntire(rawText.replace(" ", "")) ?: return null
    val numeric = match.groupValues[1].toBigDecimalOrNull() ?: return null
    val unit = match.groupValues[2].takeIf { it.isNotEmpty() } ?: defaultUnitSymbol
    return DerivedEngineeringQuantity.Decimal(
        canonicalText = numeric.stripTrailingZeros().toPlainString(),
        unitSymbol = unit,
    )
}

private fun com.engineeringood.athena.ir.DerivedEngineeringSubjectContext.provenanceFor(
    inputKind: DerivedEngineeringInputKind,
): com.engineeringood.athena.ir.SourceProvenance? {
    return inputs.firstOrNull { input -> input.kind == inputKind }?.trace?.provenance
}
