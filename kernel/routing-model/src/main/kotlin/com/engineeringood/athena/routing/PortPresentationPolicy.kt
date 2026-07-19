package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId

/** Electrical role of a port or terminal before side and anchor selection. */
enum class ElectricalPortRole {
    INPUT,
    OUTPUT,
    POWER,
    GROUND,
    BIDIRECTIONAL,
    TERMINAL,
}

/** Preferred side for a route to enter or exit a terminal anchor. */
enum class TerminalSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

/** One policy-owned presentation rule for a port role. */
data class PortPresentationRule(
    val portRole: ElectricalPortRole,
    val preferredSide: TerminalSide,
)

/** Domain-owned policy that resolves preferred terminal sides for schematic routing. */
data class PortPresentationPolicy(
    val rules: List<PortPresentationRule>,
    val fallbackSide: TerminalSide = TerminalSide.RIGHT,
    val policySource: String = "m24:schematic-default",
) {
    init {
        val duplicateRoles = rules
            .groupingBy(PortPresentationRule::portRole)
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
        require(duplicateRoles.isEmpty()) {
            "Port presentation policy must not define duplicate role rules: $duplicateRoles"
        }
    }

    fun preferredSideFor(portRole: ElectricalPortRole): TerminalSide {
        return rules.firstOrNull { rule -> rule.portRole == portRole }?.preferredSide ?: fallbackSide
    }

    fun terminalAnchor(
        anchorId: TerminalAnchorId,
        subjectId: StableSemanticIdentity,
        occurrenceId: LayoutOccurrenceId,
        portId: ElectricalPortId,
        portRole: ElectricalPortRole,
        gridPoint: SchematicRoutePoint,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = anchorId,
            subjectId = subjectId,
            occurrenceId = occurrenceId,
            portId = portId,
            portRole = portRole,
            side = preferredSideFor(portRole),
            point = gridPoint,
            gridPoint = gridPoint,
            policySource = policySource,
        )
    }
}

/** Occurrence-specific anchor where a governed route attaches to a port or terminal. */
data class TerminalAnchorFact(
    val anchorId: TerminalAnchorId,
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val portId: ElectricalPortId,
    val portSemanticId: StableSemanticIdentity? = null,
    val portRole: ElectricalPortRole,
    val side: TerminalSide,
    val point: SchematicRoutePoint,
    val gridPoint: SchematicRoutePoint = point,
    val policySource: String = "m24:schematic-default",
)
