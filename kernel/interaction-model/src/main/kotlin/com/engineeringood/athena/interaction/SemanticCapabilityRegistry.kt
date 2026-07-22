package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity

data class InteractionRegistryInput(
    val sourceContextId: String,
    val sourceRevision: String? = null,
    val subjects: List<InteractionRegistrySubjectFact>,
    val occurrences: List<InteractionRegistryOccurrenceFact> = emptyList(),
) {
    init {
        require(sourceContextId.isNotBlank()) { "Interaction registry source context id must not be blank." }
    }
}

data class InteractionRegistrySubjectFact(
    val canonicalSubjectId: StableSemanticIdentity,
    val subjectKind: InteractionSubjectKind,
    val sourceRange: SourceRangeRef? = null,
    val diagnosticId: String? = null,
    val capabilities: List<SemanticCapability> = emptyList(),
    val presentationMetadata: Map<String, String> = emptyMap(),
    val standardMetadata: Map<String, String> = emptyMap(),
)

data class InteractionRegistryOccurrenceFact(
    val canonicalSubjectId: StableSemanticIdentity,
    val subjectKind: InteractionSubjectKind,
    val projectionViewId: String? = null,
    val sheetId: String? = null,
    val documentProjectionId: String? = null,
    val occurrenceId: String? = null,
    val sourceRevision: String? = null,
    val presentationMetadata: Map<String, String> = emptyMap(),
    val standardMetadata: Map<String, String> = emptyMap(),
    val adapterMetadata: Map<String, String> = emptyMap(),
)

