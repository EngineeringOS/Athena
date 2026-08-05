package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.language.RelationDeclaration
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.ExternalEvidenceDeclaration
import com.engineeringood.athena.language.GridDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.ProjectionConstructDeclaration
import com.engineeringood.athena.language.ProjectionPolicyDeclaration
import com.engineeringood.athena.language.RegionDeclaration
import com.engineeringood.athena.language.SheetDeclaration
import com.engineeringood.athena.language.ViewDeclaration
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainEntityBlueprint
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginOwnershipClaim
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaRenderContribution
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.plugin.host.AthenaPluginSource
import com.engineeringood.athena.runtime.AthenaExecutionContext
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

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "alpha-semantics.lower",
            stage = AthenaCompilerContributionStage.LOWER,
            displayName = "Alpha lowering",
        ),
        AthenaCompilerPassContribution(
            contributionId = "alpha-semantics.validate",
            stage = AthenaCompilerContributionStage.VALIDATE,
            displayName = "Alpha validation",
        ),
    )

    override val validationContributions: List<AthenaValidationContribution> = listOf(
        AthenaValidationContribution(
            contributionId = "alpha-semantics.validation.rules",
            displayName = "Alpha validation rules",
        ),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            entities = listOf(
                context.syntheticEntity(
                    name = "AlphaDevice",
                    concept = "PLC",
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = "alpha-semantics.validation.rules",
                    diagnostics = listOf(
                        context.domainDiagnostic(
                            ruleId = "domain.validation.alpha",
                            message = "alpha",
                        ),
                    ),
                ),
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

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "zeta-semantics.lower",
            stage = AthenaCompilerContributionStage.LOWER,
            displayName = "Zeta lowering",
        ),
        AthenaCompilerPassContribution(
            contributionId = "zeta-semantics.validate",
            stage = AthenaCompilerContributionStage.VALIDATE,
            displayName = "Zeta validation",
        ),
    )

    override val validationContributions: List<AthenaValidationContribution> = listOf(
        AthenaValidationContribution(
            contributionId = "zeta-semantics.validation.rules",
            displayName = "Zeta validation rules",
        ),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            entities = listOf(
                context.syntheticEntity(
                    name = "ZetaDevice",
                    concept = "Motor",
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = "zeta-semantics.validation.rules",
                    diagnostics = listOf(
                        context.domainDiagnostic(
                            ruleId = "domain.validation.zeta",
                            message = "zeta",
                        ),
                    ),
                ),
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

internal class LowerOnlySemanticsTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.lower-only",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "lower-only.lower",
            stage = AthenaCompilerContributionStage.LOWER,
            displayName = "Lower only lowering",
        ),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            entities = listOf(
                context.syntheticEntity(
                    name = "LowerOnlyDevice",
                    concept = "TestEntity",
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = "lower-only.validation.rules",
                    diagnostics = listOf(
                        context.domainDiagnostic(
                            ruleId = "domain.validation.lower-only",
                            message = "lower-only should not validate without a validate-stage declaration",
                        ),
                    ),
                ),
            ),
        )
    }
}

internal class GenericLoweringOnlyTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.generic-lowering-only",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "generic-lowering-only.lower",
            stage = AthenaCompilerContributionStage.LOWER,
            displayName = "Generic lowering only",
        ),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        // Exhaustive partition over Declaration so future sealed variants fail at compile time.
        val entityDeclarations = mutableListOf<EntityDeclaration>()
        val portDeclarations = mutableListOf<PortDeclaration>()
        for (declaration in context.source.ast.declarations) {
            when (declaration) {
                is EntityDeclaration -> entityDeclarations += declaration
                is PortDeclaration -> portDeclarations += declaration
                is RelationDeclaration -> Unit
                is ExternalEvidenceDeclaration -> Unit
                is ProjectionPolicyDeclaration -> Unit
                is LayoutDeclaration -> Unit
                is InstallationDeclaration -> Unit
                is ViewDeclaration -> Unit
                is SheetDeclaration -> Unit
                is GridDeclaration -> Unit
                is RegionDeclaration -> Unit
                is ProjectionConstructDeclaration -> Unit
            }
        }

        val entities = entityDeclarations.mapNotNull(context::entityOrNull)
        val lowerableEntityNames = entities.map { entity -> entity.name }.toSet()
        val ports = buildList {
            addAll(portDeclarations)
            entityDeclarations.forEach { entity ->
                addAll(entity.nestedPorts)
                entity.nestedFunctions.forEach { function -> addAll(function.nestedPorts) }
            }
        }.filter { declaration -> declaration.qualifiedName.parts.firstOrNull() in lowerableEntityNames }
            .mapNotNull(context::portOrNull)
        val functions = entityDeclarations.flatMap { entity ->
            entity.nestedFunctions.map { function -> context.function(entity, function) }
        }
        return AthenaDomainLoweringContribution(
            entities = entities,
            ports = ports,
            functions = functions,
        )
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = "generic-lowering-only.validation.rules",
                    diagnostics = listOf(
                        context.domainDiagnostic(
                            ruleId = "domain.validation.generic-lowering-only",
                            message = "generic-lowering-only should not validate without a validate-stage declaration",
                        ),
                    ),
                ),
            ),
        )
    }
}

