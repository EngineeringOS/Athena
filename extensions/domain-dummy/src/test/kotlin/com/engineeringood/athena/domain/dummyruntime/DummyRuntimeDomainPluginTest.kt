package com.engineeringood.athena.domain.dummyruntime

import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContributor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DummyRuntimeDomainPluginTest {
    @Test
    fun `publishes the synthetic dummy runtime plugin through the domain contract`() {
        val plugin = DummyRuntimeDomainPlugin()

        assertIs<AthenaDomainPlugin>(plugin)
        assertIs<AthenaRuntimePluginViewContributor>(plugin)
        assertEquals(AthenaPluginType.DOMAIN, plugin.manifest.pluginType)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            plugin.manifest.requiredExtensionPoints,
        )
        assertEquals(setOf("dummy-runtime"), plugin.domainCapabilities)
    }

    @Test
    fun `publishes synthetic schema and render metadata without global view definitions`() {
        val plugin = DummyRuntimeDomainPlugin()

        assertEquals("dummy-runtime", plugin.domainSchema.domainId)
        assertEquals(
            listOf("Glyph", "Pulse", "Totem"),
            plugin.domainSchema.entities.map { entity -> entity.typeId },
        )
        assertEquals(
            listOf("DummyLink"),
            plugin.domainSchema.connections.map { connection -> connection.typeId },
        )
        assertEquals(
            setOf("Glyph", "Pulse", "Totem"),
            plugin.domainSchema.properties.first { property -> property.name == "type" }.allowedSymbolValues,
        )
        assertEquals(
            listOf("dummy-runtime.render.synthetic-panel"),
            plugin.renderContributions.map { contribution -> contribution.contributionId },
        )
        assertEquals(
            listOf(setOf("dummy-panel")),
            plugin.renderContributions.map { contribution -> contribution.viewIds },
        )
    }
}
