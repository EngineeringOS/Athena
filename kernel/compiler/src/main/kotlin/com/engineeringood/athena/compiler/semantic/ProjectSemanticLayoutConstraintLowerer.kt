package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutAxis
import com.engineeringood.athena.layout.LayoutConstraint
import com.engineeringood.athena.layout.LayoutConstraintId
import com.engineeringood.athena.layout.LayoutConstraintSubject
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutIntentId
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.language.LayoutAxis as SyntaxLayoutAxis

/**
 * Lowers semantically bindable M23 layout hints into governed layout constraints.
 */
class ProjectSemanticLayoutConstraintLowerer {
    fun lower(snapshot: ProjectSemanticGraphSnapshot): LayoutConstraintSnapshot {
        val namespacesBySourceUnit = snapshot.namespaces
            .flatMap { namespace -> namespace.sourceUnitIds.map { sourceUnitId -> sourceUnitId to namespace } }
            .toMap()
        val declarationsByNamespaceAndName = snapshot.declarations
            .filter { declaration -> declaration.kind == "device" && declaration.qualifiedAuthoredName.size == 1 }
            .associateBy { declaration -> NamespaceNameKey(declaration.namespaceId, declaration.qualifiedAuthoredName.single()) }
        val constraints = mutableListOf<LayoutConstraint>()

        snapshot.sourceUnits
            .sortedBy { sourceUnit -> sourceUnit.sourceUnitId.value }
            .forEach { sourceUnit ->
                val namespace = namespacesBySourceUnit[sourceUnit.sourceUnitId] ?: return@forEach
                sourceUnit.authoredDeclarations
                    .filterIsInstance<LayoutDeclaration>()
                    .sortedBy { declaration -> declaration.span.start.offset }
                    .forEach { declaration ->
                        declaration.statements.forEach { statement ->
                            val subject = declarationsByNamespaceAndName[
                                NamespaceNameKey(namespace.namespaceId, statement.subject),
                            ] ?: return@forEach
                            val target = declarationsByNamespaceAndName[
                                NamespaceNameKey(namespace.namespaceId, statement.target),
                            ] ?: return@forEach
                            constraints += statement.toConstraint(
                                sourceUnitId = sourceUnit.sourceUnitId,
                                viewFamily = declaration.viewFamily,
                                subject = subject,
                                target = target,
                            )
                        }
                    }
            }

        return LayoutConstraintSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m23:layout-constraints:${snapshot.graphId.value}"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            constraints = constraints,
        )
    }

    private fun LayoutStatement.toConstraint(
        sourceUnitId: SourceUnitId,
        viewFamily: String,
        subject: ProjectSemanticDeclaration,
        target: ProjectSemanticDeclaration,
    ): LayoutConstraint {
        val subjectName = subject.qualifiedAuthoredName.single()
        val targetName = target.qualifiedAuthoredName.single()
        val subjectRef = subject.toConstraintSubject(sourceUnitId, viewFamily, span)
        val targetRef = target.toConstraintSubject(sourceUnitId, viewFamily, span)
        val constraintId = LayoutConstraintId(
            "constraint:m23:$viewFamily:${relationToken()}:$subjectName:$targetName:${span.start.offset}",
        )
        return when (this) {
            is LayoutStatement.PlaceNear -> LayoutConstraint.near(constraintId, subjectRef, targetRef)
            is LayoutStatement.PlaceBelow -> LayoutConstraint.below(constraintId, subjectRef, targetRef)
            is LayoutStatement.AlignWith -> LayoutConstraint.alignedWith(
                constraintId = constraintId,
                subject = subjectRef,
                target = targetRef,
                axis = axis.toConstraintAxis(),
            )
            is LayoutStatement.GroupWith -> LayoutConstraint.groupedWith(constraintId, subjectRef, targetRef)
        }.copy(authoredPriority = AuthoredLayoutIntentPriority.PREFERENCE)
    }

    private fun ProjectSemanticDeclaration.toConstraintSubject(
        sourceUnitId: SourceUnitId,
        viewFamily: String,
        span: SourceSpan,
    ): LayoutConstraintSubject {
        val name = qualifiedAuthoredName.single()
        return LayoutConstraintSubject(
            intentId = LayoutIntentId("intent:m23:$viewFamily:$name"),
            subjectId = StableSemanticIdentity(declarationId.value),
            occurrenceId = LayoutOccurrenceId("occurrence:m23:$viewFamily:$name"),
            viewId = viewFamily,
            sourceSpan = LayoutSourceSpan(
                sourceUnitId = sourceUnitId.value,
                startLine = span.start.line,
                startColumn = span.start.column,
                endLine = span.end.line,
                endColumn = span.end.column,
            ),
        )
    }

    private fun LayoutStatement.relationToken(): String {
        return when (this) {
            is LayoutStatement.PlaceNear -> "near"
            is LayoutStatement.PlaceBelow -> "below"
            is LayoutStatement.AlignWith -> "aligned-with"
            is LayoutStatement.GroupWith -> "grouped-with"
        }
    }

    private fun SyntaxLayoutAxis.toConstraintAxis(): LayoutAxis {
        return when (this) {
            SyntaxLayoutAxis.Horizontal -> LayoutAxis.HORIZONTAL
            SyntaxLayoutAxis.Vertical -> LayoutAxis.VERTICAL
        }
    }

    private data class NamespaceNameKey(
        val namespaceId: NamespaceId,
        val name: String,
    )
}
