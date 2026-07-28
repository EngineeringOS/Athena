package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.ExactMillimeters
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalDuctV0
import com.engineeringood.athena.physical.PhysicalEnclosureV0
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationClearanceV0
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenanceV0
import com.engineeringood.athena.physical.PhysicalInstallationContractV0
import com.engineeringood.athena.physical.PhysicalInstallationIRV0
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSizeV0
import com.engineeringood.athena.physical.PhysicalInstallationSpaceV0
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountedOccurrenceV0
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRouteChannelV0
import com.engineeringood.athena.physical.PhysicalRouteIntentV0
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.RouteChannelTopologyCompiler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CabinetRoutingCompilerTest {
    @Test
    fun `routes aliased connection through exact channel lanes and emits deterministic proof`() {
        val ir = ir(
            routes = listOf(route("Main", "CH1", "CH2")),
            channels = listOf(
                channel("CH1", x = 20, y = 10, width = 70, height = 20, orientation = PhysicalInfrastructureOrientation.Horizontal),
                channel("CH2", x = 90, y = 10, width = 20, height = 80, orientation = PhysicalInfrastructureOrientation.Vertical),
            ),
        )
        val topology = assertIs<com.engineeringood.athena.physical.RouteChannelTopologyCompilation.Success>(
            RouteChannelTopologyCompiler.compile(ir.space.channels, ir.routes),
        ).topology

        val result = CabinetRoutingCompiler.compile(
            CabinetRoutingRequest(
                ir = ir,
                topology = topology,
                joins = listOf(
                    join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                    join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                ),
                endpoints = listOf(
                    CabinetConnectionEndpointBinding(
                        connectionAlias = "Main",
                        from = CabinetRouteEndpointRef(key("component:Source"), "OUT"),
                        to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                    ),
                ),
            ),
        )

        val success = assertIs<CabinetRoutingCompilation.Success>(result)
        val route = success.routes.single()

        assertEquals("Main", route.connectionAlias)
        assertEquals(listOf(PhysicalObjectId("CH1"), PhysicalObjectId("CH2")), route.orderedChannelIds)
        assertEquals(CabinetRouteEndpointPoint(key("component:Source"), "OUT", CabinetPointD(20.0, 110.0)), route.from)
        assertEquals(CabinetRouteEndpointPoint(key("component:Target"), "IN", CabinetPointD(100.0, 180.0)), route.to)
        assertEquals(
            listOf(
                CabinetRouteSegment(CabinetPointD(20.0, 110.0), CabinetPointD(90.0, 110.0)),
                CabinetRouteSegment(CabinetPointD(90.0, 110.0), CabinetPointD(100.0, 110.0)),
                CabinetRouteSegment(CabinetPointD(100.0, 110.0), CabinetPointD(100.0, 180.0)),
            ),
            route.segments,
        )
        assertEquals(ExactMillimeters(10, 1), route.laneCentersByChannel.getValue(PhysicalObjectId("CH1")))
        assertEquals(ExactMillimeters(10, 1), route.laneCentersByChannel.getValue(PhysicalObjectId("CH2")))
        assertEquals(
            CabinetRouteProof(
                routeCount = 1,
                channelUse = mapOf(PhysicalObjectId("CH1") to 1, PhysicalObjectId("CH2") to 1),
                endpointBindingCount = 2,
                segmentCount = 3,
                bodyIntersectionCount = 0,
                offChannelSegmentCount = 0,
                unboundEndpointCount = 0,
            ),
            success.proof,
        )
    }

    @Test
    fun `fails closed on missing anchors missing lanes missing adjacency and body intersections`() {
        val validChannels = listOf(
            channel("CH1", x = 20, y = 10, width = 70, height = 20, orientation = PhysicalInfrastructureOrientation.Horizontal),
            channel("CH2", x = 90, y = 10, width = 20, height = 80, orientation = PhysicalInfrastructureOrientation.Vertical),
            channel("GAP", x = 140, y = 10, width = 20, height = 80, orientation = PhysicalInfrastructureOrientation.Vertical),
        )
        val missingAnchorFailure = assertIs<CabinetRoutingCompilation.Failure>(
            CabinetRoutingCompiler.compile(
                CabinetRoutingRequest(
                    ir = ir(
                        routes = listOf(route("MissingAnchor", "CH1")),
                        channels = validChannels.take(1),
                    ),
                    topology = topology(
                        channels = validChannels.take(1),
                        topologyRoutes = listOf(route("MissingAnchor", "CH1")),
                    ),
                    joins = listOf(
                        join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                        join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                    ),
                    endpoints = listOf(
                        CabinetConnectionEndpointBinding(
                            connectionAlias = "MissingAnchor",
                            from = CabinetRouteEndpointRef(key("component:Source"), "MISSING"),
                            to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                        ),
                    ),
                ),
            ),
        )
        val missingLaneFailure = assertIs<CabinetRoutingCompilation.Failure>(
            CabinetRoutingCompiler.compile(
                CabinetRoutingRequest(
                    ir = ir(
                        routes = listOf(route("NoLane", "CH1")),
                        channels = validChannels.take(1),
                    ),
                    topology = topology(
                        channels = validChannels.take(1),
                        topologyRoutes = listOf(route("Other", "CH1")),
                    ),
                    joins = listOf(
                        join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                        join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                    ),
                    endpoints = listOf(
                        CabinetConnectionEndpointBinding(
                            connectionAlias = "NoLane",
                            from = CabinetRouteEndpointRef(key("component:Source"), "OUT"),
                            to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                        ),
                    ),
                ),
            ),
        )
        val adjacencyFailure = assertIs<CabinetRoutingCompilation.Failure>(
            CabinetRoutingCompiler.compile(
                CabinetRoutingRequest(
                    ir = ir(
                        routes = listOf(route("NoAdjacency", "CH1", "GAP")),
                        channels = validChannels,
                    ),
                    topology = topology(
                        channels = validChannels,
                        topologyRoutes = listOf(route("NoAdjacency", "CH1"), route("NoAdjacency", "GAP")),
                    ),
                    joins = listOf(
                        join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                        join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                    ),
                    endpoints = listOf(
                        CabinetConnectionEndpointBinding(
                            connectionAlias = "NoAdjacency",
                            from = CabinetRouteEndpointRef(key("component:Source"), "OUT"),
                            to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                        ),
                    ),
                ),
            ),
        )
        val bodyIntersectionFailure = assertIs<CabinetRoutingCompilation.Failure>(
            CabinetRoutingCompiler.compile(
                CabinetRoutingRequest(
                    ir = ir(
                        routes = listOf(route("Blocked", "CH1")),
                        channels = validChannels.take(1),
                    ),
                    topology = topology(
                        channels = validChannels.take(1),
                        topologyRoutes = listOf(route("Blocked", "CH1")),
                    ),
                    joins = listOf(
                        join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                        join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                        join("component:Obstacle", occurrenceId = "OBS", anchorId = "X", anchor = CabinetPointD(55.0, 110.0)),
                    ),
                    endpoints = listOf(
                        CabinetConnectionEndpointBinding(
                            connectionAlias = "Blocked",
                            from = CabinetRouteEndpointRef(key("component:Source"), "OUT"),
                            to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                        ),
                    ),
                ),
            ),
        )

        val failure = listOf(
            missingAnchorFailure,
            missingLaneFailure,
            adjacencyFailure,
            bodyIntersectionFailure,
        ).flatMap { it.diagnostics }

        assertEquals(
            setOf(
                "cabinet.route.endpoint.unbound_anchor",
                "cabinet.route.lane_assignment.missing",
                "cabinet.route.adjacency.missing",
                "cabinet.route.body_intersection",
            ),
            failure.map { diagnostic -> diagnostic.code }.toSet(),
        )
    }

    private fun ir(
        routes: List<PhysicalRouteIntentV0>,
        channels: List<PhysicalRouteChannelV0>,
    ): PhysicalInstallationIRV0 = PhysicalInstallationIRV0(
        sourceUnitId = sourceUnit,
        installationId = installationId,
        space = PhysicalInstallationSpaceV0(
            enclosure = PhysicalEnclosureV0(
                id = PhysicalObjectId("ENC"),
                size = PhysicalInstallationSize3i(220, 180, 200),
                provenance = provenance("enclosure"),
            ),
            surfaces = emptyList(),
            rails = emptyList(),
            ducts = listOf(
                PhysicalDuctV0(
                    id = PhysicalObjectId("D1"),
                    enclosureId = PhysicalObjectId("ENC"),
                    at = PhysicalPoint2i(0, 90),
                    size = PhysicalSize2i(200, 90),
                    orientation = PhysicalInfrastructureOrientation.Horizontal,
                    wall = PhysicalNonNegativeMillimeters.from(0)!!,
                    provenance = provenance("duct:D1"),
                ),
            ),
            channels = channels,
            terminalGroups = emptyList(),
            mountedOccurrences = listOf(mounted("component:Source", "SRC"), mounted("component:Target", "TGT")),
        ),
        routes = routes,
    )

    private fun topology(
        channels: List<PhysicalRouteChannelV0>,
        topologyRoutes: List<PhysicalRouteIntentV0>,
    ): com.engineeringood.athena.physical.RouteChannelTopology = assertIs<com.engineeringood.athena.physical.RouteChannelTopologyCompilation.Success>(
        RouteChannelTopologyCompiler.compile(channels = channels, routes = topologyRoutes),
    ).topology

    private fun channel(
        id: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        orientation: PhysicalInfrastructureOrientation,
    ): PhysicalRouteChannelV0 = PhysicalRouteChannelV0(
        id = PhysicalObjectId(id),
        ductId = PhysicalObjectId("D1"),
        at = PhysicalPoint2i(x, y),
        size = PhysicalSize2i(width, height),
        orientation = orientation,
        lanes = 1,
        margin = PhysicalNonNegativeMillimeters.from(2)!!,
        provenance = provenance("channel:$id"),
    )

    private fun route(alias: String, vararg channelIds: String): PhysicalRouteIntentV0 = PhysicalRouteIntentV0(
        connectionAlias = alias,
        channelIds = channelIds.map(::PhysicalObjectId),
        provenance = provenance("route:$alias"),
    )

    private fun join(
        subject: String,
        occurrenceId: String,
        anchorId: String,
        anchor: CabinetPointD,
    ): CabinetOccurrenceVisualJoin = CabinetOccurrenceVisualJoin(
        key = key(subject),
        physicalOccurrenceId = PhysicalObjectId(occurrenceId),
        representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
        transform = CabinetVisualTransform(
            id = CabinetTransformId("cabinet-transform:$subject"),
            key = key(subject),
            targetFrame = identityFrame(),
            intrinsicBounds = CabinetRectD(0.0, 0.0, 10.0, 10.0),
            footprint = CabinetSizeD(10.0, 10.0),
            orientation = PhysicalInstallationOrientation.Deg0,
        ),
        body = CabinetTransformedBody(CabinetTransformId("cabinet-transform:$subject"), CabinetRectD(anchor.x - 5.0, anchor.y - 5.0, 10.0, 10.0)),
        anchors = listOf(CabinetTransformedAnchor(anchorId, CabinetTransformId("cabinet-transform:$subject"), anchor)),
    )

    private fun mounted(subject: String, occurrenceId: String): PhysicalMountedOccurrenceV0 = PhysicalMountedOccurrenceV0(
        occurrenceId = PhysicalObjectId(occurrenceId),
        key = key(subject),
        semanticSubjectId = StableSemanticIdentity(subject),
        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("surface")),
        at = PhysicalPoint2i(0, 0),
        selectedOrientation = PhysicalInstallationOrientation.Deg0,
        contract = contract(subject),
        provenance = provenance("mount:$subject"),
    )

    private fun contract(subject: String): PhysicalInstallationContractV0 = PhysicalInstallationContractV0(
        subjectIdentity = StableSemanticIdentity(subject),
        size = PhysicalInstallationSizeV0(
            PhysicalPositiveMillimeters.from(10)!!,
            PhysicalPositiveMillimeters.from(10)!!,
            PhysicalPositiveMillimeters.from(10)!!,
        ),
        mountingTypeId = PhysicalMountingTypeId("surface"),
        allowedOrientations = setOf(PhysicalInstallationOrientation.Deg0),
        clearance = PhysicalInstallationClearanceV0(
            PhysicalNonNegativeMillimeters.from(0)!!,
            PhysicalNonNegativeMillimeters.from(0)!!,
            PhysicalNonNegativeMillimeters.from(0)!!,
            PhysicalNonNegativeMillimeters.from(0)!!,
        ),
        compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
        provenance = PhysicalInstallationContractProvenanceV0(
            field(PhysicalInstallationContractField.Width),
            field(PhysicalInstallationContractField.Height),
            field(PhysicalInstallationContractField.Depth),
            field(PhysicalInstallationContractField.MountingType),
            field(PhysicalInstallationContractField.AllowedOrientations),
            field(PhysicalInstallationContractField.ClearanceTop),
            field(PhysicalInstallationContractField.ClearanceRight),
            field(PhysicalInstallationContractField.ClearanceBottom),
            field(PhysicalInstallationContractField.ClearanceLeft),
            field(PhysicalInstallationContractField.CompatibleContainerKinds),
        ),
    )

    private fun field(field: PhysicalInstallationContractField): PhysicalInstallationContractFieldProvenance =
        PhysicalInstallationContractFieldProvenance(
            field = field,
            source = com.engineeringood.athena.physical.PhysicalContractSource(
                com.engineeringood.athena.physical.PhysicalContractSourceKind.Project,
                "test",
            ),
            span = PhysicalSourceSpan("src/main.athena", 1, 1),
        )

    private fun key(subject: String): InstallationOccurrenceKey = InstallationOccurrenceKey(
        sourceUnitId = sourceUnit,
        installationId = installationId,
        canonicalSemanticSubjectId = StableSemanticIdentity(subject),
    )

    private fun provenance(id: String): PhysicalSourceProvenance = PhysicalSourceProvenance(
        sourceUnitId = sourceUnit,
        declarationId = id,
        span = PhysicalSourceSpan("src/main.athena", 1, 1),
    )

    private fun identityFrame(): CabinetTargetFrame = CabinetTargetFrame(
        origin = CabinetPointD(0.0, 0.0),
        alongAxis = CabinetVectorD(1.0, 0.0),
        normalAxis = CabinetVectorD(0.0, 1.0),
    )

    private companion object {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
    }
}
