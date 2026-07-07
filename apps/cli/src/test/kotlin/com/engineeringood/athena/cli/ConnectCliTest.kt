package com.engineeringood.athena.cli

import com.engineeringood.athena.runtime.AthenaRuntime
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConnectCliTest {
    @Test
    fun `connect command routes through the runtime command boundary and refreshes projections`() {
        val sourcePath = writeProject(
            """
                system Connectable {
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
            val runtime = AthenaRuntime()
            val output = BootstrapCli(runtime = runtime).run(
                listOf("connect", sourcePath.toString(), "PLC1.out", "M1.in"),
            )

            assertContains(output, "Command successful")
            assertContains(output, "Command: CONNECT_PORTS")
            assertContains(output, "Changed semantic ids: connection:PLC1.out->M1.in, port:M1.in, port:PLC1.out")
            assertContains(output, "connections before: 0")
            assertContains(output, "connections after: 1")
            assertContains(output, "viewer connections: 1")
            assertEquals(sourcePath, assertNotNull(runtime.activeExecutionContext).project.sourcePath)
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `connect command surfaces explicit rejection when a requested port does not exist`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val output = BootstrapCli().run(
                listOf("connect", sourcePath.toString(), "PLC1.out", "Missing.in"),
            )

            assertContains(output, "Command rejected")
            assertContains(output, "Command: CONNECT_PORTS")
            assertContains(output, "Target port `Missing.in` does not exist.")
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-cli-connect-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
