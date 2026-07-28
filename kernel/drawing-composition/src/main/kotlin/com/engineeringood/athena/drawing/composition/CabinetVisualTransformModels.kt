package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalInstallationOrientation
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalSourceProvenance

@JvmInline
value class CabinetTransformId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class CabinetRepresentationOccurrenceId(val value: String) {
    override fun toString(): String = value
}

data class CabinetPointD(
    val x: Double,
    val y: Double,
)

data class CabinetVectorD(
    val x: Double,
    val y: Double,
)

data class CabinetSizeD(
    val width: Double,
    val height: Double,
)

data class CabinetRectD(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
) {
    val origin: CabinetPointD
        get() = CabinetPointD(x, y)

    val right: Double
        get() = x + width

    val bottom: Double
        get() = y + height
}

data class CabinetTargetFrame(
    val origin: CabinetPointD,
    val alongAxis: CabinetVectorD,
    val normalAxis: CabinetVectorD,
) {
    val determinant: Double = alongAxis.x * normalAxis.y - alongAxis.y * normalAxis.x
}

data class CabinetPhysicalOccurrenceInput(
    val key: InstallationOccurrenceKey,
    val occurrenceId: PhysicalObjectId,
    val targetLocalPosition: CabinetPointD,
    val footprint: CabinetSizeD,
    val orientation: PhysicalInstallationOrientation,
    val targetFrame: CabinetTargetFrame,
    val provenance: PhysicalSourceProvenance,
)

data class CabinetRepresentationOccurrenceInput(
    val key: InstallationOccurrenceKey,
    val representationOccurrenceId: CabinetRepresentationOccurrenceId,
    val intrinsicBounds: CabinetRectD,
    val anchors: List<CabinetIntrinsicAnchor>,
)

data class CabinetIntrinsicAnchor(
    val id: String,
    val point: CabinetPointD,
)

data class CabinetVisualTransform(
    val id: CabinetTransformId,
    val key: InstallationOccurrenceKey,
    val targetFrame: CabinetTargetFrame,
    val intrinsicBounds: CabinetRectD,
    val footprint: CabinetSizeD,
    val orientation: PhysicalInstallationOrientation,
)

data class CabinetTransformedBody(
    val transformId: CabinetTransformId,
    val bounds: CabinetRectD,
)

data class CabinetTransformedAnchor(
    val id: String,
    val transformId: CabinetTransformId,
    val point: CabinetPointD,
)

data class CabinetOccurrenceVisualJoin(
    val key: InstallationOccurrenceKey,
    val physicalOccurrenceId: PhysicalObjectId,
    val representationOccurrenceId: CabinetRepresentationOccurrenceId,
    val transform: CabinetVisualTransform,
    val body: CabinetTransformedBody,
    val anchors: List<CabinetTransformedAnchor>,
)

data class CabinetVisualTransformDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

sealed interface CabinetVisualTransformCompilation {
    data class Success(val joins: List<CabinetOccurrenceVisualJoin>) : CabinetVisualTransformCompilation

    data class Failure(val diagnostics: List<CabinetVisualTransformDiagnostic>) :
        CabinetVisualTransformCompilation
}
