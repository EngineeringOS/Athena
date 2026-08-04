package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialReality
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
        val structuredFields = listOf(subject, problem, correction)
        require(structuredFields.all { field -> field == null } || structuredFields.all { field -> field != null }) {
            "Transformation diagnostic details must be either complete or absent."
        }
        require(sourceTrace == null || subject != null) {
            "Transformation diagnostic source trace requires structured details."
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

internal fun List<SpatialDiagnostic>.toSpatialTransformationFailure(): RealityTransformationResult.Failure =
    RealityTransformationResult.Failure(
        diagnostics = map { diagnostic ->
            RealityTransformationDiagnostic(
                reality = SpatialReality.name,
                message = "${diagnostic.subject}: ${diagnostic.problem} ${diagnostic.correction}",
                subject = diagnostic.subject,
                problem = diagnostic.problem,
                correction = diagnostic.correction,
                sourceTrace = diagnostic.sourceTrace,
            )
        },
    )
