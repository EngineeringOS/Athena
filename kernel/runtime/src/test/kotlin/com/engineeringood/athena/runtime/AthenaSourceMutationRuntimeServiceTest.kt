package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaSourceMutationRuntimeServiceTest {
    @Test
    fun `accepted source mutation evaluation stays preview-only and preserves canonical runtime state`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
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
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())

            val dirtyCompilation = context.compiler().compile(
                sourcePath,
                """
                    system Connectable {
                      device PLC1 {
                        type Switch
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
                        signal Digital
                      }

                      connect PLC1.out -> M1.in
                    }
                """.trimIndent(),
            )

            val result = context.sourceMutationRuntime().evaluate(
                context = context,
                sourcePath = sourcePath,
                compilation = dirtyCompilation,
            )

            val accepted = assertIs<AthenaSourceMutationAccepted>(result)
            assertEquals("connectable", accepted.projectName)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, accepted.mutationCategory)
            assertEquals(AthenaMutationOutcome.ACCEPTED, accepted.outcome)
            assertTrue(accepted.validationFeedback.isEmpty())
            assertTrue(accepted.beforeDocument.connections.isEmpty())
            assertEquals(1, accepted.afterDocument.connections.size)
            assertContains(accepted.changedSemanticIds, "connection:PLC1.out->M1.in")
            assertEquals(AthenaSemanticDiffInspectionSource.SOURCE, accepted.inspection.source)
            assertContains(accepted.inspection.affectedSemanticIds, "connection:PLC1.out->M1.in")
            assertEquals(
                listOf(
                    AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
                    AthenaProjectionRefreshConsequenceLayer.LAYOUT,
                    AthenaProjectionRefreshConsequenceLayer.RENDERING,
                ),
                accepted.inspection.projectionConsequences.map { consequence -> consequence.layer }.sortedBy(Enum<*>::name),
            )
            assertTrue(accepted.inspection.projectionConsequences.all { consequence -> consequence.affectedViewIds.isNotEmpty() })
            assertEquals(
                listOf("cabinet"),
                accepted.inspection.projectionConsequences.first { consequence ->
                    consequence.layer == AthenaProjectionRefreshConsequenceLayer.RENDERING
                }.affectedViewIds,
            )
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertNull(context.latestSemanticDiffInspection())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `invalid dirty source returns governed validation feedback without mutating runtime state`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
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
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())

            val dirtyCompilation = context.compiler().compile(
                sourcePath,
                """
                    system Connectable {
                      device PLC1 {
                        type Switch
                      }

                      port PLC1.out {
                        direction out
                        signal Digital
                      }

                      connect PLC1.out -> M1.in
                    }
                """.trimIndent(),
            )

            val result = context.sourceMutationRuntime().evaluate(
                context = context,
                sourcePath = sourcePath,
                compilation = dirtyCompilation,
            )

            val validationFeedback = assertIs<AthenaSourceMutationValidationFeedbackResult>(result)
            assertEquals("connectable", validationFeedback.projectName)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, validationFeedback.mutationCategory)
            assertEquals(AthenaMutationOutcome.VALIDATION_FEEDBACK, validationFeedback.outcome)
            assertTrue(validationFeedback.validationFeedback.isNotEmpty())
            assertTrue(validationFeedback.validationFeedback.all { feedback -> feedback.code.isNotBlank() })
            assertContains(
                validationFeedback.validationFeedback.flatMap { feedback -> feedback.relatedSemanticIds },
                "connection:PLC1.out->M1.in",
            )
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertNull(context.latestSemanticDiffInspection())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `same-file alias paths are accepted as authoritative runtime source`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
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
                    signal Digital
                  }
                }
            """.trimIndent(),
        )
        val aliasPath = sourcePath.parent.resolve("${sourcePath.fileName}.link")

        try {
            Files.createLink(aliasPath, sourcePath)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )
            val dirtyCompilation = context.compiler().compile(
                aliasPath,
                """
                    system Connectable {
                      device PLC1 {
                        type Switch
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
                        signal Digital
                      }

                      connect PLC1.out -> M1.in
                    }
                """.trimIndent(),
            )

            val result = context.sourceMutationRuntime().evaluate(
                context = context,
                sourcePath = aliasPath,
                compilation = dirtyCompilation,
            )

            assertIs<AthenaSourceMutationAccepted>(result)
        } finally {
            Files.deleteIfExists(aliasPath)
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `semantic-invalid canonical baselines are unavailable for source mutation evaluation`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type Switch
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  connect PLC1.out -> Missing.in
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())
            assertTrue(canonicalCompilation.semanticResult.diagnostics.isNotEmpty())

            val dirtyCompilation = context.compiler().compile(
                sourcePath,
                """
                    system Connectable {
                      device PLC1 {
                        type Switch
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
                        signal Digital
                      }

                      connect PLC1.out -> M1.in
                    }
                """.trimIndent(),
            )

            val unavailable = assertIs<AthenaSourceMutationUnavailable>(
                context.sourceMutationRuntime().evaluate(
                    context = context,
                    sourcePath = sourcePath,
                    compilation = dirtyCompilation,
                ),
            )
            assertEquals(AthenaMutationOutcome.UNAVAILABLE, unavailable.outcome)
            assertContains(unavailable.reason.lowercase(), "canonical")
            assertContains(unavailable.reason.lowercase(), "semantic")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `source mutation evaluation rejects non-authoritative paths and reports unavailable canonical state`() {
        val sourcePath = writeProject(
            """
                system Connectable {
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
        val brokenPath = writeProject("system Broken {")

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "connectable",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = assertIs<CompilerCompilationSuccess>(context.compileActiveProject())

            val rejected = assertIs<AthenaSourceMutationRejected>(
                context.sourceMutationRuntime().evaluate(
                    context = context,
                    sourcePath = sourcePath.parent.resolve("other.athena"),
                    compilation = context.compiler().compile(
                        sourcePath.parent.resolve("other.athena"),
                        "system Other { }",
                    ),
                ),
            )
            assertEquals(AthenaMutationOutcome.REJECTED, rejected.outcome)
            assertContains(rejected.reason, "authoritative")
            assertSame(canonicalCompilation, context.compileActiveProject())

            val brokenContext = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )
            val unavailable = assertIs<AthenaSourceMutationUnavailable>(
                brokenContext.sourceMutationRuntime().evaluate(
                    context = brokenContext,
                    sourcePath = brokenPath,
                    compilation = brokenContext.compiler().compile(
                        brokenPath,
                        """
                            system Broken {
                              device PLC1 {
                                type Switch
                              }
                            }
                        """.trimIndent(),
                    ),
                ),
            )
            assertEquals(AthenaMutationOutcome.UNAVAILABLE, unavailable.outcome)
            assertContains(unavailable.reason.lowercase(), "canonical")
        } finally {
            Files.deleteIfExists(sourcePath)
            Files.deleteIfExists(brokenPath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-source-mutation-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }

    @Test
    fun `accepted source mutation carries knowledge diagnostics and impact consequences for governed engineering change`() {
        val sourcePath = writeProject(
            """
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
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "motor-impact-proof",
                sourcePath = sourcePath,
            )

            val dirtyCompilation = context.compiler().compile(
                sourcePath,
                """
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
                """.trimIndent(),
            )

            val result = context.sourceMutationRuntime().evaluate(
                context = context,
                sourcePath = sourcePath,
                compilation = dirtyCompilation,
            )

            val accepted = assertIs<AthenaSourceMutationAccepted>(result)
            assertEquals(3, accepted.inspection.knowledgeDiagnostics.size)
            val consequence = accepted.inspection.impactConsequences.consequences.single()
            assertEquals("component:M1", consequence.affectedSubjectIdentity.value)
            assertEquals(listOf("component:M1"), consequence.triggerSubjectIdentities.map { identity -> identity.value })
            assertEquals(
                listOf(
                    com.engineeringood.athena.ir.EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                    com.engineeringood.athena.ir.EngineeringImpactReasonKind.DERIVED_CONTEXT_CHANGED,
                    com.engineeringood.athena.ir.EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED,
                    com.engineeringood.athena.ir.EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED,
                ),
                consequence.reasonKinds,
            )
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }
}
