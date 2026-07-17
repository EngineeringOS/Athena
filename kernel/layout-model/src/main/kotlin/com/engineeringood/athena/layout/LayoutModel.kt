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
 * Stable identifier for one immutable layout-intent snapshot.
 *
 * Snapshot ids are owned by the governed projection/layout pipeline. They are not renderer, DOM, or
 * canvas session identifiers.
 */
@JvmInline
value class LayoutSnapshotId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one explainable layout-intent item inside a snapshot.
 */
@JvmInline
value class LayoutIntentId(val value: String) {
    override fun toString(): String = value
}

/**
 * Canonical occurrence identity for one subject representation inside a layout-intent snapshot.
 */
@JvmInline
value class LayoutOccurrenceId(val value: String) {
    override fun toString(): String = value
}

/**
 * Source span carried with layout intent so IDE reveal and audit paths stay tied to authored input.
 */
data class LayoutSourceSpan(
    val sourceUnitId: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
) {
    init {
        require(sourceUnitId.isNotBlank()) { "Layout source span requires a source unit id." }
        require(startLine > 0) { "Layout source span start line must be positive." }
        require(startColumn > 0) { "Layout source span start column must be positive." }
        require(endLine >= startLine) { "Layout source span end line must not precede start line." }
        require(endColumn > 0) { "Layout source span end column must be positive." }
        require(endLine > startLine || endColumn >= startColumn) {
            "Layout source span end column must not precede start column on the same line."
        }
    }
}

/**
 * First schematic-role vocabulary used by M21 layout intent before solved placement exists.
 */
enum class SchematicLayoutRole {
    POWER_SOURCE,
    PROTECTION,
    CONTROLLER,
    HMI,
    TERMINAL,
    LOAD,
    CONDUCTOR,
    ANNOTATION,
}

/**
 * Preferred schematic sheet zone for one layout-intent item.
 */
enum class SchematicLayoutZone {
    POWER,
    CONTROL,
    TERMINAL,
    LOAD,
    ANNOTATION,
}

/**
 * Explainable alignment preference before a layout strategy solves coordinates.
 */
enum class LayoutAlignment {
    LEFT_TO_RIGHT,
    TOP_TO_BOTTOM,
    ROW,
    COLUMN,
}

/**
 * Stable priority vocabulary for deterministic layout-intent ordering and later strategy decisions.
 */
enum class LayoutPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW,
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
 * Explainable relationship constraint between layout-intent items before solved layout facts exist.
 */
data class LayoutIntentRelationshipConstraint(
    val relationship: SchematicLayoutRelationship,
    val targetIntentId: LayoutIntentId,
    val axis: LayoutAxis? = null,
) {
    internal fun stableKey(): String = listOf(
        relationship.name,
        targetIntentId.value,
        axis?.name.orEmpty(),
    ).joinToString(separator = "|")
}

/**
 * Schematic relationship vocabulary used by layout intent. These are constraints, not route facts.
 */
enum class SchematicLayoutRelationship {
    BEFORE,
    AFTER,
    NEAR,
    ALIGNED_WITH,
    GROUPED_WITH,
    CONNECTS_TO,
}

/**
 * Stable identifier for one governed layout constraint.
 */
@JvmInline
value class LayoutConstraintId(val value: String) {
    override fun toString(): String = value
}

/**
 * Canonical subject identity carried by one layout constraint.
 *
 * The subject binds optimization input back to semantic, occurrence, sheet, view, snapshot, and
 * source identities. It deliberately does not carry coordinates or renderer state.
 */
data class LayoutConstraintSubject(
    val intentId: LayoutIntentId,
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val sheetId: String? = null,
    val viewId: String? = null,
    val sourceSpan: LayoutSourceSpan? = null,
)

/**
 * M22 constraint vocabulary used between layout intent and solved layout facts.
 */
enum class LayoutConstraintKind {
    NEAR,
    BELOW,
    ALIGNED_WITH,
    GROUPED_WITH,
    PREFERRED_ZONE,
    PRESERVE_ORDER,
    ROUTE_LANE_PREFERENCE,
}

/**
 * Basic schematic route-lane preference. This is not physical routing.
 */
enum class SchematicRouteLanePreference {
    HORIZONTAL_FIRST,
    VERTICAL_FIRST,
    DIRECT,
}

/**
 * One governed layout constraint used as optimization input.
 *
 * Constraints express engineering presentation relationships and preferences. Solvers may turn them
 * into coordinates later, but authored constraints remain relationship/zone/lane based.
 */
