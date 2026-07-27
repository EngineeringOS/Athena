package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.SymbolBounds
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object AthenaElementReferenceValidator {
    fun validate(
        declarations: List<AuthoredRepresentationDeclaration>,
        identities: List<RepresentationIdentityOccurrence>,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        val candidatesByKey = identities.groupBy { occurrence -> DefinitionKey(occurrence.libraryId, occurrence.identity) }
        val byKey = candidatesByKey.mapNotNull { (key, candidates) ->
            candidates.singleOrNull()?.let { candidate -> key to candidate }
        }.toMap()
        val byIdentity = identities.groupBy(RepresentationIdentityOccurrence::identity)
        declarations.filter { it.declaration is ElementDeclaration }.forEach { occurrence ->
            val element = occurrence.declaration as ElementDeclaration
            if (element.graphic?.svgResource != null) return@forEach
            val subject = "element.${element.name}"
            val childrenById = element.children.associateBy(ElementChildDeclaration::id)
            element.children.forEach childLoop@{ child ->
                val identity = child.symbolIdentity ?: return@childLoop
                val key = DefinitionKey(occurrence.libraryId, identity.value)
                if (candidatesByKey[key].orEmpty().size > 1) return@childLoop
                val target = byKey[key]
                when {
                    target == null && byIdentity[identity.value].orEmpty().isNotEmpty() -> add(
                        issue(
                            "element.child.symbol.cross-library",
                            occurrence.file,
                            identity.span,
                            "$subject.children.${child.id}.symbol",
                            "Element child `${child.id}` cannot resolve cross-library representation `${identity.value}`.",
                        ),
                    )
                    target == null -> add(
                        issue(
                            "element.child.symbol.unresolved",
                            occurrence.file,
                            identity.span,
                            "$subject.children.${child.id}.symbol",
                            "Element child `${child.id}` references missing same-library Symbol `${identity.value}`.",
                        ),
                    )
                    target.declaration is ElementDeclaration -> add(
                        issue(
                            "element.child.kind.invalid",
                            occurrence.file,
                            identity.span,
                            "$subject.children.${child.id}.symbol",
                            "Element child `${child.id}` must resolve to an atomic Symbol, not Element `${identity.value}`.",
                        ),
                    )
                }
            }
            validateExportsAndBounds(occurrence, element, childrenById, byKey, this)
        }
    }.canonicalRepresentationDiagnostics()

    private fun validateExportsAndBounds(
        occurrence: AuthoredRepresentationDeclaration,
        element: ElementDeclaration,
        childrenById: Map<String, ElementChildDeclaration>,
        byKey: Map<DefinitionKey, RepresentationIdentityOccurrence>,
        diagnostics: MutableList<AthenaRepresentationSourceDiagnostic>,
    ) {
        val subject = "element.${element.name}"
        val exportsByChildAnchor = element.exportedAnchors.groupBy { export -> export.childId.value to export.childAnchorId.value }
        exportsByChildAnchor.filterValues { it.size > 1 }
            .toSortedMap(compareBy<Pair<String, String>>({ it.first }, { it.second }))
            .forEach { (key, duplicates) ->
                val childId = key.first
                val anchorId = key.second
                diagnostics += issue(
                    "element.export.child-anchor.duplicate",
                    occurrence.file,
                    duplicates[1].span,
                    "$subject.children.$childId.anchors.$anchorId",
                    "Child anchor `$childId.$anchorId` must be exported exactly once.",
                )
            }
        element.exportedAnchors.forEach { export ->
            val child = childrenById[export.childId.value]
            if (child == null) {
                diagnostics += issue("element.export.child.unresolved", occurrence.file, export.referenceSpan, "$subject.exports.${export.id}", "Element export `${export.id}` references missing child `${export.childId.value}`.")
                return@forEach
            }
        }
        val labelExportsByChildSlot = element.exportedLabels.groupBy { export -> export.childId.value to export.childLabelId.value }
        labelExportsByChildSlot.filterValues { it.size > 1 }
            .toSortedMap(compareBy<Pair<String, String>>({ it.first }, { it.second }))
            .forEach { (key, duplicates) ->
                diagnostics += issue(
                    "element.export.child-label.duplicate",
                    occurrence.file,
                    duplicates[1].span,
                    "$subject.children.${key.first}.labels.${key.second}",
                    "Child label `${key.first}.${key.second}` must be exported at most once.",
                )
            }
        element.exportedLabels.forEach { export ->
            val child = childrenById[export.childId.value]
            if (child == null) {
                diagnostics += issue("element.export.label.child.unresolved", occurrence.file, export.referenceSpan, "$subject.labelExports.${export.id}", "Element label export `${export.id}` references missing child `${export.childId.value}`.")
                return@forEach
            }
        }
        element.children.forEach { child ->
            val target = child.symbolIdentity?.let { byKey[DefinitionKey(occurrence.libraryId, it.value)] }
            val symbol = target?.declaration as? SymbolDeclaration ?: return@forEach
            val elementBounds = element.bounds
            val childBounds = symbol.graphic?.bounds
            if (elementBounds != null && elementBounds.isValidSymbolBounds() && childBounds != null && childBounds.isValidSymbolBounds() && child.hasValidTransform()) {
                val transformed = transformBounds(childBounds, child)
                if (!elementBounds.contains(transformed)) {
                    diagnostics += issue(
                        "element.child.bounds.out-of-element",
                        occurrence.file,
                        requireNotNull(child.translate).span,
                        "$subject.children.${child.id}.bounds",
                        "Transformed child `${child.id}` must lie inside Element bounds.",
                    )
                }
            }
        }
    }

    private fun ElementChildDeclaration.hasValidTransform(): Boolean =
        translate?.let { it.x.isFinite() && it.y.isFinite() } == true &&
            rotate?.value?.isFinite() == true &&
            scale?.let { it.x.isFinite() && it.y.isFinite() && it.x > 0.0 && it.y > 0.0 } == true

    private fun transformBounds(bounds: SymbolBounds, child: ElementChildDeclaration): GraphicBounds {
        val points = listOf(
            GraphicPoint(bounds.x, bounds.y),
            GraphicPoint(bounds.x + bounds.width, bounds.y),
            GraphicPoint(bounds.x + bounds.width, bounds.y + bounds.height),
            GraphicPoint(bounds.x, bounds.y + bounds.height),
        ).map { point -> transformPoint(point, child) }
        val minX = points.minOf(GraphicPoint::x)
        val minY = points.minOf(GraphicPoint::y)
        return GraphicBounds(minX, minY, points.maxOf(GraphicPoint::x) - minX, points.maxOf(GraphicPoint::y) - minY)
    }

    private fun transformPoint(point: GraphicPoint, child: ElementChildDeclaration): GraphicPoint {
        val scale = requireNotNull(child.scale)
        val radians = requireNotNull(child.rotate).value * PI / 180.0
        val scaledX = point.x * scale.x
        val scaledY = point.y * scale.y
        val translated = requireNotNull(child.translate)
        return GraphicPoint(
            scaledX * cos(radians) - scaledY * sin(radians) + translated.x,
            scaledX * sin(radians) + scaledY * cos(radians) + translated.y,
        )
    }

    private fun SymbolBounds.contains(other: GraphicBounds): Boolean =
        other.x >= x && other.y >= y && other.x + other.width <= x + width && other.y + other.height <= y + height
}
