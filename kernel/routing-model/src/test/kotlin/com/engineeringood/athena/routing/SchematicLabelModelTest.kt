package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchematicLabelModelTest {
    @Test
    fun `rule based schematic label strategy preserves subject and route identity`() {
        val route = sampleRouteFact()
        val result = RuleBasedSchematicLabelStrategy().solve(
            SchematicLabelSnapshot(
                snapshotId = route.snapshotId,
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(
                    subjectLabelRequest(),
                    routeLabelRequest(route),
                    crossReferenceRequest(),
                ),
            ),
        )

        assertEquals(route.snapshotId, result.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, result.family)
        assertEquals(
            listOf(
                SchematicLabelId("label:device/plc-1"),
                SchematicLabelId("label:route/sensor-to-plc"),
                SchematicLabelId("label:xref/plc-1"),
            ),
            result.labelFacts.map(SchematicLabelFact::labelId),
        )

        val device = result.labelFacts.single { fact -> fact.kind == SchematicLabelKind.DEVICE_NAME }
        assertEquals("PLC1", device.text)
        assertEquals(StableSemanticIdentity("component:plc-1"), device.anchor.subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:plc-1"), device.anchor.occurrenceId)

        val routeLabel = result.labelFacts.single { fact -> fact.kind == SchematicLabelKind.ROUTE_NAME }
        assertEquals(route.routeId, routeLabel.anchor.routeId)
        assertEquals(route.source.endpointId, routeLabel.anchor.endpointId)

        val crossReference = result.labelFacts.single { fact -> fact.kind == SchematicLabelKind.CROSS_REFERENCE }
        assertEquals(StableSemanticIdentity("component:plc-1"), crossReference.anchor.subjectId)
    }

    @Test
    fun `label placements avoid own subject anchor and primary route segment`() {
        val route = sampleRouteFact()
        val result = RuleBasedSchematicLabelStrategy().solve(
            SchematicLabelSnapshot(
                snapshotId = route.snapshotId,
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(subjectLabelRequest(), routeLabelRequest(route)),
            ),
        )

        val subjectLabel = result.labelFacts.single { fact -> fact.kind == SchematicLabelKind.DEVICE_NAME }
        assertFalse(subjectLabel.placement.origin == subjectLabel.anchor.point)

        val routeLabel = result.labelFacts.single { fact -> fact.kind == SchematicLabelKind.ROUTE_NAME }
        val segment = requireNotNull(routeLabel.anchor.routeSegment)
        if (segment.orientation == SchematicRouteSegmentOrientation.HORIZONTAL) {
            assertFalse(routeLabel.placement.origin.y == segment.start.y)
        } else {
            assertFalse(routeLabel.placement.origin.x == segment.start.x)
        }
    }

    @Test
    fun `label facts remain deterministic across repeated runs`() {
        val route = sampleRouteFact()
        val snapshot = SchematicLabelSnapshot(
            snapshotId = route.snapshotId,
            family = ElectricalProjectionFamily.SCHEMATIC,
            requests = listOf(routeLabelRequest(route), crossReferenceRequest(), subjectLabelRequest()),
        )
        val strategy = RuleBasedSchematicLabelStrategy()

        val first = strategy.solve(snapshot)
        val second = strategy.solve(snapshot)

        assertEquals(first, second)
    }

    @Test
    fun `label strategy rejects duplicate label ids`() {
        val snapshot = SchematicLabelSnapshot(
            snapshotId = LayoutSnapshotId("snapshot:m21:labels"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            requests = listOf(subjectLabelRequest(), subjectLabelRequest(text = "PLC duplicate")),
        )

        assertFailsWith<IllegalArgumentException> {
            RuleBasedSchematicLabelStrategy().solve(snapshot)
        }
    }

    @Test
    fun `label facts make acceptance subjects identifiable`() {
        val route = sampleRouteFact()
        val result = RuleBasedSchematicLabelStrategy().solve(
            SchematicLabelSnapshot(
                snapshotId = route.snapshotId,
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(
                    subjectLabelRequest(
                        labelId = SchematicLabelId("label:device/power"),
                        text = "24V PSU",
                        subjectId = StableSemanticIdentity("component:ps-1"),
                        occurrenceId = LayoutOccurrenceId("occurrence:schematic:ps-1"),
                    ),
                    subjectLabelRequest(
                        labelId = SchematicLabelId("label:device/protection"),
                        text = "QF1",
                        subjectId = StableSemanticIdentity("component:qf-1"),
                        occurrenceId = LayoutOccurrenceId("occurrence:schematic:qf-1"),
                    ),
                    subjectLabelRequest(),
                    subjectLabelRequest(
                        labelId = SchematicLabelId("label:terminal/xt-1"),
                        text = "XT1",
                        kind = SchematicLabelKind.TERMINAL_NAME,
                        subjectId = StableSemanticIdentity("component:xt-1"),
                        occurrenceId = LayoutOccurrenceId("occurrence:schematic:xt-1"),
                    ),
                    routeLabelRequest(route),
                ),
            ),
        )

        val visibleText = result.labelFacts.map(SchematicLabelFact::text).toSet()
        assertTrue(visibleText.containsAll(listOf("24V PSU", "QF1", "PLC1", "XT1", "SensorSignal")))
    }

    @Test
    fun `label model boundary avoids frontend and deferred scope contracts`() {
        val source = readModuleSource()
        val forbiddenTerms = listOf(
            "css",
            "dom",
            "canvas",
            "manual pixel",
            "cabinet",
            "physical",
            "desktop",
            "drag",
            "ai layout",
            "elk",
            "dagre",
            "graphviz",
        )

        forbiddenTerms.forEach { forbidden ->
            assertFalse(
                source.containsForbiddenScope(forbidden),
                "Story 3.3 label contract must not introduce `$forbidden` scope.",
            )
        }
    }

    private fun sampleRouteFact(): SchematicRouteFact {
        val routeSnapshot = SchematicRouteSnapshot(
            snapshotId = LayoutSnapshotId("snapshot:m21:labels"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            requests = listOf(
                SchematicRouteRequest(
                    routeId = SchematicRouteId("route:signal/sensor-to-plc"),
                    source = SchematicRouteEndpointRef(
                        endpointId = SchematicEndpointId("endpoint:sensor-s1/out"),
                        subjectId = StableSemanticIdentity("component:sensor-s1"),
                        occurrenceId = LayoutOccurrenceId("occurrence:schematic:sensor-s1"),
                        anchor = SchematicRoutePoint(x = 0, y = 40),
                    ),
                    target = SchematicRouteEndpointRef(
                        endpointId = SchematicEndpointId("endpoint:plc-1/input"),
                        subjectId = StableSemanticIdentity("component:plc-1"),
                        occurrenceId = LayoutOccurrenceId("occurrence:schematic:plc-1"),
                        anchor = SchematicRoutePoint(x = 320, y = 160),
                    ),
                ),
            ),
        )
        return RuleBasedSchematicRouteStrategy().solve(routeSnapshot).routeFacts.single()
    }

    private fun subjectLabelRequest(
        labelId: SchematicLabelId = SchematicLabelId("label:device/plc-1"),
        text: String = "PLC1",
        kind: SchematicLabelKind = SchematicLabelKind.DEVICE_NAME,
        subjectId: StableSemanticIdentity = StableSemanticIdentity("component:plc-1"),
        occurrenceId: LayoutOccurrenceId = LayoutOccurrenceId("occurrence:schematic:plc-1"),
    ): SchematicLabelRequest {
        return SchematicLabelRequest(
            labelId = labelId,
            kind = kind,
            text = text,
            anchor = SchematicLabelAnchor(
                subjectId = subjectId,
                occurrenceId = occurrenceId,
                point = SchematicRoutePoint(x = 320, y = 160),
            ),
        )
    }

    private fun routeLabelRequest(route: SchematicRouteFact): SchematicLabelRequest {
        return SchematicLabelRequest(
            labelId = SchematicLabelId("label:route/sensor-to-plc"),
            kind = SchematicLabelKind.ROUTE_NAME,
            text = "SensorSignal",
            anchor = SchematicLabelAnchor(
                routeId = route.routeId,
                endpointId = route.source.endpointId,
                routeSegment = route.segments.first(),
                point = route.segments.first().start,
            ),
        )
    }

    private fun crossReferenceRequest(): SchematicLabelRequest {
        return subjectLabelRequest(
            labelId = SchematicLabelId("label:xref/plc-1"),
            text = "-> Sheet 02",
            kind = SchematicLabelKind.CROSS_REFERENCE,
        )
    }

    private fun readModuleSource(): String {
        val repoRoot = generateSequence(java.nio.file.Path.of("").toAbsolutePath()) { path -> path.parent }
            .first { path -> java.nio.file.Files.exists(path.resolve("settings.gradle.kts")) }
        return java.nio.file.Files.readString(
            repoRoot.resolve("kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicLabelModel.kt"),
        )
    }

    private fun String.containsForbiddenScope(forbidden: String): Boolean {
        val lower = lowercase()
        return when (forbidden) {
            "elk",
            "dagre",
            "graphviz" -> Regex("\\b${Regex.escape(forbidden)}\\b").containsMatchIn(lower)
            else -> lower.contains(forbidden)
        }
    }
}
