package com.engineeringood.athena.layout.engine

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutConstraintId
import com.engineeringood.athena.layout.LayoutConstraintKind
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutIntentId
import com.engineeringood.athena.layout.LayoutIntentItem
import com.engineeringood.athena.layout.LayoutIntentSnapshot
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutPriority
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.SchematicLayoutRole
import com.engineeringood.athena.layout.SchematicLayoutZone

/**
 * Governed M22 input for schematic layout optimization.
 */
data class SchematicLayoutOptimizationInput(
    val intentSnapshot: LayoutIntentSnapshot,
    val constraintSnapshot: LayoutConstraintSnapshot,
    val existingPlacementFacts: List<SchematicPlacementFact> = emptyList(),
)

/**
 * Deterministic M22 optimization result expressed only as Athena-owned layout facts.
 */
data class SchematicLayoutOptimizationResult(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val placementFacts: List<SchematicPlacementFact>,
    val regionFacts: List<SchematicRegionFact> = emptyList(),
    val groupFacts: List<SchematicLayoutGroupFact> = emptyList(),
    val appliedConstraintIds: List<LayoutConstraintId> = emptyList(),
    val helperId: LayoutHelperAdapterId? = null,
)

/**
 * Boundary that turns governed intent and constraints into normalized Athena layout facts.
 */
interface SchematicLayoutOptimizer {
    fun optimize(input: SchematicLayoutOptimizationInput): SchematicLayoutOptimizationResult
}

/**
 * Strategy boundary that turns governed schematic layout intent into Athena-owned layout facts.
 */
interface SchematicLayoutStrategy {
    fun solve(snapshot: LayoutIntentSnapshot): SchematicLayoutStrategyResult
}

/**
 * Deterministic result emitted by a schematic layout strategy.
 */
data class SchematicLayoutStrategyResult(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val placementFacts: List<SchematicPlacementFact>,
    val regionFacts: List<SchematicRegionFact> = emptyList(),
)

/**
 * Stable sheet position owned by Athena layout facts.
 */
data class SchematicLayoutPoint(
    val x: Int,
    val y: Int,
)

/**
 * Stable sheet size owned by Athena layout facts.
 */
data class SchematicLayoutSize(
    val width: Int,
    val height: Int,
)

/**
 * Placement fact derived from one layout-intent item.
 *
 * Facts preserve upstream identity and remain independent from renderer-local state.
 */
data class SchematicPlacementFact(
    val intentId: LayoutIntentId,
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val snapshotId: LayoutSnapshotId,
    val role: SchematicLayoutRole,
    val preferredZone: SchematicLayoutZone,
    val position: SchematicLayoutPoint,
    val size: SchematicLayoutSize,
    val sourceSpan: LayoutSourceSpan? = null,
)

/** Stable schematic region identity owned by Athena layout facts and scoped by snapshot id. */
@JvmInline
value class SchematicRegionId(val value: String) {
    override fun toString(): String = value
}

/** Bounds of a coherent schematic region in sheet coordinates. */
data class SchematicRegionBounds(
    val origin: SchematicLayoutPoint,
    val size: SchematicLayoutSize,
)

/**
 * Grouping fact that makes engineering regions inspectable without downstream reconstruction.
 */
data class SchematicRegionFact(
    val regionId: SchematicRegionId,
    val snapshotId: LayoutSnapshotId,
    val zone: SchematicLayoutZone,
    val intentIds: List<LayoutIntentId>,
    val occurrenceIds: List<LayoutOccurrenceId>,
    val roles: List<SchematicLayoutRole>,
    val bounds: SchematicRegionBounds,
)

/** Stable schematic grouping identity owned by Athena layout facts and scoped by constraints. */
@JvmInline
value class SchematicLayoutGroupId(val value: String) {
    override fun toString(): String = value
}

/**
 * Grouping fact derived from governed constraints, not downstream inference.
 */
data class SchematicLayoutGroupFact(
    val groupId: SchematicLayoutGroupId,
    val snapshotId: LayoutSnapshotId,
    val constraintIds: List<LayoutConstraintId>,
    val intentIds: List<LayoutIntentId>,
    val occurrenceIds: List<LayoutOccurrenceId>,
    val roles: List<SchematicLayoutRole>,
    val zone: SchematicLayoutZone,
)

