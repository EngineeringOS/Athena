package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AthenaPluginDiscoveryTest {
    @Test
    fun `discovers the default dummy and electrical runtime plugins from the JVM classpath in deterministic order`() {
        val report = AthenaPluginDiscovery().discover()

        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.dummy-runtime",
                "com.engineeringood.athena.domain.electrical-runtime",
            ),
            report.candidates.map { it.manifest.pluginId },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.dummy-runtime",
                "com.engineeringood.athena.domain.electrical-runtime",
            ),
            report.approvedInventory.approvedPlugins.map { it.candidate.manifest.pluginId },
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

    @Test
    fun `rejects plugins that claim core ownership before runtime hosting`() {
        val report = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = FixedAthenaPluginSource(listOf(SovereignOwnershipClaimTestPlugin())),
        ).discover()

        assertEquals(emptyList(), report.approvedInventory.approvedPlugins)
        assertEquals(
            listOf(
                "plugin.manifest.ownership-claim.forbidden",
                "plugin.manifest.ownership-claim.forbidden",
            ),
            report.rejectedCandidates.single().diagnostics.map { it.ruleId.value },
        )
    }
}
