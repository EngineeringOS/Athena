package com.engineeringood.athena.ide.lsp

import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionReadModelWirePayloadTest {
    @Test
    fun `wire payload publishes enum names instead of ordinals`() {
        val publication = ConnectionReadModelPublication(
            state = ConnectionReadModelPublicationState.READY,
            attemptedInputRevision = "input:sha256:${"1".repeat(64)}",
            acceptedInputRevision = "input:sha256:${"1".repeat(64)}",
            connectionIrDigest = "sha256:${"2".repeat(64)}",
            items = listOf(
                ConnectionReadModelItem(
                    itemKind = ConnectionReadModelItemKind.CONNECTION,
                    semanticId = "connection:test",
                    displayName = "A.out -> B.in",
                    connectionKind = "WIRE",
                    endpoints = emptyList(),
                    potentialOrSignal = null,
                    resolvedSpecifications = emptyList(),
                    validation = ConnectionReadModelValidation(ConnectionReadModelValidationState.VALID, emptyList()),
                    placed = true,
                    projectionCount = 1,
                    projectionTraceIds = listOf("trace:test"),
                    sourceTrace = ConnectionReadModelSourceTrace("src/test.athena", 1, 1, 1, 2, "connection:test"),
                ),
            ),
            diagnostics = emptyList(),
        )

        val payload = publication.toWirePayload()
        @Suppress("UNCHECKED_CAST")
        val item = (payload.getValue("items") as List<Map<String, Any?>>).single()
        @Suppress("UNCHECKED_CAST")
        val validation = item.getValue("validation") as Map<String, Any?>

        assertEquals("READY", payload["state"])
        assertEquals("CONNECTION", item["itemKind"])
        assertEquals("VALID", validation["state"])
    }
}
