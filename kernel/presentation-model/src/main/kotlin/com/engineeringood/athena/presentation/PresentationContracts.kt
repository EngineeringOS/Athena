package com.engineeringood.athena.presentation

private val SHA_ID = Regex("^[a-z][a-z-]*:sha256:[0-9a-f]{64}$")
private val SHA_DIGEST = Regex("^sha256:[0-9a-f]{64}$")
private val INPUT_REVISION = Regex("^input:sha256:[0-9a-f]{64}$")

@JvmInline value class SceneId(val value: String) { init { require(SHA_ID.matches(value) && value.startsWith("scene:")) } }
@JvmInline value class SceneElementId(val value: String) { init { require(SHA_ID.matches(value)) } }
@JvmInline value class StyleId(val value: String) { init { require(SHA_ID.matches(value) && value.startsWith("style:")) } }
@JvmInline value class AssetId(val value: String) { init { require(SHA_ID.matches(value) && value.startsWith("asset:")) } }
@JvmInline value class TraceId(val value: String) { init { require(SHA_ID.matches(value) && value.startsWith("trace:")) } }
@JvmInline value class InputRevision(val value: String) { init { require(INPUT_REVISION.matches(value)) } }
@JvmInline value class SceneDigest(val value: String) {
    init { require(SHA_DIGEST.matches(value)) }
    companion object { fun uncomputed() = SceneDigest("sha256:${"0".repeat(64)}") }
}

data class ScenePoint(val x: Int, val y: Int)

data class SceneBounds(val x: Int, val y: Int, val width: Int, val height: Int) {
    init { require(width > 0 && height > 0) }
}

data class ScenePage(val pageBounds: SceneBounds, val drawingBounds: SceneBounds) {
    init {
        require(drawingBounds.x >= pageBounds.x && drawingBounds.y >= pageBounds.y)
        require(drawingBounds.x + drawingBounds.width <= pageBounds.x + pageBounds.width)
        require(drawingBounds.y + drawingBounds.height <= pageBounds.y + pageBounds.height)
    }
}

enum class CoordinateLabelMode { ALPHA, NUMERIC }

data class ScenePlotFrame(
    val columns: Int,
    val rows: Int,
    val columnLabels: CoordinateLabelMode = CoordinateLabelMode.ALPHA,
    val rowLabels: CoordinateLabelMode = CoordinateLabelMode.NUMERIC,
) {
    init {
        require(columns > 0 && rows > 0)
        require(columnLabels == CoordinateLabelMode.ALPHA)
        require(rowLabels == CoordinateLabelMode.NUMERIC)
    }
}

data class SceneSnapGrid(
    val sheetId: String,
    val step: Int,
    val drawingOrigin: ScenePoint,
    val formulaVersion: String = "athena-grid-2",
) {
    init {
        require(sheetId.isNotBlank())
        require(step > 0)
        require(formulaVersion == "athena-grid-2")
    }
}

data class ResolvedStyle(
    val styleId: StyleId,
    val strokeRgba: String,
    val fillRgba: String,
    val strokeWidth: Int,
    val dash: List<Int> = emptyList(),
    val lineCap: StrokeLineCap = StrokeLineCap.BUTT,
    val lineJoin: StrokeLineJoin = StrokeLineJoin.MITER,
    val fillRule: FillRule = FillRule.NONZERO,
    val opacity: Int = 255,
    val fontAssetId: AssetId? = null,
    val fontSize: Int = 1,
    val fontWeight: Int = 400,
    val textAlign: TextAlign = TextAlign.START,
    val textBaseline: TextBaseline = TextBaseline.ALPHABETIC,
    val routeMarker: RouteMarker = RouteMarker.NONE,
    val portDisplay: PortDisplay = PortDisplay.MARKER,
) {
    init {
        require(Regex("^#[0-9a-f]{8}$").matches(strokeRgba))
        require(Regex("^#[0-9a-f]{8}$").matches(fillRgba))
        require(strokeWidth > 0 && dash.all { it > 0 })
        require(opacity in 0..255)
        require(fontSize > 0 && fontWeight in 1..1000)
    }
}

enum class StrokeLineCap { BUTT, ROUND, SQUARE }
enum class StrokeLineJoin { MITER, ROUND, BEVEL }
enum class FillRule { NONZERO, EVENODD }
enum class TextAlign { START, MIDDLE, END }
enum class TextBaseline { ALPHABETIC, MIDDLE, HANGING }
enum class RouteMarker { NONE, END_ARROW }
enum class PortDisplay { HIDDEN, MARKER, MARKER_AND_LABEL }

enum class AssetMediaKind { SVG, PNG, WOFF2 }

