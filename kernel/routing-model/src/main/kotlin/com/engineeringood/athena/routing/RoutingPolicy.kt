package com.engineeringood.athena.routing

/** Schematic routing policy consumed by route intent and route solving. */
data class RoutingPolicy(
    val policyId: RoutingPolicyId,
    val orthogonalOnly: Boolean = true,
    val gridSize: Int = 20,
    val defaultLaneSpacing: Int = 40,
    val portPresentationPolicy: PortPresentationPolicy,
) {
    init {
        require(gridSize > 0) { "Routing policy grid size must be positive." }
        require(defaultLaneSpacing > 0) { "Routing policy lane spacing must be positive." }
    }

    fun preferredSideFor(portRole: ElectricalPortRole): TerminalSide {
        return portPresentationPolicy.preferredSideFor(portRole)
    }
}
