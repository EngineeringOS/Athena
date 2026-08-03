package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalInstallationId
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalRigidFrame2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.PhysicalVector2i
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CabinetVisualTransformCompilerTest {
    @Test
    fun `joins exactly one physical and one representation occurrence by installation key`() {
        val result = CabinetVisualTransformCompiler.compile(
            physicalOccurrences = listOf(physical("component:A")),
            representationOccurrences = listOf(representation("component:A")),
            enclosureToDrawing = identityFrame(),
        )

        val join = assertIs<CabinetVisualTransformCompilation.Success>(result).joins.single()

        assertEquals(key("component:A"), join.key)
        assertEquals(CabinetTransformId("cabinet-transform:component:A"), join.transform.id)
        assertEquals(join.transform.id, join.body.transformId)
        assertEquals(join.transform.id, join.anchors.single().transformId)
    }

    @Test
    fun `fails closed on missing and duplicate join sides`() {
        val result = CabinetVisualTransformCompiler.compile(
            physicalOccurrences = listOf(
                physical("component:A"),
                physical("component:A", occurrenceId = "A2"),
                physical("component:B"),
            ),
            representationOccurrences = listOf(
                representation("component:C"),
                representation("component:D"),
                representation("component:D"),
            ),
            enclosureToDrawing = identityFrame(),
        )

        val failure = assertIs<CabinetVisualTransformCompilation.Failure>(result)

        assertEquals(
            setOf(
                "cabinet.join.duplicate_physical",
                "cabinet.join.duplicate_representation",
                "cabinet.join.missing_physical",
                "cabinet.join.missing_representation",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
        )
    }

    @Test
    fun `transforms body and anchors for all occurrence rotations`() {
        val expectations = mapOf(
            PhysicalInstallationOrientation.Deg0 to CabinetRectD(10.0, 20.0, 50.0, 25.0),
            PhysicalInstallationOrientation.Deg90 to CabinetRectD(10.0, 20.0, 25.0, 50.0),
            PhysicalInstallationOrientation.Deg180 to CabinetRectD(10.0, 20.0, 50.0, 25.0),
            PhysicalInstallationOrientation.Deg270 to CabinetRectD(10.0, 20.0, 25.0, 50.0),
        )

        expectations.forEach { (orientation, expectedBounds) ->
            val join = assertIs<CabinetVisualTransformCompilation.Success>(
                CabinetVisualTransformCompiler.compile(
                    physicalOccurrences = listOf(physical("component:A", orientation = orientation)),
                    representationOccurrences = listOf(representation("component:A")),
                    enclosureToDrawing = identityFrame(),
                ),
            ).joins.single()

            assertEquals(expectedBounds, join.body.bounds.rounded())
            assertEquals(join.transform.id, join.anchors.single().transformId)
        }
    }

    @Test
    fun `vertical rail target frame keeps determinant positive and does not mirror anchors`() {
        val join = assertIs<CabinetVisualTransformCompilation.Success>(
            CabinetVisualTransformCompiler.compile(
                physicalOccurrences = listOf(
                    physical(
                        "component:A",
                        targetFrame = CabinetTargetFrame(
                            origin = CabinetPointD(100.0, 40.0),
                            alongAxis = CabinetVectorD(0.0, 1.0),
                            normalAxis = CabinetVectorD(-1.0, 0.0),
                        ),
                    ),
                ),
                representationOccurrences = listOf(representation("component:A")),
                enclosureToDrawing = identityFrame(),
            ),
        ).joins.single()

        assertEquals(1.0, join.transform.targetFrame.determinant)
        assertEquals(CabinetPointD(55.0, 50.0), join.body.bounds.origin.rounded())
        assertEquals(CabinetPointD(67.5, 75.0), join.anchors.single().point.rounded())
    }

    private fun physical(
        subject: String,
        occurrenceId: String = "A",
        orientation: PhysicalInstallationOrientation = PhysicalInstallationOrientation.Deg0,
        targetFrame: CabinetTargetFrame = identityFrame(),
    ): CabinetPhysicalOccurrenceInput = CabinetPhysicalOccurrenceInput(
        key = key(subject),
        occurrenceId = PhysicalObjectId(occurrenceId),
        targetLocalPosition = CabinetPointD(10.0, 20.0),
        footprint = CabinetSizeD(50.0, 25.0),
        orientation = orientation,
        targetFrame = targetFrame,
        provenance = PhysicalSourceProvenance(sourceUnit, "mount:$subject", PhysicalSourceSpan("src/main.athena", 1, 1)),
    )

    private fun representation(subject: String): CabinetRepresentationOccurrenceInput =
        CabinetRepresentationOccurrenceInput(
            key = key(subject),
            representationOccurrenceId = CabinetRepresentationOccurrenceId("rep:$subject"),
            intrinsicBounds = CabinetRectD(5.0, 10.0, 20.0, 10.0),
            anchors = listOf(CabinetIntrinsicAnchor("A1", CabinetPointD(15.0, 15.0))),
        )

    private fun key(subject: String): InstallationOccurrenceKey = InstallationOccurrenceKey(
        sourceUnitId = sourceUnit,
        installationId = PhysicalInstallationId("MainCabinet"),
        canonicalSemanticSubjectId = StableSemanticIdentity(subject),
    )

    private fun identityFrame(): CabinetTargetFrame = CabinetTargetFrame(
        origin = CabinetPointD(0.0, 0.0),
        alongAxis = CabinetVectorD(1.0, 0.0),
        normalAxis = CabinetVectorD(0.0, 1.0),
    )

    private fun CabinetRectD.rounded(): CabinetRectD = CabinetRectD(
        x = x.round1(),
        y = y.round1(),
        width = width.round1(),
        height = height.round1(),
    )

    private fun CabinetPointD.rounded(): CabinetPointD = CabinetPointD(x.round1(), y.round1())

    private fun Double.round1(): Double = kotlin.math.round(this * 10.0) / 10.0

    private companion object {
        val sourceUnit = PhysicalSourceUnitId("src/main.athena")
    }
}
