package com.engineeringood.athena.layout.engine

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutAlignment
import com.engineeringood.athena.layout.LayoutIntentId
import com.engineeringood.athena.layout.LayoutIntentItem
import com.engineeringood.athena.layout.LayoutIntentSnapshot
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutPriority
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.SchematicLayoutRole
import com.engineeringood.athena.layout.SchematicLayoutZone
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SchematicLayoutEngineTest {
    @Test
    fun `rule based schematic strategy emits deterministic placement facts`() {
        val snapshot = sampleIntentSnapshot()
        val strategy = RuleBasedSchematicLayoutStrategy()

        val first = strategy.solve(snapshot)
        val second = strategy.solve(snapshot)

        assertEquals(first, second)
        assertEquals(snapshot.snapshotId, first.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, first.family)
        assertEquals(
            listOf("intent:power/ps-1", "intent:protection/qf-1", "intent:control/plc-1"),
            first.placementFacts.map { fact -> fact.intentId.value },
        )
        assertEquals(SchematicLayoutPoint(x = 0, y = 0), first.placementFacts[0].position)
        assertEquals(SchematicLayoutPoint(x = 0, y = 120), first.placementFacts[1].position)
        assertEquals(SchematicLayoutPoint(x = 240, y = 0), first.placementFacts[2].position)
    }

    @Test
    fun `placement facts preserve canonical identity from layout intent`() {
        val snapshot = sampleIntentSnapshot()
        val result = RuleBasedSchematicLayoutStrategy().solve(snapshot)
        val controller = result.placementFacts.single { fact -> fact.role == SchematicLayoutRole.CONTROLLER }

        assertEquals(LayoutIntentId("intent:control/plc-1"), controller.intentId)
        assertEquals(StableSemanticIdentity("component:plc-1"), controller.subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:plc-1"), controller.occurrenceId)
        assertEquals(snapshot.snapshotId, controller.snapshotId)
        assertEquals(SchematicLayoutZone.CONTROL, controller.preferredZone)
        assertEquals(LayoutSourceSpan("src/02-layout-intelligence-acceptance.athena", 20, 3, 25, 4), controller.sourceSpan)
    }

    @Test
    fun `rule based schematic strategy rejects non schematic snapshots`() {
        val nonSchematicSnapshot = sampleIntentSnapshot().copy(family = ElectricalProjectionFamily.DOCUMENTATION)

        assertFailsWith<IllegalArgumentException> {
            RuleBasedSchematicLayoutStrategy().solve(nonSchematicSnapshot)
        }
    }

    @Test
    fun `subordinate helper proposal normalizes into Athena owned placement facts`() {
        val snapshot = sampleIntentSnapshot()
        val strategyResult = RuleBasedSchematicLayoutStrategy().solve(snapshot)
        val reversedProposal = SchematicLayoutHelperProposal(
            helperId = LayoutHelperAdapterId("helper:local-rule-check"),
            snapshotId = snapshot.snapshotId,
            placementFacts = strategyResult.placementFacts.reversed(),
            notes = listOf("proposal remains subordinate"),
        )

        val normalized = SchematicLayoutHelperNormalizer().normalize(snapshot, reversedProposal)

        assertEquals(snapshot.snapshotId, normalized.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, normalized.family)
        assertEquals(
            listOf("intent:control/plc-1", "intent:power/ps-1", "intent:protection/qf-1"),
            normalized.placementFacts.map { fact -> fact.intentId.value },
        )
        assertEquals(
            strategyResult.placementFacts.map { fact -> fact.subjectId }.toSet(),
            normalized.placementFacts.map { fact -> fact.subjectId }.toSet(),
        )
    }

    @Test
    fun `subordinate helper proposal cannot replace canonical identity`() {
        val snapshot = sampleIntentSnapshot()
        val strategyResult = RuleBasedSchematicLayoutStrategy().solve(snapshot)
        val badFact = strategyResult.placementFacts.first().copy(
            subjectId = StableSemanticIdentity("component:wrong"),
        )

        assertFailsWith<IllegalArgumentException> {
            SchematicLayoutHelperNormalizer().normalize(
                snapshot = snapshot,
                proposal = SchematicLayoutHelperProposal(
                    helperId = LayoutHelperAdapterId("helper:local-rule-check"),
                    snapshotId = snapshot.snapshotId,
                    placementFacts = listOf(badFact),
                ),
            )
        }
    }

    @Test
    fun `subordinate helper proposal must cover every layout intent item`() {
        val snapshot = sampleIntentSnapshot()
        val incompleteFacts = RuleBasedSchematicLayoutStrategy().solve(snapshot).placementFacts.drop(1)

        assertFailsWith<IllegalArgumentException> {
            SchematicLayoutHelperNormalizer().normalize(
                snapshot = snapshot,
                proposal = SchematicLayoutHelperProposal(
                    helperId = LayoutHelperAdapterId("helper:local-rule-check"),
                    snapshotId = snapshot.snapshotId,
                    placementFacts = incompleteFacts,
                ),
            )
        }
    }

    @Test
    fun `layout engine boundary avoids renderer frontend final stack and deferred scope contracts`() {
        val source = readModuleSource()
        val forbiddenTerms = listOf(
            "css",
            "dom",
            "canvas",
            "routefact",
            "labelfact",
            "elk",
            "dagre",
            "graphviz",
            "cabinet",
            "physical",
            "desktop",
            "drag",
            "ai layout",
        )

        forbiddenTerms.forEach { forbidden ->
            assertFalse(
                source.lowercase().contains(forbidden),
                "Story 2.2 layout engine contract must not introduce `$forbidden` scope.",
            )
        }
    }

    private fun sampleIntentSnapshot(): LayoutIntentSnapshot {
        return LayoutIntentSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m21:strategy"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = listOf(
                LayoutIntentItem(
                    intentId = LayoutIntentId("intent:control/plc-1"),
                    subjectId = StableSemanticIdentity("component:plc-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:plc-1"),
                    role = SchematicLayoutRole.CONTROLLER,
                    preferredZone = SchematicLayoutZone.CONTROL,
                    priority = LayoutPriority.HIGH,
                    alignment = LayoutAlignment.TOP_TO_BOTTOM,
                    sourceSpan = LayoutSourceSpan("src/02-layout-intelligence-acceptance.athena", 20, 3, 25, 4),
                ),
                LayoutIntentItem(
                    intentId = LayoutIntentId("intent:protection/qf-1"),
                    subjectId = StableSemanticIdentity("component:qf-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:qf-1"),
                    role = SchematicLayoutRole.PROTECTION,
                    preferredZone = SchematicLayoutZone.POWER,
                    priority = LayoutPriority.HIGH,
                ),
                LayoutIntentItem(
                    intentId = LayoutIntentId("intent:power/ps-1"),
                    subjectId = StableSemanticIdentity("component:ps-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:ps-1"),
                    role = SchematicLayoutRole.POWER_SOURCE,
                    preferredZone = SchematicLayoutZone.POWER,
                    priority = LayoutPriority.CRITICAL,
                ),
            ),
        )
    }

    private fun readModuleSource(): String {
        val repoRoot = generateSequence(Path.of("").toAbsolutePath()) { path -> path.parent }
            .first { path -> Files.exists(path.resolve("settings.gradle.kts")) }
        return Files.readString(
            repoRoot.resolve("kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt"),
        )
    }
}
