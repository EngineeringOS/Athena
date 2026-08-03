package com.engineeringood.athena.presentation

import kotlin.test.Test
import kotlin.test.assertFalse

class PresentationNamingHygieneTest {
    @Test
    fun `active presentation connector fields avoid stale intent naming`() {
        val fieldNames = PresentationConnector::class.java.declaredFields.map { field -> field.name }

        assertFalse(
            fieldNames.any { name -> name.contains("Intent") },
            "Presentation connector fields must use direct M39 names: $fieldNames",
        )
    }

    @Test
    fun `active representation trace names avoid vague evidence naming`() {
        val names = listOf(
            PresentationRepresentationFact::class.java.simpleName,
            PresentationPackageTrace::class.java.simpleName,
            PresentationRepresentationFact::class.java.declaredFields.joinToString(" ") { field -> field.name },
            GraphicOccurrenceTraceTable::class.java.declaredFields.joinToString(" ") { field -> field.name },
            GraphicOccurrenceTraceStats::class.java.simpleName,
            CabinetSelectionTrace::class.java.simpleName,
            CabinetEditRequest::class.java.simpleName,
            CabinetEditPath::class.java.simpleName,
        )

        assertFalse(
            names.any { name -> name.contains("Evidence") },
            "Active Presentation package trace names must be direct: $names",
        )
    }
}
