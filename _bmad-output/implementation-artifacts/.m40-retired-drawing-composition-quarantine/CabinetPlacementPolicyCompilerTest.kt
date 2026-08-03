package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetIntrinsicAnchor
import com.engineeringood.athena.drawing.composition.CabinetPointD
import com.engineeringood.athena.drawing.composition.CabinetRectD
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceId
import com.engineeringood.athena.drawing.composition.CabinetRepresentationOccurrenceInput
import com.engineeringood.athena.drawing.composition.CabinetSizeD
import com.engineeringood.athena.drawing.composition.CabinetTargetFrame
import com.engineeringood.athena.drawing.composition.CabinetVectorD
import com.engineeringood.athena.drawing.composition.CabinetPhysicalOccurrenceInput
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutConstraintOwner
import com.engineeringood.athena.layout.LayoutConstraintStrength
import com.engineeringood.athena.layout.LayoutGraphAnchor
import com.engineeringood.athena.layout.LayoutGraphBounds
import com.engineeringood.athena.layout.LayoutGraphConstraintKind
import com.engineeringood.athena.layout.LayoutGraphConstraint
import com.engineeringood.athena.layout.LayoutGraphObjectId
import com.engineeringood.athena.layout.LayoutGraphOccurrence
import com.engineeringood.athena.layout.LayoutGraphPoint
import com.engineeringood.athena.layout.LayoutGraphPort
import com.engineeringood.athena.layout.LayoutGraphProvenance
import com.engineeringood.athena.layout.LayoutGraphRelationshipKind
import com.engineeringood.athena.layout.LayoutGraphRelationship
import com.engineeringood.athena.layout.LayoutGraphSnapshotId
import com.engineeringood.athena.layout.LayoutGraphSourceUnitId
import com.engineeringood.athena.layout.LayoutGraph
import com.engineeringood.athena.physical.ExactMillimeters
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalContainerKindId
import com.engineeringood.athena.physical.PhysicalContractSource
import com.engineeringood.athena.physical.PhysicalContractSourceKind
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalEnclosure
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationClearance
import com.engineeringood.athena.physical.PhysicalInstallationContractField
import com.engineeringood.athena.physical.PhysicalInstallationContractFieldProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContractProvenance
import com.engineeringood.athena.physical.PhysicalInstallationContract
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInstallationOrientation as PhysicalMountOrientation
import com.engineeringood.athena.physical.PhysicalInstallationSize3i
import com.engineeringood.athena.physical.PhysicalInstallationSpace
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalMountingSurface
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalNonNegativeMillimeters
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalPositiveMillimeters
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRigidFrame2i
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.physical.PhysicalVector2i
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CabinetPlacementPolicyCompilerTest {
    @Test
    fun `rejects occurrence mismatch as unplaced placement evidence`() {
        val request = request(placements = emptyList())

        val failure = assertIs<CabinetPlacementPolicyCompilation.Failure>(CabinetPlacementPolicyCompiler.evaluate(request))
        assertTrue(failure.diagnostics.any { diagnostic -> diagnostic.code == "cabinet.placement.policy.occurrence.mismatch" })
    }

    @Test
    fun `rejects overlap containment orientation clearance and bounds violations from physical policy`() {
        val placementResult = assertIs<CabinetPlacementCompilation.Success>(
            CabinetPlacementCompiler.compile(request().toPlacementRequest()),
        )

        val failure = assertIs<CabinetPlacementPolicyCompilation.Failure>(
            CabinetPlacementPolicyCompiler.evaluate(
                CabinetPlacementPolicyRequest(
                    layoutGraph = request().layoutGraph,
                    physicalIr = badPhysicalIr(),
                    placements = placementResult.placements,
                ),
            ),
        )

        val codes = failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet()
        assertTrue("cabinet.placement.policy.physical.constraint.occurrence.collision" in codes)
        assertTrue("cabinet.placement.policy.physical.constraint.clearance.collision" in codes)
        assertTrue("cabinet.placement.policy.physical.constraint.orientation.not_allowed" in codes)
        assertTrue("cabinet.placement.policy.physical.constraint.depth.exceeds_enclosure" in codes)
        assertTrue("cabinet.placement.policy.physical.constraint.occurrence.outside_enclosure" in codes)
    }

    @Test
    fun `validates proposed placement geometry instead of the original physical occurrence`() {
        val validRequest = request(placements = null)
        val proposal = validRequest.placements.single()
        val movedOutside = proposal.copy(
            proposedPhysicalOccurrence = proposal.proposedPhysicalOccurrence.copy(
                targetLocalPosition = CabinetPointD(100.0, 100.0),
            ),
        )

        val failure = assertIs<CabinetPlacementPolicyCompilation.Failure>(
            CabinetPlacementPolicyCompiler.evaluate(validRequest.copy(placements = listOf(movedOutside))),
        )

        assertTrue(
            failure.diagnostics.any { diagnostic ->
                diagnostic.code == "cabinet.placement.policy.physical.constraint.occurrence.outside_target"
            },
        )
    }

    private fun request(placements: List<CabinetPlacementFact>? = null): CabinetPlacementPolicyRequest {
        val placementRequest = request().toPlacementRequest()
        val placementResult = assertIs<CabinetPlacementCompilation.Success>(CabinetPlacementCompiler.compile(placementRequest))
        return CabinetPlacementPolicyRequest(
            layoutGraph = placementRequest.layoutGraph,
            physicalIr = placementRequest.layoutGraph.toPhysicalIrForPolicy(),
            placements = placements ?: placementResult.placements,
        )
    }

    private fun request(): PlacementFixture = PlacementFixture(
        layoutGraph = layoutGraph(),
        physicalOccurrence = physicalOccurrence("component:A"),
        representationOccurrence = representationOccurrence("component:A"),
    )

    private fun PlacementFixture.toPlacementRequest(): CabinetPlacementRequest =
        CabinetPlacementRequest(
            layoutGraph = layoutGraph,
            plannerId = "athena-native",
            plannerVersion = "1.0.0",
            physicalOccurrences = listOf(physicalOccurrence),
            representationOccurrences = listOf(representationOccurrence),
            enclosureToDrawing = CabinetTargetFrame(
                origin = CabinetPointD(0.0, 0.0),
                alongAxis = CabinetVectorD(1.0, 0.0),
                normalAxis = CabinetVectorD(0.0, 1.0),
            ),
        )

    private fun layoutGraph(): LayoutGraph {
        val sourceUnit = LayoutGraphSourceUnitId("src/main.athena")
        val provenance = LayoutGraphProvenance(sourceUnitId = sourceUnit, declarationId = "layout:component:A", span = null)
        return LayoutGraph.canonical(
            snapshotId = LayoutGraphSnapshotId("layout:src/main.athena:MainCabinet:snapshot-a"),
            sourceUnitId = sourceUnit,
            installationId = "MainCabinet",
            compilerSnapshotId = "compiler:snapshot-a",
            occurrences = listOf(
                LayoutGraphOccurrence(
                    occurrenceId = LayoutGraphObjectId("occurrence:component:A"),
                    semanticSubjectId = "component:A",
                    physicalOccurrenceId = "physical:A",
                    bounds = LayoutGraphBounds(20, 20, 60, 40),
                    representationBounds = LayoutGraphBounds(0, 0, 60, 40),
                    ports = listOf(
                        LayoutGraphPort(
                            portSemanticId = "component:A.port.L1",
                            terminalIdentity = "XT1.1",
                            anchorId = "anchor-l1",
                            direction = "in",
                            signal = "power",
                            required = true,
                            provenance = provenance,
                        ),
                    ),
                    anchors = listOf(
                        LayoutGraphAnchor(
                            anchorId = "anchor-l1",
                            geometryRef = "terminal-l1",
                            primitiveId = "body",
                            point = LayoutGraphPoint(10, 20),
                            role = "terminal",
                            required = true,
                            provenance = provenance,
                        ),
                    ),
                    constraints = listOf(
                        LayoutGraphConstraint(
                            constraintId = LayoutGraphObjectId("layout:component:A:port:component:A.port.L1"),
                            owner = LayoutConstraintOwner.SEMANTIC,
                            strength = LayoutConstraintStrength.REQUIRED,
                            kind = LayoutGraphConstraintKind.PORT_ANCHOR_BINDING,
                            subjectId = "component:A.port.L1",
                            targetId = "anchor-l1",
                            note = "XT1.1",
                            provenance = provenance,
                        ),
                    ),
                    provenance = provenance,
                ),
            ),
            obstacles = emptyList(),
            relationships = listOf(
                LayoutGraphRelationship(
                    relationshipId = LayoutGraphObjectId("layout:component:A:containment"),
                    kind = LayoutGraphRelationshipKind.CONTAINMENT,
                    sourceId = "occurrence:component:A",
                    targetId = "enclosure:ENC1",
                    provenance = provenance,
                ),
            ),
            constraints = emptyList(),
        )
    }

    private fun badPhysicalIr(): PhysicalInstallationIR {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
        val installationId = PhysicalInstallationId("MainCabinet")
        val contract = contract("component:A")
        return PhysicalInstallationIR(
            sourceUnitId = sourceUnit,
            installationId = installationId,
            space = PhysicalInstallationSpace(
                enclosure = PhysicalEnclosure(
                    id = PhysicalObjectId("ENC1"),
                    size = PhysicalInstallationSize3i(120, 80, 40),
                    provenance = provenance(sourceUnit, "ENC1"),
                ),
                surfaces = listOf(
                    PhysicalMountingSurface(
                        id = PhysicalObjectId("Backplate"),
                        enclosureId = PhysicalObjectId("ENC1"),
                        at = PhysicalPoint2i(0, 0),
                        size = com.engineeringood.athena.physical.PhysicalSize2i(120, 80),
                        acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35")),
                        provenance = provenance(sourceUnit, "Backplate"),
                    ),
                ),
                rails = listOf(
                    PhysicalRail(
                        id = PhysicalObjectId("DIN1"),
                        surfaceId = PhysicalObjectId("Backplate"),
                        at = PhysicalPoint2i(10, 20),
                        length = PhysicalPositiveMillimeters.from(60)!!,
                        orientation = PhysicalInfrastructureOrientation.Horizontal,
                        mountingType = PhysicalMountingTypeId("din35"),
                        frame = PhysicalRigidFrame2i(PhysicalPoint2i(10, 20), PhysicalVector2i(1, 0), PhysicalVector2i(0, 1)),
                        provenance = provenance(sourceUnit, "DIN1"),
                    ),
                ),
                ducts = listOf(
                    PhysicalDuct(
                        id = PhysicalObjectId("D1"),
                        enclosureId = PhysicalObjectId("ENC1"),
                        at = PhysicalPoint2i(0, 0),
                        size = com.engineeringood.athena.physical.PhysicalSize2i(20, 20),
                        orientation = PhysicalInfrastructureOrientation.Horizontal,
                        wall = PhysicalNonNegativeMillimeters.from(2)!!,
                        provenance = provenance(sourceUnit, "D1"),
                    ),
                ),
                channels = listOf(
                    PhysicalRouteChannel(
                        id = PhysicalObjectId("CH1"),
                        ductId = PhysicalObjectId("D1"),
                        at = PhysicalPoint2i(0, 0),
                        size = com.engineeringood.athena.physical.PhysicalSize2i(16, 16),
                        lanes = 2,
                        margin = PhysicalNonNegativeMillimeters.from(2)!!,
                        provenance = provenance(sourceUnit, "CH1"),
                    ),
                ),
                terminalGroups = listOf(
                    PhysicalTerminalGroup(
                        id = PhysicalObjectId("XT1"),
                        enclosureId = PhysicalObjectId("ENC1"),
                        at = PhysicalPoint2i(90, 50),
                        size = com.engineeringood.athena.physical.PhysicalSize2i(20, 20),
                        orientation = PhysicalInfrastructureOrientation.Horizontal,
                        acceptedMountingTypes = setOf(PhysicalMountingTypeId("terminal-snap")),
                        orderedOccurrenceKeys = emptyList(),
                        provenance = provenance(sourceUnit, "XT1"),
                    ),
                ),
                mountedOccurrences = listOf(
                    PhysicalMountedOccurrence(
                        occurrenceId = PhysicalObjectId("M1"),
                        key = key(sourceUnit, installationId, "component:A"),
                        semanticSubjectId = StableSemanticIdentity("component:A"),
                        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                        at = PhysicalPoint2i(5, 5),
                        selectedOrientation = PhysicalMountOrientation.Deg90,
                        contract = contract.copy(
                            allowedOrientations = setOf(PhysicalMountOrientation.Deg0),
                            clearance = PhysicalInstallationClearance(
                                top = PhysicalNonNegativeMillimeters.from(2)!!,
                                right = PhysicalNonNegativeMillimeters.from(2)!!,
                                bottom = PhysicalNonNegativeMillimeters.from(2)!!,
                                left = PhysicalNonNegativeMillimeters.from(2)!!,
                            ),
                        ),
                        provenance = provenance(sourceUnit, "M1"),
                    ),
                    PhysicalMountedOccurrence(
                        occurrenceId = PhysicalObjectId("M2"),
                        key = key(sourceUnit, installationId, "component:B"),
                        semanticSubjectId = StableSemanticIdentity("component:B"),
                        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                        at = PhysicalPoint2i(8, 8),
                        selectedOrientation = PhysicalMountOrientation.Deg0,
                        contract = contract.copy(
                            subjectIdentity = StableSemanticIdentity("component:B"),
                            size = contract.size.copy(
                                depth = PhysicalPositiveMillimeters.from(60)!!,
                            ),
                        ),
                        provenance = provenance(sourceUnit, "M2"),
                    ),
                    PhysicalMountedOccurrence(
                        occurrenceId = PhysicalObjectId("M3"),
                        key = key(sourceUnit, installationId, "component:C"),
                        semanticSubjectId = StableSemanticIdentity("component:C"),
                        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                        at = PhysicalPoint2i(100, 60),
                        selectedOrientation = PhysicalMountOrientation.Deg0,
                        contract = contract.copy(
                            subjectIdentity = StableSemanticIdentity("component:C"),
                        ),
                        provenance = provenance(sourceUnit, "M3"),
                    ),
                ),
            ),
            routes = emptyList(),
        )
    }

    private fun contract(subject: String): PhysicalInstallationContract {
        val source = PhysicalContractSource(PhysicalContractSourceKind.Project, "project:$subject")
        val provenance = PhysicalInstallationContractField.entries.associateWith { field ->
            PhysicalInstallationContractFieldProvenance(field, source, PhysicalSourceSpan("src/main.athena", 1, 1))
        }
        return PhysicalInstallationContract(
            subjectIdentity = StableSemanticIdentity(subject),
            size = com.engineeringood.athena.physical.PhysicalInstallationSize(
                width = PhysicalPositiveMillimeters.from(30)!!,
                height = PhysicalPositiveMillimeters.from(30)!!,
                depth = PhysicalPositiveMillimeters.from(35)!!,
            ),
            mountingTypeId = PhysicalMountingTypeId("din35"),
            allowedOrientations = setOf(PhysicalMountOrientation.Deg0),
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
                compatibleContainerKinds = provenance.getValue(PhysicalInstallationContractField.CompatibleContainerKinds),
            ),
        )
    }

    private fun key(sourceUnit: PhysicalSourceUnitId, installationId: PhysicalInstallationId, subject: String): InstallationOccurrenceKey =
        InstallationOccurrenceKey(
            sourceUnitId = sourceUnit,
            installationId = installationId,
            canonicalSemanticSubjectId = StableSemanticIdentity(subject),
        )

    private fun provenance(sourceUnitId: PhysicalSourceUnitId, declarationId: String): PhysicalSourceProvenance =
        PhysicalSourceProvenance(
            sourceUnitId = sourceUnitId,
            declarationId = declarationId,
            span = PhysicalSourceSpan("src/main.athena", 1, 1),
        )

    private fun LayoutGraph.toPhysicalIrForPolicy(): PhysicalInstallationIR {
        val sourceUnit = PhysicalSourceUnitId(sourceUnitId.value)
        val installationId = PhysicalInstallationId(this.installationId)
        return PhysicalInstallationIR(
            sourceUnitId = sourceUnit,
            installationId = installationId,
            space = PhysicalInstallationSpace(
                enclosure = PhysicalEnclosure(
                    id = PhysicalObjectId("ENC1"),
                    size = PhysicalInstallationSize3i(120, 80, 40),
                    provenance = provenance(sourceUnit, "ENC1"),
                ),
                surfaces = listOf(
                    PhysicalMountingSurface(
                        id = PhysicalObjectId("Backplate"),
                        enclosureId = PhysicalObjectId("ENC1"),
                        at = PhysicalPoint2i(0, 0),
                        size = com.engineeringood.athena.physical.PhysicalSize2i(120, 80),
                        acceptedMountingTypes = setOf(PhysicalMountingTypeId("din35")),
                        provenance = provenance(sourceUnit, "Backplate"),
                    ),
                ),
                rails = emptyList(),
                ducts = emptyList(),
                channels = emptyList(),
                terminalGroups = emptyList(),
                mountedOccurrences = listOf(
                    PhysicalMountedOccurrence(
                        occurrenceId = PhysicalObjectId("physical:A"),
                        key = key(sourceUnit, installationId, "component:A"),
                        semanticSubjectId = StableSemanticIdentity("component:A"),
                        target = PhysicalMountTargetRef.Surface(PhysicalObjectId("Backplate")),
                        at = PhysicalPoint2i(5, 5),
                        selectedOrientation = PhysicalMountOrientation.Deg0,
                        contract = contract("component:A").copy(
                            size = com.engineeringood.athena.physical.PhysicalInstallationSize(
                                width = PhysicalPositiveMillimeters.from(60)!!,
                                height = PhysicalPositiveMillimeters.from(40)!!,
                                depth = PhysicalPositiveMillimeters.from(35)!!,
                            ),
                        ),
                        provenance = provenance(sourceUnit, "physical:A"),
                    ),
                ),
            ),
            routes = emptyList(),
        )
    }

    private fun physicalOccurrence(subject: String): CabinetPhysicalOccurrenceInput = CabinetPhysicalOccurrenceInput(
        key = key(
            PhysicalSourceUnitId("src/main.athena"),
            PhysicalInstallationId("MainCabinet"),
            subject,
        ),
        occurrenceId = PhysicalObjectId("physical:A"),
        targetId = PhysicalObjectId("Backplate"),
        targetLocalPosition = CabinetPointD(5.0, 5.0),
        footprint = CabinetSizeD(60.0, 40.0),
        orientation = PhysicalMountOrientation.Deg0,
        targetFrame = CabinetTargetFrame(
            origin = CabinetPointD(0.0, 0.0),
            alongAxis = CabinetVectorD(1.0, 0.0),
            normalAxis = CabinetVectorD(0.0, 1.0),
        ),
        provenance = PhysicalSourceProvenance(
            sourceUnitId = PhysicalSourceUnitId("src/main.athena"),
            declarationId = "mount:$subject",
            span = PhysicalSourceSpan("src/main.athena", 1, 1),
        ),
    )

    private fun representationOccurrence(subject: String): CabinetRepresentationOccurrenceInput =
        CabinetRepresentationOccurrenceInput(
            key = key(
                PhysicalSourceUnitId("src/main.athena"),
                PhysicalInstallationId("MainCabinet"),
                subject,
            ),
            representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
            intrinsicBounds = CabinetRectD(0.0, 0.0, 60.0, 40.0),
            anchors = listOf(CabinetIntrinsicAnchor("anchor-l1", CabinetPointD(10.0, 20.0))),
        )

    private data class PlacementFixture(
        val layoutGraph: LayoutGraph,
        val physicalOccurrence: CabinetPhysicalOccurrenceInput,
        val representationOccurrence: CabinetRepresentationOccurrenceInput,
    )
}
