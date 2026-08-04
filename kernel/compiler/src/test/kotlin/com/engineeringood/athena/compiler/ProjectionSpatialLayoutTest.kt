package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionConstructId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionRegion
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetConstruct
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectionSpatialLayoutTest {
    @Test
    fun `spatial layout distributes three regions across the drawing area`() {
        val result = ProjectionSpatialLayout().place(projectionDocument())

        assertEquals(SpatialRect(x = 0, y = 0, width = 1200, height = 800), ProjectionSpatialLayout.SHEET_EXTENT)
        assertEquals(SpatialRect(x = 40, y = 60, width = 1120, height = 640), ProjectionSpatialLayout.DRAWING_AREA)
        assertEquals(740, ProjectionSpatialLayout.TITLE_BLOCK_START_Y)
        assertTrue(result.diagnostics.isEmpty())
        assertEquals(3, result.occurrences.size)
        val rectangles = result.occurrences.map { occurrence -> occurrence.rectangle }

        assertTrue(rectangles.all { rectangle ->
            rectangle.isInside(ProjectionSpatialLayout.DRAWING_AREA)
        })

        val orderedRectangles = rectangles.sortedBy { rectangle -> rectangle.x }
        assertTrue(orderedRectangles.zipWithNext().all { (left, right) ->
            right.x - left.right >= 32
        })

        val occupiedWidth = rectangles.maxOf { rectangle -> rectangle.right } - rectangles.minOf { rectangle -> rectangle.x }
        val occupiedHeight = rectangles.maxOf { rectangle -> rectangle.bottom } - rectangles.minOf { rectangle -> rectangle.y }
        assertTrue(occupiedWidth >= 616, "occupied width must cover at least 55% of the Drawing Area")
        assertTrue(occupiedHeight >= 288, "occupied height must cover at least 45% of the Drawing Area")
        assertTrue(result.occurrences.all { occurrence ->
            occurrence.placementReason.text.contains("region", ignoreCase = true) &&
                occurrence.placementReason.text.contains("Drawing Area", ignoreCase = true)
        })
    }

    @Test
    fun `canonical rolling shutter placement covers eight occurrences and three regions`() {
        val nodes = listOf("Supply", "F1", "Q1", "S1Up", "S1Down", "K1", "K2", "M1")
            .map(::namedNode)
        val regions = listOf(
            ProjectionRegion(
                regionId = "rolling-shutter/main/power",
                name = "Power",
                occurrenceNames = listOf("Supply", "F1", "Q1"),
            ),
            ProjectionRegion(
                regionId = "rolling-shutter/main/control",
                name = "Control",
                occurrenceNames = listOf("S1Up", "S1Down", "K1", "K2"),
            ),
            ProjectionRegion(
                regionId = "rolling-shutter/main/load",
                name = "Load",
                occurrenceNames = listOf("M1"),
            ),
        )

        val projection = projectionDocument(nodes = nodes, regions = regions)
        val layout = ProjectionSpatialLayout()
        val result = layout.place(projection)
        val repeated = layout.place(projection)
        val permuted = layout.place(
            projection.copy(
                nodes = projection.nodes.reversed(),
                connections = projection.connections.reversed(),
                sheets = projection.sheets.reversed().map { sheet ->
                    sheet.copy(subjects = sheet.subjects.reversed())
                },
            ),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(result, repeated)
        assertEquals(result, permuted)
        assertEquals(
            listOf(
                expectedOccurrence("Supply", regions[0], x = 176, y = 84, regionOrder = 1, memberOrder = 1, separated = true),
                expectedOccurrence("F1", regions[0], x = 176, y = 360, regionOrder = 1, memberOrder = 2, separated = true),
                expectedOccurrence("Q1", regions[0], x = 176, y = 636, regionOrder = 1, memberOrder = 3, separated = true),
                expectedOccurrence("S1Up", regions[1], x = 560, y = 84, regionOrder = 2, memberOrder = 1, separated = true),
                expectedOccurrence("S1Down", regions[1], x = 560, y = 268, regionOrder = 2, memberOrder = 2, separated = true),
                expectedOccurrence("K1", regions[1], x = 560, y = 452, regionOrder = 2, memberOrder = 3, separated = true),
                expectedOccurrence("K2", regions[1], x = 560, y = 636, regionOrder = 2, memberOrder = 4, separated = true),
                expectedOccurrence("M1", regions[2], x = 944, y = 636, regionOrder = 3, memberOrder = 1, separated = false),
            ),
            result.occurrences,
        )
        assertTrue(result.occurrences.all { occurrence ->
            occurrence.rectangle.isInside(ProjectionSpatialLayout.DRAWING_AREA) &&
                occurrence.rectangle.bottom < ProjectionSpatialLayout.TITLE_BLOCK_START_Y &&
                setOf(
                    occurrence.sheetId,
                    occurrence.regionId,
                    occurrence.occurrenceId.projectionId,
                ).all(occurrence.sourceTrace.projectionIds::contains)
        })
        val regionRanges = result.occurrences.groupBy { occurrence -> occurrence.regionId }
            .values
            .map { occurrences ->
                occurrences.minOf { occurrence -> occurrence.rectangle.x } to
                    occurrences.maxOf { occurrence -> occurrence.rectangle.right }
            }
            .sortedBy { (left, _) -> left }
        assertTrue(regionRanges.zipWithNext().all { (left, right) -> right.first - left.second >= 32 })
        assertTrue(result.occurrences.groupBy { occurrence -> occurrence.regionId }.values.all { occurrences ->
            occurrences.sortedBy { occurrence -> occurrence.rectangle.y }.zipWithNext().all { (above, below) ->
                below.rectangle.y - above.rectangle.bottom >= ProjectionSpatialLayout.OCCURRENCE_SEPARATION
            }
        })
        val occupiedWidth = result.occurrences.maxOf { occurrence -> occurrence.rectangle.right } -
            result.occurrences.minOf { occurrence -> occurrence.rectangle.x }
        val occupiedHeight = result.occurrences.maxOf { occurrence -> occurrence.rectangle.bottom } -
            result.occurrences.minOf { occurrence -> occurrence.rectangle.y }
        assertTrue(occupiedWidth >= ProjectionSpatialLayout.DRAWING_AREA.width * 55 / 100)
        assertTrue(occupiedHeight >= ProjectionSpatialLayout.DRAWING_AREA.height * 45 / 100)
    }

    @Test
    fun `spatial placement facts are canonical when projection nodes are unordered`() {
        val canonical = ProjectionSpatialLayout().place(projectionDocument())
        val permuted = ProjectionSpatialLayout().place(
            projectionDocument(nodes = listOf(loadNode(), breakerNode(), supplyNode())),
        )

        assertEquals(canonical, permuted)
    }

    @Test
    fun `spatial placement uses construct topology authored and stable ordering constraints`() {
        val constructFirst = namedNode("ConstructFirst")
        val constructSecond = namedNode("ConstructSecond")
        val source = namedNode("Source")
        val target = namedNode("Target")
        val authoredFirst = namedNode("AuthoredZ")
        val authoredSecond = namedNode("AuthoredA")
        val nodes = listOf(authoredSecond, target, constructSecond, authoredFirst, source, constructFirst)
        val region = ProjectionRegion(
            regionId = "engineering-projection/01-main/ordered",
            name = "Ordered",
            occurrenceNames = listOf(
                authoredFirst.label,
                target.label,
                source.label,
                constructSecond.label,
                constructFirst.label,
                authoredSecond.label,
            ),
        )
        val connection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/source-target"),
            semanticId = StableSemanticIdentity("connection:source-target"),
            originGeometryElementId = GeometryElementId("origin:connection:source-target"),
            source = ProjectionConnectionEndpoint(
                ProjectionOccurrencePortId(source.projectionId, StableSemanticIdentity("port:Source.out")),
            ),
            target = ProjectionConnectionEndpoint(
                ProjectionOccurrencePortId(target.projectionId, StableSemanticIdentity("port:Target.in")),
            ),
        )
        val projection = projectionDocument(nodes = nodes, regions = listOf(region)).let { document ->
            val sheet = document.sheets.single().copy(
                constructs = listOf(
                    ProjectionSheetConstruct(
                        constructId = ProjectionConstructId("construct:ordered"),
                        kind = "sequence",
                        name = "Train",
                        memberNames = listOf(constructFirst.label, constructSecond.label),
                    ),
                ),
            )
            document.copy(connections = listOf(connection), sheets = listOf(sheet))
        }

        val result = ProjectionSpatialLayout().place(projection)

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                constructFirst.projectionId.value,
                constructSecond.projectionId.value,
                source.projectionId.value,
                target.projectionId.value,
                authoredFirst.projectionId.value,
                authoredSecond.projectionId.value,
            ),
            result.occurrences.sortedBy { occurrence -> occurrence.rectangle.y }
                .map { occurrence -> occurrence.occurrenceId.projectionId },
        )
        val reasonsById = result.occurrences.associate { occurrence ->
            occurrence.occurrenceId.projectionId to occurrence.placementReason.text
        }
        assertTrue(reasonsById.getValue(constructFirst.projectionId.value).contains("Construct Train member order 1"))
        assertTrue(reasonsById.getValue(source.projectionId.value).contains("Connection topology order"))
        assertTrue(reasonsById.getValue(authoredFirst.projectionId.value).contains("authored Region member order 1"))
        assertTrue(reasonsById.values.all { reason -> reason.contains("stable Projection identity fallback") })
    }

    @Test
    fun `spatial placement publishes typed owned geometry with source trace`() {
        val result = ProjectionSpatialLayout().place(projectionDocument())

        assertEquals(3, result.occurrences.size)
        assertEquals(
            listOf(
                "engineering-projection/01-main/supply",
                "engineering-projection/01-main/control",
                "engineering-projection/01-main/load",
            ),
            result.occurrences.map { occurrence -> occurrence.regionId },
        )
        assertTrue(result.occurrences.all { occurrence ->
            occurrence.sheetId == "engineering-projection/sheet/01-main" &&
                occurrence.rectangle.width > 0 &&
                occurrence.rectangle.height > 0 &&
                occurrence.placementReason.constraints.contains("Drawing Area (40,60,1120,640)") &&
                occurrence.sourceTrace.projectionIds.contains(occurrence.occurrenceId.projectionId) &&
                occurrence.sourceTrace.geometryElementIds.size == 1
        })
    }

    @Test
    fun `spatial occurrence identity keeps owning sheet and projection identity as typed components`() {
        val occurrence = ProjectionSpatialLayout().place(projectionDocument()).occurrences.first()

        assertEquals("SpatialOccurrenceId", occurrence.occurrenceId::class.java.simpleName)
        assertEquals(
            setOf("sheetId", "projectionId"),
            occurrence.occurrenceId::class.java.declaredFields
                .filterNot { field -> field.isSynthetic }
                .map { field -> field.name }
                .toSet(),
        )
    }

    @Test
    fun `spatial placement keeps unassigned occurrences in one explicit final region`() {
        val result = ProjectionSpatialLayout().place(
            projectionDocument(nodes = listOf(auxNode(), loadNode(), breakerNode(), supplyNode())),
        )

        assertEquals(4, result.occurrences.size)
        val unassigned = result.occurrences.last()
        assertEquals("projection/node/component:Aux", unassigned.occurrenceId.projectionId)
        assertEquals("engineering-projection/sheet/01-main/region/unassigned", unassigned.regionId)
        assertTrue(unassigned.placementReason.text.contains("explicit final Unassigned Region"))
        assertFalse(unassigned.placementReason.text.contains("authored order"))
    }

    @Test
    fun `spatial placement rejects duplicate region membership with actionable trace`() {
        val regions = projectionRegions() + ProjectionRegion(
            regionId = "engineering-projection/01-main/duplicate-supply",
            name = "Duplicate Supply",
            occurrenceNames = listOf("Supply"),
        )

        val result = ProjectionSpatialLayout().place(projectionDocument(regions = regions))

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Occurrence Supply", diagnostic.subject)
        assertEquals("is assigned to more than one Region on Sheet engineering-projection/sheet/01-main", diagnostic.problem)
        assertEquals("List Supply in exactly one Region on that Sheet.", diagnostic.correction)
        assertTrue(diagnostic.sourceTrace.projectionIds.contains("projection/node/component:Supply"))
        assertEquals(listOf(GeometryElementId("origin:Supply")), diagnostic.sourceTrace.geometryElementIds)
    }

    @Test
    fun `spatial placement distinguishes duplicate entries inside one region`() {
        val duplicateRegion = projectionRegions().first().copy(
            occurrenceNames = listOf("Supply", "Supply"),
        )

        val result = ProjectionSpatialLayout().place(
            projectionDocument(regions = listOf(duplicateRegion) + projectionRegions().drop(1)),
        )

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Occurrence Supply", diagnostic.subject)
        assertEquals(
            "is listed more than once in Region Supply on Sheet engineering-projection/sheet/01-main",
            diagnostic.problem,
        )
        assertEquals("List Supply once in Region Supply.", diagnostic.correction)
    }

    @Test
    fun `spatial placement rejects ambiguous duplicate labels before region resolution`() {
        val first = namedNode("Shared").copy(
            projectionId = ProjectionNodeId("projection/node/shared/first"),
            semanticId = StableSemanticIdentity("component:SharedFirst"),
            originGeometryElementId = GeometryElementId("origin:SharedFirst"),
        )
        val second = namedNode("Shared").copy(
            projectionId = ProjectionNodeId("projection/node/shared/second"),
            semanticId = StableSemanticIdentity("component:SharedSecond"),
            originGeometryElementId = GeometryElementId("origin:SharedSecond"),
        )
        val region = ProjectionRegion(
            regionId = "engineering-projection/01-main/shared",
            name = "Shared Region",
            occurrenceNames = listOf("Shared"),
        )

        val result = ProjectionSpatialLayout().place(
            projectionDocument(nodes = listOf(second, first), regions = listOf(region)),
        )

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Region Shared Region member Shared", diagnostic.subject)
        assertEquals("matches 2 projected Occurrences", diagnostic.problem)
        assertEquals("Give each projected Occurrence a unique label before assigning it to a Region.", diagnostic.correction)
        assertEquals(
            listOf("projection/node/shared/first", "projection/node/shared/second"),
            diagnostic.sourceTrace.projectionIds.filter { id -> id.startsWith("projection/node/") },
        )
    }

    @Test
    fun `spatial placement rejects unknown and cross sheet region members`() {
        val first = namedNode("First")
        val second = namedNode("Second")
        val projection = projectionDocument(
            nodes = listOf(first, second),
            regions = listOf(
                ProjectionRegion(
                    regionId = "engineering-projection/01-main/main",
                    name = "Main",
                    occurrenceNames = listOf("First", "Second", "Missing"),
                ),
            ),
        )
        val firstSheet = projection.sheets.single().copy(
            subjects = listOf(ProjectionSheetSubject(first.semanticId, nodeIds = listOf(first.projectionId))),
        )
        val secondSheetId = ProjectionSheetId("engineering-projection/sheet/02-secondary")
        val secondSubjects = listOf(ProjectionSheetSubject(second.semanticId, nodeIds = listOf(second.projectionId)))
        val secondSheet = ProjectionSheet(
            sheetId = secondSheetId,
            displayName = "Secondary",
            order = 1,
            subjects = secondSubjects,
            regions = listOf(
                ProjectionRegion(
                    regionId = "engineering-projection/02-secondary/secondary",
                    name = "Secondary",
                    occurrenceNames = listOf("Second"),
                ),
            ),
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = secondSheetId,
                displayName = "Secondary",
                order = 1,
                subjects = secondSubjects,
            ),
        )

        val result = ProjectionSpatialLayout().place(
            projection.copy(sheets = listOf(firstSheet, secondSheet)),
        )

        assertTrue(result.occurrences.isEmpty())
        assertEquals(2, result.diagnostics.size)
        val diagnosticsBySubject = result.diagnostics.associateBy { diagnostic -> diagnostic.subject }
        assertEquals(
            "belongs to Sheet engineering-projection/sheet/02-secondary, not Sheet engineering-projection/sheet/01-main",
            diagnosticsBySubject.getValue("Region Main member Second").problem,
        )
        assertEquals(
            "does not resolve to a projected Occurrence",
            diagnosticsBySubject.getValue("Region Main member Missing").problem,
        )
        assertTrue(result.diagnostics.all { diagnostic -> diagnostic.correction.isNotBlank() })
    }

    @Test
    fun `spatial placement rejects a region that cannot keep minimum vertical separation`() {
        val nodes = (1..8).map { index -> namedNode("N$index") }
        val denseRegion = ProjectionRegion(
            regionId = "engineering-projection/01-main/dense",
            name = "Dense",
            occurrenceNames = nodes.map { node -> node.label },
        )

        val result = ProjectionSpatialLayout().place(
            projectionDocument(nodes = nodes, regions = listOf(denseRegion)),
        )

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Region Dense", diagnostic.subject)
        assertEquals(
            "cannot place 8 Occurrences with 48-unit vertical separation inside the Drawing Area",
            diagnostic.problem,
        )
        assertEquals("Split Region Dense or reduce its Occurrence count.", diagnostic.correction)
        assertEquals(8, diagnostic.sourceTrace.geometryElementIds.size)
    }

    @Test
    fun `spatial placement keeps minimum vertical separation at region capacity`() {
        val nodes = (1..7).map { index -> namedNode("N$index") }
        val region = ProjectionRegion(
            regionId = "engineering-projection/01-main/capacity",
            name = "Capacity",
            occurrenceNames = nodes.map { node -> node.label },
        )

        val result = ProjectionSpatialLayout().place(
            projectionDocument(nodes = nodes, regions = listOf(region)),
        )

        assertTrue(result.diagnostics.isEmpty())
        val rectangles = result.occurrences.map { occurrence -> occurrence.rectangle }.sortedBy { rectangle -> rectangle.y }
        assertTrue(rectangles.all { rectangle -> rectangle.isInside(ProjectionSpatialLayout.DRAWING_AREA) })
        assertTrue(rectangles.zipWithNext().all { (above, below) ->
            below.y - above.bottom >= ProjectionSpatialLayout.OCCURRENCE_SEPARATION
        })
    }

    @Test
    fun `spatial placement rejects an occurrence omitted from the only sheet`() {
        val projection = projectionDocument(nodes = listOf(supplyNode(), auxNode())).let { document ->
            document.copy(
                sheets = listOf(
                    document.sheets.single().copy(
                        subjects = document.sheets.single().subjects.filter { subject ->
                            subject.nodeIds == listOf(supplyNode().projectionId)
                        },
                        regions = listOf(projectionRegions().first()),
                    ),
                ),
            )
        }

        val result = ProjectionSpatialLayout().place(projection)

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Occurrence Aux", diagnostic.subject)
        assertEquals("resolves to 0 owning Sheets", diagnostic.problem)
        assertEquals("Reference Aux from at least one Sheet subject list.", diagnostic.correction)
    }

    @Test
    fun `spatial placement sheet qualifies one repeated Projection occurrence across Sheets`() {
        val projection = projectionDocument()
        val duplicateSheetId = ProjectionSheetId("engineering-projection/sheet/02-duplicate")
        val duplicateSheet = projection.sheets.single().copy(
            sheetId = duplicateSheetId,
            displayName = "Duplicate",
            order = 1,
            subjects = listOf(ProjectionSheetSubject(supplyNode().semanticId)),
            regions = emptyList(),
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = duplicateSheetId,
                displayName = "Duplicate",
                order = 1,
                subjects = listOf(ProjectionSheetSubject(supplyNode().semanticId)),
            ),
        )

        val result = ProjectionSpatialLayout().place(
            projection.copy(sheets = projection.sheets + duplicateSheet),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            setOf(
                SpatialOccurrenceId(projection.sheets.single().sheetId.value, supplyNode().projectionId.value),
                SpatialOccurrenceId(duplicateSheetId.value, supplyNode().projectionId.value),
            ),
            result.occurrences
                .filter { occurrence -> occurrence.occurrenceId.projectionId == supplyNode().projectionId.value }
                .map { occurrence -> occurrence.occurrenceId }
                .toSet(),
        )
    }

    @Test
    fun `explicit node ids keep semantic sibling occurrences on separate sheets`() {
        val semanticId = StableSemanticIdentity("component:SharedSubject")
        val first = namedNode("FirstView").copy(semanticId = semanticId)
        val second = namedNode("SecondView").copy(semanticId = semanticId)
        val projection = projectionDocument(nodes = listOf(first, second), regions = emptyList())
        val firstSheetId = ProjectionSheetId("engineering-projection/sheet/01-first")
        val secondSheetId = ProjectionSheetId("engineering-projection/sheet/02-second")
        val firstSubjects = listOf(ProjectionSheetSubject(semanticId, nodeIds = listOf(first.projectionId)))
        val secondSubjects = listOf(ProjectionSheetSubject(semanticId, nodeIds = listOf(second.projectionId)))
        val firstSheet = ProjectionSheet(
            sheetId = firstSheetId,
            displayName = "First",
            order = 0,
            subjects = firstSubjects,
            regions = listOf(
                ProjectionRegion("engineering-projection/first", "First", listOf(first.label)),
            ),
            publication = ProjectionSheetPublication.fromProjectionState(firstSheetId, "First", 0, firstSubjects),
        )
        val secondSheet = ProjectionSheet(
            sheetId = secondSheetId,
            displayName = "Second",
            order = 1,
            subjects = secondSubjects,
            regions = listOf(
                ProjectionRegion("engineering-projection/second", "Second", listOf(second.label)),
            ),
            publication = ProjectionSheetPublication.fromProjectionState(secondSheetId, "Second", 1, secondSubjects),
        )

        val result = ProjectionSpatialLayout().place(
            projection.copy(sheets = listOf(firstSheet, secondSheet)),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            mapOf(
                first.projectionId.value to firstSheetId.value,
                second.projectionId.value to secondSheetId.value,
            ),
            result.occurrences.associate { occurrence ->
                occurrence.occurrenceId.projectionId to occurrence.sheetId
            },
        )
    }

    @Test
    fun `spatial placement rejects duplicate projection node identities`() {
        val duplicateId = ProjectionNodeId("projection/node/duplicate")
        val first = namedNode("First").copy(projectionId = duplicateId)
        val second = namedNode("Second").copy(projectionId = duplicateId)

        val result = ProjectionSpatialLayout().place(
            projectionDocument(nodes = listOf(second, first), regions = emptyList()),
        )

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Projection occurrence identity ${duplicateId.value}", diagnostic.subject)
        assertEquals("is used by 2 projected Occurrences", diagnostic.problem)
        assertEquals("Give every projected Occurrence a unique Projection identity.", diagnostic.correction)
        assertEquals(
            listOf(GeometryElementId("origin:First"), GeometryElementId("origin:Second")),
            diagnostic.sourceTrace.geometryElementIds,
        )
    }

    @Test
    fun `spatial placement rejects invalid effective region identities with structured diagnostics`() {
        val first = namedNode("First")
        val second = namedNode("Second")
        val blank = ProjectionSpatialLayout().place(
            projectionDocument(
                nodes = listOf(first),
                regions = listOf(ProjectionRegion(regionId = "", name = "Blank", occurrenceNames = listOf(first.label))),
            ),
        )
        val duplicate = ProjectionSpatialLayout().place(
            projectionDocument(
                nodes = listOf(first, second),
                regions = listOf(
                    ProjectionRegion("region:shared", "First", listOf(first.label)),
                    ProjectionRegion("region:shared", "Second", listOf(second.label)),
                ),
            ),
        )
        val reservedId = "engineering-projection/sheet/01-main/region/unassigned"
        val reserved = ProjectionSpatialLayout().place(
            projectionDocument(
                nodes = listOf(first, second),
                regions = listOf(ProjectionRegion(reservedId, "Reserved", listOf(first.label))),
            ),
        )

        assertEquals("is blank", blank.diagnostics.single().problem)
        assertEquals("is used by 2 Regions on one Sheet", duplicate.diagnostics.single().problem)
        assertEquals("is reserved for compiler-owned unassigned Occurrences", reserved.diagnostics.single().problem)
        assertTrue(listOf(blank, duplicate, reserved).all { result -> result.occurrences.isEmpty() })
    }

    @Test
    fun `spatial placement rejects duplicate sheet identities`() {
        val first = namedNode("First")
        val second = namedNode("Second")
        val projection = projectionDocument(nodes = listOf(first, second), regions = emptyList())
        val duplicateId = projection.sheets.single().sheetId
        val sheets = listOf(first, second).mapIndexed { index, node ->
            val subjects = listOf(ProjectionSheetSubject(node.semanticId, nodeIds = listOf(node.projectionId)))
            ProjectionSheet(
                sheetId = duplicateId,
                displayName = "Sheet ${index + 1}",
                order = index,
                subjects = subjects,
                publication = ProjectionSheetPublication.fromProjectionState(
                    duplicateId,
                    "Sheet ${index + 1}",
                    index,
                    subjects,
                ),
            )
        }

        val result = ProjectionSpatialLayout().place(projection.copy(sheets = sheets))

        val diagnostic = result.diagnostics.single()
        assertEquals("Sheet identity ${duplicateId.value}", diagnostic.subject)
        assertEquals("is used by 2 Sheets", diagnostic.problem)
        assertEquals("Give every Sheet a unique identity.", diagnostic.correction)
        assertTrue(result.occurrences.isEmpty())
    }

    @Test
    fun `spatial placement uses earliest authored construct when memberships overlap`() {
        val first = namedNode("First")
        val projection = projectionDocument(
            nodes = listOf(first),
            regions = listOf(ProjectionRegion("region:first", "First", listOf(first.label))),
        )
        val sheet = projection.sheets.single().copy(
            constructs = listOf("Train A", "Train B").map { name ->
                ProjectionSheetConstruct(
                    constructId = ProjectionConstructId("construct:$name"),
                    kind = "sequence",
                    name = name,
                    memberNames = listOf(first.label),
                )
            },
        )

        val result = ProjectionSpatialLayout().place(projection.copy(sheets = listOf(sheet)))

        assertTrue(result.diagnostics.isEmpty())
        val reason = result.occurrences.single().placementReason.text
        assertTrue(reason.contains("Construct Train A member order 1"))
        assertFalse(reason.contains("Construct Train B"))
    }

    @Test
    fun `spatial placement uses exact typed occurrence identity when semantic identities repeat`() {
        val sharedSemanticId = StableSemanticIdentity("component:Shared")
        val first = namedNode("First").copy(semanticId = sharedSemanticId)
        val second = namedNode("Second").copy(semanticId = sharedSemanticId)
        val target = namedNode("Target")
        val projection = projectionDocument(
            nodes = listOf(first, second, target),
            regions = listOf(ProjectionRegion("region:all", "All", listOf("First", "Second", "Target"))),
        ).copy(
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("connection:ambiguous"),
                    semanticId = StableSemanticIdentity("connection:ambiguous"),
                    originGeometryElementId = GeometryElementId("origin:connection:ambiguous"),
                    source = ProjectionConnectionEndpoint(
                        ProjectionOccurrencePortId(first.projectionId, StableSemanticIdentity("port:First.out")),
                    ),
                    target = ProjectionConnectionEndpoint(
                        ProjectionOccurrencePortId(target.projectionId, StableSemanticIdentity("port:Target.in")),
                    ),
                ),
            ),
        )

        val result = ProjectionSpatialLayout().place(projection)

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(3, result.occurrences.size)
    }

    @Test
    fun `region member owned by no sheet gets complete ownership diagnostic`() {
        val owned = namedNode("Owned")
        val unowned = namedNode("Unowned")
        val projection = projectionDocument(
            nodes = listOf(owned, unowned),
            regions = listOf(ProjectionRegion("region:main", "Main", listOf("Owned", "Unowned"))),
        )
        val sheet = projection.sheets.single().copy(
            subjects = listOf(ProjectionSheetSubject(owned.semanticId, nodeIds = listOf(owned.projectionId))),
        )

        val result = ProjectionSpatialLayout().place(projection.copy(sheets = listOf(sheet)))

        val diagnostic = result.diagnostics.single { item -> item.subject == "Region Main member Unowned" }
        assertEquals("is not owned by any Sheet", diagnostic.problem)
        assertEquals("Reference Unowned from exactly one Sheet before assigning it to Region Main.", diagnostic.correction)
    }

    @Test
    fun `spatial placement safely skips a sheet with no owned occurrences`() {
        val projection = projectionDocument()
        val emptySheetId = ProjectionSheetId("engineering-projection/sheet/02-connections")
        val emptySheet = ProjectionSheet(
            sheetId = emptySheetId,
            displayName = "Connections",
            order = 1,
            subjects = emptyList(),
            regions = emptyList(),
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = emptySheetId,
                displayName = "Connections",
                order = 1,
                subjects = emptyList(),
            ),
        )

        val attempt = runCatching {
            ProjectionSpatialLayout().place(projection.copy(sheets = projection.sheets + emptySheet))
        }

        assertTrue(attempt.isSuccess, "empty Sheet must not fail placement: ${attempt.exceptionOrNull()}")
        val result = attempt.getOrThrow()
        assertTrue(result.diagnostics.isEmpty())
        assertEquals(3, result.occurrences.size)
        assertTrue(result.occurrences.all { occurrence -> occurrence.sheetId == projection.sheets.single().sheetId.value })
    }

    @Test
    fun `spatial placement rejects region columns wider than drawing area capacity`() {
        val nodes = (1..11).map { index -> namedNode("R$index") }
        val regions = nodes.mapIndexed { index, node ->
            ProjectionRegion(
                regionId = "engineering-projection/01-main/region-${index + 1}",
                name = "Region ${index + 1}",
                occurrenceNames = listOf(node.label),
            )
        }

        val result = ProjectionSpatialLayout().place(projectionDocument(nodes = nodes, regions = regions))

        assertTrue(result.occurrences.isEmpty())
        val diagnostic = result.diagnostics.single()
        assertEquals("Sheet engineering-projection/sheet/01-main", diagnostic.subject)
        assertEquals(
            "cannot fit 11 Region columns with 80-unit Occurrences and 32-unit gutters inside the Drawing Area",
            diagnostic.problem,
        )
        assertEquals("Reduce the Sheet Region count or move Regions to another Sheet.", diagnostic.correction)
    }

    @Test
    fun `spatial placement identity traces to projection occurrence ids`() {
        val result = ProjectionSpatialLayout().place(projectionDocument())

        assertTrue(result.occurrences.all { occurrence ->
            occurrence.occurrenceId.projectionId.startsWith("projection/node/") &&
                occurrence.sourceTrace.projectionIds.contains(occurrence.occurrenceId.projectionId)
        })
    }

    @Test
    fun `projection spatial compiler reports an empty projection plainly`() {
        val result = ProjectionSpatialCompiler().transform(
            projectionDocument(nodes = emptyList(), regions = emptyList(), connections = emptyList()),
        )

        val failure = kotlin.test.assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(
            listOf(RealityTransformationDiagnostic(reality = "Projection Reality", message = "empty sheet")),
            failure.diagnostics,
        )
    }

    @Test
    fun `projection models stay coordinate free`() {
        val projectionPropertyNames = listOf(
            ProjectionDocument::class.java,
            ProjectionNode::class.java,
            ProjectionConnection::class.java,
        ).flatMap { type -> type.declaredFields.map { field -> field.name } }
        val forbidden = listOf("x", "y", "width", "height", "bounds", "anchorPosition", "lane", "routePoints")

        forbidden.forEach { token ->
            assertFalse(
                projectionPropertyNames.any { name -> name.equals(token, ignoreCase = true) },
                "Projection model must not own spatial field `$token`: $projectionPropertyNames",
            )
        }
    }

    @Test
    fun `new spatial placement names avoid stale architecture terms`() {
        val names = listOf(
            ProjectionSpatialLayout::class.simpleName.orEmpty(),
            SpatialLayoutResult::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Spatial placement names must not contain `$token`: $names",
            )
        }
    }

    @Test
    fun `projection spatial layout delegates placement to shared rule based engine`() {
        assertTrue(
            ProjectionSpatialLayout::class.java.declaredFields.any { field ->
                field.type.name == "com.engineeringood.athena.layout.engine.RuleBasedLayoutEngine"
            },
            "ProjectionSpatialLayout must adapt Projection constraints into the shared domain-neutral layout engine.",
        )
        assertTrue(
            ProjectionSpatialLayout::class.java.constructors.all { constructor ->
                constructor.parameterTypes.none { type ->
                    type.name == "com.engineeringood.athena.layout.engine.RuleBasedLayoutEngine"
                }
            },
            "ProjectionSpatialLayout public constructors must not expose the concrete rule-based engine.",
        )
    }

    private fun expectedOccurrence(
        name: String,
        region: ProjectionRegion,
        x: Int,
        y: Int,
        regionOrder: Int,
        memberOrder: Int,
        separated: Boolean,
    ): SpatialOccurrenceGeometry {
        val sheetId = "engineering-projection/sheet/01-main"
        val projectionId = "projection/node/component:$name"
        return SpatialOccurrenceGeometry(
            occurrenceId = SpatialOccurrenceId(sheetId = sheetId, projectionId = projectionId),
            subjectId = StableSemanticIdentity("component:$name"),
            sheetId = sheetId,
            regionId = region.regionId,
            rectangle = SpatialRect(x = x, y = y, width = 80, height = 40),
            placementReason = SpatialPlacementReason(
                constraints = buildList {
                    add("owning Sheet $sheetId")
                    add("Region ${region.name} in authored order $regionOrder")
                    add("authored Region member order $memberOrder")
                    add("stable Projection identity fallback $projectionId")
                    add("Drawing Area (40,60,1120,640)")
                    add("32-unit Region gutter")
                    add("24-unit grouping padding")
                    if (separated) add("48-unit minimum vertical separation")
                },
            ),
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(sheetId, region.regionId, projectionId),
                geometryElementIds = listOf(GeometryElementId("origin:$name")),
            ),
        )
    }

    private fun projectionDocument(
        nodes: List<ProjectionNode> = listOf(supplyNode(), breakerNode(), loadNode()),
        regions: List<ProjectionRegion> = projectionRegions(),
        connections: List<ProjectionConnection> = listOf(testConnection()),
    ): ProjectionDocument {
        val view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection")
        val subjects = nodes.map { node -> ProjectionSheetSubject(node.semanticId, nodeIds = listOf(node.projectionId)) } +
            connections.map { connection ->
                ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId))
            }
        val sheetId = ProjectionSheetId("engineering-projection/sheet/01-main")
        return ProjectionDocument(
            view = view,
            nodes = nodes,
            connections = connections,
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Engineering Projection Main",
                    order = 0,
                    subjects = subjects,
                    regions = regions,
                    grid = ProjectionSheetGrid("grid:main", rows = 8, columns = 12),
                    publication = ProjectionSheetPublication.fromProjectionState(
                        sheetId = sheetId,
                        displayName = "Engineering Projection Main",
                        order = 0,
                        subjects = subjects,
                    ),
                ),
            ),
        )
    }

    private fun testConnection(): ProjectionConnection = ProjectionConnection(
        projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.1"),
        semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
        originGeometryElementId = GeometryElementId("origin:connection"),
    )

    private fun projectionRegions(): List<ProjectionRegion> = listOf(
        ProjectionRegion(
            regionId = "engineering-projection/01-main/supply",
            name = "Supply",
            occurrenceNames = listOf("Supply"),
        ),
        ProjectionRegion(
            regionId = "engineering-projection/01-main/control",
            name = "Control",
            occurrenceNames = listOf("Q1"),
        ),
        ProjectionRegion(
            regionId = "engineering-projection/01-main/load",
            name = "Load",
            occurrenceNames = listOf("Load"),
        ),
    )

    private fun supplyNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Supply"),
            semanticId = StableSemanticIdentity("component:Supply"),
            label = "Supply",
            originGeometryElementId = GeometryElementId("origin:Supply"),
        )

    private fun breakerNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q1"),
            semanticId = StableSemanticIdentity("component:Q1"),
            label = "Q1",
            originGeometryElementId = GeometryElementId("origin:Q1"),
        )

    private fun loadNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Load"),
            semanticId = StableSemanticIdentity("component:Load"),
            label = "Load",
            originGeometryElementId = GeometryElementId("origin:Load"),
        )

    private fun auxNode(): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Aux"),
            semanticId = StableSemanticIdentity("component:Aux"),
            label = "Aux",
            originGeometryElementId = GeometryElementId("origin:Aux"),
        )

    private fun namedNode(name: String): ProjectionNode =
        ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:$name"),
            semanticId = StableSemanticIdentity("component:$name"),
            label = name,
            originGeometryElementId = GeometryElementId("origin:$name"),
        )
}
