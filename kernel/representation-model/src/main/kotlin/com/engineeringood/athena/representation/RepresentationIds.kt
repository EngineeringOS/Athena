package com.engineeringood.athena.representation

@JvmInline
value class RepresentationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation id must not be blank." }
    }
}

@JvmInline
value class SymbolFamilyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Symbol family id must not be blank." }
    }
}

@JvmInline
value class PresentationPrimitiveId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation primitive id must not be blank." }
    }
}

@JvmInline
value class PresentationTerminalId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation terminal id must not be blank." }
    }
}

@JvmInline
value class RepresentationSubjectId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation subject id must not be blank." }
    }
}

@JvmInline
value class RepresentationOccurrenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation occurrence id must not be blank." }
    }
}

@JvmInline
value class SemanticPortId(val value: String) {
    init {
        require(value.isNotBlank()) { "Semantic port id must not be blank." }
    }
}

@JvmInline
value class PhysicalTerminalId(val value: String) {
    init {
        require(value.isNotBlank()) { "Physical terminal id must not be blank." }
    }
}

@JvmInline
value class PresentationRouteAnchorId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation route anchor id must not be blank." }
    }
}

@JvmInline
value class PresentationLabelAnchorId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation label anchor id must not be blank." }
    }
}

@JvmInline
value class LabelFactId(val value: String) {
    init {
        require(value.isNotBlank()) { "Label fact id must not be blank." }
    }
}

@JvmInline
value class LabelValue(val value: String) {
    init {
        require(value.isNotBlank()) { "Label value must not be blank." }
    }
}
