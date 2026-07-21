package com.engineeringood.athena.interaction

import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionEnvelopeTest {
    @Test
    fun `interaction envelope carries versioned product safe payload metadata`() {
        val envelope = InteractionEnvelope(
            requestId = "request:subjects",
            activeSourceUri = "file:///workspace/main.athena",
            activeSourceRevision = "rev-1",
            payloadKind = InteractionPayloadKind.SUBJECTS,
            payload = mapOf("count" to "2"),
            adapterMetadata = mapOf("widgetId" to "graph-view"),
        )

        assertEquals(InteractionEnvelope.M29_SCHEMA_VERSION, envelope.schemaVersion)
        assertEquals("file:///workspace/main.athena", envelope.activeSourceUri)
        assertEquals("rev-1", envelope.activeSourceRevision)
        assertEquals("2", envelope.payload["count"])
        assertEquals("graph-view", envelope.adapterMetadata["widgetId"])
    }

    @Test
    fun `interaction envelope rejects unsupported schema versions`() {
        val diagnostics = InteractionEnvelope.validateSchemaVersion("m29.interaction.v0")

        assertEquals(InteractionDiagnosticCode.TRANSPORT_UNSUPPORTED_VERSION, diagnostics.single().code)
        assertEquals(InteractionDiagnosticSeverity.ERROR, diagnostics.single().severity)
    }
}
