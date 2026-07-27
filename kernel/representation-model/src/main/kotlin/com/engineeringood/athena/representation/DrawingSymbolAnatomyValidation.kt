package com.engineeringood.athena.representation

enum class DrawingSymbolDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class DrawingSymbolDiagnosticCode(val wireValue: String) {
    init {
        require(wireValue.isNotBlank()) { "Drawing symbol diagnostic code must not be blank." }
    }

    override fun toString(): String = wireValue
}

enum class DrawingSymbolDiagnosticCodes(val wireValue: String) {
    IDENTITY_MISSING("drawing.symbol.identity.missing"),
    VERSION_MISSING("drawing.symbol.version.missing"),
    PACKAGE_MISSING("drawing.symbol.package.missing"),
    DOMAIN_TAGS_MISSING("drawing.symbol.domain-tags.missing"),
    PROFILE_TAGS_MISSING("drawing.symbol.profile-tags.missing"),
    LIFECYCLE_MISSING("drawing.symbol.lifecycle.missing"),
    PRIMITIVE_MISSING("drawing.symbol.primitive.missing"),
    ANCHOR_MISSING("drawing.symbol.anchor.missing"),
    ANCHOR_DUPLICATE("drawing.symbol.anchor.duplicate"),
    ANCHOR_REQUIRED_MISSING("drawing.symbol.anchor.required-missing"),
    ANCHOR_OUT_OF_BOUNDS("drawing.symbol.anchor.out-of-bounds"),
    TERMINAL_ROLE_MISSING("drawing.symbol.anchor.terminal-role-missing"),
    LABEL_SLOT_MISSING("drawing.symbol.label-slot.missing"),
    LABEL_SLOT_DUPLICATE("drawing.symbol.label-slot.duplicate"),
    REFERENCE_SLOT_MISSING("drawing.symbol.reference-slot.missing"),
    REFERENCE_SLOT_DUPLICATE("drawing.symbol.reference-slot.duplicate"),
    HOTSPOT_MISSING("drawing.symbol.hotspot.missing"),
    HOTSPOT_DUPLICATE("drawing.symbol.hotspot.duplicate"),
    HOTSPOT_INVALID("drawing.symbol.hotspot.invalid"),
    HOTSPOT_OUT_OF_BOUNDS("drawing.symbol.hotspot.out-of-bounds"),
    BOUNDS_MISSING("drawing.symbol.bounds.missing"),
    BOUNDS_INVALID("drawing.symbol.bounds.invalid"),
    ORIENTATION_MISSING("drawing.symbol.orientation.missing"),
    PROVENANCE_MISSING("drawing.symbol.provenance.missing"),
    AUTHORITY_FORBIDDEN("drawing.symbol.authority-forbidden"),
}

data class DrawingSymbolDiagnostic(
    val code: DrawingSymbolDiagnosticCode,
    val severity: DrawingSymbolDiagnosticSeverity,
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

data class DrawingSymbolAnatomyValidationResult(
    val diagnostics: List<DrawingSymbolDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == DrawingSymbolDiagnosticSeverity.ERROR }

    fun toTransportPayload(): List<Map<String, String>> = diagnostics.map { it.toTransportMap() }
}

