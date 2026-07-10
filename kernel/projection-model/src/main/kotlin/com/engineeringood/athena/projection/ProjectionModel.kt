package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition

/**
 * Projection-local identifier for one node in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionNodeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one connection in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionConnectionId(val value: String) {
    override fun toString(): String = value
}

/**
 * Projection-local identifier for one label in a derived projection document.
 *
 * The identifier is stable only within one projection view and remains secondary to canonical
 * semantic identity.
 */
@JvmInline
value class ProjectionLabelId(val value: String) {
    override fun toString(): String = value
}

/**
 * One absolute point in the projection plane.
 */
data class ProjectionPoint(
    val x: Int,
    val y: Int,
)

/**
 * Axis-aligned bounds for one projection node.
 */
data class ProjectionBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

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

/**
 * Compiler-derived projection document for one supported view.
 *
 * The view definition remains layout-owned. This module packages the view together with one
 * inspectable downstream projection document that runtime and adapters can consume without
 * rebuilding private graph state from raw geometry.
 */
data class ProjectionDocument(
    val view: ViewDefinition,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val nodes: List<ProjectionNode>,
    val connections: List<ProjectionConnection>,
    val labels: List<ProjectionLabel>,
)
