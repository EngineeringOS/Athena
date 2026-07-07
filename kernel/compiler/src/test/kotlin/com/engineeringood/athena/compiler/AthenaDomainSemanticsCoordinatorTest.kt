package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SystemDeclaration
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaSourceDocument
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.host.ApprovedAthenaPlugin
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.host.AthenaPluginCandidate
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaDomainSemanticsCoordinatorTest {
    @Test
    fun `aggregates lowering and validation contributions in deterministic approved-plugin order`() {
        val coordinator = AthenaDomainSemanticsCoordinator(
            inventory = AthenaApprovedPluginInventory.fromApproved(
                listOf(
                    approvedPlugin(ZetaSemanticsTestPlugin()),
                    approvedPlugin(AlphaSemanticsTestPlugin()),
                ),
            ),
        )
        val source = CompilerSourceDocument(
            file = "examples/m0/deterministic.athena",
            ast = SourceFileAst(
                system = SystemDeclaration("Deterministic", span(1, 1, 1, 14)),
                declarations = emptyList(),
                span = span(1, 1, 1, 14),
            ),
        )

        val lowering = coordinator.lower(source)

        assertEquals(
            listOf("AlphaDevice", "ZetaDevice"),
            lowering.components.map { it.name },
        )

        val validation = coordinator.validate(
            document = EngineeringDocument(
                system = EngineeringSystem(
                    id = StableSemanticIdentity("system:Deterministic"),
                    name = "Deterministic",
                    provenance = SourceProvenance(source.file, 1, 1, 1, 14),
                ),
                components = emptyList(),
                ports = emptyList(),
                connections = emptyList(),
            ),
            context = AthenaPluginValidationContext(
                document = EngineeringDocument(
                    system = EngineeringSystem(
                        id = StableSemanticIdentity("system:Deterministic"),
                        name = "Deterministic",
                        provenance = SourceProvenance(source.file, 1, 1, 1, 14),
                    ),
                    components = emptyList(),
                    ports = emptyList(),
                    connections = emptyList(),
                ),
                source = AthenaSourceDocument(
                    file = source.file,
                    ast = source.ast,
                ),
                approvedPluginIds = listOf(
                    "com.engineeringood.athena.domain.alpha-semantics",
                    "com.engineeringood.athena.domain.zeta-semantics",
                ),
            ),
        )

        assertEquals(
            listOf(
                "domain.validation.alpha",
                "domain.validation.zeta",
            ),
            validation.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha-semantics",
                "com.engineeringood.athena.domain.zeta-semantics",
            ),
            validation.attributions.map { attribution -> attribution.pluginId },
        )
        assertEquals(
            listOf(
                "alpha-semantics.validation.rules",
                "zeta-semantics.validation.rules",
            ),
            validation.attributions.map { attribution -> attribution.contributionId },
        )
        assertEquals(
            listOf(
                listOf("domain.validation.alpha"),
                listOf("domain.validation.zeta"),
            ),
            validation.attributions.map { attribution -> attribution.ruleIds.map { ruleId -> ruleId.value } },
        )
        assertEquals(
            listOf(
                SemanticDiagnosticCategory.DOMAIN,
                SemanticDiagnosticCategory.DOMAIN,
            ),
            validation.diagnostics.map { it.category },
        )
    }

    @Test
    fun `domain callbacks run only when the plugin declared the matching compiler stage`() {
        val coordinator = AthenaDomainSemanticsCoordinator(
            activeDomainPlugins = listOf(
                LowerOnlySemanticsTestPlugin(),
                ValidateOnlySemanticsTestPlugin(),
                SemanticEnrichmentOnlyTestPlugin(),
            ),
        )
        val source = CompilerSourceDocument(
            file = "examples/m0/staged.athena",
            ast = SourceFileAst(
                system = SystemDeclaration("Staged", span(1, 1, 1, 8)),
                declarations = emptyList(),
                span = span(1, 1, 1, 8),
            ),
        )
        val document = EngineeringDocument(
            system = EngineeringSystem(
                id = StableSemanticIdentity("system:Staged"),
                name = "Staged",
                provenance = SourceProvenance(source.file, 1, 1, 1, 8),
            ),
            components = emptyList(),
            ports = emptyList(),
            connections = emptyList(),
        )

        val lowering = coordinator.lower(source)
        val validation = coordinator.validate(
            document = document,
            context = AthenaPluginValidationContext(
                document = document,
                source = AthenaSourceDocument(
                    file = source.file,
                    ast = source.ast,
                ),
                approvedPluginIds = coordinator.activePluginIds,
            ),
        )
        val enrichment = coordinator.enrichSemantics(
            document = document,
            context = AthenaSemanticEnrichmentContext(
                document = document,
                source = AthenaSourceDocument(
                    file = source.file,
                    ast = source.ast,
                ),
                approvedPluginIds = coordinator.activePluginIds,
            ),
        )

        assertEquals(listOf("LowerOnlyDevice"), lowering.components.map { it.name })
        assertEquals(
            listOf("domain.validation.validate-only"),
            validation.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("validate-only.validation.rules"),
            validation.attributions.map { attribution -> attribution.contributionId },
        )
        assertEquals(
            listOf("synthetic semantic enrichment note"),
            enrichment.notes.map { it.message },
        )
        assertEquals(
            listOf("domain.enrichment.synthetic"),
            enrichment.diagnostics.map { it.ruleId.value },
        )
    }

    private fun approvedPlugin(plugin: AthenaDomainPlugin): ApprovedAthenaPlugin {
        return ApprovedAthenaPlugin(
            candidate = AthenaPluginCandidate(
                plugin = plugin,
                implementationClassName = plugin::class.java.name,
                manifest = plugin.manifest,
            ),
        )
    }

    private fun span(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceSpan {
        return SourceSpan(
            start = SourcePosition(offset = 0, line = startLine, column = startColumn),
            end = SourcePosition(offset = 0, line = endLine, column = endColumn),
        )
    }
}
