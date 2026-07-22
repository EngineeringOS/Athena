package com.engineeringood.athena.representation

data class M30DemoRepresentationDevice(
    val semanticId: RepresentationSubjectId,
    val semanticRole: RepresentationSemanticRole,
    val occurrenceRole: RepresentationOccurrenceRole,
    val symbolId: RepresentationSymbolId,
    val terminalId: PresentationTerminalId,
    val semanticPortId: SemanticPortId,
    val label: LabelValue,
)

data class M30DemoRepresentationReferenceFact(
    val semanticId: RepresentationSubjectId,
    val targetSemanticId: RepresentationSubjectId,
    val label: LabelValue,
)

data class M30DemoRepresentationSample(
    val devices: List<M30DemoRepresentationDevice>,
    val referenceFacts: List<M30DemoRepresentationReferenceFact>,
) {
    companion object {
        fun controlSheet(): M30DemoRepresentationSample = M30DemoRepresentationSample(
            devices = listOf(
                device("component:SupplyPS1", "supply-reference", RepresentationOccurrenceRole.SUPPLY_REFERENCE, "iec.supply-reference.compact", "out", "SupplyPS1.lplus", "PS1"),
                device("component:TerminalXT1", "terminal-transition", RepresentationOccurrenceRole.TERMINAL, "iec.terminal.compact", "inout", "TerminalXT1.1", "XT1"),
                device("component:StartSwitchS1", "switch-contact", RepresentationOccurrenceRole.SWITCH_CONTACT, "iec.switch-contact.no.compact", "left", "StartSwitchS1.13", "S1"),
                device("component:ControlRelayK1", "coil-actuator", RepresentationOccurrenceRole.COIL_ACTUATOR, "iec.coil.compact", "a1", "ControlRelayK1.a1", "K1"),
                device("component:StatusLampH1", "lamp-indicator", RepresentationOccurrenceRole.LAMP_INDICATOR, "iec.lamp.compact", "in", "StatusLampH1.x1", "H1"),
                device("component:MotorM1", "power-load", RepresentationOccurrenceRole.LOAD_SYMBOL, "iec.motor.compact", "u1", "MotorM1.u1", "M1"),
                device("component:MainBreakerQF1", "protective-device", RepresentationOccurrenceRole.PROTECTIVE_DEVICE, "iec.protective-device.compact", "line", "MainBreakerQF1.line", "QF1"),
            ),
            referenceFacts = listOf(
                M30DemoRepresentationReferenceFact(
                    semanticId = RepresentationSubjectId("connection:ControlRelayK1.a1->MotorM1.u1"),
                    targetSemanticId = RepresentationSubjectId("sheet:field/continuation:MotorM1"),
                    label = LabelValue("/2.4"),
                ),
            ),
        )

        private fun device(
            semanticId: String,
            role: String,
            occurrenceRole: RepresentationOccurrenceRole,
            symbolId: String,
            terminalId: String,
            semanticPortId: String,
            label: String,
        ): M30DemoRepresentationDevice = M30DemoRepresentationDevice(
            semanticId = RepresentationSubjectId(semanticId),
            semanticRole = RepresentationSemanticRole(role),
            occurrenceRole = occurrenceRole,
            symbolId = RepresentationSymbolId(symbolId),
            terminalId = PresentationTerminalId(terminalId),
            semanticPortId = SemanticPortId(semanticPortId),
            label = LabelValue(label),
        )
    }
}

data class M30DemoRepresentationBindingProof(
    val deviceOccurrences: List<RepresentationOccurrence>,
    val referenceOccurrences: List<RepresentationOccurrence>,
    val diagnostics: List<RepresentationDiagnostic>,
)

fun M30DemoRepresentationBindingProof.toBindingStatusPayload(): Map<String, String> = linkedMapOf(
    "accepted" to diagnostics.isEmpty().toString(),
    "deviceOccurrenceCount" to deviceOccurrences.size.toString(),
    "referenceOccurrenceCount" to referenceOccurrences.size.toString(),
    "diagnosticCount" to diagnostics.size.toString(),
    "missingBindingDiagnosticCount" to diagnostics.count { diagnostic ->
        diagnostic.code in MISSING_BINDING_DIAGNOSTIC_CODES
    }.toString(),
    "deviceSymbolIds" to deviceOccurrences
        .map { occurrence -> occurrence.symbolId.value }
        .distinct()
        .sorted()
        .joinToString(","),
    "occurrenceRoles" to (deviceOccurrences + referenceOccurrences)
        .map { occurrence -> occurrence.occurrenceRole.name }
        .distinct()
        .sorted()
        .joinToString(","),
    "compositionMembershipCount" to (deviceOccurrences + referenceOccurrences)
        .sumOf { occurrence -> occurrence.compositionIntentMembership.size }
        .toString(),
)

object RepresentationFallbackGuard {
    fun acceptsRendererFallback(
        fallbackUsed: Boolean,
        diagnostics: List<RepresentationDiagnostic>,
    ): Boolean = fallbackUsed.not() || diagnostics.isNotEmpty()
}

