package com.engineeringood.athena.ide.lsp

enum class ConnectionReadModelPublicationState { READY, STALE, UNAVAILABLE }

enum class ConnectionReadModelItemKind { CONNECTION, NET }

enum class ConnectionReadModelValidationState { VALID, WARNING }

data class AthenaConnectionReadModelTextDocument(val uri: String)

data class AthenaConnectionReadModelParams(
    val textDocument: AthenaConnectionReadModelTextDocument? = null,
)

data class ConnectionReadModelDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
)

data class ConnectionReadModelSourceTrace(
    val relativePath: String,
    val startLine: Int,
    val startCharacter: Int,
    val endLine: Int,
    val endCharacter: Int,
    val subjectId: String,
)

data class ConnectionReadModelEndpoint(
    val portId: String,
    val authoredPath: String,
    val role: String,
    val sourceTrace: ConnectionReadModelSourceTrace,
)

data class ConnectionReadModelSpecification(
    val name: String,
    val value: String,
)

data class ConnectionReadModelValidation(
    val state: ConnectionReadModelValidationState,
    val diagnostics: List<ConnectionReadModelDiagnostic>,
)

data class ConnectionReadModelItem(
    val itemKind: ConnectionReadModelItemKind,
    val semanticId: String,
    val displayName: String,
    val connectionKind: String,
    val endpoints: List<ConnectionReadModelEndpoint>,
    val potentialOrSignal: String?,
    val resolvedSpecifications: List<ConnectionReadModelSpecification>,
    val validation: ConnectionReadModelValidation,
    val placed: Boolean,
    val projectionCount: Int,
    val projectionTraceIds: List<String>,
    val sourceTrace: ConnectionReadModelSourceTrace,
)

data class ConnectionReadModelPublication(
    val schemaVersion: Int = 1,
    val state: ConnectionReadModelPublicationState,
    val attemptedInputRevision: String,
    val acceptedInputRevision: String?,
    val connectionIrDigest: String?,
    val items: List<ConnectionReadModelItem>,
    val diagnostics: List<ConnectionReadModelDiagnostic>,
)
