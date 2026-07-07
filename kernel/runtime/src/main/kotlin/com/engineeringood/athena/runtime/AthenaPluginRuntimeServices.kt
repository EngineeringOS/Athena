package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.ApprovedAthenaPlugin
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscoveryReport
import com.engineeringood.athena.compiler.plugin.RejectedAthenaPluginCandidate
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.PluginValidationDiagnostic
import com.engineeringood.athena.plugin.PluginValidationRuleId
import com.engineeringood.athena.plugin.PluginValidationSeverity

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
     * Returns hosted plugin metadata in deterministic approved-plugin order.
     */
    fun hostedPlugins(): List<AthenaHostedRuntimePlugin>

    /**
     * Returns runtime-hosted domain semantics contributions in deterministic approved-plugin order.
     */
    fun domainSemanticsContributions(): List<AthenaRuntimePluginDomainSemanticsContribution>

    /**
     * Returns all runtime command contributions exposed by the hosted plugin set.
     */
    fun commandContributions(): List<AthenaRuntimePluginCommandContribution>

    /**
     * Returns supported view-definition contributions exposed by the hosted plugin set.
     */
    fun viewDefinitionContributions(): List<AthenaRuntimePluginViewDefinitionContribution>

    /**
     * Executes one hosted runtime command contribution through the existing command runtime.
     */
    fun executeCommandContribution(
        context: AthenaExecutionContext,
        contributionId: String,
    ): AthenaRuntimePluginCommandExecution

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
    val attachedExtensionPoints: Set<AthenaExtensionPoint>,
    val domainCapabilities: Set<String>,
    val commandContributionIds: List<String>,
    val viewDefinitionIds: List<String>,
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
 * Optional plugin-side contract for runtime command contributions.
 */
interface AthenaRuntimePluginCommandContributor : AthenaPlugin {
    /**
     * Returns runtime command contribution descriptors exposed by this plugin.
     */
    fun commandContributions(): List<AthenaRuntimePluginCommandContribution> = emptyList()
}

/**
 * Runtime-owned descriptor for one plugin command contribution.
 */
data class AthenaRuntimePluginCommandContribution(
    val contributionId: String,
    val displayName: String,
    val description: String,
    val pluginId: String = "",
    val factory: AthenaRuntimePluginCommandFactory,
)

/**
 * Factory that derives one runtime command request from the active execution context.
 */
fun interface AthenaRuntimePluginCommandFactory {
    /**
     * Creates one runtime command request for the supplied execution context.
     */
    fun create(context: AthenaExecutionContext): AthenaRuntimePluginCommandRequest
}

/**
 * Plugin-side request returned before runtime decides whether to execute a contributed command.
 */
sealed interface AthenaRuntimePluginCommandRequest

/**
 * Request that is ready to execute through the runtime-owned command service.
 */
data class AthenaRuntimePluginCommandReady(
    val command: AthenaCommand,
) : AthenaRuntimePluginCommandRequest

/**
 * Request that could not produce a safe command for the current runtime context.
 */
data class AthenaRuntimePluginCommandRejected(
    val reason: String,
) : AthenaRuntimePluginCommandRequest

/**
 * Runtime-owned outcome of one contributed plugin command execution attempt.
 */
sealed interface AthenaRuntimePluginCommandExecution {
    /**
     * Contributed command identifier associated with the execution attempt.
     */
    val contributionId: String

    /**
     * Hosted plugin identifier associated with the execution attempt.
     */
    val pluginId: String
}

/**
 * Successful contributed plugin command execution routed through the standard command runtime.
 */
data class AthenaRuntimePluginCommandExecutionSuccess(
    override val contributionId: String,
    override val pluginId: String,
    val result: AthenaCommandExecutionSuccess,
) : AthenaRuntimePluginCommandExecution

/**
 * Rejected contributed plugin command because the contribution could not safely produce or apply a command.
 */
data class AthenaRuntimePluginCommandExecutionRejected(
    override val contributionId: String,
    override val pluginId: String,
    val reason: String,
) : AthenaRuntimePluginCommandExecution

/**
 * Unavailable contributed plugin command because the contribution id was not hosted or the runtime path was blocked.
 */
