package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetConnectionEndpointBinding
import com.engineeringood.athena.drawing.composition.CabinetOccurrenceVisualJoin
import com.engineeringood.athena.drawing.composition.CabinetPointD
import com.engineeringood.athena.drawing.composition.CabinetRectD
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceId
import com.engineeringood.athena.drawing.composition.CabinetRouteEndpointRef
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompilation
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompiler
import com.engineeringood.athena.drawing.composition.CabinetRoutingRequest
import com.engineeringood.athena.drawing.composition.CabinetSizeD
import com.engineeringood.athena.drawing.composition.CabinetTargetFrame
import com.engineeringood.athena.drawing.composition.CabinetTransformedAnchor
import com.engineeringood.athena.drawing.composition.CabinetTransformedBody
import com.engineeringood.athena.drawing.composition.CabinetTransformId
import com.engineeringood.athena.drawing.composition.CabinetVectorD
import com.engineeringood.athena.drawing.composition.CabinetVisualTransform
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.physical.ExactMillimeters
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalEnclosure
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationClearance
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContract
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSize
import com.engineeringood.athena.physical.PhysicalInstallationSpace
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalRouteIntent
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.RouteChannelTopologyCompiler
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.RouteBundle
import com.engineeringood.athena.routing.RouteBundleId
import com.engineeringood.athena.routing.RouteBundleMember
import com.engineeringood.athena.routing.RouteIntent
import com.engineeringood.athena.routing.RouteIntentCompilation
import com.engineeringood.athena.routing.RouteIntentConstraint
import com.engineeringood.athena.routing.RouteIntentConstraintId
import com.engineeringood.athena.routing.RouteIntentConstraintKind
import com.engineeringood.athena.routing.RouteIntentConstraintOwner
import com.engineeringood.athena.routing.RouteIntentConstraintStrength
import com.engineeringood.athena.routing.RouteIntentConstraintTarget
import com.engineeringood.athena.routing.RouteIntentId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CabinetRouteRealizationCompilerTest {
    @Test
    fun `realizes default cabinet route channel sequence through passable adjacency`() {
        val request = request(
        routes = listOf(route("Main")),
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
        )

        val realized = assertIs<CabinetRouteRealizationCompilation.Success>(
            CabinetRouteRealizationCompiler.compile(request),
        ).ir

        val route = realized.routes.single()
        assertEquals(listOf(PhysicalObjectId("CH1"), PhysicalObjectId("CH2")), route.channelIds)

        val topology = assertIs<com.engineeringood.athena.physical.RouteChannelTopologyCompilation.Success>(
            RouteChannelTopologyCompiler.compile(realized.space.channels, realized.routes),
        ).topology
        val routing = assertIs<CabinetRoutingCompilation.Success>(
            CabinetRoutingCompiler.compile(
                CabinetRoutingRequest(
                    ir = realized,
                    topology = topology,
                    joins = request.joins,
                    endpoints = request.endpoints,
                ),
            ),
        )

        assertEquals(1, routing.routes.size)
        assertEquals("Main", routing.routes.single().connectionAlias)
    }

    @Test
    fun `fails closed when a required cabinet channel is unresolved`() {
        val request = request(
            routes = listOf(route("Blocked", "MISSING_CHANNEL")),
            joins = listOf(
                join("component:Source", occurrenceId = "SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                join("component:Target", occurrenceId = "TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                join(
                    "component:Obstacle",
                    occurrenceId = "OBS",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 125.0),
                    body = CabinetRectD(51.0, 120.0, 10.0, 10.0),
                ),
                join(
                    "component:ObstacleAbove",
                    occurrenceId = "OBS_ABOVE",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 109.0),
                    body = CabinetRectD(51.0, 104.0, 10.0, 10.0),
                ),
                join(
                    "component:ObstacleBelow",
                    occurrenceId = "OBS_BELOW",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 136.0),
                    body = CabinetRectD(51.0, 131.0, 10.0, 10.0),
                ),
            ),
            endpoints = listOf(
                CabinetConnectionEndpointBinding(
                    connectionAlias = "Blocked",
                    from = CabinetRouteEndpointRef(key("component:Source"), "OUT"),
                    to = CabinetRouteEndpointRef(key("component:Target"), "IN"),
                ),
            ),
            extraMountedOccurrences = listOf(
                mounted(
                    subject = "component:Obstacle",
                    occurrenceId = "OBS",
                    clearance = PhysicalInstallationClearance(
                        top = PhysicalNonNegativeMillimeters.from(20)!!,
                        right = PhysicalNonNegativeMillimeters.from(0)!!,
                        bottom = PhysicalNonNegativeMillimeters.from(0)!!,
                        left = PhysicalNonNegativeMillimeters.from(0)!!,
                    ),
                ),
            ),
        )

        val failure = assertIs<CabinetRouteRealizationCompilation.Failure>(
            CabinetRouteRealizationCompiler.compile(request),
        )

        assertEquals(
            listOf("cabinet.route.realization.constraint.unresolved"),
            failure.diagnostics.map { diagnostic -> diagnostic.code },
        )
    }

    @Test
    fun `validates candidate route sequences in isolation`() {
        val request = request(
            routes = listOf(route("Good"), route("Bad", "MISSING_CHANNEL")),
            joins = listOf(
                join("component:GoodSource", occurrenceId = "GOOD_SRC", anchorId = "OUT", anchor = CabinetPointD(70.0, 110.0)),
                join("component:GoodTarget", occurrenceId = "GOOD_TGT", anchorId = "IN", anchor = CabinetPointD(85.0, 110.0)),
                join("component:BadSource", occurrenceId = "BAD_SRC", anchorId = "OUT", anchor = CabinetPointD(20.0, 110.0)),
                join("component:BadTarget", occurrenceId = "BAD_TGT", anchorId = "IN", anchor = CabinetPointD(100.0, 180.0)),
                join(
                    "component:Obstacle",
                    occurrenceId = "OBS",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 125.0),
                    body = CabinetRectD(51.0, 120.0, 10.0, 10.0),
                ),
                join(
                    "component:ObstacleAbove",
                    occurrenceId = "OBS_ABOVE",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 109.0),
                    body = CabinetRectD(51.0, 104.0, 10.0, 10.0),
                ),
                join(
                    "component:ObstacleBelow",
                    occurrenceId = "OBS_BELOW",
                    anchorId = "X",
                    anchor = CabinetPointD(56.0, 136.0),
                    body = CabinetRectD(51.0, 131.0, 10.0, 10.0),
                ),
            ),
            endpoints = listOf(
                CabinetConnectionEndpointBinding(
                    connectionAlias = "Good",
                    from = CabinetRouteEndpointRef(key("component:GoodSource"), "OUT"),
                    to = CabinetRouteEndpointRef(key("component:GoodTarget"), "IN"),
                ),
                CabinetConnectionEndpointBinding(
                    connectionAlias = "Bad",
                    from = CabinetRouteEndpointRef(key("component:BadSource"), "OUT"),
                    to = CabinetRouteEndpointRef(key("component:BadTarget"), "IN"),
                ),
            ),
            extraMountedOccurrences = listOf(
                mounted("component:GoodSource", "GOOD_SRC"),
                mounted("component:GoodTarget", "GOOD_TGT"),
                mounted("component:BadSource", "BAD_SRC"),
                mounted("component:BadTarget", "BAD_TGT"),
                mounted(
                    subject = "component:Obstacle",
                    occurrenceId = "OBS",
                    clearance = PhysicalInstallationClearance(
                        top = PhysicalNonNegativeMillimeters.from(20)!!,
                        right = PhysicalNonNegativeMillimeters.from(0)!!,
                        bottom = PhysicalNonNegativeMillimeters.from(0)!!,
                        left = PhysicalNonNegativeMillimeters.from(0)!!,
                    ),
                ),
            ),
        )

        val failure = assertIs<CabinetRouteRealizationCompilation.Failure>(
            CabinetRouteRealizationCompiler.compile(request),
        )

        assertEquals(listOf("Bad"), failure.diagnostics.map { diagnostic -> diagnostic.subject })
    }

    private fun request(
        routes: List<PhysicalRouteIntent>,
        joins: List<CabinetOccurrenceVisualJoin>,
        endpoints: List<CabinetConnectionEndpointBinding>,
        extraMountedOccurrences: List<PhysicalMountedOccurrence> = emptyList(),
    ): CabinetRouteRealizationRequest = CabinetRouteRealizationRequest(
        physicalIr = PhysicalInstallationIR(
            sourceUnitId = sourceUnit,
            installationId = installationId,
            space = PhysicalInstallationSpace(
                enclosure = PhysicalEnclosure(
                    id = PhysicalObjectId("ENC"),
                    size = PhysicalInstallationSize3i(220, 180, 200),
                    provenance = provenance("enclosure"),
                ),
                surfaces = emptyList(),
                rails = emptyList(),
                ducts = listOf(
                    PhysicalDuct(
                        id = PhysicalObjectId("D1"),
                        enclosureId = PhysicalObjectId("ENC"),
                        at = PhysicalPoint2i(0, 90),
                        size = PhysicalSize2i(200, 90),
                        orientation = PhysicalInfrastructureOrientation.Horizontal,
                        wall = PhysicalNonNegativeMillimeters.from(0)!!,
                        provenance = provenance("duct:D1"),
                    ),
                ),
                channels = listOf(
                    channel("CH1", x = 20, y = 10, width = 70, height = 20, orientation = PhysicalInfrastructureOrientation.Horizontal),
                    channel("CH2", x = 90, y = 10, width = 20, height = 80, orientation = PhysicalInfrastructureOrientation.Vertical),
                ),
                terminalGroups = emptyList(),
                mountedOccurrences = listOf(
                    mounted("component:Source", "SRC"),
                    mounted("component:Target", "TGT"),
                ) + extraMountedOccurrences,
            ),
            routes = routes,
        ),
        routeIntents = routeIntentCompilation(routes),
        joins = joins,
        endpoints = endpoints,
        enclosureToDrawing = CabinetTargetFrame(
            origin = CabinetPointD(0.0, 0.0),
            alongAxis = CabinetVectorD(1.0, 0.0),
            normalAxis = CabinetVectorD(0.0, 1.0),
        ),
    )

    private fun routeIntentCompilation(routes: List<PhysicalRouteIntent>): RouteIntentCompilation {
        val intents = routes.map { route ->
            val provenance = SourceProvenance("fixture.athena", 1, 1, 1, 2)
            val connectionId = ElectricalConnectionId("connection:${route.connectionAlias}")
            RouteIntent(
                intentId = RouteIntentId("route:${connectionId.value}"),
                connectionId = connectionId,
                sourcePortReference = EngineeringReference(
                    listOf("Source", "out"),
                    StableSemanticIdentity("port:Source.out"),
                    provenance,
                ),
                targetPortReference = EngineeringReference(
                    listOf("Target", "in"),
                    StableSemanticIdentity("port:Target.in"),
                    provenance,
                ),
                constraints = route.channelIds.mapIndexed { index, channelId ->
                    RouteIntentConstraint(
                        constraintId = RouteIntentConstraintId("through:${route.connectionAlias}:$index"),
                        kind = RouteIntentConstraintKind.THROUGH,
                        owner = RouteIntentConstraintOwner.PHYSICAL,
                        strength = RouteIntentConstraintStrength.REQUIRED,
                        target = RouteIntentConstraintTarget.Reference(
                            EngineeringReference(
                                listOf(channelId.value),
                                StableSemanticIdentity("physical-channel:${channelId.value}"),
                                provenance,
                            ),
                        ),
                        provenance = provenance,
                    )
                },
                compatibilityEvidence = emptyList(),
                provenance = provenance,
            )
        }
        val bundles = intents.map { intent ->
            RouteBundle(
                bundleId = RouteBundleId("bundle:${intent.connectionId.value}"),
                members = listOf(RouteBundleMember(intent.intentId, intent.connectionId, intent.provenance)),
                constraints = emptyList(),
                provenance = intent.provenance,
            )
        }
        return RouteIntentCompilation.canonical(
            routeIntents = intents,
            routeBundles = bundles,
            provenance = intents.firstOrNull()?.provenance ?: SourceProvenance("fixture.athena", 1, 1, 1, 2),
        )
    }

    private fun channel(
        id: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        orientation: PhysicalInfrastructureOrientation,
    ): PhysicalRouteChannel = PhysicalRouteChannel(
        id = PhysicalObjectId(id),
        ductId = PhysicalObjectId("D1"),
        at = PhysicalPoint2i(x, y),
        size = PhysicalSize2i(width, height),
        orientation = orientation,
        lanes = 1,
        margin = PhysicalNonNegativeMillimeters.from(2)!!,
        provenance = provenance("channel:$id"),
    )

    private fun route(alias: String, vararg channelIds: String): PhysicalRouteIntent = PhysicalRouteIntent(
        connectionAlias = alias,
        channelIds = channelIds.map(::PhysicalObjectId),
        provenance = provenance("route:$alias"),
    )

    private fun join(
        subject: String,
        occurrenceId: String,
        anchorId: String,
        anchor: CabinetPointD,
        body: CabinetRectD = CabinetRectD(anchor.x - 5.0, anchor.y - 5.0, 10.0, 10.0),
    ): CabinetOccurrenceVisualJoin = CabinetOccurrenceVisualJoin(
        key = key(subject),
        physicalOccurrenceId = PhysicalObjectId(occurrenceId),
        representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
        transform = CabinetVisualTransform(
            id = CabinetTransformId("cabinet-transform:$subject"),
            key = key(subject),
            targetFrame = CabinetTargetFrame(
                origin = CabinetPointD(0.0, 0.0),
                alongAxis = CabinetVectorD(1.0, 0.0),
                normalAxis = CabinetVectorD(0.0, 1.0),
            ),
            intrinsicBounds = CabinetRectD(0.0, 0.0, 10.0, 10.0),
            footprint = CabinetSizeD(10.0, 10.0),
            orientation = PhysicalInstallationOrientation.Deg0,
        ),
        body = CabinetTransformedBody(CabinetTransformId("cabinet-transform:$subject"), body),
        anchors = listOf(CabinetTransformedAnchor(anchorId, CabinetTransformId("cabinet-transform:$subject"), anchor)),
    )

    private fun mounted(
        subject: String,
        occurrenceId: String,
        clearance: PhysicalInstallationClearance = PhysicalInstallationClearance(
            top = PhysicalNonNegativeMillimeters.from(0)!!,
            right = PhysicalNonNegativeMillimeters.from(0)!!,
            bottom = PhysicalNonNegativeMillimeters.from(0)!!,
            left = PhysicalNonNegativeMillimeters.from(0)!!,
        ),
    ): PhysicalMountedOccurrence = PhysicalMountedOccurrence(
        occurrenceId = PhysicalObjectId(occurrenceId),
        key = key(subject),
        semanticSubjectId = StableSemanticIdentity(subject),
        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("surface")),
        at = PhysicalPoint2i(0, 0),
        selectedOrientation = PhysicalInstallationOrientation.Deg0,
        contract = contract(subject, clearance),
        provenance = provenance("mount:$subject"),
    )

    private fun contract(
        subject: String,
        clearance: PhysicalInstallationClearance,
    ): PhysicalInstallationContract = PhysicalInstallationContract(
        subjectIdentity = StableSemanticIdentity(subject),
        size = PhysicalInstallationSize(
            PhysicalPositiveMillimeters.from(10)!!,
            PhysicalPositiveMillimeters.from(10)!!,
            PhysicalPositiveMillimeters.from(10)!!,
        ),
        mountingTypeId = PhysicalMountingTypeId("surface"),
        allowedOrientations = setOf(PhysicalInstallationOrientation.Deg0),
        clearance = clearance,
        compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
        provenance = PhysicalInstallationContractProvenance(
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

    private companion object {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
    }
}
