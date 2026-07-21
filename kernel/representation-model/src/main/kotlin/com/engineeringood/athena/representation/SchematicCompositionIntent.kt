package com.engineeringood.athena.representation

@JvmInline
value class SchematicSpatialIntentReference(val value: String) {
    init {
        require(value.isNotBlank()) { "Schematic spatial intent reference must not be blank." }
    }
}

@JvmInline
value class SchematicCompositionFactId(val value: String) {
    init {
        require(value.isNotBlank()) { "Schematic composition fact id must not be blank." }
    }
}

enum class SchematicCompositionFactKind {
    RAIL,
    COLUMN,
    TERMINAL_GROUP,
    ROUTE_LANE,
    REFERENCE_ZONE,
    LABEL_BAND,
    ALIGNMENT_GROUP,
}

data class SchematicCompositionInput(
    val occurrences: List<RepresentationOccurrence>,
    val boundsByOccurrence: Map<RepresentationOccurrenceId, PresentationBounds>,
    val terminalAnchorCountByOccurrence: Map<RepresentationOccurrenceId, Int>,
    val spatialIntentReferences: List<SchematicSpatialIntentReference>,
) {
    companion object {
        fun empty(): SchematicCompositionInput = SchematicCompositionInput(
            occurrences = emptyList(),
            boundsByOccurrence = emptyMap(),
            terminalAnchorCountByOccurrence = emptyMap(),
            spatialIntentReferences = emptyList(),
        )
    }
}

data class SchematicCompositionFact(
    val factId: SchematicCompositionFactId,
    val kind: SchematicCompositionFactKind,
    val membershipId: CompositionIntentMembershipId,
    val occurrenceIds: List<RepresentationOccurrenceId>,
    val spatialIntentReferences: List<SchematicSpatialIntentReference>,
) {
    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "factId" to factId.value,
        "kind" to kind.name,
        "membershipId" to membershipId.value,
        "occurrenceIds" to occurrenceIds.joinToString(separator = "|") { id -> id.value },
        "spatialIntentReferences" to spatialIntentReferences.joinToString(separator = "|") { reference -> reference.value },
    )
}

data class SchematicCompositionPlan(
    val facts: List<SchematicCompositionFact>,
) {
    fun toTransportPayload(): List<Map<String, String>> = facts.map { fact -> fact.toTransportMap() }
}

class SchematicCompositionIntentCompiler {
    fun plan(input: SchematicCompositionInput): SchematicCompositionPlan {
        if (input.occurrences.isEmpty()) {
            return SchematicCompositionPlan(emptyList())
        }
        val occurrenceIds = input.occurrences
            .filter { occurrence -> occurrence.occurrenceId in input.boundsByOccurrence }
            .filter { occurrence -> input.terminalAnchorCountByOccurrence.getOrDefault(occurrence.occurrenceId, 0) >= 0 }
            .map { occurrence -> occurrence.occurrenceId }
            .sortedBy { id -> id.value }
        val facts = SchematicCompositionFactKind.entries.map { kind ->
            SchematicCompositionFact(
                factId = SchematicCompositionFactId("composition:${kind.name.lowercase()}"),
                kind = kind,
                membershipId = CompositionIntentMembershipId("composition:${kind.name.lowercase()}"),
                occurrenceIds = occurrenceIds,
                spatialIntentReferences = input.spatialIntentReferences.sortedBy { reference -> reference.value },
            )
        }
        return SchematicCompositionPlan(facts)
    }
}
