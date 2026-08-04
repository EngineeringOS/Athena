package com.engineeringood.athena.projection

import com.engineeringood.athena.layout.ViewDefinition

/**
 * Compiler-derived projection document for one supported view.
 *
 * Projection Reality selects what belongs in a view. It does not publish canvas size, coordinates,
 * anchor positions, labels, route endpoints, or renderer guidance.
 */
data class ProjectionDocument(
    val view: ViewDefinition,
    val nodes: List<ProjectionNode>,
    val connections: List<ProjectionConnection>,
    val occurrencePorts: List<ProjectionOccurrencePort> = emptyList(),
    val resolvedSubjects: List<ProjectionResolvedSubject> = emptyList(),
    val sheets: List<ProjectionSheet> = emptyList(),
    val notationPack: ProjectionNotationPack? = null,
    val crossReferences: List<ProjectionCrossReference> = emptyList(),
)