/**
 * Stable identifier for a subordinate layout helper.
 */
@JvmInline
value class LayoutHelperAdapterId(val value: String) {
    override fun toString(): String = value
}

/**
 * Proposal emitted by a subordinate helper. It is not renderer truth and not layout authority.
 */
data class SchematicLayoutHelperProposal(
    val helperId: LayoutHelperAdapterId,
    val snapshotId: LayoutSnapshotId,
    val placementFacts: List<SchematicPlacementFact>,
    val notes: List<String> = emptyList(),
)

/**
 * Normalizes subordinate helper proposals into Athena-owned schematic layout facts.
 */
class SchematicLayoutHelperNormalizer {
    fun normalize(
        snapshot: LayoutIntentSnapshot,
        proposal: SchematicLayoutHelperProposal,
    ): SchematicLayoutStrategyResult {
        require(snapshot.family == ElectricalProjectionFamily.SCHEMATIC) {
            "Schematic layout helper normalization only accepts schematic layout intent snapshots."
        }
        require(proposal.snapshotId == snapshot.snapshotId) {
            "Schematic layout helper proposals must target the active layout intent snapshot."
        }
        val intentById = snapshot.items.associateBy(LayoutIntentItem::intentId)
        val proposalIntentIds = proposal.placementFacts.map(SchematicPlacementFact::intentId)
        require(proposalIntentIds.size == proposalIntentIds.toSet().size) {
            "Schematic layout helper proposals must reference each layout intent item at most once."
        }
        require(proposalIntentIds.size == intentById.size && proposalIntentIds.toSet() == intentById.keys) {
            "Schematic layout helper proposals must cover every layout intent item exactly once."
        }
        proposal.placementFacts.forEach { fact ->
            val intent = requireNotNull(intentById[fact.intentId]) {
                "Schematic layout helper proposals must reference known layout intent ids."
            }
            require(fact.snapshotId == snapshot.snapshotId) {
                "Schematic layout helper placement facts must preserve snapshot identity."
            }
            require(fact.subjectId == intent.subjectId) {
                "Schematic layout helper placement facts must preserve canonical subject identity."
            }
            require(fact.occurrenceId == intent.occurrenceId) {
                "Schematic layout helper placement facts must preserve occurrence identity."
            }
            require(fact.role == intent.role) {
                "Schematic layout helper placement facts must preserve schematic role."
            }
            require(fact.preferredZone == intent.preferredZone) {
                "Schematic layout helper placement facts must preserve preferred schematic zone."
            }
            fact.requireValidGeometry()
        }
        val normalizedPlacementFacts = proposal.placementFacts.sortedBy { fact -> fact.intentId.value }
        return SchematicLayoutStrategyResult(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            placementFacts = normalizedPlacementFacts,
            regionFacts = buildRegionFacts(snapshot.snapshotId, normalizedPlacementFacts),
        )
    }
}

/**
 * Rule-based M22 optimizer that keeps optimization authority inside Athena layout facts.
 */
class RuleBasedSchematicLayoutOptimizer(
    private val strategy: SchematicLayoutStrategy = RuleBasedSchematicLayoutStrategy(),
) : SchematicLayoutOptimizer {
    override fun optimize(input: SchematicLayoutOptimizationInput): SchematicLayoutOptimizationResult {
        require(input.intentSnapshot.family == ElectricalProjectionFamily.SCHEMATIC) {
            "Rule-based schematic layout optimizer only accepts schematic layout intent snapshots."
        }
        require(input.constraintSnapshot.family == input.intentSnapshot.family) {
            "Layout constraints must target the same projection family as layout intent."
        }
        require(input.constraintSnapshot.snapshotId == input.intentSnapshot.snapshotId) {
            "Layout constraints must target the active layout intent snapshot."
        }
        input.existingPlacementFacts.forEach { fact ->
            require(fact.snapshotId == input.intentSnapshot.snapshotId) {
                "Existing placement facts must preserve the active layout snapshot identity."
            }
            fact.requireValidGeometry()
        }
        val canonicalIntent = LayoutIntentSnapshot.canonical(
            snapshotId = input.intentSnapshot.snapshotId,
            family = input.intentSnapshot.family,
            items = input.intentSnapshot.items,
            relationshipConstraints = input.intentSnapshot.relationshipConstraints,
        )
        val canonicalConstraints = LayoutConstraintSnapshot.canonical(
            snapshotId = input.constraintSnapshot.snapshotId,
            family = input.constraintSnapshot.family,
            constraints = input.constraintSnapshot.constraints,
        )
        val constrainedIntent = canonicalIntent.withPreferredZoneConstraints(canonicalConstraints)
        val solved = strategy.solve(constrainedIntent)
        return SchematicLayoutOptimizationResult(
            snapshotId = solved.snapshotId,
            family = solved.family,
            placementFacts = solved.placementFacts,
            regionFacts = solved.regionFacts,
            groupFacts = buildGroupFacts(solved.snapshotId, canonicalConstraints, solved.placementFacts),
            appliedConstraintIds = canonicalConstraints.constraints
                .map { constraint -> constraint.constraintId }
                .sortedBy(LayoutConstraintId::value),
        )
    }
}