object DrawingSymbolAnatomyValidator {
    fun validate(anatomy: DrawingSymbolAnatomy): DrawingSymbolAnatomyValidationResult {
        val diagnostics = mutableListOf<DrawingSymbolDiagnostic>()
        if (anatomy.identity == null) missing(DrawingSymbolDiagnosticCodes.IDENTITY_MISSING, "identity", diagnostics)
        if (anatomy.version == null) missing(DrawingSymbolDiagnosticCodes.VERSION_MISSING, "version", diagnostics)
        if (anatomy.packageId == null) missing(DrawingSymbolDiagnosticCodes.PACKAGE_MISSING, "packageId", diagnostics)
        if (anatomy.domainTags.isEmpty()) missing(DrawingSymbolDiagnosticCodes.DOMAIN_TAGS_MISSING, "domainTags", diagnostics)
        if (anatomy.profileTags.isEmpty()) missing(DrawingSymbolDiagnosticCodes.PROFILE_TAGS_MISSING, "profileTags", diagnostics)
        if (anatomy.lifecycle == null) missing(DrawingSymbolDiagnosticCodes.LIFECYCLE_MISSING, "lifecycle", diagnostics)
        if (anatomy.primitives.isEmpty()) missing(DrawingSymbolDiagnosticCodes.PRIMITIVE_MISSING, "primitives", diagnostics)
        if (anatomy.anchors.isEmpty()) missing(DrawingSymbolDiagnosticCodes.ANCHOR_MISSING, "anchors", diagnostics)
        if (anatomy.labelSlots.isEmpty()) missing(DrawingSymbolDiagnosticCodes.LABEL_SLOT_MISSING, "labelSlots", diagnostics)
        if (anatomy.referenceSlots.isEmpty()) missing(DrawingSymbolDiagnosticCodes.REFERENCE_SLOT_MISSING, "referenceSlots", diagnostics)
        if (anatomy.hotspots.isEmpty()) missing(DrawingSymbolDiagnosticCodes.HOTSPOT_MISSING, "hotspots", diagnostics)
        if (anatomy.bounds == null) {
            missing(DrawingSymbolDiagnosticCodes.BOUNDS_MISSING, "bounds", diagnostics)
        } else if (!anatomy.bounds.isPositive()) {
            diagnostics += diagnostic(
                DrawingSymbolDiagnosticCodes.BOUNDS_INVALID,
                "bounds",
                "Drawing symbol bounds must have positive width and height.",
            )
        }
        if (anatomy.orientations.isEmpty()) missing(DrawingSymbolDiagnosticCodes.ORIENTATION_MISSING, "orientations", diagnostics)
        if (anatomy.provenance == null) missing(DrawingSymbolDiagnosticCodes.PROVENANCE_MISSING, "provenance", diagnostics)

        duplicateIds(anatomy.anchors, { it.anchorId.value }, DrawingSymbolDiagnosticCodes.ANCHOR_DUPLICATE, "anchors", diagnostics)
        duplicateIds(anatomy.labelSlots, { it.slotId.value }, DrawingSymbolDiagnosticCodes.LABEL_SLOT_DUPLICATE, "labelSlots", diagnostics)
        duplicateIds(anatomy.referenceSlots, { it.slotId.value }, DrawingSymbolDiagnosticCodes.REFERENCE_SLOT_DUPLICATE, "referenceSlots", diagnostics)
        duplicateIds(anatomy.hotspots, { it.hotspotId.value }, DrawingSymbolDiagnosticCodes.HOTSPOT_DUPLICATE, "hotspots", diagnostics)

        if (anatomy.anchors.isNotEmpty() && anatomy.anchors.none { it.required }) {
            diagnostics += diagnostic(
                DrawingSymbolDiagnosticCodes.ANCHOR_REQUIRED_MISSING,
                "anchors",
                "Drawing symbol anatomy requires at least one required anchor.",
            )
        }
        anatomy.anchors.forEach { anchor ->
            if (anchor.role == DrawingSymbolAnchorRole.TERMINAL && anchor.terminalRole == null) {
                diagnostics += diagnostic(
                    DrawingSymbolDiagnosticCodes.TERMINAL_ROLE_MISSING,
                    "anchors.${anchor.anchorId.value}",
                    "Terminal anchors require a terminal role.",
                )
            }
            if (anatomy.bounds != null && anatomy.bounds.isPositive() && !anatomy.bounds.contains(anchor.point)) {
                diagnostics += diagnostic(
                    DrawingSymbolDiagnosticCodes.ANCHOR_OUT_OF_BOUNDS,
                    "anchors.${anchor.anchorId.value}",
                    "Drawing symbol anchor must lie inside declared bounds.",
                )
            }
        }
        anatomy.hotspots.forEach { hotspot ->
            if (!hotspot.bounds.isPositive()) {
                diagnostics += diagnostic(
                    DrawingSymbolDiagnosticCodes.HOTSPOT_INVALID,
                    "hotspots.${hotspot.hotspotId.value}",
                    "Drawing symbol hotspot bounds must be positive.",
                )
            } else if (anatomy.bounds != null && anatomy.bounds.isPositive() && !anatomy.bounds.contains(hotspot.bounds)) {
                diagnostics += diagnostic(
                    DrawingSymbolDiagnosticCodes.HOTSPOT_OUT_OF_BOUNDS,
                    "hotspots.${hotspot.hotspotId.value}",
                    "Drawing symbol hotspot must lie inside declared bounds.",
                )
            }
        }
        if (anatomy.forbiddenAuthorityClaims.isNotEmpty()) {
            diagnostics += diagnostic(
                DrawingSymbolDiagnosticCodes.AUTHORITY_FORBIDDEN,
                "forbiddenAuthorityClaims",
                "Drawing symbol anatomy must not own: ${anatomy.forbiddenAuthorityClaims.sortedBy { it.name }.joinToString(",")}",
            )
        }
        return DrawingSymbolAnatomyValidationResult(
            diagnostics.sortedWith(compareBy({ it.code.wireValue }, { it.subject }, { it.message })),
        )
    }

    private fun <T> duplicateIds(
        values: List<T>,
        id: (T) -> String,
        code: DrawingSymbolDiagnosticCodes,
        subject: String,
        diagnostics: MutableList<DrawingSymbolDiagnostic>,
    ) {
        values.groupingBy(id).eachCount().filterValues { it > 1 }.keys.sorted().forEach { duplicate ->
            diagnostics += diagnostic(code, "$subject.$duplicate", "Drawing symbol member ids must be unique.")
        }
    }

    private fun DrawingSymbolBounds.isPositive(): Boolean = width > 0 && height > 0

    private fun DrawingSymbolBounds.contains(point: DrawingSymbolPoint): Boolean =
        point.x in x..(x + width) && point.y in y..(y + height)

    private fun DrawingSymbolBounds.contains(other: DrawingSymbolBounds): Boolean =
        other.x >= x && other.y >= y &&
            other.x + other.width <= x + width && other.y + other.height <= y + height

    private fun missing(
        code: DrawingSymbolDiagnosticCodes,
        subject: String,
        diagnostics: MutableList<DrawingSymbolDiagnostic>,
    ) {
        diagnostics += diagnostic(code, subject, "Drawing symbol anatomy requires `$subject`.")
    }

    private fun diagnostic(
        code: DrawingSymbolDiagnosticCodes,
        subject: String,
        message: String,
    ): DrawingSymbolDiagnostic = DrawingSymbolDiagnostic(
        code = DrawingSymbolDiagnosticCode(code.wireValue),
        severity = DrawingSymbolDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
