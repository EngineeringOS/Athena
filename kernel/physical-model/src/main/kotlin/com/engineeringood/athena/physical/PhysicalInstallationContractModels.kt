package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.security.MessageDigest

@JvmInline
value class PhysicalPositiveMillimeters private constructor(val value: Int) {
    companion object {
        fun from(value: Int): PhysicalPositiveMillimeters? =
            value.takeIf { it > 0 }?.let(::PhysicalPositiveMillimeters)
    }
}

@JvmInline
value class PhysicalNonNegativeMillimeters private constructor(val value: Int) {
    companion object {
        fun from(value: Int): PhysicalNonNegativeMillimeters? =
            value.takeIf { it >= 0 }?.let(::PhysicalNonNegativeMillimeters)
    }
}

@JvmInline
value class PhysicalContainerKindId(val value: String) : Comparable<PhysicalContainerKindId> {
    override fun compareTo(other: PhysicalContainerKindId): Int = value.compareTo(other.value)

    override fun toString(): String = value
}

enum class PhysicalInstallationOrientation(val token: String) {
    Deg0("deg0"),
    Deg90("deg90"),
    Deg180("deg180"),
    Deg270("deg270"),
}

enum class PhysicalInstallationContractField {
    Width,
    Height,
    Depth,
    MountingType,
    AllowedOrientations,
    ClearanceTop,
    ClearanceRight,
    ClearanceBottom,
    ClearanceLeft,
    CompatibleContainerKinds,
}

enum class PhysicalContractSourceKind(val precedence: Int) {
    Trait(0),
    Project(1),
}

data class PhysicalSourceSpan(
    val file: String,
    val line: Int,
    val column: Int,
)

data class PhysicalContractSource(
    val kind: PhysicalContractSourceKind,
    val id: String,
)

sealed interface PhysicalInstallationContractValue {
    data class LengthMillimeters(val value: Int) : PhysicalInstallationContractValue

    data class MountingType(val value: PhysicalMountingTypeId) : PhysicalInstallationContractValue

    data class Orientations(
        val values: Set<PhysicalInstallationOrientation>,
    ) : PhysicalInstallationContractValue

    data class ContainerKinds(
        val values: Set<PhysicalContainerKindId>,
    ) : PhysicalInstallationContractValue
}

data class PhysicalInstallationContractFact(
    val field: PhysicalInstallationContractField,
    val value: PhysicalInstallationContractValue,
    val source: PhysicalContractSource,
    val span: PhysicalSourceSpan? = null,
)

data class PhysicalInstallationContractFieldProvenance(
    val field: PhysicalInstallationContractField,
    val source: PhysicalContractSource,
    val span: PhysicalSourceSpan?,
)

data class PhysicalInstallationSize(
    val width: PhysicalPositiveMillimeters,
    val height: PhysicalPositiveMillimeters,
    val depth: PhysicalPositiveMillimeters,
)

data class PhysicalInstallationClearance(
    val top: PhysicalNonNegativeMillimeters,
    val right: PhysicalNonNegativeMillimeters,
    val bottom: PhysicalNonNegativeMillimeters,
    val left: PhysicalNonNegativeMillimeters,
)

data class PhysicalInstallationContractProvenance(
    val width: PhysicalInstallationContractFieldProvenance,
    val height: PhysicalInstallationContractFieldProvenance,
    val depth: PhysicalInstallationContractFieldProvenance,
    val mountingType: PhysicalInstallationContractFieldProvenance,
    val allowedOrientations: PhysicalInstallationContractFieldProvenance,
    val clearanceTop: PhysicalInstallationContractFieldProvenance,
    val clearanceRight: PhysicalInstallationContractFieldProvenance,
    val clearanceBottom: PhysicalInstallationContractFieldProvenance,
    val clearanceLeft: PhysicalInstallationContractFieldProvenance,
    val compatibleContainerKinds: PhysicalInstallationContractFieldProvenance,
)

data class PhysicalInstallationContract(
    val subjectIdentity: StableSemanticIdentity,
    val size: PhysicalInstallationSize,
    val mountingTypeId: PhysicalMountingTypeId,
    val allowedOrientations: Set<PhysicalInstallationOrientation>,
    val clearance: PhysicalInstallationClearance,
    val compatibleContainerKinds: Set<PhysicalContainerKindId>,
    val provenance: PhysicalInstallationContractProvenance,
) {
    val canonicalDigestMaterial: String =
        "size.width=${size.width.value};" +
            "size.height=${size.height.value};" +
            "size.depth=${size.depth.value};" +
            "mountingType=${mountingTypeId.value};" +
            "orientations=${allowedOrientations.sorted().joinToString(",") { it.token }};" +
            "clearance.top=${clearance.top.value};" +
            "clearance.right=${clearance.right.value};" +
            "clearance.bottom=${clearance.bottom.value};" +
            "clearance.left=${clearance.left.value};" +
            "containers=${compatibleContainerKinds.sorted().joinToString(",") { it.value }}"

    val canonicalDigest: String = sha256(canonicalDigestMaterial)
}

data class PhysicalInstallationContractDiagnostic(
    val code: String,
    val subjectIdentity: StableSemanticIdentity,
    val field: PhysicalInstallationContractField,
    val source: PhysicalContractSource?,
    val span: PhysicalSourceSpan?,
    val measured: String?,
    val expected: String,
)

sealed interface PhysicalInstallationContractResolution {
    data class Success(val contract: PhysicalInstallationContract) : PhysicalInstallationContractResolution

    data class Failure(val diagnostics: List<PhysicalInstallationContractDiagnostic>) :
        PhysicalInstallationContractResolution
}

private fun sha256(value: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { byte -> "%02x".format(byte) }
}
