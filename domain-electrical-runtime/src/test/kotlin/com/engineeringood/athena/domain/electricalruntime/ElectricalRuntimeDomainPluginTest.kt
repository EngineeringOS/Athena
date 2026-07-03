package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginType
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
            setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
            plugin.manifest.requiredExtensionPoints,
        )
        assertEquals(setOf("electrical-runtime"), plugin.domainCapabilities)
    }
}
