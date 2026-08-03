package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.streams.asSequence
import kotlin.test.Test
import kotlin.test.assertTrue

class M38DrawingAuthorityAuditTest {
    @Test
    fun `production source has no competing visible drawing authority`() {
        val repoRoot = resolveRepoRoot()
        val productionFiles = Files.walk(repoRoot).use { stream ->
            stream.asSequence()
                .filter(Files::isRegularFile)
                .filter { path -> path.toString().replace('\\', '/').contains("/src/main/") }
                .filter { path -> path.fileName.toString().endsWith(".kt") || path.fileName.toString().endsWith(".ts") }
                .filterNot { path -> path.toString().replace('\\', '/').contains("/build/") }
                .toList()
        }

        val violations = productionFiles.flatMap { path ->
            val text = path.readText()
            forbiddenAuthorityPatterns.mapNotNull { pattern ->
                if (pattern.regex.containsMatchIn(text)) {
                    "${repoRoot.relativize(path)}: ${pattern.message}"
                } else {
                    null
                }
            }
        }.sorted()

        assertTrue(
            violations.isEmpty(),
            violations.joinToString("\n"),
        )
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts")) && Files.exists(current.resolve("kernel"))) {
                return current
            }
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }

    private data class ForbiddenAuthorityPattern(
        val regex: Regex,
        val message: String,
    )

    private companion object {
        private val forbiddenAuthorityPatterns = listOf(
            ForbiddenAuthorityPattern(
                Regex("""\b(?:class|object|interface|enum class|data class)\s+\w*(?:Proof|Demo|Sample)\w*\b"""),
                "production type name must not contain Proof, Demo, or Sample.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""\b(?:class|object|interface|enum class|data class)\s+\w*M\d{2}\w*\b"""),
                "production type name must not contain milestone names.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""\b(?:class|object|interface|enum class|data class)\s+\w*V[01]\w*\b"""),
                "production type name must not use V0/V1 suffix naming.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""\bPresentationRouteAttachmentFact\b|\battachRoutesToPresentationTerminals\b|\busesCenterFallback\b"""),
                "route-to-terminal presentation attachment helper competes with strict PresentationConnector authority.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""AthenaGovernedGraphicMutationTarget\.[A-Za-z_]*RouteFact\b|\bRouteFact,\s*(?:\r?\n\s*)?\}"""),
                "RouteFact must not be a direct governed graphic mutation target.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""\brouteFactSnapshot\b"""),
                "public presentation payload must not expose routeFactSnapshot.",
            ),
            ForbiddenAuthorityPattern(
                Regex("""body[- ]center fallback|fallback endpoint|renderer repair|hardcoded sample policy""", RegexOption.IGNORE_CASE),
                "stale fallback or renderer repair authority text remains in production source.",
            ),
        )
    }
}
