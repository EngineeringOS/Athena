package com.engineeringood.athena.presentation

import java.io.ByteArrayInputStream
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.imageio.ImageIO
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException

private const val SVG_SAFE_PROFILE_ID = "svg-safe-1"
private const val MAX_SVG_SOURCE_BYTES = 2 * 1024 * 1024
private const val MAX_PNG_DIMENSION = 4096
private const val MAX_PNG_DECODED_BYTES = 64L * 1024L * 1024L
private const val MAX_WOFF2_BYTES = 4 * 1024 * 1024
private const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"

private val allowedSvgElements = setOf("svg", "g", "path", "rect", "circle", "ellipse", "line", "polyline", "polygon", "defs", "clipPath", "title", "desc")
private val forbiddenSvgElements = setOf("script", "style", "animate", "animateTransform", "filter", "foreignObject", "text", "use", "image")
private val allowedSvgAttributes = setOf(
    "viewBox", "transform", "d", "x", "y", "x1", "y1", "x2", "y2", "width", "height", "cx", "cy", "r", "rx", "ry",
    "points", "fill", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin", "stroke-dasharray", "fill-rule", "clip-path", "id",
)

data class AssetAdmissionInput(
    val relativePath: String,
    val mediaKind: AssetMediaKind,
    val bytes: ByteArray,
    val profileId: String = SVG_SAFE_PROFILE_ID,
    val intrinsicBounds: SceneBounds? = null,
)

data class AdmittedSceneAsset(
    val asset: SceneAsset,
    val entry: AssetBundleEntry,
    val trace: SceneTrace,
)

sealed interface AssetAdmissionResult {
    data class Admitted(val asset: AdmittedSceneAsset) : AssetAdmissionResult
    data class Rejected(val diagnostic: SceneDiagnostic) : AssetAdmissionResult
}

/** Admits local package bytes before they become renderer-visible Presentation facts. */
object PresentationAssetCompiler {
    fun admit(input: AssetAdmissionInput): AssetAdmissionResult = runCatching {
        require(isPortableLocalPath(input.relativePath)) { "Asset path must be a portable local package-relative path." }
        require(input.profileId == SVG_SAFE_PROFILE_ID) { "Unknown asset safety profile `${input.profileId}`." }
        val canonical = when (input.mediaKind) {
            AssetMediaKind.SVG -> canonicalSvg(input)
            AssetMediaKind.PNG -> canonicalPng(input)
            AssetMediaKind.WOFF2 -> canonicalWoff2(input)
        }
        val contentDigest = "sha256:${sha256(canonical.bytes)}"
        val assetId = PresentationAssetIdentity.assetId(input.mediaKind, input.profileId, canonical.bytes)
        val trace = assetTrace(input.relativePath, input.bytes, assetId)
        val entry = AssetBundleEntry("bundle:${contentDigest.removePrefix("sha256:")}", contentDigest, canonical.bytes)
        AssetAdmissionResult.Admitted(
            AdmittedSceneAsset(
                asset = SceneAsset(assetId, contentDigest, input.mediaKind, input.profileId, canonical.bounds, entry.entryId, trace.traceId),
                entry = entry,
                trace = trace,
            ),
        )
    }.getOrElse { failure ->
        AssetAdmissionResult.Rejected(
            SceneDiagnostic(
                subject = input.relativePath,
                problem = failure.message ?: "Asset admission failed.",
                correction = "Use a local asset that conforms to the svg-safe-1 profile.",
                code = diagnosticCode(failure),
            ),
        )
    }

    private fun canonicalSvg(input: AssetAdmissionInput): CanonicalAsset {
        require(input.bytes.size <= MAX_SVG_SOURCE_BYTES) { "SVG source exceeds the 2 MiB svg-safe-1 limit." }
        val source = strictUtf8(input.bytes)
        require(!source.contains("<!DOCTYPE", ignoreCase = true)) { "SVG DTD declarations are prohibited." }
        require(!source.contains("<!ENTITY", ignoreCase = true)) { "SVG entity declarations are prohibited." }
        val document = secureBuilder().parse(InputSource(StringReader(source)))
        val root = document.documentElement ?: error("SVG has no root element.")
        require(root.localNameOrNodeName() == "svg" && (root.namespaceURI == null || root.namespaceURI == SVG_NAMESPACE)) { "SVG root element must be svg." }
        val serialized = serializeSvg(root, depth = 0, counter = ElementCounter())
        val bounds = viewBoxBounds(root)
        require(input.intrinsicBounds == null || input.intrinsicBounds == bounds) { "SVG declared intrinsic bounds do not match its viewBox." }
        return CanonicalAsset(serialized.toByteArray(StandardCharsets.UTF_8), bounds)
    }

