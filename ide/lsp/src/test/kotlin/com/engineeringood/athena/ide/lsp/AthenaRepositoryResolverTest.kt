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
                      name: com.engineeringood.factoryline
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: 1
                    primaryPackage:
                      name: com.engineeringood.factoryline
                      version: 0.1.0
                    packages:
                      - name: com.engineeringood.factoryline
                        version: 0.1.0
                        sourceRoot: src
                        dependencies: []
                """.trimIndent(),
            )
            val sourceRoot = repositoryRoot.resolve("src").createDirectories()
            val packageRoot = sourceRoot.resolve("com/engineeringood/factoryline").createDirectories()
            val firstSourcePath = packageRoot.resolve("a-control.athena")
            val secondSourcePath = packageRoot.resolve("factoryline.athena")
            firstSourcePath.writeText(governedAthenaSource("system Control { }"))
            secondSourcePath.writeText(governedAthenaSource("system FactoryLine { }"))

            val resolution = AthenaRepositoryResolver().resolve(repositoryRoot)

            val success = assertIs<AthenaRepositoryResolutionSuccess>(resolution)
            assertEquals(repositoryRoot.toAbsolutePath().normalize(), success.descriptor.repositoryRoot)
            assertEquals(repositoryRoot.resolve("athena.yaml").toAbsolutePath().normalize(), success.descriptor.manifestPath)
            assertEquals(repositoryRoot.resolve("athena.lock").toAbsolutePath().normalize(), success.descriptor.lockPath)
            assertEquals(sourceRoot.toAbsolutePath().normalize(), success.descriptor.sourceRootPath)
            assertEquals(firstSourcePath.toAbsolutePath().normalize(), success.descriptor.sourcePath)
            assertEquals("factoryline", success.descriptor.projectName)
            assertEquals("com.engineeringood.factoryline", success.descriptor.primaryPackageName)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolve allows a missing lock for first-open authoring repositories`() {
        val repositoryRoot = kotlin.io.path.createTempDirectory("athena-repository-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factoryline
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            val packageRoot = repositoryRoot.resolve("src/com/engineeringood/factoryline").createDirectories()
            packageRoot.resolve("factoryline.athena").writeText(governedAthenaSource("system FactoryLine { }"))

            val resolution = AthenaRepositoryResolver().resolve(repositoryRoot)

            val success = assertIs<AthenaRepositoryResolutionSuccess>(resolution)
            assertEquals(repositoryRoot.resolve("athena.lock").toAbsolutePath().normalize(), success.descriptor.lockPath)
            assertEquals("com.engineeringood.factoryline", success.descriptor.primaryPackageName)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolve never uses Folio or Page Companion as engineering source seed`() {
        val repositoryRoot = kotlin.io.path.createTempDirectory("athena-repository-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factoryline
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            val packageRoot = repositoryRoot.resolve("src/com/engineeringood/factoryline").createDirectories()
            val sourcePath = packageRoot.resolve("z-control.athena")
            packageRoot.resolve("a-control.folio.athena").writeText(
                "folio control { page power }",
            )
            packageRoot.resolve("a-control.sheet.athena").writeText(
                """
                    sheet "control" {
                      page format A3 landscape
                      frame: 17 * 16
                      snap: 1
                    }
                """.trimIndent(),
            )
            sourcePath.writeText(governedAthenaSource("system Control { }"))

            val resolution = AthenaRepositoryResolver().resolve(repositoryRoot)

            val success = assertIs<AthenaRepositoryResolutionSuccess>(resolution)
            assertEquals(sourcePath.toAbsolutePath().normalize(), success.descriptor.sourcePath)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
