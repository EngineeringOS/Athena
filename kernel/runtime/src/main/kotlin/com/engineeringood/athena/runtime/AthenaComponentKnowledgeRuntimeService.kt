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

        return AthenaComponentKnowledgeReady(
            projectName = context.project.name,
            contributingPluginIds = compilation.knowledgeContext.componentKnowledgeContributors,
            activeConceptCount = compilation.knowledgeContext.activeComponentConceptCount,
            activeImplementationCount = compilation.knowledgeContext.activeComponentImplementationCount,
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
