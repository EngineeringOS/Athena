package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.RouteConstraint
import com.engineeringood.athena.routing.RouteConstraintId
import com.engineeringood.athena.routing.RouteConstraintKind
import com.engineeringood.athena.routing.RouteConstraintPriority

internal object ProfessionalDrawingRouteHardRules {
    fun constraintsFor(connection: EngineeringConnection): List<RouteConstraint> {
        val id = connection.id.value
        val connectionId = ElectricalConnectionId(id)
        return listOf(
            required(id, connectionId, "orthogonal", RouteConstraintKind.ORTHOGONAL_ONLY),
            required(id, connectionId, "avoid-body", RouteConstraintKind.AVOID_COMPONENT_BODY),
            required(id, connectionId, "label-clearance", RouteConstraintKind.LABEL_CLEARANCE),
            required(id, connectionId, "crossing-policy", RouteConstraintKind.CROSSING_POLICY),
        )
    }

    private fun required(
        connectionIdValue: String,
        connectionId: ElectricalConnectionId,
        ruleId: String,
        kind: RouteConstraintKind,
    ): RouteConstraint = RouteConstraint(
        constraintId = RouteConstraintId("constraint:$connectionIdValue:$ruleId"),
        kind = kind,
        connectionId = connectionId,
        priority = RouteConstraintPriority.REQUIRED,
        description = "Compiler-owned professional drawing hard rule: ${kind.name.lowercase().replace('_', '-')}.",
    )
}
