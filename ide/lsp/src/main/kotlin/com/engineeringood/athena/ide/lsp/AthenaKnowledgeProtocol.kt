package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.ir.EngineeringImpactConsequence
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

/**
 * One typed engineering knowledge diagnostic transported through the Athena LSP boundary.
 */
data class AthenaEngineeringKnowledgeDiagnosticPayload(
    val severity: String,
    val ruleId: String,
    val message: String,
    val subjectIdentity: String? = null,
)

/**
 * One typed engineering impact consequence transported through the Athena LSP boundary.
 */
data class AthenaEngineeringImpactConsequencePayload(
    val affectedSubjectIdentity: String,
    val triggerSubjectIdentities: List<String>,
    val reasonKinds: List<String>,
    val affectedInputKinds: List<String>,
    val affectedDerivedValueKinds: List<String>,
    val affectedCapabilityFactKinds: List<String>,
    val affectedConstraintRuleKinds: List<String>,
)

/**
 * Current engineering knowledge snapshot published alongside semantic inspection results.
 */
data class AthenaEngineeringKnowledgeInspectionPayload(
    val derivedSubjectCount: Int,
    val capabilityFactCount: Int,
    val constraintEvaluationCount: Int,
    val knowledgeDiagnosticsCount: Int,
    val knowledgeDiagnostics: List<AthenaEngineeringKnowledgeDiagnosticPayload>,
)

internal fun SemanticDiagnostic.toKnowledgePayload(): AthenaEngineeringKnowledgeDiagnosticPayload {
    return AthenaEngineeringKnowledgeDiagnosticPayload(
        severity = severity.name.lowercase(),
        ruleId = ruleId.value,
        message = message,
        subjectIdentity = subjectIdentity?.value,
    )
}

internal fun EngineeringImpactConsequence.toPayload(): AthenaEngineeringImpactConsequencePayload {
    return AthenaEngineeringImpactConsequencePayload(
        affectedSubjectIdentity = affectedSubjectIdentity.value,
        triggerSubjectIdentities = triggerSubjectIdentities.map { identity -> identity.value },
        reasonKinds = reasonKinds.map { reason -> reason.name.lowercase().replace('_', '-') },
        affectedInputKinds = affectedInputKinds.map { kind -> kind.name.lowercase().replace('_', '-') },
        affectedDerivedValueKinds = affectedDerivedValueKinds.map { kind -> kind.name.lowercase().replace('_', '-') },
        affectedCapabilityFactKinds = affectedCapabilityFactKinds.map { kind -> kind.name.lowercase().replace('_', '-') },
        affectedConstraintRuleKinds = affectedConstraintRuleKinds.map { kind -> kind.name.lowercase().replace('_', '-') },
    )
}
