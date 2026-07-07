package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaPluginSource
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginOwnershipClaim
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContributor
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandFactory
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandRejected
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContributor
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId

class MalformedManifestTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "",
        pluginVersion = "",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = ""),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )
}

class IncompatibleCoreVersionTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.future",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "9.0.0"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )
}

internal class AlphaDomainTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.alpha",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )
}

internal class AlphaSemanticsTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.alpha-semantics",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            components = listOf(
                context.component(
                    name = "AlphaDevice",
                    kind = "device",
                    properties = listOf(
                        EngineeringProperty("type", EngineeringPropertyValue.Symbol("PLC")),
                    ),
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): List<SemanticDiagnostic> {
        return listOf(
            context.domainDiagnostic(
                ruleId = "domain.validation.alpha",
                message = "alpha",
            ),
        )
    }
}

internal class ZetaDomainTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.zeta",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )
}

internal class DuplicateAlphaDomainTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.alpha",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )
}

internal class ZetaSemanticsTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.zeta-semantics",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            components = listOf(
                context.component(
                    name = "ZetaDevice",
                    kind = "device",
                    properties = listOf(
                        EngineeringProperty("type", EngineeringPropertyValue.Symbol("Motor")),
                    ),
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): List<SemanticDiagnostic> {
        return listOf(
            context.domainDiagnostic(
                ruleId = "domain.validation.zeta",
                message = "zeta",
            ),
        )
    }
}

internal class SovereignOwnershipClaimTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.sovereign-claim",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
        ownershipClaims = setOf(
            AthenaPluginOwnershipClaim.ENGINEERING_IR,
            AthenaPluginOwnershipClaim.WORKSPACE_LIFECYCLE,
        ),
    )
}

internal class UndeclaredRuntimeCommandTestPlugin : AthenaDomainPlugin, AthenaRuntimePluginCommandContributor {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.undeclared-runtime-command",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun commandContributions(): List<AthenaRuntimePluginCommandContribution> {
        return listOf(
            AthenaRuntimePluginCommandContribution(
                contributionId = "undeclared-runtime-command",
                displayName = "Undeclared runtime command",
                description = "Used by tests to prove hosted runtime contract rejection.",
                factory = AthenaRuntimePluginCommandFactory {
                    AthenaRuntimePluginCommandRejected("Not intended for execution.")
                },
            ),
        )
    }
}

internal class DeclaredButMissingRuntimeCommandTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.missing-runtime-command",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.RUNTIME_COMMANDS,
        ),
    )
}

internal class UndeclaredRuntimeViewTestPlugin : AthenaDomainPlugin, AthenaRuntimePluginViewContributor {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.undeclared-runtime-view",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        return listOf(AthenaRuntimePluginViewContribution())
    }
}

internal class DeclaredButMissingRuntimeViewTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.missing-runtime-view",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.RUNTIME_VIEWS,
        ),
    )
}

internal class FixedAthenaPluginSource(
    private val plugins: List<AthenaPlugin>,
) : AthenaPluginSource {
    override fun loadPlugins(): List<AthenaPlugin> = plugins
}

internal class ThrowingAthenaPluginSource(
    private val message: String,
) : AthenaPluginSource {
    override fun loadPlugins(): List<AthenaPlugin> {
        error(message)
    }
}
