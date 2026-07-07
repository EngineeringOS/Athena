package com.engineeringood.athena.compiler.plugin

import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaCoreVersion
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaRendererPlugin
import com.engineeringood.athena.plugin.AthenaRulePlugin
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.plugin.PluginValidationDiagnostic
import com.engineeringood.athena.plugin.PluginValidationResult
import com.engineeringood.athena.plugin.PluginValidationRuleId
import com.engineeringood.athena.plugin.PluginValidationSeverity
/** Validates plugin manifests and directly-instantiated plugin objects against the core-owned M0 contract. */
class AthenaPluginValidator {
    private val allowedExtensionPointsByType = mapOf(
        AthenaPluginType.DOMAIN to setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.VIEW_DEFINITIONS,
        ) + runtimeHostedExtensionPoints,
        AthenaPluginType.RULE to setOf(AthenaExtensionPoint.RULE_EVALUATION) + runtimeHostedExtensionPoints,
        AthenaPluginType.RENDERER to setOf(AthenaExtensionPoint.RENDERING) + runtimeHostedExtensionPoints,
    )

    /** Validates a plugin object by checking both its manifest and its typed contract. */
    fun validate(plugin: AthenaPlugin): PluginValidationResult {
        val diagnostics = buildList {
            addAll(validateManifest(plugin.manifest).diagnostics)

            val declaredTypes = declaredTypes(plugin)
            when {
                declaredTypes.size != 1 -> add(
                    diagnostic(
                        ruleId = "plugin.contract.type.invalid",
                        subject = "pluginType",
                        message = "Plugin must implement exactly one typed plugin contract, but declared `${declaredTypes.map { it.name }.sorted()}`.",
                    ),
                )

                plugin.manifest.pluginType != declaredTypes.single() -> add(
                    diagnostic(
                        ruleId = "plugin.contract.type.mismatch",
                        subject = "pluginType",
                        message = "Typed plugin contract expects `${declaredTypes.single()}`, but manifest declared `${plugin.manifest.pluginType}`.",
                    ),
                )
            }
        }
        return PluginValidationResult(diagnostics)
    }

    /** Validates a plugin for activation against the current core runtime version surface. */
    fun validateForActivation(
        plugin: AthenaPlugin,
        runtime: AthenaCoreRuntime,
    ): PluginValidationResult {
        val diagnostics = buildList {
            addAll(validate(plugin).diagnostics)
            addAll(validateCoreCompatibility(plugin.manifest.coreCompatibility, runtime).diagnostics)
        }
        return PluginValidationResult(diagnostics)
    }

    /** Validates a plugin manifest without requiring classpath discovery or activation. */
    fun validateManifest(manifest: AthenaPluginManifest): PluginValidationResult {
        val diagnostics = buildList {
            val maximumInclusive = manifest.coreCompatibility.maximumInclusive

            if (manifest.pluginId.isBlank()) {
                add(diagnostic("plugin.manifest.id.blank", "pluginId", "Plugin id must not be blank."))
            } else if (!PLUGIN_ID_PATTERN.matches(manifest.pluginId)) {
                add(
                    diagnostic(
                        "plugin.manifest.id.invalid",
                        "pluginId",
                        "Plugin id `${manifest.pluginId}` must use lowercase dot-or-hyphen-separated segments.",
                    ),
                )
            }

            if (manifest.pluginVersion.isBlank()) {
                add(diagnostic("plugin.manifest.version.blank", "pluginVersion", "Plugin version must not be blank."))
            }

            if (manifest.coreCompatibility.minimumInclusive.isBlank()) {
                add(
                    diagnostic(
                        "plugin.manifest.core-compatibility.minimum.blank",
                        "coreCompatibility.minimumInclusive",
                        "Core compatibility minimum version must not be blank.",
                    ),
                )
            }

            if (maximumInclusive != null && maximumInclusive.isBlank()) {
                add(
                    diagnostic(
                        "plugin.manifest.core-compatibility.maximum.blank",
                        "coreCompatibility.maximumInclusive",
                        "Core compatibility maximum version must not be blank when declared.",
                    ),
                )
            }

            val minimumVersion = AthenaCoreVersion.parse(manifest.coreCompatibility.minimumInclusive)
            if (manifest.coreCompatibility.minimumInclusive.isNotBlank() && minimumVersion == null) {
                add(
                    diagnostic(
                        "plugin.manifest.core-compatibility.minimum.invalid",
                        "coreCompatibility.minimumInclusive",
                        "Core compatibility minimum version `${manifest.coreCompatibility.minimumInclusive}` is invalid.",
                    ),
                )
            }

            val maximumVersionText = maximumInclusive
            val maximumVersion = maximumVersionText?.let(AthenaCoreVersion::parse)
            if (maximumVersionText != null && maximumVersionText.isNotBlank() && maximumVersion == null) {
                add(
                    diagnostic(
                        "plugin.manifest.core-compatibility.maximum.invalid",
                        "coreCompatibility.maximumInclusive",
                        "Core compatibility maximum version `$maximumVersionText` is invalid.",
                    ),
                )
            }

            if (minimumVersion != null && maximumVersion != null && minimumVersion > maximumVersion) {
                add(
                    diagnostic(
                        "plugin.manifest.core-compatibility.range.invalid",
                        "coreCompatibility",
                        "Core compatibility minimum `${manifest.coreCompatibility.minimumInclusive}` must not exceed maximum `$maximumVersionText`.",
                    ),
                )
            }

            if (manifest.requiredExtensionPoints.isEmpty()) {
                add(
                    diagnostic(
                        "plugin.manifest.extension-point.missing",
                        "requiredExtensionPoints",
                        "At least one required extension point must be declared.",
                    ),
                )
            }

            val allowedExtensionPoints = allowedExtensionPointsByType.getValue(manifest.pluginType)
            if (manifest.requiredExtensionPoints.any { it !in allowedExtensionPoints }) {
                add(
                    diagnostic(
                        "plugin.manifest.extension-point.illegal-for-type",
                        "requiredExtensionPoints",
                        "Plugin type `${manifest.pluginType}` may require only `$allowedExtensionPoints`.",
                    ),
                )
            }

            manifest.ownershipClaims.sortedBy { it.name }.forEach { ownershipClaim ->
                add(
                    diagnostic(
                        "plugin.manifest.ownership-claim.forbidden",
                        "ownershipClaims",
                        "Plugin `${manifest.pluginId}` may not claim `${ownershipClaim.name}` because canonical semantics, lifecycle, and runtime orchestration remain core-owned.",
                    ),
                )
            }
        }

        return PluginValidationResult(diagnostics)
    }

    private fun declaredTypes(plugin: AthenaPlugin): Set<AthenaPluginType> {
        return buildSet {
            if (plugin is AthenaDomainPlugin) {
                add(AthenaPluginType.DOMAIN)
            }
            if (plugin is AthenaRulePlugin) {
                add(AthenaPluginType.RULE)
            }
            if (plugin is AthenaRendererPlugin) {
                add(AthenaPluginType.RENDERER)
            }
        }
    }

    private fun diagnostic(ruleId: String, subject: String, message: String): PluginValidationDiagnostic {
        return PluginValidationDiagnostic(
            severity = PluginValidationSeverity.ERROR,
            ruleId = PluginValidationRuleId(ruleId),
            subject = subject,
            message = message,
        )
    }

    private fun validateCoreCompatibility(
        coreCompatibility: CoreVersionRange,
        runtime: AthenaCoreRuntime,
    ): PluginValidationResult {
        val minimumVersion = AthenaCoreVersion.parse(coreCompatibility.minimumInclusive)
            ?: return PluginValidationResult(emptyList())
        val maximumVersion = coreCompatibility.maximumInclusive?.let(AthenaCoreVersion::parse)

        val isSupported = runtime.version >= minimumVersion && (maximumVersion == null || runtime.version <= maximumVersion)
        if (isSupported) {
            return PluginValidationResult(emptyList())
        }

        return PluginValidationResult(
            listOf(
                diagnostic(
                    ruleId = "plugin.activation.core-version.unsupported",
                    subject = "coreCompatibility",
                    message = "Plugin requires Athena core versions in `${renderRange(coreCompatibility)}`, but current core is `${runtime.version}`.",
                ),
            ),
        )
    }

    private fun renderRange(coreCompatibility: CoreVersionRange): String {
        return if (coreCompatibility.maximumInclusive == null) {
            "[${coreCompatibility.minimumInclusive}, +inf)"
        } else {
            "[${coreCompatibility.minimumInclusive}, ${coreCompatibility.maximumInclusive}]"
        }
    }
}

private val runtimeHostedExtensionPoints = setOf(
    AthenaExtensionPoint.RUNTIME_COMMANDS,
    AthenaExtensionPoint.RUNTIME_VIEWS,
)

private val PLUGIN_ID_PATTERN = Regex("[a-z0-9]+([.-][a-z0-9]+)*")
