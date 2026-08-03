package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.projection.ProjectionDocument

/**
 * Engineering -> Projection reality transformation for authored M40 views (Story 3.1).
 *
 * One thin typed transformation: accepts Engineering Reality, emits Projection Reality only.
 */
class AuthoredProjectionTransformation : RealityTransformation<EngineeringDocument, ProjectionDocument> {
    override fun transform(input: EngineeringDocument): RealityTransformationResult<ProjectionDocument> {
        return when (val outcome = AuthoredProjectionViewCompiler.compile(input)) {
            is AuthoredProjectionCompilation.Success -> {
                if (outcome.documents.size != 1) {
                    RealityTransformationResult.Failure(
                        listOf(
                            RealityTransformationDiagnostic(
                                reality = "Projection Reality",
                                message = "Authored projection produced ${outcome.documents.size} views; the typed transformation expects exactly one view per Engineering snapshot.",
                            ),
                        ),
                    )
                } else {
                    RealityTransformationResult.Success(outcome.documents.single())
                }
            }

            is AuthoredProjectionCompilation.Failure -> RealityTransformationResult.Failure(
                outcome.diagnostics.map { diagnostic ->
                    RealityTransformationDiagnostic(
                        reality = "Projection Reality",
                        message = diagnostic.message,
                    )
                },
            )
        }
    }
}
