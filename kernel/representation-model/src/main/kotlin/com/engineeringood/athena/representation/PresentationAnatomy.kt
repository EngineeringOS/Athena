package com.engineeringood.athena.representation

enum class RepresentationContext {
    ELECTRICAL_SCHEMATIC,
}

enum class PresentationSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

enum class TerminalPresentationRole {
    POWER_INPUT,
    POWER_OUTPUT,
    DIGITAL_INPUT,
    DIGITAL_OUTPUT,
    COMMUNICATION,
    PROTECTIVE_EARTH,
    TERMINAL_TRANSITION,
    BIDIRECTIONAL,
}

enum class TerminalMarker {
    CIRCLE,
    SQUARE,
    LINE,
}

@JvmInline
value class TerminalNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Terminal number must not be blank." }
    }
}

data class TerminalNotation(
    val marker: TerminalMarker,
    val number: TerminalNumber,
)

sealed interface PresentationPrimitive {
    val primitiveId: PresentationPrimitiveId

    data class Line(
        override val primitiveId: PresentationPrimitiveId,
        val start: PresentationPoint,
        val end: PresentationPoint,
    ) : PresentationPrimitive

    data class Rectangle(
        override val primitiveId: PresentationPrimitiveId,
        val origin: PresentationPoint,
        val size: PresentationSize,
    ) : PresentationPrimitive

    data class Polyline(
        override val primitiveId: PresentationPrimitiveId,
        val points: List<PresentationPoint>,
    ) : PresentationPrimitive {
        init {
            require(points.size >= 2) { "Polyline primitive requires at least two points." }
        }
    }

    data class Circle(
        override val primitiveId: PresentationPrimitiveId,
        val center: PresentationPoint,
        val radius: GridUnit,
    ) : PresentationPrimitive {
        init {
            require(radius.value > 0) { "Circle primitive radius must be positive." }
        }
    }

    data class Text(
        override val primitiveId: PresentationPrimitiveId,
        val origin: PresentationPoint,
        val text: String,
    ) : PresentationPrimitive {
        init {
            require(text.isNotBlank()) { "Text primitive content must not be blank." }
        }
    }
}

data class PresentationTerminalPoint(
    val terminalId: PresentationTerminalId,
    val role: TerminalPresentationRole,
    val localPoint: PresentationPoint,
    val side: PresentationSide,
    val notation: TerminalNotation,
)

enum class PresentationLabelRole {
    DEVICE_TAG,
    COMPONENT_LABEL,
    TERMINAL_LABEL,
    ROUTE_LABEL,
    DYNAMIC_TEXT,
}

data class PresentationLabelAnchor(
    val anchorId: PresentationLabelAnchorId,
    val role: PresentationLabelRole,
    val point: PresentationPoint,
)
