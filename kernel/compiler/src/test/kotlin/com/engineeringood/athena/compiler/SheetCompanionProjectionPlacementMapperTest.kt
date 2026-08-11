package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.ConnectionProjectionEndpoint
import com.engineeringood.athena.projection.ConnectionProjectionId
import com.engineeringood.athena.projection.ConnectionProjectionParticipant
import com.engineeringood.athena.projection.LogicalRouteConstraint
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.layout.ViewDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SheetCompanionProjectionPlacementMapperTest {
    @Test
    fun `maps known placements to stable projection occurrence constraints`() {
        val source = parse(
            """
                sheet demo {
                page format A3 landscape
                frame: 17 * 16
                snap: 1
                KM1 at (64, 48) lock
                Q1 at (24, 16)
                }
            """.trimIndent(),
        )
        val projection = projection()
        val result = SheetCompanionProjectionPlacementMapper().map(source, projection.sheets.single(), projection)

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(listOf("node:KM1", "node:Q1"), result.constraints.map { it.occurrenceId.value })
        assertEquals(64, result.constraints[0].point.x)
        assertTrue(result.constraints[0].locked)
        assertEquals(16, result.constraints[1].point.y)
        assertEquals(1, result.constraints[0].snapStep)
    }

    @Test
    fun `reports unknown occurrences and keeps deterministic diagnostic order`() {
        val source = parse(
            """
                sheet demo {
                page format A3 landscape
                frame: 17 * 16
                snap: 1
                Missing at (24, 16)
                }
            """.trimIndent(),
        )
        val projection = projection()
        val result = SheetCompanionProjectionPlacementMapper().map(source, projection.sheets.single(), projection)
        assertTrue(result.constraints.isEmpty())
        assertEquals(1, result.diagnostics.size)
        assertTrue(result.diagnostics.single().message.contains("does not resolve"))
        assertEquals(5, result.diagnostics.single().sourceSpan.startLine)
    }

    @Test
    fun `lowers sheet route waypoints onto exact connection projection`() {
        val source = parse(
            """
                sheet demo {
                page format A3 landscape
                frame: 17 * 16
                snap: 1
                route "view/connection/C1" via "bend-main" at (24, 16)
                }
            """.trimIndent(),
        )
        val projection = projectionWithConnection()

        val result = SheetCompanionProjectionPlacementMapper().mapRouteConstraints(
            source,
            projection.sheets.single(),
            projection,
        )

        assertTrue(result.diagnostics.isEmpty())
        val constraint = result.connections.single().logicalRouteConstraints.single() as LogicalRouteConstraint.Via
        assertEquals("bend-main", constraint.targetId.value)
        assertEquals(24, constraint.point.column)
        assertEquals(16, constraint.point.row)
    }

    private fun parse(text: String) = assertIs<SheetCompanionParseSuccess>(AthenaSheetCompanionParser().parse("demo.sheet.athena", text)).source

    private fun projection(): ProjectionDocument {
        val sheetId = ProjectionSheetId("view/sheet/1")
        val q1 = ProjectionNode(ProjectionNodeId("node:Q1"), StableSemanticIdentity("entity:Q1"), "Q1", GeometryElementId("q1"))
        val km1 = ProjectionNode(ProjectionNodeId("node:KM1"), StableSemanticIdentity("entity:KM1"), "KM1", GeometryElementId("km1"))
        return ProjectionDocument(
            view = ViewDefinition(
                id = "view:test",
                displayName = "Test",
            ),
            nodes = listOf(q1, km1),
            connections = emptyList(),
            sheets = listOf(ProjectionSheet(sheetId, "Main", 0, subjects = listOf(
                com.engineeringood.athena.projection.ProjectionSheetSubject(q1.semanticId, listOf(q1.projectionId)),
                com.engineeringood.athena.projection.ProjectionSheetSubject(km1.semanticId, listOf(km1.projectionId)),
            ))),
        )
    }

    private fun projectionWithConnection(): ProjectionDocument {
        val base = projection()
        val connectionId = ConnectionProjectionId("view/connection/C1")
        val connection = ConnectionProjection(
            projectionId = connectionId,
            semanticId = StableSemanticIdentity("connection:C1"),
            originGeometryElementId = GeometryElementId("connection:C1"),
            participants = listOf(
                ConnectionProjectionParticipant(
                    ConnectionEndpointRole.SOURCE,
                    ConnectionProjectionEndpoint(ProjectionOccurrencePortId(base.nodes[0].projectionId, StableSemanticIdentity("Q1.out"))),
                ),
                ConnectionProjectionParticipant(
                    ConnectionEndpointRole.SINK,
                    ConnectionProjectionEndpoint(ProjectionOccurrencePortId(base.nodes[1].projectionId, StableSemanticIdentity("KM1.in"))),
                ),
            ),
        )
        val sheet = base.sheets.single().copy(
            subjects = base.sheets.single().subjects + com.engineeringood.athena.projection.ProjectionSheetSubject(
                semanticId = connection.semanticId,
                connectionIds = listOf(connectionId),
            ),
        )
        return base.copy(connections = listOf(connection), sheets = listOf(sheet))
    }
}
