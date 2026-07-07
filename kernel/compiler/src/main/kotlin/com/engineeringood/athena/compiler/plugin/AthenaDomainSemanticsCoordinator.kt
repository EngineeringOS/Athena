package com.engineeringood.athena.compiler.plugin

/** Compiler-owned coordinator that aggregates active domain plugin semantics in deterministic approved-plugin order. */
class AthenaDomainSemanticsCoordinator(
    private val activeDomainPlugins: List<AthenaDomainPlugin>,
) {
    constructor(inventory: AthenaApprovedPluginInventory) : this(
        activeDomainPlugins = inventory
            .attachedPlugins(AthenaExtensionPoint.DOMAIN_SEMANTICS)
            .mapNotNull { it.candidate.plugin as? AthenaDomainPlugin },
    )

    /** Active approved domain plugin ids in the order the compiler will evaluate them. */
    val activePluginIds: List<String> = activeDomainPlugins.map { plugin -> plugin.manifest.pluginId }

    /** True when at least one approved domain plugin is active for the current compiler instance. */
    val hasActivePlugins: Boolean
        get() = activeDomainPlugins.isNotEmpty()

    /** Aggregates domain lowering contributions inside the declared `LOWER` pass. */
    fun lower(source: com.engineeringood.athena.compiler.CompilerSourceDocument): AthenaDomainLoweringContribution {
        val context = AthenaDomainLoweringContext(source)
        return activeDomainPlugins.fold(AthenaDomainLoweringContribution.EMPTY) { aggregate, plugin ->
            val contribution = plugin.lower(context)
            AthenaDomainLoweringContribution(
                components = aggregate.components + contribution.components,
                ports = aggregate.ports + contribution.ports,
                connections = aggregate.connections + contribution.connections,
            )
        }
    }

    /** Aggregates domain validation diagnostics inside the declared `VALIDATE` pass. */
    fun validate(
        document: com.engineeringood.athena.ir.EngineeringDocument,
        context: AthenaPluginValidationContext,
    ): AthenaDomainValidationContribution {
        val diagnostics = activeDomainPlugins.flatMap { plugin ->
            plugin.validate(context.copy(document = document))
        }
        return AthenaDomainValidationContribution(diagnostics)
    }
}
