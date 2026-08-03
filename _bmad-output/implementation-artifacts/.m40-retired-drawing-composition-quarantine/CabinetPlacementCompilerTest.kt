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
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CabinetPlacementCompilerTest {
    @Test
    fun `normalizes deterministic placement proposals from a transient layout graph`() {
        val request = request()

        val first = CabinetPlacementCompiler.compile(request)
        val second = CabinetPlacementCompiler.compile(request)

        assertEquals(first, second)

        val success = assertIs<CabinetPlacementCompilation.Success>(first)
        assertEquals("athena-native", success.evidence.plannerId)
        assertEquals("1.0.0", success.evidence.plannerVersion)
        assertEquals(request.layoutGraph.snapshotId.value, success.evidence.snapshotId)
        assertEquals(1, success.evidence.placementCount)
        assertEquals(listOf("component:A"), success.evidence.placementKeys)
        assertEquals(listOf("component:A"), success.placements.map { placement -> placement.key.canonicalSemanticSubjectId.value })
        assertEquals(CabinetPointD(100.0, 120.0), success.placements.single().proposedPhysicalOccurrence.targetLocalPosition)
        assertEquals(CabinetRectD(100.0, 120.0, 60.0, 40.0), success.placements.single().join.body.bounds)
        assertEquals("compiler:snapshot-a", success.placements.single().compilerSnapshotId)
        assertEquals(
            listOf("cabinet-transform:component:A"),
            success.placements.map { placement -> placement.join.transform.id.value },
        )
    }

    @Test
    fun `recovers target local origin from a vertical frame bounding box`() {
        val base = request()
        val request = base.copy(
            layoutGraph = base.layoutGraph.copy(
                occurrences = base.layoutGraph.occurrences.map { occurrence ->
                    occurrence.copy(bounds = LayoutGraphBounds(936, 380, 64, 64))
                },
            ),
            physicalOccurrences = base.physicalOccurrences.map { occurrence ->
                occurrence.copy(
                    footprint = CabinetSizeD(64.0, 64.0),
                    orientation = PhysicalInstallationOrientation.Deg90,
                    targetFrame = CabinetTargetFrame(
                        origin = CabinetPointD(1000.0, 160.0),
                        alongAxis = CabinetVectorD(0.0, 1.0),
                        normalAxis = CabinetVectorD(-1.0, 0.0),
                    ),
                )
            },
        )

        val success = assertIs<CabinetPlacementCompilation.Success>(CabinetPlacementCompiler.compile(request))

        assertEquals(
            CabinetPointD(220.0, 0.0),
            success.placements.single().proposedPhysicalOccurrence.targetLocalPosition,
        )
        val body = success.placements.single().join.body.bounds
        assertEquals(936.0, body.x)
        assertTrue(body.y >= 380.0)
        assertTrue(body.right <= 1000.0)
        assertTrue(body.bottom <= 444.0)
    }

    @Test
    fun `rejects snapshot mismatched placement proposals before normalization`() {
        val request = request(
            layoutGraph = layoutGraph().copy(
                snapshotId = LayoutGraphSnapshotId("layout:other.athena:MainCabinet:snapshot-a"),
            ),
        )

        val failure = assertIs<CabinetPlacementCompilation.Failure>(CabinetPlacementCompiler.compile(request))
        assertEquals(listOf("cabinet.placement.snapshot.mismatch"), failure.diagnostics.map { diagnostic -> diagnostic.code })
    }

    private fun request(layoutGraph: LayoutGraph = layoutGraph()): CabinetPlacementRequest =
        CabinetPlacementRequest(
            layoutGraph = layoutGraph,
            plannerId = "athena-native",
            plannerVersion = "1.0.0",
            physicalOccurrences = listOf(physicalOccurrence("component:A")),
            representationOccurrences = listOf(representationOccurrence("component:A")),
            enclosureToDrawing = CabinetTargetFrame(
                origin = CabinetPointD(0.0, 0.0),
                alongAxis = CabinetVectorD(1.0, 0.0),
                normalAxis = CabinetVectorD(0.0, 1.0),
            ),
        )

    private fun layoutGraph(): LayoutGraph {
        val sourceUnit = LayoutGraphSourceUnitId("src/main.athena")
        val provenance = LayoutGraphProvenance(
            sourceUnitId = sourceUnit,
            declarationId = "layout:component:A",
            span = null,
        )
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
                    bounds = LayoutGraphBounds(100, 120, 60, 40),
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

    private fun physicalOccurrence(subject: String) = CabinetPhysicalOccurrenceInput(
        key = key(subject),
        occurrenceId = PhysicalObjectId("physical:A"),
        targetId = PhysicalObjectId("enclosure:ENC1"),
        targetLocalPosition = CabinetPointD(10.0, 20.0),
        footprint = CabinetSizeD(60.0, 40.0),
        orientation = PhysicalInstallationOrientation.Deg0,
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

    private fun representationOccurrence(subject: String) = CabinetRepresentationOccurrenceInput(
        key = key(subject),
        representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
        intrinsicBounds = CabinetRectD(0.0, 0.0, 60.0, 40.0),
        anchors = listOf(CabinetIntrinsicAnchor("anchor-l1", CabinetPointD(10.0, 20.0))),
    )

    private fun key(subject: String): InstallationOccurrenceKey = InstallationOccurrenceKey(
        sourceUnitId = PhysicalSourceUnitId("src/main.athena"),
        installationId = PhysicalInstallationId("MainCabinet"),
        canonicalSemanticSubjectId = StableSemanticIdentity(subject),
    )
}
