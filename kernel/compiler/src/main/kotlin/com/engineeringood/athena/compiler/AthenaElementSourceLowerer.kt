package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.SymbolBounds
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicPrimitiveIrValidator
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicTransform
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationBodyAuthority
import com.engineeringood.athena.representation.RepresentationCompositionChild
import com.engineeringood.athena.representation.RepresentationCompositionChildId
import com.engineeringood.athena.representation.RepresentationContractValidator
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationExportedAnchor
import com.engineeringood.athena.representation.RepresentationExportedLabelSlot
import com.engineeringood.athena.representation.RepresentationIntrinsicComposition
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationValidationInput
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.RepresentationVersion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object AthenaElementSourceLowerer {
    fun lower(
        file: String,
        libraryId: RepresentationLibraryId,
        element: ElementDeclaration,
        symbols: Map<DefinitionKey, RepresentationDefinition>,
        resourcesById: Map<String, ResolvedSourceResource>,
    ): AthenaRepresentationLoweringResult {
        val identity = requireNotNull(element.identity)
        val version = requireNotNull(element.version)
        val graphic = element.graphic
        val svgResource = graphic?.svgResource
        if (svgResource != null) {
            val resource = resourcesById[svgResource.value] ?: return AthenaRepresentationLoweringResult(
                null,
                listOf(
                    representationDiagnostic(
                        code = "element.graphic.resource.unresolved",
                        file = file,
                        span = svgResource.span,
                        subject = "element.${element.name}.graphic.resource",
                        message = "Element graphic must reference a declared source-local resource id.",
                    ),
                ),
            )
            return lowerSvgGraphic(file, libraryId, element, identity.value, version.value, resource.declaration)
        }
        val bounds = requireNotNull(element.bounds)
        val unavailableChildren = element.children.mapNotNull { child ->
            val symbolIdentity = requireNotNull(child.symbolIdentity).value
            if (DefinitionKey(libraryId, symbolIdentity) in symbols) {
                null
            } else {
                representationDiagnostic(
                    code = "element.child.symbol.unavailable",
                    file = file,
                    span = child.span,
                    subject = symbolIdentity,
                    message = "Child Symbol '$symbolIdentity' did not compile successfully.",
                )
            }
        }.canonicalRepresentationDiagnostics()
        if (unavailableChildren.isNotEmpty()) {
            return AthenaRepresentationLoweringResult(null, unavailableChildren)
        }
        val childCompilations = element.children.map { child ->
            val childSymbolIdentity = requireNotNull(child.symbolIdentity).value
            compileChild(child, symbols.getValue(DefinitionKey(libraryId, childSymbolIdentity)))
        }.sortedWith(compareBy({ it.zOrder }, { it.childId.value }))
        val exportDiagnostics = compiledExportDiagnostics(file, element, childCompilations)
        if (exportDiagnostics.isNotEmpty()) return AthenaRepresentationLoweringResult(null, exportDiagnostics)
        val styleDiagnostics = styleDiagnostics(file, element, childCompilations)
        if (styleDiagnostics.isNotEmpty()) return AthenaRepresentationLoweringResult(null, styleDiagnostics)

        val styles = childCompilations.flatMap { child -> child.styleTokens }
            .distinctBy { style -> style.styleTokenId }
            .sortedBy { style -> style.styleTokenId.value }
        val exports = element.exportedAnchors.sortedBy { export -> export.id }.map { export ->
            val child = childCompilations.single { compiled -> compiled.childId.value == export.childId.value }
            val source = child.sourceDefinition.anchors.single { anchor -> anchor.anchorId.value == export.childAnchorId.value }
            val exportedId = RepresentationAnchorId(export.id)
            val primitiveId = GraphicPrimitiveId("${child.childId.value}.${source.primitiveId.value}")
            CompiledExport(
                contract = RepresentationAnchorContract(
                    anchorId = exportedId,
                    geometryRef = source.geometryRef,
                    primitiveId = primitiveId,
                    point = transformPoint(source.point, child.sourceChild),
                    role = source.role,
                    required = source.required,
                    acceptedDirections = source.acceptedDirections,
                    acceptedSignals = source.acceptedSignals,
                    port = source.port,
                ),
                intrinsic = RepresentationExportedAnchor(
                    anchorId = exportedId,
                    childId = child.childId,
                    childAnchorId = source.anchorId,
                ),
            )
        }
        val graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(identity.value),
            bounds = bounds.toGraphicBounds(),
            primitives = childCompilations.map(CompiledChild::primitive),
            styleTokens = styles,
            provenanceSources = listOf(file),
        )
        val labelExports = element.exportedLabels.sortedBy { export -> export.id }.map { export ->
            val child = childCompilations.single { compiled -> compiled.childId.value == export.childId.value }
            val source = child.sourceDefinition.labelSlots.single { slot -> slot.slotId.value == export.childLabelId.value }
            val sourceBounds = requireNotNull(source.bounds)
            val scale = requireNotNull(child.sourceChild.scale)
            val rotate = requireNotNull(child.sourceChild.rotate).value
            val translate = requireNotNull(child.sourceChild.translate)
            CompiledLabelExport(
                slot = RepresentationLabelSlot(
                    slotId = RepresentationLabelSlotId(export.id),
                    role = source.role,
                    origin = source.origin?.let { point -> transformPoint(point, child.sourceChild) },
                    bounds = sourceBounds.transform(scale.x, scale.y, rotate, translate.x, translate.y),
                    styleTokenId = source.styleTokenId,
                ),
                intrinsic = RepresentationExportedLabelSlot(
                    slotId = RepresentationLabelSlotId(export.id),
                    childId = child.childId,
                    childSlotId = source.slotId,
                ),
            )
        }
        val definition = RepresentationDefinition(
            symbolId = RepresentationSymbolId(identity.value),
            libraryId = libraryId,
            version = RepresentationVersion(version.value),
            lifecycle = RepresentationLifecycle(
                RepresentationLifecycleState.ACTIVE,
                RepresentationProvenance(file),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            anatomy = compatibilityAnatomy(identity.value, bounds),
            labelSlots = labelExports.map(CompiledLabelExport::slot),
            variants = listOf(RepresentationVariantId("standard")),
            bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
            definitionKind = RepresentationDefinitionKind.ELEMENT,
            graphicBody = graphicBody,
            anchors = exports.map(CompiledExport::contract),
            intrinsicComposition = RepresentationIntrinsicComposition(
                children = childCompilations.map { child ->
                    RepresentationCompositionChild(
                        childId = child.childId,
                        symbolId = child.sourceDefinition.symbolId,
                        zOrder = child.zOrder,
                        transforms = child.transforms,
                    )
                },
                exportedAnchors = exports.map(CompiledExport::intrinsic),
                exportedLabelSlots = labelExports.map(CompiledLabelExport::intrinsic),
            ),
        )
        val diagnostics = buildList {
            GraphicPrimitiveIrValidator.validate(graphicBody).diagnostics.forEach { issue ->
                add(
                    representationDiagnostic(
                        "element.graphic.invalid",
                        file,
                        element.span,
                        issue.subject,
                        "${issue.code.wireValue}: ${issue.message}",
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
                        "element.definition.invalid",
                        file,
                        element.span,
                        issue.subjectId?.value ?: "element.${element.name}",
                        "${issue.code.wireValue}: ${issue.message}",
                    ),
                )
            }
        }.canonicalRepresentationDiagnostics()
        return AthenaRepresentationLoweringResult(definition.takeIf { diagnostics.isEmpty() }, diagnostics)
    }

    private fun lowerSvgGraphic(
        file: String,
        libraryId: RepresentationLibraryId,
        element: ElementDeclaration,
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
                RepresentationLifecycleState.ACTIVE,
                RepresentationProvenance(file),
            ),
            kind = RepresentationSymbolKind.GENERIC,
            anatomy = compatibilityAnatomy(identity, SymbolBounds(bounds.x, bounds.y, bounds.width, bounds.height, resource.span)),
            labelSlots = emptyList(),
            variants = listOf(RepresentationVariantId("standard")),
            bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
            definitionKind = RepresentationDefinitionKind.ELEMENT,
            graphicBody = graphicBody,
            anchors = emptyList(),
            intrinsicComposition = null,
        )
        val diagnostics = buildList {
            GraphicPrimitiveIrValidator.validate(graphicBody).diagnostics.forEach { issue ->
                add(
                    representationDiagnostic(
                        "element.graphic.invalid",
                        file,
                        element.span,
                        issue.subject,
                        "${issue.code.wireValue}: ${issue.message}",
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
                        "element.definition.invalid",
                        file,
                        element.span,
                        issue.subjectId?.value ?: "element.${element.name}",
                        "${issue.code.wireValue}: ${issue.message}",
                    ),
                )
            }
        }.canonicalRepresentationDiagnostics()
        return AthenaRepresentationLoweringResult(definition.takeIf { diagnostics.isEmpty() }, diagnostics)
    }

    private fun compileChild(child: ElementChildDeclaration, symbol: RepresentationDefinition): CompiledChild {
        val childId = RepresentationCompositionChildId(child.id)
        val sourceBounds = requireNotNull(symbol.graphicBody.bounds)
        val namespaced = GraphicPrimitive.Group(
            primitiveId = GraphicPrimitiveId("${child.id}.group"),
            bounds = sourceBounds,
            children = symbol.graphicBody.primitives.map { primitive -> primitive.namespace(child.id) },
        )
        val scale = requireNotNull(child.scale)
        val rotate = requireNotNull(child.rotate).value
        val translate = requireNotNull(child.translate)
        val scaleTransform = GraphicTransform.Scale(scale.x, scale.y, GraphicPoint(0.0, 0.0))
        val rotationTransform = GraphicTransform.Rotation(rotate, GraphicPoint(0.0, 0.0))
        val translationTransform = GraphicTransform.Translation(translate.x, translate.y)
        val scaledBounds = sourceBounds.transform(scale.x, scale.y, 0.0, 0.0, 0.0)
        val scaled = GraphicPrimitive.Transformed(
            GraphicPrimitiveId("${child.id}.transform.scale"),
            scaledBounds,
            scaleTransform,
            namespaced,
        )
        val rotatedBounds = scaledBounds.transform(1.0, 1.0, rotate, 0.0, 0.0)
        val rotated = GraphicPrimitive.Transformed(
            GraphicPrimitiveId("${child.id}.transform.rotate"),
            rotatedBounds,
            rotationTransform,
            scaled,
        )
        val translatedBounds = GraphicBounds(
            rotatedBounds.x + translate.x,
            rotatedBounds.y + translate.y,
            rotatedBounds.width,
            rotatedBounds.height,
        )
        val translated = GraphicPrimitive.Transformed(
            GraphicPrimitiveId("${child.id}.transform.translate"),
            translatedBounds,
            translationTransform,
            rotated,
        )
        return CompiledChild(
            childId,
            child,
            symbol,
            requireNotNull(child.zOrder).value.toInt(),
            listOf(scaleTransform, rotationTransform, translationTransform),
            translated,
            symbol.graphicBody.styleTokens,
        )
    }

    private fun styleDiagnostics(
        file: String,
        element: ElementDeclaration,
        children: List<CompiledChild>,
    ): List<AthenaRepresentationSourceDiagnostic> = children.flatMap { child -> child.styleTokens }
        .groupBy { style -> style.styleTokenId }
        .filterValues { styles -> styles.distinct().size > 1 }
        .map { (styleId, _) ->
            representationDiagnostic(
                "element.style-token.collision",
                file,
                element.span,
                "element.${element.name}.styles.${styleId.value}",
                "Element children define incompatible style token `${styleId.value}`.",
            )
        }.canonicalRepresentationDiagnostics()

    private fun compiledExportDiagnostics(
        file: String,
        element: ElementDeclaration,
        children: List<CompiledChild>,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        val subject = "element.${element.name}"
        val childrenById = children.associateBy { child -> child.childId.value }
        element.exportedAnchors.forEach { export ->
            val child = childrenById[export.childId.value] ?: return@forEach
            if (child.sourceDefinition.anchors.none { anchor -> anchor.anchorId.value == export.childAnchorId.value }) {
                add(
                    representationDiagnostic(
                        "element.export.child-anchor.unresolved",
                        file,
                        export.referenceSpan,
                        "$subject.exports.${export.id}",
                        "Element export `${export.id}` references missing child anchor `${export.childId.value}.${export.childAnchorId.value}`.",
                    ),
                )
            }
        }
        element.exportedLabels.forEach { export ->
            val child = childrenById[export.childId.value] ?: return@forEach
            if (child.sourceDefinition.labelSlots.none { slot -> slot.slotId.value == export.childLabelId.value }) {
                add(
                    representationDiagnostic(
                        "element.export.child-label.unresolved",
                        file,
                        export.referenceSpan,
                        "$subject.labelExports.${export.id}",
                        "Element label export `${export.id}` references missing child label `${export.childId.value}.${export.childLabelId.value}`.",
                    ),
                )
            }
        }
        children.forEach { child ->
            child.sourceDefinition.anchors.filter { anchor -> anchor.required }.forEach { anchor ->
                val exportCount = element.exportedAnchors.count { export ->
                    export.childId.value == child.childId.value && export.childAnchorId.value == anchor.anchorId.value
                }
                if (exportCount != 1) {
                    add(
                        representationDiagnostic(
                            "element.child.anchor.unexported",
                            file,
                            child.sourceChild.headerSpan,
                            "$subject.children.${child.childId.value}.anchors.${anchor.anchorId.value}",
                            "Connectable child anchor `${child.childId.value}.${anchor.anchorId.value}` must be exported exactly once.",
                        ),
                    )
                }
            }
        }
    }.canonicalRepresentationDiagnostics()

    private fun GraphicPrimitive.namespace(prefix: String): GraphicPrimitive = when (this) {
        is GraphicPrimitive.Line -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Polyline -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Arc -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Circle -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Rectangle -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Text -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Marker -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.ConnectionDot -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.ReferenceArrow -> copy(primitiveId = namespaced(prefix))
        is GraphicPrimitive.Group -> copy(
            primitiveId = namespaced(prefix),
            children = children.map { child -> child.namespace(prefix) },
        )
        is GraphicPrimitive.Transformed -> copy(
            primitiveId = namespaced(prefix),
            child = child.namespace(prefix),
        )
    }

    private fun GraphicPrimitive.namespaced(prefix: String) = GraphicPrimitiveId("$prefix.${primitiveId.value}")

    private fun transformPoint(point: GraphicPoint, child: ElementChildDeclaration): GraphicPoint {
        val scale = requireNotNull(child.scale)
        val radians = requireNotNull(child.rotate).value * PI / 180.0
        val x = point.x * scale.x
        val y = point.y * scale.y
        val translate = requireNotNull(child.translate)
        return GraphicPoint(
            x * cos(radians) - y * sin(radians) + translate.x,
            x * sin(radians) + y * cos(radians) + translate.y,
        )
    }

    private fun GraphicBounds.transform(scaleX: Double, scaleY: Double, rotation: Double, dx: Double, dy: Double): GraphicBounds {
        val radians = rotation * PI / 180.0
        val points = listOf(
            GraphicPoint(x, y),
            GraphicPoint(x + width, y),
            GraphicPoint(x + width, y + height),
            GraphicPoint(x, y + height),
        ).map { point ->
            val scaledX = point.x * scaleX
            val scaledY = point.y * scaleY
            GraphicPoint(
                scaledX * cos(radians) - scaledY * sin(radians) + dx,
                scaledX * sin(radians) + scaledY * cos(radians) + dy,
            )
        }
        val minX = points.minOf(GraphicPoint::x)
        val minY = points.minOf(GraphicPoint::y)
        return GraphicBounds(minX, minY, points.maxOf(GraphicPoint::x) - minX, points.maxOf(GraphicPoint::y) - minY)
    }

    private fun SymbolBounds.toGraphicBounds() = GraphicBounds(x, y, width, height)

    private data class CompiledChild(
        val childId: RepresentationCompositionChildId,
        val sourceChild: ElementChildDeclaration,
        val sourceDefinition: RepresentationDefinition,
        val zOrder: Int,
        val transforms: List<GraphicTransform>,
        val primitive: GraphicPrimitive,
        val styleTokens: List<GraphicStyleToken>,
    )

    private data class CompiledExport(
        val contract: RepresentationAnchorContract,
        val intrinsic: RepresentationExportedAnchor,
    )

    private data class CompiledLabelExport(
        val slot: RepresentationLabelSlot,
        val intrinsic: RepresentationExportedLabelSlot,
    )
}
