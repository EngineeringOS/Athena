package com.engineeringood.athena.physical

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RouteChannelTopologyCompilerTest {
    @Test
    fun `computes exact rational lane centers and allocates aliases by stable order`() {
        val result = RouteChannelTopologyCompiler.compile(
            channels = listOf(
                channel(
                    "CH1",
                    width = 101,
                    height = 20,
                    orientation = PhysicalInfrastructureOrientation.Vertical,
                    lanes = 3,
                    margin = 2,
                ),
            ),
            routes = listOf(route("Z", "CH1"), route("A", "CH1"), route("M", "CH1")),
        )

        val topology = assertIs<RouteChannelTopologyCompilation.Success>(result).topology
        val ch1 = topology.channels.single()

        assertEquals(
            listOf(
                ExactMillimeters(109, 6),
                ExactMillimeters(101, 2),
                ExactMillimeters(497, 6),
            ),
            ch1.laneCenters,
        )
        assertEquals(
            mapOf("A" to 0, "M" to 1, "Z" to 2),
            topology.laneAssignments.associate { assignment -> assignment.connectionAlias to assignment.laneIndex },
        )
        assertEquals(3, topology.evidence.allocatedLaneCount)
    }

    @Test
    fun `fails closed on invalid lane geometry and overflow`() {
        val result = RouteChannelTopologyCompiler.compile(
            channels = listOf(
                channel("BAD_LANES", lanes = 0),
                channel("BAD_SPAN", width = 4, orientation = PhysicalInfrastructureOrientation.Vertical, lanes = 1, margin = 2),
                channel("FULL", lanes = 1),
            ),
            routes = listOf(route("A", "FULL"), route("B", "FULL")),
        )

        val failure = assertIs<RouteChannelTopologyCompilation.Failure>(result)

        assertEquals(
            setOf(
                "physical.route.channel.lanes.invalid",
                "physical.route.channel.usable_span.invalid",
                "physical.route.channel.capacity.overflow",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
    }

    @Test
    fun `derives same duct positive shared boundary adjacency with exact midpoint`() {
        val result = RouteChannelTopologyCompiler.compile(
            channels = listOf(
                channel("A", x = 0, y = 0, width = 100, height = 20, lanes = 4),
                channel("B", x = 100, y = 0, width = 20, height = 80, orientation = PhysicalInfrastructureOrientation.Vertical),
            ),
            routes = listOf(route("Main", "A", "B")),
        )

        val adjacency = assertIs<RouteChannelTopologyCompilation.Success>(result).topology.adjacencies.single()

        assertEquals(PhysicalObjectId("A"), adjacency.fromChannelId)
        assertEquals(PhysicalObjectId("B"), adjacency.toChannelId)
        assertEquals(RouteChannelBoundaryPoint(ExactMillimeters(100, 1), ExactMillimeters(10, 1)), adjacency.passableMidpoint)
    }

    @Test
    fun `rejects cross duct corner gap and overlap channel transitions`() {
        val result = RouteChannelTopologyCompiler.compile(
            channels = listOf(
                channel("A", x = 0, y = 0, width = 100, height = 20, lanes = 4),
                channel("CROSS", ductId = "D2", x = 100, y = 0, width = 20, height = 80),
                channel("CORNER", x = 100, y = 20, width = 20, height = 20),
                channel("GAP", x = 101, y = 0, width = 20, height = 80),
                channel("OVERLAP", x = 90, y = 0, width = 20, height = 80),
            ),
            routes = listOf(
                route("Cross", "A", "CROSS"),
                route("Corner", "A", "CORNER"),
                route("Gap", "A", "GAP"),
                route("Overlap", "A", "OVERLAP"),
            ),
        )

        val failure = assertIs<RouteChannelTopologyCompilation.Failure>(result)

        assertEquals(
            setOf(
                "physical.route.channel.adjacency.cross_duct",
                "physical.route.channel.adjacency.not_passable",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
    }

    private fun channel(
        id: String,
        ductId: String = "D1",
        x: Int = 0,
        y: Int = 0,
        width: Int = 101,
        height: Int = 20,
        orientation: PhysicalInfrastructureOrientation = PhysicalInfrastructureOrientation.Horizontal,
        lanes: Int = 3,
        margin: Int = 2,
    ): PhysicalRouteChannel = PhysicalRouteChannel(
        id = PhysicalObjectId(id),
        ductId = PhysicalObjectId(ductId),
        at = PhysicalPoint2i(x, y),
        size = PhysicalSize2i(width, height),
        orientation = orientation,
        lanes = lanes,
        margin = PhysicalNonNegativeMillimeters.from(margin)!!,
        provenance = PhysicalSourceProvenance(
            PhysicalSourceUnitId("src/main.athena"),
            "channel:$id",
            PhysicalSourceSpan("src/main.athena", 1, 1),
        ),
    )

    private fun route(alias: String, vararg channelIds: String): PhysicalRouteIntent = PhysicalRouteIntent(
        connectionAlias = alias,
        channelIds = channelIds.map(::PhysicalObjectId),
        provenance = PhysicalSourceProvenance(
            PhysicalSourceUnitId("src/main.athena"),
            "route:$alias",
            PhysicalSourceSpan("src/main.athena", 1, 1),
        ),
    )
}
