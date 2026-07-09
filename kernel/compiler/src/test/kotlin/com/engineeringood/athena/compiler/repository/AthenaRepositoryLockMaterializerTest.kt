package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaRepositoryLockMaterializerTest {
    @Test
    fun `materializes deterministic athena lock content from canonical resolver output`() {
        val repositoryRoot = createTempDirectory("athena-lock-materialization-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: vendor/alpha
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                sourceFileName = "alpha.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )

            val first = AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val firstBytes = repositoryRoot.resolve("athena.lock").readText()
            val second = AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            val secondBytes = repositoryRoot.resolve("athena.lock").readText()

            assertTrue(first.isValid, first.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" })
            assertEquals(first.lock, second.lock)
            assertEquals(first.renderedLock, second.renderedLock)
            assertEquals(firstBytes, secondBytes)
            assertEquals(first.renderedLock, firstBytes)
            assertTrue(firstBytes.contains("primaryPackage:"))
            assertTrue(firstBytes.contains("sourceRoot: vendor/alpha/src"))
            assertTrue(firstBytes.contains("dependencies: []"))
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports stale athena lock content explicitly`() {
        val repositoryRoot = createTempDirectory("athena-lock-validation-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: vendor/alpha
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                sourceFileName = "alpha.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: 1
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                    packages:
                      - name: com.engineeringood.root
                        version: 1.0.0
                        sourceRoot: src
                        dependencies: []
                """.trimIndent(),
            )

            val result = AthenaCompiler().validateRepositoryLock(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.lock.content.out-of-date"
                },
                result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
            assertNotNull(result.expectedLock)
            assertNotNull(result.actualLock)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports malformed athena lock structure explicitly`() {
        val repositoryRoot = createTempDirectory("athena-lock-validation-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: 1
                    packages:
                      - name: com.engineeringood.root
                        dependencies: []
                """.trimIndent(),
            )

            val result = AthenaCompiler().validateRepositoryLock(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.lock.primary-package.block.missing"
                },
                result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeGovernedRepository(
    repositoryRoot: java.nio.file.Path,
    sourceFileName: String,
    manifestBody: String,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(manifestBody)
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve(sourceFileName).writeText(
        "system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }",
    )
}
