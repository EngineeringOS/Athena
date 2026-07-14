package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess

/**
 * Runtime-owned facade that resolves M14 component knowledge for the active canonical project state.
 *
 * The service reuses the compiler-owned knowledge context. It does not let frontend surfaces or the
 * runtime layer re-resolve component knowledge independently.
 */
class AthenaComponentKnowledgeRuntimeService {
    /** Publishes the active session component-knowledge snapshot for [context]. */
    fun inspect(context: AthenaExecutionContext): AthenaComponentKnowledgeRuntimeResult {
        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> AthenaComponentKnowledgeUnavailable(
                projectName = context.project.name,
                reason = "Component knowledge is unavailable because `${context.project.sourcePath}` does not compile: " +
                    compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )

            is CompilerCompilationSuccess -> inspectCompiledProject(context, compilation)
        }
    }

    private fun inspectCompiledProject(
        context: AthenaExecutionContext,
        compilation: CompilerCompilationSuccess,
    ): AthenaComponentKnowledgeReady {
        val implementationsBySubjectId = compilation.knowledgeContext.resolvedImplementations.associateBy { resolved ->
            resolved.semanticSubjectId.value
        }
        val availableComponents = context.pluginRuntimeServices()
            .componentKnowledgeContributions()
            .flatMap { contribution ->
                contribution.componentKnowledge.engineeringConcepts.map { concept ->
                    concept to contribution.componentKnowledge.partImplementations.filter { implementation ->
                        implementation.conceptId == concept.conceptId
                    }
                }
            }
            .groupBy(
                keySelector = { (concept, _) -> concept.conceptId.value },
                valueTransform = { (concept, implementations) -> concept to implementations },
            )
            .values
            .map { entries ->
                val concept = entries.map { (candidate, _) -> candidate }
                    .distinctBy { candidate -> candidate.conceptId.value }
                    .sortedWith(compareBy(
                        { candidate -> candidate.conceptId.value },
                        { candidate -> candidate.displayName },
                    ))
                    .first()
                val implementations = entries
                    .flatMap { (_, implementations) -> implementations }
                    .distinctBy { implementation -> implementation.implementationId.value }
                    .sortedWith(compareBy(
                        { implementation -> implementation.vendorId.value },
                        { implementation -> implementation.vendorPartNumber.value },
                        { implementation -> implementation.implementationId.value },
                    ))
                AthenaAvailableAuthoringComponent(
                    concept = concept,
                    implementations = implementations,
                )
            }
            .sortedWith(compareBy(
                { entry -> entry.concept.conceptId.value },
                { entry -> entry.concept.displayName },
            ))

        return AthenaComponentKnowledgeReady(
            projectName = context.project.name,
            systemSemanticId = compilation.document.system.id.value,
            contributingPluginIds = compilation.knowledgeContext.componentKnowledgeContributors,
            activeConceptCount = compilation.knowledgeContext.activeComponentConceptCount,
            activeImplementationCount = compilation.knowledgeContext.activeComponentImplementationCount,
            availableComponents = availableComponents,
            components = compilation.knowledgeContext.resolvedComponents.map { resolvedComponent ->
                AthenaResolvedComponentKnowledgeEntry(
                    resolvedComponent = resolvedComponent,
                    resolvedImplementation = implementationsBySubjectId[resolvedComponent.semanticSubjectId.value],
                )
            },
            semanticPorts = compilation.knowledgeContext.resolvedSemanticPorts,
            physicalTraits = compilation.knowledgeContext.resolvedPhysicalTraits,
            diagnostics = compilation.knowledgeContext.componentKnowledgeDiagnostics,
        )
    }
}
