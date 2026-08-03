package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import java.util.Collections

data class SpatialDrawingPoint(
    val x: Int,
    val y: Int,
)

data class SpatialRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "Spatial rectangle width must be positive." }
        require(height > 0) { "Spatial rectangle height must be positive." }
        require(x.toLong() + width.toLong() <= Int.MAX_VALUE) {
            "Spatial rectangle horizontal extent must stay within Int drawing units."
        }
        require(y.toLong() + height.toLong() <= Int.MAX_VALUE) {
            "Spatial rectangle vertical extent must stay within Int drawing units."
        }
    }

    val origin: SpatialDrawingPoint
        get() = SpatialDrawingPoint(x = x, y = y)

    val right: Int
        get() = x + width

    val bottom: Int
        get() = y + height

    fun isInside(container: SpatialRect): Boolean =
        x >= container.x && y >= container.y && right <= container.right && bottom <= container.bottom
}

class SpatialPlacementReason(constraints: List<String>) {
    val constraints: List<String> = constraints.immutableCopy()

    init {
        require(this.constraints.isNotEmpty()) { "Spatial placement reason must name at least one constraint." }
        require(this.constraints.all(String::isNotBlank)) { "Spatial placement constraints must not be blank." }
    }

    val text: String
        get() = constraints.joinToString(separator = "; ")

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialPlacementReason && constraints == other.constraints

    override fun hashCode(): Int = constraints.hashCode()

    override fun toString(): String = "SpatialPlacementReason(constraints=$constraints)"
}

class SpatialSourceTrace(
    projectionIds: List<String>,
    geometryElementIds: List<GeometryElementId>,
) {
    val projectionIds: List<String> = projectionIds.immutableCopy()
    val geometryElementIds: List<GeometryElementId> = geometryElementIds.immutableCopy()

    init {
        require(this.projectionIds.isNotEmpty()) { "Spatial source trace must contain a Projection identity." }
        require(this.projectionIds.all(String::isNotBlank)) { "Spatial source trace Projection identities must not be blank." }
        require(this.geometryElementIds.isNotEmpty()) { "Spatial source trace must contain a geometry source identity." }
        require(this.geometryElementIds.all { geometryId -> geometryId.value.isNotBlank() }) {
            "Spatial source trace geometry identities must not be blank."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialSourceTrace &&
            projectionIds == other.projectionIds && geometryElementIds == other.geometryElementIds

    override fun hashCode(): Int = 31 * projectionIds.hashCode() + geometryElementIds.hashCode()

    override fun toString(): String =
        "SpatialSourceTrace(projectionIds=$projectionIds, geometryElementIds=$geometryElementIds)"
}

data class SpatialOccurrenceId(
    val sheetId: String,
    val projectionId: String,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial occurrence Sheet identity must not be blank." }
        require(projectionId.isNotBlank()) { "Spatial occurrence Projection identity must not be blank." }
    }
}

data class SpatialOccurrenceGeometry(
    val occurrenceId: SpatialOccurrenceId,
    val subjectId: StableSemanticIdentity,
    val sheetId: String,
    val regionId: String,
    val rectangle: SpatialRect,
    val placementReason: SpatialPlacementReason,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial occurrence Sheet identity must not be blank." }
        require(occurrenceId.sheetId == sheetId) {
            "Spatial occurrence identity must name its owning Sheet."
        }
        require(regionId.isNotBlank()) { "Spatial occurrence Region identity must not be blank." }
    }
}

data class SpatialDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(subject.isNotBlank()) { "Spatial diagnostic subject must not be blank." }
        require(problem.isNotBlank()) { "Spatial diagnostic problem must not be blank." }
        require(correction.isNotBlank()) { "Spatial diagnostic correction must not be blank." }
    }
}

private fun <T> List<T>.immutableCopy(): List<T> = Collections.unmodifiableList(toList())
