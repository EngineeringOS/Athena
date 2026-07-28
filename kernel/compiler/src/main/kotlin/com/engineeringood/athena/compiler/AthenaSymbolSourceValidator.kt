package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolBounds
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolDynamicLabelDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.RepresentationAnchorRole

internal object AthenaSymbolSourceValidator {
    private val identityPattern = Regex("[A-Za-z][A-Za-z0-9_-]*(\\.[A-Za-z][A-Za-z0-9_-]*)+")
    private val versionPattern = Regex(
        """(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)""" +
            """(?:-((?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)""" +
            """(?:\.(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*))?""" +
            """(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?""",
    )

    fun validate(file: String, symbol: SymbolDeclaration): List<AthenaRepresentationSourceDiagnostic> = buildList {
        val identity = symbol.identity
        when {
            identity == null -> addIssue(
                "symbol.identity.missing",
                file,
                symbol.span,
                "symbol.${symbol.name}.identity",
                "Symbol requires an identity.",
            )
            !identity.value.matches(identityPattern) -> addIssue(
                "symbol.identity.invalid",
                file,
                identity.span,
                "symbol.${symbol.name}.identity",
                "Symbol identity must be a stable dotted identifier.",
            )
        }
        val version = symbol.version
        when {
            version == null -> addIssue(
                "symbol.version.missing",
                file,
                symbol.span,
                "symbol.${symbol.name}.version",
                "Symbol requires a semantic version.",
            )
            !version.value.matches(versionPattern) -> addIssue(
                "symbol.version.invalid",
                file,
                version.span,
                "symbol.${symbol.name}.version",
                "Symbol version must use semantic version form major.minor.patch.",
            )
        }

        val graphic = symbol.graphic
        if (graphic == null) {
            addIssue("symbol.graphic.missing", file, symbol.span, "symbol.${symbol.name}.graphic", "Symbol requires a graphic body.")
            return@buildList
        }
        val svgResource = graphic.svgResource
        if (svgResource != null) {
            if (graphic.bounds != null || graphic.primitives.isNotEmpty() || graphic.labels.isNotEmpty()) {
                addIssue(
                    "symbol.graphic.body.ambiguous",
                    file,
                    graphic.span,
                    "symbol.${symbol.name}.graphic",
                    "Symbol graphic must choose either native primitives or one SVG reference.",
                )
            }
            val authoredAnchorRefs = symbol.anchors.mapNotNull { anchor -> anchor.primitiveRef?.value }.toSet()
            symbol.anchors.forEach { anchor -> validateAnchor(file, anchor, null, authoredAnchorRefs) }
            return@buildList
        }
        val bounds = graphic.bounds
        if (bounds == null) {
            addIssue(
                "symbol.graphic.bounds.missing",
                file,
                graphic.span,
                "symbol.${symbol.name}.graphic.bounds",
                "Symbol graphic requires explicit bounds.",
            )
        } else if (!bounds.isValidSymbolBounds()) {
            addIssue(
                "symbol.graphic.bounds.invalid",
                file,
                bounds.span,
                "symbol.${symbol.name}.graphic.bounds",
                "Symbol graphic bounds must be finite, positive, and supported by the compatibility shell.",
            )
        }
        if (graphic.primitives.isEmpty()) {
            addIssue(
                "symbol.graphic.primitive.missing",
                file,
                graphic.span,
                "symbol.${symbol.name}.graphic.primitives",
                "Symbol graphic requires at least one primitive.",
            )
        }
        graphic.primitives.groupBy { primitive -> primitive.id }
            .filterValues { duplicates -> duplicates.size > 1 }
            .toSortedMap()
            .forEach { (id, duplicates) ->
                addIssue(
                    "symbol.primitive.id.duplicate",
                    file,
                    duplicates[1].span,
                    "graphic.primitives.$id",
                    "Graphic primitive ids must be unique.",
                )
            }
        graphic.primitives.forEach { primitive -> validatePrimitive(file, primitive, bounds) }
        graphic.labels.groupBy(SymbolDynamicLabelDeclaration::id)
            .filterValues { duplicates -> duplicates.size > 1 }
            .toSortedMap()
            .forEach { (id, duplicates) ->
                addIssue(
                    "symbol.label.id.duplicate",
                    file,
                    duplicates[1].span,
                    "graphic.labels.$id",
                    "Dynamic label slot ids must be unique.",
                )
            }
        graphic.labels.forEach { label -> validateLabel(file, label, bounds) }

        val primitiveIds = graphic.primitives.map { primitive -> primitive.id }.toSet()
        symbol.anchors.groupBy { anchor -> anchor.id }
            .filterValues { duplicates -> duplicates.size > 1 }
            .toSortedMap()
            .forEach { (id, duplicates) ->
                addIssue(
                    "symbol.anchor.id.duplicate",
                    file,
                    duplicates[1].span,
                    "anchors.$id",
                    "Symbol anchor ids must be unique.",
                )
            }
        symbol.anchors.forEach { anchor -> validateAnchor(file, anchor, bounds, primitiveIds) }
    }.canonicalRepresentationDiagnostics()

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validatePrimitive(
        file: String,
        primitive: SymbolGraphicPrimitiveDeclaration,
        bounds: SymbolBounds?,
    ) {
        if (AthenaSymbolGraphicStyleRegistryV0.resolve(primitive.style) == null) {
            addIssue(
                "symbol.style.unknown",
                file,
                primitive.span,
                "graphic.primitives.${primitive.id}.style",
                "Unknown governed Symbol style `${primitive.style}`.",
            )
        }
        when (primitive) {
            is SymbolGraphicPrimitiveDeclaration.Line -> {
                if (!primitive.from.isFinite() || !primitive.to.isFinite() || primitive.from.samePosition(primitive.to)) {
                    addIssue("symbol.primitive.geometry.invalid", file, primitive.span, "graphic.primitives.${primitive.id}", "Line endpoints must be finite and distinct.")
                } else if (bounds.isValidAndDoesNotContain(primitive.from, primitive.to)) {
                    addIssue("symbol.primitive.point.out-of-bounds", file, primitive.span, "graphic.primitives.${primitive.id}", "Line endpoints must lie inside Symbol bounds.")
                }
            }
            is SymbolGraphicPrimitiveDeclaration.Polyline -> {
                if (primitive.points.size < 2 || primitive.points.any { point -> !point.isFinite() }) {
                    addIssue("symbol.primitive.polyline.invalid", file, primitive.span, "graphic.primitives.${primitive.id}", "Polyline requires at least two finite points.")
                } else if (bounds.isValidAndDoesNotContain(*primitive.points.toTypedArray())) {
                    addIssue("symbol.primitive.point.out-of-bounds", file, primitive.span, "graphic.primitives.${primitive.id}", "Polyline points must lie inside Symbol bounds.")
                }
            }
            is SymbolGraphicPrimitiveDeclaration.Arc -> {
                if (!primitive.center.isFinite() || !primitive.radius.isFinite() || primitive.radius <= 0.0 ||
                    !primitive.startAngleDegrees.isFinite() || !primitive.sweepAngleDegrees.isFinite() ||
                    primitive.sweepAngleDegrees == 0.0 || kotlin.math.abs(primitive.sweepAngleDegrees) > 360.0
                ) {
                    addIssue("symbol.primitive.arc.invalid", file, primitive.span, "graphic.primitives.${primitive.id}", "Arc requires a finite center, positive radius, finite start angle, and non-zero sweep no greater than 360 degrees.")
                } else if (bounds.isValidAndDoesNotContainCircle(primitive.center, primitive.radius)) {
                    addIssue("symbol.primitive.point.out-of-bounds", file, primitive.span, "graphic.primitives.${primitive.id}", "Arc bounds must lie inside Symbol bounds.")
                }
            }
            is SymbolGraphicPrimitiveDeclaration.Circle -> {
                if (!primitive.center.isFinite() || !primitive.radius.isFinite() || primitive.radius <= 0.0) {
                    addIssue("symbol.primitive.circle.invalid", file, primitive.span, "graphic.primitives.${primitive.id}", "Circle requires a finite center and positive finite radius.")
                } else if (bounds.isValidAndDoesNotContainCircle(primitive.center, primitive.radius)) {
                    addIssue("symbol.primitive.point.out-of-bounds", file, primitive.span, "graphic.primitives.${primitive.id}", "Circle bounds must lie inside Symbol bounds.")
                }
            }
            is SymbolGraphicPrimitiveDeclaration.Rectangle -> {
                if (!primitive.origin.isFinite() || !primitive.size.width.isFinite() || !primitive.size.height.isFinite() ||
                    primitive.size.width <= 0.0 || primitive.size.height <= 0.0
                ) {
                    addIssue("symbol.primitive.rectangle.invalid", file, primitive.span, "graphic.primitives.${primitive.id}", "Rectangle requires a finite origin and positive finite size.")
                } else if (bounds.isValidAndDoesNotContainRectangle(primitive.origin, primitive.size.width, primitive.size.height)) {
                    addIssue("symbol.primitive.point.out-of-bounds", file, primitive.span, "graphic.primitives.${primitive.id}", "Rectangle bounds must lie inside Symbol bounds.")
                }
            }
        }
    }

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validateLabel(
        file: String,
        label: SymbolDynamicLabelDeclaration,
        bounds: SymbolBounds?,
    ) {
        if (!label.origin.isFinite() || !label.size.width.isFinite() || !label.size.height.isFinite() ||
            label.size.width <= 0.0 || label.size.height <= 0.0
        ) {
            addIssue("symbol.label.bounds.invalid", file, label.span, "graphic.labels.${label.id}", "Dynamic label requires a finite origin and positive finite size.")
        } else if (bounds.isValidAndDoesNotContainRectangle(label.origin, label.size.width, label.size.height)) {
            addIssue("symbol.label.out-of-bounds", file, label.span, "graphic.labels.${label.id}", "Dynamic label bounds must lie inside Symbol bounds.")
        }
        if (label.role.value.toLabelRole() == null) {
            addIssue("symbol.label.role.unknown", file, label.role.span, "graphic.labels.${label.id}.role", "Unknown dynamic label role `${label.role.value}`.")
        }
        if (AthenaSymbolGraphicStyleRegistryV0.resolve(label.style) == null) {
            addIssue("symbol.style.unknown", file, label.span, "graphic.labels.${label.id}.style", "Unknown governed Symbol style `${label.style}`.")
        }
    }

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validateAnchor(
        file: String,
        anchor: SymbolAnchorDeclaration,
        bounds: SymbolBounds?,
        primitiveIds: Set<String>,
    ) {
        val primitiveRef = anchor.primitiveRef
        when {
            primitiveRef == null -> addIssue(
                "symbol.anchor.primitive-ref.missing",
                file,
                anchor.span,
                "anchors.${anchor.id}.primitiveRef",
                "Symbol anchor requires primitiveRef.",
            )
            primitiveRef.value !in primitiveIds -> addIssue(
                "symbol.anchor.primitive-ref.unresolved",
                file,
                primitiveRef.span,
                "anchors.${anchor.id}.primitiveRef",
                "Symbol anchor references missing primitive `${primitiveRef.value}`.",
            )
        }
        val point = anchor.point
        when {
            point == null -> addIssue(
                "symbol.anchor.point.missing",
                file,
                anchor.span,
                "anchors.${anchor.id}.point",
                "Symbol anchor requires an explicit point.",
            )
            !point.isFinite() -> addIssue(
                "symbol.anchor.point.invalid",
                file,
                point.span,
                "anchors.${anchor.id}.point",
                "Symbol anchor point must be finite.",
            )
            bounds != null && bounds.isValidSymbolBounds() && !bounds.contains(point.x, point.y) -> addIssue(
                "symbol.anchor.point.out-of-bounds",
                file,
                point.span,
                "anchors.${anchor.id}.point",
                "Symbol anchor point must lie inside Symbol bounds.",
            )
        }
        val role = anchor.role
        val resolvedRole = role?.value?.toAnchorRole()
        when {
            role == null -> addIssue(
                "symbol.anchor.role.missing",
                file,
                anchor.span,
                "anchors.${anchor.id}.role",
                "Symbol anchor requires a role.",
            )
            resolvedRole == null -> addIssue(
                "symbol.anchor.role.unknown",
                file,
                role.span,
                "anchors.${anchor.id}.role",
                "Unknown Symbol anchor role `${role.value}`.",
            )
        }
        if (resolvedRole == RepresentationAnchorRole.TERMINAL) {
            if (anchor.acceptedDirections.isEmpty()) {
                addIssue(
                    "symbol.anchor.direction.missing",
                    file,
                    anchor.span,
                    "anchors.${anchor.id}.acceptedDirections",
                    "Terminal anchor requires at least one accepted direction.",
                )
            }
            if (anchor.acceptedSignals.isEmpty()) {
                addIssue(
                    "symbol.anchor.signal.missing",
                    file,
                    anchor.span,
                    "anchors.${anchor.id}.acceptedSignals",
                    "Terminal anchor requires at least one accepted signal.",
                )
            }
        }
    }
}

