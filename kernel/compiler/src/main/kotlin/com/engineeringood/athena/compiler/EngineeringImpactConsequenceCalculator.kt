package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.DerivedEngineeringInput
import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringSubjectContext
import com.engineeringood.athena.ir.DerivedEngineeringValue
import com.engineeringood.athena.ir.DerivedEngineeringValueKind
import com.engineeringood.athena.ir.EngineeringCapabilityFact
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import com.engineeringood.athena.ir.EngineeringKnowledgeState
import com.engineeringood.athena.ir.EngineeringConstraintEvaluation
import com.engineeringood.athena.ir.EngineeringConstraintEvaluations
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringImpactConsequence
import com.engineeringood.athena.ir.EngineeringImpactConsequences
import com.engineeringood.athena.ir.EngineeringImpactReasonKind
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Computes deterministic engineering impact consequences across two already-compiled canonical states.
 *
 * The calculator compares governed inputs, derived context, capability facts, and constraint
 * evaluations so downstream consequence stays semantic-first instead of collapsing into text diff.
 */
class EngineeringImpactConsequenceCalculator {
    /**
     * Calculates impact consequences between [before] and [after] compilation outputs.
     */
    fun calculate(
        before: CompilerCompilationSuccess,
        after: CompilerCompilationSuccess,
    ): EngineeringImpactConsequences {
        return calculate(
            before = EngineeringKnowledgeState(
                derivedContext = before.derivedContext,
                capabilityFacts = before.capabilityFacts,
                constraintEvaluations = before.constraintEvaluations,
            ),
            after = EngineeringKnowledgeState(
                derivedContext = after.derivedContext,
                capabilityFacts = after.capabilityFacts,
                constraintEvaluations = after.constraintEvaluations,
            ),
        )
    }

    /**
     * Calculates impact consequences between two neutral engineering-knowledge snapshots.
     */
    fun calculate(
        before: EngineeringKnowledgeState,
        after: EngineeringKnowledgeState,
    ): EngineeringImpactConsequences {
        val impactBuilders = linkedMapOf<String, ImpactBuilder>()
        val changedInputKinds = changedInputKindsBySubject(before, after)
        val changedDerivedKinds = changedDerivedValueKindsBySubject(before, after)
        val changedCapabilityKinds = changedCapabilityFactKindsBySubject(before, after)
        val changedConstraintRules = changedConstraintRuleKindsBySubject(before, after)

        changedInputKinds.forEach { (subjectIdentity, inputKinds) ->
            val builder = impactBuilders.builderFor(subjectIdentity)
            builder.triggerSubjectIdentities += subjectIdentity
            builder.reasonKinds += EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED
            builder.affectedInputKinds += inputKinds
        }

        changedDerivedKinds.forEach { (subjectIdentity, valueKinds) ->
            val builder = impactBuilders.builderFor(subjectIdentity)
            builder.triggerSubjectIdentities += subjectIdentity
            builder.reasonKinds += EngineeringImpactReasonKind.DERIVED_CONTEXT_CHANGED
            builder.affectedDerivedValueKinds += valueKinds
        }

        changedCapabilityKinds.forEach { (subjectIdentity, factKinds) ->
            val builder = impactBuilders.builderFor(subjectIdentity)
            builder.triggerSubjectIdentities += subjectIdentity
            builder.reasonKinds += EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED
            builder.affectedCapabilityFactKinds += factKinds
        }

        changedConstraintRules.forEach { (subjectIdentity, ruleKinds) ->
            val affectedSubjectIdentities = affectedSubjectsForChangedRules(
                before = before.constraintEvaluations,
                after = after.constraintEvaluations,
                subjectIdentity = subjectIdentity,
                ruleKinds = ruleKinds,
            ).ifEmpty { listOf(subjectIdentity) }
            affectedSubjectIdentities.forEach { affectedSubjectIdentity ->
                val builder = impactBuilders.builderFor(affectedSubjectIdentity)
                builder.triggerSubjectIdentities += subjectIdentity
                builder.reasonKinds += EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED
                builder.affectedConstraintRuleKinds += ruleKinds
            }
        }

        return EngineeringImpactConsequences.canonical(
            impactBuilders.values.map(ImpactBuilder::build),
        )
    }
}

private data class ImpactBuilder(
    val affectedSubjectIdentity: StableSemanticIdentity,
    val triggerSubjectIdentities: MutableSet<StableSemanticIdentity> = linkedSetOf(),
    val reasonKinds: MutableSet<EngineeringImpactReasonKind> = linkedSetOf(),
    val affectedInputKinds: MutableSet<DerivedEngineeringInputKind> = linkedSetOf(),
    val affectedDerivedValueKinds: MutableSet<DerivedEngineeringValueKind> = linkedSetOf(),
    val affectedCapabilityFactKinds: MutableSet<EngineeringCapabilityFactKind> = linkedSetOf(),
    val affectedConstraintRuleKinds: MutableSet<EngineeringConstraintRuleKind> = linkedSetOf(),
) {
    fun build(): EngineeringImpactConsequence {
        return EngineeringImpactConsequence(
            affectedSubjectIdentity = affectedSubjectIdentity,
            triggerSubjectIdentities = triggerSubjectIdentities.toList(),
            reasonKinds = reasonKinds.toList(),
            affectedInputKinds = affectedInputKinds.toList(),
            affectedDerivedValueKinds = affectedDerivedValueKinds.toList(),
            affectedCapabilityFactKinds = affectedCapabilityFactKinds.toList(),
            affectedConstraintRuleKinds = affectedConstraintRuleKinds.toList(),
        )
    }
}

