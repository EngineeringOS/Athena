package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*

data class PlaceholderResolutionResult(val values: Map<String, PackageItemValue>?, val diagnostics: List<PlaceholderDiagnostic>) {
    val isValid get() = values != null && diagnostics.isEmpty()
}

data class PlaceholderDiagnostic(val subject: String, val problem: String, val correction: String, val code: String)

object PlaceholderResolver {
    fun resolve(schema: PlaceholderSchema, assignments: List<PlaceholderAssignment>): PlaceholderResolutionResult {
        val byName = assignments.groupBy(PlaceholderAssignment::name)
        val diagnostics = mutableListOf<PlaceholderDiagnostic>()
        schema.placeholders.forEach { placeholder ->
            val matches = byName[placeholder.name].orEmpty()
            if (matches.isEmpty()) diagnostics += PlaceholderDiagnostic(placeholder.name, "Representation placeholder has no value.", "Provide a ${placeholder.valueType.name.lowercase()} value for `${placeholder.targetPath}`.", "placeholder.value.missing")
            if (matches.size > 1) diagnostics += PlaceholderDiagnostic(placeholder.name, "Representation placeholder has multiple values.", "Provide exactly one value for `${placeholder.targetPath}`.", "placeholder.value.duplicate")
            matches.singleOrNull()?.let { assignment ->
                if (!matchesType(assignment.value, placeholder.valueType)) diagnostics += PlaceholderDiagnostic(placeholder.name, "Representation placeholder value has the wrong type.", "Provide a ${placeholder.valueType.name.lowercase()} value for `${placeholder.targetPath}`.", "placeholder.value.type")
            }
        }
        val unknown = byName.keys - schema.placeholders.map(RepresentationPlaceholder::name).toSet()
        unknown.sorted().forEach { diagnostics += PlaceholderDiagnostic(it, "Placeholder is not declared by the representation schema.", "Declare `$it` as a representation placeholder before assigning it.", "placeholder.name.unknown") }
        val ordered = diagnostics.sortedWith(compareBy(PlaceholderDiagnostic::subject, PlaceholderDiagnostic::code))
        return if (ordered.isEmpty()) PlaceholderResolutionResult(assignments.associate { it.name to it.value }.toSortedMap(), emptyList()) else PlaceholderResolutionResult(null, ordered)
    }

    private fun matchesType(value: PackageItemValue, type: PlaceholderValueType) = when (type) {
        PlaceholderValueType.TEXT -> value is PackageItemValue.TextValue
        PlaceholderValueType.NUMBER -> value is PackageItemValue.NumberValue
        PlaceholderValueType.BOOLEAN -> value is PackageItemValue.BooleanValue
    }
}

