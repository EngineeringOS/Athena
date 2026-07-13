package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaComponentKnowledgeContribution
import com.engineeringood.athena.plugin.AthenaComponentKnowledgeContributor
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPresentationPackContributor
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaRenderContribution
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContributor
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContributor
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewSummary

/** Reference Electrical/Runtime proof plugin that publishes the stable M3 hosted domain surface. */
class ElectricalRuntimeDomainPlugin :
    AthenaDomainPlugin,
    AthenaComponentKnowledgeContributor,
    AthenaPresentationPackContributor,
    AthenaViewDefinitionContributor,
    AthenaRuntimePluginCommandContributor,
    AthenaRuntimePluginViewContributor,
    AthenaSemanticReviewEnrichmentContributor {
    /** Core-owned manifest declaring the sample plugin's identity, type, compatibility, and extension point. */
    override val manifest: AthenaPluginManifest = ELECTRICAL_RUNTIME_MANIFEST

    /** Minimal capability declaration showing the plugin remains a domain extension rather than core authority. */
    override val domainCapabilities: Set<String> = ELECTRICAL_RUNTIME_CAPABILITIES

    /** Generic electrical proof schema published through the stable plugin API. */
    override val domainSchema: AthenaDomainSchema = ELECTRICAL_DOMAIN_SCHEMA

    /** Inspectable validation contribution declarations exposed by the electrical proof plugin. */
    override val validationContributions: List<AthenaValidationContribution> = ELECTRICAL_VALIDATION_CONTRIBUTIONS

    /** Inspectable compiler-stage contribution declarations exposed by the electrical proof plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution> = ELECTRICAL_COMPILER_PASS_CONTRIBUTIONS

    /** Inspectable renderer-facing contribution declarations exposed by the electrical proof plugin. */
    override val renderContributions: List<AthenaRenderContribution> = ELECTRICAL_RENDER_CONTRIBUTIONS

    /** Publishes the first governed electrical primitive presentation packs through the stable SPI. */
    override fun primitivePresentationPacks() = ELECTRICAL_PRIMITIVE_PRESENTATION_PACKS

    /** Publishes the first governed electrical composite presentation packs through the stable SPI. */
    override fun compositePresentationPacks() = ELECTRICAL_COMPOSITE_PRESENTATION_PACKS

    /** Lowers authored Electrical/Runtime proof declarations into compiler-owned semantic blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return lowerElectricalRuntime(context)
    }

    /** Validates Electrical/Runtime proof properties and connection compatibility over canonical Engineering IR. */
    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return validateElectricalRuntime(context)
    }

    /** Contributes the first runtime-hosted electrical command proof without bypassing the command runtime. */
    override fun commandContributions(): List<AthenaRuntimePluginCommandContribution> {
        return electricalRuntimeCommandContributions()
    }

    /** Contributes the first governed electrical projection-family set without turning views into semantic truth. */
    override fun viewDefinitions() = ELECTRICAL_RUNTIME_VIEW_DEFINITIONS

    /** Contributes the first runtime-hosted electrical view proof through existing shell seams. */
    override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
        return electricalRuntimeViewContributions(context)
    }

    /** Publishes the first narrow M14 electrical component knowledge slice through the hosted plugin seam. */
    override fun componentKnowledge(): AthenaComponentKnowledgeContribution {
        return AthenaComponentKnowledgeContribution(
            engineeringConcepts = electricalEngineeringConcepts(),
            partImplementations = siemensElectricalPartImplementations(),
            semanticPorts = plcCpuResolvedSemanticPorts(),
            physicalTraits = siemensProofResolvedPhysicalTraits(),
        )
    }

    /** Adds electrical review interpretation without mutating or replacing the core semantic review facts. */
    override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> {
        return enrichElectricalRuntimeReview(
            pluginId = manifest.pluginId,
            review = review,
        )
    }
}
