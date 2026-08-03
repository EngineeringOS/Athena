package com.engineeringood.athena.compiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActiveSourceSyntaxHygieneTest {
    @Test
    fun `maintained professional drawing sources do not use removed connection intent syntax`() {
        val repoRoot = resolveRepoRoot()
        val sources = listOf(
            "examples/m39/reality-product-proof/src/com/engineeringood/m39/realityproductproof/01-reality-product-proof.athena",
        ).map(repoRoot::resolve)

        sources.forEach { source ->
            assertTrue(Files.exists(source), "Missing maintained source: $source")
            val text = source.readText()
            assertFalse(text.contains("intent {"), "Removed connection intent block remains in $source")
            assertFalse(text.contains("intent default {"), "Removed default intent block remains in $source")
        }
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
