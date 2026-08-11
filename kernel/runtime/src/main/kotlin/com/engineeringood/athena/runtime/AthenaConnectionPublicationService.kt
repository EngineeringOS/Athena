package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.connection.ConnectionDocument

enum class ConnectionPublicationState { ACCEPTED, STALE, UNAVAILABLE }

data class ConnectionPublication(
    val state: ConnectionPublicationState,
    val document: ConnectionDocument?,
    val diagnostics: List<String>,
)

/** Retains last accepted Connection IR while rejecting invalid replacements. */
class AthenaConnectionPublicationService {
    private var accepted: ConnectionDocument? = null

    fun publish(compilation: CompilerCompilationResult): ConnectionPublication {
        val success = compilation as? CompilerCompilationSuccess
        val candidate = success?.connectionIr
        if (candidate != null && success.connectionIrDiagnostics.isEmpty()) {
            accepted = candidate
            return ConnectionPublication(ConnectionPublicationState.ACCEPTED, candidate, emptyList())
        }
        val diagnostics = when {
            success?.connectionIrDiagnostics?.isNotEmpty() == true -> success.connectionIrDiagnostics
            success?.semanticResult?.diagnostics?.isNotEmpty() == true -> success.semanticResult.diagnostics.map { it.message }
            else -> listOf("Connection IR is unavailable because compilation did not produce an accepted document.")
        }
        return if (accepted == null) {
            ConnectionPublication(ConnectionPublicationState.UNAVAILABLE, null, diagnostics)
        } else {
            ConnectionPublication(ConnectionPublicationState.STALE, accepted, diagnostics)
        }
    }

    fun current(): ConnectionDocument? = accepted
}
