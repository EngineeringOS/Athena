package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.PluginValidationDiagnostic

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

/** Output of the source layer before governance approval is applied. */
data class AthenaPluginSourceResult(
    val candidates: List<AthenaPluginCandidate>,
    val rejectedCandidates: List<RejectedAthenaPluginCandidate>,
)

/** Output of the approval layer after validation and deterministic admission are complete. */
data class AthenaPluginApprovalResult(
    val runtime: AthenaCoreRuntime,
    val rejectedCandidates: List<RejectedAthenaPluginCandidate>,
    val approvedInventory: AthenaApprovedPluginInventory,
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

/** Full discovery and approval report produced before any compilation pass uses hosted plugins. */
data class AthenaPluginDiscoveryReport(
    val runtime: AthenaCoreRuntime,
    val candidates: List<AthenaPluginCandidate>,
    val rejectedCandidates: List<RejectedAthenaPluginCandidate>,
    val approvedInventory: AthenaApprovedPluginInventory,
)

internal val pluginCandidateComparator = compareBy<AthenaPluginCandidate>(
    { it.manifest.pluginId },
    { it.manifest.pluginVersion },
    { it.implementationClassName },
)

internal val approvedPluginComparator = compareBy<ApprovedAthenaPlugin>(
    { it.candidate.manifest.pluginId },
    { it.candidate.manifest.pluginVersion },
    { it.candidate.implementationClassName },
)

internal val rejectedPluginComparator = compareBy<RejectedAthenaPluginCandidate>(
    { it.pluginId == null },
    { it.pluginId ?: "" },
    { it.pluginVersion ?: "" },
    { it.implementationClassName },
)
