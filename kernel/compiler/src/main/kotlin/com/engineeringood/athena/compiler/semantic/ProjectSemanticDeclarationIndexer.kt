package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.RelationDeclaration

class ProjectSemanticDeclarationIndexer {
    fun index(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val namespacesBySourceUnit = snapshot.namespaces
            .flatMap { namespace -> namespace.sourceUnitIds.map { it to namespace } }
            .toMap()
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        val declarations = snapshot.sourceUnits.flatMap { sourceUnit ->
            val namespace = namespacesBySourceUnit[sourceUnit.sourceUnitId] ?: return@flatMap emptyList()
            sourceUnit.authoredDeclarations.flatMap { authoredDeclaration ->
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
                        code = ProjectSemanticDiagnosticCode(
                            if (duplicate.kind == CONNECTION_DECLARATION_KIND) {
                                "semantic.connection.alias.duplicate"
                            } else {
                                "semantic.declaration.duplicate"
                            },
                        ),
                        severity = ProjectSemanticDiagnosticSeverity.ERROR,
                        message = if (duplicate.kind == CONNECTION_DECLARATION_KIND) {
                            "Duplicate authored connection alias `${duplicate.qualifiedAuthoredName.single()}`."
                        } else {
                            "Duplicate authored ${duplicate.kind} declaration `${duplicate.qualifiedAuthoredName.joinToString(".")}`."
                        },
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
    ): List<ProjectSemanticDeclaration> {
        val semanticDeclarations = when (this) {
            is DeviceDeclaration -> listOf(
                "device" to listOf(name) to span,
            ) + nestedPorts.map { port -> "port" to port.qualifiedName.parts to port.span } +
                interfaces.flatMap { connectivityInterface ->
                    connectivityInterface.ports.map { port ->
                        "port" to listOf(name, port.name) to port.span
                    }
                } +
                nestedFunctions.map { function -> "function" to listOf(name, function.name) to function.span }
            is PortDeclaration -> listOf("port" to qualifiedName.parts to span)
            is ConnectionDeclaration -> listOf(CONNECTION_DECLARATION_KIND to listOf(alias) to aliasSpan)
            is ConnectionGroupDeclaration -> connections.map { connection ->
                CONNECTION_DECLARATION_KIND to listOf(connection.alias) to connection.aliasSpan
            }
            is RelationDeclaration -> targets.map { target ->
                CONNECTION_DECLARATION_KIND to listOf(relationMemberAlias(word.value, from, target)) to word.span
            }
            else -> return emptyList()
        }
        return semanticDeclarations.map { (kindAndName, authoredSpan) ->
            val (kind, qualifiedName) = kindAndName
            ProjectSemanticDeclaration(
                declarationId = CanonicalSemanticIdentityBuilder.declarationId(sourceUnitId, kind, qualifiedName),
                namespaceId = namespaceId,
                sourceUnitId = sourceUnitId,
                kind = kind,
                qualifiedAuthoredName = qualifiedName,
                authoredSpan = authoredSpan,
            )
        }
    }

    private companion object {
        private const val CONNECTION_DECLARATION_KIND = "connection"

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

private fun relationMemberAlias(relationWord: String, from: QualifiedName, target: QualifiedName): String =
    "${relationWord}_${from.parts.joinToString("_")}_to_${target.parts.joinToString("_")}"
