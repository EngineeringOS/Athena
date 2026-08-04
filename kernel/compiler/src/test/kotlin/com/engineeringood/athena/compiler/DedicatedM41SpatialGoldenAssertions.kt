package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialAlignment
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialGridReferenceId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialQualityMetrics
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.assertEquals

private const val GOLDEN_SHEET_ID = "schematic/sheet/S1"
private const val GOLDEN_GRID_ID = "schematic/G1"
private val GOLDEN_DRAWING_AREA = SpatialRect(40, 60, 1120, 640)

internal fun assertDedicatedM41SpatialGolden(spatial: SpatialSheet) {
    val occurrenceSpecs = listOf(
        GoldenOccurrence("Supply", "Power Distribution", 1, SpatialRect(176, 84, 80, 40), "L1", 1, 1, 1),
        GoldenOccurrence("Breaker", "Power Distribution", 1, SpatialRect(176, 636, 80, 40), "L1", 2, 2, 2),
        GoldenOccurrence("Contactor", "Control Logic", 2, SpatialRect(560, 84, 80, 40), "R1", 2, 3, 3),
        GoldenOccurrence("StartButton", "Control Logic", 2, SpatialRect(560, 360, 80, 40), null, null, 1, 1),
        GoldenOccurrence("LimitSwitch", "Control Logic", 2, SpatialRect(560, 636, 80, 40), null, null, 2, 2),
        GoldenOccurrence("Motor", "Load And Protection", 3, SpatialRect(944, 84, 80, 40), "B1", 2, 2, 1),
        GoldenOccurrence("EarthBar", "Load And Protection", 3, SpatialRect(944, 360, 80, 40), null, null, 1, 2),
        GoldenOccurrence("Cabinet", "Load And Protection", 3, SpatialRect(944, 636, 80, 40), null, null, 3, 3),
    )
    val expectedOccurrences = occurrenceSpecs.map(::expectedOccurrence)
    assertEquals(expectedOccurrences, spatial.occurrences)

    val regionSpecs = listOf(
        GoldenRegion(
            "Power Distribution",
            listOf("Supply", "Breaker"),
            listOf("Supply", "Breaker"),
            SpatialRect(152, 60, 128, 640),
        ),
        GoldenRegion(
            "Control Logic",
            listOf("StartButton", "LimitSwitch", "Contactor"),
            listOf("Contactor", "StartButton", "LimitSwitch"),
            SpatialRect(536, 60, 128, 640),
        ),
        GoldenRegion(
            "Load And Protection",
            listOf("Motor", "EarthBar", "Cabinet"),
            listOf("Motor", "EarthBar", "Cabinet"),
            SpatialRect(920, 60, 128, 640),
        ),
    )
    val expectedRegions = regionSpecs.map(::expectedRegion)
    assertEquals(expectedRegions, spatial.regions)

    val constructSpecs = listOf(
        GoldenConstruct("power-rail", "L1", listOf("Supply", "Breaker"), SpatialRect(152, 60, 128, 640)),
        GoldenConstruct("rung", "R1", listOf("Breaker", "Contactor"), SpatialRect(152, 60, 512, 640)),
        GoldenConstruct("branch", "B1", listOf("Contactor", "Motor"), SpatialRect(536, 60, 512, 88)),
        GoldenConstruct("wire-bundle", "W1", listOf("Supply"), SpatialRect(152, 60, 128, 88)),
        GoldenConstruct("terminal-strip", "X1", listOf("Contactor"), SpatialRect(536, 60, 128, 88)),
        GoldenConstruct("contact-group", "C1", listOf("Contactor"), SpatialRect(536, 60, 128, 88)),
        GoldenConstruct("coil-group", "K1", listOf("Contactor"), SpatialRect(536, 60, 128, 88)),
    )
    val expectedConstructs = constructSpecs.map(::expectedConstruct)
    assertEquals(expectedConstructs, spatial.constructs)

    val expectedAlignments = regionSpecs.map { spec ->
        val source = SpatialAlignmentSource.Region(SpatialRegionId(GOLDEN_SHEET_ID, regionId(spec.name)))
        SpatialAlignment(
            alignmentId = SpatialAlignmentId(GOLDEN_SHEET_ID, source),
            sheetId = GOLDEN_SHEET_ID,
            constraintSource = source,
            occurrenceIds = spec.alignmentMembers.map(::occurrenceId),
            sourceTrace = regionTrace(spec.name, spec.authoredMembers),
        )
    } + constructSpecs.map { spec ->
        val source = SpatialAlignmentSource.Construct(
            SpatialConstructId(GOLDEN_SHEET_ID, constructId(spec.kind, spec.name)),
        )
        SpatialAlignment(
            alignmentId = SpatialAlignmentId(GOLDEN_SHEET_ID, source),
            sheetId = GOLDEN_SHEET_ID,
            constraintSource = source,
            occurrenceIds = spec.members.map(::occurrenceId),
            sourceTrace = constructTrace(spec.kind, spec.name, spec.members),
        )
    }
    assertEquals(expectedAlignments, spatial.alignments)

    assertEquals(
        SpatialGridDefinition(
            sheetId = GOLDEN_SHEET_ID,
            gridId = GOLDEN_GRID_ID,
            drawingArea = GOLDEN_DRAWING_AREA,
            rows = 8,
            columns = 10,
            sourceTrace = SpatialSourceTrace(
                listOf(GOLDEN_SHEET_ID, GOLDEN_GRID_ID),
                listOf(sheetOrigin()),
            ),
        ),
        spatial.grid,
    )
    assertEquals(expectedGridReferences(expectedOccurrences, expectedConstructs), spatial.gridReferences)

    assertEquals(
        SpatialSourceTrace(listOf(GOLDEN_SHEET_ID), listOf(sheetOrigin())),
        spatial.sourceTrace,
    )
    assertEquals(SpatialQualitySnapshotId(GOLDEN_SHEET_ID), spatial.quality.qualitySnapshotId)
    assertEquals(GOLDEN_SHEET_ID, spatial.quality.sheetId)
    assertEquals(expectedQualityTrace(regionSpecs, constructSpecs), spatial.quality.sourceTrace)

    val expectedAnchors = expectedAnchors(occurrenceSpecs)
    val expectedRoutes = expectedRoutes(expectedAnchors)
    val expectedLanes = expectedRoutes.groupBy(SpatialRoute::laneId)
        .map { (laneId, routes) ->
            SpatialLane(
                laneId = laneId,
                sheetId = GOLDEN_SHEET_ID,
                orientation = laneId.orientation,
                coordinate = laneId.coordinate,
                routeIds = routes.map(SpatialRoute::routeId).sortedBy(SpatialRouteId::value),
            )
        }
        .sortedBy { lane -> lane.laneId.value }
    val expectedQuality = SpatialQualitySnapshot(
        qualitySnapshotId = SpatialQualitySnapshotId(GOLDEN_SHEET_ID),
        sheetId = GOLDEN_SHEET_ID,
        metrics = SpatialQualityMetrics(
            occurrenceOverlapCount = 0,
            constructContainmentFailureCount = 0,
            routeBodyIntersectionCount = 0,
            routeCrossingCount = 3,
            twistCount = 0,
            usedLaneCount = 7,
            peakRoutesPerLane = 2,
            density = 8.0 / 716_800.0,
            occupancy = 25_600.0 / 716_800.0,
        ),
        sourceTrace = expectedQualityTrace(regionSpecs, constructSpecs),
    )
    val expectedSheet = SpatialSheet(
        sheetId = GOLDEN_SHEET_ID,
        extent = SpatialRect(0, 0, 1200, 800),
        drawingArea = GOLDEN_DRAWING_AREA,
        grid = SpatialGridDefinition(
            sheetId = GOLDEN_SHEET_ID,
            gridId = GOLDEN_GRID_ID,
            drawingArea = GOLDEN_DRAWING_AREA,
            rows = 8,
            columns = 10,
            sourceTrace = SpatialSourceTrace(listOf(GOLDEN_SHEET_ID, GOLDEN_GRID_ID), listOf(sheetOrigin())),
        ),
        occurrences = expectedOccurrences,
        regions = expectedRegions,
        constructs = expectedConstructs,
        alignments = expectedAlignments,
        anchors = expectedAnchors,
        lanes = expectedLanes,
        routes = expectedRoutes,
        gridReferences = expectedGridReferences(expectedOccurrences, expectedConstructs),
        quality = expectedQuality,
        sourceTrace = SpatialSourceTrace(listOf(GOLDEN_SHEET_ID), listOf(sheetOrigin())),
    )
    assertEquals(SpatialDocument(listOf(expectedSheet)), SpatialDocument(listOf(spatial)))
}

