package com.engineeringood.athena.compiler

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationAssetPackageCompilerTest {
    @Test
    fun `compiles locked package resources through governed asset admission`() {
        val repositoryRoot = fixtureRepository()
        try {
            val lock = AthenaCompiler().materializeRepositoryLock(repositoryRoot).lock
            val result = PresentationAssetPackageCompiler().compile(repositoryRoot, lock!!.packageSnapshots)

            assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString { it.problem })
            assertEquals(1, result.admitted.size)
            assertEquals("SVG", result.admitted.single().asset.mediaKind.name)
            assertEquals("ASSET_DEFINITION", result.admitted.single().trace.origins.single().role.name)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects resource bytes changed after lock admission`() {
        val repositoryRoot = fixtureRepository()
        try {
            val lock = AthenaCompiler().materializeRepositoryLock(repositoryRoot).lock
            repositoryRoot.resolve("src/com/engineeringood/assets/resources/terminal.svg")
                .writeText("<svg viewBox=\"0 0 8 8\"/>")

            val result = PresentationAssetPackageCompiler().compile(repositoryRoot, lock!!.packageSnapshots)

            assertTrue(result.admitted.isEmpty())
            assertEquals("asset.resource.digest-mismatch", result.diagnostics.single().code)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun fixtureRepository(): Path {
        val root = createTempDirectory("athena-presentation-assets-")
        root.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.assets
                  version: 1.0.0
                  sourceRoot: src
            """.trimIndent(),
        )
        val packageRoot = root.resolve("src/com/engineeringood/assets").apply { createDirectories() }
        packageRoot.resolve("assets.athena").writeText(
            """
                package com.engineeringood.assets

                system Assets { }
            """.trimIndent(),
        )
        packageRoot.resolve("resources/terminal.svg").apply {
            parent.createDirectories()
            writeText("<svg viewBox=\"0 0 4 4\"><rect width=\"4\" height=\"4\"/></svg>")
        }
        return root
    }
}
