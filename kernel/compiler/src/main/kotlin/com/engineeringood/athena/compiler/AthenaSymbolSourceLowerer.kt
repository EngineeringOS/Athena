package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicPrimitiveIrValidator
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationContractValidator
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationValidationInput
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.RepresentationVersion
import kotlin.math.max
import kotlin.math.min

internal object AthenaSymbolSourceLowerer {
    fun lower(
        file: String,
        libraryId: RepresentationLibraryId,
        symbol: SymbolDeclaration,
        resourcesById: Map<String, ResolvedSourceResource>,
    ): AthenaRepresentationLoweringResult {
        val identity = requireNotNull(symbol.identity)
        val version = requireNotNull(symbol.version)
        val graphic = requireNotNull(symbol.graphic)
        val svgResource = graphic.svgResource
        if (svgResource != null) {
            val resource = resourcesById[svgResource.value] ?: return AthenaRepresentationLoweringResult(
                null,
                listOf(
                    representationDiagnostic(
                        code = "symbol.graphic.resource.unresolved",
                        file = file,
                        span = svgResource.span,
                        subject = "symbol.${symbol.name}.graphic.resource",
                        message = "Symbol graphic must reference a declared source-local resource id.",
                    ),
                ),
            )
            return lowerSvgGraphic(file, libraryId, symbol, identity.value, version.value, resource.declaration)
        }
        val bounds = requireNotNull(graphic.bounds)
        val graphicBounds = GraphicBounds(bounds.x, bounds.y, bounds.width, bounds.height)
        val graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(identity.value),
            bounds = graphicBounds,
            primitives = graphic.primitives.map { primitive -> primitive.toGraphicPrimitive(graphicBounds) },
            styleTokens = (graphic.primitives.map { primitive -> primitive.style } + graphic.labels.map { label -> label.style })
                .distinct().sorted().map { styleId ->
                requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(styleId))
            },
            provenanceSources = listOf(file),
        )
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId(identity.value),
            libraryId = libraryId,
            version = RepresentationVersion(version.value),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance(file),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            labelSlots = graphic.labels.map { label ->
                RepresentationLabelSlot(
                    slotId = RepresentationLabelSlotId(label.id),
                    role = requireNotNull(label.role.value.toLabelRole()),
                    origin = GraphicPoint(label.origin.x, label.origin.y),
                    bounds = GraphicBounds(label.origin.x, label.origin.y, label.size.width, label.size.height),
                    styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(label.style)).styleTokenId,
                )
            },
            variants = listOf(RepresentationVariantId("standard")),
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = graphicBody,
            anchors = emptyList(),
        )
        val anchorResults = symbol.anchors.map { anchor ->
            lowerAnchor(
                file = file,
                anchor = anchor,
                primitiveIds = graphicBody.primitives.map { primitive -> primitive.primitiveId }.toSet(),
                geometryPrimitiveIdsByRef = emptyMap(),
                allowPrimitiveIdFallback = true,
            )
        }
        val anchorDiagnostics = anchorResults.mapNotNull { it.diagnostic }.canonicalRepresentationDiagnostics()
        if (anchorDiagnostics.isNotEmpty()) {
            return AthenaRepresentationLoweringResult(null, anchorDiagnostics)
        }
        val definitionWithAnchors = definition.copy(anchors = anchorResults.mapNotNull { it.anchor })

        val diagnostics = buildList {
            GraphicPrimitiveIrValidator.validate(graphicBody).diagnostics.forEach { issue ->
                add(
                    representationDiagnostic(
                        code = "symbol.graphic.invalid",
                        file = file,
                        span = graphic.span,
                        subject = issue.subject,
                        message = "${issue.code.wireValue}: ${issue.message}",
                    ),
                )
            }
            RepresentationContractValidator.validate(
                RepresentationValidationInput(
                    allowedLibraries = setOf(libraryId),
                    policies = emptyList(),
                    definitions = listOf(definitionWithAnchors),
                    occurrences = emptyList(),
                ),
            ).diagnostics.forEach { issue ->
                add(
                    representationDiagnostic(
                        code = "symbol.definition.invalid",
                        file = file,
                        span = symbol.span,
                        subject = issue.subjectId?.value ?: "symbol.${symbol.name}",
                        message = "${issue.code.wireValue}: ${issue.message}",
                    ),
                )
            }
        }.canonicalRepresentationDiagnostics()
        return AthenaRepresentationLoweringResult(definitionWithAnchors.takeIf { diagnostics.isEmpty() }, diagnostics)
    }

    private fun lowerSvgGraphic(
        file: String,
        libraryId: RepresentationLibraryId,
        symbol: SymbolDeclaration,
        identity: String,
        version: String,
        resource: com.engineeringood.athena.language.RepresentationResourceDeclaration,
    ): AthenaRepresentationLoweringResult {
        val svg = AthenaSvgGraphicBodyCompiler.compile(file, identity, resource)
        if (svg.diagnostics.isNotEmpty()) return AthenaRepresentationLoweringResult(null, svg.diagnostics)
        val graphicBody = requireNotNull(svg.document)
        val bounds = requireNotNull(graphicBody.bounds)
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId(identity),
            libraryId = libraryId,
            version = RepresentationVersion(version),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance(file),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            labelSlots = emptyList(),
            variants = listOf(RepresentationVariantId("standard")),
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = graphicBody,
            anchors = emptyList(),
        )
        val anchorResults = symbol.anchors.map { anchor ->
            lowerAnchor(
                file = file,
                anchor = anchor,
                primitiveIds = graphicBody.primitives.map { primitive -> primitive.primitiveId }.toSet(),
                geometryPrimitiveIdsByRef = svg.primitiveIdsByGeometryRef,
                allowPrimitiveIdFallback = false,
            )
        }
        val anchorDiagnostics = anchorResults.mapNotNull { it.diagnostic }.canonicalRepresentationDiagnostics()
        if (anchorDiagnostics.isNotEmpty()) {
            return AthenaRepresentationLoweringResult(null, anchorDiagnostics)
        }
        val definitionWithAnchors = definition.copy(anchors = anchorResults.mapNotNull { it.anchor })
        val diagnostics = validateLoweredSvg(file, symbol.span, definitionWithAnchors, graphicBody).canonicalRepresentationDiagnostics()
        return AthenaRepresentationLoweringResult(definitionWithAnchors.takeIf { diagnostics.isEmpty() }, diagnostics)
    }

    private fun validateLoweredSvg(
        file: String,
        span: com.engineeringood.athena.language.SourceSpan,
        definition: RepresentationDefinition,
        graphicBody: GraphicPrimitiveDocument,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        GraphicPrimitiveIrValidator.validate(graphicBody).diagnostics.forEach { issue ->
            add(representationDiagnostic("symbol.graphic.invalid", file, span, issue.subject, "${issue.code.wireValue}: ${issue.message}"))
        }
        RepresentationContractValidator.validate(
            RepresentationValidationInput(
                allowedLibraries = setOf(definition.libraryId),
                policies = emptyList(),
                definitions = listOf(definition),
                occurrences = emptyList(),
            ),
        ).diagnostics.forEach { issue ->
            add(
                representationDiagnostic(
                    "symbol.definition.invalid",
                    file,
                    span,
                    issue.subjectId?.value ?: "symbol.${definition.symbolId.value}",
                    "${issue.code.wireValue}: ${issue.message}",
                ),
            )
        }
    }
}

