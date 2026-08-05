package com.engineeringood.athena.runtime

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.PluginValidationDiagnostic
import com.engineeringood.athena.plugin.PluginValidationRuleId
import com.engineeringood.athena.plugin.PluginValidationSeverity
import com.engineeringood.athena.plugin.host.ApprovedAthenaPlugin
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.host.AthenaHostedPluginContributionCategory
import com.engineeringood.athena.plugin.host.AthenaHostedPluginInventorySnapshot
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleSnapshot
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleState
import com.engineeringood.athena.plugin.host.AthenaHostedPluginRegistry
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import com.engineeringood.athena.plugin.host.AthenaPluginDiscoveryReport
import com.engineeringood.athena.plugin.host.RejectedAthenaPluginCandidate
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewSummary

/**
 * Runtime-owned contract for hosted plugin discovery, inspection, and typed contribution access.
 */
interface AthenaPluginRuntimeServices {
    /**
     * Returns the full inspectable discovery and approval report for the hosted plugin set.
     */
    fun discoveryReport(): AthenaPluginDiscoveryReport

    /**
     * Returns the approved plugin inventory shared with runtime and compiler consumers.
     */
    fun approvedInventory(): AthenaApprovedPluginInventory = discoveryReport().approvedInventory

    /**
     * Returns the current hosted lifecycle state and inventory snapshot.
     */
    fun hostedLifecycle(): AthenaHostedPluginLifecycleSnapshot

    /**
     * Returns the current hosted inventory snapshot in the same state reported by [hostedLifecycle].
     */
    fun hostedInventory(): AthenaHostedPluginInventorySnapshot = hostedLifecycle().inventory

    /**
     * Transitions hosted plugins into the initialized state without ceding runtime ownership.
     */
    fun initializeHostedPlugins(): AthenaHostedPluginLifecycleSnapshot

    /**
     * Transitions hosted plugins into the shutdown state while preserving inspection evidence.
     */
    fun shutdownHostedPlugins(): AthenaHostedPluginLifecycleSnapshot

    /**
     * Returns hosted plugin metadata in deterministic approved-plugin order.
     */
    fun hostedPlugins(): List<AthenaHostedRuntimePlugin>

    /**
     * Returns runtime-hosted domain semantics contributions in deterministic approved-plugin order.
     */
    fun domainSemanticsContributions(): List<AthenaRuntimePluginDomainSemanticsContribution>

    /**
     * Returns supported view-definition contributions exposed by the hosted plugin set.
     */
    fun viewDefinitionContributions(): List<AthenaRuntimePluginViewDefinitionContribution>

    /**
     * Returns hosted semantic review enrichers in deterministic approved-plugin order.
     */
    fun semanticReviewEnrichmentContributors(): List<AthenaRuntimePluginSemanticReviewEnrichmentContribution>

    /**
     * Publishes additive semantic review enrichments over one already-generated core review summary.
     */
    fun enrichReview(summary: SemanticReviewSummary): List<SemanticReviewEnrichment>

    /**
     * Returns runtime view contributions derived from the active execution context.
     */
    fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution>

    /**
     * Returns the invariants that remain core-owned and non-overridable even when plugins are active.
     */
    fun coreOwnedInvariants(): List<String> = ATHENA_PLUGIN_CORE_OWNED_INVARIANTS
}

/**
 * Runtime-owned inspection record for one approved hosted plugin.
 */
data class AthenaHostedRuntimePlugin(
    val pluginId: String,
    val pluginVersion: String,
    val implementationClassName: String,
    val lifecycleState: AthenaHostedPluginLifecycleState,
    val attachedExtensionPoints: Set<AthenaExtensionPoint>,
    val contributionCategories: Set<AthenaHostedPluginContributionCategory>,
    val domainCapabilities: Set<String>,
    val viewDefinitionIds: List<String>,
    val semanticReviewEnrichmentCount: Int,
    val viewContributionCount: Int,
)

/**
 * Runtime-owned inspection record for one hosted plugin view-definition contribution.
 */
data class AthenaRuntimePluginViewDefinitionContribution(
    val pluginId: String,
    val viewDefinitions: List<ViewDefinition>,
)

