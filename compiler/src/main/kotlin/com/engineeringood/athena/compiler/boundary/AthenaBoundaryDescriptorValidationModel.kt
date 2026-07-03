package com.engineeringood.athena.compiler.boundary

import java.nio.file.Path

/** Severity assigned to external boundary descriptor diagnostics. */
enum class AthenaBoundarySeverity {
    ERROR,
}

/** Stable identifier for one external boundary descriptor validation rule. */
@JvmInline
value class AthenaBoundaryRuleId(val value: String) {
    override fun toString(): String = value
}

/** Inspectable validation diagnostic emitted while loading or validating an external boundary descriptor. */
data class AthenaBoundaryDiagnostic(
    val severity: AthenaBoundarySeverity,
    val ruleId: AthenaBoundaryRuleId,
    val subject: String,
    val message: String,
)

/** Valid external boundary descriptor candidate loaded successfully and eligible for M0 posture validation. */
data class AthenaBoundaryCandidateDescriptor(
    val packageRoot: Path,
    val descriptor: AthenaBoundaryDescriptor,
)

/** Inspectable external boundary descriptor that remains passive and non-sovereign in M0. */
data class AthenaValidBoundaryDescriptor(
    val packageRoot: Path,
    val descriptorId: String,
    val category: AthenaBoundaryCategory,
    val direction: AthenaBoundaryDirection,
    val upstreamAuthority: AthenaBoundarySemanticAuthority,
    val exchangeForms: List<AthenaBoundaryExchangeFormKind>,
    val compatibilityAssumptions: List<AthenaBoundaryCompatibilityAssumption>,
)

/** Inspectable rejected external boundary descriptor with optional identity hint and stable diagnostics. */
data class AthenaRejectedBoundaryDescriptor(
    val packageRoot: Path,
    val descriptorId: String?,
    val diagnostics: List<AthenaBoundaryDiagnostic>,
)

/** Deterministic compiler-owned validation report for the configured external boundary descriptors. */
data class AthenaBoundaryValidationReport(
    val source: AthenaBoundaryDescriptorSource,
    val candidates: List<AthenaBoundaryCandidateDescriptor>,
    val validDescriptors: List<AthenaValidBoundaryDescriptor>,
    val rejectedDescriptors: List<AthenaRejectedBoundaryDescriptor>,
) {
    companion object {
        /** Returns an empty boundary validation report for compiler runs with no descriptor inputs. */
        fun empty(
            source: AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource.empty(),
        ): AthenaBoundaryValidationReport {
            return AthenaBoundaryValidationReport(
                source = source,
                candidates = emptyList(),
                validDescriptors = emptyList(),
                rejectedDescriptors = emptyList(),
            )
        }
    }
}
