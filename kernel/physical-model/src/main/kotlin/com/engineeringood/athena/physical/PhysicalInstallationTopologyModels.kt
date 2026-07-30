package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity

@JvmInline
value class PhysicalSourceUnitId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class PhysicalInstallationId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class PhysicalObjectId(val value: String) : Comparable<PhysicalObjectId> {
    override fun compareTo(other: PhysicalObjectId): Int = value.compareTo(other.value)

    override fun toString(): String = value
}

enum class PhysicalInfrastructureOrientation {
    Horizontal,
    Vertical,
}

data class PhysicalPoint2i(
    val x: Int,
    val y: Int,
)

data class PhysicalSize2i(
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
    }
}

data class PhysicalInstallationSize3i(
    val width: Int,
    val height: Int,
    val depth: Int,
) {
    init {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
        require(depth > 0) { "depth must be positive" }
    }
}

data class PhysicalVector2i(
    val x: Int,
    val y: Int,
)

data class PhysicalRigidFrame2i(
    val origin: PhysicalPoint2i,
    val alongAxis: PhysicalVector2i,
    val normalAxis: PhysicalVector2i,
) {
    val determinant: Int = alongAxis.x * normalAxis.y - alongAxis.y * normalAxis.x

    /** Maps an `(along, normal)` target-local point into its parent coordinate system. */
    fun toParent(point: PhysicalPoint2i): PhysicalPoint2i = PhysicalPoint2i(
        x = origin.x + (alongAxis.x * point.x) + (normalAxis.x * point.y),
        y = origin.y + (alongAxis.y * point.x) + (normalAxis.y * point.y),
    )
}

data class PhysicalSourceProvenance(
    val sourceUnitId: PhysicalSourceUnitId,
    val declarationId: String,
    val span: PhysicalSourceSpan?,
)

data class PhysicalInstallationIntent(
    val sourceUnitId: PhysicalSourceUnitId,
    val installationId: PhysicalInstallationId,
    val enclosures: List<PhysicalEnclosureIntent>,
    val surfaces: List<PhysicalMountingSurfaceIntent>,
    val rails: List<PhysicalRailIntent>,
    val ducts: List<PhysicalDuctIntent>,
    val channels: List<PhysicalRouteChannelIntent>,
    val terminalGroups: List<PhysicalTerminalGroupIntent>,
    val mounts: List<PhysicalMountedOccurrenceIntent>,
    val routes: List<PhysicalRouteIntentSource>,
)

data class PhysicalEnclosureIntent(
    val id: PhysicalObjectId,
    val size: PhysicalInstallationSize3i,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalMountingSurfaceIntent(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val acceptedMountingTypes: Set<PhysicalMountingTypeId>,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRailIntent(
    val id: PhysicalObjectId,
    val surfaceId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val length: PhysicalPositiveMillimeters,
    val orientation: PhysicalInfrastructureOrientation,
    val mountingType: PhysicalMountingTypeId,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalDuctIntent(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation,
    val wall: PhysicalNonNegativeMillimeters,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRouteChannelIntent(
    val id: PhysicalObjectId,
    val ductId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation = PhysicalInfrastructureOrientation.Horizontal,
    val lanes: Int,
    val margin: PhysicalNonNegativeMillimeters,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalTerminalGroupIntent(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation,
    val acceptedMountingTypes: Set<PhysicalMountingTypeId>,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalMountedOccurrenceIntent(
    val occurrenceId: PhysicalObjectId,
    val semanticSubjectId: StableSemanticIdentity,
    val targetId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val selectedOrientation: PhysicalInstallationOrientation,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRouteIntentSource(
    val connectionAlias: String,
    val channelIds: List<PhysicalObjectId>,
    val provenance: PhysicalSourceProvenance,
)

data class InstallationOccurrenceKey(
    val sourceUnitId: PhysicalSourceUnitId,
    val installationId: PhysicalInstallationId,
    val canonicalSemanticSubjectId: StableSemanticIdentity,
)

sealed interface PhysicalMountTargetRef {
    val id: PhysicalObjectId

    data class Surface(override val id: PhysicalObjectId) : PhysicalMountTargetRef

    data class Rail(override val id: PhysicalObjectId) : PhysicalMountTargetRef

    data class TerminalGroup(override val id: PhysicalObjectId) : PhysicalMountTargetRef
}

data class PhysicalInstallationIR(
    val sourceUnitId: PhysicalSourceUnitId,
    val installationId: PhysicalInstallationId,
    val space: PhysicalInstallationSpace,
    val routes: List<PhysicalRouteIntent>,
)

data class PhysicalInstallationSpace(
    val enclosure: PhysicalEnclosure,
    val surfaces: List<PhysicalMountingSurface>,
    val rails: List<PhysicalRail>,
    val ducts: List<PhysicalDuct>,
    val channels: List<PhysicalRouteChannel>,
    val terminalGroups: List<PhysicalTerminalGroup>,
    val mountedOccurrences: List<PhysicalMountedOccurrence>,
)

data class PhysicalEnclosure(
    val id: PhysicalObjectId,
    val size: PhysicalInstallationSize3i,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalMountingSurface(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val acceptedMountingTypes: Set<PhysicalMountingTypeId>,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRail(
    val id: PhysicalObjectId,
    val surfaceId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val length: PhysicalPositiveMillimeters,
    val orientation: PhysicalInfrastructureOrientation,
    val mountingType: PhysicalMountingTypeId,
    val frame: PhysicalRigidFrame2i,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalDuct(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation,
    val wall: PhysicalNonNegativeMillimeters,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRouteChannel(
    val id: PhysicalObjectId,
    val ductId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation = PhysicalInfrastructureOrientation.Horizontal,
    val lanes: Int,
    val margin: PhysicalNonNegativeMillimeters,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalTerminalGroup(
    val id: PhysicalObjectId,
    val enclosureId: PhysicalObjectId,
    val at: PhysicalPoint2i,
    val size: PhysicalSize2i,
    val orientation: PhysicalInfrastructureOrientation,
    val acceptedMountingTypes: Set<PhysicalMountingTypeId>,
    val orderedOccurrenceKeys: List<InstallationOccurrenceKey>,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalMountedOccurrence(
    val occurrenceId: PhysicalObjectId,
    val key: InstallationOccurrenceKey,
    val semanticSubjectId: StableSemanticIdentity,
    val target: PhysicalMountTargetRef,
    val at: PhysicalPoint2i,
    val selectedOrientation: PhysicalInstallationOrientation,
    val contract: PhysicalInstallationContract,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalRouteIntent(
    val connectionAlias: String,
    val channelIds: List<PhysicalObjectId>,
    val provenance: PhysicalSourceProvenance,
)

data class PhysicalInstallationTopologyDiagnostic(
    val code: String,
    val subject: String,
    val span: PhysicalSourceSpan?,
    val measured: String?,
    val expected: String,
)

sealed interface PhysicalInstallationTopologyCompilation {
    data class Success(val ir: PhysicalInstallationIR) : PhysicalInstallationTopologyCompilation

    data class Failure(val diagnostics: List<PhysicalInstallationTopologyDiagnostic>) :
        PhysicalInstallationTopologyCompilation
}
