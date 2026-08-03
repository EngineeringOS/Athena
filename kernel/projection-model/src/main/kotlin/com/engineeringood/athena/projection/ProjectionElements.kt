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

/**
 * One view-specific projection connection occurrence anchored to canonical semantic identity.
 */
data class ProjectionConnection(
    val projectionId: ProjectionConnectionId,
    val semanticId: StableSemanticIdentity,
    val originGeometryElementId: GeometryElementId,
    val sourceOccurrenceId: String? = null,
    val targetOccurrenceId: String? = null,
    val sourcePortId: String? = null,
    val targetPortId: String? = null,
)
