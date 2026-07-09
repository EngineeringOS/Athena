package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.repository.PackageDependencySource
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaRepositoryResolutionInputBuilderTest {
    @Test
    fun `builds deterministic resolution input from manifest dependency declarations`() {
        val repositoryRoot = createTempDirectory("athena-resolution-input-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      version: 1.0.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.zeta
                        source: local-package
                      - name: com.engineeringood.alpha
                        version: 2.0.0
                        source: local-path
                        locator: vendor\alpha
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")

            val first = AthenaCompiler().buildRepositoryResolutionInput(repositoryRoot)
            val second = AthenaCompiler().buildRepositoryResolutionInput(repositoryRoot)

            assertEquals(first, second)
            assertTrue(first.isValid)
            assertEquals("com.engineeringood.demo", first.resolutionInput?.rootPackage?.name)
            assertEquals("src", first.resolutionInput?.rootSourcePath)
            assertEquals(
                listOf("com.engineeringood.alpha", "com.engineeringood.zeta"),
                first.resolutionInput?.dependencies?.map { dependency -> dependency.packageId.name },
            )
            assertEquals("vendor/alpha", first.resolutionInput?.dependencies?.first()?.locator)
            assertEquals(
                PackageDependencySource.LOCAL_PACKAGE,
                first.resolutionInput?.dependencies?.last()?.source,
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports malformed and unsupported dependency declarations through compiler owned diagnostics`() {
        val repositoryRoot = createTempDirectory("athena-resolution-input-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    dependencies:
                      - name: Invalid Package
                        source: remote-registry
                      - name: com.engineeringood.blank-locator
                        source: local-path
                        locator:
                      - source: local-package
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()

            val result = AthenaCompiler().buildRepositoryResolutionInput(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.manifest.dependencies.name.invalid",
                    "repository.contract.manifest.dependencies.source.unsupported",
                    "repository.contract.manifest.dependencies.locator.missing",
                    "repository.contract.manifest.dependencies.name.missing",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
