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
    val projectPorts: List<RepresentationProjectPortFact> = emptyList(),
    val priority: RepresentationPolicyPriority,
    val referenceBindings: List<RepresentationReferenceBinding> = emptyList(),
    val compositionIntentMembership: List<CompositionIntentMembershipId> = emptyList(),
    val functionSemanticId: RepresentationSubjectId? = null,
)

data class RepresentationProjectPortFact(
    val semanticPortId: SemanticPortId,
    val role: RepresentationAnchorRole,
    val direction: RepresentationDirectionPredicate,
    val signal: RepresentationSignalPredicate,
    val terminal: PhysicalTerminalId?,
    val provenance: RepresentationProvenance,
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
                listOfNotNull(
                    request.canonicalSemanticId.value,
                    request.functionSemanticId?.value,
                    request.projectionOccurrenceId.value,
                ).joinToString("@"),
            ),
            canonicalSemanticId = request.canonicalSemanticId,
            functionSemanticId = request.functionSemanticId,
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
                }.filter { compatibleBinding ->
                    request.isCompatibleTerminalBinding(compatibleBinding)
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
        if ((subjectKind == RepresentationSubjectKind.FUNCTION) != (functionSemanticId != null)) {
            diagnostics += RepresentationDiagnostic(
                code = RepresentationDiagnosticCode.SOURCE_AUTHORITY_VIOLATION,
                message = "Function representation subjects require exactly one Engineering Function identity.",
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
        definition.labelSlots
            .filterNot { slot -> slot.slotId in labelValues.keys }
            .forEach { slot ->
                diagnostics += RepresentationDiagnostic(
                    code = RepresentationDiagnosticCode.LABEL_SLOT_MISSING,
                    message = "Required label slot `${slot.slotId.value}` has no authored value for `${definition.symbolId.value}`.",
                    subjectId = canonicalSemanticId,
                    provenance = definition.lifecycle.provenance,
                )
            }
        return diagnostics
    }

    private fun RepresentationBindingRequest.isCompatibleTerminalBinding(
        binding: RepresentationCompatibleTerminalBinding,
    ): Boolean {
        if (definition.bodyAuthority == RepresentationBodyAuthority.LEGACY_PRESENTATION_ANATOMY) {
            val hasLegacyTerminal = definition.anatomy.terminals.any { terminal ->
                terminal.terminalId == binding.terminalId
            }
            val hasAuthoredProjectPort = projectPorts.any { port -> port.semanticPortId == binding.semanticPortId }
            return hasLegacyTerminal && hasAuthoredProjectPort
        }
        val anchor = definition.anchors.singleOrNull { anchor ->
            anchor.role == RepresentationAnchorRole.TERMINAL &&
                PresentationTerminalId(anchor.anchorId.value) == binding.terminalId
        } ?: return false
        val port = projectPorts.singleOrNull { port -> port.semanticPortId == binding.semanticPortId } ?: return false
        return port.role == RepresentationAnchorRole.TERMINAL &&
            port.direction in anchor.acceptedDirections &&
            port.signal in anchor.acceptedSignals &&
            (anchor.terminal == null || anchor.terminal == port.terminal)
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
