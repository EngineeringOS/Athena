package com.engineeringood.athena.ir

/**
 * Canonical kernel-owned engineering-knowledge snapshot attached to one compiled semantic state.
 *
 * The snapshot stays renderer-neutral and compiler-neutral: it carries only the inspectable
 * derived context, capability facts, and constraint evaluations needed by later review,
 * diagnostics, and impact-computation flows.
 */
data class EngineeringKnowledgeState(
    val derivedContext: DerivedEngineeringContext = DerivedEngineeringContext.canonical(emptyList()),
    val capabilityFacts: EngineeringCapabilityFacts = EngineeringCapabilityFacts.canonical(emptyList()),
    val constraintEvaluations: EngineeringConstraintEvaluations = EngineeringConstraintEvaluations.canonical(emptyList()),
)
