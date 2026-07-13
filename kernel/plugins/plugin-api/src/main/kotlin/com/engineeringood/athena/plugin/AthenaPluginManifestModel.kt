package com.engineeringood.athena.plugin

/** Core-owned plugin type vocabulary for hosted Athena plugins. */
enum class AthenaPluginType {
    DOMAIN,
    RULE,
    RENDERER,
}

/** Core-owned extension-point vocabulary for approved plugin attachment. */
enum class AthenaExtensionPoint {
    DOMAIN_SEMANTICS,
    VIEW_DEFINITIONS,
    PRESENTATION_PACKS,
    RULE_EVALUATION,
    RENDERING,
    SEMANTIC_REVIEW_ENRICHMENT,
    RUNTIME_COMMANDS,
    RUNTIME_VIEWS,
}

/** Core-owned ownership boundaries that plugins are never allowed to claim. */
enum class AthenaPluginOwnershipClaim {
    ENGINEERING_IR,
    WORKSPACE_LIFECYCLE,
    PROJECT_LIFECYCLE,
    RUNTIME_ORCHESTRATION,
    DIRECT_SEMANTIC_MUTATION,
}

/** Minimal core-version compatibility range declared by a plugin manifest. */
data class CoreVersionRange(
    val minimumInclusive: String,
    val maximumInclusive: String? = null,
)

/** Core-owned manifest model required for every Athena plugin declaration. */
data class AthenaPluginManifest(
    val pluginId: String,
    val pluginVersion: String,
    val pluginType: AthenaPluginType,
    val coreCompatibility: CoreVersionRange,
    val requiredExtensionPoints: Set<AthenaExtensionPoint>,
    val ownershipClaims: Set<AthenaPluginOwnershipClaim> = emptySet(),
)
