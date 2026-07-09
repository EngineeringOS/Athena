package com.engineeringood.athena.runtime

import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPlugin
import com.engineeringood.athena.plugin.AthenaPluginManifest
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.CoreVersionRange
import com.engineeringood.athena.plugin.host.AthenaHostedPluginContributionCategory
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleState
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import com.engineeringood.athena.plugin.host.AthenaPluginSource
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.Path

class AthenaPluginRuntimeServicesTest {
    @Test
    fun `default runtime hosts an inspectable approved plugin inventory and shares it with compiler`() {
        val runtime = AthenaRuntime()

        val pluginServices = runtime.serviceRegistry.pluginRuntimeServices()
        val hostedPlugins = pluginServices.hostedPlugins()
        val hostedPluginIds = hostedPlugins.map { plugin -> plugin.pluginId }
        val hostedDomainPluginIds = pluginServices.domainSemanticsContributions().map { contribution -> contribution.pluginId }
        val compilerPluginIds = runtime.serviceRegistry.compiler().pluginInventory.approvedPlugins.map { plugin ->
            plugin.candidate.manifest.pluginId
        }

        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.dummy-runtime",
                "com.engineeringood.athena.domain.electrical-runtime",
            ),
            hostedPluginIds,
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.dummy-runtime",
                "com.engineeringood.athena.domain.electrical-runtime",
            ),
            hostedDomainPluginIds,
        )
        assertEquals(hostedPluginIds, compilerPluginIds)
        assertEquals(
            pluginServices.discoveryReport().approvedInventory.approvedPlugins.map { plugin ->
                plugin.candidate.manifest.pluginId
            },
            compilerPluginIds,
        )
        assertEquals(
            hostedDomainPluginIds,
            runtime.serviceRegistry.compiler().pluginInventory
                .approvedPlugins
                .map { plugin -> plugin.candidate.plugin }
                .filterIsInstance<AthenaDomainPlugin>()
                .map { plugin -> plugin.manifest.pluginId },
        )
        assertTrue(pluginServices.coreOwnedInvariants().isNotEmpty())
        val dummyPlugin = hostedPlugins.first { plugin -> plugin.pluginId == "com.engineeringood.athena.domain.dummy-runtime" }
        val electricalPlugin = hostedPlugins.first { plugin -> plugin.pluginId == "com.engineeringood.athena.domain.electrical-runtime" }
        assertEquals(AthenaHostedPluginLifecycleState.INITIALIZED, pluginServices.hostedLifecycle().state)
        assertEquals(AthenaHostedPluginLifecycleState.INITIALIZED, dummyPlugin.lifecycleState)
        assertEquals(AthenaHostedPluginLifecycleState.INITIALIZED, electricalPlugin.lifecycleState)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            dummyPlugin.attachedExtensionPoints,
        )
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.VIEW_DEFINITIONS,
                AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
                AthenaExtensionPoint.RUNTIME_COMMANDS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            electricalPlugin.attachedExtensionPoints,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.RUNTIME_COMMAND !in dummyPlugin.contributionCategories,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.RUNTIME_VIEW in dummyPlugin.contributionCategories,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.RENDER in dummyPlugin.contributionCategories,
        )
        assertEquals(emptyList(), dummyPlugin.viewDefinitionIds)
        assertTrue(
            AthenaHostedPluginContributionCategory.RUNTIME_COMMAND in electricalPlugin.contributionCategories,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.RUNTIME_VIEW in electricalPlugin.contributionCategories,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.SEMANTIC_REVIEW_ENRICHMENT in electricalPlugin.contributionCategories,
        )
        assertTrue(
            AthenaHostedPluginContributionCategory.RENDER in electricalPlugin.contributionCategories,
        )
        assertEquals(listOf("cabinet", "wiring"), electricalPlugin.viewDefinitionIds)
        assertEquals(0, dummyPlugin.semanticReviewEnrichmentCount)
        assertEquals(1, electricalPlugin.semanticReviewEnrichmentCount)
    }

    @Test
    fun `hosted runtime plugin command contributions still execute through the command runtime`() {
        val sourcePath = writeProject(
            """
                system PluginCommandDemo {
                  device PLC1 {
                    type Switch
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(sourcePath.parent)
            val context = workspace.activateProject(
                projectName = "plugin-command-demo",
                sourcePath = sourcePath,
            )
            val pluginServices = runtime.serviceRegistry.pluginRuntimeServices()

            val contributionIds = pluginServices.commandContributions().map { contribution -> contribution.contributionId }
            val execution = pluginServices.executeCommandContribution(
                context = context,
                contributionId = "electrical-runtime.connect-first-compatible",
            )

            assertContains(contributionIds, "electrical-runtime.connect-first-compatible")
            val success = assertIs<AthenaRuntimePluginCommandExecutionSuccess>(execution)
            assertEquals(AthenaCommandKind.CONNECT_PORTS, success.result.commandKind)
            assertEquals(1, context.commandRuntime().history(context).records.size)
            val graphProjection = assertIs<AthenaEngineeringGraphReadyProjection>(context.projectEngineeringGraphProjection())
            assertEquals(1, graphProjection.graph.nodesOfKind(AthenaEngineeringGraphNodeKind.CONNECTION).size)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `hosted runtime plugin view contributions stay scoped to dummy-owned declarations`() {
        val sourcePath = writeProject(
            """
                system DummyPluginViewDemo {
                  device G1 {
                    domain "dummy-runtime"
                    type Glyph
                  }

                  device P1 {
                    domain "dummy-runtime"
                    type Pulse
                  }

                  port G1.emit {
                    flow emit
                    tint Amber
                  }

                  port P1.absorb {
                    flow absorb
                    tint Amber
                  }

                  connect G1.emit -> P1.absorb
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(sourcePath.parent)
            val context = workspace.activateProject(
                projectName = "dummy-plugin-view-demo",
                sourcePath = sourcePath,
            )

            val contributions = runtime.serviceRegistry.pluginRuntimeServices().viewContributions(context)

            assertEquals(
                listOf("com.engineeringood.athena.domain.dummy-runtime"),
                contributions.map { contribution -> contribution.pluginId },
            )
            assertContains(
                contributions.flatMap { contribution -> contribution.inspectorGroups }.map { group -> group.title },
                "Dummy runtime",
            )
            assertTrue(
                contributions.flatMap { contribution -> contribution.diagnosticsEntries }
                    .any { entry -> entry.contains("Dummy runtime plugin", ignoreCase = true) },
            )
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `hosted runtime plugin view contributions derive runtime owned inspector and diagnostics data`() {
        val sourcePath = writeProject(
            """
                system PluginViewDemo {
                  device PLC1 {
                    type Switch
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(sourcePath.parent)
            val context = workspace.activateProject(
                projectName = "plugin-view-demo",
                sourcePath = sourcePath,
            )

            val contributions = runtime.serviceRegistry.pluginRuntimeServices().viewContributions(context)

            assertTrue(contributions.isNotEmpty())
            assertContains(contributions.map { contribution -> contribution.pluginId }, "com.engineeringood.athena.domain.electrical-runtime")
            assertContains(
                contributions.flatMap { contribution -> contribution.inspectorGroups }.map { group -> group.title },
                "Electrical runtime",
            )
            assertTrue(
                contributions.flatMap { contribution -> contribution.diagnosticsEntries }
                    .any { entry -> entry.contains("Electrical runtime plugin", ignoreCase = true) },
            )
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `hosted runtime services expose electrical view-definition contributions deterministically`() {
        val runtime = AthenaRuntime()

        val contributions = runtime.serviceRegistry.pluginRuntimeServices().viewDefinitionContributions()
        assertEquals(
            listOf("com.engineeringood.athena.domain.electrical-runtime"),
            contributions.map { contribution -> contribution.pluginId },
        )
        val electricalContribution = contributions.first { contribution ->
            contribution.pluginId == "com.engineeringood.athena.domain.electrical-runtime"
        }
        val cabinet = electricalContribution.viewDefinitions.first { definition -> definition.id == "cabinet" }
        val wiring = electricalContribution.viewDefinitions.first { definition -> definition.id == "wiring" }

        assertEquals(listOf("cabinet", "wiring"), electricalContribution.viewDefinitions.map { definition -> definition.id })
        assertEquals(LayoutIntent.STRUCTURAL, cabinet.layoutIntent)
        assertEquals(LayoutIntent.CONNECTIVITY, wiring.layoutIntent)
        assertTrue(cabinet.groupingRules.isNotEmpty())
        assertTrue(wiring.groupingRules.isNotEmpty())
    }

    @Test
    fun `hosted runtime services expose electrical render contributions deterministically`() {
        val runtime = AthenaRuntime()

        val contributions = runtime.serviceRegistry.pluginRuntimeServices().renderContributions()
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.dummy-runtime",
                "com.engineeringood.athena.domain.electrical-runtime",
            ),
            contributions.map { contribution -> contribution.pluginId },
        )
        val dummyContribution = contributions.first { contribution ->
            contribution.pluginId == "com.engineeringood.athena.domain.dummy-runtime"
        }
        val electricalContribution = contributions.first { contribution ->
            contribution.pluginId == "com.engineeringood.athena.domain.electrical-runtime"
        }

        assertEquals(
            listOf("dummy-runtime.render.synthetic-panel"),
            dummyContribution.renderContributions.map { contribution -> contribution.contributionId },
        )
        assertEquals(
            listOf(setOf("dummy-panel")),
            dummyContribution.renderContributions.map { contribution -> contribution.viewIds },
        )
        assertEquals(
            listOf(setOf("svg")),
            dummyContribution.renderContributions.map { contribution -> contribution.rendererTargets },
        )
        assertEquals(
            listOf(
                "electrical-runtime.render.cabinet",
                "electrical-runtime.render.wiring",
            ),
            electricalContribution.renderContributions.map { contribution -> contribution.contributionId },
        )
        assertEquals(
            listOf(setOf("cabinet"), setOf("wiring")),
            electricalContribution.renderContributions.map { contribution -> contribution.viewIds },
        )
        assertEquals(
            listOf(setOf("svg"), setOf("svg")),
            electricalContribution.renderContributions.map { contribution -> contribution.rendererTargets },
        )
    }

    @Test
    fun `hosted runtime services expose semantic review enrichment contributors deterministically`() {
        val pluginServices = AthenaHostedPluginRuntimeServices()

        val contributors = pluginServices.semanticReviewEnrichmentContributors()
        val enrichments = pluginServices.enrichReview(
            SemanticReviewSummary(
                baseline = SemanticBaselineDescriptor(
                    baselineId = "baseline",
                    label = "Baseline",
                ),
                affectedPackages = listOf(PackageIdentifier("com.engineeringood.demo", "1.0.0")),
                diagnostics = listOf(
                    SemanticDiagnostic(
                        severity = SemanticDiagnosticSeverity.ERROR,
                        ruleId = SemanticRuleId("connection.signal.incompatible"),
                        category = SemanticDiagnosticCategory.CONNECTION,
                        subjectIdentity = null,
                        provenance = SourceProvenance(
                            file = "src/demo.athena",
                            startLine = 1,
                            startColumn = 1,
                            endLine = 1,
                            endColumn = 1,
                        ),
                        message = "Connection `PLC1.out -> M1.in` mixes incompatible signals `Digital` and `Analog`.",
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("com.engineeringood.athena.domain.electrical-runtime"),
            contributors.map { contribution -> contribution.pluginId },
        )
        assertEquals(
            listOf(
                SemanticReviewEnrichmentKind.DOMAIN_LABEL,
                SemanticReviewEnrichmentKind.REVIEW_HINT,
                SemanticReviewEnrichmentKind.DOMAIN_SUMMARY,
            ),
            enrichments.map { enrichment -> enrichment.kind },
        )
    }

    @Test
    fun `hosted runtime services reject plugins that overreach declared runtime contracts`() {
        val pluginServices = AthenaHostedPluginRuntimeServices(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(
                        ElectricalRuntimeDomainPlugin(),
                        UndeclaredRuntimeCommandTestPlugin(),
                        DeclaredButMissingRuntimeCommandTestPlugin(),
                        UndeclaredViewDefinitionTestPlugin(),
                        DeclaredButMissingViewDefinitionTestPlugin(),
                        UndeclaredSemanticReviewEnrichmentTestPlugin(),
                        DeclaredButMissingSemanticReviewEnrichmentTestPlugin(),
                        UndeclaredRuntimeViewTestPlugin(),
                        DeclaredButMissingRuntimeViewTestPlugin(),
                    ),
                ),
            ),
        )

        val report = pluginServices.discoveryReport()

        assertEquals(
            listOf("com.engineeringood.athena.domain.electrical-runtime"),
            report.approvedInventory.approvedPlugins.map { plugin -> plugin.candidate.manifest.pluginId },
        )
        assertEquals(
            listOf(
                "plugin.runtime.contract.command.undeclared",
                "plugin.runtime.contract.command.unimplemented",
                "plugin.runtime.contract.semantic-review-enrichment.undeclared",
                "plugin.runtime.contract.semantic-review-enrichment.unimplemented",
                "plugin.runtime.contract.view-definition.undeclared",
                "plugin.runtime.contract.view-definition.unimplemented",
                "plugin.runtime.contract.view.undeclared",
                "plugin.runtime.contract.view.unimplemented",
            ),
            report.rejectedCandidates.flatMap { candidate -> candidate.diagnostics.map { it.ruleId.value } }.sorted(),
        )
        assertTrue(
            pluginServices.coreOwnedInvariants().any { invariant ->
                invariant.contains("Engineering IR", ignoreCase = true)
            },
        )
    }

    @Test
    fun `hosted runtime review enrichment failures degrade into additive warnings`() {
        val pluginServices = AthenaHostedPluginRuntimeServices(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(ThrowingSemanticReviewEnrichmentTestPlugin()),
                ),
            ),
        )

        val enrichments = pluginServices.enrichReview(
            SemanticReviewSummary(
                baseline = SemanticBaselineDescriptor(
                    baselineId = "baseline",
                    label = "Baseline",
                ),
            ),
        )

        assertEquals(1, enrichments.size)
        assertEquals(SemanticReviewEnrichmentKind.PLUGIN_WARNING, enrichments.single().kind)
        assertTrue(enrichments.single().message.contains("failed", ignoreCase = true))
    }

    @Test
    fun `hosted runtime lifecycle can be shut down without losing inspection evidence`() {
        val pluginServices = AthenaHostedPluginRuntimeServices()

        val initialized = pluginServices.hostedLifecycle()
        val shutdown = pluginServices.shutdownHostedPlugins()

        assertEquals(AthenaHostedPluginLifecycleState.INITIALIZED, initialized.state)
        assertEquals(AthenaHostedPluginLifecycleState.SHUTDOWN, shutdown.state)
        assertTrue(shutdown.inventory.approvedPluginCount > 0)
        assertTrue(shutdown.inventory.approvedPlugins.all { plugin -> plugin.lifecycleState == AthenaHostedPluginLifecycleState.SHUTDOWN })
        assertEquals(emptyList(), pluginServices.commandContributions())
        assertEquals(emptyList(), pluginServices.domainSemanticsContributions())
        assertEquals(emptyList(), pluginServices.renderContributions())
        assertEquals(emptyList(), pluginServices.semanticReviewEnrichmentContributors())
        assertEquals(emptyList(), pluginServices.viewDefinitionContributions())
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-plugin-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }
}

private class FixedAthenaPluginSource(
    private val plugins: List<AthenaPlugin>,
) : AthenaPluginSource {
    override fun loadPlugins(): List<AthenaPlugin> = plugins
}

private class UndeclaredRuntimeCommandTestPlugin : AthenaDomainPlugin, AthenaRuntimePluginCommandContributor {
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

private class DeclaredButMissingRuntimeCommandTestPlugin : AthenaDomainPlugin {
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

private class UndeclaredRuntimeViewTestPlugin : AthenaDomainPlugin, AthenaRuntimePluginViewContributor {
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

private class UndeclaredViewDefinitionTestPlugin : AthenaDomainPlugin, AthenaViewDefinitionContributor {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.undeclared-view-definition",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun viewDefinitions(): List<com.engineeringood.athena.layout.ViewDefinition> {
        return listOf(
            com.engineeringood.athena.layout.ViewDefinition(
                id = "undeclared",
                displayName = "Undeclared",
            ),
        )
    }
}

private class DeclaredButMissingViewDefinitionTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.missing-view-definition",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.VIEW_DEFINITIONS,
        ),
    )
}

private class UndeclaredSemanticReviewEnrichmentTestPlugin : AthenaDomainPlugin, AthenaSemanticReviewEnrichmentContributor {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.undeclared-semantic-review-enrichment",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
    )

    override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> = emptyList()
}

private class DeclaredButMissingSemanticReviewEnrichmentTestPlugin : AthenaDomainPlugin {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.missing-semantic-review-enrichment",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
        ),
    )
}

private class ThrowingSemanticReviewEnrichmentTestPlugin : AthenaDomainPlugin, AthenaSemanticReviewEnrichmentContributor {
    override val manifest: AthenaPluginManifest = AthenaPluginManifest(
        pluginId = "com.engineeringood.athena.domain.throwing-semantic-review-enrichment",
        pluginVersion = "0.0.1-SNAPSHOT",
        pluginType = AthenaPluginType.DOMAIN,
        coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
        requiredExtensionPoints = setOf(
            AthenaExtensionPoint.DOMAIN_SEMANTICS,
            AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
        ),
    )

    override fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> {
        error("boom")
    }
}

private class DeclaredButMissingRuntimeViewTestPlugin : AthenaDomainPlugin {
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