data class AthenaRuntimePluginCommandExecutionUnavailable(
    override val contributionId: String,
    override val pluginId: String,
    val reason: String,
) : AthenaRuntimePluginCommandExecution

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
    private val hostedDiscoveryReport: AthenaPluginDiscoveryReport = enforceHostedRuntimeBoundaries(discoveredReport)
    private val approvedPlugins = hostedDiscoveryReport.approvedInventory.approvedPlugins

    override fun discoveryReport(): AthenaPluginDiscoveryReport = hostedDiscoveryReport

    override fun domainSemanticsContributions(): List<AthenaRuntimePluginDomainSemanticsContribution> {
        return approvedPlugins.mapNotNull { approvedPlugin ->
            val domainPlugin = approvedPlugin.candidate.plugin as? AthenaDomainPlugin ?: return@mapNotNull null
            AthenaRuntimePluginDomainSemanticsContribution(
                pluginId = approvedPlugin.candidate.manifest.pluginId,
                domainCapabilities = domainPlugin.domainCapabilities,
                domainPlugin = domainPlugin,
            )
        }
    }

    override fun hostedPlugins(): List<AthenaHostedRuntimePlugin> {
        return approvedPlugins.map { approvedPlugin ->
            val plugin = approvedPlugin.candidate.plugin
            AthenaHostedRuntimePlugin(
                pluginId = approvedPlugin.candidate.manifest.pluginId,
                pluginVersion = approvedPlugin.candidate.manifest.pluginVersion,
                implementationClassName = approvedPlugin.candidate.implementationClassName,
                attachedExtensionPoints = approvedPlugin.attachedExtensionPoints,
                domainCapabilities = domainSemanticsContributionFor(plugin)?.domainCapabilities.orEmpty(),
                commandContributionIds = commandContributionsFor(plugin).map { contribution -> contribution.contributionId },
                viewDefinitionIds = viewDefinitionsFor(plugin).map { definition -> definition.id },
                viewContributionCount = if (plugin is AthenaRuntimePluginViewContributor) 1 else 0,
            )
        }
    }

    override fun commandContributions(): List<AthenaRuntimePluginCommandContribution> {
        return approvedPlugins.flatMap { approvedPlugin ->
            commandContributionsFor(approvedPlugin.candidate.plugin)
        }
    }

    override fun viewDefinitionContributions(): List<AthenaRuntimePluginViewDefinitionContribution> {
        return approvedPlugins.mapNotNull { approvedPlugin ->
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

    override fun executeCommandContribution(
        context: AthenaExecutionContext,
        contributionId: String,
    ): AthenaRuntimePluginCommandExecution {
        val contribution = commandContributions().firstOrNull { candidate -> candidate.contributionId == contributionId }
            ?: return AthenaRuntimePluginCommandExecutionUnavailable(
                contributionId = contributionId,
                pluginId = "",
                reason = "Hosted runtime command contribution `$contributionId` is not available.",
            )

        return when (val request = contribution.factory.create(context)) {
            is AthenaRuntimePluginCommandRejected -> AthenaRuntimePluginCommandExecutionRejected(
                contributionId = contribution.contributionId,
                pluginId = contribution.pluginId,
                reason = request.reason,
            )

            is AthenaRuntimePluginCommandReady -> when (val execution = context.commandRuntime().execute(context, request.command)) {
                is AthenaCommandExecutionSuccess -> AthenaRuntimePluginCommandExecutionSuccess(
                    contributionId = contribution.contributionId,
                    pluginId = contribution.pluginId,
                    result = execution,
                )

                is AthenaCommandExecutionRejected -> AthenaRuntimePluginCommandExecutionRejected(
                    contributionId = contribution.contributionId,
                    pluginId = contribution.pluginId,
                    reason = execution.reason,
                )

                is AthenaCommandExecutionUnavailable -> AthenaRuntimePluginCommandExecutionUnavailable(
                    contributionId = contribution.contributionId,
                    pluginId = contribution.pluginId,
                    reason = execution.reason,
                )
            }
        }
    }

    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        return approvedPlugins.flatMap { approvedPlugin ->
            val plugin = approvedPlugin.candidate.plugin as? AthenaRuntimePluginViewContributor ?: return@flatMap emptyList()
            plugin.viewContributions(context).map { contribution ->
                contribution.copy(pluginId = approvedPlugin.candidate.manifest.pluginId)
            }
        }
    }

    private fun commandContributionsFor(plugin: AthenaPlugin): List<AthenaRuntimePluginCommandContribution> {
        val contributor = plugin as? AthenaRuntimePluginCommandContributor ?: return emptyList()
        return contributor.commandContributions().map { contribution ->
            contribution.copy(pluginId = plugin.manifest.pluginId)
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
            extensionPoint = AthenaExtensionPoint.RUNTIME_COMMANDS,
            implementsContract = plugin is AthenaRuntimePluginCommandContributor,
            undeclaredRuleId = "plugin.runtime.contract.command.undeclared",
            unimplementedRuleId = "plugin.runtime.contract.command.unimplemented",
            contractName = "runtime command contributions",
        )
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
    "All semantic mutation must flow through the `Command Runtime`.",
    "Plugin contributions remain extensions over runtime-owned contracts rather than top-level owners.",
)

private val hostedRejectedPluginComparator = compareBy<RejectedAthenaPluginCandidate>(
    { it.pluginId.orEmpty() },
    { it.pluginVersion.orEmpty() },
    { it.implementationClassName },
)
