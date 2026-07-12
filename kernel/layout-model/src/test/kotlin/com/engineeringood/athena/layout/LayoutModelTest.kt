package com.engineeringood.athena.layout

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LayoutModelTest {
    @Test
    fun `view definition can attach governed electrical projection family contracts`() {
        val view = ViewDefinition(
            id = "schematic",
            displayName = "Schematic",
            familyContract = ElectricalProjectionDescriptor(
                family = ElectricalProjectionFamily.SCHEMATIC,
            ),
        )

        val familyContract = assertIs<ElectricalProjectionDescriptor>(view.familyContract)

        assertEquals(ElectricalProjectionFamily.SCHEMATIC, familyContract.family)
        assertEquals(ProjectionIdentityAnchor.CANONICAL_SUBJECT, familyContract.identityAnchor)
        assertEquals(ProjectionSemanticAuthority.CANONICAL_ENGINEERING, familyContract.semanticAuthority)
    }

    @Test
    fun `preserves canonical semantic identity across groups nodes and relationships`() {
        val componentSemanticId = StableSemanticIdentity("component:cabinet/main")
        val portSemanticId = StableSemanticIdentity("port:cabinet/main.power")
        val groupId = LayoutGroupId("cabinet/group/main")
        val componentLayoutId = LayoutNodeId("cabinet/node/main")
        val portLayoutId = LayoutNodeId("cabinet/node/main.power")
        val document = LayoutDocument(
            view = ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
            ),
            groups = listOf(
                LayoutGroup(
                    groupId = groupId,
                    label = "Main Cabinet",
                    kind = "component-group",
                    semanticIds = listOf(componentSemanticId, portSemanticId),
                    memberLayoutIds = listOf(componentLayoutId, portLayoutId),
                ),
            ),
            nodes = listOf(
                LayoutNode(
                    layoutId = componentLayoutId,
                    semanticId = componentSemanticId,
                    label = "Main Cabinet",
                    kind = "component",
                    groupId = groupId,
                    emphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
                ),
                LayoutNode(
                    layoutId = portLayoutId,
                    semanticId = portSemanticId,
                    label = "power",
                    kind = "port",
                    groupId = groupId,
                    order = 1,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = LayoutPlacementRelation.WITHIN,
                        referenceLayoutId = componentLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.PLACEMENT),
                ),
            ),
            relationships = listOf(
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId("cabinet/relationship/main.power"),
                    semanticId = portSemanticId,
                    kind = LayoutRelationshipKind.OWNERSHIP,
                    sourceLayoutId = componentLayoutId,
                    targetLayoutId = portLayoutId,
                    emphasis = listOf(ViewEmphasis.OWNERSHIP),
                ),
            ),
        )

        assertEquals(listOf(componentSemanticId, portSemanticId), document.groups.single().semanticIds)
        assertEquals(componentSemanticId, document.nodes.first().semanticId)
        assertEquals(portSemanticId, document.relationships.single().semanticId)
        assertEquals("cabinet/node/main", document.nodes.first().layoutId.value)
        assertEquals(LayoutPlacementRelation.WITHIN, document.nodes.last().relativePlacement?.relation)
        assertEquals(LayoutRelationshipKind.OWNERSHIP, document.relationships.single().kind)
        assertEquals(ProjectionInteractivity.INSPECT_ONLY, document.view.ownershipContract.interactivity)
        assertEquals(false, document.view.ownershipContract.isInteractive)
    }
}
