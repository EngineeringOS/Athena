package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticCommitEntryKind
import com.engineeringood.athena.scm.SemanticReviewEntryKind
import com.engineeringood.athena.scm.SemanticBaselineResolver
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaSemanticScmStateServiceTest {
    @Test
    fun `projects deterministic semantic review and commit state for one baseline request`() {
        val root = createTempDirectory("athena-runtime-semantic-scm-state-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeSemanticScmRepository(
                repositoryRoot = baselineRoot,
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
            writeSemanticScmRepository(
                repositoryRoot = currentRoot,
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
                ),
            )
            val session = runtime.openWorkspace(currentRoot).activateRepositoryGraphSession(
                projectName = "demo",
                sourcePath = currentRoot.resolve("src").resolve("demo.athena"),
            )
            val descriptor = SemanticBaselineDescriptor(
                baselineId = "baseline-review-commit",
                label = "Baseline",
            )
            val locator = SemanticBaselineLocator(
                adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                locator = "../baseline",
                label = "Baseline repository",
            )

            val first = runtime.serviceRegistry.semanticScmStates().inspect(
                session = session,
                descriptor = descriptor,
                locator = locator,
            )
            val second = runtime.serviceRegistry.semanticScmStates().inspect(
                session = session,
                descriptor = descriptor,
                locator = locator,
            )

            assertEquals(first, second)
            assertEquals(AthenaSemanticScmStateStatus.READY, first.status)
            assertTrue(first.isReady)
            assertTrue(first.diagnostics.isEmpty())
            val review = assertNotNull(first.reviewSummary)
            val commit = assertNotNull(first.commitIntent)
            assertTrue(review.entries.any { entry -> entry.kind == SemanticReviewEntryKind.PACKAGE_DEPENDENCY })
            assertTrue(review.entries.any { entry -> entry.kind == SemanticReviewEntryKind.ENGINEERING_CHANGE })
            assertTrue(commit.entries.any { entry -> entry.kind == SemanticCommitEntryKind.PACKAGE_DEPENDENCY })
            assertTrue(commit.entries.any { entry -> entry.kind == SemanticCommitEntryKind.ENGINEERING_CHANGE })
            assertTrue(commit.entries.any { entry -> entry.kind == SemanticCommitEntryKind.DERIVED_CONSEQUENCE })
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `surfaces unresolved baseline diagnostics without fabricating review state`() {
        val currentRoot = createTempDirectory("athena-runtime-semantic-scm-state-missing-")
        try {
            writeSemanticScmRepository(
                repositoryRoot = currentRoot,
                sourceText = "system Demo { }",
            )
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
            val session = runtime.openWorkspace(currentRoot).activateRepositoryGraphSession(
                projectName = "demo",
                sourcePath = currentRoot.resolve("src").resolve("demo.athena"),
            )

            val state = runtime.serviceRegistry.semanticScmStates().inspect(
                session = session,
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "missing-baseline",
                    label = "Missing baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            )

            assertEquals(AthenaSemanticScmStateStatus.BASELINE_UNRESOLVED, state.status)
            assertTrue(!state.isReady)
            assertTrue(state.diagnostics.any { diagnostic ->
                diagnostic.ruleId.value == "semantic.baseline.repository-root.missing"
            })
            assertEquals(null, state.reviewSummary)
            assertEquals(null, state.commitIntent)
        } finally {
            currentRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeSemanticScmRepository(
    repositoryRoot: java.nio.file.Path,
    sourceText: String,
    dependencyLocator: String? = null,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        buildString {
            appendLine("primaryPackage:")
            appendLine("  name: com.engineeringood.demo")
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
    sourceRoot.resolve("demo.athena").writeText(sourceText)

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
