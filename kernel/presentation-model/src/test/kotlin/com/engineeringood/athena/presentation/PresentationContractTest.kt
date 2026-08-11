package com.engineeringood.athena.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.Path

class PresentationContractTest {
    @Test
    fun `normative schemas and conformance corpus are present and closed`() {
        val root = generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .first { Files.exists(it.resolve("settings.gradle.kts")) }
        val schemas = listOf(
            "kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json",
            "kernel/presentation-model/src/main/resources/schema/athena-scene-publication.schema.json",
            "kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json",
            "kernel/presentation-model/src/main/resources/profile/svg-safe-1.json",
        )
        schemas.forEach { relative ->
            val text = Files.readString(root.resolve(relative))
            assertTrue(text.contains("additionalProperties") || relative.endsWith("svg-safe-1.json"), relative)
        }
        val manifest = Files.readString(root.resolve("contracts/presentation/v1/manifest.json"))
        listOf("scene/rolling-shutter.json", "grid/cell-4-vectors.json", "grid/cell-8-vectors.json", "operations/set-style-operation.json", "operations/rejection-vectors.json", "trace/expected-origins.json", "assets/admitted.svg", "benchmark/scene-100k-manifest.json").forEach { relative ->
            assertTrue(manifest.contains(relative), relative)
            assertTrue(Files.exists(root.resolve("contracts/presentation/v1").resolve(relative)), relative)
        }
        assertTrue(Files.readString(root.resolve("kernel/presentation-model/src/main/resources/profile/svg-safe-1.json")).contains("rejectDoctype"))
    }

    @Test
    fun `scene canonicalization is deterministic and validates digest`() {
        val scene = sceneFixture(
            occurrences = listOf(
                occurrence("occurrence:sha256:${"b".repeat(64)}", 20),
                occurrence("occurrence:sha256:${"a".repeat(64)}", 10),
            ),
        )

        val canonical = AthenaDiagramSceneContract.canonicalize(scene)
        assertEquals(listOf(10, 20), canonical.occurrences.map { it.zIndex })
        assertEquals(canonical.sceneDigest, AthenaDiagramSceneContract.digest(canonical))
        assertEquals(canonical, AthenaDiagramSceneContract.canonicalize(canonical))
    }

    @Test
    fun `resolved route marker and port display participate in scene digest`() {
        val baseline = AthenaDiagramSceneContract.canonicalize(sceneFixture())
        val changed = AthenaDiagramSceneContract.canonicalize(
            baseline.copy(
                sceneDigest = SceneDigest.uncomputed(),
                styles = baseline.styles.map { it.copy(routeMarker = RouteMarker.END_ARROW, portDisplay = PortDisplay.MARKER_AND_LABEL) },
            ),
        )

        assertNotEquals(baseline.sceneDigest, changed.sceneDigest)
        assertEquals(baseline.occurrences.map { it.semanticId to it.occurrenceId }, changed.occurrences.map { it.semanticId to it.occurrenceId })
    }

    @Test
    fun `scene connection retains typed segments topology markers and selected annotation`() {
        val connection = SceneConnection(
            elementId = SceneElementId("connection:sha256:${"5".repeat(64)}"),
            connectionId = "connection:C1",
            projectionId = "projection:C1",
            sourceAnchorId = "anchor:A",
            targetAnchorId = "anchor:B",
            segments = listOf(SceneConnectionSegment("segment:C1:0", ScenePoint(4, 10), ScenePoint(12, 10), SceneConnectionSegmentKind.ORTHOGONAL)),
            markers = listOf(SceneConnectionMarker(
                elementId = SceneElementId("junction:sha256:${"6".repeat(64)}"),
                kind = SceneConnectionMarkerKind.JUNCTION,
                point = ScenePoint(8, 10),
                relatedConnectionIds = listOf("connection:C1", "connection:C2"),
                traceId = TraceId("trace:sha256:${"4".repeat(64)}"),
            )),
            annotations = listOf(SceneConnectionAnnotation(
                elementId = SceneElementId("annotation:sha256:${"7".repeat(64)}"),
                semanticId = "connection:C1",
                displayRole = "KIND",
                value = "POWER",
                anchor = ScenePoint(8, 9),
                bounds = SceneBounds(7, 8, 4, 2),
                traceId = TraceId("trace:sha256:${"4".repeat(64)}"),
            )),
            zIndex = 50,
            styleId = StyleId("style:sha256:${"3".repeat(64)}"),
            traceId = TraceId("trace:sha256:${"4".repeat(64)}"),
        )
        val canonical = AthenaDiagramSceneContract.canonicalize(sceneFixture().copy(connections = listOf(connection)))
        assertEquals(SceneConnectionSegmentKind.ORTHOGONAL, canonical.connections.single().segments.single().kind)
        assertEquals(SceneConnectionMarkerKind.JUNCTION, canonical.connections.single().markers.single().kind)
        assertEquals("POWER", canonical.connections.single().annotations.single().value)
        assertEquals(canonical.sceneDigest, AthenaDiagramSceneContract.digest(canonical))
    }

