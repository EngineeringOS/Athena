package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaRepositoryReportPublisherTest {
    @Test
    fun `publishes canonical repository graph report from compiler owned authority`() {
        val repositoryRoot = createTempDirectory("athena-repository-report-")
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
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val first = AthenaCompiler().publishRepositoryGraphReport(repositoryRoot)
            val second = AthenaCompiler().publishRepositoryGraphReport(repositoryRoot)

            assertEquals(first, second)
            assertTrue(first.isValid, first.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" })
            assertEquals(AthenaRepositoryReportLockState.CURRENT, first.lockState)
            assertEquals("com.engineeringood.root", first.report?.repository?.lock?.primaryPackage?.name)
            assertEquals(
                listOf("com.engineeringood.root", "com.engineeringood.alpha"),
                first.report?.graph?.packages?.map { resolvedPackage -> resolvedPackage.packageId.name },
            )
            assertTrue(first.report?.diagnostics?.isEmpty() == true)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `publishes canonical report with explicit diagnostics when lock is stale`() {
        val repositoryRoot = createTempDirectory("athena-repository-report-")
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

            val result = AthenaCompiler().publishRepositoryGraphReport(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(AthenaRepositoryReportLockState.STALE, result.lockState)
            assertNotNull(result.report)
            assertEquals("com.engineeringood.alpha", result.report?.repository?.lock?.packages?.get(1)?.packageId?.name)
            assertTrue(
                result.diagnostics.any { diagnostic -> diagnostic.code == "repository.lock.content.out-of-date" },
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
