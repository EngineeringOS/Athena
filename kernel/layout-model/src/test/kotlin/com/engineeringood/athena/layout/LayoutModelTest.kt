package com.engineeringood.athena.layout

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class LayoutModelTest {
    @Test
    fun `layout intent snapshot carries explainable schematic intent and canonical identity`() {
        val controllerIntentId = LayoutIntentId("intent:controller/plc-1")
        val terminalIntentId = LayoutIntentId("intent:terminal/xt-1")
        val controllerSourceSpan = LayoutSourceSpan(
            sourceUnitId = "src/02-layout-intelligence-acceptance.athena",
            startLine = 12,
            startColumn = 5,
            endLine = 18,
            endColumn = 6,
        )
        val controllerIntent = LayoutIntentItem(
            intentId = controllerIntentId,
            subjectId = StableSemanticIdentity("component:plc-1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:plc-1"),
            role = SchematicLayoutRole.CONTROLLER,
            preferredZone = SchematicLayoutZone.CONTROL,
            priority = LayoutPriority.HIGH,
            alignment = LayoutAlignment.TOP_TO_BOTTOM,
            relationshipConstraints = listOf(
                LayoutIntentRelationshipConstraint(
                    relationship = SchematicLayoutRelationship.NEAR,
                    targetIntentId = terminalIntentId,
                    axis = LayoutAxis.VERTICAL,
                ),
            ),
            sourceSpan = controllerSourceSpan,
        )

        val snapshot = LayoutIntentSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m21:schematic:intent"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = listOf(
                LayoutIntentItem(
                    intentId = terminalIntentId,
                    subjectId = StableSemanticIdentity("component:xt-1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:schematic:xt-1"),
                    role = SchematicLayoutRole.TERMINAL,
                    preferredZone = SchematicLayoutZone.TERMINAL,
                    priority = LayoutPriority.NORMAL,
                    alignment = LayoutAlignment.COLUMN,
                ),
                controllerIntent,
            ),
        )

        assertEquals(LayoutSnapshotId("snapshot:m21:schematic:intent"), snapshot.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, snapshot.family)
        assertEquals(listOf(controllerIntentId, terminalIntentId), snapshot.items.map(LayoutIntentItem::intentId))
        assertEquals(StableSemanticIdentity("component:plc-1"), snapshot.items.first().subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:plc-1"), snapshot.items.first().occurrenceId)
        assertEquals(SchematicLayoutRole.CONTROLLER, snapshot.items.first().role)
        assertEquals(SchematicLayoutZone.CONTROL, snapshot.items.first().preferredZone)
        assertEquals(LayoutPriority.HIGH, snapshot.items.first().priority)
        assertEquals(LayoutAlignment.TOP_TO_BOTTOM, snapshot.items.first().alignment)
        assertEquals(SchematicLayoutRelationship.NEAR, snapshot.items.first().relationshipConstraints.single().relationship)
        assertEquals(controllerSourceSpan, snapshot.items.first().sourceSpan)
    }

    @Test
    fun `layout intent snapshot contract stays renderer independent and deterministic`() {
        val powerIntent = LayoutIntentItem(
            intentId = LayoutIntentId("intent:power/ps-1"),
            subjectId = StableSemanticIdentity("component:ps-1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:ps-1"),
            role = SchematicLayoutRole.POWER_SOURCE,
            preferredZone = SchematicLayoutZone.POWER,
            priority = LayoutPriority.CRITICAL,
            alignment = LayoutAlignment.LEFT_TO_RIGHT,
        )
        val protectionIntent = LayoutIntentItem(
            intentId = LayoutIntentId("intent:protection/qf-1"),
            subjectId = StableSemanticIdentity("component:qf-1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:qf-1"),
            role = SchematicLayoutRole.PROTECTION,
            preferredZone = SchematicLayoutZone.POWER,
            priority = LayoutPriority.HIGH,
        )
        val relation = LayoutIntentRelationshipConstraint(
            relationship = SchematicLayoutRelationship.BEFORE,
            targetIntentId = protectionIntent.intentId,
            axis = LayoutAxis.HORIZONTAL,
        )

        val first = LayoutIntentSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m21:deterministic"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = listOf(protectionIntent, powerIntent),
            relationshipConstraints = listOf(relation),
        )
        val second = LayoutIntentSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m21:deterministic"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = listOf(protectionIntent, powerIntent),
            relationshipConstraints = listOf(relation),
        )
        val rendererStateNames = setOf("css", "dom", "canvas", "pixel", "route", "adapter", "engine")
        val snapshotFieldNames = LayoutIntentSnapshot::class.java.declaredFields.map { field -> field.name.lowercase() }
        val itemFieldNames = LayoutIntentItem::class.java.declaredFields.map { field -> field.name.lowercase() }

        assertEquals(first, second)
        assertEquals(listOf(powerIntent.intentId, protectionIntent.intentId), first.items.map(LayoutIntentItem::intentId))
        assertEquals(listOf(relation), first.relationshipConstraints)
        rendererStateNames.forEach { forbidden ->
            assertFalse(snapshotFieldNames.any { field -> field.contains(forbidden) }, "Snapshot must not contain renderer field `$forbidden`.")
            assertFalse(itemFieldNames.any { field -> field.contains(forbidden) }, "Intent item must not contain renderer field `$forbidden`.")
        }
    }

    @Test
    fun `layout source span rejects inverted same-line ranges`() {
        assertFailsWith<IllegalArgumentException> {
            LayoutSourceSpan(
                sourceUnitId = "src/02-layout-intelligence-acceptance.athena",
                startLine = 10,
                startColumn = 20,
                endLine = 10,
                endColumn = 12,
            )
        }
    }

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
