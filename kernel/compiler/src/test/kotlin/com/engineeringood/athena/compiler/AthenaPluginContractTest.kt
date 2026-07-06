package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginManifest
import com.engineeringood.athena.compiler.plugin.AthenaPluginOwnershipClaim
import com.engineeringood.athena.compiler.plugin.AthenaRulePlugin
import com.engineeringood.athena.compiler.plugin.AthenaPlugin
import com.engineeringood.athena.compiler.plugin.AthenaPluginType
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidator
import com.engineeringood.athena.compiler.plugin.CoreVersionRange
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaPluginContractTest {
    @Test
    fun `accepts a valid domain plugin manifest through the core-owned validator`() {
        val plugin = object : AthenaDomainPlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.domain.sample",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
            )
        }

        val result = AthenaPluginValidator().validate(plugin)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
    }

    @Test
    fun `rejects blank manifest fields with stable diagnostics`() {
        val manifest = AthenaPluginManifest(
            pluginId = "",
            pluginVersion = "",
            pluginType = AthenaPluginType.DOMAIN,
            coreCompatibility = CoreVersionRange(minimumInclusive = ""),
            requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
        )

        val result = AthenaPluginValidator().validateManifest(manifest)

        assertTrue(!result.isValid)
        assertEquals(
            listOf(
                "plugin.manifest.id.blank",
                "plugin.manifest.version.blank",
                "plugin.manifest.core-compatibility.minimum.blank",
            ),
            result.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                "pluginId",
                "pluginVersion",
                "coreCompatibility.minimumInclusive",
            ),
            result.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `rejects extension points that are illegal for the declared plugin type`() {
        val manifest = AthenaPluginManifest(
            pluginId = "com.engineeringood.athena.renderer.bad",
            pluginVersion = "0.0.1-SNAPSHOT",
            pluginType = AthenaPluginType.RENDERER,
            coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
            requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
        )

        val result = AthenaPluginValidator().validateManifest(manifest)

        assertTrue(!result.isValid)
        assertEquals(
            listOf("plugin.manifest.extension-point.illegal-for-type"),
            result.diagnostics.map { it.ruleId.value },
        )
    }

    @Test
    fun `rejects manifest ownership claims that would make a plugin sovereign`() {
        val plugin = object : AthenaDomainPlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.domain.sovereign",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
                ownershipClaims = setOf(AthenaPluginOwnershipClaim.ENGINEERING_IR),
            )
        }

        val result = AthenaPluginValidator().validate(plugin)

        assertTrue(!result.isValid)
        assertEquals(
            listOf("plugin.manifest.ownership-claim.forbidden"),
            result.diagnostics.map { it.ruleId.value },
        )
    }

    @Test
    fun `rejects plugin objects whose manifest type does not match the typed contract`() {
        val plugin = object : AthenaDomainPlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.domain.mismatch",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.RULE,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.RULE_EVALUATION),
            )
        }

        val result = AthenaPluginValidator().validate(plugin)

        assertTrue(!result.isValid)
        assertEquals(
            listOf("plugin.contract.type.mismatch"),
            result.diagnostics.map { it.ruleId.value },
        )
    }

    @Test
    fun `rejects plugin objects that do not implement exactly one typed plugin contract`() {
        val untypedPlugin = object : AthenaPlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.plugin.untyped",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
            )
        }
        val multiTypedPlugin = object : AthenaDomainPlugin, AthenaRulePlugin {
            override val manifest: AthenaPluginManifest = AthenaPluginManifest(
                pluginId = "com.engineeringood.athena.plugin.hybrid",
                pluginVersion = "0.0.1-SNAPSHOT",
                pluginType = AthenaPluginType.DOMAIN,
                coreCompatibility = CoreVersionRange(minimumInclusive = "0.0.1-SNAPSHOT"),
                requiredExtensionPoints = setOf(AthenaExtensionPoint.DOMAIN_SEMANTICS),
            )
        }

        val untypedResult = AthenaPluginValidator().validate(untypedPlugin)
        val multiTypedResult = AthenaPluginValidator().validate(multiTypedPlugin)

        assertEquals(listOf("plugin.contract.type.invalid"), untypedResult.diagnostics.map { it.ruleId.value })
        assertEquals(listOf("plugin.contract.type.invalid"), multiTypedResult.diagnostics.map { it.ruleId.value })
    }

    @Test
    fun `supports directly instantiating the sample electrical runtime domain plugin`() {
        val plugin = ElectricalRuntimeDomainPlugin()

        val result = AthenaPluginValidator().validate(plugin)

        assertIs<AthenaDomainPlugin>(plugin)
        assertTrue(result.isValid)
        assertEquals(AthenaPluginType.DOMAIN, plugin.manifest.pluginType)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.RUNTIME_COMMANDS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            plugin.manifest.requiredExtensionPoints,
        )
    }
}
