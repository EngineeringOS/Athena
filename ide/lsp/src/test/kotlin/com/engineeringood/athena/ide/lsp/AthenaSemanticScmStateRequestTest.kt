package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams

class AthenaSemanticScmStateRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `semantic scm request exposes runtime owned review and commit payloads`() {
        val root = kotlin.io.path.createTempDirectory("athena-lsp-semantic-scm-")
        val current = root.resolve("current")
        val baseline = root.resolve("baseline")
        try {
            writeSemanticScmFixture(
                repositoryRoot = baseline,
                sourceText = """
                    system Demo {
                      device PLC1 {
                        type Switch
                      }
                    }
                """.trimIndent(),
            )
            writeSemanticScmFixture(
                repositoryRoot = current,
                dependencyLocator = "../alpha-package",
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
            AthenaCompiler().materializeRepositoryLock(baseline)
            AthenaCompiler().materializeRepositoryLock(current)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = current.toUri().toString()
                    },
                ).get()

                val payload = server.semanticScmState(
                    AthenaSemanticScmStateParams(
                        adapterId = "scm-git",
                        locator = "../baseline",
                        locatorLabel = "Baseline repository",
                        baselineId = "baseline-review-commit",
                        baselineLabel = "Baseline",
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("scm-git", payload.adapterId)
                assertEquals("../baseline", payload.locator)
                assertEquals("baseline-review-commit", payload.baselineId)
                assertEquals("Baseline", payload.baselineLabel)
                assertNotNull(payload.review)
                assertNotNull(payload.commit)
                assertTrue(payload.review.entries.any { entry -> entry.kind == "package-dependency" })
                assertTrue(payload.review.entries.any { entry -> entry.kind == "engineering-change" })
                assertTrue(payload.commit.entries.any { entry -> entry.kind == "package-dependency" })
                assertTrue(payload.commit.entries.any { entry -> entry.kind == "derived-consequence" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic scm request surfaces unresolved baseline diagnostics`() {
        val repository = createGovernedTestRepository("athena-lsp-semantic-scm-missing-")
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

                val payload = server.semanticScmState(
                    AthenaSemanticScmStateParams(
                        adapterId = "scm-git",
                        locator = "../baseline",
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("baseline-unresolved", payload.status)
                assertTrue(payload.diagnostics.any { diagnostic ->
                    diagnostic.ruleId == "semantic.baseline.repository-root.missing"
                })
                assertEquals(null, payload.review)
                assertEquals(null, payload.commit)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic scm request exposes engineering impact and affected subjects for the m9 proof slice`() {
        val root = kotlin.io.path.createTempDirectory("athena-lsp-semantic-scm-m9-")
        val current = root.resolve("current")
        val baseline = root.resolve("baseline")
        try {
            writeSemanticScmFixture(
                repositoryRoot = baseline,
                sourceText = semanticScmKnowledgeBaselineSource,
            )
            writeSemanticScmFixture(
                repositoryRoot = current,
                sourceText = semanticScmKnowledgeChangedSource,
            )
            AthenaCompiler().materializeRepositoryLock(baseline)
            AthenaCompiler().materializeRepositoryLock(current)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = current.toUri().toString()
                    },
                ).get()

                val payload = server.semanticScmState(
                    AthenaSemanticScmStateParams(
                        adapterId = "scm-git",
                        locator = "../baseline",
                        baselineId = "baseline-m9-review",
                        baselineLabel = "M9 baseline",
                    ),
                ).get()

                assertNotNull(payload)
                val review = assertNotNull(payload.review)
                val commit = assertNotNull(payload.commit)
                assertEquals(1, review.engineeringImpactConsequences.size)
                assertEquals("component:M1", review.engineeringImpactConsequences.single().affectedSubjectIdentity)
                assertTrue(review.entries.any { entry -> entry.kind == "engineering-impact" })
                assertTrue(commit.entries.any { entry -> entry.kind == "engineering-impact" })
                assertTrue(payload.diagnostics.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}

private fun writeSemanticScmFixture(
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

private val semanticScmKnowledgeBaselineSource = """
    system MotorImpactProof {
      device M1 {
        type Motor
        power "7.5kw"
        voltage "400V"
        powerFactor "0.86"
        efficiency "0.92"
        breakerRatedCurrent "10A"
        cableAllowedCurrent "12A"
        relayRatedCurrent "13A"
      }
    }
""".trimIndent()

private val semanticScmKnowledgeChangedSource = """
    system MotorImpactProof {
      device M1 {
        type Motor
        power "9kw"
        voltage "400V"
        powerFactor "0.86"
        efficiency "0.92"
        breakerRatedCurrent "10A"
        cableAllowedCurrent "12A"
        relayRatedCurrent "13A"
      }
    }
""".trimIndent()
