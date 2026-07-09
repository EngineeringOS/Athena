package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.integrations.scm.git.GitSemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticBaselineResolver
import com.engineeringood.athena.scm.SemanticReviewEnrichmentKind
import com.engineeringood.athena.scm.SemanticReviewEntryKind
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaSemanticReviewServiceTest {
    @Test
    fun `reuses the semantic diff path and produces deterministic review summaries`() {
        val root = createTempDirectory("athena-runtime-semantic-review-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeReviewRepository(
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
            writeReviewRepository(
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
                    baselineId = "baseline-review",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val diff = runtime.serviceRegistry.semanticDiffs().compareAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val fromDiff = runtime.serviceRegistry.semanticReviews().summarizeDiff(diff)
            val first = runtime.serviceRegistry.semanticReviews().summarizeAgainstBaseline(
                session = session,
                baseline = baseline,
            )
            val second = runtime.serviceRegistry.semanticReviews().summarizeAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            assertEquals(fromDiff, first)
            assertEquals(first, second)
            assertTrue(first.entries.any { entry -> entry.kind == SemanticReviewEntryKind.PACKAGE_DEPENDENCY })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticReviewEntryKind.ENGINEERING_CHANGE })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticReviewEntryKind.DERIVED_CONSEQUENCE })
            assertTrue(first.entries.any { entry -> entry.kind == SemanticReviewEntryKind.VALIDATION_IMPACT })
            assertEquals(
                listOf(
                    SemanticReviewEnrichmentKind.DOMAIN_LABEL,
                    SemanticReviewEnrichmentKind.REVIEW_HINT,
                    SemanticReviewEnrichmentKind.DOMAIN_SUMMARY,
                ),
                first.enrichments
                    .filter { enrichment -> enrichment.pluginId == "com.engineeringood.athena.domain.electrical-runtime" }
                    .map { enrichment -> enrichment.kind },
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `publishes one degraded-input warning when semantic comparison input is incomplete`() {
        val root = createTempDirectory("athena-runtime-semantic-review-invalid-")
        try {
            val currentRoot = root.resolve("current")
            val baselineRoot = root.resolve("baseline")
            writeReviewRepository(
                repositoryRoot = baselineRoot,
                packageName = "com.engineeringood.demo",
                sourceFileName = "demo.athena",
                sourceText = "system Demo { }",
            )
            writeReviewRepository(
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
                    baselineId = "baseline-invalid-review",
                    label = "Baseline",
                ),
                locator = SemanticBaselineLocator(
                    adapterId = GitSemanticBaselineAdapter.ADAPTER_ID,
                    locator = "../baseline",
                ),
            ).snapshot ?: error("Expected baseline snapshot")

            val summary = runtime.serviceRegistry.semanticReviews().summarizeAgainstBaseline(
                session = session,
                baseline = baseline,
            )

            val inputWarning = summary.entries.single { entry -> entry.kind == SemanticReviewEntryKind.INPUT_WARNING }

            assertTrue(inputWarning.factReferences.any { reference ->
                reference.identifier.contains("COMPARISON_INPUT_INCOMPLETE")
            })
            assertTrue(inputWarning.factReferences.any { reference ->
                reference.identifier.contains("semantic.current.compile.parse-failed")
            })
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}

private fun writeReviewRepository(
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
