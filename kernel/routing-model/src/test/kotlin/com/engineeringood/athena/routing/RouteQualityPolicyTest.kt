package com.engineeringood.athena.routing

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RouteQualityPolicyTest {
    @Test
    fun `valid route quality policy separates hard rejects from scored preferences`() {
        val compilation = assertIs<RouteQualityPolicyCompilation.Success>(
            RouteQualityPolicyCompiler().compile(RouteQualityPolicy.standardProfessional()),
        )
        val policy = compilation.policy

        assertEquals(
            setOf(
                "missing-anchor",
                "semantic-incompatibility",
                "route-body-intersection",
                "invalid-channel",
                "clearance-violation",
            ),
            policy.hardRejects.map { reject -> reject.criterion }.toSet(),
        )
        assertEquals(
            setOf("crossings", "bends", "lane-changes", "label-collisions", "length", "density", "stable-tie-breaker"),
            policy.scoringRules.map { rule -> rule.criterion }.toSet(),
        )
    }

    @Test
    fun `route quality policy rejects unknown criteria invalid weights and disabled hard rejects`() {
        val policy = RouteQualityPolicy(
            policyId = RouteQualityPolicyId("bad-policy"),
            hardRejects = listOf(
                RouteQualityHardReject("missing-anchor", enabled = false),
                RouteQualityHardReject("unknown-hard-reject"),
            ),
            scoringRules = listOf(
                RouteQualityScoringRule("crossings", weight = -1),
                RouteQualityScoringRule("unknown-soft", weight = 3),
            ),
        )

        val diagnostics = assertIs<RouteQualityPolicyCompilation.Failure>(
            RouteQualityPolicyCompiler().compile(policy),
        ).diagnostics.map { diagnostic -> diagnostic.code }

        assertContains(diagnostics, "route.quality.hard-reject.disabled")
        assertContains(diagnostics, "route.quality.hard-reject.unknown")
        assertContains(diagnostics, "route.quality.scoring.weight.invalid")
        assertContains(diagnostics, "route.quality.scoring.criterion.unknown")
    }

    @Test
    fun `route quality scoring uses route metrics deterministically`() {
        val policy = assertIs<RouteQualityPolicyCompilation.Success>(
            RouteQualityPolicyCompiler().compile(RouteQualityPolicy.standardProfessional()),
        ).policy
        val metrics = RouteQualityMetrics(
            crossingCount = 2,
            bendCount = 3,
            length = 100,
            channelChangeCount = 1,
            bundleContinuityPenalty = 4,
            labelClearanceViolationCount = 1,
        )

        val score = RouteQualityPolicyCompiler().score(metrics, policy)

        assertTrue(score.totalPenalty > 0)
        assertEquals(
            listOf("bends", "crossings", "density", "label-collisions", "lane-changes", "length", "stable-tie-breaker"),
            score.components.map { component -> component.criterion },
        )
    }
}
