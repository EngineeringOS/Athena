package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * One view-specific projection occurrence anchored to canonical semantic identity.
 */
data class ProjectionNode(
    val projectionId: ProjectionNodeId,
    val semanticId: StableSemanticIdentity,
    val label: String,
    val originGeometryElementId: GeometryElementId,
)

/** Stable coordinate-free identity of one semantic port on one projected Occurrence. */
data class ProjectionOccurrencePortId(
    val occurrenceId: ProjectionNodeId,
    val portId: StableSemanticIdentity,
)

/** One projected occurrence-port fact; boundary geometry remains Spatial-owned. */
data class ProjectionOccurrencePort(
    val occurrencePortId: ProjectionOccurrencePortId,
    val originGeometryElementId: GeometryElementId,
)

/** Typed endpoint reference retained even when the matching projected port fact is absent. */
data class ProjectionConnectionEndpoint(
    val occurrencePortId: ProjectionOccurrencePortId,
)

/**
 * One view-specific projection connection occurrence anchored to canonical semantic identity.
 */
data class ProjectionConnection(
    val projectionId: ProjectionConnectionId,
    val semanticId: StableSemanticIdentity,
    val originGeometryElementId: GeometryElementId,
    val source: ProjectionConnectionEndpoint? = null,
    val target: ProjectionConnectionEndpoint? = null,
)
