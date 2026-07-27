package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.SourceSpan

/**
 * Binds M23 authored layout-hint references against compiler-owned semantic declarations.
 *
 * This pass resolves only names that already exist in the semantic declaration index. It does not
 * lower constraints, infer engineering meaning, or consult frontend/parser-generator state.
 */
class ProjectSemanticLayoutHintBinder {
    fun bind(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val namespacesBySourceUnit = snapshot.namespaces
            .flatMap { namespace -> namespace.sourceUnitIds.map { sourceUnitId -> sourceUnitId to namespace } }
            .toMap()
        val declarationsByNamespaceAndName = snapshot.declarations
            .filter { declaration -> declaration.kind == "device" || declaration.kind == "function" }
            .groupBy { declaration ->
                NamespaceNameKey(declaration.namespaceId, declaration.qualifiedAuthoredName.joinToString("."))
            }
            .mapValues { (_, declarations) -> declarations.sortedBy { declaration -> declaration.declarationId.value } }

        val bindings = mutableListOf<ProjectSemanticBinding>()
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        snapshot.sourceUnits
            .sortedBy { sourceUnit -> sourceUnit.sourceUnitId.value }
            .forEach { sourceUnit ->
                val namespace = namespacesBySourceUnit[sourceUnit.sourceUnitId] ?: return@forEach
                sourceUnit.authoredDeclarations
                    .filterIsInstance<LayoutDeclaration>()
                    .sortedBy { declaration -> declaration.span.start.offset }
                    .forEach { declaration ->
                        diagnostics += declaration.conflictDiagnostics(sourceUnit.sourceUnitId)
                        declaration.layoutReferences().forEach { reference ->
                            val declarationCandidates = declarationsByNamespaceAndName[
                                NamespaceNameKey(namespace.namespaceId, reference.name),
                            ].orEmpty()
                            val resolvedDeclaration = declarationCandidates.singleOrNull()
                            if (resolvedDeclaration == null) {
                                diagnostics += ProjectSemanticDiagnostic(
                                    code = ProjectSemanticDiagnosticCode("semantic.layout.reference.unknown"),
                                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                                    message = "Unknown layout reference `${reference.name}` in `${declaration.viewFamily}`.",
                                    sourceUnitId = sourceUnit.sourceUnitId,
                                    sourceSpan = reference.span,
                                )
                            } else {
                                bindings += ProjectSemanticBinding(
                                    bindingId = CanonicalSemanticIdentityBuilder.bindingId(
                                        sourceUnit.sourceUnitId,
                                        reference.span,
                                        resolvedDeclaration.declarationId,
                                    ),
                                    sourceUnitId = sourceUnit.sourceUnitId,
                                    referenceSpan = reference.span,
                                    resolvedDeclarationId = resolvedDeclaration.declarationId,
                                )
                            }
                        }
                    }
            }

        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            snapshot.sourceUnits,
            snapshot.namespaces,
            snapshot.declarations,
            snapshot.bindings + bindings.distinctBy { binding -> binding.bindingId },
            snapshot.diagnostics + diagnostics,
        )
    }

    private fun LayoutDeclaration.layoutReferences(): List<LayoutReference> {
        return statements.flatMap { statement ->
            when (statement) {
                is LayoutStatement.PlaceAt -> listOf(
                    LayoutReference(statement.subject.parts.joinToString("."), statement.subject.span),
                )
                is LayoutStatement.PlaceNear -> listOf(
                    LayoutReference(statement.subject, statement.span),
                    LayoutReference(statement.target, statement.span),
                )
                is LayoutStatement.PlaceBelow -> listOf(
                    LayoutReference(statement.subject, statement.span),
                    LayoutReference(statement.target, statement.span),
                )
                is LayoutStatement.AlignWith -> listOf(
                    LayoutReference(statement.subject, statement.span),
                    LayoutReference(statement.target, statement.span),
                )
                is LayoutStatement.GroupWith -> listOf(
                    LayoutReference(statement.subject, statement.span),
                    LayoutReference(statement.target, statement.span),
                )
            }
        }
    }

    private fun LayoutDeclaration.conflictDiagnostics(sourceUnitId: SourceUnitId): List<ProjectSemanticDiagnostic> {
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        diagnostics += hardPlacementDiagnostics(sourceUnitId)
        val relativeStatements = statements.filterNot { statement -> statement is LayoutStatement.PlaceAt }
        relativeStatements
            .map { statement -> statement.toHintKey() }
            .groupBy { key -> listOf(key.subject, key.relation, key.target, key.axis.orEmpty()) }
            .forEach { (key, duplicates) ->
                duplicates.drop(1).forEach { duplicate ->
                    diagnostics += ProjectSemanticDiagnostic(
                        code = ProjectSemanticDiagnosticCode("semantic.layout.hint.duplicate"),
                        severity = ProjectSemanticDiagnosticSeverity.WARNING,
                        message = "Duplicate layout hint `${key[1]}` for `${key[0]}` -> `${key[2]}` in `$viewFamily` at priority PREFERENCE.",
                        sourceUnitId = sourceUnitId,
                        sourceSpan = duplicate.span,
                    )
                }
            }

        relativeStatements
            .map { statement -> statement.toHintKey() }
            .groupBy { key -> key.subject to key.target }
            .forEach { (_, hintsForPair) ->
                val distinct = hintsForPair
                    .filter { key -> key.relation in PLACEMENT_RELATIONS }
                    .distinctBy { key -> key.relation }
                if (distinct.size > 1) {
                    val first = distinct.minBy { key -> key.span.start.offset }
                    diagnostics += ProjectSemanticDiagnostic(
                        code = ProjectSemanticDiagnosticCode("semantic.layout.hint.contradictory"),
                        severity = ProjectSemanticDiagnosticSeverity.WARNING,
                        message = "Contradictory layout hints for `${first.subject}` -> `${first.target}` in `$viewFamily`: ${
                            distinct.joinToString(", ") { key -> key.relation }
                        } at priority PREFERENCE.",
                        sourceUnitId = sourceUnitId,
                        sourceSpan = first.span,
                    )
                }
                hintsForPair
                    .filter { key -> key.relation == "aligned-with" }
                    .groupBy { key -> key.subject to key.target }
                    .forEach { (_, alignHints) ->
                        val alignAxes = alignHints.distinctBy { key -> key.axis.orEmpty() }
                        if (alignAxes.size > 1) {
                            val first = alignAxes.minBy { key -> key.span.start.offset }
                            diagnostics += ProjectSemanticDiagnostic(
                                code = ProjectSemanticDiagnosticCode("semantic.layout.hint.contradictory"),
                                severity = ProjectSemanticDiagnosticSeverity.WARNING,
                                message = "Contradictory layout hints for `${first.subject}` -> `${first.target}` in `$viewFamily`: ${
                                    alignAxes.joinToString(", ") { key -> "aligned-with axis ${key.axis}" }
                                } at priority PREFERENCE.",
                                sourceUnitId = sourceUnitId,
                                sourceSpan = first.span,
                            )
                        }
                    }
            }
        return diagnostics
    }

    private fun LayoutStatement.toHintKey(): LayoutHintKey {
        val (subjectText, targetText) = when (this) {
            is LayoutStatement.PlaceAt -> subject.parts.joinToString(".") to subject.parts.joinToString(".")
            is LayoutStatement.PlaceNear -> subject to target
            is LayoutStatement.PlaceBelow -> subject to target
            is LayoutStatement.AlignWith -> subject to target
            is LayoutStatement.GroupWith -> subject to target
        }
        return LayoutHintKey(subjectText, relationText(), targetText, axisText(), span)
    }

    private fun LayoutStatement.relationText(): String {
        return when (this) {
            is LayoutStatement.PlaceNear -> "near"
            is LayoutStatement.PlaceBelow -> "below"
            is LayoutStatement.AlignWith -> "aligned-with"
            is LayoutStatement.GroupWith -> "grouped-with"
            is LayoutStatement.PlaceAt -> "at"
        }
    }

    private fun LayoutStatement.axisText(): String? {
        return when (this) {
            is LayoutStatement.AlignWith -> axis.name.lowercase()
            is LayoutStatement.PlaceNear,
            is LayoutStatement.PlaceBelow,
            is LayoutStatement.GroupWith,
                -> null
            is LayoutStatement.PlaceAt -> orientation.name.lowercase()
        }
    }

    private fun LayoutDeclaration.hardPlacementDiagnostics(
        sourceUnitId: SourceUnitId,
    ): List<ProjectSemanticDiagnostic> {
        val placements = statements.filterIsInstance<LayoutStatement.PlaceAt>()
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        placements.groupBy { placement -> placement.subject.parts.joinToString(".") }
            .forEach { (subject, authored) ->
                val ordered = authored.sortedBy { placement -> placement.span.start.offset }
                ordered.drop(1).forEach { duplicate ->
                    val first = ordered.first()
                    val samePlacement = first.position.column == duplicate.position.column &&
                        first.position.row == duplicate.position.row &&
                        first.orientation == duplicate.orientation
                    diagnostics += ProjectSemanticDiagnostic(
                        code = ProjectSemanticDiagnosticCode(
                            if (samePlacement) "semantic.layout.placement.duplicate" else "semantic.layout.placement.conflicting",
                        ),
                        severity = ProjectSemanticDiagnosticSeverity.ERROR,
                        message = if (samePlacement) {
                            "Duplicate hard drawing placement for `$subject` in `$viewFamily`."
                        } else {
                            "Conflicting hard drawing placements for `$subject` in `$viewFamily`."
                        },
                        sourceUnitId = sourceUnitId,
                        sourceSpan = duplicate.span,
                    )
                }
            }
        placements.groupBy { placement -> placement.position.column to placement.position.row }
            .values
            .filter { cell -> cell.map { placement -> placement.subject.parts }.distinct().size > 1 }
            .forEach { cell ->
                cell.sortedBy { placement -> placement.span.start.offset }
                    .distinctBy { placement -> placement.subject.parts }
                    .drop(1)
                    .forEach { collision ->
                        diagnostics += ProjectSemanticDiagnostic(
                            code = ProjectSemanticDiagnosticCode("semantic.layout.placement.cell-collision"),
                            severity = ProjectSemanticDiagnosticSeverity.ERROR,
                            message = "Drawing grid cell (${collision.position.column}, ${collision.position.row}) is assigned to multiple subjects in `$viewFamily`.",
                            sourceUnitId = sourceUnitId,
                            sourceSpan = collision.span,
                        )
                    }
            }

        val fixedBySubject = placements.groupBy { placement -> placement.subject.parts.joinToString(".") }
            .mapNotNull { (subject, authored) -> authored.singleOrNull()?.let { subject to it } }
            .toMap()
        statements.filterNot { statement -> statement is LayoutStatement.PlaceAt }.forEach { statement ->
            val key = statement.toHintKey()
            val subject = fixedBySubject[key.subject] ?: return@forEach
            val target = fixedBySubject[key.target] ?: return@forEach
            val conflicts = when (statement) {
                is LayoutStatement.PlaceBelow -> subject.position.row <= target.position.row
                is LayoutStatement.AlignWith -> when (statement.axis) {
                    com.engineeringood.athena.language.LayoutAxis.Horizontal -> subject.position.row != target.position.row
                    com.engineeringood.athena.language.LayoutAxis.Vertical -> subject.position.column != target.position.column
                }
                is LayoutStatement.PlaceNear,
                is LayoutStatement.GroupWith,
                    -> false
                is LayoutStatement.PlaceAt -> false
            }
            if (conflicts) {
                diagnostics += ProjectSemanticDiagnostic(
                    code = ProjectSemanticDiagnosticCode("semantic.layout.placement.relative-conflict"),
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Hard drawing placement conflicts with `${key.relation}` for `${key.subject}` -> `${key.target}` in `$viewFamily`.",
                    sourceUnitId = sourceUnitId,
                    sourceSpan = statement.span,
                )
            }
        }
        return diagnostics
    }

    private data class NamespaceNameKey(
        val namespaceId: NamespaceId,
        val name: String,
    )

    private data class LayoutReference(
        val name: String,
        val span: SourceSpan,
    )

    private data class LayoutHintKey(
        val subject: String,
        val relation: String,
        val target: String,
        val axis: String?,
        val span: SourceSpan,
    )

    private companion object {
        private val PLACEMENT_RELATIONS = setOf("near", "below")
    }
}
