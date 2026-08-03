package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
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
    val placedAnchors: List<PresentationPlacedAnchor>,
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
        require(placedAnchors.map { anchor -> anchor.anchorId }.distinct().size == placedAnchors.size) {
            "Graphic occurrence placed Anchor ids must be unique."
        }
        val placedAnchorIds = placedAnchors.map { anchor -> anchor.anchorId.value }.toSet()
        require(terminalBindings.all { binding -> binding.anchorId in placedAnchorIds }) {
            "Graphic terminal bindings must reuse placed Anchor points."
        }
    }
}

data class PresentationPlacedAnchor(
    val anchorId: RepresentationAnchorId,
    val geometryRef: String,
    val primitiveId: GraphicPrimitiveId,
    val point: SchematicRoutePoint,
    val role: RepresentationAnchorRole,
    val required: Boolean,
    val sourceProvenance: List<String>,
) {
    init {
        require(geometryRef.isNotBlank()) { "Placed Anchor geometry reference must not be blank." }
        require(sourceProvenance.isNotEmpty()) { "Placed Anchor requires source provenance." }
    }
}

data class PresentationGraphicTerminalBinding(
    val portSemanticId: String,
    val bindingId: RepresentationPortAnchorBindingId,
    val anchorId: String,
    val terminalIdentity: String,
    val point: SchematicRoutePoint,
    val labelPoint: SchematicRoutePoint,
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
