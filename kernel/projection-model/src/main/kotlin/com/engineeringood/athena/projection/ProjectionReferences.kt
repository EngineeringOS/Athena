package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Narrow repeated-reference vocabulary emitted by one projection document.
 */
enum class ProjectionCrossReferenceKind {
    REPEATED_REFERENCE,
}

@JvmInline
value class ProjectionCrossReferenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Projection cross-reference id must not be blank." }
    }

    override fun toString(): String = value
}

data class ProjectionCrossReferenceLink(
    val semanticId: StableSemanticIdentity,
    val sourceSheetId: ProjectionSheetId,
    val targetSheetId: ProjectionSheetId,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val compactNotation: String,
) {
    init {
        require(sourceOccurrenceId.isNotBlank()) { "Projection cross-reference source occurrence id must not be blank." }
        require(targetOccurrenceId.isNotBlank()) { "Projection cross-reference target occurrence id must not be blank." }
        require(compactNotation.isNotBlank()) { "Projection cross-reference compact notation must not be blank." }
    }
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
    val crossReferenceId: ProjectionCrossReferenceId = ProjectionCrossReferenceId("cross-reference:${semanticId.value}"),
    val sheetIds: List<ProjectionSheetId> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
    val links: List<ProjectionCrossReferenceLink> = emptyList(),
)
