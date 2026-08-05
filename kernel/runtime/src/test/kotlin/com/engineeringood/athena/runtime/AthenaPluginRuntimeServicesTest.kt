package com.engineeringood.athena.runtime

import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaPluginRuntimeServicesTest {
    @Test
    fun `runtime and compiler share approved domain plugins`() {
        val runtime = AthenaRuntime()
        val services = runtime.serviceRegistry.pluginRuntimeServices()

        val hostedIds = services.domainSemanticsContributions().map { contribution -> contribution.pluginId }
        val compilerIds = runtime.serviceRegistry.compiler().pluginInventory.approvedPlugins
            .map { approved -> approved.candidate.plugin }
            .filterIsInstance<AthenaDomainPlugin>()
            .map { plugin -> plugin.manifest.pluginId }

        assertEquals(hostedIds, compilerIds)
        assertTrue(hostedIds.contains("com.engineeringood.athena.domain.electrical-runtime"))
    }

    @Test
    fun `electrical plugin exposes only current runtime extension points`() {
        val services = AthenaHostedPluginRuntimeServices()
        val electrical = services.hostedPlugins().single { plugin ->
            plugin.pluginId == "com.engineeringood.athena.domain.electrical-runtime"
        }

        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.VIEW_DEFINITIONS,
                AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
            ),
            electrical.attachedExtensionPoints,
        )
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            electrical.viewDefinitionIds,
        )
    }

    @Test
    fun `shutdown removes active contributions but preserves inventory`() {
        val services = AthenaHostedPluginRuntimeServices()

        val shutdown = services.shutdownHostedPlugins()

        assertEquals(AthenaHostedPluginLifecycleState.SHUTDOWN, shutdown.state)
        assertTrue(shutdown.inventory.approvedPluginCount > 0)
        assertEquals(emptyList(), services.domainSemanticsContributions())
        assertEquals(emptyList(), services.semanticReviewEnrichmentContributors())
        assertEquals(emptyList(), services.viewDefinitionContributions())
    }
}
