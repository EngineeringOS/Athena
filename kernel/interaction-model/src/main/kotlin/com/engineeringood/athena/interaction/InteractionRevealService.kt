package com.engineeringood.athena.interaction

object InteractionRevealService {
    fun reveal(
        registry: SemanticCapabilityRegistry,
        request: InteractionRevealRequest,
    ): InteractionRevealResult {
        val subject = runCatching { registry.requireSubject(request.subject) }.getOrElse {
            return InteractionRevealResult(
                subject = request.subject,
                targets = emptyList(),
                diagnostics = listOf(missingTarget(request.subject, null)),
                partial = true,
            )
        }

        val targets = request.preferredTargets.mapNotNull { target ->
            revealTarget(subject, target)
        }
        val missingDiagnostics = request.preferredTargets
            .filter { target -> targets.none { reveal -> reveal.target == target } }
            .map { target -> missingTarget(request.subject, target) }

        return InteractionRevealResult(
            subject = request.subject,
            targets = targets,
            diagnostics = missingDiagnostics,
            partial = missingDiagnostics.isNotEmpty(),
        )
    }

    private fun revealTarget(
        subject: InteractionSubject,
        target: InteractionRevealSurface,
    ): InteractionRevealTarget? {
        return when (target) {
            InteractionRevealSurface.SOURCE -> subject.sourceRange?.let { sourceRange ->
                InteractionRevealTarget(target = target, sourceRange = sourceRange)
            }
            InteractionRevealSurface.GRAPH -> subject.occurrences.firstOrNull()?.let { occurrence ->
                InteractionRevealTarget(target = target, occurrence = occurrence)
            }
            InteractionRevealSurface.INSPECTOR -> InteractionRevealTarget(target = target)
            InteractionRevealSurface.PROBLEMS -> subject.diagnosticId?.let { diagnosticId ->
                InteractionRevealTarget(target = target, diagnosticId = diagnosticId)
            }
        }
    }

    private fun missingTarget(
        subject: InteractionSubjectKey,
        target: InteractionRevealSurface?,
    ): InteractionDiagnostic {
        val suffix = target?.let { " for $it" }.orEmpty()
        return InteractionDiagnostic(
            code = InteractionDiagnosticCode.REVEAL_MISSING_TARGET,
            severity = InteractionDiagnosticSeverity.WARNING,
            message = "Interaction reveal target is not available$suffix.",
            subject = subject,
            retryable = false,
        )
    }
}
