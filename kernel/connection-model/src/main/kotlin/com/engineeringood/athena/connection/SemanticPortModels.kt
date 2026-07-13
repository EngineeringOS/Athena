package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Stable identifier for one vendor-neutral semantic port type.
 *
 * This identity describes engineering connection meaning above canonical `Engineering IR`. It does
 * not encode graph coordinates, shape ids, or frontend widget state.
 */
@JvmInline
value class SemanticPortTypeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for the engineering role carried by one semantic port definition.
 *
 * Roles such as `L+`, `M`, `PE`, or `MPI` may later be published by domain slices without turning
 * renderer anchors or labels into the source of truth.
 */
@JvmInline
value class SemanticPortRoleId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one signal family carried by a semantic port.
 *
 * Signal family remains a knowledge contract. Rich compatibility judgement stays out of this
 * module and belongs to later M9 evaluation logic.
 */
@JvmInline
value class SemanticSignalFamilyId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one optional protocol or bus carried by a semantic port definition.
 */
@JvmInline
value class SemanticProtocolId(val value: String) {
    override fun toString(): String = value
}

/**
 * Directional intent carried by one semantic port definition.
 */
enum class SemanticPortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL,
    PASSIVE,
}

/**
 * Vendor-neutral semantic port knowledge contract.
 *
 * This definition describes stable engineering role, direction, signal family, and optional
 * protocol-bearing metadata. It does not absorb compatibility rules, sufficiency judgement,
 * routing geometry, or presentation state. Those richer evaluations belong downstream in M9
 * derived-context, capability-fact, and constraint-evaluation layers.
 */
data class SemanticPortDefinition(
    val portTypeId: SemanticPortTypeId,
    val displayName: String,
    val roleId: SemanticPortRoleId,
    val direction: SemanticPortDirection,
    val signalFamilyId: SemanticSignalFamilyId,
    val protocolIds: Set<SemanticProtocolId> = emptySet(),
    val summary: String? = null,
)

/**
 * Read-only resolved semantic port knowledge for one canonical authored port.
 *
 * The `portSemanticId` still points at the canonical authored port in `Engineering IR`. Resolution
 * adds semantic port knowledge above that canonical state and does not create a second mutation
 * path. It also does not decide connection compatibility or sufficiency.
 */
data class ResolvedSemanticPortDefinition(
    val portSemanticId: StableSemanticIdentity,
    val ownerSemanticId: StableSemanticIdentity,
    val definition: SemanticPortDefinition,
)
