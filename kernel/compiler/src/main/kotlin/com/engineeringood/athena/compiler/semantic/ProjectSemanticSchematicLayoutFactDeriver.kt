package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutConstraintKind
import com.engineeringood.athena.layout.LayoutIntentItem
import com.engineeringood.athena.layout.LayoutIntentSnapshot
import com.engineeringood.athena.layout.LayoutPriority
import com.engineeringood.athena.layout.SchematicLayoutRole
import com.engineeringood.athena.layout.SchematicLayoutZone
import com.engineeringood.athena.layout.engine.RuleBasedSchematicLayoutOptimizer
import com.engineeringood.athena.layout.engine.SchematicLayoutOptimizationInput
import com.engineeringood.athena.layout.engine.SchematicLayoutOptimizationResult
import java.util.Locale

/**
 * Feeds compiler-owned M23 layout constraints into the existing governed schematic layout engine.
 */
class ProjectSemanticSchematicLayoutFactDeriver(
    private val constraintLowerer: ProjectSemanticLayoutConstraintLowerer = ProjectSemanticLayoutConstraintLowerer(),
    private val optimizer: RuleBasedSchematicLayoutOptimizer = RuleBasedSchematicLayoutOptimizer(),
) {
    fun derive(snapshot: ProjectSemanticGraphSnapshot): SchematicLayoutOptimizationResult {
        val constraints = constraints(snapshot)
        val intent = intentSnapshot(snapshot, constraints)
        val solved = optimizer.optimize(
            SchematicLayoutOptimizationInput(
                intentSnapshot = intent,
                constraintSnapshot = constraints,
            ),
        )
        val fixedByIntent = constraints.constraints
            .filter { constraint -> constraint.kind == LayoutConstraintKind.AT_GRID }
            .associateBy { constraint -> constraint.subject.intentId }
        return solved.copy(
            placementFacts = solved.placementFacts.map { fact ->
                val fixed = fixedByIntent[fact.intentId]
                fact.copy(
                    functionId = fixed?.subject?.functionId,
                    gridPosition = fixed?.gridPosition,
                    orientation = fixed?.orientation,
                )
            },
        )
    }

    fun constraints(snapshot: ProjectSemanticGraphSnapshot): LayoutConstraintSnapshot =
        constraintLowerer.lower(snapshot)

    private fun intentSnapshot(
        snapshot: ProjectSemanticGraphSnapshot,
        constraints: LayoutConstraintSnapshot,
    ): LayoutIntentSnapshot {
        val authoredTypesBySubjectId = authoredTypesBySubjectId(snapshot)
        val subjects = constraints.constraints
            .flatMap { constraint -> listOfNotNull(constraint.subject, constraint.target) }
            .distinctBy { subject -> subject.intentId }
            .map { subject ->
                val role = roleFor(authoredTypesBySubjectId[subject.subjectId.value])
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

    private fun authoredTypesBySubjectId(snapshot: ProjectSemanticGraphSnapshot): Map<String, String> {
        return snapshot.sourceUnits.flatMap { sourceUnit ->
            sourceUnit.authoredDeclarations
                .filterIsInstance<DeviceDeclaration>()
                .mapNotNull { declaration ->
                    val authoredType = declaration.fields
                        .firstOrNull { field -> field.name == "type" }
                        ?.value
                        ?.authoredText()
                        ?: return@mapNotNull null
                    val declarationId = CanonicalSemanticIdentityBuilder.declarationId(
                        sourceUnit.sourceUnitId,
                        "device",
                        listOf(declaration.name),
                    )
                    declarationId.value to authoredType
                }
        }.toMap()
    }

    private fun roleFor(authoredType: String?): SchematicLayoutRole {
        return when (authoredType?.lowercase(Locale.ROOT)) {
            "powersupply", "power-source", "source" -> SchematicLayoutRole.POWER_SOURCE
            "protectivedevice", "protection", "breaker", "fuse", "fusedisconnector" ->
                SchematicLayoutRole.PROTECTION
            "controller", "plc", "relaycontroller" -> SchematicLayoutRole.CONTROLLER
            "hmi", "operatorinterface" -> SchematicLayoutRole.HMI
            "terminal", "terminalblock" -> SchematicLayoutRole.TERMINAL
            "motor", "lamp", "load" -> SchematicLayoutRole.LOAD
            else -> SchematicLayoutRole.ANNOTATION
        }
    }

    private fun ScalarValue.authoredText(): String = when (this) {
        is ScalarValue.Identifier -> text
        is ScalarValue.StringLiteral -> text
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
