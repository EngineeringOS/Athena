package com.engineeringood.athena.representation

import java.util.IdentityHashMap
import kotlin.math.min

enum class GraphicPrimitiveDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class GraphicPrimitiveDiagnosticCode(val wireValue: String) {
    init {
        require(wireValue.isNotBlank()) { "Graphic primitive diagnostic code must not be blank." }
    }
}

data class GraphicPrimitiveDiagnostic(
    val code: GraphicPrimitiveDiagnosticCode,
    val severity: GraphicPrimitiveDiagnosticSeverity,
    val subject: String,
    val message: String,
) {
    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "code" to code.wireValue,
        "severity" to severity.name,
        "subject" to subject,
        "message" to message,
    )
}

data class GraphicPrimitiveValidationResult(
    val diagnostics: List<GraphicPrimitiveDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == GraphicPrimitiveDiagnosticSeverity.ERROR }

    fun toTransportPayload(): List<Map<String, String>> = diagnostics.map { it.toTransportMap() }
}

object GraphicPrimitiveIrValidator {
    private const val MAX_NESTING_DEPTH = 64

    fun validate(document: GraphicPrimitiveDocument): GraphicPrimitiveValidationResult {
        val diagnostics = mutableListOf<GraphicPrimitiveDiagnostic>()
        if (document.documentId == null) {
            diagnostics += diagnostic("graphic.ir.document-id.missing", "documentId", "Graphic Primitive IR requires a document id.")
        }
        if (document.bounds == null) {
            diagnostics += diagnostic("graphic.ir.bounds.missing", "bounds", "Graphic Primitive IR requires document bounds.")
        } else if (!document.bounds.isPositiveFinite()) {
            diagnostics += diagnostic("graphic.ir.bounds.invalid", "bounds", "Graphic Primitive IR bounds must be finite and positive.")
        }
        if (document.primitives.isEmpty()) {
            diagnostics += diagnostic("graphic.ir.primitive.missing", "primitives", "Graphic Primitive IR requires at least one primitive.")
        }
        if (document.styleTokens.isEmpty()) {
            diagnostics += diagnostic("graphic.ir.style-token.missing", "styleTokens", "Graphic Primitive IR requires at least one style token.")
        }

        document.styleTokens.groupingBy { it.styleTokenId.value }.eachCount()
            .filterValues { it > 1 }.keys.sorted().forEach { id ->
                diagnostics += diagnostic(
                    "graphic.ir.style-token.duplicate",
                    "styleTokens.$id",
                    "Graphic style token ids must be unique.",
                )
            }
        val styleIds = document.styleTokens.map { it.styleTokenId }.toSet()
        document.styleTokens.forEach { style ->
            if (!style.strokeWidth.isFinite() || style.strokeWidth <= 0.0 ||
                style.dashPattern.any { !it.isFinite() || it <= 0.0 }
            ) {
                diagnostics += diagnostic(
                    "graphic.ir.style-token.invalid",
                    "styleTokens.${style.styleTokenId.value}",
                    "Graphic style stroke width and dash values must be finite and positive.",
                )
            }
        }

        val seenIds = mutableSetOf<String>()
        val ancestors = IdentityHashMap<GraphicPrimitive, Boolean>()
        document.primitives.forEach { primitive ->
            if (document.bounds != null && document.bounds.isPositiveFinite() && !document.bounds.contains(primitive.bounds)) {
                diagnostics += diagnostic(
                    "graphic.ir.bounds.out-of-document",
                    "primitives.${primitive.primitiveId.value}.bounds",
                    "Graphic primitive bounds must lie inside document bounds.",
                )
            }
            validatePrimitive(primitive, styleIds, seenIds, ancestors, 0, diagnostics)
        }
        if (document.forbiddenAuthorityClaims.isNotEmpty()) {
            diagnostics += diagnostic(
                "graphic.ir.authority-forbidden",
                "forbiddenAuthorityClaims",
                "Graphic Primitive IR must not own: ${document.forbiddenAuthorityClaims.sortedBy { it.name }.joinToString(",")}",
            )
        }

        return GraphicPrimitiveValidationResult(
            diagnostics.sortedWith(compareBy({ it.code.wireValue }, { it.subject }, { it.message })),
        )
    }