private fun MutableList<AthenaRepresentationSourceDiagnostic>.addIssue(
    code: String,
    file: String,
    span: SourceSpan,
    subject: String,
    message: String,
) {
    add(representationDiagnostic(code, file, span, subject, message))
}

internal fun SymbolBounds.isValidSymbolBounds(): Boolean =
    x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite() &&
        width > 0.0 && height > 0.0 && width <= Int.MAX_VALUE.toDouble() && height <= Int.MAX_VALUE.toDouble()

internal fun SymbolBounds.contains(pointX: Double, pointY: Double): Boolean =
    pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height

private fun com.engineeringood.athena.language.SymbolPoint.isFinite(): Boolean = x.isFinite() && y.isFinite()

private fun com.engineeringood.athena.language.SymbolPoint.samePosition(other: com.engineeringood.athena.language.SymbolPoint): Boolean =
    x == other.x && y == other.y

private fun SymbolBounds?.isValidAndDoesNotContain(vararg points: com.engineeringood.athena.language.SymbolPoint): Boolean =
    this != null && isValidSymbolBounds() && points.any { point -> !contains(point.x, point.y) }

private fun SymbolBounds?.isValidAndDoesNotContainCircle(
    center: com.engineeringood.athena.language.SymbolPoint,
    radius: Double,
): Boolean = this != null && isValidSymbolBounds() &&
    (!contains(center.x - radius, center.y - radius) || !contains(center.x + radius, center.y + radius))

