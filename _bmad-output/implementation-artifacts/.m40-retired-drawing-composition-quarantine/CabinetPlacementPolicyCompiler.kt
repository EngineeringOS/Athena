package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.LayoutGraph
import com.engineeringood.athena.physical.PhysicalConstraintEvaluation
import com.engineeringood.athena.physical.PhysicalConstraintEvaluator
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalMountTargetRef

data class CabinetPlacementPolicyRequest(
    val layoutGraph: LayoutGraph,
    val physicalIr: PhysicalInstallationIR,
    val placements: List<CabinetPlacementFact>,
)

data class CabinetPlacementPolicyEvidence(
    val snapshotId: String,
    val placementCount: Int,
    val diagnosticCount: Int,
    val plannerIds: List<String>,
)

data class CabinetPlacementPolicyDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

sealed interface CabinetPlacementPolicyCompilation {
    data class Success(
        val placements: List<CabinetPlacementFact>,
        val evidence: CabinetPlacementPolicyEvidence,
    ) : CabinetPlacementPolicyCompilation

    data class Failure(val diagnostics: List<CabinetPlacementPolicyDiagnostic>) : CabinetPlacementPolicyCompilation
}

object CabinetPlacementPolicyCompiler {
    fun evaluate(request: CabinetPlacementPolicyRequest): CabinetPlacementPolicyCompilation {
        val diagnostics = mutableListOf<CabinetPlacementPolicyDiagnostic>()
        val layoutKeys = request.layoutGraph.occurrences.map { occurrence -> occurrence.semanticSubjectId }.sorted()
        val placementKeys = request.placements.map { placement -> placement.key.canonicalSemanticSubjectId.value }.sorted()
        val physicalKeys = request.physicalIr.space.mountedOccurrences.map { occurrence -> occurrence.semanticSubjectId.value }.sorted()
        if (layoutKeys != placementKeys || layoutKeys != physicalKeys) {
            diagnostics += diagnostic(
                code = "cabinet.placement.policy.occurrence.mismatch",
                subject = request.layoutGraph.snapshotId.value,
                message = "Validated placements must match the layout graph and physical occurrence set.",
            )
        }

        if (request.placements.map { placement -> placement.key }.distinct().size != request.placements.size) {
            diagnostics += diagnostic(
                code = "cabinet.placement.policy.duplicate",
                subject = request.layoutGraph.snapshotId.value,
                message = "Validated placements must be unique by installation key.",
            )
        }

        val proposedByOccurrenceId = request.placements.associateBy { it.proposedPhysicalOccurrence.occurrenceId }
        val proposedIr = request.physicalIr.copy(
            space = request.physicalIr.space.copy(
                mountedOccurrences = request.physicalIr.space.mountedOccurrences.map { occurrence ->
                    val proposal = proposedByOccurrenceId[occurrence.occurrenceId]?.proposedPhysicalOccurrence
                        ?: return@map occurrence
                    occurrence.copy(
                        target = request.physicalIr.targetReference(proposal.targetId) ?: occurrence.target,
                        at = com.engineeringood.athena.physical.PhysicalPoint2i(
                            proposal.targetLocalPosition.x.toInt(),
                            proposal.targetLocalPosition.y.toInt(),
                        ),
                    )
                },
            ),
        )
        when (val physicalEvaluation = PhysicalConstraintEvaluator.evaluate(proposedIr)) {
            is PhysicalConstraintEvaluation.Success -> Unit
            is PhysicalConstraintEvaluation.Failure -> {
                diagnostics += physicalEvaluation.diagnostics.map { issue ->
                    diagnostic(
                        code = "cabinet.placement.policy.${issue.code}",
                        subject = issue.subject,
                        message = buildString {
                            append(issue.expected)
                            issue.measured?.let { append(" measured=").append(it) }
                        },
                    )
                }
            }
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetPlacementPolicyCompilation.Failure(
                diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
            )
        }

        val orderedPlacements = request.placements.sortedBy { placement -> placement.key.canonicalSemanticSubjectId.value }
        return CabinetPlacementPolicyCompilation.Success(
            placements = orderedPlacements,
            evidence = CabinetPlacementPolicyEvidence(
                snapshotId = request.layoutGraph.snapshotId.value,
                placementCount = orderedPlacements.size,
                diagnosticCount = 0,
                plannerIds = orderedPlacements.map { placement -> placement.plannerId }.distinct(),
            ),
        )
    }
}

private fun diagnostic(code: String, subject: String, message: String): CabinetPlacementPolicyDiagnostic =
    CabinetPlacementPolicyDiagnostic(code = code, subject = subject, message = message)

private fun PhysicalInstallationIR.targetReference(
    targetId: com.engineeringood.athena.physical.PhysicalObjectId,
): PhysicalMountTargetRef? = when {
    space.surfaces.any { it.id == targetId } -> PhysicalMountTargetRef.Surface(targetId)
    space.rails.any { it.id == targetId } -> PhysicalMountTargetRef.Rail(targetId)
    space.terminalGroups.any { it.id == targetId } -> PhysicalMountTargetRef.TerminalGroup(targetId)
    else -> null
}
