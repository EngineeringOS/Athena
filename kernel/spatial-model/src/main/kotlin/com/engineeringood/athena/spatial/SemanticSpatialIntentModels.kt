package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Stable identifier for one semantic spatial intent fact.
 *
 * Intent ids are compiler/projection owned. They are not DOM, SVG, canvas, or router backend ids.
 */
@JvmInline
value class SemanticSpatialIntentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Semantic spatial intent id must not be blank." }
    }

    override fun toString(): String = value
}

/**
 * Stable identifier for an immutable semantic spatial intent snapshot.
 */
@JvmInline
value class SemanticSpatialIntentSnapshotId(val value: String) {
    init {
        require(value.isNotBlank()) { "Semantic spatial intent snapshot id must not be blank." }
    }

    override fun toString(): String = value
}

/**
 * Projection scope for M27 Semantic Spatial Compiler v0.
 *
 * M27 deliberately admits only 2D electrical schematic projection. Future values can be added for
 * other projections without making this contract a general CAD geometry kernel.
 */
enum class SemanticSpatialProjectionScope {
    ELECTRICAL_SCHEMATIC_2D,
}

/**
 * Subject role carried by a spatial intent fact.
 */
enum class SemanticSpatialSubjectKind {
    COMPONENT,
    PORT,
    TERMINAL,
    CONNECTION,
    ROUTE,
    DOCUMENT_OCCURRENCE,
}

/**
 * Direction preference before any solver produces route geometry.
 */
enum class SemanticSpatialDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
}

/**
 * Terminal or anchor side preference before any renderer paints a symbol.
 */
enum class SemanticSpatialSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

/**
 * Lane preference for relationship occupancy. This is not route geometry.
 */
enum class SemanticSpatialLanePreference {
    HORIZONTAL_FIRST,
    VERTICAL_FIRST,
    DIRECT,
}

/**
 * Route ordering preference for deterministic lane assignment.
 */
enum class SemanticSpatialOrdering {
    SOURCE_TO_TARGET,
    TARGET_TO_SOURCE,
    DOCUMENT_ORDER,
    SIGNAL_FLOW,
}

/**
 * Constraint strength used to explain conflicts before routing/layout compilation.
 */
enum class SemanticSpatialPriority(val sortRank: Int) {
    HARD(0),
    STRONG(1),
    SOFT(2),
    PREFERENCE(3),
}

/**
 * Source of a spatial intent fact.
 *
 * Source attribution is mandatory so future AI or policy-generated intent cannot be confused with
 * hard semantic truth.
 */
enum class SemanticSpatialConstraintSource {
    SEMANTIC_MODEL,
    PRESENTATION_POLICY,
    ROUTING_POLICY,
    LAYOUT_FACT,
    DOCUMENT_PROJECTION_POLICY,
    USER_PREVIEW,
    AI_SUGGESTION,
}

/**
 * Bounded confidence in one generated spatial intent fact.
 */
data class SemanticSpatialConfidence(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Semantic spatial confidence must be between 0.0 and 1.0." }
    }
}

/**
 * Canonical subject reference for one spatial intent fact.
 *
 * This binds the intent to semantic/projected identities only. It intentionally excludes raw x/y
 * coordinates, DOM handles, SVG element ids, and backend-specific route ids.
 */
data class SemanticSpatialSubjectRef(
    val semanticId: StableSemanticIdentity,
    val kind: SemanticSpatialSubjectKind,
    val occurrenceId: String? = null,
    val sheetId: String? = null,
    val viewId: String? = null,
    val anchorId: String? = null,
    val terminalId: String? = null,
    val portId: String? = null,
) {
    init {
        require(occurrenceId?.isNotBlank() != false) { "Occurrence id must not be blank when present." }
        require(sheetId?.isNotBlank() != false) { "Sheet id must not be blank when present." }
        require(viewId?.isNotBlank() != false) { "View id must not be blank when present." }
        require(anchorId?.isNotBlank() != false) { "Anchor id must not be blank when present." }
        require(terminalId?.isNotBlank() != false) { "Terminal id must not be blank when present." }
        require(portId?.isNotBlank() != false) { "Port id must not be blank when present." }
    }

    internal fun stableKey(): String = listOf(
        kind.name,
        semanticId.value,
        occurrenceId.orEmpty(),
        sheetId.orEmpty(),
        viewId.orEmpty(),
        anchorId.orEmpty(),
        terminalId.orEmpty(),
        portId.orEmpty(),
    ).joinToString(separator = "|")
}

/**
 * Domain-neutral grouping hint for subjects or routes.
 */
