package com.engineeringood.athena.svg

import com.engineeringood.athena.presentation.AssetAdmissionInput
import com.engineeringood.athena.presentation.AssetAdmissionResult
import com.engineeringood.athena.presentation.AssetBundle
import com.engineeringood.athena.presentation.AssetMediaKind
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.AthenaDiagramSceneContract
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.PortDirection
import com.engineeringood.athena.presentation.PresentationAssetCompiler
import com.engineeringood.athena.presentation.ResolvedStyle
import com.engineeringood.athena.presentation.SceneBounds
import com.engineeringood.athena.presentation.SceneDecoration
import com.engineeringood.athena.presentation.SceneDigest
import com.engineeringood.athena.presentation.SceneElementId
import com.engineeringood.athena.presentation.SceneId
import com.engineeringood.athena.presentation.SceneOccurrence
import com.engineeringood.athena.presentation.ScenePage
import com.engineeringood.athena.presentation.ScenePoint
import com.engineeringood.athena.presentation.ScenePort
import com.engineeringood.athena.presentation.SceneConnection
import com.engineeringood.athena.presentation.SceneConnectionAnnotation
import com.engineeringood.athena.presentation.SceneConnectionMarker
import com.engineeringood.athena.presentation.SceneConnectionMarkerKind
import com.engineeringood.athena.presentation.SceneConnectionSegment
import com.engineeringood.athena.presentation.SceneSnapGrid
import com.engineeringood.athena.presentation.SceneTrace
import com.engineeringood.athena.presentation.SourceOrigin
import com.engineeringood.athena.presentation.StyleId
import com.engineeringood.athena.presentation.TraceId
import com.engineeringood.athena.presentation.TraceRole
import com.engineeringood.athena.presentation.DecorationKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AthenaSvgRendererTest {
    @Test
    fun `renderer emits deterministic clean page from canonical scene and bundle only`() {
        val revision = InputRevision("input:sha256:${"1".repeat(64)}")
        val style = ResolvedStyle(StyleId("style:sha256:${"2".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val trace = SceneTrace(
            TraceId("trace:sha256:${"3".repeat(64)}"),
            listOf(SourceOrigin("sheet.athena", "${"4".repeat(64)}", TraceRole.SHEET_DECLARATION, 0, 0, 0, 0, "sheet", true)),
        )
        val scene = AthenaDiagramSceneContract.canonicalize(
            AthenaDiagramScene(
                sceneId = SceneId("scene:sha256:${"5".repeat(64)}"),
                inputRevision = revision,
                sceneDigest = SceneDigest.uncomputed(),
                page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
                snapGrid = SceneSnapGrid("sheet", 4, ScenePoint(2, 2)),
                styles = listOf(style),
                assets = emptyList(),
                occurrences = emptyList(),
                connections = listOf(
                    SceneConnection(
                        elementId = SceneElementId("route:sha256:${"d".repeat(64)}"),
                        connectionId = "relationship:S1.output:S2.input",
                        projectionId = "projection:S1.output:S2.input",
                        sourceAnchorId = "port:S1.output",
                        targetAnchorId = "port:S2.input",
                        segments = listOf(SceneConnectionSegment("route-segment", ScenePoint(2, 10), ScenePoint(18, 10))),
                        zIndex = 0,
                        styleId = style.styleId,
                        traceId = trace.traceId,
                    ),
                ),
                decorations = listOf(
                    SceneDecoration(SceneElementId("decoration:sha256:${"6".repeat(64)}"), DecorationKind.PAGE_BACKGROUND, SceneBounds(0, 0, 20, 20), -100, style.styleId, trace.traceId),
                    SceneDecoration(SceneElementId("decoration:sha256:${"7".repeat(64)}"), DecorationKind.FRAME_SEGMENT, SceneBounds(0, 0, 20, 1), -90, style.styleId, trace.traceId),
                ),
                traces = listOf(trace),
            ),
        )

        assertEquals(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 20 20\"><rect fill=\"#ffffff\" height=\"20\" width=\"20\" x=\"0\" y=\"0\"/><line stroke=\"#000000\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-width=\"1\" vector-effect=\"non-scaling-stroke\" shape-rendering=\"crispEdges\" x1=\"0.5\" x2=\"19.5\" y1=\"0.5\" y2=\"0.5\"/><polyline fill=\"none\" points=\"2,10 18,10\" stroke=\"#000000\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-width=\"1\" vector-effect=\"non-scaling-stroke\"/></svg>",
            AthenaSvgRenderer.render(scene, AssetBundle(revision, emptyList())),
        )
    }

    @Test
    fun `renderer paints explicit connection topology and selected annotation deterministically`() {
        val revision = InputRevision("input:sha256:${"e".repeat(64)}")
        val style = ResolvedStyle(StyleId("style:sha256:${"f".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val trace = SceneTrace(
            TraceId("trace:sha256:${"1".repeat(64)}"),
            listOf(SourceOrigin("project.athena", "2".repeat(64), TraceRole.RELATIONSHIP_DECLARATION, 0, 0, 0, 0, "connection:C1", true)),
        )
        val connection = SceneConnection(
            elementId = SceneElementId("connection:sha256:${"3".repeat(64)}"),
            connectionId = "connection:C1",
            projectionId = "projection:C1",
            sourceAnchorId = "anchor:A",
            targetAnchorId = "anchor:B",
            segments = listOf(SceneConnectionSegment("segment:C1:0", ScenePoint(2, 10), ScenePoint(18, 10))),
            markers = listOf(
                SceneConnectionMarker(SceneElementId("junction:sha256:${"4".repeat(64)}"), SceneConnectionMarkerKind.JUNCTION, ScenePoint(6, 10), listOf("connection:C1", "connection:C2"), traceId = trace.traceId),
                SceneConnectionMarker(SceneElementId("crossing:sha256:${"5".repeat(64)}"), SceneConnectionMarkerKind.CROSSING, ScenePoint(10, 10), listOf("connection:C1", "connection:C3"), bridgeOwner = true, traceId = trace.traceId),
                SceneConnectionMarker(SceneElementId("interruption:sha256:${"6".repeat(64)}"), SceneConnectionMarkerKind.INTERRUPTION_START, ScenePoint(14, 10), listOf("continuation:A", "continuation:B"), traceId = trace.traceId),
            ),
            annotations = listOf(SceneConnectionAnnotation(
                SceneElementId("annotation:sha256:${"7".repeat(64)}"), "connection:C1", "KIND", "POWER", ScenePoint(8, 8), SceneBounds(7, 7, 5, 2), trace.traceId,
            )),
            zIndex = 0,
            styleId = style.styleId,
            traceId = trace.traceId,
        )
        val scene = AthenaDiagramSceneContract.canonicalize(AthenaDiagramScene(
            sceneId = SceneId("scene:sha256:${"8".repeat(64)}"),
            inputRevision = revision,
            sceneDigest = SceneDigest.uncomputed(),
            page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
            snapGrid = SceneSnapGrid("sheet", 4, ScenePoint(2, 2)),
            styles = listOf(style),
            assets = emptyList(),
            occurrences = emptyList(),
            connections = listOf(connection),
            decorations = emptyList(),
            traces = listOf(trace),
        ))
        val first = AthenaSvgRenderer.render(scene, AssetBundle(revision, emptyList()))
        assertEquals(first, AthenaSvgRenderer.render(scene, AssetBundle(revision, emptyList())))
        assertTrue(first.contains("<circle cx=\"6\" cy=\"10\" r=\"1\""))
        assertTrue(first.contains("Q10,8"))
        assertTrue(first.contains("<circle cx=\"14\" cy=\"10\" r=\"0.5\""))
        assertTrue(first.contains(">POWER</text>"))
        assertTrue(first.contains("vector-effect=\"non-scaling-stroke\""))
    }

    @Test
    fun `renderer embeds admitted geometry deterministically without painting port hit targets`() {
        val (scene, bundle) = assetBackedScene()

        val first = AthenaSvgRenderer.render(scene, bundle)
        val second = AthenaSvgRenderer.render(scene, bundle)

        assertEquals(first, second)
        assertTrue(first.contains("<image"))
        assertTrue(first.contains("data:image/svg+xml;base64,"))
        assertFalse(first.contains("<circle"))
        assertFalse(first.contains("r=\"9\""))
    }

    @Test
    fun `renderer rejects occurrence without admitted asset instead of drawing fallback geometry`() {
        val (scene, bundle) = assetBackedScene()
        val unbacked = AthenaDiagramSceneContract.canonicalize(
            scene.copy(
                assets = emptyList(),
                occurrences = scene.occurrences.map { occurrence -> occurrence.copy(assetId = null) },
            ),
        )

        val failure = assertFailsWith<IllegalArgumentException> {
            AthenaSvgRenderer.render(unbacked, AssetBundle(bundle.inputRevision, emptyList()))
        }

        assertTrue(failure.message.orEmpty().contains("admitted asset"))
    }

    private fun assetBackedScene(): Pair<AthenaDiagramScene, AssetBundle> {
        val revision = InputRevision("input:sha256:${"8".repeat(64)}")
        val admitted = assertIs<AssetAdmissionResult.Admitted>(
            PresentationAssetCompiler.admit(
                AssetAdmissionInput(
                    relativePath = "resources/switch.svg",
                    mediaKind = AssetMediaKind.SVG,
                    bytes = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10"><line x1="1" y1="5" x2="9" y2="5" stroke="#000000" stroke-width="1"/></svg>""".encodeToByteArray(),
                ),
            ),
        ).asset
        val style = ResolvedStyle(StyleId("style:sha256:${"9".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val occurrence = SceneOccurrence(
            elementId = SceneElementId("occurrence:sha256:${"a".repeat(64)}"),
            occurrenceId = "sheet/occurrence/S1",
            subjectId = "function:S1.switch",
            bounds = SceneBounds(4, 4, 10, 10),
            placementAnchor = ScenePoint(9, 9),
            zIndex = 1,
            styleId = style.styleId,
            traceId = admitted.trace.traceId,
            assetId = admitted.asset.assetId,
            ports = listOf(
                ScenePort(
                    elementId = SceneElementId("port:sha256:${"b".repeat(64)}"),
                    anchorId = "anchor:S1.output",
                    semanticPortId = "port:S1.output",
                    point = ScenePoint(14, 9),
                    hitRadius = 9,
                    direction = PortDirection.OUT,
                    styleId = style.styleId,
                    traceId = admitted.trace.traceId,
                ),
            ),
        )
        val scene = AthenaDiagramSceneContract.canonicalize(
            AthenaDiagramScene(
                sceneId = SceneId("scene:sha256:${"c".repeat(64)}"),
                inputRevision = revision,
                sceneDigest = SceneDigest.uncomputed(),
                page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
                snapGrid = SceneSnapGrid("sheet", 4, ScenePoint(2, 2)),
                styles = listOf(style),
                assets = listOf(admitted.asset),
                occurrences = listOf(occurrence),
                connections = emptyList(),
                decorations = emptyList(),
                traces = listOf(admitted.trace),
            ),
        )
        return scene to AssetBundle(revision, listOf(admitted.entry))
    }
}
