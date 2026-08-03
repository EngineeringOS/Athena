package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialReality

class ProjectionSpatialCompiler(
    private val layout: ProjectionSpatialLayout = ProjectionSpatialLayout(),
    private val geometryCompiler: SpatialGeometryCompiler = SpatialGeometryCompiler(),
    private val routeCompiler: SpatialRouteCompiler = SpatialRouteCompiler(),
    private val qualityCompiler: SpatialQualityCompiler = SpatialQualityCompiler(),
) : RealityTransformation<ProjectionDocument, SpatialDocument> {
    override fun transform(input: ProjectionDocument): RealityTransformationResult<SpatialDocument> {
        val projectionValidation = ProjectionReality.validate(input)
        if (!projectionValidation.isValid) {
            return projectionValidation.issues.toTransformationFailure()
        }

        val layoutResult = layout.place(input)
        if (layoutResult.diagnostics.isNotEmpty()) {
            return layoutResult.diagnostics.toSpatialTransformationFailure()
        }
        val geometryResult = geometryCompiler.compile(input, layoutResult.occurrences)
        if (geometryResult.diagnostics.isNotEmpty()) {
            return geometryResult.diagnostics.toSpatialTransformationFailure()
        }
        val routeResult = routeCompiler.compile(input, layoutResult.occurrences)
        if (routeResult.diagnostics.isNotEmpty()) {
            return RealityTransformationResult.Failure(routeResult.diagnostics)
        }

        val output = SpatialDocument(
            occurrences = layoutResult.occurrences,
            regions = geometryResult.regions,
            constructs = geometryResult.constructs,
            anchorPositions = routeResult.anchorPositions,
            alignments = geometryResult.alignments,
            lanes = routeResult.lanes,
            routes = routeResult.routes,
            qualityMeasurements = qualityCompiler.measure(
                occurrences = layoutResult.occurrences,
                lanes = routeResult.lanes,
                routes = routeResult.routes,
            ),
        )
        val spatialValidation = SpatialReality.validate(output)
        if (!spatialValidation.isValid) {
            return spatialValidation.issues.toTransformationFailure()
        }
        return RealityTransformationResult.Success(output)
    }
}

private fun List<SpatialDiagnostic>.toSpatialTransformationFailure(): RealityTransformationResult.Failure =
    RealityTransformationResult.Failure(
        map { diagnostic ->
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
