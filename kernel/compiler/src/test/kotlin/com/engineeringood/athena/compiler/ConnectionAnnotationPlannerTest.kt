package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionEndpointFact
import com.engineeringood.athena.connection.ConnectionFact
import com.engineeringood.athena.connection.ConnectionSourceTrace
import com.engineeringood.athena.connection.ResolvedConnectionSpecification
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.spatial.ConnectionAnnotationDisplayRole
import com.engineeringood.athena.spatial.ConnectionAnnotationPlanning
import com.engineeringood.athena.spatial.ConnectionAnnotationSelection
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRoutePlanId
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectionAnnotationPlannerTest {
    private val spatialTrace = SpatialSourceTrace(listOf("sheet:main", "connection:C1"), listOf(GeometryElementId("geometry:route")))

    @Test
    fun `no explicit selection emits no annotations`() {
        val result = ConnectionAnnotationPlanner().plan(
            sheetId = "sheet:main",
            drawingArea = SpatialRect(0, 0, 160, 120),
            connectionIr = connectionIr(),
            selections = emptyList(),
            routes = listOf(route()),
            occurrences = emptyList(),
        )

        val success = assertIs<ConnectionAnnotationPlanning.Success>(result)
        assertEquals(emptyList(), success.plan.annotations)
    }

    @Test
    fun `explicit kind annotation resolves stable value and placement`() {
        val selection = ConnectionAnnotationSelection(
            sheetId = "sheet:main",
            semanticId = StableSemanticIdentity("connection:C1"),
            displayRole = ConnectionAnnotationDisplayRole.KIND,
            sourceTrace = spatialTrace,
        )
        val first = assertIs<ConnectionAnnotationPlanning.Success>(ConnectionAnnotationPlanner().plan(
            sheetId = "sheet:main",
            drawingArea = SpatialRect(0, 0, 160, 120),
            connectionIr = connectionIr(),
            selections = listOf(selection),
            routes = listOf(route()),
            occurrences = emptyList(),
        )).plan
        val second = assertIs<ConnectionAnnotationPlanning.Success>(ConnectionAnnotationPlanner().plan(
            sheetId = "sheet:main",
            drawingArea = SpatialRect(0, 0, 160, 120),
            connectionIr = connectionIr(),
            selections = listOf(selection),
            routes = listOf(route()),
            occurrences = emptyList(),
        )).plan

        assertEquals("CONDUCTOR", first.annotations.single().value)
        assertEquals(first, second)
        assertEquals("sheet:main", first.annotations.single().id.sheetId)
    }

    @Test
    fun `kind annotation fits compact authored Sheet coordinates`() {
        val result = ConnectionAnnotationPlanner().plan(
            sheetId = "sheet:main",
            drawingArea = SpatialRect(4, 4, 68, 64),
            connectionIr = connectionIr(),
            selections = listOf(ConnectionAnnotationSelection("sheet:main", StableSemanticIdentity("connection:C1"), ConnectionAnnotationDisplayRole.KIND, spatialTrace)),
            routes = listOf(route().copy(points = listOf(SpatialPoint(20, 36), SpatialPoint(60, 36)))),
            occurrences = emptyList(),
        )

        val success = assertIs<ConnectionAnnotationPlanning.Success>(result)
        assertTrue(success.plan.annotations.single().bounds.isInside(SpatialRect(4, 4, 68, 64)))
    }

    @Test
    fun `impossible placement fails closed with correction`() {
        val result = ConnectionAnnotationPlanner().plan(
            sheetId = "sheet:main",
            drawingArea = SpatialRect(0, 0, 20, 20),
            connectionIr = connectionIr(),
            selections = listOf(ConnectionAnnotationSelection("sheet:main", StableSemanticIdentity("connection:C1"), ConnectionAnnotationDisplayRole.KIND, spatialTrace)),
            routes = listOf(route()),
            occurrences = listOf(SpatialOccurrenceGeometry(
                occurrenceId = SpatialOccurrenceId("sheet:main", "projection:body"),
                subjectId = StableSemanticIdentity("body"),
                sheetId = "sheet:main",
                regionId = "region:main",
                rectangle = SpatialRect(0, 0, 20, 20),
                placementReason = SpatialPlacementReason(listOf("test")),
                sourceTrace = spatialTrace,
            )),
        )

        val failure = assertIs<ConnectionAnnotationPlanning.Failure>(result)
        assertEquals("Annotation selection `connection:C1`", failure.diagnostics.single().subject)
        assertEquals("Move the selection or enlarge the Drawing Area.", failure.diagnostics.single().correction)
    }

    private fun connectionIr() = ConnectionDocument.canonical(
        connections = listOf(ConnectionFact(
            id = StableSemanticIdentity("connection:C1"),
            kind = ConnectionKind.CONDUCTOR,
            endpoints = listOf(
                endpoint("port:A", ConnectionEndpointRole.SOURCE),
                endpoint("port:B", ConnectionEndpointRole.SINK),
            ),
            specification = ResolvedConnectionSpecification(emptyList()),
            trace = ConnectionSourceTrace(SourceProvenance("source.athena", 1, 1, 1, 1)),
        )),
        nets = emptyList(),
        topologyOperators = emptyList(),
    )

    private fun endpoint(id: String, role: ConnectionEndpointRole) = ConnectionEndpointFact(
        portId = StableSemanticIdentity(id),
        authoredPath = listOf(id),
        role = role,
        trace = ConnectionSourceTrace(SourceProvenance("source.athena", 1, 1, 1, 1)),
    )

    private fun route(): ConnectionRoutePlan {
        val source = SpatialOccurrenceId("sheet:main", "occurrence:source")
        val target = SpatialOccurrenceId("sheet:main", "occurrence:target")
        return ConnectionRoutePlan(
            routeId = ConnectionRoutePlanId("sheet:main", "projection:C1"),
            sheetId = "sheet:main",
            connectionId = StableSemanticIdentity("connection:C1"),
            projectionConnectionId = "projection:C1",
            sourceAnchorId = SpatialAnchorId("sheet:main", source, StableSemanticIdentity("port:A")),
            targetAnchorId = SpatialAnchorId("sheet:main", target, StableSemanticIdentity("port:B")),
            points = listOf(SpatialPoint(20, 60), SpatialPoint(140, 60)),
            sourceTrace = spatialTrace,
            laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 60),
        )
    }
}
