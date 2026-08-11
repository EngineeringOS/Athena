package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.semantics.core.EngineeringImpactEntry
import com.engineeringood.athena.semantics.core.EngineeringImpactStatus
import com.engineeringood.athena.semantics.core.EngineeringValidationDocument

/** Compiler-owned deterministic comparison of published validation evidence. */
object EngineeringImpactCalculator {
    fun compare(before: EngineeringValidationDocument, after: EngineeringValidationDocument): List<EngineeringImpactEntry> {
        val beforeById = entries(before)
        val afterById = entries(after)
        return (beforeById.keys + afterById.keys).distinct().sorted().map { id ->
            val current = afterById[id] ?: beforeById.getValue(id)
            EngineeringImpactEntry(
                id = id,
                kind = current.first,
                status = if (beforeById[id] == afterById[id]) EngineeringImpactStatus.UNCHANGED else EngineeringImpactStatus.CHANGED,
                subject = current.second,
            )
        }
    }

    private fun entries(document: EngineeringValidationDocument): Map<String, Pair<String, String>> = buildMap {
        document.satisfaction.forEach { put("requirement:${it.requirementId}", "requirement" to "${it.requirementId}|${it.status}") }
        document.judgements.forEach { put("judgement:${it.id}", "judgement" to it.subject.authoredPath.joinToString(".")) }
        document.correctionOptions.forEach { put("correction:${it.id}", "correction" to it.subject.authoredPath.joinToString(".")) }
        document.derivation.forEach { put("derivation:${it.order}", "derivation" to it.subject) }
    }
}
