package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolver
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaSemanticBaselineServiceTest {
    @Test
    fun `resolves a baseline from the active repository graph session rather than frontend state`() {
        val root = createTempDirectory("athena-runtime-semantic-baseline-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            val currentSourcePath = writeGovernedRepository(
                repositoryRoot = currentRoot,
                packageName = "com.engineeringood.current",
                sourceFileName = "current.athena",
            )
            writeGovernedRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.baseline",
                sourceFileName = "baseline.athena",
            )
            AthenaCompiler().materializeRepositoryLock(currentRoot)
            AthenaCompiler().materializeRepositoryLock(baselineRoot)

            val runtime = AthenaRuntime(
                serviceRegistry = AthenaServiceRegistry(
                    semanticBaselineServiceProvider = {
                        AthenaSemanticBaselineService(
                            baselineResolver = SemanticBaselineResolver(
                                adapters = listOf(
                                    GitSemanticBaselineAdapter { AthenaCompiler() },
                                ),
                            ),
                        )
                    },
                ),
            )
            val workspace = runtime.openWorkspace(currentRoot)
            val session = workspace.activateRepositoryGraphSession(
                projectName = "current",
                sourcePath = currentSourcePath,
            )

            val result = runtime.serviceRegistry.semanticBaselines().resolveBaseline(
                session = session,
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "baseline-1",
                    label = "Relative baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            )

            assertTrue(result.isResolved)
            assertEquals(
                "com.engineeringood.baseline",
                result.snapshot?.repositoryReport?.repository?.manifest?.primaryPackage?.id?.name,
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}

private fun writeGovernedRepository(
    repositoryRoot: java.nio.file.Path,
    packageName: String,
    sourceFileName: String,
): java.nio.file.Path {
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
    val sourcePath = sourceRoot.resolve(sourceFileName)
    sourcePath.writeText("system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }")
    return sourcePath
}
