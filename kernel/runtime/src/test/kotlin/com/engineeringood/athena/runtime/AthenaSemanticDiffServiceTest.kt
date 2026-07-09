package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolver
import com.engineeringood.athena.scm.SemanticChangeCategory
import com.engineeringood.athena.scm.SemanticDerivedConsequenceType
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaSemanticDiffServiceTest {
    @Test
    fun `produces the same semantic diff for the same baseline and current repository state`() {
        val root = createTempDirectory("athena-runtime-semantic-diff-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeGovernedRepository(
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
            writeGovernedRepository(
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
                    baselineId = "baseline-1",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val first = runtime.serviceRegistry.semanticDiffs().compareAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val second = runtime.serviceRegistry.semanticDiffs().compareAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            assertEquals(first, second)
            assertTrue(first.authoredChanges.any { change -> change.category == SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED })
            assertTrue(first.authoredChanges.any { change -> change.category == SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED })
            assertTrue(first.authoredChanges.any { change -> change.category == SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED })
            assertTrue(first.derivedConsequences.any { consequence ->
                consequence.type == SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED
            })
            assertEquals(
                listOf(
                    SemanticDerivedConsequenceType.LOCK_UPDATED,
                    SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
                    SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED,
                ),
                first.derivedConsequences.map { consequence -> consequence.type },
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `publishes deterministic incomplete-input consequences when the current source does not compile`() {
        val root = createTempDirectory("athena-runtime-semantic-diff-invalid-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeGovernedRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                sourceText = "system Demo { }",
            )
            writeGovernedRepository(
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
                    baselineId = "baseline-invalid",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val first = runtime.serviceRegistry.semanticDiffs().compareAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val second = runtime.serviceRegistry.semanticDiffs().compareAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            assertEquals(first, second)
            assertEquals(
                listOf(SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE),
                first.derivedConsequences.map { consequence -> consequence.type },
            )
            assertEquals(
                "semantic.current.compile.parse-failed",
                first.derivedConsequences.single().diagnostic?.ruleId?.value,
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
