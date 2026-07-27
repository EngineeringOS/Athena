package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.TerminalSide

/** Compiler-owned occurrence of canonical Graphic Primitive material on a governed drawing sheet. */
data class PresentationGraphicOccurrence(
    val occurrenceId: RepresentationOccurrenceId,
    val semanticSubjectId: String,
    val physicalComponentId: String,
    val functionId: String?,
    val bounds: PresentationDrawingBounds,
    val orientation: LayoutOrientation,
    val deviceLabel: String,
    val modelLabel: String?,
    val packageId: String,
    val definitionId: String,
    val bindingRuleId: String,
    val graphic: GraphicPrimitiveDocument,
    val terminalBindings: List<PresentationGraphicTerminalBinding>,
    val labels: List<PresentationGraphicLabel>,
    val sourceProvenance: List<String>,
    val authorities: PresentationGraphicOccurrenceAuthorities = PresentationGraphicOccurrenceAuthorities(),
) {
    init {
        require(semanticSubjectId.isNotBlank()) { "Graphic occurrence semantic subject id must not be blank." }
        require(physicalComponentId.isNotBlank()) { "Graphic occurrence physical component id must not be blank." }
        require(deviceLabel.isNotBlank()) { "Graphic occurrence device label must not be blank." }
        require(packageId.isNotBlank() && definitionId.isNotBlank() && bindingRuleId.isNotBlank()) {
            "Graphic occurrence material provenance must be complete."
        }
        require(graphic.primitives.isNotEmpty()) { "Graphic occurrence requires canonical Graphic Primitive material." }
    }
}

data class PresentationGraphicTerminalBinding(
    val portSemanticId: String,
    val anchorId: String,
    val terminalIdentity: String,
    val point: SchematicRoutePoint,
    val side: TerminalSide,
) {
    init {
        require(portSemanticId.isNotBlank() && anchorId.isNotBlank() && terminalIdentity.isNotBlank()) {
            "Graphic terminal bindings require semantic port, anchor, and terminal identities."
        }
    }
}

data class PresentationGraphicLabel(
    val labelId: String,
    val role: String,
    val value: String,
    val bounds: PresentationDrawingBounds,
) {
    init {
        require(labelId.isNotBlank() && role.isNotBlank() && value.isNotBlank()) {
            "Graphic labels require non-blank identity, role, and value."
        }
    }
}

data class PresentationGraphicOccurrenceAuthorities(
    val graphic: String = "graphic-primitive-ir",
    val placement: String = "semantic-layout-facts",
    val material: String = "representation-material-resolver",
)