    private fun validatePrimitive(
        primitive: GraphicPrimitive,
        styleIds: Set<GraphicStyleTokenId>,
        seenIds: MutableSet<String>,
        ancestors: IdentityHashMap<GraphicPrimitive, Boolean>,
        depth: Int,
        diagnostics: MutableList<GraphicPrimitiveDiagnostic>,
    ) {
        if (depth > MAX_NESTING_DEPTH) {
            diagnostics += diagnostic(
                "graphic.ir.nesting.depth-exceeded",
                "primitives.${primitive.primitiveId.value}",
                "Graphic primitive nesting exceeds $MAX_NESTING_DEPTH levels.",
            )
            return
        }
        if (ancestors.put(primitive, true) != null) {
            diagnostics += diagnostic(
                "graphic.ir.nesting.cycle",
                "primitives.${primitive.primitiveId.value}",
                "Graphic primitive groups and transforms must be acyclic.",
            )
            return
        }
        if (!seenIds.add(primitive.primitiveId.value)) {
            diagnostics += diagnostic(
                "graphic.ir.primitive-id.duplicate",
                "primitives.${primitive.primitiveId.value}",
                "Graphic primitive ids must be unique across the scene.",
            )
        }
        if (!primitive.bounds.isPositiveFinite()) {
            diagnostics += diagnostic(
                "graphic.ir.bounds.invalid",
                "primitives.${primitive.primitiveId.value}.bounds",
                "Graphic primitive bounds must be finite and positive.",
            )
        }
        primitive.styleTokenId?.let { styleTokenId ->
            if (styleTokenId !in styleIds) {
                diagnostics += diagnostic(
                    "graphic.ir.style-token.unresolved",
                    "primitives.${primitive.primitiveId.value}.styleTokenId",
                    "Graphic primitive references an unresolved style token.",
                )
            }
        }
        if (!primitive.hasValidGeometry()) {
            diagnostics += diagnostic(
                "graphic.ir.geometry.invalid",
                "primitives.${primitive.primitiveId.value}",
                "Graphic primitive geometry is invalid for `${primitive.kind.wireValue}`.",
            )
        }
        when (primitive) {
            is GraphicPrimitive.Group -> primitive.children.forEach { child ->
                if (primitive.bounds.isPositiveFinite() && !primitive.bounds.contains(child.bounds)) {
                    diagnostics += diagnostic(
                        "graphic.ir.bounds.out-of-group",
                        "primitives.${child.primitiveId.value}.bounds",
                        "Grouped primitive bounds must lie inside group bounds.",
                    )
                }
                validatePrimitive(child, styleIds, seenIds, ancestors, depth + 1, diagnostics)
            }
            is GraphicPrimitive.Transformed ->
                validatePrimitive(primitive.child, styleIds, seenIds, ancestors, depth + 1, diagnostics)
            else -> Unit
        }
        ancestors.remove(primitive)
    }

    private fun GraphicPrimitive.hasValidGeometry(): Boolean = bounds.isPositiveFinite() && when (this) {
        is GraphicPrimitive.Line -> start.isFinite() && end.isFinite() && start != end && bounds.contains(start) && bounds.contains(end)
        is GraphicPrimitive.Polyline ->
            points.size >= 2 && points.distinct().size >= 2 && points.all { it.isFinite() && bounds.contains(it) }
        is GraphicPrimitive.Arc ->
            center.isFinite() && radius.isFinite() && radius > 0.0 &&
                startAngleDegrees.isFinite() && sweepAngleDegrees.isFinite() && sweepAngleDegrees != 0.0 &&
                bounds.containsCircle(center, radius)
        is GraphicPrimitive.Circle -> center.isFinite() && radius.isFinite() && radius > 0.0 && bounds.containsCircle(center, radius)
        is GraphicPrimitive.Rectangle ->
            cornerRadius.isFinite() && cornerRadius >= 0.0 && cornerRadius <= min(bounds.width, bounds.height) / 2.0
        is GraphicPrimitive.Text -> origin.isFinite() && text.isNotBlank() && bounds.contains(origin)
        is GraphicPrimitive.Marker -> origin.isFinite() && bounds.contains(origin)
        is GraphicPrimitive.ConnectionDot ->
            center.isFinite() && radius.isFinite() && radius > 0.0 && bounds.containsCircle(center, radius)
        is GraphicPrimitive.ReferenceArrow ->
            start.isFinite() && end.isFinite() && start != end && headSize.isFinite() && headSize > 0.0 &&
                bounds.contains(start) && bounds.contains(end)
        is GraphicPrimitive.Group -> children.isNotEmpty()
        is GraphicPrimitive.Transformed -> transform.isValid()
    }

    private fun GraphicTransform.isValid(): Boolean = when (this) {
        is GraphicTransform.Translation -> dx.isFinite() && dy.isFinite()
        is GraphicTransform.Rotation -> angleDegrees.isFinite() && pivot.isFinite()
        is GraphicTransform.Scale -> x.isFinite() && y.isFinite() && x > 0.0 && y > 0.0 && pivot.isFinite()
    }

    private fun GraphicPoint.isFinite(): Boolean = x.isFinite() && y.isFinite()

    private fun GraphicBounds.isPositiveFinite(): Boolean =
        x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite() && width > 0.0 && height > 0.0

    private fun GraphicBounds.contains(point: GraphicPoint): Boolean =
        point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height

    private fun GraphicBounds.contains(other: GraphicBounds): Boolean =
        other.isPositiveFinite() && other.x >= x && other.y >= y &&
            other.x + other.width <= x + width && other.y + other.height <= y + height

    private fun GraphicBounds.containsCircle(center: GraphicPoint, radius: Double): Boolean =
        center.x - radius >= x && center.y - radius >= y &&
            center.x + radius <= x + width && center.y + radius <= y + height

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): GraphicPrimitiveDiagnostic = GraphicPrimitiveDiagnostic(
        code = GraphicPrimitiveDiagnosticCode(code),
        severity = GraphicPrimitiveDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
