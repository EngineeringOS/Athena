package com.engineeringood.athena.cli

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ParseCliTest {
    @Test
    fun `parses the demo cabinet example through the cli entry path`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val output = BootstrapCli().run(listOf("parse", examplePath.toString()))

        assertContains(output, "Parse successful")
        assertContains(output, "DemoCabinet")
        assertContains(output, "device declarations: 2")
        assertContains(output, "port declarations: 2")
        assertContains(output, "connection declarations: 1")
    }

    @Test
    fun `stops the pipeline after syntax diagnostics`() {
        val brokenSource = """
            system Broken {
              connect P1.out P2.in
            }
        """.trimIndent()
        val brokenPath = Files.createTempFile("athena-broken-", ".athena")
        Files.writeString(brokenPath, brokenSource)

        try {
            val output = BootstrapCli().run(listOf("parse", brokenPath.toString()))

            assertContains(output, "Syntax diagnostics")
            assertContains(output, "Pipeline stopped before semantic validation and rendering")
            assertContains(output, brokenPath.fileName.toString())
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
