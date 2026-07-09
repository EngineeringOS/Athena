package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticContractBreakRisk
import com.engineeringood.athena.scm.SemanticDependencyMovementKind
import com.engineeringood.athena.scm.SemanticReleaseRelevance
import com.engineeringood.athena.scm.SemanticBaselineResolver
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaSemanticHistoryStateServiceTest {
    @Test
    fun `projects deterministic package history through one runtime owned state service`() {
        val root = createTempDirectory("athena-runtime-semantic-history-state-")
        try {
            val currentRoot = root.resolve("current")
            val baselineA = root.resolve("baseline-a")
            val baselineB = root.resolve("baseline-b")
            writeSemanticHistoryRepository(
                repositoryRoot = baselineA,
                packageVersion = "0.9.0",
                sourceText = """
                    system Demo {
                      device PLC1 {
                        type Switch
                      }
                    }
                """.trimIndent(),
            )
            writeSemanticHistoryRepository(
                repositoryRoot = baselineB,
                packageVersion = "1.0.0",
                dependencyLocator = "vendor/alpha",
                dependencyVersion = "1.0.0",
                sourceText = """
                    system Demo {
                      device PLC1 {
                        type Switch
                      }
                    }
                """.trimIndent(),
            )
            writeSemanticHistoryRepository(
                repositoryRoot = currentRoot,
                packageVersion = "1.1.0",
                dependencyLocator = "vendor/alpha",
                dependencyVersion = "2.0.0",
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
            AthenaCompiler().materializeRepositoryLock(baselineA)
            AthenaCompiler().materializeRepositoryLock(baselineB)
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

            val state = runtime.serviceRegistry.semanticHistoryStates().inspect(
                session = session,
                packageId = PackageIdentifier(name = "com.engineeringood.demo"),
                baselineRequests = listOf(
                    semanticHistoryBaselineRequest("baseline-a", "Baseline A", "../baseline-a"),
                    semanticHistoryBaselineRequest("baseline-b", "Baseline B", "../baseline-b"),
                ),
            )

            assertEquals(AthenaSemanticHistoryStateStatus.READY, state.status)
            assertTrue(state.isReady)
            assertTrue(state.diagnostics.isEmpty())
            assertEquals(2, state.baselineRequests.size)

            val summary = assertNotNull(state.historySummary)
            assertEquals("com.engineeringood.demo", summary.packageId.name)
            assertEquals(2, summary.baselineSequence.size)
            assertEquals(SemanticReleaseRelevance.REVIEW_REQUIRED, summary.releaseRelevance)
            assertEquals(SemanticContractBreakRisk.HIGH, summary.contractBreakRisk)
            assertTrue(summary.packageLineage.any { lineage -> lineage.currentVersion == "1.1.0" })
            assertTrue(summary.entries.any { entry ->
                entry.dependencyMovements.any { movement ->
                    movement.kind == SemanticDependencyMovementKind.VERSION_CHANGED
                }
            })
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `surfaces unresolved baseline diagnostics without fabricating package history`() {
        val currentRoot = createTempDirectory("athena-runtime-semantic-history-missing-")
        try {
            writeSemanticHistoryRepository(
                repositoryRoot = currentRoot,
                packageVersion = "1.0.0",
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

            val state = runtime.serviceRegistry.semanticHistoryStates().inspect(
                session = session,
                packageId = PackageIdentifier(name = "com.engineeringood.demo"),
                baselineRequests = listOf(
                    semanticHistoryBaselineRequest("missing", "Missing baseline", "../baseline"),
                ),
            )

            assertEquals(AthenaSemanticHistoryStateStatus.BASELINE_UNRESOLVED, state.status)
            assertTrue(!state.isReady)
            assertTrue(state.diagnostics.any { diagnostic ->
                diagnostic.ruleId.value == "semantic.baseline.repository-root.missing"
            })
            assertEquals(null, state.historySummary)
        } finally {
            currentRoot.toFile().deleteRecursively()
        }
    }
}

private fun semanticHistoryBaselineRequest(
    baselineId: String,
    label: String,
    locator: String,
): AthenaSemanticHistoryBaselineRequest {
    return AthenaSemanticHistoryBaselineRequest(
        descriptor = SemanticBaselineDescriptor(
            baselineId = baselineId,
            label = label,
        ),
        locator = SemanticBaselineLocator(
            adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
            locator = locator,
            label = label,
        ),
    )
}

private fun writeSemanticHistoryRepository(
    repositoryRoot: java.nio.file.Path,
    packageVersion: String,
    sourceText: String,
    dependencyLocator: String? = null,
    dependencyVersion: String? = null,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        buildString {
            appendLine("primaryPackage:")
            appendLine("  name: com.engineeringood.demo")
            appendLine("  version: $packageVersion")
            appendLine("  sourceRoot: src")
            if (dependencyLocator != null && dependencyVersion != null) {
                appendLine("dependencies:")
                appendLine("  - name: com.engineeringood.alpha")
                appendLine("    version: $dependencyVersion")
                appendLine("    source: local-path")
                appendLine("    locator: $dependencyLocator")
            }
        }.trimEnd(),
    )
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve("demo.athena").writeText(sourceText)

    if (dependencyLocator != null && dependencyVersion != null) {
        val dependencyRoot = repositoryRoot.resolve(dependencyLocator).createDirectories()
        dependencyRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.alpha
                  version: $dependencyVersion
                  sourceRoot: src
            """.trimIndent(),
        )
        dependencyRoot.resolve("athena.lock").writeText("# lock")
        val dependencySourceRoot = dependencyRoot.resolve("src").createDirectories()
        dependencySourceRoot.resolve("alpha.athena").writeText("system Alpha { }")
        AthenaCompiler().materializeRepositoryLock(dependencyRoot)
    }
}
