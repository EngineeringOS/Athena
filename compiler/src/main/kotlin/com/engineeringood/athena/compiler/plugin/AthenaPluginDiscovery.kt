package com.engineeringood.athena.compiler.plugin

import java.util.ServiceLoader

/** Core-owned source of plugin implementations available to the compiler at startup. */
interface AthenaPluginSource {
    /** Loads the currently available plugin implementations from the configured source. */
    fun loadPlugins(): List<AthenaPlugin>
}

/** JVM-first plugin source that discovers Athena plugins from `ServiceLoader` on the local classpath. */
class ServiceLoaderAthenaPluginSource(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        ?: ServiceLoaderAthenaPluginSource::class.java.classLoader,
) : AthenaPluginSource {
    override fun loadPlugins(): List<AthenaPlugin> {
        return ServiceLoader.load(AthenaPlugin::class.java, classLoader).toList()
    }
}

/** Deterministic candidate record for one discovered plugin implementation. */
data class AthenaPluginCandidate(
    val plugin: AthenaPlugin,
    val implementationClassName: String,
    val manifest: AthenaPluginManifest,
)

/** Rejected plugin candidate together with the diagnostics that prevented activation. */
data class RejectedAthenaPluginCandidate(
    val implementationClassName: String,
    val pluginId: String?,
    val pluginVersion: String?,
    val pluginType: AthenaPluginType?,
    val diagnostics: List<PluginValidationDiagnostic>,
) {
    companion object {
        /** Creates one rejected candidate record from a loaded plugin candidate. */
        fun fromCandidate(
            candidate: AthenaPluginCandidate,
            diagnostics: List<PluginValidationDiagnostic>,
        ): RejectedAthenaPluginCandidate {
            return RejectedAthenaPluginCandidate(
                implementationClassName = candidate.implementationClassName,
                pluginId = candidate.manifest.pluginId,
                pluginVersion = candidate.manifest.pluginVersion,
                pluginType = candidate.manifest.pluginType,
                diagnostics = diagnostics,
            )
        }
    }
}

/** Approved plugin candidate attached to the extension points declared in its core-owned manifest. */
data class ApprovedAthenaPlugin(
    val candidate: AthenaPluginCandidate,
    val attachedExtensionPoints: Set<AthenaExtensionPoint> = candidate.manifest.requiredExtensionPoints,
)

/** Approved plugin inventory grouped by core-owned extension points without changing compiler pass order. */
data class AthenaApprovedPluginInventory(
    val approvedPlugins: List<ApprovedAthenaPlugin>,
    private val approvedPluginsByExtensionPoint: Map<AthenaExtensionPoint, List<ApprovedAthenaPlugin>>,
) {
    /** Returns the approved plugins attached at [extensionPoint] in deterministic core-owned order. */
    fun attachedPlugins(extensionPoint: AthenaExtensionPoint): List<ApprovedAthenaPlugin> {
        return approvedPluginsByExtensionPoint[extensionPoint].orEmpty()
    }

    companion object {
        /** Empty inventory used when no compatible plugins were approved for activation. */
        val EMPTY: AthenaApprovedPluginInventory = AthenaApprovedPluginInventory(
            approvedPlugins = emptyList(),
            approvedPluginsByExtensionPoint = AthenaExtensionPoint.entries.associateWith { emptyList() },
        )

        /** Builds a deterministic approved inventory from the validated plugin list. */
        fun fromApproved(approvedPlugins: List<ApprovedAthenaPlugin>): AthenaApprovedPluginInventory {
            if (approvedPlugins.isEmpty()) {
                return EMPTY
            }

            val sortedApproved = approvedPlugins.sortedWith(approvedPluginComparator)
            return AthenaApprovedPluginInventory(
                approvedPlugins = sortedApproved,
                approvedPluginsByExtensionPoint = AthenaExtensionPoint.entries.associateWith { extensionPoint ->
                    sortedApproved.filter { extensionPoint in it.attachedExtensionPoints }
                },
            )
        }
    }
}

/** Full discovery and activation report produced before any compilation pass uses plugins. */
data class AthenaPluginDiscoveryReport(
    val runtime: AthenaCoreRuntime,
    val candidates: List<AthenaPluginCandidate>,
    val rejectedCandidates: List<RejectedAthenaPluginCandidate>,
    val approvedInventory: AthenaApprovedPluginInventory,
)

