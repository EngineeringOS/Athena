package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.knowledge.protocol.AthenaSchema
import com.engineeringood.athena.knowledge.protocol.CanonicalDocumentBytes
import java.nio.charset.StandardCharsets

object ValidationCanonicalJsonProtocol {
    fun encode(document: EngineeringValidationDocument): CanonicalDocumentBytes {
        val json = "{" +
            "\"\$schema\":\"${AthenaSchema.VALIDATION.id}\",\"schemaVersion\":1," +
            "\"state\":\"${document.state}\"," +
            "\"requirements\":[${document.requirements.sortedBy { it.id }.joinToString(",") { "{\"id\":\"${escape(it.id)}\",\"capability\":\"${escape(it.capability.toString())}\"}" }}]," +
            "\"satisfaction\":[${document.satisfaction.sortedBy { it.requirementId }.joinToString(",") { "{\"requirementId\":\"${escape(it.requirementId)}\",\"status\":\"${it.status}\"}" }}]," +
            "\"judgements\":[${document.judgements.sortedBy { it.id }.joinToString(",") { "{\"id\":\"${escape(it.id)}\",\"status\":\"${it.status}\",\"problem\":\"${escape(it.problem)}\"}" }}]" +
            "}"
        return CanonicalDocumentBytes(AthenaSchema.VALIDATION, json.toByteArray(StandardCharsets.UTF_8))
    }

    private fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
}
