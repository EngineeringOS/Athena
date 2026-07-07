package com.engineeringood.athena.compiler

/** Stable identifiers for the explicit compiler passes that make up the current M3 pipeline. */
enum class CompilerPassId {
    PARSE,
    LOWER,
    SEMANTIC_ENRICHMENT,
    VALIDATE,
    BACKEND_PREPARATION,
    BACKEND_EMISSION,
}

/** Declared metadata for one compiler pass, including its responsibility and typed boundary. */
data class CompilerPassDescriptor(
    val id: CompilerPassId,
    val responsibility: String,
    val inputState: String,
    val outputState: String,
)

/** Execution status recorded for one compiler pass during a compilation run. */
enum class CompilerPassExecutionStatus {
    SUCCEEDED,
    FAILED,
    SKIPPED,
}

/** One pass execution record captured in the inspectable compiler pipeline report. */
data class CompilerPassRecord(
    val pass: CompilerPassDescriptor,
    val status: CompilerPassExecutionStatus,
    val outputSummary: String,
)

/** Ordered report of pass execution for one compiler compilation request. */
data class CompilerPipelineReport(
    val passes: List<CompilerPassRecord>,
)
