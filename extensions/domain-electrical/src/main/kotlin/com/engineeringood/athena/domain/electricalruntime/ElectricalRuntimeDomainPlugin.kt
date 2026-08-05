package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.plugin.AthenaCompilerPassContribution
import com.engineeringood.athena.plugin.AthenaDomainLoweringContext
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaDomainSchema
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaPluginValidationResult
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaValidationContribution
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewSummary

/** Electrical domain plugin hosted behind the stable domain extension surface. */
class ElectricalRuntimeDomainPlugin :
    AthenaDomainPlugin,
    AthenaViewDefinitionContributor,
    AthenaSemanticReviewEnrichmentContributor {
    /** Core-owned manifest declaring plugin identity, type, compatibility, and extension points. */
    override val manifest: AthenaPluginManifest = ELECTRICAL_RUNTIME_MANIFEST

    /** Minimal capability declaration showing the plugin remains a domain extension rather than core authority. */
    override val domainCapabilities: Set<String> = ELECTRICAL_RUNTIME_CAPABILITIES

    /** Generic electrical evidence schema published through the stable plugin API. */
    override val domainSchema: AthenaDomainSchema = ELECTRICAL_DOMAIN_SCHEMA

    /** Inspectable validation contribution declarations exposed by the electrical evidence plugin. */
    override val validationContributions: List<AthenaValidationContribution> = ELECTRICAL_VALIDATION_CONTRIBUTIONS

    /** Inspectable compiler-stage contribution declarations exposed by the electrical evidence plugin. */
    override val compilerPassContributions: List<AthenaCompilerPassContribution> = ELECTRICAL_COMPILER_PASS_CONTRIBUTIONS

    /** Lowers authored Electrical/Runtime evidence declarations into compiler-owned semantic blueprints. */
    override fun lower(context: AthenaDomainLoweringContext): AthenaDomainLoweringContribution {
        return lowerElectricalRuntime(context)
    }

    /** Validates Electrical/Runtime evidence properties and connection compatibility over canonical Engineering IR. */
    override fun validate(context: AthenaPluginValidationContext): AthenaPluginValidationResult {
        return validateElectricalRuntime(context)
    }

    /** Contributes the first governed electrical projection-family set without turning views into semantic truth. */
    override fun viewDefinitions() = ELECTRICAL_RUNTIME_VIEW_DEFINITIONS

    /** Adds electrical review interpretation without mutating or replacing the core semantic review facts. */
    override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> {
        return enrichElectricalRuntimeReview(
            pluginId = manifest.pluginId,
            review = review,
        )
    }
}
