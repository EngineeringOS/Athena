package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpatialAnchorCompilerTest {
    @Test
    fun `referenced occurrence ports compile once to literal stable boundary anchors`() {
        val fixture = sharedPortFixture()

        val result = SpatialAnchorCompiler().compile(fixture.projection, fixture.occurrences)

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                anchor(
                    sheetId = MAIN_SHEET,
                    occurrenceId = "occ:A",
                    portId = "port:A.p1",
                    side = SpatialBoundarySide.RIGHT,
                    point = SpatialPoint(20, 13),
                    connectionIds = listOf("connection:1", "connection:2"),
                ),
                anchor(
                    sheetId = MAIN_SHEET,
                    occurrenceId = "occ:A",
                    portId = "port:A.p2",
                    side = SpatialBoundarySide.RIGHT,
                    point = SpatialPoint(20, 16),
                    connectionIds = listOf("connection:3"),
                ),
                anchor(
                    sheetId = MAIN_SHEET,
                    occurrenceId = "occ:B",
                    portId = "port:B.in",
                    side = SpatialBoundarySide.LEFT,
                    point = SpatialPoint(40, 15),
                    connectionIds = listOf("connection:1", "connection:3"),
                ),
                anchor(
                    sheetId = MAIN_SHEET,
                    occurrenceId = "occ:C",
                    portId = "port:C.in",
                    side = SpatialBoundarySide.LEFT,
                    point = SpatialPoint(50, 15),
                    connectionIds = listOf("connection:2"),
                ),
            ),
            result.anchorPositions,
        )
    }

    @Test
    fun `connection port sheet and geometry permutations produce equal canonical anchors`() {
        val fixture = sharedPortFixture()
        val expected = SpatialAnchorCompiler().compile(fixture.projection, fixture.occurrences)

        repeat(20) { seed ->
            val random = Random(seed)
            val shuffledProjection = fixture.projection.copy(
                nodes = fixture.projection.nodes.shuffled(random),
                occurrencePorts = fixture.projection.occurrencePorts.shuffled(random),
                connections = fixture.projection.connections.shuffled(random),
                sheets = fixture.projection.sheets.shuffled(random),
            )

            assertEquals(
                expected,
                SpatialAnchorCompiler().compile(shuffledProjection, fixture.occurrences.shuffled(random)),
            )
        }
    }

    @Test
    fun `equal axis is horizontal and coincident centers use endpoint role`() {
        val owner = node("occ:owner")
        val diagonal = node("occ:diagonal")
        val coincident = node("occ:coincident")
        val diagonalSource = port(owner, "port:owner.diagonal")
        val coincidentSource = port(owner, "port:owner.coincident")
        val diagonalTarget = port(diagonal, "port:diagonal.in")
        val coincidentTarget = port(coincident, "port:coincident.in")
        val projection = projection(
            nodes = listOf(owner, diagonal, coincident),
            ports = listOf(diagonalSource, coincidentSource, diagonalTarget, coincidentTarget),
            connections = listOf(
                connection("connection:diagonal", diagonalSource, diagonalTarget),
                connection("connection:coincident", coincidentSource, coincidentTarget),
            ),
        )
        val result = SpatialAnchorCompiler().compile(
            projection,
            listOf(
                geometry(owner, 10, 10, 10, 10),
                geometry(diagonal, 30, 30, 10, 10),
                geometry(coincident, 10, 10, 10, 10),
            ),
        )
        val byPort = result.anchorPositions.associateBy { anchor -> anchor.subject.portId.value }

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(SpatialBoundarySide.RIGHT, byPort.getValue("port:owner.diagonal").side)
        assertEquals(SpatialBoundarySide.RIGHT, byPort.getValue("port:owner.coincident").side)
        assertEquals(SpatialBoundarySide.LEFT, byPort.getValue("port:coincident.in").side)
    }

    @Test
    fun `sheet occurrence and port all participate in anchor identity`() {
        val firstOwner = node("occ:first")
        val firstPeer = node("occ:first-peer")
        val secondOwner = node("occ:second")
        val secondPeer = node("occ:second-peer")
        val firstPort = port(firstOwner, "port:shared")
        val firstPeerPort = port(firstPeer, "port:first-peer.in")
        val secondPort = port(secondOwner, "port:shared")
        val secondPeerPort = port(secondPeer, "port:second-peer.in")
        val secondSheet = "view/sheet/secondary"
        val projection = projection(
            nodes = listOf(firstOwner, firstPeer, secondOwner, secondPeer),
            ports = listOf(firstPort, firstPeerPort, secondPort, secondPeerPort),
            connections = listOf(
                connection("connection:first", firstPort, firstPeerPort),
                connection("connection:second", secondPort, secondPeerPort),
            ),
            sheets = listOf(
                sheet(MAIN_SHEET, listOf(firstOwner, firstPeer), listOf("connection:first")),
                sheet(secondSheet, listOf(secondOwner, secondPeer), listOf("connection:second"), order = 1),
            ),
        )

        val anchors = SpatialAnchorCompiler().compile(
            projection,
            listOf(
                geometry(firstOwner, 0, 0, 10, 10, MAIN_SHEET),
                geometry(firstPeer, 20, 0, 10, 10, MAIN_SHEET),
                geometry(secondOwner, 0, 0, 10, 10, secondSheet),
                geometry(secondPeer, 20, 0, 10, 10, secondSheet),
            ),
        ).anchorPositions.filter { anchor -> anchor.subject.portId.value == "port:shared" }

        assertEquals(2, anchors.size)
        assertNotEquals(anchors[0].anchorId, anchors[1].anchorId)
        assertEquals(listOf(MAIN_SHEET, secondSheet), anchors.map { anchor -> anchor.sheetId })
    }

    @Test
    fun `connection owning Sheet selects Anchor for a repeated Projection occurrence`() {
        val repeated = node("occ:repeated")
        val peer = node("occ:peer")
        val source = port(repeated, "port:repeated.out")
        val target = port(peer, "port:peer.in")
        val connection = connection("connection:main", source, target)
        val secondSheet = "view/sheet/secondary"
        val projection = projection(
            nodes = listOf(repeated, peer),
            ports = listOf(source, target),
            connections = listOf(connection),
            sheets = listOf(
                sheet(MAIN_SHEET, listOf(repeated, peer), listOf(connection.projectionId.value)),
                sheet(secondSheet, listOf(repeated), emptyList(), order = 1),
            ),
        )

        val result = SpatialAnchorCompiler().compile(
            projection,
            listOf(
                geometry(repeated, 0, 0, 10, 10, MAIN_SHEET),
                geometry(peer, 20, 0, 10, 10, MAIN_SHEET),
                geometry(repeated, 0, 20, 10, 10, secondSheet),
            ),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(2, result.anchorPositions.size)
        assertTrue(result.anchorPositions.all { anchor -> anchor.sheetId == MAIN_SHEET })
        assertEquals(
            setOf("port:repeated.out", "port:peer.in"),
            result.anchorPositions.map { anchor -> anchor.subject.portId.value }.toSet(),
        )
    }

    @Test
    fun `side and point changes cannot change anchor identity`() {
        val fixture = twoPortFixture(peerX = 30, peerY = 0)
        val moved = twoPortFixture(peerX = -30, peerY = 20)

        val first = SpatialAnchorCompiler().compile(fixture.projection, fixture.occurrences).anchorPositions
        val second = SpatialAnchorCompiler().compile(moved.projection, moved.occurrences).anchorPositions

        assertEquals(first.map(SpatialAnchorPosition::anchorId), second.map(SpatialAnchorPosition::anchorId))
        assertNotEquals(first.map(SpatialAnchorPosition::side), second.map(SpatialAnchorPosition::side))
        assertNotEquals(first.map(SpatialAnchorPosition::point), second.map(SpatialAnchorPosition::point))
    }

    @Test
    fun `multiple ports on every side receive literal distinct non corner points`() {
        val owner = node("occ:owner")
        val right = node("occ:right")
        val left = node("occ:left")
        val top = node("occ:top")
        val bottom = node("occ:bottom")
        val peers = mapOf(
            "left" to left,
            "right" to right,
            "top" to top,
            "bottom" to bottom,
        )
        val ownerPorts = peers.keys.flatMap { side ->
            listOf(port(owner, "port:owner.$side.1"), port(owner, "port:owner.$side.2"))
        }
        val peerPorts = peers.flatMap { (side, peer) ->
            listOf(port(peer, "port:$side.in.1"), port(peer, "port:$side.in.2"))
        }
        val connections = peers.keys.flatMap { side ->
            (1..2).map { index ->
                connection(
                    "connection:$side:$index",
                    ownerPorts.single { port -> port.occurrencePortId.portId.value == "port:owner.$side.$index" },
                    peerPorts.single { port -> port.occurrencePortId.portId.value == "port:$side.in.$index" },
                )
            }
        }
        val projection = projection(
            listOf(owner, right, left, top, bottom),
            ownerPorts + peerPorts,
            connections,
        )
        val result = SpatialAnchorCompiler().compile(
            projection,
            listOf(
                geometry(owner, 10, 10, 10, 10),
                geometry(right, 40, 10, 10, 10),
                geometry(left, -20, 10, 10, 10),
                geometry(top, 10, -20, 10, 10),
                geometry(bottom, 10, 40, 10, 10),
            ),
        )
        val ownerPoints = result.anchorPositions
            .filter { anchor -> anchor.subject.occurrenceId.projectionId == owner.projectionId.value }
            .associate { anchor -> anchor.subject.portId.value to anchor.point }

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            mapOf(
                "port:owner.left.1" to SpatialPoint(10, 13),
                "port:owner.left.2" to SpatialPoint(10, 16),
                "port:owner.right.1" to SpatialPoint(20, 13),
                "port:owner.right.2" to SpatialPoint(20, 16),
                "port:owner.top.1" to SpatialPoint(13, 10),
                "port:owner.top.2" to SpatialPoint(16, 10),
                "port:owner.bottom.1" to SpatialPoint(13, 20),
                "port:owner.bottom.2" to SpatialPoint(16, 20),
            ),
            ownerPoints,
        )
    }

    @Test
    fun `null endpoint fails with exact diagnostic and no partial anchors`() {
        val fixture = sharedPortFixture()
        val broken = fixture.projection.copy(
            connections = fixture.projection.connections.map { connection ->
                if (connection.projectionId.value == "connection:1") connection.copy(source = null) else connection
            },
        )

        assertSingleFailure(
            result = SpatialAnchorCompiler().compile(broken, fixture.occurrences),
            subject = "Connection connection:1 source endpoint",
            problem = "is missing typed Occurrence and port identity",
            correction = "Reference one projected occurrence-port as the source endpoint.",
            projectionIds = listOf("connection:1"),
            geometryIds = listOf("geometry:connection:1"),
        )
    }

    @Test
    fun `unknown occurrence fails with exact diagnostic and no partial anchors`() {
        val fixture = sharedPortFixture()
        val unknown = ProjectionOccurrencePortId(ProjectionNodeId("occ:unknown"), StableSemanticIdentity("port:unknown"))
        val broken = fixture.projection.copy(
            occurrencePorts = fixture.projection.occurrencePorts + ProjectionOccurrencePort(
                unknown,
                GeometryElementId("geometry:port:port:unknown"),
            ),
            connections = fixture.projection.connections.map { connection ->
                if (connection.projectionId.value == "connection:1") {
                    connection.copy(source = ProjectionConnectionEndpoint(unknown))
                } else {
                    connection
                }
            },
        )
        val occurrences = fixture.occurrences + SpatialOccurrenceGeometry(
            occurrenceId = SpatialOccurrenceId(MAIN_SHEET, "occ:unknown"),
            subjectId = StableSemanticIdentity("component:unknown"),
            sheetId = MAIN_SHEET,
            regionId = "region:$MAIN_SHEET",
            rectangle = SpatialRect(0, 0, 10, 10),
            placementReason = SpatialPlacementReason(listOf("test placement")),
            sourceTrace = SpatialSourceTrace(
                listOf(MAIN_SHEET, "occ:unknown"),
                listOf(GeometryElementId("geometry:occurrence:occ:unknown")),
            ),
        )

        assertSingleFailure(
            result = SpatialAnchorCompiler().compile(broken, occurrences),
            subject = "Port port:unknown on Occurrence occ:unknown",
            problem = "references an unknown Occurrence",
            correction = "Project Occurrence occ:unknown before referencing its port.",
            projectionIds = listOf("occ:unknown", "port:unknown"),
            geometryIds = listOf(
                "geometry:occurrence:occ:unknown",
                "geometry:port:port:unknown",
            ),
        )
    }

    @Test
    fun `missing and duplicate projected port facts fail exactly`() {
        val fixture = sharedPortFixture()
        val sourcePort = fixture.projection.occurrencePorts.single {
            port -> port.occurrencePortId.portId.value == "port:A.p1"
        }
        val missing = fixture.projection.copy(
            occurrencePorts = fixture.projection.occurrencePorts - sourcePort,
        )
        assertSingleFailure(
            SpatialAnchorCompiler().compile(missing, fixture.occurrences),
            "Port port:A.p1 on Occurrence occ:A",
            "has 0 projected occurrence-port facts",
            "Publish exactly one occurrence-port fact for this referenced engineering port.",
            listOf(MAIN_SHEET, "occ:A", "port:A.p1"),
            listOf(
                "geometry:node:occ:A",
                "geometry:occurrence:occ:A",
                "geometry:sheet:$MAIN_SHEET",
            ),
        )

        val duplicate = fixture.projection.copy(
            occurrencePorts = fixture.projection.occurrencePorts + sourcePort.copy(
                originGeometryElementId = GeometryElementId("geometry:port:duplicate:A.p1"),
            ),
        )
        assertSingleFailure(
            SpatialAnchorCompiler().compile(duplicate, fixture.occurrences),
            "Port port:A.p1 on Occurrence occ:A",
            "has 2 projected occurrence-port facts",
            "Publish exactly one occurrence-port fact for this referenced engineering port.",
            listOf(MAIN_SHEET, "occ:A", "port:A.p1"),
            listOf(
                "geometry:node:occ:A",
                "geometry:occurrence:occ:A",
                "geometry:port:duplicate:A.p1",
                "geometry:port:port:A.p1",
                "geometry:sheet:$MAIN_SHEET",
            ),
        )
    }

    @Test
    fun `missing duplicate and foreign geometry facts fail exactly`() {
        val fixture = sharedPortFixture()
        val sourceGeometry = fixture.occurrences.single { geometry -> geometry.occurrenceId.projectionId == "occ:A" }
        assertSingleFailure(
            SpatialAnchorCompiler().compile(fixture.projection, fixture.occurrences - sourceGeometry),
            "Occurrence occ:A",
            "has 0 Spatial geometry facts",
            "Publish exactly one geometry fact for this referenced Occurrence.",
            listOf(MAIN_SHEET, "occ:A", "port:A.p1", "port:A.p2"),
            listOf(
                "geometry:node:occ:A",
                "geometry:port:port:A.p1",
                "geometry:port:port:A.p2",
                "geometry:sheet:$MAIN_SHEET",
            ),
        )

        val duplicate = sourceGeometry.copy(
            sourceTrace = SpatialSourceTrace(
                listOf(MAIN_SHEET, "occ:A"),
                listOf(GeometryElementId("geometry:occurrence:duplicate:occ:A")),
            ),
        )
        val duplicateResult = SpatialAnchorCompiler().compile(
            fixture.projection,
            fixture.occurrences + duplicate,
        )
        assertTrue(duplicateResult.anchorPositions.isEmpty())
        assertEquals(1, duplicateResult.diagnostics.size)
        assertEquals("has 2 Spatial geometry facts", duplicateResult.diagnostics.single().problem)
        assertEquals(
            listOf(MAIN_SHEET, "occ:A", "port:A.p1", "port:A.p2"),
            duplicateResult.diagnostics.single().sourceTrace.projectionIds,
        )

        val foreignSheet = "view/sheet/foreign"
        val foreign = sourceGeometry.copy(
            occurrenceId = SpatialOccurrenceId(foreignSheet, "occ:A"),
            sheetId = foreignSheet,
        )
        val foreignResult = SpatialAnchorCompiler().compile(
            fixture.projection,
            fixture.occurrences - sourceGeometry + foreign,
        )
        assertTrue(foreignResult.anchorPositions.isEmpty())
        assertEquals(
            listOf("has geometry on Sheet $foreignSheet but belongs to Sheet $MAIN_SHEET"),
            foreignResult.diagnostics.map { diagnostic -> diagnostic.problem },
        )
    }

    @Test
    fun `duplicate connection identity and identical endpoints fail before side selection`() {
        val fixture = sharedPortFixture()
        val connection1 = fixture.projection.connections.single { connection -> connection.projectionId.value == "connection:1" }
        val connection2 = fixture.projection.connections.single { connection -> connection.projectionId.value == "connection:2" }
        val duplicate = fixture.projection.copy(
            connections = fixture.projection.connections + connection2.copy(
                projectionId = connection1.projectionId,
                originGeometryElementId = GeometryElementId("geometry:connection:1:duplicate"),
            ),
        )
        assertSingleFailure(
            SpatialAnchorCompiler().compile(duplicate, fixture.occurrences),
            "Connection identity connection:1",
            "is used by 2 projected Connections",
            "Give every projected Connection a unique identity before choosing Anchor sides.",
            listOf("connection:1"),
            listOf("geometry:connection:1", "geometry:connection:1:duplicate"),
        )

        val identical = fixture.projection.copy(
            connections = listOf(connection1.copy(target = connection1.source)),
        )
        assertSingleFailure(
            SpatialAnchorCompiler().compile(identical, fixture.occurrences),
            "Connection connection:1",
            "uses Port port:A.p1 on Occurrence occ:A as both source and target",
            "Connect two distinct occurrence-ports.",
            listOf("connection:1", "occ:A", "port:A.p1"),
            listOf("geometry:connection:1"),
        )
    }

    @Test
    fun `capacity boundary passes and one unit less fails without partial anchors`() {
        val fixture = sharedPortFixture()
        fun withOwnerHeight(height: Int): List<SpatialOccurrenceGeometry> = fixture.occurrences.map { geometry ->
            if (geometry.occurrenceId.projectionId == "occ:A") {
                geometry.copy(rectangle = geometry.rectangle.copy(height = height))
            } else {
                geometry
            }
        }

        val boundary = SpatialAnchorCompiler().compile(fixture.projection, withOwnerHeight(3))
        assertTrue(boundary.diagnostics.isEmpty())
        assertEquals(listOf(11, 12), boundary.anchorPositions.filter { anchor ->
            anchor.subject.occurrenceId.projectionId == "occ:A"
        }.map { anchor -> anchor.point.y })

        assertSingleFailure(
            SpatialAnchorCompiler().compile(fixture.projection, withOwnerHeight(2)),
            "Occurrence occ:A right side",
            "has 2 drawing units for 2 referenced ports",
            "Provide at least 3 drawing units on this side so every Anchor is distinct and non-corner.",
            listOf(MAIN_SHEET, "occ:A", "port:A.p1", "port:A.p2"),
            listOf(
                "geometry:connection:1",
                "geometry:connection:2",
                "geometry:connection:3",
                "geometry:node:occ:A",
                "geometry:occurrence:occ:A",
                "geometry:port:port:A.p1",
                "geometry:port:port:A.p2",
                "geometry:sheet:$MAIN_SHEET",
            ),
        )
    }

    @Test
    fun `multi defect diagnostics and empty facts stay equal under permutation`() {
        val fixture = sharedPortFixture()
        val duplicatePort = fixture.projection.occurrencePorts.first().copy(
            originGeometryElementId = GeometryElementId("geometry:port:duplicate"),
        )
        val brokenProjection = fixture.projection.copy(
            occurrencePorts = fixture.projection.occurrencePorts + duplicatePort,
            connections = fixture.projection.connections.map { connection ->
                if (connection.projectionId.value == "connection:2") connection.copy(target = null) else connection
            },
        )
        val expected = SpatialAnchorCompiler().compile(brokenProjection, fixture.occurrences)
        assertTrue(expected.anchorPositions.isEmpty())
        assertEquals(2, expected.diagnostics.size)

        repeat(20) { seed ->
            val random = Random(seed)
            val shuffled = brokenProjection.copy(
                nodes = brokenProjection.nodes.shuffled(random),
                occurrencePorts = brokenProjection.occurrencePorts.shuffled(random),
                connections = brokenProjection.connections.shuffled(random),
                sheets = brokenProjection.sheets.shuffled(random),
            )
            assertEquals(
                expected,
                SpatialAnchorCompiler().compile(shuffled, fixture.occurrences.shuffled(random)),
            )
        }
    }

    @Test
    fun `projection spatial compiler returns failure and publishes no document for anchor defects`() {
        val fixture = sharedPortFixture()
        val broken = fixture.projection.copy(
            connections = fixture.projection.connections.map { connection ->
                if (connection.projectionId.value == "connection:1") connection.copy(source = null) else connection
            },
            sheets = fixture.projection.sheets.map { sheet ->
                sheet.copy(grid = ProjectionSheetGrid("grid:main", rows = 4, columns = 4))
            },
        )

        val failure = assertIs<RealityTransformationResult.Failure>(ProjectionSpatialCompiler().transform(broken))
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.subject == "Connection connection:1 source endpoint" &&
                diagnostic.problem == "is missing typed Occurrence and port identity"
        })
    }

    private fun sharedPortFixture(): AnchorFixture {
        val a = node("occ:A")
        val b = node("occ:B")
        val c = node("occ:C")
        val a1 = port(a, "port:A.p1")
        val a2 = port(a, "port:A.p2")
        val b1 = port(b, "port:B.in")
        val c1 = port(c, "port:C.in")
        val connections = listOf(
            connection("connection:2", a1, c1),
            connection("connection:3", a2, b1),
            connection("connection:1", a1, b1),
        )
        return AnchorFixture(
            projection = projection(listOf(a, b, c), listOf(a1, a2, b1, c1), connections),
            occurrences = listOf(
                geometry(a, 10, 10, 10, 10),
                geometry(b, 40, 10, 10, 10),
                geometry(c, 50, 10, 10, 10),
            ),
        )
    }

    private fun twoPortFixture(peerX: Int, peerY: Int): AnchorFixture {
        val owner = node("occ:owner")
        val peer = node("occ:peer")
        val source = port(owner, "port:owner.out")
        val target = port(peer, "port:peer.in")
        return AnchorFixture(
            projection = projection(
                listOf(owner, peer),
                listOf(source, target),
                listOf(connection("connection:move", source, target)),
            ),
            occurrences = listOf(
                geometry(owner, 0, 0, 10, 10),
                geometry(peer, peerX, peerY, 10, 10),
            ),
        )
    }

    private fun projection(
        nodes: List<ProjectionNode>,
        ports: List<ProjectionOccurrencePort>,
        connections: List<ProjectionConnection>,
        sheets: List<ProjectionSheet> = listOf(sheet(MAIN_SHEET, nodes, connections.map { it.projectionId.value })),
    ): ProjectionDocument = ProjectionDocument(
        view = ViewDefinition("view", "View"),
        nodes = nodes,
        occurrencePorts = ports,
        connections = connections,
        sheets = sheets,
    )

    private fun sheet(
        sheetId: String,
        nodes: List<ProjectionNode>,
        connectionIds: List<String>,
        order: Int = 0,
    ): ProjectionSheet = ProjectionSheet(
        sheetId = ProjectionSheetId(sheetId),
        displayName = sheetId,
        order = order,
        originGeometryElementId = GeometryElementId("geometry:sheet:$sheetId"),
        subjects = nodes.map { node ->
            ProjectionSheetSubject(node.semanticId, nodeIds = listOf(node.projectionId))
        } + connectionIds.map { connectionId ->
            ProjectionSheetSubject(
                StableSemanticIdentity("semantic:$connectionId"),
                connectionIds = listOf(ProjectionConnectionId(connectionId)),
            )
        },
    )

    private fun node(id: String): ProjectionNode = ProjectionNode(
        projectionId = ProjectionNodeId(id),
        semanticId = StableSemanticIdentity("component:${id.substringAfter(':')}"),
        label = id,
        originGeometryElementId = GeometryElementId("geometry:node:$id"),
    )

    private fun port(node: ProjectionNode, id: String): ProjectionOccurrencePort = ProjectionOccurrencePort(
        occurrencePortId = ProjectionOccurrencePortId(node.projectionId, StableSemanticIdentity(id)),
        originGeometryElementId = GeometryElementId("geometry:port:$id"),
    )

    private fun connection(
        id: String,
        source: ProjectionOccurrencePort,
        target: ProjectionOccurrencePort,
    ): ProjectionConnection = ProjectionConnection(
        projectionId = ProjectionConnectionId(id),
        semanticId = StableSemanticIdentity("semantic:$id"),
        originGeometryElementId = GeometryElementId("geometry:$id"),
        source = ProjectionConnectionEndpoint(source.occurrencePortId),
        target = ProjectionConnectionEndpoint(target.occurrencePortId),
    )

    private fun geometry(
        node: ProjectionNode,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        sheetId: String = MAIN_SHEET,
    ): SpatialOccurrenceGeometry = SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId(sheetId, node.projectionId.value),
        subjectId = node.semanticId,
        sheetId = sheetId,
        regionId = "region:$sheetId",
        rectangle = SpatialRect(x, y, width, height),
        placementReason = SpatialPlacementReason(listOf("test placement")),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, node.projectionId.value),
            geometryElementIds = listOf(GeometryElementId("geometry:occurrence:${node.projectionId.value}")),
        ),
    )

    private fun anchor(
        sheetId: String,
        occurrenceId: String,
        portId: String,
        side: SpatialBoundarySide,
        point: SpatialPoint,
        connectionIds: List<String>,
    ): SpatialAnchorPosition {
        val occurrence = SpatialOccurrenceId(sheetId, occurrenceId)
        val port = StableSemanticIdentity(portId)
        val subject = SpatialOccurrencePortSubject(occurrence, port)
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId(sheetId, occurrence, port),
            sheetId = sheetId,
            subject = subject,
            side = side,
            point = point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(sheetId, occurrenceId, portId) + connectionIds,
                geometryElementIds = (
                    listOf(
                        "geometry:node:$occurrenceId",
                        "geometry:occurrence:$occurrenceId",
                        "geometry:port:$portId",
                        "geometry:sheet:$sheetId",
                    ) + connectionIds.map { connectionId -> "geometry:$connectionId" }
                    ).sorted().map(::GeometryElementId),
            ),
        )
    }

    private data class AnchorFixture(
        val projection: ProjectionDocument,
        val occurrences: List<SpatialOccurrenceGeometry>,
    )

    private companion object {
        const val MAIN_SHEET = "view/sheet/main"
    }

    private fun assertSingleFailure(
        result: SpatialAnchorCompilationResult,
        subject: String,
        problem: String,
        correction: String,
        projectionIds: List<String>,
        geometryIds: List<String>,
    ) {
        assertTrue(result.anchorPositions.isEmpty())
        assertEquals(1, result.diagnostics.size)
        val diagnostic = result.diagnostics.single()
        assertEquals(subject, diagnostic.subject)
        assertEquals(problem, diagnostic.problem)
        assertEquals(correction, diagnostic.correction)
        assertEquals(projectionIds, diagnostic.sourceTrace.projectionIds)
        assertEquals(geometryIds, diagnostic.sourceTrace.geometryElementIds.map(GeometryElementId::value))
    }
}
