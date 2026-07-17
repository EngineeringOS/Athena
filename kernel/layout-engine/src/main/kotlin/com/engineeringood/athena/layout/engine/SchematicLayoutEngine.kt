package com.engineeringood.athena.layout.engine

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
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
        require(proposal.placementFacts.map(SchematicPlacementFact::intentId).toSet() == intentById.keys) {
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
        }
        return SchematicLayoutStrategyResult(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            placementFacts = proposal.placementFacts.sortedBy { fact -> fact.intentId.value },
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
        )
    }
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