private fun MutableMap<String, ImpactBuilder>.builderFor(
    subjectIdentity: StableSemanticIdentity,
): ImpactBuilder {
    return getOrPut(subjectIdentity.value) {
        ImpactBuilder(affectedSubjectIdentity = subjectIdentity)
    }
}

private fun changedInputKindsBySubject(
    before: EngineeringKnowledgeState,
    after: EngineeringKnowledgeState,
): Map<StableSemanticIdentity, Set<DerivedEngineeringInputKind>> {
    val beforeSubjects = before.derivedContext.subjects.associateBy(DerivedEngineeringSubjectContext::subjectIdentity)
    val afterSubjects = after.derivedContext.subjects.associateBy(DerivedEngineeringSubjectContext::subjectIdentity)
    return changedKindsBySubject(
        beforeSubjects = beforeSubjects,
        afterSubjects = afterSubjects,
        selector = DerivedEngineeringSubjectContext::inputs,
        keySelector = DerivedEngineeringInput::kind,
    )
}

private fun changedDerivedValueKindsBySubject(
    before: EngineeringKnowledgeState,
    after: EngineeringKnowledgeState,
): Map<StableSemanticIdentity, Set<DerivedEngineeringValueKind>> {
    val beforeSubjects = before.derivedContext.subjects.associateBy(DerivedEngineeringSubjectContext::subjectIdentity)
    val afterSubjects = after.derivedContext.subjects.associateBy(DerivedEngineeringSubjectContext::subjectIdentity)
    return changedKindsBySubject(
        beforeSubjects = beforeSubjects,
        afterSubjects = afterSubjects,
        selector = DerivedEngineeringSubjectContext::derivedValues,
        keySelector = DerivedEngineeringValue::kind,
    )
}

private fun changedCapabilityFactKindsBySubject(
    before: EngineeringKnowledgeState,
    after: EngineeringKnowledgeState,
): Map<StableSemanticIdentity, Set<EngineeringCapabilityFactKind>> {
    val beforeSubjects = before.capabilityFacts.subjects.associateBy { subject -> subject.subjectIdentity }
    val afterSubjects = after.capabilityFacts.subjects.associateBy { subject -> subject.subjectIdentity }
    return changedKindsBySubject(
        beforeSubjects = beforeSubjects,
        afterSubjects = afterSubjects,
        selector = { subject -> subject.facts },
        keySelector = EngineeringCapabilityFact::kind,
    )
}

private fun changedConstraintRuleKindsBySubject(
    before: EngineeringKnowledgeState,
    after: EngineeringKnowledgeState,
): Map<StableSemanticIdentity, Set<EngineeringConstraintRuleKind>> {
    val beforeSubjects = before.constraintEvaluations.subjects.associateBy { subject -> subject.subjectIdentity }
    val afterSubjects = after.constraintEvaluations.subjects.associateBy { subject -> subject.subjectIdentity }
    return changedKindsBySubject(
        beforeSubjects = beforeSubjects,
        afterSubjects = afterSubjects,
        selector = { subject -> subject.evaluations },
        keySelector = EngineeringConstraintEvaluation::ruleKind,
    )
}

private fun <Subject, Item, Kind> changedKindsBySubject(
    beforeSubjects: Map<StableSemanticIdentity, Subject>,
    afterSubjects: Map<StableSemanticIdentity, Subject>,
    selector: (Subject) -> List<Item>,
    keySelector: (Item) -> Kind,
): Map<StableSemanticIdentity, Set<Kind>> {
    return (beforeSubjects.keys + afterSubjects.keys)
        .sortedBy(StableSemanticIdentity::value)
        .mapNotNull { subjectIdentity ->
            val beforeItems = beforeSubjects[subjectIdentity].orEmptyMap(selector, keySelector)
            val afterItems = afterSubjects[subjectIdentity].orEmptyMap(selector, keySelector)
            val changedKinds = (beforeItems.keys + afterItems.keys)
                .filter { kind -> beforeItems[kind] != afterItems[kind] }
                .toSet()
            if (changedKinds.isEmpty()) {
                null
            } else {
                subjectIdentity to changedKinds
            }
        }
        .toMap(linkedMapOf())
}

private fun <Subject, Item, Kind> Subject?.orEmptyMap(
    selector: (Subject) -> List<Item>,
    keySelector: (Item) -> Kind,
): Map<Kind, Item> {
    return this?.let(selector)?.associateBy(keySelector).orEmpty()
}

private fun affectedSubjectsForChangedRules(
    before: EngineeringConstraintEvaluations,
    after: EngineeringConstraintEvaluations,
    subjectIdentity: StableSemanticIdentity,
    ruleKinds: Set<EngineeringConstraintRuleKind>,
): List<StableSemanticIdentity> {
    val beforeSubject = before.subjects.firstOrNull { subject -> subject.subjectIdentity == subjectIdentity }
    val afterSubject = after.subjects.firstOrNull { subject -> subject.subjectIdentity == subjectIdentity }
    val beforeByRule = beforeSubject?.evaluations?.associateBy(EngineeringConstraintEvaluation::ruleKind).orEmpty()
    val afterByRule = afterSubject?.evaluations?.associateBy(EngineeringConstraintEvaluation::ruleKind).orEmpty()
    return ruleKinds
        .flatMap { ruleKind ->
            beforeByRule[ruleKind]?.affectedSubjectIdentities.orEmpty() +
                afterByRule[ruleKind]?.affectedSubjectIdentities.orEmpty()
        }
        .distinct()
        .sortedBy(StableSemanticIdentity::value)
}
