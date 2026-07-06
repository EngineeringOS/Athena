package com.engineeringood.athena.compiler.plugin

/** Severity assigned to plugin validation diagnostics. */
enum class PluginValidationSeverity {
    ERROR,
}

/** Stable identifier for one plugin validation rule. */
@JvmInline
value class PluginValidationRuleId(val value: String) {
    override fun toString(): String = value
}

/** Inspectable validation diagnostic emitted for a plugin manifest or typed plugin contract mismatch. */
data class PluginValidationDiagnostic(
    val severity: PluginValidationSeverity,
    val ruleId: PluginValidationRuleId,
    val subject: String,
    val message: String,
)

/** Result of validating a plugin manifest or plugin object against the core-owned contract. */
data class PluginValidationResult(
    val diagnostics: List<PluginValidationDiagnostic>,
) {
    /** True when no error-level diagnostics were emitted. */
    val isValid: Boolean
        get() = diagnostics.none { it.severity == PluginValidationSeverity.ERROR }
}
