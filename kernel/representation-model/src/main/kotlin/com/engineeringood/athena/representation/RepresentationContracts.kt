package com.engineeringood.athena.representation

@JvmInline
value class RepresentationPolicyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation policy id must not be blank." }
    }
}

@JvmInline
value class RepresentationSymbolId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation symbol id must not be blank." }
    }
}

@JvmInline
value class RepresentationLibraryId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation library id must not be blank." }
    }
}

@JvmInline
value class RepresentationVariantId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation variant id must not be blank." }
    }
}

@JvmInline
value class RepresentationStandardProfileId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation standard profile id must not be blank." }
    }
}

@JvmInline
value class RepresentationSemanticRole(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation semantic role must not be blank." }
    }
}

@JvmInline
value class RepresentationVersion(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation version must not be blank." }
    }
}

@JvmInline
value class RepresentationProjectionOccurrenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation projection occurrence id must not be blank." }
    }
}

@JvmInline
value class RepresentationLabelSlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation label slot id must not be blank." }
    }
}

@JvmInline
value class RepresentationReferenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation reference id must not be blank." }
    }
}

@JvmInline
value class CompositionIntentMembershipId(val value: String) {
    init {
        require(value.isNotBlank()) { "Composition intent membership id must not be blank." }
    }
}

@JvmInline
value class RepresentationPolicyPriority(val value: Int) {
    init {
        require(value >= 0) { "Representation policy priority must be non-negative." }
    }
}

enum class RepresentationProjectionKind {
    ELECTRICAL_SCHEMATIC,
    DOCUMENT,
    CABINET,
    MAINTENANCE,
}

enum class RepresentationSubjectKind {
    COMPONENT,
    PORT,
    RELATIONSHIP,
    REFERENCE,
    SHEET_OCCURRENCE,
}

enum class RepresentationOccurrenceRole {
    SUPPLY_REFERENCE,
    TERMINAL,
    SWITCH_CONTACT,
    COIL_ACTUATOR,
    LAMP_INDICATOR,
    MOTOR_LOAD,
    LOAD_SYMBOL,
    PROTECTIVE_DEVICE,
    FOLIO_REFERENCE,
    ROUTE,
    LABEL,
}

enum class RepresentationFallbackBehavior {
    DIAGNOSTIC_ONLY,
    ALLOW_EXPLICIT_FALLBACK,
}

enum class RepresentationSymbolKind {
    SUPPLY_REFERENCE,
    TERMINAL,
    SWITCH_CONTACT,
    COIL_ACTUATOR,
    LAMP_INDICATOR,
    MOTOR_LOAD,
    PROTECTIVE_DEVICE,
    FOLIO_REFERENCE,
}

enum class RepresentationLifecycleState {
    ACTIVE,
    DEPRECATED,
    SUPERSEDED,
}

enum class RepresentationReferenceKind {
    COIL_CONTACT,
    DEVICE_TERMINAL_STRIP,
    COMPONENT_LOCATION,
    FOLIO_CONTINUATION,
    PREVIOUS_NEXT_REFERENCE,
}

enum class RepresentationDiagnosticCode(val wireValue: String) {
    SYMBOL_MISSING("representation.symbol.missing"),
    SYMBOL_UNSUPPORTED_ROLE("representation.symbol.unsupported-role"),
    ANCHOR_MISSING("representation.anchor.missing"),
    TERMINAL_INCOMPATIBLE("representation.terminal.incompatible"),
    LABEL_SLOT_MISSING("representation.label-slot.missing"),
    BINDING_AMBIGUOUS("representation.binding.ambiguous"),
    LIBRARY_INVALID("representation.library.invalid"),
    COMPOSITION_UNSATISFIED("representation.composition.unsatisfied"),
    POLICY_AMBIGUOUS("representation.policy.ambiguous"),
    POLICY_MISSING("representation.policy.missing"),
    LIFECYCLE_UNSUPPORTED("representation.lifecycle.unsupported"),
}

data class RepresentationPolicy(
    val policyId: RepresentationPolicyId,
    val projectionKind: RepresentationProjectionKind,
    val standardProfile: RepresentationStandardProfileId? = null,
    val subjectKind: RepresentationSubjectKind,
    val semanticRole: RepresentationSemanticRole? = null,
    val occurrenceRole: RepresentationOccurrenceRole,
    val symbolFamilyId: SymbolFamilyId,
    val symbolId: RepresentationSymbolId,
    val variant: RepresentationVariantId? = null,
    val fallback: RepresentationFallbackBehavior,
    val priority: RepresentationPolicyPriority,
) {
    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "fallback" to fallback.name,
        "occurrenceRole" to occurrenceRole.name,
        "policyId" to policyId.value,
        "priority" to priority.value.toString(),
        "projectionKind" to projectionKind.name,
        "semanticRole" to (semanticRole?.value ?: ""),
        "standardProfile" to (standardProfile?.value ?: ""),
        "subjectKind" to subjectKind.name,
        "symbolFamilyId" to symbolFamilyId.value,
        "symbolId" to symbolId.value,
        "variant" to (variant?.value ?: ""),
    )
}

