package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.PluginValidationDiagnostic
import com.engineeringood.athena.plugin.PluginValidationRuleId
import com.engineeringood.athena.plugin.PluginValidationSeverity

/** Source-layer service that enumerates plugin candidates before approval is applied. */
class AthenaPluginCandidateSource(
    private val source: AthenaPluginSource = ServiceLoaderAthenaPluginSource(),
) {
    /** Materializes deterministic candidates plus source-level rejections without approving activation. */
    fun enumerateCandidates(): AthenaPluginSourceResult {
        val sourceLoad = runCatching { source.loadPlugins() }
        if (sourceLoad.isFailure) {
            return AthenaPluginSourceResult(
                candidates = emptyList(),
                rejectedCandidates = listOf(sourceFailure(sourceLoad.exceptionOrNull())),
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

        return AthenaPluginSourceResult(
            candidates = candidates,
            rejectedCandidates = manifestFailures.sortedWith(rejectedPluginComparator),
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
