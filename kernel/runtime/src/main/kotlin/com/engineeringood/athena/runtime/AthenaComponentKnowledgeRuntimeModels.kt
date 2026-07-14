package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeDiagnostic
import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.component.ResolvedComponentDefinition
import com.engineeringood.athena.connection.ResolvedSemanticPortDefinition
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.part.ResolvedPartImplementation
import com.engineeringood.athena.physical.ResolvedPhysicalTraitDefinition

/** Runtime-owned result of one component-knowledge inspection request. */
sealed interface AthenaComponentKnowledgeRuntimeResult {
    /** Active project name associated with the inspection result. */
    val projectName: String
}

/** Read-only resolved component knowledge entry for one canonical semantic subject. */
data class AthenaResolvedComponentKnowledgeEntry(
    val resolvedComponent: ResolvedComponentDefinition,
    val resolvedImplementation: ResolvedPartImplementation?,
)

/** Read-only available authoring component derived from active component-knowledge contributions. */
data class AthenaAvailableAuthoringComponent(
    val concept: EngineeringConceptDefinition,
    val implementations: List<PartImplementationDefinition>,
)

/** Successful runtime-owned component-knowledge snapshot for one active project session. */
data class AthenaComponentKnowledgeReady(
    override val projectName: String,
    val systemSemanticId: String,
    val contributingPluginIds: List<String>,
    val activeConceptCount: Int,
    val activeImplementationCount: Int,
    val availableComponents: List<AthenaAvailableAuthoringComponent>,
    val components: List<AthenaResolvedComponentKnowledgeEntry>,
    val semanticPorts: List<ResolvedSemanticPortDefinition>,
    val physicalTraits: List<ResolvedPhysicalTraitDefinition>,
    val diagnostics: List<AthenaComponentKnowledgeDiagnostic>,
) : AthenaComponentKnowledgeRuntimeResult

/** Explicit unavailable result when Athena cannot build a usable component-knowledge snapshot. */
data class AthenaComponentKnowledgeUnavailable(
    override val projectName: String,
    val reason: String,
) : AthenaComponentKnowledgeRuntimeResult