private fun expectedOccurrence(spec: GoldenOccurrence): SpatialOccurrenceGeometry {
    val orderingConstraints = buildList {
        if (spec.constructName != null) {
            add("Construct ${spec.constructName} member order ${spec.constructMemberOrder}")
        }
        add("Connection topology order ${spec.topologyOrder}")
        add("authored Region member order ${spec.authoredMemberOrder}")
        add("stable Projection identity fallback ${occurrenceProjectionId(spec.name)}")
    }
    return SpatialOccurrenceGeometry(
        occurrenceId = occurrenceId(spec.name),
        subjectId = StableSemanticIdentity("component:${spec.name}"),
        sheetId = GOLDEN_SHEET_ID,
        regionId = regionId(spec.regionName),
        rectangle = spec.rectangle,
        placementReason = SpatialPlacementReason(
            listOf(
                "owning Sheet $GOLDEN_SHEET_ID",
                "Region ${spec.regionName} in authored order ${spec.regionOrder}",
            ) + orderingConstraints + listOf(
                "Drawing Area (40,60,1120,640)",
                "32-unit Region gutter",
                "24-unit grouping padding",
                "48-unit minimum vertical separation",
            ),
        ),
        sourceTrace = occurrenceTrace(spec.name, spec.regionName),
    )
}

