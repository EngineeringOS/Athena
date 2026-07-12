package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ProjectionIdentityAnchor
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.layout.ProjectionSemanticAuthority
import com.engineeringood.athena.layout.ViewEmphasis
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPluginType
import com.engineeringood.athena.plugin.AthenaRenderSurface
import com.engineeringood.athena.plugin.AthenaSemanticReviewEnrichmentContributor
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ElectricalRuntimeDomainPluginTest {
    @Test
    fun `publishes the sample electrical runtime plugin through the domain contract`() {
        val plugin = ElectricalRuntimeDomainPlugin()

        assertIs<AthenaDomainPlugin>(plugin)
        assertIs<AthenaSemanticReviewEnrichmentContributor>(plugin)
        assertEquals(AthenaPluginType.DOMAIN, plugin.manifest.pluginType)
        assertEquals(
            setOf(
                AthenaExtensionPoint.DOMAIN_SEMANTICS,
                AthenaExtensionPoint.VIEW_DEFINITIONS,
                AthenaExtensionPoint.SEMANTIC_REVIEW_ENRICHMENT,
                AthenaExtensionPoint.RUNTIME_COMMANDS,
                AthenaExtensionPoint.RUNTIME_VIEWS,
            ),
            plugin.manifest.requiredExtensionPoints,
        )
        assertEquals(setOf("electrical-runtime"), plugin.domainCapabilities)
    }

    @Test
    fun `publishes the first governed electrical projection family set through typed view definitions`() {
        val plugin = ElectricalRuntimeDomainPlugin()
        val contributor = assertIs<AthenaViewDefinitionContributor>(plugin)

        val viewDefinitions = contributor.viewDefinitions()
        val cabinet = viewDefinitions.first { definition -> definition.id == "cabinet" }
        val wiring = viewDefinitions.first { definition -> definition.id == "wiring" }
        val schematic = viewDefinitions.first { definition -> definition.id == "schematic" }
        val documentation = viewDefinitions.first { definition -> definition.id == "documentation" }
        val cabinetFamily = assertIs<ElectricalProjectionDescriptor>(cabinet.familyContract)
        val wiringFamily = assertIs<ElectricalProjectionDescriptor>(wiring.familyContract)
        val schematicFamily = assertIs<ElectricalProjectionDescriptor>(schematic.familyContract)
        val documentationFamily = assertIs<ElectricalProjectionDescriptor>(documentation.familyContract)

        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            viewDefinitions.map { definition -> definition.id },
        )
        assertEquals(LayoutIntent.STRUCTURAL, cabinet.layoutIntent)
        assertEquals(listOf("group-by-owner", "group-by-component"), cabinet.groupingRules)
        assertEquals(listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT), cabinet.viewEmphasis)
        assertEquals(ProjectionInteractivity.INTERACTIVE, cabinet.ownershipContract.interactivity)
        assertEquals(ElectricalProjectionFamily.CABINET, cabinetFamily.family)
        assertEquals(
            listOf("adjust-layout-placement", "adjust-layout-grouping"),
            cabinet.ownershipContract.projectionCommandIds,
        )
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            cabinet.ownershipContract.transientInteractionKinds,
        )
        assertEquals(
            listOf("layout-placement", "layout-group-membership"),
            cabinet.ownershipContract.persistedProjectionMetadataKeys,
        )
        assertEquals(LayoutIntent.CONNECTIVITY, wiring.layoutIntent)
        assertEquals(listOf("group-by-signal", "group-by-connection-path"), wiring.groupingRules)
        assertEquals(listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW), wiring.viewEmphasis)
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, wiring.ownershipContract.interactivity)
        assertEquals(ElectricalProjectionFamily.WIRING, wiringFamily.family)
        assertEquals(emptyList(), wiring.ownershipContract.projectionCommandIds)
        assertEquals(
            listOf("navigate-view", "inspect-selection", "preview-related-elements"),
            wiring.ownershipContract.transientInteractionKinds,
        )
        assertEquals(emptyList(), wiring.ownershipContract.persistedProjectionMetadataKeys)
        assertEquals(LayoutIntent.CONNECTIVITY, schematic.layoutIntent)
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, schematic.ownershipContract.interactivity)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, schematicFamily.family)
        assertEquals(LayoutIntent.STRUCTURAL, documentation.layoutIntent)
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, documentation.ownershipContract.interactivity)
        assertEquals(ElectricalProjectionFamily.DOCUMENTATION, documentationFamily.family)
        assertEquals(ProjectionIdentityAnchor.CANONICAL_SUBJECT, cabinetFamily.identityAnchor)
        assertEquals(ProjectionSemanticAuthority.CANONICAL_ENGINEERING, cabinetFamily.semanticAuthority)
    }

    @Test
    fun `publishes the narrowed m3 proof vocabulary through the stable schema contract`() {
        val plugin = ElectricalRuntimeDomainPlugin()

        assertEquals(
            listOf("Lamp", "Motor", "Switch"),
            plugin.domainSchema.entities.map { entity -> entity.typeId },
        )
        assertEquals(
            listOf("Wire"),
            plugin.domainSchema.connections.map { connection -> connection.typeId },
        )
        assertEquals(
            setOf("Lamp", "Motor", "Switch"),
            plugin.domainSchema.properties.first { property -> property.name == "type" }.allowedSymbolValues,
        )
    }

    @Test
    fun `publishes graphical surface mappings through extension owned render contributions`() {
        val plugin = ElectricalRuntimeDomainPlugin()

        val cabinetContribution = plugin.renderContributions.first { contribution ->
            contribution.contributionId == "electrical-runtime.render.cabinet"
        }
        val wiringContribution = plugin.renderContributions.first { contribution ->
            contribution.contributionId == "electrical-runtime.render.wiring"
        }

        assertEquals(setOf("svg", "graph-workbench"), cabinetContribution.rendererTargets)
        assertEquals(
            listOf(AthenaRenderSurface.CANVAS, AthenaRenderSurface.NODE, AthenaRenderSurface.EDGE),
            cabinetContribution.surfaceMappings.map { mapping -> mapping.surface },
        )
        assertEquals("rgba(22, 18, 12, 0.92)", cabinetContribution.surfaceMappings.first().tokens["canvasTint"])
        assertEquals(setOf("svg", "graph-workbench"), wiringContribution.rendererTargets)
        assertEquals(
            listOf(AthenaRenderSurface.CANVAS, AthenaRenderSurface.NODE, AthenaRenderSurface.EDGE),
            wiringContribution.surfaceMappings.map { mapping -> mapping.surface },
        )
        assertEquals("rgba(96, 223, 255, 0.94)", wiringContribution.surfaceMappings.last().tokens["stroke"])
    }
}
