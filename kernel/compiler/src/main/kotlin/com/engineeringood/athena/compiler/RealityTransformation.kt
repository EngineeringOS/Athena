package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialSourceTrace

interface RealityTransformation<InputReality, OutputReality> {
    fun transform(input: InputReality): RealityTransformationResult<OutputReality>
}

sealed interface RealityTransformationResult<out OutputReality> {
    data class Success<OutputReality>(
        val output: OutputReality,
    ) : RealityTransformationResult<OutputReality>

    data class Failure(
        val diagnostics: List<RealityTransformationDiagnostic>,
    ) : RealityTransformationResult<Nothing> {
        init {
            require(diagnostics.isNotEmpty()) { "Transformation failure requires diagnostics." }
        }
    }
}

data class RealityTransformationDiagnostic(
    val reality: String,
    val message: String,
    val subject: String? = null,
    val problem: String? = null,
    val correction: String? = null,
    val sourceTrace: SpatialSourceTrace? = null,
) {
    init {
        require(reality.isNotBlank()) { "Transformation diagnostic reality must not be blank." }
        require(message.isNotBlank()) { "Transformation diagnostic message must not be blank." }
        val structuredFields = listOf(subject, problem, correction, sourceTrace)
        require(structuredFields.all { field -> field == null } || structuredFields.all { field -> field != null }) {
            "Transformation diagnostic geometry details must be either complete or absent."
        }
        require(subject == null || subject.isNotBlank()) { "Transformation diagnostic subject must not be blank." }
        require(problem == null || problem.isNotBlank()) { "Transformation diagnostic problem must not be blank." }
        require(correction == null || correction.isNotBlank()) { "Transformation diagnostic correction must not be blank." }
    }
}

internal fun List<com.engineeringood.athena.ir.RealityValidationIssue>.toTransformationFailure():
    RealityTransformationResult.Failure =
    RealityTransformationResult.Failure(
        diagnostics = map { issue ->
            RealityTransformationDiagnostic(
                reality = issue.reality,
                message = issue.message,
            )
        },
    )
