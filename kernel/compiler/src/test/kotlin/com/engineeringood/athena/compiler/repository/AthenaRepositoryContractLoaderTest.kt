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

class AthenaRepositoryContractLoaderTest {
    @Test
    fun `loads a valid repository root contract deterministically`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock is derived later")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")

            val loader = AthenaRepositoryContractLoader()
            val first = loader.load(repositoryRoot)
            val second = loader.load(repositoryRoot)

            assertEquals(first, second)
            assertTrue(first.isValid)
            assertTrue(first.diagnostics.isEmpty())
            assertTrue(first.manifestPresent)
            assertTrue(first.lockPresent)
            assertEquals(repositoryRoot.toAbsolutePath().normalize(), first.repositoryRoot)
            assertEquals("com.engineeringood.demo", first.repository?.manifest?.primaryPackage?.id?.name)
            assertEquals("1.0.0", first.repository?.manifest?.primaryPackage?.id?.version)
            assertEquals("src", first.repository?.manifest?.primaryPackage?.sourceRoot)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports missing manifest fields and invalid primary package identity explicitly`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: Invalid Package
                      version:
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.manifest.primary-package.name.invalid",
                    "repository.contract.manifest.primary-package.version.blank",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
            assertEquals(null, result.repository)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports missing lock and unsupported source-root layout explicitly`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: sources
                """.trimIndent(),
            )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.lock.missing",
                    "repository.contract.manifest.primary-package.source-root.unsupported",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
            assertEquals(null, result.repository)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `allows missing lock when a caller opts into authoring-first repository validation`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")

            val result = AthenaRepositoryContractLoader().load(
                repositoryRoot = repositoryRoot,
                options = AthenaRepositoryContractLoadOptions(
                    requireLockFile = false,
                ),
            )

            assertTrue(result.isValid)
            assertTrue(result.diagnostics.isEmpty())
            assertTrue(result.manifestPresent)
            assertFalse(result.lockPresent)
            assertEquals("com.engineeringood.demo", result.repository?.manifest?.primaryPackage?.id?.name)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects authored sources outside src and nested manifests`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")
            repositoryRoot.resolve("rogue.athena").writeText("system Rogue { }")
            repositoryRoot.resolve("packages").resolve("nested").createDirectories()
            repositoryRoot.resolve("packages").resolve("nested").resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.nested
                      sourceRoot: src
                """.trimIndent(),
            )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertNotNull(result.repository)
            assertEquals(
                listOf(
                    "repository.contract.manifest.nested.unsupported",
                    "repository.contract.layout.authored-source.outside-source-root",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.message.contains("packages/nested/athena.yaml")
                },
            )
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.message.contains("rogue.athena")
                },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `allows nested governed repositories for graph resolution validation`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: vendor/alpha
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")
            repositoryRoot.resolve("vendor").resolve("alpha").createDirectories()
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("src").createDirectories()
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("src").resolve("alpha.athena").writeText("system Alpha { }")

            val result = AthenaRepositoryContractLoader().load(
                repositoryRoot = repositoryRoot,
                options = AthenaRepositoryContractLoadOptions(
                    allowNestedGovernedSubrepositories = true,
                ),
            )

            assertTrue(result.isValid)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("com.engineeringood.demo", result.repository?.manifest?.primaryPackage?.id?.name)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `compiler facade exposes repository contract validation through the JVM semantic path`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()

            val result = AthenaCompiler().validateRepositoryContract(repositoryRoot)

            assertTrue(result.isValid)
            assertEquals("com.engineeringood.demo", result.repository?.manifest?.primaryPackage?.id?.name)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `compiler facade keeps strict lock validation by default`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")

            val result = AthenaCompiler().validateRepositoryContract(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf("repository.contract.lock.missing"),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `governed repository bootstrap seed passes the repository contract validator`() {
        val repositoryRoot = createTempDirectory("athena-repository-bootstrap-")
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
                    # Derived resolution state for the Athena package graph.
                    # Generated from compiler-owned repository resolution. Manifest intent remains authoritative.
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
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("factory-line.athena").writeText("system FactoryLine { }")

            val result = AthenaCompiler().validateRepositoryContract(repositoryRoot)

            assertTrue(result.isValid)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("com.engineeringood.factory-line", result.repository?.manifest?.primaryPackage?.id?.name)
            assertEquals("0.1.0", result.repository?.manifest?.primaryPackage?.id?.version)
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `loads dependency declarations in deterministic normalized order`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.zeta
                        source: local-package
                      - name: com.engineeringood.alpha
                        version: 1.2.0
                        source: local-path
                        locator: vendor\alpha
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("src").resolve("demo.athena").writeText("system Demo { }")

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertTrue(result.isValid)
            assertEquals(
                listOf("com.engineeringood.alpha", "com.engineeringood.zeta"),
                result.repository?.manifest?.dependencies?.map { dependency -> dependency.packageId.name },
            )
            assertEquals(
                "vendor/alpha",
                result.repository?.manifest?.dependencies?.first()?.locator,
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports malformed dependency declarations explicitly`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    dependencies:
                      name: com.engineeringood.invalid
                      - source: local-package
                      - name: com.engineeringood.remote
                        source: remote-registry
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.manifest.dependencies.item.malformed",
                    "repository.contract.manifest.dependencies.name.missing",
                    "repository.contract.manifest.dependencies.source.unsupported",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
