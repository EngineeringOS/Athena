package com.engineeringood.athena.ide.lsp

import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Verifies the governed repository-open seed rules for the IDE path.
 */
class AthenaRepositoryResolverTest {
    @Test
    fun `resolve derives repository meaning from contract and picks one deterministic editor seed`() {
        val repositoryRoot = kotlin.io.path.createTempDirectory("athena-repository-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factory-line
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: 1
                    primaryPackage:
                      name: com.engineeringood.factory-line
                      version: 0.1.0
                    packages:
                      - name: com.engineeringood.factory-line
                        version: 0.1.0
                        sourceRoot: src
                        dependencies: []
                """.trimIndent(),
            )
            val sourceRoot = repositoryRoot.resolve("src").createDirectories()
            val firstSourcePath = sourceRoot.resolve("a-control.athena")
            val secondSourcePath = sourceRoot.resolve("factory-line.athena")
            firstSourcePath.writeText("system Control { }")
            secondSourcePath.writeText("system FactoryLine { }")

            val resolution = AthenaRepositoryResolver().resolve(repositoryRoot)

            val success = assertIs<AthenaRepositoryResolutionSuccess>(resolution)
            assertEquals(repositoryRoot.toAbsolutePath().normalize(), success.descriptor.repositoryRoot)
            assertEquals(repositoryRoot.resolve("athena.yaml").toAbsolutePath().normalize(), success.descriptor.manifestPath)
            assertEquals(repositoryRoot.resolve("athena.lock").toAbsolutePath().normalize(), success.descriptor.lockPath)
            assertEquals(sourceRoot.toAbsolutePath().normalize(), success.descriptor.sourceRootPath)
            assertEquals(firstSourcePath.toAbsolutePath().normalize(), success.descriptor.sourcePath)
            assertEquals("factory-line", success.descriptor.projectName)
            assertEquals("com.engineeringood.factory-line", success.descriptor.primaryPackageName)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolve reports compiler-owned contract diagnostics for invalid repositories`() {
        val repositoryRoot = kotlin.io.path.createTempDirectory("athena-repository-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factory-line
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )

            val resolution = AthenaRepositoryResolver().resolve(repositoryRoot)

            val failure = assertIs<AthenaRepositoryResolutionFailure>(resolution)
            assertTrue(failure.reason.contains("repository.contract.lock.missing"))
            assertTrue(failure.reason.contains("athena.lock"))
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
