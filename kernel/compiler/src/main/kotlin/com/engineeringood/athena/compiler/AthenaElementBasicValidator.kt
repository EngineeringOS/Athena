package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.ElementDeclaration

internal object AthenaElementBasicValidator {
    private val identityPattern = Regex("[A-Za-z][A-Za-z0-9_-]*(\\.[A-Za-z][A-Za-z0-9_-]*)+")
    private val versionPattern = Regex(
        """(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)""" +
            """(?:-((?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)""" +
            """(?:\.(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*))?""" +
            """(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?""",
    )

    fun validate(file: String, element: ElementDeclaration): List<AthenaRepresentationSourceDiagnostic> = buildList {
        val subject = "element.${element.name}"
        val identity = element.identity
        when {
            identity == null -> add(issue("element.identity.missing", file, element.span, "$subject.identity", "Element requires an identity."))
            !identity.value.matches(identityPattern) -> add(issue("element.identity.invalid", file, identity.span, "$subject.identity", "Element identity must be a stable dotted identifier."))
        }
        val version = element.version
        when {
            version == null -> add(issue("element.version.missing", file, element.span, "$subject.version", "Element requires a semantic version."))
            !version.value.matches(versionPattern) -> add(issue("element.version.invalid", file, version.span, "$subject.version", "Element version must use semantic version form major.minor.patch."))
        }
        val graphic = element.graphic
        if (graphic != null) {
            val svgResource = graphic.svgResource
            if (svgResource != null) {
                if (element.bounds != null || element.children.isNotEmpty() || element.exportedAnchors.isNotEmpty() || element.exportedLabels.isNotEmpty()) {
                    add(issue("element.graphic.body.ambiguous", file, graphic.span, "$subject.graphic", "Element graphic must choose either child composition or one SVG reference."))
                }
                return@buildList
            }
        }
        val bounds = element.bounds
        when {
            bounds == null -> add(issue("element.bounds.missing", file, element.span, "$subject.bounds", "Element requires explicit bounds."))
            !bounds.isValidSymbolBounds() -> add(issue("element.bounds.invalid", file, bounds.span, "$subject.bounds", "Element bounds must be finite and positive."))
        }
        if (element.children.isEmpty()) {
            add(issue("element.child.missing", file, element.span, "$subject.children", "Element requires at least one Symbol child."))
        }
        element.children.groupBy(ElementChildDeclaration::id).filterValues { it.size > 1 }.toSortedMap().forEach { (id, duplicates) ->
            add(issue("element.child.id.duplicate", file, duplicates[1].headerSpan, "$subject.children.$id", "Element child ids must be unique."))
        }
        element.children.forEach { child -> validateChild(file, subject, child, this) }
        element.children.mapNotNull { child -> child.validZOrder()?.let { it to child } }
            .groupBy({ it.first }, { it.second }).filterValues { it.size > 1 }.toSortedMap().forEach { (_, duplicates) ->
                val duplicate = duplicates[1]
                add(issue("element.child.z-order.duplicate", file, requireNotNull(duplicate.zOrder).span, "$subject.children.${duplicate.id}.zOrder", "Element child z-orders must be unique."))
            }
        element.exportedAnchors.groupBy { export -> export.id }.filterValues { it.size > 1 }.toSortedMap().forEach { (id, duplicates) ->
            add(issue("element.export.id.duplicate", file, duplicates[1].span, "$subject.exports.$id", "Element exported anchor ids must be unique."))
        }
        element.exportedLabels.groupBy { export -> export.id }.filterValues { it.size > 1 }.toSortedMap().forEach { (id, duplicates) ->
            add(issue("element.export.label.id.duplicate", file, duplicates[1].span, "$subject.labelExports.$id", "Element exported label ids must be unique."))
        }
    }.canonicalRepresentationDiagnostics()

    private fun validateChild(
        file: String,
        elementSubject: String,
        child: ElementChildDeclaration,
        diagnostics: MutableList<AthenaRepresentationSourceDiagnostic>,
    ) {
        val subject = "$elementSubject.children.${child.id}"
        if (child.symbolIdentity == null) diagnostics += issue("element.child.symbol.missing", file, child.headerSpan, "$subject.symbol", "Element child requires one Symbol identity.")
        if (child.translate == null) diagnostics += issue("element.child.translate.missing", file, child.headerSpan, "$subject.translate", "Element child requires one explicit translate transform.")
        if (child.rotate == null) diagnostics += issue("element.child.rotate.missing", file, child.headerSpan, "$subject.rotate", "Element child requires one explicit rotate transform.")
        if (child.scale == null) diagnostics += issue("element.child.scale.missing", file, child.headerSpan, "$subject.scale", "Element child requires one explicit scale transform.")
        if (child.zOrder == null) diagnostics += issue("element.child.z-order.missing", file, child.headerSpan, "$subject.zOrder", "Element child requires one explicit integer zOrder.")
        child.translate?.takeUnless { it.x.isFinite() && it.y.isFinite() }?.let {
            diagnostics += issue("element.child.translate.invalid", file, it.span, "$subject.translate", "Element child translate values must be finite.")
        }
        child.rotate?.takeUnless { it.value.isFinite() }?.let {
            diagnostics += issue("element.child.rotate.invalid", file, it.span, "$subject.rotate", "Element child rotation must be finite.")
        }
        child.scale?.takeUnless { it.x.isFinite() && it.y.isFinite() && it.x > 0.0 && it.y > 0.0 }?.let {
            diagnostics += issue("element.child.scale.invalid", file, it.span, "$subject.scale", "Element child scale values must be finite and positive.")
        }
        child.zOrder?.takeUnless { it.value.isFinite() && it.value % 1.0 == 0.0 && it.value in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble() }?.let {
            diagnostics += issue("element.child.z-order.invalid", file, it.span, "$subject.zOrder", "Element child zOrder must be a bounded integer.")
        }
    }
}
