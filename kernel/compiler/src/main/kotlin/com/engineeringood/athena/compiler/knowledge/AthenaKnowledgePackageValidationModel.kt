package com.engineeringood.athena.compiler.knowledge

/** Severity assigned to governed knowledge package diagnostics. */
enum class AthenaKnowledgePackageSeverity {
    ERROR,
}

/** Stable identifier for one governed knowledge package validation rule. */
@JvmInline
value class AthenaKnowledgePackageRuleId(val value: String) {
    override fun toString(): String = value
}

/** Inspectable validation diagnostic emitted while loading a governed knowledge package. */
data class AthenaKnowledgePackageDiagnostic(
    val severity: AthenaKnowledgePackageSeverity,
    val ruleId: AthenaKnowledgePackageRuleId,
    val subject: String,
    val message: String,
)
