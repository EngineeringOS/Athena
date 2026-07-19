package com.engineeringood.athena.routing

import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SchematicRouteIntentProjectorTest {
    @Test
    fun `semantic connections project into deterministic schematic route intent`() {
        val snapshotId = LayoutSnapshotId("snapshot:m24:semantic-route-intent")
        val layoutContext = SchematicRoutingLayoutContext(gridSize = 20)
        val intents = listOf(
            semanticIntent("connection:PLC1.DO1->HMI1.IN1", "PLC1", "DO1", "HMI1", "IN1", ElectricalSignalClass.DIGITAL_OUTPUT),
            semanticIntent("connection:PLC1.DO2->XT1.1", "PLC1", "DO2", "XT1", "1", ElectricalSignalClass.DIGITAL_OUTPUT),
            semanticIntent("connection:PS1.L+->QF1.L+", "PS1", "L+", "QF1", "L+", ElectricalSignalClass.POWER),
        ).reversed()
        val anchors = listOf(
            anchor("PLC1", "DO1", ElectricalPortRole.OUTPUT, TerminalSide.RIGHT, 320, 120),
            anchor("HMI1", "IN1", ElectricalPortRole.INPUT, TerminalSide.LEFT, 520, 120),
            anchor("PLC1", "DO2", ElectricalPortRole.OUTPUT, TerminalSide.RIGHT, 320, 180),
            anchor("XT1", "1", ElectricalPortRole.TERMINAL, TerminalSide.LEFT, 520, 180),
            anchor("PS1", "L+", ElectricalPortRole.POWER, TerminalSide.RIGHT, 120, 80),
            anchor("QF1", "L+", ElectricalPortRole.POWER, TerminalSide.LEFT, 280, 80),
        ).shuffled()

        val snapshot = SchematicRouteIntentProjector().project(
            snapshotId = snapshotId,
            viewId = "schematic",
            sheetId = "schematic/sheet/01-main",
            layoutContext = layoutContext,
            connectionIntents = intents,
            anchors = anchors,
        )

        assertEquals(snapshotId, snapshot.snapshotId)
        assertEquals("schematic", snapshot.viewId)
        assertEquals("schematic/sheet/01-main", snapshot.sheetId)
        assertSame(layoutContext, snapshot.layoutContext)
        assertEquals(
            listOf(
                ElectricalConnectionId("connection:PLC1.DO1->HMI1.IN1"),
                ElectricalConnectionId("connection:PLC1.DO2->XT1.1"),
                ElectricalConnectionId("connection:PS1.L+->QF1.L+"),
            ),
            snapshot.routeIntents.map { intent -> intent.connectionIntent.connectionId },
        )
        assertEquals(
            listOf(
                ElectricalPortId("PLC1.DO1") to ElectricalPortId("HMI1.IN1"),
                ElectricalPortId("PLC1.DO2") to ElectricalPortId("XT1.1"),
                ElectricalPortId("PS1.L+") to ElectricalPortId("QF1.L+"),
            ),
            snapshot.routeIntents.map { intent -> intent.connectionIntent.sourcePortId to intent.connectionIntent.targetPortId },
        )
        assertEquals(
            listOf(
                ElectricalConnectionRole.CONTROL_SIGNAL,
                ElectricalConnectionRole.TERMINAL_TRANSITION,
                ElectricalConnectionRole.POWER_FEED,
            ),
            snapshot.routeIntents.map { intent -> intent.connectionIntent.role },
        )
        assertEquals(
            listOf(
                SchematicRouteId("route:connection:PLC1.DO1->HMI1.IN1"),
                SchematicRouteId("route:connection:PLC1.DO2->XT1.1"),
                SchematicRouteId("route:connection:PS1.L+->QF1.L+"),
            ),
            snapshot.toEngineInput().requests.map(AthenaRouteRequest::routeId),
        )
        assertEquals(layoutContext, snapshot.toEngineInput().layoutContext)
    }

    private fun semanticIntent(
        connectionId: String,
        sourceSubject: String,
        sourcePort: String,
        targetSubject: String,
        targetPort: String,
        signalClass: ElectricalSignalClass,
    ): ElectricalConnectionIntent {
        val source = portRef(sourceSubject, sourcePort, SemanticPortDirection.OUTPUT, signalClass)
        val targetKind = if (targetSubject.startsWith("XT")) {
            ElectricalConnectionEndpointKind.TERMINAL
        } else {
            ElectricalConnectionEndpointKind.DEVICE
        }
        val target = portRef(targetSubject, targetPort, SemanticPortDirection.INPUT, signalClass, targetKind)
        return ElectricalConnectionIntentClassifier().classify(
            ElectricalConnectionIntentInput(
                connectionId = ElectricalConnectionId(connectionId),
                sourcePort = source,
                targetPort = target,
            ),
        )
    }

    private fun portRef(
        subject: String,
        port: String,
        direction: SemanticPortDirection,
        signalClass: ElectricalSignalClass,
        endpointKind: ElectricalConnectionEndpointKind = ElectricalConnectionEndpointKind.DEVICE,
    ): ElectricalConnectionPortRef {
        return ElectricalConnectionPortRef(
            subjectId = StableSemanticIdentity("component:$subject"),
            portSemanticId = StableSemanticIdentity("port:$subject.$port"),
            portId = ElectricalPortId("$subject.$port"),
            endpointKind = endpointKind,
            direction = direction,
            signalFamilyId = SemanticSignalFamilyId(signalClass.signalFamily()),
        )
    }

    private fun anchor(
        subject: String,
        port: String,
        role: ElectricalPortRole,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$subject:$port"),
            subjectId = StableSemanticIdentity("component:$subject"),
            occurrenceId = LayoutOccurrenceId("occurrence:component:$subject"),
            portId = ElectricalPortId("$subject.$port"),
            portRole = role,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
            policySource = "m24:schematic-default",
        )
    }

    private fun ElectricalSignalClass.signalFamily(): String {
        return when (this) {
            ElectricalSignalClass.POWER -> "24v"
            ElectricalSignalClass.GROUND -> "ground"
            ElectricalSignalClass.DIGITAL_INPUT -> "digital-input"
            ElectricalSignalClass.DIGITAL_OUTPUT -> "digital-output"
            ElectricalSignalClass.ANALOG_SIGNAL -> "analog-signal"
            ElectricalSignalClass.CONTROL -> "control"
            ElectricalSignalClass.UNKNOWN -> "unknown"
        }
    }
}
