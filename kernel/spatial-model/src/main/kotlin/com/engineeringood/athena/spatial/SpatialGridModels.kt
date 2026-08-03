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
        require(rowIndex >= 0) { "Spatial Grid Reference row index must not be negative." }
        require(rowLabel.isNotBlank()) { "Spatial Grid Reference row label must not be blank." }
        require(columnIndex >= 0) { "Spatial Grid Reference column index must not be negative." }
        require(columnNumber == columnIndex + 1) {
            "Spatial Grid Reference column number must be one-based."
        }
        require(cellReference == "$rowLabel$columnNumber") {
            "Spatial Grid Reference cell must use row label followed by column number."
        }
    }
}
