package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse

class AthenaProjectionProtocolArchitectureTest {
    @Test
    fun `production projection protocol has no milestone repository branches`() {
        val source = Files.readString(
            repositoryRoot().resolve(
                "ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt",
            ),
        )

        assertFalse(Regex("professionalM\\d+|isProfessionalM\\d+|examples/m\\d+|m\\d+CabinetView").containsMatchIn(source))
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
