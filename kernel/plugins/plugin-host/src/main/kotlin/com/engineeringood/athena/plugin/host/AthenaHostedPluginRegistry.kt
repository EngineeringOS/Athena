package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.AthenaCompilerPassContributor
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaRenderContributor
import com.engineeringood.athena.plugin.AthenaValidationContributor
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor

/** Host-owned lifecycle states for the approved plugin set in one JVM process. */
enum class AthenaHostedPluginLifecycleState {
    LOADED,
    INITIALIZED,
    SHUTDOWN,
}

/** Generic contribution categories surfaced by the hosted plugin inventory inspection path. */
enum class AthenaHostedPluginContributionCategory {
    DOMAIN_SEMANTICS,
    DOMAIN_SCHEMA,
    VALIDATION,
    COMPILER_PASS,
    RENDER,
    VIEW_DEFINITION,
    SEMANTIC_REVIEW_ENRICHMENT,
    RUNTIME_COMMAND,
    RUNTIME_VIEW,
}

/** Inspectable host-owned descriptor for one approved plugin in the current lifecycle state. */
data class AthenaHostedPluginDescriptor(
    val pluginId: String,
    val pluginVersion: String,
    val implementationClassName: String,
    val lifecycleState: AthenaHostedPluginLifecycleState,
    val attachedExtensionPoints: Set<AthenaExtensionPoint>,
    val contributionCategories: Set<AthenaHostedPluginContributionCategory>,
    val domainId: String?,
    val validationContributionIds: List<String>,
    val compilerContributionIds: List<String>,
    val renderContributionIds: List<String>,
    val viewDefinitionIds: List<String>,
)

/** Inspectable inventory snapshot for the currently hosted plugin set. */
data class AthenaHostedPluginInventorySnapshot(
    val lifecycleState: AthenaHostedPluginLifecycleState,
    val candidateCount: Int,
    val approvedPluginCount: Int,
    val rejectedPluginCount: Int,
    val approvedPlugins: List<AthenaHostedPluginDescriptor>,
    val rejectedCandidates: List<RejectedAthenaPluginCandidate>,
)

/** Inspectable lifecycle snapshot for the currently hosted plugin set. */
data class AthenaHostedPluginLifecycleSnapshot(
    val state: AthenaHostedPluginLifecycleState,
    val inventory: AthenaHostedPluginInventorySnapshot,
)