private fun SymbolBounds?.isValidAndDoesNotContainRectangle(
    origin: com.engineeringood.athena.language.SymbolPoint,
    width: Double,
    height: Double,
): Boolean = this != null && isValidSymbolBounds() &&
    (!contains(origin.x, origin.y) || !contains(origin.x + width, origin.y + height))

internal fun String.isSafeRelativeSvgPath(): Boolean =
    endsWith(".svg", ignoreCase = true) &&
        !startsWith("/") &&
        !startsWith("\\") &&
        !contains(":") &&
        split('/', '\\').none { segment -> segment == ".." || segment.isBlank() }

internal fun String.toAnchorRole(): RepresentationAnchorRole? = when (this) {
    "terminal" -> RepresentationAnchorRole.TERMINAL
    "reference" -> RepresentationAnchorRole.REFERENCE
    "placement" -> RepresentationAnchorRole.PLACEMENT
    "label" -> RepresentationAnchorRole.LABEL
    "hotspot" -> RepresentationAnchorRole.HOTSPOT
    else -> null
}

internal fun String.toLabelRole(): PresentationLabelRole? = when (this) {
    "device-tag" -> PresentationLabelRole.DEVICE_TAG
    "terminal-label" -> PresentationLabelRole.TERMINAL_LABEL
    "reference" -> PresentationLabelRole.ROUTE_LABEL
    "model" -> PresentationLabelRole.COMPONENT_LABEL
    else -> null
}