data class SceneAsset(
    val assetId: AssetId,
    val digest: String,
    val mediaKind: AssetMediaKind,
    val profileId: String,
    val intrinsicBounds: SceneBounds? = null,
    val bundleEntryId: String,
    val traceId: TraceId,
) {
    init {
        require(SHA_DIGEST.matches(digest) && profileId.isNotBlank() && bundleEntryId.isNotBlank())
        require((mediaKind == AssetMediaKind.WOFF2) == (intrinsicBounds == null))
    }
}

enum class PortDirection { IN, OUT, BIDIRECTIONAL }

data class ScenePort(
    val elementId: SceneElementId,
    val anchorId: String,
    val semanticPortId: String,
    val point: ScenePoint,
    val hitRadius: Int,
    val direction: PortDirection,
    val styleId: StyleId,
    val traceId: TraceId,
) {
    init { require(anchorId.isNotBlank() && semanticPortId.isNotBlank() && hitRadius > 0) }
}

data class SceneLabel(
    val elementId: SceneElementId,
    val role: String,
    val text: String,
    val anchor: ScenePoint,
    val bounds: SceneBounds,
    val rotationDegrees: Int,
    val styleId: StyleId,
    val traceId: TraceId,
) {
    init { require(role.isNotBlank()) }
}

data class SceneOccurrence(
    val elementId: SceneElementId,
    val occurrenceId: String,
    val subjectId: String,
    val semanticId: String = subjectId,
    val representationRef: String? = null,
    val bounds: SceneBounds,
    val placementAnchor: ScenePoint,
    val zIndex: Int,
    val styleId: StyleId,
    val traceId: TraceId,
    val assetId: AssetId? = null,
    val ports: List<ScenePort> = emptyList(),
    val labels: List<SceneLabel> = emptyList(),
) {
    init { require(occurrenceId.isNotBlank() && subjectId.isNotBlank() && semanticId.isNotBlank()) }
}

enum class SceneConnectionSegmentKind { ORTHOGONAL, SHARED }

data class SceneConnectionSegment(
    val segmentId: String,
    val start: ScenePoint,
    val end: ScenePoint,
    val kind: SceneConnectionSegmentKind = SceneConnectionSegmentKind.ORTHOGONAL,
) {
    init {
        require(segmentId.isNotBlank())
        require(start != end && (start.x == end.x || start.y == end.y)) { "Scene connection segments must be orthogonal and non-empty." }
    }
}

enum class SceneConnectionMarkerKind { JUNCTION, CROSSING, INTERRUPTION_START, INTERRUPTION_END }

data class SceneConnectionMarker(
    val elementId: SceneElementId,
    val kind: SceneConnectionMarkerKind,
    val point: ScenePoint,
    val relatedConnectionIds: List<String>,
    val bridgeOwner: Boolean = false,
    val traceId: TraceId,
) {
    init {
        require(relatedConnectionIds.isNotEmpty() && relatedConnectionIds.all(String::isNotBlank))
    }
}

data class SceneConnectionAnnotation(
    val elementId: SceneElementId,
    val semanticId: String,
    val displayRole: String,
    val value: String,
    val anchor: ScenePoint,
    val bounds: SceneBounds,
    val traceId: TraceId,
) {
    init { require(semanticId.isNotBlank() && displayRole.isNotBlank() && value.isNotBlank()) }
}

data class SceneConnection(
    val elementId: SceneElementId,
    val connectionId: String,
    val projectionId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val segments: List<SceneConnectionSegment>,
    val markers: List<SceneConnectionMarker> = emptyList(),
    val annotations: List<SceneConnectionAnnotation> = emptyList(),
    val zIndex: Int,
    val styleId: StyleId,
    val traceId: TraceId,
) {
    init {
        require(connectionId.isNotBlank() && projectionId.isNotBlank() && sourceAnchorId.isNotBlank() && targetAnchorId.isNotBlank())
        require(segments.isNotEmpty())
        require(segments.zipWithNext().all { it.first.end == it.second.start })
    }
}

enum class DecorationKind { PAGE_BACKGROUND, FRAME_SEGMENT, COORDINATE_LABEL }

data class SceneDecoration(
    val elementId: SceneElementId,
    val kind: DecorationKind,
    val bounds: SceneBounds,
    val zIndex: Int,
    val styleId: StyleId,
    val traceId: TraceId,
    val text: String? = null,
)

enum class TraceRole {
    SEMANTIC_DECLARATION,
    PORT_DECLARATION,
    RELATIONSHIP_DECLARATION,
    SHEET_DECLARATION,
    SHEET_PLACEMENT,
    ASSET_DEFINITION,
    SPATIAL_DERIVATION,
}

