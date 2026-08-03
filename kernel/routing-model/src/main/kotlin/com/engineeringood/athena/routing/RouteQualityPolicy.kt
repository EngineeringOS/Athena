package com.engineeringood.athena.routing

@JvmInline
value class RouteQualityPolicyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route quality policy id must not be blank." }
    }
}

data class RouteQualityHardReject(
    val criterion: String,
    val enabled: Boolean = true,
) {
    init {
        require(criterion.isNotBlank()) { "Route quality hard reject criterion must not be blank." }
    }
}

data class RouteQualityScoringRule(
    val criterion: String,
    val weight: Int,
) {
    init {
        require(criterion.isNotBlank()) { "Route quality scoring criterion must not be blank." }
    }
}

data class RouteQualityPolicy(
    val policyId: RouteQualityPolicyId,
    val hardRejects: List<RouteQualityHardReject>,
    val scoringRules: List<RouteQualityScoringRule>,
) {
    companion object {
        fun standardProfessional(): RouteQualityPolicy = RouteQualityPolicy(
            policyId = RouteQualityPolicyId("standard-professional-routing"),
            hardRejects = RouteQualityPolicyCompiler.requiredHardRejects.map { criterion ->
                RouteQualityHardReject(criterion)
            },
            scoringRules = listOf(
                RouteQualityScoringRule("crossings", 10),
                RouteQualityScoringRule("bends", 4),
                RouteQualityScoringRule("lane-changes", 5),
                RouteQualityScoringRule("label-collisions", 20),
                RouteQualityScoringRule("length", 1),
                RouteQualityScoringRule("density", 3),
                RouteQualityScoringRule("stable-tie-breaker", 1),
            ),
        )
    }
}

data class RouteQualityPolicyDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

sealed interface RouteQualityPolicyCompilation {
    data class Success(val policy: RouteQualityPolicy) : RouteQualityPolicyCompilation
    data class Failure(val diagnostics: List<RouteQualityPolicyDiagnostic>) : RouteQualityPolicyCompilation
}

data class RouteQualityScoreComponent(
    val criterion: String,
    val rawValue: Int,
    val weight: Int,
    val penalty: Int,
)

data class RouteQualityScore(
    val components: List<RouteQualityScoreComponent>,
) {
    val totalPenalty: Int = components.sumOf { component -> component.penalty }
}

class RouteQualityPolicyCompiler {
    fun compile(policy: RouteQualityPolicy): RouteQualityPolicyCompilation {
        val diagnostics = mutableListOf<RouteQualityPolicyDiagnostic>()
        val hardRejectsByCriterion = policy.hardRejects.groupBy { reject -> reject.criterion }
        requiredHardRejects.forEach { criterion ->
            if (hardRejectsByCriterion[criterion].isNullOrEmpty()) {
                diagnostics += diagnostic(
                    "route.quality.hard-reject.missing",
                    criterion,
                    "Route quality policy must include hard reject '$criterion'.",
                )
            }
        }
        policy.hardRejects.forEach { reject ->
            if (reject.criterion !in requiredHardRejects) {
                diagnostics += diagnostic(
                    "route.quality.hard-reject.unknown",
                    reject.criterion,
                    "Route quality hard reject '${reject.criterion}' is not compiler-owned.",
                )
            }
            if (!reject.enabled) {
                diagnostics += diagnostic(
                    "route.quality.hard-reject.disabled",
                    reject.criterion,
                    "Route quality hard reject '${reject.criterion}' cannot be disabled.",
                )
            }
        }
        policy.scoringRules.forEach { rule ->
            if (rule.criterion !in supportedScoringCriteria) {
                diagnostics += diagnostic(
                    "route.quality.scoring.criterion.unknown",
                    rule.criterion,
                    "Route quality scoring criterion '${rule.criterion}' is not supported.",
                )
            }
            if (rule.weight !in 0..100) {
                diagnostics += diagnostic(
                    "route.quality.scoring.weight.invalid",
                    rule.criterion,
                    "Route quality scoring weight must be between 0 and 100.",
                )
            }
        }
        return if (diagnostics.isEmpty()) {
            RouteQualityPolicyCompilation.Success(policy)
        } else {
            RouteQualityPolicyCompilation.Failure(diagnostics.sortedWith(compareBy({ it.code }, { it.subject })))
        }
    }

    fun score(
        metrics: RouteQualityMetrics,
        policy: RouteQualityPolicy,
    ): RouteQualityScore {
        val validPolicy = when (val compilation = compile(policy)) {
            is RouteQualityPolicyCompilation.Success -> compilation.policy
            is RouteQualityPolicyCompilation.Failure -> {
                error("Cannot score route metrics with invalid route quality policy: ${compilation.diagnostics}")
            }
        }
        val components = validPolicy.scoringRules
            .sortedBy { rule -> rule.criterion }
            .map { rule ->
                val raw = metrics.rawValue(rule.criterion)
                RouteQualityScoreComponent(
                    criterion = rule.criterion,
                    rawValue = raw,
                    weight = rule.weight,
                    penalty = raw * rule.weight,
                )
            }
        return RouteQualityScore(components)
    }

    private fun RouteQualityMetrics.rawValue(criterion: String): Int = when (criterion) {
        "crossings" -> crossingCount
        "bends" -> bendCount
        "lane-changes" -> channelChangeCount
        "label-collisions" -> labelClearanceViolationCount
        "length" -> length
        "density" -> bundleContinuityPenalty
        "stable-tie-breaker" -> 1
        else -> 0
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): RouteQualityPolicyDiagnostic = RouteQualityPolicyDiagnostic(code, subject, message)

    companion object {
        val requiredHardRejects: Set<String> = setOf(
            "missing-anchor",
            "semantic-incompatibility",
            "route-body-intersection",
            "invalid-channel",
            "clearance-violation",
        )
        val supportedScoringCriteria: Set<String> = setOf(
            "crossings",
            "bends",
            "lane-changes",
            "label-collisions",
            "length",
            "density",
            "stable-tie-breaker",
        )
    }
}