private fun expectedRegion(spec: GoldenRegion): SpatialRegionGeometry = SpatialRegionGeometry(
    regionId = SpatialRegionId(GOLDEN_SHEET_ID, regionId(spec.name)),
    sheetId = GOLDEN_SHEET_ID,
    memberOccurrenceIds = spec.authoredMembers.map(::occurrenceId),
    bounds = spec.bounds,
    sourceTrace = regionTrace(spec.name, spec.authoredMembers),
)

private fun expectedConstruct(spec: GoldenConstruct): SpatialConstructGeometry = SpatialConstructGeometry(
    constructId = SpatialConstructId(GOLDEN_SHEET_ID, constructId(spec.kind, spec.name)),
    sheetId = GOLDEN_SHEET_ID,
    kind = spec.kind,
    name = spec.name,
    memberOccurrenceIds = spec.members.map(::occurrenceId),
    envelope = spec.envelope,
    sourceTrace = constructTrace(spec.kind, spec.name, spec.members),
)

private fun expectedGridReferences(
    occurrences: List<SpatialOccurrenceGeometry>,
    constructs: List<SpatialConstructGeometry>,
): List<SpatialGridReference> {
    val occurrenceTraces = occurrences.associate { occurrence -> occurrence.occurrenceId to occurrence.sourceTrace }
    val constructTraces = constructs.associate { construct -> construct.constructId to construct.sourceTrace }
    val occurrenceCells = listOf(
        "Breaker" to "H2",
        "Cabinet" to "H9",
        "Contactor" to "A6",
        "EarthBar" to "E9",
        "LimitSwitch" to "H6",
        "Motor" to "A9",
        "StartButton" to "E6",
        "Supply" to "A2",
    )
    val constructCells = listOf(
        Triple("branch", "B1", "A7"),
        Triple("coil-group", "K1", "A6"),
        Triple("contact-group", "C1", "A6"),
        Triple("power-rail", "L1", "E2"),
        Triple("rung", "R1", "E4"),
        Triple("terminal-strip", "X1", "A6"),
        Triple("wire-bundle", "W1", "A2"),
    )
    return occurrenceCells.map { (name, cell) ->
        val subject = SpatialGridReferenceSubject.Occurrence(occurrenceId(name))
        gridReference(subject, cell, occurrenceTraces.getValue(subject.occurrenceId))
    } + constructCells.map { (kind, name, cell) ->
        val subject = SpatialGridReferenceSubject.Construct(
            SpatialConstructId(GOLDEN_SHEET_ID, constructId(kind, name)),
        )
        gridReference(subject, cell, constructTraces.getValue(subject.constructId))
    }
}

private fun gridReference(
    subject: SpatialGridReferenceSubject,
    cell: String,
    sourceTrace: SpatialSourceTrace,
): SpatialGridReference {
    val rowLabel = cell.takeWhile(Char::isLetter)
    val rowIndex = rowLabel.single() - 'A'
    val columnNumber = cell.drop(rowLabel.length).toInt()
    return SpatialGridReference(
        gridReferenceId = SpatialGridReferenceId(GOLDEN_SHEET_ID, subject),
        sheetId = GOLDEN_SHEET_ID,
        gridId = GOLDEN_GRID_ID,
        subject = subject,
        rowIndex = rowIndex,
        rowLabel = rowLabel,
        columnIndex = columnNumber - 1,
        columnNumber = columnNumber,
        cellReference = cell,
        sourceTrace = sourceTrace,
    )
}

