package com.engineeringood.athena.routing

import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutSourceSpan
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ElectricalConnectionIntentClassifierTest {
    @Test
    fun `classifies digital output to input as control signal and preserves identities`() {
        val span = LayoutSourceSpan(
            sourceUnitId = "src/main.athena",
            startLine = 12,
            startColumn = 3,
            endLine = 12,
            endColumn = 27,
        )
        val intent = ElectricalConnectionIntentClassifier().classify(
            sampleInput(
                sourcePort = samplePort(
                    subjectId = StableSemanticIdentity("component:PLC1"),
                    portSemanticId = StableSemanticIdentity("port:PLC1.DO1"),
                    portId = ElectricalPortId("DO1"),
                    direction = SemanticPortDirection.OUTPUT,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
                targetPort = samplePort(
                    subjectId = StableSemanticIdentity("component:HMI1"),
                    portSemanticId = StableSemanticIdentity("port:HMI1.IN1"),
                    portId = ElectricalPortId("IN1"),
                    direction = SemanticPortDirection.INPUT,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
                sourceSpan = span,
            ),
        )

        assertEquals(ElectricalConnectionRole.CONTROL_SIGNAL, intent.role)
        assertEquals(ElectricalSignalClass.DIGITAL_OUTPUT, intent.signalClass)
        assertEquals(ElectricalConnectionId("connection:PLC1.DO1->HMI1.IN1"), intent.connectionId)
        assertEquals(StableSemanticIdentity("component:PLC1"), intent.sourceSubjectId)
        assertEquals(StableSemanticIdentity("port:PLC1.DO1"), intent.sourcePortSemanticId)
        assertEquals(StableSemanticIdentity("component:HMI1"), intent.targetSubjectId)
        assertEquals(StableSemanticIdentity("port:HMI1.IN1"), intent.targetPortSemanticId)
        assertEquals(span, intent.sourceSpan)
        assertFalse(intent.quality.isDegraded)
    }

    @Test
    fun `classifies power feed and load connection from signal and endpoint role`() {
        val classifier = ElectricalConnectionIntentClassifier()

        val powerIntent = classifier.classify(
            sampleInput(
                sourcePort = samplePort(
                    direction = SemanticPortDirection.OUTPUT,
                    signalFamilyId = SemanticSignalFamilyId("24v"),
                ),
                targetPort = samplePort(
                    direction = SemanticPortDirection.INPUT,
                    signalFamilyId = SemanticSignalFamilyId("24v"),
                ),
            ),
        )
        val loadIntent = classifier.classify(
            sampleInput(
                sourcePort = samplePort(
                    direction = SemanticPortDirection.OUTPUT,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
                targetPort = samplePort(
                    endpointKind = ElectricalConnectionEndpointKind.LOAD,
                    direction = SemanticPortDirection.INPUT,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
            ),
        )

        assertEquals(ElectricalConnectionRole.POWER_FEED, powerIntent.role)
        assertEquals(ElectricalSignalClass.POWER, powerIntent.signalClass)
        assertEquals(ElectricalConnectionRole.LOAD_CONNECTION, loadIntent.role)
        assertEquals(ElectricalSignalClass.DIGITAL_OUTPUT, loadIntent.signalClass)
    }

    @Test
    fun `classifies terminal transition before ordinary control routing`() {
        val intent = ElectricalConnectionIntentClassifier().classify(
            sampleInput(
                sourcePort = samplePort(
                    subjectId = StableSemanticIdentity("component:PLC1"),
                    portSemanticId = StableSemanticIdentity("port:PLC1.DO1"),
                    portId = ElectricalPortId("DO1"),
                    direction = SemanticPortDirection.OUTPUT,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
                targetPort = samplePort(
                    subjectId = StableSemanticIdentity("component:XT1"),
                    portSemanticId = StableSemanticIdentity("port:XT1.1"),
                    portId = ElectricalPortId("1"),
                    endpointKind = ElectricalConnectionEndpointKind.TERMINAL,
                    direction = SemanticPortDirection.PASSIVE,
                    signalFamilyId = SemanticSignalFamilyId("digital"),
                ),
            ),
        )

        assertEquals(ElectricalConnectionRole.TERMINAL_TRANSITION, intent.role)
        assertEquals(ElectricalSignalClass.DIGITAL_OUTPUT, intent.signalClass)
    }

    @Test
    fun `degrades unsupported signal and direction combinations explicitly`() {
        val intent = ElectricalConnectionIntentClassifier().classify(
            sampleInput(
                sourcePort = samplePort(
                    direction = SemanticPortDirection.PASSIVE,
                    signalFamilyId = SemanticSignalFamilyId("vendor-private"),
                ),
                targetPort = samplePort(
                    direction = SemanticPortDirection.PASSIVE,
                    signalFamilyId = SemanticSignalFamilyId("vendor-private"),
                ),
            ),
        )

        assertEquals(ElectricalConnectionRole.UNKNOWN, intent.role)
        assertEquals(ElectricalSignalClass.UNKNOWN, intent.signalClass)
        assertEquals(ElectricalConnectionIntentQualityState.DEGRADED, intent.quality.state)
        assertContains(intent.quality.message.orEmpty(), "Unsupported")
    }

    private fun sampleInput(
        connectionId: ElectricalConnectionId = ElectricalConnectionId("connection:PLC1.DO1->HMI1.IN1"),
        sourcePort: ElectricalConnectionPortRef = samplePort(),
        targetPort: ElectricalConnectionPortRef = samplePort(
            subjectId = StableSemanticIdentity("component:HMI1"),
            portSemanticId = StableSemanticIdentity("port:HMI1.IN1"),
            portId = ElectricalPortId("IN1"),
            direction = SemanticPortDirection.INPUT,
        ),
        sourceSpan: LayoutSourceSpan? = null,
    ): ElectricalConnectionIntentInput {
        return ElectricalConnectionIntentInput(
            connectionId = connectionId,
            sourcePort = sourcePort,
            targetPort = targetPort,
            sourceSpan = sourceSpan,
        )
    }

    private fun samplePort(
        subjectId: StableSemanticIdentity = StableSemanticIdentity("component:PLC1"),
        portSemanticId: StableSemanticIdentity = StableSemanticIdentity("port:PLC1.DO1"),
        portId: ElectricalPortId = ElectricalPortId("DO1"),
        endpointKind: ElectricalConnectionEndpointKind = ElectricalConnectionEndpointKind.DEVICE,
        direction: SemanticPortDirection = SemanticPortDirection.OUTPUT,
        signalFamilyId: SemanticSignalFamilyId = SemanticSignalFamilyId("digital"),
    ): ElectricalConnectionPortRef {
        return ElectricalConnectionPortRef(
            subjectId = subjectId,
            portSemanticId = portSemanticId,
            portId = portId,
            endpointKind = endpointKind,
            direction = direction,
            signalFamilyId = signalFamilyId,
        )
    }
}
