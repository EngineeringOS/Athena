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
            writePackagedProjectSource(repositoryRoot)

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
    fun `rejects hyphenated primary package identity segments`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.factory-line
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf("repository.contract.manifest.primary-package.name.invalid"),
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
            writePackagedProjectSource(repositoryRoot)

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
            writePackagedProjectSource(repositoryRoot)
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
    fun `allows representation package sources under declared package roots`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    representationPackageRoots:
                      - packages/representation
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            writePackagedProjectSource(repositoryRoot)
            repositoryRoot.resolve("packages")
                .resolve("representation")
                .resolve("athena")
                .resolve("vendor")
                .createDirectories()
            repositoryRoot.resolve("packages")
                .resolve("representation")
                .resolve("athena")
                .resolve("vendor")
                .resolve("drive.athena")
                .writeText(
                    """
                        package athena.vendor

                        symbol drive {
                          identity "vendor.drive"
                          version "1.0.0"
                        }
                    """.trimIndent(),
                )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertTrue(result.isValid, result.diagnostics.toString())
            assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
            assertEquals(
                setOf(repositoryRoot.resolve("packages/representation").toRealPath()),
                result.representationPackageRoots,
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `validates governed source package hierarchy under primary and representation roots`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    representationPackageRoots:
                      - packages/representation
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src/com/engineeringood/demo").createDirectories()
            repositoryRoot.resolve("src/com/engineeringood/demo/demo.athena").writeText(
                """
                    package com.engineeringood.demo

                    system Demo { }
                """.trimIndent(),
            )
            repositoryRoot.resolve("packages/representation/com/vendor/drive").createDirectories()
            repositoryRoot.resolve("packages/representation/com/vendor/drive/drive.athena").writeText(
                """
                    package com.vendor.drive

                    symbol drive {
                      identity "vendor.drive"
                      version "1.0.0"
                    }
                """.trimIndent(),
            )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertTrue(result.isValid, result.diagnostics.toString())
            assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects default package and mismatched governed source path`() {
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
            repositoryRoot.resolve("src/com/engineeringood/demo").createDirectories()
            repositoryRoot.resolve("src/com/engineeringood/demo/missing-package.athena").writeText("system Demo { }")
            repositoryRoot.resolve("src/com/engineeringood/wrong").createDirectories()
            repositoryRoot.resolve("src/com/engineeringood/wrong/demo.athena").writeText(
                """
                    package com.engineeringood.demo

                    system Demo { }
                """.trimIndent(),
            )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.package.default-forbidden",
                    "repository.contract.package.path-mismatch",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
            assertTrue(result.diagnostics.all { diagnostic -> diagnostic.sourcePath != null })
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects uppercase package segments and case-only path mismatch while ignoring generated caches`() {
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
            repositoryRoot.resolve("src/com/engineeringood/demo").createDirectories()
            repositoryRoot.resolve("src/com/engineeringood/demo/uppercase-package.athena").writeText(
                """
                    package com.Engineeringood.demo

                    system Demo { }
                """.trimIndent(),
            )
            repositoryRoot.resolve("src/Org/example/caseonly").createDirectories()
            repositoryRoot.resolve("src/Org/example/caseonly/case-path.athena").writeText(
                """
                    package org.example.caseonly

                    system DemoCase { }
                """.trimIndent(),
            )
            repositoryRoot.resolve(".athena/snapshots/cache/com/Engineeringood/demo").createDirectories()
            repositoryRoot.resolve(".athena/snapshots/cache/com/Engineeringood/demo/generated.athena").writeText(
                """
                    package com.engineeringood.demo

                    system Generated { }
                """.trimIndent(),
            )

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertFalse(result.isValid)
            assertEquals(
                listOf(
                    "repository.contract.package.segment-case",
                    "repository.contract.package.path-mismatch",
                ),
                result.diagnostics.map { diagnostic -> diagnostic.code },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `normalizes quoted representation package roots through the canonical manifest loader`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    representationPackageRoots:
                      - "packages/representation"
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories()
            repositoryRoot.resolve("packages/representation").createDirectories()

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertTrue(result.isValid, result.diagnostics.toString())
            assertEquals(
                setOf(repositoryRoot.resolve("packages/representation").toRealPath()),
                result.representationPackageRoots,
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `ignores derived representation snapshots under athena state`() {
        val repositoryRoot = createTempDirectory("athena-repository-contract-")
        try {
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.demo
                      sourceRoot: src
                    representationPackageRoots:
                      - packages/representation
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            writePackagedProjectSource(repositoryRoot)
            repositoryRoot.resolve(".athena")
                .resolve("snapshots")
                .resolve("m34-live-cabinet")
                .resolve("packages")
                .resolve("representation")
                .resolve("athena")
                .resolve("iec")
                .createDirectories()
            repositoryRoot.resolve(".athena")
                .resolve("snapshots")
                .resolve("m34-live-cabinet")
                .resolve("packages")
                .resolve("representation")
                .resolve("athena")
                .resolve("iec")
                .resolve("cabinet-element-set.athena")
                .writeText("package athena.iec\n")
            repositoryRoot.resolve(".athena")
                .resolve("snapshots")
                .resolve("m34-live-cabinet")
                .resolve("athena.yaml")
                .writeText("primaryPackage:\n  name: com.generated.snapshot\n  sourceRoot: src\n")

            val result = AthenaRepositoryContractLoader().load(repositoryRoot)

            assertTrue(result.isValid, result.diagnostics.toString())
            assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
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
            writePackagedProjectSource(repositoryRoot)
            repositoryRoot.resolve("vendor").resolve("alpha").createDirectories()
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      sourceRoot: src
                """.trimIndent(),
            )
            repositoryRoot.resolve("vendor").resolve("alpha").resolve("athena.lock").writeText("# lock")
            writePackagedProjectSource(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                packageName = "com.engineeringood.alpha",
                fileName = "alpha.athena",
                systemName = "Alpha",
            )

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
            writePackagedProjectSource(repositoryRoot)

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
                      name: com.engineeringood.factoryline
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
                      name: com.engineeringood.factoryline
                      version: 0.1.0
                    packages:
                      - name: com.engineeringood.factoryline
                        version: 0.1.0
                        sourceRoot: src
                        dependencies: []
                """.trimIndent(),
            )
            writePackagedProjectSource(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.factoryline",
                fileName = "factoryline.athena",
                systemName = "FactoryLine",
            )

            val result = AthenaCompiler().validateRepositoryContract(repositoryRoot)

            assertTrue(result.isValid)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("com.engineeringood.factoryline", result.repository?.manifest?.primaryPackage?.id?.name)
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
            writePackagedProjectSource(repositoryRoot)

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

private fun writePackagedProjectSource(
    repositoryRoot: java.nio.file.Path,
    packageName: String = "com.engineeringood.demo",
    fileName: String = "demo.athena",
    systemName: String = "Demo",
) {
    val packageDirectory = repositoryRoot.resolve("src").resolve(packageName.replace('.', '/'))
    packageDirectory.createDirectories()
    packageDirectory.resolve(fileName).writeText(
        """
            package $packageName

            system $systemName { }
        """.trimIndent(),
    )
}
