package com.engineeringood.athena.routing

/** Stable identity for one governed electrical connection. */
@JvmInline
value class ElectricalConnectionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Electrical connection id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one source-owned electrical port or terminal. */
@JvmInline
value class ElectricalPortId(val value: String) {
    init {
        require(value.isNotBlank()) { "Electrical port id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one routing policy used by a schematic projection. */
@JvmInline
value class RoutingPolicyId(val value: String) {
    init {
        require(value.isNotBlank()) { "Routing policy id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one occurrence-specific terminal anchor. */
@JvmInline
value class TerminalAnchorId(val value: String) {
    init {
        require(value.isNotBlank()) { "Terminal anchor id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one governed route constraint. */
@JvmInline
value class RouteConstraintId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route constraint id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identity for one ordered schematic route bundle. */
@JvmInline
value class RouteBundleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Route bundle id must not be blank." }
    }

    override fun toString(): String = value
}
