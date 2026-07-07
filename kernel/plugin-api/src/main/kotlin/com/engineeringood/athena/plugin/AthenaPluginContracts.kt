package com.engineeringood.athena.plugin

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

/** Shared base contract for all Athena plugins published through core-owned extension boundaries. */
interface AthenaPlugin {
    /** Core-owned manifest declaring plugin identity, type, and compatibility metadata. */
    val manifest: AthenaPluginManifest
}

/**
 * Typed contract for domain plugins.
 *
 * Domain plugins may declare capability metadata, but they may not redefine semantic authority or compiler
 * pass ordering.
 */
interface AthenaDomainPlugin : AthenaPlugin {
    /** Optional capability keys describing the domain surface the plugin intends to contribute. */
    val domainCapabilities: Set<String>
        get() = emptySet()

    /** Domain-owned lowering contribution evaluated inside the compiler-owned lowering stage. */
    fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution = AthenaDomainLoweringContribution.EMPTY

    /** Domain-owned semantic validation diagnostics evaluated inside the compiler-owned validation stage. */
    fun validate(context: AthenaPluginValidationContext): List<SemanticDiagnostic> = emptyList()
}

/**
 * Typed contract for plugins that contribute supported view-definition metadata.
 *
 * View definitions may declare layout intent and presentation policy, but they may not redefine engineering meaning.
 */
interface AthenaViewDefinitionContributor : AthenaPlugin {
    /** Returns supported view definitions in deterministic plugin-owned order. */
    fun viewDefinitions(): List<ViewDefinition> = emptyList()
}

/**
 * Typed contract for rule plugins.
 *
 * Rule plugins may declare rule capability metadata, but they may not replace canonical `Engineering IR`.
 */
interface AthenaRulePlugin : AthenaPlugin {
    /** Optional rule capability keys describing the rule surface the plugin intends to contribute. */
    val ruleCapabilities: Set<String>
        get() = emptySet()
}

/**
 * Typed contract for renderer plugins.
 *
 * Renderer plugins may declare target metadata, but rendering remains downstream of semantic truth.
 */
interface AthenaRendererPlugin : AthenaPlugin {
    /** Optional renderer targets the plugin knows how to produce. */
    val rendererTargets: Set<String>
        get() = emptySet()
}
