package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionEndpointFact
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.runtime.AthenaConnectionPublicationService
import com.engineeringood.athena.runtime.ConnectionPublicationState
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import java.nio.file.Path

/** Session-scoped read projection over accepted Connection IR. */
class ConnectionReadModelService(
    private val repositoryRoot: Path,
    private val connectionPublicationService: AthenaConnectionPublicationService = AthenaConnectionPublicationService(),
) {
    private val acceptedByInputRevision = linkedMapOf<String, ConnectionReadModelPublication>()
    private var acceptedInputRevision: String? = null

    @Synchronized
    fun publish(
        attemptedInputRevision: InputRevision,
        compilation: CompilerCompilationResult,
    ): ConnectionReadModelPublication {
        val connectionPublication = connectionPublicationService.publish(compilation)
        return when (connectionPublication.state) {
            ConnectionPublicationState.ACCEPTED -> {
                val success = compilation as CompilerCompilationSuccess
                val document = requireNotNull(connectionPublication.document)
                val ready = ConnectionReadModelPublication(
                    state = ConnectionReadModelPublicationState.READY,
                    attemptedInputRevision = attemptedInputRevision.value,
                    acceptedInputRevision = attemptedInputRevision.value,
                    connectionIrDigest = document.digest.value,
                    items = ConnectionReadModelMapper.map(
                        repositoryRoot = repositoryRoot,
                        document = document,
                        compilation = success,
                    ),
                    diagnostics = emptyList(),
                )
                acceptedInputRevision = attemptedInputRevision.value
                acceptedByInputRevision[attemptedInputRevision.value] = ready
                ready
            }

            ConnectionPublicationState.STALE -> stale(attemptedInputRevision, connectionPublication.diagnostics)
            ConnectionPublicationState.UNAVAILABLE -> unavailable(attemptedInputRevision, connectionPublication.diagnostics)
        }
    }

    private fun stale(attemptedInputRevision: InputRevision, problems: List<String>): ConnectionReadModelPublication {
        val acceptedRevision = acceptedInputRevision
        val accepted = acceptedRevision?.let(acceptedByInputRevision::get)
        return if (accepted == null) {
            unavailable(attemptedInputRevision, problems)
        } else {
            accepted.copy(
                state = ConnectionReadModelPublicationState.STALE,
                attemptedInputRevision = attemptedInputRevision.value,
                diagnostics = problems.toReadModelDiagnostics(),
            )
        }
    }

    private fun unavailable(attemptedInputRevision: InputRevision, problems: List<String>) = ConnectionReadModelPublication(
        state = ConnectionReadModelPublicationState.UNAVAILABLE,
        attemptedInputRevision = attemptedInputRevision.value,
        acceptedInputRevision = null,
        connectionIrDigest = null,
        items = emptyList(),
        diagnostics = problems.toReadModelDiagnostics(),
    )
}

