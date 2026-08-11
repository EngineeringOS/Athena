package com.engineeringood.athena.packageplatform

data class PartTechnicalField(
    val name: String,
    val value: PackageItemValue,
) {
    init {
        require(name.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
    }
}

data class PartAccessory(
    val accessoryId: PackageItemIdentity,
    val role: String,
) {
    init {
        require(role.isNotBlank())
    }
}

data class PartFacts(
    val manufacturer: String,
    val articleNumber: String,
    val technicalFields: List<PartTechnicalField>,
    val functionTemplates: Set<String> = emptySet(),
    val capabilityContracts: Set<String> = emptySet(),
    val interfaceContracts: Set<String> = emptySet(),
    val accessories: List<PartAccessory> = emptyList(),
) {
    init {
        require(manufacturer.isNotBlank())
        require(articleNumber.isNotBlank())
        require(technicalFields.map(PartTechnicalField::name).distinct().size == technicalFields.size)
        require(accessories.map { it.accessoryId.key }.distinct().size == accessories.size)
    }
}

/** Source-owned reference to one admitted physical or procurement Part item. */
data class PartItemReference(
    val identity: PackageItemIdentity,
) {
    init {
        require(identity.itemId.isNotBlank())
    }
}

/**
 * One projection-independent implementation selection. Its stable identity is the Function and role;
 * replacing the selected Part does not replace this binding.
 */
data class FunctionPartBinding(
    val functionId: String,
    val implementationRole: String,
    val part: PartItemReference,
) {
    init {
        require(functionId.matches(Regex("^function:[A-Za-z][A-Za-z0-9._-]*(?:\\.[A-Za-z][A-Za-z0-9._-]*)+$")))
        require(implementationRole.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
    }

    val bindingId: String
        get() = "$functionId#$implementationRole"
}
