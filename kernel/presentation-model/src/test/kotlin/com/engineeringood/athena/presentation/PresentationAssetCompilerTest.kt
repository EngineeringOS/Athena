package com.engineeringood.athena.presentation

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PresentationAssetCompilerTest {
    @Test
    fun `svg safe profile rejects excessive depth and element count`() {
        val deep = (0..40).joinToString("") { "<g>" } + "<path d=\"M0 0\"/>" + (0..40).joinToString("") { "</g>" }
        val result = PresentationAssetCompiler.admit(AssetAdmissionInput("assets/deep.svg", AssetMediaKind.SVG, "<svg viewBox=\"0 0 1 1\">$deep</svg>".toByteArray()))
        assertTrue(result is AssetAdmissionResult.Rejected)
    }
    @Test
    fun `svg safe one admits canonical shared SVG and creates traceable content addressed asset`() {
        val admitted = assertIs<AssetAdmissionResult.Admitted>(
            PresentationAssetCompiler.admit(
                AssetAdmissionInput(
                    relativePath = "assets/terminal.svg",
                    mediaKind = AssetMediaKind.SVG,
                    bytes = contract("assets/admitted.svg").toByteArray(),
                ),
            ),
        ).asset

        assertEquals("svg-safe-1", admitted.asset.profileId)
        assertEquals(admitted.asset.digest, admitted.entry.digest)
        assertTrue(admitted.asset.assetId.value.startsWith("asset:sha256:"))
        assertEquals(TraceRole.ASSET_DEFINITION, admitted.trace.origins.single().role)
        assertTrue(admitted.trace.origins.single().primary)
        assertEquals("assets/terminal.svg", admitted.trace.origins.single().relativePath)
        assertEquals("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 16 16\"><title>Terminal</title><rect fill=\"#ffffff\" height=\"14\" stroke=\"#000000\" stroke-width=\"1\" width=\"14\" x=\"1\" y=\"1\"/><circle cx=\"8\" cy=\"8\" fill=\"#000000\" r=\"2\"/></svg>", admitted.entry.utf8())
        val manifest = contract("assets/bundle-manifest.json")
        listOf(admitted.entry.entryId, admitted.entry.digest, admitted.asset.assetId.value, admitted.trace.traceId.value, admitted.trace.origins.single().sourceDigest)
            .forEach { expected -> assertTrue(manifest.contains("\"$expected\""), "Shared asset manifest lacks `$expected`.") }
    }

    @Test
    fun `svg safe one rejects every shared prohibited vector with a human correction`() {
        val vectors = contract("assets/rejected-vectors.json")
        val matches = Regex("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"\\s*,\\s*\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"\\s*,\\s*\\\"source\\\"\\s*:\\s*\\\"((?:[^\\\"\\\\]|\\\\.)*)\\\"")
            .findAll(vectors)
            .toList()
        assertTrue(matches.isNotEmpty())
        matches.forEach { match ->
                val source = match.groupValues[3].replace("\\\"", "\"").replace("\\\\", "\\")
                val result = assertIs<AssetAdmissionResult.Rejected>(
                    PresentationAssetCompiler.admit(
                        AssetAdmissionInput("assets/${match.groupValues[1]}.svg", AssetMediaKind.SVG, source.toByteArray()),
                    ),
                )
                assertTrue(result.diagnostic.subject.isNotBlank())
                assertTrue(result.diagnostic.problem.isNotBlank())
                assertTrue(result.diagnostic.correction.isNotBlank())
                assertEquals(match.groupValues[2], result.diagnostic.code)
            }
    }

    @Test
    fun `svg safe one rejects entities unknown attributes malformed input and external URLs`() {
        listOf(
            "<!ENTITY x 'boom'><svg/>",
            "<svg viewBox=\"0 0 4 4\"><rect x=\"0\" y=\"0\" width=\"4\" height=\"4\" mystery=\"x\"/></svg>",
            "<svg viewBox=\"0 0 4 4\"><rect fill=\"url(https://example.invalid/fill)\" x=\"0\" y=\"0\" width=\"4\" height=\"4\"/></svg>",
            "<svg><rect></svg>",
        ).forEach { source ->
            assertIs<AssetAdmissionResult.Rejected>(
                PresentationAssetCompiler.admit(AssetAdmissionInput("assets/invalid.svg", AssetMediaKind.SVG, source.toByteArray())),
            )
        }
    }

    @Test
    fun `svg safe one admits static package symbols with local reuse labels and integer dimensions`() {
        val source = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="40" height="100">
              <defs><g id="terminal"><line x1="0" y1="0" x2="0" y2="4" stroke="#000000"/></g></defs>
              <g transform="translate(20,10)"><use xlink:href="#terminal"/><rect x="-8" y="10" width="16" height="32" fill="none" stroke="#000000"/><text x="0" y="6" font-family="sans-serif" font-size="8" text-anchor="middle">KM1</text></g>
              Your Browser does not support inline SVG!
            </svg>
        """.trimIndent()

        val admitted = assertIs<AssetAdmissionResult.Admitted>(
            PresentationAssetCompiler.admit(AssetAdmissionInput("assets/contactor-coil.svg", AssetMediaKind.SVG, source.toByteArray())),
        ).asset

        assertEquals(SceneBounds(0, 0, 40, 100), admitted.asset.intrinsicBounds)
        assertTrue(admitted.entry.utf8().contains("xlink:href=\"#terminal\""))
        assertTrue(admitted.entry.utf8().contains(">KM1</text>"))
        assertTrue(!admitted.entry.utf8().contains("Your Browser"))
        assertIs<AssetAdmissionResult.Rejected>(
            PresentationAssetCompiler.admit(
                AssetAdmissionInput(
                    "assets/external-use.svg",
                    AssetMediaKind.SVG,
                    source.replace("#terminal", "https://example.invalid/terminal").toByteArray(),
                ),
            ),
        )
    }

    @Test
    fun `generic svg profile treats anchor-like metadata as non-authoritative`() {
        val source = "<svg viewBox=\"0 0 4 4\"><g id=\"port-line\" data-athena-direction=\"OUT\"><circle cx=\"2\" cy=\"2\" r=\"1\"/></g></svg>"
        val result = assertIs<AssetAdmissionResult.Rejected>(
            PresentationAssetCompiler.admit(AssetAdmissionInput("assets/symbol.svg", AssetMediaKind.SVG, source.toByteArray())),
        )
        assertEquals("asset.svg.unknown-attribute", result.diagnostic.code)
    }

    @Test
    fun `raster and font limits fail closed while valid binary assets preserve media rules`() {
        val validPng = validPng()
        val pngResult = PresentationAssetCompiler.admit(AssetAdmissionInput("assets/icon.png", AssetMediaKind.PNG, validPng))
        val png = assertIs<AssetAdmissionResult.Admitted>(pngResult, (pngResult as? AssetAdmissionResult.Rejected)?.diagnostic.toString()).asset
        assertEquals(SceneBounds(0, 0, 1, 1), png.asset.intrinsicBounds)

        assertIs<AssetAdmissionResult.Rejected>(
            PresentationAssetCompiler.admit(AssetAdmissionInput("assets/huge.png", AssetMediaKind.PNG, png(width = 4097, height = 1))),
        )
        assertIs<AssetAdmissionResult.Admitted>(
            PresentationAssetCompiler.admit(AssetAdmissionInput("fonts/athena.woff2", AssetMediaKind.WOFF2, validWoff2())),
        ).also { admitted -> assertEquals(null, admitted.asset.asset.intrinsicBounds) }
        assertIs<AssetAdmissionResult.Rejected>(
            PresentationAssetCompiler.admit(AssetAdmissionInput("fonts/incomplete.woff2", AssetMediaKind.WOFF2, byteArrayOf('w'.code.toByte(), 'O'.code.toByte(), 'F'.code.toByte(), '2'.code.toByte()))),
        )
    }

    @Test
    fun `asset input paths remain portable local package paths`() {
        listOf("../outside.svg", "assets\\terminal.svg", "/assets/terminal.svg", "https://example.invalid/terminal.svg").forEach { path ->
            assertIs<AssetAdmissionResult.Rejected>(
                PresentationAssetCompiler.admit(AssetAdmissionInput(path, AssetMediaKind.SVG, contract("assets/admitted.svg").toByteArray())),
            )
        }
    }

    @Test
    fun `bundle deduplicates admitted bytes and validates scene linkage revision and integrity`() {
        val first = admitted("assets/a.svg")
        val second = admitted("assets/b.svg")
        val revision = InputRevision("input:sha256:${"1".repeat(64)}")
        val bundle = PresentationAssetCompiler.bundle(revision, listOf(first, second))
        assertEquals(1, bundle.entries.size)

        val style = ResolvedStyle(StyleId("style:sha256:${"2".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val scene = AthenaDiagramSceneContract.canonicalize(
            AthenaDiagramScene(
                sceneId = SceneId("scene:sha256:${"3".repeat(64)}"),
                inputRevision = revision,
                sceneDigest = SceneDigest.uncomputed(),
                page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
                snapGrid = SceneSnapGrid("sheet", 1, ScenePoint(2, 2)),
                styles = listOf(style),
                assets = listOf(first.asset),
                occurrences = emptyList(),
                connections = emptyList(),
                decorations = emptyList(),
                traces = listOf(first.trace),
            ),
        )
        AthenaDiagramSceneContract.validate(scene, bundle)
        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(scene, bundle.copy(inputRevision = InputRevision("input:sha256:${"4".repeat(64)}")))
        }
        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(scene, bundle.copy(entries = listOf(bundle.entries.single().copy(bytes = "tampered".toByteArray()))))
        }
    }

    @Test
    fun `bundle coverage rejects missing surplus and wrong entries`() {
        val first = admitted("assets/a.svg")
        val second = assertIs<AssetAdmissionResult.Admitted>(
            PresentationAssetCompiler.admit(
                AssetAdmissionInput(
                    "assets/b.svg",
                    AssetMediaKind.SVG,
                    "<svg viewBox=\"0 0 16 16\"><circle cx=\"8\" cy=\"8\" r=\"3\"/></svg>".toByteArray(),
                ),
            ),
        ).asset
        val revision = InputRevision("input:sha256:${"9".repeat(64)}")
        val style = ResolvedStyle(StyleId("style:sha256:${"a".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val scene = AthenaDiagramSceneContract.canonicalize(
            AthenaDiagramScene(
                sceneId = SceneId("scene:sha256:${"b".repeat(64)}"),
                inputRevision = revision,
                sceneDigest = SceneDigest.uncomputed(),
                page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
                snapGrid = SceneSnapGrid("sheet", 1, ScenePoint(2, 2)),
                styles = listOf(style),
                assets = listOf(first.asset),
                occurrences = emptyList(),
                connections = emptyList(),
                decorations = emptyList(),
                traces = listOf(first.trace),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(scene, AssetBundle(revision, emptyList()))
        }
        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(scene, AssetBundle(revision, listOf(first.entry, second.entry)))
        }
        assertFailsWith<IllegalArgumentException> {
            AthenaDiagramSceneContract.validate(scene, AssetBundle(revision, listOf(second.entry)))
        }
    }

    @Test
    fun `ready publication rejects diagnostics and unavailable publication carries no partial scene`() {
        val revision = InputRevision("input:sha256:${"c".repeat(64)}")
        val diagnostic = SceneDiagnostic("asset.svg", "Asset rejected.", "Use a governed local asset.", "asset.svg.rejected")
        val admitted = admitted("assets/ready.svg")
        val style = ResolvedStyle(StyleId("style:sha256:${"d".repeat(64)}"), "#000000ff", "#ffffffff", 1)
        val trace = admitted.trace
        val scene = AthenaDiagramSceneContract.canonicalize(
            AthenaDiagramScene(
                sceneId = SceneId("scene:sha256:${"e".repeat(64)}"),
                inputRevision = revision,
                sceneDigest = SceneDigest.uncomputed(),
                page = ScenePage(SceneBounds(0, 0, 20, 20), SceneBounds(2, 2, 16, 16)),
                snapGrid = SceneSnapGrid("sheet", 1, ScenePoint(2, 2)),
                styles = listOf(style),
                assets = listOf(admitted.asset),
                occurrences = emptyList(),
                connections = emptyList(),
                decorations = emptyList(),
                traces = listOf(trace),
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            AthenaScenePublication(
                state = PublicationState.READY,
                attemptedInputRevision = revision,
                acceptedInputRevision = revision,
                scene = scene,
                assetBundle = AssetBundle(revision, listOf(admitted.entry)),
                diagnostics = listOf(diagnostic),
            )
        }
        val unavailable = AthenaScenePublication.unavailable(revision, diagnostic)
        assertEquals(PublicationState.UNAVAILABLE, unavailable.state)
        assertEquals(null, unavailable.scene)
        assertEquals(null, unavailable.assetBundle)
    }

    private fun admitted(path: String): AdmittedSceneAsset = assertIs<AssetAdmissionResult.Admitted>(
        PresentationAssetCompiler.admit(AssetAdmissionInput(path, AssetMediaKind.SVG, contract("assets/admitted.svg").toByteArray())),
    ).asset

    private fun contract(relative: String): String {
        val root = generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .first { Files.exists(it.resolve("settings.gradle.kts")) }
        return Files.readString(root.resolve("contracts/presentation/v1").resolve(relative))
    }

    private fun AssetBundleEntry.utf8() = bytes.decodeToString()

    private fun png(width: Int, height: Int): ByteArray = byteArrayOf(
        0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        0, 0, 0, 13, 'I'.code.toByte(), 'H'.code.toByte(), 'D'.code.toByte(), 'R'.code.toByte(),
        ((width ushr 24) and 0xff).toByte(), ((width ushr 16) and 0xff).toByte(), ((width ushr 8) and 0xff).toByte(), (width and 0xff).toByte(),
        ((height ushr 24) and 0xff).toByte(), ((height ushr 16) and 0xff).toByte(), ((height ushr 8) and 0xff).toByte(), (height and 0xff).toByte(),
    )

    private fun validPng(): ByteArray = ByteArrayOutputStream().use { output ->
        check(ImageIO.write(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", output))
        output.toByteArray()
    }

    private fun validWoff2(): ByteArray = byteArrayOf(
        'w'.code.toByte(), 'O'.code.toByte(), 'F'.code.toByte(), '2'.code.toByte(),
        0, 1, 0, 0, 0, 0, 0, 48, 0, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
    )
}