private fun expectedAnchors(
    occurrences: List<GoldenOccurrence>,
): List<SpatialAnchorPosition> {
    val regionByOccurrence = occurrences.associate { occurrence -> occurrence.name to occurrence.regionName }
    return goldenAnchorSpecs.map { spec ->
        val occurrenceId = occurrenceId(spec.occurrence)
        val portId = StableSemanticIdentity("port:${spec.occurrence}.${spec.port}")
        val subject = SpatialOccurrencePortSubject(occurrenceId, portId)
        SpatialAnchorPosition(
            anchorId = SpatialAnchorId(GOLDEN_SHEET_ID, occurrenceId, portId),
            sheetId = GOLDEN_SHEET_ID,
            subject = subject,
            side = spec.side,
            point = spec.point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(GOLDEN_SHEET_ID, occurrenceId.projectionId, portId.value) +
                    spec.connections.map(::connectionProjectionId) +
                    regionId(regionByOccurrence.getValue(spec.occurrence)),
                geometryElementIds = (
                    listOf(
                        sheetOrigin(),
                        occurrenceOrigin(spec.occurrence),
                        GeometryElementId(
                            "projection:schematic:${occurrenceProjectionId(spec.occurrence)}:port:${portId.value}",
                        ),
                    ) + spec.connections.map { connection -> GeometryElementId(connectionGeometryId(connection)) }
                    ).distinct().sortedBy { geometryId -> geometryId.value },
            ),
        )
    }.sortedWith(
        compareBy<SpatialAnchorPosition>(
            { anchor -> anchor.subject.occurrenceId.projectionId },
            { anchor -> anchor.subject.portId.value },
        ),
    )
}

private fun expectedRoutes(
    anchors: List<SpatialAnchorPosition>,
): List<SpatialRoute> {
    val anchorsByEndpoint = anchors.associateBy { anchor ->
        anchor.subject.occurrenceId.projectionId.substringAfterLast('/') to
            anchor.subject.portId.value.substringAfter('.')
    }
    return goldenRouteSpecs.map { spec ->
        val source = anchorsByEndpoint.getValue(spec.sourceOccurrence to spec.sourcePort)
        val target = anchorsByEndpoint.getValue(spec.targetOccurrence to spec.targetPort)
        val requiredProjectionIds = listOf(
            GOLDEN_SHEET_ID,
            connectionProjectionId(spec.connection),
            source.subject.occurrenceId.projectionId,
            source.subject.portId.value,
            target.subject.occurrenceId.projectionId,
            target.subject.portId.value,
        )
        val requiredSet = requiredProjectionIds.toSet()
        val inheritedProjectionIds = (source.sourceTrace.projectionIds + target.sourceTrace.projectionIds)
            .filterNot(requiredSet::contains)
            .distinct()
            .sorted()
        val laneId = SpatialLaneId(GOLDEN_SHEET_ID, spec.laneOrientation, spec.laneCoordinate)
        SpatialRoute(
            routeId = SpatialRouteId(GOLDEN_SHEET_ID, connectionProjectionId(spec.connection)),
            sheetId = GOLDEN_SHEET_ID,
            connectionId = StableSemanticIdentity(connectionSemanticId(spec.connection)),
            sourceAnchorId = source.anchorId,
            targetAnchorId = target.anchorId,
            laneId = laneId,
            sourceTrace = SpatialSourceTrace(
                projectionIds = requiredProjectionIds + inheritedProjectionIds,
                geometryElementIds = (
                    listOf(GeometryElementId(connectionGeometryId(spec.connection))) +
                        source.sourceTrace.geometryElementIds +
                        target.sourceTrace.geometryElementIds
                    ).distinct().sortedBy { geometryId -> geometryId.value },
            ),
            points = spec.points,
        )
    }.sortedBy { route -> route.routeId.value }
}