data class LayoutConstraint(
    val constraintId: LayoutConstraintId,
    val kind: LayoutConstraintKind,
    val subject: LayoutConstraintSubject,
    val target: LayoutConstraintSubject? = null,
    val axis: LayoutAxis? = null,
    val zone: SchematicLayoutZone? = null,
    val orderedSubjects: List<LayoutConstraintSubject> = emptyList(),
    val routeLanePreference: SchematicRouteLanePreference? = null,
    val snapshotId: LayoutSnapshotId? = null,
) {
    internal fun stableKey(): String = listOf(
        kind.ordinal.toString().padStart(2, '0'),
        constraintId.value,
        subject.intentId.value,
        target?.intentId?.value.orEmpty(),
        axis?.name.orEmpty(),
        zone?.name.orEmpty(),
        routeLanePreference?.name.orEmpty(),
        orderedSubjects.joinToString(separator = ",") { item -> item.intentId.value },
    ).joinToString(separator = "|")

    internal fun withSnapshot(snapshotId: LayoutSnapshotId): LayoutConstraint = copy(snapshotId = snapshotId)

    companion object {
        fun near(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            target: LayoutConstraintSubject,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.NEAR,
            subject = subject,
            target = target,
        )

        fun below(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            target: LayoutConstraintSubject,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.BELOW,
            subject = subject,
            target = target,
            axis = LayoutAxis.VERTICAL,
        )

        fun alignedWith(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            target: LayoutConstraintSubject,
            axis: LayoutAxis? = null,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.ALIGNED_WITH,
            subject = subject,
            target = target,
            axis = axis,
        )

        fun groupedWith(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            target: LayoutConstraintSubject,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.GROUPED_WITH,
            subject = subject,
            target = target,
        )

        fun preferredZone(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            zone: SchematicLayoutZone,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.PREFERRED_ZONE,
            subject = subject,
            zone = zone,
        )

        fun preserveOrder(
            constraintId: LayoutConstraintId,
            subjects: List<LayoutConstraintSubject>,
        ): LayoutConstraint {
            require(subjects.isNotEmpty()) { "Preserve-order constraints require at least one subject." }
            return LayoutConstraint(
                constraintId = constraintId,
                kind = LayoutConstraintKind.PRESERVE_ORDER,
                subject = subjects.first(),
                orderedSubjects = subjects,
            )
        }

        fun routeLanePreference(
            constraintId: LayoutConstraintId,
            subject: LayoutConstraintSubject,
            target: LayoutConstraintSubject,
            lane: SchematicRouteLanePreference,
        ): LayoutConstraint = LayoutConstraint(
            constraintId = constraintId,
            kind = LayoutConstraintKind.ROUTE_LANE_PREFERENCE,
            subject = subject,
            target = target,
            routeLanePreference = lane,
        )
    }
}

/**
 * Immutable, ordered constraint snapshot consumed by M22 layout optimization.
 */
data class LayoutConstraintSnapshot(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val constraints: List<LayoutConstraint>,
) {
    companion object {
        fun canonical(
            snapshotId: LayoutSnapshotId,
            family: ElectricalProjectionFamily,
            constraints: List<LayoutConstraint>,
        ): LayoutConstraintSnapshot = LayoutConstraintSnapshot(
            snapshotId = snapshotId,
            family = family,
            constraints = constraints
                .map { constraint -> constraint.withSnapshot(snapshotId) }
                .sortedBy(LayoutConstraint::stableKey),
        )
    }
}

/**
 * One explainable schematic layout-intent item anchored to canonical subject and occurrence identity.
 *
 * This is pre-solver intent. It deliberately avoids coordinates, CSS, DOM, canvas interaction state,
 * route segments, label placement, or adapter output.
 */
data class LayoutIntentItem(
    val intentId: LayoutIntentId,
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val role: SchematicLayoutRole,
    val preferredZone: SchematicLayoutZone,
    val priority: LayoutPriority = LayoutPriority.NORMAL,
    val alignment: LayoutAlignment? = null,
    val relationshipConstraints: List<LayoutIntentRelationshipConstraint> = emptyList(),
    val sourceSpan: LayoutSourceSpan? = null,
)

/**
 * Immutable, ordered layout-intent snapshot derived before any M21 layout strategy solves facts.
 */
data class LayoutIntentSnapshot(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val items: List<LayoutIntentItem>,
    val relationshipConstraints: List<LayoutIntentRelationshipConstraint> = emptyList(),
) {
    companion object {
        fun canonical(
            snapshotId: LayoutSnapshotId,
            family: ElectricalProjectionFamily,
            items: List<LayoutIntentItem>,
            relationshipConstraints: List<LayoutIntentRelationshipConstraint> = emptyList(),
        ): LayoutIntentSnapshot = LayoutIntentSnapshot(
            snapshotId = snapshotId,
            family = family,
            items = items.sortedBy { item -> item.intentId.value },
            relationshipConstraints = relationshipConstraints.sortedBy(LayoutIntentRelationshipConstraint::stableKey),
        )
    }
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
