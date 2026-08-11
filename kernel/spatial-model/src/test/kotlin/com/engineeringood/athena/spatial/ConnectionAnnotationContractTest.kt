package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConnectionAnnotationContractTest {
    private val trace = SpatialSourceTrace(listOf("sheet:main", "connection:C1"), listOf(GeometryElementId("geometry:annotation")))

    @Test
    fun `annotation identity and plan ordering are stable`() {
        val second = annotation("connection:C2", ConnectionAnnotationDisplayRole.KIND, "WIRE")
        val first = annotation("connection:C1", ConnectionAnnotationDisplayRole.KIND, "CONDUCTOR")
        val plan = ConnectionAnnotationPlan(listOf(second, first))

        assertEquals(listOf(first.id.value, second.id.value), plan.annotations.map { it.id.value })
        assertEquals(plan, ConnectionAnnotationPlan(listOf(first, second)))
    }

    @Test
    fun `annotation rejects foreign sheet and out of bounds values`() {
        assertFailsWith<IllegalArgumentException> {
            ConnectionAnnotation(
                id = ConnectionAnnotationId("sheet:other", "connection:C1", ConnectionAnnotationDisplayRole.KIND),
                sheetId = "sheet:main",
                semanticId = StableSemanticIdentity("connection:C1"),
                displayRole = ConnectionAnnotationDisplayRole.KIND,
                value = "WIRE",
                anchor = SpatialPoint(10, 10),
                bounds = SpatialRect(10, 10, 4, 4),
                sourceTrace = trace,
            )
        }
    }

    @Test
    fun `annotation plan rejects duplicate identities`() {
        val item = annotation("connection:C1", ConnectionAnnotationDisplayRole.KIND, "CONDUCTOR")

        assertFailsWith<IllegalArgumentException> {
            ConnectionAnnotationPlan(listOf(item, item))
        }
    }

    private fun annotation(id: String, role: ConnectionAnnotationDisplayRole, value: String) = ConnectionAnnotation(
        id = ConnectionAnnotationId("sheet:main", id, role),
        sheetId = "sheet:main",
        semanticId = StableSemanticIdentity(id),
        displayRole = role,
        value = value,
        anchor = SpatialPoint(20, 20),
        bounds = SpatialRect(16, 16, 8, 8),
        sourceTrace = trace,
    )
}
