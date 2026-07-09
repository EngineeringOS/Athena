package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaHostedPluginRegistryTest {
    @Test
    fun `registry exposes loaded initialized and shutdown lifecycle states over one approved inventory`() {
        val registry = AthenaHostedPluginRegistry(
            discoveredReport = AthenaPluginDiscovery(
                source = FixedAthenaPluginSource(
                    listOf(
                        ElectricalRuntimeDomainPlugin(),
                        AlphaDomainTestPlugin(),
                    ),
                ),
            ).discover(),
        )

        val loaded = registry.lifecycleSnapshot()
        val initialized = registry.initializeHostedPlugins()
        val shutdown = registry.shutdownHostedPlugins()

        assertEquals(AthenaHostedPluginLifecycleState.LOADED, loaded.state)
        assertEquals(AthenaHostedPluginLifecycleState.INITIALIZED, initialized.state)
        assertEquals(AthenaHostedPluginLifecycleState.SHUTDOWN, shutdown.state)
        assertEquals(2, loaded.inventory.approvedPluginCount)
        assertEquals(2, loaded.inventory.candidateCount)
        assertTrue(
            loaded.inventory.approvedPlugins.any { plugin ->
                plugin.pluginId == "com.engineeringood.athena.domain.electrical-runtime"
            },
        )
        assertTrue(
            initialized.inventory.approvedPlugins.any { plugin ->
                AthenaHostedPluginContributionCategory.RUNTIME_COMMAND in plugin.contributionCategories
            },
        )
        assertTrue(
            initialized.inventory.approvedPlugins.any { plugin ->
                AthenaHostedPluginContributionCategory.SEMANTIC_REVIEW_ENRICHMENT in plugin.contributionCategories
            },
        )
        assertTrue(
            shutdown.inventory.approvedPlugins.all { plugin ->
                plugin.lifecycleState == AthenaHostedPluginLifecycleState.SHUTDOWN
            },
        )
    }
}
