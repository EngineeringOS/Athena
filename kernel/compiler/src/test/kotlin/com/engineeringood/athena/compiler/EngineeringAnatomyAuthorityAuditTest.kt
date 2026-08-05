package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.streams.asSequence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineeringAnatomyAuthorityAuditTest {
    @Test
    fun `active production source contains only current Entity anatomy authority`() {
        val repoRoot = resolveRepoRoot()
        val violations = Files.walk(repoRoot).use { paths ->
            paths.asSequence()
                .filter(Files::isRegularFile)
                .filter { path -> "/src/main/" in path.normalized() }
                .filterNot { path -> "/build/" in path.normalized() || "/reference/" in path.normalized() }
                .filter { path -> path.extension in activeSourceExtensions }
                .flatMap { path ->
                    authorityViolations(path.readText()).map { violation ->
                        "${repoRoot.relativize(path)}: $violation"
                    }
                }
                .sorted()
                .toList()
        }

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }

    @Test
    fun `authority audit detects retired types identities and compatibility shells`() {
        val forbidden =
            """
            data class EngineeringComponent(val id: String)
            typealias EngineeringEntity = EngineeringComponent
            data class DeviceDeclaration(val name: String)
            val identity = "component:M1"
            class EngineeringComponentCompatibilityAdapter
            """.trimIndent()

        assertEquals(
            listOf(
                "Engineering anatomy compatibility adapter or fallback",
                "Engineering anatomy compatibility alias",
                "retired DeviceDeclaration project authority",
                "retired EngineeringComponent authority",
                "retired component semantic identity prefix",
            ),
            authorityViolations(forbidden).sorted(),
        )
    }

    private fun authorityViolations(source: String): List<String> = forbiddenAuthorityPatterns.mapNotNull { pattern ->
        pattern.message.takeIf { pattern.regex.containsMatchIn(source) }
    }

    private fun Path.normalized(): String = toString().replace('\\', '/')

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts")) && Files.isDirectory(current.resolve("kernel"))) {
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
        val activeSourceExtensions = setOf("kt", "java", "ts", "tsx", "g4")
        val forbiddenAuthorityPatterns = listOf(
            ForbiddenAuthorityPattern(Regex("""\bEngineeringComponent\b"""), "retired EngineeringComponent authority"),
            ForbiddenAuthorityPattern(Regex("""\bDeviceDeclaration\b"""), "retired DeviceDeclaration project authority"),
            ForbiddenAuthorityPattern(Regex("""[\"']component:"""), "retired component semantic identity prefix"),
            ForbiddenAuthorityPattern(
                Regex("""\btypealias\s+(?:EngineeringEntity|EngineeringComponent|DeviceDeclaration)\b"""),
                "Engineering anatomy compatibility alias",
            ),
            ForbiddenAuthorityPattern(
                Regex(
                    """\b(?:class|object|interface)\s+\w*(?:EngineeringComponent|DeviceDeclaration)\w*""" +
                        """(?:Compatibility|Adapter|Fallback)\w*\b|""" +
                        """\b(?:class|object|interface)\s+\w*(?:Compatibility|Adapter|Fallback)\w*""" +
                        """(?:EngineeringComponent|DeviceDeclaration)\w*\b""",
                ),
                "Engineering anatomy compatibility adapter or fallback",
            ),
        )
    }
}
