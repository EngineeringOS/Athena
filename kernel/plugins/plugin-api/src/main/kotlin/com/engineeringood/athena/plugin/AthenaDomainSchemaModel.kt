package com.engineeringood.athena.plugin

/** Generic canonical subject kinds that a plugin-declared schema may interpret without extending grammar. */
enum class AthenaDomainSchemaSubjectKind {
    COMPONENT,
    PORT,
    CONNECTION,
}

/** Typed property value kinds a plugin may declare for its domain schema. */
enum class AthenaDomainPropertyValueKind {
    SYMBOL,
    TEXT,
}

/** Root schema declaration published by one hosted domain through the stable plugin API. */
data class AthenaDomainSchema(
    val domainId: String,
    val displayName: String,
    val description: String = "",
    val capabilities: Set<String> = emptySet(),
    val entities: List<AthenaDomainEntitySchema> = emptyList(),
    val properties: List<AthenaDomainPropertySchema> = emptyList(),
    val ports: List<AthenaDomainPortSchema> = emptyList(),
    val connections: List<AthenaDomainConnectionSchema> = emptyList(),
) {
    companion object {
        /** Empty schema used when a plugin does not declare domain schema metadata yet. */
        val EMPTY: AthenaDomainSchema = AthenaDomainSchema(
            domainId = "",
            displayName = "",
        )
    }
}

/** Generic entity type declaration contributed by one hosted domain. */
data class AthenaDomainEntitySchema(
    val typeId: String,
    val displayName: String,
    val subjectKind: AthenaDomainSchemaSubjectKind,
    val description: String = "",
    val propertyNames: Set<String> = emptySet(),
    val portTypeIds: Set<String> = emptySet(),
)

/** Generic property declaration contributed by one hosted domain. */
data class AthenaDomainPropertySchema(
    val name: String,
    val displayName: String,
    val valueKind: AthenaDomainPropertyValueKind,
    val appliesTo: Set<AthenaDomainSchemaSubjectKind>,
    val required: Boolean = false,
    val allowedSymbolValues: Set<String> = emptySet(),
    val description: String = "",
)

/** Generic port type declaration contributed by one hosted domain. */
data class AthenaDomainPortSchema(
    val typeId: String,
    val displayName: String,
    val description: String = "",
    val propertyNames: Set<String> = emptySet(),
    val allowedDirections: Set<String> = emptySet(),
)

/** Generic connection type declaration contributed by one hosted domain. */
data class AthenaDomainConnectionSchema(
    val typeId: String,
    val displayName: String,
    val description: String = "",
    val propertyNames: Set<String> = emptySet(),
    val sourcePortTypeIds: Set<String> = emptySet(),
    val targetPortTypeIds: Set<String> = emptySet(),
)
