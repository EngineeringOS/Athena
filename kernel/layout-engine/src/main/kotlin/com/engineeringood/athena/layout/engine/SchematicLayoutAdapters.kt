package com.engineeringood.athena.layout.engine

import com.engineeringood.athena.layout.LayoutConstraintId
import com.engineeringood.athena.layout.LayoutIntentSnapshot

/**
 * Isolated local helper boundary for the M22 ELK-style spike.
 */
interface SchematicLayoutHelperAdapter {
    val helperId: LayoutHelperAdapterId

    fun propose(input: SchematicLayoutOptimizationInput): SchematicLayoutHelperProposal?
}

/**
 * Local experimental ELK-style adapter. It introduces no external dependency and only emits helper
 * proposals that must be normalized by Athena before use.
 */
class ExperimentalElkSchematicLayoutAdapter(
    private val enabled: Boolean = true,
    private val strategy: SchematicLayoutStrategy = RuleBasedSchematicLayoutStrategy(),
) : SchematicLayoutHelperAdapter {
    override val helperId: LayoutHelperAdapterId = LayoutHelperAdapterId("helper:experimental-elk")

    override fun propose(input: SchematicLayoutOptimizationInput): SchematicLayoutHelperProposal? {
        if (!enabled) {
            return null
        }
        require(input.constraintSnapshot.snapshotId == input.intentSnapshot.snapshotId) {
            "Experimental layout adapter input must target the active layout snapshot."
        }
        require(input.constraintSnapshot.family == input.intentSnapshot.family) {
            "Experimental layout adapter input must preserve the layout family."
        }
        val canonicalIntent = LayoutIntentSnapshot.canonical(
            snapshotId = input.intentSnapshot.snapshotId,
            family = input.intentSnapshot.family,
            items = input.intentSnapshot.items,
            relationshipConstraints = input.intentSnapshot.relationshipConstraints,
        )
        val solved = strategy.solve(canonicalIntent)
        return SchematicLayoutHelperProposal(
            helperId = helperId,
            snapshotId = solved.snapshotId,
            placementFacts = solved.placementFacts,
            notes = input.constraintSnapshot.constraints
                .map { constraint -> constraint.constraintId.value }
                .sorted(),
        )
    }
}

/**
 * Optimizer wrapper that normalizes local helper output back into Athena layout facts.
 */
class ExperimentalElkSchematicLayoutOptimizer(
    private val adapter: SchematicLayoutHelperAdapter = ExperimentalElkSchematicLayoutAdapter(),
    private val fallback: SchematicLayoutOptimizer = RuleBasedSchematicLayoutOptimizer(),
    private val normalizer: SchematicLayoutHelperNormalizer = SchematicLayoutHelperNormalizer(),
) : SchematicLayoutOptimizer {
    override fun optimize(input: SchematicLayoutOptimizationInput): SchematicLayoutOptimizationResult {
        val proposal = adapter.propose(input) ?: return fallback.optimize(input)
        val normalized = normalizer.normalize(input.intentSnapshot, proposal)
        return SchematicLayoutOptimizationResult(
            snapshotId = normalized.snapshotId,
            family = normalized.family,
            placementFacts = normalized.placementFacts,
            regionFacts = normalized.regionFacts,
            appliedConstraintIds = input.constraintSnapshot.constraints
                .map { constraint -> constraint.constraintId }
                .sortedBy(LayoutConstraintId::value),
            helperId = proposal.helperId,
        )
    }
}
