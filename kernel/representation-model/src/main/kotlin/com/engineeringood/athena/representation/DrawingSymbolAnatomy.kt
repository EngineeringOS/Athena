package com.engineeringood.athena.representation

@JvmInline
value class DrawingSymbolIdentity(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol identity must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolVersion(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol version must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolPackageId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol package id must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolTag(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol tag must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolAnchorId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol anchor id must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolSlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol slot id must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolHotspotId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol hotspot id must not be blank." }
    }
}

@JvmInline
value class DrawingSymbolTerminalRole(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing symbol terminal role must not be blank." }
    }
}

enum class DrawingSymbolLifecycle {
    ACTIVE,
    DEPRECATED,
    SUPERSEDED,
}

enum class DrawingSymbolOrientation {
    HORIZONTAL,
    VERTICAL,
}

enum class DrawingSymbolAnchorRole {
    CONNECTION,
    TERMINAL,
    REFERENCE,
    PLACEMENT,
}

enum class DrawingSymbolLabelRole {
    DEVICE_TAG,
    TERMINAL_NUMBER,
    RATING_MODEL,
    CROSS_REFERENCE,
    LOCATION,
    STATUS,
    GENERIC,
}

enum class DrawingSymbolReferenceRole {
    CROSS_REFERENCE,
    CONTINUATION,
    LOCATION,
    GENERIC,
}

enum class DrawingSymbolForbiddenAuthority {
    ENGINEERING_TRUTH,
    SOURCE_MUTATION,
    DOM_SELECTOR,
    SVG_PATH,
    ATHENA_VISUAL_SYNTAX,
}

data class DrawingSymbolPoint(
    val x: Int,
    val y: Int,
)

data class DrawingSymbolBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class DrawingSymbolAnchor(
    val anchorId: DrawingSymbolAnchorId,
    val point: DrawingSymbolPoint,
    val role: DrawingSymbolAnchorRole,
    val required: Boolean,
    val terminalRole: DrawingSymbolTerminalRole? = null,
)

data class DrawingSymbolLabelSlot(
    val slotId: DrawingSymbolSlotId,
    val role: DrawingSymbolLabelRole,
    val required: Boolean,
)

data class DrawingSymbolReferenceSlot(
    val slotId: DrawingSymbolSlotId,
    val role: DrawingSymbolReferenceRole,
    val required: Boolean,
)

data class DrawingSymbolHotspot(
    val hotspotId: DrawingSymbolHotspotId,
    val bounds: DrawingSymbolBounds,
)

data class DrawingSymbolProvenance(
    val source: String,
    val version: String,
) {
    init {
        require(source.isNotBlank()) { "Drawing symbol provenance source must not be blank." }
        require(version.isNotBlank()) { "Drawing symbol provenance version must not be blank." }
    }
}

data class DrawingSymbolAnatomy(
    val identity: DrawingSymbolIdentity?,
    val version: DrawingSymbolVersion?,
    val packageId: DrawingSymbolPackageId?,
    val domainTags: Set<DrawingSymbolTag>,
    val profileTags: Set<DrawingSymbolTag>,
    val lifecycle: DrawingSymbolLifecycle?,
    val primitives: List<GraphicPrimitive>,
    val anchors: List<DrawingSymbolAnchor>,
    val labelSlots: List<DrawingSymbolLabelSlot>,
    val referenceSlots: List<DrawingSymbolReferenceSlot>,
    val hotspots: List<DrawingSymbolHotspot>,
    val bounds: DrawingSymbolBounds?,
    val orientations: Set<DrawingSymbolOrientation>,
    val provenance: DrawingSymbolProvenance?,
    val forbiddenAuthorityClaims: Set<DrawingSymbolForbiddenAuthority> = emptySet(),
)
