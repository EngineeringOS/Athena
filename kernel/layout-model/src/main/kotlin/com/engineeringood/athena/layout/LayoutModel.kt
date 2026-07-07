package com.engineeringood.athena.layout

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Typed definition of one supported human-facing projection context.
 *
 * View definitions describe presentation intent only. They do not redefine engineering meaning.
 */
data class ViewDefinition(
    val id: String,
    val displayName: String,
    val layoutIntent: LayoutIntent = LayoutIntent.STRUCTURAL,
    val groupingRules: List<String> = emptyList(),
    val viewEmphasis: List<ViewEmphasis> = emptyList(),
    val description: String? = null,
)

/**
 * Small typed vocabulary for the first supported layout-intent modes.
 */
enum class LayoutIntent {
    STRUCTURAL,
    CONNECTIVITY,
}

/**
 * Presentation-emphasis hints that later projection stages may interpret without changing semantics.
 */
enum class ViewEmphasis {
    OWNERSHIP,
    PLACEMENT,
    CONNECTIVITY,
    SIGNAL_FLOW,
}

/**
 * Projection-local identifier for one layout node.
 *
 * This identifier organizes layout structure but never replaces canonical semantic identity.
 */
@JvmInline
value class LayoutNodeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one layout group.
 *
 * Groups organize related layout nodes without changing canonical semantic identity.
 */
@JvmInline
value class LayoutGroupId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one layout relationship.
 *
 * Relationships remain layout-owned links that always point back to canonical semantic identity.
 */
@JvmInline
value class LayoutRelationshipId(val value: String) {
    override fun toString(): String = value
}

/**
 * Relative orientation vocabulary used by layout intent before geometry exists.
 */
enum class LayoutAxis {
    HORIZONTAL,
    VERTICAL,
}

/**
 * Relative placement relation used to explain how one layout node is arranged against another.
 */
enum class LayoutPlacementRelation {
    BEFORE,
    AFTER,
    WITHIN,
}

/**
 * Layout-owned relationship vocabulary for the first explicit projection proof.
 */
enum class LayoutRelationshipKind {
    OWNERSHIP,
    CONNECTIVITY,
}

/**
 * Relative layout hint that preserves arrangement intent without introducing exact geometry.
 */
data class LayoutRelativePlacement(
    val axis: LayoutAxis,
    val relation: LayoutPlacementRelation,
    val referenceLayoutId: LayoutNodeId? = null,
)

/**
 * Explicit layout intermediate representation document derived downstream from canonical `Engineering IR`.
 */
data class LayoutDocument(
    val view: ViewDefinition,
    val groups: List<LayoutGroup> = emptyList(),
    val nodes: List<LayoutNode>,
    val relationships: List<LayoutRelationship> = emptyList(),
)

/**
 * Explicit layout group that collects related nodes for one view.
 *
 * The group may aggregate several canonical identities, but it never replaces them.
 */
data class LayoutGroup(
    val groupId: LayoutGroupId,
    val label: String,
    val kind: String,
    val semanticIds: List<StableSemanticIdentity>,
    val memberLayoutIds: List<LayoutNodeId>,
)

/**
 * One layout-level projection item anchored to canonical semantic identity.
 */
data class LayoutNode(
    val layoutId: LayoutNodeId,
    val semanticId: StableSemanticIdentity,
    val label: String,
    val kind: String,
    val groupId: LayoutGroupId? = null,
    val order: Int = 0,
    val relativePlacement: LayoutRelativePlacement? = null,
    val emphasis: List<ViewEmphasis> = emptyList(),
)

/**
 * One layout-level relationship between nodes that still points back to canonical semantic identity.
 */
data class LayoutRelationship(
    val relationshipId: LayoutRelationshipId,
    val semanticId: StableSemanticIdentity,
    val kind: LayoutRelationshipKind,
    val sourceLayoutId: LayoutNodeId,
    val targetLayoutId: LayoutNodeId,
    val emphasis: List<ViewEmphasis> = emptyList(),
)