private fun SymbolGraphicPrimitiveDeclaration.toGraphicPrimitive(documentBounds: GraphicBounds): GraphicPrimitive = when (this) {
    is SymbolGraphicPrimitiveDeclaration.Line -> GraphicPrimitive.Line(
        primitiveId = GraphicPrimitiveId(id),
        bounds = lineBounds(from.x, from.y, to.x, to.y, documentBounds),
        start = GraphicPoint(from.x, from.y),
        end = GraphicPoint(to.x, to.y),
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Polyline -> GraphicPrimitive.Polyline(
        primitiveId = GraphicPrimitiveId(id),
        bounds = pointBounds(points.map { GraphicPoint(it.x, it.y) }, documentBounds),
        points = points.map { GraphicPoint(it.x, it.y) },
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Arc -> GraphicPrimitive.Arc(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(center.x - radius, center.y - radius, radius * 2.0, radius * 2.0),
        center = GraphicPoint(center.x, center.y),
        radius = radius,
        startAngleDegrees = startAngleDegrees,
        sweepAngleDegrees = sweepAngleDegrees,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Circle -> GraphicPrimitive.Circle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(center.x - radius, center.y - radius, radius * 2.0, radius * 2.0),
        center = GraphicPoint(center.x, center.y),
        radius = radius,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Rectangle -> GraphicPrimitive.Rectangle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(origin.x, origin.y, size.width, size.height),
        cornerRadius = 0.0,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistry.resolve(style)).styleTokenId,
    )
}

private fun pointBounds(points: List<GraphicPoint>, document: GraphicBounds): GraphicBounds {
    val x = positiveAxisBounds(points.minOf(GraphicPoint::x), points.maxOf(GraphicPoint::x), document.x, document.width)
    val y = positiveAxisBounds(points.minOf(GraphicPoint::y), points.maxOf(GraphicPoint::y), document.y, document.height)
    return GraphicBounds(x.first, y.first, x.second, y.second)
}

private fun lineBounds(
    fromX: Double,
    fromY: Double,
    toX: Double,
    toY: Double,
    document: GraphicBounds,
): GraphicBounds {
    val x = positiveAxisBounds(min(fromX, toX), max(fromX, toX), document.x, document.width)
    val y = positiveAxisBounds(min(fromY, toY), max(fromY, toY), document.y, document.height)
    return GraphicBounds(x.first, y.first, x.second, y.second)
}

private fun positiveAxisBounds(
    minimum: Double,
    maximum: Double,
    containerStart: Double,
    containerSize: Double,
): Pair<Double, Double> {
    if (maximum > minimum) return minimum to (maximum - minimum)
    val size = min(0.001, containerSize)
    val start = (minimum - size / 2.0).coerceIn(containerStart, containerStart + containerSize - size)
    return start to size
}

private fun lowerAnchor(
    file: String,
    anchor: SymbolAnchorDeclaration,
    primitiveIds: Set<GraphicPrimitiveId>,
    geometryPrimitiveIdsByRef: Map<String, List<GraphicPrimitiveId>>,
    allowPrimitiveIdFallback: Boolean,
): LoweredAnchor {
    val ref = requireNotNull(anchor.ref) {
        "Symbol anchor ref is validated before lowering."
    }
    val geometryRef = ref.value
    val primitiveId = geometryPrimitiveIdsByRef[geometryRef]?.singleOrNull()
        ?: if (allowPrimitiveIdFallback) primitiveIds.singleOrNull { candidate -> candidate.value == geometryRef } else null
    if (primitiveId == null) {
        return LoweredAnchor(
            anchor = null,
            diagnostic = representationDiagnostic(
                code = "symbol.anchor.ref.unresolved",
                file = file,
                span = ref.span,
                subject = "anchors.${anchor.id}.ref",
                message = "Symbol anchor references missing geometry `${geometryRef}`.",
            ),
        )
    }
    val role = requireNotNull(anchor.role) {
        "Symbol anchor role is validated before lowering."
    }
    val point = requireNotNull(anchor.point) {
        "Symbol anchor point is validated before lowering."
    }
    return LoweredAnchor(
        anchor = RepresentationAnchorContract(
            anchorId = RepresentationAnchorId(anchor.id),
            geometryRef = geometryRef,
            primitiveId = primitiveId,
            point = GraphicPoint(point.x, point.y),
            role = requireNotNull(role.value.toAnchorRole()),
            required = true,
        ),
        diagnostic = null,
    )
}

private data class LoweredAnchor(
    val anchor: RepresentationAnchorContract?,
    val diagnostic: AthenaRepresentationSourceDiagnostic?,
)