/**
 * First deterministic strategy for M21. It groups intent by schematic zone and orders each group by
 * priority and stable intent id.
 */
class RuleBasedSchematicLayoutStrategy(
    private val zoneSpacing: Int = 240,
    private val itemSpacing: Int = 120,
) : SchematicLayoutStrategy {
    override fun solve(snapshot: LayoutIntentSnapshot): SchematicLayoutStrategyResult {
        require(snapshot.family == ElectricalProjectionFamily.SCHEMATIC) {
            "Rule-based schematic layout strategy only accepts schematic layout intent snapshots."
        }
        val orderedItems = snapshot.items.sortedWith(
            compareBy<LayoutIntentItem>(
                { item -> item.preferredZone.ordinal },
                { item -> item.priority.ordinal },
                { item -> item.intentId.value },
            ),
        )
        val indexByZone = mutableMapOf<SchematicLayoutZone, Int>()
        val facts = orderedItems.map { item ->
            val zoneIndex = item.preferredZone.ordinal
            val itemIndex = indexByZone.getOrDefault(item.preferredZone, 0)
            indexByZone[item.preferredZone] = itemIndex + 1
            SchematicPlacementFact(
                intentId = item.intentId,
                subjectId = item.subjectId,
                occurrenceId = item.occurrenceId,
                snapshotId = snapshot.snapshotId,
                role = item.role,
                preferredZone = item.preferredZone,
                position = SchematicLayoutPoint(
                    x = zoneIndex * zoneSpacing,
                    y = itemIndex * itemSpacing,
                ),
                size = item.role.defaultSize(),
                sourceSpan = item.sourceSpan,
            )
        }
        return SchematicLayoutStrategyResult(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            placementFacts = facts,
            regionFacts = buildRegionFacts(snapshot.snapshotId, facts),
        )
    }
}

private fun buildRegionFacts(
    snapshotId: LayoutSnapshotId,
    placementFacts: List<SchematicPlacementFact>,
): List<SchematicRegionFact> {
    placementFacts.forEach { fact -> fact.requireValidGeometry() }
    return placementFacts
        .groupBy(SchematicPlacementFact::preferredZone)
        .toSortedMap(compareBy { zone -> zone.ordinal })
        .map { (zone, zoneFacts) ->
            val orderedFacts = zoneFacts.sortedWith(
                compareBy<SchematicPlacementFact>(
                    { fact -> fact.position.y },
                    { fact -> fact.position.x },
                    { fact -> fact.intentId.value },
                    { fact -> fact.occurrenceId.value },
                    { fact -> fact.role.name },
                ),
            )
            SchematicRegionFact(
                regionId = SchematicRegionId("region:schematic:${zone.name.lowercase()}"),
                snapshotId = snapshotId,
                zone = zone,
                intentIds = orderedFacts.map(SchematicPlacementFact::intentId),
                occurrenceIds = orderedFacts.map(SchematicPlacementFact::occurrenceId),
                roles = orderedFacts.map(SchematicPlacementFact::role).distinct(),
                bounds = orderedFacts.toRegionBounds(),
            )
        }
}