/**
 * Runtime-owned descriptor for one hosted domain semantics contribution.
 */
data class AthenaRuntimePluginDomainSemanticsContribution(
    val pluginId: String,
    val domainCapabilities: Set<String>,
    val domainPlugin: AthenaDomainPlugin,
)

/**
 * Runtime-owned descriptor for one hosted semantic review enrichment contributor.
 */
data class AthenaRuntimePluginSemanticReviewEnrichmentContribution(
    val pluginId: String,
    val enricher: AthenaSemanticReviewEnrichmentContributor,
)

/**
 * Optional plugin-side contract for runtime view contributions.
 */
interface AthenaRuntimePluginViewContributor : AthenaPlugin {
    /**
     * Returns runtime-owned view contributions derived from the active execution context.
     */
    fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> = emptyList()
}

/**
 * Runtime-owned view contribution emitted by one hosted plugin.
 */
data class AthenaRuntimePluginViewContribution(
    val pluginId: String = "",
    val inspectorGroups: List<AthenaRuntimePluginInspectorGroup> = emptyList(),
    val diagnosticsEntries: List<String> = emptyList(),
    val consoleEntries: List<String> = emptyList(),
)

/**
 * Runtime-owned inspector group emitted by one plugin view contribution.
 */
data class AthenaRuntimePluginInspectorGroup(
    val title: String,
    val fields: List<AthenaRuntimePluginInspectorField>,
)

/**
 * Runtime-owned inspector field emitted by one plugin view contribution.
 */
data class AthenaRuntimePluginInspectorField(
    val label: String,
    val value: String,
)

/**
 * Default hosted plugin services implementation for the JVM-first local runtime.
 */
