package com.engineeringood.athena.representation

data class RepresentationBindingRequest(
    val canonicalSemanticId: RepresentationSubjectId,
    val projectionOccurrenceId: RepresentationProjectionOccurrenceId,
    val subjectKind: RepresentationSubjectKind,
    val semanticRole: RepresentationSemanticRole?,
    val projectionKind: RepresentationProjectionKind,
    val policy: RepresentationPolicy,
    val definition: RepresentationDefinition,
    val labelValues: Map<RepresentationLabelSlotId, LabelValue>,
    val terminalPorts: Map<PresentationTerminalId, SemanticPortId>,
    val priority: RepresentationPolicyPriority,
    val referenceBindings: List<RepresentationReferenceBinding> = emptyList(),
    val compositionIntentMembership: List<CompositionIntentMembershipId> = emptyList(),
)

data class RepresentationBindingResult(
    val occurrenceOrNull: RepresentationOccurrence?,
    val diagnostics: List<RepresentationDiagnostic>,
) {
    val occurrence: RepresentationOccurrence
        get() = requireNotNull(occurrenceOrNull) { "Representation binding did not produce an occurrence." }
}

class RepresentationBindingCompiler {
    fun bind(request: RepresentationBindingRequest): RepresentationBindingResult {
        val policyDiagnostics = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(request.definition.libraryId),
                policies = listOf(request.policy),
                definitions = listOf(request.definition),
                occurrences = emptyList(),
            ),
        ).diagnostics
        val requestDiagnostics = request.boundaryDiagnostics()
        val preflightDiagnostics = policyDiagnostics + requestDiagnostics
        if (preflightDiagnostics.isNotEmpty()) {
            return RepresentationBindingResult(
                occurrenceOrNull = null,
                diagnostics = preflightDiagnostics.sortedDiagnostics(),
            )
        }

        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId(
                "${request.canonicalSemanticId.value}@${request.projectionOccurrenceId.value}",
            ),
            canonicalSemanticId = request.canonicalSemanticId,
            projectionOccurrenceId = request.projectionOccurrenceId,
            occurrenceRole = request.policy.occurrenceRole,
            symbolId = request.policy.symbolId,
            variant = request.policy.variant,
            labelBindings = request.labelValues.map { (slotId, value) ->
                RepresentationLabelBinding(slotId, value)
            },
            terminalBindings = request.terminalPorts.map { (terminalId, semanticPortId) ->
                RepresentationTerminalBinding(terminalId, semanticPortId)
            },
            referenceBindings = request.referenceBindings,
            compositionIntentMembership = request.compositionIntentMembership,
        )
        val validation = RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(request.definition.libraryId),
                policies = listOf(request.policy),
                definitions = listOf(request.definition),
                occurrences = listOf(occurrence),
                compatibleTerminalBindings = request.terminalPorts.map { (terminalId, semanticPortId) ->
                    RepresentationCompatibleTerminalBinding(terminalId, semanticPortId)
                }.toSet(),
                compositionMemberships = request.compositionIntentMembership.toSet(),
            ),
        )
        return if (validation.diagnostics.isEmpty()) {
            RepresentationBindingResult(occurrence, emptyList())
        } else {
            RepresentationBindingResult(null, validation.diagnostics)
        }
    }

    private fun RepresentationBindingRequest.boundaryDiagnostics(): List<RepresentationDiagnostic> {
        val diagnostics = mutableListOf<RepresentationDiagnostic>()
        if (policy.projectionKind != projectionKind ||
            policy.subjectKind != subjectKind ||
            policy.semanticRole != semanticRole ||
            policy.priority.value != priority.value
        ) {
            diagnostics += RepresentationDiagnostic(
                code = RepresentationDiagnosticCode.POLICY_MISSING,
                message = "Representation binding request does not match policy `${policy.policyId.value}`.",
                subjectId = canonicalSemanticId,
            )
        }
        if (policy.symbolId != definition.symbolId) {
            diagnostics += RepresentationDiagnostic(
                code = RepresentationDiagnosticCode.SYMBOL_MISSING,
                message = "Policy symbol `${policy.symbolId.value}` does not match definition `${definition.symbolId.value}`.",
                subjectId = canonicalSemanticId,
            )
        }
        return diagnostics
    }
}

private fun List<RepresentationDiagnostic>.sortedDiagnostics(): List<RepresentationDiagnostic> {
    return sortedWith(
        compareBy<RepresentationDiagnostic>(
            { diagnostic -> diagnostic.code.wireValue },
            { diagnostic -> diagnostic.subjectId?.value.orEmpty() },
            { diagnostic -> diagnostic.message },
        ),
    )
}
