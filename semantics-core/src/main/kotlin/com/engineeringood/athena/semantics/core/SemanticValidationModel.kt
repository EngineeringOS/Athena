package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Severity assigned to one semantic diagnostic emitted during validation. */
enum class SemanticDiagnosticSeverity {
    ERROR,
    WARNING,
}

/** Broad semantic rule family used to group diagnostics for inspection and pipeline policy. */
enum class SemanticDiagnosticCategory {
    REFERENCE,
    UNIQUENESS,
    PROPERTY,
    CONNECTION,
    DOMAIN,
}

/** Stable identifier for one semantic validation rule. */
@JvmInline
value class SemanticRuleId(val value: String) {
    override fun toString(): String = value
}

/** Provenance-rich semantic diagnostic emitted over canonical Engineering IR. */
data class SemanticDiagnostic(
    val severity: SemanticDiagnosticSeverity,
    val ruleId: SemanticRuleId,
    val category: SemanticDiagnosticCategory,
    val subjectIdentity: StableSemanticIdentity?,
    val provenance: SourceProvenance,
    val message: String,
)

/** Explicit policy outcome that tells later compiler passes whether they may continue. */
enum class SemanticContinuationDecision {
    CONTINUE,
    STOP_DOWNSTREAM,
}

/** Result of running semantic validation over canonical Engineering IR. */
data class SemanticValidationResult(
    val diagnostics: List<SemanticDiagnostic>,
    val continuationDecision: SemanticContinuationDecision,
) {
    /** True when no error-level semantic diagnostics were emitted. */
    val isSemanticallyValid: Boolean
        get() = diagnostics.none { it.severity == SemanticDiagnosticSeverity.ERROR }
}
