package com.engineeringood.athena.spatial

data class SpatialGridDefinition(
    val sheetId: String,
    val gridId: String,
    val drawingArea: SpatialRect,
    val rows: Int,
    val columns: Int,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial grid Sheet identity must not be blank." }
        require(gridId.isNotBlank()) { "Spatial grid identity must not be blank." }
        require(rows in 1..MAX_SUPPORTED_ROWS) {
            "Spatial grid rows must be between 1 and $MAX_SUPPORTED_ROWS."
        }
        require(columns > 0) { "Spatial grid columns must be positive." }
    }

    companion object {
        const val MAX_SUPPORTED_ROWS: Int = 18_278
    }
}

sealed interface SpatialGridReferenceSubject {
    val sheetId: String
    val projectionId: String

    data class Occurrence(
        val occurrenceId: SpatialOccurrenceId,
    ) : SpatialGridReferenceSubject {
        override val sheetId: String = occurrenceId.sheetId
        override val projectionId: String = occurrenceId.projectionId
    }

    data class Construct(
        val constructId: SpatialConstructId,
    ) : SpatialGridReferenceSubject {
        override val sheetId: String = constructId.sheetId
        override val projectionId: String = constructId.projectionId
    }
}

data class SpatialGridReferenceId(
    val sheetId: String,
    val subject: SpatialGridReferenceSubject,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial Grid Reference Sheet identity must not be blank." }
        require(subject.sheetId == sheetId) {
            "Spatial Grid Reference identity subject must belong to its owning Sheet."
        }
    }
}

data class SpatialGridReference(
    val gridReferenceId: SpatialGridReferenceId,
    val sheetId: String,
    val gridId: String,
    val subject: SpatialGridReferenceSubject,
    val rowIndex: Int,
    val rowLabel: String,
    val columnIndex: Int,
    val columnNumber: Int,
    val cellReference: String,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(gridReferenceId.sheetId == sheetId) {
            "Spatial Grid Reference identity must name its owning Sheet."
        }
        require(gridReferenceId.subject == subject) {
            "Spatial Grid Reference identity must name its subject."
        }
        require(subject.sheetId == sheetId) {
            "Spatial Grid Reference subject must belong to its owning Sheet."
        }
        require(gridId.isNotBlank()) { "Spatial Grid Reference grid identity must not be blank." }
        require(rowIndex in 0 until SpatialGridDefinition.MAX_SUPPORTED_ROWS) {
            "Spatial Grid Reference row index must be between 0 and " +
                "${SpatialGridDefinition.MAX_SUPPORTED_ROWS - 1}."
        }
        require(rowLabel == spatialGridRowLabel(rowIndex)) {
            "Spatial Grid Reference row label must match its zero-based row index."
        }
        require(columnIndex in 0 until Int.MAX_VALUE) {
            "Spatial Grid Reference column index must be between 0 and ${Int.MAX_VALUE - 1}."
        }
        require(columnNumber > 0 && columnNumber.toLong() == columnIndex.toLong() + 1L) {
            "Spatial Grid Reference column number must be one-based."
        }
        require(cellReference == "$rowLabel$columnNumber") {
            "Spatial Grid Reference cell must use row label followed by column number."
        }
    }
}

fun spatialGridRowLabel(rowIndex: Int): String {
    require(rowIndex in 0 until SpatialGridDefinition.MAX_SUPPORTED_ROWS) {
        "Spatial grid row index must be between 0 and ${SpatialGridDefinition.MAX_SUPPORTED_ROWS - 1}."
    }
    var remaining = rowIndex.toLong() + 1L
    return buildString {
        while (remaining > 0L) {
            val digit = ((remaining - 1L) % 26L).toInt()
            append(('A'.code + digit).toChar())
            remaining = (remaining - 1L) / 26L
        }
    }.reversed()
}
