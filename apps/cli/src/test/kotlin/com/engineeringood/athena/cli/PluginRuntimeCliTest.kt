package com.engineeringood.athena.cli

import com.engineeringood.athena.runtime.AthenaRuntime
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class PluginRuntimeCliTest {
    @Test
    fun `cli exposes hosted runtime plugins and can execute a contributed plugin command`() {
        val sourcePath = writeProject(
            """
                system PluginCliDemo {
                  device PLC1 {
                    type Switch
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val cli = BootstrapCli(runtime = AthenaRuntime())

            val plugins = cli.run(listOf("plugins"))
            val execution = cli.run(
                listOf(
                    "plugin-command",
                    sourcePath.toString(),
                    "electrical-runtime.connect-first-compatible",
                ),
            )
            val history = BootstrapCli().run(listOf("history", sourcePath.toString()))

            assertContains(plugins, "Hosted plugins")
            assertContains(plugins, "Hosted plugins: 2")
            assertContains(plugins, "com.engineeringood.athena.domain.dummy-runtime")
            assertContains(plugins, "com.engineeringood.athena.domain.electrical-runtime")
            assertContains(plugins, "electrical-runtime.connect-first-compatible")
            assertTrue(
                plugins.indexOf("com.engineeringood.athena.domain.dummy-runtime") <
                    plugins.indexOf("com.engineeringood.athena.domain.electrical-runtime"),
            )

            assertContains(execution, "Plugin command successful")
            assertContains(execution, "Contribution: electrical-runtime.connect-first-compatible")
            assertContains(execution, "Plugin: com.engineeringood.athena.domain.electrical-runtime")
            assertContains(execution, "Command: CONNECT_PORTS")
            assertContains(execution, "connections after: 1")

            assertContains(history, "History entries: 1")
            assertContains(history, "command-0001 CONNECT_PORTS APPLIED")
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-cli-plugin-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
