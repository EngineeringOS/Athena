package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.DrawingGridPosition
import com.engineeringood.athena.layout.LayoutAxis
import com.engineeringood.athena.layout.LayoutConstraint
import com.engineeringood.athena.layout.LayoutConstraintId
import com.engineeringood.athena.layout.LayoutConstraintSubject
import com.engineeringood.athena.layout.LayoutConstraintSnapshot
import com.engineeringood.athena.layout.LayoutIntentId
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutOrientation
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
            .filter { declaration -> declaration.kind == "device" || declaration.kind == "function" }
            .associateBy { declaration ->
                NamespaceNameKey(declaration.namespaceId, declaration.qualifiedAuthoredName.joinToString("."))
            }
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
                            val subjectName = statement.subjectName()
                            val subject = declarationsByNamespaceAndName[
                                NamespaceNameKey(namespace.namespaceId, subjectName),
                            ] ?: return@forEach
                            val physicalSubject = if (subject.kind == "function") {
                                declarationsByNamespaceAndName[
                                    NamespaceNameKey(namespace.namespaceId, subject.qualifiedAuthoredName.first()),
                                ] ?: return@forEach
                            } else {
                                subject
                            }
                            val target = statement.targetName()?.let { targetName ->
                                declarationsByNamespaceAndName[
                                    NamespaceNameKey(namespace.namespaceId, targetName),
                                ] ?: return@forEach
                            }
                            constraints += statement.toConstraint(
                                sourceUnitId = sourceUnit.sourceUnitId,
                                viewFamily = declaration.viewFamily,
                                subject = ResolvedLayoutSubject(
                                    physicalDeclaration = physicalSubject,
                                    functionDeclaration = subject.takeIf { candidate -> candidate.kind == "function" },
                                ),
                                target = target,
                            )
                        }
                    }
            }

        return LayoutConstraintSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:layout-constraints:${snapshot.graphId.value}"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            constraints = constraints,
        )
    }

    private fun LayoutStatement.toConstraint(
        sourceUnitId: SourceUnitId,
        viewFamily: String,
        subject: ResolvedLayoutSubject,
        target: ProjectSemanticDeclaration?,
    ): LayoutConstraint {
        val subjectName = subject.authoredName
        val targetName = target?.qualifiedAuthoredName?.joinToString(".").orEmpty()
        val subjectRef = subject.toConstraintSubject(sourceUnitId, viewFamily, span)
        val targetRef = target?.let { declaration ->
            ResolvedLayoutSubject(declaration).toConstraintSubject(sourceUnitId, viewFamily, span)
        }
        val constraintId = LayoutConstraintId(
            "constraint:$viewFamily:${relationToken()}:$subjectName:$targetName:${span.start.offset}",
        )
        return when (this) {
            is LayoutStatement.PlaceNear -> LayoutConstraint.near(constraintId, subjectRef, requireNotNull(targetRef))
            is LayoutStatement.PlaceBelow -> LayoutConstraint.below(constraintId, subjectRef, requireNotNull(targetRef))
            is LayoutStatement.AlignWith -> LayoutConstraint.alignedWith(
                constraintId = constraintId,
                subject = subjectRef,
                target = requireNotNull(targetRef),
                axis = axis.toConstraintAxis(),
            )
            is LayoutStatement.GroupWith -> LayoutConstraint.groupedWith(constraintId, subjectRef, requireNotNull(targetRef))
            is LayoutStatement.PlaceAt -> LayoutConstraint.atGrid(
                constraintId = constraintId,
                subject = subjectRef,
                position = DrawingGridPosition(position.column, position.row),
                orientation = when (orientation) {
                    com.engineeringood.athena.language.LayoutOrientation.Horizontal -> LayoutOrientation.HORIZONTAL
                    com.engineeringood.athena.language.LayoutOrientation.Vertical -> LayoutOrientation.VERTICAL
                },
            )
        }.let { constraint ->
            if (this is LayoutStatement.PlaceAt) constraint else constraint.copy(
                authoredPriority = AuthoredLayoutIntentPriority.PREFERENCE,
            )
        }
    }

    private fun ResolvedLayoutSubject.toConstraintSubject(
        sourceUnitId: SourceUnitId,
        viewFamily: String,
        span: SourceSpan,
    ): LayoutConstraintSubject {
        val name = authoredName
        return LayoutConstraintSubject(
            intentId = LayoutIntentId("intent:layout:$viewFamily:$name"),
            subjectId = StableSemanticIdentity(physicalDeclaration.declarationId.value),
            occurrenceId = LayoutOccurrenceId("occurrence:layout:$viewFamily:$name"),
            viewId = viewFamily,
            sourceSpan = LayoutSourceSpan(
                sourceUnitId = sourceUnitId.value,
                startLine = span.start.line,
                startColumn = span.start.column,
                endLine = span.end.line,
                endColumn = span.end.column,
            ),
            functionId = functionDeclaration?.declarationId?.value?.let(::StableSemanticIdentity),
        )
    }

    private fun LayoutStatement.relationToken(): String {
        return when (this) {
            is LayoutStatement.PlaceNear -> "near"
            is LayoutStatement.PlaceBelow -> "below"
            is LayoutStatement.AlignWith -> "aligned-with"
            is LayoutStatement.GroupWith -> "grouped-with"
            is LayoutStatement.PlaceAt -> "at"
        }
    }

    private fun LayoutStatement.subjectName(): String = when (this) {
        is LayoutStatement.PlaceAt -> subject.parts.joinToString(".")
        is LayoutStatement.PlaceNear -> subject
        is LayoutStatement.PlaceBelow -> subject
        is LayoutStatement.AlignWith -> subject
        is LayoutStatement.GroupWith -> subject
    }

    private fun LayoutStatement.targetName(): String? = when (this) {
        is LayoutStatement.PlaceAt -> null
        is LayoutStatement.PlaceNear -> target
        is LayoutStatement.PlaceBelow -> target
        is LayoutStatement.AlignWith -> target
        is LayoutStatement.GroupWith -> target
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

    private data class ResolvedLayoutSubject(
        val physicalDeclaration: ProjectSemanticDeclaration,
        val functionDeclaration: ProjectSemanticDeclaration? = null,
    ) {
        val authoredName: String = functionDeclaration?.qualifiedAuthoredName
            ?.joinToString(".")
            ?: physicalDeclaration.qualifiedAuthoredName.joinToString(".")
    }
}
