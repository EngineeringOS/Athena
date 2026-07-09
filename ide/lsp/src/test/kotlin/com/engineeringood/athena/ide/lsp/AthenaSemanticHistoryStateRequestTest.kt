package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams

class AthenaSemanticHistoryStateRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `semantic history request exposes runtime owned package evolution payloads`() {
        val root = kotlin.io.path.createTempDirectory("athena-lsp-semantic-history-")
        val current = root.resolve("current")
        val baselineA = root.resolve("baseline-a")
        val baselineB = root.resolve("baseline-b")
        try {
            writeSemanticHistoryFixture(
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
            writeSemanticHistoryFixture(
                repositoryRoot = baselineB,
                packageVersion = "1.0.0",
                dependencyLocator = "../alpha-baseline",
                dependencyVersion = "1.0.0",
                sourceText = """
                    system Demo {
                      device PLC1 {
                        type Switch
                      }
                    }
                """.trimIndent(),
            )
            writeSemanticHistoryFixture(
                repositoryRoot = current,
                packageVersion = "1.1.0",
                dependencyLocator = "../alpha-current",
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
            AthenaCompiler().materializeRepositoryLock(current)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = current.toUri().toString()
                    },
                ).get()

                val payload = server.semanticHistoryState(
                    AthenaSemanticHistoryStateParams(
                        packageName = "com.engineeringood.demo",
                        baselines = listOf(
                            AthenaSemanticHistoryBaselineParams(
                                adapterId = "scm-git",
                                locator = "../baseline-a",
                                locatorLabel = "Baseline A repository",
                                baselineId = "baseline-a",
                                baselineLabel = "Baseline A",
                            ),
                            AthenaSemanticHistoryBaselineParams(
                                adapterId = "scm-git",
                                locator = "../baseline-b",
                                locatorLabel = "Baseline B repository",
                                baselineId = "baseline-b",
                                baselineLabel = "Baseline B",
                            ),
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("com.engineeringood.demo", payload.packageId.name)
                assertEquals(2, payload.baselines.size)
                val history = assertNotNull(payload.history)
                assertEquals(2, history.baselineCount)
                assertEquals("review-required", history.releaseRelevance)
                assertEquals("high", history.contractBreakRisk)
                assertTrue(history.packageLineage.any { lineage -> lineage.currentVersion == "1.1.0" })
                assertTrue(history.entries.any { entry ->
                    entry.dependencyMovements.any { movement -> movement.kind == "retargeted" }
                })
            } finally {
                server.shutdown().get()
            }
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic history request surfaces unresolved baseline diagnostics`() {
        val repository = createGovernedTestRepository("athena-lsp-semantic-history-missing-")
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.semanticHistoryState(
                    AthenaSemanticHistoryStateParams(
                        packageName = "com.engineeringood.demo",
                        baselines = listOf(
                            AthenaSemanticHistoryBaselineParams(
                                adapterId = "scm-git",
                                locator = "../baseline",
                            ),
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("baseline-unresolved", payload.status)
                assertTrue(payload.diagnostics.any { diagnostic ->
                    diagnostic.ruleId == "semantic.baseline.repository-root.missing"
                })
                assertEquals(null, payload.history)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeSemanticHistoryFixture(
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
