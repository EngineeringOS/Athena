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
import kotlin.test.assertIs

class ProjectionModelContractTest {
    @Test
    fun `projection document keeps layout owned view definition and geometry origin references`() {
        val document = ProjectionDocument(
            view = ViewDefinition(
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
            ),
            canvasWidth = 480,
            canvasHeight = 172,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/node/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 40, y = 60, width = 140, height = 72),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
            ),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("cabinet/connection/PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    start = ProjectionPoint(x = 104, y = 86),
                    end = ProjectionPoint(x = 316, y = 86),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                ),
            ),
            labels = listOf(
                ProjectionLabel(
                    projectionId = ProjectionLabelId("cabinet/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    bounds = ProjectionBounds(x = 56, y = 78, width = 48, height = 16),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/label/port_PLC1_out"),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("cabinet/geometry/box/component_PLC1", document.nodes.single().originGeometryElementId.value)
        assertEquals(
            "cabinet/geometry/path/connection_PLC1_out_M1_in",
            document.connections.single().originGeometryElementId.value,
        )
        assertEquals("cabinet/geometry/label/port_PLC1_out", document.labels.single().originGeometryElementId.value)
        assertEquals(true, document.view.ownershipContract.isInteractive)
        assertEquals(listOf("move-projection-node"), document.view.ownershipContract.projectionCommandIds)
        assertEquals(ElectricalProjectionFamily.CABINET, assertIs<ElectricalProjectionDescriptor>(document.view.familyContract).family)
    }

    @Test
    fun `projection documents preserve one canonical subject identity across multiple electrical families`() {
        val componentSemanticId = StableSemanticIdentity("component:PLC1")
        val cabinetDocument = ProjectionDocument(
            view = ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
                layoutIntent = LayoutIntent.STRUCTURAL,
                familyContract = ElectricalProjectionDescriptor(
                    family = ElectricalProjectionFamily.CABINET,
                ),
            ),
            canvasWidth = 320,
            canvasHeight = 180,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 24, y = 40, width = 120, height = 56),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
            labels = emptyList(),
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
            canvasWidth = 360,
            canvasHeight = 200,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("schematic/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 32, y = 52, width = 100, height = 40),
                    originGeometryElementId = GeometryElementId("schematic/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
            labels = emptyList(),
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
        val overviewNodeId = ProjectionNodeId("documentation/node/component_PLC1_overview")
        val referenceNodeId = ProjectionNodeId("documentation/node/component_PLC1_reference")

        val document = ProjectionDocument(
            view = ViewDefinition(
                id = "documentation",
                displayName = "Documentation",
                layoutIntent = LayoutIntent.STRUCTURAL,
                familyContract = ElectricalProjectionDescriptor(
                    family = ElectricalProjectionFamily.DOCUMENTATION,
                ),
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            nodes = listOf(
                ProjectionNode(
                    projectionId = overviewNodeId,
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 32, y = 40, width = 120, height = 48),
                    originGeometryElementId = GeometryElementId("documentation/geometry/box/component_PLC1_overview"),
                ),
                ProjectionNode(
                    projectionId = referenceNodeId,
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 240, y = 56, width = 120, height = 48),
                    originGeometryElementId = GeometryElementId("documentation/geometry/box/component_PLC1_reference"),
                ),
            ),
            connections = emptyList(),
            labels = emptyList(),
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
        assertEquals(
            componentSemanticId,
            document.sheets.first().subjects.single().semanticId,
        )
        assertEquals(
            componentSemanticId,
            document.sheets.last().subjects.single().semanticId,
        )
        assertEquals(
            listOf("documentation/node/component_PLC1_overview"),
            document.sheets.first().subjects.single().nodeIds.map { nodeId -> nodeId.value },
        )
        assertEquals(
            listOf("documentation/node/component_PLC1_reference"),
            document.sheets.last().subjects.single().nodeIds.map { nodeId -> nodeId.value },
        )
    }

    @Test
    fun `projection notation packs keep symbol and label choices downstream of canonical semantics`() {
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
            canvasWidth = 640,
            canvasHeight = 320,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("schematic/node/component_PLC1"),
                    semanticId = componentSemanticId,
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 40, y = 60, width = 120, height = 48),
                    originGeometryElementId = GeometryElementId("schematic/geometry/box/component_PLC1"),
                ),
            ),
            connections = emptyList(),
            labels = emptyList(),
            notationPack = ProjectionNotationPack(
                packId = ProjectionNotationPackId("electrical-notation/schematic/default-v1"),
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

        assertEquals("electrical-notation/schematic/default-v1", document.notationPack?.packId?.value)
        assertEquals(componentSemanticId, document.notationPack?.subjects?.single()?.semanticId)
        assertEquals("device.schematic.default", document.notationPack?.subjects?.single()?.symbolKey?.value)
        assertEquals(ProjectionLabelPolicy.SUBJECT_LABEL, document.notationPack?.subjects?.single()?.labelPolicy)
        assertEquals(listOf("canonical-device"), document.notationPack?.subjects?.single()?.markerKeys)
    }
}