    private fun canonicalPng(input: AssetAdmissionInput): CanonicalAsset {
        require(input.bytes.size >= 24) { "PNG is incomplete." }
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
        require(input.bytes.copyOfRange(0, 8).contentEquals(signature)) { "PNG signature is invalid." }
        require(input.bytes.copyOfRange(12, 16).decodeToString() == "IHDR") { "PNG must begin with an IHDR chunk." }
        val width = ByteBuffer.wrap(input.bytes, 16, 4).int
        val height = ByteBuffer.wrap(input.bytes, 20, 4).int
        require(width in 1..MAX_PNG_DIMENSION && height in 1..MAX_PNG_DIMENSION) { "PNG dimensions exceed the svg-safe-1 limit." }
        require(width.toLong() * height.toLong() * 4L <= MAX_PNG_DECODED_BYTES) { "PNG decoded bytes exceed the svg-safe-1 limit." }
        val image = ImageIO.read(ByteArrayInputStream(input.bytes))
        require(image != null && image.width == width && image.height == height) { "PNG bytes are malformed." }
        val bounds = SceneBounds(0, 0, width, height)
        require(input.intrinsicBounds == null || input.intrinsicBounds == bounds) { "PNG declared intrinsic bounds do not match its IHDR dimensions." }
        return CanonicalAsset(input.bytes.copyOf(), bounds)
    }

    private fun canonicalWoff2(input: AssetAdmissionInput): CanonicalAsset {
        require(input.bytes.size <= MAX_WOFF2_BYTES) { "WOFF2 source exceeds the 4 MiB svg-safe-1 limit." }
        require(input.bytes.size >= WOFF2_HEADER_BYTES) { "WOFF2 header is incomplete." }
        require(input.bytes.copyOfRange(0, 4).decodeToString() == "wOF2") { "WOFF2 signature is invalid." }
        val declaredLength = unsignedInt(input.bytes, 8)
        val tableCount = unsignedShort(input.bytes, 12)
        val compressedBytes = unsignedInt(input.bytes, 20)
        require(declaredLength == input.bytes.size.toLong()) { "WOFF2 declared length does not match its bytes." }
        require(tableCount > 0) { "WOFF2 must declare at least one table." }
        require(compressedBytes <= input.bytes.size - WOFF2_HEADER_BYTES) { "WOFF2 compressed data exceeds its bytes." }
        require(input.intrinsicBounds == null) { "WOFF2 font assets do not have visual intrinsic bounds." }
        return CanonicalAsset(input.bytes.copyOf(), null)
    }

