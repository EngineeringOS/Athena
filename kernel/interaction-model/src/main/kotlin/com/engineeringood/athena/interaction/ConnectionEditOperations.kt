package com.engineeringood.athena.interaction

import java.math.BigDecimal

private val PORT_IDENTITY = Regex("^port:[A-Za-z][A-Za-z0-9_-]*(?:\\.[A-Za-z0-9][A-Za-z0-9_-]*)+$")
private val CONNECTION_IDENTITY = Regex("^connection:[A-Za-z0-9._:/>-]+$")
private val PROJECTION_IDENTITY = Regex("^[A-Za-z][A-Za-z0-9._:/>-]+$")
private val REQUIREMENT_SYMBOL = Regex("^[A-Za-z][A-Za-z0-9._-]*$")
private val UNIT_SYMBOL = Regex("^[A-Za-z][A-Za-z0-9._*/^-]*$")
private val EXACT_DECIMAL = Regex("^(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?$")

/** Closed connection kinds admitted by engineering edit intent. */
enum class ConnectionEditKind { CONDUCTOR, WIRE, CABLE_CORE, JUMPER, BUSBAR, SIGNAL }

/** Binary Connection endpoint role. PASS belongs to Net editing, not Connect/Reconnect. */
enum class ConnectionEditEndpointRole { SOURCE, SINK }

data class ConnectionEditEndpoint(
    val role: ConnectionEditEndpointRole,
    val portId: String,
) {
    init {
        require(PORT_IDENTITY.matches(portId)) { "Connection endpoint requires a canonical Port identity." }
    }
}

enum class ConnectionRequirementKind {
    CROSS_SECTION,
    COLOR_CODE,
    CONDUCTOR_TYPE,
    SHIELDING,
    SOURCE_TERMINATION,
    TARGET_TERMINATION,
    REQUIRED_LENGTH,
}

sealed interface ConnectionRequirementValue {
    data class Quantity(val value: String, val unit: String) : ConnectionRequirementValue {
        init {
            require(EXACT_DECIMAL.matches(value) && value.toBigDecimal() > BigDecimal.ZERO) {
                "Connection quantity requires a positive exact decimal value."
            }
            require(UNIT_SYMBOL.matches(unit)) { "Connection quantity requires a unit symbol." }
        }
    }

    data class Symbol(val value: String) : ConnectionRequirementValue {
        init { require(REQUIREMENT_SYMBOL.matches(value)) { "Connection requirement Symbol value is malformed." } }
    }

    data class Boolean(val value: kotlin.Boolean) : ConnectionRequirementValue
}

data class ConnectionRequirementIntent(
    val kind: ConnectionRequirementKind,
    val value: ConnectionRequirementValue,
) {
    init {
        val valid = when (kind) {
            ConnectionRequirementKind.CROSS_SECTION,
            ConnectionRequirementKind.REQUIRED_LENGTH -> value is ConnectionRequirementValue.Quantity

            ConnectionRequirementKind.SHIELDING -> value is ConnectionRequirementValue.Boolean
            ConnectionRequirementKind.COLOR_CODE,
            ConnectionRequirementKind.CONDUCTOR_TYPE,
            ConnectionRequirementKind.SOURCE_TERMINATION,
            ConnectionRequirementKind.TARGET_TERMINATION -> value is ConnectionRequirementValue.Symbol
        }
        require(valid) { "Connection requirement `${kind.name}` has the wrong typed value." }
    }
}

data class ConnectPorts(
    val connectionKind: ConnectionEditKind,
    val endpoints: List<ConnectionEditEndpoint>,
    val requirements: List<ConnectionRequirementIntent>,
) : EditOperationBody {
    override val kind = EditOperationKind.CONNECT_PORTS

    init {
        require(endpoints.map(ConnectionEditEndpoint::role) == listOf(ConnectionEditEndpointRole.SOURCE, ConnectionEditEndpointRole.SINK)) {
            "Connect requires canonical SOURCE then SINK endpoints."
        }
        require(endpoints.map(ConnectionEditEndpoint::portId).distinct().size == endpoints.size) {
            "Connect requires two distinct canonical Port identities."
        }
        require(requirements == requirements.sortedBy { it.kind.ordinal }) {
            "Connection requirements must be in canonical kind order."
        }
        require(requirements.map(ConnectionRequirementIntent::kind).distinct().size == requirements.size) {
            "Connection requirements must not repeat a kind."
        }
        if (connectionKind == ConnectionEditKind.WIRE || connectionKind == ConnectionEditKind.CABLE_CORE) {
            require(requirements.any { it.kind == ConnectionRequirementKind.CROSS_SECTION }) {
                "${connectionKind.name} Connect intent requires CROSS_SECTION."
            }
        }
    }
}

data class ReconnectConnectionEndpoint(
    val connectionId: String,
    val endpointRole: ConnectionEditEndpointRole,
    val replacementPortId: String,
) : EditOperationBody {
    override val kind = EditOperationKind.RECONNECT_CONNECTION_ENDPOINT

    init {
        require(CONNECTION_IDENTITY.matches(connectionId)) { "Reconnect requires a canonical Connection identity." }
        require(PORT_IDENTITY.matches(replacementPortId)) { "Reconnect requires a canonical replacement Port identity." }
    }
}

enum class LogicalRouteTargetKind { SEGMENT, BEND }
data class LogicalRouteTarget(
    val kind: LogicalRouteTargetKind,
    val ordinal: Int,
) {
    init { require(ordinal > 0) { "Logical route target ordinal must be positive." } }
}

data class LogicalRoutePoint(
    val column: Int,
    val row: Int,
) {
    init { require(column > 0 && row > 0) { "Logical route point requires positive column and row." } }
}

data class AdjustConnectionRoute(
    val sheetId: String,
    val connectionId: String,
    val projectionId: String,
    val target: LogicalRouteTarget,
    val point: LogicalRoutePoint,
) : EditOperationBody {
    override val kind = EditOperationKind.ADJUST_CONNECTION_ROUTE

    init {
        require(sheetId.isNotBlank()) { "Route adjustment requires a Sheet identity." }
        require(CONNECTION_IDENTITY.matches(connectionId)) { "Route adjustment requires a canonical Connection identity." }
        require(PROJECTION_IDENTITY.matches(projectionId)) { "Route adjustment requires a stable projection identity." }
    }
}
