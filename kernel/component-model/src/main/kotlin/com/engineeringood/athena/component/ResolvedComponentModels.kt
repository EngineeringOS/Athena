package com.engineeringood.athena.component

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Read-only resolved component knowledge for one canonical semantic subject.
 *
 * The `semanticSubjectId` still points at the canonical authored object in `Engineering IR`.
 * Component resolution adds concept identity above that canonical state; it does not become a new
 * authoring surface or mutation path.
 */
data class ResolvedComponentDefinition(
    val semanticSubjectId: StableSemanticIdentity,
    val authoredComponentReference: String,
    val concept: EngineeringConceptDefinition,
)
