package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Narrow repeated-reference vocabulary emitted by one projection document.
 */
enum class ProjectionCrossReferenceKind {
    REPEATED_REFERENCE,
}

/**
 * Inspectable downstream cross-reference for one canonical semantic subject.
 *
 * M11 keeps this vocabulary intentionally small. Canonical engineering meaning remains upstream;
 * this contract only records how downstream sheets and occurrences point back to it.
 */
data class ProjectionCrossReference(
    val semanticId: StableSemanticIdentity,
    val kind: ProjectionCrossReferenceKind,
    val sheetIds: List<ProjectionSheetId> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
)