data class SemanticSpatialGrouping(
    val groupId: String,
    val role: String,
) {
    init {
        require(groupId.isNotBlank()) { "Spatial grouping id must not be blank." }
        require(role.isNotBlank()) { "Spatial grouping role must not be blank." }
    }

    internal fun stableKey(): String = "$groupId|$role"
}

/**
 * Separation preference between this subject and another canonical subject.
 */
data class SemanticSpatialSeparation(
    val from: SemanticSpatialSubjectRef,
    val avoid: SemanticSpatialSubjectRef,
    val priority: SemanticSpatialPriority = SemanticSpatialPriority.STRONG,
) {
    internal fun stableKey(): String = listOf(
        priority.sortRank.toString().padStart(2, '0'),
        from.stableKey(),
        avoid.stableKey(),
    ).joinToString(separator = "|")
}

/**
 * Component-body avoidance target. This remains an identity-level constraint, not a geometry model.
 */
data class SemanticSpatialAvoidance(
    val target: SemanticSpatialSubjectRef,
    val reason: String,
    val priority: SemanticSpatialPriority = SemanticSpatialPriority.HARD,
) {
    init {
        require(reason.isNotBlank()) { "Spatial avoidance reason must not be blank." }
    }

    internal fun stableKey(): String = listOf(
        priority.sortRank.toString().padStart(2, '0'),
        target.stableKey(),
        reason,
    ).joinToString(separator = "|")
}

/**
 * One compiler-owned semantic spatial intent fact.
 *
 * This is pre-layout and pre-routing intent. It does not store canvas coordinates, route points,
 * solved bounds, CSS, DOM state, SVG segments, or backend-specific data.
 */
data class SemanticSpatialIntent(
    val intentId: SemanticSpatialIntentId,
    val scope: SemanticSpatialProjectionScope,
    val subject: SemanticSpatialSubjectRef,
    val relation: String,
    val priority: SemanticSpatialPriority,
    val confidence: SemanticSpatialConfidence,
    val source: SemanticSpatialConstraintSource,
    val preferredDirection: SemanticSpatialDirection? = null,
    val terminalSide: SemanticSpatialSide? = null,
    val lanePreference: SemanticSpatialLanePreference? = null,
    val ordering: SemanticSpatialOrdering? = null,
    val groupings: List<SemanticSpatialGrouping> = emptyList(),
    val separations: List<SemanticSpatialSeparation> = emptyList(),
    val avoidances: List<SemanticSpatialAvoidance> = emptyList(),
) {
    init {
        require(relation.isNotBlank()) { "Semantic spatial relation must not be blank." }
        require(scope == SemanticSpatialProjectionScope.ELECTRICAL_SCHEMATIC_2D) {
            "M27 Semantic Spatial Compiler v0 only admits 2D electrical schematic projection."
        }
    }

    internal fun stableKey(): String = listOf(
        priority.sortRank.toString().padStart(2, '0'),
        source.name,
        scope.name,
        subject.stableKey(),
        relation,
        intentId.value,
        preferredDirection?.name.orEmpty(),
        terminalSide?.name.orEmpty(),
        lanePreference?.name.orEmpty(),
        ordering?.name.orEmpty(),
        groupings.sortedBy(SemanticSpatialGrouping::stableKey).joinToString(separator = ",") { it.stableKey() },
        separations.sortedBy(SemanticSpatialSeparation::stableKey).joinToString(separator = ",") { it.stableKey() },
        avoidances.sortedBy(SemanticSpatialAvoidance::stableKey).joinToString(separator = ",") { it.stableKey() },
    ).joinToString(separator = "|")
}

/**
 * Immutable canonical snapshot of semantic spatial intent facts.
 */
data class SemanticSpatialIntentSnapshot(
    val snapshotId: SemanticSpatialIntentSnapshotId,
    val scope: SemanticSpatialProjectionScope,
    val intents: List<SemanticSpatialIntent>,
) {
    init {
        require(intents.all { intent -> intent.scope == scope }) {
            "All semantic spatial intents must match the snapshot projection scope."
        }
    }

    companion object {
        fun canonical(
            snapshotId: SemanticSpatialIntentSnapshotId,
            scope: SemanticSpatialProjectionScope,
            intents: List<SemanticSpatialIntent>,
        ): SemanticSpatialIntentSnapshot = SemanticSpatialIntentSnapshot(
            snapshotId = snapshotId,
            scope = scope,
            intents = intents.sortedBy(SemanticSpatialIntent::stableKey),
        )
    }
}