/** Host-owned registry that governs load, initialize, inspect, and shutdown over one discovery report. */
class AthenaHostedPluginRegistry(
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    discoveredReport: AthenaPluginDiscoveryReport = pluginDiscovery.discover(),
    autoInitialize: Boolean = false,
) {
    private val hostedDiscoveryReport: AthenaPluginDiscoveryReport = discoveredReport
    private var lifecycleState: AthenaHostedPluginLifecycleState = AthenaHostedPluginLifecycleState.LOADED

    init {
        if (autoInitialize) {
            initializeHostedPlugins()
        }
    }

    /** Returns the immutable discovery report governed by this registry instance. */
    fun discoveryReport(): AthenaPluginDiscoveryReport = hostedDiscoveryReport

    /** Returns the immutable approved inventory governed by this registry instance. */
    fun approvedInventory(): AthenaApprovedPluginInventory = hostedDiscoveryReport.approvedInventory

    /** Returns approved plugin descriptors in deterministic order for the current lifecycle state. */
    fun hostedPlugins(): List<AthenaHostedPluginDescriptor> {
        return approvedInventory().approvedPlugins.map { approvedPlugin ->
            descriptorFor(approvedPlugin, lifecycleState)
        }
    }

    /** Returns an inspectable inventory snapshot for the current lifecycle state. */
    fun inventorySnapshot(): AthenaHostedPluginInventorySnapshot {
        return AthenaHostedPluginInventorySnapshot(
            lifecycleState = lifecycleState,
            candidateCount = hostedDiscoveryReport.candidates.size,
            approvedPluginCount = approvedInventory().approvedPlugins.size,
            rejectedPluginCount = hostedDiscoveryReport.rejectedCandidates.size,
            approvedPlugins = hostedPlugins(),
            rejectedCandidates = hostedDiscoveryReport.rejectedCandidates,
        )
    }

    /** Returns the current lifecycle state together with the active inventory snapshot. */
    fun lifecycleSnapshot(): AthenaHostedPluginLifecycleSnapshot {
        return AthenaHostedPluginLifecycleSnapshot(
            state = lifecycleState,
            inventory = inventorySnapshot(),
        )
    }

    /** Transitions the hosted plugin set from `LOADED` to `INITIALIZED` without ceding orchestration ownership. */
    fun initializeHostedPlugins(): AthenaHostedPluginLifecycleSnapshot {
        if (lifecycleState != AthenaHostedPluginLifecycleState.SHUTDOWN) {
            lifecycleState = AthenaHostedPluginLifecycleState.INITIALIZED
        }
        return lifecycleSnapshot()
    }

    /** Transitions the hosted plugin set into `SHUTDOWN` while preserving inspectable inventory evidence. */
    fun shutdownHostedPlugins(): AthenaHostedPluginLifecycleSnapshot {
        lifecycleState = AthenaHostedPluginLifecycleState.SHUTDOWN
        return lifecycleSnapshot()
    }

    private fun descriptorFor(
        approvedPlugin: ApprovedAthenaPlugin,
        state: AthenaHostedPluginLifecycleState,
    ): AthenaHostedPluginDescriptor {
        val plugin = approvedPlugin.candidate.plugin
        val domainSchema = (plugin as? AthenaDomainPlugin)?.domainSchema ?: AthenaDomainSchema.EMPTY
        val validationContributionIds = (plugin as? AthenaValidationContributor)
            ?.validationContributions
            ?.map { contribution -> contribution.contributionId }
            .orEmpty()
        val compilerContributionIds = (plugin as? AthenaCompilerPassContributor)
            ?.compilerPassContributions
            ?.map { contribution -> contribution.contributionId }
            .orEmpty()
        val renderContributionIds = (plugin as? AthenaRenderContributor)
            ?.renderContributions
            ?.map { contribution -> contribution.contributionId }
            .orEmpty()
        val viewDefinitionIds = (plugin as? AthenaViewDefinitionContributor)
            ?.viewDefinitions()
            ?.map { definition -> definition.id }
            .orEmpty()

        return AthenaHostedPluginDescriptor(
            pluginId = approvedPlugin.candidate.manifest.pluginId,
            pluginVersion = approvedPlugin.candidate.manifest.pluginVersion,
            implementationClassName = approvedPlugin.candidate.implementationClassName,
            lifecycleState = state,
            attachedExtensionPoints = approvedPlugin.attachedExtensionPoints,
            contributionCategories = contributionCategoriesFor(
                approvedPlugin = approvedPlugin,
                domainSchema = domainSchema,
                validationContributionIds = validationContributionIds,
                compilerContributionIds = compilerContributionIds,
                renderContributionIds = renderContributionIds,
                viewDefinitionIds = viewDefinitionIds,
            ),
            domainId = domainSchema.domainId.takeIf { it.isNotBlank() },
            validationContributionIds = validationContributionIds,
            compilerContributionIds = compilerContributionIds,
            renderContributionIds = renderContributionIds,
            viewDefinitionIds = viewDefinitionIds,
        )
    }

    private fun contributionCategoriesFor(
        approvedPlugin: ApprovedAthenaPlugin,
        domainSchema: AthenaDomainSchema,
        validationContributionIds: List<String>,
        compilerContributionIds: List<String>,
        renderContributionIds: List<String>,
        viewDefinitionIds: List<String>,
    ): Set<AthenaHostedPluginContributionCategory> {
        return buildSet {
            val plugin = approvedPlugin.candidate.plugin
            if (plugin is AthenaDomainPlugin) {
                add(AthenaHostedPluginContributionCategory.DOMAIN_SEMANTICS)
            }
            if (domainSchema != AthenaDomainSchema.EMPTY) {
                add(AthenaHostedPluginContributionCategory.DOMAIN_SCHEMA)
            }
            if (validationContributionIds.isNotEmpty()) {
                add(AthenaHostedPluginContributionCategory.VALIDATION)
            }
            if (compilerContributionIds.isNotEmpty()) {
                add(AthenaHostedPluginContributionCategory.COMPILER_PASS)
            }
            if (renderContributionIds.isNotEmpty()) {
                add(AthenaHostedPluginContributionCategory.RENDER)
            }
            if (viewDefinitionIds.isNotEmpty()) {
                add(AthenaHostedPluginContributionCategory.VIEW_DEFINITION)
            }
            if (AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT in approvedPlugin.attachedExtensionPoints) {
                add(AthenaHostedPluginContributionCategory.SEMANTIC_REVIEW_ENRICHMENT)
            }
            if (AthenaExtensionPoint.RUNTIME_COMMANDS in approvedPlugin.attachedExtensionPoints) {
                add(AthenaHostedPluginContributionCategory.RUNTIME_COMMAND)
            }
            if (AthenaExtensionPoint.RUNTIME_VIEWS in approvedPlugin.attachedExtensionPoints) {
                add(AthenaHostedPluginContributionCategory.RUNTIME_VIEW)
            }
        }
    }
}
