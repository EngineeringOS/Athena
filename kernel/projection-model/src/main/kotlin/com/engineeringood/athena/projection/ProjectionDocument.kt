package com.engineeringood.athena.projection

import com.engineeringood.athena.layout.ViewDefinition

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
    val sheets: List<ProjectionSheet> = emptyList(),
    val notationPack: ProjectionNotationPack? = null,
    val crossReferences: List<ProjectionCrossReference> = emptyList(),
    val electricalAnchors: List<ElectricalAnchor> = emptyList(),
    val electricalConnectionEndpoints: List<ElectricalConnectionEndpoint> = emptyList(),
    val electricalRoutingCorridors: List<ElectricalRoutingCorridor> = emptyList(),
)
