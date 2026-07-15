package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.QualifiedName

class ProjectSemanticReferenceLinker {
    fun link(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val namespacesBySourceUnit = snapshot.namespaces
            .flatMap { namespace -> namespace.sourceUnitIds.map { it to namespace } }
            .toMap()
        val declarationsById = snapshot.declarations.associateBy { it.declarationId }
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        val bindings = snapshot.sourceUnits.flatMap { sourceUnit ->
            val namespace = namespacesBySourceUnit[sourceUnit.sourceUnitId] ?: return@flatMap emptyList()
            val availableDeclarations = namespace.declarationIds
                .mapNotNull(declarationsById::get)
                .filter { it.kind == PORT_DECLARATION_KIND }
            sourceUnit.authoredDeclarations
                .filterIsInstance<ConnectionDeclaration>()
                .flatMap { connection ->
                    listOf(connection.from, connection.to).mapNotNull { reference ->
                        linkReference(sourceUnit.sourceUnitId, reference, availableDeclarations, diagnostics)
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
            (snapshot.bindings + bindings).distinctBy { it.bindingId },
            snapshot.diagnostics + diagnostics,
        )
    }

    private fun linkReference(
        sourceUnitId: SourceUnitId,
        reference: QualifiedName,
        availableDeclarations: List<ProjectSemanticDeclaration>,
        diagnostics: MutableList<ProjectSemanticDiagnostic>,
    ): ProjectSemanticBinding? {
        val candidates = availableDeclarations
            .filter { it.qualifiedAuthoredName == reference.parts }
            .sortedBy { it.declarationId.value }
        return when (candidates.size) {
            0 -> {
                diagnostics += ProjectSemanticDiagnostic(
                    code = ProjectSemanticDiagnosticCode("semantic.reference.unresolved"),
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Unresolved authored reference `${reference.parts.joinToString(".")}`.",
                    sourceUnitId = sourceUnitId,
                    sourceSpan = reference.span,
                )
                null
            }

            1 -> {
                val declarationId = candidates.single().declarationId
                ProjectSemanticBinding(
                    bindingId = CanonicalSemanticIdentityBuilder.bindingId(sourceUnitId, reference.span, declarationId),
                    sourceUnitId = sourceUnitId,
                    referenceSpan = reference.span,
                    resolvedDeclarationId = declarationId,
                )
            }

            else -> {
                diagnostics += ProjectSemanticDiagnostic(
                    code = ProjectSemanticDiagnosticCode("semantic.reference.ambiguous"),
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Ambiguous authored reference `${reference.parts.joinToString(".")}`.",
                    sourceUnitId = sourceUnitId,
                    sourceSpan = reference.span,
                    relatedLocations = candidates.map { candidate ->
                        ProjectSemanticRelatedLocation(
                            sourceUnitId = candidate.sourceUnitId,
                            sourceSpan = candidate.authoredSpan,
                            message = "Candidate ${candidate.kind} declaration.",
                        )
                    },
                )
                null
            }
        }
    }

    private companion object {
        private const val PORT_DECLARATION_KIND = "port"
    }
}
