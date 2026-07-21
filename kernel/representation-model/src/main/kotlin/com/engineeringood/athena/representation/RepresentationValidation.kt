package com.engineeringood.athena.representation

data class RepresentationCompatibleTerminalBinding(
    val terminalId: PresentationTerminalId,
    val semanticPortId: SemanticPortId,
)

data class RepresentationValidationInput(
    val allowedLibraries: Set<RepresentationLibraryId>,
    val supportedLifecycleStates: Set<RepresentationLifecycleState> = setOf(RepresentationLifecycleState.ACTIVE),
    val policies: List<RepresentationPolicy>,
    val definitions: List<RepresentationDefinition>,
    val occurrences: List<RepresentationOccurrence>,
    val compatibleTerminalBindings: Set<RepresentationCompatibleTerminalBinding> = emptySet(),
    val compositionMemberships: Set<CompositionIntentMembershipId> = emptySet(),
)

data class RepresentationValidationResult(
    val diagnostics: List<RepresentationDiagnostic>,
) {
    val accepted: Boolean
        get() = diagnostics.isEmpty()

    fun toTransportPayload(): List<Map<String, String>> {
        return diagnostics.map { diagnostic -> diagnostic.toTransportMap() }
    }
}

object RepresentationContractValidator {
    fun validate(input: RepresentationValidationInput): RepresentationValidationResult {
        val diagnostics = mutableListOf<RepresentationDiagnostic>()
        val definitionsBySymbol = input.definitions.groupBy { definition -> definition.symbolId }
        val firstDefinitionBySymbol = definitionsBySymbol.mapValues { (_, definitions) -> definitions.first() }

        definitionsBySymbol
            .filterValues { definitions -> definitions.size > 1 }
            .keys
            .forEach { symbolId ->
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.BINDING_AMBIGUOUS,
                    "Multiple representation definitions declare `${symbolId.value}`.",
                    symbolId,
                )
            }

