package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionFact
import com.engineeringood.athena.connection.NetFact
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.ConnectionProjectionIdentityKind
import com.engineeringood.athena.projection.ProjectionDocument

/** Validates that every published projection points at an admitted canonical Connection IR fact. */
internal object ConnectionProjectionValidator {
    fun report(projection: ProjectionDocument, connectionIr: ConnectionDocument): List<ProjectionViewDiagnostic> =
        projection.connections.flatMap { projected ->
            val expected = when (projected.identityKind) {
                ConnectionProjectionIdentityKind.CONNECTION -> connectionIr.connections.singleOrNull { it.id == projected.semanticId }
                ConnectionProjectionIdentityKind.NET -> connectionIr.nets.singleOrNull { it.id == projected.semanticId }
            }
            when (expected) {
                null -> listOf(diagnostic(projection, projected, "does not resolve to an accepted Connection IR identity", "Reference one admitted Connection or Net identity."))
                else -> validateEndpoints(projection, projected, expected)
            }
        }

    private fun validateEndpoints(
        projection: ProjectionDocument,
        projected: ConnectionProjection,
        expected: Any,
    ): List<ProjectionViewDiagnostic> {
        val expectedEndpoints = when (expected) {
            is ConnectionFact -> expected.endpoints
            is NetFact -> expected.endpoints
            else -> error("Unsupported Connection IR fact")
        }
        val actual = projected.participants.map { it.endpoint.occurrencePortId.portId }
        val expectedIds = expectedEndpoints.map { it.portId }
        if (actual.toSet() != expectedIds.toSet() || actual.size != expectedIds.size) {
            return listOf(
                diagnostic(
                    projection,
                    projected,
                    "selects occurrence Ports that do not match the accepted Connection IR endpoints",
                    "Select each occurrence Port from the referenced Connection or Net exactly once.",
                ),
            )
        }
        return emptyList()
    }

    private fun diagnostic(
        projection: ProjectionDocument,
        projected: ConnectionProjection,
        problem: String,
        correction: String,
    ) = ProjectionViewDiagnostic(
        view = projection.view.id,
        message = "Connection Projection '${projected.projectionId.value}' $problem. $correction",
    )
}
