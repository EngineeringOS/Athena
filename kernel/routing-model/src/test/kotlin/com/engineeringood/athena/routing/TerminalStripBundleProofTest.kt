package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TerminalStripBundleProofTest {
    @Test
    fun `terminal strip routes keep stable ordered lanes inside one semantic bundle`() {
        val bundleId = RouteBundleId("bundle:xt1:control")
        val result = AthenaRouteEngineV0().solve(
            AthenaRouteEngineInput(
                snapshotId = LayoutSnapshotId("snapshot:m24:terminal-strip"),
                layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
                componentBounds = listOf(
                    componentBounds("component:PLC1", x = 220, y = 140, width = 100, height = 180),
                    componentBounds("component:XT1", x = 520, y = 140, width = 80, height = 180),
                ),
                requests = listOf(
                    request("03", bundleId, terminalY = 260),
                    request("01", bundleId, terminalY = 180),
                    request("02", bundleId, terminalY = 220),
                ),
            ),
        )
        val bundle = RouteBundleFact.canonical(bundleId, result.routeFacts)

        assertEquals(listOf(SchematicRouteLane(0), SchematicRouteLane(1), SchematicRouteLane(2)), result.routeFacts.map(RouteFact::lane))
        assertEquals(
            listOf(ElectricalPortId("XT1.1"), ElectricalPortId("XT1.2"), ElectricalPortId("XT1.3")),
            result.routeFacts.map { fact -> fact.target.portId },
        )
        assertEquals(bundleId, bundle.bundleId)
        assertEquals(result.routeFacts.map(RouteFact::routeId), bundle.orderedRouteIds)
        assertEquals(
            listOf("XT1.1", "XT1.2", "XT1.3"),
            result.routeFacts.flatMap { fact -> fact.labels.map(RouteLabelFact::text) },
        )
        result.routeFacts.flatMap(RouteFact::labels).forEach { label ->
            assertFalse(
                result.routeFacts.any { fact -> fact.source.subjectId == StableSemanticIdentity("component:PLC1") } &&
                    listOf(
                        SchematicLabelSubjectBounds(SchematicRoutePoint(220, 140), 100, 180),
                        SchematicLabelSubjectBounds(SchematicRoutePoint(520, 140), 80, 180),
                    ).any { bounds -> bounds.contains(label.placement.origin) },
                "Route label `${label.text}` must not be placed over a component body.",
            )
        }
    }

    private fun componentBounds(
        subjectId: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): SchematicComponentBounds {
        return SchematicComponentBounds(
            subjectId = StableSemanticIdentity(subjectId),
            occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
            topLeft = SchematicRoutePoint(x = x, y = y),
            width = width,
            height = height,
        )
    }

    private fun request(
        terminalNumber: String,
        bundleId: RouteBundleId,
        terminalY: Int,
    ): AthenaRouteRequest {
        val source = anchor("anchor:PLC1:DO$terminalNumber", "component:PLC1", "DO$terminalNumber", TerminalSide.RIGHT, 320, terminalY)
        val target = anchor("anchor:XT1:$terminalNumber", "component:XT1", "XT1.${terminalNumber.toInt()}", TerminalSide.LEFT, 520, terminalY)
        val connectionId = ElectricalConnectionId("connection:PLC1.DO$terminalNumber->XT1.$terminalNumber")
        return AthenaRouteRequest(
            routeId = SchematicRouteId("route:xt1:${terminalNumber.toInt()}"),
            bundleId = bundleId,
            connectionIntent = ElectricalConnectionIntent(
                connectionId = connectionId,
                sourceSubjectId = source.subjectId,
                sourcePortId = source.portId,
                targetSubjectId = target.subjectId,
                targetPortId = target.portId,
                role = ElectricalConnectionRole.TERMINAL_TRANSITION,
                signalClass = ElectricalSignalClass.DIGITAL_OUTPUT,
            ),
            sourceAnchor = source,
            targetAnchor = target,
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:${connectionId.value}:terminal-order"),
                    kind = RouteConstraintKind.TERMINAL_ORDER,
                    connectionId = connectionId,
                ),
            ),
        )
    }

    private fun anchor(
        anchorId: String,
        subjectId: String,
        portId: String,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId(anchorId),
            subjectId = StableSemanticIdentity(subjectId),
            occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
            portId = ElectricalPortId(portId),
            portRole = ElectricalPortRole.TERMINAL,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
            policySource = "m24:schematic-default",
        )
    }
}
