package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutIntentItem
import com.engineeringood.athena.layout.LayoutIntentSnapshot
import com.engineeringood.athena.layout.LayoutPriority
import com.engineeringood.athena.layout.SchematicLayoutRole
import com.engineeringood.athena.layout.SchematicLayoutZone
import com.engineeringood.athena.layout.engine.RuleBasedSchematicLayoutOptimizer
import com.engineeringood.athena.layout.engine.SchematicLayoutOptimizationInput
import com.engineeringood.athena.layout.engine.SchematicLayoutOptimizationResult

/**
 * Feeds compiler-owned M23 layout constraints into the existing governed schematic layout engine.
 */
class ProjectSemanticSchematicLayoutFactDeriver(
    private val constraintLowerer: ProjectSemanticLayoutConstraintLowerer = ProjectSemanticLayoutConstraintLowerer(),
    private val optimizer: RuleBasedSchematicLayoutOptimizer = RuleBasedSchematicLayoutOptimizer(),
) {
    fun derive(snapshot: ProjectSemanticGraphSnapshot): SchematicLayoutOptimizationResult {
        val constraints = constraints(snapshot)
        val intent = intentSnapshot(constraints)
        return optimizer.optimize(
            SchematicLayoutOptimizationInput(
                intentSnapshot = intent,
                constraintSnapshot = constraints,
            ),
        )
    }

    fun constraints(snapshot: ProjectSemanticGraphSnapshot): LayoutConstraintSnapshot = constraintLowerer.lower(snapshot)

    private fun intentSnapshot(constraints: LayoutConstraintSnapshot): LayoutIntentSnapshot {
        val subjects = constraints.constraints
            .flatMap { constraint -> listOfNotNull(constraint.subject, constraint.target) }
            .distinctBy { subject -> subject.intentId }
            .map { subject ->
                val role = roleFor(subject.intentId.value)
                LayoutIntentItem(
                    intentId = subject.intentId,
                    subjectId = subject.subjectId,
                    occurrenceId = subject.occurrenceId,
                    role = role,
                    preferredZone = zoneFor(role),
                    priority = LayoutPriority.NORMAL,
                    sourceSpan = subject.sourceSpan,
                )
            }
        return LayoutIntentSnapshot.canonical(
            snapshotId = constraints.snapshotId,
            family = ElectricalProjectionFamily.SCHEMATIC,
            items = subjects,
        )
    }

    private fun roleFor(intentId: String): SchematicLayoutRole {
        val name = intentId.substringAfterLast(':').uppercase()
        return when {
            name.startsWith("PLC") -> SchematicLayoutRole.CONTROLLER
            name.startsWith("HMI") -> SchematicLayoutRole.HMI
            name.startsWith("XT") -> SchematicLayoutRole.TERMINAL
            name.startsWith("QF") -> SchematicLayoutRole.PROTECTION
            name.startsWith("M") -> SchematicLayoutRole.LOAD
            else -> SchematicLayoutRole.ANNOTATION
        }
    }

    private fun zoneFor(role: SchematicLayoutRole): SchematicLayoutZone {
        return when (role) {
            SchematicLayoutRole.POWER_SOURCE,
            SchematicLayoutRole.PROTECTION,
                -> SchematicLayoutZone.POWER
            SchematicLayoutRole.CONTROLLER,
            SchematicLayoutRole.HMI,
                -> SchematicLayoutZone.CONTROL
            SchematicLayoutRole.TERMINAL -> SchematicLayoutZone.TERMINAL
            SchematicLayoutRole.LOAD -> SchematicLayoutZone.LOAD
            SchematicLayoutRole.CONDUCTOR,
            SchematicLayoutRole.ANNOTATION,
                -> SchematicLayoutZone.ANNOTATION
        }
    }
}
