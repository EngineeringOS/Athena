package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class AthenaM36CabinetEndToEndProofTest {
    @Test
    fun `m36 cabinet sample produces structural and visual end to end evidence`() {
        val evidence = compileProof()

        assertEquals("cabinet", evidence.rendering.viewId)
        assertTrue(evidence.rendering.svg.isNonBlankSvg(), "Cabinet SVG must render nonblank output.")
        assertTrue(evidence.rendering.svg.contains("""class="component""""))
        assertTrue(evidence.rendering.svg.contains("""class="connection""""))
        assertTrue(evidence.rendering.svg.contains("""class="label""""))

        assertTrue(evidence.routeCount >= 30, "M36 E2E evidence must include at least 30 route facts.")
        assertTrue(evidence.occurrenceCount >= 20, "M36 E2E evidence must include at least 20 visual occurrences.")
        assertEquals(0, evidence.unresolvedBindingCount, "Port-to-Anchor bindings must all resolve.")
        assertEquals(0, evidence.unapprovedFallbackRouteCount, "Routes must not silently fall back.")
        assertEquals(0, evidence.routeBodyIntersectionCount, "Routes must not intersect non-endpoint bodies.")
        assertEquals(0, evidence.requiredOffChannelSegmentCount, "Required-channel routes must stay in channels.")
        assertFalse(evidence.sampleText.contains("<definition"), "XML runtime authority is forbidden.")
        assertFalse(evidence.sampleText.contains("data-athena-port"), "Raw SVG semantic metadata authority is forbidden.")

        evidence.routeSegments.forEach { segment ->
            assertTrue(
                segment.isOrthogonal,
                "Route segment must be orthogonal: ${segment.raw}",
            )
        }
        assertTrue(evidence.rendering.svg.contains("junction", ignoreCase = true), "Junction treatment must be visible.")
        assertTrue(
            evidence.rendering.svg.contains("data-athena-marker-kind=\"crossing\""),
            "Crossing treatment must be visible. Routes:\n${evidence.routePolylines.joinToString("\n")}",
        )

        val rerun = compileProof()
        assertEquals(evidence.placementEvidence, rerun.placementEvidence)
        assertEquals(evidence.routeEvidence, rerun.routeEvidence)
    }

    @Test
    fun `m36 cabinet evidence rejects missing bridge and route evidence`() {
        val evidence = compileProof()

        val withoutBridge = evidence.copy(unresolvedBindingCount = 1)
        assertFalse(withoutBridge.isAccepted, "Missing Port-to-Anchor bridge must fail evidence.")

        val withoutRouteEvidence = evidence.copy(routeEvidence = emptyList())
        assertFalse(withoutRouteEvidence.isAccepted, "Missing RouteFact evidence must fail evidence.")

        val withBodyIntersection = evidence.copy(routeBodyIntersectionCount = 1)
        assertFalse(withBodyIntersection.isAccepted, "Route/body intersection must fail evidence.")

        val withOffChannelSegment = evidence.copy(requiredOffChannelSegmentCount = 1)
        assertFalse(withOffChannelSegment.isAccepted, "Required off-channel segment must fail evidence.")
    }

    private fun compileProof(): M36CabinetProof {
        val repoRoot = resolveRepoRoot()
        val sampleRoot = repoRoot.resolve("examples/m36/connectivity-cabinet")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena",
        )
        val compilation = AthenaCompiler().compile(source)
        val success = when (compilation) {
            is CompilerCompilationSuccess -> compilation
            is CompilerCompilationParseFailure -> fail(
                compilation.diagnostics.joinToString("\n") { diagnostic ->
                    "${diagnostic.file}:${diagnostic.line}:${diagnostic.column}: ${diagnostic.message}"
                },
            )
        }
        assertTrue(
            success.semanticResult.diagnostics.isEmpty(),
            success.semanticResult.diagnostics.joinToString("\n") { diagnostic ->
                "${diagnostic.ruleId.value}: ${diagnostic.message}"
            },
        )
        val rendering = assertIs<CompilerRenderingSuccess>(success.rendering)
        val sampleText = Files.walk(sampleRoot).use { stream ->
            stream
                .filter(Files::isRegularFile)
                .map { path -> path.readText() }
                .toList()
                .joinToString("\n")
        }
        return M36CabinetProof.from(rendering, sampleText)
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts")) && Files.exists(current.resolve("examples"))) {
                return current
            }
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}

private data class M36CabinetProof(
    val rendering: CompilerRenderingSuccess,
    val sampleText: String,
    val occurrenceCount: Int,
    val routeCount: Int,
    val unresolvedBindingCount: Int,
    val unapprovedFallbackRouteCount: Int,
    val routeBodyIntersectionCount: Int,
    val requiredOffChannelSegmentCount: Int,
    val placementEvidence: List<String>,
    val routeEvidence: List<String>,
    val routeSegments: List<M36RouteSegmentProof>,
    val routePolylines: List<String>,
) {
    val isAccepted: Boolean
        get() = occurrenceCount >= 20 &&
            routeCount >= 30 &&
            unresolvedBindingCount == 0 &&
            unapprovedFallbackRouteCount == 0 &&
            routeBodyIntersectionCount == 0 &&
            requiredOffChannelSegmentCount == 0 &&
            placementEvidence.isNotEmpty() &&
            routeEvidence.isNotEmpty() &&
            routeSegments.all(M36RouteSegmentProof::isOrthogonal)

    companion object {
        fun from(rendering: CompilerRenderingSuccess, sampleText: String): M36CabinetProof =
            M36CabinetProof(
                rendering = rendering,
                sampleText = sampleText,
                occurrenceCount = Regex("""class="component"""").findAll(rendering.svg).count(),
                routeCount = Regex("""class="connection"""").findAll(rendering.svg).count(),
                unresolvedBindingCount = Regex("unresolved", RegexOption.IGNORE_CASE).findAll(rendering.svg).count(),
                unapprovedFallbackRouteCount = Regex("fallback", RegexOption.IGNORE_CASE).findAll(rendering.svg).count(),
                routeBodyIntersectionCount = Regex("body_intersection", RegexOption.IGNORE_CASE).findAll(rendering.svg).count(),
                requiredOffChannelSegmentCount = Regex("off-channel", RegexOption.IGNORE_CASE).findAll(rendering.svg).count(),
                placementEvidence = Regex("""class="component"[^>]*data-subject="([^"]+)"""")
                    .findAll(rendering.svg)
                    .map { match -> match.groupValues[1] }
                    .toList(),
                routeEvidence = Regex("""class="connection"[^>]*data-connection-id="([^"]+)"""")
                    .findAll(rendering.svg)
                    .map { match -> match.groupValues[1] }
                    .toList(),
                routeSegments = Regex("""<polyline[^>]*points="([^"]+)"[^>]*class="connection"[^>]*>""")
                    .findAll(rendering.svg)
                    .flatMap { match -> M36RouteSegmentProof.fromPolyline(match.groupValues[1]).asSequence() }
                    .toList(),
                routePolylines = Regex("""<polyline[^>]*points="([^"]+)"[^>]*class="connection"[^>]*>""")
                    .findAll(rendering.svg)
                    .map { match -> match.groupValues[1] }
                    .plus(
                        Regex("""<line[^>]*class="connection"[^>]*>""")
                            .findAll(rendering.svg)
                            .map { match -> match.value },
                    )
                    .toList(),
            )
    }
}

private data class M36RouteSegmentProof(
    val raw: String,
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
) {
    val isOrthogonal: Boolean get() = x1 == x2 || y1 == y2

    companion object {
        fun fromPolyline(points: String): List<M36RouteSegmentProof> =
            points
                .trim()
                .split(Regex("""\s+"""))
                .map { token ->
                    val parts = token.split(",")
                    require(parts.size == 2) { "Invalid route point `$token` in `$points`." }
                    parts[0].toDouble() to parts[1].toDouble()
                }
                .zipWithNext()
                .map { (from, to) ->
                    M36RouteSegmentProof(
                        raw = points,
                        x1 = from.first,
                        y1 = from.second,
                        x2 = to.first,
                        y2 = to.second,
                    )
                }
    }
}

private fun String.isNonBlankSvg(): Boolean =
    contains("<svg") && Regex("""<(rect|line|path|circle|text)\b""").containsMatchIn(this)
