package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.ir.DerivedEngineeringInputKind
import com.engineeringood.athena.ir.DerivedEngineeringValueKind
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import com.engineeringood.athena.ir.EngineeringConstraintRuleKind
import com.engineeringood.athena.ir.EngineeringImpactReasonKind
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringImpactConsequenceCalculatorTest {
    @Test
    fun `compile outputs produce deterministic impact consequences for a governed motor power change`() {
        val repoRoot = resolveRepoRoot()
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(repoRoot.resolve("extensions/knowledge-electrical-basic")),
            ),
        )

        val before = assertIs<CompilerCompilationSuccess>(
            compiler.compile(
                path = repoRoot.resolve("examples/m9/motor-impact-proof.athena"),
                sourceText = impactFixture(
                    power = "7.5kw",
                    breakerRatedCurrent = "10A",
                ),
            ),
        )
        val after = assertIs<CompilerCompilationSuccess>(
            compiler.compile(
                path = repoRoot.resolve("examples/m9/motor-impact-proof.athena"),
                sourceText = impactFixture(
                    power = "9kw",
                    breakerRatedCurrent = "10A",
                ),
            ),
        )

        val first = compiler.calculateImpactConsequences(before = before, after = after)
        val second = compiler.calculateImpactConsequences(before = before, after = after)

        assertEquals(first, second)
        val consequence = first.consequences.single()
        assertEquals("component:M1", consequence.affectedSubjectIdentity.value)
        assertEquals(listOf("component:M1"), consequence.triggerSubjectIdentities.map { identity -> identity.value })
        assertEquals(
            listOf(
                EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                EngineeringImpactReasonKind.DERIVED_CONTEXT_CHANGED,
                EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED,
                EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED,
            ),
            consequence.reasonKinds,
        )
        assertEquals(
            listOf(DerivedEngineeringInputKind.MOTOR_POWER),
            consequence.affectedInputKinds,
        )
        assertEquals(
            listOf(
                DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                DerivedEngineeringValueKind.THERMAL_LOAD,
            ),
            consequence.affectedDerivedValueKinds,
        )
        assertEquals(
            listOf(
                EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_CABLE_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_RELAY_SIZING_CURRENT,
            ),
            consequence.affectedCapabilityFactKinds,
        )
        assertEquals(
            listOf(
                EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                EngineeringConstraintRuleKind.CABLE_SUFFICIENCY,
                EngineeringConstraintRuleKind.RELAY_SUFFICIENCY,
            ),
            consequence.affectedConstraintRuleKinds,
        )
    }

    @Test
    fun `breaker sizing changes still publish constraint impact without derived-context churn`() {
        val repoRoot = resolveRepoRoot()
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(repoRoot.resolve("extensions/knowledge-electrical-basic")),
            ),
        )

        val before = assertIs<CompilerCompilationSuccess>(
            compiler.compile(
                path = repoRoot.resolve("examples/m9/motor-impact-proof.athena"),
                sourceText = impactFixture(
                    power = "7.5kw",
                    breakerRatedCurrent = "10A",
                ),
            ),
        )
        val after = assertIs<CompilerCompilationSuccess>(
            compiler.compile(
                path = repoRoot.resolve("examples/m9/motor-impact-proof.athena"),
                sourceText = impactFixture(
                    power = "7.5kw",
                    breakerRatedCurrent = "25A",
                ),
            ),
        )

        val consequence = compiler.calculateImpactConsequences(before = before, after = after).consequences.single()

        assertEquals(
            listOf(
                EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED,
            ),
            consequence.reasonKinds,
        )
        assertEquals(listOf(DerivedEngineeringInputKind.BREAKER_RATED_CURRENT), consequence.affectedInputKinds)
        assertTrue(consequence.affectedDerivedValueKinds.isEmpty())
        assertTrue(consequence.affectedCapabilityFactKinds.isEmpty())
        assertEquals(listOf(EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY), consequence.affectedConstraintRuleKinds)
    }

    private fun impactFixture(
        power: String,
        breakerRatedCurrent: String,
    ): String {
        return """
            system MotorImpactProof {
              device M1 {
                type Motor
                power "$power"
                voltage "400V"
                powerFactor "0.86"
                efficiency "0.92"
                breakerRatedCurrent "$breakerRatedCurrent"
                cableAllowedCurrent "12A"
                relayRatedCurrent "13A"
              }
            }
        """.trimIndent()
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
