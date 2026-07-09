package com.engineeringood.athena.plugin

import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PluginApiContributionContractTest {
    @Test
    fun `publishes generic domain schema and inspectable contributions through the stable plugin api`() {
        val plugin = object : AthenaDomainPlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.domain.synthetic",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
            )
            override val domainSchema: AthenaDomainSchema = AthenaDomainSchema(
                domainId = "synthetic-domain",
                displayName = "Synthetic Domain",
                capabilities = setOf("synthetic"),
                entities = listOf(
                    AthenaDomainEntitySchema(
                        typeId = "synthetic-device",
                        displayName = "Synthetic Device",
                        subjectKind = AthenaDomainSchemaSubjectKind.COMPONENT,
                        propertyNames = setOf("category"),
                        portTypeIds = setOf("synthetic-port"),
                    ),
                ),
                properties = listOf(
                    AthenaDomainPropertySchema(
                        name = "category",
                        displayName = "Category",
                        valueKind = AthenaDomainPropertyValueKind.SYMBOL,
                        appliesTo = setOf(AthenaDomainSchemaSubjectKind.COMPONENT),
                        required = true,
                        allowedSymbolValues = setOf("synthetic-device"),
                    ),
                ),
                ports = listOf(
                    AthenaDomainPortSchema(
                        typeId = "synthetic-port",
                        displayName = "Synthetic Port",
                        propertyNames = setOf("direction"),
                        allowedDirections = setOf("in", "out"),
                    ),
                ),
                connections = listOf(
                    AthenaDomainConnectionSchema(
                        typeId = "synthetic-link",
                        displayName = "Synthetic Link",
                        sourcePortTypeIds = setOf("synthetic-port"),
                        targetPortTypeIds = setOf("synthetic-port"),
                    ),
                ),
            )
            override val validationContributions: List<AthenaValidationContribution> = listOf(
                AthenaValidationContribution(
                    contributionId = "synthetic.validation.compatibility",
                    displayName = "Synthetic validation",
                    description = "Validates synthetic-domain compatibility rules.",
                ),
            )
            override val compilerPassContributions: List<AthenaCompilerPassContribution> = listOf(
                AthenaCompilerPassContribution(
                    contributionId = "synthetic.lowering",
                    stage = AthenaCompilerContributionStage.LOWER,
                    displayName = "Synthetic lowering",
                    description = "Interprets generic authored structures as synthetic-domain meaning.",
                ),
            )
            override val renderContributions: List<AthenaRenderContribution> = listOf(
                AthenaRenderContribution(
                    contributionId = "synthetic.render.default",
                    displayName = "Synthetic render intent",
                    description = "Publishes renderer-facing intent without owning backend orchestration.",
                    viewIds = setOf("default"),
                    rendererTargets = setOf("svg"),
                ),
            )
        }

        assertIs<AthenaDomainSchemaContributor>(plugin)
        assertIs<AthenaValidationContributor>(plugin)
        assertIs<AthenaCompilerPassContributor>(plugin)
        assertIs<AthenaRenderContributor>(plugin)
        assertEquals("synthetic-domain", plugin.domainSchema.domainId)
        assertEquals(listOf("synthetic-device"), plugin.domainSchema.entities.map { entity -> entity.typeId })
        assertEquals(listOf("synthetic-port"), plugin.domainSchema.ports.map { port -> port.typeId })
        assertEquals(listOf("synthetic-link"), plugin.domainSchema.connections.map { connection -> connection.typeId })
        assertEquals(
            listOf("synthetic.validation.compatibility"),
            plugin.validationContributions.map { contribution -> contribution.contributionId },
        )
        assertEquals(
            listOf(AthenaCompilerContributionStage.LOWER),
            plugin.compilerPassContributions.map { contribution -> contribution.stage },
        )
        assertEquals(
            listOf(setOf("svg")),
            plugin.renderContributions.map { contribution -> contribution.rendererTargets },
        )
    }

    @Test
    fun `publishes additive semantic review enrichment through the stable plugin api`() {
        val plugin = object : AthenaDomainPlugin, AthenaSemanticReviewEnrichmentContributor {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.domain.synthetic-review",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(
                    AthenaExtensionPoint.DOMAIN_SEMANTICS,
                    AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
                ),
            )
            override val domainCapabilities: Set<String> = setOf("synthetic-review")

            override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> {
                return listOf(
                    SemanticReviewEnrichment(
                        pluginId = manifest.pluginId,
                        kind = SemanticReviewEnrichmentKind.DOMAIN_SUMMARY,
                        message = "Synthetic review covers the current semantic review summary.",
                    ),
                )
            }
        }

        val enrichments = plugin.enrichReview(
            SemanticReviewSummary(
                baseline = SemanticBaselineDescriptor(
                    baselineId = "baseline",
                    label = "Baseline",
                ),
            ),
        )

        assertIs<AthenaSemanticReviewEnrichmentContributor>(plugin)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
            ),
            plugin.manifest.requiredExtensionPoints,
        )
        assertEquals(1, enrichments.size)
        assertEquals(SemanticReviewEnrichmentKind.DOMAIN_SUMMARY, enrichments.single().kind)
        assertTrue(enrichments.single().message.contains("semantic review summary"))
    }
}
