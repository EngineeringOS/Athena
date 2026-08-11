package com.engineeringood.athena.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConnectionEditOperationsTest {
    @Test
    fun `connect owns canonical endpoint and requirement intent`() {
        val operation = ConnectPorts(
            connectionKind = ConnectionEditKind.WIRE,
            endpoints = listOf(
                ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "port:Supply.L1"),
                ConnectionEditEndpoint(ConnectionEditEndpointRole.SINK, "port:Breaker.line"),
            ),
            requirements = listOf(
                ConnectionRequirementIntent(
                    ConnectionRequirementKind.CROSS_SECTION,
                    ConnectionRequirementValue.Quantity("1.5", "mm2"),
                ),
            ),
        )

        assertEquals(EditOperationKind.CONNECT_PORTS, operation.kind)
        assertEquals(EditAuthorityClass.ENGINEERING, operation.kind.authorityClass)
    }

    @Test
    fun `connect rejects noncanonical endpoints roles and requirements`() {
        val source = ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "port:Supply.L1")
        val sink = ConnectionEditEndpoint(ConnectionEditEndpointRole.SINK, "port:Breaker.line")
        val crossSection = ConnectionRequirementIntent(
            ConnectionRequirementKind.CROSS_SECTION,
            ConnectionRequirementValue.Quantity("1.5", "mm2"),
        )
        val color = ConnectionRequirementIntent(
            ConnectionRequirementKind.COLOR_CODE,
            ConnectionRequirementValue.Symbol("black"),
        )

        assertFailsWith<IllegalArgumentException> { ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "Supply.L1") }
        assertEquals("port:Q1.1", ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "port:Q1.1").portId)
        assertFailsWith<IllegalArgumentException> { ConnectPorts(ConnectionEditKind.WIRE, listOf(sink, source), listOf(crossSection)) }
        assertFailsWith<IllegalArgumentException> { ConnectPorts(ConnectionEditKind.WIRE, listOf(source, sink), listOf(color, crossSection)) }
        assertFailsWith<IllegalArgumentException> {
            ConnectionRequirementIntent(ConnectionRequirementKind.CROSS_SECTION, ConnectionRequirementValue.Symbol("1.5mm2"))
        }
    }

    @Test
    fun `reconnect targets one closed binary endpoint role`() {
        val operation = ReconnectConnectionEndpoint(
            connectionId = "connection:src/project.athena:wire:Supply.L1:Breaker.line",
            endpointRole = ConnectionEditEndpointRole.SINK,
            replacementPortId = "port:Contactor.L1",
        )

        assertEquals(EditOperationKind.RECONNECT_CONNECTION_ENDPOINT, operation.kind)
        assertFailsWith<IllegalArgumentException> { operation.copy(connectionId = "relationship:power") }
    }

    @Test
    fun `route adjustment contains stable logical intent without viewport geometry`() {
        val operation = AdjustConnectionRoute(
            sheetId = "sheet-main",
            connectionId = "connection:src/project.athena:wire:Supply.L1:Breaker.line",
            projectionId = "view/sheet-main/connection/Supply-Breaker",
            target = LogicalRouteTarget(LogicalRouteTargetKind.SEGMENT, ordinal = 2),
            point = LogicalRoutePoint(column = 17, row = 8),
        )

        assertEquals(EditOperationKind.ADJUST_CONNECTION_ROUTE, operation.kind)
        assertEquals(EditAuthorityClass.PRESENTATION, operation.kind.authorityClass)
        assertFailsWith<IllegalArgumentException> { operation.copy(target = operation.target.copy(ordinal = 0)) }
        assertFailsWith<IllegalArgumentException> { operation.copy(point = operation.point.copy(column = 0)) }
        assertFailsWith<IllegalArgumentException> { operation.copy(point = operation.point.copy(row = 0)) }
    }
}