data class SemanticCapabilityRegistry(
    val sourceContextId: String,
    val sourceRevision: String?,
    val subjects: List<InteractionSubject>,
) {
    private val subjectsByKey = subjects.associateBy(InteractionSubject::key)

    fun requireSubject(key: InteractionSubjectKey): InteractionSubject {
        return requireNotNull(subjectsByKey[key]) { "Interaction subject ${key.canonicalSubjectId} was not registered." }
    }

    fun diagnosticsForActiveRevision(activeSourceRevision: String?): List<InteractionDiagnostic> {
        if (sourceRevision == null || sourceRevision == activeSourceRevision) {
            return emptyList()
        }

        return listOf(
            InteractionDiagnostic(
                code = InteractionDiagnosticCode.REGISTRY_STALE,
                severity = InteractionDiagnosticSeverity.WARNING,
                message = "Interaction registry was built for source revision $sourceRevision but active revision is $activeSourceRevision.",
                retryable = true,
            ),
        )
    }

    fun discoverAuthoringCapabilities(
        subjectKey: InteractionSubjectKey,
        requestedBy: InteractionProvenance,
        intentKind: AuthoringIntentKind? = null,
    ): AuthoringCapabilityDiscoveryResult {
        val subject = runCatching { requireSubject(subjectKey) }.getOrElse {
            return AuthoringCapabilityDiscoveryResult(
                evidence = emptyList(),
                diagnostics = listOf(
                    InteractionDiagnostic(
                        code = InteractionDiagnosticCode.SUBJECT_UNRESOLVED,
                        severity = InteractionDiagnosticSeverity.ERROR,
                        message = "Interaction subject ${subjectKey.canonicalSubjectId} is not registered.",
                        subject = subjectKey,
                        retryable = false,
                    ),
                ),
            )
        }
        val evidence = mutableListOf<AuthoringCapabilityEvidence>()
        val diagnostics = mutableListOf<InteractionDiagnostic>()
        subject.capabilities
            .filter { capability -> capability.authoring != null }
            .filter { capability -> intentKind == null || capability.authoring?.intentKind == intentKind }
            .forEach { capability ->
                val authoring = requireNotNull(capability.authoring)
                val unavailableMessage = when {
                    !capability.enabled -> capability.disabledReason?.message
                        ?: "Authoring capability ${capability.capabilityId} is disabled."

                    requestedBy.originSurface !in authoring.allowedOrigins ->
                        "Authoring capability ${capability.capabilityId} is unavailable for the actor policy at ${requestedBy.originSurface.name.lowercase()}."

                    else -> authoring.requirements
                        .firstOrNull { requirement -> !requirement.satisfied }
                        ?.let { requirement ->
                            val requirementName = requirement.kind.name.lowercase().replace('_', ' ')
                            "Authoring capability ${capability.capabilityId} has an unavailable $requirementName requirement `${requirement.identifier}`: ${requirement.reason}"
                        }
                }
                if (unavailableMessage != null) {
                    diagnostics += InteractionDiagnostic(
                        code = InteractionDiagnosticCode.AUTHORING_CAPABILITY_UNAVAILABLE,
                        severity = InteractionDiagnosticSeverity.WARNING,
                        message = unavailableMessage,
                        subject = subjectKey,
                        retryable = false,
                    )
                } else {
                    evidence += AuthoringCapabilityEvidence(
                        capabilityId = capability.capabilityId,
                        intentKind = authoring.intentKind,
                        subject = subjectKey,
                        actorOrigin = requestedBy.originSurface,
                        satisfiedRequirements = authoring.requirements,
                    )
                }
            }

        return AuthoringCapabilityDiscoveryResult(
            evidence = evidence,
            diagnostics = diagnostics,
        )
    }

    companion object {
        fun build(input: InteractionRegistryInput): SemanticCapabilityRegistry {
            val occurrenceFactsBySubject = input.occurrences.groupBy { occurrence ->
                InteractionSubjectKey(
                    canonicalSubjectId = occurrence.canonicalSubjectId,
                    subjectKind = occurrence.subjectKind,
                    sourceContextId = input.sourceContextId,
                )
            }
            val subjects = input.subjects.map { fact ->
                val key = InteractionSubjectKey(
                    canonicalSubjectId = fact.canonicalSubjectId,
                    subjectKind = fact.subjectKind,
                    sourceContextId = input.sourceContextId,
                )
                val occurrenceFacts = occurrenceFactsBySubject[key].orEmpty()
                InteractionSubject(
                    key = key,
                    occurrences = occurrenceFacts.map { occurrence ->
                        InteractionOccurrenceKey(
                            subjectKey = key,
                            projectionViewId = occurrence.projectionViewId,
                            sheetId = occurrence.sheetId,
                            documentProjectionId = occurrence.documentProjectionId,
                            occurrenceId = occurrence.occurrenceId,
                            sourceRevision = occurrence.sourceRevision ?: input.sourceRevision,
                        )
                    },
                    sourceRange = fact.sourceRange,
                    diagnosticId = fact.diagnosticId,
                    capabilities = fact.capabilities,
                    presentationMetadata = fact.presentationMetadata + occurrenceFacts.mergedPresentationMetadata(),
                    standardMetadata = fact.standardMetadata + occurrenceFacts.mergedStandardMetadata(),
                    adapterMetadata = occurrenceFacts.mergedAdapterMetadata(),
                    provenance = InteractionProvenance(originSurface = InteractionOriginSurface.RUNTIME),
                )
            }

            return SemanticCapabilityRegistry(
                sourceContextId = input.sourceContextId,
                sourceRevision = input.sourceRevision,
                subjects = subjects,
            )
        }
    }
}

private fun List<InteractionRegistryOccurrenceFact>.mergedPresentationMetadata(): Map<String, String> {
    return flatMap { occurrence -> occurrence.presentationMetadata.entries }
        .associate { entry -> entry.key to entry.value }
}

private fun List<InteractionRegistryOccurrenceFact>.mergedStandardMetadata(): Map<String, String> {
    return flatMap { occurrence -> occurrence.standardMetadata.entries }
        .associate { entry -> entry.key to entry.value }
}

private fun List<InteractionRegistryOccurrenceFact>.mergedAdapterMetadata(): Map<String, String> {
    return flatMap { occurrence -> occurrence.adapterMetadata.entries }
        .associate { entry -> entry.key to entry.value }
}
