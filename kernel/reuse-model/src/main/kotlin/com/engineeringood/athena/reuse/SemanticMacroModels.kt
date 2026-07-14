package com.engineeringood.athena.reuse

import com.engineeringood.athena.repository.PackageIdentifier

/**
 * Stable identity for one platform-owned Semantic Macro definition.
 *
 * This id is distinct from package identity. Packages govern discovery and versioning, while the
 * macro id names the reusable semantic assembly contract itself.
 */
@JvmInline
value class SemanticMacroId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identity for one configured Semantic Macro instantiation request.
 *
 * Instantiation identity stays above runtime expansion and below package governance so later review,
 * acceptance, and traceability flows can point at one reusable request contract.
 */
@JvmInline
value class SemanticMacroInstantiationId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable name for one Semantic Macro parameter.
 *
 * Parameter names stay surface-agnostic so workbench forms, DSL, AI, or API flows can share the
 * same contract without introducing frontend-local identifiers.
 */
@JvmInline
value class SemanticMacroParameterName(val value: String) {
    override fun toString(): String = value
}

/**
 * Governing package location for one Semantic Macro definition.
 *
 * The package binding records where the definition resolves through the existing M5 package graph
 * without allowing package identity to replace the macro's own contract identity.
 */
data class SemanticMacroPackageBinding(
    val packageId: PackageIdentifier,
    val definitionPath: String? = null,
)

/** Enumerates the first transport-friendly value kinds supported by Semantic Macro parameter schema. */
enum class SemanticMacroParameterValueKind {
    TEXT,
    SYMBOL,
    BOOLEAN,
    INTEGER,
}

/**
 * Small typed value surface for Semantic Macro parameters.
 *
 * These values remain transport-friendly and review-friendly. They do not define runtime execution,
 * widget state, or renderer payloads.
 */
sealed interface SemanticMacroParameterValue {
    /** Human-authored free text value such as a display label or equipment note. */
    data class Text(val text: String) : SemanticMacroParameterValue

    /** Symbolic contract value such as one mode key, catalog token, or semantic selector. */
    data class Symbol(val text: String) : SemanticMacroParameterValue

    /** Boolean toggle for optional semantic assembly behavior. */
    data class BooleanValue(val value: Boolean) : SemanticMacroParameterValue

    /** Integer value for narrow count or rating style parameters. */
    data class IntegerValue(val value: Int) : SemanticMacroParameterValue
}

/**
 * Parameter schema published by one Semantic Macro contract.
 *
 * The schema defines reusable parameter meaning only. It does not define form layout, validation
 * execution, or any UI-local widget behavior.
 */
data class SemanticMacroParameterDefinition(
    val name: SemanticMacroParameterName,
    val valueKind: SemanticMacroParameterValueKind,
    val label: String,
    val description: String? = null,
    val required: Boolean = false,
    val defaultValue: SemanticMacroParameterValue? = null,
    val validationRules: SemanticMacroParameterValidationRules = SemanticMacroParameterValidationRules(),
)

/**
 * Inspectable validation rules for one Semantic Macro parameter definition.
 *
 * Rules stay transport-safe and runtime-owned so parameter defaults, required checks, and proof-slice
 * constraints do not fragment across workbench widgets or later API surfaces.
 */
data class SemanticMacroParameterValidationRules(
    val allowedValues: List<String> = emptyList(),
    val pattern: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minInteger: Int? = null,
    val maxInteger: Int? = null,
)

/**
 * Platform-owned semantic reuse contract published for one governed macro definition.
 *
 * This contract remains semantic-first. It does not collapse into graphics truth, runtime catalog
 * orchestration, or a second mutation authority.
 */
data class SemanticMacroContract(
    val macroId: SemanticMacroId,
    val displayName: String,
    val summary: String,
    val packageBinding: SemanticMacroPackageBinding,
    val parameters: List<SemanticMacroParameterDefinition> = emptyList(),
    val classificationKeys: Set<String> = emptySet(),
)
