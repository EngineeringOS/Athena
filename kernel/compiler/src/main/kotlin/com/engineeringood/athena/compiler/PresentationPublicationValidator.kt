package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationDocument

data class PresentationPublicationIssue(
    val code: String,
    val subject: String,
    val message: String,
) {
    init {
        require(code.isNotBlank()) { "Presentation publication issue code must not be blank." }
        require(subject.isNotBlank()) { "Presentation publication issue subject must not be blank." }
        require(message.isNotBlank()) { "Presentation publication issue message must not be blank." }
    }
}

object PresentationPublicationValidator {
    fun validate(document: PresentationDocument): List<PresentationPublicationIssue> {
        val issues = mutableListOf<PresentationPublicationIssue>()
        val connectorsById = document.connectors.groupBy { connector -> connector.occurrenceId.value }
        val markersById = document.connectionMarkers.groupBy { marker -> marker.markerId.value }

        connectorsById.filterValues { connectors -> connectors.size > 1 }.forEach { (connectorId, _) ->
            issues += issue("presentation.publication.connector.duplicate", connectorId, "Presentation document has duplicate Connector ids.")
        }
        markersById.filterValues { markers -> markers.size > 1 }.forEach { (markerId, _) ->
            issues += issue("presentation.publication.marker.duplicate", markerId, "Presentation document has duplicate Connection marker ids.")
        }

        document.connectors.forEach { connector ->
            connector.tokenOverrides.keys
                .filter { key -> key.contains("route", ignoreCase = true) || key.contains("stroke", ignoreCase = true) }
                .forEach { key ->
                    issues += issue(
                        "presentation.publication.token.required-fact",
                        connector.occurrenceId.value,
                        "Connector required fact '$key' must be typed, not hidden in token overrides.",
                    )
                }
            if (connector.sourceProjectionIds.isEmpty()) {
                issues += issue("presentation.publication.connector.trace.missing", connector.occurrenceId.value, "Connector requires source projection trace.")
            }
            listOf("source" to connector.sourceEndpoint, "target" to connector.targetEndpoint).forEach { (role, endpoint) ->
                if (endpoint.sourceProvenance.isEmpty()) {
                    issues += issue(
                        "presentation.publication.endpoint.trace.missing",
                        "${connector.occurrenceId.value}:$role",
                        "Connector endpoint '$role' requires source trace. Add Athena source evidence to the Port to Anchor binding.",
                    )
                }
                val missingIdentities = listOf(
                    "port" to endpoint.portSemanticId.value,
                    "binding" to endpoint.bindingId.value,
                    "occurrence" to endpoint.occurrenceId.value,
                    "anchor" to endpoint.anchorId.value,
                ).filter { (_, value) -> value.isBlank() }
                if (missingIdentities.isNotEmpty()) {
                    issues += issue(
                        "presentation.publication.endpoint.identity.missing",
                        "${connector.occurrenceId.value}:$role",
                        "Connector endpoint '$role' is missing ${missingIdentities.joinToString { it.first }} identity. Correct the Athena Port to Anchor binding.",
                    )
                }
            }
            connector.markerIds.forEach { markerId ->
                val marker = markersById[markerId.value]?.singleOrNull()
                if (marker == null) {
                    issues += issue("presentation.publication.marker.reference.missing", connector.occurrenceId.value, "Connector references missing marker '${markerId.value}'.")
                } else if (connector.occurrenceId !in marker.connectorIds) {
                    issues += issue("presentation.publication.marker.reference.conflict", connector.occurrenceId.value, "Connector marker reference is not bidirectional for '${markerId.value}'.")
                }
            }
        }

        document.connectionMarkers.forEach { marker ->
            marker.connectorIds.forEach { connectorId ->
                val connector = connectorsById[connectorId.value]?.singleOrNull()
                if (connector == null) {
                    issues += issue("presentation.publication.marker.connector.missing", marker.markerId.value, "Connection marker references missing connector '${connectorId.value}'.")
                } else if (marker.markerId !in connector.markerIds) {
                    issues += issue("presentation.publication.marker.connector.conflict", marker.markerId.value, "Connection marker reference is not bidirectional for '${connectorId.value}'.")
                }
            }
        }

        return issues.sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
    }

    private fun issue(code: String, subject: String, message: String): PresentationPublicationIssue =
        PresentationPublicationIssue(code, subject.ifBlank { "unknown" }, message)
}
