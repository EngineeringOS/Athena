package com.engineeringood.athena.layout

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Explicit interactivity posture for one supported projection view.
 *
 * The posture governs whether the view is inspect-only or may participate in future governed
 * command emission. It does not itself grant frontend-local mutation authority.
 */
enum class ProjectionInteractivity {
    INSPECT_ONLY,
    INTERACTIVE,
}

/**
 * Explicit ownership contract for one supported projection view.
 *
 * The contract states what the projection may display, what governed command families it may
 * emit later, which interaction remains transient-only, and which representation metadata may
 * persist without redefining engineering truth.
 */
data class ProjectionOwnershipContract(
    val interactivity: ProjectionInteractivity = ProjectionInteractivity.INSPECT_ONLY,
    val displayScopes: List<String> = emptyList(),
    val semanticCommandIds: List<String> = emptyList(),
    val projectionCommandIds: List<String> = emptyList(),
    val transientInteractionKinds: List<String> = emptyList(),
    val persistedProjectionMetadataKeys: List<String> = emptyList(),
) {
    /**
     * Returns `true` when the projection is allowed to participate in governed mutation paths.
     */
    val isInteractive: Boolean
        get() = interactivity == ProjectionInteractivity.INTERACTIVE
}

/**
 * Canonical identity anchor used by one governed projection family contract.
 *
 * Projection families may repeat, regroup, or summarize engineering subjects, but they must keep
 * the same canonical subject identity as the anchor for reveal, inspection, and downstream review.
 */
enum class ProjectionIdentityAnchor {
    CANONICAL_SUBJECT,
}

/**
 * Semantic authority declared by one governed projection family contract.
 *
 * Family contracts classify downstream presentation families only. They do not create a second
 * semantic authority beside canonical engineering meaning.
 */
enum class ProjectionSemanticAuthority {
    CANONICAL_ENGINEERING,
}

/**
 * Typed downstream family contract attached to one supported projection view.
 *
 * The contract classifies a projection family without redefining canonical engineering meaning.
 */
sealed interface ProjectionFamilyContract {
    /**
     * Stable anchor that keeps the family attached to canonical subject identity.
     */
    val identityAnchor: ProjectionIdentityAnchor

    /**
     * Semantic authority that remains upstream of any downstream family representation.
     */
    val semanticAuthority: ProjectionSemanticAuthority
}

/**
 * First governed electrical projection-family vocabulary for the serious ECAD workbench path.
 */
enum class ElectricalProjectionFamily {
    /**
     * Connectivity-first schematic family used for canonical electrical inspection.
     */
    SCHEMATIC,

    /**
     * Structural cabinet family used for grouped placement and ownership inspection.
     */
    CABINET,

    /**
     * Connectivity-first wiring family used for signal-flow and route inspection.
     */
    WIRING,

    /**
     * Documentation-oriented family used for summarized downstream electrical outputs.
     */
    DOCUMENTATION,
}

/**
 * Typed electrical projection-family contract attached to one supported view definition.
 *
 * The descriptor keeps electrical-family classification explicit while preserving canonical
 * engineering meaning and stable subject identity outside downstream views.
 */
data class ElectricalProjectionDescriptor(
    val family: ElectricalProjectionFamily,
    override val identityAnchor: ProjectionIdentityAnchor = ProjectionIdentityAnchor.CANONICAL_SUBJECT,
    override val semanticAuthority: ProjectionSemanticAuthority = ProjectionSemanticAuthority.CANONICAL_ENGINEERING,
) : ProjectionFamilyContract

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
    val ownershipContract: ProjectionOwnershipContract = ProjectionOwnershipContract(),
    val familyContract: ProjectionFamilyContract? = null,
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
