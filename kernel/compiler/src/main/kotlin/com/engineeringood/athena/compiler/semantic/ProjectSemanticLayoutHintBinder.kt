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
            .filter { declaration -> declaration.kind == "device" && declaration.qualifiedAuthoredName.size == 1 }
            .groupBy { declaration ->
                NamespaceNameKey(declaration.namespaceId, declaration.qualifiedAuthoredName.single())
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
            listOf(
                LayoutReference(statement.subject, statement.span),
                LayoutReference(statement.target, statement.span),
            )
        }
    }

    private fun LayoutDeclaration.conflictDiagnostics(sourceUnitId: SourceUnitId): List<ProjectSemanticDiagnostic> {
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        statements
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

        statements
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
        return LayoutHintKey(
            subject = subject,
            relation = relationText(),
            target = target,
            axis = axisText(),
            span = span,
        )
    }

    private fun LayoutStatement.relationText(): String {
        return when (this) {
            is LayoutStatement.PlaceNear -> "near"
            is LayoutStatement.PlaceBelow -> "below"
            is LayoutStatement.AlignWith -> "aligned-with"
            is LayoutStatement.GroupWith -> "grouped-with"
        }
    }

    private fun LayoutStatement.axisText(): String? {
        return when (this) {
            is LayoutStatement.AlignWith -> axis.name.lowercase()
            is LayoutStatement.PlaceNear,
            is LayoutStatement.PlaceBelow,
            is LayoutStatement.GroupWith,
                -> null
        }
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
