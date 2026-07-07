package com.engineeringood.athena.plugin

import com.engineeringood.athena.layout.ViewDefinition

/** Shared base contract for all Athena plugins published through core-owned extension boundaries. */
interface AthenaPlugin {
    /** Core-owned manifest declaring plugin identity, type, and compatibility metadata. */
    val manifest: AthenaPluginManifest
}

/** Stable domain-schema contract for plugins that publish domain meaning over generic authored structures. */
interface AthenaDomainSchemaContributor : AthenaPlugin {
    /** Generic schema declaration published by this plugin. */
    val domainSchema: AthenaDomainSchema
        get() = AthenaDomainSchema.EMPTY
}

/** Stable validation contribution contract for plugins that publish inspectable domain validation intent. */
interface AthenaValidationContributor : AthenaPlugin {
    /** Validation contribution declarations published by this plugin. */
    val validationContributions: List<AthenaValidationContribution>
        get() = emptyList()
}

/** Stable compiler-stage contribution contract for plugins that publish inspectable pass participation intent. */
interface AthenaCompilerPassContributor : AthenaPlugin {
    /** Compiler-stage contribution declarations published by this plugin. */
    val compilerPassContributions: List<AthenaCompilerPassContribution>
        get() = emptyList()
}

/** Stable renderer-facing contribution contract for plugins that publish inspectable render intent. */
interface AthenaRenderContributor : AthenaPlugin {
    /** Renderer-facing contribution declarations published by this plugin. */
    val renderContributions: List<AthenaRenderContribution>
        get() = emptyList()
}

/**
 * Typed contract for domain plugins.
 *
 * Domain plugins may declare capability metadata, but they may not redefine semantic authority or compiler
 * pass ordering.
 */
interface AthenaDomainPlugin :
    AthenaPlugin,
    AthenaDomainSchemaContributor,
    AthenaValidationContributor,
    AthenaCompilerPassContributor,
    AthenaRenderContributor {
    /** Generic schema declaration published by this domain plugin. */
    override val domainSchema: AthenaDomainSchema
        get() = AthenaDomainSchema.EMPTY

    /** Optional capability keys describing the domain surface the plugin intends to contribute. */
    val domainCapabilities: Set<String>
        get() = emptySet()

    /** Validation contribution declarations published by this domain plugin. */
    override val validationContributions: List<AthenaValidationContribution>
        get() = emptyList()

    /** Compiler-stage contribution declarations published by this domain plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution>
        get() = emptyList()

    /** Renderer-facing contribution declarations published by this domain plugin. */
    override val renderContributions: List<AthenaRenderContribution>
        get() = emptyList()

    /** Domain-owned lowering contribution evaluated inside the compiler-owned lowering stage. */
    fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution = AthenaDomainLoweringContribution.EMPTY

    /** Domain-owned semantic-enrichment contribution evaluated inside the compiler-owned enrichment stage. */
    fun enrichSemantics(context: AthenaSemanticEnrichmentContext): AthenaDomainSemanticEnrichmentContribution =
        AthenaDomainSemanticEnrichmentContribution.EMPTY

    /** Domain-owned semantic validation diagnostics evaluated inside the compiler-owned validation stage. */
    fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult = AthenaPluginValidationResult.EMPTY
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
interface AthenaRendererPlugin : AthenaPlugin, AthenaRenderContributor {
    /** Optional renderer targets the plugin knows how to produce. */
    val rendererTargets: Set<String>
        get() = emptySet()
}