private fun expectedQualityTrace(
    regions: List<GoldenRegion>,
    constructs: List<GoldenConstruct>,
): SpatialSourceTrace {
    val occurrenceNames = listOf(
        "Breaker",
        "Cabinet",
        "Contactor",
        "EarthBar",
        "LimitSwitch",
        "Motor",
        "StartButton",
        "Supply",
    )
    val referencedPorts = listOf(
        "Breaker" to "line",
        "Breaker" to "load",
        "Cabinet" to "PE",
        "Contactor" to "L",
        "Contactor" to "N",
        "Contactor" to "PE",
        "Contactor" to "U",
        "Contactor" to "coilA1",
        "Contactor" to "coilA2",
        "EarthBar" to "PE",
        "LimitSwitch" to "contact13",
        "Motor" to "PE",
        "Motor" to "U",
        "StartButton" to "contact13",
        "Supply" to "L1",
        "Supply" to "N",
    )
    val connectionNames = listOf(
        "control_LimitSwitch_contact13_to_Contactor_coilA2",
        "control_StartButton_contact13_to_Contactor_coilA1",
        "earth_EarthBar_PE_to_Cabinet_PE",
        "earth_EarthBar_PE_to_Contactor_PE",
        "earth_EarthBar_PE_to_Motor_PE",
        "power_Breaker_load_to_Contactor_L",
        "power_Contactor_U_to_Motor_U",
        "power_Supply_L1_to_Breaker_line",
        "power_Supply_N_to_Contactor_N",
    )
    val projectionIds = listOf(GOLDEN_SHEET_ID) + (
        listOf(GOLDEN_GRID_ID) +
            regions.map { spec -> regionId(spec.name) } +
            constructs.map { spec -> constructId(spec.kind, spec.name) } +
            connectionNames.map(::connectionProjectionId) +
            occurrenceNames.map(::occurrenceProjectionId) +
            referencedPorts.map { (occurrence, port) -> "port:$occurrence.$port" }
        ).distinct().sorted()
    val geometryIds = (
        listOf(sheetOrigin()) +
            regions.map { spec -> GeometryElementId("projection-region:${regionId(spec.name)}") } +
            constructs.map { spec -> GeometryElementId("projection-construct:${constructId(spec.kind, spec.name)}") } +
            connectionNames.map { name -> GeometryElementId(connectionGeometryId(name)) } +
            referencedPorts.map { (occurrence, port) ->
                GeometryElementId(
                    "projection:schematic:${occurrenceProjectionId(occurrence)}:port:port:$occurrence.$port",
                )
            } +
            occurrenceNames.map { name -> occurrenceOrigin(name) }
        ).distinct().sortedBy { geometryId -> geometryId.value }
    return SpatialSourceTrace(projectionIds, geometryIds)
}

private fun occurrenceTrace(name: String, regionName: String): SpatialSourceTrace = SpatialSourceTrace(
    listOf(GOLDEN_SHEET_ID, regionId(regionName), occurrenceProjectionId(name)),
    listOf(occurrenceOrigin(name)),
)

private fun regionTrace(name: String, members: List<String>): SpatialSourceTrace = SpatialSourceTrace(
    listOf(GOLDEN_SHEET_ID, regionId(name)) + members.map(::occurrenceProjectionId),
    listOf(GeometryElementId("projection-region:${regionId(name)}")) + members.map(::occurrenceOrigin),
)

private fun constructTrace(kind: String, name: String, members: List<String>): SpatialSourceTrace = SpatialSourceTrace(
    listOf(GOLDEN_SHEET_ID, constructId(kind, name)) + members.map(::occurrenceProjectionId),
    listOf(GeometryElementId("projection-construct:${constructId(kind, name)}")) + members.map(::occurrenceOrigin),
)

private fun occurrenceId(name: String): SpatialOccurrenceId =
    SpatialOccurrenceId(GOLDEN_SHEET_ID, occurrenceProjectionId(name))

private fun occurrenceProjectionId(name: String): String = "$GOLDEN_SHEET_ID/occurrence/$name"

private fun occurrenceOrigin(name: String): GeometryElementId =
    GeometryElementId("projection:schematic:sheet:S1:occurrence:$name")

private fun regionId(name: String): String = "schematic/S1/$name"

private fun constructId(kind: String, name: String): String = "schematic/S1/$kind:$name"

private fun connectionProjectionId(name: String): String =
    "schematic/connection/connection:examples/m41/rolling-shutter/src/com/engineeringood/m41/" +
        "rollingshutter/01-rolling-shutter-spatial.athena:$name"

