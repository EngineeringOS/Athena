package com.engineeringood.athena.plugin

import com.engineeringood.athena.component.EngineeringConceptDefinition
import com.engineeringood.athena.connection.ResolvedSemanticPortDefinition
import com.engineeringood.athena.part.PartImplementationDefinition
import com.engineeringood.athena.physical.ResolvedPhysicalTraitDefinition

/**
 * Plugin-owned read-only component knowledge published above canonical `Engineering IR`.
 *
 * The contribution can describe vendor-neutral concepts, vendor implementations, and narrow
 * resolved semantic-port or physical-trait slices without opening a second mutation path.
 */
data class AthenaComponentKnowledgeContribution(
    val engineeringConcepts: List<EngineeringConceptDefinition> = emptyList(),
    val partImplementations: List<PartImplementationDefinition> = emptyList(),
    val semanticPorts: List<ResolvedSemanticPortDefinition> = emptyList(),
    val physicalTraits: List<ResolvedPhysicalTraitDefinition> = emptyList(),
) {
    companion object {
        /** Empty contribution used when a plugin does not participate in M14 component knowledge. */
        val EMPTY: AthenaComponentKnowledgeContribution = AthenaComponentKnowledgeContribution()
    }
}

/**
 * Typed contract for plugins that publish resolved component knowledge for downstream runtime and IDE consumers.
 */
interface AthenaComponentKnowledgeContributor : AthenaPlugin {
    /** Returns the plugin-owned component knowledge contribution in deterministic plugin-owned order. */
    fun componentKnowledge(): AthenaComponentKnowledgeContribution = AthenaComponentKnowledgeContribution.EMPTY
}
