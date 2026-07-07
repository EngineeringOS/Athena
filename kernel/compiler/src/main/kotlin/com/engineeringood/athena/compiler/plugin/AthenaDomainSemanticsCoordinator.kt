package com.engineeringood.athena.compiler.plugin

import com.engineeringood.athena.compiler.CompilerSourceDocument
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContributor
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainValidationAttribution
import com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution
import com.engineeringood.athena.plugin.AthenaDomainValidationContribution
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaSourceDocument
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory

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

    /** Declared compiler-stage contribution ids grouped by stage in deterministic approved-plugin order. */
    private val declaredStageContributionIds: Map<AthenaCompilerContributionStage, List<String>> =
        AthenaCompilerContributionStage.entries.associateWith { stage ->
            activeDomainPlugins.flatMap { plugin ->
                val contributor = plugin as? AthenaCompilerPassContributor ?: return@flatMap emptyList()
                contributor.compilerPassContributions
                    .filter { contribution -> contribution.stage == stage }
                    .map { contribution -> contribution.contributionId }
            }
        }

    /** Domain plugins that are allowed to participate in each compiler stage. */
    private val stageParticipants: Map<AthenaCompilerContributionStage, List<AthenaDomainPlugin>> =
        AthenaCompilerContributionStage.entries.associateWith { stage ->
            activeDomainPlugins.filter { plugin ->
                declaredStageContributionIds.getValue(stage).any { contributionId ->
                    plugin.compilerPassContributions.any { contribution ->
                        contribution.stage == stage && contribution.contributionId == contributionId
                    }
                }
            }
        }

    /** True when at least one approved domain plugin is active for the current compiler instance. */
    val hasActivePlugins: Boolean
        get() = activeDomainPlugins.isNotEmpty()

    /** Declared contribution ids for the supplied compiler [stage] in deterministic approved-plugin order. */
    fun declaredContributionIds(stage: AthenaCompilerContributionStage): List<String> = declaredStageContributionIds.getValue(stage)

    /** True when at least one approved plugin is allowed to execute inside the supplied compiler [stage]. */
    fun hasParticipants(stage: AthenaCompilerContributionStage): Boolean = stageParticipants.getValue(stage).isNotEmpty()

    /** Aggregates domain lowering contributions inside the declared `LOWER` pass. */
    fun lower(source: CompilerSourceDocument): AthenaDomainLoweringContribution {
        val context = AthenaDomainLoweringContext(source.toAthenaSourceDocument())
        return stageParticipants.getValue(AthenaCompilerContributionStage.LOWER).fold(AthenaDomainLoweringContribution.EMPTY) { aggregate, plugin ->
            val contribution = plugin.lower(context)
            AthenaDomainLoweringContribution(
                components = aggregate.components + contribution.components,
                ports = aggregate.ports + contribution.ports,
                connections = aggregate.connections + contribution.connections,
            )
        }
    }

    /** Aggregates semantic-enrichment notes and diagnostics inside the declared `SEMANTIC_ENRICHMENT` pass. */
    fun enrichSemantics(
        document: EngineeringDocument,
        context: AthenaSemanticEnrichmentContext,
    ): AthenaDomainSemanticEnrichmentContribution {
        return stageParticipants.getValue(AthenaCompilerContributionStage.SEMANTIC_ENRICHMENT).fold(
            AthenaDomainSemanticEnrichmentContribution.EMPTY,
        ) { aggregate, plugin ->
            val contribution = plugin.enrichSemantics(context.copy(document = document))
            AthenaDomainSemanticEnrichmentContribution(
                notes = aggregate.notes + contribution.notes,
                diagnostics = aggregate.diagnostics + contribution.diagnostics,
            )
        }
    }

    /** Aggregates domain validation diagnostics inside the declared `VALIDATE` pass. */
    fun validate(
        document: EngineeringDocument,
        context: AthenaPluginValidationContext,
    ): AthenaDomainValidationContribution {
        val attributions = stageParticipants.getValue(AthenaCompilerContributionStage.VALIDATE).flatMap { plugin ->
            attributeValidationContributions(
                plugin = plugin,
                result = plugin.validate(context.copy(document = document)),
            )
        }
        return AthenaDomainValidationContribution(attributions)
    }

    private fun attributeValidationContributions(
        plugin: AthenaDomainPlugin,
        result: AthenaPluginValidationResult,
    ): List<AthenaDomainValidationAttribution> {
        val diagnosticsByContributionId = result.contributions
            .groupBy(keySelector = { contribution -> contribution.contributionId }, valueTransform = { contribution -> contribution.diagnostics })
            .mapValues { (_, diagnostics) -> diagnostics.flatten() }

        return plugin.validationContributions.mapNotNull { contribution ->
            val diagnostics = diagnosticsByContributionId[contribution.contributionId].orEmpty()
            if (diagnostics.isEmpty()) {
                null
            } else {
                AthenaDomainValidationAttribution(
                    pluginId = plugin.manifest.pluginId,
                    contributionId = contribution.contributionId,
                    diagnostics = diagnostics,
                )
            }
        }
    }
}

private fun CompilerSourceDocument.toAthenaSourceDocument(): AthenaSourceDocument {
    return AthenaSourceDocument(
        file = file,
        ast = ast,
    )
}
