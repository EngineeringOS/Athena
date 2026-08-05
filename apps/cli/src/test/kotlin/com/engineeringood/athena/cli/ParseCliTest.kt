package com.engineeringood.athena.cli

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains

class ParseCliTest {
    @Test
    fun `parses M42 Entity anatomy`() {
        val path = Files.createTempFile("athena-cli-anatomy-", ".athena")
        Files.writeString(
            path,
            """
            system Anatomy {
              entity Drive {
                concept Drive
                function main {
                  role main
                  port powerIn { direction in flow Power }
                }
              }
            }
            """.trimIndent(),
        )

        try {
            val output = BootstrapCli().run(listOf("parse", path.toString()))
            assertContains(output, "Parse successful")
            assertContains(output, "System: Anatomy")
            assertContains(output, "Entities: 1")
            assertContains(output, "Ports: 1")
        } finally {
            Files.deleteIfExists(path)
        }
    }

    @Test
    fun `reports syntax diagnostics without invoking retired rendering`() {
        val path = Files.createTempFile("athena-cli-invalid-", ".athena")
        Files.writeString(path, "system Broken { device M1 {} }")

        try {
            val output = BootstrapCli().run(listOf("parse", path.toString()))
            assertContains(output, "Syntax diagnostics")
            assertContains(output, "Pipeline stopped before engineering lowering.")
        } finally {
            Files.deleteIfExists(path)
        }
    }
}