data class RepresentationProvenance(
    val source: String,
) {
    init {
        require(source.isNotBlank()) { "Representation provenance source must not be blank." }
    }
}

data class RepresentationLifecycle(
    val state: RepresentationLifecycleState,
    val provenance: RepresentationProvenance,
    val supersededBy: RepresentationSymbolId? = null,
    val migrationHint: String? = null,
) {
    init {
        require(migrationHint == null || migrationHint.isNotBlank()) {
            "Representation migration hint must be null or non-blank."
        }
    }
}

data class RepresentationStyleToken(
    val name: String,
    val value: String,
) {
    init {
        require(name.isNotBlank()) { "Representation style token name must not be blank." }
        require(value.isNotBlank()) { "Representation style token value must not be blank." }
    }
}

data class RepresentationLabelSlot(
    val slotId: RepresentationLabelSlotId,
    val role: PresentationLabelRole,
)

data class RepresentationDefinition(
    val symbolId: RepresentationSymbolId,
    val libraryId: RepresentationLibraryId,
    val version: RepresentationVersion,
    val lifecycle: RepresentationLifecycle,
    val kind: RepresentationSymbolKind,
    val anatomy: PresentationAnatomy,
    val labelSlots: List<RepresentationLabelSlot>,
    val variants: List<RepresentationVariantId> = emptyList(),
    val styleTokens: List<RepresentationStyleToken> = emptyList(),
) {
    init {
        require(labelSlots.isNotEmpty()) { "Representation definition requires at least one label slot." }
    }

    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "kind" to kind.name,
        "labelSlotCount" to labelSlots.size.toString(),
        "libraryId" to libraryId.value,
        "lifecycleState" to lifecycle.state.name,
        "symbolId" to symbolId.value,
        "variantCount" to variants.size.toString(),
        "version" to version.value,
    )
}

data class RepresentationLabelBinding(
    val slotId: RepresentationLabelSlotId,
    val value: LabelValue,
)

data class RepresentationTerminalBinding(
    val terminalId: PresentationTerminalId,
    val semanticPortId: SemanticPortId,
)

data class RepresentationReferenceBinding(
    val referenceId: RepresentationReferenceId,
    val kind: RepresentationReferenceKind,
    val targetSemanticId: RepresentationSubjectId,
)

data class RepresentationOccurrence(
    val occurrenceId: RepresentationOccurrenceId,
    val canonicalSemanticId: RepresentationSubjectId,
    val projectionOccurrenceId: RepresentationProjectionOccurrenceId,
    val occurrenceRole: RepresentationOccurrenceRole,
    val symbolId: RepresentationSymbolId,
    val variant: RepresentationVariantId? = null,
    val labelBindings: List<RepresentationLabelBinding> = emptyList(),
    val terminalBindings: List<RepresentationTerminalBinding> = emptyList(),
    val referenceBindings: List<RepresentationReferenceBinding> = emptyList(),
    val compositionIntentMembership: List<CompositionIntentMembershipId> = emptyList(),
    val diagnostics: List<RepresentationDiagnostic> = emptyList(),
) {
    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "canonicalSemanticId" to canonicalSemanticId.value,
        "compositionMembershipCount" to compositionIntentMembership.size.toString(),
        "diagnosticCount" to diagnostics.size.toString(),
        "labelBindingCount" to labelBindings.size.toString(),
        "occurrenceId" to occurrenceId.value,
        "occurrenceRole" to occurrenceRole.name,
        "projectionOccurrenceId" to projectionOccurrenceId.value,
        "referenceBindingCount" to referenceBindings.size.toString(),
        "symbolId" to symbolId.value,
        "terminalBindingCount" to terminalBindings.size.toString(),
        "variant" to (variant?.value ?: ""),
    )
}

data class RepresentationDiagnostic(
    val code: RepresentationDiagnosticCode,
    val message: String,
    val subjectId: RepresentationSubjectId? = null,
) {
    init {
        require(message.isNotBlank()) { "Representation diagnostic message must not be blank." }
    }

    fun toTransportMap(): Map<String, String> = buildMap {
        put("code", code.wireValue)
        put("message", message)
        subjectId?.let { id -> put("subjectId", id.value) }
    }
}
