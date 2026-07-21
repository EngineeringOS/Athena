package com.engineeringood.athena.representation

data class CompositionContentBounds(
    val id: String,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    init {
        require(id.isNotBlank()) { "Composition content bounds id must not be blank." }
        require(maxX >= minX) { "Composition content bounds maxX must not precede minX." }
        require(maxY >= minY) { "Composition content bounds maxY must not precede minY." }
    }
}

data class CompositionViewBox(
    val minX: Int,
    val minY: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width >= 0) { "Composition viewBox width must not be negative." }
        require(height >= 0) { "Composition viewBox height must not be negative." }
    }
}

data class CompositionBoundsProofInput(
    val contentBounds: List<CompositionContentBounds>,
    val declaredViewBox: CompositionViewBox,
    val governedMargin: Int,
    val labels: List<String>,
    val visibleWrapperBorders: Boolean,
) {
    init {
        require(governedMargin >= 0) { "Composition governed margin must not be negative." }
    }
}

data class CompositionBoundsProofDiagnostic(
    val code: String,
    val message: String,
)

data class CompositionBoundsProof(
    val derivedViewBox: CompositionViewBox,
    val diagnostics: List<CompositionBoundsProofDiagnostic>,
) {
    val accepted: Boolean
        get() = diagnostics.isEmpty()
}

class CompositionBoundsProofGuard {
    fun verify(input: CompositionBoundsProofInput): CompositionBoundsProof {
        val derived = input.contentBounds.deriveViewBox(input.governedMargin)
        val diagnostics = mutableListOf<CompositionBoundsProofDiagnostic>()
        if (input.declaredViewBox != derived) {
            diagnostics += CompositionBoundsProofDiagnostic(
                code = "composition.bounds.hard-coded-viewbox",
                message = "Declared viewBox does not match content-derived bounds plus governed margin.",
            )
        }
        if (input.contentBounds.hasOffscreenDuplicate(input.declaredViewBox)) {
            diagnostics += CompositionBoundsProofDiagnostic(
                code = "composition.bounds.offscreen-duplicate",
                message = "Content contains a duplicate-sized element far outside the local symbol cluster.",
            )
        }
        input.labels.groupingBy { label -> label }.eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
            .forEach { label ->
                diagnostics += CompositionBoundsProofDiagnostic(
                    code = "composition.bounds.repeated-label",
                    message = "Label `$label` appears more than once in the same proof payload.",
                )
            }
        if (input.visibleWrapperBorders) {
            diagnostics += CompositionBoundsProofDiagnostic(
                code = "composition.bounds.wrapper-border-visible",
                message = "Normal-state non-symbol wrapper border is visible.",
            )
        }
        return CompositionBoundsProof(
            derivedViewBox = derived,
            diagnostics = diagnostics.sortedBy { diagnostic -> diagnostic.code },
        )
    }
}

private fun List<CompositionContentBounds>.deriveViewBox(margin: Int): CompositionViewBox {
    if (isEmpty()) {
        return CompositionViewBox(0, 0, 0, 0)
    }
    val minX = minOf { bounds -> bounds.minX } - margin
    val minY = minOf { bounds -> bounds.minY } - margin
    val maxX = maxOf { bounds -> bounds.maxX } + margin
    val maxY = maxOf { bounds -> bounds.maxY } + margin
    return CompositionViewBox(
        minX = minX,
        minY = minY,
        width = maxX - minX,
        height = maxY - minY,
    )
}

private fun List<CompositionContentBounds>.hasOffscreenDuplicate(viewBox: CompositionViewBox): Boolean {
    val visibleMaxX = viewBox.minX + viewBox.width
    val visibleMaxY = viewBox.minY + viewBox.height
    return any { bounds ->
        bounds.minX > visibleMaxX ||
            bounds.minY > visibleMaxY ||
            bounds.maxX < viewBox.minX ||
            bounds.maxY < viewBox.minY
    } || groupBy { bounds -> bounds.shapeKey() }
        .any { (_, bounds) -> bounds.size > 1 && bounds.maxOf { it.minX } - bounds.minOf { it.minX } > viewBox.width }
}

private fun CompositionContentBounds.shapeKey(): String = "${maxX - minX}x${maxY - minY}"