data class SourceOrigin(
    val relativePath: String,
    val sourceDigest: String,
    val role: TraceRole,
    val startLine: Int,
    val startCharacter: Int,
    val endLine: Int,
    val endCharacter: Int,
    val subjectId: String,
    val primary: Boolean,
) {
    init {
        require(relativePath.isNotBlank() && Regex("^[0-9a-f]{64}$").matches(sourceDigest) && subjectId.isNotBlank())
        require(startLine >= 0 && startCharacter >= 0 && endLine >= startLine && endCharacter >= 0)
    }
}

data class SceneTrace(val traceId: TraceId, val origins: List<SourceOrigin>) {
    init { require(origins.isNotEmpty() && origins.count { it.primary } == 1) }
}

data class AthenaDiagramScene(
    val schemaVersion: Int = 2,
    val sceneId: SceneId,
    val inputRevision: InputRevision,
    val sceneDigest: SceneDigest,
    val page: ScenePage,
    val plotFrame: ScenePlotFrame = ScenePlotFrame(columns = 1, rows = 1),
    val snapGrid: SceneSnapGrid,
    val styles: List<ResolvedStyle>,
    val assets: List<SceneAsset>,
    val occurrences: List<SceneOccurrence>,
    val connections: List<SceneConnection>,
    val decorations: List<SceneDecoration>,
    val traces: List<SceneTrace>,
) { init { require(schemaVersion == 2) } }

data class AssetBundleEntry(val entryId: String, val digest: String, val bytes: ByteArray) {
    init {
        require(entryId.isNotBlank() && SHA_DIGEST.matches(digest))
        require(digest == "sha256:${sha256(bytes)}")
    }
    override fun equals(other: Any?) = other is AssetBundleEntry && entryId == other.entryId && digest == other.digest && bytes.contentEquals(other.bytes)
    override fun hashCode() = 31 * (31 * entryId.hashCode() + digest.hashCode()) + bytes.contentHashCode()
}

data class AssetBundle(val inputRevision: InputRevision, val entries: List<AssetBundleEntry>) {
    init {
        require(entries.map { it.entryId }.distinct().size == entries.size) { "Asset bundle entry ids must be unique." }
        require(entries.map { it.digest }.distinct().size == entries.size) { "Asset bundle bytes must be deduplicated." }
        require(entries.sumOf { it.bytes.size.toLong() } <= MAX_PUBLICATION_ASSET_BYTES) { "Asset bundle exceeds the svg-safe-1 encoded byte limit." }
    }
}

data class SceneDiagnostic(val subject: String, val problem: String, val correction: String, val code: String) {
    init { require(subject.isNotBlank() && problem.isNotBlank() && correction.isNotBlank() && code.isNotBlank()) }
}

enum class PublicationState { READY, STALE, UNAVAILABLE }

data class AthenaScenePublication(
    val schemaVersion: Int = 1,
    val state: PublicationState,
    val attemptedInputRevision: InputRevision,
    val acceptedInputRevision: InputRevision? = null,
    val scene: AthenaDiagramScene? = null,
    val assetBundle: AssetBundle? = null,
    val diagnostics: List<SceneDiagnostic>,
) {
    val commandsEnabled get() = state == PublicationState.READY

    init {
        require(schemaVersion == 1)
        when (state) {
            PublicationState.READY -> {
                require(acceptedInputRevision == attemptedInputRevision)
                require(scene != null && assetBundle != null)
                require(diagnostics.isEmpty()) { "READY publication cannot carry diagnostics." }
                require(scene.inputRevision == acceptedInputRevision && assetBundle.inputRevision == acceptedInputRevision)
                AthenaDiagramSceneContract.validate(scene, assetBundle)
            }
            PublicationState.STALE -> {
                require(acceptedInputRevision != null && scene != null && assetBundle != null && diagnostics.isNotEmpty())
                require(scene.inputRevision == acceptedInputRevision && assetBundle.inputRevision == acceptedInputRevision)
                AthenaDiagramSceneContract.validate(scene, assetBundle)
            }
            PublicationState.UNAVAILABLE -> {
                require(acceptedInputRevision == null && scene == null && assetBundle == null && diagnostics.isNotEmpty())
            }
        }
    }

    companion object {
        fun ready(scene: AthenaDiagramScene, assets: AssetBundle) = AthenaScenePublication(
            state = PublicationState.READY,
            attemptedInputRevision = scene.inputRevision,
            acceptedInputRevision = scene.inputRevision,
            scene = scene,
            assetBundle = assets,
            diagnostics = emptyList(),
        )

        fun unavailable(revision: InputRevision, diagnostic: SceneDiagnostic) = AthenaScenePublication(
            state = PublicationState.UNAVAILABLE,
            attemptedInputRevision = revision,
            diagnostics = listOf(diagnostic),
        )
    }
}

internal const val MAX_PUBLICATION_ASSET_BYTES = 32 * 1024 * 1024

internal fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
