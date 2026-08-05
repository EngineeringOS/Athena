package com.engineeringood.athena.ir

/** Canonical authored project Entity with stable identity independent from display structure. */
data class EngineeringEntity(
    val id: StableSemanticIdentity,
    val name: String,
    val conceptReference: EngineeringDefinitionReference,
    val properties: List<EngineeringProperty>,
    val structureAssignments: List<EngineeringStructureAssignment>,
    val provenance: SourceProvenance,
)

/** Typed Entity-only subject reference used by Function ownership. */
data class EngineeringEntityReference(val reference: EngineeringReference) {
    init {
        require(reference.authoredPath.isNotEmpty()) { "Engineering Entity reference requires an authored path" }
    }
}

/** Typed Function-only subject reference used by Port ownership and later Relationship participation. */
data class EngineeringFunctionReference(val reference: EngineeringReference) {
    init {
        require(reference.authoredPath.isNotEmpty()) { "Engineering Function reference requires an authored path" }
    }
}

/** Entity-owned functional partition; ownership establishes identity and containment only. */
data class EngineeringFunction(
    val id: StableSemanticIdentity,
    val owner: EngineeringEntityReference,
    val name: String,
    val roleReference: EngineeringDefinitionReference,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Exact subject level that owns one Engineering Port. */
sealed interface EngineeringPortOwner {
    val reference: EngineeringReference

    data class Entity(val entity: EngineeringEntityReference) : EngineeringPortOwner {
        override val reference: EngineeringReference
            get() = entity.reference
    }

    data class Function(val function: EngineeringFunctionReference) : EngineeringPortOwner {
        override val reference: EngineeringReference
            get() = function.reference
    }
}

/** Cross-domain direction of admitted movement through one Port. */
enum class EngineeringPortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL,
}

/** Inclusive number of Relationship participations admitted at one Port; null maximum means unbounded. */
data class EngineeringPortCardinality(
    val minimum: Int,
    val maximum: Int?,
) {
    init {
        require(minimum >= 0) { "Engineering Port minimum cardinality must not be negative" }
        require(maximum == null || maximum >= minimum) {
            "Engineering Port maximum cardinality must be at least its minimum"
        }
    }
}

/** Optional package-defined terminal or interface designation attached to one exact Port. */
data class EngineeringInterfaceDesignation(
    val definitionReference: EngineeringDefinitionReference,
    val value: EngineeringValue,
    val provenance: SourceProvenance,
)

/** Canonical Entity- or Function-owned project interface. */
data class EngineeringPort(
    val id: StableSemanticIdentity,
    val owner: EngineeringPortOwner,
    val name: String,
    val direction: EngineeringPortDirection,
    val admittedFlowReferences: List<EngineeringDefinitionReference>,
    val cardinality: EngineeringPortCardinality,
    val interfaceDesignation: EngineeringInterfaceDesignation?,
    val properties: List<EngineeringProperty>,
    val provenance: SourceProvenance,
)

/** Domain-neutral assignment to one package-defined structure aspect. */
data class EngineeringStructureAssignment(
    val aspectReference: EngineeringDefinitionReference,
    val value: EngineeringValue,
    val displayDesignation: String?,
    val provenance: SourceProvenance,
)
