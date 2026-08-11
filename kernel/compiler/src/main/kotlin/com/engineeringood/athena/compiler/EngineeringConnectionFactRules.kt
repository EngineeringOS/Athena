package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.ir.ConnectionKind

/** Closed, source-owned physical fact vocabulary for M46 connectivity. */
internal object EngineeringConnectionFactRules {
    val permittedFacts: Set<String> = setOf(
        "crossSection",
        "colorCode",
        "conductorType",
        "shielding",
        "sourceTermination",
        "targetTermination",
        "requiredLength",
    )

    fun violations(kind: ConnectionKind, properties: List<PropertyAssignment>): List<String> {
        val names = properties.map { it.name }
        val violations = names.filterNot(permittedFacts::contains).map { "unknown:$it" }.toMutableList()
        if (kind == ConnectionKind.WIRE || kind == ConnectionKind.CABLE_CORE) {
            if ("crossSection" !in names) violations += "required:crossSection"
        }
        if (kind == ConnectionKind.CABLE_CORE && "conductorType" !in names) {
            violations += "required:conductorType"
        }
        properties.forEach { property ->
            val valid = when (property.name) {
                "crossSection", "requiredLength" -> property.value is ScalarValue.Quantity
                "colorCode", "conductorType", "sourceTermination", "targetTermination" ->
                    property.value is ScalarValue.Symbol || property.value is ScalarValue.Text
                "shielding" -> property.value is ScalarValue.Boolean || property.value is ScalarValue.Symbol
                else -> true
            }
            if (!valid) violations += "type:${property.name}"
        }
        return violations.distinct()
    }
}
