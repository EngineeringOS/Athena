package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaCoreRuntime
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AthenaPluginDiscoveryTest {
    @Test
    fun `discovers the sample electrical runtime plugin from the JVM classpath`() {
        val report = AthenaPluginDiscovery().discover()

        assertContains(
            report.candidates.map { it.manifest.pluginId },
            "com.engineeringood.athena.domain.electrical-runtime",
        )
        assertContains(
            report.approvedInventory.approvedPlugins.map { it.candidate.manifest.pluginId },
            "com.engineeringood.athena.domain.electrical-runtime",
        )
    }

    @Test
    fun `rejects malformed and incompatible plugins before activation`() {
        val report = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = FixedAthenaPluginSource(
                listOf(
                    MalformedManifestTestPlugin(),
                    IncompatibleCoreVersionTestPlugin(),
                ),
            ),
        ).discover()

        assertEquals(
            listOf("", "com.engineeringood.athena.domain.future"),
            report.rejectedCandidates.map { it.pluginId.orEmpty() },
        )
        assertEquals(
            listOf(
                listOf(
                    "plugin.manifest.id.blank",
                    "plugin.manifest.version.blank",
                    "plugin.manifest.core-compatibility.minimum.blank",
                ),
                listOf("plugin.activation.core-version.unsupported"),
            ),
            report.rejectedCandidates.map { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
        assertEquals(emptyList(), report.approvedInventory.approvedPlugins)
    }

    @Test
    fun `rejects duplicate plugin identities before activation`() {
        val report = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = FixedAthenaPluginSource(
                listOf(
                    AlphaDomainTestPlugin(),
                    DuplicateAlphaDomainTestPlugin(),
                ),
            ),
        ).discover()

        assertEquals(emptyList(), report.approvedInventory.approvedPlugins)
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha",
                "com.engineeringood.athena.domain.alpha",
            ),
            report.rejectedCandidates.mapNotNull { it.pluginId },
        )
        assertEquals(
            listOf(
                listOf("plugin.activation.identity.duplicate"),
                listOf("plugin.activation.identity.duplicate"),
            ),
            report.rejectedCandidates.map { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
    }

    @Test
    fun `turns plugin source failures into inspectable rejected candidates`() {
        val report = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = ThrowingAthenaPluginSource("broken provider source"),
        ).discover()

        assertEquals(emptyList(), report.candidates)
        assertEquals(emptyList(), report.approvedInventory.approvedPlugins)
        assertEquals(
            listOf("plugin.discovery.source.unreadable"),
            report.rejectedCandidates.single().diagnostics.map { it.ruleId.value },
        )
    }

    @Test
    fun `builds a deterministic approved inventory grouped by extension point`() {
        val report = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = FixedAthenaPluginSource(
                listOf(
                    ZetaDomainTestPlugin(),
                    AlphaDomainTestPlugin(),
                ),
            ),
        ).discover()

        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha",
                "com.engineeringood.athena.domain.zeta",
            ),
            report.candidates.map { it.manifest.pluginId },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha",
                "com.engineeringood.athena.domain.zeta",
            ),
            report.approvedInventory.attachedPlugins(AthenaExtensionPoint.DOMAIN_SEMANTICS)
                .map { it.candidate.manifest.pluginId },
        )
        assertEquals(
            emptyList(),
            report.approvedInventory.attachedPlugins(AthenaExtensionPoint.RENDERING),
        )
    }
}
