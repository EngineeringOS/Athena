package com.engineeringood.athena.representation

@JvmInline
value class GraphicPrimitiveDocumentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Graphic Primitive IR document id must not be blank." }
    }
}

@JvmInline
value class GraphicPrimitiveId(val value: String) {
    init {
        require(value.isNotBlank()) { "Graphic primitive id must not be blank." }
    }
}

@JvmInline
value class GraphicStyleTokenId(val value: String) {
    init {
        require(value.isNotBlank()) { "Graphic style token id must not be blank." }
    }
}

@JvmInline
value class GraphicPaintToken(val value: String) {
    init {
        require(value.matches(Regex("[A-Za-z][A-Za-z0-9._-]*"))) {
            "Graphic paint token must be a renderer-neutral token name."
        }
    }
}

data class GraphicPoint(
    val x: Double,
    val y: Double,
)

data class GraphicBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

enum class GraphicFill {
    TRANSPARENT,
    BACKGROUND,
    FOREGROUND,
}

enum class GraphicLineCap {
    BUTT,
    ROUND,
    SQUARE,
}

enum class GraphicLineJoin {
    MITER,
    ROUND,
    BEVEL,
}

enum class GraphicTextAnchor {
    START,
    MIDDLE,
    END,
}

enum class GraphicTextBaseline {
    ALPHABETIC,
    CENTRAL,
    HANGING,
}

data class GraphicStyleToken(
    val styleTokenId: GraphicStyleTokenId,
    val stroke: GraphicPaintToken,
    val strokeWidth: Double,
    val fill: GraphicFill,
    val lineCap: GraphicLineCap,
    val lineJoin: GraphicLineJoin,
    val dashPattern: List<Double> = emptyList(),
    val textAnchor: GraphicTextAnchor = GraphicTextAnchor.START,
    val textBaseline: GraphicTextBaseline = GraphicTextBaseline.ALPHABETIC,
)

enum class GraphicPrimitiveKind(val wireValue: String) {
    LINE("line"),
    POLYLINE("polyline"),
    ARC("arc"),
    CIRCLE("circle"),
    RECTANGLE("rectangle"),
    TEXT("text"),
    MARKER("marker"),
    CONNECTION_DOT("connection-dot"),
    REFERENCE_ARROW("reference-arrow"),
    GROUP("group"),
    TRANSFORM("transform"),
}

enum class GraphicMarkerKind {
    TERMINAL,
    REFERENCE,
    CROSSING,
}

sealed interface GraphicTransform {
    data class Translation(val dx: Double, val dy: Double) : GraphicTransform

    data class Rotation(val angleDegrees: Double, val pivot: GraphicPoint) : GraphicTransform

    data class Scale(val x: Double, val y: Double, val pivot: GraphicPoint) : GraphicTransform
}

sealed interface GraphicPrimitive {
    val primitiveId: GraphicPrimitiveId
    val bounds: GraphicBounds
    val styleTokenId: GraphicStyleTokenId?
    val kind: GraphicPrimitiveKind

    data class Line(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val start: GraphicPoint,
        val end: GraphicPoint,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.LINE
    }

    data class Polyline(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val points: List<GraphicPoint>,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.POLYLINE
    }

    data class Arc(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val center: GraphicPoint,
        val radius: Double,
        val startAngleDegrees: Double,
        val sweepAngleDegrees: Double,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.ARC
    }

    data class Circle(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val center: GraphicPoint,
        val radius: Double,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.CIRCLE
    }

    data class Rectangle(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val cornerRadius: Double,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.RECTANGLE
    }

    data class Text(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val origin: GraphicPoint,
        val text: String,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.TEXT
    }

    data class Marker(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val origin: GraphicPoint,
        val markerKind: GraphicMarkerKind,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.MARKER
    }

    data class ConnectionDot(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val center: GraphicPoint,
        val radius: Double,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.CONNECTION_DOT
    }

    data class ReferenceArrow(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val start: GraphicPoint,
        val end: GraphicPoint,
        val headSize: Double,
        override val styleTokenId: GraphicStyleTokenId,
    ) : GraphicPrimitive {
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.REFERENCE_ARROW
    }

    data class Group(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val children: List<GraphicPrimitive>,
    ) : GraphicPrimitive {
        override val styleTokenId: GraphicStyleTokenId? = null
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.GROUP
    }

    data class Transformed(
        override val primitiveId: GraphicPrimitiveId,
        override val bounds: GraphicBounds,
        val transform: GraphicTransform,
        val child: GraphicPrimitive,
    ) : GraphicPrimitive {
        override val styleTokenId: GraphicStyleTokenId? = null
        override val kind: GraphicPrimitiveKind = GraphicPrimitiveKind.TRANSFORM
    }
}

enum class GraphicPrimitiveForbiddenAuthority {
    ENGINEERING_TRUTH,
    PACKAGE_RESOLUTION,
    SOURCE_MUTATION,
    DOM_SELECTOR,
    CSS_SELECTOR,
    SVG_ELEMENT_ID,
    SVG_PATH_DATA,
}

data class GraphicPrimitiveDocument(
    val documentId: GraphicPrimitiveDocumentId?,
    val bounds: GraphicBounds?,
    val primitives: List<GraphicPrimitive>,
    val styleTokens: List<GraphicStyleToken>,
    val provenanceSources: List<String> = emptyList(),
    val forbiddenAuthorityClaims: Set<GraphicPrimitiveForbiddenAuthority> = emptySet(),
)