    private fun secureBuilder() = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        setFeature("http://xml.org/sax/features/external-general-entities", false)
        setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
    }.newDocumentBuilder().apply {
        setEntityResolver { _, _ -> InputSource(StringReader("")) }
    }

    private fun serializeSvg(element: Element, depth: Int, counter: ElementCounter): String {
        require(depth <= MAX_SVG_DOM_DEPTH) { "SVG DOM depth exceeds svg-safe-1 limit." }
        require(++counter.value <= MAX_SVG_ELEMENTS) { "SVG element count exceeds svg-safe-1 limit." }
        val name = element.localNameOrNodeName()
        require(name in allowedSvgElements) {
            if (name in forbiddenSvgElements) "SVG element `$name` is prohibited by svg-safe-1." else "SVG element `$name` is not admitted by svg-safe-1."
        }
        require(element.namespaceURI == null || element.namespaceURI == SVG_NAMESPACE) { "SVG foreign namespaces are prohibited." }
        val attributes = buildList {
            for (index in 0 until element.attributes.length) {
                val attribute = element.attributes.item(index)
                val attributeName = attribute.nodeName
                if (attributeName == "xmlns" || attribute.prefix == "xmlns") {
                    require(attribute.nodeValue == SVG_NAMESPACE) { "SVG foreign namespaces are prohibited." }
                    add(attributeName to attribute.nodeValue)
                } else {
                    require(!attributeName.lowercase().startsWith("on")) { "SVG event attributes are prohibited." }
                    require(attributeName in allowedSvgAttributes) { "SVG attribute `$attributeName` is not admitted by svg-safe-1." }
                    validateAttributeValue(attributeName, attribute.nodeValue)
                    add(attributeName to attribute.nodeValue)
                }
            }
        }.sortedWith(compareBy<Pair<String, String>> { if (it.first == "xmlns") 0 else 1 }.thenBy { it.first })
        val children = buildList {
            for (index in 0 until element.childNodes.length) {
                val child = element.childNodes.item(index)
                when (child.nodeType) {
                    Node.ELEMENT_NODE -> add(serializeSvg(child as Element, depth + 1, counter))
                    Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> {
                        val text = child.nodeValue.orEmpty()
                        if (text.isNotBlank()) {
                            require(name in setOf("title", "desc")) { "SVG text content is admitted only in title or desc." }
                            add(escapeText(text))
                        }
                    }
                    Node.COMMENT_NODE -> Unit
                    else -> error("SVG node type ${child.nodeType} is not admitted by svg-safe-1.")
                }
            }
        }
        val attributesText = attributes.joinToString("") { " ${it.first}=\"${escapeAttribute(it.second)}\"" }
        return if (children.isEmpty()) "<$name$attributesText/>" else "<$name$attributesText>${children.joinToString("")}</$name>"
    }

    private fun validateAttributeValue(name: String, value: String) {
        val lower = value.lowercase()
        if (name == "style" || lower.contains("url(") && !(name == "clip-path" && value.matches(Regex("url\\(#[A-Za-z_][A-Za-z0-9_.:-]*\\)")))) {
            error("SVG external or non-local URL values are prohibited.")
        }
        require(!Regex("^(?:https?|data|file|javascript):", RegexOption.IGNORE_CASE).containsMatchIn(value.trim())) { "SVG external URL values are prohibited." }
    }

    private fun viewBoxBounds(root: Element): SceneBounds {
        val parts = root.getAttribute("viewBox").trim().split(Regex("\\s+"))
        require(parts.size == 4) { "SVG requires an integer viewBox." }
        val values = parts.map { it.toIntOrNull() ?: error("SVG viewBox must use integer logical units.") }
        return SceneBounds(values[0], values[1], values[2], values[3])
    }

    private fun strictUtf8(bytes: ByteArray): String = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        error("SVG source is not valid UTF-8.")
    }

    private fun assetTrace(relativePath: String, sourceBytes: ByteArray, assetId: AssetId): SceneTrace {
        val sourceDigest = sha256(sourceBytes)
        val traceId = TraceId("trace:sha256:${sha256("ASSET_DEFINITION|$relativePath|$sourceDigest|${assetId.value}".toByteArray(StandardCharsets.UTF_8))}")
        return SceneTrace(
            traceId,
            listOf(SourceOrigin(relativePath, sourceDigest, TraceRole.ASSET_DEFINITION, 0, 0, 0, 0, assetId.value, true)),
        )
    }

    private fun diagnosticCode(failure: Throwable): String {
        val message = failure.message.orEmpty().lowercase()
        return when {
            "portable local package-relative" in message -> "asset.path.invalid"
            "dtd" in message -> "asset.svg.doctype"
            "entity" in message -> "asset.svg.entity"
            "event attribute" in message -> "asset.svg.event-attribute"
            "external" in message || "url" in message -> "asset.svg.external-url"
            "`style`" in message || "css" in message -> "asset.svg.css"
            "prohibited" in message -> "asset.svg.forbidden-element"
            "attribute" in message && "not admitted" in message -> "asset.svg.unknown-attribute"
            "element" in message && "not admitted" in message -> "asset.svg.unknown-element"
            failure is SAXParseException || "malformed" in message -> "asset.svg.malformed"
            "svg source exceeds" in message || "viewbox" in message -> "asset.svg.limit"
            "png dimensions" in message || "png decoded" in message -> "asset.png.limit"
            "png" in message -> "asset.png.invalid"
            "woff2 source exceeds" in message -> "asset.woff2.limit"
            "woff2" in message -> "asset.woff2.invalid"
            else -> "asset.svg.rejected"
        }
    }

    private fun Element.localNameOrNodeName(): String = localName ?: nodeName.substringAfter(':')

    private fun isPortableLocalPath(path: String): Boolean = path.isNotBlank() &&
        !path.startsWith('/') &&
        !path.contains('\\') &&
        !path.contains("://") &&
        path.split('/').all { segment -> segment.isNotBlank() && segment != "." && segment != ".." }

    private fun unsignedInt(bytes: ByteArray, offset: Int): Long = ByteBuffer.wrap(bytes, offset, 4).int.toLong() and 0xffff_ffffL

    private fun unsignedShort(bytes: ByteArray, offset: Int): Int = ByteBuffer.wrap(bytes, offset, 2).short.toInt() and 0xffff

    private fun escapeAttribute(value: String): String = value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
    private fun escapeText(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    fun bundle(inputRevision: InputRevision, admitted: List<AdmittedSceneAsset>): AssetBundle = AssetBundle(
        inputRevision = inputRevision,
        entries = admitted.map { it.entry }.distinctBy { it.digest }.sortedBy { it.entryId },
    )
}

private class ElementCounter(var value: Int = 0)

private data class CanonicalAsset(val bytes: ByteArray, val bounds: SceneBounds?)

object PresentationAssetIdentity {
    fun assetId(mediaKind: AssetMediaKind, profileId: String, canonicalBytes: ByteArray): AssetId =
        AssetId("asset:sha256:${identityDigest(mediaKind, profileId, canonicalBytes)}")
}

private fun identityDigest(mediaKind: AssetMediaKind, profileId: String, canonicalBytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(mediaKind.name.toByteArray(StandardCharsets.UTF_8)); digest.update(0)
    digest.update(profileId.toByteArray(StandardCharsets.UTF_8)); digest.update(0)
    digest.update(canonicalBytes)
    return digest.digest().joinToString("") { "%02x".format(it) }
}

private const val WOFF2_HEADER_BYTES = 48
private const val MAX_SVG_DOM_DEPTH = 32
private const val MAX_SVG_ELEMENTS = 512