internal object ConnectionReadModelMapper {
    fun map(
        repositoryRoot: Path,
        document: ConnectionDocument,
        compilation: CompilerCompilationSuccess,
    ): List<ConnectionReadModelItem> {
        val routesByConnection = compilation.spatialDocuments
            .flatMap { spatial -> spatial.sheets }
            .flatMap { sheet -> sheet.routes }
            .groupBy { route -> route.connectionId.value }
        val diagnosticsBySubject = compilation.semanticResult.diagnostics
            .filter { diagnostic -> diagnostic.subjectIdentity != null }
            .groupBy { diagnostic -> requireNotNull(diagnostic.subjectIdentity).value }

        val connections = document.connections.map { connection ->
            val routes = routesByConnection[connection.id.value].orEmpty()
            ConnectionReadModelItem(
                itemKind = ConnectionReadModelItemKind.CONNECTION,
                semanticId = connection.id.value,
                displayName = connection.endpoints.joinToString(" -> ") { endpoint -> endpoint.authoredPath.joinToString(".") },
                connectionKind = connection.kind.name,
                endpoints = connection.endpoints.map { endpoint -> endpoint.toReadModel(repositoryRoot) },
                potentialOrSignal = null,
                resolvedSpecifications = connection.specification.properties.map { property ->
                    ConnectionReadModelSpecification(property.name, property.value.render())
                },
                validation = validation(diagnosticsBySubject[connection.id.value].orEmpty()),
                placed = routes.isNotEmpty(),
                projectionCount = routes.size,
                projectionTraceIds = routes.flatMap { route -> route.sourceTrace.projectionIds }.distinct().sorted(),
                sourceTrace = connection.trace.provenance.toReadModel(repositoryRoot, connection.id.value),
            )
        }
        val nets = document.nets.map { net ->
            val routes = routesByConnection[net.id.value].orEmpty()
            ConnectionReadModelItem(
                itemKind = ConnectionReadModelItemKind.NET,
                semanticId = net.id.value,
                displayName = net.name,
                connectionKind = net.kind.name,
                endpoints = net.endpoints.map { endpoint -> endpoint.toReadModel(repositoryRoot) },
                potentialOrSignal = net.potentialOrSignal?.joinToString("."),
                resolvedSpecifications = net.specification.properties.map { property ->
                    ConnectionReadModelSpecification(property.name, property.value.render())
                },
                validation = validation(diagnosticsBySubject[net.id.value].orEmpty()),
                placed = routes.isNotEmpty(),
                projectionCount = routes.size,
                projectionTraceIds = routes.flatMap { route -> route.sourceTrace.projectionIds }.distinct().sorted(),
                sourceTrace = net.trace.provenance.toReadModel(repositoryRoot, net.id.value),
            )
        }
        return connections.sortedBy(ConnectionReadModelItem::semanticId) + nets.sortedBy(ConnectionReadModelItem::semanticId)
    }

    private fun ConnectionEndpointFact.toReadModel(repositoryRoot: Path) = ConnectionReadModelEndpoint(
        portId = portId.value,
        authoredPath = authoredPath.joinToString("."),
        role = role.name,
        sourceTrace = trace.provenance.toReadModel(repositoryRoot, portId.value),
    )

    private fun validation(diagnostics: List<SemanticDiagnostic>): ConnectionReadModelValidation {
        val warnings = diagnostics.filter { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.WARNING }
        return ConnectionReadModelValidation(
            state = if (warnings.isEmpty()) ConnectionReadModelValidationState.VALID else ConnectionReadModelValidationState.WARNING,
            diagnostics = warnings.map { diagnostic ->
                ConnectionReadModelDiagnostic(
                    subject = diagnostic.subjectIdentity?.value ?: "Connection",
                    problem = diagnostic.message,
                    correction = "Correct the named engineering fact in Athena source.",
                    code = diagnostic.ruleId.value,
                )
            },
        )
    }
}

private fun SourceProvenance.toReadModel(repositoryRoot: Path, subjectId: String): ConnectionReadModelSourceTrace {
    val sourcePath = runCatching { Path.of(file).toAbsolutePath().normalize() }.getOrNull()
    val normalizedRoot = repositoryRoot.toAbsolutePath().normalize()
    val relativePath = if (sourcePath != null && sourcePath.startsWith(normalizedRoot)) {
        normalizedRoot.relativize(sourcePath).toString()
    } else {
        file
    }.replace('\\', '/')
    return ConnectionReadModelSourceTrace(
        relativePath = relativePath,
        startLine = startLine,
        startCharacter = startColumn,
        endLine = endLine,
        endCharacter = endColumn,
        subjectId = subjectId,
    )
}

private fun EngineeringValue.render(): String = when (this) {
    is EngineeringValue.Quantity -> "${value} ${unit.authoredName.joinToString(".")}"
    is EngineeringValue.Integer -> value.toString()
    is EngineeringValue.Boolean -> value.toString()
    is EngineeringValue.Text -> text
    is EngineeringValue.Symbol -> text
    is EngineeringValue.Reference -> reference.authoredPath.joinToString(".")
}

private fun List<String>.toReadModelDiagnostics(): List<ConnectionReadModelDiagnostic> = distinct().sorted().map { problem ->
    ConnectionReadModelDiagnostic(
        subject = "Connection Navigator",
        problem = problem,
        correction = "Correct the authored connection facts, then refresh Connection Navigator.",
        code = "connection.read-model.unavailable",
    )
}
