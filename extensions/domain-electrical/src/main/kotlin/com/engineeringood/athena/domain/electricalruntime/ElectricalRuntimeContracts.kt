package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaDomainConnectionSchema
import com.engineeringood.athena.plugin.AthenaDomainEntitySchema
import com.engineeringood.athena.plugin.AthenaDomainPortSchema
import com.engineeringood.athena.plugin.AthenaDomainPropertySchema
import com.engineeringood.athena.plugin.AthenaDomainPropertyValueKind
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaDomainSchemaSubjectKind
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaExtensionPoint.PRESENTATION_PACKS
import com.engineeringood.athena.plugin.AthenaRenderContribution
import com.engineeringood.athena.plugin.AthenaRenderSurface
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.CoreVersionRange

internal const val ELECTRICAL_DOMAIN_ID = "electrical-runtime"
internal const val ELECTRICAL_VALIDATION_CONTRIBUTION_ID = "electrical-runtime.validation.component-and-port-rules"

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
        PRESENTATION_PACKS,
        AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
        AthenaExtensionPoint.RUNTIME_COMMANDS,
        AthenaExtensionPoint.RUNTIME_VIEWS,
    ),
)

internal val ELECTRICAL_DOMAIN_SCHEMA = AthenaDomainSchema(
    domainId = ELECTRICAL_DOMAIN_ID,
    displayName = "Electrical Runtime",
    description = "Reference real evidence-domain schema for hosted electrical semantics on the Athena JVM-first path.",
    capabilities = setOf(ELECTRICAL_DOMAIN_ID),
    relationWords = ELECTRICAL_RELATION_WORDS,
    entities = listOf(
        electricalComponent("Breaker", "Circuit protection and isolation device."),
        electricalComponent("Contactor", "Electromagnetically operated switching device with partitioned functions."),
        electricalComponent("FuseDisconnector", "Fused isolation device."),
        electricalComponent("Lamp", "Electrical indicator or load component."),
        electricalComponent("LimitSwitch", "Mechanically actuated position switch."),
        electricalComponent("Motor", "Electrical rotating load component."),
        electricalComponent("PowerSource", "Electrical supply source."),
        electricalComponent("ProtectiveEarth", "Protective bonding endpoint."),
        electricalComponent("PushButton", "Manually actuated control switch."),
        electricalComponent("Switch", "Generic electrical switching component retained for existing projects."),
        electricalComponent("Terminal", "Field or panel terminal assembly."),
        electricalComponent("Transformer", "Electrical transformer with isolated winding ports."),
    ),
    properties = listOf(
        AthenaDomainPropertySchema(
            name = "type",
            displayName = "Component type",
            valueKind = AthenaDomainPropertyValueKind.SYMBOL,
            appliesTo = setOf(AthenaDomainSchemaSubjectKind.COMPONENT),
            required = true,
            allowedSymbolValues = VALID_DEVICE_TYPES,
            description = "Proof-domain type selector interpreted by the electrical evidence plugin over generic components.",
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
    connections = listOf(
        AthenaDomainConnectionSchema(
            typeId = "Wire",
            displayName = "Wire",
            description = "Hosted wire connection interpreted over the generic connection surface.",
            sourcePortTypeIds = setOf("electrical-port"),
            targetPortTypeIds = setOf("electrical-port"),
        ),
    ),
)

private fun electricalComponent(typeId: String, description: String) = AthenaDomainEntitySchema(
    typeId = typeId,
    displayName = typeId,
    subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
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

internal val ELECTRICAL_RENDER_CONTRIBUTIONS = listOf(
    AthenaRenderContribution(
        contributionId = "electrical-runtime.render.cabinet",
        displayName = "Electrical cabinet rendering intent",
        description = "Publishes cabinet-view visual intent for hosted electrical structure without taking renderer ownership.",
        viewIds = setOf("cabinet"),
        rendererTargets = setOf("svg", "graph-workbench"),
        surfaceMappings = listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "var(--athena-graph-cabinet-canvas-tint)",
                    "gridMajor" to "var(--athena-graph-cabinet-grid-major)",
                    "gridMinor" to "var(--athena-graph-cabinet-grid-minor)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "var(--athena-graph-cabinet-node-fill)",
                    "stroke" to "var(--athena-graph-cabinet-node-stroke)",
                    "label" to "var(--athena-graph-cabinet-node-label)",
                    "meta" to "var(--athena-graph-cabinet-node-meta)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "var(--athena-graph-cabinet-edge-stroke)",
                ),
            ),
        ),
    ),
    AthenaRenderContribution(
        contributionId = "electrical-runtime.render.wiring",
        displayName = "Electrical wiring rendering intent",
        description = "Publishes wiring-view visual intent for hosted electrical connectivity without taking renderer ownership.",
        viewIds = setOf("wiring"),
        rendererTargets = setOf("svg", "graph-workbench"),
        surfaceMappings = listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "var(--athena-graph-wiring-canvas-tint)",
                    "gridMajor" to "var(--athena-graph-wiring-grid-major)",
                    "gridMinor" to "var(--athena-graph-wiring-grid-minor)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "var(--athena-graph-wiring-node-fill)",
                    "stroke" to "var(--athena-graph-wiring-node-stroke)",
                    "label" to "var(--athena-graph-wiring-node-label)",
                    "meta" to "var(--athena-graph-wiring-node-meta)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "var(--athena-graph-wiring-edge-stroke)",
                ),
            ),
        ),
    ),
)
