package com.engineeringood.athena.component

@JvmInline
value class EngineeringConceptTemplateId(val value: String) {
    init {
        require(value.isNotBlank()) { "Engineering concept template id must not be blank." }
    }
}

@JvmInline
value class EngineeringSemanticType(val value: String) {
    init {
        require(value.isNotBlank()) { "Engineering semantic type must not be blank." }
    }
}

enum class EngineeringConceptPropertyValueKind {
    TEXT,
    SYMBOL,
    BOOLEAN,
    INTEGER,
}

data class EngineeringConceptPropertyTemplate(
    val name: String,
    val valueKind: EngineeringConceptPropertyValueKind,
    val required: Boolean = false,
    val defaultValue: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Engineering concept property name must not be blank." }
    }
}

enum class EngineeringConceptPortDirection {
    IN,
    OUT,
    INOUT,
}

@JvmInline
value class EngineeringSignalOrMedium(val value: String) {
    init {
        require(value.isNotBlank()) { "Engineering signal or medium must not be blank." }
    }
}

data class EngineeringConceptPortTemplate(
    val name: String,
    val direction: EngineeringConceptPortDirection,
    val signalOrMedium: EngineeringSignalOrMedium,
    val terminalNumber: String? = null,
    val required: Boolean = true,
) {
    init {
        require(name.isNotBlank()) { "Engineering concept port name must not be blank." }
    }
}

data class EngineeringConceptRelationshipCapability(
    val relationshipType: String,
    val portNames: Set<String>,
) {
    init {
        require(relationshipType.isNotBlank()) { "Engineering relationship capability type must not be blank." }
        require(portNames.isNotEmpty()) { "Engineering relationship capability must name at least one port." }
    }
}

data class EngineeringConceptTemplateProvenance(
    val domainId: String,
    val source: String,
) {
    init {
        require(domainId.isNotBlank()) { "Engineering concept template domain id must not be blank." }
        require(source.isNotBlank()) { "Engineering concept template source must not be blank." }
    }
}

/** Semantic anatomy used to create one entity; visual representation is resolved downstream. */
data class EngineeringConceptTemplate(
    val templateId: EngineeringConceptTemplateId,
    val conceptId: EngineeringConceptId,
    val semanticType: EngineeringSemanticType,
    val defaultModel: String? = null,
    val propertySchema: List<EngineeringConceptPropertyTemplate> = emptyList(),
    val nestedPorts: List<EngineeringConceptPortTemplate> = emptyList(),
    val relationshipCapabilities: List<EngineeringConceptRelationshipCapability> = emptyList(),
    val provenance: EngineeringConceptTemplateProvenance,
) {
    init {
        require(propertySchema.map { property -> property.name }.distinct().size == propertySchema.size) {
            "Engineering concept template property names must be unique."
        }
        require(nestedPorts.map { port -> port.name }.distinct().size == nestedPorts.size) {
            "Engineering concept template nested port names must be unique."
        }
        val portNames = nestedPorts.map { port -> port.name }.toSet()
        require(relationshipCapabilities.all { capability -> capability.portNames.all(portNames::contains) }) {
            "Engineering relationship capabilities may reference only declared nested ports."
        }
    }
}
