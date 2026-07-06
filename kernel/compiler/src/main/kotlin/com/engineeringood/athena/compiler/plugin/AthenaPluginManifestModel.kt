package com.engineeringood.athena.compiler.plugin

/** Core-owned plugin type vocabulary for the first M0 plugin classes. */
enum class AthenaPluginType {
    DOMAIN,
    RULE,
    RENDERER,
}

/** Core-owned extension-point vocabulary for plugin attachment in the M0 compiler substrate. */
enum class AthenaExtensionPoint {
    DOMAIN_SEMANTICS,
    RULE_EVALUATION,
    RENDERING,
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
