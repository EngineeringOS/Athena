package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.ViewDefinition

/**
 * Rebuildable downstream presentation document for one supported projection view.
 *
 * `PresentationDocument` is never a second semantic authority. Canonical engineering meaning
 * remains in `Engineering IR`, view-family ownership remains in `Projection Model`, and this
 * document only describes how one governed downstream presentation language should appear.
 */
data class PresentationDocument(
    val view: ViewDefinition,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val primitivePacks: List<PresentationPrimitivePack>,
    val compositePacks: List<PresentationCompositePack>,
    val resolvedSubjects: List<PresentationResolvedSubject> = emptyList(),
    val occurrences: List<PresentationOccurrence>,
    val connectors: List<PresentationConnector> = emptyList(),
)
