package com.engineeringood.athena.compiler

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertTrue

class KernelDomainNeutralityTest {

    @Test
    fun `kernel production sources contain no domain construct implementations`() {
        val repoRoot = resolveRepoRoot()
        val forbidden = listOf(
            "RailProjection",
            "RungProjection",
            "BranchProjection",
            "WireBundleProjection",
            "TerminalStripProjection",
            "ContactGroupProjection",
            "CoilGroupProjection",
        )
        val kernelMain = repoRoot.resolve("kernel")
        val offenders = mutableListOf<String>()
        Files.walk(kernelMain).use { stream ->
            stream
                .filter { path -> path.toString().contains(File.separator + "src" + File.separator + "main") }
                .filter { path -> path.toString().endsWith(".kt") }
                .forEach { path ->
                    val text = path.readText()
                    forbidden.forEach { name ->
                        if (text.contains(name)) {
                            offenders += "${path}:$name"
                        }
                    }
                }
        }
        assertTrue(
            offenders.isEmpty(),
            "Kernel production sources must stay domain-neutral; found: ${offenders.joinToString("\n")}",
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
}
