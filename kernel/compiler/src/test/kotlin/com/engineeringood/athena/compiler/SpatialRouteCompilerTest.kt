package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpatialRouteCompilerTest {
    @Test
    fun `spatial route compiler derives anchors lanes and routes`() {
        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            occurrences = occurrences(),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(4, result.anchorPositions.size)
        assertEquals(listOf("lane:engineering-projection:main"), result.lanes.map { lane -> lane.laneId })
        assertEquals(1, result.routes.size)
    }

    @Test
    fun `route endpoints equal placed anchor points`() {
        val result = SpatialRouteCompiler().compile(projectionDocument(), occurrences())
        val route = result.routes.single()
        val anchorById = result.anchorPositions.associateBy { anchor -> anchor.anchorId }
        val sourceAnchor = requireNotNull(anchorById[route.sourceAnchorId])
        val targetAnchor = requireNotNull(anchorById[route.targetAnchorId])

        assertEquals(sourceAnchor.x, route.points.first().x)
        assertEquals(sourceAnchor.y, route.points.first().y)
        assertEquals(targetAnchor.x, route.points.last().x)
        assertEquals(targetAnchor.y, route.points.last().y)
    }

    @Test
    fun `route carries connection occurrence lane and anchor identities`() {
        val route = SpatialRouteCompiler().compile(projectionDocument(), occurrences()).routes.single()

        assertEquals("connection:Supply.L1-to-Q1.1", route.connectionId.value)
        assertEquals("projection/node/component:Supply", route.sourceOccurrenceId)
        assertEquals("projection/node/component:Q1", route.targetOccurrenceId)
        assertEquals("anchor:projection/node/component:Supply:right", route.sourceAnchorId)
        assertEquals("anchor:projection/node/component:Q1:left", route.targetAnchorId)
        assertEquals("lane:engineering-projection:main", route.laneId)
    }

    @Test
    fun `spatial route compiler reports missing occurrence geometry plainly`() {
        val result = SpatialRouteCompiler().compile(
            projection = projectionDocument(),
            occurrences = emptyList(),
        )

        assertTrue(result.routes.isEmpty())
        assertEquals(1, result.diagnostics.size)
        assertEquals("Spatial Reality", result.diagnostics.single().reality)
        assertEquals("missing occurrence geometry facts", result.diagnostics.single().message)
    }

    @Test
    fun `projection models do not own route endpoint geometry`() {
        val projectionPropertyNames = listOf(
            ProjectionDocument::class.java,
            ProjectionNode::class.java,
            ProjectionConnection::class.java,
        ).flatMap { type -> type.declaredFields.map { field -> field.name } }
        val forbidden = listOf("routeStart", "routeEnd", "routePoints", "sourceAnchorId", "targetAnchorId")

        forbidden.forEach { token ->
            assertFalse(
                projectionPropertyNames.any { name -> name.equals(token, ignoreCase = true) },
                "Projection model must not own route geometry field `$token`: $projectionPropertyNames",
            )
        }
    }

    @Test
    fun `new spatial route names avoid stale architecture terms`() {
        val names = listOf(
            SpatialRouteCompiler::class.simpleName.orEmpty(),
            SpatialRouteCompilationResult::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Spatial route names must not contain `$token`: $names",
            )
        }
    }

    private fun projectionDocument(): ProjectionDocument {
        val view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection")
        val supplyNode = supplyNode()
        val breakerNode = breakerNode()
        val connection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.1"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
            originGeometryElementId = GeometryElementId("origin:connection"),
        )
        val subjects = listOf(
            ProjectionSheetSubject(supplyNode.semanticId, nodeIds = listOf(supplyNode.projectionId)),
            ProjectionSheetSubject(breakerNode.semanticId, nodeIds = listOf(breakerNode.projectionId)),
            ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId)),
        )
        val sheetId = ProjectionSheetId("engineering-projection/sheet/01-main")
        return ProjectionDocument(
            view = view,
            nodes = listOf(supplyNode, breakerNode),
            connections = listOf(connection),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Engineering Projection Main",
                    order = 0,
                    subjects = subjects,
                    publication = ProjectionSheetPublication.fromProjectionState(
                        sheetId = sheetId,
                        displayName = "Engineering Projection Main",
                        order = 0,
                        subjects = subjects,
                    ),
                ),
            ),
        )
    }

    private fun occurrences(): List<SpatialOccurrenceGeometry> = listOf(
        testSpatialOccurrence("projection/node/component:Supply", "component:Supply", 0, 0),
        testSpatialOccurrence("projection/node/component:Q1", "component:Q1", 140, 0),
    )

    private fun supplyNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Supply"),
            semanticId = StableSemanticIdentity("component:Supply"),
            label = "Supply",
            originGeometryElementId = GeometryElementId("origin:Supply"),
        )

    private fun breakerNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q1"),
            semanticId = StableSemanticIdentity("component:Q1"),
            label = "Q1",
            originGeometryElementId = GeometryElementId("origin:Q1"),
        )
}
