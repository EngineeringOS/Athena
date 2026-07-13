package com.engineeringood.athena.projection

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Projection-local identifier for one electrical anchor occurrence.
 *
 * The identifier stays projection-owned so repeated references may publish more than one anchor for
 * the same canonical port without creating a second engineering identity.
 */
@JvmInline
value class ElectricalAnchorId(val value: String) {
    override fun toString(): String = value
}

/**
 * Edge side used by one electrical anchor occurrence on a projection node.
 */
enum class ElectricalAnchorSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

/**
 * Typed downstream electrical anchor for one canonical port occurrence in one projection.
 *
 * The anchor stays downstream of canonical engineering identity. It gives renderers a stable
 * endpoint occurrence to target without turning route geometry into truth.
 */
data class ElectricalAnchor(
    val anchorId: ElectricalAnchorId,
    val portSemanticId: StableSemanticIdentity,
    val ownerSemanticId: StableSemanticIdentity,
    val nodeId: ProjectionNodeId,
    val labelId: ProjectionLabelId? = null,
    val position: ProjectionPoint,
    val side: ElectricalAnchorSide,
)

/**
 * Projection-local identifier for one electrical connection endpoint occurrence.
 */
@JvmInline
value class ElectricalConnectionEndpointId(val value: String) {
    override fun toString(): String = value
}

/**
 * Canonical endpoint role for one electrical connection occurrence.
 */
enum class ElectricalConnectionEndpointRole {
    SOURCE,
    TARGET,
}

/**
 * Typed mapping from one projection connection occurrence back to one canonical connection endpoint.
 *
 * The mapping keeps endpoint identity anchored in canonical engineering semantics while still
 * letting projections choose which downstream anchor occurrence should host the rendered endpoint.
 */
data class ElectricalConnectionEndpoint(
    val endpointId: ElectricalConnectionEndpointId,
    val projectionConnectionId: ProjectionConnectionId,
    val connectionSemanticId: StableSemanticIdentity,
    val endpointRole: ElectricalConnectionEndpointRole,
    val portSemanticId: StableSemanticIdentity,
    val anchorId: ElectricalAnchorId,
)

/**
 * Projection-local identifier for one electrical routing-corridor occurrence.
 */
@JvmInline
value class ElectricalRoutingCorridorId(val value: String) {
    override fun toString(): String = value
}

/**
 * Narrow first routing vocabulary for downstream electrical rendering guidance.
 *
 * The style classifies guidance only. It is not a claim that the published bend points are the
 * final visual path or a new canonical engineering truth.
 */
enum class ElectricalRoutingStyle {
    ORTHOGONAL,
}

/**
 * Typed downstream routing guidance for one electrical connection occurrence.
 *
 * This corridor is renderer guidance only. Semantic layer still owns endpoint truth, projection
 * owns preferred corridor hints, and renderer owns the final visual path.
 */
data class ElectricalRoutingCorridor(
    val corridorId: ElectricalRoutingCorridorId,
    val projectionConnectionId: ProjectionConnectionId,
    val connectionSemanticId: StableSemanticIdentity,
    val sourceAnchorId: ElectricalAnchorId,
    val targetAnchorId: ElectricalAnchorId,
    val routingStyle: ElectricalRoutingStyle = ElectricalRoutingStyle.ORTHOGONAL,
    val preferredBendPoints: List<ProjectionPoint> = emptyList(),
)
