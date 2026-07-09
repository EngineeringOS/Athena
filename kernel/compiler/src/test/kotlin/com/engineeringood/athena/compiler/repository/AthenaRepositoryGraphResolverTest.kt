package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaRepositoryGraphResolverTest {
    @Test
    fun `resolves deterministic local first graph with transitive local path and local package references`() {
        val repositoryRoot = createTempDirectory("athena-graph-resolution-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
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
                      - name: com.engineeringood.beta
                        source: local-package
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                packageName = "com.engineeringood.alpha",
                sourceFileName = "alpha.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      version: 1.0.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.beta
                        source: local-path
                        locator: ../beta
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("beta"),
                packageName = "com.engineeringood.beta",
                sourceFileName = "beta.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.beta
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )

            val first = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)
            val second = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertEquals(first, second)
            assertTrue(first.isValid, first.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" })
            assertEquals("com.engineeringood.root", first.graph?.rootPackage?.name)
            assertEquals(
                listOf(
                    "com.engineeringood.root",
                    "com.engineeringood.alpha",
                    "com.engineeringood.beta",
                ),
                first.graph?.packages?.map { resolvedPackage -> resolvedPackage.packageId.name },
            )
            assertEquals(
                listOf("com.engineeringood.alpha", "com.engineeringood.beta"),
                first.graph?.packages?.first()?.directDependencies?.map { dependency -> dependency.name },
            )
            assertEquals(
                listOf("com.engineeringood.beta"),
                first.graph?.packages?.get(1)?.directDependencies?.map { dependency -> dependency.name },
            )
            assertEquals("src", first.graph?.packages?.first()?.sourceRoot)
            assertEquals("vendor/alpha/src", first.graph?.packages?.get(1)?.sourceRoot)
            assertEquals("vendor/beta/src", first.graph?.packages?.get(2)?.sourceRoot)
            assertTrue(first.report?.diagnostics?.isEmpty() == true)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports local path package identity mismatch explicitly`() {
        val repositoryRoot = createTempDirectory("athena-graph-resolution-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: vendor/other
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("other"),
                packageName = "com.engineeringood.other",
                sourceFileName = "other.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.other
                      sourceRoot: src
                """.trimIndent(),
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.resolution.local-path.package-id.mismatch"
                },
                result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports unresolved local package references explicitly`() {
        val repositoryRoot = createTempDirectory("athena-graph-resolution-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.missing
                        source: local-package
                """.trimIndent(),
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.resolution.local-package.unresolved"
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
    packageName: String,
    sourceFileName: String,
    manifestBody: String,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(manifestBody)
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve(sourceFileName).writeText("system ${sourceFileName.substringBefore('.') .replaceFirstChar(Char::uppercase)} { }")
}
