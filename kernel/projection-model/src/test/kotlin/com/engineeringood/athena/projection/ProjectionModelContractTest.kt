package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.layout.ViewDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class ProjectionModelContractTest {
    @Test
    fun `projection document can be built without spatial or presentation facts`() {
        val document = ProjectionDocument(
            view = cabinetView(),
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/projection/node/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
            ),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("cabinet/projection/connection/PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:test:plc1_out_to_m1_in"),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("cabinet/geometry/box/component_PLC1", document.nodes.single().originGeometryElementId.value)
        assertEquals(
            "cabinet/geometry/path/connection_PLC1_out_M1_in",
            document.connections.single().originGeometryElementId.value,
        )
        assertEquals(true, document.view.ownershipContract.isInteractive)
        assertEquals(listOf("move-projection-node"), document.view.ownershipContract.projectionCommandIds)
        assertEquals(ElectricalProjectionFamily.CABINET, assertIs<ElectricalProjectionDescriptor>(document.view.familyContract).family)
    }

    @Test
    fun `projection root publishes view sheets occurrences projection groups and reading order only`() {
        val documentProperties = ProjectionDocument::class.java.declaredFields.map { field -> field.name }.toSet()
        val nodeProperties = ProjectionNode::class.java.declaredFields.map { field -> field.name }.toSet()
        val portProperties = ProjectionOccurrencePort::class.java.declaredFields.map { field -> field.name }.toSet()
        val portIdentityProperties = ProjectionOccurrencePortId::class.java.declaredFields.map { field -> field.name }.toSet()
        val endpointProperties = ProjectionConnectionEndpoint::class.java.declaredFields.map { field -> field.name }.toSet()
        val connectionProperties = ProjectionConnection::class.java.declaredFields.map { field -> field.name }.toSet()

        assertEquals(
            setOf("view", "nodes", "connections", "occurrencePorts", "resolvedSubjects", "sheets", "notationPack", "crossReferences"),
            documentProperties,
        )
        assertEquals(setOf("projectionId", "semanticId", "label", "originGeometryElementId"), nodeProperties)
        assertEquals(setOf("occurrencePortId", "originGeometryElementId"), portProperties)
        assertEquals(setOf("occurrenceId", "portId"), portIdentityProperties)
        assertEquals(setOf("occurrencePortId"), endpointProperties)
        assertEquals(
            setOf(
                "projectionId",
                "semanticId",
                "originGeometryElementId",
                "source",
                "target",
            ),
            connectionProperties,
        )
        assertFalse("canvasWidth" in documentProperties)
        assertFalse("canvasHeight" in documentProperties)
        assertFalse("labels" in documentProperties)
        assertFalse("electricalAnchors" in documentProperties)
        assertFalse("electricalConnectionEndpoints" in documentProperties)
        assertFalse("electricalRoutingCorridors" in documentProperties)
        assertFalse("bounds" in nodeProperties)
        assertFalse(portProperties.any { property -> property in setOf("x", "y", "side", "point", "anchorId") })
        assertFalse("start" in connectionProperties)
        assertFalse("end" in connectionProperties)
    }

    @Test
    fun `projection occurrence ports and connection endpoints retain typed engineering identity`() {
        val occurrenceId = ProjectionNodeId("schematic/occurrence/Q1")
        val portId = StableSemanticIdentity("port:Q1.1")
        val occurrencePortId = ProjectionOccurrencePortId(occurrenceId, portId)
        val port = ProjectionOccurrencePort(
            occurrencePortId = occurrencePortId,
            originGeometryElementId = GeometryElementId("projection:port:Q1.1"),
        )
        val endpoint = ProjectionConnectionEndpoint(occurrencePortId)

        assertEquals(occurrenceId, port.occurrencePortId.occurrenceId)
        assertEquals(portId, endpoint.occurrencePortId.portId)
    }

    @Test
    fun `projection documents preserve one canonical subject identity across multiple electrical families`() {
        val componentSemanticId = StableSemanticIdentity("component:PLC1")
        val cabinetDocument = ProjectionDocument(
            view = cabinetView(),
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/projection/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
        )
        val schematicDocument = ProjectionDocument(
            view = ViewDefinition(
                id = "schematic",
                displayName = "Schematic",
                layoutIntent = LayoutIntent.CONNECTIVITY,
                familyContract = ElectricalProjectionDescriptor(
                    family = ElectricalProjectionFamily.SCHEMATIC,
                ),
            ),
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("schematic/projection/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("schematic/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
        )

        assertEquals(componentSemanticId, cabinetDocument.nodes.single().semanticId)
        assertEquals(componentSemanticId, schematicDocument.nodes.single().semanticId)
        assertEquals(
            ElectricalProjectionFamily.CABINET,
            assertIs<ElectricalProjectionDescriptor>(cabinetDocument.view.familyContract).family,
        )
        assertEquals(
            ElectricalProjectionFamily.SCHEMATIC,
            assertIs<ElectricalProjectionDescriptor>(schematicDocument.view.familyContract).family,
        )
    }

    @Test
    fun `projection sheets keep projection owned identity and preserve canonical subject anchors`() {
        val componentSemanticId = StableSemanticIdentity("component:PLC1")
        val overviewSheetId = ProjectionSheetId("documentation/sheet/01-overview")
        val referenceSheetId = ProjectionSheetId("documentation/sheet/02-reference")
        val overviewNodeId = ProjectionNodeId("documentation/projection/node/component_PLC1_overview")
        val referenceNodeId = ProjectionNodeId("documentation/projection/node/component_PLC1_reference")
        val document = ProjectionDocument(
            view = ViewDefinition(
                id = "documentation",
                displayName = "Documentation",
                layoutIntent = LayoutIntent.STRUCTURAL,
                familyContract = ElectricalProjectionDescriptor(
                    family = ElectricalProjectionFamily.DOCUMENTATION,
                ),
            ),
            nodes = listOf(
                ProjectionNode(
                    projectionId = overviewNodeId,
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("documentation/geometry/box/component_PLC1_overview"),
                ),
                ProjectionNode(
                    projectionId = referenceNodeId,
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("documentation/geometry/box/component_PLC1_reference"),
                ),
            ),
            connections = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = overviewSheetId,
                    displayName = "Overview",
                    order = 0,
                    nextSheetId = referenceSheetId,
                    subjects = listOf(
                        ProjectionSheetSubject(
                            semanticId = componentSemanticId,
                            nodeIds = listOf(overviewNodeId),
                        ),
                    ),
                ),
                ProjectionSheet(
                    sheetId = referenceSheetId,
                    displayName = "Reference",
                    order = 1,
                    previousSheetId = overviewSheetId,
                    subjects = listOf(
                        ProjectionSheetSubject(
                            semanticId = componentSemanticId,
                            nodeIds = listOf(referenceNodeId),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("documentation/sheet/01-overview", "documentation/sheet/02-reference"),
            document.sheets.map { sheet -> sheet.sheetId.value },
        )
        assertEquals("documentation/sheet/02-reference", document.sheets.first().nextSheetId?.value)
        assertEquals("documentation/sheet/01-overview", document.sheets.last().previousSheetId?.value)
        assertEquals(componentSemanticId, document.sheets.first().subjects.single().semanticId)
        assertEquals(listOf("documentation/projection/node/component_PLC1_overview"), document.sheets.first().subjects.single().nodeIds.map { nodeId -> nodeId.value })
        assertEquals("A3", document.sheets.first().publication.pageSize.format)
        assertEquals("schematic-sheet", document.sheets.first().composition.representationFamilyId)
    }

    @Test
    fun `projection notation packs keep symbol choices downstream of canonical semantics`() {
        val componentSemanticId = StableSemanticIdentity("component:PLC1")
        val document = ProjectionDocument(
            view = ViewDefinition(
                id = "schematic",
                displayName = "Schematic",
                layoutIntent = LayoutIntent.CONNECTIVITY,
                familyContract = ElectricalProjectionDescriptor(
                    family = ElectricalProjectionFamily.SCHEMATIC,
                ),
            ),
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("schematic/projection/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    originGeometryElementId = GeometryElementId("schematic/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
            notationPack = ProjectionNotationPack(
                packId = ProjectionNotationPackId("electrical-notation/schematic/default"),
                displayName = "Electrical Schematic Default",
                subjects = listOf(
                    ProjectionNotationSubject(
                        semanticId = componentSemanticId,
                        symbolKey = ProjectionSymbolKey("device.schematic.default"),
                        labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                        markerKeys = listOf("canonical-device"),
                    ),
                ),
            ),
        )

        assertEquals("electrical-notation/schematic/default", document.notationPack?.packId?.value)
        assertEquals(componentSemanticId, document.notationPack?.subjects?.single()?.semanticId)
        assertEquals("device.schematic.default", document.notationPack?.subjects?.single()?.symbolKey?.value)
    }
}

private fun cabinetView(): ViewDefinition {
    return ViewDefinition(
        id = "cabinet",
        displayName = "Cabinet",
        layoutIntent = LayoutIntent.STRUCTURAL,
        description = "Structural view",
        ownershipContract = ProjectionOwnershipContract(
            interactivity = ProjectionInteractivity.INTERACTIVE,
            displayScopes = listOf("devices", "ports"),
            projectionCommandIds = listOf("move-projection-node"),
            transientInteractionKinds = listOf("pan", "zoom"),
            persistedProjectionMetadataKeys = listOf("node-position"),
        ),
        familyContract = ElectricalProjectionDescriptor(
            family = ElectricalProjectionFamily.CABINET,
        ),
    )
}
