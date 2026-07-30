package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import java.io.StringReader
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.sin
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

internal object AthenaSvgGraphicBodySupport {
    const val MAX_SVG_BYTES = 262_144L
    const val MAX_ELEMENTS = 512
    const val MAX_DEPTH = 32
    const val MAX_EMITTED_PRIMITIVES = 256

    private const val SVG_NS = "http://www.w3.org/2000/svg"
    private const val XLINK_NS = "http://www.w3.org/1999/xlink"

    val styleToken = GraphicStyleToken(
        styleTokenId = GraphicStyleTokenId("svg-default"),
        stroke = GraphicPaintToken("foreground"),
        strokeWidth = 1.0,
        fill = GraphicFill.TRANSPARENT,
        lineCap = GraphicLineCap.BUTT,
        lineJoin = GraphicLineJoin.MITER,
    )

    private val allowedAthenaAttributes = setOf(
        "data-athena-ref",
    )

    private val forbiddenMetadataAttributes = setOf(
        "data-athena-schema",
        "data-athena-identity",
        "data-athena-version",
        "data-athena-kind",
        "data-athena-lifecycle",
        "data-athena-profile",
        "data-athena-binding",
        "data-athena-device",
        "data-athena-port",
        "data-athena-connection",
        "data-athena-layout",
        "data-athena-anchor",
        "data-athena-point",
        "data-athena-role",
        "data-athena-direction",
        "data-athena-signal",
        "data-athena-label-slot",
        "data-athena-hotspot",
    )

    private val referenceableGeometryNodes = setOf("g", "line", "rect", "circle", "text")

    fun parseSvg(file: String, source: String): SvgGraphicBodyCompilation {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isXIncludeAware = false
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        return try {
            SvgGraphicBodyCompilation(file, root = factory.newDocumentBuilder().parse(InputSource(StringReader(source))).documentElement)
        } catch (exception: Exception) {
            SvgGraphicBodyCompilation(
                file,
                diagnostics = listOf(
                    issue(
                        "svg.xml.invalid",
                        file,
                        source.spanOf("<"),
                        "svg",
                        exception.message ?: "SVG XML could not be parsed safely.",
                    ),
                ),
            )
        }
    }

    fun validateRoot(file: String, source: String, root: Element): List<AthenaRepresentationSourceDiagnostic> = buildList {
        if (root.namespaceURI != SVG_NS || root.localName != "svg") {
            add(issue("svg.root.invalid", file, source.spanOf(root.tagName), "svg.root", "SVG graphic body root must be an SVG namespace <svg> element."))
        }
        root.attributesList().filter { attribute -> attribute.name.startsWith("data-athena-") }
            .forEach { attribute ->
                add(
                    issue(
                        "svg.metadata.forbidden",
                        file,
                        source.spanOf(attribute.name),
                        "svg.root.${attribute.name}",
                        "SVG root must not declare Athena metadata.",
                    ),
                )
            }
        addAll(validateSecurityAttributes(file, source, root))
        if (root.hasAttribute("transform") && parseTransformOrNull(root) == null) {
            add(issue("svg.transform.invalid", file, source.spanOf("transform"), "svg.root.transform", "SVG transform must use the supported finite transform syntax."))
        }
        documentBoundsOrNull(root) ?: add(
            issue(
                "svg.viewbox.invalid",
                file,
                source.spanOf(if (source.contains("viewBox")) "viewBox" else "<svg"),
                "svg.root.viewBox",
                "SVG root requires a valid positive viewBox or width and height.",
            ),
        )
    }