private fun connectionGeometryId(name: String): String =
    "projection:schematic:connection:examples/m41/rolling-shutter/src/com/engineeringood/m41/" +
        "rollingshutter/01-rolling-shutter-spatial.athena:$name"

private fun connectionSemanticId(name: String): String =
    "connection:examples/m41/rolling-shutter/src/com/engineeringood/m41/" +
        "rollingshutter/01-rolling-shutter-spatial.athena:$name"

private fun sheetOrigin(): GeometryElementId = GeometryElementId("projection-sheet:$GOLDEN_SHEET_ID")

private data class GoldenOccurrence(
    val name: String,
    val regionName: String,
    val regionOrder: Int,
    val rectangle: SpatialRect,
    val constructName: String?,
    val constructMemberOrder: Int?,
    val topologyOrder: Int,
    val authoredMemberOrder: Int,
)

private data class GoldenRegion(
    val name: String,
    val authoredMembers: List<String>,
    val alignmentMembers: List<String>,
    val bounds: SpatialRect,
)

private data class GoldenConstruct(
    val kind: String,
    val name: String,
    val members: List<String>,
    val envelope: SpatialRect,
)

private data class GoldenAnchor(
    val occurrence: String,
    val port: String,
    val side: SpatialBoundarySide,
    val point: SpatialPoint,
    val connections: List<String>,
)

private data class GoldenRoute(
    val connection: String,
    val sourceOccurrence: String,
    val sourcePort: String,
    val targetOccurrence: String,
    val targetPort: String,
    val laneOrientation: SpatialLaneOrientation,
    val laneCoordinate: Int,
    val points: List<SpatialPoint>,
)

private val goldenAnchorSpecs = listOf(
    GoldenAnchor("Breaker", "line", SpatialBoundarySide.TOP, SpatialPoint(202, 636), listOf("power_Supply_L1_to_Breaker_line")),
    GoldenAnchor("Breaker", "load", SpatialBoundarySide.TOP, SpatialPoint(229, 636), listOf("power_Breaker_load_to_Contactor_L")),
    GoldenAnchor("Cabinet", "PE", SpatialBoundarySide.TOP, SpatialPoint(984, 636), listOf("earth_EarthBar_PE_to_Cabinet_PE")),
    GoldenAnchor("Contactor", "L", SpatialBoundarySide.BOTTOM, SpatialPoint(580, 124), listOf("power_Breaker_load_to_Contactor_L")),
    GoldenAnchor("Contactor", "N", SpatialBoundarySide.LEFT, SpatialPoint(560, 104), listOf("power_Supply_N_to_Contactor_N")),
    GoldenAnchor("Contactor", "PE", SpatialBoundarySide.RIGHT, SpatialPoint(640, 97), listOf("earth_EarthBar_PE_to_Contactor_PE")),
    GoldenAnchor("Contactor", "U", SpatialBoundarySide.RIGHT, SpatialPoint(640, 110), listOf("power_Contactor_U_to_Motor_U")),
    GoldenAnchor("Contactor", "coilA1", SpatialBoundarySide.BOTTOM, SpatialPoint(600, 124), listOf("control_StartButton_contact13_to_Contactor_coilA1")),
    GoldenAnchor("Contactor", "coilA2", SpatialBoundarySide.BOTTOM, SpatialPoint(620, 124), listOf("control_LimitSwitch_contact13_to_Contactor_coilA2")),
    GoldenAnchor(
        "EarthBar",
        "PE",
        SpatialBoundarySide.BOTTOM,
        SpatialPoint(984, 400),
        listOf(
            "earth_EarthBar_PE_to_Cabinet_PE",
            "earth_EarthBar_PE_to_Contactor_PE",
            "earth_EarthBar_PE_to_Motor_PE",
        ),
    ),
    GoldenAnchor("LimitSwitch", "contact13", SpatialBoundarySide.TOP, SpatialPoint(600, 636), listOf("control_LimitSwitch_contact13_to_Contactor_coilA2")),
    GoldenAnchor("Motor", "PE", SpatialBoundarySide.BOTTOM, SpatialPoint(984, 124), listOf("earth_EarthBar_PE_to_Motor_PE")),
    GoldenAnchor("Motor", "U", SpatialBoundarySide.LEFT, SpatialPoint(944, 104), listOf("power_Contactor_U_to_Motor_U")),
    GoldenAnchor("StartButton", "contact13", SpatialBoundarySide.TOP, SpatialPoint(600, 360), listOf("control_StartButton_contact13_to_Contactor_coilA1")),
    GoldenAnchor("Supply", "L1", SpatialBoundarySide.BOTTOM, SpatialPoint(216, 124), listOf("power_Supply_L1_to_Breaker_line")),
    GoldenAnchor("Supply", "N", SpatialBoundarySide.RIGHT, SpatialPoint(256, 104), listOf("power_Supply_N_to_Contactor_N")),
)

