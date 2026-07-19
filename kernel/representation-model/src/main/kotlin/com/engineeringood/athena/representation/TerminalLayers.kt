package com.engineeringood.athena.representation

data class SemanticPortRef(
    val subjectId: RepresentationSubjectId,
    val portId: SemanticPortId,
    val role: TerminalPresentationRole,
)

data class PhysicalTerminalRef(
    val terminalId: PhysicalTerminalId,
    val semanticPort: SemanticPortRef,
)

data class PresentationRouteAnchor(
    val anchorId: PresentationRouteAnchorId,
    val point: PresentationPoint,
)

data class PresentationTerminalFact(
    val presentationTerminalId: PresentationTerminalId,
    val subjectId: RepresentationSubjectId,
    val occurrenceId: RepresentationOccurrenceId,
    val portId: SemanticPortId,
    val physicalTerminalId: PhysicalTerminalId,
    val side: PresentationSide,
    val routeAnchor: PresentationRouteAnchor,
    val notation: TerminalNotation,
) {
    val terminalNumberDerivedFromRendererText: Boolean
        get() = false
}
