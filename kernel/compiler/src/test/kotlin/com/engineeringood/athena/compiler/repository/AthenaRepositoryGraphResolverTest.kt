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
    fun `selects declared direct child package from project catalog into canonical graph`() {
        val repositoryRoot = createTempDirectory("athena-package-catalog-")
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
                      - name: com.vendor.drive
                        version: 1.0.0
                        source: local-package
                """.trimIndent(),
            )
            writeCatalogPackage(
                repositoryRoot = repositoryRoot,
                directoryName = "com.vendor.drive",
                packageName = "com.vendor.drive",
                version = "1.0.0",
            )
            writeCatalogPackage(
                repositoryRoot = repositoryRoot,
                directoryName = "com.vendor.unused",
                packageName = "com.vendor.unused",
                version = "1.0.0",
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertTrue(result.isValid, result.diagnostics.joinToString("\n") { "${it.code}: ${it.message}" })
            assertEquals(
                listOf("com.engineeringood.root", "com.vendor.drive"),
                result.graph?.packages?.map { it.packageId.name },
            )
            assertEquals("packages/com.vendor.drive", result.graph?.packages?.last()?.sourceRoot)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects direct child package whose id differs from directory name`() {
        val repositoryRoot = createTempDirectory("athena-package-catalog-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                """.trimIndent(),
            )
            writeCatalogPackage(
                repositoryRoot = repositoryRoot,
                directoryName = "com.vendor.wrong",
                packageName = "com.vendor.drive",
                version = "1.0.0",
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(result.diagnostics.any { it.code == "repository.catalog.package-id.directory-mismatch" })
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects nested package manifests under direct catalog child`() {
        val repositoryRoot = createTempDirectory("athena-package-catalog-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                """.trimIndent(),
            )
            val nested = repositoryRoot.resolve("packages/com.vendor.drive/nested").createDirectories()
            nested.resolve("package.yaml").writeText(
                """
                    packageId:
                      name: com.vendor.drive
                      version: 1.0.0
                """.trimIndent(),
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.catalog.package-manifest.missing" ||
                        diagnostic.code == "repository.catalog.package-manifest.nested-forbidden"
                },
                result.diagnostics.toString(),
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects malformed package manifest instead of guessing fields`() {
        val repositoryRoot = createTempDirectory("athena-package-catalog-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                """.trimIndent(),
            )
            val packageRoot = repositoryRoot.resolve("packages/com.vendor.drive").createDirectories()
            packageRoot.resolve("package.yaml").writeText(
                """
                    packageId:
                      name: com.vendor.drive
                      version: [1.0.0
                """.trimIndent(),
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { it.code == "repository.catalog.package-manifest.malformed" },
                result.diagnostics.toString(),
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects duplicate package manifest keys deterministically`() {
        val repositoryRoot = createTempDirectory("athena-package-catalog-")
        try {
            writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      sourceRoot: src
                """.trimIndent(),
            )
            val packageRoot = repositoryRoot.resolve("packages/com.vendor.drive").createDirectories()
            packageRoot.resolve("package.yaml").writeText(
                """
                    packageId:
                      name: com.vendor.drive
                      name: com.vendor.other
                      version: 1.0.0
                """.trimIndent(),
            )

            val result = AthenaCompiler().resolveRepositoryGraph(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { it.code == "repository.catalog.package-manifest.malformed" },
                result.diagnostics.toString(),
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

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
                        source: local-path
                        locator: vendor/beta
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
                        source: local-package
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

private fun writeCatalogPackage(
    repositoryRoot: java.nio.file.Path,
    directoryName: String,
    packageName: String,
    version: String,
) {
    val packageRoot = repositoryRoot.resolve("packages").resolve(directoryName).createDirectories()
    packageRoot.resolve("package.yaml").writeText(
        """
            packageId:
              name: $packageName
              version: $version
        """.trimIndent(),
    )
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
    val packageDirectory = repositoryRoot.resolve("src").resolve(packageName.replace('.', '/')).createDirectories()
    packageDirectory.resolve(sourceFileName).writeText(
        """
            package $packageName

            system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }
        """.trimIndent(),
    )
}