class M30DemoRepresentationBinder(
    private val compiler: RepresentationBindingCompiler = RepresentationBindingCompiler(),
) {
    fun bind(
        sample: M30DemoRepresentationSample,
        library: NativeRepresentationLibrary,
    ): M30DemoRepresentationBindingProof {
        val definitionsBySymbol = library.definitions.associateBy { definition -> definition.symbolId }
        val diagnostics = mutableListOf<RepresentationDiagnostic>()
        val deviceOccurrences = sample.devices.mapNotNull { device ->
            val definition = definitionsBySymbol[device.symbolId]
            if (definition == null) {
                diagnostics += RepresentationDiagnostic(
                    code = RepresentationDiagnosticCode.SYMBOL_MISSING,
                    message = "Missing demo symbol `${device.symbolId.value}`.",
                    subjectId = device.semanticId,
                )
                return@mapNotNull null
            }
            val policy = policyFor(
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = device.semanticRole,
                occurrenceRole = device.occurrenceRole,
                symbolId = device.symbolId,
            )
            val labelSlot = definition.labelSlots.first().slotId
            val result = compiler.bind(
                RepresentationBindingRequest(
                    canonicalSemanticId = device.semanticId,
                    projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/${device.semanticId.value}"),
                    subjectKind = RepresentationSubjectKind.COMPONENT,
                    semanticRole = device.semanticRole,
                    projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                    policy = policy,
                    definition = definition,
                    labelValues = mapOf(labelSlot to device.label),
                    terminalPorts = mapOf(device.terminalId to device.semanticPortId),
                    priority = RepresentationPolicyPriority(100),
                ),
            )
            diagnostics += result.diagnostics
            result.occurrenceOrNull
        }
        val referenceOccurrences = sample.referenceFacts.mapNotNull { referenceFact ->
            val symbolId = RepresentationSymbolId("iec.folio-reference.compact")
            val definition = definitionsBySymbol[symbolId] ?: return@mapNotNull null
            val policy = policyFor(
                subjectKind = RepresentationSubjectKind.REFERENCE,
                semanticRole = RepresentationSemanticRole("folio-continuation"),
                occurrenceRole = RepresentationOccurrenceRole.FOLIO_REFERENCE,
                symbolId = symbolId,
            )
            val result = compiler.bind(
                RepresentationBindingRequest(
                    canonicalSemanticId = referenceFact.semanticId,
                    projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/reference:${referenceFact.semanticId.value}"),
                    subjectKind = RepresentationSubjectKind.REFERENCE,
                    semanticRole = RepresentationSemanticRole("folio-continuation"),
                    projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                    policy = policy,
                    definition = definition,
                    labelValues = mapOf(definition.labelSlots.first().slotId to referenceFact.label),
                    terminalPorts = mapOf(PresentationTerminalId("continuation") to SemanticPortId(referenceFact.semanticId.value)),
                    priority = RepresentationPolicyPriority(100),
                    referenceBindings = listOf(
                        RepresentationReferenceBinding(
                            referenceId = RepresentationReferenceId("reference:${referenceFact.semanticId.value}"),
                            kind = RepresentationReferenceKind.FOLIO_CONTINUATION,
                            targetSemanticId = referenceFact.targetSemanticId,
                        ),
                    ),
                ),
            )
            diagnostics += result.diagnostics
            result.occurrenceOrNull
        }
        return M30DemoRepresentationBindingProof(
            deviceOccurrences = deviceOccurrences,
            referenceOccurrences = referenceOccurrences,
            diagnostics = diagnostics.sortedWith(
                compareBy<RepresentationDiagnostic>(
                    { diagnostic -> diagnostic.code.wireValue },
                    { diagnostic -> diagnostic.subjectId?.value.orEmpty() },
                    { diagnostic -> diagnostic.message },
                ),
            ),
        )
    }

    private fun policyFor(
        subjectKind: RepresentationSubjectKind,
        semanticRole: RepresentationSemanticRole,
        occurrenceRole: RepresentationOccurrenceRole,
        symbolId: RepresentationSymbolId,
    ): RepresentationPolicy = RepresentationPolicy(
        policyId = RepresentationPolicyId("policy:${semanticRole.value}"),
        projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
        subjectKind = subjectKind,
        semanticRole = semanticRole,
        occurrenceRole = occurrenceRole,
        symbolFamilyId = compactSymbolFamily(symbolId),
        symbolId = symbolId,
        variant = RepresentationVariantId("compact"),
        fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
        priority = RepresentationPolicyPriority(100),
    )
}

private fun compactSymbolFamily(symbolId: RepresentationSymbolId): SymbolFamilyId =
    SymbolFamilyId(symbolId.value.removeSuffix(".compact"))

private val MISSING_BINDING_DIAGNOSTIC_CODES = setOf(
    RepresentationDiagnosticCode.SYMBOL_MISSING,
    RepresentationDiagnosticCode.SYMBOL_UNSUPPORTED_ROLE,
    RepresentationDiagnosticCode.ANCHOR_MISSING,
    RepresentationDiagnosticCode.LABEL_SLOT_MISSING,
    RepresentationDiagnosticCode.BINDING_AMBIGUOUS,
    RepresentationDiagnosticCode.POLICY_MISSING,
)