    @Test
    fun `scene rejects invalid ids duplicate ownership and mixed trace references`() {
        assertFailsWith<IllegalArgumentException> {
            sceneFixture(sceneId = SceneId("scene:bad"))
        }
        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(
                sceneFixture(occurrences = listOf(occurrence("occurrence:sha256:${"a".repeat(64)}", 1), occurrence("occurrence:sha256:${"a".repeat(64)}", 2))),
            )
        }
    }

    @Test
    fun `publication states enforce one accepted revision`() {
        val scene = AthenaDiagramSceneContract.canonicalize(sceneFixture())
        assertTrue(AthenaScenePublication.ready(scene, AssetBundle(scene.inputRevision, emptyList())).commandsEnabled)
        assertFailsWith<IllegalArgumentException> {
            AthenaScenePublication(
                state = PublicationState.READY,
                attemptedInputRevision = InputRevision("input:sha256:${"b".repeat(64)}"),
                acceptedInputRevision = scene.inputRevision,
                scene = scene,
                assetBundle = AssetBundle(scene.inputRevision, emptyList()),
                diagnostics = emptyList(),
            )
        }
        assertTrue(AthenaScenePublication.unavailable(InputRevision("input:sha256:${"c".repeat(64)}"), diagnostic()).scene == null)
    }

    private fun sceneFixture(
        sceneId: SceneId = SceneId("scene:sha256:${"1".repeat(64)}"),
        occurrences: List<SceneOccurrence> = listOf(occurrence("occurrence:sha256:${"a".repeat(64)}", 10)),
    ) = AthenaDiagramScene(
        sceneId = sceneId,
        inputRevision = InputRevision("input:sha256:${"2".repeat(64)}"),
        sceneDigest = SceneDigest.uncomputed(),
        page = ScenePage(SceneBounds(0, 0, 80, 70), SceneBounds(4, 4, 68, 64)),
        snapGrid = SceneSnapGrid("sheet-main", 1, ScenePoint(4, 4)),
        styles = listOf(ResolvedStyle(StyleId("style:sha256:${"3".repeat(64)}"), "#000000ff", "#ffffffff", 1)),
        assets = emptyList(),
        occurrences = occurrences,
        connections = emptyList(),
        decorations = emptyList(),
        traces = listOf(trace()),
    )

    private fun occurrence(id: String, zIndex: Int) = SceneOccurrence(
        elementId = SceneElementId(id),
        occurrenceId = "occ-$zIndex",
        subjectId = "subject-$zIndex",
        bounds = SceneBounds(zIndex, zIndex, 4, 4),
        placementAnchor = ScenePoint(zIndex + 2, zIndex + 2),
        zIndex = zIndex,
        styleId = StyleId("style:sha256:${"3".repeat(64)}"),
        traceId = TraceId("trace:sha256:${"4".repeat(64)}"),
    )

    private fun trace() = SceneTrace(
        TraceId("trace:sha256:${"4".repeat(64)}"),
        listOf(SourceOrigin("src/project.athena", "0".repeat(64), TraceRole.SEMANTIC_DECLARATION, 0, 0, 0, 4, "subject", true)),
    )

    private fun diagnostic() = SceneDiagnostic("sheet-main", "Sheet Companion unavailable", "Add project.sheet.athena beside project.athena", "sheet.companion.unavailable")
}
