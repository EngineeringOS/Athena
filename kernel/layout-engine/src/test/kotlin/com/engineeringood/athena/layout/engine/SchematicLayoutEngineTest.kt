package com.engineeringood.athena.layout.engine

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutAlignment
import com.engineeringood.athena.layout.LayoutConstraint
import com.engineeringood.athena.layout.LayoutConstraintId
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutConstraintSubject
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
import kotlin.test.assertTrue

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
            listOf(
                "intent:power/ps-1",
                "intent:protection/qf-1",
                "intent:control/plc-1",
                "intent:terminal/xt-1",
                "intent:load/m-1",
            ),
            first.placementFacts.map { fact -> fact.intentId.value },
        )
        assertEquals(SchematicLayoutPoint(x = 0, y = 0), first.placementFacts[0].position)
        assertEquals(SchematicLayoutPoint(x = 0, y = 120), first.placementFacts[1].position)
        assertEquals(SchematicLayoutPoint(x = 240, y = 0), first.placementFacts[2].position)
        assertEquals(SchematicLayoutPoint(x = 480, y = 0), first.placementFacts[3].position)
        assertEquals(SchematicLayoutPoint(x = 720, y = 0), first.placementFacts[4].position)
    }

    @Test
    fun `rule based schematic strategy emits identifiable region facts`() {
        val snapshot = sampleIntentSnapshot()
        val result = RuleBasedSchematicLayoutStrategy().solve(snapshot)

        assertEquals(
            listOf(
                SchematicLayoutZone.POWER,
                SchematicLayoutZone.CONTROL,
                SchematicLayoutZone.TERMINAL,
                SchematicLayoutZone.LOAD,
            ),
            result.regionFacts.map { fact -> fact.zone },
        )

        val power = result.regionFacts.single { fact -> fact.zone == SchematicLayoutZone.POWER }
        assertEquals(SchematicRegionId("region:schematic:power"), power.regionId)
        assertEquals(snapshot.snapshotId, power.snapshotId)
        assertEquals(
            listOf(SchematicLayoutRole.POWER_SOURCE, SchematicLayoutRole.PROTECTION),
            power.roles,
        )
        assertEquals(
            listOf(LayoutIntentId("intent:power/ps-1"), LayoutIntentId("intent:protection/qf-1")),
            power.intentIds,
        )
        assertEquals(
            listOf(
                LayoutOccurrenceId("occurrence:schematic:ps-1"),
                LayoutOccurrenceId("occurrence:schematic:qf-1"),
            ),
            power.occurrenceIds,
        )
        assertEquals(SchematicLayoutPoint(x = 0, y = 0), power.bounds.origin)
        assertEquals(SchematicLayoutSize(width = 160, height = 216), power.bounds.size)

        val control = result.regionFacts.single { fact -> fact.zone == SchematicLayoutZone.CONTROL }
        assertEquals(listOf(SchematicLayoutRole.CONTROLLER), control.roles)
        assertEquals(listOf(LayoutIntentId("intent:control/plc-1")), control.intentIds)

        val terminal = result.regionFacts.single { fact -> fact.zone == SchematicLayoutZone.TERMINAL }
        assertEquals(listOf(SchematicLayoutRole.TERMINAL), terminal.roles)

        val load = result.regionFacts.single { fact -> fact.zone == SchematicLayoutZone.LOAD }
        assertEquals(listOf(SchematicLayoutRole.LOAD), load.roles)
    }

    @Test
    fun `region facts remain deterministic across repeated runs`() {
        val snapshot = sampleIntentSnapshot()
        val strategy = RuleBasedSchematicLayoutStrategy()

        val first = strategy.solve(snapshot).regionFacts
        val second = strategy.solve(snapshot).regionFacts

        assertEquals(first, second)
    }

    @Test
    fun `region facts are explainable from layout intent roles and zones`() {
        val snapshot = sampleIntentSnapshot()
        val result = RuleBasedSchematicLayoutStrategy().solve(snapshot)
        val intentById = snapshot.items.associateBy(LayoutIntentItem::intentId)

        result.regionFacts.forEach { region ->
            assertTrue(region.intentIds.isNotEmpty())
            region.intentIds.forEach { intentId ->
                val item = requireNotNull(intentById[intentId])
                assertEquals(item.preferredZone, region.zone)
                assertTrue(
                    region.roles.contains(item.role),
                    "Region ${region.regionId} should expose role ${item.role} from layout intent.",
                )
            }
        }
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
            listOf(
                "intent:control/plc-1",
                "intent:load/m-1",
                "intent:power/ps-1",
                "intent:protection/qf-1",
                "intent:terminal/xt-1",
            ),
            normalized.placementFacts.map { fact -> fact.intentId.value },
        )
        assertEquals(
            strategyResult.placementFacts.map { fact -> fact.subjectId }.toSet(),
            normalized.placementFacts.map { fact -> fact.subjectId }.toSet(),
        )
        assertEquals(strategyResult.regionFacts, normalized.regionFacts)
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
    fun `subordinate helper proposal must reference each layout intent exactly once`() {
        val snapshot = sampleIntentSnapshot()
        val facts = RuleBasedSchematicLayoutStrategy().solve(snapshot).placementFacts
        val duplicateFacts = facts + facts.first()

        assertFailsWith<IllegalArgumentException> {
            SchematicLayoutHelperNormalizer().normalize(
                snapshot = snapshot,
                proposal = SchematicLayoutHelperProposal(
                    helperId = LayoutHelperAdapterId("helper:local-rule-check"),
                    snapshotId = snapshot.snapshotId,
                    placementFacts = duplicateFacts,
                ),
            )
        }
    }

    @Test
    fun `subordinate helper proposal must provide valid sheet geometry`() {
        val snapshot = sampleIntentSnapshot()
        val facts = RuleBasedSchematicLayoutStrategy().solve(snapshot).placementFacts
        val badFacts = facts.mapIndexed { index, fact ->
            if (index == 0) {
                fact.copy(size = SchematicLayoutSize(width = 0, height = fact.size.height))
            } else {
                fact
            }
        }

        assertFailsWith<IllegalArgumentException> {
            SchematicLayoutHelperNormalizer().normalize(
                snapshot = snapshot,
                proposal = SchematicLayoutHelperProposal(
                    helperId = LayoutHelperAdapterId("helper:local-rule-check"),
                    snapshotId = snapshot.snapshotId,
                    placementFacts = badFacts,
                ),
            )
        }
    }

    @Test
    fun `optimization boundary canonicalizes inputs and emits stable Athena layout facts`() {
        val snapshot = sampleIntentSnapshot()
        val byIntent = snapshot.items.associateBy(LayoutIntentItem::intentId)
        val controller = byIntent.getValue(LayoutIntentId("intent:control/plc-1")).toConstraintSubject("sheet:m22")
        val terminal = byIntent.getValue(LayoutIntentId("intent:terminal/xt-1")).toConstraintSubject("sheet:m22")
        val protection = byIntent.getValue(LayoutIntentId("intent:protection/qf-1")).toConstraintSubject("sheet:m22")
        val constraints = LayoutConstraintSnapshot.canonical(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            constraints = listOf(
                LayoutConstraint.groupedWith(LayoutConstraintId("constraint:group:plc-xt"), terminal, controller),
                LayoutConstraint.alignedWith(LayoutConstraintId("constraint:align:qf-plc"), protection, controller),
                LayoutConstraint.near(LayoutConstraintId("constraint:near:plc-xt"), controller, terminal),
            ).reversed(),
        )
        val existingFacts = RuleBasedSchematicLayoutStrategy().solve(snapshot).placementFacts.reversed()
        val input = SchematicLayoutOptimizationInput(
            intentSnapshot = snapshot.copy(items = snapshot.items.reversed()),
            constraintSnapshot = constraints.copy(constraints = constraints.constraints.reversed()),
            existingPlacementFacts = existingFacts,
        )
        val optimizer = RuleBasedSchematicLayoutOptimizer()

        val first = optimizer.optimize(input)
        val second = optimizer.optimize(input)

        assertEquals(first, second)
        assertEquals(snapshot.snapshotId, first.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, first.family)
        assertEquals(
            listOf(
                "intent:power/ps-1",
                "intent:protection/qf-1",
                "intent:control/plc-1",
                "intent:terminal/xt-1",
                "intent:load/m-1",
            ),
            first.placementFacts.map { fact -> fact.intentId.value },
        )
        assertEquals(
            listOf(
                "constraint:align:qf-plc",
                "constraint:group:plc-xt",
                "constraint:near:plc-xt",
            ),
            first.appliedConstraintIds.map { id -> id.value },
        )
        assertEquals(first.regionFacts, RuleBasedSchematicLayoutStrategy().solve(snapshot).regionFacts)
    }

    @Test
    fun `optimization applies preferred zone constraints and emits governed grouping facts`() {
        val hmi = LayoutIntentItem(
            intentId = LayoutIntentId("intent:hmi/hmi-1"),
            subjectId = StableSemanticIdentity("component:hmi-1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:hmi-1"),
            role = SchematicLayoutRole.HMI,
            preferredZone = SchematicLayoutZone.ANNOTATION,
            priority = LayoutPriority.NORMAL,
        )
        val snapshot = LayoutIntentSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m22:grouping"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = sampleIntentSnapshot().items + hmi,
        )
        val byIntent = snapshot.items.associateBy(LayoutIntentItem::intentId)
        val controller = byIntent.getValue(LayoutIntentId("intent:control/plc-1")).toConstraintSubject("sheet:m22")
        val hmiSubject = byIntent.getValue(LayoutIntentId("intent:hmi/hmi-1")).toConstraintSubject("sheet:m22")
        val constraints = LayoutConstraintSnapshot.canonical(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            constraints = listOf(
                LayoutConstraint.preferredZone(
                    constraintId = LayoutConstraintId("constraint:zone:hmi-control"),
                    subject = hmiSubject,
                    zone = SchematicLayoutZone.CONTROL,
                ),
                LayoutConstraint.groupedWith(
                    constraintId = LayoutConstraintId("constraint:group:hmi-plc"),
                    subject = hmiSubject,
                    target = controller,
                ),
            ).reversed(),
        )

        val result = RuleBasedSchematicLayoutOptimizer().optimize(
            SchematicLayoutOptimizationInput(
                intentSnapshot = snapshot,
                constraintSnapshot = constraints,
            ),
        )
        val hmiFact = result.placementFacts.single { fact -> fact.intentId == hmi.intentId }
        val group = result.groupFacts.single()

        assertEquals(SchematicLayoutZone.CONTROL, hmiFact.preferredZone)
        assertEquals(SchematicLayoutPoint(x = 240, y = 120), hmiFact.position)
        assertEquals(SchematicLayoutGroupId("group:constraint:group:hmi-plc"), group.groupId)
        assertEquals(listOf(LayoutConstraintId("constraint:group:hmi-plc")), group.constraintIds)
        assertEquals(
            listOf(LayoutIntentId("intent:control/plc-1"), LayoutIntentId("intent:hmi/hmi-1")),
            group.intentIds,
        )
        assertEquals(
            listOf(
                LayoutOccurrenceId("occurrence:schematic:plc-1"),
                LayoutOccurrenceId("occurrence:schematic:hmi-1"),
            ),
            group.occurrenceIds,
        )
        assertEquals(listOf(SchematicLayoutRole.CONTROLLER, SchematicLayoutRole.HMI), group.roles)
        assertEquals(SchematicLayoutZone.CONTROL, group.zone)
    }

    @Test
    fun `experimental ELK adapter normalizes output into Athena facts and falls back when disabled`() {
        val snapshot = sampleIntentSnapshot()
        val constraints = LayoutConstraintSnapshot.canonical(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            constraints = emptyList(),
        )
        val input = SchematicLayoutOptimizationInput(
            intentSnapshot = snapshot,
            constraintSnapshot = constraints,
        )
        val optimizer = ExperimentalElkSchematicLayoutOptimizer()
        val disabledOptimizer = ExperimentalElkSchematicLayoutOptimizer(
            adapter = ExperimentalElkSchematicLayoutAdapter(enabled = false),
        )

        val first = optimizer.optimize(input)
        val second = optimizer.optimize(input)
        val fallback = disabledOptimizer.optimize(input)

        assertEquals(first, second)
        assertEquals(LayoutHelperAdapterId("helper:experimental-elk"), first.helperId)
        assertEquals(
            RuleBasedSchematicLayoutOptimizer().optimize(input).placementFacts.map(SchematicPlacementFact::intentId).toSet(),
            first.placementFacts.map(SchematicPlacementFact::intentId).toSet(),
        )
        assertEquals(
            RuleBasedSchematicLayoutOptimizer().optimize(input).regionFacts,
            first.regionFacts,
        )
        assertEquals(first.placementFacts.map(SchematicPlacementFact::intentId).toSet(), fallback.placementFacts.map(SchematicPlacementFact::intentId).toSet())
        assertEquals(null, fallback.helperId)
    }

    @Test
    fun `layout engine boundary avoids renderer frontend final stack and deferred scope contracts`() {
        val source = readModuleSource()
        val forbiddenTerms = listOf(
            "css",
            "dom",
            "canvas",
            "routefact",
            "route fact",
            "conductor route",
            "labelfact",
            "label fact",
            "label avoidance",
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

    private fun LayoutIntentItem.toConstraintSubject(sheetId: String): LayoutConstraintSubject = LayoutConstraintSubject(
        intentId = intentId,
        subjectId = subjectId,
        occurrenceId = occurrenceId,
        sheetId = sheetId,
        viewId = "schematic-sheet",
        sourceSpan = sourceSpan,
    )

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
                LayoutIntentItem(
                    intentId = LayoutIntentId("intent:terminal/xt-1"),
                    subjectId = StableSemanticIdentity("component:xt-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:xt-1"),
                    role = SchematicLayoutRole.TERMINAL,
                    preferredZone = SchematicLayoutZone.TERMINAL,
                    priority = LayoutPriority.NORMAL,
                ),
                LayoutIntentItem(
                    intentId = LayoutIntentId("intent:load/m-1"),
                    subjectId = StableSemanticIdentity("component:m-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:m-1"),
                    role = SchematicLayoutRole.LOAD,
                    preferredZone = SchematicLayoutZone.LOAD,
                    priority = LayoutPriority.NORMAL,
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
