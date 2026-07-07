package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.plugin.AthenaCoreRuntime
import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.compiler.plugin.AthenaPluginManifest
import com.engineeringood.athena.compiler.plugin.AthenaPluginSource
import com.engineeringood.athena.compiler.plugin.AthenaPluginType
import com.engineeringood.athena.compiler.plugin.CoreVersionRange
import com.engineeringood.athena.compiler.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import com.engineeringood.athena.layout.LayoutIntent
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

        assertContains(hostedPluginIds, "com.engineeringood.athena.domain.electrical-runtime")
        assertContains(hostedDomainPluginIds, "com.engineeringood.athena.domain.electrical-runtime")
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
                .filterIsInstance<com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin>()
                .map { plugin -> plugin.manifest.pluginId },
        )
        assertTrue(pluginServices.coreOwnedInvariants().isNotEmpty())
        val electricalPlugin = hostedPlugins.first { plugin -> plugin.pluginId == "com.engineeringood.athena.domain.electrical-runtime" }
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.VIEW_DEFINITIONS,
                AthenaExtensionPoint.RUNTIME_COMMANDS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            electricalPlugin.attachedExtensionPoints,
        )
        assertEquals(listOf("cabinet", "wiring"), electricalPlugin.viewDefinitionIds)
    }

    @Test
    fun `hosted runtime plugin command contributions still execute through the command runtime`() {
        val sourcePath = writeProject(
            """
                system PluginCommandDemo {
                  device PLC1 {
                    type PLC
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
    fun `hosted runtime plugin view contributions derive runtime owned inspector and diagnostics data`() {
        val sourcePath = writeProject(
            """
                system PluginViewDemo {
                  device PLC1 {
                    type PLC
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

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-plugin-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }
}

private class FixedAthenaPluginSource(
    private val plugins: List<com.engineeringood.athena.compiler.plugin.AthenaPlugin>,
) : AthenaPluginSource {
    override fun loadPlugins(): List<com.engineeringood.athena.compiler.plugin.AthenaPlugin> = plugins
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