private val goldenRouteSpecs = listOf(
    GoldenRoute(
        "control_LimitSwitch_contact13_to_Contactor_coilA2",
        "LimitSwitch",
        "contact13",
        "Contactor",
        "coilA2",
        SpatialLaneOrientation.VERTICAL,
        600,
        listOf(
            SpatialPoint(600, 636), SpatialPoint(600, 635), SpatialPoint(600, 400),
            SpatialPoint(640, 400), SpatialPoint(640, 360), SpatialPoint(620, 360),
            SpatialPoint(620, 125), SpatialPoint(620, 124),
        ),
    ),
    GoldenRoute(
        "control_StartButton_contact13_to_Contactor_coilA1",
        "StartButton",
        "contact13",
        "Contactor",
        "coilA1",
        SpatialLaneOrientation.VERTICAL,
        600,
        listOf(SpatialPoint(600, 360), SpatialPoint(600, 359), SpatialPoint(600, 125), SpatialPoint(600, 124)),
    ),
    GoldenRoute(
        "earth_EarthBar_PE_to_Cabinet_PE",
        "EarthBar",
        "PE",
        "Cabinet",
        "PE",
        SpatialLaneOrientation.VERTICAL,
        984,
        listOf(SpatialPoint(984, 400), SpatialPoint(984, 401), SpatialPoint(984, 635), SpatialPoint(984, 636)),
    ),
    GoldenRoute(
        "earth_EarthBar_PE_to_Contactor_PE",
        "EarthBar",
        "PE",
        "Contactor",
        "PE",
        SpatialLaneOrientation.HORIZONTAL,
        401,
        listOf(
            SpatialPoint(984, 400), SpatialPoint(984, 401), SpatialPoint(641, 401),
            SpatialPoint(641, 97), SpatialPoint(640, 97),
        ),
    ),
    GoldenRoute(
        "earth_EarthBar_PE_to_Motor_PE",
        "EarthBar",
        "PE",
        "Motor",
        "PE",
        SpatialLaneOrientation.VERTICAL,
        944,
        listOf(
            SpatialPoint(984, 400), SpatialPoint(984, 401), SpatialPoint(944, 401),
            SpatialPoint(944, 125), SpatialPoint(984, 125), SpatialPoint(984, 124),
        ),
    ),
    GoldenRoute(
        "power_Breaker_load_to_Contactor_L",
        "Breaker",
        "load",
        "Contactor",
        "L",
        SpatialLaneOrientation.VERTICAL,
        229,
        listOf(
            SpatialPoint(229, 636), SpatialPoint(229, 635), SpatialPoint(229, 125),
            SpatialPoint(580, 125), SpatialPoint(580, 124),
        ),
    ),
    GoldenRoute(
        "power_Contactor_U_to_Motor_U",
        "Contactor",
        "U",
        "Motor",
        "U",
        SpatialLaneOrientation.HORIZONTAL,
        104,
        listOf(
            SpatialPoint(640, 110), SpatialPoint(641, 110), SpatialPoint(641, 104),
            SpatialPoint(943, 104), SpatialPoint(944, 104),
        ),
    ),
    GoldenRoute(
        "power_Supply_L1_to_Breaker_line",
        "Supply",
        "L1",
        "Breaker",
        "line",
        SpatialLaneOrientation.VERTICAL,
        202,
        listOf(
            SpatialPoint(216, 124), SpatialPoint(216, 125), SpatialPoint(202, 125),
            SpatialPoint(202, 635), SpatialPoint(202, 636),
        ),
    ),
    GoldenRoute(
        "power_Supply_N_to_Contactor_N",
        "Supply",
        "N",
        "Contactor",
        "N",
        SpatialLaneOrientation.HORIZONTAL,
        104,
        listOf(SpatialPoint(256, 104), SpatialPoint(257, 104), SpatialPoint(559, 104), SpatialPoint(560, 104)),
    ),
)
