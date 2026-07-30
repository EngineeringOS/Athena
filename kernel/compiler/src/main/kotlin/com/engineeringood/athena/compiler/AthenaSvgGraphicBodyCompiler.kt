package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import java.nio.file.Files
import java.util.ArrayDeque
import kotlin.math.hypot
import org.w3c.dom.Element

internal object AthenaSvgGraphicBodyCompiler {
    fun compile(
        athenaFile: String,
        definitionId: String,
        resource: RepresentationResourceDeclaration,
    ): SvgGraphicBodyCompilation {
        val svgPath = resolvePackageLocalSvgPath(athenaFile, resource.path.value) ?: return SvgGraphicBodyCompilation(
            svgFile = resource.path.value,
            diagnostics = listOf(
                issue(
                    "svg.path.invalid",
                    athenaFile,
                    resource.path.span,
                    "resource.${resource.id}.path",
                    "SVG graphic path must resolve inside the representation source directory.",
                ),
            ),
        )
        if (!Files.isRegularFile(svgPath)) {
            return SvgGraphicBodyCompilation(
                svgFile = svgPath.toString(),
                diagnostics = listOf(
                    issue(
                        "svg.file.missing",
                        athenaFile,
                        resource.path.span,
                        "resource.${resource.id}.path",
                        "Referenced SVG graphic body does not exist.",
                    ),
                ),
            )
        }
        if (Files.isSymbolicLink(svgPath)) {
            return SvgGraphicBodyCompilation(
                svgFile = svgPath.toString(),
                diagnostics = listOf(
                    issue(
                        "svg.path.symlink.forbidden",
                        athenaFile,
                        resource.path.span,
                        "resource.${resource.id}.path",
                        "Referenced SVG graphic body must be a regular package-local resource, not a symbolic link.",
                    ),
                ),
            )
        }
        if (Files.size(svgPath) > AthenaSvgGraphicBodySupport.MAX_SVG_BYTES) {
            return SvgGraphicBodyCompilation(
                svgFile = svgPath.toString(),
                diagnostics = listOf(
                    issue(
                        "svg.budget.bytes.exceeded",
                        athenaFile,
                        resource.path.span,
                        "svg.bytes",
                        "SVG graphic body exceeds the maximum source byte budget.",
                    ),
                ),
            )
        }

        val source = Files.readString(svgPath)
        val parsed = AthenaSvgGraphicBodySupport.parseSvg(svgPath.toString(), source)
        if (parsed.diagnostics.isNotEmpty()) return parsed

        val root = requireNotNull(parsed.root)
        val diagnostics = mutableListOf<AthenaRepresentationSourceDiagnostic>()
        diagnostics += AthenaSvgGraphicBodySupport.validateRoot(svgPath.toString(), source, root)

        val metrics = AthenaSvgGraphicBodySupport.treeMetrics(root)
        if (metrics.elements > AthenaSvgGraphicBodySupport.MAX_ELEMENTS) {
            diagnostics += issue(
                "svg.budget.elements.exceeded",
                svgPath.toString(),
                AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                "svg.elements",
                "SVG graphic body exceeds the maximum element budget.",
            )
        }
        if (metrics.maxDepth > AthenaSvgGraphicBodySupport.MAX_DEPTH) {
            diagnostics += issue(
                "svg.budget.depth.exceeded",
                svgPath.toString(),
                AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                "svg.depth",
                "SVG graphic body exceeds the maximum DOM depth budget.",
            )
        }

        val geometryIndex = AthenaSvgGraphicBodySupport.geometryNodeIndex(svgPath.toString(), source, root)
        diagnostics += geometryIndex.diagnostics
        diagnostics += AthenaSvgGraphicBodySupport.validateUseGraph(svgPath.toString(), source, root, geometryIndex)
        AthenaSvgGraphicBodySupport.walkElements(root)
            .filter { element -> element !== root }
            .forEach { element -> diagnostics += AthenaSvgGraphicBodySupport.validateElement(svgPath.toString(), source, element) }

        if (diagnostics.isNotEmpty()) {
            return SvgGraphicBodyCompilation(svgPath.toString(), diagnostics = diagnostics.canonicalRepresentationDiagnostics())
        }

        val documentBounds = AthenaSvgGraphicBodySupport.documentBounds(root)
        val primitives = mutableListOf<PrimitiveWithNode>()
        val loweringDiagnostics = mutableListOf<AthenaRepresentationSourceDiagnostic>()
        root.childElements().forEach { child ->
            child.lowerRenderableSubtree(
                file = svgPath.toString(),
                source = source,
                geometryIndex = geometryIndex,
                containerBounds = documentBounds,
                inheritedTransform = AthenaSvgGraphicBodySupport.SvgTransform.identity(),
                activeGeometryIds = ArrayDeque(),
                activeGeometryRefs = ArrayDeque(),
                primitives = primitives,
                diagnostics = loweringDiagnostics,
            )
        }
        if (loweringDiagnostics.isNotEmpty()) {
            return SvgGraphicBodyCompilation(svgPath.toString(), diagnostics = loweringDiagnostics.canonicalRepresentationDiagnostics())
        }
        if (primitives.size > AthenaSvgGraphicBodySupport.MAX_EMITTED_PRIMITIVES) {
            diagnostics += issue(
                "svg.budget.primitives.exceeded",
                svgPath.toString(),
                AthenaSvgGraphicBodySupport.sourceSpan(source, "<svg"),
                "svg.primitives",
                "SVG graphic body exceeds the maximum emitted primitive budget.",
            )
        }
        if (diagnostics.isNotEmpty()) {
            return SvgGraphicBodyCompilation(svgPath.toString(), diagnostics = diagnostics.canonicalRepresentationDiagnostics())
        }

        return SvgGraphicBodyCompilation(
            svgFile = svgPath.toString(),
            document = GraphicPrimitiveDocument(
                documentId = GraphicPrimitiveDocumentId(definitionId),
                bounds = documentBounds,
                primitives = primitives.map(PrimitiveWithNode::primitive),
                styleTokens = listOf(AthenaSvgGraphicBodySupport.styleToken),
                provenanceSources = listOf(athenaFile, svgPath.toString()),
            ),
            primitiveIdsByGeometryRef = primitives
                .mapNotNull { primitive -> primitive.geometryRef?.let { ref -> ref to primitive.primitive.primitiveId } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, primitiveIds) -> primitiveIds.distinct() },
        )
    }

    private fun Element.lowerRenderableSubtree(
        file: String,
        source: String,
        geometryIndex: AthenaSvgGraphicBodySupport.SvgGeometryNodeIndex,
        containerBounds: GraphicBounds,
        inheritedTransform: AthenaSvgGraphicBodySupport.SvgTransform,
        activeGeometryIds: ArrayDeque<String>,
        activeGeometryRefs: ArrayDeque<String>,
        primitives: MutableList<PrimitiveWithNode>,
        diagnostics: MutableList<AthenaRepresentationSourceDiagnostic>,
    ) {
        val localGeometryRef = getAttribute("data-athena-ref").takeIf(String::isNotBlank)
        if (localGeometryRef != null) {
            activeGeometryRefs.addLast(localGeometryRef)
        }
        try {
        when (localName) {
            "defs" -> return
            "use" -> {
                val href = AthenaSvgGraphicBodySupport.useHrefOrNull(this)
                if (href == null) {
                    diagnostics += issue(
                        "svg.use.reference.missing",
                        file,
                        AthenaSvgGraphicBodySupport.sourceSpan(source, tagName),
                        "svg.use.href",
                        "SVG use elements require a local fragment reference.",
                    )
                    return
                }
                val geometryId = href.removePrefix("#")
                val target = geometryIndex.resolveReference(geometryId)
                if (target == null) {
                    val candidates = geometryIndex.referenceCandidates(geometryId)
                    diagnostics += issue(
                        if (candidates.size > 1) "svg.use.reference.ambiguous" else "svg.use.reference.missing",
                        file,
                        AthenaSvgGraphicBodySupport.sourceSpan(source, href),
                        "svg.use.$geometryId",
                        "SVG use references must target exactly one marked geometry node.",
                    )
                    return
                }
                if (activeGeometryIds.contains(geometryId)) {
                    diagnostics += issue(
                        "svg.use.cycle",
                        file,
                        AthenaSvgGraphicBodySupport.sourceSpan(source, href),
                        "svg.use.$geometryId",
                        "SVG use expansion must remain acyclic.",
                    )
                    return
                }
                val localTransform = buildUseTransform()
                activeGeometryIds.addLast(geometryId)
                try {
                    target.lowerRenderableSubtree(
                        file = file,
                        source = source,
                        geometryIndex = geometryIndex,
                        containerBounds = containerBounds,
                        inheritedTransform = inheritedTransform.then(localTransform),
                        activeGeometryIds = activeGeometryIds,
                        activeGeometryRefs = activeGeometryRefs,
                        primitives = primitives,
                        diagnostics = diagnostics,
                    )
                } finally {
                    activeGeometryIds.removeLast()
                }
            }
            "g" -> childElements().forEach { child ->
                child.lowerRenderableSubtree(
                    file = file,
                    source = source,
                    geometryIndex = geometryIndex,
                    containerBounds = containerBounds,
                    inheritedTransform = inheritedTransform.then(localTransform()),
                    activeGeometryIds = activeGeometryIds,
                    activeGeometryRefs = activeGeometryRefs,
                    primitives = primitives,
                    diagnostics = diagnostics,
                )
            }
            "rect", "line", "circle", "text" -> {
                val primitive = toPrimitive(
                    index = primitives.size + 1,
                    file = file,
                    source = source,
                    containerBounds = containerBounds,
                    transform = inheritedTransform.then(localTransform()),
                    diagnostics = diagnostics,
                ) ?: return
                primitives += PrimitiveWithNode(primitive, this, activeGeometryRefs.lastOrNull())
            }
            else -> childElements().forEach { child ->
                child.lowerRenderableSubtree(
                    file = file,
                    source = source,
                    geometryIndex = geometryIndex,
                    containerBounds = containerBounds,
                    inheritedTransform = inheritedTransform.then(localTransform()),
                    activeGeometryIds = activeGeometryIds,
                    activeGeometryRefs = activeGeometryRefs,
                    primitives = primitives,
                    diagnostics = diagnostics,
                )
            }
        }
        } finally {
            if (localGeometryRef != null) {
                activeGeometryRefs.removeLast()
            }
        }
    }

    private fun Element.localTransform(): AthenaSvgGraphicBodySupport.SvgTransform =
        AthenaSvgGraphicBodySupport.parseTransformOrNull(this) ?: AthenaSvgGraphicBodySupport.SvgTransform.identity()

    private fun Element.buildUseTransform(): AthenaSvgGraphicBodySupport.SvgTransform {
        val translateX = getAttribute("x").takeIf(String::isNotBlank)?.toDoubleOrNull()
        val translateY = getAttribute("y").takeIf(String::isNotBlank)?.toDoubleOrNull()
        val translate = if ((translateX == null && hasAttribute("x")) || (translateY == null && hasAttribute("y"))) {
            return AthenaSvgGraphicBodySupport.SvgTransform.identity()
        } else {
            AthenaSvgGraphicBodySupport.SvgTransform(
                listOf(
                    AthenaSvgGraphicBodySupport.SvgTransformOperation.Translate(translateX ?: 0.0, translateY ?: 0.0),
                ),
            )
        }
        return localTransform().then(translate)
    }

    private fun Element.toPrimitive(
        index: Int,
        file: String,
        source: String,
        containerBounds: GraphicBounds,
        transform: AthenaSvgGraphicBodySupport.SvgTransform,
        diagnostics: MutableList<AthenaRepresentationSourceDiagnostic>,
    ): GraphicPrimitive? {
        val id = GraphicPrimitiveId("svg-${index.toString().padStart(4, '0')}")
        return when (localName) {
            "rect" -> {
                val x = finiteNumberOrDefault("x")
                val y = finiteNumberOrDefault("y")
                val width = finiteNumberOrDefault("width")
                val height = finiteNumberOrDefault("height")
                if (x == null || y == null) {
                    val field = if (x == null) "x" else "y"
                    diagnostics += geometryIssue(
                        "svg.rect.coordinate.invalid",
                        file,
                        source,
                        field,
                        "svg.rect.geometry",
                        "SVG rectangle coordinates must be finite numbers.",
                    )
                    return null
                }
                if (width == null || height == null || width <= 0.0 || height <= 0.0) {
                    val field = if (width == null || width <= 0.0) "width" else "height"
                    diagnostics += geometryIssue(
                        "svg.rect.size.invalid",
                        file,
                        source,
                        field,
                        "svg.rect.geometry",
                        "SVG rectangle width and height must be finite positive numbers.",
                    )
                    return null
                }
                GraphicPrimitive.Rectangle(
                    primitiveId = id,
                    bounds = AthenaSvgGraphicBodySupport.transformBounds(GraphicBounds(x, y, width, height), transform),
                    cornerRadius = 0.0,
                    styleTokenId = AthenaSvgGraphicBodySupport.styleToken.styleTokenId,
                )
            }
            "line" -> {
                val x1 = finiteNumberOrDefault("x1")
                val y1 = finiteNumberOrDefault("y1")
                val x2 = finiteNumberOrDefault("x2")
                val y2 = finiteNumberOrDefault("y2")
                val invalidField = listOf("x1" to x1, "y1" to y1, "x2" to x2, "y2" to y2)
                    .firstOrNull { (_, value) -> value == null }
                    ?.first
                if (invalidField != null) {
                    diagnostics += geometryIssue(
                        "svg.line.coordinate.invalid",
                        file,
                        source,
                        invalidField,
                        "svg.line.geometry",
                        "SVG line coordinates must be finite numbers.",
                    )
                    return null
                }
                val start = requireNotNull(x1).let { x -> GraphicPoint(x, requireNotNull(y1)) }
                val end = requireNotNull(x2).let { x -> GraphicPoint(x, requireNotNull(y2)) }
                if (start == end) {
                    diagnostics += geometryIssue(
                        "svg.line.degenerate",
                        file,
                        source,
                        "x2",
                        "svg.line.geometry",
                        "SVG line endpoints must not be identical.",
                    )
                    return null
                }
                val transformedStart = AthenaSvgGraphicBodySupport.transformPoint(start, transform)
                val transformedEnd = AthenaSvgGraphicBodySupport.transformPoint(end, transform)
                GraphicPrimitive.Line(
                    primitiveId = id,
                    bounds = AthenaSvgGraphicBodySupport.lineBounds(transformedStart, transformedEnd, containerBounds),
                    start = transformedStart,
                    end = transformedEnd,
                    styleTokenId = AthenaSvgGraphicBodySupport.styleToken.styleTokenId,
                )
            }
            "circle" -> {
                val cx = finiteNumberOrDefault("cx")
                val cy = finiteNumberOrDefault("cy")
                if (cx == null || cy == null) {
                    val field = if (cx == null) "cx" else "cy"
                    diagnostics += geometryIssue(
                        "svg.circle.coordinate.invalid",
                        file,
                        source,
                        field,
                        "svg.circle.geometry",
                        "SVG circle coordinates must be finite numbers.",
                    )
                    return null
                }
                val radius = finiteNumberOrDefault("r")
                if (radius == null || radius <= 0.0) {
                    diagnostics += geometryIssue(
                        "svg.circle.radius.invalid",
                        file,
                        source,
                        "r",
                        "svg.circle.geometry",
                        "SVG circle radius must be a finite positive number.",
                    )
                    return null
                }
                val center = GraphicPoint(cx, cy)
                val transformedCenter = AthenaSvgGraphicBodySupport.transformPoint(center, transform)
                val edge = AthenaSvgGraphicBodySupport.transformPoint(GraphicPoint(center.x + radius, center.y), transform)
                val transformedRadius = hypot(edge.x - transformedCenter.x, edge.y - transformedCenter.y)
                GraphicPrimitive.Circle(
                    primitiveId = id,
                    bounds = GraphicBounds(
                        transformedCenter.x - transformedRadius,
                        transformedCenter.y - transformedRadius,
                        transformedRadius * 2.0,
                        transformedRadius * 2.0,
                    ),
                    center = transformedCenter,
                    radius = transformedRadius,
                    styleTokenId = AthenaSvgGraphicBodySupport.styleToken.styleTokenId,
                )
            }
            "text" -> {
                val x = finiteNumberOrDefault("x")
                val y = finiteNumberOrDefault("y")
                if (x == null || y == null) {
                    val field = if (x == null) "x" else "y"
                    diagnostics += geometryIssue(
                        "svg.text.coordinate.invalid",
                        file,
                        source,
                        field,
                        "svg.text.geometry",
                        "SVG text coordinates must be finite numbers.",
                    )
                    return null
                }
                val text = textContent.trim()
                if (text.isBlank()) {
                    diagnostics += issue(
                        "svg.text.value.missing",
                        file,
                        AthenaSvgGraphicBodySupport.sourceSpan(source, "<text"),
                        "svg.text.value",
                        "SVG text primitives require non-blank text content.",
                    )
                    return null
                }
                val origin = AthenaSvgGraphicBodySupport.transformPoint(GraphicPoint(x, y), transform)
                GraphicPrimitive.Text(
                    primitiveId = id,
                    bounds = AthenaSvgGraphicBodySupport.transformBounds(GraphicBounds(x, y, 1.0, 1.0), transform),
                    origin = origin,
                    text = text,
                    styleTokenId = AthenaSvgGraphicBodySupport.styleToken.styleTokenId,
                )
            }
            else -> null
        }
    }

    private fun Element.finiteNumberOrDefault(name: String): Double? {
        if (!hasAttribute(name)) return 0.0
        return getAttribute(name).toDoubleOrNull()?.takeIf(Double::isFinite)
    }

    private fun Element.geometryIssue(
        code: String,
        file: String,
        source: String,
        field: String,
        subject: String,
        message: String,
    ): AthenaRepresentationSourceDiagnostic = issue(
        code,
        file,
        AthenaSvgGraphicBodySupport.sourceSpan(source, attributeToken(field)),
        subject,
        message,
    )

    private fun Element.attributeToken(name: String): String = "$name=\"${getAttribute(name)}\""

    private fun Element.childElements(): List<Element> = AthenaSvgGraphicBodySupport.childElements(this)
}
