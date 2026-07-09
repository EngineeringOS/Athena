package com.engineeringood.athena.integrations.scm.git

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolutionRequest
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitSemanticBaselineAdapterTest {
    @Test
    fun `loads a baseline snapshot from a relative repository locator`() {
        val root = createTempDirectory("athena-git-baseline-adapter-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeGovernedRepository(currentRoot, "com.engineeringood.current")
            writeGovernedRepository(baselineRoot, "com.engineeringood.baseline")
            AthenaCompiler().materializeRepositoryLock(baselineRoot)

            val adapter = GitSemanticBaselineAdapter { AthenaCompiler() }
            val result = adapter.resolve(
                SemanticBaselineResolutionRequest(
                    descriptor = SemanticBaselineDescriptor(
                        baselineId = "baseline-1",
                        label = "Relative baseline",
                    ),
                    locator = SemanticBaselineLocator(
                        adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                        locator = "../baseline",
                    ),
                    currentRepositoryRoot = currentRoot,
                ),
            )

            assertTrue(result.isResolved)
            assertEquals(
                "com.engineeringood.baseline",
                result.snapshot?.repositoryReport?.repository?.manifest?.primaryPackage?.id?.name,
            )
            assertEquals("Root", result.snapshot?.engineeringDocuments?.single()?.system?.name)
            assertTrue(result.snapshot?.validationResult != null)
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `surfaces deterministic semantic diagnostics when the baseline repository is missing`() {
        val currentRoot = createTempDirectory("athena-git-baseline-missing-")
        try {
            writeGovernedRepository(currentRoot, "com.engineeringood.current")
            val adapter = GitSemanticBaselineAdapter { AthenaCompiler() }
            val result = adapter.resolve(
                SemanticBaselineResolutionRequest(
                    descriptor = SemanticBaselineDescriptor(
                        baselineId = "baseline-2",
                        label = "Missing baseline",
                    ),
                    locator = SemanticBaselineLocator(
                        adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                        locator = "../missing-baseline",
                    ),
                    currentRepositoryRoot = currentRoot,
                ),
            )

            assertFalse(result.isResolved)
            assertEquals("semantic.baseline.repository-root.missing", result.diagnostics.single().ruleId.value)
        } finally {
            currentRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `keeps baseline snapshot diagnostics when the baseline source cannot compile`() {
        val root = createTempDirectory("athena-git-baseline-invalid-source-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeGovernedRepository(currentRoot, "com.engineeringood.current")
            writeGovernedRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.baseline",
                sourceText = "system Root {",
            )
            AthenaCompiler().materializeRepositoryLock(baselineRoot)

            val adapter = GitSemanticBaselineAdapter { AthenaCompiler() }
            val result = adapter.resolve(
                SemanticBaselineResolutionRequest(
                    descriptor = SemanticBaselineDescriptor(
                        baselineId = "baseline-3",
                        label = "Invalid baseline",
                    ),
                    locator = SemanticBaselineLocator(
                        adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                        locator = "../baseline",
                    ),
                    currentRepositoryRoot = currentRoot,
                ),
            )

            assertFalse(result.isResolved)
            assertTrue(result.snapshot != null)
            assertEquals(
                "semantic.baseline.compile.parse-failed",
                result.snapshot?.diagnostics?.single()?.ruleId?.value,
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}

private fun writeGovernedRepository(
    repositoryRoot: java.nio.file.Path,
    packageName: String,
    sourceText: String = "system Root { }",
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        """
            primaryPackage:
              name: $packageName
              version: 1.0.0
              sourceRoot: src
        """.trimIndent(),
    )
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve("root.athena").writeText(sourceText)
}
