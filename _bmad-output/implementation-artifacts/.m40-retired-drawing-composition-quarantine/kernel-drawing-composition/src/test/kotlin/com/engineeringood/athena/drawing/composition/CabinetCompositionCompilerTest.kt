package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalEnclosure
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalContractSource
import com.engineeringood.athena.physical.PhysicalContractSourceKind
import com.engineeringood.athena.physical.PhysicalInstallationClearance
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContract
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSpace
import com.engineeringood.athena.physical.PhysicalMountingSurface
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRigidFrame2i
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalSize2i
import com.engineeringood.athena.physical.PhysicalInstallationSize
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.physical.PhysicalVector2i
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveKind
import com.engineeringood.athena.representation.GraphicMarkerKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CabinetCompositionCompilerTest {
    @Test
    fun `composes deterministic paint only cabinet graphic document`() {
        val result = CabinetCompositionCompiler.compile(
            request = CabinetCompositionRequest(
                ir = physicalIr(),
                joins = listOf(join("component:QF35", CabinetRectD(100.0, 120.0, 45.0, 90.0))),
                policy = CabinetCompositionPolicy(documentId = "m35-cabinet", padding = 20.0),
            ),
        )

        val output = assertIs<CabinetCompositionResult.Success>(result)
        val document = output.document

        assertEquals("m35-cabinet", document.documentId?.value)
        assertEquals(CabinetRectD(-20.0, -20.0, 840.0, 640.0), document.bounds!!.toCabinetRect())
        assertEquals(
            listOf(
                "frame:m35-cabinet",
                "enclosure:ENC1",
                "surface:Backplate",
                "rail:DIN1",
                "duct:D1",
                "channel:CH1",
                "terminal-group:XT1",
                "mounted-body:QF35Mount",
                "mounted-label:QF35Mount",
            ),
            document.primitives.map { primitive -> primitive.primitiveId.value },
        )
        assertEquals(
            listOf(
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.LINE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.RECTANGLE,
                GraphicPrimitiveKind.TEXT,
            ),
            document.primitives.map { primitive -> primitive.kind },
        )
        assertEquals(
            listOf(
                "physical-installation-ir",
                "cabinet-occurrence-visual-join",
                "cabinet-composition-compiler",
            ),
            document.provenanceSources,
        )
        assertEquals(0, document.forbiddenAuthorityClaims.size)
        assertEquals(9, output.evidence.primitiveCount)
        assertEquals("physical-ir+joined-representation", output.evidence.boundsAuthority)
    }

    @Test
    fun `fails before document output when join has no matching physical occurrence`() {
        val result = CabinetCompositionCompiler.compile(
            request = CabinetCompositionRequest(
                ir = physicalIr(),
                joins = listOf(join("component:OTHER", CabinetRectD(100.0, 120.0, 45.0, 90.0))),
                policy = CabinetCompositionPolicy(documentId = "m35-cabinet", padding = 20.0),
            ),
        )

        val failure = assertIs<CabinetCompositionResult.Failure>(result)
        assertEquals(listOf("cabinet.composition.join_without_physical"), failure.diagnostics.map { it.code })
    }

    @Test
    fun `composes route polylines and endpoint markers from routed physical facts`() {
        val result = CabinetCompositionCompiler.compile(
            request = CabinetCompositionRequest(
                ir = physicalIr(),
                joins = listOf(join("component:QF35", CabinetRectD(100.0, 120.0, 45.0, 90.0))),
                routes = listOf(routeFact()),
                policy = CabinetCompositionPolicy(documentId = "m35-cabinet", padding = 20.0),
            ),
        )

        val output = assertIs<CabinetCompositionResult.Success>(result)
        val primitives = output.document.primitives.associateBy { primitive -> primitive.primitiveId.value }
        val route = assertIs<GraphicPrimitive.Polyline>(primitives.getValue("route:feed_in"))
        val sourceMarker = assertIs<GraphicPrimitive.Marker>(primitives.getValue("route-endpoint:feed_in:source"))
        val targetMarker = assertIs<GraphicPrimitive.Marker>(primitives.getValue("route-endpoint:feed_in:target"))

        assertEquals(GraphicPrimitiveKind.POLYLINE, route.kind)
        assertEquals(listOf(CabinetPointD(120.0, 150.0), CabinetPointD(140.0, 150.0), CabinetPointD(140.0, 210.0)), route.points.map { point -> CabinetPointD(point.x, point.y) })
        assertEquals(GraphicMarkerKind.TERMINAL, sourceMarker.markerKind)
        assertEquals(GraphicMarkerKind.TERMINAL, targetMarker.markerKind)
        assertEquals("cabinet.route", route.styleTokenId.value)
        assertEquals("cabinet.route-endpoint", sourceMarker.styleTokenId.value)
        assertEquals("cabinet.route-endpoint", targetMarker.styleTokenId.value)
        assertEquals(0, output.document.forbiddenAuthorityClaims.size)
        assertEquals("physical-ir+joined-representation+route-facts", output.evidence.boundsAuthority)
    }

    private fun physicalIr(): PhysicalInstallationIR = PhysicalInstallationIR(
        sourceUnitId = sourceUnit,
        installationId = installationId,
        space = PhysicalInstallationSpace(
            enclosure = PhysicalEnclosure(
                id = PhysicalObjectId("ENC1"),
                size = PhysicalInstallationSize3i(800, 600, 250),
                provenance = provenance("ENC1"),
            ),
            surfaces = listOf(
                PhysicalMountingSurface(
                    id = PhysicalObjectId("Backplate"),
                    enclosureId = PhysicalObjectId("ENC1"),
                    at = PhysicalPoint2i(20, 20),
                    size = PhysicalSize2i(760, 560),
                    acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35")),
                    provenance = provenance("Backplate"),
                ),
            ),
            rails = listOf(
                PhysicalRail(
                    id = PhysicalObjectId("DIN1"),
                    surfaceId = PhysicalObjectId("Backplate"),
                    at = PhysicalPoint2i(60, 120),
                    length = PhysicalPositiveMillimeters.from(680)!!,
                    orientation = PhysicalInfrastructureOrientation.Horizontal,
                    mountingType = PhysicalMountingTypeId("din35"),
                    frame = PhysicalRigidFrame2i(PhysicalPoint2i(60, 120), PhysicalVector2i(1, 0), PhysicalVector2i(0, 1)),
                    provenance = provenance("DIN1"),
                ),
            ),
            ducts = listOf(
                PhysicalDuct(
                    id = PhysicalObjectId("D1"),
                    enclosureId = PhysicalObjectId("ENC1"),
                    at = PhysicalPoint2i(30, 60),
                    size = PhysicalSize2i(40, 480),
                    orientation = PhysicalInfrastructureOrientation.Vertical,
                    wall = PhysicalNonNegativeMillimeters.from(2)!!,
                    provenance = provenance("D1"),
                ),
            ),
            channels = listOf(
                PhysicalRouteChannel(
                    id = PhysicalObjectId("CH1"),
                    ductId = PhysicalObjectId("D1"),
                    at = PhysicalPoint2i(0, 0),
                    size = PhysicalSize2i(36, 476),
                    lanes = 4,
                    margin = PhysicalNonNegativeMillimeters.from(4)!!,
                    provenance = provenance("CH1"),
                ),
            ),
            terminalGroups = listOf(
                PhysicalTerminalGroup(
                    id = PhysicalObjectId("XT1"),
                    enclosureId = PhysicalObjectId("ENC1"),
                    at = PhysicalPoint2i(520, 420),
                    size = PhysicalSize2i(180, 50),
                    orientation = PhysicalInfrastructureOrientation.Horizontal,
                    acceptedMountingTypes = setOf(PhysicalMountingTypeId("terminal-snap")),
                    orderedOccurrenceKeys = listOf(key("component:QF35")),
                    provenance = provenance("XT1"),
                ),
            ),
            mountedOccurrences = listOf(
                physicalOccurrenceStub("QF35Mount", "component:QF35"),
            ),
        ),
        routes = emptyList(),
    )

    private fun physicalOccurrenceStub(id: String, subject: String) =
        com.engineeringood.athena.physical.PhysicalMountedOccurrence(
            occurrenceId = PhysicalObjectId(id),
            key = key(subject),
            semanticSubjectId = StableSemanticIdentity(subject),
            target = com.engineeringood.athena.physical.PhysicalMountTargetRef.Rail(PhysicalObjectId("DIN1")),
            at = PhysicalPoint2i(100, 0),
            selectedOrientation = com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg0,
            contract = contract(subject),
            provenance = provenance(id),
        )

    private fun contract(subject: String): PhysicalInstallationContract {
        val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$subject")
        val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
            PhysicalInstallationContractFieldProvenance(field, source, null)
        }
        return PhysicalInstallationContract(
            subjectIdentity = StableSemanticIdentity(subject),
            size = PhysicalInstallationSize(
                width = PhysicalPositiveMillimeters.from(45)!!,
                height = PhysicalPositiveMillimeters.from(90)!!,
                depth = PhysicalPositiveMillimeters.from(70)!!,
            ),
            mountingTypeId = PhysicalMountingTypeId("din35"),
            allowedOrientations = setOf(com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg0),
            clearance = PhysicalInstallationClearance(
                top = PhysicalNonNegativeMillimeters.from(0)!!,
                right = PhysicalNonNegativeMillimeters.from(0)!!,
                bottom = PhysicalNonNegativeMillimeters.from(0)!!,
                left = PhysicalNonNegativeMillimeters.from(0)!!,
            ),
            compatibleContainerKinds = setOf(PhysicalContainerKindId("cabinet")),
            provenance = PhysicalInstallationContractProvenance(
                width = provenance.getValue(PhysicalInstallationContractField.Width),
                height = provenance.getValue(PhysicalInstallationContractField.Height),
                depth = provenance.getValue(PhysicalInstallationContractField.Depth),
                mountingType = provenance.getValue(PhysicalInstallationContractField.MountingType),
                allowedOrientations = provenance.getValue(PhysicalInstallationContractField.AllowedOrientations),
                clearanceTop = provenance.getValue(PhysicalInstallationContractField.ClearanceTop),
                clearanceRight = provenance.getValue(PhysicalInstallationContractField.ClearanceRight),
                clearanceBottom = provenance.getValue(PhysicalInstallationContractField.ClearanceBottom),
                clearanceLeft = provenance.getValue(PhysicalInstallationContractField.ClearanceLeft),
                compatibleContainerKinds = provenance.getValue(
                    PhysicalInstallationContractField.CompatibleContainerKinds,
                ),
            ),
        )
    }

    private fun join(subject: String, bounds: CabinetRectD): CabinetOccurrenceVisualJoin =
        CabinetOccurrenceVisualJoin(
            key = key(subject),
            physicalOccurrenceId = PhysicalObjectId("QF35Mount"),
            representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
            transform = CabinetVisualTransform(
                id = CabinetTransformId("cabinet-transform:$subject"),
                key = key(subject),
                targetFrame = CabinetTargetFrame(CabinetPointD(0.0, 0.0), CabinetVectorD(1.0, 0.0), CabinetVectorD(0.0, 1.0)),
                intrinsicBounds = CabinetRectD(0.0, 0.0, 20.0, 20.0),
                footprint = CabinetSizeD(bounds.width, bounds.height),
                orientation = com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg0,
            ),
            body = CabinetTransformedBody(CabinetTransformId("cabinet-transform:$subject"), bounds),
            anchors = emptyList(),
        )

    private fun routeFact(): CabinetRouteFact = CabinetRouteFact(
        connectionAlias = "feed_in",
        orderedChannelIds = listOf(PhysicalObjectId("CH1")),
        from = CabinetRouteEndpointPoint(
            key = key("component:QF35"),
            anchorId = "L1",
            point = CabinetPointD(120.0, 150.0),
        ),
        to = CabinetRouteEndpointPoint(
            key = key("component:QF35"),
            anchorId = "T1",
            point = CabinetPointD(140.0, 210.0),
        ),
        laneCentersByChannel = mapOf(PhysicalObjectId("CH1") to com.engineeringood.athena.physical.ExactMillimeters(10, 1)),
        segments = listOf(
            CabinetRouteSegment(CabinetPointD(120.0, 150.0), CabinetPointD(140.0, 150.0)),
            CabinetRouteSegment(CabinetPointD(140.0, 150.0), CabinetPointD(140.0, 210.0)),
        ),
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

    private fun com.engineeringood.athena.representation.GraphicBounds.toCabinetRect(): CabinetRectD =
        CabinetRectD(x, y, width, height)

    private companion object {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
    }
}
