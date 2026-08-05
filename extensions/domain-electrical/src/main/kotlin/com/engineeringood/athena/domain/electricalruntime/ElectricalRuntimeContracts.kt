package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaDomainRelationshipSchema
import com.engineeringood.athena.plugin.AthenaDomainEntitySchema
import com.engineeringood.athena.plugin.AthenaDomainPortSchema
import com.engineeringood.athena.plugin.AthenaDomainPropertySchema
import com.engineeringood.athena.plugin.AthenaDomainPropertyValueKind
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaDomainSchemaSubjectKind
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.CoreVersionRange

internal const val ELECTRICAL_DOMAIN_ID = "electrical-runtime"
internal const val ELECTRICAL_VALIDATION_CONTRIBUTION_ID = "electrical-runtime.validation.entity-and-port-rules"

internal val VALID_DEVICE_TYPES = setOf(
    "Breaker",
    "Contactor",
    "FuseDisconnector",
    "Lamp",
    "LimitSwitch",
    "Motor",
    "PowerSource",
    "ProtectiveEarth",
    "PushButton",
    "Switch",
    "Terminal",
    "Transformer",
)

internal val ELECTRICAL_RUNTIME_CAPABILITIES: Set<String> = setOf(ELECTRICAL_DOMAIN_ID)
internal val ELECTRICAL_RELATION_WORD_LIST: List<String> = listOf("power", "control", "earth")
internal val ELECTRICAL_RELATION_WORDS: Set<String> = ELECTRICAL_RELATION_WORD_LIST.toSet()

internal val ELECTRICAL_RUNTIME_MANIFEST = AthenaPluginManifest(
    pluginId = "com.engineeringood.athena.domain.electrical-runtime",
    pluginVersion = "0.0.1-SNAPSHOT",
    pluginType = AthenaPluginType.DOMAIN,
    coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
    requiredExtensionPoints = setOf(
        AthenaExtensionPoint.DOMAIN_SEMANTICS,
        AthenaExtensionPoint.VIEW_DEFINITIONS,
        AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
    ),
)

internal val ELECTRICAL_DOMAIN_SCHEMA = AthenaDomainSchema(
    domainId = ELECTRICAL_DOMAIN_ID,
    displayName = "Electrical Runtime",
    description = "Reference real evidence-domain schema for hosted electrical semantics on the Athena JVM-first path.",
    capabilities = setOf(ELECTRICAL_DOMAIN_ID),
    relationWords = ELECTRICAL_RELATION_WORDS,
    entities = listOf(
        electricalEntity("Breaker", "Circuit protection and isolation Entity."),
        electricalEntity("Contactor", "Electromagnetically operated switching Entity with partitioned Functions."),
        electricalEntity("FuseDisconnector", "Fused isolation Entity."),
        electricalEntity("Lamp", "Electrical indicator or load Entity."),
        electricalEntity("LimitSwitch", "Mechanically actuated position switch."),
        electricalEntity("Motor", "Electrical rotating load Entity."),
        electricalEntity("PowerSource", "Electrical supply source."),
        electricalEntity("ProtectiveEarth", "Protective bonding endpoint."),
        electricalEntity("PushButton", "Manually actuated control switch."),
        electricalEntity("Switch", "Generic electrical switching Entity."),
        electricalEntity("Terminal", "Field or panel terminal assembly."),
        electricalEntity("Transformer", "Electrical transformer with isolated winding Ports."),
    ),
    properties = listOf(
        AthenaDomainPropertySchema(
            name = "type",
            displayName = "Entity type",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.ENTITY),
            required = true,
            allowedSymbolValues = VALID_DEVICE_TYPES,
            description = "Electrical type selector interpreted by the domain plugin over generic Entities.",
        ),
        AthenaDomainPropertySchema(
            name = "direction",
            displayName = "Port direction",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = true,
            allowedSymbolValues = setOf("in", "out"),
            description = "Direction metadata interpreted by the electrical evidence plugin over generic ports.",
        ),
        AthenaDomainPropertySchema(
            name = "signal",
            displayName = "Port signal",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = false,
            description = "Optional signal metadata used to validate port compatibility and wiring intent.",
        ),
        AthenaDomainPropertySchema(
            name = "terminal",
            displayName = "Physical terminal identity",
            valueKind = AthenaDomainPropertyValueKind.TEXT,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.PORT),
            required = false,
            description = "Authored terminal designation preserved as engineering truth.",
        ),
    ),
    ports = listOf(
        AthenaDomainPortSchema(
            typeId = "electrical-port",
            displayName = "Electrical port",
            description = "Generic hosted electrical port for the first evidence domain.",
            propertyNames = setOf("direction", "signal", "terminal"),
            allowedDirections = setOf("in", "out"),
        ),
    ),
    relationships = listOf(
        AthenaDomainRelationshipSchema(
            typeId = "Wire",
            displayName = "Wire",
            description = "Hosted wire relationship interpreted over typed relationship participants.",
            sourcePortTypeIds = setOf("electrical-port"),
            targetPortTypeIds = setOf("electrical-port"),
        ),
    ),
)

private fun electricalEntity(typeId: String, description: String) = AthenaDomainEntitySchema(
    typeId = typeId,
    displayName = typeId,
    subjectKind = AthenaDomainSchemaSubjectKind.ENTITY,
    description = description,
    propertyNames = setOf("type"),
    portTypeIds = setOf("electrical-port"),
)

internal val ELECTRICAL_VALIDATION_CONTRIBUTIONS = listOf(
    AthenaValidationContribution(
        contributionId = ELECTRICAL_VALIDATION_CONTRIBUTION_ID,
        displayName = "Electrical property and compatibility validation",
        description = "Validates supported component types, port direction and signal metadata, and connection compatibility.",
    ),
)

internal val ELECTRICAL_COMPILER_PASS_CONTRIBUTIONS = listOf(
    AthenaCompilerPassContribution(
        contributionId = "electrical-runtime.lower.devices-ports-connections",
        stage = AthenaCompilerContributionStage.LOWER,
        displayName = "Electrical lowering",
        description = "Interprets generic devices, ports, and connections as hosted electrical evidence semantics during lowering.",
    ),
    AthenaCompilerPassContribution(
        contributionId = "electrical-runtime.validate.compatibility-and-property-rules",
        stage = AthenaCompilerContributionStage.VALIDATE,
        displayName = "Electrical validation",
        description = "Applies hosted electrical compatibility and property validation during the compiler-owned validate stage.",
    ),
)
