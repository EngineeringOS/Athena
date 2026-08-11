package com.engineeringood.athena.ide.lsp

internal fun ConnectionReadModelPublication.toWirePayload(): Map<String, Any?> = buildMap {
    put("schemaVersion", schemaVersion)
    put("state", state.name)
    put("attemptedInputRevision", attemptedInputRevision)
    acceptedInputRevision?.let { put("acceptedInputRevision", it) }
    connectionIrDigest?.let { put("connectionIrDigest", it) }
    put("items", items.map(ConnectionReadModelItem::toWirePayload))
    put("diagnostics", diagnostics.map(ConnectionReadModelDiagnostic::toWirePayload))
}

private fun ConnectionReadModelItem.toWirePayload(): Map<String, Any?> = buildMap {
    put("itemKind", itemKind.name)
    put("semanticId", semanticId)
    put("displayName", displayName)
    put("connectionKind", connectionKind)
    put("endpoints", endpoints.map(ConnectionReadModelEndpoint::toWirePayload))
    potentialOrSignal?.let { put("potentialOrSignal", it) }
    put("resolvedSpecifications", resolvedSpecifications.map { specification ->
        mapOf("name" to specification.name, "value" to specification.value)
    })
    put("validation", mapOf(
        "state" to validation.state.name,
        "diagnostics" to validation.diagnostics.map(ConnectionReadModelDiagnostic::toWirePayload),
    ))
    put("placed", placed)
    put("projectionCount", projectionCount)
    put("projectionTraceIds", projectionTraceIds)
    put("sourceTrace", sourceTrace.toWirePayload())
}

private fun ConnectionReadModelEndpoint.toWirePayload(): Map<String, Any?> = mapOf(
    "portId" to portId,
    "authoredPath" to authoredPath,
    "role" to role,
    "sourceTrace" to sourceTrace.toWirePayload(),
)

private fun ConnectionReadModelSourceTrace.toWirePayload(): Map<String, Any?> = mapOf(
    "relativePath" to relativePath,
    "startLine" to startLine,
    "startCharacter" to startCharacter,
    "endLine" to endLine,
    "endCharacter" to endCharacter,
    "subjectId" to subjectId,
)

private fun ConnectionReadModelDiagnostic.toWirePayload(): Map<String, String> = mapOf(
    "subject" to subject,
    "problem" to problem,
    "correction" to correction,
    "code" to code,
)