private fun LayoutIntentSnapshot.withPreferredZoneConstraints(
    constraintSnapshot: LayoutConstraintSnapshot,
): LayoutIntentSnapshot {
    val preferredZones = constraintSnapshot.constraints
        .filter { constraint -> constraint.kind == LayoutConstraintKind.PREFERRED_ZONE && constraint.zone != null }
        .associate { constraint -> constraint.subject.intentId to requireNotNull(constraint.zone) }
    if (preferredZones.isEmpty()) {
        return this
    }
    return LayoutIntentSnapshot.canonical(
        snapshotId = snapshotId,
        family = family,
        items = items.map { item ->
            preferredZones[item.intentId]?.let { zone -> item.copy(preferredZone = zone) } ?: item
        },
        relationshipConstraints = relationshipConstraints,
    )
}

private fun buildGroupFacts(
    snapshotId: LayoutSnapshotId,
    constraintSnapshot: LayoutConstraintSnapshot,
    placementFacts: List<SchematicPlacementFact>,
): List<SchematicLayoutGroupFact> {
    val placementByIntent = placementFacts.associateBy(SchematicPlacementFact::intentId)
    return constraintSnapshot.constraints
        .filter { constraint -> constraint.kind == LayoutConstraintKind.GROUPED_WITH && constraint.target != null }
        .sortedBy { constraint -> constraint.constraintId.value }
        .mapNotNull { constraint ->
            val members = listOfNotNull(
                placementByIntent[constraint.subject.intentId],
                placementByIntent[requireNotNull(constraint.target).intentId],
            ).distinctBy(SchematicPlacementFact::intentId)
                .sortedWith(
                    compareBy<SchematicPlacementFact>(
                        { fact -> fact.preferredZone.ordinal },
                        { fact -> fact.position.y },
                        { fact -> fact.position.x },
                        { fact -> fact.intentId.value },
                    ),
                )
            if (members.size < 2) {
                null
            } else {
                SchematicLayoutGroupFact(
                    groupId = SchematicLayoutGroupId("group:${constraint.constraintId.value}"),
                    snapshotId = snapshotId,
                    constraintIds = listOf(constraint.constraintId),
                    intentIds = members.map(SchematicPlacementFact::intentId),
                    occurrenceIds = members.map(SchematicPlacementFact::occurrenceId),
                    roles = members.map(SchematicPlacementFact::role).distinct(),
                    zone = members.first().preferredZone,
                )
            }
        }
}

private fun List<SchematicPlacementFact>.toRegionBounds(): SchematicRegionBounds {
    val left = minOf { fact -> fact.position.x }
    val top = minOf { fact -> fact.position.y }
    val right = maxOf { fact -> fact.position.x.toLong() + fact.size.width.toLong() }.toIntExact()
    val bottom = maxOf { fact -> fact.position.y.toLong() + fact.size.height.toLong() }.toIntExact()
    return SchematicRegionBounds(
        origin = SchematicLayoutPoint(x = left, y = top),
        size = SchematicLayoutSize(width = right - left, height = bottom - top),
    )
}

private fun SchematicPlacementFact.requireValidGeometry() {
    require(position.x >= 0 && position.y >= 0) {
        "Schematic layout helper placement facts must use non-negative sheet coordinates."
    }
    require(size.width > 0 && size.height > 0) {
        "Schematic layout helper placement facts must use positive sheet sizes."
    }
    (position.x.toLong() + size.width.toLong()).toIntExact()
    (position.y.toLong() + size.height.toLong()).toIntExact()
}

private fun Long.toIntExact(): Int {
    require(this in Int.MIN_VALUE..Int.MAX_VALUE) {
        "Schematic layout geometry must stay within Int sheet coordinate bounds."
    }
    return toInt()
}

private fun SchematicLayoutRole.defaultSize(): SchematicLayoutSize {
    return when (this) {
        SchematicLayoutRole.POWER_SOURCE,
        SchematicLayoutRole.PROTECTION,
        SchematicLayoutRole.CONTROLLER,
        SchematicLayoutRole.HMI,
        SchematicLayoutRole.TERMINAL,
        SchematicLayoutRole.LOAD -> SchematicLayoutSize(width = 160, height = 96)
        SchematicLayoutRole.CONDUCTOR,
        SchematicLayoutRole.ANNOTATION -> SchematicLayoutSize(width = 120, height = 48)
    }
}
