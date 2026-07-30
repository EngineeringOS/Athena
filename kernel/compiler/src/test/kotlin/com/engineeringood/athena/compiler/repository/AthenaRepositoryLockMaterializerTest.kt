package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.packageplatform.PackageAdmissionLimits
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
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
            assertTrue(firstBytes.contains("version: 2"))
            assertTrue(firstBytes.contains("schema: repository-lock-v2"))
            assertTrue(firstBytes.contains("compilerSchema: athena-lock-v2"))
            assertTrue(firstBytes.contains("snapshotDigest: package-snapshot:"))
            assertTrue(firstBytes.contains("sourceHashes:"))
            assertTrue(firstBytes.contains("primaryPackage:"))
            assertTrue(firstBytes.contains("sourceRoot: vendor/alpha/src"))
            assertTrue(firstBytes.contains("dependencies: []"))
            Files.newDirectoryStream(repositoryRoot, "athena.lock.tmp-*").use { stream ->
                assertFalse(stream.iterator().hasNext(), "Atomic materialization must not leave temporary lock files after success.")
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `validate mode fails closed when athena lock is missing and does not write`() {
        val repositoryRoot = createTempDirectory("athena-lock-missing-")
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
                """.trimIndent(),
            )

            val result = AthenaCompiler().validateRepositoryLock(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(result.diagnostics.any { diagnostic -> diagnostic.code == "repository.lock.missing" })
            assertFalse(repositoryRoot.resolve("athena.lock").exists(), "Validate mode must not write missing lock files.")
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `source content changes the admitted package snapshot digest independently from lock bytes`() {
        val repositoryRoot = createTempDirectory("athena-lock-digest-")
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
                """.trimIndent(),
            )
            val first = AthenaCompiler().materializeRepositoryLock(repositoryRoot)
            repositoryRoot.resolve("athena.lock").writeText(first.renderedLock!!.replace("# Derived", "# Comment changed\n# Derived"))
            val nonCanonical = AthenaCompiler().validateRepositoryLock(repositoryRoot)
            val packageDirectory = repositoryRoot.resolve("src").resolve("com/engineeringood/root")
            packageDirectory.resolve("root.athena").writeText(
                """
                    package com.engineeringood.root

                    system RootChanged { }
                """.trimIndent(),
            )
            val stale = AthenaCompiler().validateRepositoryLock(repositoryRoot)

            assertFalse(nonCanonical.isValid)
            assertTrue(nonCanonical.diagnostics.any { diagnostic -> diagnostic.code == "repository.lock.noncanonical" })
            assertFalse(stale.isValid)
            assertTrue(stale.diagnostics.any { diagnostic -> diagnostic.code == "repository.lock.stale" })
            assertTrue(
                stale.renderedExpectedLock!!.substringAfter("snapshotDigest: ") != first.renderedLock.substringAfter("snapshotDigest: "),
                "Source bytes must affect package snapshot digest.",
            )
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
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: 2
                    schema: repository-lock-v2
                    compilerSchema: athena-lock-v2
                    validatedLockStateDigest: lock-state:stale
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                    packages:
                      - name: com.engineeringood.root
                        version: 1.0.0
                        sourceRoot: src
                        snapshotDigest: package-snapshot:stale
                        sourceHashes: []
                        resourceHashes: []
                        dependencies: []
                """.trimIndent(),
            )

            val result = AthenaCompiler().validateRepositoryLock(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic ->
                    diagnostic.code == "repository.lock.stale"
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
                packageName = "com.engineeringood.root",
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
                    diagnostic.code == "repository.lock.schema-incompatible"
                },
                result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `admission rejects escaped local path dependencies before lock materialization`() {
        val repositoryRoot = createTempDirectory("athena-lock-escaped-dependency-")
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
                        locator: ../alpha
                """.trimIndent(),
            )

            val result = AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic -> diagnostic.code == "repository.contract.manifest.dependencies.locator.invalid" },
                result.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
            assertFalse(repositoryRoot.resolve("athena.lock").exists(), "Invalid admission input must not write a lock.")
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `admission rejects governed source unit budget excess`() {
        val repositoryRoot = createTempDirectory("athena-lock-source-budget-")
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
                """.trimIndent(),
            )
            val packageDirectory = repositoryRoot.resolve("src").resolve("com/engineeringood/root")
            packageDirectory.resolve("second.athena").writeText(
                """
                    package com.engineeringood.root

                    system Second { }
                """.trimIndent(),
            )

            val result = AthenaRepositoryLockMaterializer(
                admissionLimits = PackageAdmissionLimits.STANDARD.copy(maxGovernedSourceUnitsPerPackage = 1),
            ).materialize(repositoryRoot)

            assertFalse(result.isValid)
            assertTrue(
                result.diagnostics.any { diagnostic -> diagnostic.code == "repository.admission.budget.source-units-exceeded" },
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
    val packageDirectory = repositoryRoot.resolve("src").resolve(packageName.replace('.', '/')).createDirectories()
    packageDirectory.resolve(sourceFileName).writeText(
        """
            package $packageName

            system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }
        """.trimIndent(),
    )
}