        input.definitions.forEach { definition ->
            if (definition.libraryId !in input.allowedLibraries) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.LIBRARY_INVALID,
                    "Representation library `${definition.libraryId.value}` is not allowed.",
                    definition.symbolId,
                )
            }
            if (definition.lifecycle.state !in input.supportedLifecycleStates) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.LIFECYCLE_UNSUPPORTED,
                    "Representation lifecycle `${definition.lifecycle.state.name}` is not supported.",
                    definition.symbolId,
                )
            }
        }

        input.policies.forEach { policy ->
            val definition = firstDefinitionBySymbol[policy.symbolId]
            if (definition == null) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.SYMBOL_MISSING,
                    "Representation policy `${policy.policyId.value}` references missing symbol `${policy.symbolId.value}`.",
                    policy.symbolId,
                )
            } else if (policy.occurrenceRole !in definition.kind.supportedOccurrenceRoles()) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.SYMBOL_UNSUPPORTED_ROLE,
                    "Symbol `${policy.symbolId.value}` does not support occurrence role `${policy.occurrenceRole.name}`.",
                    policy.symbolId,
                )
            }
        }

        input.occurrences.forEach { occurrence ->
            val definition = firstDefinitionBySymbol[occurrence.symbolId]
            if (definition == null) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.SYMBOL_MISSING,
                    "Representation occurrence `${occurrence.occurrenceId.value}` references missing symbol `${occurrence.symbolId.value}`.",
                    occurrence.symbolId,
                )
            }

            if (input.policies.none { policy ->
                    policy.symbolId == occurrence.symbolId && policy.occurrenceRole == occurrence.occurrenceRole
                }
            ) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.POLICY_MISSING,
                    "No representation policy matches occurrence `${occurrence.occurrenceId.value}`.",
                    occurrence.canonicalSemanticId,
                )
            }

            val labelSlotIds = definition?.labelSlots.orEmpty().map { slot -> slot.slotId }.toSet()
            occurrence.labelBindings
                .filterNot { binding -> binding.slotId in labelSlotIds }
                .forEach { binding ->
                    diagnostics += diagnostic(
                        RepresentationDiagnosticCode.LABEL_SLOT_MISSING,
                        "Occurrence `${occurrence.occurrenceId.value}` binds missing label slot `${binding.slotId.value}`.",
                        occurrence.canonicalSemanticId,
                    )
                }

            val terminalIds = definition?.anatomy?.terminals.orEmpty().map { terminal -> terminal.terminalId }.toSet()
            occurrence.terminalBindings.forEach { binding ->
                if (binding.terminalId !in terminalIds) {
                    diagnostics += diagnostic(
                        RepresentationDiagnosticCode.ANCHOR_MISSING,
                        "Occurrence `${occurrence.occurrenceId.value}` binds missing terminal anchor `${binding.terminalId.value}`.",
                        occurrence.canonicalSemanticId,
                    )
                }
                val compatibleBinding = RepresentationCompatibleTerminalBinding(binding.terminalId, binding.semanticPortId)
                if (compatibleBinding !in input.compatibleTerminalBindings) {
                    diagnostics += diagnostic(
                        RepresentationDiagnosticCode.TERMINAL_INCOMPATIBLE,
                        "Terminal `${binding.terminalId.value}` is not compatible with semantic port `${binding.semanticPortId.value}`.",
                        occurrence.canonicalSemanticId,
                    )
                }
            }

            occurrence.compositionIntentMembership
                .filterNot { membership -> membership in input.compositionMemberships }
                .forEach { membership ->
                    diagnostics += diagnostic(
                        RepresentationDiagnosticCode.COMPOSITION_UNSATISFIED,
                        "Composition membership `${membership.value}` is not satisfied.",
                        occurrence.canonicalSemanticId,
                    )
                }
        }

        return RepresentationValidationResult(diagnostics.canonical())
    }

    private fun diagnostic(
        code: RepresentationDiagnosticCode,
        message: String,
        symbolId: RepresentationSymbolId,
    ): RepresentationDiagnostic = RepresentationDiagnostic(
        code = code,
        message = message,
        subjectId = RepresentationSubjectId("symbol:${symbolId.value}"),
    )

    private fun diagnostic(
        code: RepresentationDiagnosticCode,
        message: String,
        subjectId: RepresentationSubjectId,
    ): RepresentationDiagnostic = RepresentationDiagnostic(
        code = code,
        message = message,
        subjectId = subjectId,
    )
}

private fun List<RepresentationDiagnostic>.canonical(): List<RepresentationDiagnostic> {
    return sortedWith(
        compareBy<RepresentationDiagnostic>(
            { diagnostic -> diagnostic.code.wireValue },
            { diagnostic -> diagnostic.subjectId?.value.orEmpty() },
            { diagnostic -> diagnostic.message },
        ),
    )
}

private fun RepresentationSymbolKind.supportedOccurrenceRoles(): Set<RepresentationOccurrenceRole> = when (this) {
    RepresentationSymbolKind.SUPPLY_REFERENCE -> setOf(RepresentationOccurrenceRole.SUPPLY_REFERENCE)
    RepresentationSymbolKind.TERMINAL -> setOf(RepresentationOccurrenceRole.TERMINAL)
    RepresentationSymbolKind.SWITCH_CONTACT -> setOf(RepresentationOccurrenceRole.SWITCH_CONTACT)
    RepresentationSymbolKind.COIL_ACTUATOR -> setOf(RepresentationOccurrenceRole.COIL_ACTUATOR)
    RepresentationSymbolKind.LAMP_INDICATOR -> setOf(RepresentationOccurrenceRole.LAMP_INDICATOR)
    RepresentationSymbolKind.MOTOR_LOAD -> setOf(
        RepresentationOccurrenceRole.MOTOR_LOAD,
        RepresentationOccurrenceRole.LOAD_SYMBOL,
    )
    RepresentationSymbolKind.PROTECTIVE_DEVICE -> setOf(RepresentationOccurrenceRole.PROTECTIVE_DEVICE)
    RepresentationSymbolKind.FOLIO_REFERENCE -> setOf(RepresentationOccurrenceRole.FOLIO_REFERENCE)
}
