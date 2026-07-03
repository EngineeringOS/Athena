package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.compiler.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPlugin
import com.engineeringood.athena.compiler.plugin.AthenaPluginManifest
import com.engineeringood.athena.compiler.plugin.AthenaPluginSource
import com.engineeringood.athena.compiler.plugin.AthenaPluginType
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.compiler.plugin.CoreVersionRange
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
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
