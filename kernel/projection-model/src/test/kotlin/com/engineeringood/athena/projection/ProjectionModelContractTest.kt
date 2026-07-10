package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ViewDefinition
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectionModelContractTest {
    @Test
    fun `projection document keeps layout owned view definition and geometry origin references`() {
        val document = ProjectionDocument(
            view = ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
                layoutIntent = LayoutIntent.STRUCTURAL,
                description = "Structural view",
            ),
            canvasWidth = 480,
            canvasHeight = 172,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/node/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 40, y = 60, width = 140, height = 72),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
            ),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("cabinet/connection/PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    start = ProjectionPoint(x = 104, y = 86),
                    end = ProjectionPoint(x = 316, y = 86),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                ),
            ),
            labels = listOf(
                ProjectionLabel(
                    projectionId = ProjectionLabelId("cabinet/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    bounds = ProjectionBounds(x = 56, y = 78, width = 48, height = 16),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/label/port_PLC1_out"),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("cabinet/geometry/box/component_PLC1", document.nodes.single().originGeometryElementId.value)
        assertEquals(
            "cabinet/geometry/path/connection_PLC1_out_M1_in",
            document.connections.single().originGeometryElementId.value,
        )
        assertEquals("cabinet/geometry/label/port_PLC1_out", document.labels.single().originGeometryElementId.value)
    }
}
