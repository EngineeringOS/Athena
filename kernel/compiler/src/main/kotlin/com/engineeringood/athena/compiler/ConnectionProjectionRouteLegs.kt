package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.ProjectionOccurrencePortId

/** One deterministic binary route leg derived from one binary Connection or multi-endpoint Net projection. */
internal data class ConnectionRouteLeg(
    val ordinal: Int,
    val source: ProjectionOccurrencePortId,
    val target: ProjectionOccurrencePortId,
) {
    val routeProjectionIdSuffix: String
        get() = if (ordinal == 0) "" else "/leg/${ordinal.toString().padStart(3, '0')}"
}

/** Splits a multi-endpoint Net into binary geometry legs without changing its semantic identity. */
internal fun ConnectionProjection.routeLegs(): List<ConnectionRouteLeg> {
    val ordered = participants.sortedBy {
        "${it.endpoint.occurrencePortId.occurrenceId.value}:${it.endpoint.occurrencePortId.portId.value}"
    }
    if (ordered.size == 2) {
        return listOf(ConnectionRouteLeg(0, ordered[0].endpoint.occurrencePortId, ordered[1].endpoint.occurrencePortId))
    }
    val sources = ordered.filter { it.role == ConnectionEndpointRole.SOURCE }
    val sinks = ordered.filter { it.role == ConnectionEndpointRole.SINK }
    return when {
        sources.size == 1 -> ordered.filterNot { it == sources.single() }.mapIndexed { index, participant ->
            ConnectionRouteLeg(index + 1, sources.single().endpoint.occurrencePortId, participant.endpoint.occurrencePortId)
        }
        sinks.size == 1 -> ordered.filterNot { it == sinks.single() }.mapIndexed { index, participant ->
            ConnectionRouteLeg(index + 1, participant.endpoint.occurrencePortId, sinks.single().endpoint.occurrencePortId)
        }
        else -> emptyList()
    }
}
