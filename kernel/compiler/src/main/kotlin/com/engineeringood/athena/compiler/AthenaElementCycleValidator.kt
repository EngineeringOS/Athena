package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementDeclaration

internal object AthenaElementCycleValidator {
    fun validate(
        declarations: List<AuthoredRepresentationDeclaration>,
        identities: List<RepresentationIdentityOccurrence>,
    ): List<AthenaRepresentationSourceDiagnostic> {
        val elements = identities.filter { it.declaration is ElementDeclaration }
        val byKey = elements.associateBy { DefinitionKey(it.libraryId, it.identity) }
        val authoredByDeclaration = declarations.associateBy(AuthoredRepresentationDeclaration::declaration)
        val graph = elements.associate { element ->
            val edges = (element.declaration as ElementDeclaration).children.mapNotNull { child ->
                val identity = child.symbolIdentity ?: return@mapNotNull null
                val target = byKey[DefinitionKey(element.libraryId, identity.value)] ?: return@mapNotNull null
                CompositionEdge(DefinitionKey(target.libraryId, target.identity), identity.span)
            }
            DefinitionKey(element.libraryId, element.identity) to edges
        }
        val emitted = mutableSetOf<String>()
        val diagnostics = mutableListOf<AthenaRepresentationSourceDiagnostic>()
        graph.keys.sortedWith(compareBy({ it.libraryId.value }, DefinitionKey::identity)).forEach { root ->
            val cycle = findCycle(root, root, graph, mutableListOf(root), mutableSetOf(root)) ?: return@forEach
            val canonicalKey = cycle.path.dropLast(1).map { it.libraryId.value + ":" + it.identity }.sorted().joinToString("|")
            if (!emitted.add(canonicalKey)) return@forEach
            val rootOccurrence = requireNotNull(byKey[root])
            diagnostics += issue(
                "element.composition.cycle",
                requireNotNull(authoredByDeclaration[requireNotNull(byKey[cycle.closingTarget]).declaration]).file,
                cycle.closingSpan,
                "${rootOccurrence.declaration.subjectPrefix()}.composition",
                "Element composition cycle: ${cycle.path.joinToString(" -> ") { it.identity }}.",
            )
        }
        return diagnostics
    }

    private fun findCycle(
        root: DefinitionKey,
        current: DefinitionKey,
        graph: Map<DefinitionKey, List<CompositionEdge>>,
        path: MutableList<DefinitionKey>,
        active: MutableSet<DefinitionKey>,
    ): CompositionCycle? {
        graph[current].orEmpty().sortedBy { it.target.identity }.forEach { edge ->
            if (edge.target == root) {
                return CompositionCycle(path + root, edge.span, edge.target)
            }
            if (active.add(edge.target)) {
                path += edge.target
                val found = findCycle(root, edge.target, graph, path, active)
                if (found != null) return found
                path.removeAt(path.lastIndex)
                active.remove(edge.target)
            }
        }
        return null
    }

    private data class CompositionEdge(
        val target: DefinitionKey,
        val span: com.engineeringood.athena.language.SourceSpan,
    )

    private data class CompositionCycle(
        val path: List<DefinitionKey>,
        val closingSpan: com.engineeringood.athena.language.SourceSpan,
        val closingTarget: DefinitionKey,
    )
}
