package com.engineeringood.athena.plugin

/** Stable compiler-stage vocabulary that plugins may target when declaring contribution intent. */
enum class AthenaCompilerContributionStage {
    LOWER,
    SEMANTIC_ENRICHMENT,
    VALIDATE,
    BACKEND_PREPARATION,
    BACKEND_EMISSION,
}

/** Inspectable validation contribution declaration published by one plugin through the stable SPI. */
data class AthenaValidationContribution(
    val contributionId: String,
    val displayName: String,
    val description: String = "",
)

/** Inspectable compiler-stage contribution declaration published by one plugin through the stable SPI. */
data class AthenaCompilerPassContribution(
    val contributionId: String,
    val stage: AthenaCompilerContributionStage,
    val displayName: String,
    val description: String = "",
)

/** Inspectable renderer-facing contribution declaration published by one plugin through the stable SPI. */
data class AthenaRenderContribution(
    val contributionId: String,
    val displayName: String,
    val description: String = "",
    val viewIds: Set<String> = emptySet(),
    val rendererTargets: Set<String> = emptySet(),
)
