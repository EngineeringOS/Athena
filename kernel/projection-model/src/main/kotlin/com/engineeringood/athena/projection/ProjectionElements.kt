package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * One derived projection node anchored to canonical semantic identity.
 *
 * The originating geometry element remains attached so downstream tooling can inspect the derived
 * path without treating geometry as the authority.
 */
data class ProjectionNode(
    val projectionId: ProjectionNodeId,
    val semanticId: StableSemanticIdentity,
    val label: String,
    val bounds: ProjectionBounds,
    val originGeometryElementId: GeometryElementId,
)

/**
 * One derived projection connection anchored to canonical semantic identity.
 *
 * The connection carries only the simple coordinates needed by current downstream renderer proofs
 * plus the originating geometry element reference for inspection.
 */
data class ProjectionConnection(
    val projectionId: ProjectionConnectionId,
    val semanticId: StableSemanticIdentity,
    val start: ProjectionPoint,
    val end: ProjectionPoint,
    val originGeometryElementId: GeometryElementId,
)

/**
 * One derived projection label anchored to canonical semantic identity.
 *
 * Labels preserve semantic affordances such as ports without forcing downstream adapters to
 * reverse-engineer geometry-only text decorations back into selectable graph state.
 */
data class ProjectionLabel(
    val projectionId: ProjectionLabelId,
    val semanticId: StableSemanticIdentity,
    val label: String,
    val bounds: ProjectionBounds,
    val originGeometryElementId: GeometryElementId,
)
