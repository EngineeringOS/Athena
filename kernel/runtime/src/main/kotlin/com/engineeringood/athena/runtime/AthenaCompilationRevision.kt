package com.engineeringood.athena.runtime

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.semantics.core.EngineeringValidationDocument
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class AthenaCompilationRevision<EngineeringSnapshot : Any>(
    val revisionId: String,
    val engineering: EngineeringSnapshot,
    val engineeringDigest: String,
    val knowledge: EngineeringKnowledgeDocument,
    val knowledgeDigest: String,
    val validation: EngineeringValidationDocument,
    val validationDigest: String,
) {
    init {
        require(revisionId.isNotBlank())
        listOf(engineeringDigest, knowledgeDigest, validationDigest).forEach {
            require(it.matches(Regex("[0-9a-f]{64}"))) { "Compilation revision digest must be lowercase SHA-256 hex." }
        }
    }
}

/** Atomic publication boundary. Readers always receive one complete revision. */
class AthenaCompilationRevisionPublisher<EngineeringSnapshot : Any> {
    private val current = AtomicReference<AthenaCompilationRevision<EngineeringSnapshot>?>(null)
    private val cache = ConcurrentHashMap<String, AthenaCompilationRevision<EngineeringSnapshot>>()

    fun publish(revision: AthenaCompilationRevision<EngineeringSnapshot>): AthenaCompilationRevision<EngineeringSnapshot> {
        cache[revision.revisionId] = revision
        current.set(revision)
        return revision
    }

    fun current(): AthenaCompilationRevision<EngineeringSnapshot>? = current.get()

    fun find(revisionId: String): AthenaCompilationRevision<EngineeringSnapshot>? = cache[revisionId]
}

data class AthenaValidationSlice(
    val revisionId: String,
    val validationDigest: String,
    val requirements: List<String>,
    val judgementIds: List<String>,
    val correctionIds: List<String>,
)

object AthenaCompilationQueries {
    fun validationSlice(revision: AthenaCompilationRevision<*>, subject: String): AthenaValidationSlice {
        val matching = revision.validation.requirements.filter { it.subject.authoredPath.joinToString(".") == subject }.map { it.id }
        val judgements = revision.validation.judgements.filter { it.subject.authoredPath.joinToString(".") == subject }.map { it.id }
        val corrections = revision.validation.correctionOptions.filter { it.subject.authoredPath.joinToString(".") == subject }.map { it.id }
        return AthenaValidationSlice(revision.revisionId, revision.validationDigest, matching, judgements, corrections)
    }
}