    fun validateElement(file: String, source: String, element: Element): List<AthenaRepresentationSourceDiagnostic> = buildList {
        if (element.namespaceURI != SVG_NS) {
            add(issue("svg.namespace.unsupported", file, source.spanOf(element.tagName), "svg.${element.localName}", "SVG graphic body only supports SVG namespace elements."))
        }
        when {
            element.localName == "script" -> add(issue("svg.script.forbidden", file, source.spanOf(element.tagName), "svg.script", "SVG script elements are forbidden."))
            element.localName == "foreignObject" -> add(issue("svg.foreign-object.forbidden", file, source.spanOf(element.tagName), "svg.foreignObject", "SVG foreignObject elements are forbidden."))
            element.localName !in setOf("defs", "g", "use", "rect", "line", "circle", "text") -> {
                add(issue("svg.element.unsupported", file, source.spanOf(element.tagName), "svg.${element.localName}", "Unsupported SVG element `${element.localName}`."))
            }
        }
        addAll(validateSecurityAttributes(file, source, element))
        element.attributesList().forEach { attribute ->
            when {
                attribute.name in allowedAthenaAttributes -> {
                    if (element.localName !in referenceableGeometryNodes) {
                        add(issue("svg.metadata.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "SVG geometry-reference hints may only annotate referenceable geometry nodes."))
                    }
                }
                attribute.name in forbiddenMetadataAttributes -> add(issue("svg.metadata.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "SVG nodes must not declare Athena metadata."))
                attribute.name.startsWith("data-athena-") -> add(issue("svg.metadata.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "Unknown SVG Athena metadata `${attribute.name}`."))
            }
        }
        if (element.hasAttribute("transform") && parseTransformOrNull(element) == null) {
            add(issue("svg.transform.invalid", file, source.spanOf("transform"), "svg.${element.localName}.transform", "SVG transform must use the supported finite transform syntax."))
        }
        if (element.localName == "use") {
            useHrefOrNull(element) ?: add(
                issue(
                    "svg.use.reference.missing",
                    file,
                    source.spanOf(element.tagName),
                    "svg.use.href",
                    "SVG use elements require a local fragment reference.",
                ),
            )
        }
    }

    fun walkElements(root: Element): List<Element> = buildList {
        fun walk(element: Element) {
            add(element)
            val children = element.childNodes
            for (index in 0 until children.length) {
                val child = children.item(index)
                if (child.nodeType == Node.ELEMENT_NODE) walk(child as Element)
            }
        }
        walk(root)
    }

    fun treeMetrics(root: Element): SvgTreeMetrics {
        var elements = 0
        var maxDepth = 0
        fun walk(element: Element, depth: Int) {
            elements += 1
            maxDepth = maxOf(maxDepth, depth)
            val children = element.childNodes
            for (index in 0 until children.length) {
                val child = children.item(index)
                if (child.nodeType == Node.ELEMENT_NODE) walk(child as Element, depth + 1)
            }
        }
        walk(root, 1)
        return SvgTreeMetrics(elements, maxDepth)
    }

    fun documentBounds(root: Element): GraphicBounds = requireNotNull(documentBoundsOrNull(root))

    fun viewBoxBounds(root: Element): GraphicBounds = documentBounds(root)

    fun childElements(element: Element): List<Element> = buildList {
        val children = element.childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child.nodeType == Node.ELEMENT_NODE) add(child as Element)
        }
    }

    fun lineBounds(start: GraphicPoint, end: GraphicPoint, container: GraphicBounds): GraphicBounds {
        val x = positiveAxisBounds(minOf(start.x, end.x), maxOf(start.x, end.x), container.x, container.width)
        val y = positiveAxisBounds(minOf(start.y, end.y), maxOf(start.y, end.y), container.y, container.height)
        return GraphicBounds(x.first, y.first, x.second, y.second)
    }

    fun sourceSpan(source: String, token: String): SourceSpan = source.spanOf(token)

    fun geometryNodeIndex(file: String, source: String, root: Element): SvgGeometryNodeIndex {
        val diagnostics = mutableListOf<AthenaRepresentationSourceDiagnostic>()
        val nodesById = linkedMapOf<String, MutableList<Element>>()
        val referenceableNodesById = linkedMapOf<String, MutableList<Element>>()
        val referenceableNodesByRef = linkedMapOf<String, MutableList<Element>>()
        walkElements(root)
            .filter { element -> element !== root }
            .filter { element -> element.localName in referenceableGeometryNodes }
            .forEach { element ->
                val id = element.getAttribute("id").takeIf(String::isNotBlank)
                if (id != null) nodesById.getOrPut(id) { mutableListOf() }.add(element)
                val geometryRef = element.getAttribute("data-athena-ref").takeIf(String::isNotBlank)
                if (geometryRef != null) {
                    if (id != null) referenceableNodesById.getOrPut(id) { mutableListOf() }.add(element)
                    referenceableNodesByRef.getOrPut(geometryRef) { mutableListOf() }.add(element)
                }
            }
        nodesById.filterValues { entries -> entries.size > 1 }
            .forEach { (id, entries) ->
                diagnostics += issue(
                    "svg.id.duplicate",
                    file,
                    source.spanOf("id=\"$id\""),
                    "svg.id.$id",
                    "SVG geometry ids must be unique, but `${entries.size}` nodes declare `$id`.",
                )
            }
        referenceableNodesByRef.filterValues { entries -> entries.size > 1 }
            .forEach { (geometryRef, entries) ->
                diagnostics += issue(
                    "svg.geometry-ref.duplicate",
                    file,
                    source.spanOf("data-athena-ref=\"$geometryRef\""),
                    "svg.geometry-ref.$geometryRef",
                    "SVG geometry references must be unique, but `${entries.size}` nodes declare `$geometryRef`.",
                )
            }
        return SvgGeometryNodeIndex(
            nodesById = nodesById.mapValues { it.value.toList() },
            referenceableNodesById = referenceableNodesById.mapValues { it.value.toList() },
            referenceableNodesByRef = referenceableNodesByRef.mapValues { it.value.toList() },
            diagnostics = diagnostics.canonicalRepresentationDiagnostics(),
        )
    }

    fun validateUseGraph(file: String, source: String, root: Element, index: SvgGeometryNodeIndex): List<AthenaRepresentationSourceDiagnostic> {
        val diagnostics = mutableListOf<AthenaRepresentationSourceDiagnostic>()

        fun visit(element: Element, activeGeometryIds: ArrayDeque<String>) {
            if (element.localName == "use") {
                val href = useHrefOrNull(element) ?: return
                val geometryId = href.removePrefix("#")
                val candidates = index.referenceCandidates(geometryId)
                when {
                    candidates.isEmpty() -> diagnostics += issue(
                        "svg.use.reference.missing",
                        file,
                        source.spanOf(href),
                        "svg.use.$geometryId",
                        "SVG use references must target exactly one marked geometry node.",
                    )
                    candidates.size > 1 -> diagnostics += issue(
                        "svg.use.reference.ambiguous",
                        file,
                        source.spanOf(href),
                        "svg.use.$geometryId",
                        "SVG use references must target exactly one marked geometry node.",
                    )
                    activeGeometryIds.contains(geometryId) -> diagnostics += issue(
                        "svg.use.cycle",
                        file,
                        source.spanOf(href),
                        "svg.use.$geometryId",
                        "SVG use expansion must remain acyclic.",
                    )
                    else -> {
                        activeGeometryIds.addLast(geometryId)
                        try {
                            childElements(candidates.single()).forEach { child -> visit(child, activeGeometryIds) }
                        } finally {
                            activeGeometryIds.removeLast()
                        }
                    }
                }
                return
            }

            childElements(element).forEach { child -> visit(child, activeGeometryIds) }
        }

        visit(root, ArrayDeque())
        return diagnostics.canonicalRepresentationDiagnostics()
    }

    private fun documentBoundsOrNull(root: Element): GraphicBounds? {
        val viewBox = root.getAttribute("viewBox").takeIf(String::isNotBlank)
            ?.split(Regex("\\s+|,"))
            ?.filter(String::isNotBlank)
            ?.mapNotNull(String::toDoubleOrNull)
        if (viewBox != null) {
            if (viewBox.size != 4 || viewBox.any { value -> !value.isFinite() } || viewBox[2] <= 0.0 || viewBox[3] <= 0.0) return null
            return GraphicBounds(viewBox[0], viewBox[1], viewBox[2], viewBox[3])
        }
        val width = root.getAttribute("width").takeIf(String::isNotBlank)?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val height = root.getAttribute("height").takeIf(String::isNotBlank)?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        return GraphicBounds(0.0, 0.0, width, height)
    }

    private fun Element.attributesList(): List<org.w3c.dom.Attr> =
        (0 until attributes.length).map { index -> attributes.item(index) as org.w3c.dom.Attr }

    private fun String.isForbiddenUrl(): Boolean {
        val value = trim().lowercase()
        return value.startsWith("http://") ||
            value.startsWith("https://") ||
            value.startsWith("data:") ||
            value.startsWith("file:") ||
            "url(" in value
    }

    private fun validateSecurityAttributes(
        file: String,
        source: String,
        element: Element,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        element.attributesList().forEach { attribute ->
            when {
                attribute.namespaceURI == XMLConstants.XMLNS_ATTRIBUTE_NS_URI -> Unit
                attribute.name.startsWith("on", ignoreCase = true) -> add(
                    issue("svg.event.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "SVG event attributes are forbidden."),
                )
                attribute.value.isForbiddenUrl() -> add(
                    issue("svg.resource-url.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "SVG external, data, file, and CSS url() resources are forbidden."),
                )
                attribute.namespaceURI != null &&
                    attribute.namespaceURI != XMLConstants.XMLNS_ATTRIBUTE_NS_URI &&
                    attribute.namespaceURI != XLINK_NS -> add(
                    issue("svg.attribute.namespace.forbidden", file, source.spanOf(attribute.name), "svg.${element.localName}.${attribute.name}", "SVG attribute namespaces other than xmlns and xlink are forbidden."),
                )
            }
        }
    }

    fun useHrefOrNull(element: Element): String? {
        val href = element.getAttribute("href").takeIf(String::isNotBlank)
            ?: element.getAttributeNS(XLINK_NS, "href").takeIf(String::isNotBlank)
        return href?.takeIf { value -> value.startsWith("#") && value.length > 1 }
    }

    fun parseTransformOrNull(element: Element): SvgTransform? {
        val transform = element.getAttribute("transform").takeIf(String::isNotBlank) ?: return SvgTransform.identity()
        return parseTransform(transform)
    }

    private fun parseTransform(raw: String): SvgTransform? {
        val tokenRegex = Regex("""([A-Za-z]+)\(([^)]*)\)""")
        val matches = tokenRegex.findAll(raw).toList()
        if (matches.isEmpty()) return null
        var cursor = 0
        matches.forEach { match ->
            if (!raw.substring(cursor, match.range.first).all { it.isWhitespace() || it == ',' }) return null
            cursor = match.range.last + 1
        }
        if (!raw.substring(cursor).all { it.isWhitespace() || it == ',' }) return null
        val tokens = matches
            .map { match ->
                val name = match.groupValues[1]
                val rawValues = match.groupValues[2]
                    .trim()
                    .split(Regex("\\s+|,"))
                    .filter(String::isNotBlank)
                val values = rawValues.map { it.toDoubleOrNull() ?: return null }
                if (values.any { !it.isFinite() }) return null
                name to values
            }
        val operations = buildList {
            tokens.forEach { (name, values) ->
                when (name.lowercase()) {
                    "translate" -> {
                        if (values.size !in 1..2) return null
                        val dx = values.getOrNull(0) ?: return null
                        val dy = values.getOrNull(1) ?: 0.0
                        add(SvgTransformOperation.Translate(dx, dy))
                    }
                    "scale" -> {
                        if (values.size !in 1..2) return null
                        val sx = values.getOrNull(0) ?: return null
                        val sy = values.getOrNull(1) ?: sx
                        add(SvgTransformOperation.Scale(sx, sy))
                    }
                    "rotate" -> {
                        val angle = values.getOrNull(0) ?: return null
                        when (values.size) {
                            1 -> add(SvgTransformOperation.Rotate(angle))
                            3 -> {
                                val pivot = GraphicPoint(values[1], values[2])
                                add(SvgTransformOperation.Translate(pivot.x, pivot.y))
                                add(SvgTransformOperation.Rotate(angle))
                                add(SvgTransformOperation.Translate(-pivot.x, -pivot.y))
                            }
                            else -> return null
                        }
                    }
                    else -> return null
                }
            }
        }
        return SvgTransform(operations)
    }

    internal fun transformPoint(point: GraphicPoint, transform: SvgTransform): GraphicPoint = transform.apply(point)

    internal fun transformBounds(bounds: GraphicBounds, transform: SvgTransform): GraphicBounds = transform.apply(bounds)

    internal data class SvgGeometryNodeIndex(
        val nodesById: Map<String, List<Element>>,
        val referenceableNodesById: Map<String, List<Element>>,
        val referenceableNodesByRef: Map<String, List<Element>>,
        val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
    ) {
        fun resolveReference(id: String): Element? = referenceableNodesById[id].orEmpty().singleOrNull()

        fun referenceCandidates(id: String): List<Element> = referenceableNodesById[id].orEmpty()

        fun resolveGeometryRef(ref: String): Element? = referenceableNodesByRef[ref].orEmpty().singleOrNull()

        fun geometryRefCandidates(ref: String): List<Element> = referenceableNodesByRef[ref].orEmpty()
    }

    internal data class SvgTransform(
        val operations: List<SvgTransformOperation> = emptyList(),
    ) {
        fun isIdentity(): Boolean = operations.isEmpty()

        fun then(other: SvgTransform): SvgTransform = SvgTransform(operations + other.operations)

        fun apply(point: GraphicPoint): GraphicPoint = operations.asReversed().fold(point) { current, operation -> operation.apply(current) }

        fun apply(bounds: GraphicBounds): GraphicBounds {
            val points = listOf(
                GraphicPoint(bounds.x, bounds.y),
                GraphicPoint(bounds.x + bounds.width, bounds.y),
                GraphicPoint(bounds.x + bounds.width, bounds.y + bounds.height),
                GraphicPoint(bounds.x, bounds.y + bounds.height),
            ).map(::apply)
            val minX = points.minOf(GraphicPoint::x)
            val minY = points.minOf(GraphicPoint::y)
            return GraphicBounds(minX, minY, points.maxOf(GraphicPoint::x) - minX, points.maxOf(GraphicPoint::y) - minY)
        }

        companion object {
            fun identity(): SvgTransform = SvgTransform()
        }
    }

    internal sealed interface SvgTransformOperation {
        fun apply(point: GraphicPoint): GraphicPoint

        data class Translate(val dx: Double, val dy: Double) : SvgTransformOperation {
            override fun apply(point: GraphicPoint): GraphicPoint = GraphicPoint(point.x + dx, point.y + dy)
        }

        data class Scale(val x: Double, val y: Double) : SvgTransformOperation {
            override fun apply(point: GraphicPoint): GraphicPoint = GraphicPoint(point.x * x, point.y * y)
        }

        data class Rotate(val angleDegrees: Double) : SvgTransformOperation {
            override fun apply(point: GraphicPoint): GraphicPoint {
                val radians = angleDegrees * Math.PI / 180.0
                val sin = sin(radians)
                val cos = cos(radians)
                return GraphicPoint(point.x * cos - point.y * sin, point.x * sin + point.y * cos)
            }
        }
    }

    private fun positiveAxisBounds(
        minimum: Double,
        maximum: Double,
        containerStart: Double,
        containerSize: Double,
    ): Pair<Double, Double> {
        if (maximum > minimum) return minimum to (maximum - minimum)
        val size = minOf(0.001, containerSize)
        val start = (minimum - size / 2.0).coerceIn(containerStart, containerStart + containerSize - size)
        return start to size
    }

    private fun String.spanOf(token: String): SourceSpan {
        val start = indexOf(token).takeIf { it >= 0 } ?: 0
        fun position(offset: Int): SourcePosition {
            val clamped = offset.coerceIn(0, length)
            val prefix = substring(0, clamped)
            return SourcePosition(
                offset = clamped,
                line = prefix.count { it == '\n' } + 1,
                column = clamped - prefix.lastIndexOf('\n'),
            )
        }
        return SourceSpan(position(start), position(start + token.length))
    }
}

internal data class SvgGraphicBodyCompilation(
    val svgFile: String,
    val document: com.engineeringood.athena.representation.GraphicPrimitiveDocument? = null,
    val diagnostics: List<AthenaRepresentationSourceDiagnostic> = emptyList(),
    val root: Element? = null,
    val primitiveIdsByGeometryRef: Map<String, List<GraphicPrimitiveId>> = emptyMap(),
)

internal data class PrimitiveWithNode(
    val primitive: GraphicPrimitive,
    val node: Element,
    val geometryRef: String? = null,
)

internal data class SvgTreeMetrics(
    val elements: Int,
    val maxDepth: Int,
)
