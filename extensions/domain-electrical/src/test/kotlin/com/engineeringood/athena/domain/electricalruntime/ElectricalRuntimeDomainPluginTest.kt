package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginType
import com.engineeringood.athena.compiler.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ViewEmphasis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ElectricalRuntimeDomainPluginTest {
    @Test
    fun `publishes the sample electrical runtime plugin through the domain contract`() {
        val plugin = ElectricalRuntimeDomainPlugin()

        assertIs<AthenaDomainPlugin>(plugin)
        assertEquals(AthenaPluginType.DOMAIN, plugin.manifest.pluginType)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.VIEW_DEFINITIONS,
                AthenaExtensionPoint.RUNTIME_COMMANDS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            plugin.manifest.requiredExtensionPoints,
        )
        assertEquals(setOf("electrical-runtime"), plugin.domainCapabilities)
    }

    @Test
    fun `publishes cabinet and wiring as the first supported view-definition proof pair`() {
        val plugin = ElectricalRuntimeDomainPlugin()
        val contributor = assertIs<AthenaViewDefinitionContributor>(plugin)

        val viewDefinitions = contributor.viewDefinitions()
        val cabinet = viewDefinitions.first { definition -> definition.id == "cabinet" }
        val wiring = viewDefinitions.first { definition -> definition.id == "wiring" }

        assertEquals(listOf("cabinet", "wiring"), viewDefinitions.map { definition -> definition.id })
        assertEquals(LayoutIntent.STRUCTURAL, cabinet.layoutIntent)
        assertEquals(listOf("group-by-owner", "group-by-component"), cabinet.groupingRules)
        assertEquals(listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT), cabinet.viewEmphasis)
        assertEquals(LayoutIntent.CONNECTIVITY, wiring.layoutIntent)
        assertEquals(listOf("group-by-signal", "group-by-connection-path"), wiring.groupingRules)
        assertEquals(listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW), wiring.viewEmphasis)
    }
}
