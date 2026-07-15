package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration

class ProjectSemanticDeclarationIndexer {
    fun index(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val namespacesBySourceUnit = snapshot.namespaces
            .flatMap { namespace -> namespace.sourceUnitIds.map { it to namespace } }
            .toMap()
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        val declarations = snapshot.sourceUnits.flatMap { sourceUnit ->
            val namespace = namespacesBySourceUnit[sourceUnit.sourceUnitId] ?: return@flatMap emptyList()
            sourceUnit.authoredDeclarations.mapNotNull { authoredDeclaration ->
                authoredDeclaration.toSemanticDeclaration(sourceUnit.sourceUnitId, namespace.namespaceId)
            }
        }
        val uniqueDeclarations = declarations
            .groupBy { it.declarationId }
            .toSortedMap(compareBy { it.value })
            .flatMap { (_, duplicates) ->
                val ordered = duplicates.sortedWith(declarationDuplicateComparator)
                ordered.drop(1).forEach { duplicate ->
                    diagnostics += ProjectSemanticDiagnostic(
                        code = ProjectSemanticDiagnosticCode("semantic.declaration.duplicate"),
                        severity = ProjectSemanticDiagnosticSeverity.ERROR,
                        message = "Duplicate authored ${duplicate.kind} declaration `${duplicate.qualifiedAuthoredName.joinToString(".")}`.",
                        sourceUnitId = duplicate.sourceUnitId,
                        sourceSpan = duplicate.authoredSpan,
                    )
                }
                ordered.take(1)
            }
        uniqueDeclarations
            .groupBy { SemanticAvailabilityKey(it.namespaceId, it.kind, it.qualifiedAuthoredName) }
            .toSortedMap(semanticAvailabilityKeyComparator)
            .forEach { (_, declarationsForName) ->
                val ordered = declarationsForName.sortedWith(declarationDuplicateComparator)
                ordered.drop(1).forEach { ambiguous ->
                    diagnostics += ProjectSemanticDiagnostic(
                        code = ProjectSemanticDiagnosticCode("semantic.declaration.ambiguous"),
                        severity = ProjectSemanticDiagnosticSeverity.ERROR,
                        message = "Ambiguous authored ${ambiguous.kind} declaration `${ambiguous.qualifiedAuthoredName.joinToString(".")}`.",
                        sourceUnitId = ambiguous.sourceUnitId,
                        sourceSpan = ambiguous.authoredSpan,
                    )
                }
            }
        val declarationIdsByNamespace = uniqueDeclarations
            .groupBy { it.namespaceId }
            .mapValues { (_, namespaceDeclarations) ->
                namespaceDeclarations.map { it.declarationId }.sortedBy { it.value }
            }
        val indexedNamespaces = snapshot.namespaces.map { namespace ->
            namespace.copy(declarationIds = declarationIdsByNamespace[namespace.namespaceId].orEmpty())
        }
        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            snapshot.sourceUnits,
            indexedNamespaces,
            uniqueDeclarations,
            snapshot.bindings,
            snapshot.diagnostics + diagnostics,
        )
    }

    private fun Declaration.toSemanticDeclaration(
        sourceUnitId: SourceUnitId,
        namespaceId: NamespaceId,
    ): ProjectSemanticDeclaration? {
        val (kind, qualifiedName) = when (this) {
            is DeviceDeclaration -> "device" to listOf(name)
            is PortDeclaration -> "port" to qualifiedName.parts
            else -> return null
        }
        return ProjectSemanticDeclaration(
            declarationId = CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, kind, qualifiedName),
            namespaceId = namespaceId,
            sourceUnitId = sourceUnitId,
            kind = kind,
            qualifiedAuthoredName = qualifiedName,
            authoredSpan = span,
        )
    }

    private companion object {
        private data class SemanticAvailabilityKey(
            val namespaceId: NamespaceId,
            val kind: String,
            val qualifiedAuthoredName: List<String>,
        )

        private val semanticAvailabilityKeyComparator = compareBy<SemanticAvailabilityKey>(
            { it.namespaceId.value },
            { it.kind },
            { it.qualifiedAuthoredName.joinToString(".") },
        )

        private val declarationDuplicateComparator = compareBy<ProjectSemanticDeclaration>(
            { it.sourceUnitId.value },
            { it.authoredSpan.start.offset },
            { it.authoredSpan.start.line },
            { it.authoredSpan.start.column },
            { it.authoredSpan.end.offset },
            { it.authoredSpan.end.line },
            { it.authoredSpan.end.column },
        )
    }
}
