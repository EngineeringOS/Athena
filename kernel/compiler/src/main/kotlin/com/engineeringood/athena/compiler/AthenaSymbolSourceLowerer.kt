package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolBounds
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicPrimitiveIrValidator
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationAnatomyAuthority
import com.engineeringood.athena.representation.PresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationBodyAuthority
import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationContractValidator
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationDirectionPredicate
import com.engineeringood.athena.representation.RepresentationId
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSignalPredicate
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationValidationInput
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.RepresentationVersion
import kotlin.math.ceil
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
                requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(styleId))
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
            anatomy = compatibilityAnatomy(identity.value, bounds),
            labelSlots = graphic.labels.map { label ->
                RepresentationLabelSlot(
                    slotId = RepresentationLabelSlotId(label.id),
                    role = requireNotNull(label.role.value.toLabelRole()),
                    origin = GraphicPoint(label.origin.x, label.origin.y),
                    bounds = GraphicBounds(label.origin.x, label.origin.y, label.size.width, label.size.height),
                    styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(label.style)).styleTokenId,
                )
            },
            variants = listOf(RepresentationVariantId("standard")),
            bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = graphicBody,
            anchors = symbol.anchors.map(SymbolAnchorDeclaration::toRepresentationAnchor),
        )

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
                    definitions = listOf(definition),
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
        return AthenaRepresentationLoweringResult(definition.takeIf { diagnostics.isEmpty() }, diagnostics)
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
        val anchorPrimitiveId = graphicBody.primitives.firstOrNull()?.primitiveId
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId(identity),
            libraryId = libraryId,
            version = RepresentationVersion(version),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance(file),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            anatomy = compatibilityAnatomy(identity, SymbolBounds(bounds.x, bounds.y, bounds.width, bounds.height, resource.span)),
            labelSlots = emptyList(),
            variants = listOf(RepresentationVariantId("standard")),
            bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
            definitionKind = RepresentationDefinitionKind.SYMBOL,
            graphicBody = graphicBody,
            anchors = symbol.anchors.map { anchor -> anchor.toRepresentationAnchor(anchorPrimitiveId) },
        )
        val diagnostics = validateLoweredSvg(file, symbol.span, definition, graphicBody).canonicalRepresentationDiagnostics()
        return AthenaRepresentationLoweringResult(definition.takeIf { diagnostics.isEmpty() }, diagnostics)
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
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Polyline -> GraphicPrimitive.Polyline(
        primitiveId = GraphicPrimitiveId(id),
        bounds = pointBounds(points.map { GraphicPoint(it.x, it.y) }, documentBounds),
        points = points.map { GraphicPoint(it.x, it.y) },
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Arc -> GraphicPrimitive.Arc(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(center.x - radius, center.y - radius, radius * 2.0, radius * 2.0),
        center = GraphicPoint(center.x, center.y),
        radius = radius,
        startAngleDegrees = startAngleDegrees,
        sweepAngleDegrees = sweepAngleDegrees,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Circle -> GraphicPrimitive.Circle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(center.x - radius, center.y - radius, radius * 2.0, radius * 2.0),
        center = GraphicPoint(center.x, center.y),
        radius = radius,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(style)).styleTokenId,
    )
    is SymbolGraphicPrimitiveDeclaration.Rectangle -> GraphicPrimitive.Rectangle(
        primitiveId = GraphicPrimitiveId(id),
        bounds = GraphicBounds(origin.x, origin.y, size.width, size.height),
        cornerRadius = 0.0,
        styleTokenId = requireNotNull(AthenaSymbolGraphicStyleRegistryV0.resolve(style)).styleTokenId,
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

private fun SymbolAnchorDeclaration.toRepresentationAnchor(
    primitiveOverride: GraphicPrimitiveId? = null,
): RepresentationAnchorContract = RepresentationAnchorContract(
    anchorId = RepresentationAnchorId(id),
    primitiveId = primitiveOverride ?: GraphicPrimitiveId(requireNotNull(primitiveRef).value),
    point = requireNotNull(point).let { value -> GraphicPoint(value.x, value.y) },
    role = requireNotNull(requireNotNull(role).value.toAnchorRole()),
    required = true,
    acceptedDirections = acceptedDirections.map { field -> field.value.toDirectionPredicate() }.toSet(),
    acceptedSignals = acceptedSignals.map { field -> RepresentationSignalPredicate(field.value) }.toSet(),
)

private fun String.toDirectionPredicate(): RepresentationDirectionPredicate = when (this) {
    "in" -> RepresentationDirectionPredicate.IN
    "out" -> RepresentationDirectionPredicate.OUT
    "bidirectional" -> RepresentationDirectionPredicate.BIDIRECTIONAL
    else -> error("ANTLR direction predicate escaped authored AST boundary: $this")
}

internal fun compatibilityAnatomy(identity: String, bounds: SymbolBounds) = PresentationAnatomy(
    representationId = RepresentationId(identity),
    context = RepresentationContext.ELECTRICAL_SCHEMATIC,
    bounds = PresentationBounds(GridUnit(ceil(bounds.width).toInt()), GridUnit(ceil(bounds.height).toInt())),
    hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
    primitives = emptyList(),
    terminals = emptyList(),
    labelAnchors = emptyList(),
    authority = PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
)
