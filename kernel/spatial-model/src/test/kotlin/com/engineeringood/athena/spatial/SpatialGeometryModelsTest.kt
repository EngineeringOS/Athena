package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class SpatialGeometryModelsTest {
    @Test
    fun `spatial rectangle rejects horizontal and vertical extent overflow`() {
        assertFailsWith<IllegalArgumentException> {
            SpatialRect(x = Int.MAX_VALUE, y = 0, width = 1, height = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialRect(x = 0, y = Int.MAX_VALUE, width = 1, height = 1)
        }
    }


    @Test
    fun `placement reason and source trace defensively copy input lists`() {
        val constraints = mutableListOf("Drawing Area")
        val projectionIds = mutableListOf("projection/node/one")
        val geometryIds = mutableListOf(GeometryElementId("origin:one"))

        val reason = SpatialPlacementReason(constraints)
        val trace = SpatialSourceTrace(projectionIds, geometryIds)
        constraints.clear()
        projectionIds.clear()
        geometryIds.clear()

        assertNotSame(constraints, reason.constraints)
        assertNotSame(projectionIds, trace.projectionIds)
        assertNotSame(geometryIds, trace.geometryElementIds)
        assertEquals(listOf("Drawing Area"), reason.constraints)
        assertEquals(listOf("projection/node/one"), trace.projectionIds)
        assertEquals(listOf(GeometryElementId("origin:one")), trace.geometryElementIds)
    }

    @Test
    fun `source trace rejects blank geometry identities`() {
        assertFailsWith<IllegalArgumentException> {
            SpatialSourceTrace(
                projectionIds = listOf("projection/node/one"),
                geometryElementIds = listOf(GeometryElementId("")),
            )
        }
    }
}