internal class SingleViewRenderTestPlugin(
    private val viewId: String = "operator-console",
) : AthenaDomainPlugin, AthenaViewDefinitionContributor {
    private val loweringDelegate = GenericLoweringOnlyTestPlugin()

    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.single-view-render",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.VIEW_DEFINITIONS,
        ),
    )

    override val compilerPassContributions: List<AthenaCompilerPassContribution> =
        loweringDelegate.compilerPassContributions

    override val renderContributions: List<AthenaRenderContribution> = listOf(
        AthenaRenderContribution(
            contributionId = "single-view-render.render.$viewId",
            displayName = "Single view render intent",
            description = "Publishes one non-electrical renderable view to prove backend selection stays plugin-owned.",
            viewIds = setOf(viewId),
            rendererTargets = setOf("svg"),
        ),
    )

    override fun viewDefinitions(): List<ViewDefinition> {
        return listOf(
            ViewDefinition(
                id = viewId,
                displayName = "Operator Console",
                description = "Synthetic plugin-owned view used to prove compiler render selection stays extensible.",
            ),
        )
    }

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return loweringDelegate.lower(context)
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return loweringDelegate.validate(context)
    }
}

internal class ValidateOnlySemanticsTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.validate-only",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "validate-only.validate",
            stage = AthenaCompilerContributionStage.VALIDATE,
            displayName = "Validate only validation",
        ),
    )

    override val validationContributions: List<AthenaValidationContribution> = listOf(
        AthenaValidationContribution(
            contributionId = "validate-only.validation.rules",
            displayName = "Validate only rules",
        ),
    )

    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return AthenaDomainLoweringContribution(
            entities = listOf(
                context.syntheticEntity(
                    name = "ValidateOnlyDevice",
                    concept = "TestEntity",
                ),
            ),
        )
    }

    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return AthenaPluginValidationResult(
            contributions = listOf(
                context.emitValidationContribution(
                    contributionId = "validate-only.validation.rules",
                    diagnostics = listOf(
                        context.domainDiagnostic(
                            ruleId = "domain.validation.validate-only",
                            message = "validate-only",
                        ),
                    ),
                ),
            ),
        )
    }
}

private fun AthenaDomainLoweringContext.syntheticEntity(
    name: String,
    concept: String,
): AthenaDomainEntityBlueprint {
    val sourceProvenance = provenance(source.ast.system.span)
    return AthenaDomainEntityBlueprint(
        name = name,
        conceptReference = EngineeringDefinitionReference(
            authoredName = listOf(concept),
            resolvedId = null,
            provenance = sourceProvenance,
        ),
        properties = listOf(
            EngineeringProperty(
                name = "type",
                value = EngineeringValue.Symbol(concept),
                provenance = sourceProvenance,
            ),
        ),
        structureAssignments = emptyList(),
        provenance = sourceProvenance,
    )
}

internal class SemanticEnrichmentOnlyTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.semantic-enrichment-only",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
        AthenaCompilerPassContribution(
            contributionId = "semantic-enrichment-only.enrich",
            stage = AthenaCompilerContributionStage.SEMANTIC_ENRICHMENT,
            displayName = "Semantic enrichment only",
        ),
    )

    override fun enrichSemantics(context: AthenaSemanticEnrichmentContext): AthenaDomainSemanticEnrichmentContribution {
        return AthenaDomainSemanticEnrichmentContribution(
            notes = listOf(
                context.note("synthetic semantic enrichment note"),
            ),
            diagnostics = listOf(
                context.domainDiagnostic(
                    ruleId = "domain.enrichment.synthetic",
                    message = "synthetic enrichment diagnostic",
                    severity = SemanticDiagnosticSeverity.WARNING,
                ),
            ),
        )
    }
}