/** Core-owned discovery and approval service for local Athena plugins on the JVM classpath. */
class AthenaPluginDiscovery(
    private val source: AthenaPluginSource = ServiceLoaderAthenaPluginSource(),
    private val validator: AthenaPluginValidator = AthenaPluginValidator(),
    private val runtime: AthenaCoreRuntime = AthenaCoreRuntime.current(),
) {
    /** Discovers local plugin candidates, validates them for activation, and returns the approved inventory. */
    fun discover(): AthenaPluginDiscoveryReport {
        val sourceLoad = runCatching { source.loadPlugins() }
        if (sourceLoad.isFailure) {
            return AthenaPluginDiscoveryReport(
                runtime = runtime,
                candidates = emptyList(),
                rejectedCandidates = listOf(sourceFailure(sourceLoad.exceptionOrNull())),
                approvedInventory = AthenaApprovedPluginInventory.EMPTY,
            )
        }

        val manifestFailures = mutableListOf<RejectedAthenaPluginCandidate>()
        val candidates = sourceLoad.getOrThrow()
            .mapNotNull { plugin ->
                val implementationClassName = plugin::class.java.name
                val manifest = runCatching { plugin.manifest }.getOrElse { exception ->
                    manifestFailures += RejectedAthenaPluginCandidate(
                        implementationClassName = implementationClassName,
                        pluginId = null,
                        pluginVersion = null,
                        pluginType = null,
                        diagnostics = listOf(
                            diagnostic(
                                ruleId = "plugin.discovery.manifest.unreadable",
                                subject = "manifest",
                                message = "Plugin manifest could not be read for `$implementationClassName`: ${exception.message ?: exception::class.simpleName}.",
                            ),
                        ),
                    )
                    return@mapNotNull null
                }
                AthenaPluginCandidate(
                    plugin = plugin,
                    implementationClassName = implementationClassName,
                    manifest = manifest,
                )
            }
            .sortedWith(pluginCandidateComparator)
        val duplicatePluginIds = candidates.groupBy { it.manifest.pluginId }
            .filterValues { it.size > 1 }
            .keys

        val approvedPlugins = mutableListOf<ApprovedAthenaPlugin>()
        val rejectedCandidates = manifestFailures.toMutableList()

        candidates.forEach { candidate ->
            val diagnostics = validator.validateForActivation(candidate.plugin, runtime).diagnostics.toMutableList()
            if (candidate.manifest.pluginId in duplicatePluginIds) {
                diagnostics += diagnostic(
                    ruleId = "plugin.activation.identity.duplicate",
                    subject = "pluginId",
                    message = "Plugin id `${candidate.manifest.pluginId}` is declared by more than one discovered candidate and must be unique.",
                )
            }

            if (diagnostics.isEmpty()) {
                approvedPlugins += ApprovedAthenaPlugin(candidate)
            } else {
                rejectedCandidates += RejectedAthenaPluginCandidate.fromCandidate(
                    candidate = candidate,
                    diagnostics = diagnostics,
                )
            }
        }

        return AthenaPluginDiscoveryReport(
            runtime = runtime,
            candidates = candidates,
            rejectedCandidates = rejectedCandidates.sortedWith(rejectedPluginComparator),
            approvedInventory = AthenaApprovedPluginInventory.fromApproved(approvedPlugins),
        )
    }

    private fun sourceFailure(exception: Throwable?): RejectedAthenaPluginCandidate {
        return RejectedAthenaPluginCandidate(
            implementationClassName = source::class.java.name,
            pluginId = null,
            pluginVersion = null,
            pluginType = null,
            diagnostics = listOf(
                diagnostic(
                    ruleId = "plugin.discovery.source.unreadable",
                    subject = "pluginSource",
                    message = "Plugin source `${source::class.java.name}` could not be loaded: ${exception?.message ?: exception?.javaClass?.simpleName ?: "unknown error"}.",
                ),
            ),
        )
    }

    private fun diagnostic(
        ruleId: String,
        subject: String,
        message: String,
    ): PluginValidationDiagnostic {
        return PluginValidationDiagnostic(
            severity = PluginValidationSeverity.ERROR,
            ruleId = PluginValidationRuleId(ruleId),
            subject = subject,
            message = message,
        )
    }
}

private fun AthenaPluginCandidate.sortKey(): Triple<String, String, String> {
    return Triple(manifest.pluginId, manifest.pluginVersion, implementationClassName)
}

private val pluginCandidateComparator = compareBy<AthenaPluginCandidate>(
    { it.manifest.pluginId },
    { it.manifest.pluginVersion },
    { it.implementationClassName },
)

private val approvedPluginComparator = compareBy<ApprovedAthenaPlugin>(
    { it.candidate.manifest.pluginId },
    { it.candidate.manifest.pluginVersion },
    { it.candidate.implementationClassName },
)

private val rejectedPluginComparator = compareBy<RejectedAthenaPluginCandidate>(
    { it.pluginId == null },
    { it.pluginId ?: "" },
    { it.pluginVersion ?: "" },
    { it.implementationClassName },
)