class AthenaHostedPluginRuntimeServices(
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    discoveredReport: AthenaPluginDiscoveryReport = pluginDiscovery.discover(),
) : AthenaPluginRuntimeServices {
    private val pluginRegistry: AthenaHostedPluginRegistry = AthenaHostedPluginRegistry(
        pluginDiscovery = pluginDiscovery,
        discoveredReport = enforceHostedRuntimeBoundaries(discoveredReport),
        autoInitialize = true,
    )

    override fun discoveryReport(): AthenaPluginDiscoveryReport = pluginRegistry.discoveryReport()

    override fun hostedLifecycle(): AthenaHostedPluginLifecycleSnapshot = pluginRegistry.lifecycleSnapshot()

    override fun initializeHostedPlugins(): AthenaHostedPluginLifecycleSnapshot = pluginRegistry.initializeHostedPlugins()

    override fun shutdownHostedPlugins(): AthenaHostedPluginLifecycleSnapshot = pluginRegistry.shutdownHostedPlugins()

    override fun domainSemanticsContributions(): List<AthenaRuntimePluginDomainSemanticsContribution> {
        return activeApprovedPlugins().mapNotNull { approvedPlugin ->
            val domainPlugin = approvedPlugin.candidate.plugin as? AthenaDomainPlugin ?: return@mapNotNull null
            AthenaRuntimePluginDomainSemanticsContribution(
                pluginId = approvedPlugin.candidate.manifest.pluginId,
                domainCapabilities = domainPlugin.domainCapabilities,
                domainPlugin = domainPlugin,
            )
        }
    }

    override fun hostedPlugins(): List<AthenaHostedRuntimePlugin> {
        val approvedPluginsById = discoveryReport().approvedInventory.approvedPlugins.associateBy { approvedPlugin ->
            approvedPlugin.candidate.manifest.pluginId
        }
        return pluginRegistry.hostedPlugins().map { hostedPlugin ->
            val approvedPlugin = approvedPluginsById.getValue(hostedPlugin.pluginId)
            val plugin = approvedPlugin.candidate.plugin
            AthenaHostedRuntimePlugin(
                pluginId = hostedPlugin.pluginId,
                pluginVersion = hostedPlugin.pluginVersion,
                implementationClassName = hostedPlugin.implementationClassName,
                lifecycleState = hostedPlugin.lifecycleState,
                attachedExtensionPoints = hostedPlugin.attachedExtensionPoints,
                contributionCategories = hostedPlugin.contributionCategories,
                domainCapabilities = domainSemanticsContributionFor(plugin)?.domainCapabilities.orEmpty(),
                viewDefinitionIds = hostedPlugin.viewDefinitionIds,
                semanticReviewEnrichmentCount = if (plugin is AthenaSemanticReviewEnrichmentContributor) 1 else 0,
                viewContributionCount = if (plugin is AthenaRuntimePluginViewContributor) 1 else 0,
            )
        }
    }

    override fun viewDefinitionContributions(): List<AthenaRuntimePluginViewDefinitionContribution> {
        return activeApprovedPlugins().mapNotNull { approvedPlugin ->
            val viewDefinitions = viewDefinitionsFor(approvedPlugin.candidate.plugin)
            if (viewDefinitions.isEmpty()) {
                null
            } else {
                AthenaRuntimePluginViewDefinitionContribution(
                    pluginId = approvedPlugin.candidate.manifest.pluginId,
                    viewDefinitions = viewDefinitions,
                )
            }
        }
    }

    override fun semanticReviewEnrichmentContributors(): List<AthenaRuntimePluginSemanticReviewEnrichmentContribution> {
        return activeApprovedPlugins().mapNotNull { approvedPlugin ->
            val enricher = approvedPlugin.candidate.plugin as? AthenaSemanticReviewEnrichmentContributor ?: return@mapNotNull null
            AthenaRuntimePluginSemanticReviewEnrichmentContribution(
                pluginId = approvedPlugin.candidate.manifest.pluginId,
                enricher = enricher,
            )
        }
    }

    override fun enrichReview(summary: SemanticReviewSummary): List<SemanticReviewEnrichment> {
        return semanticReviewEnrichmentContributors().flatMap { contribution ->
            runCatching { contribution.enricher.enrichReview(summary) }
                .getOrElse { error ->
                    listOf(
                        SemanticReviewEnrichment(
                            pluginId = contribution.pluginId,
                            kind = SemanticReviewEnrichmentKind.PLUGIN_WARNING,
                            message = "Hosted semantic review enrichment failed: ${error.message ?: error::class.simpleName.orEmpty()}",
                        ),
                    )
                }
                .map { enrichment -> enrichment.copy(pluginId = contribution.pluginId) }
        }
    }

    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        return activeApprovedPlugins().flatMap { approvedPlugin ->
            val plugin = approvedPlugin.candidate.plugin as? AthenaRuntimePluginViewContributor ?: return@flatMap emptyList()
            plugin.viewContributions(context).map { contribution ->
                contribution.copy(pluginId = approvedPlugin.candidate.manifest.pluginId)
            }
        }
    }

    private fun domainSemanticsContributionFor(plugin: AthenaPlugin): AthenaRuntimePluginDomainSemanticsContribution? {
        val domainPlugin = plugin as? AthenaDomainPlugin ?: return null
        return AthenaRuntimePluginDomainSemanticsContribution(
            pluginId = plugin.manifest.pluginId,
            domainCapabilities = domainPlugin.domainCapabilities,
            domainPlugin = domainPlugin,
        )
    }

    private fun viewDefinitionsFor(plugin: AthenaPlugin): List<ViewDefinition> {
        val contributor = plugin as? AthenaViewDefinitionContributor ?: return emptyList()
        return contributor.viewDefinitions()
    }

    private fun activeApprovedPlugins(): List<ApprovedAthenaPlugin> {
        return if (hostedLifecycle().state == AthenaHostedPluginLifecycleState.SHUTDOWN) {
            emptyList()
        } else {
            discoveryReport().approvedInventory.approvedPlugins
        }
    }

    private fun enforceHostedRuntimeBoundaries(
        discoveryReport: AthenaPluginDiscoveryReport,
    ): AthenaPluginDiscoveryReport {
        val hostedApprovedPlugins = mutableListOf<ApprovedAthenaPlugin>()
        val hostedRejectedCandidates = discoveryReport.rejectedCandidates.toMutableList()

        discoveryReport.approvedInventory.approvedPlugins.forEach { approvedPlugin ->
            val diagnostics = runtimeContractDiagnostics(approvedPlugin)
            if (diagnostics.isEmpty()) {
                hostedApprovedPlugins += approvedPlugin
            } else {
                hostedRejectedCandidates += RejectedAthenaPluginCandidate.fromCandidate(
                    candidate = approvedPlugin.candidate,
                    diagnostics = diagnostics,
                )
            }
        }

        return AthenaPluginDiscoveryReport(
            runtime = discoveryReport.runtime,
            candidates = discoveryReport.candidates,
            rejectedCandidates = hostedRejectedCandidates.sortedWith(hostedRejectedPluginComparator),
            approvedInventory = AthenaApprovedPluginInventory.fromApproved(hostedApprovedPlugins),
        )
    }

    private fun runtimeContractDiagnostics(approvedPlugin: ApprovedAthenaPlugin): List<PluginValidationDiagnostic> {
        val plugin = approvedPlugin.candidate.plugin
        val diagnostics = mutableListOf<PluginValidationDiagnostic>()

        diagnostics += missingRuntimeContractDiagnostic(
            approvedPlugin = approvedPlugin,
            extensionPoint = AthenaExtensionPoint.RUNTIME_VIEWS,
            implementsContract = plugin is AthenaRuntimePluginViewContributor,
            undeclaredRuleId = "plugin.runtime.contract.view.undeclared",
            unimplementedRuleId = "plugin.runtime.contract.view.unimplemented",
            contractName = "runtime view contributions",
        )
        diagnostics += missingRuntimeContractDiagnostic(
            approvedPlugin = approvedPlugin,
            extensionPoint = AthenaExtensionPoint.VIEW_DEFINITIONS,
            implementsContract = plugin is AthenaViewDefinitionContributor,
            undeclaredRuleId = "plugin.runtime.contract.view-definition.undeclared",
            unimplementedRuleId = "plugin.runtime.contract.view-definition.unimplemented",
            contractName = "view definition contributions",
        )
        diagnostics += missingRuntimeContractDiagnostic(
            approvedPlugin = approvedPlugin,
            extensionPoint = AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
            implementsContract = plugin is AthenaSemanticReviewEnrichmentContributor,
            undeclaredRuleId = "plugin.runtime.contract.semantic-review-enrichment.undeclared",
            unimplementedRuleId = "plugin.runtime.contract.semantic-review-enrichment.unimplemented",
            contractName = "semantic review enrichment contributions",
        )

        return diagnostics
    }

    private fun missingRuntimeContractDiagnostic(
        approvedPlugin: ApprovedAthenaPlugin,
        extensionPoint: AthenaExtensionPoint,
        implementsContract: Boolean,
        undeclaredRuleId: String,
        unimplementedRuleId: String,
        contractName: String,
    ): List<PluginValidationDiagnostic> {
        val declaresContract = extensionPoint in approvedPlugin.attachedExtensionPoints
        if (implementsContract == declaresContract) {
            return emptyList()
        }

        val message = if (implementsContract) {
            "Plugin `${approvedPlugin.candidate.manifest.pluginId}` exposes $contractName but does not declare `$extensionPoint` in its manifest."
        } else {
            "Plugin `${approvedPlugin.candidate.manifest.pluginId}` declares `$extensionPoint` but does not implement the hosted contract for $contractName."
        }
        return listOf(
            PluginValidationDiagnostic(
                severity = PluginValidationSeverity.ERROR,
                ruleId = PluginValidationRuleId(if (implementsContract) undeclaredRuleId else unimplementedRuleId),
                subject = "requiredExtensionPoints",
                message = message,
            ),
        )
    }
}

private val ATHENA_PLUGIN_CORE_OWNED_INVARIANTS = listOf(
    "`Athena Runtime` owns workspace and project lifecycle orchestration.",
    "`Engineering IR` remains the only canonical semantic authority.",
    "Plugin contributions remain extensions over runtime-owned contracts rather than top-level owners.",
)

private val hostedRejectedPluginComparator = compareBy<RejectedAthenaPluginCandidate>(
    { it.pluginId.orEmpty() },
    { it.pluginVersion.orEmpty() },
    { it.implementationClassName },
)
