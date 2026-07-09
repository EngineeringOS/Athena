package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolver
import com.engineeringood.athena.scm.SemanticCommitEntryKind
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaSemanticCommitServiceTest {
    @Test
    fun `reuses the semantic review path and produces deterministic commit intent`() {
        val root = createTempDirectory("athena-runtime-semantic-commit-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeCommitRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                sourceText = """
                    system Demo {
                      device PLC1 {
                        type Switch
                      }

                      port PLC1.out {
                        direction out
                        signal Digital
                      }
                    }
                """.trimIndent(),
            )
            writeCommitRepository(
                repositoryRoot = currentRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                dependencyLocator = "vendor/alpha",
                sourceText = """
                    system Demo {
                      device PLC1 {
                        model "S7-1200"
                      }

                      device M1 {
                        type Motor
                      }

                      port PLC1.out {
                        direction out
                        signal Digital
                      }

                      port M1.in {
                        direction in
                        signal Analog
                      }

                      connect PLC1.out -> M1.in
                      connect PLC1.out -> Missing.in
                    }
                """.trimIndent(),
            )
            AthenaCompiler().materializeRepositoryLock(baselineRoot)
            AthenaCompiler().materializeRepositoryLock(currentRoot)

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
                    semanticDiffServiceProvider = {
                        AthenaSemanticDiffService()
                    },
                ),
            )
            val workspace = runtime.openWorkspace(currentRoot)
            val session = workspace.activateRepositoryGraphSession(
                projectName = "demo",
                sourcePath = currentRoot.resolve("src").resolve("demo.athena"),
            )
            val baseline = runtime.serviceRegistry.semanticBaselines().resolveBaseline(
                session = session,
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "baseline-commit",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val review = runtime.serviceRegistry.semanticReviews().summarizeAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val fromReview = runtime.serviceRegistry.semanticCommits().prepareReview(review)
            val first = runtime.serviceRegistry.semanticCommits().prepareAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val second = runtime.serviceRegistry.semanticCommits().prepareAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            assertEquals(fromReview, first)
            assertEquals(first, second)
            assertTrue(first.entries.any { entry -> entry.kind == SemanticCommitEntryKind.PACKAGE_DEPENDENCY })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticCommitEntryKind.ENGINEERING_CHANGE })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticCommitEntryKind.DERIVED_CONSEQUENCE })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticCommitEntryKind.VALIDATION_CONSEQUENCE })
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `publishes one degraded-input commit warning when semantic comparison input is incomplete`() {
        val root = createTempDirectory("athena-runtime-semantic-commit-invalid-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeCommitRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                sourceText = "system Demo { }",
            )
            writeCommitRepository(
                repositoryRoot = currentRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                sourceText = "system Demo {",
            )
            AthenaCompiler().materializeRepositoryLock(baselineRoot)
            AthenaCompiler().materializeRepositoryLock(currentRoot)

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
                projectName = "demo",
                sourcePath = currentRoot.resolve("src").resolve("demo.athena"),
            )
            val baseline = runtime.serviceRegistry.semanticBaselines().resolveBaseline(
                session = session,
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "baseline-invalid-commit",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val intent = runtime.serviceRegistry.semanticCommits().prepareAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            val inputWarning = intent.entries.single { entry -> entry.kind == SemanticCommitEntryKind.INPUT_WARNING }
            assertTrue(inputWarning.factReferences.any { reference ->
                reference.identifier.contains("INPUT_WARNING")
            })
            assertTrue(inputWarning.factReferences.any { reference ->
                reference.identifier.contains("semantic.current.compile.parse-failed")
            })
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}

private fun writeCommitRepository(
    repositoryRoot: java.nio.file.Path,
    packageName: String,
    sourceFileName: String,
    sourceText: String,
    dependencyLocator: String? = null,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        buildString {
            appendLine("primaryPackage:")
            appendLine("  name: $packageName")
            appendLine("  version: 1.0.0")
            appendLine("  sourceRoot: src")
            if (dependencyLocator != null) {
                appendLine("dependencies:")
                appendLine("  - name: com.engineeringood.alpha")
                appendLine("    version: 1.0.0")
                appendLine("    source: local-path")
                appendLine("    locator: $dependencyLocator")
            }
        }.trimEnd(),
    )
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve(sourceFileName).writeText(sourceText)

    if (dependencyLocator != null) {
        val dependencyRoot = repositoryRoot.resolve(dependencyLocator).createDirectories()
        dependencyRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.alpha
                  version: 1.0.0
                  sourceRoot: src
            """.trimIndent(),
        )
        dependencyRoot.resolve("athena.lock").writeText("# lock")
        val dependencySourceRoot = dependencyRoot.resolve("src").createDirectories()
        dependencySourceRoot.resolve("alpha.athena").writeText("system Alpha { }")
        AthenaCompiler().materializeRepositoryLock(dependencyRoot)
    }
}
